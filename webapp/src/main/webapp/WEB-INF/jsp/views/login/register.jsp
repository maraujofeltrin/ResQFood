<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
<spring:message code="register.title" var="registerTitle" text="Registrate"/>
<paw:head title="${registerTitle}" />
<body class="bg-surface text-on-surface antialiased flex flex-col min-h-screen">
    <paw:navbar />
    
    <main class="pt-24 flex flex-col items-center justify-center flex-grow py-12">
        <div class="w-full max-w-md bg-surface-container-lowest rounded-xl shadow-soft p-8 border border-outline-variant">
            <h1 class="font-headline text-3xl font-bold text-center text-primary mb-6"><spring:message code="register.title" text="Regístrate"/></h1>
            
            <c:if test="${not empty successMessage}">
                <div class="bg-primary/10 text-primary rounded-lg p-4 mb-6 text-sm">
                    <c:out value="${successMessage}"/>
                </div>
            </c:if>
            
            <c:url value="/create" var="postPath"/>
            <form:form modelAttribute="registerForm" action="${postPath}" method="post" class="space-y-5">
                
                <div>
                    <form:label path="name" cssClass="block font-label text-sm font-medium text-secondary mb-1">
                        <spring:message code="register.label.name" text="Nombre completo"/>
                    </form:label>
                    <spring:message code="register.placeholder.name" var="namePlaceholder" text="Ej: Juan Perez"/>
                    <form:input type="text" path="name"
                           cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                           placeholder="${namePlaceholder}"/>
                    <form:errors path="name" cssClass="text-error text-sm mt-1" element="p"/>
                </div>

                <div>
                    <form:label path="email" cssClass="block font-label text-sm font-medium text-secondary mb-1">
                        <spring:message code="register.label.email" text="Correo electrónico"/>
                    </form:label>
                    <spring:message code="register.placeholder.email" var="emailPlaceholder" text="nombre@ejemplo.com"/>
                    <form:input type="email" path="email"
                           cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                           placeholder="${emailPlaceholder}"/>
                    <form:errors path="email" cssClass="text-error text-sm mt-1" element="p"/>
                </div>
                
                <div>
                    <form:label path="password" cssClass="block font-label text-sm font-medium text-secondary mb-1">
                        <spring:message code="register.label.password" text="Contraseña"/>
                    </form:label>
                    <spring:message code="register.placeholder.password" var="passwordPlaceholder" text="••••••••"/>
                    <form:input type="password" path="password"
                           cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                           placeholder="${passwordPlaceholder}"/>
                    <form:errors path="password" cssClass="text-error text-sm mt-1" element="p"/>
                </div>
                
                <div>
                    <form:label path="repeatPassword" cssClass="block font-label text-sm font-medium text-secondary mb-1">
                        <spring:message code="register.label.repeatPassword" text="Repetir contraseña"/>
                    </form:label>
                    <spring:message code="register.placeholder.repeatPassword" var="repeatPlaceholder" text="••••••••"/>
                    <form:input type="password" path="repeatPassword"
                           cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                           placeholder="${repeatPlaceholder}"/>
                    <form:errors path="repeatPassword" cssClass="text-error text-sm mt-1" element="p"/>
                </div>
                
                <div class="space-y-2">
                    <form:label path="role" cssClass="block font-label text-sm font-medium text-secondary mb-1">
                        <spring:message code="register.label.role" text="Tipo de cuenta"/>
                    </form:label>
                    <div class="flex gap-4">
                        <label class="flex items-center gap-2 cursor-pointer bg-surface-container-lowest border border-outline hover:border-primary p-3 rounded-lg flex-1 transition-colors">
                            <form:radiobutton path="role" value="CLIENT" cssClass="text-primary focus:ring-primary border-outline" />
                            <span class="text-sm font-medium text-on-surface"><spring:message code="register.role.client" text="Cliente"/></span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer bg-surface-container-lowest border border-outline hover:border-primary p-3 rounded-lg flex-1 transition-colors">
                            <form:radiobutton path="role" value="COMMERCE" cssClass="text-primary focus:ring-primary border-outline" />
                            <span class="text-sm font-medium text-on-surface"><spring:message code="register.role.commerce" text="Comercio"/></span>
                        </label>
                    </div>
                    <form:errors path="role" cssClass="text-error text-sm mt-1" element="p"/>
                </div>
                
                <button type="submit" 
                        class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-6 h-12 flex items-center justify-center">
                    <spring:message code="register.submit" text="Crear cuenta"/>
                </button>
            </form:form>
            
            <div class="mt-6 text-center text-sm text-secondary">
                <spring:message code="register.prompt.hasAccount" text="¿Ya tienes cuenta?"/> 
                <a href="${pageContext.request.contextPath}/login" class="text-primary font-medium hover:underline">
                    <spring:message code="register.link.login" text="Inicia sesión aquí"/>
                </a>
            </div>
            
        </div>
    </main>
    <paw:footer />
</body>
</html>
