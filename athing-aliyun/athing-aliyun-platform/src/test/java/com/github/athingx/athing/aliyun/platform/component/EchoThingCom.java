package com.github.athingx.athing.aliyun.platform.component;

import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.component.annotation.*;

@ThCom(id = "echo", name = "echo")
@ThEvent(id = "echo_event", type = EchoThingCom.Echo.class)
public interface EchoThingCom {

    @ThService(isSync = true)
    Echo echoBySync(@ThParam("echo") Echo echo);

    @ThService
    Echo echoByAsync(@ThParam("echo") Echo echo);

    @ThProperty
    State getState();

    void setState(State state);

    /**
     * 设备状态
     */
    enum State {
        ON,
        OFF
    }

    /**
     * 回声信号
     */
    class Echo implements ThingEvent.Data {

        private final String words;

        public Echo(String words) {
            this.words = words;
        }

        public String getWords() {
            return words;
        }

    }

}
