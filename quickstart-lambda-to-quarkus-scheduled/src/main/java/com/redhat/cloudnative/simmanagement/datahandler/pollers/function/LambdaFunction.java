package com.redhat.cloudnative.simmanagement.datahandler.pollers.function;

import com.redhat.cloudnative.simmanagement.datahandler.models.SimEvent;
import com.redhat.cloudnative.simmanagement.datahandler.models.SimEventType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.cloudnative.simmanagement.datahandler.pollers.repository.LastFetchedEventRepository;

@ApplicationScoped
public class LambdaFunction extends MasterDataPoller {

  private static final Logger logger = LoggerFactory.getLogger(LambdaFunction.class);

  @Inject
  LastFetchedEventRepository lastFetchedEvetRepository;

  public int handleEvent(String simId) {

   
    // FIXME - BUSINESS CODE GOES HERE
    int numberOfEvents = 1;

    try{
      // Normally arrives from DB polling
      SimEvent newEvent = new SimEvent(simId, SimEventType.DEVICE_UPDATED, "Update 1", "additional information", UUID.randomUUID().toString());

      super.handleNotifications(newEvent);

    } catch (RuntimeException e) {

    }
    return numberOfEvents;
  }


}

