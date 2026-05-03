@rem Gradle startup script for Windows
@rem Generado para Gradle 8.6 — FileExplorer project
@rem
@rem NOTA: Requiere gradle\wrapper\gradle-wrapper.jar
@rem Si no tienes el jar, genera el wrapper con: gradle wrapper --gradle-version 8.6

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Check for gradle-wrapper.jar
if not exist "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" (
    echo.
    echo ERROR: gradle-wrapper.jar no encontrado en gradle\wrapper\
    echo.
    echo Para generarlo, ejecuta una sola vez:
    echo   gradle wrapper --gradle-version 8.6
    echo.
    exit /b 1
)

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME está definido como "%JAVA_HOME%" pero no se encontró java.exe
exit /b 1

:execute
@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
if "%ERRORLEVEL%"=="0" goto mainEnd
exit /b %ERRORLEVEL%

:mainEnd
if "%OS%"=="Windows_NT" endlocal
