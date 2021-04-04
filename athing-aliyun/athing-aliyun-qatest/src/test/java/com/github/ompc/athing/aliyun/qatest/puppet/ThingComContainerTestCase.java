package com.github.ompc.athing.aliyun.qatest.puppet;

import com.github.ompc.athing.aliyun.qatest.puppet.component.FamilyThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.FatherThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.MotherThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.impl.FamilyThingComImpl;
import com.github.ompc.athing.aliyun.thing.container.ThingComContainerImpl;
import com.github.ompc.athing.aliyun.thing.container.loader.ThingComLoader;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.ThingComContainer;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.boot.ThDepend;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

public class ThingComContainerTestCase {

    private ThingComContainer buildingThingComContainer(ThingCom... thingComponents) throws ThingException {
        return new ThingComContainerImpl("PRODUCT_ID", "THING_ID") {{
            loading(new HashSet<ThingComLoader>() {{
                add((productId, thingId) -> thingComponents);
            }});
        }};
    }

    @Test(expected = ThingException.class)
    public void test$unsupported() throws ThingException {
        buildingThingComContainer(
                new ThingCom() {

                    @ThDepend
                    private Object target;

                }
        );
    }

    @Test
    public void test$success() throws ThingException {

        final ThingComContainer container = buildingThingComContainer(
                new FamilyThingComImpl(),
                new FatherThingCom() {
                },
                new MotherThingCom() {
                }
        );

        final FatherThingCom father = container.getThingComponent(FatherThingCom.class, true);
        final MotherThingCom mother = container.getThingComponent(MotherThingCom.class, true);
        final FamilyThingCom family = container.getThingComponent(FamilyThingCom.class, true);

        Assert.assertEquals(father, family.getFather());
        Assert.assertEquals(mother, family.getMother());
        Assert.assertNotNull(family.getMembers());
        Assert.assertEquals(2, family.getMembers().length);

    }

}
