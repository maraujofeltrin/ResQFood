<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="content" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="<c:out value='${id}'/>" class="modal" onclick="if(event.target===this){event.preventDefault();event.stopPropagation();this.style.display='none';}">
    <div class="modal-content" role="dialog" aria-modal="true" aria-labelledby="<c:out value='${id}'/>-title" onclick="event.preventDefault();event.stopPropagation();">
        <span class="close" onclick="event.preventDefault();event.stopPropagation();document.getElementById('<c:out value='${id}'/>').style.display='none'">&times;</span>
        <h2 id="<c:out value='${id}'/>-title" class="text-primary"><c:out value="${title}" /></h2>
        <c:choose>
            <c:when test="${not empty content}">
                <p><c:out value="${content}" /></p>
            </c:when>
            <c:otherwise>
                <jsp:doBody/>
            </c:otherwise>
        </c:choose>
    </div>
</div>
