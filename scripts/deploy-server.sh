#!/bin/bash

# SheetCell Server Deployment Script
# This script deploys the server WAR file to Tomcat

set -e  # Exit on any error

echo "🚀 Deploying SheetCell Server..."
echo "================================"

# Configuration
TOMCAT_HOME=${TOMCAT_HOME:-"/usr/local/tomcat"}
TOMCAT_WEBAPPS="$TOMCAT_HOME/webapps"
SERVER_WAR="modules/sheetcell-server/target/sheetcell-server-1.0.0.war"
APP_NAME="sheetcell"

# Check if server WAR exists
if [ ! -f "$SERVER_WAR" ]; then
    echo "❌ Server WAR not found. Please build the project first using ./scripts/build.sh"
    exit 1
fi

# Check if Tomcat directory exists
if [ ! -d "$TOMCAT_HOME" ]; then
    echo "❌ Tomcat not found at $TOMCAT_HOME"
    echo "   Please set TOMCAT_HOME environment variable or install Tomcat"
    exit 1
fi

# Stop Tomcat if running
echo "🛑 Stopping Tomcat..."
if [ -f "$TOMCAT_HOME/bin/shutdown.sh" ]; then
    "$TOMCAT_HOME/bin/shutdown.sh" || true
    sleep 3
fi

# Remove old deployment
echo "🧹 Removing old deployment..."
rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"
rm -f "$TOMCAT_WEBAPPS/$APP_NAME.war"

# Deploy new WAR
echo "📦 Deploying new WAR file..."
cp "$SERVER_WAR" "$TOMCAT_WEBAPPS/$APP_NAME.war"

# Start Tomcat
echo "🚀 Starting Tomcat..."
if [ -f "$TOMCAT_HOME/bin/startup.sh" ]; then
    "$TOMCAT_HOME/bin/startup.sh"
    echo "✅ Server deployed successfully!"
    echo "🌐 Access the application at: http://localhost:8080/$APP_NAME"
else
    echo "❌ Tomcat startup script not found"
    exit 1
fi 