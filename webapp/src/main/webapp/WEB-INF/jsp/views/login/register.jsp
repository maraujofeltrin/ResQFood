<%@ page contentType="text/html;charset=UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core"%> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %> <%@ taglib prefix="spring"
uri="http://www.springframework.org/tags"%> <%@ taglib prefix="paw"
uri="http://itba.edu.ar/paw/tags" %>
<!DOCTYPE html>
<html class="light" lang="${pageContext.response.locale.language}">
  <spring:message code="register.title" var="registerTitle" text="Registrate" />
  <paw:head title="${registerTitle}" />
  <body
    class="bg-surface text-on-surface antialiased flex flex-col min-h-screen"
  >
    <paw:navbar />

    <main
      class="pt-24 flex flex-col items-center justify-center flex-grow py-12 px-4"
    >
      <div
        class="w-full max-w-2xl bg-surface-container-lowest rounded-xl shadow-soft p-8 border border-outline-variant"
      >
        <h1
          class="font-headline text-3xl font-bold text-center text-primary mb-6"
        >
          <spring:message code="register.title" text="Regístrate" />
        </h1>

        <c:if test="${not empty successMessage}">
          <div class="bg-primary/10 text-primary rounded-lg p-4 mb-6 text-sm">
            <c:out value="${successMessage}" />
          </div>
        </c:if>

        <c:url value="/register" var="postPath" />
        <form:form
          modelAttribute="registerForm"
          action="${postPath}"
          method="post"
          class="space-y-5"
          novalidate="novalidate"
        >
          <c:set
            var="registerBinding"
            value="${requestScope['org.springframework.validation.BindingResult.registerForm']}"
          />
          <c:if
            test="${registerBinding != null and not empty registerBinding.globalErrors}"
          >
            <div
              class="bg-error-container text-on-error-container rounded-lg p-4 mb-6 text-sm"
            >
              <c:forEach var="error" items="${registerBinding.globalErrors}">
                <p>
                  <spring:message
                    code="${error.code}"
                    text="${error.defaultMessage}"
                  />
                </p>
              </c:forEach>
            </div>
          </c:if>

          <div>
            <form:label
              path="credentials.email"
              cssClass="block font-label text-sm font-medium text-secondary mb-1"
            >
              <spring:message
                code="register.label.email"
                text="Correo electrónico"
              />
            </form:label>
            <spring:message
              code="register.placeholder.email"
              var="emailPlaceholder"
              text="nombre@ejemplo.com"
            />
            <form:input
              type="email"
              path="credentials.email"
              cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${emailPlaceholder}"
            />
            <form:errors
              path="credentials.email"
              cssClass="text-error text-sm mt-1"
              element="p"
            />
          </div>

          <div>
            <form:label
              path="credentials.phone"
              cssClass="block font-label text-sm font-medium text-secondary mb-1"
            >
              <spring:message code="register.label.phone" text="Teléfono" />
            </form:label>
            <spring:message
              code="register.placeholder.phone"
              var="phonePlaceholder"
              text="Ej: 1134656787"
            />
            <form:input
              type="text"
              path="credentials.phone"
              cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
              placeholder="${phonePlaceholder}"
            />
            <form:errors
              path="credentials.phone"
              cssClass="text-error text-sm mt-1"
              element="p"
            />
          </div>

          <div>
            <form:label
              path="credentials.password"
              cssClass="block font-label text-sm font-medium text-secondary mb-1"
            >
              <spring:message
                code="register.label.password"
                text="Contraseña"
              />
            </form:label>
            <spring:message
              code="register.placeholder.password"
              var="passwordPlaceholder"
              text="••••••••"
            />
            <div class="password-wrapper">
              <form:password
                path="credentials.password"
                showPassword="false"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${passwordPlaceholder}"
              />
              <button type="button" class="password-toggle" aria-label="Toggle password visibility">
                <span class="material-symbols-outlined">visibility</span>
              </button>
            </div>
            <form:errors
              path="credentials.password"
              cssClass="text-error text-sm mt-1"
              element="p"
            />
          </div>

          <div>
            <form:label
              path="credentials.repeatPassword"
              cssClass="block font-label text-sm font-medium text-secondary mb-1"
            >
              <spring:message
                code="register.label.repeatPassword"
                text="Repetir contraseña"
              />
            </form:label>
            <spring:message
              code="register.placeholder.repeatPassword"
              var="repeatPlaceholder"
              text="••••••••"
            />
            <div class="password-wrapper">
              <form:password
                path="credentials.repeatPassword"
                showPassword="false"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${repeatPlaceholder}"
              />
              <button type="button" class="password-toggle" aria-label="Toggle password visibility">
                <span class="material-symbols-outlined">visibility</span>
              </button>
            </div>
            <form:errors
              path="credentials.repeatPassword"
              cssClass="text-error text-sm mt-1"
              element="p"
            />
          </div>

          <div class="space-y-3">
            <form:label
              path="role"
              cssClass="block font-label text-sm font-medium text-secondary mb-1"
            >
              <spring:message
                code="register.label.role"
                text="Tipo de cuenta"
              />
            </form:label>
            <div
              id="roleSelector"
              class="grid grid-cols-1 sm:grid-cols-2 gap-3"
              role="radiogroup"
              aria-label="Role selector"
            >
              <label
                for="role-client"
                class="cursor-pointer rounded-lg border border-outline px-4 py-3 transition-colors has-[:checked]:border-primary has-[:checked]:bg-primary/10"
              >
                <div class="flex items-center gap-3">
                  <form:radiobutton
                    path="role"
                    id="role-client"
                    value="CLIENT"
                    cssClass="text-primary focus:ring-primary border-outline"
                  />
                  <span class="font-medium text-on-surface">
                    <spring:message
                      code="register.role.client"
                      text="Cliente"
                    />
                  </span>
                </div>
              </label>
              <label
                for="role-commerce"
                class="cursor-pointer rounded-lg border border-outline px-4 py-3 transition-colors has-[:checked]:border-primary has-[:checked]:bg-primary/10"
              >
                <div class="flex items-center gap-3">
                  <form:radiobutton
                    path="role"
                    id="role-commerce"
                    value="COMMERCE"
                    cssClass="text-primary focus:ring-primary border-outline"
                  />
                  <span class="font-medium text-on-surface">
                    <spring:message
                      code="register.role.commerce"
                      text="Comercio"
                    />
                  </span>
                </div>
              </label>
            </div>
            <form:errors
              path="role"
              cssClass="text-error text-sm mt-1"
              element="p"
            />
          </div>

          <section
            id="client-data-section"
            class="space-y-4 border border-outline-variant rounded-lg p-4 hidden"
          >
            <h2 class="font-headline text-lg text-primary">
              <spring:message
                code="register.section.client.title"
                text="Datos de Cliente"
              />
            </h2>

            <div>
              <form:label
                path="clientProfile.firstName"
                cssClass="block font-label text-sm font-medium text-secondary mb-1"
              >
                <spring:message
                  code="register.label.clientName"
                  text="Nombre"
                />
              </form:label>
              <spring:message
                code="register.placeholder.clientName"
                var="clientNamePlaceholder"
                text="Ej: María"
              />
              <form:input
                type="text"
                path="clientProfile.firstName"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${clientNamePlaceholder}"
                data-required="true"
              />
              <form:errors
                path="clientProfile.firstName"
                cssClass="text-error text-sm mt-1"
                element="p"
              />
            </div>

            <div>
              <form:label
                path="clientProfile.lastName"
                cssClass="block font-label text-sm font-medium text-secondary mb-1"
              >
                <spring:message
                  code="register.label.clientLastName"
                  text="Apellido"
                />
              </form:label>
              <spring:message
                code="register.placeholder.clientLastName"
                var="clientLastNamePlaceholder"
                text="Ej: García"
              />
              <form:input
                type="text"
                path="clientProfile.lastName"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${clientLastNamePlaceholder}"
                data-required="true"
              />
              <form:errors
                path="clientProfile.lastName"
                cssClass="text-error text-sm mt-1"
                element="p"
              />
            </div>

            <div class="flex items-center gap-2">
              <form:checkbox
                path="clientProfile.notificationsVisibilityPreferences"
                id="notificationsVisibilityPreferences"
                cssClass="text-primary focus:ring-primary border-outline"
              />
              <label
                for="notificationsVisibilityPreferences"
                class="text-sm text-on-surface"
              >
                <spring:message
                  code="register.label.notificationsVisibilityPreferences"
                  text="Recibir notificaciones por correo"
                />
              </label>
            </div>
          </section>

          <section
            id="commerce-data-section"
            class="space-y-4 border border-outline-variant rounded-lg p-4 hidden"
          >
            <h2 class="font-headline text-lg text-primary">
              <spring:message
                code="register.section.commerce.title"
                text="Datos de Comercio"
              />
            </h2>

            <div>
              <form:label
                path="commerceProfile.commercialName"
                cssClass="block font-label text-sm font-medium text-secondary mb-1"
              >
                <spring:message
                  code="register.label.commercialName"
                  text="Nombre comercial"
                />
              </form:label>
              <spring:message
                code="register.placeholder.commercialName"
                var="commercialNamePlaceholder"
                text="Ej: La Gran Panadería"
              />
              <form:input
                type="text"
                path="commerceProfile.commercialName"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${commercialNamePlaceholder}"
                data-required="true"
              />
              <form:errors
                path="commerceProfile.commercialName"
                cssClass="text-error text-sm mt-1"
                element="p"
              />
            </div>

            <div>
              <form:label
                path="commerceProfile.category"
                cssClass="block font-label text-sm font-medium text-secondary mb-1"
              >
                <spring:message
                  code="register.label.category"
                  text="Categoría"
                />
              </form:label>
              <form:select
                path="commerceProfile.category"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                data-required="true"
              >
                <form:option value="" label="--" />
                <form:option value="BAKERY"
                  ><spring:message
                    code="commerce.category.BAKERY"
                    text="Panadería"
                /></form:option>
                <form:option value="RESTAURANT"
                  ><spring:message
                    code="commerce.category.RESTAURANT"
                    text="Restaurante"
                /></form:option>
                <form:option value="GREENGROCER"
                  ><spring:message
                    code="commerce.category.GREENGROCER"
                    text="Verdulería"
                /></form:option>
                <form:option value="OTHER"
                  ><spring:message code="commerce.category.OTHER" text="Otros"
                /></form:option>
              </form:select>
              <form:errors
                path="commerceProfile.category"
                cssClass="text-error text-sm mt-1"
                element="p"
              />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <form:label
                  path="commerceProfile.street"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message code="register.label.street" text="Calle" />
                </form:label>
                <spring:message
                  code="register.placeholder.street"
                  var="streetPlaceholder"
                  text="Av. Siempre Viva"
                />
                <form:input
                  type="text"
                  path="commerceProfile.street"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                  placeholder="${streetPlaceholder}"
                  data-required="true"
                />
                <form:errors
                  path="commerceProfile.street"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>

              <div>
                <form:label
                  path="commerceProfile.streetNumber"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message
                    code="register.label.streetNumber"
                    text="Número"
                  />
                </form:label>
                <spring:message
                  code="register.placeholder.streetNumber"
                  var="streetNumberPlaceholder"
                  text="123"
                />
                <form:input
                  type="number"
                  path="commerceProfile.streetNumber"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                  placeholder="${streetNumberPlaceholder}"
                  data-required="true"
                />
                <form:errors
                  path="commerceProfile.streetNumber"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <form:label
                  path="commerceProfile.city"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message code="register.label.city" text="Ciudad" />
                </form:label>
                <spring:message
                  code="register.placeholder.city"
                  var="cityPlaceholder"
                  text="Buenos Aires"
                />
                <form:select
                  path="commerceProfile.city"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                  data-required="true"
                >
                  <form:option value="" label="${cityPlaceholder}" />
                  <c:forEach var="muni" items="${availableMunicipalities}">
                    <form:option value="${muni.name()}">
                      <spring:message
                        code="pack.catalog.filter.location.municipality.${muni.name()}"
                      />
                    </form:option>
                  </c:forEach>
                </form:select>
                <form:errors
                  path="commerceProfile.city"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>

              <div>
                <form:label
                  path="commerceProfile.province"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message
                    code="register.label.province"
                    text="Provincia"
                  />
                </form:label>
                <spring:message
                  code="register.placeholder.province"
                  var="provincePlaceholder"
                  text="Buenos Aires"
                />
                <form:input
                  type="text"
                  path="commerceProfile.province"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none cursor-not-allowed bg-surface-variant/50"
                  placeholder="${provincePlaceholder}"
                  readonly="true"
                  disabled="true"
                  data-always-disabled="true"
                />
                <form:errors
                  path="commerceProfile.province"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>
            </div>

            <div>
              <form:label
                path="commerceProfile.postalCode"
                cssClass="block font-label text-sm font-medium text-secondary mb-1"
              >
                <spring:message
                  code="register.label.postalCode"
                  text="Código postal"
                />
              </form:label>
              <spring:message
                code="register.placeholder.postalCode"
                var="postalCodePlaceholder"
                text="C1425"
              />
              <form:input
                type="text"
                path="commerceProfile.postalCode"
                cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                placeholder="${postalCodePlaceholder}"
                data-required="true"
              />
              <form:errors
                path="commerceProfile.postalCode"
                cssClass="text-error text-sm mt-1"
                element="p"
              />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <form:label
                  path="commerceProfile.openingTime"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message
                    code="register.label.openingTime"
                    text="Horario de apertura"
                  />
                </form:label>
                <form:input
                  type="time"
                  path="commerceProfile.openingTime"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                  placeholder="08:00"
                  data-required="true"
                />
                <form:errors
                  path="commerceProfile.openingTime"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>

              <div>
                <form:label
                  path="commerceProfile.closingTime"
                  cssClass="block font-label text-sm font-medium text-secondary mb-1"
                >
                  <spring:message
                    code="register.label.closingTime"
                    text="Horario de cierre"
                  />
                </form:label>
                <form:input
                  type="time"
                  path="commerceProfile.closingTime"
                  cssClass="w-full px-4 py-3 rounded-lg border border-outline hover:border-primary focus:border-primary focus:ring-2 focus:ring-primary/20 transition-colors bg-surface-container-lowest text-on-surface outline-none"
                  placeholder="20:00"
                  data-required="true"
                />
                <form:errors
                  path="commerceProfile.closingTime"
                  cssClass="text-error text-sm mt-1"
                  element="p"
                />
              </div>
            </div>
          </section>

          <button
            type="submit"
            class="w-full bg-primary text-on-primary font-semibold py-3 px-4 rounded-full transition-colors shadow-soft hover:shadow-lifted mt-6 h-12 flex items-center justify-center"
          >
            <spring:message code="register.submit" text="Crear cuenta" />
          </button>
        </form:form>

        <div class="mt-6 text-center text-sm text-secondary">
          <spring:message
            code="register.prompt.hasAccount"
            text="¿Ya tienes cuenta?"
          />
          <a
            href="${pageContext.request.contextPath}/login"
            class="text-primary font-medium hover:underline"
          >
            <spring:message
              code="register.link.login"
              text="Inicia sesión aquí"
            />
          </a>
        </div>
      </div>
    </main>
    <paw:footer />

    <script>
      (function () {
        const roleSelector = document.getElementById("roleSelector");
        const roleInputs = document.querySelectorAll('input[name="role"]');
        const clientSection = document.getElementById("client-data-section");
        const commerceSection = document.getElementById(
          "commerce-data-section",
        );

        function applySectionState(section, active) {
          if (!section) {
            return;
          }

          section.style.display = active ? "block" : "none";

          const allFields = section.querySelectorAll("input:not([data-always-disabled]), select, textarea");
          allFields.forEach(function (field) {
            field.disabled = !active;
          });

          const fields = section.querySelectorAll('[data-required="true"]');
          fields.forEach(function (field) {
            if (active) {
              field.setAttribute("required", "required");
            } else {
              field.removeAttribute("required");
            }
          });
        }

        function toggleSections() {
          const selected = document.querySelector('input[name="role"]:checked');
          const role = selected ? selected.value : "";
          const isClient = role === "CLIENT";
          const isCommerce = role === "COMMERCE";

          applySectionState(clientSection, isClient);
          applySectionState(commerceSection, isCommerce);
        }

        if (roleSelector && roleInputs.length > 0) {
          roleInputs.forEach(function (input) {
            input.addEventListener("change", toggleSections);
          });
          toggleSections();
        }
      })();
    </script>
    <script src="${pageContext.request.contextPath}/js/password-toggle.js"></script>
  </body>
</html>
