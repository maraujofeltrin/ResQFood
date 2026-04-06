<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title><spring:message code="app.brand"/> | <spring:message code="landing.pageTitle.suffix"/></title>
    <!-- CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <!-- Fonts -->
    <link href="https://fonts.googleapis.com" rel="preconnect"/>
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600&display=swap" rel="stylesheet"/>
    <!-- Icons -->
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script id="tailwind-config">
          tailwind.config = {
            darkMode: "class",
            theme: {
              extend: {
                colors: {
                  "surface-container-low": "#f5f3f9",
                  "surface-bright": "#fbf8fe",
                  "primary": "#152965",
                  "on-primary-fixed-variant": "#314380",
                  "error": "#ba1a1a",
                  "primary-fixed-dim": "#b6c4ff",
                  "on-tertiary-fixed-variant": "#693c02",
                  "tertiary-fixed": "#ffdcbe",
                  "surface-container": "#efedf3",
                  "on-secondary-fixed-variant": "#40465f",
                  "outline-variant": "#c5c5d1",
                  "surface-container-highest": "#e3e1e7",
                  "secondary-container": "#d9defe",
                  "primary-fixed": "#dce1ff",
                  "surface-container-lowest": "#ffffff",
                  "on-tertiary-container": "#e3a464",
                  "on-tertiary": "#ffffff",
                  "on-secondary": "#ffffff",
                  "tertiary-fixed-dim": "#fcb977",
                  "on-tertiary-fixed": "#2c1600",
                  "on-primary-fixed": "#001550",
                  "on-background": "#1b1b20",
                  "surface-dim": "#dbd9df",
                  "on-secondary-container": "#5b617c",
                  "primary-container": "#2e407d",
                  "on-primary": "#ffffff",
                  "outline": "#757681",
                  "inverse-primary": "#b6c4ff",
                  "surface-tint": "#4a5b9a",
                  "secondary-fixed": "#dce1ff",
                  "on-error-container": "#93000a",
                  "error-container": "#ffdad6",
                  "surface": "#fbf8fe",
                  "surface-variant": "#e3e1e7",
                  "secondary-fixed-dim": "#bfc5e4",
                  "on-surface-variant": "#454650",
                  "on-primary-container": "#9daef3",
                  "tertiary-container": "#653900",
                  "on-secondary-fixed": "#141a31",
                  "on-error": "#ffffff",
                  "background": "#fbf8fe",
                  "inverse-on-surface": "#f2f0f6",
                  "inverse-surface": "#303035",
                  "tertiary": "#462600",
                  "surface-container-high": "#e9e7ed",
                  "secondary": "#575d78",
                  "on-surface": "#1b1b20"
                },
                fontFamily: {
                  "headline": ["Plus Jakarta Sans"],
                  "body": ["Be Vietnam Pro"],
                  "label": ["Plus Jakarta Sans"]
                },
              },
            },
          }
    </script>
    <style>
      body { font-family: 'Be Vietnam Pro', sans-serif; }
      h1, h2, h3 { font-family: 'Plus Jakarta Sans', sans-serif; }
      .material-symbols-outlined { font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24; }
      .asymmetric-clip { clip-path: polygon(0 0, 100% 0, 100% 85%, 0% 100%); }
    </style>
</head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
    
    <paw:navbar />

    <main class="pt-24 flex-grow">
        
        <paw:landingHero />

        <!-- Dual Path Section -->
        <section class="max-w-7xl mx-auto px-6 py-12">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <spring:message code="landing.path.clients.tag" var="landingPathClientsTag"/>
                <spring:message code="landing.path.clients.title" var="landingPathClientsTitle"/>
                <spring:message code="landing.path.clients.description" var="landingPathClientsDesc"/>
                <spring:message code="landing.path.clients.btn" var="landingPathClientsBtn"/>
                <spring:message code="landing.path.clients.bullet1" var="landingPathClientsBullet1"/>
                <spring:message code="landing.path.clients.bullet2" var="landingPathClientsBullet2"/>
                <!-- Path 1: Rescuers -->
                <paw:landingPathCard
                    isCommerce="false"
                    tagLabel="${landingPathClientsTag}"
                    title="${landingPathClientsTitle}"
                    description="${landingPathClientsDesc}"
                    btnText="${landingPathClientsBtn}"
                    btnIcon="arrow_forward"
                    btnHref="/packs"
                    bgIcon="local_mall">
                    <li class="flex items-center gap-3 text-secondary">
                        <span class="material-symbols-outlined text-primary" data-icon="savings">savings</span>
                        <c:out value="${landingPathClientsBullet1}"/>
                    </li>
                    <li class="flex items-center gap-3 text-secondary">
                        <span class="material-symbols-outlined text-primary" data-icon="location_on">location_on</span>
                        <c:out value="${landingPathClientsBullet2}"/>
                    </li>
                </paw:landingPathCard>

                <spring:message code="landing.path.commerce.tag" var="landingPathCommerceTag"/>
                <spring:message code="landing.path.commerce.title" var="landingPathCommerceTitle"/>
                <spring:message code="landing.path.commerce.description" var="landingPathCommerceDesc"/>
                <spring:message code="landing.path.commerce.btn" var="landingPathCommerceBtn"/>
                <spring:message code="landing.path.commerce.bullet1" var="landingPathCommerceBullet1"/>
                <spring:message code="landing.path.commerce.bullet2" var="landingPathCommerceBullet2"/>
                <!-- Path 2: Partners -->
                <paw:landingPathCard
                    isCommerce="true"
                    tagLabel="${landingPathCommerceTag}"
                    title="${landingPathCommerceTitle}"
                    description="${landingPathCommerceDesc}"
                    btnText="${landingPathCommerceBtn}"
                    btnIcon="dashboard"
                    btnHref="/commerce"
                    bgIcon="storefront">
                    <li class="flex items-center gap-3 text-primary-fixed">
                        <span class="material-symbols-outlined text-on-primary-container" data-icon="trending_up">trending_up</span>
                        <c:out value="${landingPathCommerceBullet1}"/>
                    </li>
                    <li class="flex items-center gap-3 text-primary-fixed">
                        <span class="material-symbols-outlined text-on-primary-container" data-icon="auto_awesome">auto_awesome</span>
                        <c:out value="${landingPathCommerceBullet2}"/>
                    </li>
                </paw:landingPathCard>
            </div>
        </section>

        <!-- Values/Bento Style Section -->
        <section class="max-w-7xl mx-auto px-6 py-24">
            <div class="text-center mb-16">
                <h2 class="text-3xl md:text-5xl font-bold text-primary mb-4 tracking-tight"><spring:message code="landing.values.sectionTitle"/></h2>
                <p class="text-secondary max-w-2xl mx-auto text-lg"><spring:message code="landing.values.sectionSubtitle"/></p>
            </div>
            
            <spring:message code="landing.values.organicGrowth.title" var="landingValueOrganicTitle"/>
            <spring:message code="landing.values.organicGrowth.description" var="landingValueOrganicDesc"/>
            <spring:message code="landing.values.curatedSurplus.title" var="landingValueCuratedTitle"/>
            <spring:message code="landing.values.curatedSurplus.description" var="landingValueCuratedDesc"/>
            <spring:message code="landing.values.transparency.title" var="landingValueTransparencyTitle"/>
            <spring:message code="landing.values.transparency.description" var="landingValueTransparencyDesc"/>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <paw:landingValueCard 
                    title="${landingValueOrganicTitle}" 
                    description="${landingValueOrganicDesc}" 
                    icon="fluid_med" />
                <paw:landingValueCard 
                    title="${landingValueCuratedTitle}" 
                    description="${landingValueCuratedDesc}" 
                    icon="temp_preferences_custom" />
                <paw:landingValueCard 
                    title="${landingValueTransparencyTitle}" 
                    description="${landingValueTransparencyDesc}" 
                    icon="award_star" />
            </div>
        </section>

    </main>

    <paw:footer />

</body>
</html>
