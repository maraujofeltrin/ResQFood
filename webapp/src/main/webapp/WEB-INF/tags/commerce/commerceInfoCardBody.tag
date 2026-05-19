<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="commercialName" required="false" %>
<%@ attribute name="streetLine" required="true" %>
<%@ attribute name="locationLine" required="true" %>
<%@ attribute name="openingTime" required="true" %>
<%@ attribute name="closingTime" required="true" %>
<%@ attribute name="openNow" required="true" type="java.lang.Boolean" %>
<%@ attribute name="showStoreName" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showClosedWarning" required="false" type="java.lang.Boolean" %>

<c:if test="${empty showStoreName}">
    <c:set var="showStoreName" value="false"/>
</c:if>
<c:if test="${empty showClosedWarning}">
    <c:set var="showClosedWarning" value="true"/>
</c:if>

<div class="commerce-info-card__grid">
    <div class="commerce-info-card__column">
        <h3 class="commerce-info-card__section-title font-headline">
            <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">location_on</span>
            <spring:message code="commerce.info.location"/>
        </h3>
        <div class="commerce-info-card__address">
            <c:if test="${showStoreName}">
                <p class="commerce-info-card__store-name"><c:out value="${commercialName}"/></p>
            </c:if>
            <p><c:out value="${streetLine}"/></p>
            <p><c:out value="${locationLine}"/></p>
        </div>
    </div>
    <div class="commerce-info-card__column">
        <h3 class="commerce-info-card__section-title font-headline">
            <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
            <spring:message code="commerce.info.hours"/>
        </h3>
        <div class="commerce-info-card__hours">
            <div class="commerce-info-card__hours-row">
                <span class="commerce-info-card__hours-label"><spring:message code="commerce.info.opening"/></span>
                <span class="commerce-info-card__hours-value"><c:out value="${openingTime}"/></span>
            </div>
            <div class="commerce-info-card__hours-row">
                <span class="commerce-info-card__hours-label"><spring:message code="commerce.info.closing"/></span>
                <span class="commerce-info-card__hours-value"><c:out value="${closingTime}"/></span>
            </div>
            <div class="commerce-info-card__status">
                <c:choose>
                    <c:when test="${openNow}">
                        <span class="commerce-info-card__status-badge"><spring:message code="commerce.info.openNow"/></span>
                    </c:when>
                    <c:otherwise>
                        <span class="commerce-info-card__status-badge commerce-info-card__status-badge--closed"><spring:message code="commerce.info.closedNow"/></span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
<div class="commerce-info-card__note">
    <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">info</span>
    <p class="commerce-info-card__note-text">
        <spring:message code="commerce.info.pickupNote"/>
    </p>
</div>
<c:if test="${showClosedWarning and not openNow}">
    <div class="commerce-info-card__note commerce-info-card__note--warning">
        <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
        <p class="commerce-info-card__note-text">
            <spring:message code="commerce.info.closedNote"/>
        </p>
    </div>
</c:if>
