<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="availableTags" required="true" type="ar.edu.itba.paw.models.PackTag[]" %>
<%@ attribute name="selectedTags" required="true" type="java.util.List" %>
<%@ attribute name="baseUrl" required="true" type="java.lang.String" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="currentSort" required="false" type="ar.edu.itba.paw.models.PackSortOption" %>

<c:set var="hasSelection" value="${not empty selectedTags}"/>
<c:set var="isPanelOpen" value="${hasSelection or param.filterOpen == 'true'}"/>

<div class="flex items-center gap-3 min-w-0">

    <%-- Toggle button: full pill when collapsed, icon-only when expanded --%>
    <button type="button" id="tagFilterToggle"
            onclick="document.getElementById('tagFilterPanel').classList.toggle('hidden');
                     document.getElementById('tagFilterLabelText').classList.toggle('hidden');
                     this.classList.toggle('px-5');
                     this.classList.toggle('px-3')"
            class="flex-shrink-0 inline-flex items-center gap-2 rounded-full py-2.5 text-sm font-semibold transition-colors duration-200
                   ${hasSelection ? 'bg-primary text-on-primary' : 'bg-surface-container-low text-on-surface hover:bg-surface-container-high'} ${isPanelOpen ? 'px-3' : 'px-5'}">
        <span class="material-symbols-outlined text-lg">filter_list</span>
        <span id="tagFilterLabelText" class="${isPanelOpen ? 'hidden' : ''}">
            <spring:message code="pack.catalog.filter.label"/>
        </span>
        <c:if test="${hasSelection}">
            <span class="bg-on-primary text-primary text-xs font-bold rounded-full w-5 h-5 inline-flex items-center justify-center">
                ${selectedTags.size()}
            </span>
        </c:if>
    </button>

    <c:if test="${hasSelection}">
        <c:url var="clearFilterUrl" value="${baseUrl}">
            <c:if test="${not empty searchQuery}">
                <c:param name="q" value="${searchQuery}"/>
            </c:if>
            <c:if test="${not empty currentSort}">
                <c:param name="sort" value="${currentSort.name()}"/>
            </c:if>
        </c:url>
        <a href="${clearFilterUrl}"
           class="flex-shrink-0 w-8 h-8 rounded-full bg-error/10 text-error hover:bg-error/20 inline-flex items-center justify-center transition-colors duration-200"
           title="<spring:message code="pack.catalog.filter.clear"/>">
            <span class="material-symbols-outlined text-base">close</span>
        </a>
    </c:if>

    <%-- Chips panel: hidden by default unless tags are already selected --%>
    <form id="tagFilterPanel"
          action="<c:url value='${baseUrl}'/>" method="GET"
          class="flex items-center gap-2 min-w-0 overflow-x-auto hide-scrollbar ${isPanelOpen ? '' : 'hidden'}">
        
        <input type="hidden" name="filterOpen" value="true"/>

        <c:if test="${not empty searchQuery}">
            <input type="hidden" name="q" value="<c:out value='${searchQuery}'/>"/>
        </c:if>

        <c:if test="${not empty currentSort}">
            <input type="hidden" name="sort" value="<c:out value='${currentSort.name()}'/>"/>
        </c:if>

        <c:forEach var="tag" items="${availableTags}">
            <c:set var="isSelected" value="false"/>
            <c:forEach var="sel" items="${selectedTags}">
                <c:if test="${sel == tag}">
                    <c:set var="isSelected" value="true"/>
                </c:if>
            </c:forEach>

            <input type="checkbox" name="tags" value="${tag.name()}"
                   id="tag-${tag.name()}" class="hidden"
                   onchange="this.form.submit()"
                   ${isSelected == 'true' ? 'checked' : ''}/>

            <label for="tag-${tag.name()}"
                   class="flex-shrink-0 cursor-pointer select-none whitespace-nowrap rounded-full px-4 py-2 text-sm font-medium transition-colors duration-200
                          ${isSelected == 'true'
                              ? 'bg-primary text-on-primary font-bold shadow-sm'
                              : 'bg-surface-container-low text-on-surface hover:bg-surface-container-high'}">
                <c:out value="${tag.displayName}"/>
            </label>
        </c:forEach>

    </form>

</div>

