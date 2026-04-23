<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

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
