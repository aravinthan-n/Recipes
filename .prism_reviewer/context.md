# Savourly Recipes - Project Context (`context.md`)

This document provides architectural context, technology specifications, directory structures, data models, and testing paradigms for **Savourly Recipes** (`savourly-recipes`). Prism Reviewer AI uses this context to provide domain-accurate code reviews.

---

## 1. Project Overview

**Savourly Recipes** is a full-stack web application designed for managing, filtering, searching, and favoriting/starring culinary recipes. It features a Single Page Application (SPA) frontend decoupled via RESTful HTTP JSON services from a Spring MVC 6 Java backend.

- **Repository**: `savourly-recipes`
- **Application Type**: Full-Stack Web Application (Servlet `.war` package with embedded Eclipse Jetty runner)
- **Primary Domain**: Culinary Recipe Management & Search

---

## 2. Technical Stack

### Backend Stack
- **Java Runtime**: Java 25 (OpenJDK 25)
- **Framework**: Spring Framework 6.2.2 (`spring-mvc`, `spring-context`, `spring-aop`)
- **Servlet Container**: Jakarta EE 10 / Jakarta Servlet API 6.0 (`DispatcherServlet`)
- **JSON Serialization**: Jackson Databind 2.18.2
- **Embedded Web Server**: Jetty Maven Plugin (`jetty-maven-plugin`)
- **Logging**: SLF4J 2.0.16 + Logback 1.5.16 (Log4j 1.2 XML bridge)

### Frontend Stack
- **Framework**: AngularJS 1.x (Single Page Application architecture)
- **Routing**: Angular Route (`angular-route.js`)
- **REST Client**: Angular Resource (`angular-resource.js` using `$resource`)
- **UI Framework & Styles**: Bootstrap 3 CSS + Angular UI Bootstrap (`ui-bootstrap-tpls-0.13.0.js`)

### Testing Infrastructure
- **Behavior-Driven Development (BDD)**: Cucumber Java 7.21.1, Cucumber JUnit Platform Engine, Gherkin specifications
- **Unit Testing**: JUnit 5 (JUnit Jupiter 5.11.4 & JUnit Platform Engine 1.11.4)
- **Mocking Framework**: Mockito 5.14.0 & ByteBuddy 1.15.11

---

## 3. System Architecture & Component Mapping

```
[ AngularJS SPA Frontend ]
        │
        │ HTTP REST / JSON Payload (/savourly/api/recipe/*)
        ▼
[ Spring MVC Controller ] ──────► RecipesController
        │
        ▼
[ Business Service Layer ] ──────► RecipesService / DefaultRecipesService
        │
        ▼
[ Persistence Layer ] ──────────► RecipesRepository / InMemoryRecipesRepositoryStub
```

### Key Component Packages (`co.uk.savourly.recipes`)
- `builder/`: Recipe test data builders (`RecipeBuilder.java`).
- `controller/`: REST API Controllers (`RecipesController.java`).
- `model/`: Domain models (`Recipe`, `Ingredient`, `User`, `Recipes`).
- `repository/`: In-memory thread-safe repository interfaces & implementations (`RecipesRepository`, `InMemoryRecipesRepositoryStub`).
- `service/`: Business service interfaces and implementations (`RecipesService`, `DefaultRecipesService`).
- `web/`: Spring MVC configuration & Servlet initialization (`RecipesMvcConfig`, `RecipesWebAppInitializer`).

---

## 4. Domain Data Model

1. **`Recipe`**:
   - Fields: `id` (String), `name` (String), `cookingTime` (int, in minutes), `imageUrl` (String), `ingredients` (List of `Ingredient`), `instructions` (String).
2. **`Ingredient`**:
   - Fields: `name` (String), `amount` (double/String format), `unit` (String).
3. **`User`**:
   - Fields: `username` (String), `starredRecipeIds` (Set of recipe IDs / names).
4. **`Recipes`**:
   - Wrapper container around a `List<Recipe>` used for collection serialization, pagination metadata, and total count wrapping.

---

## 5. REST API Endpoints

All endpoints are mapped under `/savourly/api/recipe`:

| Method | Endpoint | Description | Query / Body Params |
| :--- | :--- | :--- | :--- |
| `GET` | `/recipe` | List recipes with optional search/filter/pagination | `pageNo`, `term`, `maxMinutes` |
| `GET` | `/recipe/detail` | Fetch single recipe details by name | `name` |
| `GET` | `/recipe/random` | Fetch random recipe excluding specified ID | `excludeId` |
| `GET` | `/recipe/search` | Search recipes by term in name/ingredient | `term` |
| `GET` | `/recipe/filter` | Filter recipes by maximum cooking time | `maxMinutes` |
| `POST` | `/recipe/star` | Star a recipe for user | `username`, `recipeName` |
| `POST` | `/recipe/unstar` | Unstar a recipe for user | `username`, `recipeName` |
| `GET` | `/recipe/starred` | Get user's starred recipes | `username` |
| `POST` | `/recipe` | Create a new recipe | JSON Body (`Recipe`) |

---

## 6. Testing Strategy & Isolation

- **Cucumber BDD Specs** (`src/test/resources/features/`):
  - `recipe_list.feature`: Scenario specs for recipe listing & pagination.
  - `recipe.feature`: Detailed recipe view and missing recipe error scenarios.
  - `filter_recipes.feature`: Term search and prep time filtering scenarios.
  - `star.feature`: Favorites / starring system scenarios.
- **State Reset Mechanism**: `InMemoryRecipesRepositoryStub` utilizes thread-safe atomic data structures (`ConcurrentHashMap`, `AtomicLong`) with deterministic state resetting before each scenario run to guarantee 100% test reproducibility.
