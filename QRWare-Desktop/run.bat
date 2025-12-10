@echo off
echo Starting QRWare Desktop Application...
echo.

echo Checking if backend is running...
curl -s http://localhost:8080/api/health >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Backend server is not responding at http://localhost:8080
    echo Please start the backend server first.
    echo.
    pause
)

echo Building and running desktop application...
echo.

gradlew.bat :desktopApp:run

pause