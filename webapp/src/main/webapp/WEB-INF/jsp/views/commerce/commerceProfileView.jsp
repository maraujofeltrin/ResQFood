<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
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

    <c:url var="commerceFavoriteAction" value="/commerces/${commerceUserId}/favorite"/>
    <spring:message code="commerce.profile.favorite.toggleAria" var="commerceFavoriteToggleAria"/>

    <main class="commerce-profile-main">
        <paw:backLink catalogUrl="${packCatalogUrl}" backLabelCode="commerce.profile.back"/>

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
                <sec:authorize access="hasRole('CLIENT')">
                    <form action="${commerceFavoriteAction}" method="post" class="commerce-profile-favorite-form">
                        <button type="submit"
                                class="commerce-profile-favorite-btn<c:if test='${commerceFavoriteSelected}'> commerce-profile-favorite-btn--selected</c:if>"
                                aria-label="<c:out value='${commerceFavoriteToggleAria}'/>"
                                aria-pressed="${commerceFavoriteSelected}">
                            <span class="material-symbols-outlined commerce-profile-favorite-icon" aria-hidden="true">favorite</span>
                        </button>
                    </form>
                </sec:authorize>
            </div>
        </header>

        <div class="commerce-profile-layout">
            <div class="commerce-profile-layout__main">
                <section class="commerce-profile-section" aria-label="<spring:message code='commerce.profile.offers.aria'/>">
                    <h2 class="commerce-profile-section__title font-headline">
                        <spring:message code="commerce.profile.available.title"/>
                    </h2>

                    <c:choose>
                        <c:when test="${totalOffers == 0}">
                            <paw:packEmptyState icon="inventory_2" title="${emptyAvailableTitle}" description="${emptyAvailableDesc}"/>
                        </c:when>
                        <c:otherwise>
                            <div class="commerce-profile-offers-scroll">
                                <div class="commerce-profile-offers-grid">
                                    <c:forEach var="pack" items="${profileOffers}">
                                        <c:choose>
                                            <c:when test="${not empty pack.auction and pack.auction.active}">
                                                <paw:packCard
                                                    packId="${pack.id}"
                                                    imageId="${pack.imageId}"
                                                    title="${pack.title}"
                                                    subtitle="${pack.description}"
                                                    badgeText="${auctionBadgeText}"
                                                    rescueLabel="${auctionCurrentBidLabel}"
                                                    price="$${pack.auction.effectivePrice}"
                                                    oldPrice="$${pack.originalPrice}"
                                                    auction="${true}"
                                                />
                                            </c:when>
                                            <c:otherwise>
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
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
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
                                    commerceUserId="${commerceUserId}"
                                    canSubmit="${commerceReviewCanSubmit}"
                                    alreadySubmitted="${commerceReviewAlreadySubmitted}"
                                    formExpanded="${commerceReviewFormExpanded}"
                                    alertKind="${commerceReviewAlertKind}"
                                    alertMessage="${commerceReviewAlertMessage}"
                                    sectionMessageCode="commerce.profile.reviews.aria"
                                    titleMessageCode="commerce.profile.reviews.title"
                                    eyebrowMessageCode="pack.detail.reviews.eyebrow"
                                    fullWidth="${true}"
                                    reviewCurrentPage="${reviewCurrentPage}"
                                    reviewTotalPages="${reviewTotalPages}"
                                    reviewPaginationBaseUrl="${pageContext.request.contextPath}/commerces/${commerceUserId}"/>
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
