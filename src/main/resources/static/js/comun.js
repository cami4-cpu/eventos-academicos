/* Utilidades compartidas por todas las páginas */
const $ = (id) => document.getElementById(id);

/** Muestra un mensaje de éxito o error en el elemento #mensaje. */
function mostrar(texto, esError) {
  const caja = $('mensaje');
  caja.textContent = texto;
  caja.className = 'mensaje ' + (texto ? (esError ? 'error' : 'ok') : '');
}

/** Lee el mensaje de error que devuelve el backend, o usa uno por defecto. */
async function textoError(res, porDefecto) {
  try {
    const d = await res.json();
    if (d.message) return d.message;
  } catch (e) { /* respuesta sin JSON */ }
  return porDefecto;
}

function crear(etiqueta, clase, texto) {
  const el = document.createElement(etiqueta);
  if (clase) el.className = clase;
  if (texto !== undefined && texto !== null) el.textContent = texto;
  return el;
}

function boton(texto, accion, clase) {
  const b = crear('button', 'btn ' + (clase || 'secundario chico'), texto);
  b.type = 'button';
  b.addEventListener('click', accion);
  return b;
}

function enlace(texto, href, clase) {
  const a = crear('a', clase || '', texto);
  a.href = href;
  if (location.pathname === href) a.setAttribute('aria-current', 'page');
  return a;
}

function insignia(estado) {
  return crear('span', 'estado ' + estado, estado.replace('_', ' '));
}

function fechaHora(valor) {
  return valor ? valor.replace('T', ' ').slice(0, 16) : '';
}

/** Consulta la sesión actual. Devuelve null si no hay sesión. */
async function cargarSesion() {
  try {
    const res = await fetch('/api/auth/me');
    const tipo = res.headers.get('content-type') || '';
    if (res.ok && tipo.includes('json')) return await res.json();
  } catch (e) { /* sin conexión */ }
  return null;
}

function esOrganizador(sesion) {
  return !!sesion && (sesion.roles.includes('ROLE_ORGANIZADOR') || sesion.roles.includes('ROLE_ADMINISTRADOR'));
}

function esAdministrador(sesion) {
  return !!sesion && sesion.roles.includes('ROLE_ADMINISTRADOR');
}

/** Pinta la barra de navegación común en el elemento #nav. */
function pintarNav(sesion) {
  const nav = $('nav');
  nav.replaceChildren();
  nav.append(enlace('Catálogo', '/'));
  if (!sesion) {
    nav.append(enlace('Iniciar sesión', '/login.html'), enlace('Crear cuenta', '/registro.html'));
    return;
  }
  nav.append(enlace('Mis inscripciones', '/mis-inscripciones.html'));
  if (esOrganizador(sesion)) nav.append(enlace('Panel del organizador', '/organizador.html'));
  if (esAdministrador(sesion)) nav.append(enlace('Historial', '/historial.html'));
  nav.append(crear('span', 'usuario', sesion.email));
  const salir = boton('Cerrar sesión', async () => {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.href = '/';
  }, 'chico');
  nav.append(salir);
}

/** Carga la sesión y pinta la barra. Devuelve la sesión (o null). */
async function iniciarNav() {
  const sesion = await cargarSesion();
  pintarNav(sesion);
  return sesion;
}

/** Llena un <select> con las categorías del backend. */
async function cargarCategorias(select) {
  const res = await fetch('/api/categorias');
  const categorias = await res.json();
  categorias.forEach(c => {
    const op = document.createElement('option');
    op.value = c.id;
    op.textContent = c.nombre;
    select.appendChild(op);
  });
}
