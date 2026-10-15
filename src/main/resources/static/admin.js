// ===== ESTADO GLOBAL =====
let todasLasSolicitudes = [];
let solicitudEditandoId = null;
let usuarioEditandoId = null;

// ===== INICIO =====
document.addEventListener('DOMContentLoaded', () => {
    actualizarFecha();
    cargarDashboard();
});

function actualizarFecha() {
    const ahora = new Date();
    const opciones = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById('topbarDate').textContent =
        ahora.toLocaleDateString('es-CO', opciones);
}

// ===== NAVEGACIÓN =====
function mostrarSeccion(nombre) {
    document.querySelectorAll('.seccion').forEach(s => s.classList.remove('activa'));
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.getElementById('seccion-' + nombre).classList.add('activa');
    document.querySelector(`[onclick="mostrarSeccion('${nombre}')"]`).classList.add('active');

    const titulos = {
        dashboard: 'Dashboard',
        solicitudes: 'Gestión de Solicitudes',
        usuarios: 'Gestión de Usuarios',
        parametros: 'Parámetros del Sistema',
        cotizaciones: 'Cotizaciones'
    };
    document.getElementById('topbarTitle').textContent = titulos[nombre] || nombre;

    if (nombre === 'solicitudes') cargarSolicitudes();
    if (nombre === 'usuarios') cargarUsuarios();
    if (nombre === 'parametros') mostrarResumenParametros();
    if (nombre === 'cotizaciones') cargarCotizaciones();
}

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('colapsado');
    document.getElementById('mainContent').classList.toggle('expandido');
}

// ===== DASHBOARD =====
async function cargarDashboard() {
    try {
        const [solicitudes, usuarios] = await Promise.all([
            fetch('/api/solicitudes').then(r => r.json()),
            fetch('/api/usuarios').then(r => r.json()).catch(() => [])
        ]);

        todasLasSolicitudes = solicitudes;

        // KPIs
        document.getElementById('kpiTotal').textContent = solicitudes.length;
        document.getElementById('kpiPendientes').textContent =
            solicitudes.filter(s => s.estado === 'Pendiente').length;
        document.getElementById('kpiAprobadas').textContent =
            solicitudes.filter(s => s.estado === 'Aprobada').length;
        document.getElementById('kpiUsuarios').textContent =
            usuarios.filter(u => u.estado === 'Activo').length;

        // Últimas 5 solicitudes
        const ultimas = [...solicitudes].reverse().slice(0, 5);
        document.getElementById('tbodyUltimas').innerHTML = ultimas.length
            ? ultimas.map(s => `
                <tr>
                    <td><code>${s.codigoSeguimiento}</code></td>
                    <td>${s.nombre}</td>
                    <td>${badgeEstado(s.estado)}</td>
                    <td>${formatFecha(s.fechaSolicitud)}</td>
                </tr>`).join('')
            : '<tr><td colspan="4" class="cargando">Sin solicitudes aún</td></tr>';

        // Gráfico de barras por estado
        renderChart(solicitudes);

    } catch (e) {
        console.error('Error cargando dashboard:', e);
    }
}

function renderChart(solicitudes) {
    const estados = ['Pendiente', 'En revisión', 'Aprobada', 'Rechazada', 'Cotización enviada'];
    const colores = ['#FF9800', '#2196F3', '#4CAF50', '#f44336', '#00BCD4'];
    const total = solicitudes.length || 1;

    document.getElementById('estadosChart').innerHTML = estados.map((estado, i) => {
        const count = solicitudes.filter(s => s.estado === estado).length;
        const pct = Math.round((count / total) * 100);
        return `
            <div class="chart-fila">
                <div class="chart-label">
                    <span>${estado}</span>
                    <span><strong>${count}</strong> (${pct}%)</span>
                </div>
                <div class="chart-bar">
                    <div class="chart-fill" style="width:${pct}%;background:${colores[i]}"></div>
                </div>
            </div>`;
    }).join('');
}

// ===== SOLICITUDES =====
async function cargarSolicitudes() {
    try {
        const resp = await fetch('/api/solicitudes');
        todasLasSolicitudes = await resp.json();
        renderTablaSolicitudes(todasLasSolicitudes);
    } catch (e) {
        mostrarToast('Error cargando solicitudes', true);
    }
}

function renderTablaSolicitudes(lista) {
    const tbody = document.getElementById('tbodySolicitudes');
    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="9" class="cargando">No hay solicitudes que mostrar</td></tr>';
        return;
    }
    tbody.innerHTML = lista.map(s => `
        <tr>
            <td><code>${s.codigoSeguimiento}</code></td>
            <td>${s.nombre}</td>
            <td>${s.correo}</td>
            <td>${s.telefono}</td>
            <td>${s.tipoCliente}</td>
            <td>${s.consumoKwh} kWh</td>
            <td>${badgeEstado(s.estado)}</td>
            <td>${formatFecha(s.fechaSolicitud)}</td>
            <td>
                <div class="acciones-td">
                    <button class="btn-accion" title="Ver detalle"
                        onclick="verDetalle(${s.id})">👁️</button>
                    <button class="btn-accion" title="Validar técnicamente"
                        onclick="validarSolicitud(${s.id}, '${s.codigoSeguimiento}')">✅</button>
                    <button class="btn-accion" title="Generar cotización"
                        onclick="generarCotizacion(${s.id}, '${s.codigoSeguimiento}')">📄</button>
                    <button class="btn-accion" title="Cambiar estado"
                        onclick="abrirModalEstado(${s.id}, '${s.codigoSeguimiento}', '${s.estado}')">🔄</button>
                    <button class="btn-accion" title="Datos faltantes"
                        onclick="abrirModalObservacion(${s.id}, '${s.codigoSeguimiento}', '${s.nombre}')">💬</button>
                </div>
            </td>
        </tr>`).join('');
}

function filtrarSolicitudes() {
    const estado = document.getElementById('filtroEstado').value;
    const texto  = document.getElementById('buscarSolicitud').value.toLowerCase();
    const filtrado = todasLasSolicitudes.filter(s => {
        const matchEstado = !estado || s.estado === estado;
        const matchTexto  = !texto ||
            s.codigoSeguimiento.toLowerCase().includes(texto) ||
            s.nombre.toLowerCase().includes(texto) ||
            s.correo.toLowerCase().includes(texto);
        return matchEstado && matchTexto;
    });
    renderTablaSolicitudes(filtrado);
}

function abrirModalEstado(id, codigo, estadoActual) {
    solicitudEditandoId = id;
    document.getElementById('modalSolicitudInfo').textContent =
        `Solicitud: ${codigo}`;
    document.getElementById('selectNuevoEstado').value = estadoActual;
    document.getElementById('modalEstado').style.display = 'flex';
}

async function confirmarCambioEstado() {
    const nuevoEstado = document.getElementById('selectNuevoEstado').value;
    try {
        const resp = await fetch(`/api/solicitudes/${solicitudEditandoId}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        });
        if (!resp.ok) throw new Error();
        cerrarModal('modalEstado');
        mostrarToast('Estado actualizado correctamente ✅');
        cargarSolicitudes();
        cargarDashboard();
    } catch (e) {
        mostrarToast('Error al actualizar estado', true);
    }
}

function verDetalle(id) {
    const s = todasLasSolicitudes.find(x => x.id === id);
    if (!s) return;
    alert(`📋 DETALLE SOLICITUD\n\n` +
        `Código: ${s.codigoSeguimiento}\n` +
        `Cliente: ${s.nombre}\n` +
        `Correo: ${s.correo}\n` +
        `Teléfono: ${s.telefono}\n` +
        `Dirección: ${s.direccion}\n` +
        `Tipo: ${s.tipoCliente}\n` +
        `Consumo: ${s.consumoKwh} kWh/mes\n` +
        `Valor factura: $${Number(s.valorFactura).toLocaleString('es-CO')}\n` +
        `Observaciones: ${s.observaciones || 'Ninguna'}\n` +
        `Estado: ${s.estado}\n` +
        `Fecha: ${formatFecha(s.fechaSolicitud)}`);
}

// ===== USUARIOS =====
async function cargarUsuarios() {
    try {
        const resp = await fetch('/api/usuarios');
        const usuarios = await resp.json();
        const tbody = document.getElementById('tbodyUsuarios');
        if (!usuarios.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="cargando">No hay usuarios registrados</td></tr>';
            return;
        }
        tbody.innerHTML = usuarios.map(u => `
            <tr>
                <td>${u.nombre}</td>
                <td>${u.correo}</td>
                <td><span class="estado-badge" style="background:#e8f5e9;color:#2e7d32">${u.rol}</span></td>
                <td>${u.estado === 'Activo'
                    ? '<span class="estado-badge estado-Aprobada">Activo</span>'
                    : '<span class="estado-badge estado-Rechazada">Inactivo</span>'}</td>
                <td>${formatFecha(u.fechaCreacion)}</td>
                <td>
                    <div class="acciones-td">
                        <button class="btn-accion" title="Editar"
                            onclick="editarUsuario(${u.id}, '${u.nombre}', '${u.correo}', '${u.rol}')">
                            ✏️
                        </button>
                        ${u.estado === 'Activo'
                            ? `<button class="btn-accion" title="Desactivar"
                                onclick="desactivarUsuario(${u.id}, '${u.nombre}')">🚫</button>`
                            : ''}
                    </div>
                </td>
            </tr>`).join('');
    } catch (e) {
        mostrarToast('Error cargando usuarios', true);
    }
}

function abrirModalUsuario() {
    usuarioEditandoId = null;
    document.getElementById('modalUsuarioTitulo').textContent = 'Nuevo Usuario';
    document.getElementById('usuarioId').value = '';
    document.getElementById('usuarioNombre').value = '';
    document.getElementById('usuarioCorreo').value = '';
    document.getElementById('usuarioRol').value = 'Asesor Comercial';
    document.getElementById('usuarioCorreo').disabled = false;
    document.getElementById('modalUsuario').style.display = 'flex';
}

function editarUsuario(id, nombre, correo, rol) {
    usuarioEditandoId = id;
    document.getElementById('modalUsuarioTitulo').textContent = 'Editar Usuario';
    document.getElementById('usuarioNombre').value = nombre;
    document.getElementById('usuarioCorreo').value = correo;
    document.getElementById('usuarioRol').value = rol;
    document.getElementById('usuarioCorreo').disabled = true;
    document.getElementById('modalUsuario').style.display = 'flex';
}

async function guardarUsuario() {
    const nombre = document.getElementById('usuarioNombre').value.trim();
    const correo = document.getElementById('usuarioCorreo').value.trim();
    const rol    = document.getElementById('usuarioRol').value;

    if (!nombre || (!usuarioEditandoId && !correo)) {
        mostrarToast('Completa todos los campos', true);
        return;
    }

    try {
        let resp;
        if (usuarioEditandoId) {
            resp = await fetch(`/api/usuarios/${usuarioEditandoId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nombre, correo, rol, estado: 'Activo' })
            });
        } else {
            resp = await fetch('/api/usuarios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nombre, correo, rol })
            });
        }
        if (!resp.ok) throw new Error();
        cerrarModal('modalUsuario');
        mostrarToast(usuarioEditandoId ? 'Usuario actualizado ✅' : 'Usuario creado ✅');
        cargarUsuarios();
        cargarDashboard();
    } catch (e) {
        mostrarToast('Error guardando usuario', true);
    }
}

async function desactivarUsuario(id, nombre) {
    if (!confirm(`¿Desactivar al usuario "${nombre}"?`)) return;
    try {
        const resp = await fetch(`/api/usuarios/${id}/desactivar`, { method: 'PUT' });
        if (!resp.ok) throw new Error();
        mostrarToast('Usuario desactivado');
        cargarUsuarios();
        cargarDashboard();
    } catch (e) {
        mostrarToast('Error desactivando usuario', true);
    }
}

// ===== PARÁMETROS =====
function guardarParametros() {
    const params = {
        precioPanel:    document.getElementById('paramPrecioPanel').value,
        precioKwh:      document.getElementById('paramPrecioKwh').value,
        hps:            document.getElementById('paramHPS').value,
        eficiencia:     document.getElementById('paramEficiencia').value,
        potenciaPanel:  document.getElementById('paramPotenciaPanel').value
    };
    localStorage.setItem('electroenergy_params', JSON.stringify(params));
    mostrarToast('Parámetros guardados ✅');
    mostrarResumenParametros();
}

function mostrarResumenParametros() {
    const saved = localStorage.getItem('electroenergy_params');
    const p = saved ? JSON.parse(saved) : {
        precioPanel: 250, precioKwh: 650, hps: 4.5,
        eficiencia: 80, potenciaPanel: 400
    };
    if (saved) {
        document.getElementById('paramPrecioPanel').value  = p.precioPanel;
        document.getElementById('paramPrecioKwh').value    = p.precioKwh;
        document.getElementById('paramHPS').value          = p.hps;
        document.getElementById('paramEficiencia').value   = p.eficiencia;
        document.getElementById('paramPotenciaPanel').value= p.potenciaPanel;
    }
    document.getElementById('resumenParametros').innerHTML = `
        <div class="resumen-item"><span>Precio panel solar</span><span>USD ${p.precioPanel}</span></div>
        <div class="resumen-item"><span>Precio por kWh</span><span>COP $${Number(p.precioKwh).toLocaleString('es-CO')}</span></div>
        <div class="resumen-item"><span>Horas Pico Solar</span><span>${p.hps} h/día</span></div>
        <div class="resumen-item"><span>Eficiencia sistema</span><span>${p.eficiencia}%</span></div>
        <div class="resumen-item"><span>Potencia por panel</span><span>${p.potenciaPanel} W</span></div>
        <div class="resumen-item" style="margin-top:12px;border-top:2px solid #e0ecd8;padding-top:12px">
            <span>Capacidad mínima estimada (100 kWh/mes)</span>
            <span>${Math.ceil((100 / (p.hps * (p.eficiencia/100) * 30)) / (p.potenciaPanel/1000))} paneles</span>
        </div>`;
}

// ===== UTILIDADES =====
function badgeEstado(estado) {
    const clase = estado.replace(/\s/g, '.');
    return `<span class="estado-badge estado-${clase}">${estado}</span>`;
}

function formatFecha(fecha) {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-CO',
        { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function cerrarModal(id) {
    document.getElementById(id).style.display = 'none';
}

function mostrarToast(msg, esError = false) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = 'toast' + (esError ? ' error' : '');
    toast.classList.add('visible');
    setTimeout(() => toast.classList.remove('visible'), 3000);
}
// ===== F-02: COTIZACIONES =====
async function generarCotizacion(solicitudId, codigo) {
    if (!confirm(`¿Generar cotización automática para la solicitud ${codigo}?`)) return;
    try {
        const resp = await fetch(`/api/cotizaciones/generar/${solicitudId}`, {
            method: 'POST'
        });
        if (!resp.ok) {
            const msg = await resp.text();
            mostrarToast('Error: ' + msg, true);
            return;
        }
        const cot = await resp.json();
        mostrarToast(`Cotización ${cot.codigoCotizacion} generada ✅`);
        cargarSolicitudes();
        mostrarDetalleCotizacion(cot);
    } catch (e) {
        mostrarToast('Error generando cotización', true);
    }
}

function mostrarDetalleCotizacion(c) {
    const s = c.solicitud;
    const fmt = n => Number(n).toLocaleString('es-CO', { minimumFractionDigits: 0 });
    alert(
        `📄 COTIZACIÓN GENERADA\n` +
        `━━━━━━━━━━━━━━━━━━━━━━━\n` +
        `Código: ${c.codigoCotizacion}\n` +
        `Cliente: ${s.nombre}\n\n` +
        `⚡ SISTEMA SOLAR\n` +
        `• Paneles requeridos: ${c.numPaneles} (${c.potenciaPanelW}W c/u)\n` +
        `• Potencia total: ${c.potenciaSistemaKw} kW\n` +
        `• Consumo cubierto: ${fmt(c.consumoMensualKwh)} kWh/mes\n\n` +
        `💰 INVERSIÓN\n` +
        `• Paneles: $${fmt(c.costoPaneles)} COP\n` +
        `• Instalación: $${fmt(c.costoInstalacion)} COP\n` +
        `• TOTAL: $${fmt(c.costoTotal)} COP\n\n` +
        `📈 RETORNO\n` +
        `• Ahorro mensual: $${fmt(c.ahorroMensualEstimado)} COP\n` +
        `• Retorno inversión: ${c.tiempoRetornoMeses} meses\n` +
        `• CO₂ evitado: ${fmt(c.reduccionCo2KgAnual)} kg/año`
    );
}
async function cargarCotizaciones() {
    const tbody = document.getElementById('tbodyCotizaciones');
    tbody.innerHTML = '<tr><td colspan="11" class="cargando">Cargando...</td></tr>';
    try {
        const resp = await fetch('/api/cotizaciones');
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        const lista = await resp.json();
        const fmt = n => Number(n).toLocaleString('es-CO', { minimumFractionDigits: 0 });

        if (!lista || lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="11" class="cargando">No hay cotizaciones aún. Genera una desde Solicitudes usando el botón 📄</td></tr>';
            return;
        }
        tbody.innerHTML = lista.map(c => {
            const clienteNombre = c.solicitud ? c.solicitud.nombre : '—';
            return `
            <tr>
                <td><code>${c.codigoCotizacion || '—'}</code></td>
                <td>${clienteNombre}</td>
                <td>${c.numPaneles} × ${c.potenciaPanelW}W</td>
                <td>${c.potenciaSistemaKw} kW</td>
                <td>$${fmt(c.costoTotal)}</td>
                <td>$${fmt(c.ahorroMensualEstimado)}/mes</td>
                <td>${c.tiempoRetornoMeses} meses</td>
                <td>${fmt(c.reduccionCo2KgAnual)} kg</td>
                <td>${badgeEstado(c.estado)}</td>
                <td>${formatFecha(c.fechaGeneracion)}</td>
                <td>
                    ${c.estado !== 'Enviada'
                        ? `<button class="btn-accion btn-enviar"
                               data-id="${c.id}"
                               data-codigo="${c.codigoCotizacion}"
                               title="Enviar al cliente">📧</button>`
                        : '<span style="color:#2e7d32;font-size:0.8rem">✅ Enviada</span>'
                    }
                </td>
            </tr>`;
        }).join('');

        setTimeout(() => {
            tbody.querySelectorAll('.btn-enviar').forEach(btn => {
                btn.addEventListener('click', () => {
                    enviarCotizacion(btn.dataset.id, btn.dataset.codigo);
                });
            });
        }, 0);

    } catch (e) {
        console.error('Error cotizaciones:', e);
        tbody.innerHTML = `<tr><td colspan="11" class="cargando" style="color:#c62828">Error: ${e.message}</td></tr>`;
    }
}
 // ===== F-03: VALIDACIÓN TÉCNICA =====
 async function validarSolicitud(solicitudId, codigo) {
     if (!confirm(`¿Validar técnicamente la solicitud ${codigo}?`)) return;
     try {
         const resp = await fetch(`/api/validaciones/validar/${solicitudId}`, {
             method: 'POST'
         });
         if (!resp.ok) throw new Error('HTTP ' + resp.status);
         const v = await resp.json();

         const iconos = {
             'APROBADA':      '✅',
             'OBSERVACIONES': '⚠️',
             'RECHAZADA':     '❌'
         };
         const icono = iconos[v.resultado] || '📋';

         alert(
             `${icono} VALIDACIÓN TÉCNICA — ${codigo}\n` +
             `━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n` +
             `Resultado: ${v.resultado}\n` +
             `Viable: ${v.esViable ? 'Sí' : 'No'}\n\n` +
             `Verificaciones:\n` +
             `• Consumo kWh: ${v.consumoValido ? '✅' : '❌'}\n` +
             `• Valor factura: ${v.facturaValida ? '✅' : '❌'}\n` +
             `• Tipo cliente: ${v.tipoClienteValido ? '✅' : '❌'}\n` +
             `• Coherencia consumo/factura: ${v.relacionConsumoFacturaOk ? '✅' : '⚠️'}\n\n` +
             `Detalle:\n${v.detalle}`
         );

         mostrarToast(`Validación: ${v.resultado} ${icono}`);
         cargarSolicitudes();
     } catch (e) {
         mostrarToast('Error en validación: ' + e.message, true);
     }

 }
 // ===== F-04: GESTIÓN DE DATOS FALTANTES =====
 let solicitudObsId = null;

 async function abrirModalObservacion(solicitudId, codigo, nombre) {
     solicitudObsId = solicitudId;
     document.getElementById('modalObsInfo').textContent =
         `Solicitud: ${codigo} — ${nombre}`;
     document.getElementById('obsDescripcion').value = '';
     document.getElementById('modalObservacion').style.display = 'flex';
     await cargarObservaciones(solicitudId);
 }

 async function cargarObservaciones(solicitudId) {
     try {
         const resp = await fetch(`/api/observaciones/solicitud/${solicitudId}`);
         const lista = await resp.json();
         const contenedor = document.getElementById('listaObservaciones');

         if (!lista.length) {
             contenedor.innerHTML = '<p style="color:#999;font-size:0.85rem;text-align:center">Sin observaciones registradas aún</p>';
             return;
         }

         const colores = {
             'Pendiente':  '#fff3cd',
             'Respondida': '#d1e7dd',
             'Cerrada':    '#f0f4f0'
         };

         contenedor.innerHTML = lista.map(o => `
             <div style="background:${colores[o.estadoObservacion] || '#f9f9f9'};
                         border-radius:8px;padding:12px;margin-bottom:8px;font-size:0.85rem">
                 <div style="display:flex;justify-content:space-between;margin-bottom:4px">
                     <strong>${o.tipo.replace('_',' ')}</strong>
                     <span style="color:#666">${o.estadoObservacion}</span>
                 </div>
                 ${o.campoAfectado ? `<div style="color:#666;margin-bottom:4px">Campo: ${o.campoAfectado}</div>` : ''}
                 <div style="margin-bottom:6px">${o.descripcion}</div>
                 ${o.respuestaCliente
                     ? `<div style="border-top:1px solid rgba(0,0,0,0.1);padding-top:6px;color:#2e7d32">
                         💬 Respuesta: ${o.respuestaCliente}</div>`
                     : o.estadoObservacion === 'Pendiente'
                         ? `<div style="margin-top:6px;display:flex;gap:6px">
                             <input type="text" id="resp_${o.id}" placeholder="Respuesta del cliente..."
                                 style="flex:1;padding:5px 8px;border:1px solid #c8dfc8;border-radius:6px;font-size:0.82rem">
                             <button class="btn-primario" style="padding:5px 10px;font-size:0.8rem"
                                 onclick="responderObservacion(${o.id})">OK</button>
                            </div>`
                         : ''
                 }
             </div>`).join('');
     } catch (e) {
         console.error('Error cargando observaciones:', e);
     }
 }

 async function guardarObservacion() {
     const descripcion = document.getElementById('obsDescripcion').value.trim();
     if (!descripcion) {
         mostrarToast('Escribe una descripción', true);
         return;
     }
     try {
         const resp = await fetch(`/api/observaciones/solicitud/${solicitudObsId}`, {
             method: 'POST',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({
                 tipo:          document.getElementById('obsTipo').value,
                 campoAfectado: document.getElementById('obsCampo').value,
                 descripcion:   descripcion
             })
         });
         if (!resp.ok) throw new Error();
         mostrarToast('Observación registrada ✅');
         document.getElementById('obsDescripcion').value = '';
         await cargarObservaciones(solicitudObsId);
         cargarSolicitudes();
     } catch (e) {
         mostrarToast('Error registrando observación', true);
     }
 }

 async function responderObservacion(obsId) {
     const respuesta = document.getElementById(`resp_${obsId}`).value.trim();
     if (!respuesta) {
         mostrarToast('Escribe una respuesta', true);
         return;
     }
     try {
         const resp = await fetch(`/api/observaciones/${obsId}/responder`, {
             method: 'PUT',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({ respuesta })
         });
         if (!resp.ok) throw new Error();
         mostrarToast('Respuesta guardada ✅');
         await cargarObservaciones(solicitudObsId);
         cargarSolicitudes();
     } catch (e) {
         mostrarToast('Error guardando respuesta', true);
     }
 }
 // ===== F-05: ENVIAR COTIZACIÓN =====
 async function enviarCotizacion(cotizacionId, codigo) {
     if (!confirm(`¿Enviar la cotización ${codigo} al cliente por correo?`)) return;
     mostrarToast('Enviando correo...');
     try {
         const resp = await fetch(`/api/cotizaciones/${cotizacionId}/enviar`, {
             method: 'POST'
         });
         const data = await resp.json();
         if (!resp.ok) throw new Error(data);
         mostrarToast(`✅ ${data.mensaje}`);
         cargarCotizaciones();
         cargarDashboard();
     } catch (e) {
         mostrarToast('Error al enviar: ' + e.message, true);
     }
 }