@echo off
setlocal enabledelayedexpansion

REM QRWare Startup Script for Windows
REM Usage: start.bat [profile] [port]
REM Example: start.bat dev 8080

REM Default values
set PROFILE=%1
set PORT=%2
if "%PROFILE%"=="" set PROFILE=dev
if "%PORT%"=="" set PORT=8080

set MAVEN_OPTS=-Xmx1024m -Xms512m

echo.
echo 🚀 Starting QRWare Warehouse Management System
echo ================================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed or not in PATH
    echo ℹ️  Please install Java 17 or higher
    pause
    exit /b 1
)

echo ✅ Java found

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH
    echo ℹ️  Please install Maven 3.6 or higher
    pause
    exit /b 1
)

echo ✅ Maven found

REM Check if port is available
netstat -an | find ":%PORT%" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo ❌ Port %PORT% is already in use
    echo ℹ️  Please choose a different port or stop the process using port %PORT%
    pause
    exit /b 1
)

echo ✅ Port %PORT% is available

REM Display startup information
echo.
echo ℹ️  Startup Configuration:
echo   Profile: %PROFILE%
echo   Port: %PORT%
echo   Maven Options: %MAVEN_OPTS%
echo.

REM Profile specific information
if "%PROFILE%"=="dev" (
    echo ℹ️  Development profile selected
    echo ℹ️  Using H2 in-memory database
    echo ℹ️  H2 Console will be available at: http://localhost:%PORT%/h2-console
) else if "%PROFILE%"=="prod" (
    echo ℹ️  Production profile selected
    echo ⚠️  Make sure PostgreSQL is running and configured
) else (
    echo ⚠️  Unknown profile: %PROFILE%
    echo ℹ️  Available profiles: dev, prod
)

echo.
echo ℹ️  Starting application...
echo ℹ️  Press Ctrl+C to stop
echo.

REM Start the application
mvn spring-boot:run ^
    -Dspring-boot.run.profiles=%PROFILE% ^
    -Dspring-boot.run.arguments="--server.port=%PORT%" ^
    -q

echo.
echo ℹ️  Application stopped
pause