package electroenergy.repository;

import electroenergy.model.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    Optional<Cotizacion> findBySolicitudId(Long solicitudId);
    Optional<Cotizacion> findByCodigoCotizacion(String codigo);
}
