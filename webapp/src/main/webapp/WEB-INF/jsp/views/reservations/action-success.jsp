<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Reserva <c:out value="${action}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body style="max-width: 560px; margin: 0 auto; padding: 24px;">
    <c:set var="successTitle" value="Reserva ${action}" />
    <paw:title text="${successTitle}" size="2rem" />

    <p style="margin: 1.5rem 0; line-height: 1.6;">
        La operación se registró correctamente.
    </p>

    <p><a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-md" style="text-decoration: none; display: inline-block;">Volver al inicio</a></p>
</body>
</html>
