# PAW 2026a-03: Gastronomic Surplus Rescue Platform

## Project Overview
This project is a web platform designed to reduce food waste in gastronomic establishments (e.g., restaurants, bakeries, cafes), following a model similar to "TooGoodToGo". 
- Commerces publish their surplus food "packs" at reduced prices at the end of the day.
- Customers explore, reserve, and pick up these packs based on location and preferences.
- **Main Goal:** Create an ecosystem where commerces reduce economic losses and users access food at lower costs, promoting responsible and sustainable consumption.
- **Features:** Search and reservation flow, dynamic pricing through auctions (Auction and Bids), notifications, favorites system, ratings, purchase history, and impact metrics (e.g., amount of rescued food).

### User Roles
- **Clients:** Search, explore, purchase/reserve packs, and place bids on active auctions.
- **Commerces:** Publish and manage the availability of their surplus food offers, either as direct sales or auctions.

## Technical Architecture

### Tech Stack
- **Language:** Java 21
- **Build Tool:** Maven (Multi-module project)
- **Architecture:** MVC (Model-View-Controller)
- **Back-end Frameworks:** Spring Framework 5.3.x (Web MVC, Context, JDBC, TX), Spring Security (authentication and HTTP authorization in the `webapp` module).
- **Database:** PostgreSQL with Flyway for database migrations.
- **Testing:** JUnit 5

### Module Structure
The project is strictly separated into multiple Maven modules to ensure decoupled architecture and separation of concerns:
- **`models`**: Domain entities and value types grouped by **domain subpackages** under `ar.edu.itba.paw.models` (e.g. `...models.user` for `User`/`Client`/`Commerce`, `...models.pack` for `Pack` and catalog enums, `...models.auction`, `...models.reservation`, `...models.security` for `Token`/`TokenType`). Prefer placing new types next to their bounded context, not the flat `models` root.
- **`*-contracts`** (`service-contracts`, `persistence-contracts`): Interfaces for DAOs and Services. **Service** interfaces and DTO-style helper types (e.g. `RegisterResult`, `PickupByCodeResult`) live under `ar.edu.itba.paw.services` in **subpackages** (`user`, `commerce`, `pack`, `auction`, `reservation`, `security`)—mirror the same names in `services` implementations.
- **`persistence`**: Data Access Object (DAO) implementations connecting to the database using Spring JDBC / `JdbcTemplate`. Contains Flyway migration scripts. Package `ar.edu.itba.paw.persistence` remains a single package for DAOs.
- **`services`**: Service implementations, one subpackage per area (same names as `service-contracts`). `@ComponentScan("ar.edu.itba.paw.services")` in `webapp` already picks up nested packages—no need to list each subpackage.
- **`webapp`**: Spring MVC Controllers, Views (JSP), custom tag components (`.tag`), and static assets (CSS, JS). Wiring of the dependency injection and configurations. Built as a `.war` and runnable via Jetty (`mvn jetty:run`).

**Rationale (short):** Large flat `models` and `services` packages made ownership and navigation harder. Subpackages express domain boundaries without changing the Maven module split.

### Spring Security (webapp)
- **Placement:** The servlet filter chain is registered in `webapp/src/main/webapp/WEB-INF/web.xml` (`DelegatingFilterProxy` → `springSecurityFilterChain`). HTTP rules, form login, logout, and remember-me are configured in `ar.edu.itba.paw.webapp.config.WebAuthConfig`. User loading and role mapping live under `ar.edu.itba.paw.webapp.auth` (e.g., `AuthUserDetailsService` implementing `UserDetailsService`; roles are exposed as `ROLE_CLIENT` and `ROLE_COMMERCE` from domain `User.Role`).
- **Views:** Use Spring Security’s JSP tag library where UI must reflect auth state (e.g., `sec:authorize` in `navbar.tag`), in addition to server-side rules on controllers.

### Frontend Patterns
- **Views**: Written in standard JSP (`.jsp` files).
- **Components**: Reusable UI elements are custom JSP tags under `WEB-INF/tags/` (grouped by area; shared pieces under `tags/shared/`). They are registered in `WEB-INF/paw.tld` (URI `http://itba.edu.ar/paw/tags`); use `<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>`.
- **Styling**: Standard CSS with a strong focus on modern aesthetics, dynamic design, gradients, hover effects, and responsive implementations (split under `webapp/src/main/webapp/css/components/`, wired from `head.tag`).
- **Design System**: The central reference for all visual and UI decisions is **[`DESIGN.md`](DESIGN.md)**. This document defines the project's color palette, typography, surface hierarchy, elevation strategy, component patterns, and do's/don'ts. All frontend changes MUST align with the guidelines described there ("ResQFood" philosophy).
- **Design Rule**: Changes on the frontend should feel premium, maintain visual consistency across components, and NOT rely on generic frameworks like Bootstrap or Tailwind unless explicitly approved or configured.

## Directives for AI Agents
When generating code or modifying the repository, strictly follow these instructions to keep consistency:

1. **Module Boundaries:** Respect the Maven multi-module dependency tree. The `webapp` module should NOT know about the `persistence` logic directly. Inject features through the `*-contracts` interfaces.
2. **Database Changes:** Any structural change to the database MUST be done by creating a new Flyway migration script in `persistence/src/main/resources/db/migration/` sequentially (e.g., `V2__name.sql`, `V3__name.sql`). Do not edit existing baseline migrations once deployed.
3. **Adding New Traits (End-to-End):** When adding new features or fields, follow the full tier-flow: 
   - Flyway Migration -> Model class update -> Interface contract -> Persistence DAO update -> Service Logic -> Controller -> JSP View.
4. **UI Harmony:** If modifying a UI component (like a pack card or search bar), ensure changes do not break other layouts. Reuse existing generic CSS classes and components instead of hardcoding redundant inline styles. Follow established UI truncation, text overflow handling, and visual conventions from `home` and `pack-detail` views. **Always consult [`DESIGN.md`](DESIGN.md)** for color tokens, typography scales, surface hierarchy, elevation rules, and component specifications before making any visual change.
5. **Enums and Mapping:** Consider encoding accents and mapping rules for enums (e.g., Category types like "Panadería") correctly before passing them to the database or view. Make robust transformations.
6. **Web Layer and Views:** The web layer must be implemented using the MVC and Front Controller patterns, via Spring Web MVC. Views must be composed of JSP files with JSTL (and should not contain Java code).
7. **Internationalization (i18n):** Whenever you add or change user-visible screens, copy, labels, buttons, error messages, or other UI text, you MUST add the corresponding keys to both [`webapp/src/main/resources/i18n/messages.properties`](webapp/src/main/resources/i18n/messages.properties) (default locale) and [`webapp/src/main/resources/i18n/messages_en.properties`](webapp/src/main/resources/i18n/messages_en.properties) (English), and resolve them in JSP/tags with Spring’s `<spring:message code="..."/>` (or equivalent) instead of hardcoding strings. Keep key naming consistent with existing prefixes (e.g. `pack.*`, `layout.*`, `error.*`).
8. **JSP Views `<head>` Boilerplate:** When creating a new JSP view, you MUST NOT repeat boilerplate `<head>...</head>` code (CSS layout, JS configs, fonts, favicons). Always use the `<paw:head>` tag (e.g., `<paw:head titleSuffixCode="..." />`, defined in `WEB-INF/tags/shared/head.tag`) or wrap the page within an existing layout component that already encapsulates it (like `paw:reservationLayout`).
9. **Security configuration:** When adding or changing URLs, HTTP methods, or role requirements, update `WebAuthConfig` so authorization matches the feature (do not rely on controller logic alone). Reuse existing role names (`CLIENT`, `COMMERCE` in `hasRole(...)`) and keep static assets under the paths already ignored by security. Align navigation and conditional UI with `sec:authorize` (or equivalent) so menus and actions stay consistent with server rules.
10. **Identity and passwords:** Resolve the current user via Spring Security (`Authentication`, `SecurityContextHolder`, or `UserDetailsService` as already done in controllers)—do not bypass the stack without a strong reason. Any new password handling must go through the existing `PasswordEncoder` bean and existing user-creation flows in services; never log or persist plaintext passwords.
11. **Manual login/session flows:** Prefer standard form login and the security filter chain. Patterns that set `SecurityContextHolder` or session attributes by hand (e.g., after email verification) are exceptional—if a feature needs similar behavior, follow existing controllers and document why the default login flow is insufficient.
12. **New types and services:** Add new model classes under the correct `ar.edu.itba.paw.models.<domain>` subpackage, and new service interfaces/impls under the matching `ar.edu.itba.paw.services.<domain>` pair. Cross-subpackage use requires explicit `import` lines (no reliance on a single “god” service package).