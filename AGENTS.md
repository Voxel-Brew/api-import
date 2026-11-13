# Import API instructions

- Own only public importer contracts, descriptors, typed options/results and reader ports.
- Depend only on api/model for domain values; never depend on AGPL application modules. Keep concrete formats, registries, file access and service registrations in impl/importer.
- Keep native handles, generated Protobuf, JavaFX, CLI parsing and DI annotations out of contracts.
- Version the SPI explicitly; verify external consumption through the independent provider fixture.
- Do not bundle API classes inside plugin JARs. Document changes in docs/file-io.md.

## Standalone repository

- Use Java 21 and the committed Gradle wrapper. Preserve the MIT license and public Java packages.
- Run `./gradlew spotlessApply` (Windows: `.\gradlew.bat spotlessApply`) before every commit, then review changes.
- Run `check` and validate Maven POM dependencies when changing publication configuration.
- Never add application-module dependencies or commit generated output or credentials.
