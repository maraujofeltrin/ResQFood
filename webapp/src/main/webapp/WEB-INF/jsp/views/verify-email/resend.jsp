<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <spring:message code="emailVerification.expired.title" var="expiredTitle" />
  <paw:head title="${expiredTitle}" />
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
          <spring:message code="emailVerification.expired.title" />
        </h1>
        <c:if test="${not empty param.expired}">
          <div
            class="bg-primary-container text-on-primary-container rounded-lg p-4 mb-6 text-sm"
          >
            <spring:message code="emailVerification.expired.message" />
          </div>
        </c:if>
        <form:form
          modelAttribute="resendForm"
          method="post"
          action="${pageContext.request.contextPath}/verify-email/resend"
          class="space-y-5"
          novalidate="novalidate"
        >
          <div>
            <label
              for="email"
              class="block font-label text-sm font-medium text-secondary mb-1"
              ><spring:message code="emailVerification.expired.label.email"
            /></label>
            <spring:message
              code="emailVerification.expired.placeholder.email"
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
              element="p"
            />
          </div>
          <button
            type="submit"
            class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-4 h-12 flex items-center justify-center"
          >
            <spring:message code="emailVerification.expired.submit" />
          </button>
        </form:form>
      </div>
    </main>
    <paw:footer />
  </body>
</html>
