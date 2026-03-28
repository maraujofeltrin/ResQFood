<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="paw"
tagdir="/WEB-INF/tags" %>
<paw:layout title="Rechazar reserva">
  <div
    class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full bg-rose-300/35 blur-3xl"
  ></div>
  <div
    class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-amber-200/35 blur-3xl"
  ></div>

  <main
    class="relative mx-auto flex min-h-screen w-full max-w-5xl items-center px-5 py-10 sm:px-8"
  >
    <section
      class="w-full rounded-[1.75rem] border border-rose-200/80 bg-white/90 p-7 shadow-soft backdrop-blur sm:p-10"
    >
      <div
        class="mb-6 inline-flex items-center gap-2 rounded-full border border-rose-300/70 bg-rose-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-rose-700"
      >
        Acción irreversible
      </div>

      <h1
        class="font-headline text-3xl font-extrabold leading-tight text-rose-800 sm:text-4xl"
      >
        ¿Está seguro de que desea rechazar la reserva?
      </h1>

      <p class="mt-4 text-base leading-relaxed text-rose-700 sm:text-lg">
        Si confirmás el rechazo, la reserva pasará a estado
        <strong>cancelado</strong> y el enlace dejará de estar disponible.
      </p>

      <div class="mt-7 rounded-2xl border border-rose-200 bg-rose-50/70 p-5">
        <p class="mb-4 font-headline text-lg font-bold text-rose-800">
          Datos de la reserva
        </p>
        <div class="grid gap-3 text-sm sm:grid-cols-2 sm:text-base">
          <p>
            <span class="font-semibold text-rose-700">Id reserva:</span>
            <c:out value="${reservation.id}" />
          </p>
          <p>
            <span class="font-semibold text-rose-700">Cliente (id):</span>
            <c:out value="${reservation.customerId}" />
          </p>
          <p>
            <span class="font-semibold text-rose-700">Pack (id):</span>
            <c:out value="${reservation.packId}" />
          </p>
          <p>
            <span class="font-semibold text-rose-700">Estado actual:</span>
            <c:out value="${reservation.status}" />
          </p>
          <p>
            <span class="font-semibold text-rose-700">Fecha de reserva:</span>
            <c:choose>
              <c:when test="${not empty reservation.reservationDate}">
                <c:out value="${reservation.reservationDate}" />
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
          <p>
            <span class="font-semibold text-rose-700">Precio final:</span>
            <c:choose>
              <c:when test="${not empty reservation.finalPrice}">
                $<c:out value="${reservation.finalPrice}" />
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
          <p>
            <span class="font-semibold text-rose-700">Código de retiro:</span>
            <c:choose>
              <c:when test="${not empty reservation.pickupCode}">
                <c:out value="${reservation.pickupCode}" />
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
          <p>
            <span class="font-semibold text-rose-700"
              >Confirmación de retiro:</span
            >
            <c:choose>
              <c:when test="${not empty reservation.pickupConfirmationDate}">
                <c:out value="${reservation.pickupConfirmationDate}" />
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>

      <form
        action="${pageContext.request.contextPath}/reservations/reject"
        method="post"
        class="mt-8"
      >
        <input type="hidden" name="token" value="<c:out value='${token}'/>" />

        <div class="flex flex-wrap items-center gap-3">
          <button
            type="submit"
            class="inline-flex items-center justify-center rounded-full bg-rose-700 px-6 py-3 font-headline text-sm font-bold text-white transition hover:bg-rose-800 focus:outline-none focus:ring-4 focus:ring-rose-300"
          >
            Sí, rechazar reserva
          </button>
          <a
            href="${pageContext.request.contextPath}/"
            class="inline-flex items-center justify-center rounded-full border border-rose-200 bg-white px-6 py-3 font-headline text-sm font-semibold text-rose-900 transition hover:bg-rose-50 focus:outline-none focus:ring-4 focus:ring-rose-200"
          >
            No, volver al inicio
          </a>
        </div>
      </form>
    </section>
  </main>
</paw:layout>
