<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="align" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="textAlign" value="${not empty align ? align : 'center'}"/>
<c:set var="textSize" value="${not empty size ? size : '4rem'}"/>

<div style="text-align: ${textAlign}; margin: 3rem 0;">
    <span class="font-headline" style="font-size: ${textSize}; font-weight: 900; letter-spacing: -2px; background: linear-gradient(135deg, #059669 0%, #022C22 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">
        <c:out value="${text}" />
    </span>
</div>
