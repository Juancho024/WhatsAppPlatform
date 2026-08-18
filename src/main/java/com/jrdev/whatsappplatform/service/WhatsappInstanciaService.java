package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.WhatsappInstancia;
import com.jrdev.whatsappplatform.repository.WhatsappInstanciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WhatsappInstanciaService {

    private final WhatsappInstanciaRepository repository;
    private final EvolutionClient evolutionClient;


    public List<WhatsappInstancia> buscarTodas() {

        return repository.buscarTodas();
    }


    public WhatsappInstancia buscarPorId(Long id) {

        return repository.buscarPorId(id).orElse(null);
    }


    public WhatsappInstancia crear(WhatsappInstancia instancia) {

        if (instancia.getInstanceName() == null || instancia.getInstanceName().isBlank()) {

            throw new IllegalArgumentException("El instanceName es obligatorio");
        }
        if (repository.existePorInstanceName(
                instancia.getInstanceName())) {

            throw new IllegalArgumentException(
                    "Ya existe una instancia con instanceName: "
                            + instancia.getInstanceName()
            );
        }

        instancia.setEstado("INACTIVA");

        Long id = repository.crear(instancia);

        /*
         * Ahora creamos la instancia REAL
         * en Evolution.
         */
        try {

            evolutionClient.crearInstancia(instancia.getInstanceName());

            /*
             * Si Evolution respondió correctamente,
             * dejamos la instancia disponible.
             */
            repository.cambiarEstado(id, "ACTIVA");

        } catch (Exception e) {

            /*
             * La instancia existe en nuestra BD,
             * pero Evolution no pudo crearla.
             */
            repository.cambiarEstado(id, "ERROR");

            throw e;
        }

        return repository.buscarPorId(id).orElseThrow();
    }


    public boolean actualizar(Long id, WhatsappInstancia instancia) {

        return repository.actualizar(id, instancia) > 0;
    }


    public void desactivar(Long id) {

        int filas = repository.cambiarEstado(id, "INACTIVA");

        if (filas == 0) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }
    }


    public void activar(Long id) {

        int filas = repository.cambiarEstado(id, "DESCONECTADA");

        if (filas == 0) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }
    }


    public String conectar(Long id) {

        WhatsappInstancia instancia = buscarPorId(id);

        if (instancia == null) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }

        String respuesta = evolutionClient.conectar(instancia.getInstanceName());

        repository.cambiarEstado(id, "CONECTANDO");

        return respuesta;
    }


    public String obtenerEstado(Long id) {

        WhatsappInstancia instancia = buscarPorId(id);

        if (instancia == null) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }

        String respuesta = evolutionClient.estadoConexion(instancia.getInstanceName());

        return respuesta;
    }


    public String desconectar(Long id) {

        WhatsappInstancia instancia = buscarPorId(id);

        if (instancia == null) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }

        String respuesta = evolutionClient.desconectar(instancia.getInstanceName());

        repository.cambiarEstado(id, "DESCONECTADA");

        return respuesta;
    }

    public String estadoConexion(Long id) {
        WhatsappInstancia instancia = repository.buscarPorId(id).orElseThrow(() -> new RuntimeException("WhatsApp Instancia no encontrada"));
        return evolutionClient.estadoConexion(instancia.getInstanceName());
    }

    public void enviarMensaje(Long id, String numero, String texto) {

        WhatsappInstancia instancia = buscarPorId(id);

        if (instancia == null) {

            throw new RuntimeException("WhatsApp Instancia no encontrada");
        }

        evolutionClient.enviarMensaje(instancia.getInstanceName(), numero, texto);
    }
    public List<WhatsappInstancia> buscarPorEmpresa(Long idEmpresa) {
        return repository.buscarPorEmpresa(idEmpresa);
    }
}