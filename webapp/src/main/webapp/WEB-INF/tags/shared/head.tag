<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="title" required="false" type="java.lang.String" %>
<%@ attribute name="titleSuffixCode" required="false" type="java.lang.String" %>

<head>
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    
    <c:choose>
        <c:when test="${not empty title}">
            <title><c:out value="${title}"/></title>
        </c:when>
        <c:when test="${not empty titleSuffixCode}">
            <title><spring:message code="app.brand"/> | <spring:message code="${titleSuffixCode}"/></title>
        </c:when>
        <c:otherwise>
            <title><spring:message code="app.brand"/></title>
        </c:otherwise>
    </c:choose>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/tokens.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/utilities.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/buttons.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/forms.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/deal-card.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/modal.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/pack-detail-layout.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/pack-detail-commerce-card.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/pack-detail-aside.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/auction.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/commerce-reviews-dashboard.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/favorites.css"/>
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="anonymous" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>

    <%-- Custom Head Content, e.g. <style> for specific pages --%>
    <jsp:doBody />
</head>
