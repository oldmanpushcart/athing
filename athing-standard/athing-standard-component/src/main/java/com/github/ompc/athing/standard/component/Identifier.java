package com.github.ompc.athing.standard.component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 标识
 */
public class Identifier {

    /**
     * 保留字集合
     * <p>
     * alink协议在支持物模型上有一定的向下兼容性问题，一些成员名会出现与topic约定的重名，
     * 这里设计保留字机制就是为了解决这类问题
     * </p>
     */
    private static final Set<String> reserves = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "batch",
            "property",
            "event",
            "service"
    )));

    /**
     * identity格式正则
     */
    private static final String IDENTITY_REGEX = "^([A-z0-9_]{1,30}:)?[A-z0-9_]{1,50}$";

    /**
     * 组件ID格式正则
     * <p>英文大小写字母、数字和下划线，不超过30个字符</p>
     */
    private static final String COMPONENT_ID_REGEX = "^[A-z0-9_]{1,30}$";

    /**
     * 成员ID格式正则
     * <p>英文大小写字母、数字和下划线，不超过50个字符</p>
     */
    private static final String MEMBER_ID_REGEX = "^[A-z0-9_]{1,50}$";

    /**
     * 默认组件ID
     */
    public static final String DEFAULT_COMPONENT_ID = "default";

    private final String componentId;
    private final String memberId;
    private final String identity;

    /**
     * 标识
     *
     * @param componentId 组件ID
     * @param memberId    成员ID
     */
    private Identifier(String componentId, String memberId) {
        this.componentId = checkComponentId(componentId);
        this.memberId = checkMemberId(memberId);
        this.identity = checkIdentity(
                DEFAULT_COMPONENT_ID.equals(componentId)
                        ? memberId
                        : String.format("%s:%s", componentId, memberId)
        );
    }

    private static String checkIdentity(String identity) {
        if (!test(identity)) {
            throw new IllegalArgumentException(String.format("illegal format identity: %s", identity));
        }
        return identity;
    }

    private static String checkComponentId(String componentId) {
        if (!componentId.matches(COMPONENT_ID_REGEX)) {
            throw new IllegalArgumentException(String.format("illegal format component-id: %s", componentId));
        }
        return componentId;
    }

    private static String checkMemberId(String memberId) {
        if (!memberId.matches(MEMBER_ID_REGEX)) {
            throw new IllegalArgumentException(String.format("illegal format member-id: %s", memberId));
        }
        if (reserves.contains(memberId)) {
            throw new IllegalArgumentException(String.format("member-id: %s is reserve words", memberId));
        }
        return memberId;
    }

    /**
     * 测试标识值是否合法
     *
     * @param identity 标识值
     * @return TRUE | FALSE
     */
    public static boolean test(String identity) {
        return identity.matches(IDENTITY_REGEX);
    }

    /**
     * 解析标识
     *
     * @param identity 标识值
     * @return 标识
     */
    public static Identifier parseIdentity(String identity) {
        final String[] segments = checkIdentity(identity).split(":");
        return segments.length == 1
                ? new Identifier(DEFAULT_COMPONENT_ID, segments[0])
                : new Identifier(segments[0], segments[1]);
    }

    /**
     * 转换为标识
     *
     * @param componentId 组件ID
     * @param memberId    成员ID
     * @return 标识
     */
    public static Identifier toIdentifier(String componentId, String memberId) {
        return new Identifier(componentId, memberId);
    }

    /**
     * 转换为标识
     *
     * @param memberId 成员ID
     * @return 标识
     */
    public static Identifier toIdentifier(String memberId) {
        return new Identifier(DEFAULT_COMPONENT_ID, memberId);
    }

    /**
     * 获取组件ID
     *
     * @return 组件ID
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * 获取成员ID
     *
     * @return 成员ID
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * 获取标识值
     *
     * @return 标识值
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * 是否默认组件
     *
     * @return TRUE | FALSE
     */
    public boolean isDefault() {
        return DEFAULT_COMPONENT_ID.equals(componentId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        // 如果目标是字符串
        if (other instanceof String) {
            return Objects.equals(identity, other);
        }

        // 目标是标识
        if (other instanceof Identifier) {
            return Objects.equals(identity, ((Identifier) other).identity);
        }

        // 其他不比对
        return false;

    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    @Override
    public String toString() {
        return identity;
    }

}
