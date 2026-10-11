package electroenergy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "validacion_tecnica")
public class ValidacionTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "es_viable", nullable = false)
    private Boolean esViable;

    @Column(name = "resultado", nullable = false)
    private String resultado; // "APROBADA", "OBSERVACIONES", "RECHAZADA"

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "consumo_valido")
    private Boolean consumoValido;

    @Column(name = "factura_valida")
    private Boolean facturaValida;

    @Column(name = "tipo_cliente_valido")
    private Boolean tipoClienteValido;

    @Column(name = "relacion_consumo_factura_ok")
    private Boolean relacionConsumoFacturaOk;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion = LocalDateTime.now();
}
