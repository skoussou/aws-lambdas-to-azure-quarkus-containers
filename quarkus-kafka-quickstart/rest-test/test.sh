#!/bin/bash

echo "------------------------------------------"

curl -v -d "@event.json" \
    -H "Content-Type: application/json" \
    -X POST https://$(oc get route quickstart-kafka -o jsonpath='{.spec.host}')/event/in
echo "------------------------------------------"
echo
echo "------------------------------------------"
echo "https://$(oc get route quickstart-kafka -o jsonpath='{.spec.host}')/q/metrics | grep events"
echo "------------------------------------------"
echo
curl https://$(oc get route quickstart-kafka -o jsonpath='{.spec.host}')/q/metrics | grep events
echo "------------------------------------------"
