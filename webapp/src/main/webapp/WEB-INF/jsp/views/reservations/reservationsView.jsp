<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="${messagePrefix}.pageTitle" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar />

<c:set var="isCommerce" value="${messagePrefix == 'commerce.reservations'}" />

<c:if test="${isCommerce}">
    <paw:commerceSidebar activeLink="reservations"/>
</c:if>

<main class="pt-24 ${isCommerce ? 'pl-24 md:pl-28 pr-6 md:pr-12' : 'px-6 md:px-12'} pb-20 max-w-7xl mx-auto flex-grow w-full transition-all duration-300">
    <c:choose>
        <c:when test="${not hasAnyReservations and not hasActiveFilters}">
            <spring:message var="emptyTitle" code="${messagePrefix}.empty.title" />
            <spring:message var="emptyDesc" code="${messagePrefix}.empty.description" />
            <paw:packEmptyState icon="inventory_2" title="${emptyTitle}" description="${emptyDesc}" />
            <c:if test="${messagePrefix == 'reservation.my'}">
                <section class="mt-8 flex items-center justify-center text-center px-6">
                    <a href="${pageContext.request.contextPath}/packs"
                       class="text-xl font-headline font-medium text-primary tracking-tight underline-offset-4 decoration-primary/40 hover:underline hover:decoration-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary transition-colors">
                        <spring:message code="reservation.my.empty.centerMessage" />
                    </a>
                </section>
            </c:if>
        </c:when>
        <c:otherwise>
            <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                <div>
                    <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                        <spring:message code="${messagePrefix}.title" />
                    </h1>
                    <p class="text-secondary font-body">
                        <spring:message code="${messagePrefix}.subtitle" />
                    </p>
                </div>
                <c:if test="${messagePrefix == 'commerce.reservations'}">
                    <div class="w-full md:w-auto flex flex-col items-stretch md:items-end gap-4">
                        <spring:message code="${messagePrefix}.filters.searchPlaceholder" var="searchPlaceholderCommerce"/>
                        <div class="flex flex-col xl:flex-row flex-wrap xl:flex-nowrap items-stretch xl:items-end gap-4 w-full xl:justify-end xl:max-w-5xl">
                            <form action="${pageContext.request.contextPath}/reservations" method="get"
                                  class="flex flex-col lg:flex-row flex-wrap lg:flex-nowrap items-stretch lg:items-end gap-3 w-full lg:flex-1 lg:min-w-0">
                                <div class="w-full lg:w-52 min-w-0 flex-shrink-0">
                                    <paw:searchBar value="${searchQuery}" placeholder="${searchPlaceholderCommerce}" classes="relative w-full" />
                                </div>
                                <div class="w-full lg:w-48 min-w-0 flex-shrink-0">
                                    <paw:inlineFormSelectDropdown
                                            selectName="status"
                                            options="${statusOptions}"
                                            selectedValue="${selectedStatus}"
                                            emptyOptionMessageCode="${messagePrefix}.filters.status.all"
                                            optionMessageCodePrefix="reservation.status."
                                            classes="w-full" />
                                </div>
                                <c:if test="${not empty selectedStatus}">
                                    <div class="w-full lg:w-auto flex flex-row items-center justify-end flex-shrink-0">
                                        <a href="${pageContext.request.contextPath}/reservations"
                                           class="px-4 py-2.5 rounded-full bg-surface-container-high text-on-surface font-semibold hover:bg-surface-container-highest transition no-underline whitespace-nowrap inline-flex items-center justify-center">
                                            <spring:message code="${messagePrefix}.filters.clear" />
                                        </a>
                                    </div>
                                </c:if>
                            </form>
                            <div class="flex items-center gap-4 xl:flex-shrink-0 md:justify-end">
                                <a href="${pageContext.request.contextPath}/commerce/verify-pickup"
                                   class="bg-primary text-on-primary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md whitespace-nowrap">
                                    <span class="material-symbols-outlined font-bold" style="font-size: 20px;">qr_code_scanner</span>
                                    <spring:message code="commerce.reservations.verifyPickup" />
                                </a>
                            </div>
                        </div>
                    </div>
                </c:if>
            </header>

            <c:if test="${messagePrefix == 'reservation.my' and not empty clientReservationsTab}">
                <div class="flex flex-col xl:flex-row xl:items-center xl:justify-between gap-4 xl:gap-6 mb-8">
                    <div class="min-w-0 overflow-x-auto -mx-1 px-1">
                        <paw:segmentedTripleTabs basePath="/reservations"
                            currentTab="${clientReservationsTab}"
                            itemsMessageCode="commerce.dashboard.tab.items"
                            packsMessageCode="commerce.dashboard.tab.packs"
                            auctionsMessageCode="commerce.dashboard.tab.auctions"
                            itemsCount="${itemsCount}"
                            packsCount="${packsCount}"
                            auctionsCount="${auctionsCount}"
                            extraQuery="${clientTabExtraQuery}"
                            marginClass="mb-0" />
                    </div>
                    <spring:message code="${messagePrefix}.filters.searchPlaceholder" var="searchPlaceholder"/>
                    <form action="${pageContext.request.contextPath}/reservations" method="get"
                          class="flex flex-col sm:flex-row flex-wrap sm:flex-nowrap items-stretch sm:items-center gap-3 w-full xl:w-auto xl:max-w-3xl xl:flex-shrink-0 xl:justify-end">
                        <input type="hidden" name="tab" value="<c:out value='${clientReservationsTab}'/>" />
                        <div class="w-full sm:w-48 min-w-0 flex-shrink-0">
                            <paw:searchBar value="${searchQuery}" placeholder="${searchPlaceholder}" classes="relative w-full" />
                        </div>
                        <div class="w-full sm:w-44 min-w-0 flex-shrink-0">
                            <c:choose>
                                <c:when test="${clientAuctionsView}">
                                    <paw:inlineFormSelectDropdown
                                            selectName="auctionStatus"
                                            options="${auctionStatusOptions}"
                                            selectedValue="${selectedAuctionStatus}"
                                            emptyOptionMessageCode="reservation.my.filters.auctionStatus.all"
                                            optionMessageCodePrefix="auction.status."
                                            classes="w-full" />
                                </c:when>
                                <c:otherwise>
                                    <paw:inlineFormSelectDropdown
                                            selectName="status"
                                            options="${statusOptions}"
                                            selectedValue="${selectedStatus}"
                                            emptyOptionMessageCode="${messagePrefix}.filters.status.all"
                                            optionMessageCodePrefix="reservation.status."
                                            classes="w-full" />
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${(clientAuctionsView and not empty selectedAuctionStatus) or (not clientAuctionsView and not empty selectedStatus)}">
                            <div class="w-full sm:w-auto flex flex-row items-center justify-end sm:justify-start flex-shrink-0">
                                <a href="<c:out value='${pageContext.request.contextPath}/reservations?tab=${clientReservationsTab}'/>"
                                   class="px-4 py-2.5 rounded-full bg-surface-container-high text-on-surface font-semibold hover:bg-surface-container-highest transition no-underline whitespace-nowrap inline-flex items-center justify-center">
                                    <spring:message code="${messagePrefix}.filters.clear" />
                                </a>
                            </div>
                        </c:if>
                    </form>
                </div>
            </c:if>

            <section>
                <c:if test="${not empty reservationActionMessageCode}">
                    <div class="mb-6 rounded-xl px-4 py-3 text-sm font-semibold ${reservationActionKind == 'success' ? 'bg-primary-container text-on-primary-container' : 'bg-error-container text-on-error-container'}"
                         role="alert">
                        <spring:message code="${reservationActionMessageCode}" />
                    </div>
                </c:if>

                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <c:choose>
                            <c:when test="${messagePrefix == 'reservation.my' and clientReservationsTab == 'auctions'}">
                                <spring:message code="reservation.my.section.auctions" />
                            </c:when>
                            <c:otherwise>
                                <spring:message code="${messagePrefix}.section.all" />
                            </c:otherwise>
                        </c:choose>
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>
                <c:choose>
                    <c:when test="${clientAuctionsView and empty clientParticipationAuctions}">
                        <section class="min-h-[35vh] flex items-center justify-center text-center px-6">
                            <p class="text-2xl md:text-3xl font-headline font-bold text-primary tracking-tight">
                                <spring:message code="reservation.my.empty.auctions.filteredMessage" />
                            </p>
                        </section>
                    </c:when>
                    <c:when test="${not clientAuctionsView and empty reservations}">
                        <section class="min-h-[35vh] flex items-center justify-center text-center px-6">
                            <p class="text-2xl md:text-3xl font-headline font-bold text-primary tracking-tight">
                                <spring:message code="${messagePrefix}.empty.filteredMessage" />
                            </p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <c:choose>
                            <c:when test="${clientAuctionsView}">
                                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                                    <c:forEach var="auction" items="${clientParticipationAuctions}">
                                        <c:choose>
                                            <c:when test="${not empty auction.pack.commerce.commercialName}">
                                                <c:set var="resolvedAuctionCommerceName" value="${auction.pack.commerce.commercialName}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="resolvedAuctionCommerceName" value="-" />
                                            </c:otherwise>
                                        </c:choose>
                                        <paw:clientAuctionParticipationCard
                                                auction="${auction}"
                                                commerceName="${resolvedAuctionCommerceName}"
                                                endLabel="${auctionEndLabels[auction.id]}"
                                                myMaxBid="${auction.myMaxBid}"
                                                badgeCode="${auctionParticipationBadges[auction.id]}" />
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                                    <c:forEach var="reservation" items="${reservations}">
                                        <c:set var="dateLabel" value="${formattedReservationDatesById[reservation.id]}" />
                                        <c:choose>
                                            <c:when test="${not empty reservation.pack.commerce.commercialName}">
                                                <c:set var="resolvedCommerceName" value="${reservation.pack.commerce.commercialName}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="resolvedCommerceName" value="-" />
                                            </c:otherwise>
                                        </c:choose>
                                        <c:set var="cardClientName" value="" />
                                        <c:if test="${messagePrefix == 'commerce.reservations'}">
                                            <c:set var="cardClientName" value="${reservation.customer.fullName}" />
                                        </c:if>

                                        <paw:reservationCard
                                                reservation="${reservation}"
                                                pack="${reservation.pack}"
                                                dateLabel="${dateLabel}"
                                                commerceName="${resolvedCommerceName}"
                                                clientName="${cardClientName}"
                                            messagePrefix="${messagePrefix}"
                                            currentPage="${currentPage}"
                                            searchQuery="${searchQuery}"
                                            selectedStatus="${selectedStatus}"
                                            auctionVisual="${not empty reservation.pack.auction}" />
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                        baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}" />
                    </c:otherwise>
                </c:choose>
            </section>
        </c:otherwise>
    </c:choose>
</main>

<paw:footer />

</body>
</html>
