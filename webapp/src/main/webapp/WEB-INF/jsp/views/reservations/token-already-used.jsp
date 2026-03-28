<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="paw"
tagdir="/WEB-INF/tags" %>
<paw:layout title="Enlace ya utilizado">
  <div
    class="pointer-events-none absolute -top-28 -left-20 h-80 w-80 rounded-full bg-appPrimary/20 blur-3xl"
  ></div>
  <div
    class="pointer-events-none absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-indigo-200/30 blur-3xl"
  ></div>

  <main
    class="relative mx-auto flex min-h-screen w-full max-w-3xl items-center px-5 py-10 sm:px-8"
  >
    <section
      class="w-full rounded-[1.75rem] border border-appBorder/70 bg-appCard/90 p-7 shadow-soft backdrop-blur sm:p-10"
    >
      <div
        class="mb-6 inline-flex items-center gap-2 rounded-full border border-appPrimary/20 bg-appPrimarySoft px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-appPrimary"
      >
        Enlace procesado
      </div>

      <h1
        class="font-headline text-3xl font-extrabold leading-tight text-appPrimary sm:text-4xl"
      >
        Este enlace ya fue utilizado
      </h1>

      <p
        class="mt-5 max-w-2xl text-base leading-relaxed text-appMuted sm:text-lg"
      >
        El enlace ya no está disponible porque se usó anteriormente.
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
</paw:layout>
