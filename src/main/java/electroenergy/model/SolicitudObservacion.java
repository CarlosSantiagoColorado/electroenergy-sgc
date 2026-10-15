package electroenergy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "solicitud_observacion")
public class SolicitudObservacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Column(name = "tipo", nullable = false)
    private String tipo; // "DATO_FALTANTE", "ACLARACION", "CORRECCION", "INFO_ADICIONAL"

    @Column(name = "campo_afectado")
    private String campoAfectado;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "estado_observacion", nullable = false)
    private String estadoObservacion = "Pendiente"; // "Pendiente", "Respondida", "Cerrada"

    @Column(name = "respuesta_cliente", columnDefinition = "TEXT")
    private String respuestaCliente;

    @Column(name = "registrado_por")
    private String registradoPor = "Administrador";

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}