package electroenergy.service;

import electroenergy.model.Cotizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarCotizacion(Cotizacion c) throws Exception {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setFrom(remitente, "ELECTROENERGY — Energía Solar");
        helper.setTo(c.getSolicitud().getCorreo());
        helper.setSubject("Cotización " + c.getCodigoCotizacion() + " — Sistema Solar ELECTROENERGY");
        helper.setText(construirHtml(c), true);

        mailSender.send(mensaje);
    }

    private String construirHtml(Cotizacion c) {
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("es", "CO"));
        String nombre    = c.getSolicitud().getNombre();
        String codigo    = c.getCodigoCotizacion();

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8">
            <style>
              body { font-family: 'Segoe UI', sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
              .wrapper { max-width: 600px; margin: 30px auto; background: #fff;
                         border-radius: 12px; overflow: hidden;
                         box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
              .header { background: linear-gradient(135deg, #1a3c1a, #2e7d32);
                        padding: 36px 32px; text-align: center; }
              .header h1 { color: #7ddc4e; margin: 0; font-size: 1.6rem; }
              .header p  { color: rgba(255,255,255,0.8); margin: 6px 0 0; }
              .body { padding: 32px; }
              .saludo { font-size: 1rem; color: #333; margin-bottom: 20px; }
              .codigo { background: #f0f7ec; border-left: 4px solid #2e7d32;
                        padding: 12px 16px; border-radius: 6px; margin-bottom: 24px;
                        font-size: 0.9rem; color: #1a3c1a; }
              .seccion { margin-bottom: 24px; }
              .seccion h3 { color: #1a3c1a; border-bottom: 2px solid #e0ecd8;
                            padding-bottom: 8px; margin-bottom: 14px; font-size: 1rem; }
              .fila { display: flex; justify-content: space-between;
                      padding: 8px 0; border-bottom: 1px solid #f5f5f5;
                      font-size: 0.9rem; }
              .fila span:last-child { font-weight: 600; color: #1a3c1a; }
              .total { background: #1a3c1a; color: #fff; padding: 16px 20px;
                       border-radius: 8px; display: flex; justify-content: space-between;
                       font-size: 1rem; margin: 16px 0; }
              .total span:last-child { font-size: 1.2rem; font-weight: 700; color: #7ddc4e; }
              .eco { background: #e8f5e9; border-radius: 8px; padding: 14px 16px;
                     text-align: center; color: #2e7d32; font-size: 0.9rem; margin-bottom: 24px; }
              .footer { background: #f0f7ec; padding: 20px 32px; text-align: center;
                        font-size: 0.78rem; color: #888; }
            </style>
            </head>
            <body>
            <div class="wrapper">
              <div class="header">
                <h1>⚡ ELECTROENERGY</h1>
                <p>Gestión de Cotizaciones — Energía Solar</p>
              </div>
              <div class="body">
                <p class="saludo">Estimado/a <strong>%s</strong>,</p>
                <p style="color:#555;font-size:0.9rem;margin-bottom:20px">
                  Nos complace presentarle la cotización para su sistema de energía solar,
                  elaborada con base en la información suministrada en su solicitud.
                </p>
                <div class="codigo">
                  📋 Código de cotización: <strong>%s</strong> &nbsp;|&nbsp;
                  Código de solicitud: <strong>%s</strong>
                </div>

                <div class="seccion">
                  <h3>⚡ Sistema Solar Propuesto</h3>
                  <div class="fila"><span>Número de paneles</span><span>%d paneles × %dW</span></div>
                  <div class="fila"><span>Potencia total del sistema</span><span>%s kW</span></div>
                  <div class="fila"><span>Consumo cubierto</span><span>%s kWh/mes</span></div>
                  <div class="fila"><span>Horas pico solar</span><span>%s h/día</span></div>
                  <div class="fila"><span>Eficiencia del sistema</span><span>%s%%</span></div>
                </div>

                <div class="seccion">
                  <h3>💰 Inversión</h3>
                  <div class="fila"><span>Costo de paneles</span><span>$ %s COP</span></div>
                  <div class="fila"><span>Costo de instalación</span><span>$ %s COP</span></div>
                  <div class="total">
                    <span>INVERSIÓN TOTAL</span>
                    <span>$ %s COP</span>
                  </div>
                </div>

                <div class="seccion">
                  <h3>📈 Retorno de Inversión</h3>
                  <div class="fila"><span>Ahorro mensual estimado</span><span>$ %s COP/mes</span></div>
                  <div class="fila"><span>Tiempo de retorno</span><span>%d meses (%s años)</span></div>
                </div>

                <div class="eco">
                  🌱 Con este sistema evitará la emisión de
                  <strong>%s kg de CO₂ por año</strong>,
                  contribuyendo a un futuro más sostenible.
                </div>

                <p style="color:#555;font-size:0.88rem">
                  Esta cotización tiene una vigencia de <strong>30 días</strong>.
                  Para más información o agendar una visita técnica, contáctenos respondiendo
                  este correo o llamando a nuestras líneas de atención.
                </p>
              </div>
              <div class="footer">
                ELECTROENERGY S.A.S. — Sistema de Gestión de Cotizaciones<br>
                Este correo fue generado automáticamente. Por favor no responda directamente.
              </div>
            </div>
            </body></html>
            """.formatted(
                nombre,
                codigo,
                c.getSolicitud().getCodigoSeguimiento(),
                c.getNumPaneles(),
                c.getPotenciaPanelW(),
                c.getPotenciaSistemaKw(),
                fmt.format(c.getConsumoMensualKwh()),
                c.getHorasPicoSolar(),
                c.getEficienciaSistema(),
                fmt.format(c.getCostoPaneles()),
                fmt.format(c.getCostoInstalacion()),
                fmt.format(c.getCostoTotal()),
                fmt.format(c.getAhorroMensualEstimado()),
                c.getTiempoRetornoMeses(),
                String.format("%.1f", c.getTiempoRetornoMeses() / 12.0),
                fmt.format(c.getReduccionCo2KgAnual())
        );
    }
}
