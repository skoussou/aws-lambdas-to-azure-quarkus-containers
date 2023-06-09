#!/bin/bash

curl -v -k https://localhost:8081/_explorer/emulator.pem > /tmp/emulatorcert.crt

echo
ls -la /tmp/emulatorcert.crt

echo
echo
sleep 5


if [ ! -f $JAVA_HOME/lib/security/jssecacerts ]
then
    echo "NO jssecacerts file exists, creating ..."
    echo "cp $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/jssecacerts"
    cp $JAVA_HOME/lib/security/cacerts $JAVA_HOME/lib/security/jssecacerts
    echo
    ls -la $JAVA_HOME/lib/security/jssecacerts
else
    echo "jssecacerts file exists"
fi

echo "keytool -delete -alias "cosmosdb" -keystore $JAVA_HOME/lib/security/jssecacerts -storepass changeit -noprompt"
sudo $JAVA_HOME/bin/keytool -delete -alias "cosmosdb" -keystore $JAVA_HOME/lib/security/jssecacerts -storepass changeit -noprompt
sleep 2
echo
echo ""
sudo $JAVA_HOME/bin/keytool -importcert -file /tmp/emulatorcert.crt -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit -noprompt
sleep 2
echo
echo "keytool -list -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit"
sudo $JAVA_HOME/bin/keytool -list -keystore $JAVA_HOME/lib/security/jssecacerts -alias "cosmosdb" --storepass changeit

exit

