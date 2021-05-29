package com.github.athingx.athing.aliyun.qatest.puppet.test;

import com.github.athingx.athing.aliyun.component.modular.api.Committer;
import com.github.athingx.athing.aliyun.component.modular.api.ModularThingCom;
import com.github.athingx.athing.aliyun.component.modular.api.ModuleUpgrade;
import com.github.athingx.athing.aliyun.component.modular.api.ModuleUpgradeListener;
import com.github.athingx.athing.aliyun.qatest.puppet.PuppetSupport;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntimes;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.standard.platform.message.ThingModularUpgradeMessage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PuppetModularTestCase extends PuppetSupport {

    /*
     * 该测试用例不执行，用于调试阿里云平台远程OTA推送
     */
    @Ignore
    @Test
    public void debug$modular$push() throws Exception {

        final ThingPromise<String> promise = ThingRuntimes.getThingRuntime(tPuppet).getThingExecutor().promise();

        // 先订阅升级的推送监听
        tPuppet.getThingCom(ModularThingCom.class, true)
                .setModuleUpgradeListener(new ModuleUpgradeListener() {
                    @Override
                    public void upgrade(String token, ModuleUpgrade upgrade, Committer committer) throws Exception {
                        upgrade.getUpgradeFile()
                                .onFailure(promise::acceptFail)
                                .onSuccess(future -> {

                                    committer.commit();

                                    try {
                                        promise.trySuccess(upgrade.getModuleId());
                                    } catch (Exception cause) {
                                        promise.tryException(cause);
                                    }

                                });
                    }
                });

        // 推送一把模块版本，等待阿里云推送最新的模块升级包
        tPuppet.getThingCom(ModularThingCom.class, true)
                .post("resource", "1.0.0")
                .sync();

        Assert.assertNotNull(promise.get());
        final ThingModularUpgradeMessage message = waitingForThingModularUpgradeMessageByModuleId(promise.get());
        Assert.assertNotNull(message);
        Assert.assertEquals("1.0.0", message.getSrcVersion());
        Assert.assertEquals("1.0.1", message.getDstVersion());
        Assert.assertEquals(ThingModularUpgradeMessage.Result.SUCCEEDED, message.getResult());

    }

}
