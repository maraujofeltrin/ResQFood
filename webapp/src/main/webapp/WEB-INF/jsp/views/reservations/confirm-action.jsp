<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<spring:message var="pageTitleText" code="reservation.token.confirm.pageTitle" />
<spring:message var="badgeText" code="reservation.token.confirm.badge" />

<paw:reservationFeedback pageTitle="${pageTitleText}"
                         badgeText="${badgeText}"
                         badgeVariant="primary">

    <paw:pickupConfirm
            formAction="${pageContext.request.contextPath}/reservations/${confirmEndpoint}"
            reservation="${reservation}"
            reservationDateFormatted="${reservationDateFormatted}"
            pickupError="${pickupError}"
            token="${token}"
            showCodeInput="${confirmEndpoint == 'accept'}" />

</paw:reservationFeedback>
