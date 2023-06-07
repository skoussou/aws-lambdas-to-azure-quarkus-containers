package com.redhat.cloudnative.kafka.model;

import java.util.HashMap;

public class Event {
    
    String id;

    String type;

    HashMap<String, String> data = new HashMap<String, String>();

    public HashMap<String, String> getData() {
        return data;
    }
    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void addData(String key, String value)   {
        this.data.put(key, value);
    }
}
