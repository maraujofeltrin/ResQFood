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
          <div class="flex items-center justify-between gap-3 mb-4 flex-wrap">
            <div class="flex items-center gap-3">
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

            <form
              id="filterForm"
              method="get"
              action="${pageContext.request.contextPath}/commerce/metrics"
              class="flex items-center gap-3"
              onsubmit="document.getElementById('daysInput').value = ''"
            >
              <input
                type="hidden"
                id="daysInput"
                name="days"
                value="<c:out value='${days}'/>"
              />
              <label class="text-sm text-secondary"
                ><spring:message code="commerce.metrics.filter.from"
              /></label>
              <input
                id="fromInput"
                type="date"
                name="from"
                class="pack-form-control"
                value="<c:out value='${from}'/>"
              />
              <label class="text-sm text-secondary"
                ><spring:message code="commerce.metrics.filter.to"
              /></label>
              <input
                id="toInput"
                type="date"
                name="to"
                class="pack-form-control"
                value="<c:out value='${to}'/>"
              />
              <button
                id="applyBtn"
                type="submit"
                class="px-4 py-2 bg-primary text-on-primary rounded-full"
              >
                <spring:message code="commerce.metrics.filter.apply" />
              </button>
            </form>
          </div>
          <canvas id="salesChart" height="120"></canvas>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
          (function () {
            const raw = JSON.parse(
              '<c:out value="${salesChartJson}" escapeXml="false"/>',
            );
            // limit to maximum 1 year on client as safeguard
            const MAX_DAYS = 365;
            let fullData = raw.slice();
            if (fullData.length > MAX_DAYS) {
              fullData = fullData.slice(fullData.length - MAX_DAYS);
            }

            // detect if a date range or a quick-days selection was provided from server
            const fromVal = '<c:out value="${from}"/>' || "";
            const toVal = '<c:out value="${to}"/>' || "";
            const daysSelected = Number('<c:out value="${days}"/>') || 0;
            const rangeActive =
              fromVal.trim().length > 0 &&
              toVal.trim().length > 0 &&
              daysSelected === 0;

            function formatDayLabelIso(s) {
              // returns DD/MM
              const parts = s.split("-");
              return parts[2] + "/" + parts[1];
            }

            function setActiveButton(activeId) {
              const ids = ["btn7", "btn30"];
              ids.forEach(function (id) {
                const btn = document.getElementById(id);
                const isActive = id === activeId;
                btn.classList.toggle("bg-primary", isActive);
                btn.classList.toggle("text-on-primary", isActive);
                btn.classList.toggle("bg-surface-container", !isActive);
                btn.classList.toggle("text-on-surface", !isActive);
              });
            }

            function aggregateWeekly(data) {
              const res = [];
              for (let i = 0; i < data.length; i += 7) {
                let sum = 0;
                const start = data[i].date;
                for (let j = 0; j < 7 && i + j < data.length; j++) {
                  sum += Number(data[i + j].count);
                }
                res.push({ date: start, count: sum });
              }
              return res;
            }

            function aggregateMonthly(data) {
              const map = new Map();
              data.forEach((d) => {
                const key = d.date.slice(0, 7); // YYYY-MM
                const prev = map.get(key) || { date: key, count: 0 };
                prev.count += Number(d.count);
                map.set(key, prev);
              });
              return Array.from(map.values()).sort((a, b) =>
                a.date.localeCompare(b.date),
              );
            }

            function computeAggregated(data) {
              const len = data.length;
              if (len <= 31) return { mode: "daily", data };
              if (len <= 92)
                return { mode: "weekly", data: aggregateWeekly(data) };
              return { mode: "monthly", data: aggregateMonthly(data) };
            }

            // initial dataset selection
            let initialData;
            let aggregationMode = "daily";
            if (daysSelected > 0) {
              // server returned a quick-days dataset (e.g. ?days=7)
              const agg = computeAggregated(fullData);
              initialData = agg.data;
              aggregationMode = agg.mode;
              // mark selected button visually
              if (daysSelected === 7) {
                setActiveButton("btn7");
              } else if (daysSelected === 30) {
                setActiveButton("btn30");
              }
            } else if (rangeActive) {
              const agg = computeAggregated(fullData);
              initialData = agg.data;
              aggregationMode = agg.mode;
              // leave quick buttons enabled so they can clear the range
              setActiveButton(null);
            } else {
              // default to last 7 days
              initialData = fullData.slice(Math.max(fullData.length - 7, 0));
              aggregationMode = "daily";
              setActiveButton("btn7");
            }

            const ctx = document.getElementById("salesChart").getContext("2d");
            const chart = new Chart(ctx, {
              type: "bar",
              data: {
                labels: initialData.map((d) => {
                  if (aggregationMode === "monthly")
                    return d.date.replace("-", "/");
                  return formatDayLabelIso(d.date);
                }),
                datasets: [
                  {
                    data: initialData.map((d) => d.count),
                    backgroundColor: "rgba(4, 120, 87, 0.2)",
                    borderColor: "#047857",
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

            function updateChartFromArray(arr) {
              chart.data.labels = arr.map((d) =>
                aggregationMode === "monthly"
                  ? d.date.replace("-", "/")
                  : formatDayLabelIso(d.date),
              );
              chart.data.datasets[0].data = arr.map((d) => d.count);
              chart.update();
            }

            document
              .getElementById("btn7")
              .addEventListener("click", function () {
                // selecting quick range clears date inputs and submits the form with days=7
                document.getElementById("fromInput").value = "";
                document.getElementById("toInput").value = "";
                document.getElementById("daysInput").value = "7";
                document.getElementById("filterForm").submit();
              });

            document
              .getElementById("btn30")
              .addEventListener("click", function () {
                document.getElementById("fromInput").value = "";
                document.getElementById("toInput").value = "";
                document.getElementById("daysInput").value = "30";
                document.getElementById("filterForm").submit();
              });

            // When the user clicks Apply, the form submits and page reloads; no extra handling needed.
          })();
        </script>
      </section>
    </main>

    <paw:footer />
  </body>
</html>
