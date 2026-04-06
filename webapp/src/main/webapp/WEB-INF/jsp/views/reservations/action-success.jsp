<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="paw"
tagdir="/WEB-INF/tags" %>
<paw:reservationLayout title="Reserva ${action}">
  <c:set var="successTitle" value="Reserva ${action}" />

  <div
    class="pointer-events-none absolute -top-20 left-1/3 h-72 w-72 -translate-x-1/2 rounded-full bg-primary-container/40 blur-3xl"
  ></div>
  <div
    class="pointer-events-none absolute -bottom-24 -right-10 h-72 w-72 rounded-full bg-appPrimary/20 blur-3xl"
  ></div>

  <main
    class="relative mx-auto flex min-h-screen w-full max-w-3xl items-center px-5 py-10 sm:px-8"
  >
    <section
      class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10"
    >
      <div
        class="mb-6 inline-flex items-center gap-2 rounded-full border border-appSuccess/25 bg-appSuccessSoft px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-appSuccess"
      >
        Operación confirmada
      </div>

      <h1
        class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl"
      >
        <c:out value="${successTitle}" />
      </h1>

      <p
        class="mt-5 max-w-2xl text-base leading-relaxed text-appMuted sm:text-lg"
      >
        La operación se registró correctamente.
      </p>

      <div class="mt-8 flex flex-wrap items-center gap-3">
        <a
          href="${pageContext.request.contextPath}/"
          class="inline-flex items-center justify-center rounded-full bg-appPrimary px-6 py-3 font-headline text-sm font-bold text-white transition hover:brightness-110 focus:outline-none focus:ring-4 focus:ring-appPrimary/25"
        >
          Volver al inicio
        </a>
      </div>
    </section>
  </main>
</paw:reservationLayout>
