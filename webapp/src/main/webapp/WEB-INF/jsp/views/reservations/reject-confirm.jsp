<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Rechazar reserva</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body style="max-width: 560px; margin: 0 auto; padding: 24px;">
    <paw:title text="¿Está seguro de que desea rechazar la reserva?" size="1.75rem" />

    <p style="margin: 1rem 0 1.5rem; line-height: 1.6; color: #374151;">
        Si confirmás el rechazo, la reserva pasará a estado <strong>cancelado</strong> y el enlace dejará de estar disponible.
    </p>

    <div style="margin: 1.5rem 0; line-height: 1.7; padding: 1rem 1.25rem; background: #f9fafb; border-radius: 8px; border: 1px solid #e5e7eb;">
        <p style="margin: 0 0 0.75rem; font-weight: 600; color: #111827;">Datos de la reserva</p>
        <p><strong>Id reserva:</strong> <c:out value="${reservation.id}" /></p>
        <p><strong>Cliente (id):</strong> <c:out value="${reservation.customerId}" /></p>
        <p><strong>Pack (id):</strong> <c:out value="${reservation.packId}" /></p>
        <p><strong>Fecha de reserva:</strong>
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
        <p><strong>Código de retiro:</strong>
            <c:choose>
                <c:when test="${not empty reservation.pickupCode}">
                    <c:out value="${reservation.pickupCode}" />
                </c:when>
                <c:otherwise>—</c:otherwise>
            </c:choose>
        </p>
        <p style="margin-bottom: 0;"><strong>Confirmación de retiro:</strong>
            <c:choose>
                <c:when test="${not empty reservation.pickupConfirmationDate}">
                    <c:out value="${reservation.pickupConfirmationDate}" />
                </c:when>
                <c:otherwise>—</c:otherwise>
            </c:choose>
        </p>
    </div>

    <form action="${pageContext.request.contextPath}/reservations/reject" method="post" style="margin-top: 2rem;">
        <input type="hidden" name="token" value="<c:out value='${token}'/>" />

        <div style="display: flex; flex-wrap: wrap; gap: 12px; align-items: center;">
            <paw:button text="Sí, rechazar reserva" htmlType="submit" type="danger" />
            <a href="${pageContext.request.contextPath}/"
               class="btn btn-secondary btn-md" style="text-decoration: none; display: inline-block;">No, volver al inicio</a>
        </div>
    </form>
</body>
</html>
