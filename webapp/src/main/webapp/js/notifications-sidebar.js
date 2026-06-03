/**
 * Notification sidebar drawer: open/close on bell click, backdrop, Escape.
 */
(function () {
  var initialized = false;

  function initNotificationSidebar() {
    if (initialized) {
      return;
    }

    var bell = document.getElementById("notification-bell");
    var sidebar = document.getElementById("notification-sidebar");
    var backdrop = document.getElementById("notification-backdrop");
    var closeBtn = document.getElementById("notification-sidebar-close");

    if (!bell || !sidebar || !backdrop) {
      return;
    }

    initialized = true;

    function isOpen() {
      return sidebar.classList.contains("notifications-sidebar--open");
    }

    function setOpen(open) {
      sidebar.classList.toggle("notifications-sidebar--open", open);
      backdrop.classList.toggle("notifications-backdrop--visible", open);
      backdrop.classList.toggle("hidden", !open);
      sidebar.setAttribute("aria-hidden", open ? "false" : "true");
      bell.setAttribute("aria-expanded", open ? "true" : "false");
      document.body.classList.toggle("notifications-sidebar-open", open);
    }

    function openSidebar() {
      setOpen(true);
      if (closeBtn) {
        closeBtn.focus();
      }
    }

    function closeSidebar() {
      setOpen(false);
    }

    function toggleSidebar() {
      if (isOpen()) {
        closeSidebar();
      } else {
        openSidebar();
      }
    }

    bell.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      toggleSidebar();
    });

    backdrop.addEventListener("click", function () {
      closeSidebar();
    });

    if (closeBtn) {
      closeBtn.addEventListener("click", function (e) {
        e.preventDefault();
        closeSidebar();
        bell.focus();
      });
    }

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && isOpen()) {
        closeSidebar();
        bell.focus();
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initNotificationSidebar);
  } else {
    initNotificationSidebar();
  }
})();
