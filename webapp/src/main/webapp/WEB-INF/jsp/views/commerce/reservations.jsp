<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="commerce.reservations.pageTitle" />
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

<paw:navbar />

<main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
    <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
        <div>
            <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                <spring:message code="commerce.reservations.title" />
            </h1>
            <p class="text-secondary font-body">
                <spring:message code="commerce.reservations.subtitle" />
            </p>
        </div>
    </header>

    <section>
        <c:choose>
            <c:when test="${empty reservations}">
                <spring:message var="emptyTitle" code="commerce.reservations.empty.title" />
                <spring:message var="emptyDesc" code="commerce.reservations.empty.description" />
                <paw:packEmptyState icon="inventory_2" title="${emptyTitle}" description="${emptyDesc}" />
            </c:when>
            <c:otherwise>
                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <spring:message code="commerce.reservations.section.all" />
                    </h2>
                    <div class="h-[1px] flex-grow bg-outline-variant"></div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                    <c:forEach var="reservation" items="${reservations}">
                        <c:set var="pack" value="${packsByReservationId[reservation.id]}" />
                        <c:set var="clientName" value="${clientNamesByReservationId[reservation.id]}" />
                        <c:set var="dateLabel" value="${formattedReservationDatesById[reservation.id]}" />

                        <article class="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow flex flex-col h-full">
                            <div class="relative h-48 sm:h-56 overflow-hidden">
                                <img src="${pageContext.request.contextPath}/packs/${pack.id}/image"
                                     alt="${pack.title}"
                                     class="w-full h-full object-cover" />
                            </div>

                            <div class="p-5 flex flex-col gap-4 flex-grow">
                                <div>
                                    <h3 class="font-bold text-lg text-on-surface truncate"><c:out value="${pack.title}" /></h3>
                                    <p class="text-secondary text-sm mt-1 line-clamp-2 h-10"><c:out value="${pack.description}" /></p>
                                </div>

                                <div class="bg-surface-container rounded-xl p-4 space-y-2 text-sm">
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="commerce.reservations.card.client" /></span>
                                        <span class="font-semibold text-on-surface"><c:out value="${clientName}" /></span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="commerce.reservations.card.status" /></span>
                                        <span class="font-semibold text-primary">
                                            <spring:message code="reservation.status.${reservation.status}" />
                                        </span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="commerce.reservations.card.quantity" /></span>
                                        <span class="font-semibold text-on-surface"><c:out value="${reservation.quantity}" /></span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="commerce.reservations.card.date" /></span>
                                        <span class="font-semibold text-on-surface"><c:out value="${dateLabel}" /></span>
                                    </div>
                                    <div class="flex items-center justify-between gap-3">
                                        <span class="text-on-surface-variant"><spring:message code="commerce.reservations.card.total" /></span>
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
            </c:otherwise>
        </c:choose>
    </section>
</main>

<paw:footer />

</body>
</html>
