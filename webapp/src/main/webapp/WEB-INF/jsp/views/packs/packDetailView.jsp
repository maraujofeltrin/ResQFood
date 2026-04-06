<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<head>
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
    <spring:message code="pack.detail.badge" var="packDetailBadge"/>
    <spring:message code="pack.detail.commerce.section" var="packDetailCommerceSectionAria"/>
    <main class="pack-detail-main">
        <div class="pack-detail-grid">
            <div class="pack-detail-media-col">
                <div class="pack-detail-hero">
                    <img class="pack-detail-hero-img"
                         src="${pageContext.request.contextPath}/packs/${packId}/image"
                         alt="${packDetailImageAlt}"/>
                </div>

                <div class="pack-detail-intro">
                    <div class="pack-detail-meta">
                        <span class="pack-detail-badge"><c:out value="${packDetailBadge}"/></span>
                        <span class="pack-detail-merchant"><c:out value="${commerceCommercialName}"/></span>
                    </div>
                    <h1 class="pack-detail-title font-headline"><c:out value="${packTitle}"/></h1>
                    <p class="pack-detail-description"><c:out value="${packDescription}"/></p>
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
                                        <c:if test="${commerceOpenNow}">
                                            <div class="commerce-info-card__status">
                                                <span class="commerce-info-card__status-badge"><spring:message code="pack.detail.commerce.openNow"/></span>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                            <div class="commerce-info-card__note">
                                <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">info</span>
                                <p class="commerce-info-card__note-text">
                                    <spring:message code="pack.detail.commerce.pickupNote"/>
                                </p>
                            </div>
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
                        <spring:bind path="quantity">
                            <paw:input id="reservation-quantity" label="${labelQuantity}" type="number"
                                       name="${status.expression}" value="${status.value}"
                                       error="${status.errorMessages[0]}"
                                       placeholder="${phQuantity}"
                                       max="${quantityMax}" step="1"
                                       wrapperClass="pack-form-field" labelClass="pack-form-label"
                                       inputClass="pack-form-control pack-form-control--tabular"
                                       errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                       errorTag="p"/>
                        </spring:bind>

                        <div class="pack-form-field">
                            <p class="pack-form-total-label"><spring:message code="pack.detail.form.total"/></p>
                            <p class="pack-form-total-hint"><spring:message code="pack.detail.form.totalHint"/></p>
                        </div>

                        <div class="pack-form-field">
                            <spring:message code="pack.detail.form.pickupWindow" var="labelPickupWindow"/>
                            <form:label path="pickupWindow" cssClass="pack-form-label">${labelPickupWindow}</form:label>
                            <div class="pack-select-wrap">
                                <form:select path="pickupWindow" items="${pickupWindows}" cssClass="pack-form-select bg-none"/>
                                <span class="material-symbols-outlined pack-select-chevron">expand_more</span>
                            </div>
                            <form:errors path="pickupWindow" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p"/>
                        </div>

                        <button type="submit" class="pack-submit-btn font-headline">
                            <spring:message code="pack.detail.form.submit"/>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </button>
                    </form:form>
                </div>
            </aside>
        </div>
    </main>

    <paw:footer />
</body>
</html>
