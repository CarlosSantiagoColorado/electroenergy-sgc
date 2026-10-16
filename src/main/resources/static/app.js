const API = 'http://localhost:8080/api/solicitudes';

// ── Helpers de validación en tiempo real ─────────────────────────────────────

/** Elimina cualquier carácter que no sea letra o espacio (incluye tildes) */
function soloLetras(input) {
    input.value = input.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]/g, '');
}

/** Elimina cualquier carácter que no sea dígito */
function soloDigitos(input) {
    input.value = input.value.replace(/\D/g, '');
}

/** Impide valores negativos o cero en campos numéricos */
function soloPositivo(input) {
    if (input.value !== '' && Number(input.value) < 0) {
        input.value = '';
    }
}

/** Valida formato de email al perder el foco */
function validarCorreo(input) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (input.value && !re.test(input.value)) {
        input.classList.add('error');
        mostrarErrorCampo(input, 'Formato de correo inválido');
    } else {
        input.classList.remove('error');
        quitarErrorCampo(input);
    }
}

function mostrarErrorCampo(input, msg) {
    let hint = input.parentElement.querySelector('.field-error');
    if (!hint) {
        hint = document.createElement('span');
        hint.className = 'field-error';
        hint.style.cssText = 'color:#DC2626;font-size:11px;display:block;margin-top:3px';
        input.parentElement.appendChild(hint);
    }
    hint.textContent = msg;
}

function quitarErrorCampo(input) {
    const hint = input.parentElement.querySelector('.field-error');
    if (hint) hint.remove();
}

// ── Registrar solicitud ──────────────────────────────────────────────────────
async function registrarSolicitud() {
    const nombre       = document.getElementById('nombre').value.trim();
    const tipoCliente  = document.getElementById('tipoCliente').value;
    const correo       = document.getElementById('correo').value.trim();
    const telefono     = document.getElementById('telefono').value.trim();
    const direccion    = document.getElementById('direccion').value.trim();
    const consumoKwh   = document.getElementById('consumoKwh').value;
    const valorFactura = document.getElementById('valorFactura').value;
    const observaciones = document.getElementById('observaciones').value.trim();

    // Limpiar errores previos
    document.getElementById('error-msg').textContent = '';
    ['nombre','tipoCliente','correo','telefono','direccion','consumoKwh','valorFactura']
        .forEach(id => {
            document.getElementById(id).classList.remove('error');
            quitarErrorCampo(document.getElementById(id));
        });

    let errores = [];

    // Nombre: obligatorio, solo letras, mínimo 3 caracteres
    if (!nombre) {
        errores.push('Nombre');
        document.getElementById('nombre').classList.add('error');
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]{3,}$/.test(nombre)) {
        errores.push('Nombre (solo letras, mínimo 3 caracteres)');
        document.getElementById('nombre').classList.add('error');
        mostrarErrorCampo(document.getElementById('nombre'), 'Solo letras, mínimo 3 caracteres');
    }

    // Tipo de cliente
    if (!tipoCliente) {
        errores.push('Tipo de cliente');
        document.getElementById('tipoCliente').classList.add('error');
    }

    // Correo: obligatorio y formato válido
    const reCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!correo) {
        errores.push('Correo');
        document.getElementById('correo').classList.add('error');
    } else if (!reCorreo.test(correo)) {
        errores.push('Correo (formato inválido)');
        document.getElementById('correo').classList.add('error');
        mostrarErrorCampo(document.getElementById('correo'), 'Formato inválido: correo@ejemplo.com');
    }

    // Teléfono: obligatorio, solo dígitos, exactamente 10
    if (!telefono) {
        errores.push('Teléfono');
        document.getElementById('telefono').classList.add('error');
    } else if (!/^\d{10}$/.test(telefono)) {
        errores.push('Teléfono (10 dígitos)');
        document.getElementById('telefono').classList.add('error');
        mostrarErrorCampo(document.getElementById('telefono'), 'Debe tener exactamente 10 dígitos');
    }

    // Dirección: obligatorio, mínimo 5 caracteres
    if (!direccion) {
        errores.push('Dirección');
        document.getElementById('direccion').classList.add('error');
    } else if (direccion.length < 5) {
        errores.push('Dirección (mínimo 5 caracteres)');
        document.getElementById('direccion').classList.add('error');
        mostrarErrorCampo(document.getElementById('direccion'), 'Mínimo 5 caracteres');
    }

    // Consumo kWh: obligatorio, número positivo
    if (!consumoKwh) {
        errores.push('Consumo kWh');
        document.getElementById('consumoKwh').classList.add('error');
    } else if (Number(consumoKwh) <= 0) {
        errores.push('Consumo kWh (debe ser mayor a 0)');
        document.getElementById('consumoKwh').classList.add('error');
        mostrarErrorCampo(document.getElementById('consumoKwh'), 'Debe ser mayor a 0');
    }

    // Valor factura: obligatorio, número positivo
    if (!valorFactura) {
        errores.push('Valor factura');
        document.getElementById('valorFactura').classList.add('error');
    } else if (Number(valorFactura) <= 0) {
        errores.push('Valor factura (debe ser mayor a 0)');
        document.getElementById('valorFactura').classList.add('error');
        mostrarErrorCampo(document.getElementById('valorFactura'), 'Debe ser mayor a 0');
    }

    // Consentimiento
    if (!document.getElementById('consentimiento').checked) {
        errores.push('Consentimiento de datos');
    }

    if (errores.length > 0) {
        document.getElementById('error-msg').textContent =
            'Corrige los siguientes campos: ' + errores.join(', ');
        return;
    }

    const solicitud = {
        nombre, tipoCliente, correo, telefono, direccion,
        consumoKwh:   parseFloat(consumoKwh),
        valorFactura: parseFloat(valorFactura),
        observaciones
    };

    try {
        const response = await fetch(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(solicitud)
        });

        if (response.ok) {
            const data = await response.json();
            document.getElementById('codigoGenerado').textContent = data.codigoSeguimiento;
            document.getElementById('seccion-formulario').style.display = 'none';
            document.getElementById('seccion-confirmacion').style.display = 'block';
            cargarSolicitudes();
        } else {
            document.getElementById('error-msg').textContent = 'Error al registrar. Intenta de nuevo.';
        }
    } catch (error) {
        document.getElementById('error-msg').textContent = 'No se pudo conectar con el servidor.';
    }
}

// ── Consultar estado ──────────────────────────────────────────────────────────
async function consultarEstado() {
    const codigo = document.getElementById('codigoBusqueda').value.trim();
    const div    = document.getElementById('resultado-consulta');

    if (!codigo) {
        div.innerHTML = '<p style="color:#DC2626;font-size:13px;margin-top:10px">Ingresa un código de seguimiento o correo.</p>';
        return;
    }

    try {
        const response = await fetch(`/api/solicitudes/consultar?q=${encodeURIComponent(codigo)}`);

        if (response.ok) {
            const d = await response.json();
            const fmt = n => Number(n).toLocaleString('es-CO', { minimumFractionDigits: 0 });
            div.innerHTML = `
                <div class="resultado-card">
                    <h3>📋 Solicitud ${d.codigoSeguimiento}</h3>
                    <div class="resultado-row"><label>Cliente</label><span>${d.nombre}</span></div>
                    <div class="resultado-row"><label>Correo</label><span>${d.correo}</span></div>
                    <div class="resultado-row"><label>Consumo (kWh)</label><span>${d.consumoKwh}</span></div>
                    <div class="resultado-row"><label>Estado</label><span class="estado-badge">${d.estado}</span></div>
                    <div class="resultado-row"><label>Fecha Registro</label><span>${formatFecha(d.fechaRegistro)}</span></div>
                    ${d.cotizacion ? `
                    <hr style="margin:16px 0;border-color:#e0e0e0">
                    <h4 style="color:#1a3c1a;margin-bottom:12px">📄 Cotización ${d.cotizacion.codigoCotizacion}</h4>
                    <div class="resultado-row"><label>Paneles</label><span>${d.cotizacion.numPaneles} × ${d.cotizacion.potenciaPanelW}W</span></div>
                    <div class="resultado-row"><label>Total inversión</label><span>$${fmt(d.cotizacion.costoTotal)}</span></div>
                    <div class="resultado-row"><label>Ahorro/mes</label><span>$${fmt(d.cotizacion.ahorroMensualEstimado)}</span></div>
                    <div class="resultado-row"><label>Tiempo de retorno</label><span>${d.cotizacion.tiempoRetornoMeses} meses</span></div>
                    <div class="resultado-row"><label>CO₂ evitado</label><span>${fmt(d.cotizacion.reduccionCo2KgAnual)} kg/año</span></div>
                    ` : `<div style="margin-top:12px;color:#777;font-size:0.9rem">⏳ Tu cotización está siendo preparada.</div>`}
                </div>`;
        } else {
            div.innerHTML = '<p style="color:#DC2626;font-size:13px;margin-top:10px">No se encontró ninguna solicitud con ese código.</p>';
        }
    } catch (error) {
        div.innerHTML = '<p style="color:#DC2626;font-size:13px;margin-top:10px">No se pudo conectar con el servidor.</p>';
    }
}

// ── Cargar todas las solicitudes ─────────────────────────────────────────────
async function cargarSolicitudes() {
    const div = document.getElementById('tabla-solicitudes');

    try {
        const response = await fetch(API);
        const lista    = await response.json();

        if (lista.length === 0) {
            div.innerHTML = '<p class="no-data">No hay solicitudes registradas aún.</p>';
            return;
        }

        div.innerHTML = `
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Código</th>
                        <th>Cliente</th>
                        <th>Tipo</th>
                        <th>Consumo (kWh)</th>
                        <th>Estado</th>
                        <th>Fecha</th>
                    </tr>
                </thead>
                <tbody>
                    ${lista.map(s => `
                    <tr>
                        <td><strong>${s.codigoSeguimiento}</strong></td>
                        <td>${s.nombre}</td>
                        <td>${s.tipoCliente}</td>
                        <td>${s.consumoKwh}</td>
                        <td><span class="estado-badge">${s.estado}</span></td>
                        <td>${formatFecha(s.fechaRegistro)}</td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
    } catch (error) {
        div.innerHTML = '<p class="no-data">No se pudo conectar con el servidor.</p>';
    }
}

// ── Utilidades ───────────────────────────────────────────────────────────────
function limpiarFormulario() {
    ['nombre','correo','telefono','direccion','consumoKwh','valorFactura','observaciones']
        .forEach(id => {
            document.getElementById(id).value = '';
            document.getElementById(id).classList.remove('error');
            quitarErrorCampo(document.getElementById(id));
        });
    document.getElementById('tipoCliente').value = '';
    document.getElementById('tipoCliente').classList.remove('error');
    document.getElementById('error-msg').textContent = '';
}

function nuevaSolicitud() {
    limpiarFormulario();
    document.getElementById('seccion-confirmacion').style.display = 'none';
    document.getElementById('seccion-formulario').style.display = 'block';
}

function formatFecha(fecha) {
    if (!fecha) return '—';
    const d = new Date(fecha);
    return d.toLocaleDateString('es-CO') + ' ' + d.toLocaleTimeString('es-CO', {hour:'2-digit', minute:'2-digit'});
}

// Cargar solicitudes al iniciar
window.onload = cargarSolicitudes;