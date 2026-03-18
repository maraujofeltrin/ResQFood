<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="content" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<div id="${id}" class="modal">
    <div class="modal-content">
        <span class="close" onclick="document.getElementById('${id}').style.display='none'">&times;</span>
        <h2><c:out value="${title}" /></h2>
        <p><c:out value="${content}" /></p>
    </div>
</div>
