<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<%@ attribute name="path" required="true" type="java.lang.String" description="Path for the image form binding" %>

<c:set var="existingImageIdVal" value="${createOfferForm.existingImageId}" />
<c:set var="existingUrl" value="" />
<c:if test="${not empty existingImageIdVal}">
    <c:set var="existingUrl" value="${pageContext.request.contextPath}/images/${existingImageIdVal}" />
</c:if>

<section class="pack-aside-card bg-surface-container-lowest shadow-soft offer-form-section">
    <h2 class="pack-aside-heading flex items-center gap-2 mb-2">
        <span class="material-symbols-outlined text-primary">add_photo_alternate</span>
        <spring:message code="commerce.createPack.form.image.label"/>
    </h2>
    <div class="pack-reservation-form mt-2">
        <form:hidden path="existingImageId" id="existing-image-id" />
        <paw:imageUpload path="${path}"
            inputId="image-input"
            existingImageUrl="${existingUrl}"
            containerClass="bg-surface-container-low hover:bg-surface-container-high rounded-xl border-2 border-dashed border-[#C2C9C2]"
            hintCode="commerce.createPack.form.image.hint"
            selectBtnCode="commerce.createPack.form.image.button.select"
            changeBtnCode="commerce.createPack.form.image.button.change"
            removeBtnCode="commerce.createPack.form.image.button.remove"
            maxSizeErrorCode="commerce.createPack.validation.image.maxSize"
            invalidTypeErrorCode="commerce.createPack.validation.image.invalidType" />

        <script>
        document.addEventListener("DOMContentLoaded", function () {
            var existingImageIdInput = document.getElementById('existing-image-id');
            var removeBtn = document.getElementById('image-input-remove-btn');
            if (removeBtn && existingImageIdInput) {
                removeBtn.addEventListener('click', function () {
                    existingImageIdInput.value = '';
                });
            }
        });
        </script>
    </div>
</section>
