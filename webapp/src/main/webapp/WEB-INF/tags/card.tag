<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="imageUrl" required="true" %>
<%@ attribute name="imageAlt" required="false" %>
<%@ attribute name="badgeText" required="false" %>
<%@ attribute name="rescueLabel" required="false" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="oldPrice" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="resolvedAlt" value="${not empty imageAlt ? imageAlt : title}"/>
<c:set var="resolvedRescueLabel" value="${not empty rescueLabel ? rescueLabel : 'Rescue For'}"/>

<div class="bg-surface-container-lowest rounded-xl overflow-hidden group shadow-sm hover:shadow-md transition-shadow flex flex-col h-full min-w-[280px]">
  <div class="relative h-48 sm:h-56 flex-shrink-0 overflow-hidden">
    <img class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" data-alt="<c:out value="${resolvedAlt}"/>" src="<c:out value="${imageUrl}"/>" alt="<c:out value="${resolvedAlt}"/>"/>
    <c:if test="${not empty badgeText}">
      <div class="absolute bottom-3 left-3 flex gap-2">
        <span class="bg-white/90 backdrop-blur text-primary px-3 py-1 rounded-full text-xs font-bold shadow-sm"><c:out value="${badgeText}"/></span>
      </div>
    </c:if>
  </div>
  <div class="p-5 flex flex-col flex-grow">
    <div class="mb-4">
      <h3 class="font-bold text-lg text-on-surface truncate"><c:out value="${title}"/></h3>
      <p class="text-secondary text-sm mt-1 line-clamp-2"><c:out value="${subtitle}"/></p>
    </div>
    <div class="flex items-center justify-between pt-4 border-t border-zinc-50 mt-auto">
      <div>
        <p class="text-zinc-400 text-xs font-bold uppercase tracking-widest mb-1"><c:out value="${resolvedRescueLabel}"/></p>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-primary"><c:out value="${price}"/></span>
          <c:if test="${not empty oldPrice}">
             <span class="text-sm text-zinc-400 line-through"><c:out value="${oldPrice}"/></span>
          </c:if>
        </div>
      </div>
      <button class="bg-primary text-on-primary px-4 py-2 rounded-full text-sm font-bold hover:scale-105 transition-transform">Rescue Now</button>
    </div>
  </div>
</div>