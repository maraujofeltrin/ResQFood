<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ attribute name="packId" required="true" type="java.lang.Long" %>
<%@ attribute name="items" required="true" type="java.util.List" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>

<c:set var="resCount" value="${fn:length(items)}"/>
<c:set var="effectivePage" value="${empty currentPage ? 1 : currentPage}"/>
<c:set var="effectiveTotalPages" value="${empty totalPages ? 1 : totalPages}"/>

<div class="bg-surface-container-highest p-8 rounded-2xl font-body">
    <div class="flex items-center justify-between gap-2 mb-6">
        <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-2xl" aria-hidden="true">receipt_long</span>
            <h3 class="text-xl font-bold font-headline text-on-surface"><spring:message code="pack.detail.reservationHistory.title"/></h3>
        </div>
        <a href="${pageContext.request.contextPath}/reservations"
           class="flex items-center justify-center h-10 w-10 text-primary bg-primary/10 hover:bg-primary hover:text-on-primary rounded-full transition-all hover:scale-110 shadow-sm"
           title="<spring:message code='pack.detail.reservationHistory.viewMyReservations'/>"
           aria-label="<spring:message code='pack.detail.reservationHistory.viewMyReservations'/>">
            <span class="material-symbols-outlined text-[1.25rem]">arrow_forward</span>
        </a>
    </div>

    <c:choose>
        <c:when test="${resCount eq 0}">
            <p class="text-sm text-secondary m-0"><spring:message code="pack.detail.reservationHistory.empty"/></p>
        </c:when>
        <c:otherwise>
            <div class="space-y-4">
                <c:forEach items="${items}" var="row">
                    <c:set var="avatarBg" value="${row.index mod 2 eq 0 ? 'bg-secondary-container' : 'bg-primary-container'}"/>

                    <c:choose>
                        <c:when test="${row.status == 'RESERVED'}">
                            <c:set var="statusColor" value="text-amber-500" />
                        </c:when>
                        <c:when test="${row.status == 'PAID'}">
                            <c:set var="statusColor" value="text-primary" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="statusColor" value="text-error" />
                        </c:otherwise>
                    </c:choose>

                    <div class="flex justify-between items-center p-4 rounded-lg bg-surface-container-lowest/50">
                        <div class="flex items-center gap-3 min-w-0">
                            <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${avatarBg} text-on-surface" aria-hidden="true">
                                <c:out value="${row.initials}"/>
                            </div>
                            <div class="min-w-0">
                                <p class="font-bold text-sm text-on-surface truncate m-0"><c:out value="${row.displayName}"/></p>
                                <div class="flex items-center gap-2 mt-0.5">
                                    <p class="text-[10px] uppercase tracking-wider text-on-surface-variant m-0"><c:out value="${row.relativeTimeLabel}"/></p>
                                    <span class="text-[10px] font-bold uppercase tracking-wider ${statusColor}">
                                        <spring:message code="reservation.status.${row.status}" />
                                    </span>
                                </div>
                            </div>
                        </div>
                        <p class="font-black text-sm tabular-nums shrink-0 ml-2 text-on-surface"><c:out value="${row.amountDisplay}"/></p>
                    </div>
                </c:forEach>
            </div>
            <paw:pagination currentPage="${effectivePage}" totalPages="${effectiveTotalPages}"
                            baseUrl="${pageContext.request.contextPath}/packs/${packId}"
                            pageParam="reservationPage"/>
        </c:otherwise>
    </c:choose>

</div>
