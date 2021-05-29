package com.github.athingx.athing.aliyun.qatest.puppet.component;

import com.github.athingx.athing.standard.component.ThingCom;
import com.github.athingx.athing.standard.component.ThingEvent;
import com.github.athingx.athing.standard.component.annotation.*;

import static com.github.athingx.athing.aliyun.qatest.puppet.component.EchoThingCom.THING_COM_ID;

@ThCom(id = THING_COM_ID, name = "echo")
@ThEvent(id = "echo_event", type = EchoThingCom.Echo.class)
public interface EchoThingCom extends ThingCom {

    String THING_COM_ID = "athing_qatest_echo";

    @ThService(isSync = true)
    Echo echoBySync(@ThParam("words") String words);

    @ThService
    Echo echoByAsync(@ThParam("echo") Echo echo);

    @ThProperty
    long now();

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
