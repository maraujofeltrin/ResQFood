<%@ tag body-content="scriptless" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="packId" required="true" %>
<%@ attribute name="imageId" required="false" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="true" %>
<%@ attribute name="imageAlt" required="false" %>
<%@ attribute name="badgeText" required="false" %>
<%@ attribute name="commerceName" required="false" %>
<%@ attribute name="manageable" type="java.lang.Boolean" required="false" %>
<%@ attribute name="smallSize" required="false" type="java.lang.Boolean" %>
<%@ attribute name="auction" type="java.lang.Boolean" required="false" %>
<%@ attribute name="auctionId" required="false" %>
<%@ attribute name="auctionActive" type="java.lang.Boolean" required="false" %>
<%@ attribute name="auctionHasBids" type="java.lang.Boolean" required="false" %>
<%@ attribute name="unavailable" type="java.lang.Boolean" required="false" %>
<%@ attribute name="asLink" type="java.lang.Boolean" required="false" %>
<%@ attribute name="participationBadgeCode" required="false" type="java.lang.String" %>
<%@ attribute name="showPriceFooter" type="java.lang.Boolean" required="false" %>
<%@ attribute name="price" required="false" %>
<%@ attribute name="oldPrice" required="false" %>
<%@ attribute name="rescueLabel" required="false" %>
<%@ attribute name="imageOverlay" fragment="true" required="false" %>

<%@ attribute name="badgeError" type="java.lang.Boolean" required="false" %>

<c:if test="${empty manageable}">
    <c:set var="manageable" value="false" />
</c:if>
<c:if test="${empty asLink}">
    <c:set var="asLink" value="true" />
</c:if>
<c:if test="${empty showPriceFooter}">
    <c:set var="showPriceFooter" value="false" />
</c:if>
<c:if test="${empty auction}">
    <c:set var="auction" value="false" />
</c:if>
<c:if test="${empty badgeError}">
    <c:set var="badgeError" value="false" />
</c:if>

<c:set var="resolvedAlt" value="${not empty imageAlt ? imageAlt : title}" />
<c:set var="resolvedRescueLabel" value="${not empty rescueLabel ? rescueLabel : ''}" />
<c:set var="minWClass" value="${smallSize ? 'min-w-[220px]' : 'min-w-[280px]'}" />
<c:set var="imgHClass" value="${smallSize ? 'h-36 sm:h-44' : 'h-48 sm:h-56'}" />

<c:choose>
  <c:when test="${badgeError == true}">
    <c:set var="badgeChipClass" value="bg-error-container/95 backdrop-blur text-on-error-container px-3 py-1 rounded-full text-xs font-bold shadow-sm"/>
    <c:choose>
        <c:when test="${auction == true}">
            <c:set var="priceLabelClass" value="text-auction text-xs font-bold uppercase tracking-widest mb-1"/>
            <c:set var="priceValueClass" value="text-2xl font-extrabold text-auction"/>
        </c:when>
        <c:otherwise>
            <c:set var="priceLabelClass" value="text-outline text-xs font-bold uppercase tracking-widest mb-1"/>
            <c:set var="priceValueClass" value="text-2xl font-extrabold text-primary"/>
        </c:otherwise>
    </c:choose>
  </c:when>
  <c:when test="${auction == true}">
    <c:set var="badgeChipClass" value="bg-auction-container/95 backdrop-blur text-on-auction-container px-3 py-1 rounded-full text-xs font-bold shadow-sm"/>
    <c:set var="priceLabelClass" value="text-auction text-xs font-bold uppercase tracking-widest mb-1"/>
    <c:set var="priceValueClass" value="text-2xl font-extrabold text-auction"/>
  </c:when>
  <c:otherwise>
    <c:set var="badgeChipClass" value="bg-white/90 backdrop-blur text-primary px-3 py-1 rounded-full text-xs font-bold shadow-sm"/>
    <c:set var="priceLabelClass" value="text-outline text-xs font-bold uppercase tracking-widest mb-1"/>
    <c:set var="priceValueClass" value="text-2xl font-extrabold text-primary"/>
  </c:otherwise>
</c:choose>

<c:set var="shadowHoverClass" value="${unavailable ? 'shadow-sm' : 'shadow-sm hover:shadow-md'}" />
<c:set var="fadeClass" value="${unavailable ? 'opacity-[0.52] saturate-[0.55] blur-[0.35px]' : ''}" />
<c:choose>
    <c:when test="${asLink && unavailable}">
        <c:set var="wrapClass" value="bg-surface-container-lowest rounded-xl overflow-hidden group ${shadowHoverClass} transition-shadow flex flex-col h-full relative ${minWClass} ${fadeClass} cursor-not-allowed text-inherit no-underline" />
    </c:when>
    <c:when test="${asLink}">
        <c:set var="wrapClass" value="bg-surface-container-lowest rounded-xl overflow-hidden group ${shadowHoverClass} transition-shadow flex flex-col h-full relative ${minWClass} cursor-pointer hover:bg-surface-container-low transition-colors text-inherit no-underline" />
    </c:when>
    <c:otherwise>
        <c:set var="wrapClass" value="bg-surface-container-lowest rounded-xl overflow-hidden group ${shadowHoverClass} transition-shadow flex flex-col h-full relative ${minWClass} ${fadeClass}" />
    </c:otherwise>
</c:choose>

<c:if test="${asLink}">
<c:set var="unavailableLinkTitle" value="" />
<c:if test="${unavailable}">
    <spring:message code="pack.card.unavailable.title" var="unavailableLinkTitle"/>
</c:if>
<a href="${pageContext.request.contextPath}/packs/${packId}" class="${wrapClass}" title="<c:out value="${unavailableLinkTitle}"/>"
    ${unavailable ? 'aria-disabled="true"' : ''}>
</c:if>
<c:if test="${not asLink}">
<article class="${wrapClass}" role="article">
</c:if>

  <div class="relative ${imgHClass} flex-shrink-0 overflow-hidden">
    <c:choose>
      <c:when test="${not empty imageId}">
        <img class="w-full h-full object-cover ${unavailable ? '' : 'group-hover:scale-105'} transition-transform duration-500"
             data-alt="<c:out value="${resolvedAlt}"/>"
             src="${pageContext.request.contextPath}/images/${imageId}"
             alt="<c:out value="${resolvedAlt}"/>" />
      </c:when>
      <c:otherwise>
        <img class="w-full h-full object-cover ${unavailable ? '' : 'group-hover:scale-105'} transition-transform duration-500"
             data-alt="<c:out value="${resolvedAlt}"/>"
             src="${pageContext.request.contextPath}/images/pack-placeholder.svg"
             alt="<c:out value="${resolvedAlt}"/>" />
      </c:otherwise>
    </c:choose>
    <c:if test="${not empty participationBadgeCode}">
      <div class="absolute top-3 left-3 z-10 flex flex-col gap-1.5 items-start">
        <c:choose>
          <c:when test="${participationBadgeCode == 'WINNING'}">
            <span class="${badgeChipClass}"><spring:message code="reservation.my.auction.badge.winning" /></span>
          </c:when>
          <c:when test="${participationBadgeCode == 'OUTBID_ACTIVE'}">
            <span class="bg-error-container/95 backdrop-blur text-on-error-container px-3 py-1 rounded-full text-xs font-bold shadow-sm"><spring:message code="reservation.my.auction.badge.outbidActive" /></span>
          </c:when>
          <c:when test="${participationBadgeCode == 'OUTBID_FINISHED'}">
            <span class="bg-error-container/95 backdrop-blur text-on-error-container px-3 py-1 rounded-full text-xs font-bold shadow-sm"><spring:message code="reservation.my.auction.badge.outbidFinished" /></span>
          </c:when>
        </c:choose>
      </div>
    </c:if>
    <c:if test="${not empty badgeText}">
      <div class="absolute bottom-3 left-3 flex gap-2 z-10">
        <span class="${badgeChipClass}"><c:out value="${badgeText}"/></span>
      </div>
    </c:if>
    <c:if test="${manageable}">
      <div class="absolute top-3 right-3 flex gap-2 z-10">
        <button type="button"
                onclick="event.preventDefault(); event.stopPropagation(); window.location.href='${pageContext.request.contextPath}/commerce/edit-pack/${packId}';"
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
    <c:if test="${auction == true && not empty auctionId && auctionActive == true && auctionHasBids == false}">
      <div class="absolute top-3 right-3 flex gap-2 z-10">
        <button type="button"
                onclick="event.preventDefault(); event.stopPropagation(); openCancelAuctionModal('${auctionId}');"
                class="bg-white/90 backdrop-blur text-error hover:text-on-error hover:bg-error p-2 flex items-center justify-center rounded-full shadow-sm hover:scale-110 transition-transform">
            <span class="material-symbols-outlined text-[1.25rem]">cancel</span>
        </button>
      </div>
    </c:if>
    <jsp:invoke fragment="imageOverlay" />
  </div>
  <c:if test="${auction == true}">
    <div class="h-1 w-full bg-auction flex-shrink-0" aria-hidden="true"></div>
  </c:if>
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
    <jsp:doBody/>
    <c:if test="${showPriceFooter}">
    <div class="flex items-center justify-between pt-4 border-t border-surface-container-low mt-auto">
      <div>
        <p class="${priceLabelClass}"><c:out value="${resolvedRescueLabel}"/></p>
        <div class="flex items-baseline gap-2">
          <span class="${priceValueClass}"><c:out value="${price}"/></span>
          <c:if test="${not empty oldPrice}">
             <span class="text-sm text-outline line-through"><c:out value="${oldPrice}"/></span>
          </c:if>
        </div>
      </div>
    </div>
    </c:if>
  </div>

<c:if test="${asLink}">
</a>
</c:if>
<c:if test="${not asLink}">
</article>
</c:if>
