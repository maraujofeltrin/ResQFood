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
<%@ attribute name="commerceUserId" required="false" type="java.lang.Long" %>
<%@ attribute name="canSubmit" required="false" type="java.lang.Boolean" %>
<%@ attribute name="alreadySubmitted" required="false" type="java.lang.Boolean" %>
<%@ attribute name="alertKind" required="false" type="java.lang.String" %>
<%@ attribute name="alertMessage" required="false" type="java.lang.String" %>
<%@ attribute name="formExpanded" required="false" type="java.lang.Boolean" %>
<%@ attribute name="fullWidth" required="false" type="java.lang.Boolean" %>
<%@ attribute name="sectionMessageCode" required="false" type="java.lang.String" %>
<%@ attribute name="titleMessageCode" required="false" type="java.lang.String" %>
<%@ attribute name="eyebrowMessageCode" required="false" type="java.lang.String" %>
<%@ attribute name="reviewCurrentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="reviewTotalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="reviewPaginationBaseUrl" required="false" type="java.lang.String" %>

<c:if test="${empty sectionMessageCode}">
    <c:set var="sectionMessageCode" value="pack.detail.reviews.section"/>
</c:if>
<c:if test="${empty titleMessageCode}">
    <c:set var="titleMessageCode" value="pack.detail.reviews.title"/>
</c:if>
<c:if test="${empty eyebrowMessageCode}">
    <c:set var="eyebrowMessageCode" value="pack.detail.reviews.eyebrow"/>
</c:if>
<c:set var="showReviewForm" value="${not empty packId or not empty commerceUserId}"/>

<section class="commerce-reviews-card<c:if test="${fullWidth}"> commerce-reviews-card--full-width</c:if>"
         id="commerce-reviews"
         aria-label="<spring:message code='${sectionMessageCode}'/>">
    <div class="commerce-reviews-card__header">
        <div class="commerce-reviews-card__header-main">
            <p class="commerce-reviews-card__eyebrow"><spring:message code="${eyebrowMessageCode}"/></p>
            <h2 class="commerce-reviews-card__title font-headline"><spring:message code="${titleMessageCode}"/></h2>
        </div>
        <div class="commerce-reviews-card__header-actions">
            <c:if test="${showReviewForm}">
                <sec:authorize access="hasRole('CLIENT')">
                    <c:if test="${canSubmit}">
                        <c:choose>
                            <c:when test="${alreadySubmitted}">
                                <spring:message code="pack.detail.reviews.form.cta.edit" var="reviewFormCtaLabel"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="pack.detail.reviews.form.cta.create" var="reviewFormCtaLabel"/>
                            </c:otherwise>
                        </c:choose>
                        <c:set var="reviewCtaAriaExpanded" value="false"/>
                        <c:if test="${formExpanded}">
                            <c:set var="reviewCtaAriaExpanded" value="true"/>
                        </c:if>
                        <button type="button"
                                id="commerce-review-form-toggle"
                                class="pack-submit-btn pack-submit-btn--compact font-headline"
                                aria-expanded="${reviewCtaAriaExpanded}"
                                aria-controls="commerce-review-form-panel">
                            <span class="pack-submit-btn__label"><c:out value="${reviewFormCtaLabel}"/></span>
                            <span class="material-symbols-outlined pack-submit-btn__icon" aria-hidden="true">rate_review</span>
                        </button>
                    </c:if>
                </sec:authorize>
            </c:if>
            <span class="commerce-reviews-card__count">
                <spring:message code="pack.detail.reviews.count" arguments="${reviewCount}"/>
            </span>
        </div>
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
                                    edited="${reviewItem.edited}"
                                    own="${reviewItem.own}"/>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <c:if test="${not empty reviewCurrentPage and not empty reviewTotalPages and reviewTotalPages > 1}">
        <paw:pagination currentPage="${reviewCurrentPage}" totalPages="${reviewTotalPages}"
                        baseUrl="${reviewPaginationBaseUrl}" pageParam="reviewPage"/>
    </c:if>

    <c:if test="${showReviewForm}">
        <sec:authorize access="hasRole('CLIENT')">
            <c:choose>
                <c:when test="${canSubmit}">
                    <paw:reviewForm packId="${packId}"
                                    commerceUserId="${commerceUserId}"
                                    alreadySubmitted="${alreadySubmitted}"
                                    formExpanded="${formExpanded}"/>
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

            var toggleBtn = document.getElementById('commerce-review-form-toggle');
            var panel = document.getElementById('commerce-review-form-panel');
            if (toggleBtn && panel) {
                function scrollToReviewForm() {
                    panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }
                function expandPanel() {
                    panel.classList.remove('hidden');
                    toggleBtn.setAttribute('aria-expanded', 'true');
                    if (typeof window.initCommerceReviewStars === 'function') {
                        window.initCommerceReviewStars();
                    }
                }
                toggleBtn.addEventListener('click', function () {
                    var hidden = panel.classList.contains('hidden');
                    if (hidden) {
                        expandPanel();
                        scrollToReviewForm();
                    } else {
                        scrollToReviewForm();
                    }
                });
                if (!panel.classList.contains('hidden')) {
                    if (typeof window.initCommerceReviewStars === 'function') {
                        window.initCommerceReviewStars();
                    }
                    requestAnimationFrame(function () {
                        scrollToReviewForm();
                    });
                }
            }
        })();
    </script>
</section>
