iniciarNav();

$('formLogin').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const cuerpo = new URLSearchParams();
  cuerpo.append('email', $('email').value);
  cuerpo.append('password', $('password').value);
  try {
    const res = await fetch('/api/auth/login', { method: 'POST', body: cuerpo });
    if (res.ok) {
      window.location.href = '/';
    } else {
      mostrar('Correo o contraseña incorrectos.', true);
    }
  } catch (e) {
    mostrar('No se pudo conectar con el servidor.', true);
  }
});
