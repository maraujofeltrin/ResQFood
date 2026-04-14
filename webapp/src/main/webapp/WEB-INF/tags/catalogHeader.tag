<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<%@ attribute name="availableSorts" required="true" type="ar.edu.itba.paw.models.PackSortOption[]" %>
<%@ attribute name="currentSort" required="true" type="ar.edu.itba.paw.models.PackSortOption" %>
<%@ attribute name="availableTags" required="true" type="ar.edu.itba.paw.models.PackTag[]" %>
<%@ attribute name="selectedTags" required="true" type="java.util.List" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="catalogBaseUrl" required="false" type="java.lang.String" %>
<%@ attribute name="activePortal" required="false" type="java.lang.String" %>

<c:set var="resolvedCatalogBaseUrl" value="${not empty catalogBaseUrl ? catalogBaseUrl : '/packs'}"/>
<c:set var="resolvedActivePortal" value="${not empty activePortal ? activePortal : 'packs'}"/>

<header class="flex flex-col gap-6 mb-12">
    <div>
        <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2"><spring:message code="pack.catalog.headline"/></h1>
        <p class="text-secondary font-body"><spring:message code="pack.catalog.subtitle"/></p>
    </div>

    <div class="inline-flex items-center self-start p-1 rounded-full bg-surface-container-low gap-1 shadow-sm">
        <a href="${pageContext.request.contextPath}/packs"
           class="px-5 py-2 rounded-full text-sm font-semibold transition-colors ${resolvedActivePortal == 'packs' ? 'bg-surface-container-lowest text-primary shadow-sm' : 'text-on-surface-variant hover:text-on-surface'}">
            <spring:message code="pack.catalog.switch.packs"/>
        </a>
        <a href="${pageContext.request.contextPath}/auctions"
           class="px-5 py-2 rounded-full text-sm font-semibold transition-colors ${resolvedActivePortal == 'auctions' ? 'bg-surface-container-lowest text-primary shadow-sm' : 'text-on-surface-variant hover:text-on-surface'}">
            <spring:message code="pack.catalog.switch.auctions"/>
        </a>
    </div>
    
    <div class="flex flex-col md:flex-row items-center gap-3 w-full xl:w-auto">
        <spring:message code="pack.catalog.search.placeholder" var="packCatalogSearchPlaceholder"/>
        
        <form action="${pageContext.request.contextPath}${resolvedCatalogBaseUrl}" method="GET" class="w-full md:w-56 max-w-full">
            <c:if test="${currentSort != null && currentSort.name() != 'DATE_DESC'}">
                <input type="hidden" name="sort" value="${currentSort.name()}"/>
            </c:if>
            <c:forEach var="tag" items="${selectedTags}">
                <input type="hidden" name="tags" value="${tag.name()}"/>
            </c:forEach>
            <paw:searchBar value="${searchQuery}" placeholder="${packCatalogSearchPlaceholder}" classes="relative w-full" />
        </form>

        <div class="w-full md:w-56 max-w-full">
            <paw:sortDropdown 
                availableSorts="${availableSorts}" 
                currentSort="${currentSort}" 
                baseUrl="${resolvedCatalogBaseUrl}" 
                searchQuery="${searchQuery}" 
                selectedTags="${selectedTags}"
                classes="w-full"
            />
        </div>

        <div class="w-full md:w-auto flex justify-end shrink-0">
            <paw:catalogFilter
                availableTags="${availableTags}"
                selectedTags="${selectedTags}"
                baseUrl="${resolvedCatalogBaseUrl}"
                searchQuery="${searchQuery}"
                currentSort="${currentSort}"
            />
        </div>
    </div>
</header>
