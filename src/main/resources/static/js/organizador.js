const filas = $('filas');
const form = $('formEvento');
const btnGuardar = $('btnGuardar');
const btnCancelarEdicion = $('btnCancelarEdicion');
let idEditando = null;

iniciarNav();

async function marcarAsistencia(idEvento, titulo, idInscripcion, valor) {
  const res = await fetch('/api/eventos/' + idEvento + '/inscritos/' + idInscripcion
    + '/asistencia?valor=' + valor, { method: 'PATCH' });
  if (!res.ok) {
    mostrar(await textoError(res, 'No se pudo registrar la asistencia.'), true);
    return;
  }
  mostrar('Asistencia registrada.', false);
  verInscritos(idEvento, titulo);
}

function indicador(valor, texto) {
  const d = crear('div', 'indicador');
  d.append(crear('strong', '', valor), crear('span', '', texto));
  return d;
}

async function verInscritos(id, titulo) {
  const zona = $('inscritos');
  zona.replaceChildren();
  const res = await fetch('/api/eventos/' + id + '/inscritos');
  if (!res.ok) { mostrar('No tienes permiso para ver los inscritos de ese evento.', true); return; }
  const inscritos = await res.json();

  zona.appendChild(crear('h2', '', 'Inscritos en "' + titulo + '"'));

  const resInd = await fetch('/api/eventos/' + id + '/inscritos/indicadores');
  if (resInd.ok) {
    const ind = await resInd.json();
    const caja = crear('div', 'indicadores');
    caja.append(
      indicador(ind.cupo, 'Cupo'),
      indicador(ind.inscritosActivos, 'Inscritos activos'),
      indicador(ind.cuposLibres, 'Cupos libres'),
      indicador(ind.porcentajeOcupacion + '%', 'Ocupación'),
      indicador(ind.asistieron, 'Asistieron'),
      indicador(ind.noAsistieron, 'No asistieron'),
      indicador(ind.pendientes, 'Pendientes'),
      indicador(ind.porcentajeAsistencia + '%', 'Asistencia'));
    zona.appendChild(caja);
  }

  if (inscritos.length === 0) {
    zona.appendChild(crear('div', 'vacio', 'Todavía no hay inscritos.'));
    return;
  }
  const envoltura = crear('div', 'tabla');
  const tabla = document.createElement('table');
  const cabecera = document.createElement('tr');
  ['Nombre', 'Correo', 'Estado', 'Asistencia', 'Fecha de inscripción', 'Registrar'].forEach(t => {
    const th = crear('th', '', t);
    th.scope = 'col';
    cabecera.appendChild(th);
  });
  const thead = document.createElement('thead');
  thead.appendChild(cabecera);
  const tbody = document.createElement('tbody');

  inscritos.forEach(i => {
    const tr = document.createElement('tr');
    tr.appendChild(crear('td', '', i.nombre));
    tr.appendChild(crear('td', '', i.email));
    const tdEstado = document.createElement('td');
    tdEstado.appendChild(insignia(i.estado));
    const tdAsis = document.createElement('td');
    tdAsis.appendChild(insignia(i.asistencia));
    tr.append(tdEstado, tdAsis, crear('td', '', fechaHora(i.fechaInscripcion)));
    const acciones = document.createElement('td');
    if (i.estado === 'ACTIVA') {
      const grupo = crear('div', 'acciones');
      grupo.append(
        boton('Asistió', () => marcarAsistencia(id, titulo, i.idInscripcion, 'ASISTIO'), 'exito chico'),
        boton('No asistió', () => marcarAsistencia(id, titulo, i.idInscripcion, 'NO_ASISTIO'), 'peligro chico'));
      acciones.appendChild(grupo);
    }
    tr.appendChild(acciones);
    tbody.appendChild(tr);
  });
  tabla.append(thead, tbody);
  envoltura.appendChild(tabla);
  zona.appendChild(envoltura);
  zona.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function cambiarEstado(id, estado) {
  const res = await fetch('/api/eventos/' + id + '/estado?estado=' + estado, { method: 'PATCH' });
  if (!res.ok) { mostrar(await textoError(res, 'No se pudo cambiar el estado.'), true); return; }
  mostrar('Estado actualizado a ' + estado + '.', false);
  cargarEventos();
}

async function cancelarEvento(e) {
  const motivo = prompt('Motivo de la cancelación de "' + e.titulo + '":');
  if (motivo === null) return;
  if (motivo.trim() === '') {
    mostrar('Debes escribir un motivo para cancelar el evento.', true);
    return;
  }
  const res = await fetch('/api/eventos/' + e.id + '/cancelar', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ motivo: motivo.trim() })
  });
  if (!res.ok) { mostrar(await textoError(res, 'No se pudo cancelar el evento.'), true); return; }
  mostrar('Evento cancelado. Las inscripciones se conservan como historial.', false);
  cargarEventos();
}

async function eliminar(id) {
  if (!confirm('¿Eliminar este evento?')) return;
  const res = await fetch('/api/eventos/' + id, { method: 'DELETE' });
  if (!res.ok) { mostrar(await textoError(res, 'No se pudo eliminar el evento.'), true); return; }
  mostrar('Evento eliminado.', false);
  cargarEventos();
}

function editar(e) {
  idEditando = e.id;
  $('titulo').value = e.titulo;
  $('descripcion').value = e.descripcion || '';
  $('fecha').value = e.fecha;
  $('horaInicio').value = (e.horaInicio || '').slice(0, 5);
  $('horaFin').value = (e.horaFin || '').slice(0, 5);
  $('modalidad').value = e.modalidad;
  $('ubicacion').value = e.ubicacion || '';
  $('cupo').value = e.cupo;
  const opcion = Array.from($('categoria').options).find(o => o.textContent === e.categoria);
  $('categoria').value = opcion ? opcion.value : '';
  $('tituloForm').textContent = 'Editar evento';
  btnGuardar.textContent = 'Guardar cambios';
  btnCancelarEdicion.hidden = false;
  mostrar('Editando "' + e.titulo + '". Cambia los datos y pulsa Guardar cambios.', false);
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function salirEdicion() {
  idEditando = null;
  form.reset();
  $('tituloForm').textContent = 'Crear evento';
  btnGuardar.textContent = 'Crear evento';
  btnCancelarEdicion.hidden = true;
}

btnCancelarEdicion.addEventListener('click', () => {
  salirEdicion();
  mostrar('', false);
});

async function cargarEventos() {
  const res = await fetch('/api/eventos');
  const eventos = await res.json();
  filas.replaceChildren();
  eventos.forEach(e => {
    const tr = document.createElement('tr');
    [e.titulo, e.fecha, e.modalidad, e.cupo].forEach(valor => tr.appendChild(crear('td', '', valor)));

    const tdEstado = document.createElement('td');
    tdEstado.appendChild(insignia(e.estado));
    if (e.estado === 'CANCELADO' && e.motivoCancelacion) {
      tdEstado.appendChild(crear('span', 'motivo',
        'Motivo: ' + e.motivoCancelacion + ' (por ' + e.canceladoPor + ', ' + fechaHora(e.fechaCancelacion) + ')'));
    }
    tr.appendChild(tdEstado);

    const acciones = crear('div', 'acciones');
    const cerrado = e.estado === 'CANCELADO' || e.estado === 'FINALIZADO';
    if (!cerrado) {
      if (e.estado === 'BORRADOR' || e.estado === 'CERRADO') {
        acciones.append(boton(e.estado === 'CERRADO' ? 'Reabrir' : 'Publicar',
          () => cambiarEstado(e.id, 'PUBLICADO'), 'exito chico'));
      }
      acciones.append(boton('Editar', () => editar(e)));
      if (e.estado === 'PUBLICADO') {
        acciones.append(boton('Cerrar', () => cambiarEstado(e.id, 'CERRADO')));
      }
      if (e.estado === 'PUBLICADO' || e.estado === 'CERRADO') {
        acciones.append(boton('Finalizar', () => cambiarEstado(e.id, 'FINALIZADO')));
      }
      acciones.append(boton('Cancelar', () => cancelarEvento(e), 'peligro chico'));
    }
    acciones.append(
      boton('Ver inscritos', () => verInscritos(e.id, e.titulo)),
      boton('Eliminar', () => eliminar(e.id), 'peligro chico'));
    const tdAcc = document.createElement('td');
    tdAcc.appendChild(acciones);
    tr.appendChild(tdAcc);
    filas.appendChild(tr);
  });
}

form.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const datos = {
    titulo: $('titulo').value,
    descripcion: $('descripcion').value,
    fecha: $('fecha').value,
    horaInicio: $('horaInicio').value,
    horaFin: $('horaFin').value,
    modalidad: $('modalidad').value,
    ubicacion: $('ubicacion').value,
    cupo: parseInt($('cupo').value, 10),
    idCategoria: parseInt($('categoria').value, 10)
  };
  const editando = idEditando !== null;
  const res = await fetch(editando ? '/api/eventos/' + idEditando : '/api/eventos', {
    method: editando ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datos)
  });
  if (!res.ok) {
    mostrar(await textoError(res, 'No se pudo guardar el evento. Revisa los datos.'), true);
    return;
  }
  mostrar(editando
    ? 'Evento actualizado.'
    : 'Evento creado como borrador. Pulsa Publicar para que salga en el catálogo.', false);
  salirEdicion();
  cargarEventos();
});

cargarCategorias($('categoria'));
cargarEventos();
