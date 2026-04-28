<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <paw:head titleSuffixCode="commerce.dashboard.pageTitle" />

  <body
    class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen"
  >
    <paw:navbar />

    <paw:commerceSidebar activeLink="metrics" />

    <main
      class="pt-24 pl-24 md:pl-28 pr-6 md:pr-12 pb-20 max-w-7xl mx-auto flex-grow w-full transition-all duration-300"
    >
      <!-- Header Section -->
      <header
        class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12"
      >
        <div>
          <h1
            class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2"
          >
            <c:out value="${commerce.commercialName}" />
          </h1>
          <p class="text-secondary font-body">
            <spring:message code="commerce.sidebar.metrics" />
          </p>
        </div>
      </header>

      <section>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div
            class="bg-surface-container-low rounded-2xl p-6 border border-outline-variant/30"
          >
            <div class="text-secondary font-body text-sm">
              <spring:message code="commerce.metrics.kpi.totalReservations" />
            </div>
            <div class="text-3xl font-headline font-bold text-primary">
              <c:out value="${totalReservations}" />
            </div>
          </div>
          <div
            class="bg-surface-container-low rounded-2xl p-6 border border-outline-variant/30"
          >
            <div class="text-secondary font-body text-sm">
              <spring:message code="commerce.metrics.kpi.totalRevenue" />
            </div>
            <div class="text-3xl font-headline font-bold text-primary">
              $<c:out value="${totalRevenue}" />
            </div>
          </div>
          <div
            class="bg-surface-container-low rounded-2xl p-6 border border-outline-variant/30"
          >
            <div class="text-secondary font-body text-sm">
              <spring:message code="commerce.metrics.kpi.bestSellingPack" />
            </div>
            <div class="text-3xl font-headline font-bold text-primary">
              <c:choose>
                <c:when test="${not empty bestSellingPackTitle}">
                  <c:out value="${bestSellingPackTitle}" />
                </c:when>
                <c:otherwise> — </c:otherwise>
              </c:choose>
            </div>
          </div>
          <div
            class="bg-surface-container-low rounded-2xl p-6 border border-outline-variant/30"
          >
            <div class="text-secondary font-body text-sm">
              <spring:message code="commerce.metrics.kpi.acceptanceRate" />
            </div>
            <div class="text-3xl font-headline font-bold text-primary">
              <c:out value="${acceptanceRatePercent}" />%
            </div>
          </div>
        </div>

        <div
          class="bg-surface rounded-2xl p-6 border border-outline-variant/30"
        >
          <h2 class="text-2xl font-headline font-bold text-on-surface mb-4">
            <spring:message code="commerce.metrics.salesByDay" />
          </h2>
          <div class="flex items-center gap-3 mb-4">
            <button
              id="btn7"
              class="px-4 py-2 bg-primary text-on-primary rounded-full"
            >
              <spring:message code="commerce.metrics.last7" />
            </button>
            <button
              id="btn30"
              class="px-4 py-2 bg-surface-container text-on-surface rounded-full"
            >
              <spring:message code="commerce.metrics.last30" />
            </button>
          </div>
          <canvas id="salesChart" height="120"></canvas>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
          (function () {
            const raw = JSON.parse(
              '<c:out value="${salesChartJson}" escapeXml="false"/>',
            );
            const fullData = raw.slice();
            function sliceDays(n) {
              return fullData.slice(Math.max(fullData.length - n, 0));
            }

            const ctx = document.getElementById("salesChart").getContext("2d");
            const initData = sliceDays(7);
            const chart = new Chart(ctx, {
              type: "bar",
              data: {
                labels: initData.map((d) => d.date),
                datasets: [
                  {
                    data: initData.map((d) => d.count),
                    backgroundColor: "rgba(5, 150, 105, 0.1)",
                    borderColor: "#059669",
                    borderWidth: 1,
                  },
                ],
              },
              options: {
                plugins: { legend: { display: false } },
                scales: {
                  x: { type: "category" },
                  y: { beginAtZero: true, ticks: { precision: 0 } },
                },
              },
            });

            function update(n) {
              const data = sliceDays(n);
              chart.data.labels = data.map((d) => d.date);
              chart.data.datasets[0].data = data.map((d) => d.count);
              chart.update();
            }

            document
              .getElementById("btn7")
              .addEventListener("click", function () {
                document
                  .getElementById("btn7")
                  .classList.add("bg-primary", "text-on-primary");
                document
                  .getElementById("btn7")
                  .classList.remove("bg-surface-container", "text-on-surface");
                document
                  .getElementById("btn30")
                  .classList.remove("bg-primary", "text-on-primary");
                document
                  .getElementById("btn30")
                  .classList.add("bg-surface-container", "text-on-surface");
                update(7);
              });
            document
              .getElementById("btn30")
              .addEventListener("click", function () {
                document
                  .getElementById("btn30")
                  .classList.add("bg-primary", "text-on-primary");
                document
                  .getElementById("btn30")
                  .classList.remove("bg-surface-container", "text-on-surface");
                document
                  .getElementById("btn7")
                  .classList.remove("bg-primary", "text-on-primary");
                document
                  .getElementById("btn7")
                  .classList.add("bg-surface-container", "text-on-surface");
                update(30);
              });
          })();
        </script>
      </section>
    </main>

    <paw:footer />
  </body>
</html>
