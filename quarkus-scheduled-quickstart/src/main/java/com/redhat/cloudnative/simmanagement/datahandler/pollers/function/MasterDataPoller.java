package com.redhat.cloudnative.simmanagement.datahandler.pollers.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.redhat.cloudnative.simmanagement.datahandler.models.SimEvent;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public abstract class MasterDataPoller {

  private static final Logger logger = LoggerFactory.getLogger(MasterDataPoller.class);


  //FIXME - Look for sim-state-manager in application.properties and update accordingly
  @Inject
  @Channel("sim-state-manager")
  @Broadcast
  Emitter<SimEvent> simStateEmitter;

  void handleNotifications(SimEvent event) {
     sendMessage(Message.of(event));
  }
  private void sendMessage(Message<SimEvent> msg){
    logger.info("SendMessage : Successfully written message to Kafka topic [sim-state-manager]: "+simStateEmitter.toString());
    simStateEmitter.send(msg);
  }
}
