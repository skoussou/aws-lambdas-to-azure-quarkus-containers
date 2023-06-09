package com.redhat.cloudnative.simmanagement.datahandler.cohash.models;

import com.redhat.cloudnative.simmanagement.datahandler.models.SimEvent;

public class SimEventMessage {
    
    SimEvent simEvent;
    String tenant;
    String trackingId;

    public SimEventMessage(SimEvent simEvent, String trackingId, String tenant) {
        this.simEvent = simEvent;
        this.tenant = tenant;
        this.trackingId = trackingId;
    }

    public SimEventMessage(SimEvent simEvent, String trackingId) {
        this.simEvent = simEvent;
        this.trackingId = trackingId;
    }
}
