package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Preferencia;
import com.jrdev.whatsappplatform.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenciaRepository extends JpaRepository<Preferencia, Long> {
    Optional<Preferencia> findByUsuario(Usuario usuario);
}