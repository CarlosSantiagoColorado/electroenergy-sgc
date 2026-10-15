package electroenergy.controller;

import electroenergy.model.SolicitudObservacion;
import electroenergy.service.ObservacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/observaciones")
@CrossOrigin(origins = "*")
public class ObservacionController {

    @Autowired private ObservacionService observacionService;

    @PostMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> registrar(
            @PathVariable Long solicitudId,
            @RequestBody SolicitudObservacion datos) {
        try {
            return ResponseEntity.ok(observacionService.registrar(solicitudId, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/responder")
    public ResponseEntity<?> responder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    observacionService.responder(id, body.get("respuesta")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(observacionService.cerrar(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<List<SolicitudObservacion>> porSolicitud(
            @PathVariable Long solicitudId) {
        return ResponseEntity.ok(observacionService.listarPorSolicitud(solicitudId));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudObservacion>> pendientes() {
        return ResponseEntity.ok(observacionService.listarPendientes());
    }

    @GetMapping
    public ResponseEntity<List<SolicitudObservacion>> todas() {
        return ResponseEntity.ok(observacionService.listarTodas());
    }
}
