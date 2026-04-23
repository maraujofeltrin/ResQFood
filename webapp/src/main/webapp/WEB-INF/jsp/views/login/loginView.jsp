<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <spring:message code="login.title" var="loginTitle" />
  <paw:head title="${loginTitle}" />
  <body
    class="bg-surface text-on-surface antialiased flex flex-col min-h-screen"
  >
    <paw:navbar />
    <main
      class="pt-24 flex flex-col items-center justify-center flex-grow py-12"
    >
      <div
        class="w-full max-w-md bg-surface-container-lowest rounded-xl shadow-soft p-8 border border-outline-variant"
      >
        <h1
          class="font-headline text-3xl font-bold text-center text-primary mb-6"
        >
          <spring:message code="login.title" />
        </h1>

        <c:if test="${not empty param.error}">
          <div
            class="bg-error-container text-on-error-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="login.error.credentials" />
          </div>
        </c:if>

        <c:if test="${not empty param.pendingVerification}">
          <div
            class="bg-primary-container text-on-primary-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="login.info.pendingVerification" />
          </div>
        </c:if>

        <c:if test="${not empty param.verified}">
          <div
            class="bg-primary-container text-on-primary-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="login.info.verified" />
          </div>
        </c:if>

        <c:if test="${not empty param.passwordReset}">
          <div
            class="bg-primary-container text-on-primary-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="login.info.passwordReset" />
          </div>
        </c:if>

        <form
          action="${pageContext.request.contextPath}/login"
          method="post"
          class="space-y-5"
        >
          <div>
            <label
              for="email"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message code="login.label.email"
            /></label>
            <spring:message
              code="login.placeholder.email"
              var="emailPlaceholder"
            />
            <input
              type="email"
              id="email"
              name="email"
              required
              class="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${emailPlaceholder}"
            />
          </div>

          <div>
            <label
              for="password"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message code="login.label.password"
            /></label>
            <spring:message
              code="login.placeholder.password"
              var="passwordPlaceholder"
            />
            <input
              type="password"
              id="password"
              name="password"
              required
              class="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${passwordPlaceholder}"
            />
            <div class="mt-2 text-right">
              <a
                href="${pageContext.request.contextPath}/password-reset/request"
                class="text-primary text-xs font-medium hover:underline"
              >
                <spring:message code="login.link.forgotPassword" />
              </a>
            </div>
          </div>

          <div class="flex items-center">
            <input
              type="checkbox"
              id="rememberMe"
              name="rememberMe"
              class="h-4 w-4 text-primary focus:ring-primary border-outline rounded cursor-pointer"
            />
            <label
              for="rememberMe"
              class="ml-2 block text-sm text-secondary cursor-pointer"
            >
              <spring:message code="login.label.rememberMe" />
            </label>
          </div>

          <button
            type="submit"
            class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-4 h-12 flex items-center justify-center"
          >
            <spring:message code="login.submit" />
          </button>
        </form>

        <div class="mt-6 text-center text-sm text-secondary">
          <spring:message code="login.prompt.noAccount" />
          <a
            href="${pageContext.request.contextPath}/register"
            class="text-primary font-medium hover:underline"
            ><spring:message code="login.link.register"
          /></a>
        </div>
      </div>
    </main>
    <paw:footer />
  </body>
</html>
