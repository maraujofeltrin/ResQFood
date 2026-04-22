<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ tag body-content="empty" %>

<%@ attribute name="reservation" required="true" type="ar.edu.itba.paw.models.Reservation" %>
<%@ attribute name="pack" required="true" type="ar.edu.itba.paw.models.Pack" %>
<%@ attribute name="dateLabel" required="true" type="java.lang.String" %>
<%@ attribute name="commerceName" required="false" type="java.lang.String" %>
<%@ attribute name="clientName" required="false" type="java.lang.String" %>
<%@ attribute name="messagePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="selectedStatus" required="false" type="java.lang.String" %>

<article class="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow flex flex-col h-full">
    <div class="relative h-48 sm:h-56 overflow-hidden">
        <img src="${pageContext.request.contextPath}/packs/${pack.id}/image"
             alt="${pack.title}"
             class="w-full h-full object-cover" />

        <c:if test="${messagePrefix == 'commerce.reservations' and reservation.status == 'RESERVED'}">
            <spring:message code="commerce.reservations.card.reject.confirm" var="rejectConfirmMsg" />
            <spring:message code="commerce.reservations.card.reject.label" var="rejectButtonLabel" />
            <spring:message code="commerce.reservations.card.reject.modalTitle" var="rejectModalTitle" />
            <spring:message code="commerce.reservations.card.reject.confirmAction" var="rejectConfirmAction" />
            <spring:message code="commerce.reservations.card.reject.cancel" var="rejectCancelLabel" />

            <button type="button"
                    class="absolute top-4 right-4 z-10 h-10 w-10 rounded-full border-0 bg-white/85 text-error shadow-sm backdrop-blur-sm transition hover:bg-error hover:text-on-error flex items-center justify-center"
                    title="${rejectButtonLabel}"
                    aria-label="${rejectButtonLabel}"
                    onclick="document.getElementById('rejectModal-${reservation.id}').style.display='flex'">
                <span class="material-symbols-outlined text-lg leading-none">close</span>
            </button>

            <paw:modal id="rejectModal-${reservation.id}" title="${rejectModalTitle}">
                <form action="${pageContext.request.contextPath}/reservations/${reservation.id}/reject" method="post" class="space-y-4">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <c:if test="${not empty currentPage and currentPage > 1}">
                        <input type="hidden" name="page" value="${currentPage}" />
                    </c:if>
                    <c:if test="${not empty searchQuery}">
                        <input type="hidden" name="q" value="${searchQuery}" />
                    </c:if>
                    <c:if test="${not empty selectedStatus}">
                        <input type="hidden" name="status" value="${selectedStatus}" />
                    </c:if>

                    <p><c:out value="${rejectConfirmMsg}" /></p>

                    <div class="flex justify-center gap-3 mt-4">
                        <button type="button" class="btn btn-cancel" onclick="document.getElementById('rejectModal-${reservation.id}').style.display='none'">${rejectCancelLabel}</button>
                        <button type="submit" class="btn btn-danger">${rejectConfirmAction}</button>
                    </div>
                </form>
            </paw:modal>
        </c:if>
    </div>

    <div class="p-5 flex flex-col gap-4 flex-grow">
        <div>
            <c:if test="${not empty commerceName}">
                <div class="flex items-center gap-2 text-secondary text-sm font-medium mb-1">
                    <span class="material-symbols-outlined text-[1rem]">storefront</span>
                    <c:out value="${commerceName}" />
                </div>
            </c:if>
            <h3 class="font-bold text-lg text-on-surface truncate"><c:out value="${pack.title}" /></h3>
            <p class="text-secondary text-sm mt-1 line-clamp-2 h-10"><c:out value="${pack.description}" /></p>
        </div>

        <div class="bg-surface-container rounded-xl p-4 space-y-2 text-sm">
            <c:if test="${not empty clientName}">
                <div class="flex items-center justify-between gap-3">
                    <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.client" /></span>
                    <span class="font-semibold text-on-surface"><c:out value="${clientName}" /></span>
                </div>
            </c:if>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.status" /></span>
                <span class="font-semibold text-primary">
                    <spring:message code="reservation.status.${reservation.status}" />
                </span>
            </div>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.quantity" /></span>
                <span class="font-semibold text-on-surface"><c:out value="${reservation.quantity}" /></span>
            </div>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.date" /></span>
                <span class="font-semibold text-on-surface"><c:out value="${dateLabel}" /></span>
            </div>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.total" /></span>
                <span class="font-semibold text-primary">
                    <fmt:formatNumber value="${reservation.finalPrice}" type="currency" currencyCode="ARS" />
                </span>
            </div>
        </div>
    </div>
</article>
