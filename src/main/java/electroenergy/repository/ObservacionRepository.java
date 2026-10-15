package electroenergy.repository;

import electroenergy.model.SolicitudObservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<SolicitudObservacion, Long> {
    List<SolicitudObservacion> findBySolicitudIdOrderByFechaRegistroDesc(Long solicitudId);
    List<SolicitudObservacion> findByEstadoObservacion(String estado);
}
