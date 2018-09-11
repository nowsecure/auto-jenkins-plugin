package com.nowsecure.auto.jenkins.domain;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * ScoreInfo encapsulates score calculation based on security risk
 * 
 * @author sbhatti
 *
 */
public class ScoreInfo {
    private double score;
    private double baseScore;
    private Object issues;
    private Object adjustedIssues;
    private String findingsDigest;
    private String findingsVersion;
    private String cvssVersion;

    public ScoreInfo() {

    }

    public static ScoreInfo fromJson(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        //
        ScoreInfo scoreInfo = new ScoreInfo();
        if (jsonObject.get("score") != null) {
            scoreInfo.setScore(((Number) jsonObject.get("score")).longValue());
        }
        if (jsonObject.get("base_score") != null) {
            scoreInfo.setBaseScore(((Number) jsonObject.get("base_score")).longValue());
        }
        scoreInfo.setFindingsDigest((String) jsonObject.get("findings_digest"));
        scoreInfo.setFindingsVersion((String) jsonObject.get("findings_version"));
        scoreInfo.setCvssVersion((String) jsonObject.get("cvss_version"));
        scoreInfo.setIssues(jsonObject.get("issues"));
        scoreInfo.setAdjustedIssues(jsonObject.get("adjusted_issues"));
        return scoreInfo;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(double baseScore) {
        this.baseScore = baseScore;
    }

    public Object getIssues() {
        return issues;
    }

    public void setIssues(Object issues) {
        this.issues = issues;
    }

    public Object getAdjustedIssues() {
        return adjustedIssues;
    }

    public void setAdjustedIssues(Object adjustedIssues) {
        this.adjustedIssues = adjustedIssues;
    }

    public String getFindingsDigest() {
        return findingsDigest;
    }

    public void setFindingsDigest(String findingsDigest) {
        this.findingsDigest = findingsDigest;
    }

    public String getFindingsVersion() {
        return findingsVersion;
    }

    public void setFindingsVersion(String findingsVersion) {
        this.findingsVersion = findingsVersion;
    }

    public String getCvssVersion() {
        return cvssVersion;
    }

    public void setCvssVersion(String cvssVersion) {
        this.cvssVersion = cvssVersion;
    }

}
