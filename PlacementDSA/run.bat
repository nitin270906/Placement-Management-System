@echo off
chcp 65001 > nul
echo ==================================================
echo   PLACEMENT MANAGEMENT SYSTEM - BUILD & LAUNCHER
echo ==================================================

echo Compiling Java source files with UTF-8 encoding...
javac -encoding UTF-8 *.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check your JDK installation.
    pause
    exit /b 1
)

echo.
echo Select Application Mode:
echo [1] Launch Pixel-Perfect Swing GUI Portal (PlacementApp)
echo [2] Launch Console Terminal App (Main)
echo [3] Run Automated Test Suite (PlacementDSATest)
echo.

set /p choice="Enter choice (1-3): "

if "%choice%"=="1" (
    echo Launching PlacementApp GUI...
    java -Dfile.encoding=UTF-8 -cp . PlacementApp
) else if "%choice%"=="2" (
    echo Launching Main Console App...
    java -Dfile.encoding=UTF-8 -cp . Main
) else if "%choice%"=="3" (
    echo Running Automated Test Suite...
    java -Dfile.encoding=UTF-8 -cp . PlacementDSATest
) else (
    echo Invalid selection. Exiting.
)
