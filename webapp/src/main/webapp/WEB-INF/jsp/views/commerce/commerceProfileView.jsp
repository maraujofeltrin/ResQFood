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
    <spring:message code="commerce.profile.available.empty.title" var="emptyAvailableTitle"/>
    <spring:message code="commerce.profile.available.empty.description" var="emptyAvailableDesc"/>
    <c:url var="packCatalogUrl" value="/packs"/>

    <main class="commerce-profile-main">
        <a href="${packCatalogUrl}"
           onclick="if (window.history.length > 1 && document.referrer.indexOf(window.location.host) !== -1) { window.history.back(); return false; }"
           class="commerce-profile-back">
            <span class="material-symbols-outlined text-xl" aria-hidden="true">arrow_back</span>
            <spring:message code="commerce.profile.back"/>
        </a>

        <header class="commerce-profile-hero commerce-profile-hero--editorial"
                aria-label="<spring:message code='commerce.profile.hero.aria'/>">
            <div class="commerce-profile-hero__brand">
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
                <div class="commerce-profile-hero__meta">
                    <h1 class="commerce-profile-hero__name font-headline"><c:out value="${commerceCommercialName}"/></h1>
                    <p class="commerce-profile-hero__category">
                        <spring:message code="commerce.category.${commerceCategory}" text="${commerceCategory}"/>
                    </p>
                    <paw:commerceReviewSummary averageRating="${commerceReviewAverageRating}" reviewCount="${commerceReviewCount}"/>
                </div>
            </div>
        </header>

        <div class="commerce-profile-layout">
            <div class="commerce-profile-layout__main">
                <section class="commerce-profile-section" aria-label="<spring:message code='commerce.profile.offers.aria'/>">
                    <h2 class="commerce-profile-section__title font-headline">
                        <spring:message code="commerce.profile.available.title"/>
                    </h2>

                    <c:choose>
                        <c:when test="${activeAuctionsTotal == 0 && directPacksTotal == 0}">
                            <paw:packEmptyState icon="inventory_2" title="${emptyAvailableTitle}" description="${emptyAvailableDesc}"/>
                        </c:when>
                        <c:otherwise>
                            <div class="commerce-profile-offers-grid">
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
                                        auction="${true}"
                                    />
                                </c:forEach>
                                <c:forEach var="pack" items="${directPacks}">
                                    <c:set var="packUnavailable" value="${pack.stock == null || pack.stock lt 1}"/>
                                    <paw:packCard
                                        packId="${pack.id}"
                                        imageId="${pack.imageId}"
                                        title="${pack.title}"
                                        subtitle="${pack.description}"
                                        price="$${pack.finalPrice}"
                                        oldPrice="$${pack.originalPrice}"
                                        unavailable="${packUnavailable}"
                                    />
                                </c:forEach>
                            </div>
                            <c:if test="${totalPages gt 1}">
                                <div class="commerce-profile-offers-pagination">
                                    <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                                    baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}"/>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="commerce-profile-section" aria-label="<spring:message code='commerce.profile.reviews.aria'/>">
                    <h2 class="commerce-profile-section__title font-headline">
                        <spring:message code="commerce.profile.reviews.title"/>
                    </h2>
                    <paw:reviewList items="${commerceReviewItems}"
                                    reviewCount="${commerceReviewCount}"
                                    averageRating="${commerceReviewAverageRating}"
                                    fullWidth="${true}"/>
                </section>
            </div>

            <aside class="commerce-profile-layout__sidebar" aria-label="<spring:message code='commerce.profile.info.aria'/>">
                <div class="commerce-profile-sidebar-card">
                    <h2 class="commerce-profile-sidebar-card__title font-headline">
                        <c:set var="escapedCommercialName"><c:out value="${commerceCommercialName}"/></c:set>
                        <spring:message code="commerce.profile.about.title" arguments="${escapedCommercialName}"/>
                    </h2>
                    <paw:commerceInfoCardSidebar
                        streetLine="${commerceStreetLine}"
                        locationLine="${commerceLocationLine}"
                        openingTime="${commerceOpeningTime}"
                        closingTime="${commerceClosingTime}"
                        openNow="${commerceOpenNow}"/>
                </div>
            </aside>
        </div>
    </main>

    <paw:footer />
</body>
</html>
