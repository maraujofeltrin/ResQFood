<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="exploreUrl" required="true" type="java.lang.String" %>

<a href="${pageContext.request.contextPath}${exploreUrl}"
   class="group inline-flex items-center gap-1 font-bold text-secondary hover:text-primary transition-colors mb-8">
    <span class="material-symbols-outlined text-xl" aria-hidden="true">arrow_back</span>
    <span class="group-hover:underline"><spring:message code="pack.catalog.backToExplore"/></span>
</a>
