<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
                    <!DOCTYPE html>
                    <html class="light" lang="${pageContext.response.locale.language}">

                    <head>
                        <meta charset="utf-8" />
                        <meta content="width=device-width, initial-scale=1.0" name="viewport" />
                        <title>
                            <spring:message code="app.brand" /> |
                            <spring:message code="commerce.dashboard.pageTitle" />
                        </title>
                        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css" />
                        <link
                            href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap"
                            rel="stylesheet" />
                        <link
                            href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
                            rel="stylesheet" />
                        <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
                        <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>
                    </head>

                    <body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

                        <paw:navbar />

                        <main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
                            <!-- Header Section -->
                            <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                                <div>
                                    <h1
                                        class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                                        <spring:message code="commerce.dashboard.title" />
                                    </h1>
                                    <p class="text-secondary font-body">
                                        <spring:message code="commerce.dashboard.subtitle" />
                                    </p>
                                </div>
                                <div class="flex items-center gap-4">
                                    <a href="${pageContext.request.contextPath}/commerce/create-pack"
                                        class="bg-primary text-on-primary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md">
                                        <span class="material-symbols-outlined font-bold"
                                            style="font-size: 20px;">add</span>
                                        <spring:message code="commerce.dashboard.createPack" />
                                    </a>
                                </div>
                            </header>

                            <section>
                                <c:choose>
                                    <c:when test="${empty packs}">
                                        <spring:message var="emptyTitle" code="commerce.dashboard.empty.title" />
                                        <spring:message var="emptyDesc" code="commerce.dashboard.empty.description" />
                                        <paw:packEmptyState icon="inventory_2" title="${emptyTitle}"
                                            description="${emptyDesc}" />
                                    </c:when>
                                    <c:otherwise>
                                        <div class="flex items-center gap-3 mb-8">
                                            <h2 class="text-2xl font-headline font-bold text-on-surface">
                                                <spring:message code="commerce.dashboard.section.allPacks" />
                                            </h2>
                                            <div class="h-[1px] flex-grow bg-outline-variant"></div>
                                        </div>

                                        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                                            <c:forEach var="pack" items="${packs}">
                                                <fmt:formatNumber value="${pack.finalPrice}" type="currency"
                                                    currencyCode="ARS" var="formattedPrice" />
                                                <fmt:formatNumber value="${pack.originalPrice}" type="currency"
                                                    currencyCode="ARS" var="formattedOldPrice" />
                                                <spring:message code="commerce.dashboard.pack.stock"
                                                    arguments="${pack.stock}" var="stockLabel" />
                                                <spring:message code="commerce.dashboard.pack.finalPriceLabel"
                                                    var="finalPriceLabel" />
                                                <paw:packCard packId="${pack.id}" title="${pack.title}"
                                                    subtitle="${pack.description}" price="${formattedPrice}"
                                                    oldPrice="${formattedOldPrice}" badgeText="Stock: ${pack.stock}"
                                                    rescueLabel="${finalPriceLabel}" />
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