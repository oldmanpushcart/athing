package com.github.athingx.athing.aliyun.framework.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * Gson工厂
 */
public class GsonFactory {

    /**
     * Alink协议：{@link Date}采用{@code long}型表示，取值为时间戳
     */
    private static final TypeAdapter<Date> dateTypeAdapterForAliyun = new TypeAdapter<Date>() {
        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            out.value(value.getTime());
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            return new Date(in.nextLong());
        }
    };

    /**
     * Alink协议：{@link Boolean}和{@code boolean}采用{@code int}型表示，取值为0和1
     */
    private static final TypeAdapter<Boolean> booleanTypeAdapterForAliyun = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value ? 1 : 0);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            return in.nextInt() != 0;
        }
    };

    /**
     * Alink协议：{@link Enum}采用{@code int}型表示，默认采用枚举常量在枚举定义中的顺序(ordinal)
     */
    private static final JsonSerializer<Enum<?>> enumJsonSerializer = (src, typeOfSrc, context) -> context.serialize(src.ordinal());

    /**
     * Alink协议：{@link Enum}采用{@code int}型表示，默认采用枚举常量在枚举定义中的顺序(ordinal)
     */
    private static final JsonDeserializer<Enum<?>> enumJsonDeserializer = (json, typeOfT, context) -> {
        final int ordinal = json.getAsInt();
        final Enum<?>[] enumConstants = (Enum<?>[]) ((Class<?>) typeOfT).getEnumConstants();
        if (null == enumConstants) {
            return null;
        }
        return Arrays.stream(enumConstants)
                .filter(enumObj -> enumObj.ordinal() == ordinal)
                .findFirst()
                .orElse(null);
    };

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, dateTypeAdapterForAliyun)
            .registerTypeAdapter(Boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeAdapter(boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeHierarchyAdapter(Enum.class, enumJsonSerializer)
            .registerTypeHierarchyAdapter(Enum.class, enumJsonDeserializer)
            .serializeSpecialFloatingPointValues()

            // Alink协议：{@link Long}和{@code long}采用{@code text}型的数字表示
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            // .setPrettyPrinting()

            .create();
    /**
     * 空对象
     */
    private static final Object empty = new Object();

    /**
     * 获取Gson
     *
     * @return gson
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * 如果目标对象是null，为了保证aliyun平台成功必须转成空对象
     *
     * @param object 目标对象
     * @return 对象
     */
    public static Object getEmptyIfNull(Object object) {
        return null == object
                ? empty
                : object;
    }


}
