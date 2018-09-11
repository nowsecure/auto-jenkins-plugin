package com.nowsecure.auto.jenkins.domain;

import java.io.StringWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

/**
 * ReportInfo encapsulates JSON response from raw analysis report
 * 
 * @author sbhatti
 *
 */
public class ReportInfo {
    private String kind;
    private String key;
    private String title;
    private String category;
    private String summary;
    private double cvss;
    private String cvssVector;
    private boolean affected;
    private String severity;
    private String description;
    private Object regulatory;
    private Object issues;
    private Object context;

    public static ReportInfo[] fromJson(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(json);
        //
        ReportInfo[] reportInfos = new ReportInfo[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            ReportInfo reportInfo = new ReportInfo();
            reportInfo.setKind((String) jsonObject.get("kind"));
            reportInfo.setKey((String) jsonObject.get("key"));
            reportInfo.setTitle((String) jsonObject.get("title"));
            reportInfo.setCategory((String) jsonObject.get("category"));
            reportInfo.setSummary((String) jsonObject.get("summary"));
            if (jsonObject.get("cvss") != null) {
                reportInfo.setCvss(((Number) jsonObject.get("cvss")).doubleValue());
            }
            reportInfo.setCvssVector((String) jsonObject.get("cvss_vector"));
            reportInfo.setAffected((Boolean) jsonObject.get("affected"));
            reportInfo.setSeverity((String) jsonObject.get("severity"));
            reportInfo.setDescription((String) jsonObject.get("description"));
            reportInfo.setRegulatory(jsonObject.get("regulatory"));
            reportInfo.setIssues(jsonObject.get("issues"));
            reportInfo.setContext(jsonObject.get("context"));
            reportInfos[i] = reportInfo;
        }

        return reportInfos;

    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getCvss() {
        return cvss;
    }

    public void setCvss(double cvss) {
        this.cvss = cvss;
    }

    public String getCvssVector() {
        return cvssVector;
    }

    public void setCvssVector(String cvssVector) {
        this.cvssVector = cvssVector;
    }

    public boolean isAffected() {
        return affected;
    }

    public void setAffected(boolean affected) {
        this.affected = affected;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getRegulatory() {
        return regulatory;
    }

    public void setRegulatory(Object regulatory) {
        this.regulatory = regulatory;
    }

    public Object getIssues() {
        return issues;
    }

    public void setIssues(Object issues) {
        this.issues = issues;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    @Override
    public String toString() {
        Yaml yaml = new Yaml();
        StringWriter writer = new StringWriter();
        yaml.dump(this, writer);
        return writer.toString();
    }

}
