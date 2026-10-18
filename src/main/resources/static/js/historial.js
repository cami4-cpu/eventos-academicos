const ETIQUETAS = {
  EVENTO_CREADO: 'Evento creado',
  EVENTO_EDITADO: 'Evento editado',
  EVENTO_PUBLICADO: 'Evento publicado',
  EVENTO_CERRADO: 'Evento cerrado',
  EVENTO_FINALIZADO: 'Evento finalizado',
  EVENTO_CANCELADO: 'Evento cancelado',
  EVENTO_ELIMINADO: 'Evento eliminado',
  INSCRIPCION_CREADA: 'Inscripción creada',
  INSCRIPCION_CANCELADA: 'Inscripción cancelada',
  ASISTENCIA_REGISTRADA: 'Asistencia registrada',
  ACCESO_NO_AUTORIZADO: 'Acceso no autorizado'
};

const filas = $('filas');
const fAccion = $('fAccion');
const fFecha = $('fFecha');
const fTexto = $('fTexto');
let registros = [];

iniciarNav();

function etiqueta(codigo) {
  return ETIQUETAS[codigo] || codigo;
}

function mostrarError(texto, href, textoEnlace) {
  const caja = $('mensaje');
  caja.className = 'mensaje error';
  caja.replaceChildren(document.createTextNode(texto));
  if (href) caja.appendChild(enlace(textoEnlace, href));
  filas.replaceChildren();
  $('contador').textContent = '';
}

function llenarAcciones() {
  const actual = fAccion.value;
  fAccion.replaceChildren();
  fAccion.appendChild(new Option('Todas', ''));
  [...new Set(registros.map(r => r.accion))].sort().forEach(codigo => {
    fAccion.appendChild(new Option(etiqueta(codigo), codigo));
  });
  fAccion.value = actual;
}

function pintar() {
  const texto = fTexto.value.trim().toLowerCase();
  const visibles = registros.filter(r =>
    (!fAccion.value || r.accion === fAccion.value)
    && (!fFecha.value || r.fecha.slice(0, 10) === fFecha.value)
    && (!texto
      || (r.usuario || '').toLowerCase().includes(texto)
      || (r.detalle || '').toLowerCase().includes(texto)));

  filas.replaceChildren();
  visibles.forEach(r => {
    const tr = document.createElement('tr');
    [r.fecha.replace('T', ' ').slice(0, 19), r.usuario, etiqueta(r.accion),
      r.entidad, r.idEntidad, r.detalle].forEach(v => {
      tr.appendChild(crear('td', '', v == null ? '' : v));
    });
    filas.appendChild(tr);
  });

  $('contador').textContent = registros.length === 0
    ? 'Todavía no hay operaciones registradas.'
    : 'Mostrando ' + visibles.length + ' de ' + registros.length + ' operaciones (las 200 más recientes).';
}

async function cargar() {
  mostrar('', false);
  let res;
  try {
    res = await fetch('/api/auditoria');
  } catch (e) {
    mostrarError('No se pudo conectar con el servidor.');
    return;
  }
  const esJson = (res.headers.get('content-type') || '').includes('application/json');
  if (res.redirected || res.status === 401 || (res.ok && !esJson)) {
    mostrarError('No has iniciado sesión. ', '/login.html', 'Inicia sesión como administrador');
    return;
  }
  if (res.status === 403) {
    mostrarError('No tienes permiso para ver el historial (solo el administrador).');
    return;
  }
  if (!res.ok) {
    mostrarError('No se pudo cargar el historial.');
    return;
  }
  registros = await res.json();
  llenarAcciones();
  pintar();
}

[fAccion, fFecha, fTexto].forEach(el => el.addEventListener('input', pintar));
$('formFiltros').addEventListener('submit', (ev) => ev.preventDefault());
$('btnLimpiar').addEventListener('click', () => {
  $('formFiltros').reset();
  pintar();
});
$('btnActualizar').addEventListener('click', cargar);

cargar();
