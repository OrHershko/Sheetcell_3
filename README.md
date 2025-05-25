# SheetCell Application (שטיסל)

A professional, collaborative spreadsheet application built with Java and JavaFX. This project provides comprehensive spreadsheet functionalities with real-time collaboration, version control, and a modern client-server architecture.

## 🏗️ Architecture Overview

SheetCell follows a modern multi-tier architecture:

- **Client-Server Model**: Scalable architecture with JavaFX client and Tomcat server
- **Multi-Module Design**: Clean separation of concerns using Maven modules
- **Real-time Collaboration**: Concurrent editing with conflict resolution
- **Version Control**: Complete audit trail for sheets and individual cells

## 📁 Project Structure

```
sheetcell/
├── modules/                           # Application modules
│   ├── sheetcell-dto/                # Data Transfer Objects
│   │   └── src/main/java/dto/        # DTO classes
│   ├── sheetcell-engine/             # Core calculation engine
│   │   └── src/main/java/            # Engine implementation
│   ├── sheetcell-ui/                 # JavaFX UI components
│   │   ├── src/main/java/            # UI components
│   │   └── src/main/resources/       # UI resources & styles
│   ├── sheetcell-client/             # Client application
│   │   ├── src/main/java/            # Client code
│   │   └── src/main/resources/       # Client resources
│   └── sheetcell-server/             # Web server (WAR)
│       ├── src/main/java/            # Server servlets
│       └── src/main/resources/       # Web resources
├── docs/                             # Documentation
│   ├── api/                          # API documentation
│   ├── development/                  # Development guides
│   └── user-guide/                   # User documentation
├── scripts/                          # Build & deployment scripts
│   ├── build.sh                      # Build all modules
│   ├── run-client.sh                 # Run client application
│   └── deploy-server.sh              # Deploy to Tomcat
├── config/                           # Configuration files
│   └── application.properties        # App configuration
├── samples/                          # Sample data files
│   └── sample-sheet.xml              # Example spreadsheet
├── pom.xml                           # Parent Maven configuration
└── README.md                         # This file
```

## 🚀 Quick Start

### Prerequisites

- **Java 17+** with JavaFX support
- **Apache Maven 3.8+**
- **Apache Tomcat 10.1.x** (for server deployment)

### Build & Run

1. **Clone and build the project:**
   ```bash
   git clone <repository-url>
   cd sheetcell
   ./scripts/build.sh
   ```

2. **Run the client application:**
   ```bash
   ./scripts/run-client.sh
   ```

3. **Deploy the server:**
   ```bash
   ./scripts/deploy-server.sh
   ```

## 🎯 Features

### Core Spreadsheet Functionality
- ✅ **XML Sheet Loading**: Import/export spreadsheet data
- ✅ **Formula Engine**: Advanced calculation engine with dependency tracking
- ✅ **Cell Updates**: Real-time recalculation of dependent cells
- ✅ **Version Control**: Track changes at sheet and cell level
- ✅ **Range Operations**: Named ranges, sorting, filtering

### JavaFX User Interface
- 🎨 **Modern UI**: Clean, responsive JavaFX interface
- 🎨 **Customization**: Adjustable columns, rows, cell styling
- 🎨 **Theming**: Multiple UI themes (light, dark, custom)
- 🎨 **Interactive Grid**: Intuitive spreadsheet interaction
- 🎨 **What-If Analysis**: Dynamic scenario modeling

### Collaboration Features
- 👥 **Multi-User Support**: Concurrent editing capabilities
- 👥 **Permission System**: Owner/Writer/Reader access levels
- 👥 **Real-time Updates**: Live synchronization of changes
- 👥 **Conflict Resolution**: Automatic handling of concurrent edits
- 👥 **User Authentication**: Secure login system

### Advanced Operations
- 📊 **Data Analysis**: Sort and filter operations
- 📊 **Complex Functions**: SUM, AVG, and range-based functions
- 📊 **Dependency Visualization**: View cell relationships
- 📊 **Progress Tracking**: Loading indicators for operations

## 🛠️ Development

### Module Dependencies
```
sheetcell-dto (base)
    ↑
sheetcell-engine
    ↑
sheetcell-ui
    ↑
sheetcell-client

sheetcell-engine
    ↑
sheetcell-server
```

### Building Individual Modules
```bash
# Build specific module
cd modules/sheetcell-engine
mvn clean compile

# Run tests
mvn test

# Package module
mvn package
```

### IDE Setup
1. Import the root `pom.xml` into IntelliJ IDEA
2. IDE will automatically detect the multi-module structure
3. Configure JavaFX runtime if needed

## 📚 Documentation

- **[Development Setup](docs/development/SETUP.md)**: Complete development environment setup
- **[API Documentation](docs/api/API.md)**: REST API reference
- **[User Guide](docs/user-guide/)**: End-user documentation

## 🔧 Configuration

Application settings can be configured in `config/application.properties`:

```properties
# Server Configuration
server.host=localhost
server.port=8080

# Client Configuration
client.server.url=http://localhost:8080/sheetcell

# Application Limits
app.max.sheets.per.user=10
app.max.concurrent.users=100
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run tests for specific module:
```bash
cd modules/sheetcell-engine
mvn test
```

## 📦 Deployment

### Server Deployment
The server module builds to a WAR file that can be deployed to any servlet container:

```bash
# Build WAR file
mvn package

# Deploy to Tomcat
./scripts/deploy-server.sh
```

### Client Distribution
The client can be distributed as:
- Executable JAR with dependencies
- Native installer (using jpackage)
- Docker container

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Add JavaDoc comments for public APIs
- Write unit tests for new functionality
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Built with JavaFX for modern UI
- Uses Apache Tomcat for server deployment
- Maven for dependency management and build automation
- Gson for JSON serialization

---

**Note**: This is an educational project demonstrating enterprise Java development practices, including multi-module Maven projects, client-server architecture, and collaborative software design.
