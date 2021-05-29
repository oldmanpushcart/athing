package com.github.athingx.athing.standard.platform.domain;

/**
 * 排序顺序
 */
public enum SortOrder {

    /**
     * 正序
     */
    ASCENDING(0),

    /**
     * 倒序
     */
    DESCENDING(1);

    private final int value;

    SortOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
