<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head title="${pageTitle}" />
<body class="bg-background font-body text-on-surface flex flex-col min-h-screen antialiased">
    <paw:navbar />

    <spring:message code="pack.detail.image.alt" var="packDetailImageAlt"/>
    <spring:message code="pack.detail.commerce.section" var="packDetailCommerceSectionAria"/>
    <c:url var="packCatalogUrl" value="/packs"/>
    <c:url var="packFavoriteAction" value="/packs/${packId}/favorite"/>
    <spring:message code="pack.detail.favorite.toggleAria" var="packDetailFavoriteToggleAria"/>
    <main class="pack-detail-main">
        <div class="flex items-center gap-2 mb-8 text-secondary">
            <a href="${packCatalogUrl}" class="group flex items-center font-bold">
                <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
                <span class="group-hover:underline"><spring:message code="pack.detail.back"/></span>
            </a>
        </div>
        <div class="pack-detail-grid">
            <div class="pack-detail-media-col">
                <div class="pack-detail-hero relative">
                    <c:choose>
                        <c:when test="${not empty packImageId}">
                            <img class="pack-detail-hero-img"
                                 src="${pageContext.request.contextPath}/images/${packImageId}"
                                 alt="<c:out value='${packDetailImageAlt}'/>"/>
                        </c:when>
                        <c:otherwise>
                            <img class="pack-detail-hero-img"
                                 src="${pageContext.request.contextPath}/images/pack-placeholder.svg"
                                 alt="<c:out value='${packDetailImageAlt}'/>"/>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${manageable}">
                      <div class="absolute top-3 right-3 flex gap-2 z-10">
                        <button type="button"
                                onclick="window.location.href='${pageContext.request.contextPath}/commerce/edit-pack/${packId}'"
                                class="bg-white/90 backdrop-blur text-secondary hover:text-primary p-2 flex items-center justify-center rounded-full shadow-sm hover:scale-110 transition-transform">
                            <span class="material-symbols-outlined text-[1.25rem]">edit</span>
                        </button>
                        <button type="button"
                                onclick="openDeleteModal('${packId}')"
                                class="bg-white/90 backdrop-blur text-error hover:text-on-error hover:bg-error p-2 flex items-center justify-center rounded-full shadow-sm hover:scale-110 transition-transform">
                            <span class="material-symbols-outlined text-[1.25rem]">delete</span>
                        </button>
                      </div>
                    </c:if>
                </div>

                <div class="pack-detail-intro">
                    <div class="pack-detail-meta">
                        <c:if test="${not empty packStockBadgeText}">
                            <span class="pack-detail-badge pack-detail-badge--stock ${packStockBadgeCssClass}"><c:out value="${packStockBadgeText}"/></span>
                        </c:if>
                    </div>
                    <div class="pack-detail-title-row">
                        <h1 class="pack-detail-title font-headline"><c:out value="${packTitle}"/></h1>
                        <sec:authorize access="hasRole('CLIENT')">
                            <form action="${packFavoriteAction}" method="post" class="pack-detail-favorite-form">
                                <button type="submit"
                                        class="pack-detail-favorite-btn<c:if test='${packFavoriteSelected}'> pack-detail-favorite-btn--selected</c:if>"
                                        aria-label="<c:out value='${packDetailFavoriteToggleAria}'/>"
                                        aria-pressed="${packFavoriteSelected}">
                                    <span class="material-symbols-outlined pack-detail-favorite-btn__icon" aria-hidden="true">favorite</span>
                                </button>
                            </form>
                        </sec:authorize>
                    </div>
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
                    <section class="pack-detail-commerce" aria-label="<c:out value='${packDetailCommerceSectionAria}'/>">
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
                    <section class="commerce-reviews-card" id="commerce-reviews" aria-label="<spring:message code='pack.detail.reviews.section'/>">
                        <div class="commerce-reviews-card__header">
                            <div>
                                <p class="commerce-reviews-card__eyebrow"><spring:message code="pack.detail.reviews.eyebrow"/></p>
                                <h2 class="commerce-reviews-card__title font-headline"><spring:message code="pack.detail.reviews.title"/></h2>
                            </div>
                            <span class="commerce-reviews-card__count">
                                <spring:message code="pack.detail.reviews.count" arguments="${commerceReviewCount}"/>
                            </span>
                        </div>

                        <c:if test="${commerceReviewAlertKind eq 'success'}">
                            <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${commerceReviewAlertMessage}"/></p>
                        </c:if>
                        <c:if test="${commerceReviewAlertKind eq 'error'}">
                            <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${commerceReviewAlertMessage}"/></p>
                        </c:if>

                        <div class="commerce-reviews-card__list">
                            <c:choose>
                                <c:when test="${empty commerceReviewItems}">
                                    <p class="commerce-reviews-card__empty"><spring:message code="pack.detail.reviews.empty"/></p>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="reviewItem" items="${commerceReviewItems}">
                                        <article class="commerce-review">
                                            <div class="commerce-review__meta">
                                                <span class="commerce-review__client"><c:out value="${reviewItem.clientName}"/></span>
                                                <span class="commerce-review__stars" aria-label="<spring:message code='pack.detail.reviews.ratingAria' arguments='${reviewItem.review.rating}'/>">
                                                    <c:forEach begin="1" end="${reviewItem.review.rating}">
                                                        <span class="material-symbols-outlined" aria-hidden="true">star</span>
                                                    </c:forEach>
                                                </span>
                                            </div>
                                            <p class="commerce-review__body"><c:out value="${reviewItem.review.body}"/></p>
                                        </article>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <sec:authorize access="hasRole('CLIENT')">
                            <c:choose>
                                <c:when test="${commerceReviewCanSubmit}">
                                    <div class="commerce-review-form-card">
                                        <h3 class="commerce-review-form-card__title font-headline">
                                            <c:choose>
                                                <c:when test="${commerceReviewAlreadySubmitted}">
                                                    <spring:message code="pack.detail.reviews.form.editTitle"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <spring:message code="pack.detail.reviews.form.title"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </h3>
                                        <c:url var="commerceReviewAction" value="/packs/${packId}/commerce-review"/>
                                        <form:form modelAttribute="commerceReviewForm" method="post" action="${commerceReviewAction}" cssClass="commerce-review-form" novalidate="novalidate">
                                            <div class="commerce-review-form__field">
                                                <span class="pack-form-label"><spring:message code="pack.detail.reviews.form.rating"/></span>
                                                <div class="commerce-review-form__rating" role="radiogroup" aria-label="<spring:message code='pack.detail.reviews.form.rating'/>">
                                                    <c:forEach begin="1" end="5" var="ratingOption">
                                                        <label class="commerce-review-form__rating-option">
                                                            <form:radiobutton path="rating" value="${ratingOption}"/>
                                                            <span><c:out value="${ratingOption}"/></span>
                                                        </label>
                                                    </c:forEach>
                                                </div>
                                                <form:errors path="rating" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p"/>
                                            </div>
                                            <div class="commerce-review-form__field">
                                                <label class="pack-form-label" for="commerce-review-body"><spring:message code="pack.detail.reviews.form.body"/></label>
                                                <form:textarea path="body" id="commerce-review-body" cssClass="commerce-review-form__textarea" rows="4"
                                                               maxlength="1000"/>
                                                <form:errors path="body" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p"/>
                                            </div>
                                            <button type="submit" class="pack-submit-btn font-headline">
                                                <span class="pack-submit-btn__label"><spring:message code="pack.detail.reviews.form.submit"/></span>
                                                <span class="material-symbols-outlined pack-submit-btn__icon">rate_review</span>
                                            </button>
                                        </form:form>
                                    </div>
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
                    </section>
                </div>
            </div>

            <aside class="pack-detail-aside flex flex-col gap-4">
                <c:if test="${clientHasActiveReservation}">
                    <div class="pack-feedback pack-feedback--warning flex items-center justify-between shadow-sm" role="alert">
                        <span><spring:message code="pack.detail.reserve.alreadyReserved"/></span>
                        <a href="${pageContext.request.contextPath}/reservations" 
                           class="flex items-center justify-center h-8 w-8 text-amber-600 bg-amber-600/10 hover:bg-amber-600 hover:text-white rounded-full transition-all hover:scale-110 shadow-sm shrink-0" 
                           title="<spring:message code='pack.detail.reserve.goToMyReservations'/>"
                           aria-label="<spring:message code='pack.detail.reserve.goToMyReservations'/>">
                            <span class="material-symbols-outlined text-[1.25rem]">arrow_forward</span>
                        </a>
                    </div>
                </c:if>

                <c:if test="${isOwner && auctionActive}">
                    <form id="cancelAuctionForm${auctionId}" 
                        action="${pageContext.request.contextPath}/commerce/auctions/${auctionId}/cancel" 
                        method="post" 
                        style="display: none;">
                    </form>
                    <spring:message code="commerce.auction.cancel.confirm" var="confirmMessage" />
                    <button type="button"
                            onclick="if (confirm('${confirmMessage}')) { document.getElementById('cancelAuctionForm${auctionId}').submit(); }"
                            class="w-full bg-error text-on-error px-6 py-3 rounded-full text-base font-bold flex items-center justify-center gap-2 hover:scale-105 transition-transform shadow-md">
                        <span class="material-symbols-outlined font-bold" style="font-size: 20px;">cancel</span>
                        <spring:message code="commerce.auction.cancel.button" />
                    </button>
                </c:if>

                <div class="pack-aside-card">
                    <c:if test="${auctionAlertKind eq 'success'}">
                        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${auctionAlertMessage}"/></p>
                    </c:if>
                    <c:if test="${auctionAlertKind eq 'error'}">
                        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${auctionAlertMessage}"/></p>
                    </c:if>

                    <c:if test="${reservationAlertKind eq 'success'}">
                        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>
                    <c:if test="${reservationAlertKind eq 'error'}">
                        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>

                    <c:choose>
                        <c:when test="${auctionActive}">
                            <div class="pack-aside-heading-row">
                                <h2 class="pack-aside-heading font-headline flex items-center gap-2 flex-1 min-w-0">
                                    <span class="material-symbols-outlined text-auction" aria-hidden="true">gavel</span>
                                    <spring:message code="pack.detail.auction.title"/>
                                </h2>
                            </div>

                            <div class="pack-price-block mb-4">
                                <p class="pack-price-block-label"><spring:message code="pack.detail.auction.currentPriceLabel"/></p>
                                <p class="pack-price-original"><c:out value="${originalPrice}"/></p>
                                <p class="pack-price-final font-headline text-auction"><c:out value="${auctionEffectivePriceDisplay}"/></p>
                                <p class="text-xs text-secondary mt-2"><c:out value="${auctionMinBidHint}"/></p>
                                <p class="text-sm text-secondary mt-3"><spring:message code="pack.detail.auction.endsAt"/>
                                    <span class="font-semibold text-on-surface"><c:out value="${auctionEndDisplay}"/></span>
                                </p>
                            </div>

                            <sec:authorize access="hasRole('CLIENT')">
                                <c:choose>
                                    <c:when test="${auctionClientIsLeading}">
                                        <p class="text-secondary text-sm mb-2" role="status"><spring:message code="pack.detail.bid.leadingInfo"/></p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url var="bidAction" value="/packs/${packId}/bid"/>
                                        <form:form modelAttribute="bidForm" cssClass="auction-bid-form" method="post"
                                                   action="${bidAction}" novalidate="novalidate" id="auction-bid-form">
                                            <spring:message code="pack.detail.bid.amount.label" var="labelBidAmount"/>
                                            <spring:message code="pack.detail.bid.amount.placeholder" var="phBidAmount"/>
                                            <spring:bind path="amount">
                                                <paw:input id="bid-amount" label="${labelBidAmount}" type="number"
                                                           name="${status.expression}" value="${status.value}"
                                                           error="${status.errorMessages[0]}"
                                                           placeholder="${phBidAmount}"
                                                           min="${bidAmountMin}" step="0.01"
                                                           wrapperClass="pack-form-field" labelClass="pack-form-label"
                                                           inputClass="pack-form-control pack-form-control--tabular"
                                                           errorClass="pack-feedback pack-feedback--error pack-form-errors"
                                                           errorTag="p"/>
                                            </spring:bind>
                                            <spring:message code="pack.detail.bid.form.submitting" var="bidSubmittingText"/>
                                            <button type="submit" class="auction-submit-btn font-headline mt-2 w-full"
                                                    id="auction-bid-submit-btn"
                                                    data-submitting-text="<c:out value='${bidSubmittingText}'/>">
                                                <span class="auction-submit-btn__label"><spring:message code="pack.detail.bid.form.submit"/></span>
                                                <span class="material-symbols-outlined auction-submit-btn__icon">gavel</span>
                                            </button>
                                        </form:form>
                                    </c:otherwise>
                                </c:choose>
                            </sec:authorize>
                            <sec:authorize access="isAnonymous()">
                                <p class="text-secondary text-sm mb-2"><spring:message code="pack.detail.bid.loginPrompt"/></p>
                                <c:url var="loginUrl" value="/login"/>
                                <a href="${loginUrl}" class="inline-flex items-center justify-center gap-2 font-bold text-primary hover:underline">
                                    <spring:message code="pack.detail.bid.loginLink"/>
                                </a>
                            </sec:authorize>
                            <sec:authorize access="hasRole('COMMERCE')">
                                <p class="pack-feedback pack-feedback--error" role="status"><spring:message code="pack.detail.bid.commerceCannotBid"/></p>
                            </sec:authorize>
                        </c:when>
                        <c:when test="${auctionPresent}">
                            <div class="pack-aside-heading-row">
                                <h2 class="pack-aside-heading font-headline flex items-center gap-2 flex-1 min-w-0">
                                    <span class="material-symbols-outlined text-secondary" aria-hidden="true">gavel</span>
                                    <spring:message code="pack.detail.auction.ended.title"/>
                                </h2>
                                <sec:authorize access="hasRole('CLIENT')">
                                    <form action="${packFavoriteAction}" method="post" class="pack-aside-favorite-form">
                                        <button type="submit"
                                                class="pack-aside-favorite-btn<c:if test='${packFavoriteSelected}'> pack-aside-favorite-btn--selected</c:if>"
                                                aria-label="<c:out value='${packDetailFavoriteToggleAria}'/>"
                                                aria-pressed="${packFavoriteSelected}">
                                            <span class="material-symbols-outlined pack-aside-favorite-icon" aria-hidden="true">favorite</span>
                                        </button>
                                    </form>
                                </sec:authorize>
                            </div>
                            <p class="text-secondary text-sm mb-2"><spring:message code="pack.detail.auction.ended.body"/></p>
                            <p class="text-sm text-on-surface"><spring:message code="pack.detail.auction.endsAt"/>
                                <span class="font-semibold"><c:out value="${auctionEndDisplay}"/></span>
                            </p>
                        </c:when>
                        <c:otherwise>
                            <div class="pack-aside-heading-row">
                                <h2 class="pack-aside-heading font-headline flex-1 min-w-0"><spring:message code="pack.detail.reserve.title"/></h2>
                                <sec:authorize access="hasRole('CLIENT')">
                                    <form action="${packFavoriteAction}" method="post" class="pack-aside-favorite-form">
                                        <button type="submit"
                                                class="pack-aside-favorite-btn<c:if test='${packFavoriteSelected}'> pack-aside-favorite-btn--selected</c:if>"
                                                aria-label="<c:out value='${packDetailFavoriteToggleAria}'/>"
                                                aria-pressed="${packFavoriteSelected}">
                                            <span class="material-symbols-outlined pack-aside-favorite-icon" aria-hidden="true">favorite</span>
                                        </button>
                                    </form>
                                </sec:authorize>
                            </div>

                    <div class="pack-price-block mb-4">
                        <p class="pack-price-block-label"><spring:message code="pack.detail.price.perPack"/></p>
                        <p class="pack-price-original"><c:out value="${originalPrice}"/></p>
                        <p class="pack-price-final font-headline"><c:out value="${finalPrice}"/></p>
                    </div>

                    <sec:authorize access="hasRole('CLIENT')">
                        <c:url var="reservationAction" value="/packs/${packId}/reserve"/>
                        <form:form modelAttribute="reservationForm" cssClass="pack-reservation-form" method="post"
                                   action="${reservationAction}" novalidate="novalidate">

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
                                 data-unit-price="<c:out value='${unitPriceNumber}'/>">
                                <p class="pack-form-total-label"><spring:message code="pack.detail.form.total"/></p>
                                <p class="pack-form-total-amount font-headline" id="reservation-total-display" aria-live="polite">—</p>
                            </div>

                            <spring:message code="pack.detail.form.submitting" var="submittingText"/>
                            <button type="submit" class="pack-submit-btn font-headline"
                                    id="reservation-submit-btn"
                                    data-submitting-text="<c:out value='${submittingText}'/>"
                                    <c:if test="${quantityMax lt 1}">disabled="disabled" aria-disabled="true"</c:if>>
                                <span class="pack-submit-btn__label"><spring:message code="pack.detail.form.submit"/></span>
                                <span class="material-symbols-outlined pack-submit-btn__icon">arrow_forward</span>
                            </button>
                        </form:form>
                    </sec:authorize>
                    <sec:authorize access="isAnonymous()">
                        <p class="text-secondary text-sm mb-2"><spring:message code="pack.detail.reserve.loginPrompt"/></p>
                        <c:url var="loginUrlReserve" value="/login"/>
                        <a href="${loginUrlReserve}" class="inline-flex items-center justify-center gap-2 font-bold text-primary hover:underline">
                            <spring:message code="pack.detail.reserve.loginLink"/>
                        </a>
                    </sec:authorize>
                    <sec:authorize access="hasRole('COMMERCE')">
                        <p class="pack-feedback pack-feedback--error" role="status"><spring:message code="pack.detail.reserve.commerceCannotReserve"/></p>
                    </sec:authorize>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${auctionPresent}">
                    <paw:bidHistoryCard packId="${packId}" items="${auctionBidHistoryItems}"/>
                </c:if>
                <c:if test="${isOwner}">
                    <paw:reservationHistoryCard packId="${packId}" items="${packReservationHistoryItems}"/>
                </c:if>
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
            var form = document.getElementById('auction-bid-form');
            var btn = document.getElementById('auction-bid-submit-btn');
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
                btn.classList.add('auction-submit-btn--submitting');
                var label = btn.querySelector('.auction-submit-btn__label');
                if (label && btn.getAttribute('data-submitting-text')) {
                    label.textContent = btn.getAttribute('data-submitting-text');
                }
            });
        })();

        (function () {
            document.querySelectorAll('[data-bid-history-target]').forEach(function (btn) {
                var targetId = btn.getAttribute('data-bid-history-target');
                if (!targetId) {
                    return;
                }
                var target = document.getElementById(targetId);
                if (!target) {
                    return;
                }
                btn.addEventListener('click', function () {
                    var hidden = target.classList.contains('hidden');
                    if (hidden) {
                        target.classList.remove('hidden');
                    } else {
                        target.classList.add('hidden');
                    }
                    var expanded = !target.classList.contains('hidden');
                    btn.setAttribute('aria-expanded', String(expanded));
                });
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
    <paw:deletePackModal />
</body>
</html>
