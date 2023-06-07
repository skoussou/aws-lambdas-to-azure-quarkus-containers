# Guidelines for enabling as `cluster-admin` Openshift Router logging

## `ingress-operator` modificatoins

* Go to Namespace `openshift-ingress-operator`
* Access `Deployment` ingress-operator and modify yaml
* Code Modifications

```yaml
  apiVersion: operator.openshift.io/v1
  kind: IngressController
metadata:
...
  name: APP-NAME-GW
  namespace: openshift-ingress-operator
spec:
...
  logging:
    access:
      destination:
        type: Container
      httpLogFormat: '%ci:%cp\ [%tr]\ %ft\ %b/%s\ %TR/%Tw/%Tc/%Tr/%Ta\ %ST\ %B\ %CC\ %CS\ %tsc\ %ac/%fc/%bc/%sc/%rc %sq/%bq\ %hr\ %hs\ %{+Q}r\ %sslc\ %sslv\ %H\ %HM'
      httpCaptureHeaders:
        request: 
        - name: X-GitHub-Event
          maxLength: 15
        - name: content-type
          maxLength: 17
        - name: Accept
          maxLength: 15
        response:
        - name: Content-length
          maxLength: 9
        - name: content-type
          maxLength: 17  
```

    
    
      






