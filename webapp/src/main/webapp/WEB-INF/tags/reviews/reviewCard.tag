<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="reviewId" required="true" type="java.lang.Long" %>
<%@ attribute name="clientName" required="true" type="java.lang.String" %>
<%@ attribute name="rating" required="true" type="java.lang.Integer" %>
<%@ attribute name="body" required="true" type="java.lang.String" %>
<%@ attribute name="formattedDate" required="false" type="java.lang.String" %>
<%@ attribute name="edited" required="false" type="java.lang.Boolean" %>

<article class="commerce-review">
    <div class="commerce-review__meta">
        <span class="commerce-review__client"><c:out value="${clientName}"/></span>
        <span class="commerce-review__stars" aria-label="<spring:message code='pack.detail.reviews.ratingAria' arguments='${rating}'/>">
            <c:forEach begin="1" end="${rating}">
                <span class="material-symbols-outlined" aria-hidden="true">star</span>
            </c:forEach>
            <c:if test="${rating < 5}">
                <c:forEach begin="1" end="${5 - rating}">
                    <span class="material-symbols-outlined commerce-review__star--empty" aria-hidden="true">star</span>
                </c:forEach>
            </c:if>
        </span>
    </div>
    <c:choose>
        <c:when test="${fn:length(body) > 255}">
            <p class="commerce-review__body commerce-review__body--truncated break-words overflow-hidden" id="commerce-review-short-${reviewId}">
                <c:out value="${fn:substring(body, 0, 255)}"/>...
                <button type="button" class="commerce-review__read-toggle font-bold ml-1 hover:underline cursor-pointer" data-review-id="${reviewId}" data-mode="more"><spring:message code="pack.detail.readMore"/></button>
            </p>
            <p class="commerce-review__body commerce-review__body--full break-words overflow-hidden hidden" id="commerce-review-full-${reviewId}">
                <c:out value="${body}"/>
                <button type="button" class="commerce-review__read-toggle font-bold ml-1 hover:underline cursor-pointer" data-review-id="${reviewId}" data-mode="less"><spring:message code="pack.detail.readLess"/></button>
            </p>
        </c:when>
        <c:otherwise>
            <p class="commerce-review__body break-words overflow-hidden"><c:out value="${body}"/></p>
        </c:otherwise>
    </c:choose>
    <c:if test="${not empty formattedDate}">
        <p class="commerce-review__date">
            <c:choose>
                <c:when test="${edited}">
                    <spring:message code="commerce.reviews.editedPrefix"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="commerce.reviews.datePrefix"/>
                </c:otherwise>
            </c:choose>
            <c:out value="${formattedDate}"/>
        </p>
    </c:if>
</article>
