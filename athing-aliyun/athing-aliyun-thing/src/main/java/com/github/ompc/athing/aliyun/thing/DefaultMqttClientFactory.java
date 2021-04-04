package com.github.ompc.athing.aliyun.thing;

import com.github.ompc.athing.standard.thing.ThingException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

import static com.github.ompc.athing.aliyun.thing.util.StringUtils.bytesToHexString;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultMqttClientFactory implements MqttClientFactory {

    @Override
    public IMqttAsyncClient make(String remote, ThingAccess access, ThingConnectOption connOpt) throws ThingException {
        final Boot boot = new Boot(access);
        try {
            return new MqttAsyncClient(remote, boot.getClientId(), new MemoryPersistence()) {

                private MqttConnectOptions inject(MqttConnectOptions options) {
                    final MqttConnectOptions target = null == options
                            ? new MqttConnectOptions()
                            : options;
                    target.setUserName(boot.getUsername());
                    target.setPassword(boot.getPassword());
                    target.setCleanSession(true);
                    target.setConnectionTimeout((int) (connOpt.getConnectTimeoutMs() / 1000));
                    target.setKeepAliveInterval((int) (connOpt.getKeepAliveIntervalMs() / 1000));
                    target.setAutomaticReconnect(false);
                    return target;
                }

                @Override
                public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback) throws MqttException {
                    return super.connect(inject(options), userContext, callback);
                }

            };
        } catch (MqttException cause) {
            throw new ThingException(access.getProductId(), access.getThingId(), "make mqtt client error!", cause);
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
