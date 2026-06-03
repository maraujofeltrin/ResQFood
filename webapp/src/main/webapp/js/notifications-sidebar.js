(function () {
  var initialized = false;

  function initNotificationSidebar() {
    if (initialized) return;

    var bell = document.getElementById("notification-bell");
    var sidebar = document.getElementById("notification-sidebar");
    var backdrop = document.getElementById("notification-backdrop");
    var closeBtn = document.getElementById("notification-sidebar-close");
    var listContainer = document.getElementById("notification-list");
    var emptyEl = document.getElementById("notification-empty");

    if (!bell || !sidebar || !backdrop || !listContainer) return;
    initialized = true;

    var ctx = sidebar.getAttribute("data-context-path") || "";
    var labels = {
      markRead:  sidebar.getAttribute("data-label-mark-read") || "",
      markUnread: sidebar.getAttribute("data-label-mark-unread") || "",
      del:       sidebar.getAttribute("data-label-delete") || "",
      justNow:   sidebar.getAttribute("data-label-just-now") || "Now"
    };

    var typeTitles = {};
    var types = [
      "RESERVATION_REQUESTED_COMMERCE", "RESERVATION_CODE_CLIENT",
      "AUCTION_WINNER_CLIENT", "AUCTION_WINNER_COMMERCE",
      "RESERVATION_REJECTED_CLIENT", "AUCTION_OUTBID_CLIENT"
    ];
    for (var i = 0; i < types.length; i++) {
      typeTitles[types[i]] = sidebar.getAttribute("data-title-" + types[i]) || types[i];
    }

    var needsFetch = true;

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
      if (needsFetch) {
        needsFetch = false;
        fetchNotifications();
      }
      if (closeBtn) closeBtn.focus();
    }

    function closeSidebar() {
      setOpen(false);
    }

    bell.addEventListener("click", function (e) {
      e.preventDefault();
      e.stopPropagation();
      if (isOpen()) { closeSidebar(); } else { openSidebar(); }
    });

    backdrop.addEventListener("click", closeSidebar);

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

    function fetchNotifications() {
      fetch(ctx + "/notifications", { credentials: "same-origin" })
        .then(function (r) { return r.json(); })
        .then(function (items) { renderItems(items); })
        .catch(function () { renderItems([]); });
    }

    function renderItems(items) {
      var articles = listContainer.querySelectorAll(".notification-item");
      for (var j = 0; j < articles.length; j++) articles[j].remove();

      for (var k = 0; k < items.length; k++) {
        listContainer.insertAdjacentHTML("beforeend", renderItem(items[k]));
      }
      checkEmpty();
    }

    function renderItem(item) {
      var isRead = item.read;
      var title = typeTitles[item.type] || item.type;
      var body = buildBody(item);
      var time = relativeTime(item.createdAt);

      var actionBtn = isRead
        ? '<button type="button" class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-primary" title="' + esc(labels.markUnread) + '" data-action="mark-unread" aria-label="' + esc(labels.markUnread) + '">mark_as_unread</button>'
        : '<button type="button" class="notification-item__action material-symbols-outlined text-sm text-primary" title="' + esc(labels.markRead) + '" data-action="mark-read" aria-label="' + esc(labels.markRead) + '">check_circle</button>';

      var deleteBtn = '<button type="button" class="notification-item__action material-symbols-outlined text-sm text-on-surface-variant hover:text-error" title="' + esc(labels.del) + '" data-action="delete" aria-label="' + esc(labels.del) + '">delete</button>';

      return '<article class="notification-item group/item p-4 flex gap-3' + (isRead ? '' : ' notification-item--unread') + '" data-notification-id="' + item.id + '">'
        + '<span class="notification-item__dot w-2 h-2 rounded-full mt-2 shrink-0 ' + (isRead ? 'bg-transparent' : 'bg-primary') + '" aria-hidden="true"></span>'
        + '<div class="flex-1 min-w-0">'
        +   '<div class="flex justify-between items-start gap-2 mb-1">'
        +     '<p class="text-sm ' + (isRead ? 'text-on-surface opacity-70' : 'font-bold text-on-surface') + '">' + esc(title) + '</p>'
        +     '<span class="text-[10px] text-on-surface-variant shrink-0">' + esc(time) + '</span>'
        +   '</div>'
        +   '<p class="text-xs text-on-surface-variant mb-2">' + esc(body) + '</p>'
        +   '<div class="notification-item__actions flex gap-2 opacity-0 group-hover/item:opacity-100 transition-opacity">'
        +     actionBtn + deleteBtn
        +   '</div>'
        + '</div>'
        + '</article>';
    }

    function buildBody(item) {
      var parts = [];
      if (item.commerceName) parts.push(item.commerceName);
      if (item.packTitle) parts.push(item.packTitle);
      if (item.pickupCode) parts.push("\u00B7 " + item.pickupCode);
      if (item.amount != null) parts.push("\u00B7 $" + formatAmount(item.amount));
      return parts.join(" ") || "";
    }

    function formatAmount(n) {
      return Number(n).toFixed(2);
    }

    function relativeTime(iso) {
      if (!iso) return "";
      var then = new Date(iso);
      var now = new Date();
      var diffMs = now.getTime() - then.getTime();
      if (diffMs < 0) diffMs = 0;
      var mins = Math.floor(diffMs / 60000);
      if (mins < 1) return labels.justNow;
      if (mins < 60) return mins + " min";
      var hours = Math.floor(mins / 60);
      if (hours < 24) return hours + " h";
      var days = Math.floor(hours / 24);
      return days + " d";
    }

    function checkEmpty() {
      var hasItems = !!listContainer.querySelector(".notification-item");
      if (emptyEl) {
        emptyEl.classList.toggle("hidden", hasItems);
      }
    }

    function updateBadge() {
      fetch(ctx + "/notifications/unread-count", { credentials: "same-origin" })
        .then(function (r) { return r.json(); })
        .then(function (data) {
          var badge = bell.querySelector(".notification-bell__badge");
          if (data.count > 0) {
            if (!badge) {
              badge = document.createElement("span");
              badge.className = "notification-bell__badge";
              badge.setAttribute("aria-hidden", "true");
              bell.appendChild(badge);
            }
            badge.textContent = data.count;
          } else if (badge) {
            badge.remove();
          }
        })
        .catch(function () {});
    }

    function postAction(url) {
      return fetch(url, { method: "POST", credentials: "same-origin" });
    }

    function replaceActions(article, nowRead) {
      var dot = article.querySelector(".notification-item__dot");
      var titleP = article.querySelector(".flex-1 .text-sm");
      var actionsDiv = article.querySelector(".notification-item__actions");
      if (!actionsDiv) return;

      if (dot) {
        dot.classList.toggle("bg-primary", !nowRead);
        dot.classList.toggle("bg-transparent", nowRead);
      }
      if (titleP) {
        if (nowRead) {
          titleP.classList.remove("font-bold", "text-on-surface");
          titleP.classList.add("text-on-surface", "opacity-70");
        } else {
          titleP.classList.remove("opacity-70");
          titleP.classList.add("font-bold", "text-on-surface");
        }
      }

      var readUnreadBtn = actionsDiv.querySelector("[data-action='mark-read'], [data-action='mark-unread']");
      if (readUnreadBtn) {
        if (nowRead) {
          readUnreadBtn.setAttribute("data-action", "mark-unread");
          readUnreadBtn.setAttribute("title", labels.markUnread);
          readUnreadBtn.setAttribute("aria-label", labels.markUnread);
          readUnreadBtn.textContent = "mark_as_unread";
          readUnreadBtn.classList.remove("text-primary");
          readUnreadBtn.classList.add("text-on-surface-variant", "hover:text-primary");
        } else {
          readUnreadBtn.setAttribute("data-action", "mark-read");
          readUnreadBtn.setAttribute("title", labels.markRead);
          readUnreadBtn.setAttribute("aria-label", labels.markRead);
          readUnreadBtn.textContent = "check_circle";
          readUnreadBtn.classList.remove("text-on-surface-variant");
          readUnreadBtn.classList.add("text-primary");
        }
      }
    }

    listContainer.addEventListener("click", function (e) {
      var btn = e.target.closest("[data-action]");
      if (!btn) return;
      var action = btn.getAttribute("data-action");
      var article = btn.closest("[data-notification-id]");
      var id = article ? article.getAttribute("data-notification-id") : null;

      if (action === "mark-read" && id) {
        postAction(ctx + "/notifications/" + id + "/read").then(function () {
          article.classList.remove("notification-item--unread");
          replaceActions(article, true);
          updateBadge();
        });
      } else if (action === "mark-unread" && id) {
        postAction(ctx + "/notifications/" + id + "/unread").then(function () {
          article.classList.add("notification-item--unread");
          replaceActions(article, false);
          updateBadge();
        });
      } else if (action === "delete" && id) {
        postAction(ctx + "/notifications/" + id + "/delete").then(function () {
          article.remove();
          checkEmpty();
          updateBadge();
        });
      }
    });

    var markAllBtn = sidebar.querySelector("[data-action='mark-all-read']");
    if (markAllBtn) {
      markAllBtn.addEventListener("click", function () {
        postAction(ctx + "/notifications/read-all").then(function () {
          var unreadItems = listContainer.querySelectorAll(".notification-item--unread");
          for (var u = 0; u < unreadItems.length; u++) {
            unreadItems[u].classList.remove("notification-item--unread");
            replaceActions(unreadItems[u], true);
          }
          updateBadge();
        });
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initNotificationSidebar);
  } else {
    initNotificationSidebar();
  }

  function esc(str) {
    if (!str) return "";
    var d = document.createElement("div");
    d.appendChild(document.createTextNode(str));
    return d.innerHTML;
  }
})();
