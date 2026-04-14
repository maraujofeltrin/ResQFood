<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head title="${pageTitle}" />
<body class="bg-background font-body text-on-surface flex flex-col min-h-screen antialiased">
    <paw:navbar />

    <spring:message code="pack.detail.image.alt" var="packDetailImageAlt"/>
    <spring:message code="pack.detail.commerce.section" var="packDetailCommerceSectionAria"/>
    <spring:message code="auction.detail.countdown.finished" var="auctionFinishedText"/>
    <spring:message code="auction.detail.form.submitting" var="submittingText"/>

    <c:url var="auctionCatalogUrl" value="/auctions"/>
    <c:url var="auctionBidAction" value="/auctions/${auctionId}/bid"/>

    <main class="pack-detail-main">
        <div class="flex items-center gap-2 mb-8 text-secondary">
            <a href="${auctionCatalogUrl}" class="group flex items-center font-bold">
                <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
                <span class="group-hover:underline"><spring:message code="pack.detail.back"/></span>
            </a>
        </div>

        <div class="pack-detail-grid">
            <div class="pack-detail-media-col">
                <div class="pack-detail-hero">
                    <img class="pack-detail-hero-img"
                         src="${pageContext.request.contextPath}/auctions/${auctionId}/image"
                         alt="${packDetailImageAlt}"/>
                </div>

                <div class="pack-detail-intro">
                    <h1 class="pack-detail-title font-headline"><c:out value="${packTitle}"/></h1>

                    <c:choose>
                        <c:when test="${fn:length(packDescription) > 255}">
                            <p class="pack-detail-description break-words overflow-hidden" id="pack-description-short">
                                <c:out value="${fn:substring(packDescription, 0, 255)}"/>...
                                <button type="button" id="read-more-btn" class="font-bold ml-1 hover:underline cursor-pointer"><spring:message code="pack.detail.readMore"/></button>
                            </p>
                            <p class="pack-detail-description break-words overflow-hidden hidden" id="pack-description-full">
                                <c:out value="${packDescription}"/>
                                <button type="button" id="read-less-btn" class="font-bold ml-1 hover:underline cursor-pointer"><spring:message code="pack.detail.readLess"/></button>
                            </p>
                        </c:when>
                        <c:otherwise>
                            <p class="pack-detail-description break-words overflow-hidden"><c:out value="${packDescription}"/></p>
                        </c:otherwise>
                    </c:choose>

                    <section class="pack-detail-commerce" aria-label="${packDetailCommerceSectionAria}">
                        <div class="commerce-info-card">
                            <div class="commerce-info-card__header">
                                <span class="material-symbols-outlined commerce-info-card__icon commerce-info-card__icon--hero" aria-hidden="true">storefront</span>
                                <h2 class="commerce-info-card__title font-headline"><spring:message code="pack.detail.commerce.heading"/></h2>
                            </div>
                            <div class="commerce-info-card__grid">
                                <div class="commerce-info-card__column">
                                    <h3 class="commerce-info-card__section-title font-headline">
                                        <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">location_on</span>
                                        <spring:message code="pack.detail.commerce.location"/>
                                    </h3>
                                    <div class="commerce-info-card__address">
                                        <p class="commerce-info-card__store-name"><c:out value="${commerceCommercialName}"/></p>
                                        <p><c:out value="${commerceStreetLine}"/></p>
                                        <p><c:out value="${commerceLocationLine}"/></p>
                                    </div>
                                </div>
                                <div class="commerce-info-card__column">
                                    <h3 class="commerce-info-card__section-title font-headline">
                                        <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
                                        <spring:message code="pack.detail.commerce.hours"/>
                                    </h3>
                                    <div class="commerce-info-card__hours">
                                        <div class="commerce-info-card__hours-row">
                                            <span class="commerce-info-card__hours-label"><spring:message code="pack.detail.commerce.opening"/></span>
                                            <span class="commerce-info-card__hours-value"><c:out value="${commerceOpeningTime}"/></span>
                                        </div>
                                        <div class="commerce-info-card__hours-row">
                                            <span class="commerce-info-card__hours-label"><spring:message code="pack.detail.commerce.closing"/></span>
                                            <span class="commerce-info-card__hours-value"><c:out value="${commerceClosingTime}"/></span>
                                        </div>
                                        <div class="commerce-info-card__status">
                                            <c:choose>
                                                <c:when test="${commerceOpenNow}">
                                                    <span class="commerce-info-card__status-badge"><spring:message code="pack.detail.commerce.openNow"/></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="commerce-info-card__status-badge commerce-info-card__status-badge--closed"><spring:message code="pack.detail.commerce.closedNow"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="commerce-info-card__note">
                                <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">info</span>
                                <p class="commerce-info-card__note-text">
                                    <spring:message code="auction.detail.note"/>
                                </p>
                            </div>
                        </div>
                    </section>
                </div>
            </div>

            <aside class="pack-detail-aside">
                <div class="pack-aside-card">
                    <c:if test="${auctionAlertKind eq 'success'}">
                        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${auctionAlertMessage}"/></p>
                    </c:if>
                    <c:if test="${auctionAlertKind eq 'error'}">
                        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${auctionAlertMessage}"/></p>
                    </c:if>

                    <h2 class="pack-aside-heading font-headline"><spring:message code="auction.detail.bid.title"/></h2>

                    <form class="pack-reservation-form" method="post" action="${auctionBidAction}" novalidate="novalidate">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                        <div class="pack-price-block">
                            <p class="pack-price-block-label"><spring:message code="auction.detail.currentBid.label"/></p>
                            <p class="pack-price-original"><c:out value="${referencePrice}"/></p>
                            <p class="pack-price-final font-headline"><c:out value="${highestBid}"/></p>
                        </div>

                        <div class="pack-price-block pack-price-block--countdown" data-end-at="${auctionEndsAtMillis}">
                            <p class="pack-price-block-label"><spring:message code="auction.detail.countdown.label"/></p>
                            <p class="auction-countdown-value font-headline" id="auction-countdown" aria-live="polite">--:--:--</p>
                            <p class="pack-form-total-hint"><spring:message code="auction.detail.countdown.hint"/></p>
                        </div>

                        <div class="pack-form-field">
                            <label class="pack-form-label" for="auction-bid-amount"><spring:message code="auction.detail.form.bidAmount"/></label>
                            <input
                                id="auction-bid-amount"
                                class="pack-form-control pack-form-control--tabular"
                                type="number"
                                name="bidAmount"
                                step="0.01"
                                min="0.01"
                                inputmode="decimal"
                                required="required"
                                placeholder="<spring:message code='auction.detail.form.bidAmount.placeholder'/>"
                                value="${param.bidAmount}"
                            />
                            <p class="pack-form-total-hint"><spring:message code="auction.detail.form.bidAmount.hint" arguments="${highestBid}"/></p>
                        </div>

                        <button type="submit" class="pack-submit-btn font-headline"
                                id="auction-submit-btn"
                                data-submitting-text="${submittingText}">
                            <span class="pack-submit-btn__label"><spring:message code="auction.detail.form.submit"/></span>
                            <span class="material-symbols-outlined pack-submit-btn__icon">gavel</span>
                        </button>
                    </form>
                </div>
            </aside>
        </div>
    </main>

    <paw:footer />

    <script>
        (function () {
            var readMoreBtn = document.getElementById('read-more-btn');
            var readLessBtn = document.getElementById('read-less-btn');
            var descShort = document.getElementById('pack-description-short');
            var descFull = document.getElementById('pack-description-full');

            if (readMoreBtn && readLessBtn && descShort && descFull) {
                readMoreBtn.addEventListener('click', function (e) {
                    e.preventDefault();
                    descShort.classList.add('hidden');
                    descFull.classList.remove('hidden');
                });
                readLessBtn.addEventListener('click', function (e) {
                    e.preventDefault();
                    descFull.classList.add('hidden');
                    descShort.classList.remove('hidden');
                });
            }
        })();

        (function () {
            var form = document.querySelector('.pack-reservation-form');
            var btn = document.getElementById('auction-submit-btn');
            if (!form || !btn) {
                return;
            }
            var submitted = false;
            form.addEventListener('submit', function (e) {
                if (submitted) {
                    e.preventDefault();
                    return;
                }
                submitted = true;
                btn.disabled = true;
                btn.setAttribute('aria-disabled', 'true');
                btn.classList.add('pack-submit-btn--submitting');
                var label = btn.querySelector('.pack-submit-btn__label');
                if (label && btn.getAttribute('data-submitting-text')) {
                    label.textContent = btn.getAttribute('data-submitting-text');
                }
            });
        })();

        (function () {
            var countdownEl = document.getElementById('auction-countdown');
            var block = document.querySelector('.pack-price-block--countdown');
            var input = document.getElementById('auction-bid-amount');
            var submitBtn = document.getElementById('auction-submit-btn');
            if (!countdownEl || !block) {
                return;
            }

            var endAt = parseInt(block.getAttribute('data-end-at'), 10);
            if (isNaN(endAt)) {
                return;
            }

            function pad(n) {
                return n < 10 ? '0' + n : String(n);
            }

            function render() {
                var now = Date.now();
                var diff = endAt - now;
                if (diff <= 0) {
                    countdownEl.textContent = '${auctionFinishedText}';
                    if (input) {
                        input.disabled = true;
                        input.setAttribute('aria-disabled', 'true');
                    }
                    if (submitBtn) {
                        submitBtn.disabled = true;
                        submitBtn.setAttribute('aria-disabled', 'true');
                    }
                    return false;
                }

                var totalSeconds = Math.floor(diff / 1000);
                var hours = Math.floor(totalSeconds / 3600);
                var minutes = Math.floor((totalSeconds % 3600) / 60);
                var seconds = totalSeconds % 60;
                countdownEl.textContent = pad(hours) + ':' + pad(minutes) + ':' + pad(seconds);
                return true;
            }

            if (render()) {
                var timer = setInterval(function () {
                    if (!render()) {
                        clearInterval(timer);
                    }
                }, 1000);
            }
        })();

        (function () {
            var bidInput = document.getElementById('auction-bid-amount');
            if (!bidInput) {
                return;
            }
            var currentBid = parseFloat('${highestBidNumber}');
            if (isNaN(currentBid)) {
                return;
            }
            var minBid = currentBid + 1;
            bidInput.min = minBid.toFixed(2);
            if (!bidInput.value) {
                bidInput.value = minBid.toFixed(2);
            }
        })();
    </script>
</body>
</html>
