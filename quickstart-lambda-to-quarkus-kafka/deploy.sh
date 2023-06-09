#!/bin/bash

#build the Java
#mvn clean package

#Login to Azure in order to gain access to the container registry
# az login

#Build the image, make sure docker is running on your laptop
#cd image-build && ./image-build.sh docker && ./image-deploy-to-registry.sh docker

#render helm template and deploy to openshift
cd chart && helm template . | oc delete -f -
cd chart && helm template -f values.yaml . | oc apply -f -