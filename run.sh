#!/bin/bash
echo "============================================================"
echo " KMM Fire Equipment Inspection System - Build & Run"
echo "============================================================"
echo

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Install Java 11+: https://adoptium.net/"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found. Install Apache Maven: https://maven.apache.org/"
    exit 1
fi

echo "Building project (first run may take a few minutes)..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "BUILD FAILED."
    exit 1
fi

echo "Build successful! Launching..."
java -jar target/FEIS-Inspection.jar
