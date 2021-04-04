package com.github.ompc.athing.aliyun.thing.executor.impl.modular;

import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.thing.ThingImpl;
import com.github.ompc.athing.aliyun.thing.executor.MqttExecutor;
import com.github.ompc.athing.aliyun.thing.executor.ThingMessenger;
import com.github.ompc.athing.aliyun.thing.util.FileUtils;
import com.github.ompc.athing.aliyun.thing.util.HttpUtils;
import com.github.ompc.athing.standard.thing.ThingException;
import com.github.ompc.athing.standard.thing.boot.Modular;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.generateToken;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * 平台推送设备固件升级执行器
 * <a href="https://help.aliyun.com/document_detail/89307.html">固件升级</a>
 */
public class ThingModularUpgradePushMqttExecutor implements MqttExecutor, MqttExecutor.MqttMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingImpl thing;
    private final ThingMessenger messenger;
    private final Gson gson = GsonFactory.getGson();

    public ThingModularUpgradePushMqttExecutor(ThingImpl thing, ThingMessenger messenger) {
        this.thing = thing;
        this.messenger = messenger;
    }

    @Override
    public void init(MqttSubscriber subscriber) throws ThingException {
        subscriber.subscribe(
                format("/ota/device/upgrade/%s/%s", thing.getProductId(), thing.getThingId()),
                this
        );
    }

    @Override
    public void handle(String mqttTopic, MqttMessage mqttMessage) {

        final PushUpgrade pushUpgrade = gson.fromJson(new String(mqttMessage.getPayload(), UTF_8), PushUpgrade.class);
        final String reqId = pushUpgrade.id;
        final String moduleId = pushUpgrade.data.module;
        final String version = pushUpgrade.data.version;

        logger.info("{}/module/upgrade/push receive upgrade push, req={};module={};version={};size={};sign-mode={};sign={};code={};message={};",
                thing, reqId, moduleId, version,
                pushUpgrade.data.size,
                pushUpgrade.data.signMethod,
                pushUpgrade.data.sign,
                pushUpgrade.code,
                pushUpgrade.message
        );

        try {

            // 检查模块是否存在
            final Modular module = thing.getThingComponents(Modular.class).stream()
                    .filter(modular -> Objects.equals(modular.getModuleId(), moduleId))
                    .findAny()
                    .orElse(null);

            if (null == module) {
                throw new ModularUpgradeProcessException(
                        ModularUpgradeProcessException.STEP_UPGRADE_FAILURE,
                        "module not existed!"
                );
            }

            // 执行升级
            try {
                module.upgrade(
                        new UpgradeImpl(pushUpgrade, ".push$" + reqId),
                        new CommitImpl(module)
                );
                upgradeProcessing(reqId, moduleId, ModularUpgradeProcessException.STEP_FINISHED, "finished");
                logger.info("{}/module/upgrade success, req={};module={};version={};", thing, reqId, moduleId, version);
            }

            // 升级过程可以随时报告进度异常
            catch (ModularUpgradeProcessException cause) {
                throw cause;
            }

            // 未知的异常判定为烧录失败
            catch (Exception cause) {
                throw new ModularUpgradeProcessException(
                        ModularUpgradeProcessException.STEP_WRITE_FAILURE,
                        String.format("write failure: %s", cause.getMessage()),
                        cause
                );
            }

        } catch (Exception cause) {

            logger.warn("{}/module/upgrade failure, req={};module={};", thing, reqId, moduleId, cause);

            // 指定的升级步骤异常
            if (cause instanceof ModularUpgradeProcessException) {
                final ModularUpgradeProcessException processCause = (ModularUpgradeProcessException) cause;
                upgradeProcessing(reqId, moduleId, processCause.getStep(), processCause.getDesc());
            }

            // 未知的升级步骤异常
            else {
                upgradeProcessing(reqId, moduleId, ModularUpgradeProcessException.STEP_UPGRADE_FAILURE, String.format("upgrade failure: %s", cause.getMessage()));
            }


        }

    }

    /*
     * 更新进度
     */
    private void upgradeProcessing(String reqId, String moduleId, int step, String desc) {

        final String topic = format("/ota/device/progress/%s/%s", thing.getProductId(), thing.getThingId());
        final Object message = new MapObject()
                .putProperty("id", generateToken())
                .enterProperty("params")
                /**/.putProperty("step", step)
                /**/.putProperty("desc", desc)
                /**/.putProperty("module", moduleId)
                .exitProperty();

        messenger.post(topic, message)
                .onSuccess(future -> logger.debug("{}/module/upgrade/push processing, req={};module={};step={};",
                        thing, reqId, moduleId, step))
                .onException(future -> logger.warn("{}/module/upgrade/push processing error, req={};module={};step={};",
                        thing, reqId, moduleId, step, future.getException())
                );
    }

    /**
     * 设备平台推送固件升级数据
     */
    private static class PushUpgrade {

        @SerializedName("id")
        String id;

        @SerializedName("code")
        String code;

        @SerializedName("message")
        String message;

        @SerializedName("data")
        Data data;

        static class Data {

            @SerializedName("signMethod")
            String signMethod;

            @SerializedName("size")
            long size;

            @SerializedName("version")
            String version;

            @SerializedName("url")
            String url;

            @SerializedName("md5")
            String md5;

            @SerializedName("sign")
            String sign;

            @SerializedName("module")
            String module;

        }

    }


    /**
     * 设备固件升级实现
     */
    private class UpgradeImpl implements Modular.Upgrade, HttpUtils.Downloading {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final String reqId;
        private final String moduleId;
        private final String version;
        private final String upgradeFileURL;
        private final String upgradeFileCHS;
        private final String suffix;
        private final AtomicReference<File> thingModuleUpgradeFileRef = new AtomicReference<>();

        private int max = 0;

        /**
         * 设备固件升级
         *
         * @param pushUpgrade 推送升级数据
         * @param suffix      文件后缀
         */
        UpgradeImpl(final PushUpgrade pushUpgrade, final String suffix) {
            this.reqId = pushUpgrade.id;
            this.moduleId = pushUpgrade.data.module;
            this.version = pushUpgrade.data.version;
            this.upgradeFileURL = pushUpgrade.data.url;
            this.upgradeFileCHS = pushUpgrade.data.md5;
            this.suffix = suffix;
        }

        private File buildingThingModuleUpgradeFile() throws IOException {
            return File.createTempFile(
                    format("%s-%s-%s-%s-", thing.getProductId(), thing.getThingId(), moduleId, version),
                    suffix
            );
        }

        private File download() throws ThingException {
            try {
                final File file = buildingThingModuleUpgradeFile();
                logger.info("{}/module/upgrade downloading: {} -> {}", thing, upgradeFileURL, file);
                HttpUtils.download(
                        new URL(upgradeFileURL),
                        thing.getThingConnOpt().getConnectTimeoutMs(),
                        thing.getThingConnOpt().getUpgradeTimeoutMs(),
                        file,
                        this
                );
                return file;
            } catch (IOException cause) {
                throw new ModularUpgradeProcessException(
                        ModularUpgradeProcessException.STEP_DOWNLOAD_FAILURE,
                        "download failure!",
                        cause
                );
            }
        }

        private File checksum(File upgradeFile) throws ThingException {
            try {
                if (!FileUtils.md5(upgradeFile).equalsIgnoreCase(upgradeFileCHS)) {
                    throw new ModularUpgradeProcessException(
                            ModularUpgradeProcessException.STEP_CHECKSUM_FAILURE,
                            "checksum failure: not match!"
                    );
                }
                return upgradeFile;
            } catch (IOException cause) {
                throw new ModularUpgradeProcessException(
                        ModularUpgradeProcessException.STEP_CHECKSUM_FAILURE,
                        "checksum failure: compute error!",
                        cause
                );
            }
        }

        public String getModuleId() {
            return moduleId;
        }

        @Override
        public String getUpgradeVersion() {
            return version;
        }

        @Override
        public File getUpgradeFile() throws ThingException {
            if (null != thingModuleUpgradeFileRef.get()) {
                return thingModuleUpgradeFileRef.get();
            }
            synchronized (thingModuleUpgradeFileRef) {
                if (null != thingModuleUpgradeFileRef.get()) {
                    return thingModuleUpgradeFileRef.get();
                }
                final File thingModuleUpgradeFile;
                thingModuleUpgradeFileRef.set(thingModuleUpgradeFile = checksum(download()));
                return thingModuleUpgradeFile;
            }
        }

        @Override
        public void processing(int process) {
            final int current = process / 20;
            if (current > max) {
                upgradeProcessing(reqId, moduleId, process, "downloading");
                max = current;
            }
        }

    }

    /**
     * 设备升级变更提交
     */
    private class CommitImpl implements Modular.Commit {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final Modular module;

        public CommitImpl(Modular module) {
            this.module = module;
        }

        @Override
        public void commit() {
            thing.getThingOp().reportModule(module)
                    .onSuccess(future -> logger.info("{}/module/upgrade/commit success, module={};version={};",
                            thing,
                            module.getModuleId(),
                            module.getModuleVersion()
                    ))
                    .onException(future -> logger.warn("{}/module/upgrade/commit failure, module={};version={};",
                            thing,
                            module.getModuleId(),
                            module.getModuleVersion()
                    ));
        }

    }

    /**
     * 升级错误异常
     */
    private class ModularUpgradeProcessException extends ThingException {

        public static final int STEP_FINISHED = 100;
        public static final int STEP_UPGRADE_FAILURE = -1;
        public static final int STEP_DOWNLOAD_FAILURE = -2;
        public static final int STEP_CHECKSUM_FAILURE = -3;
        public static final int STEP_WRITE_FAILURE = -4;

        private final int step;
        private final String desc;

        ModularUpgradeProcessException(int step, String desc) {
            super(thing, format("thing upgrade process failure, step=%s;desc=%s", step, desc));
            this.step = step;
            this.desc = desc;
        }

        ModularUpgradeProcessException(int step, String desc, Throwable cause) {
            super(thing, format("thing upgrade process failure, step=%s;desc=%s", step, desc), cause);
            this.step = step;
            this.desc = desc;
        }

        public int getStep() {
            return step;
        }

        public String getDesc() {
            return desc;
        }
    }

}
