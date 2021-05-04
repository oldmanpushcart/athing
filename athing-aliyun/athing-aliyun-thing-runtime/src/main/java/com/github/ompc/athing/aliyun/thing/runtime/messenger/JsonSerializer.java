package com.github.ompc.athing.aliyun.thing.runtime.messenger;

/**
 * Json序列化器
 */
public interface JsonSerializer {

    /**
     * 将目标对象序列化为Json字符串
     *
     * @param object 对象实例
     * @return Json字符串
     */
    String toJson(Object object);

}
