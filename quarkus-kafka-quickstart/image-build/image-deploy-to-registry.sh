#!/bin/bash

BUILD_TOOL=$1 #podman or docker
echo $BUILD_TOOL
if [[ "$BUILD_TOOL" != "podman" ]] && [[ "$BUILD_TOOL" != "docker" ]]; then
    echo "Usage: build.sh [podman|docker]"
    echo "Please specify the build tool you want to use!📦"
    exit 1
fi

IMAGE=quickstart-kafka
REGISTRY_HOST=$2 #Point to registry
REPO=quickstart-kafka
VERSION=latest
USER=$3
AZUREREGISTRYNAME=$4

TOKEN=$(az acr login --name $AZUREREGISTRYNAME --expose-token | jq -r .accessToken)
echo TOKEN : $TOKEN
# az acr login --name <AZUREREGISTRYNAME>
$BUILD_TOOL login ${REGISTRY_HOST} -u ${USER} -p $TOKEN

TAG=$REGISTRY_HOST/$REPO/$IMAGE:$VERSION

$BUILD_TOOL tag $IMAGE $TAG

$BUILD_TOOL push $TAG