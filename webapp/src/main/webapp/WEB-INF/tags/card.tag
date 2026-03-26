<%@ tag language="java" pageEncoding="UTF-8" %>

<%@ attribute name="title" required="true" %>
<%@ attribute name="rating" required="true" type="java.lang.Double" %>
<%@ attribute name="subtitle" required="true" %>

<%@ attribute name="imageUrl" required="true" %>
<%@ attribute name="imageAlt" required="false" %>

<%@ attribute name="badgeText" required="false" %>

<%@ attribute name="isFavorite" required="false" type="java.lang.Boolean" %>
<%@ attribute name="favoriteAriaLabel" required="false" %>

<%@ attribute name="rescueLabel" required="false" %>
<%@ attribute name="price" required="true" %>
<%@ attribute name="oldPrice" required="false" %>

<%@ attribute name="distanceLabel" required="false" %>
<%@ attribute name="distanceText" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="resolvedAlt" value="${not empty imageAlt ? imageAlt : title}"/>
<c:set var="resolvedRescueLabel" value="${not empty rescueLabel ? rescueLabel : 'Rescue For'}"/>
<c:set var="resolvedDistanceLabel" value="${not empty distanceLabel ? distanceLabel : 'Distance'}"/>
<c:set var="resolvedFavAria" value="${not empty favoriteAriaLabel ? favoriteAriaLabel : 'Agregar a favoritos'}"/>
<c:set var="favFill" value="${isFavorite != null && isFavorite ? 1 : 0}"/>

<div class="deal-card">
  <div class="deal-card__media">
    <img
      class="deal-card__img"
      src="<c:out value='${imageUrl}'/>"
      alt="<c:out value='${resolvedAlt}'/>"
    />

    <c:if test="${not empty badgeText}">
      <div class="deal-card__badges">
        <span class="deal-card__badge"><c:out value="${badgeText}"/></span>
      </div>
    </c:if>

    <button class="deal-card__fav" type="button" aria-label="<c:out value='${resolvedFavAria}'/>">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' ${favFill};">favorite</span>
    </button>
  </div>

  <div class="deal-card__body">
    <div class="deal-card__header">
      <h3 class="deal-card__title"><c:out value="${title}"/></h3>

      <div class="deal-card__rating" aria-label="Rating">
        <span class="material-symbols-outlined deal-card__ratingStar">star</span>
        <span class="deal-card__ratingValue">
          <fmt:formatNumber value="${rating}" minFractionDigits="1" maxFractionDigits="1"/>
        </span>
      </div>
    </div>

    <p class="deal-card__subtitle"><c:out value="${subtitle}"/></p>

    <div class="deal-card__footer">
      <div class="deal-card__col">
        <p class="deal-card__label"><c:out value="${resolvedRescueLabel}"/></p>
        <div class="deal-card__priceRow">
          <span class="deal-card__price"><c:out value="${price}"/></span>
          <c:if test="${not empty oldPrice}">
            <span class="deal-card__oldPrice"><c:out value="${oldPrice}"/></span>
          </c:if>
        </div>
      </div>

      <div class="deal-card__col deal-card__col--right">
        <p class="deal-card__label"><c:out value="${resolvedDistanceLabel}"/></p>
        <p class="deal-card__distance"><c:out value="${distanceText}"/></p>
      </div>
    </div>
  </div>
</div>
