package electroenergy.controller;

import electroenergy.model.Solicitud;
import electroenergy.service.SolicitudService;
import electroenergy.repository.SolicitudRepository;
import electroenergy.repository.CotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;
    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private CotizacionRepository cotizacionRepository;

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
    @GetMapping("/consultar")
    public ResponseEntity<?> consultarEstado(@RequestParam String q) {
        return solicitudRepository.findByCodigoSeguimientoOrCorreo(q, q)
                .map(s -> {
                    Map<String, Object> res = new LinkedHashMap<>();
                    res.put("codigoSeguimiento", s.getCodigoSeguimiento());
                    res.put("nombre", s.getNombre());
                    res.put("correo", s.getCorreo());
                    res.put("estado", s.getEstado());
                    res.put("consumoKwh", s.getConsumoKwh());
                    res.put("fechaRegistro", s.getFechaRegistro());
                    cotizacionRepository.findBySolicitudId(s.getId())
                            .ifPresent(c -> res.put("cotizacion", c));
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    // PUT - Cambiar estado de una solicitud
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        Optional<Solicitud> opt = solicitudService.buscarPorId(id);
        if (opt.isPresent()) {
            Solicitud s = opt.get();
            s.setEstado(body.get("estado"));
            return ResponseEntity.ok(solicitudService.guardar(s));
        }
        return ResponseEntity.notFound().build();
    }
}