<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="packId" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="imageAlt" required="false" %>
<%@ attribute name="badgeText" required="false" %>
<%@ attribute name="rescueLabel" required="false" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="oldPrice" required="false" %>
<%@ attribute name="commerceName" required="false" %>
<%@ attribute name="commerceId" required="false" %>
<%@ attribute name="manageable" type="java.lang.Boolean" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="resolvedAlt" value="${not empty imageAlt ? imageAlt : title}"/>
<c:set var="resolvedRescueLabel" value="${not empty rescueLabel ? rescueLabel : 'Rescue For'}"/>
<c:if test="${empty manageable}">
    <c:set var="manageable" value="false" />
</c:if>

<a href="${pageContext.request.contextPath}/packs/${packId}" class="bg-surface-container-lowest rounded-xl overflow-hidden group shadow-sm hover:shadow-md transition-shadow flex flex-col h-full min-w-[280px] cursor-pointer hover:bg-surface-container-low transition-colors text-inherit no-underline">
  <div class="relative h-48 sm:h-56 flex-shrink-0 overflow-hidden">
    <img class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" data-alt="<c:out value="${resolvedAlt}"/>" src="${pageContext.request.contextPath}/packs/${packId}/image" alt="<c:out value="${resolvedAlt}"/>"/>
    <c:if test="${not empty badgeText}">
      <div class="absolute bottom-3 left-3 flex gap-2">
        <span class="bg-white/90 backdrop-blur text-primary px-3 py-1 rounded-full text-xs font-bold shadow-sm"><c:out value="${badgeText}"/></span>
      </div>
    </c:if>
    <c:if test="${manageable}">
      <div class="absolute top-3 right-3 flex gap-2">
        <button type="button" 
                onclick="event.preventDefault(); event.stopPropagation(); window.location.href='${pageContext.request.contextPath}/commerce/${commerceId}/edit-pack/${packId}';" 
                class="bg-white/90 backdrop-blur text-secondary hover:text-primary p-2 flex items-center justify-center rounded-full shadow-sm hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-[1.25rem]">edit</span>
        </button>
        <button type="button" 
                onclick="event.preventDefault(); event.stopPropagation(); openDeleteModal(${packId});" 
                class="bg-white/90 backdrop-blur text-error hover:text-on-error hover:bg-error p-2 flex items-center justify-center rounded-full shadow-sm hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-[1.25rem]">delete</span>
        </button>
      </div>
    </c:if>
  </div>
  <div class="p-5 flex flex-col flex-grow">
    <div class="mb-4">
      <c:if test="${not empty commerceName}">
          <div class="flex items-center gap-1 mb-1 text-secondary text-sm font-medium">
             <span class="material-symbols-outlined text-[1rem]">storefront</span>
             <c:out value="${commerceName}"/>
          </div>
      </c:if>
      <h3 class="font-bold text-lg text-on-surface truncate"><c:out value="${title}"/></h3>
      <p class="text-secondary text-sm mt-1 line-clamp-2 h-10"><c:out value="${subtitle}"/></p>
    </div>
    <div class="flex items-center justify-between pt-4 border-t border-surface-container-low mt-auto">
      <div>
        <p class="text-outline text-xs font-bold uppercase tracking-widest mb-1"><c:out value="${resolvedRescueLabel}"/></p>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-primary"><c:out value="${price}"/></span>
          <c:if test="${not empty oldPrice}">
             <span class="text-sm text-outline line-through"><c:out value="${oldPrice}"/></span>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</a>
