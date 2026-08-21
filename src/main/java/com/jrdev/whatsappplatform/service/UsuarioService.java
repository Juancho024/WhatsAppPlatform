package com.jrdev.whatsappplatform.service;

import com.jrdev.whatsappplatform.model.Empresa;
import com.jrdev.whatsappplatform.model.Usuario;
import com.jrdev.whatsappplatform.repository.EmpresaRepository;
import com.jrdev.whatsappplatform.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Transactional // 🔥 Garantiza que o se borra TODO, o no se borra NADA
    public void eliminarUsuarioYSuEcosistema(Long userId) {

        // 1. Buscamos todas las empresas que pertenecen a este usuario
        List<Empresa> empresasDelUsuario = empresaRepository.findEmpresasByUsuarioId(userId);

        // 2. Borramos cada empresa.
        // ¡OJO AQUÍ!: Gracias a que en tu SQL le pusiste ON DELETE CASCADE a las llaves foráneas,
        // al ejecutar este delete, PostgreSQL automáticamente borrará también:
        // las instancias, los contactos, los chats, los mensajes y la media de esa empresa.
        for (Empresa empresa : empresasDelUsuario) {
            empresaRepository.eliminarEmpresaPorId(empresa.getIdEmpresa());
        }

        // 3. Finalmente, borramos al usuario de la tabla 'usuarios'
        usuarioRepository.deleteById(userId);
    }
}