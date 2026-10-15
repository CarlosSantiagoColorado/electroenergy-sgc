package electroenergy.controller;

import electroenergy.model.Cotizacion;
import electroenergy.service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import electroenergy.service.EmailService;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cotizaciones")
@CrossOrigin(origins = "*")

public class CotizacionController {

    @Autowired
    private CotizacionService cotizacionService;
    @Autowired
    private EmailService emailService;

    // Generar cotización para una solicitud
    @PostMapping("/generar/{solicitudId}")
    public ResponseEntity<?> generar(@PathVariable Long solicitudId) {
        try {
            Cotizacion c = cotizacionService.generarCotizacion(solicitudId);
            return ResponseEntity.ok(c);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Listar todas las cotizaciones
    @GetMapping
    public ResponseEntity<List<Cotizacion>> listar() {
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    // Ver cotización de una solicitud específica
    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> porSolicitud(@PathVariable Long solicitudId) {
        return cotizacionService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ver cotización por su ID
    @GetMapping("/{id}")
    public ResponseEntity<?> porId(@PathVariable Long id) {
        return cotizacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Nuevo endpoint:
    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarCotizacion(@PathVariable Long id) {
        try {
            Cotizacion c = cotizacionService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("La cotización no ha sido encontrada"));

            emailService.enviarCotizacion(c);

            c.setEstado("Enviada");
            c.setFechaEnvio(LocalDateTime.now());
            c.getSolicitud().setEstado("Cotización enviada");

            return ResponseEntity.ok(java.util.Map.of(
                    "mensaje", "Cotización enviada correctamente a " + c.getSolicitud().getCorreo(),
                    "codigo",  c.getCodigoCotizacion()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar: " + e.getMessage());
        }
    }
}