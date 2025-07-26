# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# LLM4S Development Guidelines

## Build & Test Commands

```bash
# Build the project (Scala 3)
sbt compile

# Build for all Scala versions (2.13 and 3)
sbt +compile

# Build and test all versions
sbt buildAll

# Run a specific sample 
sbt "samples/runMain org.llm4s.samples.basic.BasicLLMCallingExample"

# Run a sample with Scala 2.13
sbt ++2.13.12 "samples/runMain org.llm4s.samples.basic.BasicLLMCallingExample"

# Run tests for the current Scala version
sbt test

# Run tests for all Scala versions
sbt +test

# Run a single test
sbt "testOnly org.llm4s.shared.WorkspaceAgentInterfaceTest"

# Format code
sbt scalafmtAll
```

# VALMURI.md

This file provides guidance to Claude Code and developers when working with the Valmuri framework.

## Development Workflow

### Build & Test Commands

```bash
# Format all code (run before commits)
mill mill.scalalib.scalafmt.ScalafmtModule/formatAll

# Compile all modules
mill valmuri.compile
mill cli.compile
mill examples.helloWorld.compile

# Run all tests with coverage
mill testAll
mill coverage

# Run specific tests
mill valmuri.test
mill "valmuri.test.testOnly valmuri.test.routing.*"

# Development server with hot reload
mill examples.helloWorld.runBackground

# Build CLI tool
mill cli.assembly

# Publish to local repository
mill publishLocal
```

## Code quality standards / Code style guidelines

### Formatting

- **Always run** `mill mill.scalalib.scalafmt.ScalafmtModule/formatAll` before commits
- **Formatting**: Follow `.scalafmt.conf` settings (120 char line length)
- Use meaningful variable names (avoid `x`, `y`, etc.)
- **Imports**: Use curly braces for imports (`import { x, y }`)
- **Error Handling**: Use `Either[LLMError, T]` for operations that may fail
- **Types**: Prefer immutable data structures and pure functions
- **Naming**: Use camelCase for variables/methods, PascalCase for classes/objects
- **Documentation**: Use Asterisk style (`/** ... */`) for ScalaDoc comments
- **Code Organization**: Keep consistent with existing package structure
- **Functional Style**: Prefer pattern matching over if/else statements
- Line length: 120 characters max
- Use trailing commas for multiline structures
- Prefer explicit types for public APIs

### Error Handling

- Use ValmuriError hierarchy for all business logic errors
- Never use raw exceptions in public APIs
- Provide meaningful error messages
