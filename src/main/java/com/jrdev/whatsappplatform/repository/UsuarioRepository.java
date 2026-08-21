package com.jrdev.whatsappplatform.repository;

import com.jrdev.whatsappplatform.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByUsuario(String usuario);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsuario(String usuario);
}