# 🚀 Valmuri Framework

> A modern, type-safe Scala framework built on ZIO for rapid web application development

[![CI](https://github.com/yourusername/valmuri/workflows/CI/badge.svg)](https://github.com/yourusername/valmuri/actions)
[![Coverage](https://codecov.io/gh/yourusername/valmuri/branch/main/graph/badge.svg)](https://codecov.io/gh/yourusername/valmuri)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## ✨ Features

- 🔥 **Fast Development**: Rails-like productivity with Scala type safety
- ⚡ **High Performance**: Built on ZIO and Netty for maximum throughput
- 🛡️ **Type Safe**: Compile-time guarantees across all layers
- 🎯 **Convention over Configuration**: Sensible defaults, minimal boilerplate
- 📦 **Batteries Included**: HTTP server, routing, database, migrations, CLI tools
- 🚀 **Production Ready**: Metrics, health checks, graceful shutdown built-in

## 🚀 Quick Start

```bash
# Install Valmuri CLI
curl -fsSL https://raw.githubusercontent.com/yourusername/valmuri/main/install.sh | bash

# Create new app
valmuri new my-awesome-app
cd my-awesome-app

# Start development server
valmuri dev
```
Your app is now running at http://localhost:8080 🎉

## 📖 Documentation
- [Getting Started Guide](docs/getting-started.md)
- [Configuration Reference](docs/configuration.md)
- [Routing & Controllers](docs/routing.md)
- [Database & Migrations](docs/database.md)
- [API Reference](docs/api-reference.md)

## 🛠️ Development
### Prerequisites
- Java 21+
- Mill Build Tool

### Building from Source
```bash
git clone https://github.com/yourusername/valmuri.git
cd valmuri

# Compile
mill valmuri.compile

# Run tests  
mill valmuri.test

# Run example
mill examples.helloWorld.run
```
## 🤝 Contributing
We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.