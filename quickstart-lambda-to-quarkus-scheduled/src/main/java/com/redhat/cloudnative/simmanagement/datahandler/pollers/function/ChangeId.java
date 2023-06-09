package com.redhat.cloudnative.simmanagement.datahandler.pollers.function;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeId {

  private String id;
  private String lastIndexKey;
  private String value;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLastIndexKey() {
    return lastIndexKey;
  }

  public void setLastIndexKey(String lastIndexKey) {
    this.lastIndexKey = lastIndexKey;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
