package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.json.simple.parser.ParseException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.nowsecure.auto.jenkins.domain.ReportInfo;
import com.nowsecure.auto.jenkins.domain.ScoreInfo;
import com.nowsecure.auto.jenkins.domain.UploadInfo;
import com.nowsecure.auto.jenkins.utils.IOHelper;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONException;

/**
 * This class defines business logic for uploading mobile binary and retrieving
 * results and score. It would fail the job if score is below user-defined
 * threshold.
 * 
 * @author sbhatti
 *
 */
public class NSAutoPlugin extends Builder implements SimpleBuildStep {

    private static final int ONE_MINUTE = 1000 * 60;
    private static final int DEFAULT_SCORE_THRESHOLD = 70;
    private static final int DEFAULT_WAIT_MINUTES = 15;
    private static final String DEFAULT_URL = "https://lab-api.nowsecure.com";
    private String apiUrl;
    private String group;
    private Secret apiKey;
    private String binaryName;
    private String description;
    private boolean waitForResults;
    private int waitMinutes = DEFAULT_WAIT_MINUTES;
    private boolean breakBuildOnScore;
    private int scoreThreshold = DEFAULT_SCORE_THRESHOLD;

    @DataBoundConstructor
    public NSAutoPlugin(Secret apiKey, String binaryName, String description, String apiUrl, String group,
            boolean waitForResults, int waitMinutes, boolean breakBuildOnScore, int scoreThreshold) {
        this.apiUrl = apiUrl;
        this.group = group;
        this.apiKey = apiKey;
        this.binaryName = binaryName;
        this.description = description;
        this.waitForResults = waitForResults;
        this.waitMinutes = waitMinutes;
        this.breakBuildOnScore = breakBuildOnScore;
        this.scoreThreshold = scoreThreshold;
    }

    public String getApiUrl() {
        return apiUrl != null && apiUrl.length() > 0 ? apiUrl : DEFAULT_URL;
    }

    @DataBoundSetter
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getGroup() {
        return group;
    }

    @DataBoundSetter
    public void setGroup(String group) {
        this.group = group;
    }

    public Secret getApiKey() {
        return apiKey;
    }

    @DataBoundSetter
    public void setApiKey(Secret apiKey) {
        this.apiKey = apiKey;
    }

    public String getBinaryName() {
        return binaryName;
    }

    @DataBoundSetter
    public void setBinaryName(String binaryName) {
        this.binaryName = binaryName;
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isWaitForResults() {
        return waitForResults;
    }

    @DataBoundSetter
    public void setWaitForResults(boolean waitForResults) {
        this.waitForResults = waitForResults;
    }

    public int getWaitMinutes() {
        return waitMinutes == 0 || waitMinutes > 100 ? DEFAULT_WAIT_MINUTES : waitMinutes;
    }

    @DataBoundSetter
    public void setWaitMinutes(int waitMinutes) {
        this.waitMinutes = waitMinutes;
    }

    public boolean isBreakBuildOnScore() {
        return breakBuildOnScore;
    }

    @DataBoundSetter
    public void setBreakBuildOnScore(boolean breakBuildOnScore) {
        this.breakBuildOnScore = breakBuildOnScore;
    }

    public int getScoreThreshold() {
        return scoreThreshold == 0 || scoreThreshold > 100 ? DEFAULT_SCORE_THRESHOLD : scoreThreshold;
    }

    @DataBoundSetter
    public void setScoreThreshold(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println(new Date() + " Executing step for " + this);
        File artifactsDir = run.getArtifactsDir();
        if (!artifactsDir.mkdirs()) {
            listener.getLogger().println(new Date() + " Could not create directory " + artifactsDir);
        }
        try {
            UploadInfo upload = upload(listener, artifactsDir, new File(workspace.getRemote()));
            //
            if (waitForResults) {
                waitForResults(listener, artifactsDir, upload);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            listener.getLogger().println(new Date() + " failed to analyze security " + e);
            throw new IOException("Failed to analyze security", e);
        }
    }

    private ReportInfo[] getReportInfos(TaskListener listener, File artifactsDir, UploadInfo upload)
            throws IOException, ParseException {
        String resultsUrl = buildUrl(
                "/app/android/" + upload.getPackageId() + "/assessment/" + upload.getTask() + "/results");
        String resultsPath = artifactsDir.getCanonicalPath() + "/ns-report.json";
        String reportJson = IOHelper.get(resultsUrl, apiKey.getPlainText());
        ReportInfo[] reportInfos = ReportInfo.fromJson(reportJson);
        if (reportInfos.length > 0) {
            IOHelper.save(resultsPath, reportJson);
            listener.getLogger()
                    .println(new Date() + " Saved analysis report from " + resultsUrl + " to " + resultsPath);
        }
        return reportInfos;
    }

    private ScoreInfo getScoreInfo(TaskListener listener, File artifactsDir, UploadInfo upload)
            throws ParseException, IOException {
        String scoreUrl = buildUrl("/assessment/" + upload.getTask() + "/summary");
        String scorePath = artifactsDir.getCanonicalPath() + "/ns-score.json";
        String scoreJson = IOHelper.get(scoreUrl, apiKey.getPlainText());
        if (scoreJson.length() == 0) {
            throw new IOException("Failed to retrieve score from " + scoreUrl);
        }
        IOHelper.save(scorePath, scoreJson);
        listener.getLogger().println(new Date() + " Saved score report from " + scoreUrl + " to " + scorePath);
        return ScoreInfo.fromJson(scoreJson);
    }

    private void waitForResults(TaskListener listener, File artifactsDir, UploadInfo upload)
            throws IOException, ParseException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < getWaitMinutes(); min++) {
            listener.getLogger().println(new Date() + " Waiting for results for job " + upload.getTask());
            try {
                Thread.sleep(ONE_MINUTE);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } // wait a minute
            ReportInfo[] reportInfos = getReportInfos(listener, artifactsDir, upload);
            if (reportInfos.length > 0) {
                ScoreInfo scoreInfo = getScoreInfo(listener, artifactsDir, upload);
                if (scoreInfo.getScore() < getScoreThreshold()) {
                    throw new IOException("Analysis failed because score (" + scoreInfo.getScore()
                                          + ") is lower than threshold " + getScoreThreshold());
                }
                long elapsed = (System.currentTimeMillis() - started) / ONE_MINUTE;
                listener.getLogger().println(new Date() + " NS Security analysis passed in " + elapsed + " minutes");
                return;
            }
        }
        long elapsed = (System.currentTimeMillis() - started) / ONE_MINUTE;
        listener.error(new Date() + " Timedout (" + elapsed + " minutes) while waiting for job " + upload.getTask());
        throw new IOException("Timedout (" + elapsed + " minutes) while waiting for job " + upload.getTask());
    }

    private UploadInfo upload(TaskListener listener, File artifactsDir, File workspaceDir)
            throws IOException, ParseException {
        if (binaryName == null || binaryName.length() == 0) {
            throw new IOException("Binary not specified");
        }
        File file = IOHelper.find(artifactsDir, binaryName);
        if (file == null) {
            file = IOHelper.find(workspaceDir, binaryName);
        }
        if (file == null) {
            throw new IOException("Failed to find " + binaryName + " under " + artifactsDir);
        }
        //
        String url = buildUrl("/build/");
        listener.getLogger().println(new Date() + " Uploading binary to " + url);

        String uploadJson = IOHelper.upload(url, apiKey.getPlainText(), file.getCanonicalPath());
        String path = artifactsDir.getCanonicalPath() + "/ns-uploaded.json";
        IOHelper.save(path, uploadJson);
        listener.getLogger().println(new Date() + " Uploaded binary to " + url + " and saved output to " + path);
        //
        UploadInfo upload = UploadInfo.fromJson(uploadJson);
        return upload;
    }

    // @Symbol("apiKey")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doTestApiKey(@QueryParameter("apiUrl") final String apiUrl,
                @QueryParameter("apiKey") final String apiKey)
                throws MessagingException, IOException, JSONException, ServletException {
            if (apiKey != null) {
                try {
                    IOHelper.get(buildUrl("/resource/usage", new URL(apiUrl), null), apiKey);
                    return FormValidation.ok();
                } catch (IOException e) {
                    return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_invalidKey());
                }
            } else {
                return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_missingKey());
            }
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.NSAutoPlugin_DescriptorImpl_DisplayName();
        }

    }

    @Override
    public String toString() {
        return "nowsecure-auto-securitytest [apiUrl=" + apiUrl + ", group=" + group + ", binaryName=" + binaryName
               + ", description=" + description + ", waitForResults=" + waitForResults + ", waitMinutes=" + waitMinutes
               + ", breakBuildOnScore=" + breakBuildOnScore + ", scoreThreshold=" + scoreThreshold + "]";
    }

    private String buildUrl(String path) throws MalformedURLException {
        return buildUrl(path, new URL(getApiUrl()), group);
    }

    private static String buildUrl(String path, URL api, String group) throws MalformedURLException {
        String baseUrl = api.getProtocol() + "://" + api.getHost();
        if (api.getPort() > 0) {
            baseUrl += ":" + api.getPort();
        }
        String url = baseUrl + path;
        if (group != null && group.length() > 0) {
            url += "?group=" + group;
        }
        return url;
    }
}
