<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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
                <span class="material-symbols-outlined pack-select-chevron">expand_more</span>
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
