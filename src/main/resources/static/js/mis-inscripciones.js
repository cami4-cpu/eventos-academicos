const filas = $('filas');
iniciarNav();

async function cancelar(idEvento) {
  if (!confirm('¿Cancelar esta inscripción?')) return;
  const res = await fetch('/api/inscripciones/' + idEvento, { method: 'DELETE' });
  if (!res.ok) { mostrar(await textoError(res, 'No se pudo cancelar la inscripción.'), true); return; }
  mostrar('Inscripción cancelada.', false);
  cargar();
}

async function cargar() {
  const res = await fetch('/api/inscripciones/mias');
  const tipo = res.headers.get('content-type') || '';
  if (!res.ok || !tipo.includes('json')) {
    window.location.href = '/login.html';
    return;
  }
  const inscripciones = await res.json();
  filas.replaceChildren();
  if (inscripciones.length === 0) {
    mostrar('Todavía no tienes inscripciones. Explora el catálogo para inscribirte.', false);
    return;
  }
  inscripciones.forEach(i => {
    const tr = document.createElement('tr');
    [i.titulo, i.fecha, i.horaInicio.slice(0, 5), i.ubicacion].forEach(valor => {
      tr.appendChild(crear('td', '', valor));
    });
    const tdEstado = document.createElement('td');
    tdEstado.appendChild(insignia(i.estado));
    const acciones = document.createElement('td');
    if (i.estado === 'ACTIVA') {
      acciones.appendChild(boton('Cancelar', () => cancelar(i.idEvento), 'peligro chico'));
    }
    tr.append(tdEstado, acciones);
    filas.appendChild(tr);
  });
}

cargar();
