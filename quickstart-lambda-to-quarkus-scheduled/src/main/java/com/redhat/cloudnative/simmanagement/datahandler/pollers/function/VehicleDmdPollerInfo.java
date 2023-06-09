package com.redhat.cloudnative.simmanagement.datahandler.pollers.function;


public class VehicleDmdPollerInfo {

  private final String externalId;
  private final String tenant;


  public VehicleDmdPollerInfo(String externalId, String tenant) {
    this.externalId = externalId;
    this.tenant = tenant;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getTenant() {
    return tenant;
  }
}
