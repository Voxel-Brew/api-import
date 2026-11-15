# Repository Guidelines

## Project Structure & Module Organization

This repository publishes the Java 21 `dev.voxelbrew.api:import` library. Public importer
contracts live in `src/main/java/dev/voxelbrew/importer/api`; keep implementations, file-system
access, JavaFX types, generated code, and dependency-injection annotations out of this API.
Version metadata is in `src/main/resources`. Unit tests use the intentionally configured
`src/test/test/java` tree; future integration and end-to-end suites belong under
`src/test/integrationTest/java` and `src/test/e2eTest/java`. Gradle convention scripts are in
`gradle/`, quality configuration is in `config/quality/`, and release automation is in `.github/`.

## Build, Test, and Development Commands

Use the committed wrapper rather than a system Gradle installation:

- `./gradlew test` runs the JUnit unit suite.
- `./gradlew check` runs unit and integration tests, formatting checks, SpotBugs, API dependency
  validation, and Node-based release-script tests.
- `./gradlew e2eTest` runs the separate end-to-end source set; `check` does not include it.
- `./gradlew spotlessApply` formats Java, Kotlin Gradle scripts, Markdown, YAML, and properties.
- `./gradlew build` compiles and packages the library, sources, and Javadocs.

CI uses Temurin 21 and Node 24. When snapshot dependencies are unavailable remotely, mirror CI
with `./gradlew check --include-build ../dependencies/bom --include-build ../dependencies/model`.

## Coding Style & Naming Conventions

Spotless applies Palantir Java Format and ktfmt; do not hand-align code against those formatters.
Java compilation treats all lint warnings as errors. Use `UpperCamelCase` for types,
`lowerCamelCase` for methods and fields, and descriptive contract names such as
`ImporterDescriptor`. Preserve the asynchronous `Importer<R>.importFrom(ImportWindow)` contract
and keep dependencies limited to the model API and BOM.

## Testing Guidelines

Tests use JUnit Jupiter. Name test classes `*Test` and test methods after observable behavior, as
in `malformedDescriptorsAndOptionsFailBeforeReading`. Add contract tests for validation,
immutability, null handling, and compatibility whenever public records or interfaces change.

## Commit & Pull Request Guidelines

History favors concise imperative subjects (`Use BOM versions...`) and occasional scoped prefixes
such as `docs:`, `ci:`, or `refactor:`. Keep each commit focused. Pull requests should explain API
and compatibility impact, link the relevant issue, and report the commands run. Screenshots are
only useful for documentation or workflow UI changes. Ensure `./gradlew check` passes before
requesting review; never commit credentials, generated build output, or signing keys.
