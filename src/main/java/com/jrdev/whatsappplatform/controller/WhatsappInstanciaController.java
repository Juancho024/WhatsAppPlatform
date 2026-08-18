package com.jrdev.whatsappplatform.controller;

import com.jrdev.whatsappplatform.model.WhatsappInstancia;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import com.jrdev.whatsappplatform.service.EvolutionClient;
import com.jrdev.whatsappplatform.service.WhatsappInstanciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp-instancias")
@RequiredArgsConstructor
public class WhatsappInstanciaController {

    private final WhatsappInstanciaService service;
    private final EvolutionClient evolutionClient;
    private final WhatsappInstanciaRepository instanciaRepository;

    @GetMapping
    public List<WhatsappInstancia> obtenerTodas() {
        return service.buscarTodas();
    }

    @PostMapping
    public ResponseEntity<WhatsappInstancia> crear(@RequestBody WhatsappInstancia instancia) {
        WhatsappInstancia creada = service.crear(instancia);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody WhatsappInstancia instancia) {
        boolean actualizada = service.actualizar(id, instancia);
        if (!actualizada) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        service.activar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/conectar")
    public ResponseEntity<String> conectar(@PathVariable Long id) {
        String respuesta = service.conectar(id);
        return ResponseEntity.ok(respuesta);
    }


    @GetMapping("/{id}/estado")
    public ResponseEntity<String> estado(@PathVariable Long id) {
        String respuesta = service.obtenerEstado(id);
        return ResponseEntity.ok(respuesta);
    }


    @PostMapping("/{id}/desconectar")
    public ResponseEntity<String> desconectar(@PathVariable Long id) {
        String respuesta = service.desconectar(id);
        return ResponseEntity.ok(respuesta);
    }


    @PostMapping("/{id}/mensaje")
    public ResponseEntity<Void> enviarMensaje(@PathVariable Long id, @RequestParam String numero, @RequestParam String texto) {
        service.enviarMensaje(id, numero, texto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/estado-conexion")
    public ResponseEntity<String> estadoConexion(@PathVariable Long id) {
        WhatsappInstancia instancia = service.buscarPorId(id);
        if (instancia == null) {
            return ResponseEntity.notFound().build();
        }
        String respuesta = service.estadoConexion(id);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/crear-y-vincular")
    public ResponseEntity<String> crearInstancia(@RequestBody WhatsappInstancia instancia) {
        try {
            // 1. Mandamos a Evolution API a crear la instancia y generar el QR
            String respuestaEvolution = evolutionClient.crearInstancia(instancia.getInstanceName());

            // 2. Si Evolution no falló, guardamos el registro en PostgreSQL
            instancia.setEstado("ACTIVA");
            Long idInstancia = instanciaRepository.crear(instancia);

            // 3. Devolvemos el JSON de Evolution (que trae el QR) al frontend (o a Postman)
            return ResponseEntity.ok(respuestaEvolution);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error creando la instancia: " + e.getMessage());
        }
    }

    @GetMapping("/empresa/{idEmpresa}")
    public List<WhatsappInstancia> obtenerPorEmpresa(@PathVariable Long idEmpresa) {
        return service.buscarPorEmpresa(idEmpresa);
    }

}