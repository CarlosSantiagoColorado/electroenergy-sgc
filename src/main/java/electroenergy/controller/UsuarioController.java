package electroenergy.controller;

import electroenergy.model.Usuario;
import electroenergy.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.crear(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Long id, @RequestBody Usuario datos) {
        return ResponseEntity.ok(usuarioService.actualizar(id, datos));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Usuario> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desactivar(id));
    }
}
