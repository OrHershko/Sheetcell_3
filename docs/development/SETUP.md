# Development Setup Guide

This guide will help you set up the SheetCell application for development.

## Prerequisites

### Required Software
- **Java Development Kit (JDK) 17 or later**
  - Download from [OpenJDK](https://openjdk.org/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
  - Ensure `JAVA_HOME` is set correctly

- **Apache Maven 3.8 or later**
  - Download from [Maven Official Site](https://maven.apache.org/download.cgi)
  - Add Maven `bin` directory to your `PATH`

- **Apache Tomcat 10.1.x** (for server deployment)
  - Download from [Tomcat Official Site](https://tomcat.apache.org/download-10.cgi)
  - Set `TOMCAT_HOME` environment variable

### Optional Software
- **IntelliJ IDEA** (recommended IDE)
- **Git** for version control

## Project Structure

```
sheetcell/
├── modules/                    # All application modules
│   ├── sheetcell-dto/         # Data Transfer Objects
│   ├── sheetcell-engine/      # Core calculation engine
│   ├── sheetcell-ui/          # JavaFX UI components
│   ├── sheetcell-client/      # Client application
│   └── sheetcell-server/      # Web server
├── docs/                      # Documentation
├── scripts/                   # Build and deployment scripts
├── config/                    # Configuration files
├── samples/                   # Sample data files
└── pom.xml                    # Parent Maven configuration
```

## Building the Project

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd sheetcell
   ```

2. **Build all modules**
   ```bash
   ./scripts/build.sh
   ```
   Or manually:
   ```bash
   mvn clean compile test package
   ```

## Running the Application

### Client Application
```bash
./scripts/run-client.sh
```
Or manually:
```bash
cd modules/sheetcell-client
mvn javafx:run
```

### Server Deployment
```bash
./scripts/deploy-server.sh
```

## Development Workflow

1. **Import into IDE**
   - Open the root `pom.xml` in IntelliJ IDEA
   - IDE will automatically detect the multi-module structure

2. **Module Dependencies**
   - `sheetcell-dto`: No dependencies (base module)
   - `sheetcell-engine`: Depends on DTO
   - `sheetcell-ui`: Depends on DTO and Engine
   - `sheetcell-client`: Depends on DTO and UI
   - `sheetcell-server`: Depends on DTO and Engine

3. **Testing**
   ```bash
   mvn test
   ```

4. **Code Style**
   - Follow Java naming conventions
   - Use meaningful variable and method names
   - Add JavaDoc comments for public APIs

## Troubleshooting

### JavaFX Issues
If you encounter JavaFX-related errors:
1. Ensure you're using a JDK that includes JavaFX
2. Or add JavaFX as external dependencies
3. Use the Maven JavaFX plugin for running applications

### Build Issues
- Ensure all dependencies are available in Maven repositories
- Check that Java and Maven versions are compatible
- Clear Maven cache: `mvn dependency:purge-local-repository`

### Server Deployment Issues
- Verify Tomcat is properly installed and `TOMCAT_HOME` is set
- Check that port 8080 is available
- Review Tomcat logs in `$TOMCAT_HOME/logs/` 