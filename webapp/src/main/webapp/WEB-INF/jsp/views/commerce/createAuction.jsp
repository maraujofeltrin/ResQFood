<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

            <paw:commerceFormLayout pageTitleCode="commerce.createAuction.pageTitle"
                titleCode="commerce.createAuction.title" subtitleCode="commerce.createAuction.subtitle">

                <c:url value="/commerce/create-auction" var="createAuctionAction">
                    <c:param name="${_csrf.parameterName}" value="${_csrf.token}" />
                </c:url>

                <form:form modelAttribute="createAuctionForm" action="${createAuctionAction}" method="post"
                    enctype="multipart/form-data" cssClass="pack-detail-grid commerce-create-auction-form" novalidate="novalidate">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                    <!-- Left Column: Form Sections -->
                    <div class="grid col-span-1 md:col-span-8 gap-8">
                        <paw:userIdentityFields />
                        <paw:commerceDataFields />
                    </div>

                    <!-- Right Column: Auction Details (Sticky) -->
                    <div class="pack-detail-aside">

                        <!-- Pack Details Section -->
                        <section class="pack-aside-card bg-surface-container-lowest shadow-soft mb-6">
                            <h2 class="pack-aside-heading mb-2"><spring:message code="commerce.createAuction.packDetails.title"/></h2>

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

                                <div class="pack-form-field">
                                    <label class="pack-form-label text-secondary"><spring:message code="commerce.createPack.form.originalPrice.label"/></label>
                                    <div class="relative">
                                        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-secondary font-bold">$</span>
                                        <form:input type="number" step="0.01" path="originalPrice"
                                            class="pack-form-control pack-form-control--tabular pl-8" cssErrorClass="pack-form-control pack-form-control--tabular pl-8 is-invalid"
                                            placeholder="0.00" required="required" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                    </div>
                                    <form:errors path="originalPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                </div>

                                <div class="pack-form-field mt-2">
                                    <label class="pack-form-label"><spring:message code="commerce.createPack.form.image.label"/></label>
                                    <input type="file" name="image" accept="image/jpeg,image/png,image/webp,image/gif"
                                        class="pack-form-control file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-primary-fixed file:text-on-primary-fixed hover:file:bg-primary-fixed-dim cursor-pointer" />
                                    <form:errors path="image" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    <p class="text-xs text-secondary mt-1"><spring:message code="commerce.createPack.form.image.hint"/></p>
                                </div>
                            </div>
                        </section>

                        <!-- Auction Settings Section (orange accent) -->
                        <section class="pack-aside-card bg-surface-container-lowest shadow-soft auction-settings-card">
                            <h2 class="pack-aside-heading flex items-center gap-2 mb-2">
                                <span class="material-symbols-outlined text-auction">gavel</span>
                                <spring:message code="commerce.createAuction.auctionSettings.title"/>
                                <span class="auction-badge ml-auto">
                                    <span class="material-symbols-outlined" style="font-size: 14px;">bolt</span>
                                    <spring:message code="commerce.createAuction.badge"/>
                                </span>
                            </h2>

                            <div class="pack-reservation-form mt-2">
                                <div class="pack-form-field">
                                    <label class="pack-form-label text-auction"><spring:message code="commerce.createAuction.form.initialPrice.label"/></label>
                                    <div class="relative">
                                        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-auction font-bold">$</span>
                                        <form:input type="number" step="0.01" path="initialPrice"
                                            class="pack-form-control pack-form-control--tabular pl-8 font-bold" cssErrorClass="pack-form-control pack-form-control--tabular pl-8 font-bold is-invalid"
                                            placeholder="0.00" required="required" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                    </div>
                                    <p class="text-xs text-secondary mt-1"><spring:message code="commerce.createAuction.form.initialPrice.hint"/></p>
                                    <form:errors path="initialPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                </div>

                                <div class="grid grid-cols-2 gap-4">
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createAuction.form.endDate.label"/></label>
                                        <form:input type="date" path="endDate"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid"
                                            required="required" />
                                        <form:errors path="endDate" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                    <div class="pack-form-field">
                                        <label class="pack-form-label"><spring:message code="commerce.createAuction.form.endTime.label"/></label>
                                        <form:input type="time" path="endTime"
                                            class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid"
                                            required="required" />
                                        <form:errors path="endTime" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>
                                </div>

                                <spring:message code="commerce.createAuction.form.submitting" var="createAuctionSubmittingText"/>
                                <button type="submit" class="auction-submit-btn font-headline mt-4"
                                        id="create-auction-submit-btn"
                                        data-submitting-text="${createAuctionSubmittingText}">
                                    <span class="auction-submit-btn__label"><spring:message code="commerce.createAuction.form.submit"/></span>
                                    <span class="material-symbols-outlined auction-submit-btn__icon">gavel</span>
                                </button>
                            </div>
                        </section>

                    </div>

                </form:form>

                <script>
                    (function () {
                        var form = document.querySelector('.commerce-create-auction-form');
                        var btn = document.getElementById('create-auction-submit-btn');
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
                            btn.classList.add('auction-submit-btn--submitting');
                            var label = btn.querySelector('.auction-submit-btn__label');
                            if (label && btn.getAttribute('data-submitting-text')) {
                                label.textContent = btn.getAttribute('data-submitting-text');
                            }
                        });
                    })();

                    document.addEventListener("DOMContentLoaded", function () {
                        var imageInput = document.querySelector('input[name="image"]');
                        if (!imageInput) return;
                        var maxFileSize = 5 * 1024 * 1024; // 5MB
                        var allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

                        imageInput.addEventListener('change', function (event) {
                            var file = event.target.files[0];
                            var errorContainer = event.target.parentNode;

                            // Remove existing client-side errors
                            var existingErrors = errorContainer.querySelectorAll('.image-js-error');
                            existingErrors.forEach(function(e) { e.remove(); });

                            if (file) {
                                var errorMsg = '';

                                if (file.size > maxFileSize) {
                                    errorMsg = "<spring:message code='commerce.createPack.validation.image.maxSize' javaScriptEscape='true'/>";
                                } else if (allowedTypes.indexOf(file.type) === -1) {
                                    errorMsg = "<spring:message code='commerce.createPack.validation.image.invalidType' javaScriptEscape='true'/>";
                                }

                                if (errorMsg !== '') {
                                    event.target.value = ''; // clear input

                                    var errorElement = document.createElement('p');
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
