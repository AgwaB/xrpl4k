# Contributing to xrpl-kotlin

Thank you for your interest in contributing to the XRPL Kotlin SDK!

## Prerequisites

- JDK 21+
- Gradle 8.12+ (wrapper included)

## Building

```bash
./gradlew build
```

## Running Tests

```bash
# JVM tests only (recommended for local development)
./gradlew jvmTest

# Quality checks
./gradlew jvmTest apiCheck ktlintCheck
```

## Code Conventions

- **Explicit API mode** is enforced: all public declarations must have explicit visibility modifiers and return types.
- **ktlint** enforces code formatting: 120-char line length, trailing commas required.
- **BCV** (Binary Compatibility Validator) tracks the public API surface via `.api` files.

See [docs/03-project-conventions.md](docs/03-project-conventions.md) for the full conventions reference.

## Pull Request Process

1. Fork the repository and create a feature branch from `main`.
2. Write tests for any new functionality (D11: every feature ships with tests).
3. Ensure `./gradlew jvmTest apiCheck ktlintCheck` passes.
4. If you changed public API, run `./gradlew apiDump` and commit the updated `.api` files.
5. Use [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.
6. Open a PR against `main`.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
