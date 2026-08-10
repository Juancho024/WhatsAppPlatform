package com.jrdev.whatsappplatform.service.bot;

import com.jrdev.whatsappplatform.dto.InventarioUniversalResponseDto;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class VentasBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "VENTAS".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().toLowerCase();
        String respuesta;

        if (texto.contains("catalogo") || texto.contains("comprar")) {
            respuesta = "¡Hola " + contacto.getNombre() + "! 🛒 Puedes ver nuestro catálogo completo aquí: " + integracion.getBaseUrl();

        } else if (texto.contains("stock") || texto.contains("buscar")) {
            try {
                // Endpoint estándar que le exigiremos al cliente
                String urlConsulta = integracion.getBaseUrl() + "/api/v1/whatsapp/inventario";

                InventarioUniversalResponseDto respuestaExterna = restClient.get()
                        .uri(urlConsulta)
                        .retrieve()
                        .body(InventarioUniversalResponseDto.class);

                if (respuestaExterna != null && respuestaExterna.isExito()) {
                    if (respuestaExterna.getDatos() == null || respuestaExterna.getDatos().isEmpty()) {
                        respuesta = "No encontré productos disponibles en este momento.";
                    } else {
                        StringBuilder sb = new StringBuilder("✅ *Inventario Actualizado:*\n\n");
                        for (InventarioUniversalResponseDto.ProductoItem item : respuestaExterna.getDatos()) {
                            sb.append("📦 *").append(item.getNombre()).append("*\n");
                            sb.append("💵 Precio: $").append(item.getPrecio()).append("\n");
                            sb.append("📊 Estado: ").append(item.isDisponible() ? "En Stock" : "Agotado").append("\n\n");
                        }
                        respuesta = sb.toString();
                    }
                } else {
                    respuesta = "El sistema de inventario reportó: " + (respuestaExterna != null ? respuestaExterna.getMensaje() : "Error desconocido.");
                }
            } catch (Exception e) {
                System.err.println("Error consultando la API de ventas: " + e.getMessage());
                respuesta = "En este momento nuestro sistema de inventario está en mantenimiento. Intenta en unos minutos.";
            }
        } else {
            respuesta = "Bienvenido al departamento de Ventas. Escribe 'catálogo' para ver la tienda o 'stock' para revisar disponibilidad de productos.";
        }

        evolutionClient.enviarMensaje(
                instancia.getInstanceName(),
                contacto.getRemotejid().replace("@s.whatsapp.net", ""),
                respuesta
        );
    }
}