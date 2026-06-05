<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %> <%@ taglib prefix="fn"
uri="http://java.sun.com/jsp/jstl/functions" %>
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
        <!-- Date filter bar with glassmorphism -->
        <div
          class="w-full md:w-auto md:mt-1 bg-surface/80 backdrop-blur-md rounded-2xl p-4 flex flex-col gap-2"
        >
          <form
            id="filterForm"
            method="get"
            action="${pageContext.request.contextPath}/commerce/metrics"
            class="flex items-center gap-3 flex-wrap md:flex-nowrap"
            onsubmit="document.getElementById('daysInput').value = ''"
          >
            <input
              type="hidden"
              id="daysInput"
              name="days"
              value="<c:out value='${days}'/>"
            />
            <div class="flex items-center gap-2">
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
            </div>
            <div class="flex items-center gap-2">
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
            </div>
            <div class="flex items-center gap-3">
              <button
                id="applyBtn"
                type="submit"
                class="px-4 py-2 bg-primary text-on-primary rounded-full whitespace-nowrap"
              >
                <spring:message code="commerce.metrics.filter.apply" />
              </button>
            </div>
          </form>
          <div class="flex flex-col gap-2 md:max-w-md">
            <form:errors
              path="metricsFilterForm.fromDate"
              cssClass="pack-feedback pack-feedback--error pack-form-errors"
              element="div"
            />
            <form:errors
              path="metricsFilterForm.toDate"
              cssClass="pack-feedback pack-feedback--error pack-form-errors"
              element="div"
            />
          </div>
        </div>
      </header>

      <section>
        <!-- KPI Grid -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
                >
                  <span class="material-symbols-outlined">shopping_cart</span>
                </div>
                <div>
                  <div class="text-sm text-secondary">
                    <spring:message
                      code="commerce.metrics.kpi.totalReservations"
                    />
                  </div>
                  <div class="text-3xl font-headline font-bold text-primary">
                    <c:out value="${totalReservations}" />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
                >
                  <span class="material-symbols-outlined">attach_money</span>
                </div>
                <div>
                  <div class="text-sm text-secondary">
                    <spring:message code="commerce.metrics.kpi.totalRevenue" />
                  </div>
                  <div class="text-3xl font-headline font-bold text-primary">
                    $<fmt:formatNumber
                      value="${totalRevenue}"
                      type="number"
                      minFractionDigits="2"
                      maxFractionDigits="2"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div
                  class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
                >
                  <span class="material-symbols-outlined">thumb_up</span>
                </div>
                <div>
                  <div class="text-sm text-secondary">
                    <spring:message
                      code="commerce.metrics.kpi.acceptanceRate"
                    />
                  </div>
                  <div class="text-3xl font-headline font-bold text-primary">
                    <c:out value="${acceptanceRatePercent}" />%
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Bento Grid: Chart + Top Packs -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          <div class="lg:col-span-2 bg-surface-container-low rounded-xl p-6">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-2xl font-headline font-bold">
                <spring:message code="commerce.metrics.trendTitle" />
              </h2>
              <div class="flex items-center gap-3">
                <button
                  id="btn7"
                  type="button"
                  class="px-4 py-2 bg-primary text-on-primary rounded-full"
                >
                  <spring:message code="commerce.metrics.last7" />
                </button>
                <button
                  id="btn30"
                  type="button"
                  class="px-4 py-2 bg-surface-container text-on-surface rounded-full"
                >
                  <spring:message code="commerce.metrics.last30" />
                </button>
              </div>
            </div>
            <canvas id="salesChart" height="140"></canvas>
          </div>
          <div class="bg-surface-container p-6 rounded-xl">
            <h3 class="text-lg font-headline font-bold mb-4">
              <spring:message code="commerce.metrics.topPacks" />
            </h3>
            <div class="space-y-4">
              <c:forEach var="p" items="${topPacks}" varStatus="st">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div class="w-8 text-secondary font-medium">
                      <c:out value="${st.index + 1}" />
                    </div>
                    <div>
                      <div class="font-medium text-on-surface">
                        <c:out value="${p.packTitle}" />
                      </div>
                      <div class="text-sm text-secondary">
                        <c:out value="${p.unitsSold}" />
                        <spring:message code="commerce.metrics.sold" />
                      </div>
                    </div>
                  </div>
                  <a href="${pageContext.request.contextPath}/commerce/edit-pack/${p.packId}"
                     class="text-secondary hover:text-primary p-2 flex items-center justify-center rounded-full hover:bg-surface-container-highest transition-colors"
                     title="<spring:message code='commerce.metrics.editPack' />">
                    <span class="material-symbols-outlined text-[1.25rem]">edit</span>
                  </a>
                </div>
              </c:forEach>
            </div>
            <c:if test="${not empty topPacks}">
              <div class="mt-6 overflow-hidden rounded-xl">
                <c:set var="topImageId" value="${topPacks[0].imageId}" />
                <c:choose>
                  <c:when test="${not empty topImageId}">
                    <img
                      src="${pageContext.request.contextPath}/images/${topImageId}"
                      alt="Top pack"
                      class="w-full h-40 object-cover rounded-xl"
                    />
                  </c:when>
                  <c:otherwise>
                    <img
                      src="${pageContext.request.contextPath}/images/pack-placeholder.svg"
                      alt="Top pack"
                      class="w-full h-40 object-cover rounded-xl"
                    />
                  </c:otherwise>
                </c:choose>
              </div>
            </c:if>
          </div>
        </div>

        <!-- Secondary Metrics Row -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
              >
                <span class="material-symbols-outlined">receipt_long</span>
              </div>
              <div>
                <div class="text-sm text-secondary">
                  <spring:message code="commerce.metrics.averageTicket" />
                </div>
                <div class="text-xl font-headline font-bold text-primary">
                  $<fmt:formatNumber
                    value="${averageTicket}"
                    type="number"
                    minFractionDigits="2"
                    maxFractionDigits="2"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
              >
                <span class="material-symbols-outlined">people</span>
              </div>
              <div>
                <div class="text-sm text-secondary">
                  <spring:message
                    code="commerce.metrics.uniqueClientsSecondary"
                  />
                </div>
                <div class="text-xl font-headline font-bold text-primary">
                  <c:out value="${uniqueClients}" />
                </div>
              </div>
            </div>
          </div>
          <div class="bg-surface-container-lowest rounded-xl p-6">
            <div class="flex items-center gap-3">
              <div
                class="w-10 h-10 rounded-lg bg-surface-container p-2 text-on-surface-variant"
              >
                <span class="material-symbols-outlined">cancel</span>
              </div>
              <div>
                <div class="text-sm text-secondary">
                  <spring:message
                    code="commerce.metrics.canceledReservations"
                  />
                </div>
                <div class="text-xl font-headline font-bold text-primary">
                  <c:out value="${canceledReservations}" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Customer Insights -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 bg-surface-container rounded-xl p-6">
            <h3 class="text-lg font-headline font-bold mb-4">
              <spring:message code="commerce.metrics.topRescuers" />
            </h3>
            <div class="space-y-2">
              <c:forEach var="client" items="${topClients}">
                <div
                  class="flex items-center justify-between p-3 rounded-lg hover:bg-surface-container-high"
                >
                  <div class="flex items-center gap-3">
                    <div
                      class="w-10 h-10 rounded-full bg-primary-fixed flex items-center justify-center text-on-primary-fixed font-medium"
                    >
                      <c:out value="${fn:substring(client.clientName,0,1)}" />
                    </div>
                    <div>
                      <div class="font-medium">
                        <c:out value="${client.clientName}" />
                      </div>
                      <div class="text-sm text-secondary">
                        <c:out value="${client.reservationCount}" />
                        <spring:message code="commerce.metrics.reservations" />
                      </div>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </div>
          </div>
          <div class="bg-surface-container-low rounded-xl p-6">
            <h3 class="text-lg font-headline font-bold mb-4">
              <spring:message code="commerce.metrics.clientRetention" />
            </h3>
            <canvas id="retentionChart" height="160"></canvas>
            <div class="mt-4 flex items-center justify-between">
              <div>
                <div class="text-sm text-secondary">
                  <spring:message code="commerce.metrics.new" />
                </div>
                <div class="font-bold" style="color: #6fdc8a">
                  <c:out value="${clientRetention.newClients}" /> (<c:out
                    value="${clientRetention.newPercent}"
                  />%)
                </div>
              </div>
              <div>
                <div class="text-sm text-secondary">
                  <spring:message code="commerce.metrics.returning" />
                </div>
                <div class="font-bold text-primary">
                  <c:out value="${clientRetention.returningClients}" /> (<c:out
                    value="${clientRetention.returningPercent}"
                  />%)
                </div>
              </div>
            </div>
          </div>
        </div>

        <spring:message
          code="commerce.metrics.reservations"
          var="chartLabelMsg"
        />
        <spring:message
          code="commerce.metrics.new"
          var="retentionNewLabelMsg"
        />
        <spring:message
          code="commerce.metrics.returning"
          var="retentionReturningLabelMsg"
        />
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
          (function () {
            const chartLabel = '<c:out value="${chartLabelMsg}"/>';
            const retentionNewLabel =
              '<c:out value="${retentionNewLabelMsg}"/>';
            const retentionReturningLabel =
              '<c:out value="${retentionReturningLabelMsg}"/>';

            const btn7 = document.getElementById("btn7");
            const btn30 = document.getElementById("btn30");
            const filterForm = document.getElementById("filterForm");

            if (btn7) {
              btn7.addEventListener("click", function (e) {
                e.preventDefault();
                document.getElementById("fromInput").value = "";
                document.getElementById("toInput").value = "";
                document.getElementById("daysInput").value = "7";
                filterForm.submit();
              });
            }
            if (btn30) {
              btn30.addEventListener("click", function (e) {
                e.preventDefault();
                document.getElementById("fromInput").value = "";
                document.getElementById("toInput").value = "";
                document.getElementById("daysInput").value = "30";
                filterForm.submit();
              });
            }

            if (!window.Chart) {
              return;
            }

            let raw = [];
            try {
              raw = JSON.parse(
                '<c:out value="${salesChartJson}" escapeXml="false"/>',
              );
            } catch (e) {
              raw = [];
            }
            const MAX_DAYS = 365;
            let fullData = raw.slice();
            if (fullData.length > MAX_DAYS) {
              fullData = fullData.slice(fullData.length - MAX_DAYS);
            }

            const fromVal = '<c:out value="${from}"/>' || "";
            const toVal = '<c:out value="${to}"/>' || "";
            const daysSelected = Number('<c:out value="${days}"/>') || 0;
            const rangeActive =
              fromVal.trim().length > 0 &&
              toVal.trim().length > 0 &&
              daysSelected === 0;

            function formatDayLabelIso(s) {
              const parts = s.split("-");
              return parts[2] + "/" + parts[1];
            }

            function setActiveButton(activeId) {
              const ids = ["btn7", "btn30"];
              ids.forEach(function (id) {
                const btn = document.getElementById(id);
                if (!btn) {
                  return;
                }
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
              data.forEach(function (d) {
                const key = d.date.slice(0, 7);
                const prev = map.get(key) || { date: key, count: 0 };
                prev.count += Number(d.count);
                map.set(key, prev);
              });
              return Array.from(map.values()).sort(function (a, b) {
                return a.date.localeCompare(b.date);
              });
            }

            function computeAggregated(data) {
              const len = data.length;
              if (len <= 31) return { mode: "daily", data: data };
              if (len <= 92)
                return { mode: "weekly", data: aggregateWeekly(data) };
              return { mode: "monthly", data: aggregateMonthly(data) };
            }

            let initialData;
            let aggregationMode = "daily";
            if (daysSelected > 0) {
              const agg = computeAggregated(fullData);
              initialData = agg.data;
              aggregationMode = agg.mode;
              if (daysSelected === 7) {
                setActiveButton("btn7");
              } else if (daysSelected === 30) {
                setActiveButton("btn30");
              }
            } else if (rangeActive) {
              const agg = computeAggregated(fullData);
              initialData = agg.data;
              aggregationMode = agg.mode;
              setActiveButton(null);
            } else {
              initialData = fullData.slice(Math.max(fullData.length - 7, 0));
              aggregationMode = "daily";
              setActiveButton("btn7");
            }

            const ctx = document.getElementById("salesChart").getContext("2d");
            const salesChart = new Chart(ctx, {
              type: "bar",
              data: {
                labels: initialData.map(function (d) {
                  if (aggregationMode === "monthly") {
                    return d.date.replace("-", "/");
                  }
                  return formatDayLabelIso(d.date);
                }),
                datasets: [
                  {
                    label: chartLabel,
                    data: initialData.map(function (d) {
                      return d.count;
                    }),
                    backgroundColor: "rgba(46,107,30,0.2)",
                    borderColor: "#2e6b1e",
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

            const retCtx = document
              .getElementById("retentionChart")
              .getContext("2d");
            const newClients =
              Number('<c:out value="${clientRetention.newClients}"/>') || 0;
            const returning =
              Number('<c:out value="${clientRetention.returningClients}"/>') ||
              0;
            const retentionChart = new Chart(retCtx, {
              type: "doughnut",
              data: {
                labels: [retentionNewLabel, retentionReturningLabel],
                datasets: [
                  {
                    data: [newClients, returning],
                    backgroundColor: ["#aff496", "#2e6b1e"],
                  },
                ],
              },
              options: { plugins: { legend: { position: "bottom" } } },
            });
          })();
        </script>
      </section>
    </main>

    <paw:footer />
  </body>
</html>
