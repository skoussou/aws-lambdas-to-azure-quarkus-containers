package com.redhat.cloudnative.simmanagement.datahandler.models;


import java.util.Objects;

public class SimEvent {

  private String externalId;
  private SimEventType event;
  private String message;
  private String data;
  private String statusCode;
  private String trackingId;

  public SimEvent(String externalId, SimEventType event, String message, String data, String statusCode, String trackingId) {
    this.externalId = externalId;
    this.event = event;
    this.message = message;
    this.data = data;
    this.statusCode = statusCode;
    this.trackingId = trackingId;
  }

  public SimEvent(String externalId, SimEventType event, String message, String data, String trackingId) {
    this.trackingId = trackingId;
    this.externalId = externalId;
    this.event = event;
    this.message = message;
    this.data = data;
  }

  public SimEvent(String externalId, SimEventType event, String message, String trackingId) {
    this.trackingId = trackingId;
    this.externalId = externalId;
    this.event = event;
    this.message = message;
  }

  public SimEvent(String externalId, SimEventType event, String trackingId) {
    this.trackingId = trackingId;
    this.externalId = externalId;
    this.event = event;
  }

  public String getExternalId() {
    return externalId;
  }

  public SimEventType getEvent() {
    return event;
  }

  public String getMessage() {
    return message;
  }

  public String getData() {
    return data;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public String getTrackingId() {
    return trackingId;
  }

  @Override
  public String toString() {
    return "SimEvent{" +
        "externalId='" + externalId + '\'' +
        ", event=" + event +
        ", message='" + message + '\'' +
        ", data='" + data + '\'' +
        ", trackingId='" + trackingId + '\'' +
        ", statusCode='" + statusCode + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimEvent simEvent = (SimEvent) o;
    return Objects.equals(externalId, simEvent.externalId) &&
        event == simEvent.event &&
        Objects.equals(message, simEvent.message) &&
        Objects.equals(statusCode, simEvent.statusCode) &&
        Objects.equals(data, simEvent.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(externalId, event, message, data, statusCode);
  }
}
