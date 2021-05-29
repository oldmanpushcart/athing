package com.github.athingx.athing.aliyun.thing.container.loader;

import com.github.athingx.athing.aliyun.thing.util.FileUtils;
import com.github.athingx.athing.standard.thing.boot.spi.ThingComJarUnLoadSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.ServiceLoader;

/**
 * 组件库文件ClassLoader
 */
public class ThingComJarClassLoader extends URLClassLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final File tempComJarFile;
    private final String _string;

    /**
     * 组件库文件ClassLoader
     *
     * @param comJarFile 组件库文件
     * @param parent     父加载器
     * @throws IOException 加载失败
     */
    public ThingComJarClassLoader(File comJarFile, ClassLoader parent) throws IOException {
        this(comJarFile, FileUtils.md5(comJarFile), parent);
    }

    private ThingComJarClassLoader(File comJarFile, String md5, ClassLoader parent) throws IOException {
        this(md5, copyToTempFile(comJarFile, md5), parent);
    }

    private ThingComJarClassLoader(String md5, File tempComJarFile, ClassLoader parent) throws MalformedURLException {
        super(new URL[]{new URL("file:" + tempComJarFile.getPath())}, parent);
        this.tempComJarFile = tempComJarFile;
        this._string = String.format("ThingComJarClassLoader[%s]$%s", md5, tempComJarFile.getAbsolutePath());
        logger.debug("{} is opened!", this);
    }

    private static File copyToTempFile(final File moduleJarFile, final String md5) throws IOException {
        final File tempFile = File.createTempFile(
                String.format("athing_component_jar_%s_", md5),
                ".jar"
        );
        tempFile.deleteOnExit();
        FileUtils.copyFile(moduleJarFile, tempFile);
        return tempFile;
    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public URL getResource(String name) {
        final URL url = findResource(name);
        if (null != url) {
            return url;
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final Enumeration<URL> urls = findResources(name);
        if (null != urls) {
            return urls;
        }
        return super.getResources(name);
    }


    @Override
    protected Class<?> loadClass(final String javaClassName, final boolean resolve) throws ClassNotFoundException {

        // STANDARD的类由parent提供
        if (javaClassName.startsWith("com.github.athingx.athing.standard.")) {
            return super.loadClass(javaClassName, resolve);
        }

        // 先检查本ClassLoader是否已加载过
        final Class<?> loadedClass = findLoadedClass(javaClassName);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 没加载过则开始加载
        try {
            final Class<?> aClass = findClass(javaClassName);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        }

        // 本ClassLoader没有命中，委托父ClassLoader加载
        catch (Exception cause) {
            return super.loadClass(javaClassName, resolve);
        }

    }

    private void onJarUnLoadCompleted() {
        try {
            ServiceLoader.load(ThingComJarUnLoadSpi.class, this)
                    .forEach(ThingComJarUnLoadSpi::onJarUnLoadCompleted);
        } catch (Throwable cause) {
            logger.warn("unloading thing-com-jar occur error! loader={};", this, cause);
        }
    }

    @Override
    public void close() {
        onJarUnLoadCompleted();
        try {
            super.close();
            logger.debug("{} is closed.", this);
        } catch (IOException cause) {
            logger.warn("{} close occur error!", this, cause);
        } finally {
            FileUtils.deleteQuietly(tempComJarFile);
        }

    }
}
