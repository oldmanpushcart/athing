package com.github.athingx.athing.standard.thing.boot.spi;

/**
 * 设备组件Jar文件卸载
 */
public interface ThingComJarUnLoadSpi {

    /**
     * 设备组件Jar文件卸载完所有组件后，正式卸载Jar文件之前调用！
     */
    void onJarUnLoadCompleted();

}
