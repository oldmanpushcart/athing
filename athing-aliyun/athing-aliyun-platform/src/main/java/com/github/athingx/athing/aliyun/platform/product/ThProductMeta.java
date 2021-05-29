package com.github.athingx.athing.aliyun.platform.product;

import com.github.athingx.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThEventMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.athingx.athing.standard.component.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 设备产品元数据
 */
public class ThProductMeta {

    private final Map<String, ThComMeta> thComMetaMap;

    public ThProductMeta(ThComMeta[] thComMetas) {
        this.thComMetaMap = Collections.unmodifiableMap(
                Stream.of(thComMetas)
                        .collect(Collectors.toMap(
                                ThComMeta::getThingComId,
                                meta -> meta
                        )));
    }

    public Map<String, ThComMeta> getThComMetaMap() {
        return thComMetaMap;
    }

    /**
     * 获取事件元数据
     *
     * @param identity 事件ID
     * @return 事件元数据
     */
    public ThEventMeta getThEventMeta(String identity) {
        if (!Identifier.test(identity)) {
            return null;
        }
        return getThEventMeta(Identifier.parseIdentity(identity));
    }

    /**
     * 获取服务元数据
     *
     * @param identity 服务ID
     * @return 服务元数据
     */
    public ThServiceMeta getThServiceMeta(String identity) {
        if (!Identifier.test(identity)) {
            return null;
        }
        return getThServiceMeta(Identifier.parseIdentity(identity));
    }

    /**
     * 获取属性元数据
     *
     * @param identity 属性ID
     * @return 属性元数据
     */
    public ThPropertyMeta getThPropertyMeta(String identity) {
        if (!Identifier.test(identity)) {
            return null;
        }
        return getThPropertyMeta(Identifier.parseIdentity(identity));
    }

    /**
     * 获取事件元数据
     *
     * @param identifier 事件ID
     * @return 事件元数据
     */
    public ThEventMeta getThEventMeta(Identifier identifier) {
        final ThComMeta thComMeta = thComMetaMap.get(identifier.getComponentId());
        return null != thComMeta
                ? thComMeta.getThEventMeta(identifier)
                : null;
    }

    /**
     * 获取服务元数据
     *
     * @param identifier 服务ID
     * @return 服务元数据
     */
    public ThServiceMeta getThServiceMeta(Identifier identifier) {
        final ThComMeta thComMeta = thComMetaMap.get(identifier.getComponentId());
        return null != thComMeta
                ? thComMeta.getThServiceMeta(identifier)
                : null;
    }

    /**
     * 获取属性元数据
     *
     * @param identifier 属性ID
     * @return 属性元数据
     */
    public ThPropertyMeta getThPropertyMeta(Identifier identifier) {
        final ThComMeta thComMeta = thComMetaMap.get(identifier.getComponentId());
        return null != thComMeta
                ? thComMeta.getThPropertyMeta(identifier)
                : null;
    }

}
