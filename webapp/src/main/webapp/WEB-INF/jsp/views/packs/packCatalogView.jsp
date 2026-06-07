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
                availableCommerceCategories="${availableCommerceCategories}"
                selectedCommerceCategory="${selectedCommerceCategory}"
            />

            <c:if test="${catalogMode ne 'ALL'}">
                <c:url var="catalogExploreUrl" value="/packs">
                    <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if>
                    <c:forEach var="tag" items="${selectedTags}">
                        <c:param name="tags" value="${tag.name()}"/>
                    </c:forEach>
                    <c:if test="${currentSort != null && currentSort.name() != 'DATE_DESC'}"><c:param name="sort" value="${currentSort.name()}"/></c:if>
                    <c:if test="${selectedMunicipality != null}"><c:param name="location" value="${selectedMunicipality.name()}"/></c:if>
                    <c:forEach var="tr" items="${selectedTimeRanges}">
                        <c:param name="timeRange" value="${tr}"/>
                    </c:forEach>
                    <c:if test="${catalogMode eq 'AUCTIONS'}"><c:param name="auctionSort" value="${currentAuctionSort.name()}"/></c:if>
                    <c:if test="${selectedCommerceCategory != null}"><c:param name="commerceCategory" value="${selectedCommerceCategory.name()}"/></c:if>
                </c:url>
                <paw:catalogBackLink exploreUrl="${catalogExploreUrl}" />
            </c:if>

            <spring:message code="pack.catalog.lastChance.badge" var="auctionBadgeText"/>
            <spring:message code="pack.catalog.auction.currentBid" var="auctionCurrentBidLabel"/>



            <c:if test="${catalogMode eq 'ALL' and not empty auctionsCarousel}">
                <c:url var="auctionsViewAllUrl" value="/packs">
                    <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if>
                    <c:forEach var="tag" items="${selectedTags}">
                        <c:param name="tags" value="${tag.name()}"/>
                    </c:forEach>
                    <c:if test="${selectedMunicipality != null}"><c:param name="location" value="${selectedMunicipality.name()}"/></c:if>
                    <c:forEach var="tr" items="${selectedTimeRanges}">
                        <c:param name="timeRange" value="${tr}"/>
                    </c:forEach>
                    <c:param name="types" value="auctions"/>
                    <c:param name="auctionSort" value="${currentAuctionSort.name()}"/>
                </c:url>
                <section class="mb-14">
                    <div class="flex items-center justify-between gap-4 mb-6">
                        <div class="flex items-center gap-3">
                            <h2 class="text-2xl font-headline font-bold text-on-surface"><spring:message code="pack.catalog.lastChance.title"/></h2>
                            <span class="auction-badge"><spring:message code="pack.catalog.lastChance.badge"/></span>
                        </div>
                        <a href="${auctionsViewAllUrl}" class="text-sm font-semibold text-primary hover:underline">
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

            <c:if test="${catalogMode eq 'ALL'}">
                <c:url var="commercesViewAllUrl" value="/packs">
                    <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if>
                    <c:if test="${selectedMunicipality != null}"><c:param name="location" value="${selectedMunicipality.name()}"/></c:if>
                    <c:if test="${selectedCommerceCategory != null}"><c:param name="commerceCategory" value="${selectedCommerceCategory.name()}"/></c:if>
                    <c:param name="types" value="commerces"/>
                </c:url>
                <section class="mb-14">
                    <div class="flex items-center justify-between gap-4 mb-6">
                        <div class="flex items-center gap-3">
                            <h2 class="text-2xl font-headline font-bold text-on-surface"><spring:message code="pack.catalog.commerces.title" text="Explora por Comercio"/></h2>
                        </div>
                        <c:if test="${not empty commercesCarousel}">
                            <a href="${commercesViewAllUrl}" class="text-sm font-semibold text-primary hover:underline">
                                <spring:message code="pack.catalog.lastChance.viewAll"/>
                            </a>
                        </c:if>
                    </div>

                    <c:choose>
                        <c:when test="${not empty commercesCarousel}">
                            <div class="flex gap-6 overflow-x-auto pb-2 snap-x snap-mandatory hide-scrollbar">
                                <c:forEach var="commerce" items="${commercesCarousel}">
                                    <div class="min-w-[280px] max-w-[320px] snap-start flex-shrink-0">
                                        <paw:commerceCard
                                            commerceId="${commerce.userId}"
                                            commerceName="${commerce.commercialName}"
                                            category="${commerce.category}"
                                            rating="${commerceRatings[commerce.userId]}"
                                            imageId="${commerceImages[commerce.userId]}"
                                        />
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-on-surface-variant text-sm font-medium py-4">
                                <spring:message code="pack.catalog.commerces.empty"/>
                            </p>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>

            <!-- Main Grid: All Available Packs -->
            <section>
                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <c:choose>
                            <c:when test="${catalogMode eq 'COMMERCES'}"><spring:message code="pack.catalog.allCommerces" text="Todos los comercios"/></c:when>
                            <c:when test="${not empty param.q}">
                                <spring:message code="pack.catalog.searchResults" arguments="${fn:escapeXml(param.q)}"/>
                            </c:when>
                            <c:when test="${catalogMode eq 'AUCTIONS'}"><spring:message code="pack.catalog.allAuctions"/></c:when>
                            <c:otherwise><spring:message code="pack.catalog.allPacks"/></c:otherwise>
                        </c:choose>
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>

                <c:set var="hasAnyFilter" value="${not empty param.q or not empty selectedTags
                    or selectedMunicipality != null or not empty selectedTimeRanges
                    or selectedCommerceCategory != null}"/>

                <c:choose>
                    <c:when test="${catalogMode eq 'AUCTIONS' and empty auctions}">
                        <c:choose>
                            <c:when test="${hasAnyFilter}">
                                <spring:message var="emptyTitle" code="pack.catalog.empty.search.auctions.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.search.auctions.description"/>
                                <c:url var="clearFiltersUrl" value="/packs">
                                    <c:param name="types" value="auctions"/>
                                </c:url>
                                <paw:packEmptyState icon="gavel" title="${emptyTitle}" description="${emptyDesc}" clearFiltersUrl="${clearFiltersUrl}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message var="emptyTitle" code="pack.catalog.empty.auctions.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.auctions.description"/>
                                <paw:packEmptyState icon="gavel" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${catalogMode eq 'COMMERCES' and empty commerces}">
                        <c:choose>
                            <c:when test="${hasAnyFilter}">
                                <spring:message var="emptyTitle" code="pack.catalog.empty.search.commerces.title" text="No hay comercios para tu búsqueda"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.search.commerces.description" text="Prueba con otros términos o filtros."/>
                                <c:url var="clearFiltersUrl" value="/packs">
                                    <c:param name="types" value="commerces"/>
                                </c:url>
                                <paw:packEmptyState icon="storefront" title="${emptyTitle}" description="${emptyDesc}" clearFiltersUrl="${clearFiltersUrl}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message var="emptyTitle" code="pack.catalog.empty.commerces.title" text="No hay comercios disponibles"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.commerces.description" text="Vuelve a intentarlo más tarde."/>
                                <paw:packEmptyState icon="storefront" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:when test="${catalogMode ne 'AUCTIONS' and catalogMode ne 'COMMERCES' and empty packs}">
                        <c:choose>
                            <c:when test="${hasAnyFilter}">
                                <spring:message var="emptyTitle" code="pack.catalog.empty.search.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.search.description"/>
                                <c:url var="clearFiltersUrl" value="/packs"/>
                                <paw:packEmptyState icon="search_off" title="${emptyTitle}" description="${emptyDesc}" clearFiltersUrl="${clearFiltersUrl}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message var="emptyTitle" code="pack.catalog.empty.title"/>
                                <spring:message var="emptyDesc"  code="pack.catalog.empty.description"/>
                                <paw:packEmptyState icon="storefront" title="${emptyTitle}" description="${emptyDesc}" />
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <c:set var="gridClasses" value="${catalogMode eq 'COMMERCES' ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6' : 'grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8'}" />
                        <div class="grid ${gridClasses}">
                            <c:choose>
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
                                <c:when test="${catalogMode eq 'COMMERCES'}">
                                    <c:forEach var="commerce" items="${commerces}">
                                        <div class="max-w-[320px] mx-auto w-full h-full">
                                            <paw:commerceCard
                                                commerceId="${commerce.userId}"
                                                commerceName="${commerce.commercialName}"
                                                category="${commerce.category}"
                                                rating="${commerceRatings[commerce.userId]}"
                                                imageId="${commerceImages[commerce.userId]}"
                                            />
                                        </div>
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
                                        formId="catalogForm" />
                    </c:otherwise>
                </c:choose>
            </section>
            </div>
        </main>
    
        <paw:footer />
    </div>

</body>
</html>
