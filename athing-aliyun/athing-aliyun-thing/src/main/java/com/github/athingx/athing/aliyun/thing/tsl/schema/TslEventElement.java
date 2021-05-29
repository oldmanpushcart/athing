package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class TslEventElement extends TslElement {

    private final EventType type;
    private final String method;

    @SerializedName("outputData")
    private final List<TslData> outputData = new LinkedList<>();

    public TslEventElement(String identifier, EventType type) {
        super(identifier);
        this.type = type;
        this.method = String.format("thing.event.%s.post", identifier);
    }

    @Override
    public String toString() {
        return "EVENT:" + getIdentifier();
    }

    public EventType getType() {
        return type;
    }

    public String getMethod() {
        return method;
    }

    public List<TslData> getOutputData() {
        return outputData;
    }

    public enum EventType {
        INFO("info"),
        WARN("warn"),
        ERROR("error");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
