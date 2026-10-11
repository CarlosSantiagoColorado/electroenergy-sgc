package electroenergy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_cotizacion", nullable = false, unique = true)
    private String codigoCotizacion;

    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    // Datos del cálculo
    @Column(name = "consumo_mensual_kwh", nullable = false)
    private BigDecimal consumoMensualKwh;

    @Column(name = "potencia_sistema_kw", nullable = false)
    private BigDecimal potenciaSistemaKw;

    @Column(name = "num_paneles", nullable = false)
    private Integer numPaneles;

    @Column(name = "potencia_panel_w", nullable = false)
    private Integer potenciaPanelW;

    @Column(name = "horas_pico_solar", nullable = false)
    private BigDecimal horasPicoSolar;

    @Column(name = "eficiencia_sistema", nullable = false)
    private BigDecimal eficienciaSistema;

    // Costos
    @Column(name = "costo_paneles", nullable = false)
    private BigDecimal costoPaneles;

    @Column(name = "costo_instalacion", nullable = false)
    private BigDecimal costoInstalacion;

    @Column(name = "costo_total", nullable = false)
    private BigDecimal costoTotal;

    @Column(name = "ahorro_mensual_estimado", nullable = false)
    private BigDecimal ahorroMensualEstimado;

    @Column(name = "tiempo_retorno_meses", nullable = false)
    private Integer tiempoRetornoMeses;

    @Column(name = "reduccion_co2_kg_anual", nullable = false)
    private BigDecimal reduccionCo2KgAnual;

    @Column(name = "estado", nullable = false)
    private String estado = "Generada";

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
}
