package electroenergy.service;

import electroenergy.model.Solicitud;
import electroenergy.model.ValidacionTecnica;
import electroenergy.repository.SolicitudRepository;
import electroenergy.repository.ValidacionTecnicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ValidacionTecnicaService {

    // ── Reglas de negocio ─────────────────────────────────────────────
    private static final BigDecimal CONSUMO_MIN_KWH   = new BigDecimal("50");
    private static final BigDecimal CONSUMO_MAX_KWH   = new BigDecimal("50000");
    private static final BigDecimal FACTURA_MIN_COP   = new BigDecimal("20000");
    private static final BigDecimal FACTURA_MAX_COP   = new BigDecimal("50000000");
    // Ratio: factura ÷ consumo debe estar entre 300 y 1500 COP/kWh
    private static final BigDecimal RATIO_MIN         = new BigDecimal("300");
    private static final BigDecimal RATIO_MAX         = new BigDecimal("1500");

    @Autowired private ValidacionTecnicaRepository validacionRepo;
    @Autowired private SolicitudRepository solicitudRepository;

    public ValidacionTecnica validar(Long solicitudId) {
        Solicitud s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Si ya fue validada, retorna la validación existente
        Optional<ValidacionTecnica> existente = validacionRepo.findBySolicitudId(solicitudId);
        if (existente.isPresent()) return existente.get();

        ValidacionTecnica v = new ValidacionTecnica();
        v.setSolicitud(s);
        v.setFechaValidacion(LocalDateTime.now());

        List<String> observaciones = new ArrayList<>();
        List<String> errores       = new ArrayList<>();
        int puntaje = 0;

        // Regla 1: Consumo mínimo y máximo
        BigDecimal consumo = s.getConsumoKwh();
        boolean consumoOk = consumo.compareTo(CONSUMO_MIN_KWH) >= 0
                && consumo.compareTo(CONSUMO_MAX_KWH) <= 0;
        v.setConsumoValido(consumoOk);
        if (consumoOk) {
            puntaje++;
        } else if (consumo.compareTo(CONSUMO_MIN_KWH) < 0) {
            errores.add("Consumo muy bajo (" + consumo + " kWh). Mínimo requerido: 50 kWh/mes.");
        } else {
            errores.add("Consumo muy alto (" + consumo + " kWh). Supera el límite de 50.000 kWh/mes.");
        }

        // Regla 2: Valor de factura
        BigDecimal factura = s.getValorFactura();
        boolean facturaOk = factura.compareTo(FACTURA_MIN_COP) >= 0
                && factura.compareTo(FACTURA_MAX_COP) <= 0;
        v.setFacturaValida(facturaOk);
        if (facturaOk) {
            puntaje++;
        } else {
            errores.add("Valor de factura fuera de rango ($" + factura + " COP).");
        }

        // Regla 3: Tipo de cliente válido
        String tipo = s.getTipoCliente();
        boolean tipoOk = tipo != null && (
                tipo.equals("Residencial") ||
                        tipo.equals("Comercial")   ||
                        tipo.equals("Industrial")  ||
                        tipo.equals("Institucional")
        );
        v.setTipoClienteValido(tipoOk);
        if (tipoOk) puntaje++;
        else errores.add("Tipo de cliente no reconocido: " + tipo);

        // Regla 4: Coherencia consumo vs factura (ratio COP/kWh)
        boolean ratioOk = false;
        if (consumoOk && facturaOk && consumo.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = factura.divide(consumo, 2, java.math.RoundingMode.HALF_UP);
            ratioOk = ratio.compareTo(RATIO_MIN) >= 0 && ratio.compareTo(RATIO_MAX) <= 0;
            if (!ratioOk) {
                observaciones.add("La relación factura/consumo (" + ratio + " COP/kWh) es inusual. Se recomienda verificar con el cliente.");
            } else {
                puntaje++;
            }
        }
        v.setRelacionConsumoFacturaOk(ratioOk);

        // Observaciones por tipo de cliente
        if ("Industrial".equals(tipo) && consumo.compareTo(new BigDecimal("500")) < 0) {
            observaciones.add("Consumo bajo para cliente Industrial. Verificar datos.");
        }
        if ("Residencial".equals(tipo) && consumo.compareTo(new BigDecimal("2000")) > 0) {
            observaciones.add("Consumo alto para cliente Residencial. Considerar tarifa comercial.");
        }

        // Determinar resultado
        String resultado;
        boolean esViable;
        StringBuilder detalle = new StringBuilder();

        if (!errores.isEmpty()) {
            resultado = "RECHAZADA";
            esViable  = false;
            detalle.append("❌ ERRORES:\n");
            errores.forEach(e -> detalle.append("• ").append(e).append("\n"));
        } else if (!observaciones.isEmpty()) {
            resultado = "OBSERVACIONES";
            esViable  = true;
            detalle.append("⚠️ OBSERVACIONES:\n");
            observaciones.forEach(o -> detalle.append("• ").append(o).append("\n"));
        } else {
            resultado = "APROBADA";
            esViable  = true;
            detalle.append("✅ Todos los datos técnicos son válidos y coherentes.");
        }

        v.setResultado(resultado);
        v.setEsViable(esViable);
        v.setDetalle(detalle.toString().trim());

        // Actualizar estado de la solicitud si fue rechazada
        if (!esViable) {
            s.setEstado("Rechazada");
            solicitudRepository.save(s);
        }

        return validacionRepo.save(v);
    }

    public Optional<ValidacionTecnica> buscarPorSolicitud(Long solicitudId) {
        return validacionRepo.findBySolicitudId(solicitudId);
    }

    public java.util.List<ValidacionTecnica> listarTodas() {
        return validacionRepo.findAll();
    }
}
