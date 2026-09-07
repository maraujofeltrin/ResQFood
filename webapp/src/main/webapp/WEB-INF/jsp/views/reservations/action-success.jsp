<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<spring:message var="actionLabel" code="${actionCode}" />
<spring:message var="successTitle" code="reservation.token.success.pageTitle" arguments="${actionLabel}" />
<spring:message var="badgeLabel" code="reservation.token.success.badge" />

<paw:reservationFeedback pageTitle="${successTitle}"
                         badgeText="${badgeLabel}"
                         badgeVariant="success"
                         blurColorTopLeft="bg-primary-container/35"
                         blurColorBottomRight="bg-primary/15">

    <h1 class="font-headline text-3xl font-extrabold leading-tight text-primary sm:text-4xl">
        <c:out value="${successTitle}" />
    </h1>

    <p class="mt-5 max-w-2xl text-base leading-relaxed text-secondary sm:text-lg">
        <spring:message code="reservation.token.success.message" />
    </p>

    <div class="mt-8 flex flex-wrap items-center gap-3">
        <a href="${pageContext.request.contextPath}/"
           class="inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 font-headline text-sm font-bold text-on-primary transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-primary/25">
            <spring:message code="reservation.token.backHome" />
        </a>
    </div>

</paw:reservationFeedback>
