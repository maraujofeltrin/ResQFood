<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ attribute name="packId" required="true" type="java.lang.Long" %>
<%@ attribute name="items" required="true" type="java.util.List" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>

<c:set var="bidCount" value="${fn:length(items)}"/>
<c:set var="effectivePage" value="${empty currentPage ? 1 : currentPage}"/>
<c:set var="effectiveTotalPages" value="${empty totalPages ? 1 : totalPages}"/>

<div class="bg-surface-container-highest p-8 rounded-2xl font-body">
    <div class="flex items-center gap-2 mb-6">
        <span class="material-symbols-outlined text-primary text-2xl" aria-hidden="true">history</span>
        <h3 class="text-xl font-bold font-headline text-on-surface"><spring:message code="pack.detail.bidHistory.title"/></h3>
    </div>

    <c:choose>
        <c:when test="${bidCount eq 0}">
            <p class="text-sm text-secondary m-0"><spring:message code="pack.detail.bidHistory.empty"/></p>
        </c:when>
        <c:otherwise>
            <div class="space-y-4">
                <c:forEach items="${items}" var="row">
                    <c:set var="avatarBg" value="${row.index mod 2 eq 0 ? 'bg-secondary-container' : 'bg-primary-container'}"/>
                    <div class="flex justify-between items-center p-4 rounded-lg ${row.leading ? 'bg-surface-container-lowest/50' : ''}">
                        <div class="flex items-center gap-3 min-w-0">
                            <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${avatarBg} text-on-surface" aria-hidden="true">
                                <c:out value="${row.initials}"/>
                            </div>
                            <div class="min-w-0">
                                <p class="font-bold text-sm text-on-surface truncate m-0"><c:out value="${row.displayName}"/></p>
                                <p class="text-[10px] uppercase tracking-wider text-on-surface-variant m-0 mt-0.5"><c:out value="${row.relativeTimeLabel}"/></p>
                            </div>
                        </div>
                        <p class="font-black text-sm tabular-nums shrink-0 ml-2 ${row.leading ? 'text-on-surface' : 'text-on-surface-variant opacity-60'}"><c:out value="${row.amountDisplay}"/></p>
                    </div>
                </c:forEach>
            </div>
            <paw:pagination currentPage="${effectivePage}" totalPages="${effectiveTotalPages}"
                            baseUrl="${pageContext.request.contextPath}/packs/${packId}"
                            pageParam="bidPage"/>
        </c:otherwise>
    </c:choose>
</div>
