<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="statusCode" value="${requestScope['javax.servlet.error.status_code']}" />
<c:set var="statusCodeDisplay" value="${statusCode}" />
<c:if test="${empty statusCode}">
  <c:set var="statusCodeDisplay" value="" />
</c:if>

<c:choose>
    <c:when test="${statusCode == 400}">
        <spring:message code="error.title.400" var="errorTitle"/>
        <spring:message code="error.message.400" var="errorMessage"/>
    </c:when>
    <c:when test="${statusCode == 403}">
        <spring:message code="error.title.403" var="errorTitle"/>
        <spring:message code="error.message.403" var="errorMessage"/>
    </c:when>
    <c:when test="${statusCode == 404}">
        <spring:message code="error.title.404" var="errorTitle"/>
        <spring:message code="error.message.404" var="errorMessage"/>
    </c:when>
    <c:when test="${statusCode == 500}">
        <spring:message code="error.title.500" var="errorTitle"/>
        <spring:message code="error.message.500" var="errorMessage"/>
    </c:when>
    <c:otherwise>
        <spring:message code="error.title.default" var="errorTitle"/>
        <spring:message code="error.message.default" var="errorMessage"/>
    </c:otherwise>
</c:choose>

<spring:message code="error.page.title" arguments="${errorTitle},${statusCodeDisplay}" var="errorPageTitle"/>

<c:choose>
    <c:when test="${statusCode != null && statusCode >= 500}">
        <c:set var="badgeClasses" value="border-amber-300/70 bg-amber-50 text-amber-700" />
        <c:set var="blobClasses" value="bg-amber-200/40" />
    </c:when>
    <c:otherwise>
        <c:set var="badgeClasses" value="border-rose-300/70 bg-rose-50 text-rose-700" />
        <c:set var="blobClasses" value="bg-rose-200/40" />
    </c:otherwise>
</c:choose>

<paw:reservationLayout title="${errorPageTitle}">
  <div class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full ${blobClasses} blur-3xl"></div>
  <div class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-appPrimary/20 blur-3xl"></div>
  <main class="relative mx-auto flex min-h-screen w-full max-w-3xl items-center px-5 py-10 sm:px-8">
    <section class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10">
      <div class="mb-6 inline-flex items-center gap-2 rounded-full border ${badgeClasses} px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em]">
        <spring:message code="error.badge" arguments="${statusCodeDisplay}"/>
      </div>
      <h1 class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl">
        <c:out value="${errorTitle}"/>
      </h1>
      <p class="mt-5 max-w-2xl text-base leading-relaxed text-appMuted sm:text-lg">
        <c:out value="${errorMessage}"/>
      </p>
      <div class="mt-8 flex flex-wrap items-center gap-3">
        <a href="${pageContext.request.contextPath}/" class="inline-flex items-center justify-center rounded-full bg-appPrimary px-6 py-3 font-headline text-sm font-bold text-white transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-appPrimary/25">
          <spring:message code="error.backHome"/>
        </a>
      </div>
    </section>
  </main>
</paw:reservationLayout>
