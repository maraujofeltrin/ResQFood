<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ attribute name="items" required="true" type="java.util.List" %>
<%@ attribute name="reviewCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="averageRating" required="false" type="java.lang.Double" %>
<%@ attribute name="packId" required="false" type="java.lang.Long" %>
<%@ attribute name="canSubmit" required="false" type="java.lang.Boolean" %>
<%@ attribute name="alreadySubmitted" required="false" type="java.lang.Boolean" %>
<%@ attribute name="alertKind" required="false" type="java.lang.String" %>
<%@ attribute name="alertMessage" required="false" type="java.lang.String" %>

<section class="commerce-reviews-card" id="commerce-reviews" aria-label="<spring:message code='pack.detail.reviews.section'/>">
    <div class="commerce-reviews-card__header">
        <div>
            <p class="commerce-reviews-card__eyebrow"><spring:message code="pack.detail.reviews.eyebrow"/></p>
            <h2 class="commerce-reviews-card__title font-headline"><spring:message code="pack.detail.reviews.title"/></h2>
        </div>
        <span class="commerce-reviews-card__count">
            <spring:message code="pack.detail.reviews.count" arguments="${reviewCount}"/>
        </span>
    </div>

    <c:if test="${alertKind eq 'success'}">
        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${alertMessage}"/></p>
    </c:if>
    <c:if test="${alertKind eq 'error'}">
        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${alertMessage}"/></p>
    </c:if>

    <div class="commerce-reviews-card__list<c:if test="${not empty items and fn:length(items) gt 3}"> commerce-reviews-card__list--scrollable</c:if>"<c:if test="${not empty items and fn:length(items) gt 3}"> tabindex="0" role="region" aria-label="<spring:message code='pack.detail.reviews.listScrollAria'/>"</c:if>>
        <c:choose>
            <c:when test="${empty items}">
                <p class="commerce-reviews-card__empty"><spring:message code="pack.detail.reviews.empty"/></p>
            </c:when>
            <c:otherwise>
                <c:forEach var="reviewItem" items="${items}">
                    <paw:reviewCard reviewId="${reviewItem.review.id}"
                                    clientName="${reviewItem.clientName}"
                                    rating="${reviewItem.review.rating}"
                                    body="${reviewItem.review.body}"
                                    formattedDate="${reviewItem.formattedDate}"
                                    edited="${reviewItem.edited}"/>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty packId}">
        <sec:authorize access="hasRole('CLIENT')">
            <c:choose>
                <c:when test="${canSubmit}">
                    <paw:reviewForm packId="${packId}" alreadySubmitted="${alreadySubmitted}"/>
                </c:when>
                <c:otherwise>
                    <p class="commerce-reviews-card__hint"><spring:message code="pack.detail.reviews.notEligible"/></p>
                </c:otherwise>
            </c:choose>
        </sec:authorize>
        <sec:authorize access="isAnonymous()">
            <p class="commerce-reviews-card__hint">
                <spring:message code="pack.detail.reviews.loginPrompt"/>
                <c:url var="reviewLoginUrl" value="/login"/>
                <a href="${reviewLoginUrl}" class="commerce-reviews-card__link"><spring:message code="pack.detail.reviews.loginLink"/></a>
            </p>
        </sec:authorize>
    </c:if>
    <script>
        (function () {
            var root = document.getElementById('commerce-reviews');
            if (!root) {
                return;
            }
            root.addEventListener('click', function (e) {
                var btn = e.target.closest('.commerce-review__read-toggle');
                if (!btn || !root.contains(btn)) {
                    return;
                }
                var id = btn.getAttribute('data-review-id');
                var mode = btn.getAttribute('data-mode');
                var shortEl = document.getElementById('commerce-review-short-' + id);
                var fullEl = document.getElementById('commerce-review-full-' + id);
                if (!shortEl || !fullEl) {
                    return;
                }
                if (mode === 'more') {
                    shortEl.classList.add('hidden');
                    fullEl.classList.remove('hidden');
                } else {
                    fullEl.classList.add('hidden');
                    shortEl.classList.remove('hidden');
                }
            });
        })();
    </script>
</section>
