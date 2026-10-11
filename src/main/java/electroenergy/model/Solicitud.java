package electroenergy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_seguimiento", unique = true, nullable = false)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String correo;

    @Column(name = "consumo_kwh", nullable = false)
    private BigDecimal consumoKwh;

    @Column(name = "valor_factura", nullable = false)
    private BigDecimal valorFactura;

    @Column(name = "tipo_cliente", nullable = false)
    private String tipoCliente;

    private String observaciones;

    @Column(nullable = false)
    private String estado = "Pendiente de revision";

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "consentimiento_datos", nullable = false)
    private Boolean consentimientoDatos = false;

    @Column(name = "fecha_consentimiento")
    private LocalDateTime fechaConsentimiento;
}
