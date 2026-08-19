package com.jrdev.whatsappplatform.service.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jrdev.whatsappplatform.model.*;
import com.jrdev.whatsappplatform.repository.ChatRepository;
import com.jrdev.whatsappplatform.repository.MensajeRepository;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import com.jrdev.whatsappplatform.service.SessionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemManagerBotStrategy implements BotStrategy {

    private final EvolutionClient evolutionClient;
    private final RestClient restClient;
    private final SessionService sessionService;

    // 🔥 1. INYECTAMOS TUS REPOSITORIOS PARA LA BASE DE DATOS
    private final ChatRepository chatRepo;
    private final MensajeRepository mensajeRepo;

    @Override
    public boolean soporta(String tipoIntegracion) {
        return "MANTENIMIENTO".equalsIgnoreCase(tipoIntegracion);
    }

    @Override
    public void procesarMensaje(WhatsappInstancia instancia, Contacto contacto, Mensaje mensaje, Integracion integracion) {
        String texto = mensaje.getContenido().trim();
        String instanceName = instancia.getInstanceName();
        String phoneNumber = contacto.getRemotejid().replace("@s.whatsapp.net", "");
        String respuesta = "";

        String estadoActual = sessionService.getUserState(instanceName, phoneNumber);

        switch (estadoActual) {
            case "NUEVO_CHAT":
                respuesta = "🏢 *Panel de SystemManager* 🏢\n\n" +
                        "¡Hola " + contacto.getNombre() + "! Bienvenido a la administración del residencial.\n\n" +
                        "Elige una opción:\n" +
                        "1️⃣ Ver todos los propietarios\n" +
                        "2️⃣ Ver propietarios con deudas (Rojo) 🔴\n" +
                        "3️⃣ Ver reportes financieros\n" +
                        "4️⃣ Ver logs del sistema 💻\n" +
                        "5️⃣ Salir";
                sessionService.saveUserState(instanceName, phoneNumber, "ESPERANDO_OPCION_MANTENIMIENTO");
                break;

            case "ESPERANDO_OPCION_MANTENIMIENTO":
                if (texto.equals("1")) {
                    // 🔥 2. USAMOS TU LÓGICA DE GUARDAR Y ENVIAR (Reemplaza al evolutionClient directo)
                    enviarYGuardarMensaje(instancia, contacto, "Consultando la lista de propietarios... ⏳");
                    respuesta = consultarPropietarios(integracion, false);
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("2")) {
                    enviarYGuardarMensaje(instancia, contacto, "Filtrando propietarios con deudas pendientes... ⏳");
                    respuesta = consultarPropietarios(integracion, true);
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("3")) {
                    enviarYGuardarMensaje(instancia, contacto, "Obteniendo los últimos reportes financieros... ⏳");
                    respuesta = consultarReportesFinancieros(integracion);
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("4")) {
                    enviarYGuardarMensaje(instancia, contacto, "Extrayendo el registro de actividad del sistema... ⏳");
                    respuesta = consultarLogsActividad(integracion);
                    sessionService.clearSession(instanceName, phoneNumber);

                } else if (texto.equals("5")) {
                    respuesta = "¡Sesión cerrada! Escríbeme cuando necesites revisar el residencial de nuevo. 👋";
                    sessionService.clearSession(instanceName, phoneNumber);

                } else {
                    respuesta = "❌ Opción no válida. Por favor responde con un número del 1 al 5.";
                }
                break;

            default:
                sessionService.clearSession(instanceName, phoneNumber);
                respuesta = "Tu sesión ha expirado. Escribe cualquier cosa para volver a ver el menú.";
                break;
        }

        if (!respuesta.isEmpty()) {
            // 🔥 Y AQUÍ GUARDAMOS LA RESPUESTA FINAL DEL BOT
            enviarYGuardarMensaje(instancia, contacto, respuesta);
        }
    }

    // ==========================================
    // 🔥 TU LÓGICA MÁGICA DE GUARDADO EN BD
    // ==========================================
    private void enviarYGuardarMensaje(WhatsappInstancia instancia, Contacto contacto, String textoMensaje) {
        String phoneNumber = contacto.getRemotejid().replace("@s.whatsapp.net", "");

        // 1. ENVIAR FÍSICAMENTE POR EVOLUTION API
        evolutionClient.enviarMensaje(instancia.getInstanceName(), phoneNumber, textoMensaje);

        try {
            // 2. BUSCAR O CREAR CHAT
            Chat chat = chatRepo.buscarPorInstanciaYContacto(instancia.getIdWhatsappInstancia(), contacto.getIdContacto()).orElse(null);

            if (chat == null) {
                chat = new Chat();
                chat.setIdWhatsAppInstancia(instancia.getIdWhatsappInstancia());
                chat.setIdContacto(contacto.getIdContacto());
                chat.setRemotejid(contacto.getRemotejid());
                chat.setEstado("ABIERTO");
                chat.setUnread_count(0);
                chat.setUltima_actividad(OffsetDateTime.now());

                Long idChat = chatRepo.crear(chat);
                chat.setIdChat(idChat);
            }

            // 3. GUARDAR EL MENSAJE COMO SALIENTE EN LA BD
            Mensaje msjSaliente = new Mensaje();
            msjSaliente.setIdChat(chat.getIdChat());
            msjSaliente.setContenido(textoMensaje);
            msjSaliente.setTipo("text");
            msjSaliente.setEnviadoPorNosotros(true);
            msjSaliente.setDireccion("OUTGOING");
            msjSaliente.setEstado("ENVIADO");
            msjSaliente.setFechaMensaje(OffsetDateTime.now());

            mensajeRepo.crear(msjSaliente);

            // 4. ACTUALIZAR ÚLTIMA ACTIVIDAD DEL CHAT PARA QUE SUBA EN LA LISTA
            chatRepo.actualizarUltimaActividad(chat.getIdChat());

        } catch (Exception e) {
            System.err.println("❌ Error guardando el mensaje del bot en la BD: " + e.getMessage());
        }
    }

    // --- 1 y 2. CONSULTAR PROPIETARIOS (Y DEUDORES) ---
    private String consultarPropietarios(Integracion integracion, boolean soloDeudores) {
        try {
            String baseUrl = obtenerBaseUrlLimpia(integracion.getBaseUrl());
            String endpoint = baseUrl + "/api/propietarios";

            List<PropietarioDto> propietarios = restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PropietarioDto>>() {});

            if (propietarios == null || propietarios.isEmpty()) {
                return "No hay propietarios registrados en el sistema.";
            }

            StringBuilder sb = new StringBuilder();
            if (soloDeudores) {
                sb.append("🔴 *PROPIETARIOS EN MORA*\n\n");
                propietarios = propietarios.stream()
                        .filter(p -> "Rojo".equalsIgnoreCase(p.getEstado()) || p.getBalance() < 0)
                        .collect(Collectors.toList());

                if (propietarios.isEmpty()) {
                    return "✅ ¡Excelentes noticias! Ningún propietario tiene deudas actualmente.";
                }
            } else {
                sb.append("👥 *LISTA DE PROPIETARIOS*\n\n");
            }

            for (PropietarioDto p : propietarios) {
                String iconoEstado = "Verde".equalsIgnoreCase(p.getEstado()) ? "🟢" : "🔴";
                sb.append(iconoEstado).append(" *Apto ").append(p.getNumApto()).append("* - ").append(p.getNombrePropietario()).append("\n");
                sb.append("   🔹 Balance: $").append(p.getBalance()).append("\n");
                sb.append("   🔹 Total Abonado: $").append(p.getTotalabonado()).append("\n\n");
            }
            return sb.toString();

        } catch (Exception e) {
            System.err.println("Error consultando propietarios: " + e.getMessage());
            return "⚠️ Ocurrió un error al conectar con la API de SystemManager.";
        }
    }

    // --- 3. CONSULTAR REPORTES FINANCIEROS ---
    private String consultarReportesFinancieros(Integracion integracion) {
        try {
            String baseUrl = obtenerBaseUrlLimpia(integracion.getBaseUrl());
            String endpoint = baseUrl + "/api/registro-financiero";

            List<RegistroFinancieroDto> registros = restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RegistroFinancieroDto>>() {});

            if (registros == null || registros.isEmpty()) {
                return "No hay reportes financieros registrados.";
            }

            Collections.reverse(registros);
            StringBuilder sb = new StringBuilder("📊 *ÚLTIMOS REPORTES FINANCIEROS*\n\n");

            registros.stream().limit(10).forEach(r -> {
                sb.append("📅 *Mes:* ").append(r.getMesCuota()).append("\n");
                sb.append("📝 *Descripción:* ").append(r.getDescripcion()).append("\n");
                sb.append("💵 *Cuota:* $").append(r.getCuotaMensual()).append(" | *Total:* $").append(r.getMontoPagar()).append("\n");
                sb.append("〰️〰️〰️〰️〰️〰️〰️〰️\n");
            });

            return sb.toString();

        } catch (Exception e) {
            System.err.println("Error consultando reportes: " + e.getMessage());
            return "⚠️ Ocurrió un error al consultar los reportes financieros.";
        }
    }

    // --- 4. CONSULTAR LOGS DE ACTIVIDAD ---
    private String consultarLogsActividad(Integracion integracion) {
        try {
            String baseUrl = obtenerBaseUrlLimpia(integracion.getBaseUrl());
            String endpoint = baseUrl + "/api/activityLog";

            List<ActivityLogDto> logs = restClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ActivityLogDto>>() {});

            if (logs == null || logs.isEmpty()) {
                return "No hay actividad reciente en el sistema.";
            }

            Collections.reverse(logs);
            StringBuilder sb = new StringBuilder("💻 *LOGS DEL SISTEMA (Últimos 10)*\n\n");

            logs.stream().limit(10).forEach(log -> {
                sb.append("🕒 *").append(log.getFecha()).append("*\n");
                sb.append("🏷️ [").append(log.getCategoria()).append("]\n");
                sb.append("💬 ").append(log.getMensaje()).append("\n\n");
            });

            return sb.toString();

        } catch (Exception e) {
            System.err.println("Error consultando logs: " + e.getMessage());
            return "⚠️ Ocurrió un error al obtener el registro de actividad.";
        }
    }

    // --- UTILIDAD: LIMPIAR URL ---
    private String obtenerBaseUrlLimpia(String rawUrl) {
        if (rawUrl == null) return "";
        if (rawUrl.endsWith("/")) rawUrl = rawUrl.substring(0, rawUrl.length() - 1);
        if (rawUrl.endsWith("/api")) rawUrl = rawUrl.substring(0, rawUrl.length() - 4);
        return rawUrl;
    }

    // ==========================================
    // CLASES DTO INTERNAS PARA MAPEAR LOS JSON
    // ==========================================
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropietarioDto {
        private Long idpropietario;
        private String numApto;
        private String nombrePropietario;
        private double totalabonado;
        private double balance;
        private String estado;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegistroFinancieroDto {
        private Long id;
        private String mesCuota;
        private double cuotaMensual;
        private String descripcion;
        private double montoPagar;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityLogDto {
        private Long idactivity;
        private String fecha;
        private String categoria;
        private String mensaje;
    }
}