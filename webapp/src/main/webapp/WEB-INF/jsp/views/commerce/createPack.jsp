<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <!DOCTYPE html>
            <html class="light" lang="en">

            <head>
                <meta charset="utf-8" />
                <meta content="width=device-width, initial-scale=1.0" name="viewport" />
                <title>Crear Pack Sorpresa | ResQFood</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css" />
                <link
                    href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap"
                    rel="stylesheet" />
                <link
                    href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
                    rel="stylesheet" />
                <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
                <script src="${pageContext.request.contextPath}/css/tailwind-config.js"></script>
            </head>

            <body class="bg-surface font-body text-on-surface antialiased flex flex-col min-h-screen">

                <!-- Navbar removed temporarily -->

                <main class="pack-detail-main">
                    <div class="flex items-center gap-2 mb-8 text-secondary">
                        <a href="${pageContext.request.contextPath}/commerce"
                            class="hover:underline flex items-center font-bold">
                            <span class="material-symbols-outlined text-xl mr-1">arrow_back</span>
                            Volver al Dashboard
                        </a>
                    </div>

                    <header class="mb-10 text-center md:text-left">
                        <h1 class="pack-detail-title mb-3">Crear Paquete Sorpresa</h1>
                        <p class="text-secondary text-lg">Publica tu excedente para que sea rescatado.</p>
                    </header>

                    <c:if test="${not empty errorMessage}">
                        <div class="pack-feedback pack-feedback--error mb-8 flex items-center gap-2">
                            <span class="material-symbols-outlined">error</span>
                            <c:out value="${errorMessage}" />
                        </div>
                    </c:if>

                    <form:form modelAttribute="createPackForm" action="${pageContext.request.contextPath}/commerce/create-pack" method="post"
                        enctype="multipart/form-data" class="pack-detail-grid" novalidate="novalidate">

                        <!-- Left Column: Form Sections -->
                        <div class="grid col-span-1 md:col-span-8 gap-8">

                            <!-- Identidad del Usuario -->
                            <section class="pack-aside-card">
                                <h2 class="pack-aside-heading flex items-center gap-2">
                                    <span class="material-symbols-outlined text-primary">person</span>
                                    Identidad del Usuario
                                </h2>
                                <p class="text-sm text-secondary">Si ya estás registrado, reusaremos tu local
                                    automáticamente.</p>

                                <div class="grid gap-5 mt-2">
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Email de Acceso</label>
                                        <form:input type="email" path="email" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="tu@email.com" required="required" />
                                        <form:errors path="email" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Nombre del Titular</label>
                                        <form:input type="text" path="name" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="Armando C." required="required" />
                                        <form:errors path="name" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                </div>
                            </section>

                            <!-- Datos del Local -->
                            <section class="pack-aside-card">
                                <h2 class="pack-aside-heading flex items-center gap-2">
                                    <span class="material-symbols-outlined text-primary">store</span>
                                    Datos del Local (Solo nuevos usuarios)
                                </h2>

                                <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mt-2">
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label">Nombre Comercial</label>
                                        <form:input type="text" path="commercialName" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="Ej: La Gran Panadería" />
                                        <form:errors path="commercialName" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label">Categoría</label>
                                        <div class="pack-select-wrap">
                                            <form:select path="category" class="pack-form-select" cssErrorClass="pack-form-select is-invalid">
                                                <form:option value="BAKERY" label="Panadería"/>
                                                <form:option value="RESTAURANT" label="Restaurante"/>
                                                <form:option value="GREENGROCER" label="Verdulería"/>
                                                <form:option value="OTHER" label="Otros"/>
                                            </form:select>
                                            <span
                                                class="material-symbols-outlined pack-select-chevron">expand_more</span>
                                        </div>
                                        <form:errors path="category" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label">Calle</label>
                                        <form:input type="text" path="street" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="Av. Siempre Viva" />
                                        <form:errors path="street" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Número</label>
                                        <form:input type="number" path="streetNumber" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="123" maxlength="5" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                        <form:errors path="streetNumber" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Código Postal</label>
                                        <form:input type="text" path="postalCode" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="C1425" />
                                        <form:errors path="postalCode" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Ciudad</label>
                                        <form:input type="text" path="city" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="Buenos Aires" />
                                        <form:errors path="city" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Provincia</label>
                                        <form:input type="text" path="province" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="CABA" />
                                        <form:errors path="province" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Horario Apertura</label>
                                        <form:input type="time" path="openingTime"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" />
                                        <form:errors path="openingTime" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Horario Cierre</label>
                                        <form:input type="time" path="closingTime"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" />
                                        <form:errors path="closingTime" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                </div>
                            </section>

                        </div>

                        <!-- Right Column: Pack Details (Sticky) -->
                        <div class="pack-detail-aside">
                            <section class="pack-aside-card bg-surface-container-lowest shadow-soft">
                                <h2 class="pack-aside-heading mb-2">Detalles del Pack</h2>

                                <div class="pack-reservation-form mt-2">
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Título del Pack</label>
                                        <form:input type="text" path="title" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="Pack de Facturas Mixtas" required="required" />
                                        <form:errors path="title" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label">Descripción</label>
                                        <form:textarea path="description" class="pack-form-control" cssErrorClass="pack-form-control is-invalid" rows="3"
                                            placeholder="Puede contener medialunas dulces y saladas..."
                                            required="required"></form:textarea>
                                        <form:errors path="description" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>

                                    <!-- Tags selection -->
                                    <div class="pack-form-field">
                                        <label class="pack-form-label mb-2 block">Etiquetas / Restricciones
                                            (Opcional)</label>
                                        <div class="grid grid-cols-2 gap-2">
                                            <c:forEach var="tag" items="${availableTags}">
                                                <label
                                                    class="flex items-center gap-2 text-sm text-secondary cursor-pointer hover:bg-surface-container-high bg-surface-container-low p-2 rounded-md transition-colors">
                                                    <form:checkbox path="tags" value="${tag.name()}"
                                                        class="rounded text-primary focus:ring-primary h-4 w-4" />
                                                    <span>${tag.displayName}</span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-2 gap-4">
                                        <div class="pack-form-field">
                                            <label class="pack-form-label text-secondary">Precio Original</label>
                                            <div class="relative">
                                                <span
                                                    class="absolute left-3 top-1/2 -translate-y-1/2 text-secondary font-bold">$</span>
                                                <form:input type="number" step="0.01" path="originalPrice"
                                                    class="pack-form-control pack-form-control--tabular pl-8" cssErrorClass="pack-form-control pack-form-control--tabular pl-8 is-invalid"
                                                    placeholder="0.00" required="required" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                            </div>
                                            <form:errors path="originalPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                        </div>
                                        <div class="pack-form-field">
                                            <label class="pack-form-label text-primary">Precio Venta</label>
                                            <div class="relative">
                                                <span
                                                    class="absolute left-3 top-1/2 -translate-y-1/2 text-primary font-bold">$</span>
                                                <form:input type="number" step="0.01" path="finalPrice"
                                                    class="pack-form-control pack-form-control--tabular pl-8 font-bold" cssErrorClass="pack-form-control pack-form-control--tabular pl-8 font-bold is-invalid"
                                                    placeholder="0.00" required="required" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                            </div>
                                            <form:errors path="finalPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                        </div>
                                    </div>

                                    <div class="pack-form-field mt-2">
                                        <label class="pack-form-label flex justify-between">
                                            Cantidad a publicar
                                            <span class="text-xs text-secondary font-normal">Packs idénticos</span>
                                        </label>
                                        <form:input type="number" path="stock"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" placeholder="Ej: 5"
                                            required="required" maxlength="3" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                        <form:errors path="stock" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>

                                    <div class="pack-form-field mt-2">
                                        <label class="pack-form-label">Imagen del Pack (Opcional)</label>
                                        <input type="file" name="image" accept="image/*"
                                            class="pack-form-control file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary-fixed file:text-on-primary-fixed hover:file:bg-primary-fixed-dim cursor-pointer" />
                                        <p class="text-xs text-secondary mt-1">Formatos: JPG, PNG, WebP. Max 5 MB.</p>
                                    </div>

                                    <button type="submit" class="pack-submit-btn mt-4">
                                        <span class="material-symbols-outlined">rocket_launch</span>
                                        Publicar Pack Ahora
                                    </button>
                                </div>
                            </section>
                        </div>

                    </form:form>
                </main>

                <!-- Footer removed temporarily -->

            </body>

            </html>