#!/bin/bash

# SheetCell Client Application Runner
# This script runs the SheetCell client application

set -e  # Exit on any error

echo "🚀 Starting SheetCell Client Application..."
echo "=========================================="

# Check if the client JAR exists
CLIENT_JAR="modules/sheetcell-client/target/sheetcell-client-1.0.0.jar"
if [ ! -f "$CLIENT_JAR" ]; then
    echo "❌ Client JAR not found. Please build the project first using ./scripts/build.sh"
    exit 1
fi

# Check if JavaFX is available
if ! java --list-modules | grep -q javafx; then
    echo "⚠️  JavaFX modules not found in JDK. Using Maven JavaFX plugin..."
    cd modules/sheetcell-client
    mvn javafx:run
else
    echo "✅ Running client with JavaFX..."
    java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar "$CLIENT_JAR"
fi 