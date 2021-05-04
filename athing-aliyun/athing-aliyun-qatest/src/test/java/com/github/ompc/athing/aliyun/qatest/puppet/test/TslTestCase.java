package com.github.ompc.athing.aliyun.qatest.puppet.test;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.qatest.puppet.component.EchoThingCom;
import com.github.ompc.athing.aliyun.qatest.puppet.component.LightThingCom;
import com.github.ompc.athing.aliyun.thing.tsl.TslDumper;
import com.github.ompc.athing.component.dmgr.api.DmgrThingCom;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TslTestCase {

    @Test
    public void test_dump() throws IOException {

        final TslDumper.DumpToZipFile dumpTo = new TslDumper.DumpToZipFile();
        TslDumper.dump(dumpTo, "a1A6pkcikAz",
                DmgrThingCom.class, LightThingCom.class, EchoThingCom.class
        );

        dumpTo.toZipFile(new File("dump.zip"));

    }

    @Test
    public void test_() {

        final MapObject object = new MapObject()
                .putProperty("name", "dukun")
                .putProperty("age", 35)
                .enterProperty("contacts")
                .putProperty("tel", "13989838402")
                .putProperty("email", "oldmanpushcart@gmail.com")
                .exitProperty();

        System.out.println(GsonFactory.getGson().toJson(object));

    }

}
