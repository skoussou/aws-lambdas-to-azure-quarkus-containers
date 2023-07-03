# Quarkus Cronjob Quick start

This quickstart is a good place to start if you are migrating a Job based lambda application. It demonstrates, setting up the job and CosmosDB connection

This project attempts to be the simplest possible project that provides a manual workflow for deploying an application with the following features:

* Written using Java and Maven.
* Application built as a container using docker or podman, and pushed to remote registry, in the Azure Container registry.
* Container deploys to Openshift, and connects to Apache Kafka.

**During the lambda migration, all of the manual steps in this workflow will be replaced by automated CICD piplines**

# Migrating a Job Based Lambda to Quarkus Application

## Define Configurations
* Add configurations for local development and testing in [`src/main/resources/application.properties`](src/main/resources/application.properties)
* Add configurations for deployment to Openshift in [`helm chart values`](chart/values.yaml) and [`helm chart template resources`](chart)
* Quarkus application dependencies are defined in the [pom.xml](pom.xml). Note these are the dependencies for scheduler service, Kafka and Cosmoddb integrations
  ```XML
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-cosmos</artifactId>
      <version>${azure-cosmos.version}</version>
    </dependency>  
  ```
## Code Migrations

* Java Lambda to `QuarkusApplication`
  * Identify the main Lambda Function and start code migration from it eg.
  ```JAVA
        public void handlerRequest() { .. }
  ```


* For a lambda which starts as the result of REST API call place the lambda code in [JobMain.java#run](src/main/java/com/redhat/cloudnative/token/madeup/JobMain.java).
    ```JAVA
    @ApplicationScoped
    public class JobMain {
      @Override
      public int run(String... args) {
        Log.info("Running JobMain .... ");
          this.httpClient = HttpClient.newBuilder()
              .connectTimeout(Duration.ofSeconds(10))
              .version(Version.HTTP_1_1)
              .build();
          this.mapper = new ObjectMapper();
  
          //FIXME - Place business method here
         // this.handleRequest();
          return 0;
      }
      ```
* CosmosDB Connection and Use
  * For Connection see [CosmosConnection.java](src/main/java/com/redhat/cloudnative/token/madeup/repository/CosmosConnection.java)
  * For use of the DB see [DefaultRepository](src/main/java/com/redhat/cloudnative/token/madeup/repository/DefaultRepository.java)

# Prerequisites

* An Azure login
* An Azure container Registry
* A login to an Openshift 4 cluster
* An Azure cosmodb instance

# Running & Testing the application

## Packaging and running the application

The application can be packaged using:
```shell script
mvn package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
mvn package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Build Container Image & Deploy in Image Registry

Run the script

```
cd image-build

./image-build.sh [docker|podman]

./image-deploy-to-registry.sh [docker|podman] <REGISTRY_HOST> <IMAGE_REPO> <REGISTRY_USER> <AZUREREGISTRYNAME>
```

Running these commands will create a image in an azure container registry called : `<REGISTRY_HOST>/<IMAGE_REPO>/quickstart-cronjob:latest`

This will also build the image and store in a registry local to your laptop.



# Deploy the Image to OpenShift

## Login to the Openshift webconsole

Login at

```
https://console-openshift-console.apps.<DOMAIN>/
```

## Download the oc cli

Click on the "?" in the top right, click the "Command Line Tools" link

Download the oc cli for your laptops architecure

## Openshift CLI Login
First Login, get the login command from the webcosole. Click on your name in top right corner, click the "Copy Login Command", click "Display Tokem" link. Copy the login command, for instance :

```
oc login --server=https://api.<DOMAIN>:6443 -u <username> -p <password)
```

## Deploy the container image to OCP

This repo contains a folder called **chart**, this contains a [Helm chart](chart) that deploys this application. The following files are templates :

* `configmap.yaml` : defines the details for cosmos db
* `cronjob.yaml` : defines a kafka topic
* `secret.yaml` : defines the Cosmos DB connection details for the production  (**modify me**)


Here is the `values` file, contains the values that will be injected into this template (**_UPDATE ACCORDINGLY_**):

```YAML
name: quickstart-cronjob
schedule: '"* * * * *"'
image:
  registry: <yourregistry>
  repository: quickstart-cronjob
  name: quickstart-cronjob
  version: latest
config:
  some:
    actor:
      name: 'Registering'
      domain: 'Test QA'
  loglevel: INFO
  cosmos:
    database: token-cache
    partitionkey: clientId
```

The template is usually used from within a CICD pipeline and executed by ArgoCD, but we can deploy it from the command line for convenience. The following command deploys from the command line, assuming that you are logged onto openshift and in your target project :

```
cd chart && helm template -f values.yaml . | oc apply -f -
```

You should now see all of the components in this project deleplyed.

If you want to delete them, just run :

```shell script
cd chart && helm template -f values.yaml . | oc delete -f -
```

## Test the application

```shell script
cd rest-test
test.sh
```

* Check container POD logs

```shell script
oc logs -f -l  app=quickstart-cronjob
```