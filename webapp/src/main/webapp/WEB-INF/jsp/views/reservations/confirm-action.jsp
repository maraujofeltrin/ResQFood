<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="paw"
tagdir="/WEB-INF/tags" %>
<paw:layout title="Confirmar aceptación de reserva">
  <div
    class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full bg-appPrimary/20 blur-3xl"
  ></div>
  <div
    class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-sky-300/25 blur-3xl"
  ></div>

  <main
    class="relative mx-auto flex min-h-screen w-full max-w-4xl items-center px-5 py-10 sm:px-8"
  >
    <section
      class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10"
    >
      <div
        class="mb-6 inline-flex items-center gap-2 rounded-full border border-appPrimary/20 bg-appPrimarySoft px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-appPrimary"
      >
        Confirmación de reserva
      </div>

      <h1
        class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl"
      >
        Confirmar aceptación de reserva
      </h1>

      <p class="mt-4 text-base leading-relaxed text-appMuted">
        Revisá los datos antes de confirmar la operación.
      </p>

      <div
        class="mt-7 grid gap-3 rounded-2xl border border-appBorder bg-appSurface p-5 text-sm sm:grid-cols-2 sm:text-base"
      >
        <p>
          <span class="font-semibold text-appPrimary">Pack id:</span>
          <c:out value="${reservation.packId}" />
        </p>
        <p>
          <span class="font-semibold text-appPrimary">Estado actual:</span>
          <c:out value="${reservation.status}" />
        </p>
        <p>
          <span class="font-semibold text-appPrimary">Fecha:</span>
          <c:choose>
            <c:when test="${not empty reservationDateFormatted}">
              <c:out value="${reservationDateFormatted}" />
            </c:when>
            <c:otherwise>—</c:otherwise>
          </c:choose>
        </p>
        <p>
          <span class="font-semibold text-appPrimary">Precio final:</span>
          <c:choose>
            <c:when test="${not empty reservation.finalPrice}">
              $<c:out value="${reservation.finalPrice}" />
            </c:when>
            <c:otherwise>—</c:otherwise>
          </c:choose>
        </p>
      </div>

      <form
        action="${pageContext.request.contextPath}/reservations/${confirmEndpoint}"
        method="post"
        class="mt-8"
      >
        <input type="hidden" name="token" value="<c:out value='${token}'/>" />

        <div class="flex flex-wrap items-center gap-3">
          <button
            type="submit"
            class="inline-flex items-center justify-center rounded-full bg-appPrimary px-6 py-3 font-headline text-sm font-bold text-white transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-appPrimary/25"
          >
            Confirmar
          </button>
          <a
            href="${pageContext.request.contextPath}/"
            class="inline-flex items-center justify-center rounded-full border border-appBorder bg-white px-6 py-3 font-headline text-sm font-semibold text-appText transition hover:bg-appSurface focus:outline-none focus:ring-4 focus:ring-appPrimary/15"
          >
            Cancelar
          </a>
        </div>
      </form>
    </section>
  </main>
</paw:layout>
