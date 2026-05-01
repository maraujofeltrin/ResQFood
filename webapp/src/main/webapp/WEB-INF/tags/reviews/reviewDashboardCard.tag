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

<div class="review-dashboard-card">
    <div class="review-dashboard-card__header">
        <div class="review-dashboard-card__client-info">
            <div class="review-dashboard-card__avatar" aria-hidden="true">
                <c:choose>
                    <c:when test="${fn:length(clientName) > 0}">
                        <c:out value="${fn:substring(clientName, 0, 1)}"/>
                    </c:when>
                    <c:otherwise>?</c:otherwise>
                </c:choose>
            </div>
            <div class="review-dashboard-card__name-date">
                <span class="review-dashboard-card__name"><c:out value="${clientName}"/></span>
                <c:if test="${not empty formattedDate}">
                    <span class="review-dashboard-card__date">
                        <c:choose>
                            <c:when test="${edited}">
                                <spring:message code="commerce.reviews.editedPrefix"/>
                            </c:when>
                            <c:otherwise>
                                <spring:message code="commerce.reviews.datePrefix"/>
                            </c:otherwise>
                        </c:choose>
                        <c:out value="${formattedDate}"/>
                    </span>
                </c:if>
            </div>
        </div>
        <span class="review-dashboard-card__stars" aria-label="<spring:message code='pack.detail.reviews.ratingAria' arguments='${rating}'/>">
            <c:forEach begin="1" end="${rating}">
                <span class="material-symbols-outlined review-dashboard-card__star--filled" aria-hidden="true">star</span>
            </c:forEach>
            <c:if test="${rating < 5}">
                <c:forEach begin="1" end="${5 - rating}">
                    <span class="material-symbols-outlined review-dashboard-card__star--empty" aria-hidden="true">star</span>
                </c:forEach>
            </c:if>
        </span>
    </div>
    <c:choose>
        <c:when test="${fn:length(body) > 255}">
            <p class="review-dashboard-card__body review-dashboard-card__body--truncated break-words overflow-hidden" id="review-dashboard-short-${reviewId}">
                <c:out value="${fn:substring(body, 0, 255)}"/>...
                <button type="button" class="review-dashboard-card__read-toggle font-bold ml-1 hover:underline cursor-pointer" data-dashboard-review-id="${reviewId}" data-mode="more"><spring:message code="pack.detail.readMore"/></button>
            </p>
            <p class="review-dashboard-card__body review-dashboard-card__body--full break-words overflow-hidden hidden" id="review-dashboard-full-${reviewId}">
                <c:out value="${body}"/>
                <button type="button" class="review-dashboard-card__read-toggle font-bold ml-1 hover:underline cursor-pointer" data-dashboard-review-id="${reviewId}" data-mode="less"><spring:message code="pack.detail.readLess"/></button>
            </p>
        </c:when>
        <c:otherwise>
            <p class="review-dashboard-card__body"><c:out value="${body}"/></p>
        </c:otherwise>
    </c:choose>
</div>

<script>
(function () {
    if (window.__reviewDashboardReadMoreInit) {
        return;
    }
    window.__reviewDashboardReadMoreInit = true;
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.review-dashboard-card__read-toggle');
        if (!btn) {
            return;
        }
        var id = btn.getAttribute('data-dashboard-review-id');
        var mode = btn.getAttribute('data-mode');
        var shortEl = document.getElementById('review-dashboard-short-' + id);
        var fullEl = document.getElementById('review-dashboard-full-' + id);
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
