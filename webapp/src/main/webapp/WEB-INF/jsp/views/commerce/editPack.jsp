<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

            <paw:commerceFormLayout pageTitleCode="commerce.editPack.pageTitle"
                titleCode="commerce.editPack.title" subtitleCode="commerce.editPack.subtitle">

                <c:url value="/commerce/edit-pack/${packId}" var="editPackAction" />

                <form:form modelAttribute="createOfferForm" action="${editPackAction}" method="post"
                    enctype="multipart/form-data" cssClass="w-full max-w-5xl mx-auto js-create-offer-form commerce-create-offer-form" novalidate="novalidate">
                    <form:hidden path="isAuction" id="isAuctionInput" />

                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start mt-2">
                        <div class="flex flex-col gap-6">

                    <!-- ── Pack Details Section ── -->
                    <section class="pack-aside-card bg-surface-container-lowest shadow-soft offer-form-section">
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
                                <form:textarea path="description" class="pack-form-control resize-none overflow-y-auto" cssErrorClass="pack-form-control resize-none overflow-y-auto is-invalid" rows="3"
                                    placeholder="${descPlch}" required="required"></form:textarea>
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

                        </div>
                    </section>
                        </div>

                        <div class="flex flex-col gap-6">

                            <!-- ── Image Upload Section ── -->
                            <paw:offerImageUpload path="image" />

                            <!-- ── Pricing & Settings (Pack-only, no toggle) ── -->
                            <section class="pack-aside-card bg-surface-container-lowest shadow-soft offer-form-section relative">
                                <header class="flex items-start justify-between gap-4 mb-4">
                                    <div class="flex-1">
                                        <h2 class="pack-aside-heading flex items-center gap-2 m-0">
                                            <span class="material-symbols-outlined text-primary">sell</span>
                                            <spring:message code="commerce.createPack.form.finalPrice.label"/>
                                        </h2>
                                    </div>
                                </header>

                                <div class="pack-reservation-form">
                                    <!-- Grid for Prices -->
                                    <div class="grid grid-cols-2 gap-4">
                                        <!-- Common: Original Price -->
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

                                        <!-- Pack: Final Price -->
                                        <div class="pack-form-field">
                                            <label class="pack-form-label text-primary"><spring:message code="commerce.createPack.form.finalPrice.label"/></label>
                                            <div class="relative">
                                                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-primary font-bold">$</span>
                                                <form:input type="number" step="0.01" path="finalPrice"
                                                    class="pack-form-control pack-form-control--tabular pl-8 font-bold" cssErrorClass="pack-form-control pack-form-control--tabular pl-8 font-bold is-invalid"
                                                    placeholder="0.00" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                            </div>
                                            <form:errors path="finalPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                        </div>
                                    </div>

                                    <!-- Pack: Stock -->
                                    <div class="pack-form-field mt-4">
                                        <label class="pack-form-label flex flex-col">
                                            <span><spring:message code="commerce.createPack.form.quantity.label"/></span>
                                            <span class="text-xs text-secondary font-normal mt-1"><spring:message code="commerce.createPack.form.quantity.hint"/></span>
                                        </label>
                                        <spring:message code="commerce.createPack.form.quantity.placeholder" var="qtyPlch"/>
                                        <form:input type="number" path="stock"
                                            class="pack-form-control pack-form-control--tabular w-1/2" cssErrorClass="pack-form-control pack-form-control--tabular w-1/2 is-invalid" placeholder="${qtyPlch}"
                                            maxlength="3" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                        <form:errors path="stock" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                    </div>

                                    <p class="text-xs text-secondary mt-3"><spring:message code="commerce.editPack.form.image.hint"/></p>
                                </div>
                            </section>

                    <!-- ── Submit Button ── -->
                    <spring:message code="commerce.editPack.form.submitting" var="submittingText"/>
                    <button type="submit" class="pack-submit-btn font-headline mt-2" id="create-offer-submit-btn"
                            data-submitting-text="${submittingText}">
                        <span class="pack-submit-btn__label"><spring:message code="commerce.editPack.form.submit"/></span>
                        <span class="material-symbols-outlined pack-submit-btn__icon">save</span>
                    </button>
                        </div>
                    </div>

                </form:form>

                <script>
                    // ── Prevent double-submit ──
                    (function () {
                        var form = document.querySelector('.js-create-offer-form');
                        var btn = document.getElementById('create-offer-submit-btn');
                        if (!form || !btn) return;
                        var submitted = false;
                        form.addEventListener('submit', function (e) {
                            if (submitted) { e.preventDefault(); return; }
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
                </script>

            </paw:commerceFormLayout>
