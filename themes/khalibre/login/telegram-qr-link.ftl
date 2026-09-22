<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayMessage=true; section>
  <#if section = "header">
    <#if linkMode>${msg("telegram.link-title")}<#else>${msg("telegram.title")}</#if>
  <#elseif section = "form">
    <script src="${url.resourcesPath}/js/qr-code-styling-1.9.2.js"></script>

    <#if linkMode>
      <p class="subtitle">${msg("telegram.link-description")}</p>
    <#else>
      <p class="subtitle">${msg("telegram.description")}</p>
    </#if>

    <div id="qr-container"></div>
    <div class="progress-bar-container">
      <div id="progress-bar" class="progress-bar-fill"></div>
    </div>
    <p class="hint">
      ${msg("telegram.using-this-device")}
      <a id="qr-link" class="kh-link" href="#" data-url="">${msg("doClickHere")}</a>
    </p>

    <script>
      (function () {
        const duration = 60;
        let winQrLink;
        let interval;
        let checkLinkInterval;
        let qrCode;
        let timeLeft = duration;
        let isRedirecting = false;

        const apiBase = "/realms/${realm.name}";
        const providerAlias = "${providerAlias!}";
        const callbackUrl = "${callbackUrl!}";

        async function checkAuthStatus() {
          if (isRedirecting) return;
          try {
            const response = await fetch(apiBase + "/telegram-auth/status");
            const data = await response.json();
            if (data.status === 'COMPLETED') {
              isRedirecting = true;
              clearInterval(interval);
              clearInterval(checkLinkInterval);
              if (winQrLink) winQrLink.close();
              window.location.href = callbackUrl;
            }
          } catch (e) {
            console.error("Error checking auth status:", e);
          }
        }

        function fetchQRCodeDataAndRender() {
          fetch(apiBase + "/telegram-auth/" + providerAlias + "/qr")
            .then(function (response) { return response.json(); })
            .then(function (data) {
              const qrLink = document.getElementById("qr-link");
              qrLink.dataset.url = data.deepLink;
              const container = document.getElementById("qr-container");
              container.innerHTML = "";

              qrCode = new QRCodeStyling({
                width: 240,
                height: 240,
                type: "svg",
                data: data.deepLink,
                image: "${url.resourcesPath}/img/telegram.svg",
                dotsOptions: { color: "#000000", type: "rounded" },
                cornersSquareOptions: { type: "extra-rounded" },
                imageOptions: { saveAsBlob: true, imageSize: 0.4, margin: 0 },
                backgroundOptions: { color: "#ffffff" },
                qrOptions: { errorCorrectionLevel: "M" }
              });
              qrCode.append(container);
              startProgressBar();
            })
            .catch(function () {
              console.error("Failed to fetch QR code.");
            });
        }

        document.getElementById("qr-link").addEventListener("click", function (event) {
          event.preventDefault();
          const url = this.dataset.url;
          if (url) {
            winQrLink = window.open(url, "_blank");
          }
        });

        function startProgressBar() {
          clearInterval(interval);
          clearInterval(checkLinkInterval);
          timeLeft = duration;
          const qrContainer = document.getElementById("qr-container");
          const progressBar = document.getElementById("progress-bar");
          if (qrContainer) qrContainer.classList.remove("blurred");
          if (progressBar) progressBar.style.width = "100%";

          checkLinkInterval = setInterval(function () {
            checkAuthStatus();
          }, 3000);

          interval = setInterval(function () {
            timeLeft--;
            const percent = (timeLeft / duration) * 100;
            if (progressBar) progressBar.style.width = percent + "%";

            if (timeLeft <= 0) {
              clearInterval(interval);
              clearInterval(checkLinkInterval);
              if (qrContainer) qrContainer.classList.add("blurred");
              setTimeout(function () {
                fetchQRCodeDataAndRender();
              }, 2000);
            }
          }, 1000);
        }

        fetch(apiBase + "/telegram-auth/" + providerAlias + "/init", { method: 'POST' })
          .then(function (r) { return r.json(); })
          .then(function (d) {
            if (d.ok) {
              console.log("Bot initialized in " + d.mode + " mode");
            } else {
              console.error("Bot init failed:", d.error);
            }
          })
          .catch(function (e) {
            console.error("Bot init error:", e);
          });

        fetchQRCodeDataAndRender();
      })();
    </script>

    <div class="${properties.kcFormOptionsWrapperClass!}">
        <span><a href="${url.loginUrl}">${msg("backToLogin")}</a></span>
    </div>
  <#elseif section = "socialProviders">
  </#if>
</@layout.registrationLayout>
