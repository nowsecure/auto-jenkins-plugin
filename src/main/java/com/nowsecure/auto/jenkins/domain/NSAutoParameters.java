package com.nowsecure.auto.jenkins.domain;

public interface NSAutoParameters {

    String getApiUrl();

    String getGroup();

    String getBinaryName();

    String getDescription();

    boolean isWaitForResults();

    int getWaitMinutes();

    boolean isBreakBuildOnScore();

    int getScoreThreshold();

}