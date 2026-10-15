package electroenergy.service;

import electroenergy.model.Solicitud;
import electroenergy.model.SolicitudObservacion;
import electroenergy.repository.ObservacionRepository;
import electroenergy.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ObservacionService {

    @Autowired private ObservacionRepository observacionRepository;
    @Autowired private SolicitudRepository solicitudRepository;

    public SolicitudObservacion registrar(Long solicitudId, SolicitudObservacion datos) {
        Solicitud s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        datos.setSolicitud(s);
        datos.setFechaRegistro(LocalDateTime.now());
        datos.setEstadoObservacion("Pendiente");

        // Cambiar estado de la solicitud a "Datos faltantes"
        s.setEstado("Datos faltantes");
        solicitudRepository.save(s);

        return observacionRepository.save(datos);
    }

    public SolicitudObservacion responder(Long observacionId, String respuesta) {
        SolicitudObservacion obs = observacionRepository.findById(observacionId)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));

        obs.setRespuestaCliente(respuesta);
        obs.setEstadoObservacion("Respondida");
        obs.setFechaRespuesta(LocalDateTime.now());

        // Si todas las observaciones de la solicitud están respondidas,
        // cambiar estado a "En revisión"
        Long solicitudId = obs.getSolicitud().getId();
        List<SolicitudObservacion> todas = observacionRepository
                .findBySolicitudIdOrderByFechaRegistroDesc(solicitudId);
        boolean todasRespondidas = todas.stream()
                .allMatch(o -> !o.getEstadoObservacion().equals("Pendiente"));

        if (todasRespondidas) {
            Solicitud s = obs.getSolicitud();
            s.setEstado("En revisión");
            solicitudRepository.save(s);
        }

        return observacionRepository.save(obs);
    }

    public SolicitudObservacion cerrar(Long observacionId) {
        SolicitudObservacion obs = observacionRepository.findById(observacionId)
                .orElseThrow(() -> new RuntimeException("Observación no encontrada"));
        obs.setEstadoObservacion("Cerrada");
        return observacionRepository.save(obs);
    }

    public List<SolicitudObservacion> listarPorSolicitud(Long solicitudId) {
        return observacionRepository.findBySolicitudIdOrderByFechaRegistroDesc(solicitudId);
    }

    public List<SolicitudObservacion> listarPendientes() {
        return observacionRepository.findByEstadoObservacion("Pendiente");
    }

    public List<SolicitudObservacion> listarTodas() {
        return observacionRepository.findAll();
    }
}
