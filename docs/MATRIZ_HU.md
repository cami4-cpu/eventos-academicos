# Matriz de historias de usuario

Estado: Implementada, Parcial o Pendiente. La evidencia es una captura de pantalla o la respuesta del endpoint, guardada en `docs/evidencias/`.

| HU | Historia | Estado | Prueba funcional y resultado esperado | Evidencia |
|---|---|---|---|---|
| HU-01 | Crear evento | Implementada | Organizador crea un evento en `organizador.html`. Queda en BORRADOR (201). Con cupo 0 responde 400. | |
| HU-02 | Editar evento | Implementada | Botón Editar cambia el título. La tabla lo muestra actualizado. Un organizador ajeno recibe 403. | |
| HU-03 | Publicar o cerrar | Implementada | Publicar un borrador lo muestra en el catálogo. Cerrarlo lo oculta y bloquea inscripciones. | |
| HU-04 | Cancelar con motivo | Implementada | Cancelar pide motivo (obligatorio), pasa a CANCELADO y queda en la auditoría. | |
| HU-05 | Consultar eventos publicados | Implementada | Sin sesión, `/` lista solo eventos PUBLICADOS, ordenados por fecha. | |
| HU-06 | Filtrar eventos | Implementada | Filtrar por texto, categoría, modalidad y fecha devuelve solo los que coinciden. | |
| HU-07 | Registrarse e iniciar sesión | Implementada | `registro.html` crea la cuenta (201). Repetir el correo da 409. `login.html` entra y `/api/auth/me` muestra los roles. | |
| HU-08 | Detalle del evento | Parcial | La tarjeta muestra horario, lugar, modalidad y cupos disponibles. No hay página de detalle aparte. | |
| HU-09 | Inscribirse | Implementada | Participante pulsa Inscribirme (201). Los cupos bajan en uno y aparece en Mis inscripciones. | |
| HU-10 | Sin duplicados y control de cupo | Implementada | Segunda inscripción al mismo evento da 409. Con el cupo lleno da 409 "No hay cupos disponibles". | |
| HU-11 | Cancelar inscripción | Parcial | Cancelar libera el cupo (204). Falta definir un plazo permitido. | |
| HU-12 | Mis inscripciones | Implementada | `mis-inscripciones.html` lista activas y canceladas. | |
| HU-13 | Lista de inscritos | Implementada | Ver inscritos muestra nombre, correo, estado y fecha. Organizador ajeno recibe 403. | |
| HU-14 | Registrar asistencia | Implementada | En un evento de hoy o anterior, Asistió y No asistió cambian el estado. Evento futuro da 409. | |
| HU-15 | Indicadores | Implementada | Ver inscritos muestra cupo, inscritos activos, cupos libres, % de ocupación y asistencia. | |
| HU-16 | Historial de operaciones | Implementada | Como administrador, `historial.html` lista las operaciones. Un participante recibe 403. | |