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
        <c:when test="${empty reservations}">
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
                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <spring:message code="reservation.my.section.all" />
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                    <c:forEach var="reservation" items="${reservations}">
                        <c:set var="pack" value="${packsByReservationId[reservation.id]}" />
                        <c:set var="commerceName" value="${commerceNamesByReservationId[reservation.id]}" />
                        <c:set var="dateLabel" value="${formattedReservationDatesById[reservation.id]}" />

                        <article class="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow flex flex-col h-full">
                            <a href="${pageContext.request.contextPath}/packs/${pack.id}" class="relative h-48 sm:h-56 overflow-hidden">
                                <img src="${pageContext.request.contextPath}/packs/${pack.id}/image"
                                     alt="${pack.title}"
                                     class="w-full h-full object-cover transition-transform duration-500 hover:scale-105" />
                            </a>

                            <div class="p-5 flex flex-col gap-4 flex-grow">
                                <div>
                                    <div class="flex items-center gap-2 text-secondary text-sm font-medium mb-1">
                                        <span class="material-symbols-outlined text-[1rem]">storefront</span>
                                        <c:out value="${commerceName}" />
                                    </div>
                                    <h3 class="font-bold text-lg text-on-surface truncate"><c:out value="${pack.title}" /></h3>
                                    <p class="text-secondary text-sm mt-1 line-clamp-2 h-10"><c:out value="${pack.description}" /></p>
                                </div>

                                <div class="bg-surface-container rounded-xl p-4 space-y-2 text-sm">
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="reservation.my.card.status" /></span>
                                        <span class="font-semibold text-primary">
                                            <spring:message code="reservation.status.${reservation.status}" />
                                        </span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="reservation.my.card.quantity" /></span>
                                        <span class="font-semibold text-on-surface"><c:out value="${reservation.quantity}" /></span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="reservation.my.card.date" /></span>
                                        <span class="font-semibold text-on-surface"><c:out value="${dateLabel}" /></span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="reservation.my.card.total" /></span>
                                        <span class="font-semibold text-primary">
                                            <fmt:formatNumber value="${reservation.finalPrice}" type="currency" currencyCode="ARS" />
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>

                <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}" />
            </section>
        </c:otherwise>
    </c:choose>
</main>

<paw:footer />

</body>
</html>
