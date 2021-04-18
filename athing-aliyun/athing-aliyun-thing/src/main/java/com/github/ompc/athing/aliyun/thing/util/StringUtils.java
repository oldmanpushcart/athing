package com.github.ompc.athing.aliyun.thing.util;

import com.github.ompc.athing.standard.thing.Thing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.System.currentTimeMillis;

public class StringUtils {

    /**
     * 生成SEQUENCE-ID（内容为数字的字符串），确保在设备维度唯一
     *
     * @return ID序列
     */
    public static String generateToken() {

        // 时间戳做种子
        final String seed = String.valueOf(currentTimeMillis());

        // 确保7天内不会重复，7*24*3600=604800(秒)，所以只要取种子的后9位数字即可
        return seed.substring(seed.length() - 7);
    }

    /**
     * 字节数组转16进制字符串
     *
     * @param bArray 目标字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(final byte[] bArray) {
        final StringBuilder sb = new StringBuilder(bArray.length * 2);
        for (byte b : bArray)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

    /**
     * 获取字符串的SHA256签名
     *
     * @param string 目标字符串
     * @return SHA256签名
     */
    public static String signBySHA256(String string) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes(StandardCharsets.UTF_8));
            return bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static String path(Thing thing) {
        return String.format("%s/%s", thing.getProductId(), thing.getThingId());
    }

}
