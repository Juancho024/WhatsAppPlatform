package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.InventarioUniversalResponseDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import com.jrdev.whatsappplatform.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VentasBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;
    private final SessionService sessionService;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "VENTAS".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().trim();
        String instanceName = instancia.getInstanceName();
        String phoneNumber = contacto.getRemotejid().replace("@s.whatsapp.net", "");
        String respuesta = "";

        // 1. Buscamos el estado actual en Redis
        String estadoActual = sessionService.getUserState(instanceName, phoneNumber);

        switch (estadoActual) {
            case "NUEVO_CHAT":
                respuesta = "¡Hola " + contacto.getNombre() + "! Bienvenido al departamento de Ventas. 🛒\n\n" +
                        "Responde con el *NÚMERO* de la opción que deseas:\n" +
                        "1️⃣ Ver catálogo completo\n" +
                        "2️⃣ Consultar stock de productos\n" +
                        "3️⃣ Registrar una venta 🛍️\n" +
                        "4️⃣ Salir";
                sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_OPCION");
                break;

            case "ESPERANDO_OPCION":
                if (texto.equals("1")) {
                    respuesta = "Puedes ver nuestro catálogo completo y comprar aquí: " + integracion.getBaseUrl();
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("2")) {
                    evolutionClient.enviarMensaje(instanceName, phoneNumber, "Consultando nuestro inventario en tiempo real... ⏳");
                    respuesta = consultarInventario(integracion);
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("3")) {
                    respuesta = "📦 *Iniciando proceso de venta*\n\nPor favor, escribe el *ID o Código* del producto que deseas comprar:";
                    sessionService.saveUserState(instanceName, phoneNumber, "VENTA_ESPERANDO_ID_PRODUCTO");

                } else if (texto.equals("4")) {
                    respuesta = "¡Gracias por contactarnos! Escríbenos cuando nos necesites.";
                    sessionService.clearSession(instanceName, phoneNumber);

                } else {
                    respuesta = "❌ Opción no válida. Por favor responde con 1, 2, 3 o 4.";
                }
                break;

            // --- SUB-FLUJO DE VENTAS GUIADO ---
            case "VENTA_ESPERANDO_ID_PRODUCTO":
                sessionService.saveUserState(instanceName, phoneNumber, "VENTA_ESPERANDO_CANTIDAD:" + texto);
                respuesta = "🔢 Has seleccionado el producto ID: *" + texto + "*. ¿Qué cantidad deseas llevar?";
                break;

            case String s when s.startsWith("VENTA_ESPERANDO_CANTIDAD:"):
                String idProducto = s.split(":")[1];
                try {
                    int cantidad = Integer.parseInt(texto);
                    if (cantidad <= 0) {
                        respuesta = "⚠️ La cantidad debe ser mayor a 0. Intenta de nuevo:";
                        break;
                    }
                    sessionService.saveUserState(instanceName, phoneNumber, "VENTA_ESPERANDO_PAGO:" + idProducto + ":" + cantidad);
                    respuesta = "💳 Selecciona el *método de pago*:\n1️⃣ EFECTIVO\n2️⃣ TARJETA\n3️⃣ TRANSFERENCIA";
                } catch (NumberFormatException e) {
                    respuesta = "❌ Por favor, introduce un número válido para la cantidad:";
                }
                break;

            case String s when s.startsWith("VENTA_ESPERANDO_PAGO:"):
                String[] partesPago = s.split(":");
                String prodIdPago = partesPago[1];
                int cantPago = Integer.parseInt(partesPago[2]);

                String metodoPago = switch (texto) {
                    case "2", "tarjeta", "TARJETA" -> "TARJETA";
                    case "3", "transferencia", "TRANSFERENCIA" -> "TRANSFERENCIA";
                    default -> "EFECTIVO";
                };

                // Consultamos los datos previos para armar el RESUMEN DE CONFIRMACIÓN antes de facturar
                respuesta = generarResumenYPedirConfirmacion(integracion, prodIdPago, cantPago, metodoPago, instanceName, phoneNumber);
                break;

            // --- ESTADO: ESPERANDO CONFIRMACIÓN FINAL (SÍ / NO) ---
            case String s when s.startsWith("VENTA_ESPERANDO_CONFIRMACION:"):
                String[] partesConf = s.split(":");
                String prodIdConf = partesConf[1];
                int cantConf = Integer.parseInt(partesConf[2]);
                String metodoConf = partesConf[3];

                String respuestaLower = texto.toLowerCase();
                if (respuestaLower.equals("sí") || respuestaLower.equals("si") || respuestaLower.equals("1") || respuestaLower.equals("confirmar")) {
                    evolutionClient.enviarMensaje(instanceName, phoneNumber, "Generando factura y descontando inventario... ⏳");
                    respuesta = procesarVentaConIdReal(integracion, prodIdConf, cantConf, metodoConf);
                } else {
                    respuesta = "❌ Operación cancelada. La compra no se ha realizado. Escribe cualquier cosa si deseas volver a empezar.";
                }
                sessionService.clearSession(instanceName, phoneNumber);
                break;

            default:
                sessionService.clearSession(instanceName, phoneNumber);
                respuesta = "Tu sesión ha expirado o hubo un error. Escribe cualquier cosa para volver a empezar.";
                break;
        }

        if (!respuesta.isEmpty()) {
            evolutionClient.enviarMensaje(instanceName, phoneNumber, respuesta);
        }
    }

    // Método para consultar inventario
    private String consultarInventario(Integracion integracion) {
        try {
            String urlConsulta = integracion.getBaseUrl();
            Map<String, Object> configuracion = (Map<String, Object>) integracion.getConfiguration();
            String token = (configuracion != null && configuracion.containsKey("api_token"))
                    ? configuracion.get("api_token").toString() : "";

            InventarioUniversalResponseDto respuestaExterna = restClient.get()
                    .uri(urlConsulta)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token)
                    .retrieve()
                    .body(InventarioUniversalResponseDto.class);

            if (respuestaExterna != null && respuestaExterna.isExito()) {
                if (respuestaExterna.getDatos() == null || respuestaExterna.getDatos().isEmpty()) {
                    return "No encontré productos disponibles en este momento.";
                } else {
                    StringBuilder sb = new StringBuilder("✅ *Inventario Actualizado:*\n\n");
                    for (InventarioUniversalResponseDto.ProductoItem item : respuestaExterna.getDatos()) {
                        sb.append("🆔 Código/ID: *").append(item.getId()).append("*\n");
                        sb.append("📦 Producto: *").append(item.getNombre()).append("*\n");
                        sb.append("💵 Precio: $").append(item.getPrecio()).append("\n");
                        sb.append("📦 Stock: ").append(item.getCantidad()).append(" unidades\n\n");
                    }
                    return sb.toString();
                }
            } else {
                return "El sistema reportó error al consultar.";
            }
        } catch (Exception e) {
            return "En este momento el sistema de inventario está en mantenimiento.";
        }
    }

    // --- MÉTODO AUXILIAR PARA RESUMEN (TODO EN MINÚSCULAS) ---
    private String generarResumenYPedirConfirmacion(Integracion integracion, String idProductoStr, int cantidad, String metodoPago, String instanceName, String phoneNumber) {
        try {
            String rawUrl = integracion.getBaseUrl();
            if (rawUrl.contains("/rest/v1")) {
                rawUrl = rawUrl.substring(0, rawUrl.indexOf("/rest/v1"));
            }
            String baseUrl = rawUrl;

            Map<String, Object> configuracion = (Map<String, Object>) integracion.getConfiguration();
            String token = (configuracion != null && configuracion.containsKey("api_token"))
                    ? configuracion.get("api_token").toString() : "";

            // URL con columnas en minúsculas exactas
            String productoUrl = baseUrl + "/rest/v1/productos?idproducto=eq." + idProductoStr + "&select=idproducto,nombre,precioventa";

            List<Map<String, Object>> productosEncontrados = restClient.get()
                    .uri(productoUrl)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (productosEncontrados == null || productosEncontrados.isEmpty()) {
                sessionService.clearSession(instanceName, phoneNumber);
                return "❌ No encontré ningún producto registrado con el Código/ID: *" + idProductoStr + "*. Operación cancelada.";
            }

            Map<String, Object> prodData = productosEncontrados.get(0);
            String nombreProducto = prodData.get("nombre").toString();
            double precioUnitario = Double.parseDouble(prodData.get("precioventa").toString());

            double subtotal = precioUnitario * cantidad;
            double itbis = subtotal * 0.18;
            double total = subtotal + itbis;

            sessionService.saveUserState(instanceName, phoneNumber, "VENTA_ESPERANDO_CONFIRMACION:" + idProductoStr + ":" + cantidad + ":" + metodoPago);

            return "📋 *Resumen de tu Compra*\n\n" +
                    "📦 *Producto:* " + nombreProducto + " (ID: " + idProductoStr + ")\n" +
                    "🔢 *Cantidad:* " + cantidad + " unidades\n" +
                    "💵 *Precio Unitario:* $" + precioUnitario + "\n" +
                    "📊 *Subtotal:* $" + String.format("%.2f", subtotal) + "\n" +
                    "🧾 *ITBIS (18%):* $" + String.format("%.2f", itbis) + "\n" +
                    "💰 *Total a Pagar:* *" + String.format("%.2f", total) + "*\n" +
                    "💳 *Método de Pago:* " + metodoPago + "\n\n" +
                    "¿Deseas confirmar y procesar esta venta?\n" +
                    "👉 Responde *SÍ* para confirmar o *NO* para cancelar.";

        } catch (Exception e) {
            System.err.println("❌ ERROR REAL EN RESUMEN DE VENTA: " + e.getMessage());
            e.printStackTrace();
            sessionService.clearSession(instanceName, phoneNumber);
            return "⚠️ Ocurrió un error al consultar el producto para el resumen.";
        }
    }

    // --- PROCESAR VENTA REAL EN SUPABASE (TODO EN MINÚSCULAS) ---
    private String procesarVentaConIdReal(Integracion integracion, String idProductoStr, int cantidad, String metodoPago) {
        try {
            String rawUrl = integracion.getBaseUrl();
            if (rawUrl.contains("/rest/v1")) {
                rawUrl = rawUrl.substring(0, rawUrl.indexOf("/rest/v1"));
            }
            String baseUrl = rawUrl;

            Map<String, Object> configuracion = (Map<String, Object>) integracion.getConfiguration();
            String token = (configuracion != null && configuracion.containsKey("api_token"))
                    ? configuracion.get("api_token").toString() : "";

            // URL con columnas en minúsculas exactas
            String productoUrl = baseUrl + "/rest/v1/productos?idproducto=eq." + idProductoStr + "&select=idproducto,nombre,precioventa";

            List<Map<String, Object>> productosEncontrados = restClient.get()
                    .uri(productoUrl)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (productosEncontrados == null || productosEncontrados.isEmpty()) {
                return "❌ Producto no encontrado al momento de facturar.";
            }

            Map<String, Object> prodData = productosEncontrados.get(0);
            String nombreProducto = prodData.get("nombre").toString();
            double precioUnitario = Double.parseDouble(prodData.get("precioventa").toString());
            long idProductoReal = Long.parseLong(prodData.get("idproducto").toString());

            double subtotal = precioUnitario * cantidad;
            double itbis = subtotal * 0.18;
            double total = subtotal + itbis;

            Map<String, Object> facturaParams = new HashMap<>();
            facturaParams.put("p_ncf", "B0100000001");
            facturaParams.put("p_tipoComprobante", "CREDITO_FISCAL");
            facturaParams.put("p_subtotal", subtotal);
            facturaParams.put("p_itbis", itbis);
            facturaParams.put("p_total", total);
            facturaParams.put("p_metodoPago", metodoPago);
            facturaParams.put("p_codigoVenta", "V-" + System.currentTimeMillis());
            facturaParams.put("p_idcliente", 1L);
            facturaParams.put("p_idempleado", 1L);

            Long idVenta = restClient.post()
                    .uri(baseUrl + "/rest/v1/rpc/generarfacturaventa")
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token)
                    .header("Content-Type", "application/json")
                    .body(facturaParams)
                    .retrieve()
                    .body(Long.class);

            if (idVenta != null && idVenta > 0) {
                Map<String, Object> detalleParams = new HashMap<>();
                detalleParams.put("p_descuento", 0.00);
                detalleParams.put("p_cantidad", cantidad);
                detalleParams.put("p_impuesto", itbis);
                detalleParams.put("p_observacion", "Venta automatizada vía Bot WhatsApp");
                detalleParams.put("p_preciounitario", precioUnitario);
                detalleParams.put("p_subtotal", subtotal);
                detalleParams.put("p_idProducto", idProductoReal);
                detalleParams.put("p_idgarantia", null);
                detalleParams.put("p_idventa", idVenta);
                detalleParams.put("p_idarticulo", null);

                restClient.post()
                        .uri(baseUrl + "/rest/v1/rpc/registrardetalleventa")
                        .header("Authorization", "Bearer " + token)
                        .header("apikey", token)
                        .header("Content-Type", "application/json")
                        .body(detalleParams)
                        .retrieve()
                        .toBodilessEntity();

                return "🎉 *¡Venta Exitosa Registrada!* 🛒\n\n" +
                        "🧾 *Detalles de la Factura*\n" +
                        "🆔 *Código de Venta / Factura:* #" + idVenta + "\n" +
                        "🏷️ *Código/ID del Producto:* " + idProductoReal + "\n" +
                        "📦 *Producto:* " + nombreProducto + "\n" +
                        "🔢 *Cantidad:* " + cantidad + " unidades\n" +
                        "💵 *Precio Unitario:* $" + precioUnitario + "\n" +
                        "💰 *Total a Pagar:* $" + String.format("%.2f", total) + "\n" +
                        "💳 *Método de Pago:* " + metodoPago + "\n\n" +
                        "¡El stock y los registros se actualizaron automáticamente en MusicStock!";
            } else {
                return "❌ El sistema no pudo generar la factura en Supabase.";
            }

        } catch (Exception e) {
            System.err.println("Error en venta por ID: " + e.getMessage());
            e.printStackTrace();
            return "⚠️ Ocurrió un error al procesar la venta en la base de datos.";
        }
    }
}