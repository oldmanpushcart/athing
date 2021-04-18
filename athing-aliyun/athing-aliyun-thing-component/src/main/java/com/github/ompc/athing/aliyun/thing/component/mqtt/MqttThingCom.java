package com.github.ompc.athing.aliyun.thing.component.mqtt;

import com.github.ompc.athing.standard.component.ThingCom;

/**
 * MQTT组件
 */
public interface MqttThingCom extends ThingCom {

    /**
     * 获取MQTT
     *
     * @return MQTT
     */
    Mqtt getMqtt();

}
