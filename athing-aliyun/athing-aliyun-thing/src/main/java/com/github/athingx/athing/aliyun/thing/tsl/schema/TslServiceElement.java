package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class TslServiceElement extends TslElement {

    @SerializedName("callType")
    private final CallType callType;
    private final String method;

    @SerializedName("inputData")
    private final List<TslData> inputData = new LinkedList<>();

    @SerializedName("outputData")
    private final List<TslData> outputData = new LinkedList<>();

    public TslServiceElement(String identifier, CallType callType) {
        super(identifier);
        this.callType = callType;
        this.method = String.format("thing.service.%s", identifier);
    }

    @Override
    public String toString() {
        return "SERVICE:" + getIdentifier();
    }

    public CallType getCallType() {
        return callType;
    }

    public String getMethod() {
        return method;
    }

    public List<TslData> getInputData() {
        return inputData;
    }

    public List<TslData> getOutputData() {
        return outputData;
    }

    public enum CallType {
        ASYNC("async"),
        SYNC("sync");

        private final String type;

        CallType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
