<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<%@ attribute name="previewPacks" type="java.util.List" required="true" %>

<c:if test="${not empty previewPacks}">
<section class="max-w-7xl mx-auto px-6 py-16 md:py-24 text-center flex flex-col items-center">
    <h1 class="text-6xl md:text-8xl font-extrabold text-primary mb-6 tracking-tight italic">
        <spring:message code="app.brand"/>
    </h1>
    <p class="text-xl md:text-2xl text-secondary max-w-3xl mb-16 leading-relaxed">
        <spring:message code="landing.hero.intro"/>
    </p>

    <!-- Carousel of packs -->
    <div class="relative w-full overflow-hidden mb-12">
        <div class="flex overflow-x-auto gap-6 pb-6 px-2 hide-scrollbar" id="hero-carousel" style="scroll-behavior: auto;">
            <!-- Duplicate for infinite scrolling illusion -->
            <c:forEach begin="1" end="3">
                <c:forEach var="pack" items="${previewPacks}">
                    <div class="shrink-0 w-56 sm:w-64 text-left">
                        <fmt:formatNumber value="${pack.finalPrice}" type="currency" currencyCode="ARS" var="formattedPrice" />
                        <fmt:formatNumber value="${pack.originalPrice}" type="currency" currencyCode="ARS" var="formattedOldPrice" />
                        <paw:packCard 
                            packId="${pack.id}" 
                            title="${pack.title}" 
                            subtitle="${pack.description}" 
                            price="${formattedPrice}" 
                            oldPrice="${formattedOldPrice}" 
                            smallSize="${true}"
                        />
                    </div>
                </c:forEach>
            </c:forEach>
        </div>
    </div>

    <!-- Explore Button -->
    <a href="${pageContext.request.contextPath}/packs" class="inline-flex items-center gap-2 px-8 py-4 bg-primary text-on-primary rounded-full hover:bg-primary-container hover:text-on-primary-container transition-colors shadow-soft font-semibold text-lg tracking-wide">
        <spring:message code="layout.nav.explore" text="Explorar excedentes"/>
        <span class="material-symbols-outlined" data-icon="arrow_forward">arrow_forward</span>
    </a>
</section>

<script>
    document.addEventListener("DOMContentLoaded", () => {
        const carousel = document.getElementById("hero-carousel");
        if (!carousel) return;

        let autoScrollTimer;
        let isHovered = false;
        let accumulatedScroll = 0;

        const startAutoScroll = () => {
            if (isHovered) return;
            autoScrollTimer = requestAnimationFrame(scrollStep);
        };

        const scrollStep = () => {
            if (isHovered) return;
            
            accumulatedScroll += 0.5; // Ajustar divisor (0.5 = mitad de velocidad)
            if (accumulatedScroll >= 1) {
                const step = Math.floor(accumulatedScroll);
                carousel.scrollLeft += step;
                accumulatedScroll -= step;
            }
            
            const scrollWidth = carousel.scrollWidth;
            const singleCopyWidth = scrollWidth / 3;
            
            if (carousel.scrollLeft >= singleCopyWidth * 2) {
                carousel.scrollLeft -= singleCopyWidth;
            }

            autoScrollTimer = requestAnimationFrame(scrollStep);
        };

        // Delay starting the auto-scroll briefly to allow layout to settle
        setTimeout(startAutoScroll, 500);

        carousel.addEventListener("mouseenter", () => {
            isHovered = true;
            cancelAnimationFrame(autoScrollTimer);
        });

        carousel.addEventListener("mouseleave", () => {
            isHovered = false;
            startAutoScroll();
        });
        
        carousel.addEventListener("touchstart", () => {
            isHovered = true;
            cancelAnimationFrame(autoScrollTimer);
        }, {passive: true});

        carousel.addEventListener("touchend", () => {
            isHovered = false;
            startAutoScroll();
        });
    });
</script>
</c:if>
