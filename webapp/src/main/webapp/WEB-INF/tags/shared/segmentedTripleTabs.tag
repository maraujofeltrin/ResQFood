<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="basePath" required="true" type="java.lang.String" %>
<%@ attribute name="currentTab" required="true" type="java.lang.String" %>
<%@ attribute name="tabItemsValue" required="false" type="java.lang.String" %>
<%@ attribute name="tabPacksValue" required="false" type="java.lang.String" %>
<%@ attribute name="tabAuctionsValue" required="false" type="java.lang.String" %>
<%@ attribute name="itemsMessageCode" required="true" type="java.lang.String" %>
<%@ attribute name="packsMessageCode" required="true" type="java.lang.String" %>
<%@ attribute name="auctionsMessageCode" required="true" type="java.lang.String" %>
<%@ attribute name="itemsCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="packsCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="auctionsCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="extraQuery" required="false" type="java.lang.String" %>
<%@ attribute name="marginClass" required="false" type="java.lang.String" %>

<c:set var="tvItems" value="${empty tabItemsValue ? 'items' : tabItemsValue}" />
<c:set var="tabMarginClass" value="${empty marginClass ? 'mb-8' : marginClass}" />
<c:set var="tvPacks" value="${empty tabPacksValue ? 'packs' : tabPacksValue}" />
<c:set var="tvAuctions" value="${empty tabAuctionsValue ? 'auctions' : tabAuctionsValue}" />
<c:set var="xq" value="${empty extraQuery ? '' : extraQuery}" />

<div class="inline-flex items-center bg-surface-variant p-1.5 rounded-full <c:out value='${tabMarginClass}'/> overflow-x-auto">
    <a href="<c:out value='${pageContext.request.contextPath}${basePath}?tab=${tvItems}${xq}'/>"
       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
       ${currentTab == tvItems ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
        <spring:message code="${itemsMessageCode}" /> (${itemsCount})
    </a>
    <a href="<c:out value='${pageContext.request.contextPath}${basePath}?tab=${tvPacks}${xq}'/>"
       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
       ${currentTab == tvPacks ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
        <spring:message code="${packsMessageCode}" /> (${packsCount})
    </a>
    <a href="<c:out value='${pageContext.request.contextPath}${basePath}?tab=${tvAuctions}${xq}'/>"
       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
       ${currentTab == tvAuctions ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
        <spring:message code="${auctionsMessageCode}" /> (${auctionsCount})
    </a>
</div>
