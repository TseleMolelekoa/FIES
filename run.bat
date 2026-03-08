@echo off
echo ============================================================
echo  KMM Fire Equipment Inspection System - Build ^& Run
echo ============================================================
echo.

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please install Java 11+
    echo Download: https://adoptium.net/
    pause & exit /b 1
)

:: Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found. Please install Apache Maven 3.8+
    echo Download: https://maven.apache.org/download.cgi
    echo.
    echo Alternatively, if you have Maven installed elsewhere, set M2_HOME
    pause & exit /b 1
)

echo Building project (downloading dependencies on first run)...
echo This may take a few minutes on first launch.
echo.
mvn clean package -q

if errorlevel 1 (
    echo.
    echo BUILD FAILED. Check output above.
    pause & exit /b 1
)

echo.
echo Build successful! Launching application...
echo.
java -jar target\FEIS-Inspection.jar

pause
