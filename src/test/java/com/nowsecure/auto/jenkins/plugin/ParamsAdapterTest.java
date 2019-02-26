package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ProxySettings;
import com.nowsecure.auto.utils.IOHelper;

import hudson.AbortException;

public class ParamsAdapterTest implements NSAutoParameters {
    private String token = "token";
    private String url = "https://lab-api.nowsecure.com";
    private File workspace = new File("/tmp");
    private File artifactsDir = new File("/tmp");
    private File file = new File("/tmp/test.apk");
    private String ipa = "/tmp/test.ipa";
    private String username;
    private String password;
    private boolean showStatusMessages;
    private String stopTestsForStatusMessage;
    private boolean debug;

    private int score;
    private int minutes;
    final IOHelper helper = new IOHelper("name", 0);

    @Before
    public void setup() throws IOException {
        new File(ipa).createNewFile();
    }

    @Test
    public void testConstructor() throws Exception {
        File dir = new File("/tmp/archive");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", workspace, dir, ipa, true, true, "pluginName", "bill",
                "pass", true, "stop", new ProxySettings(), true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertNotNull(param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(new File(ipa), param.getFile());
        Assert.assertEquals(30, param.getWaitMinutes());
        Assert.assertEquals(70, param.getScoreThreshold());
        Assert.assertEquals("pass", param.getPassword());
        Assert.assertEquals("stop", param.getStopTestsForStatusMessage());
        Assert.assertEquals("bill", param.getUsername());
        Assert.assertTrue(param.isShowStatusMessages());
    }

    @Test
    public void testConstructorWithScore() throws Exception {
        File dir = new File("/tmp/archive");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", workspace, dir, ipa, true, true, "pluginName",
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), false);

        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertNotNull(param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(new File(ipa), param.getFile());
        score = 60;
        minutes = 40;
        Assert.assertEquals(40, param.getWaitMinutes());
        Assert.assertEquals(60, param.getScoreThreshold());
    }

    @Test
    public void testConstructorWait() throws Exception {
        File dir = new File("/tmp/archive");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", workspace, dir, ipa, false, true, "pluginName",
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertNotNull(param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(new File(ipa), param.getFile());
        Assert.assertEquals(30, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test
    public void testConstructorScore() throws Exception {
        File dir = new File("/tmp/archive");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", workspace, dir, ipa, true, false, "pluginName",
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), false);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertNotNull(param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(new File(ipa), param.getFile());
        Assert.assertEquals(0, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test
    public void testConstructorNoWait() throws Exception {
        File dir = new File("/tmp/archive");
        ParamsAdapter param = new ParamsAdapter(this, "newToken", workspace, dir, ipa, false, false, "pluginName",
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), true);
        Assert.assertEquals("newToken", param.getApiKey());
        Assert.assertNotNull(param.getApiUrl());
        Assert.assertEquals("desc", param.getDescription());
        Assert.assertEquals("group", param.getGroup());
        Assert.assertEquals(dir, param.getArtifactsDir());
        Assert.assertEquals(new File(ipa), param.getFile());
        Assert.assertEquals(0, param.getWaitMinutes());
        Assert.assertEquals(0, param.getScoreThreshold());
    }

    @Test(expected = AbortException.class)
    public void testConstructorNullToken() throws Exception {
        token = null;
        new ParamsAdapter(this, null, new File("/tmp/archive"), new File("/tmp/test.ipa"), "binary ", true, true, null,
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), true);
    }

    @Test(expected = AbortException.class)
    public void testConstructorBinary() throws Exception {
        token = null;
        new ParamsAdapter(this, "xxxx", new File("/tmp/archive"), new File("/tmp/test.ipa"), null, true, true, null,
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), false);
    }

    @Test(expected = AbortException.class)
    public void testConstructorEmptyToken() throws Exception {
        token = null;
        new ParamsAdapter(this, "", new File("/tmp/archive"), new File("/tmp/test.ipa"), "binary ", true, true, null,
                username, password, showStatusMessages, stopTestsForStatusMessage, new ProxySettings(), true);
    }

    @Test
    public void testHasFile() throws Exception {
        File dir = new File("/tmp/tmp");
        dir.mkdirs();
        File file = new File(dir, "tst");
        file.createNewFile();
        Assert.assertTrue(ParamsAdapter.hasFile(file.getParentFile(), new File("."), file.getName(), "name"));
        file.delete();
        dir.delete();
    }

    @Test
    public void testHasFileAbsoluteNonExistant() throws Exception {
        File dir = new File("/tmp/tmp");
        File file = new File("/tmp/xxx/xxx");
        Assert.assertFalse(
                ParamsAdapter.hasFile(file.getParentFile(), new File("/tmpxxxx"), file.getAbsolutePath(), "name"));
        file.delete();
        dir.delete();
    }

    @Test
    public void testHasFileAbsolute() throws Exception {
        File dir = new File("/tmp/tmp");
        dir.mkdirs();
        File file = new File(dir, "tst");
        file.createNewFile();
        Assert.assertTrue(ParamsAdapter.hasFile(file.getParentFile(), new File("."), file.getAbsolutePath(), "name"));
        file.delete();
        dir.delete();
    }

    @Test
    public void testHasFileNonExistant() throws Exception {
        File dir = new File("/tmp/tmp");
        File file = new File(dir, "tst");
        Assert.assertFalse(ParamsAdapter.hasFile(file.getParentFile(), dir, file.getName(), "name"));
        dir.delete();
    }

    @Test
    public void testToString() throws Exception {
        ParamsAdapter params = new ParamsAdapter(this, "", new File("/tmp/archive"), new File("/tmp/test.ipa"),
                "binary ", true, true, null, username, password, showStatusMessages, stopTestsForStatusMessage,
                new ProxySettings(), true);
        Assert.assertNotNull(params.toString());
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

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isShowStatusMessages() {
        return showStatusMessages;
    }

    public void setShowStatusMessages(boolean showStatusMessages) {
        this.showStatusMessages = showStatusMessages;
    }

    @Override
    public String getStopTestsForStatusMessage() {
        return stopTestsForStatusMessage;
    }

    public void setStopTestsForStatusMessage(String stopTestsForStatusMessage) {
        this.stopTestsForStatusMessage = stopTestsForStatusMessage;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ProxySettings getProxySettings() {
        return new ProxySettings();
    }

    @Override
    public boolean isProxyEnabled() {
        return false;
    }

}
