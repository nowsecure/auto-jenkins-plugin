package com.nowsecure.auto.jenkins.domain;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * UploadInfo encapsulates meta-data when mobile binary is uploaded
 * 
 * @author sbhatti
 *
 */
public class UploadInfo {
    private String application;
    private String group;
    private String account;
    private String platform;
    private String packageId;
    private Long task;
    private String binary;
    private String creator;
    private String created;

    public UploadInfo() {

    }

    public static UploadInfo fromJson(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        // for error message
        String name = (String) jsonObject.get("name");
        String message = (String) jsonObject.get("message");
        if (name != null && message != null) {
            throw new RuntimeException(name + " " + message);
        }
        //
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setApplication((String) jsonObject.get("application"));
        uploadInfo.setGroup((String) jsonObject.get("group"));
        uploadInfo.setAccount((String) jsonObject.get("account"));
        uploadInfo.setPlatform((String) jsonObject.get("platform"));
        uploadInfo.setPackageId((String) jsonObject.get("package"));
        uploadInfo.setTask(((Number) jsonObject.get("task")).longValue());
        uploadInfo.setCreator((String) jsonObject.get("creator"));
        uploadInfo.setCreated((String) jsonObject.get("created"));

        if (uploadInfo.getPackageId() == null || uploadInfo.getPackageId().length() == 0) {
            throw new IllegalStateException("Package-id not found in JSON");
        }
        if (uploadInfo.getTask() == 0) {
            throw new IllegalStateException("Task not found in JSON");
        }
        return uploadInfo;

    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public Long getTask() {
        return task;
    }

    public void setTask(Long task) {
        this.task = task;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "ReportInfo [application=" + application + ", group=" + group + ", account=" + account + ", platform="
               + platform + ", packageId=" + packageId + ", task=" + task + ", binary=" + binary + ", creator="
               + creator + ", created=" + created + "]";
    }

}
