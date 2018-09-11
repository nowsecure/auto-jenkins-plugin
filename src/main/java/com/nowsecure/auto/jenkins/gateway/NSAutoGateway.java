package com.nowsecure.auto.jenkins.gateway;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.simple.parser.ParseException;

import com.nowsecure.auto.jenkins.domain.NSAutoParameters;
import com.nowsecure.auto.jenkins.domain.ReportInfo;
import com.nowsecure.auto.jenkins.domain.ScoreInfo;
import com.nowsecure.auto.jenkins.domain.UploadInfo;
import com.nowsecure.auto.jenkins.utils.IOHelper;

import hudson.FilePath;
import hudson.model.TaskListener;

public class NSAutoGateway {
    private static final String NOWSECURE_AUTO_SECURITYTEST_UPLOADED_JSON = "/nowsecure-auto-securitytest-uploaded.json";
    private static final String NOWSECURE_AUTO_SECURITYTEST_SCORE_JSON = "/nowsecure-auto-securitytest-score.json";
    private static final String NOWSECURE_AUTO_SECURITYTEST_REPORT_JSON = "/nowsecure-auto-securitytest-report.json";
    private static final int ONE_MINUTE = 1000 * 60;
    //
    private final NSAutoParameters params;
    private final File workspace;
    private final File artifactsDir;
    private final TaskListener listener;

    public NSAutoGateway(NSAutoParameters params, File artifactsDir, FilePath workspace, TaskListener listener)
            throws IOException {
        this.params = params;
        this.workspace = new File(workspace.getRemote());
        this.artifactsDir = artifactsDir;
        this.listener = listener;
        if (!artifactsDir.mkdirs()) {
            info("Could not create directory " + artifactsDir);
        }
        if (params.getBinaryName() == null || params.getBinaryName().length() == 0) {
            throw new IOException("Binary not specified");
        }
    }

    public void execute() throws InterruptedException, IOException {
        info("Executing step for " + this);
        try {
            UploadInfo uploadInfo = upload();
            //
            if (params.isWaitForResults()) {
                waitForResults(uploadInfo);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            throw new IOException("Failed to analyze security", e);
        }
    }

    @Override
    public String toString() {
        return "nowsecure-auto-securitytest [apiUrl=" + params.getApiUrl() + ", group=" + params.getGroup()
               + ", binaryName=" + params.getBinaryName() + ", waitForResults=" + params.isWaitForResults()
               + ", waitMinutes=" + params.getWaitMinutes() + ", breakBuildOnScore=" + params.isBreakBuildOnScore()
               + ", scoreThreshold=" + params.getScoreThreshold() + "]";
    }

    private ReportInfo[] getReportInfos(UploadInfo uploadInfo) throws IOException, ParseException {
        String resultsUrl = buildUrl(
                "/app/android/" + uploadInfo.getPackageId() + "/assessment/" + uploadInfo.getTask() + "/results");
        String resultsPath = artifactsDir.getCanonicalPath() + NOWSECURE_AUTO_SECURITYTEST_REPORT_JSON;
        String reportJson = IOHelper.get(resultsUrl, params.getApiKey().getPlainText());
        ReportInfo[] reportInfos = ReportInfo.fromJson(reportJson);
        if (reportInfos.length > 0) {
            IOHelper.save(resultsPath, reportJson);
            info("Saved test report from " + resultsUrl + " to " + resultsPath);
        }
        return reportInfos;
    }

    private ScoreInfo getScoreInfo(UploadInfo uploadInfo) throws ParseException, IOException {
        String scoreUrl = buildUrl("/assessment/" + uploadInfo.getTask() + "/summary");
        String scorePath = artifactsDir.getCanonicalPath() + NOWSECURE_AUTO_SECURITYTEST_SCORE_JSON;
        String scoreJson = IOHelper.get(scoreUrl, params.getApiKey().getPlainText());
        if (scoreJson.length() == 0) {
            return null;
        }
        IOHelper.save(scorePath, scoreJson);
        info("Saved score report from " + scoreUrl + " to " + scorePath);
        return ScoreInfo.fromJson(scoreJson);
    }

    private void waitForResults(UploadInfo uploadInfo) throws IOException, ParseException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < params.getWaitMinutes(); min++) {
            info("waiting results for job " + uploadInfo.getTask() + " [" + getElapsedMinutes(started) + "]");
            try {
                Thread.sleep(ONE_MINUTE);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } // wait a minute
            ScoreInfo scoreInfo = getScoreInfo(uploadInfo);
            if (scoreInfo != null) {
                getReportInfos(uploadInfo);
                if (scoreInfo.getScore() < params.getScoreThreshold()) {
                    throw new IOException("Test failed because score (" + scoreInfo.getScore()
                                          + ") is lower than threshold " + params.getScoreThreshold());
                }
                info("test passed with score " + scoreInfo.getScore() + " in " + getElapsedMinutes(started));
                return;
            }
        }
        throw new IOException(
                "Timedout (" + getElapsedMinutes(started) + ") while waiting for job " + uploadInfo.getTask());
    }

    private String getElapsedMinutes(long started) {
        return (System.currentTimeMillis() - started) / ONE_MINUTE + " minutes";
    }

    private UploadInfo upload() throws IOException, ParseException {
        File file = IOHelper.find(artifactsDir, params.getBinaryName());
        if (file == null) {
            file = IOHelper.find(workspace, params.getBinaryName());
        }
        if (file == null) {
            throw new IOException("Failed to find " + params.getBinaryName() + " under " + artifactsDir);
        }
        //
        String url = buildUrl("/build/");
        info("uploading binary " + file.getAbsolutePath() + " to " + url);
        String uploadJson = IOHelper.upload(url, params.getApiKey().getPlainText(), file.getCanonicalPath());
        String path = artifactsDir.getCanonicalPath() + NOWSECURE_AUTO_SECURITYTEST_UPLOADED_JSON;
        IOHelper.save(path, uploadJson); //
        UploadInfo uploadInfo = UploadInfo.fromJson(uploadJson);
        info("uploaded binary with job-id " + uploadInfo.getTask() + " and saved output to " + path);
        return uploadInfo;
    }

    private String buildUrl(String path) throws MalformedURLException {
        return buildUrl(path, new URL(params.getApiUrl()), params.getGroup());
    }

    public static String buildUrl(String path, URL api, String group) throws MalformedURLException {
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

    void info(Object msg) {
        listener.getLogger().println(new Date() + " nowsecure-auto-securitytest " + msg);
    }

    void error(Object msg) {
        listener.error(new Date() + " nowsecure-auto-securitytest " + msg);
    }
}
