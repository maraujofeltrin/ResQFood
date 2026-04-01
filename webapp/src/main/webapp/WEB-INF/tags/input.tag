<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="error" required="false" %>
<%@ attribute name="autocomplete" required="false" %>
<%@ attribute name="min" required="false" %>
<%@ attribute name="max" required="false" %>
<%@ attribute name="step" required="false" %>
<%@ attribute name="wrapperClass" required="false" %>
<%@ attribute name="labelClass" required="false" %>
<%@ attribute name="inputClass" required="false" %>
<%@ attribute name="errorClass" required="false" %>
<%@ attribute name="errorTag" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="hasError" value="${not empty error}" />
<c:set var="wc" value="${not empty wrapperClass ? wrapperClass : 'input-field'}" />
<c:set var="lc" value="${not empty labelClass ? labelClass : 'input-label'}" />
<c:set var="ic" value="${not empty inputClass ? inputClass : 'input-control'}" />
<c:set var="ec" value="${not empty errorClass ? errorClass : 'input-error'}" />
<c:set var="et" value="${not empty errorTag ? errorTag : 'span'}" />

<div class="${wc}">
	<label class="${lc}" for="${id}">
		<c:out value="${label}" />
	</label>

	<input
		id="${id}"
		name="${not empty name ? name : id}"
		type="${not empty type ? type : 'text'}"
		class="${ic}${hasError ? ' is-invalid' : ''}"
		<c:if test="${not empty placeholder}">placeholder="<c:out value="${placeholder}" />"</c:if>
		value="<c:out value="${value}" />"
		aria-invalid="${hasError}"
		<c:if test="${not empty autocomplete}">autocomplete="<c:out value="${autocomplete}" />"</c:if>
		<c:if test="${not empty min}">min="${min}"</c:if>
		<c:if test="${not empty max}">max="${max}"</c:if>
		<c:if test="${not empty step}">step="${step}"</c:if>
	/>

	<c:if test="${hasError}">
		<c:choose>
			<c:when test="${et eq 'p'}">
				<p class="${ec}"><c:out value="${error}" /></p>
			</c:when>
			<c:otherwise>
				<span class="${ec}"><c:out value="${error}" /></span>
			</c:otherwise>
		</c:choose>
	</c:if>
</div>
