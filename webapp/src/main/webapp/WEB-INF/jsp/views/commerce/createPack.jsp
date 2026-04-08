<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
            <!DOCTYPE html>
            <html class="light" lang="${pageContext.response.locale.language}">

            <head>
                <link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon">
                <meta charset="utf-8" />
                <meta content="width=device-width, initial-scale=1.0" name="viewport" />
                <title><spring:message code="commerce.createPack.pageTitle"/> | <spring:message code="app.brand"/></title>
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
                            <spring:message code="commerce.createPack.backToDashboard"/>
                        </a>
                    </div>

                    <header class="mb-10 text-center md:text-left">
                        <h1 class="pack-detail-title mb-3"><spring:message code="commerce.createPack.title"/></h1>
                        <p class="text-secondary text-lg"><spring:message code="commerce.createPack.subtitle"/></p>
                    </header>

                    <c:if test="${not empty errorMessage}">
                        <div class="pack-feedback pack-feedback--error mb-8 flex items-center gap-2">
                            <span class="material-symbols-outlined">error</span>
                            <c:out value="${errorMessage}" />
                        </div>
                    </c:if>

                    <form:form modelAttribute="createPackForm" action="${pageContext.request.contextPath}/commerce/create-pack" method="post"
                        enctype="multipart/form-data" cssClass="pack-detail-grid commerce-create-pack-form" novalidate="novalidate">

                        <!-- Left Column: Form Sections -->
                        <div class="grid col-span-1 md:col-span-8 gap-8">

                            <!-- Identidad del Usuario -->
                            <section class="pack-aside-card">
                                <h2 class="pack-aside-heading flex items-center gap-2">
                                    <span class="material-symbols-outlined text-primary">person</span>
                                    <spring:message code="commerce.createPack.userIdentify.title"/>
                                </h2>
                                <p class="text-sm text-secondary"><spring:message code="commerce.createPack.userIdentify.subtitle"/></p>

                                <div class="grid gap-5 mt-2">
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.email.label"/></label>
                                        <spring:message code="commerce.createPack.form.email.placeholder" var="emailPlaceholder"/>
                                        <form:input type="email" path="email" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${emailPlaceholder}" required="required" />
                                        <form:errors path="email" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.name.label"/></label>
                                        <spring:message code="commerce.createPack.form.name.placeholder" var="namePlaceholder"/>
                                        <form:input type="text" path="name" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${namePlaceholder}" required="required" />
                                        <form:errors path="name" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                </div>
                            </section>

                            <!-- Datos del Local -->
                            <section class="pack-aside-card">
                                <h2 class="pack-aside-heading flex items-center gap-2">
                                    <span class="material-symbols-outlined text-primary">store</span>
                                    <spring:message code="commerce.createPack.commerceData.title"/>
                                </h2>

                                <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mt-2">
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.commercialName.label"/></label>
                                        <spring:message code="commerce.createPack.form.commercialName.placeholder" var="commercialNamePlch"/>
                                        <form:input type="text" path="commercialName" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${commercialNamePlch}" />
                                        <form:errors path="commercialName" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.category.label"/></label>
                                        <div class="pack-select-wrap">
                                            <spring:message var="catBakery" code="commerce.category.BAKERY"/>
                                            <spring:message var="catRestaurant" code="commerce.category.RESTAURANT"/>
                                            <spring:message var="catGreengrocer" code="commerce.category.GREENGROCER"/>
                                            <spring:message var="catOther" code="commerce.category.OTHER"/>
                                            <form:select path="category" class="pack-form-select" cssErrorClass="pack-form-select is-invalid">
                                                <form:option value="BAKERY" label="${catBakery}"/>
                                                <form:option value="RESTAURANT" label="${catRestaurant}"/>
                                                <form:option value="GREENGROCER" label="${catGreengrocer}"/>
                                                <form:option value="OTHER" label="${catOther}"/>
                                            </form:select>
                                            <span
                                                class="material-symbols-outlined pack-select-chevron">expand_more</span>
                                        </div>
                                        <form:errors path="category" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field md:col-span-2">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.street.label"/></label>
                                        <spring:message code="commerce.createPack.form.street.placeholder" var="streetPlch"/>
                                        <form:input type="text" path="street" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${streetPlch}" />
                                        <form:errors path="street" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.streetNumber.label"/></label>
                                        <spring:message code="commerce.createPack.form.streetNumber.placeholder" var="streetNumPlch"/>
                                        <form:input type="number" path="streetNumber" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${streetNumPlch}" maxlength="5" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                        <form:errors path="streetNumber" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.postalCode.label"/></label>
                                        <spring:message code="commerce.createPack.form.postalCode.placeholder" var="postalCodePlch"/>
                                        <form:input type="text" path="postalCode" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${postalCodePlch}" />
                                        <form:errors path="postalCode" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.city.label"/></label>
                                        <spring:message code="commerce.createPack.form.city.placeholder" var="cityPlch"/>
                                        <form:input type="text" path="city" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${cityPlch}" />
                                        <form:errors path="city" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.province.label"/></label>
                                        <spring:message code="commerce.createPack.form.province.placeholder" var="provPlch"/>
                                        <form:input type="text" path="province" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${provPlch}" />
                                        <form:errors path="province" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.openingTime.label"/></label>
                                        <form:input type="time" path="openingTime"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" />
                                        <form:errors path="openingTime" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.closingTime.label"/></label>
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
                                <h2 class="pack-aside-heading mb-2"><spring:message code="commerce.createPack.packDetails.title"/></h2>

                                <div class="pack-reservation-form mt-2">
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.packTitle.label"/></label>
                                        <spring:message code="commerce.createPack.form.packTitle.placeholder" var="packTitlePlch"/>
                                        <form:input type="text" path="title" class="pack-form-control" cssErrorClass="pack-form-control is-invalid"
                                            placeholder="${packTitlePlch}" required="required" />
                                        <form:errors path="title" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.description.label"/></label>
                                        <spring:message code="commerce.createPack.form.description.placeholder" var="descPlch"/>
                                        <form:textarea path="description" class="pack-form-control" cssErrorClass="pack-form-control is-invalid" rows="3"
                                            placeholder="${descPlch}"
                                            required="required"></form:textarea>
                                        <form:errors path="description" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>

                                    <!-- Tags selection -->
                                    <div class="pack-form-field">
                                        <label class="pack-form-label mb-2 block"><spring:message code="commerce.createPack.form.tags.label"/></label>
                                        <div class="grid grid-cols-2 gap-2">
                                            <c:forEach var="tag" items="${availableTags}">
                                                <label
                                                    class="flex items-center gap-2 text-sm text-secondary cursor-pointer hover:bg-surface-container-high bg-surface-container-low p-2 rounded-md transition-colors">
                                                    <form:checkbox path="tags" value="${tag.name()}"
                                                        class="rounded text-primary focus:ring-primary h-4 w-4" />
                                                    <span><spring:message code="pack.tag.${tag.name()}"/></span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="grid grid-cols-2 gap-4">
                                        <div class="pack-form-field">
                                            <label class="pack-form-label text-secondary"><spring:message code="commerce.createPack.form.originalPrice.label"/></label>
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
                                            <label class="pack-form-label text-primary"><spring:message code="commerce.createPack.form.finalPrice.label"/></label>
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
                                            <spring:message code="commerce.createPack.form.quantity.label"/>
                                            <span class="text-xs text-secondary font-normal"><spring:message code="commerce.createPack.form.quantity.hint"/></span>
                                        </label>
                                        <spring:message code="commerce.createPack.form.quantity.placeholder" var="qtyPlch"/>
                                        <form:input type="number" path="stock"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" placeholder="${qtyPlch}"
                                            required="required" maxlength="3" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                        <form:errors path="stock" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>

                                    <div class="pack-form-field mt-2">
                                        <label class="pack-form-label"><spring:message code="commerce.createPack.form.image.label"/></label>
                                        <input type="file" name="image" accept="image/jpeg,image/png,image/webp,image/gif"
                                            class="pack-form-control file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary-fixed file:text-on-primary-fixed hover:file:bg-primary-fixed-dim cursor-pointer" />
                                        <form:errors path="image" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                        <p class="text-xs text-secondary mt-1"><spring:message code="commerce.createPack.form.image.hint"/></p>
                                    </div>

                                    <spring:message code="commerce.createPack.form.submitting" var="createPackSubmittingText"/>
                                    <button type="submit" class="pack-submit-btn font-headline mt-4"
                                            id="create-pack-submit-btn"
                                            data-submitting-text="${createPackSubmittingText}">
                                        <span class="pack-submit-btn__label"><spring:message code="commerce.createPack.form.submit"/></span>
                                        <span class="material-symbols-outlined pack-submit-btn__icon">rocket_launch</span>
                                    </button>
                                </div>
                            </section>
                        </div>

                    </form:form>
                </main>

                <!-- Footer removed temporarily -->

                <script>
                    (function () {
                        var form = document.querySelector('.commerce-create-pack-form');
                        var btn = document.getElementById('create-pack-submit-btn');
                        if (!form || !btn) {
                            return;
                        }
                        var submitted = false;
                        form.addEventListener('submit', function (e) {
                            if (submitted) {
                                e.preventDefault();
                                return;
                            }
                            submitted = true;
                            btn.disabled = true;
                            btn.setAttribute('aria-disabled', 'true');
                            btn.classList.add('pack-submit-btn--submitting');
                            var label = btn.querySelector('.pack-submit-btn__label');
                            if (label && btn.getAttribute('data-submitting-text')) {
                                label.textContent = btn.getAttribute('data-submitting-text');
                            }
                        });
                    })();

                    document.addEventListener("DOMContentLoaded", function () {
                        const imageInput = document.querySelector('input[name="image"]');
                        const maxFileSize = 5 * 1024 * 1024; // 5MB
                        const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

                        imageInput.addEventListener('change', function (event) {
                            const file = event.target.files[0];
                            const errorContainer = event.target.parentNode;

                            // Remove existing client-side errors
                            const existingErrors = errorContainer.querySelectorAll('.image-js-error');
                            existingErrors.forEach(e => e.remove());

                            if (file) {
                                let errorMsg = '';

                                if (file.size > maxFileSize) {
                                    errorMsg = "<spring:message code='commerce.createPack.validation.image.maxSize' javaScriptEscape='true'/>";
                                } else if (!allowedTypes.includes(file.type)) {
                                    errorMsg = "<spring:message code='commerce.createPack.validation.image.invalidType' javaScriptEscape='true'/>";
                                }

                                if (errorMsg !== '') {
                                    event.target.value = ''; // clear input

                                    const errorElement = document.createElement('p');
                                    errorElement.className = 'pack-feedback pack-feedback--error pack-form-errors image-js-error mt-2';
                                    errorElement.textContent = errorMsg;

                                    // Insert after the input
                                    event.target.parentNode.insertBefore(errorElement, event.target.nextSibling);
                                }
                            }
                        });
                    });
                </script>

            </body>

            </html>