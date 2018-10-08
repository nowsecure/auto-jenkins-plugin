package com.nowsecure.auto.jenkins.plugin;

import java.io.File;

import com.nowsecure.auto.domain.NSAutoParameters;

import hudson.AbortException;

public class ParamsAdapter implements NSAutoParameters {
    private final NSAutoParameters delegateParams;
    private final String token;
    private final File artifactsDir;
    private final File file;
    private final boolean breakBuildOnScore;
    private final boolean waitForResults;

    public ParamsAdapter(NSAutoParameters delegateParams, String token, File artifactsDir, File file,
            boolean breakBuildOnScore, boolean waitForResults) throws AbortException {
        this.delegateParams = delegateParams;
        this.artifactsDir = artifactsDir;
        this.file = file;
        this.breakBuildOnScore = breakBuildOnScore;
        this.waitForResults = waitForResults;
        this.token = token == null || token.isEmpty() ? delegateParams.getApiKey() : token;
        if (token == null || token.trim().isEmpty()) {
            throw new AbortException(Messages.NSAutoPlugin_DescriptorImpl_errors_missingKey());
        }
    }

    @Override
    public String getApiKey() {
        return token;
    }

    @Override
    public String getApiUrl() {
        // TODO Auto-generated method stub
        return delegateParams.getApiUrl();
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
        return file;
    }

    @Override
    public String getGroup() {
        return delegateParams.getGroup();
    }

    @Override
    public int getScoreThreshold() {
        return breakBuildOnScore && waitForResults ? delegateParams.getScoreThreshold() : 0;
    }

    @Override
    public int getWaitMinutes() {
        return waitForResults ? delegateParams.getWaitMinutes() : 0;
    }

}
