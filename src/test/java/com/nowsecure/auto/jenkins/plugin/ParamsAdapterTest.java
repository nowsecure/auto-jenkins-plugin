package com.nowsecure.auto.jenkins.plugin;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.domain.NSAutoParameters;

import hudson.AbortException;

public class ParamsAdapterTest implements NSAutoParameters {
    private String token = "token";
    private String url = "url";
    private File artifactsDir = new File("/tmp");
    private File file = new File("/tmp/test.apk");
    private int score;
    private int minutes;

    @Test
    public void testConstructor() throws Exception {
        File dir = new File("/tmp/archive");
        File ipa = new File("/tmp/test.ipa");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", dir, ipa, true, true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertEquals("url", param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(ipa, param.getFile());
        Assert.assertEquals(30, param.getWaitMinutes());
        Assert.assertEquals(70, param.getScoreThreshold());
    }
    
    @Test
    public void testConstructorWithScore() throws Exception {
        File dir = new File("/tmp/archive");
        File ipa = new File("/tmp/test.ipa");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", dir, ipa, true, true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertEquals("url", param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(ipa, param.getFile());
        score = 60;
        minutes = 40;
        Assert.assertEquals(40, param.getWaitMinutes());
        Assert.assertEquals(60, param.getScoreThreshold());
    }

    @Test
    public void testConstructorWait() throws Exception {
        File dir = new File("/tmp/archive");
        File ipa = new File("/tmp/test.ipa");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", dir, ipa, false, true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertEquals("url", param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(ipa, param.getFile());
        Assert.assertEquals(30, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test
    public void testConstructorScore() throws Exception {
        File dir = new File("/tmp/archive");
        File ipa = new File("/tmp/test.ipa");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", dir, ipa, true, false);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertEquals("url", param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(ipa, param.getFile());
        Assert.assertEquals(0, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test
    public void testConstructorNoWait() throws Exception {
        File dir = new File("/tmp/archive");
        File ipa = new File("/tmp/test.ipa");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", dir, ipa, false, false);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertEquals("url", param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(ipa, param.getFile());
        Assert.assertEquals(0, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test(expected = AbortException.class)
    public void testConstructorNullToken() throws Exception {
        token = null;
        new ParamsAdapter(this, null, new File("/tmp/archive"), new File("/tmp/test.ipa"), true, true);
    }

    @Test(expected = AbortException.class)
    public void testConstructorEmptyToken() throws Exception {
        token = null;
        new ParamsAdapter(this, "", new File("/tmp/archive"), new File("/tmp/test.ipa"), true, true);
    }

    @Override
    public String getApiKey() {
        return token;
    }

    @Override
    public String getApiUrl() {
        return url;
    }

    @Override
    public File getArtifactsDir() {
        return artifactsDir;
    }

    @Override
    public String getDescription() {
        return "desc";
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getGroup() {
        return "group";
    }

    @Override
    public int getScoreThreshold() {
        return score;
    }

    @Override
    public int getWaitMinutes() {
        return minutes;
    }

}
