package com.github.athingx.athing.standard.platform;

import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.platform.domain.SortOrder;
import com.github.athingx.athing.standard.platform.domain.ThingPropertySnapshot;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 设备模版
 */
public interface ThingTemplate {

    /**
     * 获取设备组件
     *
     * @param expectThingComType 期待组件类型
     * @param <T>                组件类型
     * @return 设备组件
     */
    <T extends ThingCom> T getThingComponent(Class<T> expectThingComType);

    /**
     * 批量设置设备属性
     *
     * @param propertyValueMap 属性值集合
     * @throws ThingPlatformException 操作失败
     */
    void batchSetProperties(Map<Identifier, Object> propertyValueMap) throws ThingPlatformException;

    /**
     * 批量获取属性快照
     *
     * @param identifiers 属性标识集合
     * @return 属性快照集合
     * @throws ThingPlatformException 操作失败
     */
    Map<Identifier, ThingPropertySnapshot> batchGetProperties(Set<Identifier> identifiers) throws ThingPlatformException;

    /**
     * 迭代查询属性快照
     *
     * @param identifier 属性标识
     * @param batch      批次数量
     *                   每次迭代器更新时从云端获取数据量，迭代器会一次拿一批数据到本地内存中进行迭代遍历
     * @param order      排序顺序
     * @return 属性快照迭代器
     * @throws ThingPlatformException 操作失败
     */
    Iterator<ThingPropertySnapshot> iteratorForPropertySnapshot(Identifier identifier, int batch, SortOrder order) throws ThingPlatformException;

}
