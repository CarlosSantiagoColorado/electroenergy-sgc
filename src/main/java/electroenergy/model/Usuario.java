package electroenergy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private String estado = "Activo";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_sesion")
    private LocalDateTime ultimaSesion;
}
