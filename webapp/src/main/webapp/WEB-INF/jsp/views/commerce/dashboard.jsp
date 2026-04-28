<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
                    <!DOCTYPE html>
                    <html class="light" lang="${pageContext.response.locale.language}">

                    <paw:head titleSuffixCode="commerce.dashboard.pageTitle" />

                    <body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

                        <paw:navbar />

                        <paw:commerceSidebar activeLink="dashboard"/>

                        <main class="pt-24 pl-24 md:pl-28 pr-6 md:pr-12 pb-20 max-w-7xl mx-auto flex-grow w-full transition-all duration-300">

                            <%-- ══ SECTION 1: Hero Banner ══ --%>
                            <section class="relative overflow-hidden rounded-2xl mb-10"
                                     style="background: linear-gradient(135deg, #059669 0%, #022C22 100%);">
                                <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 px-8 py-10 md:px-12 md:py-12 relative z-10">
                                    <div class="flex-1">
                                        <h1 class="text-3xl md:text-4xl font-headline font-extrabold text-white tracking-tight mb-3">
                                            <spring:message code="commerce.dashboard.welcome" arguments="${commerce.commercialName}" />
                                        </h1>
                                        <p class="text-primary-fixed/80 font-body text-base md:text-lg max-w-xl">
                                            <spring:message code="commerce.dashboard.heroSubtitle" />
                                        </p>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/commerce/create-offer"
                                       class="flex items-center gap-3 bg-surface-container-lowest text-primary px-7 py-4 rounded-full text-base font-bold hover:scale-105 transition-transform shadow-lifted whitespace-nowrap">
                                        <span class="material-symbols-outlined font-bold" style="font-size: 22px;">add</span>
                                        <spring:message code="commerce.dashboard.createOffer" />
                                    </a>
                                </div>
                                <%-- Decorative circles --%>
                                <div class="absolute -top-12 -right-12 w-48 h-48 rounded-full bg-white/5"></div>
                                <div class="absolute -bottom-8 -left-8 w-32 h-32 rounded-full bg-white/5"></div>
                            </section>

                            <%-- ══ SECTION 2: Stat Cards ══ --%>
                            <section class="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-12">

                                <%-- Total publications card --%>
                                <div class="bg-surface-container-lowest rounded-2xl p-7 flex flex-col gap-4 shadow-soft group hover:shadow-lifted transition-shadow">
                                    <div class="h-12 w-12 rounded-xl bg-primary-container flex items-center justify-center">
                                        <span class="material-symbols-outlined text-primary" style="font-size: 26px;">inventory_2</span>
                                    </div>
                                    <span class="text-sm font-semibold text-on-surface-variant uppercase tracking-wider">
                                        <spring:message code="commerce.dashboard.stat.totalPublications" />
                                    </span>
                                    <div class="flex items-end justify-between">
                                        <span class="text-4xl font-headline font-extrabold text-on-surface">
                                            <c:out value="${totalPublications}" />
                                        </span>
                                        <a href="${pageContext.request.contextPath}/commerce/products"
                                           class="h-10 w-10 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface-variant hover:bg-primary hover:text-on-primary transition-colors"
                                           title="<spring:message code='commerce.sidebar.products' />">
                                            <span class="material-symbols-outlined" style="font-size: 20px;">arrow_forward</span>
                                        </a>
                                    </div>
                                </div>

                                <%-- Sold today card --%>
                                <div class="bg-surface-container-lowest rounded-2xl p-7 flex flex-col gap-4 shadow-soft group hover:shadow-lifted transition-shadow">
                                    <div class="h-12 w-12 rounded-xl bg-primary-container flex items-center justify-center">
                                        <span class="material-symbols-outlined text-primary" style="font-size: 26px;">point_of_sale</span>
                                    </div>
                                    <span class="text-sm font-semibold text-on-surface-variant uppercase tracking-wider">
                                        <spring:message code="commerce.dashboard.stat.soldToday" />
                                    </span>
                                    <div class="flex items-end justify-between">
                                        <span class="text-4xl font-headline font-extrabold text-on-surface">
                                            <c:out value="${soldToday}" />
                                        </span>
                                        <a href="${pageContext.request.contextPath}/commerce/metrics"
                                           class="h-10 w-10 rounded-full bg-surface-container-high flex items-center justify-center text-on-surface-variant hover:bg-primary hover:text-on-primary transition-colors"
                                           title="<spring:message code='commerce.sidebar.metrics' />">
                                            <span class="material-symbols-outlined" style="font-size: 20px;">arrow_forward</span>
                                        </a>
                                    </div>
                                </div>

                            </section>

                            <%-- ══ SECTION 3: Recent Reservations ══ --%>
                            <section>
                                <paw:reservationHistoryCard packId="${0}" items="${dashboardReservationHistoryItems}" />
                            </section>

                        </main>

                        <paw:footer />

                    </body>

                    </html>