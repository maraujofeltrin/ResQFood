<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<%@ attribute name="commercialName" required="false" %>
<%@ attribute name="streetLine" required="true" %>
<%@ attribute name="locationLine" required="true" %>
<%@ attribute name="openingTime" required="true" %>
<%@ attribute name="closingTime" required="true" %>
<%@ attribute name="openNow" required="true" type="java.lang.Boolean" %>
<%@ attribute name="showStoreName" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showClosedWarning" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showHeader" required="false" type="java.lang.Boolean" %>
<%@ attribute name="commerceUserId" required="false" type="java.lang.Long" %>
<%@ attribute name="profileImageId" required="false" type="java.lang.Long" %>
<%@ attribute name="averageRating" required="false" type="java.lang.Double" %>
<%@ attribute name="reviewCount" required="false" type="java.lang.Integer" %>
<%@ attribute name="favoriteSelected" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showFavoriteButton" required="false" type="java.lang.Boolean" %>

<c:if test="${empty showHeader}">
    <c:set var="showHeader" value="true"/>
</c:if>
<c:if test="${empty reviewCount}">
    <c:set var="reviewCount" value="0"/>
</c:if>

<div class="commerce-info-card">
    <c:if test="${showHeader}">
        <div class="commerce-info-card__header">
            <div class="commerce-info-card__header-main">
                <c:url var="commerceProfileUrl" value="/commerces/${commerceUserId}"/>
                <c:set var="escapedCommercialName"><c:out value="${commercialName}"/></c:set>
                <spring:message code="commerce.info.profileLink.aria" arguments="${escapedCommercialName}" var="commerceProfileLinkAria"/>
                <a href="${commerceProfileUrl}"
                   class="commerce-info-card__profile-link font-headline"
                   aria-label="${commerceProfileLinkAria}">
                    <span class="commerce-info-card__profile-link-avatar" aria-hidden="true">
                        <c:choose>
                            <c:when test="${not empty profileImageId}">
                                <img src="${pageContext.request.contextPath}/images/${profileImageId}"
                                     alt=""/>
                            </c:when>
                            <c:otherwise>
                                <span class="material-symbols-outlined commerce-info-card__profile-link-avatar-fallback">storefront</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="commerce-info-card__profile-link-name"><c:out value="${commercialName}"/></span>
                    <span class="material-symbols-outlined commerce-info-card__profile-link-chevron" aria-hidden="true">chevron_right</span>
                </a>
            </div>
            
            <c:if test="${showFavoriteButton}">
                <sec:authorize access="hasRole('CLIENT')">
                    <form action="${pageContext.request.contextPath}/commerces/${commerceUserId}/favorite" method="post" class="commerce-info-card__favorite-form">
                        <button type="submit"
                                class="commerce-info-card__favorite-btn<c:if test='${favoriteSelected}'> commerce-info-card__favorite-btn--selected</c:if>"
                                aria-label="<spring:message code='commerce.profile.favorite.toggleAria'/>"
                                aria-pressed="${favoriteSelected}">
                            <span class="material-symbols-outlined commerce-info-card__favorite-icon" aria-hidden="true">favorite</span>
                        </button>
                    </form>
                </sec:authorize>
            </c:if>

            <paw:commerceReviewSummary averageRating="${averageRating}" reviewCount="${reviewCount}"/>
        </div>
    </c:if>
    <paw:commerceInfoCardBody
        commercialName="${commercialName}"
        streetLine="${streetLine}"
        locationLine="${locationLine}"
        openingTime="${openingTime}"
        closingTime="${closingTime}"
        openNow="${openNow}"
        showStoreName="${showStoreName}"
        showClosedWarning="${showClosedWarning}"/>
</div>
