@echo off
echo Testing QRWare Desktop build...
echo.

echo 1. Cleaning previous builds...
gradlew.bat clean

echo.
echo 2. Building shared module...
gradlew.bat :shared:build

echo.
echo 3. Building desktop app...
gradlew.bat :desktopApp:build

echo.
echo 4. Running tests...
gradlew.bat test

echo.
echo Build complete!
pause