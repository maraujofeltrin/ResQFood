<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ tag body-content="empty" %>

<%@ attribute name="reservation" required="true" type="ar.edu.itba.paw.models.reservation.Reservation" %>
<%@ attribute name="pack" required="true" type="ar.edu.itba.paw.models.pack.Pack" %>
<%@ attribute name="dateLabel" required="true" type="java.lang.String" %>
<%@ attribute name="commerceName" required="false" type="java.lang.String" %>
<%@ attribute name="clientName" required="false" type="java.lang.String" %>
<%@ attribute name="messagePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="selectedStatus" required="false" type="java.lang.String" %>
<%@ attribute name="auctionVisual" type="java.lang.Boolean" required="false" %>

<c:choose>
    <c:when test="${reservation.status == 'RESERVED'}">
        <c:set var="statusColorClass" value="text-amber-500" />
    </c:when>
    <c:when test="${reservation.status == 'PAID'}">
        <c:set var="statusColorClass" value="text-primary" />
    </c:when>
    <c:otherwise>
        <c:set var="statusColorClass" value="text-error" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${auctionVisual == true}">
        <fmt:formatNumber value="${reservation.finalPrice}" type="currency" currencyCode="ARS" var="formattedReservationTotal" />
        <paw:packCardShell packId="${pack.id}" imageId="${pack.imageId}" title="${pack.title}" subtitle="${pack.description}"
            commerceName="${commerceName}" auction="true" asLink="true" smallSize="${messagePrefix == 'commerce.reservations'}">
                <jsp:attribute name="imageOverlay">
                    <!-- Removed top-right cross action — replaced by action buttons below the details -->
                </jsp:attribute>
            <jsp:body>
                <div class="bg-surface-container rounded-xl p-4 space-y-2 text-sm mb-2">
                    <c:if test="${not empty clientName}">
                        <div class="flex items-center justify-between gap-3">
                            <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.client" /></span>
                            <span class="font-semibold text-on-surface"><c:out value="${clientName}" /></span>
                        </div>
                    </c:if>
                    <div class="flex items-center justify-between gap-3">
                        <span class="text-on-surface-variant"><spring:message code="${messagePrefix}.card.status" /></span>
                        <span class="font-semibold ${statusColorClass}">
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
                            <c:out value="${formattedReservationTotal}"/>
                        </span>
                    </div>
                </div>

                <div class="mt-3 flex items-center justify-end gap-3">
                    <spring:message code="commerce.reservations.card.accept.label" var="acceptButtonLabel" />
                    <spring:message code="commerce.reservations.card.cancel.label" var="cancelButtonLabel" />
                    <button type="button"
                            title="<c:out value='${acceptButtonLabel}'/>"
                            aria-label="<c:out value='${acceptButtonLabel}'/>"
                            onclick="event.preventDefault(); event.stopPropagation(); window.location.href='${pageContext.request.contextPath}/commerce/verify-pickup';"
                            class="bg-primary text-on-primary px-4 py-2 rounded-full font-semibold hover:brightness-105 transition inline-flex items-center gap-2">
                        <span class="material-symbols-outlined text-base">qr_code_scanner</span>
                        <span><c:out value="${acceptButtonLabel}"/></span>
                    </button>
                    <button type="button"
                            title="<c:out value='${cancelButtonLabel}'/>"
                            aria-label="<c:out value='${cancelButtonLabel}'/>"
                            onclick="event.preventDefault(); event.stopPropagation(); document.getElementById('rejectModal-${reservation.id}').style.display='flex';"
                            class="bg-surface-container-high text-on-surface px-4 py-2 rounded-full font-semibold hover:bg-surface-container-highest transition inline-flex items-center gap-2">
                        <span><c:out value="${cancelButtonLabel}"/></span>
                    </button>
                </div>
            </jsp:body>
        </paw:packCardShell>

        <c:if test="${messagePrefix == 'commerce.reservations' and reservation.status == 'RESERVED'}">
            <spring:message code="commerce.reservations.card.reject.confirm" var="rejectConfirmMsg" />
            <spring:message code="commerce.reservations.card.reject.label" var="rejectButtonLabel" />
            <spring:message code="commerce.reservations.card.reject.modalTitle" var="rejectModalTitle" />
            <spring:message code="commerce.reservations.card.reject.confirmAction" var="rejectConfirmAction" />
            <spring:message code="commerce.reservations.card.reject.cancel" var="rejectCancelLabel" />

            <paw:modal id="rejectModal-${reservation.id}" title="${rejectModalTitle}">
                <form action="${pageContext.request.contextPath}/reservations/${reservation.id}/reject" method="post" class="space-y-4">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <c:if test="${not empty currentPage and currentPage > 1}">
                        <input type="hidden" name="page" value="<c:out value='${currentPage}'/>" />
                    </c:if>
                    <c:if test="${not empty searchQuery}">
                        <input type="hidden" name="q" value="<c:out value='${searchQuery}'/>" />
                    </c:if>
                    <c:if test="${not empty selectedStatus}">
                        <input type="hidden" name="status" value="<c:out value='${selectedStatus}'/>" />
                    </c:if>
                    <p><c:out value="${rejectConfirmMsg}" /></p>
                    <div class="flex justify-center gap-3 mt-4">
                        <button type="button" class="btn btn-cancel" onclick="document.getElementById('rejectModal-${reservation.id}').style.display='none'"><c:out value="${rejectCancelLabel}" /></button>
                        <button type="submit" class="btn btn-danger"><c:out value="${rejectConfirmAction}" /></button>
                    </div>
                </form>
            </paw:modal>
        </c:if>
    </c:when>
    <c:otherwise>
        <a href="${pageContext.request.contextPath}/packs/${pack.id}" class="block bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md hover:bg-surface-container-low transition-colors transition-shadow flex flex-col h-full cursor-pointer no-underline text-inherit group">
            <c:choose>
                <c:when test="${messagePrefix == 'commerce.reservations'}">
                    <div class="relative h-36 sm:h-44 overflow-hidden">
                </c:when>
                <c:otherwise>
                    <div class="relative h-48 sm:h-56 overflow-hidden">
                </c:otherwise>
            </c:choose>
                <c:choose>
                    <c:when test="${not empty pack.imageId}">
                        <img src="${pageContext.request.contextPath}/images/${pack.imageId}"
                             alt="<c:out value='${pack.title}'/>"
                             class="w-full h-full object-cover" />
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/images/pack-placeholder.svg"
                             alt="<c:out value='${pack.title}'/>"
                             class="w-full h-full object-cover" />
                    </c:otherwise>
                </c:choose>
                <!-- top-right cross removed; actions moved into the card footer -->
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
                        <span class="font-semibold ${statusColorClass}">
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

                <div class="mt-3 flex items-center justify-end gap-3">
                    <spring:message code="commerce.reservations.card.accept.label" var="acceptButtonLabel" />
                    <spring:message code="commerce.reservations.card.cancel.label" var="cancelButtonLabel" />
                    <button type="button"
                            title="<c:out value='${acceptButtonLabel}'/>"
                            aria-label="<c:out value='${acceptButtonLabel}'/>"
                            onclick="event.preventDefault(); event.stopPropagation(); window.location.href='${pageContext.request.contextPath}/commerce/verify-pickup';"
                            class="bg-primary text-on-primary px-4 py-2 rounded-full font-semibold hover:brightness-105 transition inline-flex items-center gap-2">
                        <span class="material-symbols-outlined text-base">qr_code_scanner</span>
                        <span><c:out value="${acceptButtonLabel}"/></span>
                    </button>
                    <button type="button"
                            title="<c:out value='${cancelButtonLabel}'/>"
                            aria-label="<c:out value='${cancelButtonLabel}'/>"
                            onclick="event.preventDefault(); event.stopPropagation(); document.getElementById('rejectModal-${reservation.id}').style.display='flex';"
                            class="bg-surface-container-high text-on-surface px-4 py-2 rounded-full font-semibold hover:bg-surface-container-highest transition inline-flex items-center gap-2">
                        <span><c:out value="${cancelButtonLabel}"/></span>
                    </button>
                </div>
            </div>
        </a>

        <c:if test="${messagePrefix == 'commerce.reservations' and reservation.status == 'RESERVED'}">
            <spring:message code="commerce.reservations.card.reject.confirm" var="rejectConfirmMsg" />
            <spring:message code="commerce.reservations.card.reject.label" var="rejectButtonLabel" />
            <spring:message code="commerce.reservations.card.reject.modalTitle" var="rejectModalTitle" />
            <spring:message code="commerce.reservations.card.reject.confirmAction" var="rejectConfirmAction" />
            <spring:message code="commerce.reservations.card.reject.cancel" var="rejectCancelLabel" />

            <paw:modal id="rejectModal-${reservation.id}" title="${rejectModalTitle}">
                <form action="${pageContext.request.contextPath}/reservations/${reservation.id}/reject" method="post" class="space-y-4">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <c:if test="${not empty currentPage and currentPage > 1}">
                        <input type="hidden" name="page" value="<c:out value='${currentPage}'/>" />
                    </c:if>
                    <c:if test="${not empty searchQuery}">
                        <input type="hidden" name="q" value="<c:out value='${searchQuery}'/>" />
                    </c:if>
                    <c:if test="${not empty selectedStatus}">
                        <input type="hidden" name="status" value="<c:out value='${selectedStatus}'/>" />
                    </c:if>

                    <p><c:out value="${rejectConfirmMsg}" /></p>

                    <div class="flex justify-center gap-3 mt-4">
                        <button type="button" class="btn btn-cancel" onclick="document.getElementById('rejectModal-${reservation.id}').style.display='none'"><c:out value="${rejectCancelLabel}" /></button>
                        <button type="submit" class="btn btn-danger"><c:out value="${rejectConfirmAction}" /></button>
                    </div>
                </form>
            </paw:modal>
        </c:if>
    </c:otherwise>
</c:choose>
