package electroenergy.repository;

import electroenergy.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    Optional<Solicitud> findByCodigoSeguimiento(String codigoSeguimiento);
    Optional<Solicitud> findByCodigoSeguimientoOrCorreo(String codigoSeguimiento, String correo);
}
