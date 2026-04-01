<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="paw" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html class="light" lang="es">
<head>
    <meta charset="utf-8"/>
    <meta content="width=device-width, initial-scale=1.0" name="viewport"/>
    <c:choose>
        <c:when test="${not empty pageTitle}">
            <title><c:out value="${pageTitle}"/></title>
        </c:when>
        <c:otherwise>
            <title>La Despensa Viva</title>
        </c:otherwise>
    </c:choose>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script id="tailwind-config">
        tailwind.config = {
          darkMode: "class",
          theme: {
            extend: {
              colors: {
                "tertiary-container": "#653900",
                "on-secondary-container": "#5b617c",
                "error-container": "#ffdad6",
                "surface-container-high": "#e9e7ed",
                "surface-container-low": "#f5f3f9",
                "outline": "#757681",
                "on-surface-variant": "#454650",
                "on-primary-fixed": "#001550",
                "primary-fixed-dim": "#b6c4ff",
                "on-surface": "#1b1b20",
                "on-secondary": "#ffffff",
                "surface-tint": "#4a5b9a",
                "error": "#ba1a1a",
                "background": "#fbf8fe",
                "inverse-on-surface": "#f2f0f6",
                "surface-dim": "#dbd9df",
                "on-background": "#1b1b20",
                "primary": "#152965",
                "surface-bright": "#fbf8fe",
                "inverse-surface": "#303035",
                "surface": "#fbf8fe",
                "on-primary-container": "#9daef3",
                "on-error": "#ffffff",
                "surface-variant": "#e3e1e7",
                "on-tertiary-container": "#e3a464",
                "on-secondary-fixed": "#141a31",
                "inverse-primary": "#b6c4ff",
                "surface-container-highest": "#e3e1e7",
                "primary-fixed": "#dce1ff",
                "tertiary": "#462600",
                "on-primary-fixed-variant": "#314380",
                "primary-container": "#2e407d",
                "on-tertiary-fixed": "#2c1600",
                "secondary-container": "#d9defe",
                "secondary": "#575d78",
                "on-error-container": "#93000a",
                "secondary-fixed-dim": "#bfc5e4",
                "outline-variant": "#c5c5d1",
                "on-primary": "#ffffff",
                "on-tertiary-fixed-variant": "#693c02",
                "tertiary-fixed-dim": "#fcb977",
                "secondary-fixed": "#dce1ff",
                "on-secondary-fixed-variant": "#40465f",
                "on-tertiary": "#ffffff",
                "tertiary-fixed": "#ffdcbe",
                "surface-container-lowest": "#ffffff",
                "surface-container": "#efedf3"
              },
              fontFamily: {
                "headline": ["Plus Jakarta Sans"],
                "body": ["Be Vietnam Pro"],
                "label": ["Plus Jakarta Sans"]
              },
              borderRadius: {"DEFAULT": "0.25rem", "lg": "0.5rem", "xl": "0.75rem", "full": "9999px"},
            },
          },
        }
    </script>
</head>
<body class="bg-background font-body text-on-surface flex flex-col min-h-screen antialiased">
    <paw:navbar />

    <main class="pack-detail-main">
        <div class="pack-detail-grid">
            <div class="pack-detail-media-col">
                <div class="pack-detail-hero">
                    <img class="pack-detail-hero-img"
                         src="${pageContext.request.contextPath}/packs/${packId}/image"
                         alt="Imagen del pack"/>
                </div>

                <div class="pack-detail-intro">
                    <div class="pack-detail-meta">
                        <span class="pack-detail-badge">SURPRISE PACK</span>
                        <span class="pack-detail-merchant"><c:out value="${commerceCommercialName}"/></span>
                    </div>
                    <h1 class="pack-detail-title font-headline"><c:out value="${packTitle}"/></h1>
                    <p class="pack-detail-description"><c:out value="${packDescription}"/></p>
                    <section class="pack-detail-commerce" aria-label="Informacion del comercio">
                        <div class="commerce-info-card">
                            <div class="commerce-info-card__header">
                                <span class="material-symbols-outlined commerce-info-card__icon commerce-info-card__icon--hero" aria-hidden="true">storefront</span>
                                <h2 class="commerce-info-card__title font-headline">Informacion del comercio</h2>
                            </div>
                            <div class="commerce-info-card__grid">
                                <div class="commerce-info-card__column">
                                    <h3 class="commerce-info-card__section-title font-headline">
                                        <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">location_on</span>
                                        Ubicacion
                                    </h3>
                                    <div class="commerce-info-card__address">
                                        <p class="commerce-info-card__store-name"><c:out value="${commerceCommercialName}"/></p>
                                        <p><c:out value="${commerceStreetLine}"/></p>
                                        <p><c:out value="${commerceLocationLine}"/></p>
                                    </div>
                                </div>
                                <div class="commerce-info-card__column">
                                    <h3 class="commerce-info-card__section-title font-headline">
                                        <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">schedule</span>
                                        Horarios de atencion
                                    </h3>
                                    <div class="commerce-info-card__hours">
                                        <div class="commerce-info-card__hours-row">
                                            <span class="commerce-info-card__hours-label">Horario de apertura:</span>
                                            <span class="commerce-info-card__hours-value"><c:out value="${commerceOpeningTime}"/></span>
                                        </div>
                                        <div class="commerce-info-card__hours-row">
                                            <span class="commerce-info-card__hours-label">Horario de cierre:</span>
                                            <span class="commerce-info-card__hours-value"><c:out value="${commerceClosingTime}"/></span>
                                        </div>
                                        <c:if test="${commerceOpenNow}">
                                            <div class="commerce-info-card__status">
                                                <span class="commerce-info-card__status-badge">ABIERTO AHORA</span>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                            <div class="commerce-info-card__note">
                                <span class="material-symbols-outlined commerce-info-card__icon" aria-hidden="true">info</span>
                                <p class="commerce-info-card__note-text">
                                    Por favor, llega durante la franja horaria de retiro seleccionada. Ten listo en tu telefono el codigo de reserva para un retiro rapido.
                                </p>
                            </div>
                        </div>
                    </section>
                </div>
            </div>

            <aside class="pack-detail-aside">
                <div class="pack-aside-card">
                    <c:if test="${reservationAlertKind eq 'success'}">
                        <p class="pack-feedback pack-feedback--success" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>
                    <c:if test="${reservationAlertKind eq 'error'}">
                        <p class="pack-feedback pack-feedback--error" role="alert"><c:out value="${reservationAlertMessage}"/></p>
                    </c:if>

                    <h2 class="pack-aside-heading font-headline">Reserva este pack</h2>

                    <c:url var="reservationAction" value="/packs/${packId}/reserve"/>
                    <form class="pack-reservation-form" method="post" action="${reservationAction}">

                        <div class="pack-price-block">
                            <p class="pack-price-block-label">Precio por pack</p>
                            <p class="pack-price-original"><c:out value="${originalPrice}"/></p>
                            <p class="pack-price-final font-headline"><c:out value="${finalPrice}"/></p>
                        </div>

                        <div class="pack-form-field">
                            <label class="pack-form-label" for="firstName">Nombre</label>
                            <input id="firstName" name="firstName" type="text" required maxlength="255" class="pack-form-control"/>
                        </div>
                        <div class="pack-form-field">
                            <label class="pack-form-label" for="lastName">Apellido</label>
                            <input id="lastName" name="lastName" type="text" required maxlength="255" class="pack-form-control"/>
                        </div>
                        <div class="pack-form-field">
                            <label class="pack-form-label" for="email">Correo electronico</label>
                            <input id="email" name="email" type="email" required maxlength="255" autocomplete="email" class="pack-form-control"/>
                        </div>
                        <div class="pack-form-field">
                            <label class="pack-form-label" for="phone">Telefono</label>
                            <input id="phone" name="phone" type="tel" required maxlength="50" autocomplete="tel" class="pack-form-control"/>
                        </div>

                        <div class="pack-form-field">
                            <label class="pack-form-label" for="quantity">Cantidad de packs</label>
                            <input id="quantity" name="quantity" type="number" required min="1" max="<c:out value="${quantityMax}"/>" step="1"
                                   value="1"
                                   class="pack-form-control pack-form-control--tabular"/>
                        </div>

                        <div class="pack-form-field">
                            <p class="pack-form-total-label">Total</p>
                            <p class="pack-form-total-hint">El monto cobrado sera el precio por pack multiplicado por la cantidad que selecciones.</p>
                        </div>

                        <div class="pack-form-field">
                            <label class="pack-form-label" for="pickupWindow">Franja horaria de retiro</label>
                            <div class="pack-select-wrap">
                                <select id="pickupWindow" name="pickupWindow" required class="pack-form-select bg-none">
                                    <c:forEach var="window" items="${pickupWindows}">
                                        <option value="<c:out value="${window}"/>"><c:out value="${window}"/></option>
                                    </c:forEach>
                                </select>
                                <span class="material-symbols-outlined pack-select-chevron">expand_more</span>
                            </div>
                        </div>

                        <button type="submit" class="pack-submit-btn font-headline">
                            Confirmar reserva
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </button>
                    </form>
                </div>
            </aside>
        </div>
    </main>

    <paw:footer />
</body>
</html>
