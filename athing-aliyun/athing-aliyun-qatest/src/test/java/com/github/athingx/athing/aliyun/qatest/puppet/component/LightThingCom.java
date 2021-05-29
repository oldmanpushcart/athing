package com.github.athingx.athing.aliyun.qatest.puppet.component;

import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.component.annotation.ThCom;
import com.github.athingx.athing.standard.component.annotation.ThProperty;
import com.github.athingx.athing.standard.component.annotation.ThService;

import static com.github.athingx.athing.aliyun.qatest.puppet.component.LightThingCom.THING_COM_ID;

@ThCom(id = THING_COM_ID, name = "light")
public interface LightThingCom extends ThingCom {

    String THING_COM_ID = "athing_qatest_light";

    @ThProperty
    int getBright();

    void setBright(int bright);

    @ThProperty
    State getState();

    void setState(State state);

    @ThService
    void turnOn();

    @ThService
    void turnOff();

    /**
     * 设备状态
     */
    enum State {

        ON,
        OFF

    }

}
