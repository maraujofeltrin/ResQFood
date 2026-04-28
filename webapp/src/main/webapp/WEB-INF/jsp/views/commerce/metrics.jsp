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

    <paw:commerceSidebar activeLink="metrics"/>

    <main class="pt-24 pl-24 md:pl-28 pr-6 md:pr-12 pb-20 max-w-7xl mx-auto flex-grow w-full transition-all duration-300">
        <!-- Header Section -->
        <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
                <h1
                    class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">
                    <c:out value="${commerce.commercialName}" />
                </h1>
                <p class="text-secondary font-body">
                    <spring:message code="commerce.sidebar.metrics" />
                </p>
            </div>
        </header>

        <section>
            <div class="bg-surface-container-low rounded-2xl p-8 border border-outline-variant/30 text-center">
                <h2 class="text-2xl font-headline font-bold text-on-surface mb-2">Métricas de Rendimiento</h2>
                <p class="text-on-surface-variant font-body">Próximamente podrás ver aquí gráficos y estadísticas sobre tus ventas y el impacto que generas rescatando alimentos.</p>
            </div>
        </section>
    </main>

    <paw:footer />

</body>

</html>
