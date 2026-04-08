<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<html>
<paw:head title="Register" />
<body>
<h2>Register</h2>
<c:if test="${not empty successMessage}">
    <p class="formSuccess"><c:out value="${successMessage}"/></p>
</c:if>
<c:if test="${not empty user}">
    <h2><spring:message code="user.greeting" arguments="${user.name}"/></h2>
    <h5><spring:message code="user.id" arguments="${user.id}"/></h5>
    <p class="user-email"><spring:message code="user.email" arguments="${user.email}"/></p>
</c:if>
<c:url value="/create" var="postPath"/>
<form:form modelAttribute="registerForm" action="${postPath}" method="post">
    <div>
        <form:label path="username">Username: </form:label>
        <form:input type="text" path="username"/>
        <form:errors path="username" cssClass="errors" element="p"/>
    </div>
    <div>
        <form:label path="password">Password: </form:label>
        <form:input type="password" path="password" />
        <form:errors path="password" cssClass="errors" element="p"/>
    </div>
    <div>
        <form:label path="repeatPassword">Repeat password: </form:label>
        <form:input type="password" path="repeatPassword"/>
        <form:errors path="repeatPassword" cssClass="errors" element="p"/>
    </div>
    <div>
        <input type="submit" value="Register!"/>
    </div>
</form:form>
</body>
</html>