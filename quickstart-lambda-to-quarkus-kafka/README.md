# Quarkus Kafka Quick start

This quickstart is a good place to start if you are migrating an event or message driven lambda application. It demonstrates, connecting and authenticating with Kafka, publishing and subscribing to messages.

This project attempts to be the simplest possible project that provides a manual workflow for deploying an application with the following features:

   * Written using Java and Maven.
   * Apache Kafka mutual-tls Connectivity
   * Apache Kafka Publish and Subscribe
   * Rest endpoint that receives an http request and sends the payload to kafka
   * Application built as a container using docker or podman, and pushed to remote registry, in the Azure Container registry.
   * Container deploys to Openshift, and connects to Apache Kafka.

**During the lambda migration, all of the manual steps in this workflow will be replaced by automated CICD piplines**

# Migrating a Message Triggered (Long Running) Lambda to Quarkus Application

## Define Configurations

* Add configurations for local development and testing in [`src/main/resources/application.properties`](src/main/resources/application.properties)
* Add configurations for deployment to Openshift in [`helm chart values`](chart/values.yaml) and [`helm chart template resources`](chart)
* Quarkus application dependencies are defined in the [pom.xml](pom.xml). Note the dependency for Kafka integrations
  ```XML
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
    </dependency>
  ```

## Code Migrations

* Java Lambda to `QuarkusApplication`
  * Identify the main Lambda Function and start code migration from it eg.
  ```JAVA
       public List<String> handleRequest(SQSEvent event, Context context)
  ```

  * For a lambda which starts as the result of a _REST_ call and creates a message/event, place the lambda code in [EventResource.java#in](src/main/java/com/redhat/cloudnative/kafka/EventResource.java) method adjusting appropriately the `@Path` configurations and HTTP Method (eg. `@POST`) to match the lambda's REST trigger. 
  ```JAVA
    @ApplicationScoped
    @Path("/event")
    public class EventResource {
    
        private static final Logger Log = Logger.getLogger(EventResource.class);
    
        @Inject
        ObjectMapper mapper;
    
        @Inject
        MeterRegistry registry;
    
        @Inject
        KafkaClientService clientService;
    
        @Inject
        @Channel("quickstart-kafka-out")
        Emitter<Event> quickstartKafkaOut;
    
        @POST
        @Produces(MediaType.TEXT_PLAIN)
        @Consumes(MediaType.APPLICATION_JSON)
        @Path("/in")
        public String in(Event event) throws JsonProcessingException {
    
            Log.info("Event received from REST : " + mapper.writeValueAsString(event));
    
            // FIXME - Call the Lambda handle method
    
            OutgoingKafkaRecordMetadata<String> metadata = OutgoingKafkaRecordMetadata.<String>builder()
                .withHeaders(new RecordHeaders().add("tracking-id", UUID.randomUUID().toString().getBytes()).add("tenant", "Mytenant".getBytes()))
                .build();
    
            event.setType("From : quickstart-kafka-out");
    
            Message msg = Message.of(event).addMetadata(metadata);
    
            quickstartKafkaOut.send(msg);
    
            registry.counter("events", Tags.of("event_type", event.getType())).increment();
    
            event.setType("From : quickstart-kafka-out");
    
            return "OK";
        }
  ```

  * For a lambda which starts as a result of a _message_, place the lambda code in [EventResource.java#consumeQuickstartKafkaIn](src/main/java/com/redhat/cloudnative/kafka/EventResource.java) method adjusting the `@Incoming` configuration to the `Topic` name monitored by the lambda (adjust all occurrences as configs also in `values.yaml` and `application.properties`).
  ```JAVA
    @Incoming("quickstart-kafka-in")
    public CompletionStage<Void> consumeQuickstartKafkaIn(Message<Event> msg) {
  
        try{
            Log.info("consumeQuickstartKafkaIn : Event received from Kafka");
  
            registry.counter("events", Tags.of("event_type", msg.getPayload().getType())).increment();
  
            Event event = msg.getPayload();
            Log.info("Payload : "+event);
  
            // FIXME - Call the Lambda handle method
        }
        catch (Exception e) {
            Log.error(e.getMessage());    
        }
        finally {
            return msg.ack();
        }
      }
    ```

  The Channel Emitter is responsible for sending the message to Kafka. More Documentation can be found on this here : https://quarkus.io/guides/kafka#sending-messages-with-emitter


# Prerequisites

* An Azure login
* An Azure container Registry
* A login to an Openshift 4 cluster
* Red Hat's AMQ Streams Operator, this deploys and manages Kafka clusters and is otherwise known as Strimzi.

Below is an example of a Custom Resource (`CR`) that creates a Kafka cluster.

```YAML
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  annotations:
  name: wc-test-kafka-cluster
spec:
  entityOperator:
    topicOperator: {}
    userOperator: {}
  kafka:
    config:
      default.replication.factor: 3
      inter.broker.protocol.version: '3.1'
      min.insync.replicas: 2
      offsets.topic.replication.factor: 3
      transaction.state.log.min.isr: 2
      transaction.state.log.replication.factor: 3
    listeners:
      - name: plain
        port: 9092
        tls: false
        type: internal
      - name: tls
        port: 9093
        tls: true
        type: internal
      - name: external
        port: 9094
        tls: true
        type: route
      - authentication:
          enablePlain: true
          type: tls
        name: mtls
        port: 9095
        tls: true
        type: route
    replicas: 3
    storage:
      size: 30Gi
      type: persistent-claim
    version: 3.3.1
  zookeeper:
    replicas: 3
    storage:
      size: 10Gi
      type: persistent-claim
```

Secrets necessary to connect to this cluster with mtls will be created in the same namespace.

This Kafka cluster has three brokers, persistent storage. Additionally an endpoint where users can connect and establish identity cryptographically with mutual tls. Secrets for this user will be automatically created.

Below is the Custom Resource (`CR`) to create a user for mtls authentication :

```YAML
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaUser
metadata:
  annotations:
  name: quickstart-kafka-user
  labels:
    strimzi.io/cluster: wc-test-kafka-cluster
spec:
  authentication:
    type: tls
```

Note the reference to the cluster via a label defined above.


Below is the Custom Resource (`CR`) that defines a Kafka Topic, not tics that it defines the time a message is allowed to stay on the topic, and the total size in bytes of all messages in the topic, when one of these thresholds is reached old messages are evicted.

```YAML
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  annotations:
  name: quickstart-kafka-in
  labels:
    strimzi.io/cluster: wc-test-kafka-cluster
spec:
  config:
    retention.ms: 604800000
    segment.bytes: 1073741824
  partitions: 1
  replicas: 3
```

A single partition guarantees message ordering, multiple replicas, message resilience.

* Apply the prerequisites `CR` in an Openshift cluster
  ```shell script
  oc apply -f prerequisites/prerequisites.yaml
  ```

# Running & Testing the application

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

* Run locally Kafka
  ```shell script
  docker|podman-compose up
  ```

* You can run your application in dev mode that enables live coding using:
  ```shell script
  mvn compile quarkus:dev
  ```

* Listen on the messages arriving on `KafkaTopic`
  * `quickstart-kafka-out`
     ```shell script
     docker|podman exec -it <CONTAINER_ID> ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic quickstart-kafka-out --from-beginning
     ```
  * `quickstart-kafka-in`
     ```shell script
     docker|podman exec -it <CONTAINER_ID> ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic quickstart-kafka-in --from-beginning
     ```
    
### Test Locally

#### `REST` -> `Kafka`

* Send Rest request
  ```shell script
  curl -v -d "@rest-test/event.json"  -H "Content-Type: application/json" -X POST http://localhost:8080/event/in
  ```
  * Result in logs
  ```YAML
  2023-06-07 15:24:36,385 INFO  [com.red.clo.kaf.EventResource] (executor-thread-1) Event received from REST : {"id":"1","type":"hello-kafka-http-req","data":{"sim":"yes please","make":"id4"}}
  2023-06-07 15:24:36,423 WARN  [org.apa.kaf.cli.NetworkClient] (kafka-producer-network-thread | kafka-producer-quickstart-kafka-out) [Producer clientId=kafka-producer-quickstart-kafka-out] Error while fetching metadata with correlation id 4 : {quickstart-kafka-out=LEADER_NOT_AVAILABLE}
  2023-06-07 15:29:09,974 INFO  [com.red.clo.kaf.EventResource] (executor-thread-1) Event recieved from REST : {"id":"1","type":"hello-kafka-http-req","data":{"sim":"yes please","make":"id4"}}
  ```
#### `Kafka` -> `Incoming` 

* Send message to Kafka `KafkaTopic` `quickstart-kafka-in`
  ```shell script
  docker|podman exec -it <CONTAINER_ID> ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic quickstart-kafka-in
  ```

  * Result in logs
    ```shell script
    2023-06-07 15:23:14,812 INFO  [com.red.clo.kaf.EventResource] (vert.x-eventloop-thread-3) consumeQuickstartKafkaIn : Event received from Kafka
    2023-06-07 15:23:14,813 INFO  [com.red.clo.kaf.EventResource] (vert.x-eventloop-thread-3) Payload : com.redhat.cloudnative.kafka.model.Event@4db60ee9
    ```
  
* Get Metrics
  ```shell script
  curl http://localhost:8080/q/metrics | grep events
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
  Dload  Upload   Total   Spent    Left  Speed
  100 56214  100 56214    0     0  3358k      0 --:--:-- --:--:-- --:--:-- 3431k
  # HELP kafka_consumer_coordinator_rebalance The total number of successful rebalance events, each event is composed of several failed re-trials until it succeeded
  # HELP kafka_consumer_coordinator_rebalance_rate_per_hour The number of successful rebalance events per hour, each event is composed of several failed re-trials until it succeeded
  # TYPE events counter
  # HELP events
  events_total{event_type="From : quickstart-kafka-out"} 3.0
  events_total{event_type="kafka-in-req"} 1.0
  ```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

Notice the message acknowledgement in the finally block. Always acknowledge messages in some way. More details can be found here : https://quarkus.io/guides/kafka#receiving-messages-from-kafka


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

Running these commands will create a image in an azure container registry called : `<REGISTRY_HOST>/<IMAGE_REPO>/quickstart-kafka:latest`

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

   * `configmap.yaml` : defines the application.properties that configures our quarkus application
   * `deployment.yaml` : defines the details of how are image is deployed
   * `quickstart-kafka-in-topic.yaml` : defines a kafka topic
   * `quickstart-kafka-out-topic.yaml` : defines a kafka topic
   * `quickstart-kafka-user.yaml` : defines a kafka user
   * `route.yaml` : defines an ingress to the app's rest endpoint
   * `service.yml` : defines a loadbalancer to distribute traffic across multiple pods containing our app
   * `serviceAccount.yaml` : defines a service account for our deployment which is allowed to view secrets.

Here is the `values` file, contains the values that will be injected into this template (**_UPDATE ACCORDINGLY_**):

```YAML
name: quickstart-kafka
image:
  registry: <registryname>.azurecr.io     #Point to registry
  repository: quickstart-kafka            #Point to repository
  name: quickstart-kafka
  version: latest

config:
  loglevel: INFO       
  kafka:
    cluster: 
      name: wc-test-kafka-cluster
    user: quickstart-kafka-user
    intopic: quickstart-kafka-in
    outtopic: quickstart-kafka-out
  ocp:
    cluster:
      domain: apps.<YOUR-DOMAIN>     #Point to K8s
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
oc logs -f -l  app=quickstart-kafka
```

* Check AMQ Streams Kafka POD for the messages received


```shell script
oc -n kafkas run kafka-consumer -ti --image=registry.redhat.io/amq7/amq-streams-kafka-33-rhel8:2.3.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server  wc-test-kafka-cluster-kafka-bootstrap.lambdas-tests.svc.cluster.local:9092 --topic my-topic --from-beginning
```


## Metrics

Metrics in a format useful to Prometheus are available at a specific end point : `/q/metrics`

The numbers of different kafka topic are measured, this is because a custom metric has been included in the code



# Further Reading
 
   * [Quarkus Kafka Reference Guide](https://quarkus.io/guides/kafka)
   * [Strimzi](https://strimzi.io/)
   * [AMQ Streams Docs](https://access.redhat.com/documentation/en-us/red_hat_amq/2021.q3/html/amq_streams_on_openshift_overview/index)
   * [Apache Kafka](https://kafka.apache.org/)
   * [Azure Container Registry Docs](https://docs.microsoft.com/en-us/azure/container-registry/)
   * [Docker Docs](https://docs.docker.com/)
   * [Podman Docs](https://docs.podman.io/en/latest/)
   * [Openshift Docs](https://docs.openshift.com/container-platform/4.12/welcome/index.html)
   * [Openshift cli documents](https://docs.openshift.com/container-platform/4.12/cli_reference/openshift_cli/getting-started-cli.html)

