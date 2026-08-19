package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository; // <-- Inyectar

    @Transactional
    public Long crearEmpresaYVincular(Empresa empresa, Long idUsuarioCreador) {

        // 1. Creamos la empresa en la BD y obtenemos su ID
        Long idEmpresa = empresaRepository.crear(empresa);

        if (idEmpresa == null) {
            throw new RuntimeException("No se pudo generar el ID de la nueva empresa.");
        }

        // 2. Vinculamos inmediatamente a este usuario como el DUEÑO de la empresa
        usuarioEmpresaRepository.vincularUsuarioAEmpresa(idUsuarioCreador, idEmpresa, "DUEÑO");

        return idEmpresa;
    }

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