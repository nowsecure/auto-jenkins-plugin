package com.nowsecure.auto.jenkins.utils;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.jenkins.utils.IOHelper;

public class IOHelperTest {
    private static final String GROUP = "aaaa";
    private static final String API = "eyJ";
    private static final String file = "apkpure_app_887.apk";

    // @Test
    public void testUploadBinary() throws Exception {
        String json = IOHelper.upload("https://lab-api.nws-stg-west.nowsecure.io/binary/", API, file);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testUpload() throws Exception {
        String json = IOHelper.upload("https://lab-api.nowsecure.com/build/?group=" + GROUP, API, file);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testGetResults() throws Exception {
        String json = IOHelper.get("https://lab-api.nowsecure.com/app/android/pkg/assessment/task/results", API);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testGetScore() throws Exception {
        String json = IOHelper.get("https://lab-api.nowsecure.com/assessment/task/summary", API);
        Assert.assertNotNull(json);
    }

    @Test(expected = IOException.class)
    public void testGetUsage() throws Exception {
        String json = IOHelper.get("https://lab-api.nowsecure.com/resource/usage", API);
        Assert.assertNotNull(json);
    }

    @Test
    public void testFind() throws Exception {
        new File("/tmp/test.out").createNewFile();
        File file = IOHelper.find(new File("/tmp"), "test.out");
        Assert.assertNotNull(file);
    }

}
