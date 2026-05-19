<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head title="${pageTitle}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components/commerce-profile.css"/>
</paw:head>
<body class="bg-background font-body text-on-surface flex flex-col min-h-screen antialiased">
    <paw:navbar />

    <spring:message code="pack.catalog.lastChance.badge" var="auctionBadgeText"/>
    <spring:message code="pack.catalog.auction.currentBid" var="auctionCurrentBidLabel"/>
    <spring:message code="commerce.profile.auctions.empty.title" var="emptyAuctionTitle"/>
    <spring:message code="commerce.profile.auctions.empty.description" var="emptyAuctionDesc"/>
    <spring:message code="commerce.profile.packs.empty.title" var="emptyPacksTitle"/>
    <spring:message code="commerce.profile.packs.empty.description" var="emptyPacksDesc"/>
    <c:url var="packCatalogUrl" value="/packs"/>

    <main class="commerce-profile-main">
        <a href="${packCatalogUrl}"
           onclick="if (window.history.length > 1 && document.referrer.indexOf(window.location.host) !== -1) { window.history.back(); return false; }"
           class="commerce-profile-back">
            <span class="material-symbols-outlined text-xl" aria-hidden="true">arrow_back</span>
            <spring:message code="commerce.profile.back"/>
        </a>

        <header class="commerce-profile-hero" aria-label="<spring:message code='commerce.profile.hero.aria'/>">
            <div class="commerce-profile-hero__logo">
                <c:choose>
                    <c:when test="${not empty profileImageId}">
                        <img src="${pageContext.request.contextPath}/images/${profileImageId}"
                             alt="<c:out value='${commerceCommercialName}'/>"/>
                    </c:when>
                    <c:otherwise>
                        <span class="material-symbols-outlined text-5xl text-outline-variant" aria-hidden="true">storefront</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <h1 class="commerce-profile-hero__name font-headline"><c:out value="${commerceCommercialName}"/></h1>
            <p class="commerce-profile-hero__category">
                <spring:message code="commerce.category.${commerceCategory}" text="${commerceCategory}"/>
            </p>
            <paw:commerceReviewSummary averageRating="${commerceReviewAverageRating}" reviewCount="${commerceReviewCount}"/>
        </header>

        <section class="commerce-profile-section commerce-profile-info-wrap" aria-label="<spring:message code='commerce.profile.info.aria'/>">
            <div class="commerce-info-card">
                <div class="commerce-info-card__grid">
                    <div class="commerce-info-card__column">
                        <h2 class="commerce-info-card__section-title font-headline">
                            <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">location_on</span>
                            <spring:message code="commerce.profile.location"/>
                        </h2>
                        <div class="commerce-info-card__address">
                            <p><c:out value="${commerceStreetLine}"/></p>
                            <p><c:out value="${commerceLocationLine}"/></p>
                        </div>
                    </div>
                    <div class="commerce-info-card__column">
                        <h2 class="commerce-info-card__section-title font-headline">
                            <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
                            <spring:message code="commerce.profile.hours"/>
                        </h2>
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
            </div>
        </section>

        <section class="commerce-profile-section" aria-label="<spring:message code='commerce.profile.reviews.aria'/>">
            <h2 class="commerce-profile-section__title font-headline"><spring:message code="commerce.profile.reviews.title"/></h2>
            <paw:reviewList items="${commerceReviewItems}"
                            reviewCount="${commerceReviewCount}"
                            averageRating="${commerceReviewAverageRating}"/>
        </section>

        <section class="commerce-profile-section" aria-label="<spring:message code='commerce.profile.offers.aria'/>">
            <h2 class="commerce-profile-section__title font-headline"><spring:message code="commerce.profile.offers.title"/></h2>

            <h3 class="text-lg font-bold text-on-surface mb-4"><spring:message code="commerce.profile.auctions.title"/></h3>
            <c:choose>
                <c:when test="${activeAuctionsTotal gt 0}">
                    <div class="commerce-profile-offers-grid mb-10">
                        <c:forEach var="auction" items="${activeAuctions}">
                            <paw:packCard
                                packId="${auction.pack.id}"
                                imageId="${auction.pack.imageId}"
                                title="${auction.pack.title}"
                                subtitle="${auction.pack.description}"
                                badgeText="${auctionBadgeText}"
                                rescueLabel="${auctionCurrentBidLabel}"
                                price="$${auction.effectivePrice}"
                                oldPrice="$${auction.pack.originalPrice}"
                                commerceName="${commerceNames[auction.pack.id]}"
                                auction="${true}"
                            />
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="mb-10">
                        <paw:packEmptyState icon="gavel" title="${emptyAuctionTitle}" description="${emptyAuctionDesc}"/>
                    </div>
                </c:otherwise>
            </c:choose>

            <h3 class="text-lg font-bold text-on-surface mb-4"><spring:message code="commerce.profile.packs.title"/></h3>
            <c:choose>
                <c:when test="${directPacksTotal gt 0}">
                    <div class="commerce-profile-offers-grid">
                        <c:forEach var="pack" items="${directPacks}">
                            <c:set var="packUnavailable" value="${pack.stock == null || pack.stock lt 1}"/>
                            <paw:packCard
                                packId="${pack.id}"
                                imageId="${pack.imageId}"
                                title="${pack.title}"
                                subtitle="${pack.description}"
                                price="$${pack.finalPrice}"
                                oldPrice="$${pack.originalPrice}"
                                commerceName="${commerceNames[pack.id]}"
                                unavailable="${packUnavailable}"
                            />
                        </c:forEach>
                    </div>
                    <c:if test="${totalPages gt 1}">
                        <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                        baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <paw:packEmptyState icon="inventory_2" title="${emptyPacksTitle}" description="${emptyPacksDesc}"/>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <paw:footer />
</body>
</html>
