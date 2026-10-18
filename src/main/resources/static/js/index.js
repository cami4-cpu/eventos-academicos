const lista = $('lista');
let sesion = null;

async function inscribir(id, btn) {
  btn.disabled = true;
  try {
    const res = await fetch('/api/inscripciones/' + id, { method: 'POST' });
    if (res.redirected) {
      sesion = null;
      pintarNav(null);
      mostrar('Tu sesión expiró. Inicia sesión de nuevo.', true);
    } else if (res.status === 201) {
      mostrar('Inscripción confirmada. La ves en "Mis inscripciones".', false);
    } else {
      mostrar(await textoError(res, 'No se pudo completar la inscripción.'), true);
    }
  } catch (e) {
    mostrar('No se pudo conectar con el servidor.', true);
  }
  // Se vuelve a pedir la lista para ver los cupos y el estado al día.
  await cargarEventos();
}

function accionEvento(e) {
  if (!sesion) return enlace('Inicia sesión para inscribirte', '/login.html', 'btn secundario');
  const btn = crear('button', 'btn');
  btn.type = 'button';
  if (e.inscrito) {
    btn.textContent = 'Ya estás inscrito';
    btn.disabled = true;
  } else if (e.cuposDisponibles <= 0) {
    btn.textContent = 'Cupo lleno';
    btn.disabled = true;
  } else {
    btn.textContent = 'Inscribirme';
    btn.addEventListener('click', () => inscribir(e.id, btn));
  }
  return btn;
}

function tarjetaEvento(e) {
  const div = crear('article', 'evento');

  const etiquetas = crear('div', 'etiquetas');
  etiquetas.append(crear('span', 'etiqueta', e.categoria), crear('span', 'etiqueta', e.modalidad));

  const meta = crear('div', 'meta');
  meta.append(
    crear('span', '', '📅 ' + e.fecha + ' · ' + e.horaInicio.slice(0, 5) + ' - ' + e.horaFin.slice(0, 5)),
    crear('span', '', '📍 ' + (e.ubicacion || 'Por definir')),
    crear('span', '', '👤 ' + e.organizador));

  const lleno = e.cuposDisponibles <= 0;
  const cupos = crear('div', 'cupos ' + (lleno ? 'lleno' : 'ok'),
    'Cupos disponibles: ' + e.cuposDisponibles + ' de ' + e.cupo);
  const barra = crear('div', 'barra-cupo' + (lleno ? ' lleno' : ''));
  const relleno = document.createElement('span');
  relleno.style.width = Math.min(100, Math.round((e.cupo - e.cuposDisponibles) * 100 / e.cupo)) + '%';
  barra.appendChild(relleno);

  div.append(etiquetas, crear('h3', '', e.titulo), meta, crear('p', '', e.descripcion || ''),
    cupos, barra, accionEvento(e));
  return div;
}

async function cargarEventos() {
  const params = new URLSearchParams();
  const valores = { q: $('q').value.trim(), categoria: $('categoria').value,
    modalidad: $('modalidad').value, fecha: $('fecha').value };
  Object.entries(valores).forEach(([k, v]) => { if (v) params.append(k, v); });

  const res = await fetch('/api/catalogo?' + params.toString());
  const eventos = await res.json();
  lista.replaceChildren();
  if (eventos.length === 0) {
    lista.appendChild(crear('div', 'vacio', 'No hay eventos que coincidan con la búsqueda.'));
    return;
  }
  eventos.forEach(e => lista.appendChild(tarjetaEvento(e)));
}

$('formFiltros').addEventListener('submit', (ev) => { ev.preventDefault(); cargarEventos(); });
$('limpiar').addEventListener('click', () => {
  $('formFiltros').reset();
  cargarEventos();
});

(async function iniciar() {
  sesion = await iniciarNav();
  await cargarCategorias($('categoria'));
  await cargarEventos();
})();
