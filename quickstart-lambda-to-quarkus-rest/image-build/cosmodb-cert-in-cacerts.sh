#!/bin/bash

# From https://learn.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates#use-the-certificate-with-java-apps

# SET JAVA_HOME eg.
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.7.0.7-5.fc37.x86_64
echo "JAVA_HOME=$JAVA_HOME"
sleep 3

# Backing up original cacerts in this folder (/etc/pki/ca-trust/extracted/java/cacerts)
cp /etc/pki/ca-trust/extracted/java/cacerts .

# If emulator was started with /AllowNetworkAccess, replace the below with the actual IP address of it:
EMULATOR_HOST=localhost
EMULATOR_PORT=8081
EMULATOR_CERT_PATH=/tmp/cosmos_emulator.cert
openssl s_client -connect ${EMULATOR_HOST}:${EMULATOR_PORT} </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $EMULATOR_CERT_PATH
# delete the cert if already exists
sudo $JAVA_HOME/bin/keytool -cacerts -delete -alias cosmos_emulator -storepass changeit -noprompt
# import the cert
sudo $JAVA_HOME/bin/keytool -cacerts -importcert -alias cosmos_emulator -file $EMULATOR_CERT_PATH --storepass changeit -noprompt

sleep 2
echo
echo "keytool -cacerts -list -keystore -alias "cosmos_emulator" --storepass changeit"
sudo $JAVA_HOME/bin/keytool -cacerts -list  -alias cosmos_emulator --storepass changeit