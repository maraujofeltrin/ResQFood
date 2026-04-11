<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="availableSorts" required="true" type="ar.edu.itba.paw.models.PackSortOption[]" %>
<%@ attribute name="currentSort" required="true" type="ar.edu.itba.paw.models.PackSortOption" %>
<%@ attribute name="baseUrl" required="true" type="java.lang.String" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="selectedTags" required="false" type="java.util.List" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<form action="<c:url value='${baseUrl}'/>" method="GET" class="${classes != null ? classes : 'relative inline-flex items-center max-w-full'}">
    <c:if test="${not empty searchQuery}">
        <input type="hidden" name="q" value="<c:out value='${searchQuery}'/>"/>
    </c:if>
    <c:if test="${not empty selectedTags}">
        <c:forEach var="tag" items="${selectedTags}">
            <input type="hidden" name="tags" value="<c:out value='${tag.name()}'/>"/>
        </c:forEach>
    </c:if>

    <div class="inline-flex items-center rounded-full py-1.5 px-3 text-sm font-semibold transition-colors duration-200 bg-surface-container-low text-on-surface hover:bg-surface-container-high relative w-full overflow-hidden shrink-0 lg:shrink w-full">
        <span class="material-symbols-outlined text-base mr-1 pointer-events-none shrink-0">swap_vert</span>
        
        <select name="sort" onchange="this.form.submit()" 
                class="appearance-none bg-transparent outline-none cursor-pointer text-sm font-semibold text-on-surface w-full pr-8 focus:outline-none focus:ring-0 truncate" 
                style="outline: none !important; box-shadow: none !important; border: none !important; text-overflow: ellipsis;">
            <c:forEach var="sortOption" items="${availableSorts}">
                <option value="${sortOption.name()}" ${currentSort == sortOption ? 'selected' : ''}>
                    <spring:message code="pack.sort.${sortOption.name()}"/>
                </option>
            </c:forEach>
        </select>
    </div>
</form>
