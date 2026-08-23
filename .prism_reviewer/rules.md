# Savourly Recipes - Custom Review Rules (`rules.md`)

This document defines repository-specific code review rules, quality standards, architectural guidelines, and anti-patterns for **Savourly Recipes** (`savourly-recipes`). Prism Reviewer AI enforces these rules when reviewing Pull Requests.

---

## 1. Java 25 & Spring MVC 6 Backend Rules

### Controller & REST API Layer
- **Spring Stereotypes**: Ensure REST controllers are annotated with `@RestController` and mapped under explicit request paths (`@RequestMapping("/recipe")`).
- **Dependency Injection**: Prefer constructor injection or explicit `@Autowired` with `@Qualifier` annotations where multiple service beans exist.
- **HTTP Status & ResponseEntity**: Return explicit `ResponseEntity<T>` with appropriate HTTP status codes (`HttpStatus.OK`, `HttpStatus.CREATED`, `HttpStatus.NOT_FOUND`, `HttpStatus.BAD_REQUEST`). Never return raw `null` from API methods without HTTP 404 wrapping.
- **Request Parameter Validation**: Validate all query parameters (`@RequestParam`) and body payloads (`@RequestBody`). Trim string inputs and handle parse exceptions gracefully.

### Service & Domain Layer
- **Service Interfaces**: Always define and implement service interfaces (`RecipesService` / `DefaultRecipesService`). Do not inject concrete implementation classes directly where interfaces exist.
- **Immutability & Safety**: Keep domain models (`Recipe`, `Ingredient`, `User`) clean with appropriate getters/setters and defensive copying for collections when needed.

### Persistence Layer (`InMemoryRecipesRepositoryStub`)
- **Thread Safety**: Ensure in-memory state uses thread-safe collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) or atomic primitives (`AtomicLong`).
- **State Isolation**: Any new persistence method or store modification must maintain compatibility with in-memory state reset hooks used during Cucumber BDD test suite executions.

---

## 2. AngularJS 1.x Frontend Rules

- **Minification Safety**: Always use strict array notation for AngularJS dependency injection (e.g. `['$scope', '$resource', function($scope, $resource) { ... }]`) to prevent code breakage when assets are minified.
- **Scope & Model Separation**: Keep logic out of `$scope` where possible; use services/resources (`$resource`) to encapsulate API HTTP calls against `/savourly/api/recipe`.
- **Template Security**: Avoid raw unescaped HTML interpolation (`ng-bind-html`) unless explicitly sanitized via `$sce`.

---

## 3. Testing & Behavior-Driven Development (BDD) Rules

- **Cucumber BDD Coverage**: Every new user feature or change to existing recipe workflows MUST include corresponding Gherkin scenarios in `src/test/resources/features/` (`.feature` files).
- **Step Definition Conventions**:
  - Keep step definitions (`RecipeSteps.java`) modular and reusable.
  - Do not duplicate existing step phrases (`Given`, `When`, `Then`).
  - Reset application/repository state before each scenario execution using `@Before` hooks.
- **JUnit 5 & Assertion Quality**: Use modern JUnit 5 assertions (`Assertions.assertEquals`, `Assertions.assertNotNull`) instead of JUnit 4 legacy methods.

---

## 4. Code Style & Logging Guidelines

- **SLF4J Logging**: Use SLF4J loggers (`private static final Logger LOG = LoggerFactory.getLogger(...)`). Never use `System.out.println()` or `e.printStackTrace()` in production code.
- **Log Levels**: Use `LOG.debug()` for execution context, `LOG.info()` for significant lifecycle events, `LOG.warn()` for recoverable validation issues, and `LOG.error()` for unexpected failures.
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods and Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`

---

## 5. Security & Performance Rules

- **Input Sanitization**: Validate prep-time parameters (e.g., `maxMinutes > 0`) and page numbers (`pageNo > 0`).
- **Resource Cleanup**: Ensure streams, connections, or web resources are properly closed.
- **Pagination Limit**: Enforce pagination boundaries (default 10 items per page) on large recipe list queries to avoid unnecessary memory overhead.

---

## 6. Anti-Patterns to Flag in PR Reviews

- ❌ Field injection without `@Qualifier` when multiple beans implement an interface.
- ❌ Direct modification of global static collections without thread synchronization.
- ❌ Missing Cucumber feature scenario for new REST endpoints or business logic changes.
- ❌ AngularJS controllers without array-based dependency injection syntax.
- ❌ Swallowing exceptions silently without logging or returning appropriate HTTP error status.
