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

            // --- SUB-FLUJO DE VENTAS GUIADO POR ID ---
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
                String[] partes = s.split(":");
                String prodId = partes[1];
                int cant = Integer.parseInt(partes[2]);

                String metodoPago = "EFECTIVO";
                if (texto.equals("2") || texto.equalsIgnoreCase("tarjeta")) {
                    metodoPago = "TARJETA";
                } else if (texto.equals("3") || texto.equalsIgnoreCase("transferencia")) {
                    metodoPago = "TRANSFERENCIA";
                }

                evolutionClient.enviarMensaje(instanceName, phoneNumber, "Validando producto y procesando factura... ⏳");

                // Ejecutamos la venta buscando los datos reales en Supabase por ID
                respuesta = procesarVentaConIdReal(integracion, prodId, cant, metodoPago);
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

    // Método para consultar inventario mostrando los IDs claramente
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

    // --- PROCESAR VENTA BUSCANDO EL PRODUCTO REAL POR ID Y MOSTRANDO CÓDIGOS AL FINAL ---
    private String procesarVentaConIdReal(Integracion integracion, String idProductoStr, int cantidad, String metodoPago) {
        try {
            String baseUrl = integracion.getBaseUrl().replace("/rpc/api_whatsapp_inventario", "");
            Map<String, Object> configuracion = (Map<String, Object>) integracion.getConfiguration();
            String token = (configuracion != null && configuracion.containsKey("api_token"))
                    ? configuracion.get("api_token").toString() : "";

            // 1. Consultamos el producto en Supabase usando su ID exacto para obtener su precio y nombre real
            String productoUrl = baseUrl + "/rest/v1/productos?idProducto=eq." + idProductoStr + "&select=idProducto,nombre,precioVenta";

            List<Map<String, Object>> productosEncontrados = restClient.get()
                    .uri(productoUrl)
                    .header("Authorization", "Bearer " + token)
                    .header("apikey", token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (productosEncontrados == null || productosEncontrados.isEmpty()) {
                return "❌ No encontré ningún producto registrado con el Código/ID: *" + idProductoStr + "*. Verifica el catálogo e intenta de nuevo.";
            }

            Map<String, Object> prodData = productosEncontrados.get(0);
            String nombreProducto = prodData.get("nombre").toString();
            double precioUnitario = Double.parseDouble(prodData.get("precioVenta").toString());
            long idProductoReal = Long.parseLong(prodData.get("idProducto").toString());

            double subtotal = precioUnitario * cantidad;
            double itbis = subtotal * 0.18; // 18% ITBIS o impuesto estándar
            double total = subtotal + itbis;

            // 2. Generamos la factura llamando a la función RPC de Supabase
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
                // 3. Registramos el detalle de la venta (esto dispara tu trigger de inventario automáticamente)
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

                // 4. Retornamos el mensaje detallado mostrando todos los códigos e IDs al usuario
                return "🎉 *¡Venta Exitosa Registrada!* 🛒\n\n" +
                        "🧾 *Detalles de la Factura*\n" +
                        "🆔 *Código de Venta / Factura:* #" + idVenta + "\n" +
                        "🏷️ *Código/ID del Producto:* " + idProductoReal + "\n" +
                        "📦 *Producto:* " + nombreProducto + "\n" +
                        "🔢 *Cantidad:* " + cantidad + " unidades\n" +
                        "💵 *Precio Unitario:* $" + precioUnitario + "\n" +
                        "💰 *Total Pagado:* $" + String.format("%.2f", total) + "\n" +
                        "💳 *Método de Pago:* " + metodoPago + "\n\n" +
                        "¡El stock y los registros se actualizaron automáticamente en MusicStock!";
            } else {
                return "❌ El sistema no pudo generar la factura en Supabase.";
            }

        } catch (Exception e) {
            System.err.println("Error en venta por ID: " + e.getMessage());
            return "⚠️ Ocurrió un error al procesar la venta. Asegúrate de digitar un ID de producto válido.";
        }
    }
}