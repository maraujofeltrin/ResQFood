<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head title="${pageTitle}" titleSuffixCode="pack.catalog.pageTitle.suffix" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">
    
    <paw:navbar />

    <div class="flex-grow flex flex-col lg:ml-64">
        <!-- Main Content Area -->
        <main class="pt-24 px-6 md:px-12 pb-20 flex-grow">
            <div class="max-w-7xl mx-auto w-full">
            <!-- Header Section -->
            <paw:catalogHeader 
                availableSorts="${availableSorts}"
                currentSort="${currentSort}"
                availableAuctionSorts="${availableAuctionSorts}"
                currentAuctionSort="${currentAuctionSort}"
                availableTags="${availableTags}"
                selectedTags="${selectedTags}"
                selectedTypes="${selectedTypes}"
                catalogMode="${catalogMode}"
                searchQuery="${param.q}"
                availableMunicipalities="${availableMunicipalities}"
                selectedMunicipality="${selectedMunicipality}"
                selectedTimeRanges="${selectedTimeRanges}"
            />

            <spring:message code="pack.catalog.lastChance.badge" var="auctionBadgeText"/>
            <spring:message code="pack.catalog.auction.currentBid" var="auctionCurrentBidLabel"/>

            <sec:authorize access="hasRole('CLIENT')">
                <c:if test="${catalogMode eq 'ALL' and not empty favoritesCarouselPacks}">
                    <section class="mb-14" aria-label="<spring:message code='pack.catalog.favorites.title'/>">
                        <div class="flex items-center justify-between gap-4 mb-6">
                            <div class="flex items-center gap-3">
                                <h2 class="text-2xl font-headline font-bold text-on-surface"><spring:message code="pack.catalog.favorites.title"/></h2>
                                <span class="auction-badge"><spring:message code="pack.catalog.favorites.badge"/></span>
                            </div>
                            <a href="${pageContext.request.contextPath}${favoritesViewAllUrl}" class="text-sm font-semibold text-primary hover:underline">
                                <spring:message code="pack.catalog.lastChance.viewAll"/>
                            </a>
                        </div>

                        <div class="flex gap-6 overflow-x-auto pb-2 snap-x snap-mandatory hide-scrollbar">
                            <c:forEach var="pack" items="${favoritesCarouselPacks}">
                                <div class="min-w-[280px] max-w-[320px] snap-start flex-shrink-0">
                                    <c:set var="favoritePackUnavailable" value="${pack.stock == null || pack.stock lt 1}"/>
                                    <c:set var="favAuction" value="${favoritePackActiveAuctions[pack.id]}"/>
                                    <c:choose>
                                        <c:when test="${not empty favAuction}">
                                            <paw:packCard
                                                packId="${pack.id}"
                                                imageId="${pack.imageId}"
                                                title="${pack.title}"
                                                subtitle="${pack.description}"
                                                badgeText="${auctionBadgeText}"
                                                rescueLabel="${auctionCurrentBidLabel}"
                                                price="$${favAuction.effectivePrice}"
                                                oldPrice="$${pack.originalPrice}"
                                                commerceName="${commerceNames[pack.id]}"
                                                auction="${true}"
                                                unavailable="${favoritePackUnavailable}"
                                            />
                                        </c:when>
                                        <c:otherwise>
                                            <paw:packCard
                                                packId="${pack.id}"
                                                imageId="${pack.imageId}"
                                                title="${pack.title}"
                                                subtitle="${pack.description}"
                                                price="$${pack.finalPrice}"
                                                oldPrice="$${pack.originalPrice}"
                                                commerceName="${commerceNames[pack.id]}"
                                                unavailable="${favoritePackUnavailable}"
                                            />
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>
            </sec:authorize>

            <c:if test="${catalogMode eq 'ALL' and not empty auctionsCarousel}">
                <section class="mb-14">
                    <div class="flex items-center justify-between gap-4 mb-6">
                        <div class="flex items-center gap-3">
                            <h2 class="text-2xl font-headline font-bold text-on-surface"><spring:message code="pack.catalog.lastChance.title"/></h2>
                            <span class="auction-badge"><spring:message code="pack.catalog.lastChance.badge"/></span>
                        </div>
                        <a href="${pageContext.request.contextPath}${auctionsViewAllUrl}" class="text-sm font-semibold text-primary hover:underline">
                            <spring:message code="pack.catalog.lastChance.viewAll"/>
                        </a>
                    </div>

                    <div class="flex gap-6 overflow-x-auto pb-2 snap-x snap-mandatory hide-scrollbar">
                        <c:forEach var="auction" items="${auctionsCarousel}">
                            <div class="min-w-[280px] max-w-[320px] snap-start flex-shrink-0">
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
                            </div>
                        </c:forEach>
                    </div>
                </section>
            </c:if>

            <!-- Main Grid: All Available Packs -->
            <section>
                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <c:choose>
                            <c:when test="${catalogMode eq 'FAVORITES'}"><spring:message code="pack.catalog.allFavorites"/></c:when>
                            <c:when test="${not empty param.q}">
                                <spring:message code="pack.catalog.searchResults" arguments="${fn:escapeXml(param.q)}"/>
                            </c:when>
                            <c:when test="${catalogMode eq 'AUCTIONS'}"><spring:message code="pack.catalog.allAuctions"/></c:when>
                            <c:otherwise><spring:message code="pack.catalog.allPacks"/></c:otherwise>
                        </c:choose>
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>

                <c:choose>
                    <c:when test="${catalogMode eq 'FAVORITES' and empty packs}">
                        <spring:message var="emptyTitle" code="pack.catalog.empty.favorites.title"/>
                        <spring:message var="emptyDesc"  code="pack.catalog.empty.favorites.description"/>
                        <paw:packEmptyState icon="favorite" title="${emptyTitle}" description="${emptyDesc}" />
                    </c:when>
                    <c:when test="${catalogMode eq 'AUCTIONS' and empty auctions}">
                        <c:choose>
                            <c:when test="${not empty param.q or not empty selectedTags}">
                                <spring:message var="emptyTitle" code="pack.catalog.empty.search.auctions.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.search.auctions.description"/>
                                <paw:packEmptyState icon="gavel" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message var="emptyTitle" code="pack.catalog.empty.auctions.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.auctions.description"/>
                                <paw:packEmptyState icon="gavel" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${catalogMode ne 'AUCTIONS' and catalogMode ne 'FAVORITES' and empty packs}">
                        <c:choose>
                            <c:when test="${not empty param.q or not empty selectedTags}">
                                <spring:message var="emptyTitle" code="pack.catalog.empty.search.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.search.description"/>
                                <paw:packEmptyState icon="search_off" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message var="emptyTitle" code="pack.catalog.empty.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.description"/>
                                <paw:packEmptyState icon="storefront" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                            <c:choose>
                                <c:when test="${catalogMode eq 'FAVORITES'}">
                                    <c:forEach var="pack" items="${packs}">
                                        <c:set var="favoritePackUnavailable" value="${pack.stock == null || pack.stock lt 1}"/>
                                        <c:set var="favAuction" value="${favoritePackActiveAuctions[pack.id]}"/>
                                        <c:choose>
                                            <c:when test="${not empty favAuction}">
                                                <paw:packCard
                                                    packId="${pack.id}"
                                                    imageId="${pack.imageId}"
                                                    title="${pack.title}"
                                                    subtitle="${pack.description}"
                                                    badgeText="${auctionBadgeText}"
                                                    rescueLabel="${auctionCurrentBidLabel}"
                                                    price="$${favAuction.effectivePrice}"
                                                    oldPrice="$${pack.originalPrice}"
                                                    commerceName="${commerceNames[pack.id]}"
                                                    auction="${true}"
                                                    unavailable="${favoritePackUnavailable}"
                                                />
                                            </c:when>
                                            <c:otherwise>
                                                <paw:packCard
                                                    packId="${pack.id}"
                                                    imageId="${pack.imageId}"
                                                    title="${pack.title}"
                                                    subtitle="${pack.description}"
                                                    price="$${pack.finalPrice}"
                                                    oldPrice="$${pack.originalPrice}"
                                                    commerceName="${commerceNames[pack.id]}"
                                                    unavailable="${favoritePackUnavailable}"
                                                />
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </c:when>
                                <c:when test="${catalogMode eq 'AUCTIONS'}">
                                    <c:forEach var="auction" items="${auctions}">
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
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="pack" items="${packs}">
                                        <paw:packCard
                                            packId="${pack.id}"
                                            imageId="${pack.imageId}"
                                            title="${pack.title}"
                                            subtitle="${pack.description}"
                                            price="$${pack.finalPrice}"
                                            oldPrice="$${pack.originalPrice}"
                                            commerceName="${commerceNames[pack.id]}"
                                        />
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                        baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}" />
                    </c:otherwise>
                </c:choose>
            </section>
            </div>
        </main>
    
        <paw:footer />
    </div>

</body>
</html>
