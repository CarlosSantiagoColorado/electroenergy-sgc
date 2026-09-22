package electroenergy.service;

import electroenergy.model.Solicitud;
import electroenergy.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    public Solicitud registrarSolicitud(Solicitud solicitud) {
        // Generar código de seguimiento único
        String codigo = generarCodigo();
        solicitud.setCodigoSeguimiento(codigo);
        solicitud.setEstado("Pendiente de revision");
        solicitud.setFechaRegistro(LocalDateTime.now());
        return solicitudRepository.save(solicitud);
    }

    public List<Solicitud> listarSolicitudes() {
        return solicitudRepository.findAll();
    }

    public Optional<Solicitud> buscarPorCodigo(String codigo) {
        return solicitudRepository.findByCodigoSeguimiento(codigo);
    }

    private String generarCodigo() {
        String año = String.valueOf(LocalDateTime.now().getYear());
        long count = solicitudRepository.count() + 1;
        return String.format("EE-%s-%05d", año, count);
    }
}
