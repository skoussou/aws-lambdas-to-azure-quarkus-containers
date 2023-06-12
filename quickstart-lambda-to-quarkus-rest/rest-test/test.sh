#!/bin/bash


echo "------------------------------------------"
curl -v  -H 'accept: text/plain' -X GET https://$(oc get route quickstart-rest -o jsonpath='{.spec.host}')/q/health/started

curl -v  -H 'accept: text/plain' -X GET https://$(oc get route quickstart-rest -o jsonpath='{.spec.host}')/hello/France
echo "------------------------------------------"

echo
echo "------------------------------------------"
echo "https://$(oc get route quickstart-rest -o jsonpath='{.spec.host}')/q/metrics | grep country"
echo "------------------------------------------"
echo
curl https://$(oc get route quickstart-rest -o jsonpath='{.spec.host}')/q/metrics | grep country
echo "------------------------------------------"

echo "------------------------------------------"

curl -v -d "@event.json" \
    -H "Content-Type: application/json" \
    -X POST https://$(oc get route quickstart-rest -o jsonpath='{.spec.host}')/hello/create
echo "------------------------------------------"


