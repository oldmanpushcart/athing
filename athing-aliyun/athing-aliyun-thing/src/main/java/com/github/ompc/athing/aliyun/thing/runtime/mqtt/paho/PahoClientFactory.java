package com.github.ompc.athing.aliyun.thing.runtime.mqtt.paho;

import com.github.ompc.athing.aliyun.thing.ThingBootOption;
import com.github.ompc.athing.aliyun.thing.runtime.access.ThingAccess;
import com.github.ompc.athing.standard.thing.ThingException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.UUID;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Paho客户端工厂
 */
public class PahoClientFactory {

    /**
     * 生产Paho客户端
     *
     * @param remote 服务器地址
     * @param access 服务器
     * @param option 启动参数
     * @return Paho客户端
     * @throws ThingException 生产失败
     */
    public IMqttAsyncClient make(URI remote, ThingAccess access, ThingBootOption option) throws ThingException {
        final Boot boot = new Boot(access);
        try {
            return new MqttAsyncClient(remote.toString(), boot.getClientId(), new MemoryPersistence()) {

                private MqttConnectOptions inject(MqttConnectOptions options) {
                    final MqttConnectOptions target = null == options
                            ? new MqttConnectOptions()
                            : options;
                    target.setUserName(boot.getUsername());
                    target.setPassword(boot.getPassword());
                    target.setCleanSession(true);
                    target.setConnectionTimeout((int) (option.getConnectTimeoutMs() / 1000));
                    target.setKeepAliveInterval((int) (option.getKeepAliveIntervalMs() / 1000));
                    target.setAutomaticReconnect(false);
                    return target;
                }

                @Override
                public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback) throws MqttException {
                    return super.connect(inject(options), userContext, callback);
                }

            };
        } catch (MqttException cause) {
            throw new ThingException(access.getProductId(), access.getThingId(), "make paho client error!", cause);
        }
    }

    /**
     * 启动信息
     */
    private static class Boot {

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();
        final ThingAccess access;

        /**
         * 构建启动信息
         *
         * @param access 设备密钥
         */
        Boot(ThingAccess access) {
            this.access = access;
        }

        /**
         * 获取MQTT帐号
         *
         * @return MQTT帐号
         */
        String getUsername() {
            return String.format("%s&%s", access.getThingId(), access.getProductId());
        }

        /**
         * 获取MQTT密码
         *
         * @return MQTT密码
         */
        char[] getPassword() {
            final String content = String.format("clientId%sdeviceName%sproductKey%stimestamp%s",
                    uniqueId,
                    access.getThingId(),
                    access.getProductId(),
                    timestamp
            );
            try {
                final Mac mac = Mac.getInstance("HMACSHA1");
                mac.init(new SecretKeySpec(access.getSecret().getBytes(UTF_8), mac.getAlgorithm()));
                return bytesToHexString(mac.doFinal(content.getBytes(UTF_8))).toCharArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 获取MQTT客户端ID
         *
         * @return 客户端ID
         */
        String getClientId() {
            return String.format("%s|securemode=3,signmethod=hmacsha1,timestamp=%s,ext=1|", uniqueId, timestamp);
        }

    }

}
