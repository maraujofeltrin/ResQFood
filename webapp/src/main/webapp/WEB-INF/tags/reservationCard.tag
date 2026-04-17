<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ tag body-content="empty" %>

<%@ attribute name="reservation" required="true" type="ar.edu.itba.paw.models.Reservation" %>
<%@ attribute name="pack" required="true" type="ar.edu.itba.paw.models.Pack" %>
<%@ attribute name="dateLabel" required="true" type="java.lang.String" %>
<%@ attribute name="commerceName" required="false" type="java.lang.String" %>
<%@ attribute name="clientName" required="false" type="java.lang.String" %>
<%@ attribute name="messagePrefix" required="true" type="java.lang.String" %>

<article class="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow flex flex-col h-full">
    <div class="relative h-48 sm:h-56 overflow-hidden">
        <img src="${pageContext.request.contextPath}/packs/${pack.id}/image"
             alt="${pack.title}"
             class="w-full h-full object-cover" />
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
