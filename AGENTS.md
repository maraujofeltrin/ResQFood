# PAW 2026a-03: Gastronomic Surplus Rescue Platform

## Project Overview
This project is a web platform designed to reduce food waste in gastronomic establishments (e.g., restaurants, bakeries, cafes), following a model similar to "TooGoodToGo". 
- Commerces publish their surplus food "packs" at reduced prices at the end of the day.
- Customers explore, reserve, and pick up these packs based on location and preferences.
- **Main Goal:** Create an ecosystem where commerces reduce economic losses and users access food at lower costs, promoting responsible and sustainable consumption.
- **Features:** Search and reservation flow, notifications, favorites system, ratings, purchase history, and impact metrics (e.g., amount of rescued food).

### User Roles
- **Clients:** Search, explore, and purchase/reserve packs.
- **Commerces:** Publish and manage the availability of their surplus food offers.

## Technical Architecture

### Tech Stack
- **Language:** Java 21
- **Build Tool:** Maven (Multi-module project)
- **Architecture:** MVC (Model-View-Controller)
- **Back-end Frameworks:** Spring Framework 5.3.x (Web MVC, Context, JDBC, TX)
- **Database:** PostgreSQL with Flyway for database migrations.
- **Testing:** JUnit 5

### Module Structure
The project is strictly separated into multiple Maven modules to ensure decoupled architecture and separation of concerns:
- **`models`**: Domain entities (e.g., `User`, `Commerce`, `Pack`) and basic validation.
- **`*-contracts`** (`service-contracts`, `persistence-contracts`): Interfaces for DAOs and Services to enforce dependency inversion and maintain decoupling.
- **`persistence`**: Data Access Object (DAO) implementations connecting to the database using Spring JDBC / `JdbcTemplate`. Contains Flyway migration scripts.
- **`services`**: Implementations of business logic and system operations.
- **`webapp`**: Spring MVC Controllers, Views (JSP), custom tag components (`.tag`), and static assets (CSS, JS). Wiring of the dependency injection and configurations. Built as a `.war` and runnable via Jetty (`mvn jetty:run`).

### Frontend Patterns
- **Views**: Written in standard JSP (`.jsp` files).
- **Components**: Reusable UI elements are implemented as custom JSP tags (`WEB-INF/tags/`, e.g., `packCard.tag`, `navbar.tag`).
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
8. **JSP Views `<head>` Boilerplate:** When creating a new JSP view, you MUST NOT repeat boilerplate `<head>...</head>` code (CSS layout, JS configs, fonts, favicons). Always use the `<paw:head>` tag (e.g., `<paw:head titleSuffixCode="..." />`, defined in `WEB-INF/tags/head.tag`) or wrap the page within an existing layout component that already encapsulates it (like `paw:reservationLayout`).
