<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Enlace no válido</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body style="max-width: 560px; margin: 0 auto; padding: 24px;">
    <paw:title text="Este enlace no es válido" size="2rem" />

    <p style="margin: 1.5rem 0; line-height: 1.6;">
        No encontramos una solicitud de reserva asociada a este enlace, o el tipo de acción no coincide.
    </p>

    <p><a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-md" style="text-decoration: none; display: inline-block;">Volver al inicio</a></p>
</body>
</html>
