<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<%@ attribute name="path" required="true" type="java.lang.String" description="Path for the image form binding" %>

<section class="pack-aside-card bg-surface-container-lowest shadow-soft offer-form-section">
    <h2 class="pack-aside-heading flex items-center gap-2 mb-2">
        <span class="material-symbols-outlined text-primary">add_photo_alternate</span>
        <spring:message code="commerce.createPack.form.image.label"/>
    </h2>
    <div class="pack-reservation-form mt-2">
        <div class="relative flex flex-col items-center justify-center border-2 border-dashed border-[#C2C9C2] rounded-xl overflow-hidden bg-surface-container-low hover:bg-surface-container-high transition-colors" style="min-height: 16rem;">
            <!-- Real Input -->
            <input type="file" id="image-input" name="${path}" accept="image/jpeg,image/png,image/webp,image/gif" class="hidden" />

            <!-- Empty state -->
            <div id="image-empty-state" class="absolute inset-0 flex flex-col items-center justify-center p-6 w-full h-full">
                <span class="material-symbols-outlined text-4xl text-secondary mb-2 group-hover:text-primary transition-colors cursor-pointer" onclick="document.getElementById('image-input').click()">upload_file</span>
                <p class="text-sm text-secondary text-center mb-4 cursor-pointer" onclick="document.getElementById('image-input').click()"><spring:message code="commerce.createPack.form.image.hint"/></p>
                <button type="button" class="py-2 px-4 rounded-full border-0 text-sm font-bold bg-primary-fixed text-on-primary-fixed hover:bg-primary-fixed-dim transition-colors" onclick="document.getElementById('image-input').click()">
                    <spring:message code="commerce.createPack.form.image.button.select"/>
                </button>
            </div>

            <!-- Preview state -->
            <div id="image-preview-state" class="absolute inset-0 w-full h-full hidden bg-surface-container-low">
                <img id="image-preview-img" src="" alt="Preview" class="w-full h-full object-contain p-2" />
                <div class="absolute inset-0 bg-black/40 opacity-0 hover:opacity-100 transition-opacity flex flex-row items-end justify-center pb-6 gap-4">
                    <button type="button" class="py-2 px-6 rounded-full text-sm font-bold transition-colors shadow-[0_12px_32px_rgba(25,28,26,0.06)]" onclick="document.getElementById('image-input').click()" style="background-color: var(--md-sys-color-surface-container-highest, #DFE2DF); color: var(--md-sys-color-on-surface, #191C1A);">
                         <spring:message code="commerce.createPack.form.image.button.change"/>
                    </button>
                    <button type="button" id="image-remove-btn" class="py-2 px-6 rounded-full text-sm font-bold shadow-[0_12px_32px_rgba(25,28,26,0.06)] transition-opacity hover:opacity-90" style="background-color: var(--md-sys-color-error-container, #FFDAD6); color: var(--md-sys-color-on-error-container, #410002);">
                         <spring:message code="commerce.createPack.form.image.button.remove"/>
                    </button>
                </div>
            </div>
        </div>
        <form:errors path="${path}" cssClass="pack-feedback pack-feedback--error pack-form-errors w-full mt-2" element="p" />
    </div>
</section>

<script>
// ── Client-side image preview and validation ──
document.addEventListener("DOMContentLoaded", function () {
    var imageInput = document.getElementById('image-input');
    var emptyState = document.getElementById('image-empty-state');
    var previewState = document.getElementById('image-preview-state');
    var previewImg = document.getElementById('image-preview-img');
    var removeBtn = document.getElementById('image-remove-btn');
    
    if (!imageInput) return;
    var maxFileSize = 5 * 1024 * 1024;
    var allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

    function showEmptyState() {
        previewImg.src = '';
        previewState.classList.add('hidden');
        emptyState.classList.remove('hidden');
    }

    imageInput.addEventListener('change', function (event) {
        var file = event.target.files[0];
        var errorContainer = event.target.closest('.pack-reservation-form');
        
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
                showEmptyState();
                
                var errorElement = document.createElement('p');
                errorElement.className = 'pack-feedback pack-feedback--error pack-form-errors image-js-error mt-2 w-full';
                errorElement.textContent = errorMsg;
                errorContainer.appendChild(errorElement);
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
            showEmptyState();
        }
    });
    
    if (removeBtn) {
        removeBtn.addEventListener('click', function() {
            imageInput.value = '';
            showEmptyState();
        });
    }
});
</script>
