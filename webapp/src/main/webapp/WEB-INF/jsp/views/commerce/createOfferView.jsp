<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
        <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

            <paw:commerceFormLayout pageTitleCode="commerce.createOffer.pageTitle"
                titleCode="commerce.createOffer.title" subtitleCode="commerce.createOffer.subtitle"
                backLabelCode="commerce.createOffer.backToDashboard">

                <c:url value="/commerce/create-offer" var="createOfferAction" />

                <form:form modelAttribute="createOfferForm" action="${createOfferAction}" method="post"
                    enctype="multipart/form-data" cssClass="commerce-create-offer-form" novalidate="novalidate">
                    <form:hidden path="isAuction" id="isAuctionInput" />

                    <!-- ── Offer Mode Toggle ── -->
                    <div class="offer-mode-toggle" id="offer-mode-toggle">
                        <spring:message code="commerce.createOffer.toggle.pack" var="togglePackLabel"/>
                        <spring:message code="commerce.createOffer.toggle.auction" var="toggleAuctionLabel"/>
                        <button type="button" class="offer-mode-toggle__option offer-mode-toggle__option--pack offer-mode-toggle__option--active"
                                id="toggle-pack-btn" data-mode="pack">
                            <span class="material-symbols-outlined" style="font-size:18px;">inventory_2</span>
                            ${togglePackLabel}
                        </button>
                        <button type="button" class="offer-mode-toggle__option offer-mode-toggle__option--auction"
                                id="toggle-auction-btn" data-mode="auction">
                            <span class="material-symbols-outlined" style="font-size:18px;">gavel</span>
                            ${toggleAuctionLabel}
                        </button>
                    </div>

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
                                <form:textarea path="description" class="pack-form-control" cssErrorClass="pack-form-control is-invalid" rows="3"
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

                    <!-- ── Pack-only Fields: Price & Stock ── -->
                    <section class="pack-aside-card bg-surface-container-lowest shadow-soft offer-form-section" id="pack-fields-section">
                        <h2 class="pack-aside-heading flex items-center gap-2 mb-2">
                            <span class="material-symbols-outlined text-primary">sell</span>
                            <spring:message code="commerce.createPack.form.finalPrice.label"/>
                        </h2>
                        <div class="pack-reservation-form mt-2">
                            <div class="grid grid-cols-2 gap-4">
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
                                <div class="pack-form-field">
                                    <label class="pack-form-label flex justify-between">
                                        <spring:message code="commerce.createPack.form.quantity.label"/>
                                        <span class="text-xs text-secondary font-normal"><spring:message code="commerce.createPack.form.quantity.hint"/></span>
                                    </label>
                                    <spring:message code="commerce.createPack.form.quantity.placeholder" var="qtyPlch"/>
                                    <form:input type="number" path="stock"
                                        class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" placeholder="${qtyPlch}"
                                        maxlength="3" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                    <form:errors path="stock" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- ── Auction-only Fields: Initial Price & End Date/Time ── -->
                    <section class="pack-aside-card bg-surface-container-lowest shadow-soft auction-settings-card offer-form-section" id="auction-fields-section" style="display:none;">
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
                                        placeholder="0.00" maxlength="10" oninput="if(this.value.length > this.maxLength) this.value = this.value.slice(0, this.maxLength);" />
                                </div>
                                <p class="text-xs text-secondary mt-1"><spring:message code="commerce.createAuction.form.initialPrice.hint"/></p>
                                <form:errors path="initialPrice" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                            </div>

                            <div class="grid grid-cols-2 gap-4">
                                <div class="pack-form-field">
                                    <label class="pack-form-label"><spring:message code="commerce.createAuction.form.endDate.label"/></label>
                                    <form:input type="date" path="endDate"
                                        class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" />
                                    <form:errors path="endDate" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                </div>
                                <div class="pack-form-field">
                                    <label class="pack-form-label"><spring:message code="commerce.createAuction.form.endTime.label"/></label>
                                    <form:input type="time" path="endTime"
                                        class="pack-form-control pack-form-control--tabular" cssErrorClass="pack-form-control pack-form-control--tabular is-invalid" />
                                    <form:errors path="endTime" cssClass="pack-feedback pack-feedback--error pack-form-errors" element="p" />
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- ── Submit Button ── -->
                    <spring:message code="commerce.createOffer.form.submitting" var="submittingText"/>
                    <spring:message code="commerce.createOffer.form.submit.pack" var="submitPackLabel"/>
                    <spring:message code="commerce.createOffer.form.submit.auction" var="submitAuctionLabel"/>
                    <button type="submit" class="pack-submit-btn font-headline mt-2" id="create-offer-submit-btn"
                            data-submitting-text="${submittingText}"
                            data-pack-label="${submitPackLabel}"
                            data-auction-label="${submitAuctionLabel}">
                        <span class="pack-submit-btn__label">${submitPackLabel}</span>
                        <span class="material-symbols-outlined pack-submit-btn__icon" id="submit-btn-icon">rocket_launch</span>
                    </button>

                </form:form>

                <script>
                    (function () {
                        var isAuctionInput = document.getElementById('isAuctionInput');
                        var togglePackBtn = document.getElementById('toggle-pack-btn');
                        var toggleAuctionBtn = document.getElementById('toggle-auction-btn');
                        var packSection = document.getElementById('pack-fields-section');
                        var auctionSection = document.getElementById('auction-fields-section');
                        var submitBtn = document.getElementById('create-offer-submit-btn');
                        var submitLabel = submitBtn.querySelector('.pack-submit-btn__label');
                        var submitIcon = document.getElementById('submit-btn-icon');

                        function setMode(isAuction) {
                            isAuctionInput.value = isAuction ? 'true' : 'false';

                            if (isAuction) {
                                togglePackBtn.classList.remove('offer-mode-toggle__option--active');
                                toggleAuctionBtn.classList.add('offer-mode-toggle__option--active');
                                packSection.style.display = 'none';
                                auctionSection.style.display = '';
                                submitLabel.textContent = submitBtn.getAttribute('data-auction-label');
                                submitIcon.textContent = 'gavel';
                                submitBtn.classList.add('offer-submit-btn--auction');
                            } else {
                                toggleAuctionBtn.classList.remove('offer-mode-toggle__option--active');
                                togglePackBtn.classList.add('offer-mode-toggle__option--active');
                                packSection.style.display = '';
                                auctionSection.style.display = 'none';
                                submitLabel.textContent = submitBtn.getAttribute('data-pack-label');
                                submitIcon.textContent = 'rocket_launch';
                                submitBtn.classList.remove('offer-submit-btn--auction');
                            }
                        }

                        togglePackBtn.addEventListener('click', function () { setMode(false); });
                        toggleAuctionBtn.addEventListener('click', function () { setMode(true); });

                        // Initialize based on current value (supports validation re-render)
                        setMode(isAuctionInput.value === 'true');
                    })();

                    // ── Prevent double-submit ──
                    (function () {
                        var form = document.querySelector('.commerce-create-offer-form');
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

                    // ── Client-side image validation ──
                    document.addEventListener("DOMContentLoaded", function () {
                        var imageInput = document.querySelector('input[name="image"]');
                        if (!imageInput) return;
                        var maxFileSize = 5 * 1024 * 1024;
                        var allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

                        imageInput.addEventListener('change', function (event) {
                            var file = event.target.files[0];
                            var errorContainer = event.target.parentNode;

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
                                    event.target.value = '';
                                    var errorElement = document.createElement('p');
                                    errorElement.className = 'pack-feedback pack-feedback--error pack-form-errors image-js-error mt-2';
                                    errorElement.textContent = errorMsg;
                                    event.target.parentNode.insertBefore(errorElement, event.target.nextSibling);
                                }
                            }
                        });
                    });
                </script>

            </paw:commerceFormLayout>
