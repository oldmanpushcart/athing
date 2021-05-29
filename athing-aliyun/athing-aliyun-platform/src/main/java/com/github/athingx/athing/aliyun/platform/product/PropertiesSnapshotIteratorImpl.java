package com.github.athingx.athing.aliyun.platform.product;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertiesDataRequest;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertiesDataResponse;
import com.github.athingx.athing.aliyun.framework.component.meta.ThPropertyMeta;
import com.github.athingx.athing.aliyun.framework.util.GsonFactory;
import com.github.athingx.athing.aliyun.platform.AliyunThingPlatformException;
import com.github.athingx.athing.aliyun.platform.AliyunThingPlatformResponseException;
import com.github.athingx.athing.standard.component.Identifier;
import com.github.athingx.athing.standard.platform.ThingPlatformException;
import com.github.athingx.athing.standard.platform.domain.SortOrder;
import com.github.athingx.athing.standard.platform.domain.ThingPropertySnapshot;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class PropertiesSnapshotIteratorImpl implements Iterator<Map.Entry<Identifier, Collection<ThingPropertySnapshot>>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = GsonFactory.getGson();

    private final IAcsClient client;
    private final String productId;
    private final String thingId;
    private final long end;
    private final SortOrder order;
    private final int size;
    private final Map<Identifier, ThPropertyMeta> thPropertyMetaMap;

    private QueryDevicePropertiesDataResponse rollingResponse;
    private Iterator<Map.Entry<Identifier, Collection<ThingPropertySnapshot>>> rollingIt;

    PropertiesSnapshotIteratorImpl(IAcsClient client, String productId, ThProductMeta thProductMeta, String thingId, Set<Identifier> identifiers, long begin, long end, SortOrder order, int size) throws ThingPlatformException {
        this.client = client;
        this.productId = productId;
        this.thingId = thingId;
        this.end = end;
        this.order = order;
        this.size = size;
        this.thPropertyMetaMap = getThPropertyMetaMap(thProductMeta, identifiers);
        rolling(begin);
    }

    private Map<Identifier, ThPropertyMeta> getThPropertyMetaMap(ThProductMeta thProductMeta, Set<Identifier> identifiers) {
        final Map<Identifier, ThPropertyMeta> thPropertyMetaMap = new HashMap<>();
        for (Identifier identifier : identifiers) {
            final ThPropertyMeta thPropertyMeta = thProductMeta.getThPropertyMeta(identifier);
            if (null == thPropertyMeta) {
                throw new IllegalArgumentException(
                        String.format("property: %s is not provide in %s", identifier, productId)
                );
            }
            thPropertyMetaMap.put(identifier, thPropertyMeta);
        }

        return thPropertyMetaMap;
    }

    private void rolling(long begin) throws ThingPlatformException {
        final List<String> identities = thPropertyMetaMap.keySet().stream()
                .map(Identifier::getIdentity)
                .collect(Collectors.toList());
        final QueryDevicePropertiesDataRequest request = new QueryDevicePropertiesDataRequest();
        request.setProductKey(productId);
        request.setDeviceName(thingId);
        request.setAsc(order.getValue());
        if (order == SortOrder.ASCENDING) {
            request.setStartTime(end);
            request.setEndTime(begin);
        } else {
            request.setStartTime(begin);
            request.setEndTime(end);
        }

        request.setPageSize(size);
        request.setIdentifiers(identities);

        try {
            final QueryDevicePropertiesDataResponse response = client.getAcsResponse(request);
            // 平台返回调用失败
            if (!response.getSuccess()) {
                throw new AliyunThingPlatformResponseException(
                        response.getRequestId(),
                        String.format("/%s/%s get property response failure, code=%s;message=%s;identifier=%s;",
                                productId,
                                thingId,
                                response.getCode(),
                                response.getErrorMessage(),
                                identities
                        ));
            }

            final Map<Identifier, Collection<ThingPropertySnapshot>> propertySnapshotMap = new HashMap<>();
            for (QueryDevicePropertiesDataResponse.PropertyDataInfo dataInfo : response.getPropertyDataInfos()) {
                final Identifier identifier = Identifier.parseIdentity(dataInfo.getIdentifier());
                propertySnapshotMap.put(
                        identifier,
                        dataInfo.getList().stream()
                                .map(info -> new ThingPropertySnapshot(
                                        identifier,
                                        gson.fromJson(info.getValue(), thPropertyMetaMap.get(identifier).getPropertyType()),
                                        info.getTime()
                                ))
                                .collect(Collectors.toList())
                );
            }

            this.rollingIt = propertySnapshotMap.entrySet().iterator();
            this.rollingResponse = response;

            logger.debug("thing-platform:/{}/{}/property rolling snapshot between({}->{}), properties={};next={};found={};",
                    productId, thingId, begin, end, identities, response.getNextValid(), response.getPropertyDataInfos().size()
            );

        } catch (ClientException cause) {
            throw new AliyunThingPlatformException(
                    String.format("/%s/%s get property error, identities=%s", productId, thingId, identities),
                    cause
            );
        }
    }

    @Override
    public boolean hasNext() {
        if (rollingIt.hasNext()) {
            return true;
        }
        if (rollingResponse.getNextValid()) {
            try {
                rolling(rollingResponse.getNextTime());
                return rollingIt.hasNext();
            } catch (ThingPlatformException cause) {
                throw new IllegalStateException(cause);
            }
        }
        return false;
    }

    public boolean rollingHasNext() {
        return rollingIt.hasNext();
    }

    @Override
    public Map.Entry<Identifier, Collection<ThingPropertySnapshot>> next() {
        return rollingIt.next();
    }
}
