<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<%@ attribute name="path" required="true" type="java.lang.String" rtexprvalue="true" description="Form binding path for the MultipartFile" %>
<%@ attribute name="inputId" required="true" type="java.lang.String" rtexprvalue="true" description="DOM id for the file input" %>
<%@ attribute name="existingImageUrl" required="false" type="java.lang.String" rtexprvalue="true" description="URL to pre-load as existing image" %>
<%@ attribute name="containerClass" required="false" type="java.lang.String" rtexprvalue="true" description="Extra CSS classes for the preview container" %>
<%@ attribute name="containerMinHeight" required="false" type="java.lang.String" rtexprvalue="true" description="Min-height of preview container" %>
<%@ attribute name="imgClass" required="false" type="java.lang.String" rtexprvalue="true" description="CSS classes for the preview img element" %>
<%@ attribute name="imgAltCode" required="false" type="java.lang.String" rtexprvalue="true" description="Optional message code for preview img alt text (defaults to Preview)" %>
<%@ attribute name="hintCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for hint text" %>
<%@ attribute name="selectBtnCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for select button" %>
<%@ attribute name="changeBtnCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for change button" %>
<%@ attribute name="removeBtnCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for remove button" %>
<%@ attribute name="maxSizeErrorCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for max size validation error" %>
<%@ attribute name="invalidTypeErrorCode" required="false" type="java.lang.String" rtexprvalue="true" description="Message code for invalid type validation error" %>
<%@ attribute name="errorsClass" required="false" type="java.lang.String" rtexprvalue="true" description="CSS class for form:errors and JS validation error elements" %>
<%@ attribute name="compactButtons" required="false" type="java.lang.Boolean" rtexprvalue="true" description="Smaller overlay change/remove controls for tight previews (e.g. avatar)" %>
<%@ attribute name="primaryActionBelow" required="false" type="java.lang.Boolean" rtexprvalue="true" description="Primary choose/select control rendered below the preview instead of inside empty state" %>
<%@ attribute name="belowHintCode" required="false" type="java.lang.String" rtexprvalue="true" description="Optional hint below the bottom button (formats/size); empty state uses hintCode when primaryActionBelow is false" %>

<c:set var="resolvedErrorsClass" value="${not empty errorsClass ? errorsClass : 'pack-feedback pack-feedback--error pack-form-errors w-full mt-2'}" />
<c:set var="isCompact" value="${compactButtons == true}" />
<c:set var="actionBelow" value="${primaryActionBelow == true}" />

<c:set var="imgPreviewId" value="${inputId}-preview-img" />
<c:set var="emptyStateId" value="${inputId}-empty-state" />
<c:set var="previewStateId" value="${inputId}-preview-state" />
<c:set var="removeId" value="${inputId}-remove-btn" />

<div data-image-upload-id="${inputId}">
    <div class="relative flex flex-col items-center justify-center overflow-hidden transition-colors ${containerClass}"
         style="min-height: ${empty containerMinHeight ? '16rem' : containerMinHeight};">
        <input type="file" id="${inputId}" name="${path}" accept="image/jpeg,image/png,image/webp,image/gif" class="hidden" />

        <div id="${emptyStateId}" class="absolute inset-0 flex flex-col items-center justify-center ${isCompact ? 'p-3' : 'p-6'} w-full h-full">
            <span class="material-symbols-outlined ${isCompact ? 'text-2xl mb-1' : 'text-4xl mb-2'} text-secondary group-hover:text-primary transition-colors cursor-pointer" onclick="document.getElementById('${inputId}').click()">upload_file</span>
            <c:choose>
                <c:when test="${actionBelow}">
                    <p class="${isCompact ? 'text-xs' : 'text-sm'} text-secondary text-center ${isCompact ? 'mb-0' : 'mb-4'} cursor-pointer px-1" onclick="document.getElementById('${inputId}').click()">
                        <c:if test="${not empty hintCode}"><spring:message code="${hintCode}"/></c:if>
                    </p>
                </c:when>
                <c:otherwise>
                    <p class="text-sm text-secondary text-center mb-4 cursor-pointer" onclick="document.getElementById('${inputId}').click()">
                        <c:if test="${not empty hintCode}"><spring:message code="${hintCode}"/></c:if>
                    </p>
                    <button type="button" class="py-2 px-4 rounded-full border-0 text-sm font-bold bg-primary-fixed text-on-primary-fixed hover:bg-primary-fixed-dim transition-colors" onclick="document.getElementById('${inputId}').click()">
                        <c:if test="${not empty selectBtnCode}"><spring:message code="${selectBtnCode}"/></c:if>
                    </button>
                </c:otherwise>
            </c:choose>
        </div>

        <div id="${previewStateId}" class="absolute inset-0 w-full h-full hidden bg-surface-container">
            <img id="${imgPreviewId}" src="" class="w-full h-full ${empty imgClass ? 'object-contain p-2' : imgClass}"
                 alt="<c:choose><c:when test="${not empty imgAltCode}"><spring:message code="${imgAltCode}"/></c:when><c:otherwise>Preview</c:otherwise></c:choose>" />
            <div class="absolute inset-0 bg-black/40 opacity-0 hover:opacity-100 transition-opacity flex flex-row items-end justify-center ${isCompact ? 'pb-2 gap-1.5' : 'pb-6 gap-4'}">
                <button type="button" class="${isCompact ? 'py-1 px-2.5 rounded-full text-xs font-bold' : 'py-2 px-6 rounded-full text-sm font-bold'} transition-colors ${isCompact ? '' : 'shadow-[0_12px_32px_rgba(25,28,26,0.06)]'}" onclick="document.getElementById('${inputId}').click()" style="background-color: var(--md-sys-color-surface-container-highest, #DFE2DF); color: var(--md-sys-color-on-surface, #191C1A);">
                    <c:if test="${not empty changeBtnCode}"><spring:message code="${changeBtnCode}"/></c:if>
                </button>
                <button type="button" id="${removeId}" class="${isCompact ? 'py-1 px-2.5 rounded-full text-xs font-bold' : 'py-2 px-6 rounded-full text-sm font-bold'} ${isCompact ? '' : 'shadow-[0_12px_32px_rgba(25,28,26,0.06)]'} transition-opacity hover:opacity-90" style="background-color: var(--md-sys-color-error-container, #FFDAD6); color: var(--md-sys-color-on-error-container, #410002);">
                    <c:if test="${not empty removeBtnCode}"><spring:message code="${removeBtnCode}"/></c:if>
                </button>
            </div>
        </div>
    </div>
    <form:errors path="${path}" cssClass="${resolvedErrorsClass}" element="p" />

    <c:if test="${actionBelow && not empty selectBtnCode}">
        <label class="text-xs font-bold uppercase tracking-wider text-on-surface-variant font-label sr-only" for="${inputId}">
            <c:if test="${not empty selectBtnCode}"><spring:message code="${selectBtnCode}"/></c:if>
        </label>
        <button type="button"
                class="mt-4 flex w-full max-w-[14rem] mx-auto sm:mx-0 items-center justify-center py-2 px-4 rounded-full border-0 bg-primary text-on-primary text-xs font-semibold shadow-soft hover:brightness-110 transition-colors cursor-pointer"
                onclick="document.getElementById('${inputId}').click()">
            <spring:message code="${selectBtnCode}"/>
        </button>
        <c:if test="${not empty belowHintCode}">
            <p class="mt-3 text-xs text-on-surface-variant font-body max-w-[14rem] text-center sm:text-left leading-relaxed mx-auto sm:mx-0">
                <spring:message code="${belowHintCode}"/>
            </p>
        </c:if>
    </c:if>
</div>

<script>
document.addEventListener("DOMContentLoaded", function () {
    var imageInput = document.getElementById('${inputId}');
    var emptyState = document.getElementById('${emptyStateId}');
    var previewState = document.getElementById('${previewStateId}');
    var previewImg = document.getElementById('${imgPreviewId}');
    var removeBtn = document.getElementById('${removeId}');
    var componentRoot = document.querySelector('[data-image-upload-id="${inputId}"]');

    if (!imageInput) return;
    var maxFileSize = 5 * 1024 * 1024;
    var allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    var existingUrl = '${not empty existingImageUrl ? existingImageUrl : ''}';

    <c:if test="${not empty existingImageUrl}">
    if (existingUrl) {
        previewImg.src = existingUrl;
        emptyState.classList.add('hidden');
        previewState.classList.remove('hidden');
    }
    </c:if>

    function resetToOriginal() {
        if (existingUrl) {
            previewImg.src = existingUrl;
            emptyState.classList.add('hidden');
            previewState.classList.remove('hidden');
        } else {
            previewImg.src = '';
            previewState.classList.add('hidden');
            emptyState.classList.remove('hidden');
        }
    }

    imageInput.addEventListener('change', function (event) {
        var file = event.target.files[0];

        var existingErrors = componentRoot ? componentRoot.querySelectorAll('.image-js-error') : [];
        existingErrors.forEach(function(e) { e.remove(); });

        if (file) {
            var errorMsg = '';
            if (file.size > maxFileSize) {
                <c:if test="${not empty maxSizeErrorCode}">
                errorMsg = "<spring:message code='${maxSizeErrorCode}' javaScriptEscape='true'/>";
                </c:if>
            } else if (allowedTypes.indexOf(file.type) === -1) {
                <c:if test="${not empty invalidTypeErrorCode}">
                errorMsg = "<spring:message code='${invalidTypeErrorCode}' javaScriptEscape='true'/>";
                </c:if>
            }

            if (errorMsg !== '') {
                event.target.value = '';
                resetToOriginal();

                var errorElement = document.createElement('p');
                errorElement.className = 'image-js-error ' + '${resolvedErrorsClass}';
                errorElement.textContent = errorMsg;
                if (componentRoot) componentRoot.appendChild(errorElement);
            } else {
                var reader = new FileReader();
                reader.onload = function(e) {
                    previewImg.src = e.target.result;
                    emptyState.classList.add('hidden');
                    previewState.classList.remove('hidden');
                }
                reader.readAsDataURL(file);
            }
        } else {
            resetToOriginal();
        }
    });

    if (removeBtn) {
        removeBtn.addEventListener('click', function() {
            imageInput.value = '';
            resetToOriginal();
        });
    }
});
</script>
