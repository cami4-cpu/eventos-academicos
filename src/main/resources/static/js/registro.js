iniciarNav();

$('formRegistro').addEventListener('submit', async (ev) => {
  ev.preventDefault();
  try {
    const res = await fetch('/api/auth/registro', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: $('nombre').value,
        email: $('email').value,
        password: $('password').value
      })
    });
    if (res.ok) {
      mostrar('Cuenta creada. Redirigiendo al inicio de sesión...', false);
      setTimeout(() => window.location.href = '/login.html', 1500);
    } else if (res.status === 409) {
      mostrar('Ese correo ya está registrado.', true);
    } else {
      mostrar('No se pudo crear la cuenta. Revisa los datos.', true);
    }
  } catch (e) {
    mostrar('No se pudo conectar con el servidor.', true);
  }
});
