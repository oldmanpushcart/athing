package com.github.athingx.athing.aliyun.framework.component.meta;

import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.component.annotation.ThEvent;

import static com.github.athingx.athing.aliyun.framework.component.ThComMetaHelper.getDefaultMemberName;
import static com.github.athingx.athing.aliyun.framework.util.CommonUtils.isEmptyString;

/**
 * 设备组件事件元数据
 */
public class ThEventMeta {

    private final Identifier identifier;
    private final ThEvent anThEvent;

    ThEventMeta(String componentId, ThEvent anThEvent) {
        this.anThEvent = anThEvent;
        this.identifier = Identifier.toIdentifier(componentId, anThEvent.id());
    }

    /**
     * 获取事件标识
     *
     * @return 事件标识
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String getName() {
        return isEmptyString(anThEvent.name())
                ? getDefaultMemberName(getIdentifier())
                : anThEvent.name();
    }

    /**
     * 获取事件描述
     *
     * @return 事件描述
     */
    public String getDesc() {
        return anThEvent.desc();
    }

    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    public Class<? extends ThingEvent.Data> getType() {
        return anThEvent.type();
    }

    /**
     * 获取事件等级
     *
     * @return 事件等级
     */
    public ThEvent.Level getLevel() {
        return anThEvent.level();
    }

}
