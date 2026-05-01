<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="packId" required="true" %>
<%@ attribute name="imageId" required="false" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="imageAlt" required="false" %>
<%@ attribute name="badgeText" required="false" %>
<%@ attribute name="rescueLabel" required="false" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="oldPrice" required="false" %>
<%@ attribute name="commerceName" required="false" %>
<%@ attribute name="commerceId" required="false" %>
<%@ attribute name="manageable" type="java.lang.Boolean" required="false" %>
<%@ attribute name="smallSize" required="false" type="java.lang.Boolean" %>
<%@ attribute name="auction" type="java.lang.Boolean" required="false" %>
<%@ attribute name="auctionId" required="false" %>
<%@ attribute name="auctionActive" type="java.lang.Boolean" required="false" %>
<%@ attribute name="auctionHasBids" type="java.lang.Boolean" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<c:set var="resolvedRescueLabel" value="${not empty rescueLabel ? rescueLabel : 'Rescue For'}"/>

<paw:packCardShell packId="${packId}" imageId="${imageId}" title="${title}" subtitle="${subtitle}" imageAlt="${imageAlt}"
    badgeText="${badgeText}" commerceName="${commerceName}" manageable="${manageable}" smallSize="${smallSize}"
    auction="${auction}" asLink="true" showPriceFooter="true"
    price="${price}" oldPrice="${oldPrice}" rescueLabel="${resolvedRescueLabel}" auctionId="${auctionId}" auctionActive="${auctionActive}"
    auctionHasBids="${auctionHasBids}">
    <jsp:attribute name="imageOverlay"></jsp:attribute>
    <jsp:body></jsp:body>
</paw:packCardShell>
