#!/bin/bash


BUILD_TOOL=$1 #podman or docker
echo $BUILD_TOOL
if [[ "$BUILD_TOOL" != "podman" ]] && [[ "$BUILD_TOOL" != "docker" ]]; then
    echo "Usage: build.sh [podman|docker]"
    echo "Please specify the build tool you want to use!📦"
    exit 1
fi

IMAGE=quickstart-scheduled
REGISTRY_HOST=localhost
VERSION=latest

EXTERNAL_IP=$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1)
echo "Local IP=$EXTERNAL_IP"

if [[ "$BUILD_TOOL" = "podman" ]]; then
    $BUILD_TOOL run -d --rm -i -p 8080:8080 $REGISTRY_HOST/$IMAGE:$VERSION -e cosmos.host=https://$EXTERNAL_IP:8081 -e cosmos.master.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==  
else
    echo "$BUILD_TOOL run -d --rm -i -p 8080:8080 -e cosmos.host=https://$EXTERNAL_IP:8081 -e cosmos.master.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==  $IMAGE:$VERSION"
    $BUILD_TOOL run -d --rm -i -p 8080:8080 -e cosmos.host=https://$EXTERNAL_IP:8081 -e cosmos.master.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==  $IMAGE:$VERSION
fi

docker ps -a

sleep 10

echo '####################################################################################################'
echo '		Setup CosmosDB Certs for Local Container Testing'
echo '####################################################################################################'
echo 
export latestcontainerid=$(docker ps -a --format "{{.ID}}" -l)

echo "APP CONTAINER ID=$latestcontainerid"


echo
echo "docker cp ./emulatorcert.crt $latestcontainerid:/tmp"
docker cp ./emulatorcert.crt $latestcontainerid:/tmp 
echo "docker cp ./emulatorcert.crt $latestcontainerid:/tmp"
docker cp ./apply-local-cosmosdb-certs.sh $latestcontainerid:/tmp 

echo
echo "docker exec --user root -it $latestcontainerid bash"
#docker exec -it 
#docker exec --user root -it $latestcontainerid bash

docker exec --user root $latestcontainerid  /tmp/apply-local-cosmosdb-certs.sh

docker container logs -f $latestcontainerid

