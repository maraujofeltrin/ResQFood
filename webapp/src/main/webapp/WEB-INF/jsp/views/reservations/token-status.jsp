<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<c:choose>
    <c:when test="${tokenStatus == 'invalid'}">
        <spring:message var="pageTitle" code="reservation.token.status.invalid.pageTitle" />
        <spring:message var="badgeText" code="reservation.token.status.invalid.badge" />
        <c:set var="badgeVariant" value="error" />
        <c:set var="blurTL" value="bg-error-container/30" />
        <c:set var="blurBR" value="bg-primary-container/25" />
        <spring:message var="title" code="reservation.token.status.invalid.title" />
        <spring:message var="description" code="reservation.token.status.invalid.description" />
    </c:when>
    <c:when test="${tokenStatus == 'expired'}">
        <spring:message var="pageTitle" code="reservation.token.status.expired.pageTitle" />
        <spring:message var="badgeText" code="reservation.token.status.expired.badge" />
        <c:set var="badgeVariant" value="primary" />
        <c:set var="blurTL" value="bg-primary-container/30" />
        <c:set var="blurBR" value="bg-primary/15" />
        <spring:message var="title" code="reservation.token.status.expired.title" />
        <spring:message var="description" code="reservation.token.status.expired.description" />
    </c:when>
    <c:otherwise>
        <spring:message var="pageTitle" code="reservation.token.status.used.pageTitle" />
        <spring:message var="badgeText" code="reservation.token.status.used.badge" />
        <c:set var="badgeVariant" value="primary" />
        <c:set var="blurTL" value="bg-primary-container/30" />
        <c:set var="blurBR" value="bg-primary/15" />
        <spring:message var="title" code="reservation.token.status.used.title" />
        <c:choose>
            <c:when test="${not empty alreadyUsedDetailCode}">
                <spring:message var="description" code="${alreadyUsedDetailCode}" />
            </c:when>
            <c:otherwise>
                <spring:message var="description" code="reservation.token.status.used.description" />
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<paw:reservationFeedback pageTitle="${pageTitle}"
                         badgeText="${badgeText}"
                         badgeVariant="${badgeVariant}"
                         blurColorTopLeft="${blurTL}"
                         blurColorBottomRight="${blurBR}">

    <h1 class="font-headline text-3xl font-extrabold leading-tight text-primary sm:text-4xl">
        <c:out value="${title}" />
    </h1>

    <p class="mt-5 max-w-2xl text-base leading-relaxed text-secondary sm:text-lg">
        <c:out value="${description}" />
    </p>

    <div class="mt-8 flex flex-wrap items-center gap-3">
        <a href="${pageContext.request.contextPath}/"
           class="inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 font-headline text-sm font-bold text-on-primary transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-primary/25">
            <spring:message code="reservation.token.backHome" />
        </a>
    </div>

</paw:reservationFeedback>
