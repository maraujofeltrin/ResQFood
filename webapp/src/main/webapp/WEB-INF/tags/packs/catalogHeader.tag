<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<%@ attribute name="availableSorts" required="true" type="ar.edu.itba.paw.models.pack.PackSortOption[]" %>
<%@ attribute name="currentSort" required="true" type="ar.edu.itba.paw.models.pack.PackSortOption" %>
<%@ attribute name="availableAuctionSorts" required="true" type="ar.edu.itba.paw.models.auction.AuctionSortOption[]" %>
<%@ attribute name="currentAuctionSort" required="true" type="ar.edu.itba.paw.models.auction.AuctionSortOption" %>
<%@ attribute name="availableTags" required="true" type="ar.edu.itba.paw.models.pack.PackTag[]" %>
<%@ attribute name="selectedTags" required="true" type="java.util.List" %>
<%@ attribute name="selectedTypes" required="true" type="java.util.List" %>
<%@ attribute name="catalogMode" required="true" type="java.lang.String" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="availableMunicipalities" required="false" type="ar.edu.itba.paw.models.pack.Municipality[]" %>
<%@ attribute name="selectedMunicipality" required="false" type="ar.edu.itba.paw.models.pack.Municipality" %>
<%@ attribute name="selectedTimeRanges" required="false" type="java.util.List" %>

<header class="flex flex-col xl:flex-row xl:items-center justify-between gap-6 mb-12">
    <div>
        <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2"><spring:message code="pack.catalog.headline"/></h1>
        <p class="text-secondary font-body"><spring:message code="pack.catalog.subtitle"/></p>
    </div>
    
    <form id="catalogForm" action="${pageContext.request.contextPath}/packs" method="GET" class="flex flex-col md:flex-row items-center gap-3 w-full xl:w-auto mt-4 xl:mt-0">
        <spring:message code="pack.catalog.search.placeholder" var="packCatalogSearchPlaceholder"/>
        
        <c:if test="${catalogMode ne 'AUCTIONS' && currentAuctionSort != null}">
            <input type="hidden" name="auctionSort" value="${currentAuctionSort.name()}"/>
        </c:if>
        <c:if test="${catalogMode eq 'AUCTIONS' && currentSort != null && currentSort.name() != 'DATE_DESC'}">
            <input type="hidden" name="sort" value="${currentSort.name()}"/>
        </c:if>

        <div class="w-full md:w-56 max-w-full">
            <paw:searchBar value="${searchQuery}" placeholder="${packCatalogSearchPlaceholder}" classes="relative w-full" />
        </div>

        <div class="w-full md:w-56 max-w-full">
            <c:choose>
                <c:when test="${catalogMode eq 'AUCTIONS'}">
                    <paw:auctionSortDropdown
                        availableSorts="${availableAuctionSorts}"
                        currentSort="${currentAuctionSort}"
                        baseUrl="/packs"
                        searchQuery="${searchQuery}"
                        selectedTags="${selectedTags}"
                        selectedTypes="${selectedTypes}"
                        classes="w-full"
                    />
                </c:when>
                <c:otherwise>
                    <paw:sortDropdown
                        availableSorts="${availableSorts}"
                        currentSort="${currentSort}"
                        baseUrl="/packs"
                        searchQuery="${searchQuery}"
                        selectedTags="${selectedTags}"
                        selectedTypes="${selectedTypes}"
                        currentAuctionSort="${currentAuctionSort}"
                        classes="w-full"
                    />
                </c:otherwise>
            </c:choose>
        </div>

        <div class="w-full md:w-auto flex justify-end shrink-0">
            <paw:catalogFilter
                availableTags="${availableTags}"
                selectedTags="${selectedTags}"
                selectedTypes="${selectedTypes}"
                baseUrl="/packs"
                searchQuery="${searchQuery}"
                currentSort="${currentSort}"
                availableMunicipalities="${availableMunicipalities}"
                selectedMunicipality="${selectedMunicipality}"
                selectedTimeRanges="${selectedTimeRanges}"
                currentAuctionSort="${currentAuctionSort}"
                catalogMode="${catalogMode}"
            />
        </div>
    </form>
</header>
