<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="activeLink" required="true" type="java.lang.String" %>

<aside class="fixed top-0 left-0 h-full w-20 hover:w-48 bg-surface-container-lowest border-r border-outline-variant/20 transition-[width] duration-300 ease-in-out z-40 overflow-hidden group pt-24 shadow-soft">
    <nav class="flex flex-col gap-2 px-3">
        <a href="${pageContext.request.contextPath}/commerce" class="flex items-center gap-4 px-3 py-3 rounded-xl transition-colors whitespace-nowrap overflow-hidden ${activeLink == 'dashboard' ? 'bg-primary/10 text-primary' : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'}" title="<spring:message code='commerce.sidebar.dashboard' />">
            <span class="material-symbols-outlined flex-shrink-0">dashboard</span>
            <span class="font-bold font-headline opacity-0 group-hover:opacity-100 transition-opacity duration-300"><spring:message code="commerce.sidebar.dashboard" /></span>
        </a>
        <a href="${pageContext.request.contextPath}/commerce/products" class="flex items-center gap-4 px-3 py-3 rounded-xl transition-colors whitespace-nowrap overflow-hidden ${activeLink == 'products' ? 'bg-primary/10 text-primary' : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'}" title="<spring:message code='commerce.sidebar.products' />">
            <span class="material-symbols-outlined flex-shrink-0">inventory_2</span>
            <span class="font-bold font-headline opacity-0 group-hover:opacity-100 transition-opacity duration-300"><spring:message code="commerce.sidebar.products" /></span>
        </a>
        <a href="${pageContext.request.contextPath}/reservations" class="flex items-center gap-4 px-3 py-3 rounded-xl transition-colors whitespace-nowrap overflow-hidden ${activeLink == 'reservations' ? 'bg-primary/10 text-primary' : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'}" title="<spring:message code='commerce.sidebar.reservations' />">
            <span class="material-symbols-outlined flex-shrink-0">book_online</span>
            <span class="font-bold font-headline opacity-0 group-hover:opacity-100 transition-opacity duration-300"><spring:message code="commerce.sidebar.reservations" /></span>
        </a>
        <a href="${pageContext.request.contextPath}/commerce/metrics" class="flex items-center gap-4 px-3 py-3 rounded-xl transition-colors whitespace-nowrap overflow-hidden ${activeLink == 'metrics' ? 'bg-primary/10 text-primary' : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'}" title="<spring:message code='commerce.sidebar.metrics' />">
            <span class="material-symbols-outlined flex-shrink-0">bar_chart</span>
            <span class="font-bold font-headline opacity-0 group-hover:opacity-100 transition-opacity duration-300"><spring:message code="commerce.sidebar.metrics" /></span>
        </a>
    </nav>
</aside>
