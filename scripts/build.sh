#!/bin/bash

# SheetCell Application Build Script
# This script builds all modules of the SheetCell application

set -e  # Exit on any error

echo "🏗️  Building SheetCell Application..."
echo "=================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Clean and compile all modules
echo "🧹 Cleaning previous builds..."
mvn clean

echo "📦 Compiling all modules..."
mvn compile

echo "🧪 Running tests..."
mvn test

echo "📦 Packaging all modules..."
mvn package

echo "✅ Build completed successfully!"
echo ""
echo "📁 Build artifacts:"
echo "   - DTO JAR: modules/sheetcell-dto/target/sheetcell-dto-1.0.0.jar"
echo "   - Engine JAR: modules/sheetcell-engine/target/sheetcell-engine-1.0.0.jar"
echo "   - UI JAR: modules/sheetcell-ui/target/sheetcell-ui-1.0.0.jar"
echo "   - Client JAR: modules/sheetcell-client/target/sheetcell-client-1.0.0.jar"
echo "   - Server WAR: modules/sheetcell-server/target/sheetcell-server-1.0.0.war" 