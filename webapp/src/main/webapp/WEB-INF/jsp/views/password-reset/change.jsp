<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <spring:message code="passwordReset.change.title" var="changeTitle" />
  <paw:head title="${changeTitle}" />
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
          <spring:message code="passwordReset.change.title" />
        </h1>
        <form:form
          modelAttribute="passwordResetChangeForm"
          method="post"
          action="${pageContext.request.contextPath}/password-reset/change"
          class="space-y-5"
        >
          <form:hidden path="token" value="${token}" />
          <div>
            <label
              for="newPassword"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message code="passwordReset.change.label.newPassword"
            /></label>
            <spring:message
              code="passwordReset.change.placeholder.newPassword"
              var="newPasswordPlaceholder"
            />
            <form:password
              id="newPassword"
              path="newPassword"
              required="required"
              showPassword="false"
              cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${newPasswordPlaceholder}"
            />
            <form:errors
              path="newPassword"
              cssClass="text-error text-sm mt-1 block"
            />
          </div>
          <div>
            <label
              for="confirmPassword"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message
                code="passwordReset.change.label.confirmPassword"
            /></label>
            <spring:message
              code="passwordReset.change.placeholder.confirmPassword"
              var="confirmPasswordPlaceholder"
            />
            <form:password
              id="confirmPassword"
              path="confirmPassword"
              required="required"
              showPassword="false"
              cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${confirmPasswordPlaceholder}"
            />
            <form:errors
              path="confirmPassword"
              cssClass="text-error text-sm mt-1 block"
            />
          </div>
          <button
            type="submit"
            class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-4 h-12 flex items-center justify-center"
          >
            <spring:message code="passwordReset.change.submit" />
          </button>
        </form:form>
      </div>
    </main>
    <paw:footer />
  </body>
</html>
