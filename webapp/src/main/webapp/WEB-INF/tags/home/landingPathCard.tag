<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="isCommerce" required="true" type="java.lang.Boolean" %>
<%@ attribute name="tagLabel" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" required="true" %>
<%@ attribute name="btnText" required="true" %>
<%@ attribute name="btnIcon" required="true" %>
<%@ attribute name="btnHref" required="true" %>
<%@ attribute name="bgIcon" required="true" %>

<c:set var="containerClasses" value="bg-surface-container-low text-primary" />
<c:set var="tagClasses" value="bg-white text-primary" />
<c:set var="titleClasses" value="text-primary" />
<c:set var="descClasses" value="text-secondary" />
<c:set var="btnClasses" value="bg-primary text-white" />
<c:set var="bgIconClasses" value="opacity-10 group-hover:opacity-20 transition-opacity translate-x-10 translate-y-10" />

<c:if test="${isCommerce}">
    <c:set var="containerClasses" value="bg-primary text-white" />
    <c:set var="tagClasses" value="bg-primary-container text-on-primary-container" />
    <c:set var="titleClasses" value="text-white" />
    <c:set var="descClasses" value="text-primary-fixed opacity-90" />
    <c:set var="btnClasses" value="bg-white text-primary" />
    <c:set var="bgIconClasses" value="opacity-5 group-hover:opacity-10 transition-opacity translate-x-10 translate-y-10" />
</c:if>

<div class="group relative overflow-hidden rounded-[2.5rem] ${containerClasses} p-10 flex flex-col justify-between h-[500px] transition-all hover:shadow-lg">
    <div class="z-10">
        <span class="inline-block px-4 py-1 rounded-full ${tagClasses} text-xs font-bold tracking-widest uppercase mb-6"><c:out value="${tagLabel}" /></span>
        <h2 class="text-4xl font-bold ${titleClasses} mb-4"><c:out value="${title}" /></h2>
        <p class="text-lg ${descClasses} leading-relaxed mb-8 max-w-sm">
            <c:out value="${description}" />
        </p>
        <ul class="space-y-3 mb-10">
            <jsp:doBody />
        </ul>
    </div>
    <div class="z-10">
        <a href="<c:url value='${btnHref}'/>" class="w-full md:w-auto inline-flex items-center justify-center gap-3 px-8 py-4 rounded-full font-bold text-lg ${btnClasses} hover:scale-105 transition-transform">
            <c:out value="${btnText}" />
            <span class="material-symbols-outlined" data-icon="<c:out value='${btnIcon}'/>"><c:out value="${btnIcon}" /></span>
        </a>
    </div>
    <!-- Background Decoration -->
    <div class="absolute bottom-0 right-0 w-64 h-64 ${bgIconClasses}">
        <span class="material-symbols-outlined text-[15rem]" data-icon="<c:out value='${bgIcon}'/>"><c:out value="${bgIcon}" /></span>
    </div>
</div>
