<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">

<paw:head titleSuffixCode="commerce.reviews.pageTitle" />

<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

    <paw:navbar />

    <paw:commerceSidebar activeLink="reviews"/>

    <main class="pt-24 pl-24 md:pl-28 pr-6 md:pr-12 pb-20 max-w-7xl mx-auto flex-grow w-full transition-all duration-300">

        <section class="mb-10">
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
                <div>
                    <h1 class="text-3xl font-headline font-extrabold text-on-surface tracking-tight">
                        <spring:message code="commerce.reviews.title" />
                    </h1>
                    <c:if test="${not empty averageRating}">
                        <div class="flex items-center gap-2 mt-2">
                            <paw:commerceReviewSummary averageRating="${averageRating}" reviewCount="${reviewCount}"/>
                        </div>
                    </c:if>
                </div>
                <span class="inline-flex items-center gap-1 bg-primary-container text-on-primary-container px-4 py-2 rounded-full text-sm font-bold">
                    <span class="material-symbols-outlined" style="font-size: 18px;">rate_review</span>
                    <spring:message code="pack.detail.reviews.count" arguments="${reviewCount}"/>
                </span>
            </div>

            <c:choose>
                <c:when test="${empty reviewItems}">
                    <div class="bg-surface-container-lowest rounded-2xl p-12 text-center shadow-soft">
                        <span class="material-symbols-outlined text-secondary mb-4" style="font-size: 48px;">rate_review</span>
                        <p class="text-secondary text-lg"><spring:message code="commerce.reviews.empty" /></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <c:forEach var="row" items="${reviewItems}">
                            <paw:reviewDashboardCard reviewId="${row.review.id}"
                                                     clientName="${row.clientName}"
                                                     rating="${row.review.rating}"
                                                     body="${row.review.body}"
                                                     formattedDate="${row.formattedDate}"
                                                     edited="${row.edited}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${totalPages > 1}">
            <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}" baseUrl="${paginationBaseUrl}"/>
        </c:if>

    </main>

    <paw:footer />

</body>

</html>
