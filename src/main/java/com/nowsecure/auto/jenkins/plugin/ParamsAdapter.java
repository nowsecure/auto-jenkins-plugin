package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ProxySettings;
import com.nowsecure.auto.utils.IOHelper;

import hudson.AbortException;

public class ParamsAdapter implements NSAutoParameters, Serializable {
    private static final int TIMEOUT = 60000;
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SCORE_THRESHOLD = 70;
    private static final int DEFAULT_WAIT_MINUTES = 30;
    private final NSAutoParameters delegateParams;
    private final String apiUrl;
    private final String token;
    private final File workspace;
    private final File artifactsDir;
    private final String binaryName;
    private final boolean breakBuildOnScore;
    private final boolean waitForResults;
    private final String pluginName;
    private final String username;
    private final String password;
    private final boolean showStatusMessages;
    private final String stopTestsForStatusMessage;
    private final boolean debug;
    private final ProxySettings proxySettings;

    //
    public ParamsAdapter(NSAutoParameters delegateParams, String overrideApiKey, File workspace, File artifactsDir,
            String binaryName, boolean breakBuildOnScore, boolean waitForResults, String pluginName, String username,
            String password, boolean showStatusMessages, String stopTestsForStatusMessage, ProxySettings proxySettings,
            boolean debug) throws AbortException, MalformedURLException, UnknownHostException {
        if (binaryName == null) {
            throw new AbortException("binaryName parameter not defined");
        }
        //
        this.apiUrl = delegateParams.getApiUrl();
        this.delegateParams = delegateParams;
        this.workspace = workspace;
        this.artifactsDir = artifactsDir;
        this.binaryName = binaryName.trim();
        this.pluginName = pluginName;
        this.breakBuildOnScore = breakBuildOnScore;
        this.waitForResults = waitForResults;
        this.username = username;
        this.password = password;
        this.showStatusMessages = showStatusMessages;
        this.stopTestsForStatusMessage = stopTestsForStatusMessage;
        this.debug = debug;
        this.proxySettings = proxySettings;
        this.token = overrideApiKey == null || overrideApiKey.trim().isEmpty() ? delegateParams.getApiKey()
                : overrideApiKey;
        if (this.token == null || this.token.trim().isEmpty()) {
            throw new AbortException(Messages.NSAutoPlugin_DescriptorImpl_errors_missingKey());
        }
    }

    @Override
    public String getApiKey() {
        return token;
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public File getArtifactsDir() {
        return artifactsDir;
    }

    @Override
    public String getDescription() {
        return delegateParams.getDescription();
    }

    @Override
    public File getFile() {
        return getFile(workspace, artifactsDir, binaryName, pluginName);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getStopTestsForStatusMessage() {
        return stopTestsForStatusMessage;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isShowStatusMessages() {
        return showStatusMessages;
    }

    public static boolean hasFile(File workspace, File artifactsDir, String binaryName, String pluginName) {
        try {
            File file = getFile(workspace, artifactsDir, binaryName, pluginName);
            return file != null && file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static File getFile(File workspace, File artifactsDir, String binaryName, String pluginName) {
        if (!artifactsDir.exists() && !artifactsDir.mkdirs()) {
            System.err.println("Couldn't create " + artifactsDir);
        }
        File file = null;
        //
        if (binaryName.startsWith("/") || binaryName.startsWith("\\")) {
            file = new File(binaryName);
            if (!file.exists()) {
                throw new IllegalArgumentException(
                        "Failed to find binary file '" + binaryName + "' ('" + file.getAbsolutePath() + "')");
            }
        } else {
            final IOHelper helper = new IOHelper(pluginName, TIMEOUT);
            try {
                file = helper.find(artifactsDir, new File(binaryName));
                if (file == null) {
                    file = helper.find(workspace, new File(binaryName));
                }
            } catch (IOException e) {
            }
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("Failed to find '" + binaryName + "' under '" + artifactsDir
                                                   + "' or under '" + workspace + "'\n");
            }
        }
        return file;
    }

    @Override
    public String getGroup() {
        return delegateParams.getGroup();
    }

    @Override
    public int getScoreThreshold() {
        return breakBuildOnScore && waitForResults ? (delegateParams.getScoreThreshold() > 0
                ? delegateParams.getScoreThreshold() : DEFAULT_SCORE_THRESHOLD) : 0;
    }

    @Override
    public int getWaitMinutes() {
        return waitForResults
                ? (delegateParams.getWaitMinutes() > 0 ? delegateParams.getWaitMinutes() : DEFAULT_WAIT_MINUTES) : 0;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    @Override
    public String toString() {
        return "Params [workspace=" + workspace + ", artifactsDir=" + artifactsDir + ", binaryName=" + binaryName
               + ", apiUrl=" + getApiUrl() + ", proxySettings=" + proxySettings + ", debug=" + debug + "]";
    }

}
