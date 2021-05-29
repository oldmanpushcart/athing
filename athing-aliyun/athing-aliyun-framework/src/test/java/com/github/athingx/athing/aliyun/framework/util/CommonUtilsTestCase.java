package com.github.athingx.athing.aliyun.framework.util;

import org.junit.Assert;
import org.junit.Test;

public class CommonUtilsTestCase {

    @Test
    public void test$isEmptyString() {
        Assert.assertTrue(CommonUtils.isEmptyString(""));
        Assert.assertTrue(CommonUtils.isEmptyString(null));
        Assert.assertFalse(CommonUtils.isEmptyString(" "));
        Assert.assertFalse(CommonUtils.isEmptyString("123"));
    }

    @Test
    public void test$isIn() {
        Assert.assertTrue(CommonUtils.isIn(789, 123, 456, 789, 890));
        Assert.assertTrue(CommonUtils.isIn(789, 789));
        Assert.assertFalse(CommonUtils.isIn(789));
        Assert.assertFalse(CommonUtils.isIn(789, 123, 456, 890));
    }

    @Test
    public void test$getLogo() {
        Assert.assertNotNull(IOUtils.getLogo("logback-test.xml"));
    }

}
