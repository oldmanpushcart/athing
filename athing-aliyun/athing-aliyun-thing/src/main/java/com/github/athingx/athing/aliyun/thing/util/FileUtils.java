package com.github.athingx.athing.aliyun.thing.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 获取一个文件的MD5值
     *
     * @return MD5
     */
    public static String md5(File file) throws IOException {
        final byte[] buffer = new byte[8192];
        try (final FileInputStream input = new FileInputStream(file)) {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            int size;
            while ((size = input.read(buffer)) != -1) {
                digest.update(buffer, 0, size);
            }
            return StringUtils.bytesToHexString(digest.digest());
        } catch (Exception cause) {
            throw new IOException(String.format("compute MD5 error, %s", file), cause);
        }
    }

    /**
     * 复制文件
     *
     * @param src  源文件
     * @param dest 目标文件
     * @throws IOException 复制文件失败
     */
    public static void copyFile(File src, File dest) throws IOException {

        try (final FileInputStream fis = new FileInputStream(src);
             final FileOutputStream fos = new FileOutputStream(dest)) {

            final byte[] data = new byte[8192];
            int size;

            while ((size = fis.read(data, 0, data.length)) != -1) {
                fos.write(data, 0, size);
            }
            fos.flush();

        }

    }

    /**
     * 安静的删除一个文件
     *
     * @param target 目标文件
     * @return TRUE | FALSE
     */
    public static boolean deleteQuietly(File target) {
        if (null == target || !target.canWrite() || !target.isFile()) {
            return false;
        }
        if (!target.exists()) {
            return true;
        }
        return target.delete();
    }

}
