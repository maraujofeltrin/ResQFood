<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
                availableTags="${availableTags}"
                selectedTags="${selectedTags}"
                searchQuery="${param.q}"
                availableMunicipalities="${availableMunicipalities}"
                selectedMunicipality="${selectedMunicipality}"
                selectedTimeRanges="${selectedTimeRanges}"
            />

            <!-- Main Grid: All Available Packs -->
            <section>
                <div class="flex items-center gap-3 mb-8">
                    <h2 class="text-2xl font-headline font-bold text-on-surface">
                        <c:choose>
                            <c:when test="${not empty param.q}"><spring:message code="pack.catalog.searchResults" arguments="${fn:escapeXml(param.q)}"/></c:when>
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
            </div>
        </main>
    
        <paw:footer />
    </div>

</body>
</html>
