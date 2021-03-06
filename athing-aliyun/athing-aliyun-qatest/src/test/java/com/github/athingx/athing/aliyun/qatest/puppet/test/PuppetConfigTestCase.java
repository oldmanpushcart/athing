package com.github.athingx.athing.aliyun.qatest.puppet.test;

import com.github.athingx.athing.aliyun.config.api.Config;
import com.github.athingx.athing.aliyun.config.api.ConfigThingCom;
import com.github.athingx.athing.aliyun.config.api.Scope;
import com.github.athingx.athing.aliyun.qatest.puppet.PuppetSupport;
import com.github.athingx.athing.aliyun.thing.runtime.ThingRuntimes;
import com.github.athingx.athing.aliyun.thing.runtime.executor.ThingPromise;
import com.github.athingx.athing.standard.platform.message.ThingReplyConfigPushMessage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PuppetConfigTestCase extends PuppetSupport {

    @Test
    public void test$config$pull() throws Exception {

        final ThingPromise<String> promise = ThingRuntimes.getThingRuntime(tPuppet).getThingExecutor().promise();

        tPuppet.getThingCom(ConfigThingCom.class, true).pull(Scope.PRODUCT)
                .onFailure(promise::acceptFail)
                .onSuccess(pullF -> {

                    final Config config = pullF.getSuccess();
                    try {
                        Assert.assertNotNull(config);
                        Assert.assertNotNull(config.getVersion());
                        Assert.assertNotNull(config.getScope());
                    } catch (Exception cause) {
                        promise.tryException(cause);
                    }

                    config.getContent().onDone(promise::acceptDone);

                });

        final String content = promise.get();
        Assert.assertNotNull(content);
        System.out.println(content);

    }


    /*
     * 该测试用例不执行，用于调试阿里云平台远程配置推送
     */
    @Ignore
    @Test
    public void debug$config$push() throws Exception {

        final ThingPromise<String> promise = ThingRuntimes.getThingRuntime(tPuppet).getThingExecutor().promise();
        tPuppet.getThingCom(ConfigThingCom.class, true)
                .setConfigApplyListener((token, config, committer) -> {
                    config.getContent()
                            .onFailure(promise::acceptFail)
                            .onSuccess(future -> {
                                committer.commit();
                                try {
                                    promise.trySuccess(token);
                                } catch (Exception cause) {
                                    promise.tryException(cause);
                                }
                            });
                });

        Assert.assertNotNull(promise.get());
        final ThingReplyConfigPushMessage message = waitingForReplyMessageByToken(promise.get());
        Assert.assertNotNull(message);
        System.out.println("config=" + promise.get());
    }

}
