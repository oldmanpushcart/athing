package com.github.athingx.athing.aliyun.thing.tsl.schema;

public class TslElement extends TslIdentifier {

    private boolean required;
    private String desc;

    public TslElement(String identifier) {
        super(identifier);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = "".equals(desc)
                ? null
                : desc;
    }

}
