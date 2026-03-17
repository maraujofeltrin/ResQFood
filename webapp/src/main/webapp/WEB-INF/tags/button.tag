<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="btnType" value="${not empty type ? type : 'primary'}"/>
<c:set var="btnSize" value="${not empty size ? size : 'md'}"/>
<c:set var="btnCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="btnDisabled" value="${disabled ne null ? disabled: false}" />
<c:set var="classes" value="btn btn-${btnType} btn-${btnSize} ${btnCssClass}" />

<button type="button"
    class="${classes}"
    <c:if test="${btnDisabled}">disabled="disabled"</c:if>>
    ${text}
</button>