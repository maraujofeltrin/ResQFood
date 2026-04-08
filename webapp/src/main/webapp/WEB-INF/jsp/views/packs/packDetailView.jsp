<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<head>
    <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <c:choose>
        <c:when test="${not empty pageTitle}">
            <title><c:out value="${pageTitle}"/></title>
        </c:when>
        <c:otherwise>
            <title><spring:message code="app.brand"/></title>
        </c:otherwise>
    </c:choose>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>
</head>
<body class="bg-background font-body text-on-surface flex flex-col min-h-screen antialiased">
    <paw:navbar />

    <spring:message code="pack.detail.image.alt" var="packDetailImageAlt"/>
    <spring:message code="pack.detail.commerce.section" var="packDetailCommerceSectionAria"/>
    <c:url var="packCatalogUrl" value="/packs"/>
    <main class="pack-detail-main">
        <div class="flex items-center gap-2 mb-8 text-secondary">
            <a href="${packCatalogUrl}" class="group flex items-center font-bold">
                <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
                <span class="group-hover:underline"><spring:message code="pack.detail.back"/></span>
            </a>
        </div>
        <div class="pack-detail-grid">
            <div class="pack-detail-media-col">
                <div class="pack-detail-hero">
                    <img class="pack-detail-hero-img"
                         src="${pageContext.request.contextPath}/packs/${packId}/image"
                         alt="${packDetailImageAlt}"/>
                </div>

                <div class="pack-detail-intro">
                    <div class="pack-detail-meta">
                        <c:if test="${not empty packStockBadgeText}">
                            <span class="pack-detail-badge pack-detail-badge--stock ${packStockBadgeCssClass}"><c:out value="${packStockBadgeText}"/></span>
                        </c:if>
                    </div>
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
                                    <spring:message code="pack.detail.commerce.pickupNote"/>
                                </p>
                            </div>
                            <c:if test="${not commerceOpenNow}">
                                <div class="commerce-info-card__note commerce-info-card__note--warning">
                                    <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
                                    <p class="commerce-info-card__note-text">
                                        <spring:message code="pack.detail.commerce.closedNote"/>
                                    </p>
                                </div>
                            </c:if>
                        </div>
                    </section>
                </div>
            </div>

            <aside class="pack-detail-aside">
                <div class="pack-aside-card">
                    <c:if test="${reservationAlertKind eq 'success'}">
                        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>
                    <c:if test="${reservationAlertKind eq 'error'}">
                        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>

                    <h2 class="pack-aside-heading font-headline"><spring:message code="pack.detail.reserve.title"/></h2>

                    <c:url var="reservationAction" value="/packs/${packId}/reserve"/>
                    <form:form modelAttribute="reservationForm" cssClass="pack-reservation-form" method="post"
                               action="${reservationAction}" novalidate="novalidate">

                        <div class="pack-price-block">
                            <p class="pack-price-block-label"><spring:message code="pack.detail.price.perPack"/></p>
                            <p class="pack-price-original"><c:out value="${originalPrice}"/></p>
                            <p class="pack-price-final font-headline"><c:out value="${finalPrice}"/></p>
                        </div>

                        <spring:message code="pack.detail.form.firstName" var="labelFirstName"/>
                        <spring:message code="pack.detail.form.firstName.placeholder" var="phFirstName"/>
                        <spring:bind path="firstName">
                            <paw:input id="reservation-firstName" label="${labelFirstName}" type="text"
                                       name="${status.expression}" value="${status.value}"
                                       error="${status.errorMessages[0]}"
                                       placeholder="${phFirstName}"
                                       wrapperClass="pack-form-field" labelClass="pack-form-label"
                                       inputClass="pack-form-control"
                                       errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                       errorTag="p"/>
                        </spring:bind>
                        <spring:message code="pack.detail.form.lastName" var="labelLastName"/>
                        <spring:message code="pack.detail.form.lastName.placeholder" var="phLastName"/>
                        <spring:bind path="lastName">
                            <paw:input id="reservation-lastName" label="${labelLastName}" type="text"
                                       name="${status.expression}" value="${status.value}"
                                       error="${status.errorMessages[0]}"
                                       placeholder="${phLastName}"
                                       wrapperClass="pack-form-field" labelClass="pack-form-label"
                                       inputClass="pack-form-control"
                                       errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                       errorTag="p"/>
                        </spring:bind>
                        <spring:message code="pack.detail.form.email" var="labelEmail"/>
                        <spring:message code="pack.detail.form.email.placeholder" var="phEmail"/>
                        <spring:bind path="email">
                            <paw:input id="reservation-email" label="${labelEmail}" type="email"
                                       name="${status.expression}" value="${status.value}"
                                       error="${status.errorMessages[0]}"
                                       placeholder="${phEmail}"
                                       autocomplete="email"
                                       wrapperClass="pack-form-field" labelClass="pack-form-label"
                                       inputClass="pack-form-control"
                                       errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                       errorTag="p"/>
                        </spring:bind>
                        <spring:message code="pack.detail.form.phone" var="labelPhone"/>
                        <spring:message code="pack.detail.form.phone.placeholder" var="phPhone"/>
                        <spring:bind path="phone">
                            <paw:input id="reservation-phone" label="${labelPhone}" type="tel"
                                       name="${status.expression}" value="${status.value}"
                                       error="${status.errorMessages[0]}"
                                       placeholder="${phPhone}"
                                       autocomplete="tel"
                                       wrapperClass="pack-form-field" labelClass="pack-form-label"
                                       inputClass="pack-form-control"
                                       errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                       errorTag="p"/>
                        </spring:bind>

                        <spring:message code="pack.detail.form.quantity" var="labelQuantity"/>
                        <spring:message code="pack.detail.form.quantity.placeholder" var="phQuantity"/>
                        <c:choose>
                            <c:when test="${quantityMax ge 1}">
                                <spring:bind path="quantity">
                                    <paw:input id="reservation-quantity" label="${labelQuantity}" type="number"
                                               name="${status.expression}" value="${status.value}"
                                               error="${status.errorMessages[0]}"
                                               placeholder="${phQuantity}"
                                               min="1" max="${quantityMax}" step="1"
                                               wrapperClass="pack-form-field" labelClass="pack-form-label"
                                               inputClass="pack-form-control pack-form-control--tabular"
                                               errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                               errorTag="p"/>
                                </spring:bind>
                            </c:when>
                            <c:otherwise>
                                <div class="pack-form-field">
                                    <label class="pack-form-label" for="reservation-quantity-hidden"><c:out value="${labelQuantity}"/></label>
                                    <p class="pack-feedback pack-feedback--error" role="alert" id="reservation-quantity-unavailable">
                                        <spring:message code="pack.detail.form.quantity.unavailable"/>
                                    </p>
                                    <form:hidden path="quantity" id="reservation-quantity-hidden"/>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="pack-form-field" id="reservation-total-block"
                             data-unit-price="${unitPriceNumber}">
                            <p class="pack-form-total-label"><spring:message code="pack.detail.form.total"/></p>
                            <p class="pack-form-total-amount font-headline" id="reservation-total-display" aria-live="polite">—</p>
                        </div>

                        <spring:message code="pack.detail.form.submitting" var="submittingText"/>
                        <button type="submit" class="pack-submit-btn font-headline"
                                id="reservation-submit-btn"
                                data-submitting-text="${submittingText}"
                                <c:if test="${quantityMax lt 1}">disabled="disabled" aria-disabled="true"</c:if>>
                            <span class="pack-submit-btn__label"><spring:message code="pack.detail.form.submit"/></span>
                            <span class="material-symbols-outlined pack-submit-btn__icon">arrow_forward</span>
                        </button>
                    </form:form>
                </div>
            </aside>
        </div>
    </main>

    <paw:footer />
    <script>
        (function () {
            var block = document.getElementById('reservation-total-block');
            var qtyInput = document.getElementById('reservation-quantity');
            var totalEl = document.getElementById('reservation-total-display');
            if (!block || !qtyInput || !totalEl) {
                return;
            }
            var unit = parseFloat(block.getAttribute('data-unit-price'));
            if (isNaN(unit)) {
                unit = 0;
            }
            var maxQ = parseInt(qtyInput.getAttribute('max'), 10);
            if (isNaN(maxQ)) {
                maxQ = 999;
            }
            var fmt = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' });
            function parseQuantity() {
                if (maxQ === 0) {
                    return 0;
                }
                var q = parseInt(qtyInput.value, 10);
                if (isNaN(q) || q < 1) {
                    q = 1;
                }
                if (q > maxQ) {
                    q = maxQ;
                }
                return q;
            }
            function updateTotal() {
                var q = parseQuantity();
                var total = unit * q;
                totalEl.textContent = fmt.format(total);
            }
            qtyInput.addEventListener('input', updateTotal);
            qtyInput.addEventListener('change', updateTotal);
            updateTotal();
        })();

        (function () {
            var form = document.querySelector('.pack-reservation-form');
            var btn = document.getElementById('reservation-submit-btn');
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
            var readMoreBtn = document.getElementById('read-more-btn');
            var readLessBtn = document.getElementById('read-less-btn');
            var descShort = document.getElementById('pack-description-short');
            var descFull = document.getElementById('pack-description-full');

            if (readMoreBtn && readLessBtn && descShort && descFull) {
                readMoreBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    descShort.classList.add('hidden');
                    descFull.classList.remove('hidden');
                });
                readLessBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    descFull.classList.add('hidden');
                    descShort.classList.remove('hidden');
                });
            }
        })();
    </script>
</body>
</html>
