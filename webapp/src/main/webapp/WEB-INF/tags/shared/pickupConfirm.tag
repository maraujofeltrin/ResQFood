<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ tag body-content="empty" pageEncoding="UTF-8" %>

<%@ attribute name="formAction" required="true" type="java.lang.String" %>
<%@ attribute name="reservation" required="false" type="ar.edu.itba.paw.models.reservation.Reservation" %>
<%@ attribute name="reservationDateFormatted" required="false" type="java.lang.String" %>
<%@ attribute name="pickupError" required="false" type="java.lang.String" %>
<%@ attribute name="pickupSuccess" required="false" type="java.lang.Boolean" %>
<%@ attribute name="confirmedPack" required="false" type="ar.edu.itba.paw.models.pack.Pack" %>
<%@ attribute name="confirmedClientName" required="false" type="java.lang.String" %>
<%@ attribute name="token" required="false" type="java.lang.String" %>
<%@ attribute name="showCodeInput" required="false" type="java.lang.Boolean" %>
<%@ attribute name="submittedCode" required="false" type="java.lang.String" %>

<c:if test="${empty showCodeInput}">
    <c:set var="showCodeInput" value="${true}" />
</c:if>

<c:choose>
    <%-- ── Success state ── --%>
    <c:when test="${pickupSuccess}">
        <div class="mb-6 inline-flex items-center gap-2 rounded-full border border-tertiary/25 bg-tertiary-container/40 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-on-tertiary-container">
            <span class="material-symbols-outlined text-sm">check_circle</span>
            <spring:message code="commerce.verifyPickup.success.title" />
        </div>

        <h2 class="font-headline text-2xl font-extrabold leading-tight text-primary sm:text-3xl">
            <spring:message code="commerce.verifyPickup.success.title" />
        </h2>

        <p class="mt-3 text-base leading-relaxed text-secondary">
            <spring:message code="commerce.verifyPickup.success.message" />
        </p>

        <c:if test="${not empty reservation || not empty confirmedPack}">
            <div class="mt-6 grid gap-3 rounded-2xl bg-surface-container p-5 text-sm sm:grid-cols-2 sm:text-base">
                <c:if test="${not empty confirmedPack}">
                    <p>
                        <span class="font-semibold text-primary"><spring:message code="commerce.verifyPickup.result.pack" />:</span>
                        <c:out value="${confirmedPack.title}" />
                    </p>
                </c:if>
                <c:if test="${not empty confirmedClientName}">
                    <p>
                        <span class="font-semibold text-primary"><spring:message code="commerce.verifyPickup.result.client" />:</span>
                        <c:out value="${confirmedClientName}" />
                    </p>
                </c:if>
                <c:set var="res" value="${not empty reservation ? reservation : confirmedReservation}" />
                <c:if test="${not empty res}">
                    <p>
                        <span class="font-semibold text-primary"><spring:message code="commerce.verifyPickup.result.status" />:</span>
                        <spring:message code="reservation.status.PAID" />
                    </p>
                    <c:if test="${not empty res.finalPrice}">
                        <p>
                            <span class="font-semibold text-primary"><spring:message code="commerce.verifyPickup.result.total" />:</span>
                            $<c:out value="${res.finalPrice}" />
                        </p>
                    </c:if>
                </c:if>
            </div>
        </c:if>

        <div class="mt-8 flex flex-wrap items-center gap-3">
            <a href="${formAction}"
               class="inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 font-headline text-sm font-bold text-on-primary transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-primary/25">
                <spring:message code="commerce.verifyPickup.tryAnother" />
            </a>
            <a href="${pageContext.request.contextPath}/reservations"
               class="inline-flex items-center justify-center rounded-full bg-surface-container-high px-6 py-3 font-headline text-sm font-semibold text-on-surface transition hover:bg-surface-container-highest focus:outline-none focus:ring-4 focus:ring-primary/15">
                <spring:message code="commerce.verifyPickup.backToReservations" />
            </a>
        </div>
    </c:when>

    <%-- ── Form / error state ── --%>
    <c:otherwise>
        <%-- Reservation data (visible when coming from email token flow with reservation loaded) --%>
        <c:if test="${not empty reservation}">
            <div class="mb-6 inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary-container/30 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-primary">
                <spring:message code="reservation.token.confirm.badge" />
            </div>

            <h2 class="font-headline text-2xl font-extrabold leading-tight text-primary sm:text-3xl">
                <spring:message code="reservation.token.confirm.title" />
            </h2>

            <p class="mt-3 text-base leading-relaxed text-secondary">
                <spring:message code="reservation.token.confirm.description" />
            </p>

            <div class="mt-6 grid gap-3 rounded-2xl bg-surface-container p-5 text-sm sm:grid-cols-2 sm:text-base">
                <p>
                    <span class="font-semibold text-primary"><spring:message code="reservation.token.data.pack" />:</span>
                    <c:out value="${reservation.packId}" />
                </p>
                <p>
                    <span class="font-semibold text-primary"><spring:message code="reservation.token.data.status" />:</span>
                    <c:out value="${reservation.status}" />
                </p>
                <p>
                    <span class="font-semibold text-primary"><spring:message code="reservation.token.data.date" />:</span>
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
        </c:if>


        <form action="${formAction}" method="post" class="mt-6">
            <c:if test="${not empty token}">
                <input type="hidden" name="token" value="<c:out value='${token}'/>" />
            </c:if>

            <c:if test="${showCodeInput}">
                <div class="mb-4">
                    <label class="block text-sm font-medium text-primary mb-2">
                        <spring:message code="commerce.verifyPickup.codeLabel" />
                    </label>
                    <input name="pickupCode" type="text"
                           class="pack-form-control uppercase tracking-widest text-center text-lg font-mono"
                           maxlength="5"
                           placeholder="ABC12"
                           value="<c:out value='${submittedCode}' default=''/>"
                           autocomplete="off" />
                    <c:if test="${not empty pickupError}">
                        <div class="mt-2 text-sm text-error font-medium">
                            <spring:message code="${pickupError}" />
                        </div>
                    </c:if>
                </div>
            </c:if>

            <div class="flex flex-wrap items-center gap-3 mt-6">
                <button type="submit"
                        class="inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 font-headline text-sm font-bold text-on-primary transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-primary/25">
                    <span class="material-symbols-outlined text-sm mr-2">verified</span>
                    <spring:message code="commerce.verifyPickup.submit" />
                </button>
                <a href="${pageContext.request.contextPath}/reservations"
                   class="inline-flex items-center justify-center rounded-full bg-surface-container-high px-6 py-3 font-headline text-sm font-semibold text-on-surface transition hover:bg-surface-container-highest focus:outline-none focus:ring-4 focus:ring-primary/15">
                    <spring:message code="commerce.verifyPickup.backToReservations" />
                </a>
            </div>
        </form>
    </c:otherwise>
</c:choose>
