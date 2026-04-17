<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %> <%@ taglib prefix="paw"
tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <spring:message code="passwordReset.request.title" var="requestTitle" />
  <paw:head title="${requestTitle}" />
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
          <spring:message code="passwordReset.request.title" />
        </h1>
        <c:if test="${not empty param.sent}">
          <div class="bg-primary/10 text-primary rounded-lg p-4 mb-6 text-sm">
            <spring:message code="passwordReset.request.sent" />
          </div>
        </c:if>
        <c:if test="${not empty param.expired}">
          <div
            class="bg-error-container text-on-error-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="passwordReset.request.expired" />
          </div>
        </c:if>
        <form:form
          modelAttribute="passwordResetRequestForm"
          method="post"
          action="/password-reset/request"
          class="space-y-5"
        >
          <div>
            <label
              for="email"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message code="passwordReset.request.label.email"
            /></label>
            <spring:message
              code="passwordReset.request.placeholder.email"
              var="emailPlaceholder"
            />
            <form:input
              type="email"
              id="email"
              path="email"
              required="required"
              class="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${emailPlaceholder}"
            />
            <form:errors
              path="email"
              cssClass="text-error text-sm mt-1 block"
            />
          </div>
          <button
            type="submit"
            class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-4 h-12 flex items-center justify-center"
          >
            <spring:message code="passwordReset.request.submit" />
          </button>
        </form:form>
      </div>
    </main>
    <paw:footer />
  </body>
</html>
