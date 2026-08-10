package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Contacto;
import com.jrdev.whatsappplatform.repository.ContactoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactoService {
    private final ContactoRepository contactoRepository;

    public List<Contacto> buscarTodas() {
        return contactoRepository.buscarTodos();
    }

    public Contacto buscarPorId(Long id) {
        return contactoRepository.buscarPorId(id).orElse(null);
    }

    public Contacto crear(Contacto contacto) {
        contactoRepository.crear(contacto);
        return contactoRepository
                .buscarTodos()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Contacto contacto) {
        return contactoRepository.actualizar(id, contacto) > 0;
    }

//    public boolean eliminar(Long id) {
//        return contactoRepository.eliminar(id) > 0;
//    }

    public boolean cambiarEstado(Long id){
        return contactoRepository.cambiarEstado(id, true) > 0;
    }

}
