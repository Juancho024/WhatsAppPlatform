package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<Empresa> buscarTodas() {
        return empresaRepository.buscarTodas();
    }

    public Empresa buscarPorId(Long id) {
        return empresaRepository.buscarPorId(id).orElse(null);
    }

    public Empresa crear(Empresa empresa) {
        empresaRepository.crear(empresa);
        return empresaRepository
                .buscarTodas()
                .stream()
                .reduce((primero, segundo) -> segundo)
                .orElse(null);
    }

    public boolean actualizar(Long id, Empresa empresa) {
        return empresaRepository.actualizar(id, empresa) > 0;
    }

    public boolean eliminar(Long id) {
        return empresaRepository.eliminar(id) > 0;
    }

    public void desactivar(Long id) {
        int filas = empresaRepository.cambiarEstado(id, "INACTIVA");
        if (filas == 0) {
            throw new RuntimeException("Empresa no encontrada");
        }
    }

    public void activar(Long id) {
        int filas = empresaRepository.cambiarEstado(id, "ACTIVA");
        if (filas == 0) {
            throw new RuntimeException("Empresa no encontrada");
        }
    }

}