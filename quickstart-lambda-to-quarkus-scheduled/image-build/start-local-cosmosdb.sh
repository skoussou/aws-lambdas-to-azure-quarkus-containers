#!/bin/bash
## starts Mongo locally and imports certificates into your JDK


BUILD_TOOL=$1 #podman or docker
echo $BUILD_TOOL
if [[ "$BUILD_TOOL" != "podman" ]] && [[ "$BUILD_TOOL" != "docker" ]]; then
    echo "Usage: build.sh [podman|docker]"
    echo "Please specify the build tool you want to use!📦"
    exit 1
fi

export COMPOSE_INTERACTIVE_NO_CLI=1
export EXTERNAL_IP=$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1)
echo "EXTERNAL_IP=$EXTERNAL_IP"

if [[ "$BUILD_TOOL" = "podman" ]]; then
    $BUILD_TOOL run     --publish 8081:8081     --publish 10251-10254:10251-10254     --memory 1g --name=cosmosdb     --env AZURE_COSMOS_EMULATOR_PARTITION_COUNT=2 --env AZURE_COSMOS_EMULATOR_IP_ADDRESS_OVERRIDE=$EXTERNAL_IP --interactive     --tty     mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator
else
    docker-compose up -d
fi


echo
$BUILD_TOOL ps -a
echo

sleep 20

curl -v -k https://localhost:8081/_explorer/emulator.pem > emulatorcert.crt


echo '####################################################################################################'
echo '		[FEDORA] Setup CosmosDB Certs for Local Java Testing'
echo '####################################################################################################'
echo
if [ ! -f $JAVA_HOME/lib/security/jssecacerts ]
then
    echo "NO jssecacerts file exists, creating ..."
    echo "cp $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/jssecacerts"
    sudo cp $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/jssecacerts
    echo
    ls -la $JAVA_HOME/lib/security/jssecacerts
else
    echo "jssecacerts file exists"
fi

echo
echo "keytool -delete -alias "cosmosdb" -keystore $JAVA_HOME/lib/security/jssecacerts -storepass changeit -noprompt"
sudo $JAVA_HOME/bin/keytool -delete -alias "cosmosdb" -keystore $JAVA_HOME/lib/security/jssecacerts -storepass changeit -noprompt
sleep 2
echo
echo ""
sudo $JAVA_HOME/bin/keytool -importcert -file ./emulatorcert.crt -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit -noprompt
sleep 2
echo
echo "keytool -list -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit"
sudo $JAVA_HOME/bin/keytool -list -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit

