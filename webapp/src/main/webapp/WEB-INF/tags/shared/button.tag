<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="type" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ attribute name="onclick" required="false" %>
<%@ attribute name="htmlType" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="btnType" value="${not empty type ? type : 'primary'}"/>
<c:set var="btnSize" value="${not empty size ? size : 'md'}"/>
<c:set var="btnCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="btnDisabled" value="${disabled ne null ? disabled: false}" />
<c:set var="buttonHtmlType" value="${not empty htmlType ? htmlType : 'button'}" />
<c:set var="classes" value="btn btn-${btnType} btn-${btnSize} ${btnCssClass}" />

<button type="${buttonHtmlType}"
    class="${classes}"
    <c:if test="${not empty onclick}">
        onclick="${onclick}"
    </c:if>
    <c:if test="${btnDisabled}">disabled="disabled"</c:if>>
    ${text}
</button>