<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<c:choose>
  <c:when test="${tokenStatus == 'invalid'}">
    <c:set var="pageTitle" value="Enlace no válido"/>
    <c:set var="blurTopLeft" value="bg-rose-200/40"/>
    <c:set var="blurBottomRight" value="bg-appPrimary/20"/>
    <c:set var="badgeClass" value="border-rose-300/70 bg-rose-50 text-rose-700"/>
    <c:set var="badgeText" value="Enlace inválido"/>
    <c:set var="title" value="Este enlace no es válido"/>
  </c:when>
  <c:when test="${tokenStatus == 'expired'}">
    <c:set var="pageTitle" value="Enlace expirado"/>
    <c:set var="blurTopLeft" value="bg-appPrimary/20"/>
    <c:set var="blurBottomRight" value="bg-primary-container/40"/>
    <c:set var="badgeClass" value="border-appPrimary/20 bg-appPrimarySoft text-appPrimary"/>
    <c:set var="badgeText" value="Estado de enlace"/>
    <c:set var="title" value="Este enlace expiró"/>
  </c:when>
  <c:otherwise>
    <c:set var="pageTitle" value="Enlace ya utilizado"/>
    <c:set var="blurTopLeft" value="bg-appPrimary/20"/>
    <c:set var="blurBottomRight" value="bg-primary-container/30"/>
    <c:set var="badgeClass" value="border-appPrimary/20 bg-appPrimarySoft text-appPrimary"/>
    <c:set var="badgeText" value="Enlace procesado"/>
    <c:set var="title" value="Este enlace ya fue utilizado"/>
  </c:otherwise>
</c:choose>
<paw:reservationLayout title="${pageTitle}">
  <div class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full ${blurTopLeft} blur-3xl"></div>
  <div class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full ${blurBottomRight} blur-3xl"></div>
  <main class="relative mx-auto flex min-h-screen w-full max-w-3xl items-center px-5 py-10 sm:px-8">
    <section class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10">
      <div class="mb-6 inline-flex items-center gap-2 rounded-full border ${badgeClass} px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em]">${badgeText}</div>
      <h1 class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl">${title}</h1>
      <p class="mt-5 max-w-2xl text-base leading-relaxed text-appMuted sm:text-lg">
        <c:choose>
          <c:when test="${tokenStatus == 'invalid'}">
            No encontramos una solicitud de reserva asociada a este enlace, o el tipo de acción no coincide.
          </c:when>
          <c:when test="${tokenStatus == 'expired'}">
            El enlace ha expirado debido a que se ha superado la fecha de validez establecida.
          </c:when>
          <c:otherwise>
            <c:choose>
              <c:when test="${not empty alreadyUsedDetail}"><c:out value="${alreadyUsedDetail}" /></c:when>
              <c:otherwise>El enlace ya no está disponible porque se usó anteriormente.</c:otherwise>
            </c:choose>
          </c:otherwise>
        </c:choose>
      </p>
      <div class="mt-8 flex flex-wrap items-center gap-3">
        <a href="${pageContext.request.contextPath}/" class="inline-flex items-center justify-center rounded-full bg-appPrimary px-6 py-3 font-headline text-sm font-bold text-white transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-appPrimary/25">Volver al inicio</a>
      </div>
    </section>
  </main>
</paw:reservationLayout>
