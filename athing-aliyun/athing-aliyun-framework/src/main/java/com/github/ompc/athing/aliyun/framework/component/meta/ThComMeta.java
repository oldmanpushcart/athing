package com.github.ompc.athing.aliyun.framework.component.meta;

import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.component.annotation.ThCom;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备组件元数据
 * <p>
 * 设备组件元数据由设备组件注解解析得来
 * </p>
 */
public class ThComMeta {

    private final String thingComId;
    private final ThCom anThCom;
    private final Class<?> thingComType;
    private final Map<Identifier, ThEventMeta> identityThEventMetaMap;
    private final Map<Identifier, ThPropertyMeta> identityThPropertyMetaMap;
    private final Map<Identifier, ThServiceMeta> identityThServiceMetaMap;

    /**
     * 命名设备组件
     *
     * @param anThCom                   设备组件注解
     * @param thingComType              设备组件接口类型
     * @param identityThEventMetaMap    事件元数据集合
     * @param identityThPropertyMetaMap 属性元数据集合
     * @param identityThServiceMetaMap  服务元数据集合
     */
    public ThComMeta(final ThCom anThCom,
                     final Class<?> thingComType,
                     final Map<Identifier, ThEventMeta> identityThEventMetaMap,
                     final Map<Identifier, ThPropertyMeta> identityThPropertyMetaMap,
                     final Map<Identifier, ThServiceMeta> identityThServiceMetaMap) {
        this(
                anThCom.id(),
                anThCom,
                thingComType,
                identityThEventMetaMap,
                identityThPropertyMetaMap,
                identityThServiceMetaMap
        );
    }


    private ThComMeta(final String thingComId,
                      final ThCom anThCom,
                      final Class<?> thingComType,
                      final Map<Identifier, ThEventMeta> identityThEventMetaMap,
                      final Map<Identifier, ThPropertyMeta> identityThPropertyMetaMap,
                      final Map<Identifier, ThServiceMeta> identityThServiceMetaMap) {
        this.thingComId = thingComId;
        this.anThCom = anThCom;
        this.thingComType = thingComType;
        this.identityThEventMetaMap = identityThEventMetaMap;
        this.identityThPropertyMetaMap = identityThPropertyMetaMap;
        this.identityThServiceMetaMap = identityThServiceMetaMap;
    }

    /**
     * 获取设备组件ID
     *
     * @return 设备组件ID
     */
    public String getThingComId() {
        return thingComId;
    }

    /**
     * 获取设备组件名称
     *
     * @return 设备组件名称
     */
    public String getThingComName() {
        return anThCom.name();
    }

    /**
     * 获取设备组件描述
     *
     * @return 设备组件描述
     */
    public String getThingComDesc() {
        return anThCom.desc();
    }

    /**
     * 获取设备组件类型
     * <p>
     * 设备组件类型必须是一个接口，且继承于{@link ThingCom}
     * </p>
     *
     * @return 设备组件类型
     */
    public Class<?> getThingComType() {
        return thingComType;
    }


    /**
     * 获取标识事件元数据集合
     *
     * @return 标识事件元数据集合
     */
    public Map<Identifier, ThEventMeta> getIdentityThEventMetaMap() {
        return identityThEventMetaMap;
    }

    /**
     * 获取标识属性元数据集合
     *
     * @return 标识属性元数据集合
     */
    public Map<Identifier, ThPropertyMeta> getIdentityThPropertyMetaMap() {
        return identityThPropertyMetaMap;
    }

    /**
     * 获取标识属性元数据
     *
     * @param identifier 属性标识
     * @return 属性元数据
     */
    public ThPropertyMeta getThPropertyMeta(Identifier identifier) {
        return identityThPropertyMetaMap.get(identifier);
    }

    /**
     * 获取标识服务元数据集合
     *
     * @return 标识服务元数据集合
     */
    public Map<Identifier, ThServiceMeta> getIdentityThServiceMetaMap() {
        return identityThServiceMetaMap;
    }

    /**
     * 获取标识服务元数据
     *
     * @param identifier 服务标识
     * @return 服务元数据
     */
    public ThServiceMeta getThServiceMeta(Identifier identifier) {
        return identityThServiceMetaMap.get(identifier);
    }

    /**
     * 匿名组件ID序列
     */
    private final static AtomicInteger anonymousSeq = new AtomicInteger(1000);

    /**
     * 生成匿名的设备组件元数据
     *
     * @param thingComType 设备组件类型
     * @return 匿名设备组件元数据
     */
    public static ThComMeta anonymous(final Class<?> thingComType) {
        return new ThComMeta(
                new ThCom() {

                    private final String id = String.format("anonymous$%s", anonymousSeq.getAndIncrement());
                    private final String name = "anonymous component";
                    private final String desc = "anonymous component";

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ThCom.class;
                    }

                    @Override
                    public String id() {
                        return id;
                    }

                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public String desc() {
                        return desc;
                    }
                },
                thingComType,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }

}
