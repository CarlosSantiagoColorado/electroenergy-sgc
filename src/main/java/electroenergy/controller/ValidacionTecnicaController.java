package electroenergy.controller;

import electroenergy.model.ValidacionTecnica;
import electroenergy.service.ValidacionTecnicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/validaciones")
@CrossOrigin(origins = "*")
public class ValidacionTecnicaController {

    @Autowired
    private ValidacionTecnicaService validacionService;

    @PostMapping("/validar/{solicitudId}")
    public ResponseEntity<?> validar(@PathVariable Long solicitudId) {
        try {
            ValidacionTecnica v = validacionService.validar(solicitudId);
            return ResponseEntity.ok(v);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ValidacionTecnica>> listar() {
        return ResponseEntity.ok(validacionService.listarTodas());
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> porSolicitud(@PathVariable Long solicitudId) {
        return validacionService.buscarPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
