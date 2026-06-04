<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<nav class="fixed top-0 w-full z-50 bg-surface-container-lowest/80 backdrop-blur-md shadow-soft font-headline antialiased">
  <div class="flex justify-between items-center px-6 py-4 max-w-screen-2xl mx-auto gap-4">
    <div class="flex items-center gap-8 flex-shrink-0">
      <a href="${pageContext.request.contextPath}/" class="text-2xl font-bold tracking-tight text-primary italic"><spring:message code="app.brand"/></a>
      <div class="hidden md:flex gap-6">
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/packs"><spring:message code="layout.nav.explore"/></a>
        <sec:authorize access="hasRole('COMMERCE')">
          <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/commerce"><spring:message code="layout.nav.commercePanel"/></a>
        </sec:authorize>
        <sec:authorize access="hasRole('CLIENT')">
          <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/reservations"><spring:message code="layout.nav.myReservations"/></a>
          <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/favorites"><spring:message code="layout.nav.myFavorites"/></a>
        </sec:authorize>
        <sec:authorize access="hasRole('COMMERCE')">
          <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/reservations"><spring:message code="layout.nav.myReservations"/></a>
        </sec:authorize>
      </div>
    </div>
    
    <div class="flex items-center gap-6">
      <sec:authorize access="!isAuthenticated()">
          <a href="${pageContext.request.contextPath}/login" class="text-on-surface-variant hover:text-primary font-medium transition-colors flex items-center gap-2">
			  <span class="material-symbols-outlined text-[1.25rem]" data-icon="login">login</span>
			  <spring:message code="layout.nav.login" text="Iniciar sesión"/></a>
      </sec:authorize>
      <sec:authorize access="isAuthenticated()">
          <spring:message code="notification.bell.label" var="notificationBellLabel"/>
          <c:set var="onNotificationsPage" value="${fn:endsWith(request.requestURI, '/notifications')}" />
          <a href="${pageContext.request.contextPath}/notifications"
             id="notification-bell"
             class="notification-bell text-primary no-underline"
             aria-label="${notificationBellLabel}"<c:if test="${onNotificationsPage}"> aria-current="page"</c:if>>
              <span class="material-symbols-outlined text-[1.35rem]" data-icon="notifications">notifications</span>
              <c:if test="${navUnreadNotificationCount > 0}">
                  <span class="notification-bell__badge" aria-hidden="true">${navUnreadNotificationCount}</span>
              </c:if>
          </a>
          <spring:message code="layout.nav.profile" var="navProfileTitle"/>
          <spring:message code="profile.avatar.alt" var="navProfileAvatarAlt"/>
          <a href="${pageContext.request.contextPath}/profile"
             class="text-on-surface-variant hover:text-primary transition-colors font-medium flex items-center gap-2 no-underline"
             title="${navProfileTitle}">
              <c:choose>
                <c:when test="${not empty navProfileImageId}">
                  <img src="${pageContext.request.contextPath}/images/${navProfileImageId}"
                       alt="${navProfileAvatarAlt}"
                       class="w-9 h-9 rounded-full object-cover shrink-0 border border-outline-variant/20 shadow-soft"
                       width="36" height="36" loading="lazy"/>
                </c:when>
                <c:otherwise>
                  <span class="material-symbols-outlined text-[1.35rem]" data-icon="account_circle">account_circle</span>
                </c:otherwise>
              </c:choose>
              <span class="sr-only">${navProfileTitle}</span>
          </a>
      </sec:authorize>
    </div>
  </div>
</nav>
