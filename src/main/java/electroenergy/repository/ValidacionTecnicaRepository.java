package electroenergy.repository;

import electroenergy.model.ValidacionTecnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ValidacionTecnicaRepository extends JpaRepository<ValidacionTecnica, Long> {
    Optional<ValidacionTecnica> findBySolicitudId(Long solicitudId);
}
