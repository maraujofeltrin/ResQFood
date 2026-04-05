<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>

<c:set var="statusCode" value="${requestScope['javax.servlet.error.status_code']}" />

<c:choose>
    <c:when test="${statusCode == 400}">
        <c:set var="errorTitle" value="Solicitud inválida" />
        <c:set var="errorMessage" value="La solicitud enviada no es válida." />
    </c:when>
    <c:when test="${statusCode == 403}">
        <c:set var="errorTitle" value="Acceso denegado" />
        <c:set var="errorMessage" value="No tenés permisos para acceder a este recurso." />
    </c:when>
    <c:when test="${statusCode == 404}">
        <c:set var="errorTitle" value="Página no encontrada" />
        <c:set var="errorMessage" value="La página que estás buscando no existe o ha sido movida." />
    </c:when>
    <c:when test="${statusCode == 500}">
        <c:set var="errorTitle" value="Error interno" />
        <c:set var="errorMessage" value="Ocurrió un error inesperado. Intentá de nuevo más tarde." />
    </c:when>
    <c:otherwise>
        <c:set var="errorTitle" value="Error inesperado" />
        <c:set var="errorMessage" value="Algo salió mal." />
    </c:otherwise>
</c:choose>

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

<paw:layout title="${errorTitle} - Error ${statusCode}">
  <div class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full ${blobClasses} blur-3xl"></div>
  <div class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-appPrimary/20 blur-3xl"></div>
  <main class="relative mx-auto flex min-h-screen w-full max-w-3xl items-center px-5 py-10 sm:px-8">
    <section class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10">
      <div class="mb-6 inline-flex items-center gap-2 rounded-full border ${badgeClasses} px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em]">
        Error ${statusCode}
      </div>
      <h1 class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl">
        ${errorTitle}
      </h1>
      <p class="mt-5 max-w-2xl text-base leading-relaxed text-appMuted sm:text-lg">
        ${errorMessage}
      </p>
      <div class="mt-8 flex flex-wrap items-center gap-3">
        <a href="${pageContext.request.contextPath}/" class="inline-flex items-center justify-center rounded-full bg-appPrimary px-6 py-3 font-headline text-sm font-bold text-white transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-appPrimary/25">
          Volver al inicio
        </a>
      </div>
    </section>
  </main>
</paw:layout>
