package com.github.ompc.athing.component.dmgr.core;

import com.github.ompc.athing.component.dmgr.core.impl.DmgrThingComImpl;
import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.boot.BootArguments;
import com.github.ompc.athing.standard.thing.boot.ThingComBoot;
import org.kohsuke.MetaInfServices;

import java.util.Properties;

@MetaInfServices
public class DmgrThingComBoot implements ThingComBoot {

    private final ThingCom thingCom = new DmgrThingComImpl();

    @Override
    public Specifications getSpecifications() {
        return () -> new Properties() {{
            put("AUTHOR", "oldmanpushcart@gmail.com");
            put("VERSION", "1.0.0-SNAPSHOT");
            put("DATE", "2020-12-02");
        }};
    }

    @Override
    public ThingCom bootUp(String productId, String thingId, BootArguments arguments) {
        return thingCom;
    }

}
