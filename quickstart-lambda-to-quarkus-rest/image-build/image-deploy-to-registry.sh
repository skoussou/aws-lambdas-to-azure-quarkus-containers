#!/bin/bash


BUILD_TOOL=$1 #podman or docker
echo $BUILD_TOOL
if [[ "$BUILD_TOOL" != "podman" ]] && [[ "$BUILD_TOOL" != "docker" ]]; then
    echo "Usage: build.sh [podman|docker]"
    echo "Please specify the build tool you want to use!📦"
    exit 1
fi

IMAGE=quickstart-rest
REGISTRY_HOST=$2 #Point to registry
REPO=$3
VERSION=latest
USER=$4
AZUREREGISTRYNAME=$5
REGISTRY_PASS=$6
TOKEN=""

TOKEN=$(az acr login --name $AZUREREGISTRYNAME --expose-token | jq -r .accessToken)
echo
echo TOKEN : $TOKEN
sleep 4
if [[ "$TOKEN" != "" ]]; then
  # az acr login --name <AZUREREGISTRYNAME>
  $BUILD_TOOL login ${REGISTRY_HOST} -u ${USER} -p $TOKEN
else
  echo "$BUILD_TOOL login ${REGISTRY_HOST} -u ${USER} -p $REGISTRY_PASS"
  $BUILD_TOOL login ${REGISTRY_HOST} -u ${USER} -p $REGISTRY_PASS
fi

TAG=$REGISTRY_HOST/$REPO/$IMAGE:$VERSION

echo "$BUILD_TOOL tag $IMAGE $TAG"
$BUILD_TOOL tag $IMAGE $TAG

sleep 2
echo "--------------------"
podman images |grep $IMAGE
echo "--------------------"
sleep 3

$BUILD_TOOL push $TAG
