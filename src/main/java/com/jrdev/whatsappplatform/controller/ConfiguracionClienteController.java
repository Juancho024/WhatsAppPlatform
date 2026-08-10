package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.model.WhatsappInstancia;
import com.jrdev.whatsappplatform.model.Integracion;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import com.jrdev.whatsappplatform.repository.IntegracionRepository;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionClienteController {

    private final EmpresaRepository empresaRepo;
    private final WhatsappInstanciaRepository instanciaRepo;
    private final IntegracionRepository integracionRepo; // Repositorio de integraciones añadido
    private final EvolutionClient evolutionClient;

    @PostMapping("/vincular-negocio")
    public ResponseEntity<String> vincularNegocio(@RequestBody ConfiguracionClienteRequest request) {
        try {
            // 0. Limpiar el nombre de la instancia para evitar espacios (Ej: "Whatsapp Solicitudes" -> "whatsapp_solicitudes")
            String instanceNameLimpio = request.getInstanceName().trim().toLowerCase().replaceAll("\\s+", "_");

            // 1. Guardar la Empresa y obtener su ID real
            Empresa empresa = new Empresa();
            empresa.setNombre(request.getNombreEmpresa());
            empresa.setEmail(request.getEmail());
            empresa.setTelefono(request.getTelefonoEmpresa());
            empresa.setEstado("ACTIVA");

            Long idEmpresa = empresaRepo.crear(empresa);
            if (idEmpresa == null) {
                return ResponseEntity.badRequest().body("Error al registrar la empresa en la base de datos.");
            }

            // 2. Crear la Instancia de WhatsApp vinculada
            WhatsappInstancia instancia = new WhatsappInstancia();
            instancia.setIdEmpresa(idEmpresa);
            instancia.setNombre(request.getNombreEmpresa() + " Principal");
            instancia.setInstanceName(instanceNameLimpio); // Usamos el nombre limpio sin espacios
            instancia.setNumero(request.getNumeroTelefonoWhatsapp());
            instancia.setProvider("EVOLUTION");
            instancia.setApiUrl(request.getApiUrlEvolution());
            instancia.setApiKey(request.getApiKeyEvolution());
            instancia.setEstado("ACTIVA");

            instanciaRepo.crear(instancia);

            // 3. Guardar la Integración en su respectiva tabla (¡Aquí se llena tu tabla vacía!)
            Integracion integracion = new Integracion();
            integracion.setIdEmpresa(idEmpresa);
            integracion.setNombre("Bot de WhatsApp - " + request.getNombreEmpresa());
            integracion.setTipo("WHATSAPP_BOT");
            integracion.setBaseUrl(request.getApiUrlEvolution());
            integracion.setEstado("ACTIVA");
            integracion.setConfiguration(Map.of("instanceName", instanceNameLimpio));
            integracion.setCreatedAt(OffsetDateTime.now());

            integracionRepo.crear(integracion);

            // 4. Crear físicamente la instancia en Evolution API y obtener el QR
            String respuestaEvolution = evolutionClient.crearInstancia(instanceNameLimpio);

            // 5. Imprimir en la consola del backend para que lo veas claro
            System.out.println("========== INSTANCIA VINCULADA ==========");
            System.out.println("Empresa ID: " + idEmpresa);
            System.out.println("Instancia conectada: " + instanceNameLimpio);
            System.out.println("=========================================");

            return ResponseEntity.ok("Negocio vinculado con éxito. ID Empresa: " + idEmpresa + "\nRespuesta Evolution: " + respuestaEvolution);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error en la configuración: " + e.getMessage());
        }
    }

    @Data
    public static class ConfiguracionClienteRequest {
        private String nombreEmpresa;
        private String email;
        private String telefonoEmpresa;
        private String instanceName;
        private String numeroTelefonoWhatsapp;
        private String apiUrlEvolution;
        private String apiKeyEvolution;
    }
}