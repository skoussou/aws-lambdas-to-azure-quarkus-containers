@echo off

set BUILD_TOOL=%1
SET EXTERNAL_IP=%2
echo "%BUILD_TOOL%"
IF NOT "%BUILD_TOOL%"=="podman" IF NOT "%BUILD_TOOL%"=="docker" (
  echo "Usage: build.sh [podman|docker]"
  echo "Please specify the build tool you want to use!"
  exit "1"
)
set "COMPOSE_INTERACTIVE_NO_CLI=1"

REM for /f "tokens=1-2 delims=:" %%a in ('ipconfig^|find "IP"') do set ip==%%b
REM set EXTERNAL_IP=%ip:~1%
echo BUILD_TOOL=%BUILD_TOOL%
REM SET "EXTERNAL_IP=10.100.226.151"
echo EXTERNAL_IP=%EXTERNAL_IP%
IF "%BUILD_TOOL%"=="podman" (
  %BUILD_TOOL% "run" "--publish" "8081:8081" "--publish" "10251-10254:10251-10254" "--memory" "1g" "--name=cosmosdb" "--env" "AZURE_COSMOS_EMULATOR_PARTITION_COUNT=2" "--env" "AZURE_COSMOS_EMULATOR_IP_ADDRESS_OVERRIDE=%EXTERNAL_IP%" "--interactive" "--tty" "mcr.microsoft.com\cosmosdb\linux\azure-cosmos-emulator"
) ELSE (
  docker-compose up -d
)
echo %BUILD_TOOL% ps -a
echo
timeout /t 20 /nobreak > NUL

curl -v -k https://localhost:8081/_explorer/emulator.pem > emulatorcert.crt


echo '####################################################################################################'
echo '		[FEDORA] Setup CosmosDB Certs for Local Java Testing'
echo '####################################################################################################'
IF NOT exist "%JAVA_HOME%\lib\security\jssecacerts" (
  echo "NO jssecacerts file exists, creating %CD%.."
  echo "xcopy %JAVA_HOME%\lib\security\cacerts %JAVA_HOME%\lib\security\jssecacerts"
  echo F|xcopy /S /Q /Y /F "%source%" "%target%"
  >> "%JAVA_HOME%\lib\security\jssecacerts" rem/
  xcopy /I /Y "%JAVA_HOME%\lib\security\cacerts" "%JAVA_HOME%\lib\security\jssecacerts"
  echo
  dir "%JAVA_HOME%\lib\security\jssecacerts"
) ELSE (
  echo "jssecacerts file exists"
)

echo
echo "keytool -delete -alias "cosmosdb" -keystore %JAVA_HOME%\lib\security\jssecacerts -storepass changeit -noprompt"
"%JAVA_HOME%\bin\keytool" "-delete" "-alias" "cosmosdb" "-keystore" "%JAVA_HOME%\lib\security\jssecacerts" "-storepass" "changeit" "-noprompt"
timeout /t 2 /nobreak > NUL
echo
"%JAVA_HOME%\bin\keytool" "-importcert" "-file" "%CD%\emulatorcert.crt" "-keystore" "%JAVA_HOME%\lib\security\jssecacerts" "-alias" "cosmosdb" "--storepass" "changeit" "-noprompt"
timeout /t 2 /nobreak > NUL
echo
echo "keytool -list -keystore %JAVA_HOME%\lib\security\jssecacerts -alias "cosmosdb" --storepass changeit"
"%JAVA_HOME%\bin\keytool" "-list" "-keystore" "%JAVA_HOME%\lib\security\jssecacerts" "-alias" "cosmosdb" "--storepass" "changeit"
