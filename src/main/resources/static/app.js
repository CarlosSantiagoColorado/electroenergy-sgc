const API = 'http://localhost:8080/api/solicitudes';

// ── Registrar solicitud ──────────────────────────────────────────────────────
async function registrarSolicitud() {
    const nombre      = document.getElementById('nombre').value.trim();
    const tipoCliente = document.getElementById('tipoCliente').value;
    const correo      = document.getElementById('correo').value.trim();
    const telefono    = document.getElementById('telefono').value.trim();
    const direccion   = document.getElementById('direccion').value.trim();
    const consumoKwh  = document.getElementById('consumoKwh').value;
    const valorFactura= document.getElementById('valorFactura').value;
    const observaciones = document.getElementById('observaciones').value.trim();

    // Limpiar errores
    document.getElementById('error-msg').textContent = '';
    ['nombre','tipoCliente','correo','telefono','direccion','consumoKwh','valorFactura']
        .forEach(id => document.getElementById(id).classList.remove('error'));

    // Validar campos obligatorios
    let errores = [];
    if (!nombre)       { errores.push('Nombre'); document.getElementById('nombre').classList.add('error'); }
    if (!tipoCliente)  { errores.push('Tipo de cliente'); document.getElementById('tipoCliente').classList.add('error'); }
    if (!correo)       { errores.push('Correo'); document.getElementById('correo').classList.add('error'); }
    if (!telefono)     { errores.push('Teléfono'); document.getElementById('telefono').classList.add('error'); }
    if (!direccion)    { errores.push('Dirección'); document.getElementById('direccion').classList.add('error'); }
    if (!consumoKwh)   { errores.push('Consumo kWh'); document.getElementById('consumoKwh').classList.add('error'); }
    if (!valorFactura) { errores.push('Valor factura'); document.getElementById('valorFactura').classList.add('error'); }

    if (errores.length > 0) {
        document.getElementById('error-msg').textContent =
            'Campos obligatorios faltantes: ' + errores.join(', ');
        return;
    }

    const solicitud = {
        nombre, tipoCliente, correo, telefono, direccion,
        consumoKwh: parseFloat(consumoKwh),
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

// ── Consultar estado ─────────────────────────────────────────────────────────
async function consultarEstado() {
    const codigo = document.getElementById('codigoBusqueda').value.trim();
    const div    = document.getElementById('resultado-consulta');

    if (!codigo) {
        div.innerHTML = '<p style="color:#DC2626;font-size:13px;margin-top:10px">Ingresa un código de seguimiento.</p>';
        return;
    }

    try {
        const response = await fetch(`${API}/${codigo}`);

        if (response.ok) {
            const s = await response.json();
            div.innerHTML = `
                <div class="resultado-card">
                    <h3>📋 Solicitud ${s.codigoSeguimiento}</h3>
                    <div class="resultado-row"><label>Cliente</label><span>${s.nombre}</span></div>
                    <div class="resultado-row"><label>Tipo</label><span>${s.tipoCliente}</span></div>
                    <div class="resultado-row"><label>Correo</label><span>${s.correo}</span></div>
                    <div class="resultado-row"><label>Teléfono</label><span>${s.telefono}</span></div>
                    <div class="resultado-row"><label>Dirección</label><span>${s.direccion}</span></div>
                    <div class="resultado-row"><label>Consumo (kWh)</label><span>${s.consumoKwh}</span></div>
                    <div class="resultado-row"><label>Valor Factura</label><span>$ ${Number(s.valorFactura).toLocaleString('es-CO')}</span></div>
                    <div class="resultado-row"><label>Estado</label><span class="estado-badge">${s.estado}</span></div>
                    <div class="resultado-row"><label>Fecha Registro</label><span>${formatFecha(s.fechaRegistro)}</span></div>
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
        .forEach(id => document.getElementById(id).value = '');
    document.getElementById('tipoCliente').value = '';
    document.getElementById('error-msg').textContent = '';
    ['nombre','tipoCliente','correo','telefono','direccion','consumoKwh','valorFactura']
        .forEach(id => document.getElementById(id).classList.remove('error'));
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