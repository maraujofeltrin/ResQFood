/**
 * Notification sidebar drawer: open/close, and mutation actions (read/unread/delete).
 */
(function () {
  var initialized = false;

  function getContextPath(sidebar) {
    var path = sidebar && sidebar.getAttribute("data-context-path");
    if (path && path.length > 0) {
      return path;
    }
    return "";
  }

  function postNotificationAction(contextPath, url, onSuccess) {
    fetch(contextPath + url, {
      method: "POST",
      credentials: "same-origin",
    })
      .then(function (response) {
        if (response.ok) {
          onSuccess();
          return;
        }
        console.warn("Notification action failed:", url, response.status);
      })
      .catch(function (err) {
        console.warn("Notification action error:", url, err);
      });
  }

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
    var contextPath = getContextPath(sidebar);

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

    function reloadPage() {
      window.location.reload();
    }

    function resolveActionUrl(action, notificationId) {
      if (action === "mark-all-read") {
        return "/notifications/read-all";
      }
      if (!notificationId) {
        return null;
      }
      if (action === "mark-read") {
        return "/notifications/" + notificationId + "/read";
      }
      if (action === "mark-unread") {
        return "/notifications/" + notificationId + "/unread";
      }
      if (action === "delete") {
        return "/notifications/" + notificationId + "/delete";
      }
      return null;
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

    sidebar.addEventListener("click", function (e) {
      var target = e.target.closest("[data-action]");
      if (!target || !sidebar.contains(target)) {
        return;
      }

      var action = target.getAttribute("data-action");
      if (!action) {
        return;
      }

      e.preventDefault();
      e.stopPropagation();

      var item = target.closest("[data-notification-id]");
      var notificationId = item ? item.getAttribute("data-notification-id") : null;
      var url = resolveActionUrl(action, notificationId);
      if (!url) {
        return;
      }

      postNotificationAction(contextPath, url, reloadPage);
    });

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
