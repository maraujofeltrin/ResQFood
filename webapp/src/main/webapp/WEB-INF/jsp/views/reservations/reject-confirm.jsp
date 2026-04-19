<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<spring:message var="pageTitleText" code="reservation.token.reject.pageTitle" />
<spring:message var="badgeText" code="reservation.token.reject.badge" />

<paw:reservationFeedback pageTitle="${pageTitleText}"
                         badgeText="${badgeText}"
                         badgeVariant="error"
                         blurColorTopLeft="bg-error-container/30"
                         blurColorBottomRight="bg-primary-container/25">

    <h1 class="font-headline text-3xl font-extrabold leading-tight text-on-surface sm:text-4xl">
        <spring:message code="reservation.token.reject.title" />
    </h1>

    <p class="mt-4 text-base leading-relaxed text-secondary sm:text-lg">
        <spring:message code="reservation.token.reject.description" htmlEscape="false" />
    </p>

    <div class="mt-7 rounded-2xl bg-surface-container p-5">
        <p class="mb-4 font-headline text-lg font-bold text-on-surface">
            <spring:message code="reservation.token.data.title" />
        </p>
        <div class="grid gap-3 text-sm sm:grid-cols-2 sm:text-base">
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.id" />:</span>
                <c:out value="${reservation.id}" />
            </p>
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.client" />:</span>
                <c:out value="${reservation.customerId}" />
            </p>
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.pack" />:</span>
                <c:out value="${reservation.packId}" />
            </p>
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.status" />:</span>
                <c:out value="${reservation.status}" />
            </p>
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.dateReservation" />:</span>
                <c:choose>
                    <c:when test="${not empty reservationDateFormatted}">
                        <c:out value="${reservationDateFormatted}" />
                    </c:when>
                    <c:otherwise>—</c:otherwise>
                </c:choose>
            </p>
            <p>
                <span class="font-semibold text-primary"><spring:message code="reservation.token.data.price" />:</span>
                <c:choose>
                    <c:when test="${not empty reservation.finalPrice}">
                        $<c:out value="${reservation.finalPrice}" />
                    </c:when>
                    <c:otherwise>—</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <form action="${pageContext.request.contextPath}/reservations/reject"
          method="post" class="mt-8">
        <input type="hidden" name="token" value="<c:out value='${token}'/>" />

        <div class="flex flex-wrap items-center gap-3">
            <button type="submit"
                    class="inline-flex items-center justify-center rounded-full bg-error px-6 py-3 font-headline text-sm font-bold text-on-error transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-error/25">
                <spring:message code="reservation.token.reject.confirm" />
            </button>
            <a href="${pageContext.request.contextPath}/"
               class="inline-flex items-center justify-center rounded-full bg-surface-container-high px-6 py-3 font-headline text-sm font-semibold text-on-surface transition hover:bg-surface-container-highest focus:outline-none focus:ring-4 focus:ring-primary/15">
                <spring:message code="reservation.token.reject.cancel" />
            </a>
        </div>
    </form>

</paw:reservationFeedback>
