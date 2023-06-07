package com.redhat.cloudnative.kafka;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cloudnative.kafka.model.Event;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.KafkaClientService;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;

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

    @Incoming("quickstart-kafka-in")
    public CompletionStage<Void> consumeQuickstartKafkaIn(Message<Event> msg) {

        try{
            Log.info("consumeQuickstartKafkaIn : Event received from Kafka");

            registry.counter("events", Tags.of("event_type", msg.getPayload().getType())).increment();

            Event event = msg.getPayload();
            Log.info("Payload : "+event);
        }
        catch (Exception e) {
            Log.error(e.getMessage());    
        }
        finally {
            return msg.ack();
        }
    }


    void onStartup(@Observes StartupEvent startupEvent) {
    }
}