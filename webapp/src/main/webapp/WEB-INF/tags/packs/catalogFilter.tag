<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ attribute name="availableTags" required="true" type="ar.edu.itba.paw.models.pack.PackTag[]" %>
<%@ attribute name="selectedTags" required="true" type="java.util.List" %>
<%@ attribute name="selectedTypes" required="true" type="java.util.List" %>
<%@ attribute name="baseUrl" required="true" type="java.lang.String" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="currentSort" required="false" type="ar.edu.itba.paw.models.pack.PackSortOption" %>
<%@ attribute name="availableMunicipalities" required="false" type="ar.edu.itba.paw.models.pack.Municipality[]" %>
<%@ attribute name="selectedMunicipality" required="false" type="ar.edu.itba.paw.models.pack.Municipality" %>
<%@ attribute name="selectedTimeRanges" required="false" type="java.util.List" %>
<%@ attribute name="currentAuctionSort" required="false" type="ar.edu.itba.paw.models.auction.AuctionSortOption" %>
<%@ attribute name="catalogMode" required="false" type="java.lang.String" %>
<%@ attribute name="availableCommerceCategories" required="false" type="ar.edu.itba.paw.models.user.Commerce.Category[]" %>
<%@ attribute name="selectedCommerceCategory" required="false" type="ar.edu.itba.paw.models.user.Commerce.Category" %>

<c:set var="selectedPacks" value="false"/>
<c:set var="selectedAuctions" value="false"/>
<c:set var="selectedFavorites" value="false"/>
<c:forEach var="type" items="${selectedTypes}">
    <c:if test="${type eq 'packs'}"><c:set var="selectedPacks" value="true"/></c:if>
    <c:if test="${type eq 'auctions'}"><c:set var="selectedAuctions" value="true"/></c:if>
    <c:if test="${type eq 'favorites'}"><c:set var="selectedFavorites" value="true"/></c:if>
</c:forEach>
<c:set var="typeFilterApplied" value="${(selectedPacks and not selectedAuctions and not selectedFavorites) or (not selectedPacks and selectedAuctions and not selectedFavorites) or (not selectedPacks and not selectedAuctions and selectedFavorites)}"/>
<c:set var="tagSelectionCount" value="${selectedTags.size()}"/>
<c:set var="timeRangeCount" value="${empty selectedTimeRanges ? 0 : fn:length(selectedTimeRanges)}"/>
<c:set var="locationCount" value="${selectedMunicipality != null ? 1 : 0}"/>
<c:set var="commerceCategoryCount" value="${selectedCommerceCategory != null ? 1 : 0}"/>
<c:set var="selectionCount" value="${tagSelectionCount + (typeFilterApplied ? 1 : 0) + locationCount + commerceCategoryCount + timeRangeCount}"/>
<c:set var="hasSelection" value="${selectionCount gt 0}"/>

    <%-- Filter configuration without form tags --%>

    <%-- MOBILE TRIGGER BUTTON (hidden on desktop) --%>
    <div class="lg:hidden flex items-center gap-3">
        <button type="button" 
                onclick="document.getElementById('mobileSidebar').classList.remove('-translate-x-full'); document.getElementById('mobileSidebarOverlay').classList.remove('hidden');"
                class="inline-flex items-center gap-2 rounded-full px-5 py-2.5 text-sm font-semibold transition-colors duration-200
                       ${hasSelection ? 'bg-primary text-on-primary' : 'bg-surface-container-low text-on-surface hover:bg-surface-container-high'}">
            <span class="material-symbols-outlined text-lg">filter_list</span>
            <span><spring:message code="pack.catalog.filter.label"/></span>
            <c:if test="${hasSelection}">
                <span class="bg-on-primary text-primary text-xs font-bold rounded-full w-5 h-5 inline-flex items-center justify-center">
                    ${selectionCount}
                </span>
            </c:if>
        </button>
    </div>

    <%-- OVERLAY FOR MOBILE --%>
    <div id="mobileSidebarOverlay" 
         onclick="document.getElementById('mobileSidebar').classList.add('-translate-x-full'); this.classList.add('hidden');" 
         class="fixed inset-0 bg-on-surface/40 backdrop-blur-sm z-40 hidden lg:hidden transition-opacity"></div>

    <%-- SIDEBAR --%>
    <aside id="mobileSidebar" class="fixed left-0 top-0 lg:top-[72px] h-full w-64 bg-surface lg:bg-surface-container-low font-body py-8 lg:py-8 z-50 lg:z-40 transition-transform duration-300 -translate-x-full lg:translate-x-0 border-r border-outline-variant/20 lg:border-none shadow-lifted lg:shadow-none flex flex-col hide-scrollbar overflow-y-auto">
        
        <%-- Mobile Header (inside sidebar) --%>
        <div class="px-6 mb-6 flex items-center justify-between lg:hidden border-b border-outline-variant/20 pb-4">
            <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-primary">filter_list</span>
                <h3 class="text-on-surface font-bold text-lg"><spring:message code="pack.catalog.filter.label"/></h3>
            </div>
            <button type="button" 
                    onclick="document.getElementById('mobileSidebar').classList.add('-translate-x-full'); document.getElementById('mobileSidebarOverlay').classList.add('hidden');" 
                    class="text-on-surface-variant hover:text-error transition-colors p-1 bg-surface-container-high rounded-full w-8 h-8 flex items-center justify-center">
                <span class="material-symbols-outlined text-sm">close</span>
            </button>
        </div>

        <%-- Desktop Header --%>
        <div class="px-6 mb-8 hidden lg:block">
            <div class="flex items-center gap-2 mb-1">
                <span class="material-symbols-outlined text-primary-fixed-dim" style="font-variation-settings: 'FILL' 1">filter_list</span>
                <h3 class="text-primary font-headline font-bold"><spring:message code="pack.catalog.filter.refine"/></h3>
            </div>
        </div>

        <div class="px-6 pb-8 mb-8 border-b border-outline-variant/20">
            <h4 class="text-xs font-bold uppercase tracking-wider text-secondary mb-4 flex items-center justify-between">
                <spring:message code="pack.catalog.filter.type"/>
            </h4>
            <div class="flex flex-col gap-3">
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="types" value="packs" onchange="this.form.submit()" ${selectedPacks ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.type.packs"/></span>
                </label>
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="types" value="auctions" onchange="this.form.submit()" ${selectedAuctions ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.type.auctions"/></span>
                </label>
                <sec:authorize access="hasRole('CLIENT')">
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="types" value="favorites" onchange="this.form.submit()" ${selectedFavorites ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.type.favorites"/></span>
                </label>
                </sec:authorize>
            </div>
        </div>

        <div class="px-6 pb-8 mb-8 border-b border-outline-variant/20">
            <h4 class="text-xs font-bold uppercase tracking-wider text-secondary mb-4 flex items-center justify-between">
                <spring:message code="pack.catalog.filter.location"/>
            </h4>
            <div class="relative w-full max-w-full overflow-hidden rounded-full shrink-0">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none shrink-0" style="font-size: 20px;">location_on</span>
                <select name="location"
                        onchange="this.form.submit()"
                        class="w-full min-w-0 pl-11 pr-8 py-2.5 bg-surface-container-low text-sm font-medium text-on-surface rounded-full border border-outline-variant/30 hover:border-outline-variant focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none transition-colors duration-200 shadow-sm appearance-none cursor-pointer truncate"
                        style="text-overflow: ellipsis;">
                    <option value=""><spring:message code="pack.catalog.filter.location.any"/></option>
                    <c:forEach var="muni" items="${availableMunicipalities}">
                        <option value="${muni.name()}" ${selectedMunicipality != null && selectedMunicipality == muni ? 'selected' : ''}>
                            <spring:message code="pack.catalog.filter.location.municipality.${muni.name()}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="px-6 pb-8 mb-8 border-b border-outline-variant/20">
            <h4 class="text-xs font-bold uppercase tracking-wider text-secondary mb-4 flex items-center justify-between">
                <spring:message code="pack.catalog.filter.commerceCategory"/>
            </h4>
            <div class="relative w-full max-w-full overflow-hidden rounded-full shrink-0">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none shrink-0" style="font-size: 20px;">storefront</span>
                <select name="commerceCategory"
                        onchange="this.form.submit()"
                        class="w-full min-w-0 pl-11 pr-8 py-2.5 bg-surface-container-low text-sm font-medium text-on-surface rounded-full border border-outline-variant/30 hover:border-outline-variant focus:border-primary focus:ring-1 focus:ring-primary focus:outline-none transition-colors duration-200 shadow-sm appearance-none cursor-pointer truncate"
                        style="text-overflow: ellipsis;">
                    <option value=""><spring:message code="pack.catalog.filter.commerceCategory.any"/></option>
                    <c:forEach var="cat" items="${availableCommerceCategories}">
                        <option value="${cat.name()}" ${selectedCommerceCategory != null && selectedCommerceCategory == cat ? 'selected' : ''}>
                            <spring:message code="commerce.category.${cat.name()}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="px-6 pb-8 mb-8 border-b border-outline-variant/20">
            <h4 class="text-xs font-bold uppercase tracking-wider text-secondary mb-4 flex items-center justify-between">
                <spring:message code="pack.catalog.filter.time"/>
            </h4>
            <div class="flex flex-col gap-3">
                <%-- Morning --%>
                <c:set var="morningSelected" value="false"/>
                <c:forEach var="tr" items="${selectedTimeRanges}">
                    <c:if test="${tr == 'morning'}"><c:set var="morningSelected" value="true"/></c:if>
                </c:forEach>
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="timeRange" value="morning" onchange="this.form.submit()" ${morningSelected ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.time.morning"/></span>
                </label>
                <%-- Afternoon --%>
                <c:set var="afternoonSelected" value="false"/>
                <c:forEach var="tr" items="${selectedTimeRanges}">
                    <c:if test="${tr == 'afternoon'}"><c:set var="afternoonSelected" value="true"/></c:if>
                </c:forEach>
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="timeRange" value="afternoon" onchange="this.form.submit()" ${afternoonSelected ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.time.afternoon"/></span>
                </label>
                <%-- Evening --%>
                <c:set var="eveningSelected" value="false"/>
                <c:forEach var="tr" items="${selectedTimeRanges}">
                    <c:if test="${tr == 'evening'}"><c:set var="eveningSelected" value="true"/></c:if>
                </c:forEach>
                <label class="cursor-pointer group flex items-center gap-3 w-fit">
                    <div class="relative flex items-center justify-center">
                        <input type="checkbox" name="timeRange" value="evening" onchange="this.form.submit()" ${eveningSelected ? 'checked' : ''} class="peer appearance-none w-5 h-5 border border-outline-variant rounded bg-surface-container-low checked:bg-primary checked:border-primary transition-colors cursor-pointer shadow-sm" />
                        <span class="material-symbols-outlined absolute text-on-primary text-[16px] opacity-0 peer-checked:opacity-100 pointer-events-none transition-opacity">check</span>
                    </div>
                    <span class="text-sm font-medium text-on-surface-variant group-hover:text-on-surface transition-colors"><spring:message code="pack.catalog.filter.time.evening"/></span>
                </label>
            </div>
        </div>

        <div class="px-6 pb-24">
            <h4 class="text-xs font-bold uppercase tracking-wider text-secondary mb-4 flex items-center justify-between">
                <spring:message code="pack.catalog.filter.tags"/>
                <c:if test="${tagSelectionCount gt 0}">
                    <span class="bg-secondary text-on-secondary text-[10px] rounded-full w-4 h-4 inline-flex items-center justify-center">
                        ${tagSelectionCount}
                    </span>
                </c:if>
            </h4>
            
            <div class="flex flex-wrap gap-2">
                <%-- Checkboxes as pill chips --%>
                <c:forEach var="tag" items="${availableTags}">
                    <c:set var="isSelected" value="false"/>
                    <c:forEach var="sel" items="${selectedTags}">
                        <c:if test="${sel == tag}"><c:set var="isSelected" value="true"/></c:if>
                    </c:forEach>

                    <input type="checkbox" name="tags" value="${tag.name()}" id="tag-${tag.name()}" class="hidden" onchange="this.form.submit()" ${isSelected ? 'checked' : ''}/>
                    
                    <label for="tag-${tag.name()}" 
                           class="cursor-pointer inline-flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-medium transition-colors duration-200 border border-transparent
                                  ${isSelected ? 'bg-secondary text-on-secondary shadow-sm font-bold' : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'}">
                        <c:if test="${isSelected}">
                            <span class="material-symbols-outlined text-[18px]">check</span>
                        </c:if>
                        <spring:message code="pack.tag.${tag.name()}"/>
                    </label>
                </c:forEach>
            </div>

            <%-- Clear all filters button --%>
            <c:if test="${hasSelection}">
                <c:url var="clearUrl" value="${baseUrl}">
                    <c:if test="${catalogMode eq 'COMMERCES'}"><c:param name="types" value="commerces"/></c:if>
                    <c:if test="${not empty searchQuery}"><c:param name="q" value="${searchQuery}"/></c:if>
                    <c:if test="${catalogMode ne 'AUCTIONS' and not empty currentSort}"><c:param name="sort" value="${currentSort.name()}"/></c:if>
                    <c:if test="${catalogMode eq 'AUCTIONS' and not empty currentAuctionSort}"><c:param name="auctionSort" value="${currentAuctionSort.name()}"/></c:if>
                </c:url>
                <div class="mt-8 border-t border-outline-variant/20 pt-6">
                    <a href="${clearUrl}" 
                       class="w-full inline-flex justify-center items-center py-2.5 text-xs font-bold text-secondary hover:bg-surface-container-high hover:text-on-surface rounded-md transition-colors uppercase tracking-widest text-center focus:outline-none">
                        <spring:message code="pack.catalog.filter.clearAll"/>
                    </a>
                </div>
            </c:if>
        </div>
    </aside>

