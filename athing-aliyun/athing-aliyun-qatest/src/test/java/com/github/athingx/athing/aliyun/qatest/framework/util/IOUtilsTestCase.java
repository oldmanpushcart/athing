package com.github.athingx.athing.aliyun.qatest.framework.util;

import com.github.athingx.athing.aliyun.framework.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

public class IOUtilsTestCase {

    private static class Resource implements Closeable {

        private boolean close = false;

        public boolean isClose() {
            return close;
        }

        @Override
        public void close() throws IOException {
            this.close = true;
        }
    }

    @Test
    public void test$closeQuietly() {

        final Resource res = new Resource();
        IOUtils.closeQuietly(res);
        Assert.assertTrue(res.isClose());

        IOUtils.closeQuietly(null);

    }

}
