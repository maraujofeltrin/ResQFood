<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<%@ attribute name="auction" required="true" type="ar.edu.itba.paw.models.auction.Auction" %>
<%@ attribute name="commerceName" required="true" type="java.lang.String" %>
<%@ attribute name="endLabel" required="true" type="java.lang.String" %>
<%@ attribute name="myMaxBid" required="true" type="java.lang.Double" %>
<%@ attribute name="badgeCode" required="false" type="java.lang.String" %>

<c:set var="pack" value="${auction.pack}" />
<fmt:formatNumber value="${auction.effectivePrice}" type="currency" currencyCode="ARS" var="formattedEffective" />
<fmt:formatNumber value="${myMaxBid}" type="currency" currencyCode="ARS" var="formattedMyBid" />
<spring:message code="reservation.my.auction.card.currentPrice" var="priceFooterLabel" />

<paw:packCardShell packId="${pack.id}" imageId="${pack.imageId}" title="${pack.title}" subtitle="${pack.description}"
    commerceName="${commerceName}" auction="true" asLink="true"
    participationBadgeCode="${badgeCode}"
    showPriceFooter="true" price="${formattedEffective}" rescueLabel="${priceFooterLabel}">
    <jsp:attribute name="imageOverlay"></jsp:attribute>
    <jsp:body>
        <div class="bg-surface-container rounded-xl p-4 space-y-2 text-sm mb-2">
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="reservation.my.auction.card.status" /></span>
                <span class="font-semibold text-primary">
                    <spring:message code="auction.status.${auction.status}" />
                </span>
            </div>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="reservation.my.auction.card.myBid" /></span>
                <span class="font-semibold text-on-surface"><c:out value="${formattedMyBid}"/></span>
            </div>
            <div class="flex items-center justify-between gap-3">
                <span class="text-on-surface-variant"><spring:message code="reservation.my.auction.card.ends" /></span>
                <span class="font-semibold text-on-surface"><c:out value="${endLabel}" /></span>
            </div>
        </div>
    </jsp:body>
</paw:packCardShell>
