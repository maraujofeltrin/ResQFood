<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="en">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <title>Dashboard del Comercio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css"/>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>
</head>
<body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">
    
    <!-- Navbar removed temporarily -->

    <main class="pt-24 px-6 md:px-12 pb-20 max-w-7xl mx-auto flex-grow w-full">
        <!-- Header Section -->
        <header class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
                <h1 class="text-4xl md:text-5xl font-headline font-extrabold text-primary tracking-tight mb-2">Mis Packs Publicados</h1>
                <p class="text-secondary font-body">Gestiona los rescates disponibles para tus clientes.</p>
            </div>
            <div class="flex items-center gap-4">
                <a href="${pageContext.request.contextPath}/commerce/create-pack" class="bg-primary text-on-primary px-6 py-3 rounded-full text-base font-bold flex items-center gap-2 hover:scale-105 transition-transform shadow-md">
                    <span class="material-symbols-outlined font-bold" style="font-size: 20px;">add</span>
                    Crear Nuevo Pack
                </a>
            </div>
        </header>

        <section>
            <c:choose>
                <c:when test="${empty packs}">
                    <div class="text-center py-16 bg-surface-container-low rounded-3xl border border-dashed border-outline-variant">
                        <span class="material-symbols-outlined text-5xl mb-4 text-outline" style="font-variation-settings: 'wght' 200;">inventory_2</span>
                        <h3 class="text-2xl font-headline font-bold text-on-surface">No hay packs publicados</h3>
                        <p class="text-secondary mt-2 text-lg">Aún no has publicado ningún paquete sorpresa.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="flex items-center gap-3 mb-8">
                        <h2 class="text-2xl font-headline font-bold text-on-surface">Tus Paquetes</h2>
                        <div class="h-[1px] flex-grow bg-outline-variant"></div>
                    </div>
                    
                    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
                        <c:forEach var="pack" items="${packs}">
                            <paw:packCard
                                packId="${pack.id}"
                                title="${pack.title}"
                                subtitle="${pack.description}"
                                price="$${pack.finalPrice}"
                                oldPrice="$${pack.originalPrice}"
                                badgeText="Stock: ${pack.stock}"
                                rescueLabel="PRECIO FINAL"
                            />
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <!-- Footer removed temporarily -->

</body>
</html>
