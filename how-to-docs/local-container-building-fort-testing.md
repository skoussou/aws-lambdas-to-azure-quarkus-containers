# Guidelines for building application and any external dependency containers for local functional tests

Examples of the scripts can be found under the [hello-cosmos](https://github.com/cariad-cloud/residency-hello-cosmos)  quickstart

## Linux Based OS

* [image-build.sh](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/image-build.sh)
* [start-local-cosmosdb.sh](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/image-build.sh)
* [apply-local-cosmosdb-certs.sh](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/apply-local-cosmosdb-certs.sh)
* [container-local-run.sh](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/container-local-run.sh)

```bash
image-build.sh [docker|podman]
start-local-cosmosdb.sh [docker|podman]
cosmodb-cert-in-cacerts.sh  [docker|podman]
container-local-run.sh [docker|podman]
```
## Windows Based OS

* [container-local-run.bat](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/container-local-run.bat)
* [start-local-cosmosdb.bat](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/start-local-cosmosdb.bat)
* [container-local-run.bat](https://github.com/cariad-cloud/residency-hello-cosmos/blob/main/container-local-run.bat)

```BASH
container-local-run.bat [docker|podman] <LOCAL_MACHINE_IP>
start-local-cosmosdb.bat  [docker|podman] <LOCAL_MACHINE_IP>
container-local-run.bat [docker|podman]  <LOCAL_MACHINE_IP>
```
