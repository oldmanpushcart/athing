package com.github.athingx.athing.aliyun.qatest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 加载配置文件
 */
public class PropertySupport {

    private final Properties properties = new Properties();

    public PropertySupport(File file) throws IOException {
        loading(file);
    }

    private void loading(File file) throws IOException {
        try (final FileReader reader = new FileReader(file)) {
            properties.load(reader);
        }
    }

}
