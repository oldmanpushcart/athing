package com.github.ompc.athing.aliyun.thing;

/**
 * 设备启动选项
 */
public class ThingBootOption {

    /**
     * 设备连接超时(毫秒)
     */
    private long connectTimeoutMs = 1000 * 30L;

    /**
     * 平台应答超时(毫秒)
     */
    private long replyTimeoutMs = 1000 * 30L;

    /**
     * 设备心跳维持间隔(毫秒)
     */
    private long keepAliveIntervalMs = 1000 * 30L;

    /**
     * 获取配置超时(毫秒)
     */
    private long configTimeoutMs = 1000 * 60L;

    /**
     * 获取升级超时(毫秒)
     */
    private long upgradeTimeoutMs = 1000 * 60L * 3;

    /**
     * 是否自动断线重连接
     */
    private boolean isAutoReconnect = false;

    /**
     * 断线重连时间间隔(毫秒)
     */
    private long reconnectTimeIntervalMs = 1000 * 5L;

    /**
     * 获取连接区域
     */
    private String connectRegion = "cn-shanghai";

    /**
     * 工作线程数
     */
    private int threads = 20;

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public long getReplyTimeoutMs() {
        return replyTimeoutMs;
    }

    public void setReplyTimeoutMs(long replyTimeoutMs) {
        this.replyTimeoutMs = replyTimeoutMs;
    }

    public long getKeepAliveIntervalMs() {
        return keepAliveIntervalMs;
    }

    public void setKeepAliveIntervalMs(long keepAliveIntervalMs) {
        this.keepAliveIntervalMs = keepAliveIntervalMs;
    }

    public long getConfigTimeoutMs() {
        return configTimeoutMs;
    }

    public void setConfigTimeoutMs(long configTimeoutMs) {
        this.configTimeoutMs = configTimeoutMs;
    }

    public long getUpgradeTimeoutMs() {
        return upgradeTimeoutMs;
    }

    public void setUpgradeTimeoutMs(long upgradeTimeoutMs) {
        this.upgradeTimeoutMs = upgradeTimeoutMs;
    }

    public long getReconnectTimeIntervalMs() {
        return reconnectTimeIntervalMs;
    }

    public void setReconnectTimeIntervalMs(long reconnectTimeIntervalMs) {
        this.reconnectTimeIntervalMs = reconnectTimeIntervalMs;
    }

    public String getConnectRegion() {
        return connectRegion;
    }

    public void setConnectRegion(String connectRegion) {
        this.connectRegion = connectRegion;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isAutoReconnect() {
        return isAutoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        isAutoReconnect = autoReconnect;
    }
}
