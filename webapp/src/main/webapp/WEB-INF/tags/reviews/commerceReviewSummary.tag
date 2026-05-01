<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="averageRating" required="false" type="java.lang.Double" %>
<%@ attribute name="reviewCount" required="true" type="java.lang.Integer" %>

<c:if test="${reviewCount > 0}">
    <div class="commerce-review-summary">
        <c:if test="${not empty averageRating}">
            <span class="commerce-review-summary__stars" aria-label="<spring:message code='commerce.reviews.averageLabel'/>">
                <c:set var="roundedRating" value="${averageRating > 0 ? (averageRating - (averageRating % 1) + (averageRating % 1 >= 0.5 ? 1 : 0)) : 0}"/>
                <c:forEach begin="1" end="5" var="i">
                    <c:choose>
                        <c:when test="${i <= roundedRating}">
                            <span class="material-symbols-outlined commerce-review-summary__star--filled" aria-hidden="true">star</span>
                        </c:when>
                        <c:otherwise>
                            <span class="material-symbols-outlined commerce-review-summary__star--empty" aria-hidden="true">star</span>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </span>
            <span class="commerce-review-summary__avg">
                <fmt:formatNumber value="${averageRating}" maxFractionDigits="1" minFractionDigits="1"/>
            </span>
        </c:if>
        <span class="commerce-review-summary__count">
            (<spring:message code="pack.detail.reviews.count" arguments="${reviewCount}"/>)
        </span>
    </div>
</c:if>
