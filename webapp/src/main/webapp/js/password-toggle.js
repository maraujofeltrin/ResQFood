/**
 * Password visibility toggle.
 *
 * Finds every element with class "password-toggle" and binds a click
 * handler that switches the sibling input between type="password" and
 * type="text", updating the Material Symbols icon accordingly.
 */
(function () {
  document.querySelectorAll(".password-toggle").forEach(function (btn) {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      var wrapper = btn.closest(".password-wrapper");
      if (!wrapper) return;

      var input = wrapper.querySelector("input");
      if (!input) return;

      var isPassword = input.type === "password";
      input.type = isPassword ? "text" : "password";

      var icon = btn.querySelector(".material-symbols-outlined");
      if (icon) {
        icon.textContent = isPassword ? "visibility_off" : "visibility";
      }
    });
  });
})();
