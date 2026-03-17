<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="align" required="false" %>
<%@ attribute name="fontFamily" required="false" %>
<%@ attribute name="size" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="textAlign" value="${not empty align ? align : 'center'}"/>
<c:set var="textFont" value="${not empty fontFamily ? fontFamily : 'Inter, system-ui, -apple-system, sans-serif'}"/>
<c:set var="textSize" value="${not empty size ? size : '4rem'}"/>

<div style="text-align: ${textAlign}; margin: 3rem 0;">
    <span style="font-family: ${textFont}; font-size: ${textSize}; font-weight: 900; letter-spacing: -2px; background: linear-gradient(135deg, #4f46e5 0%, #ec4899 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">
        <c:out value="${text}" />
    </span>
</div>
