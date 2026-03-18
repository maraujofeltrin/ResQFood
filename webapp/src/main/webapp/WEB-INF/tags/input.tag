<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="error" required="false" %>
<%@ attribute name="required" required="false" type="java.lang.Boolean" %>
<%@ attribute name="minlength" required="false" type="java.lang.Integer" %>
<%@ attribute name="maxlength" required="false" type="java.lang.Integer" %>
<%@ attribute name="pattern" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="hasError" value="${not empty error}" />

<div class="input-field">
	<label class="input-label" for="${id}">
		<c:out value="${label}" />
	</label>

	<input
		id="${id}"
		name="${not empty name ? name : id}"
		type="${not empty type ? type : 'text'}"
		class="input-control ${hasError ? 'is-invalid' : ''}"
		placeholder="${not empty placeholder ? placeholder : ''}"
		value="${not empty value ? value : ''}"
		aria-invalid="${hasError}"
		<c:if test="${required}">required="required"</c:if>
		<c:if test="${minlength ne null}">minlength="${minlength}"</c:if>
		<c:if test="${maxlength ne null}">maxlength="${maxlength}"</c:if>
		<c:if test="${not empty pattern}">pattern="${pattern}"</c:if>
	/>

	<c:if test="${hasError}">
		<span class="input-error"><c:out value="${error}" /></span>
	</c:if>
</div>
