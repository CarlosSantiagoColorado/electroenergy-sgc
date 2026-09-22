package electroenergy.controller;

import electroenergy.model.Solicitud;
import electroenergy.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    // POST - Registrar nueva solicitud
    @PostMapping
    public ResponseEntity<Solicitud> registrar(@RequestBody Solicitud solicitud) {
        Solicitud nueva = solicitudService.registrarSolicitud(solicitud);
        return ResponseEntity.ok(nueva);
    }

    // GET - Listar todas las solicitudes
    @GetMapping
    public ResponseEntity<List<Solicitud>> listar() {
        return ResponseEntity.ok(solicitudService.listarSolicitudes());
    }

    // GET - Consultar por código de seguimiento
    @GetMapping("/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        Optional<Solicitud> solicitud = solicitudService.buscarPorCodigo(codigo);
        if (solicitud.isPresent()) {
            return ResponseEntity.ok(solicitud.get());
        }
        return ResponseEntity.notFound().build();
    }
}