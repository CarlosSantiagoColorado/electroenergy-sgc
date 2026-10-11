package electroenergy.repository;

import electroenergy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByEstado(String estado);
    List<Usuario> findByRol(String rol);
}
