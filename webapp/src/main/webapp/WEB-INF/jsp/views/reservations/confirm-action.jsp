<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Confirmar aceptación de reserva</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body style="max-width: 560px; margin: 0 auto; padding: 24px;">
    <paw:title text="Confirmar aceptación de reserva" size="2rem" />

    <div style="margin: 1.5rem 0; line-height: 1.6;">
        <p><strong>Pack id:</strong> <c:out value="${reservation.packId}" /></p>
        <p><strong>Fecha:</strong>
            <c:choose>
                <c:when test="${not empty reservation.reservationDate}">
                    <c:out value="${reservation.reservationDate}" />
                </c:when>
                <c:otherwise>—</c:otherwise>
            </c:choose>
        </p>
        <p><strong>Precio final:</strong>
            <c:choose>
                <c:when test="${not empty reservation.finalPrice}">
                    $<c:out value="${reservation.finalPrice}" />
                </c:when>
                <c:otherwise>—</c:otherwise>
            </c:choose>
        </p>
        <p><strong>Estado actual:</strong> <c:out value="${reservation.status}" /></p>
    </div>

    <form action="${pageContext.request.contextPath}/reservations/${confirmEndpoint}" method="post"
          style="margin-top: 2rem;">
        <input type="hidden" name="token" value="<c:out value='${token}'/>" />

        <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: center;">
            <paw:button text="Confirmar" htmlType="submit" type="primary" />
            <a href="${pageContext.request.contextPath}/"
               class="btn btn-secondary btn-md" style="text-decoration: none; display: inline-block;">Cancelar</a>
        </div>
    </form>
</body>
</html>
