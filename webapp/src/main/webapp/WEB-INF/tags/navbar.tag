<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<nav class="fixed top-0 w-full z-50 bg-surface-container-lowest/80 backdrop-blur-md shadow-soft font-headline antialiased">
  <div class="flex justify-between items-center px-6 py-4 max-w-screen-2xl mx-auto gap-4">
    <div class="flex items-center gap-8 flex-shrink-0">
      <a href="${pageContext.request.contextPath}/" class="text-2xl font-bold tracking-tight text-primary italic"><spring:message code="app.brand"/></a>
      <div class="hidden md:flex gap-6">
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/packs"><spring:message code="layout.nav.explore"/></a>
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="${pageContext.request.contextPath}/commerce"><spring:message code="layout.nav.commercePanel"/></a>
      </div>
    </div>
    
    <div class="flex items-center gap-4">
      <sec:authorize access="!isAuthenticated()">
          <a href="${pageContext.request.contextPath}/login" class="text-on-surface-variant hover:text-primary font-medium transition-colors"><spring:message code="layout.nav.login" text="Iniciar sesión"/></a>
      </sec:authorize>
      <sec:authorize access="isAuthenticated()">
          <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
              <button type="submit" class="text-on-surface-variant hover:text-error transition-colors font-medium">
                <spring:message code="layout.nav.logout" text="Cerrar sesión"/>
              </button>
          </form>
      </sec:authorize>
    </div>
  </div>
</nav>
