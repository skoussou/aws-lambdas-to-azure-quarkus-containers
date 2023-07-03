# Guidelines for building application and any external dependency containers for local functional tests

Examples of the scripts can be found under the [quickstart-lambda-to-quarkus-rest](../quickstart-lambda-to-quarkus-rest/image-build/)  quickstart

## Linux Based OS

* [image-build.sh](../quickstart-lambda-to-quarkus-rest/image-build/image-build.sh)
* [start-local-cosmosdb.sh](../quickstart-lambda-to-quarkus-rest/image-build/start-local-cosmosdb.sh)
* [container-local-run.sh](../quickstart-lambda-to-quarkus-rest/image-build/container-local-run.sh)

```bash
image-build.sh [docker|podman]
start-local-cosmosdb.sh [docker|podman]
cosmodb-cert-in-cacerts.sh  [docker|podman]
container-local-run.sh [docker|podman]
```
## Windows Based OS

* [container-local-run.bat](../quickstart-lambda-to-quarkus-rest/image-build/container-local-run.bat)
* [start-local-cosmosdb.bat](../quickstart-lambda-to-quarkus-rest/image-build/start-local-cosmosdb.bat)
* [container-local-run.bat](../quickstart-lambda-to-quarkus-rest/image-build/container-local-run.bat)

```BASH
container-local-run.bat [docker|podman] <LOCAL_MACHINE_IP>
start-local-cosmosdb.bat  [docker|podman] <LOCAL_MACHINE_IP>
container-local-run.bat [docker|podman]  <LOCAL_MACHINE_IP>
```
