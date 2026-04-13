<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
                    <!DOCTYPE html>
                    <html class="light" lang="${pageContext.response.locale.language}">

                    <paw:head titleSuffixCode="commerce.dashboard.pageTitle" />

                    <body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

                        <paw:navbar />

                        <main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
                            <!-- Header Section -->
                            <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                                <div>
                                    <h1
                                        class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                                        <c:out value="${commerce.commercialName}" />
                                    </h1>
                                    <p class="text-secondary font-body">
                                        <spring:message code="commerce.dashboard.subtitle" />
                                    </p>
                                </div>
                                <div class="flex items-center gap-4">
                                    <a href="${pageContext.request.contextPath}/commerce/${commerceId}/create-pack"
                                        class="bg-primary text-on-primary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md">
                                        <span class="material-symbols-outlined font-bold"
                                            style="font-size: 20px;">add</span>
                                        <spring:message code="commerce.dashboard.createPack" />
                                    </a>
                                    <a href="#" class="bg-secondary text-on-secondary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md">
                                        <span class="material-symbols-outlined font-bold"
                                            style="font-size: 20px;">gavel</span>
                                        <spring:message code="commerce.dashboard.createAuction" />
                                    </a>
                                </div>
                            </header>

                            <section>
                                <div class="inline-flex items-center bg-surface-variant p-1.5 rounded-full mb-8 overflow-x-auto">
                                    <a href="${pageContext.request.contextPath}/commerce/${commerceId}?tab=items" 
                                       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
                                       ${currentTab == 'items' ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
                                        <spring:message code="commerce.dashboard.tab.items" />
                                    </a>
                                    <a href="${pageContext.request.contextPath}/commerce/${commerceId}?tab=packs" 
                                       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
                                       ${currentTab == 'packs' ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
                                        <spring:message code="commerce.dashboard.tab.packs" />
                                    </a>
                                    <a href="${pageContext.request.contextPath}/commerce/${commerceId}?tab=auctions" 
                                       class="px-6 py-2.5 text-base font-bold transition-all whitespace-nowrap rounded-full
                                       ${currentTab == 'auctions' ? 'bg-surface text-primary shadow-sm' : 'text-secondary hover:text-primary'}">
                                        <spring:message code="commerce.dashboard.tab.auctions" />
                                    </a>
                                </div>

                                <c:choose>
                                    <c:when test="${empty packs}">
                                        <c:choose>
                                            <c:when test="${currentTab == 'auctions'}">
                                                <spring:message var="emptyTitle" code="commerce.dashboard.empty.auctions.title" />
                                                <spring:message var="emptyDesc" code="commerce.dashboard.empty.auctions.description" />
                                                <paw:packEmptyState icon="gavel" title="${emptyTitle}" description="${emptyDesc}" />
                                            </c:when>
                                            <c:otherwise>
                                                <spring:message var="emptyTitle" code="commerce.dashboard.empty.title" />
                                                <spring:message var="emptyDesc" code="commerce.dashboard.empty.description" />
                                                <paw:packEmptyState icon="inventory_2" title="${emptyTitle}"
                                                    description="${emptyDesc}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="flex items-center gap-3 mb-8">
                                            <h2 class="text-2xl font-headline font-bold text-on-surface">
                                                <c:choose>
                                                    <c:when test="${currentTab == 'items'}"><spring:message code="commerce.dashboard.section.allItems" /></c:when>
                                                    <c:when test="${currentTab == 'packs'}"><spring:message code="commerce.dashboard.section.allPacks" /></c:when>
                                                    <c:otherwise><spring:message code="commerce.dashboard.section.allAuctions" /></c:otherwise>
                                                </c:choose>
                                            </h2>
                                            <div class="h-[1px] flex-grow bg-outline-variant"></div>
                                        </div>

                                        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                                            <c:forEach var="pack" items="${packs}">
                                                <fmt:formatNumber value="${pack.finalPrice}" type="currency"
                                                    currencyCode="ARS" var="formattedPrice" />
                                                <fmt:formatNumber value="${pack.originalPrice}" type="currency"
                                                    currencyCode="ARS" var="formattedOldPrice" />
                                                <spring:message code="commerce.dashboard.pack.stock"
                                                    arguments="${pack.stock}" var="stockLabel" />
                                                <spring:message code="commerce.dashboard.pack.finalPriceLabel"
                                                    var="finalPriceLabel" />
                                                <paw:packCard packId="${pack.id}" title="${pack.title}"
                                                    subtitle="${pack.description}" price="${formattedPrice}"
                                                    oldPrice="${formattedOldPrice}" badgeText="Stock: ${pack.stock}"
                                                    rescueLabel="${finalPriceLabel}"
                                                    manageable="true"
                                                    commerceId="${commerceId}" />
                                            </c:forEach>
                                        </div>

                                        <paw:pagination currentPage="${currentPage}" totalPages="${totalPages}"
                                            baseUrl="${pageContext.request.contextPath}${paginationBaseUrl}" />
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </main>

                        <paw:footer />

                        <!-- Delete Confirmation Modal -->
                        <div id="deleteModal" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4">
                            <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" onclick="closeDeleteModal()"></div>
                            <div class="relative bg-surface rounded-2xl p-8 max-w-md w-full shadow-2xl flex flex-col gap-4 transform transition-all scale-95 opacity-0" id="deleteModalContent">
                                <div class="flex items-center gap-4 text-error mb-2">
                                    <span class="material-symbols-outlined text-4xl">warning</span>
                                    <h3 class="text-2xl font-headline font-bold text-on-surface"><spring:message code="commerce.dashboard.delete.title" /></h3>
                                </div>
                                <p class="text-secondary font-body"><spring:message code="commerce.dashboard.delete.description" /></p>
                                <div class="flex items-center justify-end gap-3 mt-4">
                                    <button type="button" onclick="closeDeleteModal()" class="px-6 py-2 rounded-full font-bold text-secondary hover:bg-surface-variant transition-colors">
                                        <spring:message code="commerce.dashboard.delete.cancel" />
                                    </button>
                                    <form id="deleteForm" method="POST" action="">
                                        <button type="submit" class="bg-error text-on-error px-6 py-2 rounded-full font-bold shadow-md hover:scale-105 transition-transform flex items-center gap-2">
                                            <span class="material-symbols-outlined text-[20px]">delete</span>
                                            <spring:message code="commerce.dashboard.delete.confirm" />
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <script>
                            function openDeleteModal(packId) {
                                const modal = document.getElementById('deleteModal');
                                const content = document.getElementById('deleteModalContent');
                                const form = document.getElementById('deleteForm');
                                
                                form.action = '${pageContext.request.contextPath}/commerce/${commerceId}/delete-pack/' + packId;
                                
                                modal.classList.remove('hidden');
                                // Trigger reflow for animation
                                void modal.offsetWidth;
                                content.classList.remove('scale-95', 'opacity-0');
                                content.classList.add('scale-100', 'opacity-100');
                            }

                            function closeDeleteModal() {
                                const modal = document.getElementById('deleteModal');
                                const content = document.getElementById('deleteModalContent');
                                
                                content.classList.remove('scale-100', 'opacity-100');
                                content.classList.add('scale-95', 'opacity-0');
                                
                                setTimeout(() => {
                                    modal.classList.add('hidden');
                                }, 200); // Wait for transition
                            }
                        </script>

                    </body>

                    </html>