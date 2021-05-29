package com.github.athingx.athing.aliyun.thing.tsl.schema;

import com.google.gson.annotations.SerializedName;

public class TslProfile {

    public static final String MAIN_TSL_PROFILE_VERSION = "1.5";
    public static final String SUB_TSL_PROFILE_VERSION = "1.0";

    @SerializedName("productKey")
    private final String productId;

    @SerializedName("version")
    private final String version;

    public TslProfile(String productId, String version) {
        this.productId = productId;
        this.version = version;
    }

    public String getProductId() {
        return productId;
    }

    public String getVersion() {
        return version;
    }

}
