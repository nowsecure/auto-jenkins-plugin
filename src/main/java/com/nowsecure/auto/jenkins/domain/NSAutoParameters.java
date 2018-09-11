package com.nowsecure.auto.jenkins.domain;

import hudson.util.Secret;

public interface NSAutoParameters {

    String getApiUrl();

    String getGroup();

    Secret getApiKey();

    String getBinaryName();

    String getDescription();

    boolean isWaitForResults();

    int getWaitMinutes();

    boolean isBreakBuildOnScore();

    int getScoreThreshold();

}