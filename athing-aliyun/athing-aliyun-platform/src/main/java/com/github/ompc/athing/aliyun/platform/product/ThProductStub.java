package com.github.ompc.athing.aliyun.platform.product;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.InvokeThingServiceRequest;
import com.aliyuncs.iot.model.v20180120.InvokeThingServiceResponse;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyRequest;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyResponse;
import com.github.ompc.athing.aliyun.framework.component.meta.ThParamMeta;
import com.github.ompc.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.ompc.athing.aliyun.framework.component.meta.ThServiceMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.framework.util.MapObject;
import com.github.ompc.athing.aliyun.platform.AliyunThingPlatformException;
import com.github.ompc.athing.aliyun.platform.AliyunThingPlatformResponseException;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.platform.ThingPlatformException;
import com.github.ompc.athing.standard.platform.domain.SortOrder;
import com.github.ompc.athing.standard.platform.domain.ThingPropertySnapshot;
import com.github.ompc.athing.standard.platform.util.TpRuntime;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 设备产品存根
 */
public class ThProductStub {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonFactory.getGson();

    private final IAcsClient client;
    private final String productId;
    private final ThProductMeta thProductMeta;

    /**
     * 设备产品存根
     *
     * @param client        ACS客户端
     * @param productId     产品ID
     * @param thProductMeta 产品元数据
     */
    public ThProductStub(IAcsClient client, String productId, ThProductMeta thProductMeta) {
        this.client = client;
        this.productId = productId;
        this.thProductMeta = thProductMeta;
    }

    /**
     * 获取产品ID
     *
     * @return 产品ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 获取产品元数据
     *
     * @return 产品元数据
     */
    public ThProductMeta getThProductMeta() {
        return thProductMeta;
    }

    // 生成服务调用参数
    private String generateServiceArguments(ThServiceMeta thServiceMeta, Object[] arguments) {
        return gson.toJson(
                Stream.of(thServiceMeta.getThParamMetas())
                        .collect(Collectors.toMap(
                                ThParamMeta::getName,
                                meta -> arguments[meta.getIndex()],
                                (a, b) -> b
                        )));
    }

    /**
     * 服务调用
     *
     * @param thingId    设备ID
     * @param identifier 服务ID
     * @param arguments  服务参数
     * @return 服务返回
     * @throws ThingPlatformException 服务调用失败
     */
    public Object service(String thingId, Identifier identifier, Object[] arguments) throws ThingPlatformException {

        final ThServiceMeta thServiceMeta = thProductMeta.getThServiceMeta(identifier);
        if (null == thServiceMeta) {
            throw new IllegalArgumentException(
                    String.format("service: %s is not provide in %s", identifier, productId)
            );
        }

        final String identity = thServiceMeta.getIdentifier().getIdentity();

        // 初始化参数
        final InvokeThingServiceRequest request = new InvokeThingServiceRequest();
        request.setProductKey(productId);
        request.setDeviceName(thingId);
        request.setIdentifier(identity);
        request.setArgs(generateServiceArguments(thServiceMeta, arguments));

        try {

            // 执行调用
            final InvokeThingServiceResponse response = client.getAcsResponse(request);

            // 平台返回调用失败
            if (!response.getSuccess()) {
                throw new AliyunThingPlatformResponseException(
                        response.getRequestId(),
                        String.format("/%s/%s invoke service: %s response failure, code=%s;message=%s;",
                                productId,
                                thingId,
                                identity,
                                response.getCode(),
                                response.getErrorMessage()
                        ));
            }

            // 返回结果
            final String reqId = response.getData().getMessageId();
            final Object result = gson.fromJson(response.getData().getResult(), thServiceMeta.getReturnType());
            logger.info("thing-platform:/{}/{}/service invoke success, req={};identity={};",
                    productId, thingId, reqId, identity);

            // 如果处于运行时环境，则对运行时环境进行赋值
            if (TpRuntime.isInRuntime()) {
                TpRuntime.getRuntime().setReqId(reqId);
            }

            return result;
        } catch (ClientException cause) {
            throw new AliyunThingPlatformException(
                    String.format("/%s/%s service: %s invoke error!", productId, thingId, identity),
                    cause
            );
        }

    }

    // 生成属性设置参数
    private String generateSetPropertyArguments(Map<Identifier, Object> propertyValueMap) {
        final MapObject parameterObjectMap = new MapObject();
        propertyValueMap.forEach((identifier, value) -> {
            final ThPropertyMeta thPropertyMeta = thProductMeta.getThPropertyMeta(identifier);

            // 检查设备属性是否存在
            if (null == thPropertyMeta) {
                throw new IllegalArgumentException(
                        String.format("property: %s is not provide in %s", identifier, productId)
                );
            }

            // 检查属性是否只读
            if (thPropertyMeta.isReadonly()) {
                throw new UnsupportedOperationException(
                        String.format("property: %s is readonly!", identifier)
                );
            }

            parameterObjectMap.putProperty(identifier.getIdentity(), value);
        });
        return gson.toJson(parameterObjectMap);
    }

    /**
     * 设置属性值
     *
     * @param thingId          设备ID
     * @param propertyValueMap 属性值集合
     * @throws ThingPlatformException 设置属性失败
     */
    public void setPropertyValue(String thingId, Map<Identifier, Object> propertyValueMap) throws ThingPlatformException {

        // 初始化参数
        final SetDevicePropertyRequest request = new SetDevicePropertyRequest();
        request.setProductKey(productId);
        request.setDeviceName(thingId);
        request.setItems(generateSetPropertyArguments(propertyValueMap));

        // 属性ID集合
        final Set<Identifier> propertyIds = propertyValueMap.keySet();

        try {

            // 执行设置
            final SetDevicePropertyResponse response = client.getAcsResponse(request);

            // 平台返回调用失败
            if (!response.getSuccess()) {
                throw new AliyunThingPlatformResponseException(
                        response.getRequestId(),
                        String.format("/%s/%s set property response failure, code=%s;message=%s;identities=%s;",
                                productId,
                                thingId,
                                response.getCode(),
                                response.getErrorMessage(),
                                propertyIds
                        ));
            }

            final String reqId = response.getData().getMessageId();
            logger.info("thing-platform:/{}/{}/property set finished, waiting for reply. req={};identity={};",
                    productId, thingId, reqId, propertyIds);

            // 如果处于运行时环境，则对运行时环境进行赋值
            if (TpRuntime.isInRuntime()) {
                TpRuntime.getRuntime().setReqId(reqId);
            }

        } catch (ClientException cause) {
            throw new AliyunThingPlatformException(
                    String.format("/%s/%s set property error, identities=%s", productId, thingId, propertyIds),
                    cause
            );
        }
    }

    /**
     * 设置属性值
     *
     * @param thingId    设备ID
     * @param identifier 属性ID
     * @param value      属性值
     * @throws ThingPlatformException 设置属性值失败
     */
    public void setPropertyValue(String thingId, Identifier identifier, Object value) throws ThingPlatformException {
        setPropertyValue(
                thingId,
                new HashMap<Identifier, Object>() {{
                    put(identifier, value);
                }}
        );
    }


    /**
     * 阿里云数据快照存储最大持续时间（毫秒）
     * <p>
     * 阿里云存储数据快照是有时间限制的，这个限制默认是30天。
     * 如果觉得30天不够，其实需要自己接收属性上报的事件，存储到自己的数据库中查询
     * </p>
     */
    private static final long SNAPSHOT_DURATION_MS = 30 * 24 * 3600 * 1000L;

    /**
     * 获取属性快照集合
     *
     * @param thingId     设备ID
     * @param identifiers 属性标识集合
     * @return 属性快照集合
     * @throws ThingPlatformException 查询属性快照失败
     */
    public Map<Identifier, ThingPropertySnapshot> getPropertySnapshotMap(String thingId, Set<Identifier> identifiers) throws ThingPlatformException {

        final long end = System.currentTimeMillis();
        final long begin = end - SNAPSHOT_DURATION_MS;

        final PropertiesSnapshotIteratorImpl propertiesSnapshotIt
                = new PropertiesSnapshotIteratorImpl(
                client, productId, thProductMeta, thingId, identifiers, begin, end, SortOrder.ASCENDING, 1
        );

        final Map<Identifier, ThingPropertySnapshot> propertySnapshotMap = new HashMap<>();
        while (propertiesSnapshotIt.rollingHasNext()) {
            final Map.Entry<Identifier, Collection<ThingPropertySnapshot>> entry = propertiesSnapshotIt.next();
            final Collection<ThingPropertySnapshot> thingPropertySnapshots = entry.getValue();
            if (null != thingPropertySnapshots && !thingPropertySnapshots.isEmpty()) {
                propertySnapshotMap.put(
                        entry.getKey(),
                        thingPropertySnapshots.iterator().next()
                );
            }
        }
        return propertySnapshotMap;
    }

    /**
     * 获取属性最新快照
     *
     * @param thingId    设备ID
     * @param identifier 属性标识
     * @return 属性最新快照值
     * @throws ThingPlatformException 获取属性快照失败
     */
    public ThingPropertySnapshot getPropertySnapshot(String thingId, Identifier identifier) throws ThingPlatformException {
        final long end = System.currentTimeMillis();
        final long begin = end - SNAPSHOT_DURATION_MS;

        final Iterator<ThingPropertySnapshot> propertySnapshotIt = new PropertySnapshotIteratorImpl(
                client, productId, thProductMeta, thingId, identifier, begin, end, SortOrder.ASCENDING, 1
        );

        return propertySnapshotIt.hasNext()
                ? propertySnapshotIt.next()
                : null;

    }

    /**
     * 迭代查询属性快照
     *
     * @param thingId    设备ID
     * @param identifier 属性标识
     * @param batch      批次数量
     * @param order      排序顺序
     * @return 属性快照迭代器
     * @throws ThingPlatformException 操作失败
     */
    public Iterator<ThingPropertySnapshot> iteratorForPropertySnapshot(String thingId, Identifier identifier, int batch, SortOrder order) throws ThingPlatformException {
        final long end = System.currentTimeMillis();
        final long begin = end - SNAPSHOT_DURATION_MS;
        return new PropertySnapshotIteratorImpl(
                client, productId, thProductMeta, thingId, identifier, begin, end, order, batch
        );
    }

}
