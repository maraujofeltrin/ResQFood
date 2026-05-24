<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>

<%@ attribute name="streetLine" required="true" %>
<%@ attribute name="locationLine" required="true" %>
<%@ attribute name="openingTime" required="true" %>
<%@ attribute name="closingTime" required="true" %>
<%@ attribute name="openNow" required="true" type="java.lang.Boolean" %>

<div class="commerce-info-card">
    <paw:commerceInfoCardBody
        streetLine="${streetLine}"
        locationLine="${locationLine}"
        openingTime="${openingTime}"
        closingTime="${closingTime}"
        openNow="${openNow}"
        showStoreName="false"
        showClosedWarning="false"/>
</div>
