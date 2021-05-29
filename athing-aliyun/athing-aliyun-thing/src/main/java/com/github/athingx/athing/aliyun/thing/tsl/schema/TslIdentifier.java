package com.github.athingx.athing.aliyun.thing.tsl.schema;

public class TslIdentifier {

    private final String identifier;
    private String name;


    public TslIdentifier(String identifier) {
        this.identifier = identifier;
        this.name = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
