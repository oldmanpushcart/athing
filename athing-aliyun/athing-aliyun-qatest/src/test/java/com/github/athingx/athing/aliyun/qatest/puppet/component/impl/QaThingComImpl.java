package com.github.athingx.athing.aliyun.qatest.puppet.component.impl;

import com.github.athingx.athing.aliyun.qatest.puppet.component.EchoThingCom;
import com.github.athingx.athing.aliyun.qatest.puppet.component.LightThingCom;
import com.github.athingx.athing.standard.thing.Thing;
import com.github.athingx.athing.standard.thing.boot.ThingComLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QaThingComImpl implements LightThingCom, EchoThingCom, ThingComLifeCycle {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile int bright = 0;
    private volatile State state = State.OFF;

    @Override
    public Echo echoBySync(String words) {
        return new Echo(words);
    }

    @Override
    public Echo echoByAsync(Echo echo) {
        return echo;
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }

    @Override
    public int getBright() {
        return bright;
    }

    @Override
    public void setBright(int bright) {
        this.bright = bright;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        final State pre = this.state;
        this.state = state;
    }

    @Override
    public void turnOn() {
        setState(State.ON);
    }

    @Override
    public void turnOff() {
        setState(State.OFF);
    }

    @Override
    public void onInitialized(Thing thing) throws Exception {
        logger.info("ThingComLifeCycle#onInitialized()");
    }

    @Override
    public void onDestroyed() {
        logger.info("ThingComLifeCycle#onDestroyed()");
    }

    @Override
    public void onConnected() {
        logger.info("ThingComLifeCycle#onConnected()");
    }

    @Override
    public void onDisconnected() {
        logger.info("ThingComLifeCycle#onDisconnected()");
    }

}
