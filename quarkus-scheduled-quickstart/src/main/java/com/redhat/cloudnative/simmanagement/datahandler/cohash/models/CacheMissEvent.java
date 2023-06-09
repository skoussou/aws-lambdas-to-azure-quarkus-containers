package com.redhat.cloudnative.simmanagement.datahandler.cohash.models;

public class CacheMissEvent {

  private String externalId;

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  @Override
  public String toString() {
    return "CacheMissEvent{" +
        "externalId='" + externalId + '\'' +
        '}';
  }
}
