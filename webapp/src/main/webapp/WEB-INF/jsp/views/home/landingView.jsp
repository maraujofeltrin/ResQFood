<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<paw:head titleSuffixCode="landing.pageTitle.suffix">
    <style>
      .asymmetric-clip { clip-path: polygon(0 0, 100% 0, 100% 85%, 0% 100%); }
    </style>
</paw:head>
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
    
    <paw:navbar />

    <main class="pt-24 flex-grow">
        <paw:landingHero previewPacks="${previewPacks}" />
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
