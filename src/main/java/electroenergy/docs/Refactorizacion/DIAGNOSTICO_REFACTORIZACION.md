# Diagnóstico Técnico y Refactorización — ELECTROENERGY

## 1. Contexto

| Campo | Detalle |
|---|---|
| **Equipo** | Carlos Santiago Colorado Antury |
| **Repositorio** | https://github.com/[usuario]/electroenergy |
| **Proyecto** | ELECTROENERGY — Sistema de Gestión de Cotizaciones Solares |
| **Tecnología** | Java 21 · Spring Boot 4.0.8 · Spring Data JPA · MySQL 8.0 |
| **Arquitectura** | Capas (Controller → Service → Repository → Entity) + API REST |
| **Funcionalidad analizada** | F-05 — Enviar cotización al cliente |
| **Requisitos relacionados** | SWR-11, SWR-12 / PBI-05 (SCRUM) |

---

## 2. Arquitectura real de la funcionalidad

La funcionalidad F-05 involucra los siguientes componentes:

```
Frontend (admin.js)
    │  POST /api/cotizaciones/{id}/enviar
    ▼
CotizacionController          ← punto de entrada HTTP
    │  buscarPorId()
    ├─► CotizacionService      ← consulta en BD
    │  enviarCotizacion()
    └─► EmailService           ← envío SMTP
    │
    │  (lógica de estado aquí — debería estar en el Service)
    ▼
[cambios de estado en memoria — SIN persistencia]
```

**Flujo actual del endpoint `POST /{id}/enviar`:**

1. `CotizacionController` recupera la entidad `Cotizacion` mediante `CotizacionService.buscarPorId()`.
2. `CotizacionController` invoca directamente a `EmailService.enviarCotizacion()`.
3. `CotizacionController` modifica `c.setEstado("Enviada")`, `c.setFechaEnvio(LocalDateTime.now())` y `c.getSolicitud().setEstado("Cotización enviada")`.
4. **No se llama a ningún repositorio para persistir esos cambios.**
5. Se retorna `ResponseEntity.ok(...)` indicando éxito, aunque el estado en BD no cambió.

---

## 3. Hallazgos

---

### H-01 · Defecto funcional + Riesgo arquitectónico

| Campo | Detalle |
|---|---|
| **ID** | H-01 |
| **Tipo** | Defecto funcional / Riesgo arquitectónico |
| **Ubicación** | `electroenergy/controller/CotizacionController.java` · método `enviarCotizacion()` · líneas 59–76 |
| **Estado** | Seleccionado para refactorización |

**Evidencia:**

```java
// CotizacionController.java — líneas 59–76
@PostMapping("/{id}/enviar")
public ResponseEntity<?> enviarCotizacion(@PathVariable Long id) {
    try {
        Cotizacion c = cotizacionService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("La cotización no ha sido encontrada"));

        emailService.enviarCotizacion(c);           // llamada directa al servicio de correo

        c.setEstado("Enviada");                     // ← lógica de dominio en el controlador
        c.setFechaEnvio(LocalDateTime.now());       // ← lógica de dominio en el controlador
        c.getSolicitud().setEstado("Cotización enviada"); // ← modifica entidad relacionada

        return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Cotización enviada correctamente a " + c.getSolicitud().getCorreo(),
                "codigo",  c.getCodigoCotizacion()
        ));
        // ⚠ No hay cotizacionRepository.save(c) ni solicitudRepository.save()
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error al enviar: " + e.getMessage());
    }
}
```

**Observación:**

El método `enviarCotizacion` del controlador ejecuta tres responsabilidades que no le corresponden: (1) invoca directamente a `EmailService` sin pasar por `CotizacionService`, (2) aplica reglas de negocio —cambio de estado de la cotización y de la solicitud, registro de fecha de envío— directamente sobre las entidades, y (3) omite la persistencia de esos cambios. Como consecuencia, aunque el correo se envíe correctamente y el frontend reciba una respuesta `200 OK`, los campos `estado` y `fechaEnvio` de `Cotizacion` y el `estado` de `Solicitud` **nunca se actualizan en la base de datos**. La próxima consulta al panel de administración seguirá mostrando la cotización como "Generada" en lugar de "Enviada".

**Criterio afectado:**

- **SRP (Single Responsibility Principle):** el controlador asume responsabilidades del servicio de dominio.
- **Separación de capas:** la capa de presentación (Controller) contiene lógica de negocio que pertenece a la capa de servicio (Service).
- **Corrección funcional:** el estado persistido en BD es inconsistente con el estado real del proceso.

**Impacto y riesgo:**

- **Funcional (alto):** el administrador no puede distinguir cotizaciones enviadas de las pendientes en el panel, porque el estado en BD nunca cambia a "Enviada".
- **De datos (medio):** `fechaEnvio` siempre queda `null` en BD, lo que invalida cualquier auditoría o reporte de cumplimiento del plazo de 48 horas (SWR-11, SWR-12).
- **Arquitectónico (medio):** si otro componente necesita ejecutar el mismo flujo de envío, deberá duplicar la lógica de estado o depender también del controlador.

**Acción propuesta:**

Mover toda la lógica de negocio al método `enviarCotizacion(Long id)` de `CotizacionService`, incluyendo la llamada a `EmailService`, los cambios de estado y la persistencia. El controlador queda reducido a recibir la petición HTTP, delegar al servicio y retornar la respuesta.

**Diseño propuesto:**

```java
// CotizacionService.java — método nuevo
public Map<String, String> enviarCotizacion(Long cotizacionId) {
    Cotizacion c = cotizacionRepository.findById(cotizacionId)
            .orElseThrow(() -> new RuntimeException("La cotización no ha sido encontrada"));

    emailService.enviarCotizacion(c);

    c.setEstado("Enviada");
    c.setFechaEnvio(LocalDateTime.now());
    cotizacionRepository.save(c);                          // ← persistencia correcta

    c.getSolicitud().setEstado("Cotización enviada");
    solicitudRepository.save(c.getSolicitud());            // ← persistencia correcta

    return Map.of(
        "mensaje", "Cotización enviada correctamente a " + c.getSolicitud().getCorreo(),
        "codigo",  c.getCodigoCotizacion()
    );
}
```

```java
// CotizacionController.java — endpoint simplificado
@PostMapping("/{id}/enviar")
public ResponseEntity<?> enviarCotizacion(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(cotizacionService.enviarCotizacion(id));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error al enviar: " + e.getMessage());
    }
}
```

**Verificación del comportamiento:**

1. Antes de refactorizar: enviar una cotización y consultar en MySQL:
   ```sql
   SELECT estado, fecha_envio FROM cotizacion WHERE id = ?;
   SELECT estado FROM solicitud WHERE id = ?;
   ```
   → Resultado esperado (bug confirmado): `estado = 'Generada'`, `fecha_envio = NULL`.

2. Después de refactorizar: repetir la misma consulta.
   → Resultado esperado (correcto): `estado = 'Enviada'`, `fecha_envio` con timestamp.

3. Verificar que el correo sigue llegando al cliente (comportamiento externo sin cambio).
4. Verificar que el panel admin muestra la cotización como "Enviada" y el botón 📧 desaparece.

---

## 4. Priorización

| ID | Tipo | Impacto (1–3) | Riesgo (1–3) | Esfuerzo (1–3) | Seleccionado |
|---|---|:---:|:---:|:---:|:---:|
| H-01 | Defecto funcional + Riesgo arq. | 3 | 3 | 1 | ✅ |
| H-02 | *(pendiente — sesión 2)* | — | — | — | ⏳ |
| H-03 | *(pendiente — sesión 2)* | — | — | — | ⏳ |

**Razón de selección de H-01:** impacto máximo (datos incorrectos en BD), esfuerzo mínimo (mover ~8 líneas al servicio), y es verificable con una consulta SQL antes y después. Además es el único hallazgo que constituye un defecto funcional real, no solo deuda técnica.

---

## 5. Intervención

> ⚠️ *Pendiente de implementación — se ejecutará en la sesión 2 del taller.*

**Cambio elegido:** mover la lógica de negocio del endpoint `POST /{id}/enviar` del controlador al servicio.

**Archivos afectados:**
- `src/main/java/electroenergy/controller/CotizacionController.java`
- `src/main/java/electroenergy/service/CotizacionService.java`

**El comportamiento observable no cambia:** el frontend recibe el mismo JSON de respuesta, el correo se sigue enviando. Lo que cambia es que el estado se persiste correctamente en BD.

---

## 6. Verificación

> ⏳ *Pendiente — sesión 2.*

---

## 7. Cierre

> ⏳ *Pendiente — sesión 2.*

**Deuda técnica identificada (no bloqueante):**

- Los parámetros técnicos de cálculo (`HPS`, `EFICIENCIA`, `PRECIO_PANEL_USD`, `TRM`) están hardcodeados como constantes en `CotizacionService`. Deberían externalizarse a `application.properties` o a una tabla de configuración en BD para poder actualizarse sin recompilar.
- El código `generarCodigo()` usa `cotizacionRepository.count() + 1`, lo que puede generar códigos duplicados si dos solicitudes se procesan simultáneamente (race condition).

---

## 8. Git

| Commit | Hash | Descripción |
|---|---|---|
| Diagnóstico | *(pendiente)* | `docs: registrar diagnostico tecnico H-01` |
| Refactorización | *(pendiente — sesión 2)* | `refactor: mover logica de envio al servicio` |
| Verificación | *(pendiente — sesión 2)* | `docs: documentar verificacion de refactorizacion` |
