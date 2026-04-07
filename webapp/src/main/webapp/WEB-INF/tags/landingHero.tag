<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<section class="max-w-7xl mx-auto px-6 py-16 md:py-24 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
    <div class="lg:col-span-7">
        <h1 class="text-5xl md:text-7xl font-extrabold text-primary leading-tight tracking-tight mb-8">
            <spring:message code="landing.hero.headline.before"/><br/>
            <span class="text-tertiary-container italic"><spring:message code="landing.hero.headline.accent"/></span> <spring:message code="landing.hero.headline.after"/>
        </h1>
        <p class="text-xl md:text-2xl text-secondary leading-relaxed max-w-2xl mb-12">
            <spring:message code="landing.hero.intro"/>
        </p>
        <div class="flex flex-wrap gap-4">
            <div class="flex items-center gap-2 bg-tertiary-fixed text-on-tertiary-fixed-variant px-4 py-2 rounded-sm font-label text-sm font-semibold">
                <span class="material-symbols-outlined text-lg" data-icon="eco">eco</span>
                <spring:message code="landing.hero.stat.rescued"/>
            </div>
            <div class="flex items-center gap-2 bg-primary-fixed text-on-primary-fixed-variant px-4 py-2 rounded-sm font-label text-sm font-semibold">
                <span class="material-symbols-outlined text-lg" data-icon="volunteer_activism">volunteer_activism</span>
                <spring:message code="landing.hero.stat.meals"/>
            </div>
        </div>
    </div>
    <div class="lg:col-span-5 relative">
        <spring:message code="landing.hero.image.alt" var="landingHeroImageAlt"/>
        <div class="rounded-[2rem] overflow-hidden shadow-2xl asymmetric-clip bg-surface-dim aspect-[4/5]">
            <img alt="<c:out value="${landingHeroImageAlt}"/>" class="w-full h-full object-cover" data-alt="Close-up of vibrant artisan vegetables, sourdough bread, and herbs in a rustic woven basket with soft morning sunlight" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCStVnCm0z9FJ0TAX9E-nMrwVQveWODFW4uNnM3272SwLulI6QdAjSYJewHBRJmacygm8mEonsQIArhQhl25GQ4uYxNbZE5MnsUd0ugQEBqSEHnBlv1DcEfDdowGxTwGWcrHuAWQX4cUoygjEy-DinxoyDhbd4SnIToblhUeTzzEs3aMxXUv8qaQS2UORs7BLScGbLHmn9oZ8UFjopBBhyi6dOTWiAlOeYHRGrMHSNPMPrxpoH0VeiYzU7UfC4LBNK21q_W7LsbPzul"/>
        </div>
        <!-- Floating Decorative Element -->
        <div class="absolute -bottom-6 -left-6 bg-surface-container-lowest p-6 rounded-2xl shadow-xl max-w-[200px]">
            <p class="text-sm font-bold text-primary mb-1"><spring:message code="landing.hero.floating.title"/></p>
            <p class="text-xs text-secondary leading-tight"><spring:message code="landing.hero.floating.text"/></p>
        </div>
    </div>
</section>
