package com.github.ompc.athing.aliyun.platform.product;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertyDataRequest;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertyDataResponse;
import com.github.ompc.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.ompc.athing.aliyun.framework.util.GsonFactory;
import com.github.ompc.athing.aliyun.platform.AliyunThingPlatformException;
import com.github.ompc.athing.aliyun.platform.AliyunThingPlatformResponseException;
import com.github.ompc.athing.standard.component.Identifier;
import com.github.ompc.athing.standard.platform.ThingPlatformException;
import com.github.ompc.athing.standard.platform.domain.SortOrder;
import com.github.ompc.athing.standard.platform.domain.ThingPropertySnapshot;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * 单个属性快照迭代器实现
 */
class PropertySnapshotIteratorImpl implements Iterator<ThingPropertySnapshot> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonFactory.getGson();

    private final IAcsClient client;
    private final String productId;
    private final String thingId;
    private final long end;
    private final SortOrder order;
    private final int batch;
    private final ThPropertyMeta thPropertyMeta;

    private QueryDevicePropertyDataResponse rollingResponse;
    private Iterator<ThingPropertySnapshot> rollingIt;

    PropertySnapshotIteratorImpl(final IAcsClient client,
                                 final String productId,
                                 final ThProductMeta thProductMeta,
                                 final String thingId,
                                 final Identifier identifier,
                                 final long begin,
                                 final long end,
                                 final SortOrder order,
                                 final int batch) throws ThingPlatformException {
        this.client = client;
        this.productId = productId;
        this.thingId = thingId;
        this.end = end;
        this.order = order;
        this.batch = batch;
        this.thPropertyMeta = getThPropertyMeta(thProductMeta, identifier);
        rolling(begin);
    }

    private ThPropertyMeta getThPropertyMeta(ThProductMeta thProductMeta, Identifier identifier) {
        final ThPropertyMeta thPropertyMeta = thProductMeta.getThPropertyMeta(identifier);
        if (null == thPropertyMeta) {
            throw new IllegalArgumentException(
                    String.format("property: %s is not provide in %s", identifier, productId)
            );
        }
        return thPropertyMeta;
    }

    // 向前滚动
    private void rolling(long begin) throws ThingPlatformException {
        final Identifier identifier = thPropertyMeta.getIdentifier();
        final QueryDevicePropertyDataRequest request = new QueryDevicePropertyDataRequest();
        request.setProductKey(productId);
        request.setDeviceName(thingId);
        request.setAsc(order.getValue());
        request.setStartTime(begin);
        request.setEndTime(end);
        request.setPageSize(batch);
        request.setIdentifier(identifier.getIdentity());

        try {
            final QueryDevicePropertyDataResponse response = client.getAcsResponse(request);

            // 平台返回调用失败
            if (!response.getSuccess()) {
                throw new AliyunThingPlatformResponseException(
                        response.getRequestId(),
                        String.format("/%s/%s get property response failure, code=%s;message=%s;identifier=%s;",
                                productId,
                                thingId,
                                response.getCode(),
                                response.getErrorMessage(),
                                identifier
                        ));
            }

            this.rollingIt = response.getData().getList().stream()
                    .map(info -> new ThingPropertySnapshot(
                            identifier,
                            gson.fromJson(info.getValue(), thPropertyMeta.getPropertyType()),
                            Long.parseLong(info.getTime())
                    ))
                    .collect(Collectors.toList()).iterator();


            this.rollingResponse = response;

            logger.debug("thing-platform:/{}/{}/property rolling snapshot between({}->{}), property={};next={};found={};batch={};",
                    productId, thingId, begin, end, identifier, response.getData().getNextValid(), response.getData().getList().size(), batch
            );

        } catch (ClientException cause) {
            throw new AliyunThingPlatformException(
                    String.format("/%s/%s get property error, identifier=%s", productId, thingId, identifier),
                    cause
            );
        }

    }

    @Override
    public boolean hasNext() {
        if (rollingIt.hasNext()) {
            return true;
        }
        if (rollingResponse.getData().getNextValid()) {
            try {
                rolling(rollingResponse.getData().getNextTime());
                return rollingIt.hasNext();
            } catch (ThingPlatformException cause) {
                throw new IllegalStateException(cause);
            }
        }
        return false;
    }

    @Override
    public ThingPropertySnapshot next() {
        return rollingIt.next();
    }

}
