package com.github.athingx.athing.aliyun.framework.component;

import com.github.athingx.athing.aliyun.framework.component.meta.ThComMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThEventMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.athingx.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.component.annotation.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ThComMetaHelperTestCase {

    /**
     * 接口多重继承组件接口
     */
    @Test
    public void test$interface_multi_extends_component() {
        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(Me.class);
        Assert.assertEquals(3, metas.size());
        Assert.assertTrue(metas.containsKey("father"));
        Assert.assertTrue(metas.containsKey("worker"));
        Assert.assertTrue(metas.containsKey("person"));
    }

    /**
     * 实现类实现多重组件接口
     */
    @Test
    public void test$class_multi_implements_component() {
        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(MyDogImpl.class);
        Assert.assertEquals(5, metas.size());
        Assert.assertTrue(metas.containsKey("father"));
        Assert.assertTrue(metas.containsKey("worker"));
        Assert.assertTrue(metas.containsKey("person"));
        Assert.assertTrue(metas.containsKey("animal"));
        Assert.assertTrue(metas.containsKey("dog"));
    }

    /**
     * 匿名组件接口
     */
    @Test
    public void test$anonymous_interface_component() {

        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(AnonymousThingCom.class);
        Assert.assertEquals(1, metas.size());

        final ThComMeta meta = metas.values().iterator().next();
        Assert.assertTrue(meta.getThingComId().startsWith("anonymous"));
        Assert.assertTrue(meta.getThingComName().startsWith("anonymous"));
        Assert.assertTrue(AnonymousThingCom.class.isAssignableFrom(meta.getThingComType()));

    }

    /**
     * 匿名组件实现
     */
    @Test
    public void test$anonymous_class_component() {

        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(AnonymousThingComImpl.class);
        Assert.assertEquals(1, metas.size());

        final ThComMeta meta = metas.values().iterator().next();
        Assert.assertTrue(meta.getThingComId().startsWith("anonymous"));
        Assert.assertTrue(meta.getThingComName().startsWith("anonymous"));
        Assert.assertTrue(AnonymousThingCom.class.isAssignableFrom(meta.getThingComType()));
        Assert.assertTrue(AnonymousThingComImpl.class.isAssignableFrom(meta.getThingComType()));

    }

    /**
     * 匿名组件实现（非实现接口）
     */
    @Test
    public void test$anonymous_without_interface_component() {

        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(AnonymousWithoutInterfaceThingComImpl.class);
        Assert.assertEquals(1, metas.size());

        final ThComMeta meta = metas.values().iterator().next();
        Assert.assertTrue(meta.getThingComId().startsWith("anonymous"));
        Assert.assertTrue(meta.getThingComName().startsWith("anonymous"));
        Assert.assertTrue(AnonymousWithoutInterfaceThingComImpl.class.isAssignableFrom(meta.getThingComType()));

    }

    @Test
    public void test$test_component() {

        final Map<String, ThComMeta> metas = ThComMetaHelper.getThComMetaMap(TestThingCom.class);
        Assert.assertEquals(1, metas.size());
        Assert.assertTrue(metas.containsKey("test"));

        final ThComMeta meta = metas.values().iterator().next();

        // 测试：prop
        {
            final ThPropertyMeta pMeta = meta.getThPropertyMeta(Identifier.toIdentifier("test", "prop"));
            Assert.assertFalse(pMeta.isReadonly());
            Assert.assertFalse(pMeta.isRequired());
            Assert.assertNotNull(pMeta.getGetter());
            Assert.assertNotNull(pMeta.getSetter());
            Assert.assertEquals(String.class, pMeta.getPropertyType());
            Assert.assertEquals("prop", pMeta.getName());
        }


        // 测试：readonly_prop_by_none_setter
        {
            final ThPropertyMeta pMeta = meta.getThPropertyMeta(Identifier.toIdentifier("test", "readonly_prop_by_none_setter"));
            Assert.assertTrue(pMeta.isReadonly());
            Assert.assertTrue(pMeta.isRequired());
            Assert.assertNotNull(pMeta.getGetter());
            Assert.assertNull(pMeta.getSetter());
            Assert.assertEquals("readonly-prop-by-none-setter", pMeta.getName());
        }

        // 测试：readonly_prop_by_wrong_setter
        {
            final ThPropertyMeta pMeta = meta.getThPropertyMeta(Identifier.toIdentifier("test", "readonly_prop_by_wrong_setter"));
            Assert.assertTrue(pMeta.isReadonly());
            Assert.assertTrue(pMeta.isRequired());
            Assert.assertNotNull(pMeta.getGetter());
            Assert.assertNull(pMeta.getSetter());
            Assert.assertEquals("readonly-prop-by-wrong-setter", pMeta.getName());
        }

        // 测试：service
        {
            final ThServiceMeta sMeta = meta.getThServiceMeta(Identifier.toIdentifier("test", "say"));
            Assert.assertEquals(String.class, sMeta.getReturnType());
            Assert.assertTrue(sMeta.isSync());
            Assert.assertEquals(2, sMeta.getThParamMetas().length);

            Assert.assertEquals(0, sMeta.getThParamMetas()[0].getIndex());
            Assert.assertEquals("arg1", sMeta.getThParamMetas()[0].getName());
            Assert.assertEquals(int.class, sMeta.getThParamMetas()[0].getType());

            Assert.assertEquals(1, sMeta.getThParamMetas()[1].getIndex());
            Assert.assertEquals("arg2", sMeta.getThParamMetas()[1].getName());
            Assert.assertEquals(String.class, sMeta.getThParamMetas()[1].getType());
            Assert.assertEquals("say", sMeta.getName());

        }

        // 测试：event
        {
            final Map<Identifier, ThEventMeta> eMataMap = meta.getIdentityThEventMetaMap();
            Assert.assertEquals(2, eMataMap.size());

            // 测试：event_a
            {
                final ThEventMeta eMeta = meta.getThEventMeta(Identifier.toIdentifier("test", "event_a"));
                Assert.assertEquals(TestAEventData.class, eMeta.getType());
                Assert.assertEquals(ThEvent.Level.INFO, eMeta.getLevel());
                Assert.assertEquals("event-a", eMeta.getName());
            }

            // 测试：event_b
            {
                final ThEventMeta eMeta = meta.getThEventMeta(Identifier.toIdentifier("test", "event_b"));
                Assert.assertEquals(TestBEventData.class, eMeta.getType());
                Assert.assertEquals(ThEvent.Level.WARN, eMeta.getLevel());
                Assert.assertEquals("event-b", eMeta.getName());
            }

        }

    }


    // --- 测试接口 ---

    public interface Me extends Father, Worker {
    }

    public interface MyDog extends Me, Dog {
    }

    public static class MyDogImpl implements MyDog {
    }

    @ThCom(id = "father", name = "father")
    public interface Father extends Person {
    }

    @ThCom(id = "worker", name = "worker")
    public interface Worker extends Person {
    }

    @ThCom(id = "person", name = "person")
    public interface Person extends ThingCom {
    }

    @ThCom(id = "animal", name = "animal")
    public interface Animal extends ThingCom {

    }

    @ThCom(id = "dog", name = "dog")
    public interface Dog extends Animal {
    }

    public interface AnonymousThingCom extends ThingCom {
    }

    public static class AnonymousThingComImpl implements AnonymousThingCom {
    }

    public static class AnonymousWithoutInterfaceThingComImpl implements ThingCom {
    }

    public static class TestAEventData implements ThingEvent.Data {
    }

    public static class TestBEventData implements ThingEvent.Data {
    }

    @ThCom(id = "test", name = "test")
    @ThEvent(id = "event_a", type = TestAEventData.class)
    @ThEvent(id = "event_b", type = TestBEventData.class, level = ThEvent.Level.WARN)
    public interface TestThingCom extends ThingCom {

        @ThProperty(isRequired = false)
        String getProp();

        String setProp(String name);

        @ThProperty
        String getReadonlyPropByNoneSetter();

        @ThProperty
        String getReadonlyPropByWrongSetter();

        int setReadonlyPropByWrongSetter(int value);

        @ThService(isSync = true)
        String say(@ThParam("arg1") int arg1, @ThParam("arg2") String arg2);

    }

}
