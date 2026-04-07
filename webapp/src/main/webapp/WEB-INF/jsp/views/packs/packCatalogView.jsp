<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <c:choose>
        <c:when test="${not empty pageTitle}">
            <title><c:out value="${pageTitle}"/></title>
        </c:when>
        <c:otherwise>
            <title><spring:message code="app.brand"/> | <spring:message code="pack.catalog.pageTitle.suffix"/></title>
        </c:otherwise>
    </c:choose>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>
</head>
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">
    
    <paw:navbar />

    <!-- Main Content Area -->
    <main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
        <!-- Header Section -->
        <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
                <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2"><spring:message code="pack.catalog.headline"/></h1>
                <p class="text-secondary font-body"><spring:message code="pack.catalog.subtitle"/></p>
            </div>
            <div class="flex flex-col sm:flex-row items-center w-full md:w-auto gap-4">
                <spring:message code="pack.catalog.search.placeholder" var="packCatalogSearchPlaceholder"/>
                <form action="${pageContext.request.contextPath}/packs" method="GET" class="w-full sm:w-auto">
                    <paw:searchBar value="${param.q}" placeholder="${packCatalogSearchPlaceholder}" classes="relative w-full sm:w-80" />
                </form>
            </div>
        </header>

        <div class="mb-10">
            <paw:tagFilter
                availableTags="${availableTags}"
                selectedTags="${selectedTags}"
                baseUrl="${pageContext.request.contextPath}/packs"
                searchQuery="${param.q}"
            />
        </div>

        <!-- Main Grid: All Available Packs -->
        <section>
            <div class="flex items-center gap-3 mb-8">
                <h2 class="text-2xl font-headline font-bold text-on-surface">
                    <c:choose>
                        <c:when test="${not empty param.q}"><spring:message code="pack.catalog.searchResults" arguments="${param.q}"/></c:when>
                        <c:otherwise><spring:message code="pack.catalog.allPacks"/></c:otherwise>
                    </c:choose>
                </h2>
                <div class="h-[1px] flex-grow bg-outline-variant"></div>
            </div>

            <c:choose>
                <c:when test="${empty packs}">
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
                        <c:forEach var="pack" items="${packs}">
                            <paw:packCard
                                packId="${pack.id}"
                                title="${pack.title}"
                                subtitle="${pack.description}"
                                price="$${pack.finalPrice}"
                                oldPrice="$${pack.originalPrice}"
                                commerceName="${commerceNames[pack.id]}"
                            />
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
