package com.redhat.cloudnative.kafka.model.deserialisers;


import com.redhat.cloudnative.kafka.model.Event;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class EventDeserialiser extends ObjectMapperDeserializer<Event> {
    public EventDeserialiser(){
        super(Event.class);
    }
}

