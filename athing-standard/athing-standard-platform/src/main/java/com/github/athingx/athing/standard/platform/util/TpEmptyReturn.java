package com.github.athingx.athing.standard.platform.util;

/**
 * 返回：空（void）
 */
public class TpEmptyReturn extends TpReturn<Void> {

    /**
     * 返回：空
     *
     * @param reqId 请求ID
     */
    public TpEmptyReturn(String reqId) {
        super(reqId, null);
    }

}
