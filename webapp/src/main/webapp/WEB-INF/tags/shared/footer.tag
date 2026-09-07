<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<footer class="w-full py-12 px-8 mt-20 bg-surface-container-high font-body text-sm leading-relaxed relative z-50">
  <div class="flex flex-col md:flex-row justify-between items-start gap-10 max-w-7xl mx-auto">
    <div>
      <span class="text-xl font-bold italic text-primary mb-3 block font-headline"><spring:message code="app.brand"/></span>
      <p class="text-secondary max-w-xs"><spring:message code="layout.footer.mission"/></p>
    </div>
    <div>
      <h4 class="font-bold text-primary mb-4 uppercase tracking-widest text-xs font-headline"><spring:message code="layout.footer.contactTitle"/></h4>
      <spring:eval expression="@environment.getProperty('mail.username')" var="layoutFooterEmail"/>
      <a class="text-secondary hover:text-primary transition-colors" href="mailto:${layoutFooterEmail}">${layoutFooterEmail}</a>
    </div>
  </div>
  <div class="max-w-7xl mx-auto mt-10 pt-8 border-t border-outline-variant/30 text-center">
    <p class="text-secondary">&copy; 2026 <span class="italic"><spring:message code="app.brand"/></span>. <spring:message code="layout.footer.copyrightTagline"/></p>
  </div>
</footer>
