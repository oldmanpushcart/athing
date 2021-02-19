package com.github.ompc.athing.aliyun.platform;

/**
 * 阿里云平台应答异常
 */
public class AliyunThingPlatformResponseException extends AliyunThingPlatformException {

    private final String requestId;

    /**
     * 阿里云平台请求异常
     *
     * @param requestId 请求ID（阿里云专有，用来沟通、跟踪和排查问题）
     * @param message   异常信息
     */
    public AliyunThingPlatformResponseException(String requestId, String message) {
        super(message);
        this.requestId = requestId;
    }

    /**
     * 获取阿里云平平台请求ID
     *
     * @return 请求ID
     */
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String getLocalizedMessage() {
        return String.format("platform:/%s/%s %s",
                getPlatformCode(),
                getRequestId(),
                getMessage()
        );
    }

}
