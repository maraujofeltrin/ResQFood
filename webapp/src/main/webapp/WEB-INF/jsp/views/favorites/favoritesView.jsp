<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="favorites.pageTitle.suffix" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

    <paw:navbar />

    <main class="pt-24 px-6 md:px-12 pb-20 flex-grow">
        <div class="max-w-6xl mx-auto w-full">

            <%-- Header section --%>
            <div class="fav-header mb-12">
                <div class="fav-header__icon-ring">
                    <span class="material-symbols-outlined fav-header__icon" style="font-variation-settings: 'FILL' 1;">favorite</span>
                </div>
                <h1 class="text-3xl md:text-4xl font-headline font-bold text-on-surface mt-4">
                    <spring:message code="favorites.title"/>
                </h1>
                <p class="text-secondary text-lg mt-2 max-w-xl mx-auto">
                    <spring:message code="favorites.subtitle"/>
                </p>
            </div>

            <div class="flex flex-col lg:grid lg:grid-cols-3 gap-12 lg:gap-8 xl:gap-12">
                <%-- Main Column: Packs --%>
                <div class="lg:col-span-2 min-w-0">
                    <div class="flex flex-col gap-12 lg:sticky lg:top-28 lg:h-max pb-4">
                        <%-- Packs section --%>
                        <section>
                        <div class="flex items-center gap-3 mb-8">
                            <span class="material-symbols-outlined text-primary text-xl">inventory_2</span>
                            <h2 class="text-xl font-headline font-semibold text-on-surface">
                                <spring:message code="favorites.section.packs"/>
                            </h2>
                            <c:if test="${totalFavorites > 0}">
                                <span class="fav-count-badge">${totalFavorites}</span>
                            </c:if>
                            <div class="h-[1px] flex-grow bg-outline-variant/40"></div>
                        </div>

                        <c:choose>
                            <c:when test="${empty packs}">
                                <div class="fav-empty-state">
                                    <div class="fav-empty-state__glow"></div>
                                    <span class="material-symbols-outlined fav-empty-state__icon"
                                          style="font-variation-settings: 'FILL' 1, 'wght' 200;">favorite</span>
                                    <h3 class="text-2xl font-headline font-bold text-on-surface mt-2">
                                        <spring:message code="favorites.empty.packs.title"/>
                                    </h3>
                                    <p class="text-secondary mt-2 text-lg max-w-md mx-auto">
                                        <spring:message code="favorites.empty.packs.description"/>
                                    </p>
                                    <a href="${pageContext.request.contextPath}/packs"
                                       class="fav-empty-state__cta mt-6">
                                        <span class="material-symbols-outlined text-lg">explore</span>
                                        <spring:message code="favorites.empty.exploreBtn"/>
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-6 md:gap-8">
                                    <c:forEach var="pack" items="${packs}">
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

                                <paw:pagination currentPage="${currentPackPage}" totalPages="${totalPackPages}"
                                                baseUrl="${pageContext.request.contextPath}/favorites?commercePage=${currentCommercePage}"
                                                pageParam="packPage" />
                            </c:otherwise>
                        </c:choose>
                    </section>
                    </div>
                </div>

                <%-- Sidebar Column: Commerces --%>
                <aside class="lg:col-span-1 min-w-0">
                    <div class="lg:sticky lg:top-28 lg:h-max pb-4">
                        <%-- Commerce favorites section --%>
                        <section>
                        <div class="flex items-center gap-3 mb-6">
                            <span class="material-symbols-outlined text-primary text-xl">storefront</span>
                            <h2 class="text-xl font-headline font-semibold text-on-surface">
                                <spring:message code="favorites.section.commerces"/>
                            </h2>
                            <c:if test="${totalFavoriteCommerces > 0}">
                                <span class="fav-count-badge bg-primary text-on-primary">${totalFavoriteCommerces}</span>
                            </c:if>
                            <div class="h-[1px] flex-grow bg-outline-variant/40"></div>
                        </div>

                        <c:choose>
                            <c:when test="${empty favoriteCommerces}">
                                <div class="fav-empty-state text-center py-8">
                                    <span class="material-symbols-outlined fav-empty-state__icon text-4xl text-outline-variant mb-4"
                                          style="font-variation-settings: 'FILL' 0, 'wght' 200;">storefront</span>
                                    <h3 class="text-lg font-headline font-bold text-on-surface">
                                        <spring:message code="favorites.empty.commerces.title"/>
                                    </h3>
                                    <p class="text-secondary mt-1 text-sm">
                                        <spring:message code="favorites.empty.commerces.description"/>
                                    </p>
                                    <a href="${pageContext.request.contextPath}/packs?types=commerces"
                                       class="text-primary font-medium hover:underline inline-flex items-center gap-1 mt-4">
                                        <span class="material-symbols-outlined text-sm">explore</span>
                                        <spring:message code="favorites.empty.exploreCommercesBtn"/>
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-4">
                                    <c:forEach var="commerce" items="${favoriteCommerces}">
                                        <paw:commerceCard
                                            commerceId="${commerce.userId}"
                                            commerceName="${commerce.commercialName}"
                                            category="${commerce.category}"
                                            rating="${commerceRatings[commerce.userId]}"
                                            imageId="${commerceImages[commerce.userId]}"
                                        />
                                    </c:forEach>
                                </div>
                                
                                <div class="mt-8">
                                    <paw:pagination currentPage="${currentCommercePage}" totalPages="${totalCommercePages}"
                                                    baseUrl="${pageContext.request.contextPath}/favorites?packPage=${currentPackPage}"
                                                    pageParam="commercePage" />
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                    </div>
                </aside>
            </div>

        </div>
    </main>

    <paw:footer />

</body>
</html>
