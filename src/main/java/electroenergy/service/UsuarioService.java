package electroenergy.service;

import electroenergy.model.Usuario;
import electroenergy.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByEstado("Activo");
    }

    public Usuario crear(Usuario usuario) {
        usuario.setEstado("Activo");
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setNombre(datos.getNombre());
        u.setRol(datos.getRol());
        u.setEstado(datos.getEstado());
        return usuarioRepository.save(u);
    }

    public Usuario desactivar(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setEstado("Inactivo");
        return usuarioRepository.save(u);
    }
}
