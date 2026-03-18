<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="name" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="placeholder" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="input-field">
	<label class="input-label" for="${id}">
		<c:out value="${label}" />
	</label>

	<input
		id="${id}"
		name="${not empty name ? name : id}"
		type="${not empty type ? type : 'text'}"
		class="input-control"
		placeholder="${not empty placeholder ? placeholder : ''}"
	/>
</div>
