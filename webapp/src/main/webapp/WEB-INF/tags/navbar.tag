<%@ tag language="java" pageEncoding="UTF-8" %>
<nav class="fixed top-0 w-full z-50 bg-surface-container-lowest/80 backdrop-blur-md shadow-soft font-headline antialiased">
  <div class="flex justify-between items-center px-6 py-4 max-w-screen-2xl mx-auto gap-4">
    <div class="flex items-center gap-8 flex-shrink-0">
      <a href="${pageContext.request.contextPath}/" class="text-2xl font-bold tracking-tight text-primary">The Living Pantry</a>
      <div class="hidden md:flex gap-6">
        <a class="text-primary font-semibold border-b-2 border-primary" href="${pageContext.request.contextPath}/packs">Discover</a>
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="#">Sustainability</a>
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="#">Impact</a>
        <a class="text-on-surface-variant hover:text-primary transition-all duration-300" href="#">How it Works</a>
      </div>
    </div>
  </div>
</nav>
