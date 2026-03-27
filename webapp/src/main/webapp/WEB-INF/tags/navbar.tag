<%@ tag language="java" pageEncoding="UTF-8" %>
<nav class="fixed top-0 w-full z-50 bg-white/80 dark:bg-zinc-950/80 backdrop-blur-md shadow-sm dark:shadow-none font-['Plus_Jakarta_Sans'] antialiased">
  <div class="flex justify-between items-center px-6 py-4 max-w-screen-2xl mx-auto gap-4">
    <div class="flex items-center gap-8 flex-shrink-0">
      <span class="text-2xl font-bold tracking-tight text-green-900 dark:text-green-500">The Living Pantry</span>
      <div class="hidden md:flex gap-6">
        <a class="text-green-700 dark:text-green-400 font-semibold border-b-2 border-green-600" href="#">Discover</a>
        <a class="text-zinc-600 dark:text-zinc-400 hover:text-green-700 dark:hover:text-green-300 transition-all duration-300" href="#">Sustainability</a>
        <a class="text-zinc-600 dark:text-zinc-400 hover:text-green-700 dark:hover:text-green-300 transition-all duration-300" href="#">Impact</a>
        <a class="text-zinc-600 dark:text-zinc-400 hover:text-green-700 dark:hover:text-green-300 transition-all duration-300" href="#">How it Works</a>
      </div>
    </div>
    <div class="relative hidden sm:block w-full max-w-md">
      <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400">search</span>
      <input class="pl-10 pr-4 py-2 bg-zinc-100/50 dark:bg-zinc-800/50 rounded-full border-none focus:ring-2 focus:ring-green-600/20 w-full text-sm" placeholder="Search harvests..." type="text"/>
    </div>
  </div>
</nav>
