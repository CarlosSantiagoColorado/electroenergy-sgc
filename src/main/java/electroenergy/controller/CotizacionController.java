package electroenergy.controller;

import electroenergy.model.Cotizacion;
import electroenergy.service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones")
@CrossOrigin(origins = "*")
public class CotizacionController {

    @Autowired
    private CotizacionService cotizacionService;

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
}