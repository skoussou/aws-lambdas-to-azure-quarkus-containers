package com.redhat.cloudnative.hellocosmos.model;

public class HelloCountry {

    private String id;
    private String hello;
    private String partitionKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }

    public String getHello() {
        return hello;
    }
    
    public void setHello(String country) {
        this.hello = country;
    }

}
