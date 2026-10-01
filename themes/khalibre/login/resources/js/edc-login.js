(function () {
  "use strict";

  function lock(button) {
    if (!button || button.dataset.edcBusy === "true") {
      return;
    }
    button.dataset.edcBusy = "true";
    button.disabled = true;
    button.classList.add("is-loading");
  }

  function unlock(button) {
    if (!button) {
      return;
    }
    button.disabled = false;
    button.classList.remove("is-loading");
    delete button.dataset.edcBusy;
  }

  function init() {
    var form = document.getElementById("kc-form-login");
    var button = document.getElementById("kc-login");
    if (!form || !button) {
      return;
    }

    // Lock only from the submit event. Disabling the button from a click or keydown handler runs
    // before the browser decides to submit, which cancels the form submission entirely.
    // The submit event covers both a button click and Enter in a text field.
    form.addEventListener("submit", function () {
      lock(button);
    });

    window.addEventListener("pageshow", function () {
      unlock(button);
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
