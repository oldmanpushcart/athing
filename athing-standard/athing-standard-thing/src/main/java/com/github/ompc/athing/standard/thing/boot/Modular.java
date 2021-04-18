package com.github.ompc.athing.standard.thing.boot;

import com.github.ompc.athing.standard.component.ThingCom;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.ThingFuture;

import java.io.File;

/**
 * 模块化
 * <p>
 * 标记一个设备组件可模块化管理，模块化后的组件可进行OTA升级
 * </p>
 */
public interface Modular extends ThingCom {

    /**
     * 获取模块ID
     *
     * @return 模块ID
     */
    String getModuleId();

    /**
     * 获取模块版本
     *
     * @return 模块版本
     */
    String getModuleVersion();

    /**
     * 模块升级
     *
     * @param upgrade 升级信息
     * @param commit  升级提交
     * @throws Exception 升级失败
     */
    void upgrade(Upgrade upgrade, Commit commit) throws Exception;

    /**
     * 升级信息
     */
    interface Upgrade {

        /**
         * 获取模块ID
         *
         * @return 模块ID
         */
        String getModuleId();

        /**
         * 获取升级版本
         *
         * @return 升级版本
         */
        String getUpgradeVersion();

        /**
         * 获取升级文件
         *
         * @return 获取文件凭证
         */
        ThingFuture<File> getUpgradeFile();

    }

    /**
     * 升级提交
     */
    interface Commit {

        /**
         * 提交升级
         *
         * @throws ThingException 提交升级失败
         */
        void commit() throws ThingException;

    }

}
