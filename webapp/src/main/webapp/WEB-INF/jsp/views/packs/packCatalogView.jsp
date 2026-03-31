<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="en">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>${pageTitle != null ? pageTitle : 'The Living Pantry | Explore & Rescue'}</title>
    <!-- CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <!-- Material Symbols -->
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script id="tailwind-config">
        tailwind.config = {
          darkMode: "class",
          theme: {
            extend: {
              colors: {
                "surface-container-lowest": "#ffffff",
                "primary-fixed": "#dce1ff",
                "surface-variant": "#e3e1e7",
                "on-tertiary-container": "#e3a464",
                "secondary-fixed": "#dce1ff",
                "on-secondary-fixed": "#141a31",
                "surface-bright": "#fbf8fe",
                "outline-variant": "#c5c5d1",
                "surface-dim": "#dbd9df",
                "on-tertiary-fixed": "#2c1600",
                "on-primary": "#ffffff",
                "background": "#fbf8fe",
                "primary-container": "#2e407d",
                "tertiary": "#462600",
                "error-container": "#ffdad6",
                "surface-container-low": "#f5f3f9",
                "inverse-on-surface": "#f2f0f6",
                "primary": "#152965",
                "primary-fixed-dim": "#b6c4ff",
                "surface-tint": "#4a5b9a",
                "tertiary-fixed": "#ffdcbe",
                "surface-container-high": "#e9e7ed",
                "on-tertiary-fixed-variant": "#693c02",
                "tertiary-fixed-dim": "#fcb977",
                "on-primary-fixed": "#001550",
                "secondary": "#575d78",
                "on-primary-fixed-variant": "#314380",
                "surface-container-highest": "#e3e1e7",
                "on-background": "#1b1b20",
                "on-primary-container": "#9daef3",
                "on-tertiary": "#ffffff",
                "on-error-container": "#93000a",
                "secondary-fixed-dim": "#bfc5e4",
                "on-secondary-container": "#5b617c",
                "on-secondary": "#ffffff",
                "surface-container": "#efedf3",
                "outline": "#757681",
                "secondary-container": "#d9defe",
                "on-error": "#ffffff",
                "inverse-surface": "#303035",
                "inverse-primary": "#b6c4ff",
                "on-surface-variant": "#454650",
                "error": "#ba1a1a",
                "on-secondary-fixed-variant": "#40465f",
                "tertiary-container": "#653900",
                "surface": "#fbf8fe",
                "on-surface": "#1b1b20"
              },
              fontFamily: {
                "headline": ["Plus Jakarta Sans"],
                "body": ["Be Vietnam Pro"],
                "label": ["Plus Jakarta Sans"]
              },
              borderRadius: {"DEFAULT": "0.25rem", "lg": "0.5rem", "xl": "0.75rem", "full": "9999px"},
            },
          },
        }
    </script>
</head>
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">
    
    <paw:navbar />

    <!-- Main Content Area -->
    <main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
        <!-- Header Section -->
        <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
                <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">Explore Surrounding Harvests</h1>
                <p class="text-secondary font-body">Rescue surplus delicacies from local merchants and artisans.</p>
            </div>
            <div class="flex flex-col sm:flex-row items-center w-full md:w-auto gap-4">
                <form action="${pageContext.request.contextPath}/packs" method="GET" class="w-full sm:w-auto">
                    <paw:searchBar value="${param.q}" placeholder="Search harvests or merchants..." classes="relative w-full sm:w-80" />
                </form>
                <div class="relative w-full sm:w-64">
                    <select class="w-full appearance-none bg-none bg-surface-container-low px-4 py-3 rounded-xl border-none text-sm font-medium focus:ring-2 focus:ring-primary/20 cursor-pointer text-on-surface">
                        <option>Sort by: Nearest</option>
                        <option>Lowest Price</option>
                    </select>
                    <span class="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-secondary">expand_more</span>
                </div>
            </div>
        </header>

        <c:if test="${empty param.q}">
        <!-- Last Chance (Horizontal Scrolling Section) -->
        <section class="mb-16">
            <div class="flex items-center justify-between mb-6">
                <div class="flex items-center gap-3">
                    <span class="material-symbols-outlined text-error" style="font-variation-settings: 'FILL' 1;">bolt</span>
                    <h2 class="text-2xl font-headline font-bold text-on-surface">Last Chance</h2>
                    <span class="bg-error-container text-on-error-container px-2 py-0.5 rounded text-xs font-bold uppercase tracking-tighter">Expiring Soon</span>
                </div>
                <button class="text-primary font-bold text-sm hover:underline">View All</button>
            </div>
            <div class="flex gap-6 overflow-x-auto hide-scrollbar pb-4 -mx-2 px-2">
                <c:forEach var="pack" items="${packs}">
                    <paw:packCard
                        packId="${pack.id}"
                        title="${pack.title}"
                        subtitle="${pack.description}"
                        imageUrl="https://lh3.googleusercontent.com/aida-public/AB6AXuArnLe6Rzm5wR1Gn1ndEBQkFtdvsycGk6yXpcIerUIaznflMIXuYgcDvinIwcVOlxh20R-9wdIjwM2Tep3kv-CAABzrA7KOVDVB3WO-Z-0UvCQMv7h7dYaWglZz0nePL9rNoZeY1GYGJAg_SmRLvjKPPyeZgQwzpzMX6rLPlanyDrhifniERD5W2pgvDDSDZWxSA__nLF3MQjVnRVqQqjqSD973WhKf7GmF0_YWEbJ2Hh9-T5zpdCNa1_519qydhlgCRSE-s0dbVVgs"
                        price="$${pack.finalPrice}"
                        oldPrice="$${pack.originalPrice}"
                        commerceName="${commerceNames[pack.id]}"
                    />
                </c:forEach>
            </div>
        </section>
        </c:if>

        <!-- Main Grid: All Available Packs -->
        <section>
            <div class="flex items-center gap-3 mb-8">
                <h2 class="text-2xl font-headline font-bold text-on-surface">
                    <c:choose>
                        <c:when test="${not empty param.q}">Search Results for "<c:out value="${param.q}"/>"</c:when>
                        <c:otherwise>All Available Packs</c:otherwise>
                    </c:choose>
                </h2>
                <div class="h-[1px] flex-grow bg-zinc-200"></div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                <c:forEach var="pack" items="${packs}">
                    <paw:packCard
                        packId="${pack.id}"
                        title="${pack.title}"
                        subtitle="${pack.description}"
                        imageUrl="https://lh3.googleusercontent.com/aida-public/AB6AXuDb9hqJJAJNKmO3vDzg7EtSwBaD2qDwByCk6_I-bar41vMvOr6ClV2eSjSKxqDojQWHI3eO8zB1BKkl1ntlGvi8EPZkbXBgzSMu9RiO7poHlFUWWEtzs2P9dfXj4foOTOoEKcnfHrLmCVCpUzdxvrhdZY2EOe0lyz4EURrPh7ee3TGa91znbF11iBDn0K7YO13wkdfJVec0vZk1h0jWNtouqj8Agx98aCT_Kuja_RcUDd3H-EaFaamyPYAagjr_yRloaXoZRO7aLVtd"
                        price="$${pack.finalPrice}"
                        oldPrice="$${pack.originalPrice}"
                        commerceName="${commerceNames[pack.id]}"
                    />
                </c:forEach>
            </div>

            <!-- Pagination component replacing hardcoded buttons -->
            <paw:pagination currentPage="1" totalPages="3" baseUrl="#" />
        </section>
    </main>

    <paw:footer />

</body>
</html>
