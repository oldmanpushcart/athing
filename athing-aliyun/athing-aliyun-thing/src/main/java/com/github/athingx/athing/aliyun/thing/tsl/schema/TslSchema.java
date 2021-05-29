package com.github.athingx.athing.aliyun.thing.tsl.schema;

import java.util.LinkedList;
import java.util.List;

public abstract class TslSchema {

    private final String schema = "https://iotx-tsl.oss-ap-southeast-1.aliyuncs.com/schema.json";
    private final TslProfile profile;
    private final List<TslPropertyElement> properties = new LinkedList<>();
    private final List<TslEventElement> events = new LinkedList<>();
    private final List<TslServiceElement> services = new LinkedList<>();

    public TslSchema(TslProfile profile) {
        this.profile = profile;
    }

    public TslProfile getProfile() {
        return profile;
    }

    public List<TslPropertyElement> getProperties() {
        return properties;
    }

    public List<TslEventElement> getEvents() {
        return events;
    }

    public List<TslServiceElement> getServices() {
        return services;
    }
}
