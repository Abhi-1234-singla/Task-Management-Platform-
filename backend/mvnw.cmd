@REM ----------------------------------------------------------------------------
@REM Maven Startup Script for Task Management Platform Backend
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF

@REM Find maven command
WHERE mvn >nul 2>nul
IF %ERRORLEVEL% EQU 0 (
    mvn %*
    EXIT /B %ERRORLEVEL%
)

IF EXIST "%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd" (
    "%USERPROFILE%\maven\apache-maven-3.9.9\bin\mvn.cmd" %*
    EXIT /B %ERRORLEVEL%
)

ECHO [ERROR] Maven not found. Please ensure Maven 3.8+ is installed or on PATH.
EXIT /B 1
