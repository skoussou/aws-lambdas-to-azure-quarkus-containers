@echo off

SET BUILD_TOOL=%1
REM SET EXTERNAL_IP=%2

echo "%BUILD_TOOL%"
IF NOT "%BUILD_TOOL%"=="podman" IF NOT "%BUILD_TOOL%"=="docker" (
  echo "Usage: build.sh [podman|docker]"
  echo "Please specify the build tool you want to use!"
  exit "1"
)
SET IMAGE=quickstart-scheduled
SET REGISTRY_HOST=localhost
SET VERSION=latest
REM echo "Local IP=%EXTERNAL_IP%"
REM IF "%BUILD_TOOL%"=="podman" (
REM   %BUILD_TOOL% "run" "-d" "--rm" "-i" "-p" "8080:8080" "%REGISTRY_HOST%/%IMAGE%:%VERSION%" "-e" "cosmos.host=https://%EXTERNAL_IP%:8081" "-e" "cosmos.master.key=C2y6yDjf5\R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw\Jw=="
REM ) ELSE (
REM   echo "STARTING--- %BUILD_TOOL% run -d --rm -i -p 8080:8080 -e cosmos.host=https://%EXTERNAL_IP%:8081  -e cosmos.master.key=C2y6yDjf5\R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw\Jw== %IMAGE%:%VERSION%"
REM   %BUILD_TOOL% run -d --rm -i -p 8080:8080 -e cosmos.host=https://%EXTERNAL_IP%:8081  -e cosmos.master.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw== %IMAGE%:%VERSION%
REM )

IF "%BUILD_TOOL%"=="podman" (
  %BUILD_TOOL% "run" "-d" "--rm" "-i" "-p" "8080:8080" "%REGISTRY_HOST%/%IMAGE%:%VERSION%"
) ELSE (
  echo "STARTING--- %BUILD_TOOL% run -d --rm -i -p 8080:8080 -e cosmos.host=https://%EXTERNAL_IP%:8081  -e cosmos.master.key=C2y6yDjf5\R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw\Jw== %IMAGE%:%VERSION%"
  %BUILD_TOOL% run -d --rm -i -p 8080:8080 %IMAGE%:%VERSION%
)


REM echo "####################################################################################################"
REM echo "		Setup CosmosDB Certs for Local Container Testing"
REM echo "####################################################################################################"
REM echo
REM %BUILD_TOOL% ps -a --format "{{.ID}}" -l > tmpFile
REM set /p latestcontainerid= < tmpFile
REM del tmpFile
REM echo "APP CONTAINER ID=%latestcontainerid%"
REM echo
REM echo "%BUILD_TOOL% cp .\emulatorcert.crt %latestcontainerid%:/tmp"
REM %BUILD_TOOL% cp .\emulatorcert.crt %latestcontainerid%:/tmp
REM echo "%BUILD_TOOL% cp .\apply-local-cosmosdb-certs.sh %latestcontainerid%:/tmp"
REM %BUILD_TOOL% cp .\apply-local-cosmosdb-certs.sh %latestcontainerid%:/tmp
REM echo
REM echo "%BUILD_TOOL% exec --user root %latestcontainerid% /tmp/apply-local-cosmosdb-certs.sh"
REM %BUILD_TOOL% exec --user root %latestcontainerid% bash -c "cp $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/jssecacerts"
REM %BUILD_TOOL% exec --user root %latestcontainerid% bash -c "$JAVA_HOME/bin/keytool -delete -alias cosmosdb -keystore $JAVA_HOME/lib/security/jssecacerts -storepass changeit -noprompt"
REM %BUILD_TOOL% exec --user root %latestcontainerid% bash -c "$JAVA_HOME/bin/keytool -importcert -file /tmp/emulatorcert.crt -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit -noprompt"
REM %BUILD_TOOL% exec --user root %latestcontainerid% bash -c "$JAVA_HOME/bin/keytool -list -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit"
REM %BUILD_TOOL% container logs -f %latestcontainerid%
