package electroenergy.service;

import electroenergy.model.Cotizacion;
import electroenergy.model.Solicitud;
import electroenergy.repository.CotizacionRepository;
import electroenergy.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {

    // ── Parámetros técnicos del sistema ──────────────────────────────
    private static final BigDecimal HPS              = new BigDecimal("4.5");   // horas pico solar
    private static final BigDecimal EFICIENCIA       = new BigDecimal("0.80");  // 80 %
    private static final int        POTENCIA_PANEL_W = 400;                     // vatios por panel
    private static final BigDecimal PRECIO_PANEL_USD = new BigDecimal("250");   // USD por panel
    private static final BigDecimal TRM              = new BigDecimal("4200");   // COP por USD
    private static final BigDecimal PRECIO_KWH_COP   = new BigDecimal("650");   // COP / kWh
    private static final BigDecimal CO2_POR_KWH      = new BigDecimal("0.385"); // kg CO₂ / kWh
    private static final BigDecimal PORCENTAJE_INST   = new BigDecimal("0.30"); // instalación = 30 % del costo paneles

    @Autowired
    private CotizacionRepository cotizacionRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;

    // ── Generar cotización ────────────────────────────────────────────
    public Cotizacion generarCotizacion(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Si ya existe una cotización para esta solicitud, la retorna
        Optional<Cotizacion> existente = cotizacionRepository.findBySolicitudId(solicitudId);
        if (existente.isPresent()) return existente.get();

        BigDecimal consumo = solicitud.getConsumoKwh(); // kWh/mes

        // 1. Potencia del sistema (kW)
        //    P = consumo_diario / (HPS * eficiencia)
        BigDecimal consumoDiario = consumo.divide(new BigDecimal("30"), 4, RoundingMode.HALF_UP);
        BigDecimal potenciaKw = consumoDiario
                .divide(HPS.multiply(EFICIENCIA), 4, RoundingMode.HALF_UP);

        // 2. Número de paneles
        BigDecimal potenciaPanelKw = new BigDecimal(POTENCIA_PANEL_W)
                .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        int numPaneles = potenciaKw.divide(potenciaPanelKw, 0, RoundingMode.CEILING).intValue();

        // 3. Costos
        BigDecimal costoPanelesCop = PRECIO_PANEL_USD
                .multiply(new BigDecimal(numPaneles))
                .multiply(TRM);
        BigDecimal costoInstalacion = costoPanelesCop.multiply(PORCENTAJE_INST)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal costoTotal = costoPanelesCop.add(costoInstalacion)
                .setScale(0, RoundingMode.HALF_UP);

        // 4. Ahorro mensual (COP)
        BigDecimal ahorroMensual = consumo.multiply(PRECIO_KWH_COP)
                .setScale(0, RoundingMode.HALF_UP);

        // 5. Tiempo de retorno (meses)
        int retornoMeses = costoTotal
                .divide(ahorroMensual, 0, RoundingMode.CEILING).intValue();

        // 6. Reducción de CO₂ (kg/año)
        BigDecimal co2Anual = consumo.multiply(new BigDecimal("12"))
                .multiply(CO2_POR_KWH).setScale(2, RoundingMode.HALF_UP);

        // 7. Armar la cotización
        Cotizacion c = new Cotizacion();
        c.setCodigoCotizacion(generarCodigo());
        c.setSolicitud(solicitud);
        c.setConsumoMensualKwh(consumo);
        c.setPotenciaSistemaKw(potenciaKw.setScale(3, RoundingMode.HALF_UP));
        c.setNumPaneles(numPaneles);
        c.setPotenciaPanelW(POTENCIA_PANEL_W);
        c.setHorasPicoSolar(HPS);
        c.setEficienciaSistema(new BigDecimal("80.00"));
        c.setCostoPaneles(costoPanelesCop.setScale(0, RoundingMode.HALF_UP));
        c.setCostoInstalacion(costoInstalacion);
        c.setCostoTotal(costoTotal);
        c.setAhorroMensualEstimado(ahorroMensual);
        c.setTiempoRetornoMeses(retornoMeses);
        c.setReduccionCo2KgAnual(co2Anual);
        c.setEstado("Generada");
        c.setFechaGeneracion(LocalDateTime.now());

        // Actualizar estado de la solicitud
        solicitud.setEstado("En revisión");
        solicitudRepository.save(solicitud);

        return cotizacionRepository.save(c);
    }

    public List<Cotizacion> listarTodas() {
        return cotizacionRepository.findAll();
    }

    public Optional<Cotizacion> buscarPorSolicitud(Long solicitudId) {
        return cotizacionRepository.findBySolicitudId(solicitudId);
    }

    public Optional<Cotizacion> buscarPorId(Long id) {
        return cotizacionRepository.findById(id);
    }

    private String generarCodigo() {
        int anio = LocalDateTime.now().getYear();
        long total = cotizacionRepository.count() + 1;
        return String.format("COT-%d-%05d", anio, total);
    }
}
