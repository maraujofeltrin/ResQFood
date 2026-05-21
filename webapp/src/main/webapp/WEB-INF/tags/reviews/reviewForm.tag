<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="packId" required="false" type="java.lang.Long" %>
<%@ attribute name="commerceUserId" required="false" type="java.lang.Long" %>
<%@ attribute name="alreadySubmitted" required="false" type="java.lang.Boolean" %>
<%@ attribute name="formExpanded" required="false" type="java.lang.Boolean" %>

<c:choose>
    <c:when test="${not empty packId}">
        <c:url var="commerceReviewAction" value="/packs/${packId}/commerce-review"/>
    </c:when>
    <c:otherwise>
        <c:url var="commerceReviewAction" value="/commerces/${commerceUserId}/commerce-review"/>
    </c:otherwise>
</c:choose>

<div class="commerce-review-form-card" id="commerce-review-form-root">
    <div id="commerce-review-form-panel"
         class="commerce-review-form-card__panel<c:if test='${not formExpanded}'> hidden</c:if>">
        <h3 class="commerce-review-form-card__title font-headline">
            <c:choose>
                <c:when test="${alreadySubmitted}">
                    <spring:message code="pack.detail.reviews.form.editTitle"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="pack.detail.reviews.form.title"/>
                </c:otherwise>
            </c:choose>
        </h3>
        <form:form modelAttribute="commerceReviewForm" method="post" action="${commerceReviewAction}"
                   cssClass="commerce-review-form" novalidate="novalidate" id="commerce-review-form">
            <div class="commerce-review-form__field">
                <span class="pack-form-label"><spring:message code="pack.detail.reviews.form.rating"/></span>
                <div class="star-rating-input" role="radiogroup" aria-label="<spring:message code='pack.detail.reviews.form.rating'/>">
                    <c:forEach begin="1" end="5" var="ratingOption">
                        <label class="star-rating-input__label" data-value="${ratingOption}">
                            <form:radiobutton path="rating" value="${ratingOption}" cssClass="star-rating-input__radio"/>
                            <span class="material-symbols-outlined star-rating-input__star" aria-hidden="true">star</span>
                        </label>
                    </c:forEach>
                </div>
                <form:errors path="rating" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p"/>
            </div>
            <div class="commerce-review-form__field">
                <label class="pack-form-label" for="commerce-review-body"><spring:message code="pack.detail.reviews.form.body"/></label>
                <p class="commerce-review-form__body-hint text-sm text-secondary m-0 mb-1"><spring:message code="pack.detail.reviews.form.bodyHint"/></p>
                <form:textarea path="body" id="commerce-review-body" cssClass="commerce-review-form__textarea" rows="3" maxlength="500"/>
                <form:errors path="body" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p"/>
                <p class="pack-feedback pack-feedback--error pack-form-errors hidden" id="commerce-review-body-client-error" role="alert">
                    <spring:message code="pack.detail.reviews.alert.invalid"/>
                </p>
            </div>
            <spring:message code="pack.detail.reviews.form.submitting" var="reviewSubmittingText"/>
            <button type="submit" class="pack-submit-btn font-headline" id="commerce-review-submit-btn"
                    data-submitting-text="<c:out value='${reviewSubmittingText}'/>">
                <span class="pack-submit-btn__label"><spring:message code="pack.detail.reviews.form.submit"/></span>
                <span class="material-symbols-outlined pack-submit-btn__icon">rate_review</span>
            </button>
        </form:form>
    </div>
</div>

<script>
(function () {
    var panel = document.getElementById('commerce-review-form-panel');
    var form = document.getElementById('commerce-review-form');
    var submitBtn = document.getElementById('commerce-review-submit-btn');
    var bodyInput = document.getElementById('commerce-review-body');
    var bodyClientError = document.getElementById('commerce-review-body-client-error');

    function initStarRating(group) {
        var labels = group.querySelectorAll('.star-rating-input__label');
        function updateStars(activeIndex, cls) {
            labels.forEach(function (lbl, i) {
                var star = lbl.querySelector('.star-rating-input__star');
                if (i <= activeIndex) {
                    star.classList.add(cls);
                } else {
                    star.classList.remove(cls);
                }
            });
        }
        function getCheckedIndex() {
            var checked = group.querySelector('.star-rating-input__radio:checked');
            if (!checked) {
                return -1;
            }
            return parseInt(checked.value, 10) - 1;
        }
        updateStars(getCheckedIndex(), 'star-rating-input__star--filled');
        labels.forEach(function (lbl, i) {
            lbl.addEventListener('mouseenter', function () {
                updateStars(i, 'star-rating-input__star--hover');
            });
            lbl.addEventListener('click', function () {
                updateStars(i, 'star-rating-input__star--filled');
            });
        });
        group.addEventListener('mouseleave', function () {
            labels.forEach(function (lbl) {
                lbl.querySelector('.star-rating-input__star').classList.remove('star-rating-input__star--hover');
            });
            updateStars(getCheckedIndex(), 'star-rating-input__star--filled');
        });
    }

    window.initCommerceReviewStars = function () {
        if (!panel) {
            return;
        }
        panel.querySelectorAll('.star-rating-input').forEach(initStarRating);
    };

    if (panel && !panel.classList.contains('hidden')) {
        window.initCommerceReviewStars();
    }

    if (form && submitBtn) {
        var submitted = false;
        form.addEventListener('submit', function (e) {
            if (bodyInput && bodyClientError) {
                var trimmed = (bodyInput.value || '').trim();
                if (trimmed.length === 0) {
                    e.preventDefault();
                    bodyClientError.classList.remove('hidden');
                    if (bodyInput.setCustomValidity) {
                        bodyInput.setCustomValidity(' ');
                        bodyInput.reportValidity();
                    }
                    return;
                }
                bodyClientError.classList.add('hidden');
                if (bodyInput.setCustomValidity) {
                    bodyInput.setCustomValidity('');
                }
            }
            if (submitted) {
                e.preventDefault();
                return;
            }
            submitted = true;
            submitBtn.disabled = true;
            submitBtn.setAttribute('aria-disabled', 'true');
            submitBtn.classList.add('pack-submit-btn--submitting');
            var label = submitBtn.querySelector('.pack-submit-btn__label');
            if (label && submitBtn.getAttribute('data-submitting-text')) {
                label.textContent = submitBtn.getAttribute('data-submitting-text');
            }
        });
        if (bodyInput && bodyClientError) {
            bodyInput.addEventListener('input', function () {
                if ((bodyInput.value || '').trim().length > 0) {
                    bodyClientError.classList.add('hidden');
                }
            });
        }
    }
})();
</script>
