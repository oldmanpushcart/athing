package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.github.athingx.athing.aliyun.thing.tsl.specs.TslSpecs;

public class TslDataType {

    private final Type type;
    private final TslSpecs specs;

    public TslDataType(TslSpecs specs) {
        this.type = specs.getType();
        this.specs = specs;
    }

    public Type getType() {
        return type;
    }

    public TslSpecs getSpecs() {
        return specs;
    }

    public enum Type {
        INT("int"),
        TEXT("text"),
        DATE("date"),
        BOOL("bool"),
        ENUM("enum"),
        ARRAY("array"),
        FLOAT("float"),
        STRUCT("struct"),
        DOUBLE("double");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
