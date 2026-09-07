<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="icon"            required="true" %>
<%@ attribute name="title"           required="true" %>
<%@ attribute name="description"     required="true" %>
<%@ attribute name="clearFiltersUrl" required="false" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="text-center py-16 bg-surface-container-low rounded-3xl border border-dashed border-outline-variant">
    <span class="material-symbols-outlined text-5xl mb-4 text-outline"
          style="font-variation-settings: 'wght' 200;"><c:out value="${icon}" /></span>
    <h3 class="text-2xl font-headline font-bold text-on-surface"><c:out value="${title}" /></h3>
    <p class="text-secondary mt-2 text-lg"><c:out value="${description}" /></p>
    <c:if test="${not empty clearFiltersUrl}">
        <div class="mt-8">
            <a href="${clearFiltersUrl}"
               class="inline-flex items-center gap-2 rounded-full px-6 py-3 text-sm font-semibold
                      bg-surface-container-highest text-on-surface
                      hover:bg-surface-container-high hover:scale-[1.03]
                      transition-all duration-200 shadow-sm">
                <span class="material-symbols-outlined text-base" aria-hidden="true">filter_list_off</span>
                <spring:message code="pack.catalog.filter.resetSearch"/>
            </a>
        </div>
    </c:if>
</div>
