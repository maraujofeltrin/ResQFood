<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="reservation.my.pageTitle" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar />

<main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
    <c:choose>
        <c:when test="${not hasAnyReservations and not hasActiveFilters}">
            <section class="min-h-[60vh] flex items-center justify-center text-center px-6">
                <p class="text-2xl md:text-3xl font-headline font-bold text-primary tracking-tight">
                    <spring:message code="reservation.my.empty.centerMessage" />
                </p>
            </section>
        </c:when>
        <c:otherwise>
            <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                <div>
                    <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                        <spring:message code="reservation.my.title" />
                    </h1>
                    <p class="text-secondary font-body">
                        <spring:message code="reservation.my.subtitle" />
                    </p>
                </div>
            </header>

            <section>
                <spring:message code="reservation.my.filters.searchPlaceholder" var="myReservationSearchPlaceholder"/>
                <form action="${pageContext.request.contextPath}/reservations/mine" method="get"
                      class="flex flex-col md:flex-row items-center gap-3 w-full mb-8">
                    <div class="w-full md:w-56 max-w-full">
                        <paw:searchBar value="${searchQuery}" placeholder="${myReservationSearchPlaceholder}" classes="relative w-full" />
                    </div>

                    <div class="w-full md:w-56 max-w-full">
                        <div class="inline-flex items-center rounded-full py-1.5 px-3 text-sm font-semibold transition-colors duration-200 bg-surface-container-low text-on-surface hover:bg-surface-container-high relative w-full overflow-hidden">
                            <span class="material-symbols-outlined text-base mr-1 pointer-events-none shrink-0">swap_vert</span>
                            <select name="status"
                                    class="appearance-none bg-transparent outline-none cursor-pointer text-sm font-semibold text-on-surface w-full pr-8 focus:outline-none focus:ring-0 truncate"
                                    style="outline: none !important; box-shadow: none !important; border: none !important; text-overflow: ellipsis;">
                                <option value=""><spring:message code="reservation.my.filters.status.all" /></option>
                                <c:forEach var="statusOption" items="${statusOptions}">
                                    <option value="${statusOption}" ${selectedStatus == statusOption.name() ? 'selected' : ''}>
                                        <spring:message code="reservation.status.${statusOption}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="w-full md:w-auto flex items-center justify-end gap-2">
                        <button type="submit" class="bg-primary text-on-primary px-5 py-2 rounded-full font-semibold hover:brightness-110 transition">
                            <spring:message code="reservation.my.filters.apply" />
                        </button>
                        <a href="${pageContext.request.contextPath}/reservations/mine"
                           class="px-4 py-2 rounded-full bg-surface-container-high text-on-surface font-semibold hover:bg-surface-container-highest transition no-underline">
                            <spring:message code="reservation.my.filters.clear" />
                        </a>
                    </div>
                </form>

                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <spring:message code="reservation.my.section.all" />
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>
                <c:choose>
                    <c:when test="${empty reservations}">
                        <section class="min-h-[35vh] flex items-center justify-center text-center px-6">
                            <p class="text-2xl md:text-3xl font-headline font-bold text-primary tracking-tight">
                                <spring:message code="reservation.my.empty.filteredMessage" />
                            </p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                            <c:forEach var="reservation" items="${reservations}">
                                <c:set var="pack" value="${packsByReservationId[reservation.id]}" />
                                <c:set var="commerceName" value="${commerceNamesByReservationId[reservation.id]}" />
                                <c:set var="dateLabel" value="${formattedReservationDatesById[reservation.id]}" />

                                <paw:reservationCard
                                        reservation="${reservation}"
                                        pack="${pack}"
                                        dateLabel="${dateLabel}"
                                        commerceName="${commerceName}"
                                        messagePrefix="reservation.my" />
                            </c:forEach>
                        </div>

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
