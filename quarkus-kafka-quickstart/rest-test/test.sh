#!/bin/bash

NAMESPACE=$1
DOMAIN=$2

curl -v -d "@event.json" \
    -H "Content-Type: application/json" \
    -X POST https://quickstart-kafka-$NAMESPACE.$DOMAIN/event/in


curl https://quickstart-kafka-$NAMESPACE.$DOMAIN/q/metrics | grep events

