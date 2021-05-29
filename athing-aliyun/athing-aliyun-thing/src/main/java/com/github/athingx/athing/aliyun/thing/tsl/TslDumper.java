package com.github.athingx.athing.aliyun.thing.tsl;

import com.github.athingx.athing.aliyun.framework.component.meta.*;
import com.github.athingx.athing.aliyun.thing.tsl.schema.*;
import com.github.athingx.athing.aliyun.thing.tsl.specs.*;
import com.github.athingx.athing.aliyun.thing.tsl.validator.TslDataValidator;
import com.github.ompc.athing.aliyun.thing.tsl.schema.*;
import com.github.ompc.athing.aliyun.thing.tsl.specs.*;
import com.github.athingx.athing.standard.component.Identifier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.github.athingx.athing.aliyun.framework.component.ThComMetaHelper.dumpToLowerCaseUnderscore;
import static com.github.athingx.athing.aliyun.framework.util.CommonUtils.isIn;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * TSL(Thing Specification Language)
 * <a href="https://www.alibabacloud.com/help/zh/doc-detail/73727.htm">物模型</a>
 */
public class TslDumper {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(TslDataType.Type.class, (JsonSerializer<TslDataType.Type>) (src, typeOfSrc, context) -> context.serialize(src.getType()))
            .registerTypeAdapter(TslServiceElement.CallType.class, (JsonSerializer<TslServiceElement.CallType>) (src, typeOfSrc, context) -> context.serialize(src.getType()))
            .registerTypeAdapter(TslEventElement.EventType.class, (JsonSerializer<TslEventElement.EventType>) (src, typeOfSrc, context) -> context.serialize(src.getValue()))
            .setPrettyPrinting()
            .create();
    private final String productId;

    // ---------- 以下为具体dump逻辑实现 ----------
    private final Collection<ThComMeta> thComMetas;

    private TslDumper(String productId, ThComMeta... thComMetas) {
        this.productId = productId;
        this.thComMetas = Stream.of(thComMetas).collect(Collectors.toList());
    }

    /**
     * dump TSL from ThingCom interface
     *
     * @param dumpToJson         dump回调
     * @param productId          产品ID
     * @param thingComInterfaces 组件接口
     */
    public static void dump(DumpToJson dumpToJson, String productId, Class<?>... thingComInterfaces) {
        new TslDumper(
                productId,
                Stream.of(thingComInterfaces)
                        .map(ThComMetaFactory::make)
                        .toArray(ThComMeta[]::new)
        ).dump(dumpToJson);
    }

    /**
     * dump class struct
     *
     * @param clazz target class
     * @return Tsl-data[]
     */
    private Collection<TslData> dumpClass(Class<?> clazz) {
        final Collection<TslData> data = new LinkedList<>();

        // java.lang.Object不需要处理
        if (clazz == Object.class) {
            return data;
        }

        // void需要特殊处理
        if (isIn(clazz, void.class, Void.class)) {
            return data;
        }

        // 原生的8个基础类型，则无法支持展开
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("not support: primitive");
        }

        // 数组不支持展开
        if (clazz.isArray()) {
            throw new IllegalArgumentException("not support: array");
        }

        // 枚举不支持展开
        if (clazz.isEnum()) {
            throw new IllegalArgumentException("not support: enum");
        }

        // 接口不支持展开
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("not support: interface");
        }

        // 展开本类
        Stream.of(clazz.getDeclaredFields())

                // 过滤掉transient
                .filter(field -> !Modifier.isTransient(field.getModifiers()))

                // 过滤静态常量
                .filter(field -> !(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())))

                // 遍历属性
                .forEach(field -> data.add(
                        new TslData(
                                dumpToLowerCaseUnderscore(field.getName()),
                                convertTypeToDataType(field.getType())))
                );

        // 递归展开父类
        data.addAll(dumpClass(clazz.getSuperclass()));

        return data;
    }

    /**
     * 转换Java类型到Tsl数据类型
     *
     * @param clazz 目标Java类型
     * @return 转换后的Tsl数据类型
     */
    private TslDataType convertTypeToDataType(Class<?> clazz) {
        final TslDataType type;

        // int
        if (isIn(clazz, int.class, Integer.class)) {
            type = new TslDataType(new IntSpecs());
        }

        // byte
        else if (isIn(clazz, byte.class, Byte.class)) {
            type = new TslDataType(new IntSpecs());
        }

        // short
        else if (isIn(clazz, short.class, Short.class)) {
            type = new TslDataType(new IntSpecs());
        }

        /* long
         * 阿里云物模型不支持long类型的数据，需要转成text，再在使用时转回来
         */
        else if (isIn(clazz, long.class, Long.class)) {
            type = new TslDataType(new TextSpecs());
        }

        // float
        else if (isIn(clazz, float.class, Float.class)) {
            type = new TslDataType(new FloatSpecs());
        }

        /*
         * double
         */
        else if (isIn(clazz, double.class, Double.class)) {
            type = new TslDataType(new DoubleSpecs());
        }

        // bool
        else if (isIn(clazz, boolean.class, Boolean.class)) {
            type = new TslDataType(new BoolSpecs());
        }

        // char
        else if (isIn(clazz, char.class, Character.class)) {
            type = new TslDataType(new TextSpecs(1));
        }

        // text
        else if (isIn(clazz, String.class)) {
            type = new TslDataType(new TextSpecs());
        }

        // date
        else if (isIn(clazz, Date.class)) {
            type = new TslDataType(new DateSpecs());
        }

        /*
         * enum
         * 阿里云物模型对枚举类必须有一个int类型的数据与具体的值对应
         * 这里采用枚举类的位置顺序，会存在一定的风险
         */
        else if (clazz.isEnum()) {
            final EnumSpecs specs = new EnumSpecs();
            for (final Enum<?> e : (Enum<?>[]) clazz.getEnumConstants()) {
                specs.put(e.ordinal(), e.name());
            }
            type = new TslDataType(specs);
        }

        // array
        else if (clazz.isArray()) {
            type = new TslDataType(new ArraySpecs(convertTypeToDataType(clazz.getComponentType())));
        }

        // struct
        else {
            type = new TslDataType(new StructSpecs(dumpClass(clazz)));
        }

        return type;
    }


    /**
     * meta to element : service
     *
     * @param meta service meta
     * @return service element
     */
    private TslServiceElement convert(ThServiceMeta meta) {

        final Identifier identifier = meta.getIdentifier();
        final TslServiceElement element = new TslServiceElement(
                identifier.getMemberId(),
                meta.isSync()
                        ? TslServiceElement.CallType.SYNC
                        : TslServiceElement.CallType.ASYNC
        );
        element.setRequired(meta.isRequired());
        element.setDesc(meta.getDesc());
        element.setName(meta.getName());

        // 转换方法返回类型
        try {
            element.getOutputData().addAll(dumpClass(meta.getReturnType()));
        } catch (Exception cause) {
            throw new TslException(
                    String.format("convert service: \"%s\" return type error!", identifier),
                    cause
            );
        }

        // 转换方法服务参数
        Stream.of(meta.getThParamMetas()).forEach(thParamMeta -> {
            try {
                element.getInputData().add(new TslData(
                        thParamMeta.getName(),
                        convertTypeToDataType(thParamMeta.getType())
                ));
            } catch (Exception cause) {
                throw new TslException(
                        String.format("convert service: \"%s\" parameter: \"%s\" at index: [%s] error!",
                                identifier,
                                thParamMeta.getName(),
                                thParamMeta.getType()
                        ),
                        cause
                );
            }
        });

        return element;
    }

    /**
     * meta to element : event
     *
     * @param meta event meta
     * @return event element
     */
    private TslEventElement convert(ThEventMeta meta) {
        final Identifier identifier = meta.getIdentifier();
        final TslEventElement.EventType type;
        switch (meta.getLevel()) {
            case WARN: {
                type = TslEventElement.EventType.WARN;
                break;
            }
            case ERROR: {
                type = TslEventElement.EventType.ERROR;
                break;
            }
            default: {
                type = TslEventElement.EventType.INFO;
                break;
            }
        }
        final TslEventElement element = new TslEventElement(identifier.getMemberId(), type);
        element.setDesc(meta.getDesc());
        element.setName(meta.getName());
        element.setRequired(true);

        try {
            element.getOutputData().addAll(dumpClass(meta.getType()));
        } catch (Exception cause) {
            throw new TslException(
                    String.format("convert event: \"%s\" type error!", meta.getIdentifier()),
                    cause
            );
        }

        return element;
    }

    /**
     * meta to element : property
     *
     * @param meta property meta
     * @return property element
     */
    private TslPropertyElement convert(ThPropertyMeta meta) {
        final Identifier identifier = meta.getIdentifier();
        final TslDataType dataType;
        try {
            dataType = convertTypeToDataType(meta.getPropertyType());
        } catch (Exception cause) {
            throw new TslException(
                    String.format("convert property: \"%s\" type error!", identifier),
                    cause
            );
        }

        final TslPropertyElement element = new TslPropertyElement(
                identifier.getMemberId(),
                meta.isReadonly(),
                dataType
        );
        element.setDesc(meta.getDesc());
        element.setRequired(meta.isRequired());
        element.setName(meta.getName());
        return element;
    }

    /**
     * dump to Tsl json
     */
    private void dump(DumpToJson dumpToJson) {

        final TslMainSchema mainSchema = new TslMainSchema(productId);

        // com
        thComMetas.forEach(componentMeta -> {

            final TslComSchema schema = mainSchema.newComSchema(componentMeta);

            // service
            componentMeta.getIdentityThServiceMetaMap().forEach((identifier, serviceMeta) ->
                    schema.getServices().add(convert(serviceMeta)));

            // property
            componentMeta.getIdentityThPropertyMetaMap().forEach((identifier, propertyMeta) ->
                    schema.getProperties().add(convert(propertyMeta)));

            // event
            componentMeta.getIdentityThEventMetaMap().forEach((identifier, eventMeta) ->
                    schema.getEvents().add(convert(eventMeta)));

            TslDataValidator.validates(schema);
            dumpToJson.dump(schema.getComponentId(), gson.toJson(schema));

        });

        TslDataValidator.validates(mainSchema);
        dumpToJson.dump("default", gson.toJson(mainSchema));
    }

    /**
     * dump to json string
     */
    public interface DumpToJson {

        void dump(String componentId, String json);

    }

    /**
     * dump to zip file
     */
    public static class DumpToZipFile implements DumpToJson {

        private final Map<String, String> dumpMap = new LinkedHashMap<>();

        @Override
        public void dump(String componentId, String json) {
            dumpMap.put(componentId, json);
        }

        public void toZipFile(final File target) throws IOException {
            try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target))) {
                for (final Map.Entry<String, String> entry : dumpMap.entrySet()) {
                    zos.putNextEntry(new ZipEntry("/" + entry.getKey() + ".json"));
                    zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                }
                zos.finish();
            }
        }

    }

}
