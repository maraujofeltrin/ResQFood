<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

            <paw:commerceFormLayout pageTitleCode="commerce.createPack.pageTitle"
                titleCode="commerce.createPack.title" subtitleCode="commerce.createPack.subtitle">

                <c:url value="/commerce/create-pack" var="createPackAction">
                    <c:param name="${_csrf.parameterName}" value="${_csrf.token}" />
                </c:url>

                <form:form modelAttribute="createPackForm" action="${createPackAction}" method="post"
                    enctype="multipart/form-data" cssClass="pack-detail-grid commerce-create-pack-form" novalidate="novalidate">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                    <!-- Left Column: Form Sections -->
                    <div class="grid col-span-1 md:col-span-8 gap-8">
                        <paw:userIdentityFields />
                        <paw:commerceDataFields />
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

            </paw:commerceFormLayout>