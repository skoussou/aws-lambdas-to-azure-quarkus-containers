package com.redhat.cloudnative.simmanagement.datahandler.pollers.function;

import com.redhat.cloudnative.simmanagement.datahandler.pollers.repository.LastFetchedEventRepository;
import java.util.UUID;
import com.redhat.cloudnative.simmanagement.datahandler.pollers.function.LambdaFunction;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class PollOrchestrator {

  private final Logger logger = LoggerFactory.getLogger(PollOrchestrator.class);

  @Inject
  MeterRegistry registry;

  // FIXME - Call the Lambda handle method
  @Inject
  LambdaFunction dmdPoller;

  @Inject
  LastFetchedEventRepository lastFetchedEventRepository;

  public PollOrchestrator() {
  }

  @Scheduled(every = "10s")
  public void dmdScheduler() {
    try {

      logger.info("******************* dmdScheduler scheduled *******************");
      // FIXME - Call the Lambda handle method
      dmdPoller.handleEvent(UUID.randomUUID().toString());

      registry.counter("country_counter", Tags.of("name", "dmdScheduled")).increment();

    }  catch (Exception e) {
      logger.error("Unknown exception for DMD Scheduler: {}", e.getMessage(), e);
    }
  }

  @Startup
  void Startup(){
    logger.info("******************* dmdScheduler STARTING *******************");
  }
}
