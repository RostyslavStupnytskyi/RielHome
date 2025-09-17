@ECHO OFF
SETLOCAL

SET "BASE_DIR=%~dp0"
SET "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"
SET "WRAPPER_PROPERTIES=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties"
SET "WRAPPER_URL="

IF EXIST "%WRAPPER_PROPERTIES%" (
  FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    IF /I "%%A"=="wrapperUrl" (
      SET "WRAPPER_URL=%%B"
    )
  )
)

IF "%WRAPPER_URL%"=="" (
  ECHO ERROR: wrapperUrl not set in %WRAPPER_PROPERTIES% 1>&2
  EXIT /B 1
)

IF NOT EXIST "%WRAPPER_JAR%" (
  POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command "param([string]$url,[string]$path); $ErrorActionPreference='Stop'; $dir = Split-Path -Parent $path; if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }; Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $path" "%WRAPPER_URL%" "%WRAPPER_JAR%"
  IF %ERRORLEVEL% NEQ 0 EXIT /B %ERRORLEVEL%
)

IF DEFINED JAVA_HOME (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  FOR %%j IN (java.exe) DO SET "JAVA_EXE=%%~$PATH:j"
)

IF NOT DEFINED JAVA_EXE (
  ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
  EXIT /B 1
)

SET CMD="%JAVA_EXE%"
IF NOT "%MAVEN_OPTS%"=="" SET CMD=%CMD% %MAVEN_OPTS%

%CMD% -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
