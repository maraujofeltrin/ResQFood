<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="commerceId" required="true" %>
<%@ attribute name="commerceName" required="true" %>
<%@ attribute name="category" required="true" %>
<%@ attribute name="rating" required="false" type="java.lang.Double" %>
<%@ attribute name="imageId" required="false" %>

<c:set var="resolvedAlt" value="${commerceName} logo" />

<a href="${pageContext.request.contextPath}/commerces/${commerceId}" class="bg-surface-container-high rounded-xl p-6 text-center hover:bg-surface-container-highest transition-colors cursor-pointer group text-inherit no-underline flex flex-col h-full">
    <div class="w-20 h-20 bg-surface-container-lowest rounded-full mx-auto mb-4 flex items-center justify-center overflow-hidden border-2 border-outline-variant flex-shrink-0">
        <c:choose>
            <c:when test="${not empty imageId}">
                <img class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                     src="${pageContext.request.contextPath}/images/${imageId}"
                     alt="<c:out value="${resolvedAlt}"/>" />
            </c:when>
            <c:otherwise>
                <span class="material-symbols-outlined text-4xl text-outline-variant">storefront</span>
            </c:otherwise>
        </c:choose>
    </div>
    
    <div class="flex flex-col flex-grow">
        <h4 class="font-bold text-lg mb-1 group-hover:text-primary transition-colors text-on-surface truncate"><c:out value="${commerceName}"/></h4>
        <p class="text-xs text-on-surface-variant uppercase tracking-widest mb-3 line-clamp-1">
            <spring:message code="commerce.category.${category}" text="${category}" />
        </p>
        
        <div class="mt-auto">
            <c:choose>
                <c:when test="${not empty rating && rating > 0}">
                    <div class="flex items-center justify-center gap-1 text-secondary font-bold">
                        <span class="material-symbols-outlined text-base" style="font-variation-settings: 'FILL' 1;">star</span>
                        <c:out value="${rating}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="flex items-center justify-center gap-1 text-outline font-medium text-sm">
                        <spring:message code="commerce.rating.new" text="Nuevo" />
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</a>
