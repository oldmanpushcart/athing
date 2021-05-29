package com.github.athingx.athing.aliyun.platform;

import com.github.athingx.athing.aliyun.platform.message.ThingJmsMessageListenerImpl;
import com.github.athingx.athing.aliyun.platform.message.ThingMessageDecoder;
import com.github.athingx.athing.aliyun.platform.product.ThProductMeta;
import com.github.athingx.athing.standard.platform.ThingPlatformException;
import com.github.athingx.athing.standard.platform.message.ThingMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Closeable;
import java.lang.IllegalStateException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备产品消息消费者
 */
class ThingMessageConsumer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ThingMessageConsumer.class);
    private final Connection connection;
    private final String name;
    private final String _string;

    private ThingMessageConsumer(final String name,
                                 final Context context,
                                 final Connection connection,
                                 final Map<String, ThProductMeta> thProductMetaMap,
                                 final Set<ThingMessageDecoder> decoders,
                                 final ThingMessageListener listener) throws JMSException, NamingException {
        this.name = name;
        this._string = String.format("TMC:%s", name);
        this.connection = connection;
        final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination queue = (Destination) context.lookup("QUEUE");
        final MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new ThingJmsMessageListenerImpl(thProductMetaMap, decoders, listener) {

            private final String _string = String.format("%s/listener", ThingMessageConsumer.this._string);

            @Override
            public String toString() {
                return _string;
            }
        });
        connection.start();
    }

    // 创建JmsContext
    private static Context createContext(final String connectionUrl,
                                         final String group) throws NamingException {
        // 初始化Context
        final Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("connectionfactory.SBCF", connectionUrl);
        hashtable.put("queue.QUEUE", group);
        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        return new InitialContext(hashtable);
    }

    // 计算并获取账号
    private static String getUsername(final ThingPlatformAccess access,
                                      final long timestamp,
                                      final String uniqueId,
                                      final String group) {
        return String.format("%s|authMode=aksign,signMethod=hmacsha1,timestamp=%s,authId=%s,consumerGroupId=%s|",
                uniqueId,
                timestamp,
                access.getIdentity(),
                group
        );
    }

    // 计算并获取密码
    private static String getPassword(final ThingPlatformAccess access,
                                      final long timestamp) {
        final String content = String.format("authId=%s&timestamp=%s", access.getIdentity(), timestamp);
        try {
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(access.getSecret().getBytes(UTF_8), mac.getAlgorithm()));
            return Base64.getEncoder().encodeToString(mac.doFinal(content.getBytes(UTF_8)));
        } catch (Exception cause) {
            throw new IllegalStateException(cause);
        }
    }

    /**
     * 构建消息消费者
     *
     * @param access        消息服务器Access
     * @param connectionUrl 消息服务器URL
     * @param group         消息组
     * @param listener      消息监听器
     * @return 消息消费者
     * @throws ThingPlatformException 构建失败
     */
    public static ThingMessageConsumer createThingMessageConsumer(final ThingPlatformAccess access,
                                                                  final String connectionUrl,
                                                                  final String group,
                                                                  final Map<String, ThProductMeta> productMetaMap,
                                                                  final Set<ThingMessageDecoder> decoders,
                                                                  final ThingMessageListener listener) throws ThingPlatformException {

        final String uniqueId = UUID.randomUUID().toString();
        final long timestamp = System.currentTimeMillis();

        try {
            final Context context = createContext(connectionUrl, group);
            final Connection connection = ((ConnectionFactory) context.lookup("SBCF")).createConnection(
                    getUsername(access, timestamp, uniqueId, group),
                    getPassword(access, timestamp)
            );
            return new ThingMessageConsumer(String.format("/%s", group), context, connection, productMetaMap, decoders, listener);
        } catch (NamingException | JMSException cause) {
            throw new AliyunThingPlatformException(
                    String.format("thing-message create consumer failure, group=%s;url=%s;", group, connectionUrl),
                    cause
            );
        }

    }

    @Override
    public String toString() {
        return _string;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (JMSException cause) {
            logger.warn("{} close failure,", this, cause);
        }
    }

}
