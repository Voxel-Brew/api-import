# Repository Guidelines

## Project Structure & Module Organization

This repository publishes the Java 21 `dev.voxelbrew.api:import` library. Public importer
contracts live in `src/main/java/dev/voxelbrew/api/importer`; keep implementations, JavaFX
types, generated code, and dependency-injection annotations out of this API. `FileImportDialog`
may inspect a source path during validation; actual bounded reading belongs to an importer.
Version metadata is in `src/main/resources`. Unit tests use the intentionally configured
`src/test/test/java` tree; future integration and end-to-end suites belong under
`src/test/integrationTest/java` and `src/test/e2eTest/java`. Gradle convention scripts are in
`gradle/`, SpotBugs configuration comes from the public BOM master branch, and release automation is in `.github/`.

## Build, Test, and Development Commands

Use the committed wrapper rather than a system Gradle installation:

- `./gradlew test` runs the JUnit unit suite.
- `./gradlew check` runs unit and integration tests, formatting checks, SpotBugs, API dependency
  validation, and Node-based release-script tests.
- `./gradlew e2eTest` runs the separate end-to-end source set; `check` does not include it.
- `./gradlew spotlessApply` formats Java, Kotlin Gradle scripts, Markdown, YAML, and properties.
- `./gradlew build` compiles and packages the library, sources, and Javadocs.

CI uses Temurin 21 and Node 24. Publish local snapshots to Maven Local and opt in with `-PuseMavenLocal=true`.

Run `./gradlew checkBinaryCompatibility` to compare the built JAR with the latest stable Maven
Central release in the same major at or below `projectVersion` (ignoring `-SNAPSHOT`). The
`check` task includes this comparison on pull requests and pushes to master and release branches.

## Coding Style & Naming Conventions

Spotless applies Palantir Java Format and ktfmt; do not hand-align code against those formatters.
Java compilation treats all lint warnings as errors. Use `UpperCamelCase` for types,
`lowerCamelCase` for methods and fields, and descriptive contract names such as
`ImporterDescriptor`. Preserve the synchronous `Importer.importFrom(ImportWindow)` contract,
returning `Optional<CadDocument>` and allowing checked failures. Keep dependencies limited to
the model API and BOM; do not depend on the export API.

Run `./gradlew checkJavaMemberOrder` to check Java source declaration order. The `check` and
`build` tasks include it. Within each class, start with static final fields, then other static fields,
final instance fields, other instance fields, static nested types, inner classes, static methods,
constructors, and instance methods. Preserve Java initialization order when moving fields.

## Testing Guidelines

Tests use JUnit Jupiter. Name test classes `*Test` and test methods after observable behavior, as
in `malformedDescriptorsAndOptionsFailBeforeReading`. Add contract tests for validation,
immutability, null handling, and compatibility whenever public records or interfaces change.

## Pre-commit SpotBugs

The build resolves VoxelBrew artifacts through Maven and fetches shared quality policy
from the public BOM master branch.

Before committing, run `./gradlew spotbugsMain` from this API repository or
`./gradlew :import:spotbugsMain` from the composite workspace.
Use `.\gradlew.bat` in place of `./gradlew` on Windows.

## Commit & Pull Request Guidelines

Prefer **Rebase and merge** for pull requests. Use **Squash and merge** only as an exception.
The **Create a merge commit** option is blocked.

History favors concise imperative subjects (`Use BOM versions...`) and occasional scoped prefixes
such as `docs:`, `ci:`, or `refactor:`. Keep each commit focused. Pull requests should explain API
and compatibility impact, link the relevant issue, and report the commands run. Screenshots are
only useful for documentation or workflow UI changes. Ensure `./gradlew check` passes before
requesting review; never commit credentials, generated build output, or signing keys.

When a pull request resolves a known GitHub issue, add `Closes #123` to the PR description,
using the actual issue number. For an issue in another repository, use
`Closes owner/repository#123`. Add the closing reference to the PR targeting
`master` so GitHub closes the issue when the PR is merged.

## Lombok in Java code

Use Lombok where it removes routine Java boilerplate without obscuring a public contract. Apply these annotations in Java-bearing repositories and modules; the website has no Java production code.

- `@Getter` / `@Setter`: Generate straightforward accessors for fields that should be exposed or mutable. Prefer field-level annotations when only some fields qualify; retain custom accessors that enforce invariants or make defensive copies.
- `@RequiredArgsConstructor` / `@NoArgsConstructor`: Generate constructors for required final or `@NonNull` fields, or a no-argument constructor when a framework or serializer needs one. Keep explicit constructors when they validate, transform, or document special initialization behavior.
- `@NonNull`: Mark required fields, parameters, and other supported elements to express and enforce non-null inputs. Replace equivalent `Objects.requireNonNull` checks in those places; keep explicit checks when they provide distinct behavior or messages.
- `@ToString`: Generate useful diagnostic string representations for value or state classes. Exclude secrets, large graphs, and recursive relationships.
- `@Data`: Use for mutable data holders that genuinely need getters, setters, equality, hashing, and string representation together. Prefer narrower annotations when only some behavior is appropriate.
- `@Value`: Use for immutable value types when its generated constructor, accessors, equality, hashing, and string representation fit the contract. Preserve defensive copies for mutable components.
- `@EqualsAndHashCode`: Generate value equality for classes whose identity is defined by selected fields. Choose included fields and superclass handling deliberately; avoid mutable fields that would break hash collections.
- `@Slf4j`: Add a logger to classes that emit operational diagnostics. Use parameterized logging and keep credentials and other sensitive data out of logs.
- `@Builder`: Use for objects with many optional or named construction inputs when it improves call-site clarity. Keep required-field validation and invariants explicit.
- `@UtilityClass`: Use for stateless collections of static helpers that must not be instantiated. Do not use it for classes that may need instances, inheritance, or dependency injection.

## AssertJ in Java tests

Use AssertJ (`org.assertj:assertj-core`) for assertions in Java unit, integration, and end-to-end tests. Keep JUnit Jupiter for test lifecycle, parameterization, and execution. Declare AssertJ without a version in test dependencies; the BOM manages its version. Prefer `assertThat`, `assertThatThrownBy`, and `assertThatCode` with descriptive failure messages and appropriate numeric tolerances. Do not add new JUnit Assertions calls.

## Service injection

Resolve services by their interfaces at the wiring point and pass them to consumers through
constructors. Provider classes must not expose static service getters or downcast discovered
services to their implementations.

## Release workflows

Dispatch **Prepare release** on `master` to cut `release/A.B.x` and open the
next-minor snapshot pull request. Merge that PR through normal branch protection.
Dispatch `release.yml` on the release branch, first with the default dry run,
then with the dry run disabled to sign, reserve annotated tag `vA.B.C`, publish
to Maven Central, create the GitHub release and advance the patch snapshot.

Release-branch pushes and completed publications run `release-merge-back.yml`.
It proposes release fixes in a PR against master, retaining master's
`projectVersion`. Resolve other conflicts through normal review. To retry,
dispatch merge-back on the release branch.

Allow GitHub Actions to create pull requests, or configure `RELEASE_PR_TOKEN`
with contents and pull-request write access for the automation branches. Use a
dedicated token when branch protection requires CI triggered by PR updates.
