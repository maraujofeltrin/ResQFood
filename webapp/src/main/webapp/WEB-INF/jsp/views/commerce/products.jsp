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

    <paw:commerceSidebar activeLink="products" />

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
            Mis productos
          </h1>
          <p class="text-secondary font-body">
            <spring:message code="commerce.dashboard.subtitle" />
          </p>
        </div>
        <div class="flex items-center gap-4">
          <a
            href="${pageContext.request.contextPath}/commerce/create-offer"
            class="bg-primary text-on-primary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md"
          >
            <span
              class="material-symbols-outlined font-bold"
              style="font-size: 20px"
              >add</span
            >
            <spring:message code="commerce.dashboard.createOffer" />
          </a>
        </div>
      </header>

      <c:if test="${dashboardAlertKind eq 'success'}">
        <div class="mb-4">
          <p class="pack-feedback pack-feedback--success" role="alert">
            <c:out value="${dashboardAlertMessage}" />
          </p>
        </div>
      </c:if>

      <c:if test="${param.cancelled}">
        <div class="mb-4">
          <p class="pack-feedback pack-feedback--success" role="alert">
            <spring:message code="commerce.auction.cancel.success" />
          </p>
        </div>
      </c:if>

      <c:if test="${param.cancelFailed}">
        <div class="mb-4">
          <p class="pack-feedback pack-feedback--error" role="alert">
            <spring:message code="commerce.auction.cancel.hasBids" />
          </p>
        </div>
      </c:if>

      <section>
        <paw:segmentedTripleTabs
          basePath="/commerce/products"
          currentTab="${currentTab}"
          itemsMessageCode="commerce.dashboard.tab.items"
          packsMessageCode="commerce.dashboard.tab.packs"
          auctionsMessageCode="commerce.dashboard.tab.auctions"
          itemsCount="${itemsCount}"
          packsCount="${packsCount}"
          auctionsCount="${auctionsCount}"
        />

        <c:choose>
          <c:when test="${empty packs}">
            <c:choose>
              <c:when test="${currentTab == 'auctions'}">
                <spring:message
                  var="emptyTitle"
                  code="commerce.dashboard.empty.auctions.title"
                />
                <spring:message
                  var="emptyDesc"
                  code="commerce.dashboard.empty.auctions.description"
                />
                <paw:packEmptyState
                  icon="gavel"
                  title="${emptyTitle}"
                  description="${emptyDesc}"
                />
              </c:when>
              <c:when test="${currentTab == 'items'}">
                <spring:message
                  var="emptyTitle"
                  code="commerce.dashboard.empty.items.title"
                />
                <spring:message
                  var="emptyDesc"
                  code="commerce.dashboard.empty.items.description"
                />
                <paw:packEmptyState
                  icon="restaurant"
                  title="${emptyTitle}"
                  description="${emptyDesc}"
                />
              </c:when>
              <c:otherwise>
                <spring:message
                  var="emptyTitle"
                  code="commerce.dashboard.empty.title"
                />
                <spring:message
                  var="emptyDesc"
                  code="commerce.dashboard.empty.description"
                />
                <paw:packEmptyState
                  icon="inventory_2"
                  title="${emptyTitle}"
                  description="${emptyDesc}"
                />
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            <div class="flex items-center gap-3 mb-8">
              <h2 class="text-2xl font-headline font-bold text-on-surface">
                <c:choose>
                  <c:when test="${currentTab == 'items'}">
                    <spring:message
                      code="commerce.dashboard.section.allItems"
                    />
                  </c:when>
                  <c:when test="${currentTab == 'packs'}">
                    <spring:message
                      code="commerce.dashboard.section.allPacks"
                    />
                  </c:when>
                  <c:otherwise>
                    <spring:message
                      code="commerce.dashboard.section.allAuctions"
                    />
                  </c:otherwise>
                </c:choose>
              </h2>
              <div class="h-[1px] flex-grow bg-outline-variant"></div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
              <c:forEach var="pack" items="${packs}">
                <fmt:formatNumber
                  value="${pack.finalPrice}"
                  type="currency"
                  currencyCode="ARS"
                  var="formattedPrice"
                />
                <fmt:formatNumber
                  value="${pack.originalPrice}"
                  type="currency"
                  currencyCode="ARS"
                  var="formattedOldPrice"
                />
                <spring:message
                  code="commerce.dashboard.pack.stock"
                  arguments="${pack.stock}"
                  var="stockLabel"
                />
                <spring:message
                  code="commerce.dashboard.pack.finalPriceLabel"
                  var="finalPriceLabel"
                />
                <paw:packCard
                  packId="${pack.id}"
                  imageId="${pack.imageId}"
                  title="${pack.title}"
                  subtitle="${pack.description}"
                  price="${formattedPrice}"
                  oldPrice="${formattedOldPrice}"
                  badgeText="Stock: ${pack.stock}"
                  rescueLabel="${finalPriceLabel}"
                  manageable="${not auctionPackIds.contains(pack.id)}"
                  commerceId="${commerceId}"
                  auction="${auctionPackIds.contains(pack.id)}"
                  auctionId="${auctionPackIds.contains(pack.id) ? packIdToAuctionId[pack.id] : ''}"
                />
              </c:forEach>
            </div>

            <paw:pagination
              currentPage="${currentPage}"
              totalPages="${totalPages}"
              baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}"
            />
          </c:otherwise>
        </c:choose>
      </section>
    </main>

    <paw:footer />

    <paw:deletePackModal />
    <paw:cancelAuctionModal />
  </body>
</html>
