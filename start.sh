#!/bin/bash

# FOS-Agri Data App Launcher Script

# Set Java 17 path
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java not found. Please install Java 17:"
    echo "  brew install openjdk@17"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" != "17" ]; then
    echo "Warning: Java 17 is recommended. Current version: $JAVA_VERSION"
fi

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 &> /dev/null; then
    echo "Error: PostgreSQL is not running on localhost:5432"
    echo "  Start PostgreSQL with: brew services start postgresql"
    exit 1
fi

echo "Starting FOS-Agri Data App in PRODUCTION mode..."
echo "Application will be available at: http://localhost:8087/"
echo ""

# Build the production JAR
echo "Building production JAR..."
./mvnw clean package -Pproduction -DskipTests

# Run the application in production mode (background)
echo "Starting application in background..."
nohup java -jar target/*.jar --spring.profiles.active=prod > app.log 2>&1 &
echo "Application started with PID: $!"
echo "Logs are being written to: app.log"
echo "To stop the application: kill $!"
