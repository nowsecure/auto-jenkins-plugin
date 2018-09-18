package com.nowsecure.auto.jenkins.plugin;

import java.io.IOException;
import java.net.URL;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import com.nowsecure.auto.jenkins.domain.NSAutoParameters;
import com.nowsecure.auto.jenkins.gateway.NSAutoGateway;
import com.nowsecure.auto.jenkins.utils.IOHelper;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
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
public class NSAutoPlugin extends Builder implements SimpleBuildStep, NSAutoParameters {
    private static final String NOWSECURE_API_KEY = "NOWSECURE_API_KEY";
    private static final int DEFAULT_SCORE_THRESHOLD = 70;
    private static final int DEFAULT_WAIT_MINUTES = 30;
    private static final String DEFAULT_URL = "https://lab-api.nowsecure.com";
    @CheckForNull
    private String apiUrl = DEFAULT_URL;
    private String group;
    private String binaryName;
    private String description = "NowSecure Auto Security Test";
    private boolean waitForResults;
    private int waitMinutes = DEFAULT_WAIT_MINUTES;
    private boolean breakBuildOnScore;
    private int scoreThreshold = DEFAULT_SCORE_THRESHOLD;
    private String apiKey;
    private boolean useBuildEndpoint;

    @DataBoundConstructor
    public NSAutoPlugin(String binaryName, String description, String apiUrl, String group, boolean waitForResults,
            int waitMinutes, boolean breakBuildOnScore, int scoreThreshold, boolean useBuildEndpoint) {
        this.apiUrl = apiUrl;
        this.group = group;
        this.binaryName = binaryName;
        this.description = description;
        this.waitForResults = waitForResults;
        this.waitMinutes = waitMinutes;
        this.breakBuildOnScore = breakBuildOnScore;
        this.scoreThreshold = scoreThreshold;
        this.useBuildEndpoint = useBuildEndpoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getApiUrl()
     */
    @Override
    @Nonnull
    public String getApiUrl() {
        return apiUrl != null && apiUrl.length() > 0 ? apiUrl : DEFAULT_URL;
    }

    @DataBoundSetter
    public void setApiUrl(@Nonnull String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getGroup()
     */
    @Override
    public String getGroup() {
        return group;
    }

    @DataBoundSetter
    public void setGroup(String group) {
        this.group = group;
    }

    public String getApiKey() {
        return apiKey;
    }

    @DataBoundSetter
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getBinaryName()
     */
    @Override
    @Nonnull
    public String getBinaryName() {
        return binaryName;
    }

    @DataBoundSetter
    public void setBinaryName(@Nonnull String binaryName) {
        this.binaryName = binaryName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#isWaitForResults()
     */
    @Override
    public boolean isWaitForResults() {
        return waitForResults;
    }

    @DataBoundSetter
    public void setWaitForResults(boolean waitForResults) {
        this.waitForResults = waitForResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getWaitMinutes()
     */
    @Override
    public int getWaitMinutes() {
        return waitMinutes == 0 || waitMinutes > 100 ? DEFAULT_WAIT_MINUTES : waitMinutes;
    }

    @DataBoundSetter
    public void setWaitMinutes(int waitMinutes) {
        this.waitMinutes = waitMinutes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#isBreakBuildOnScore()
     */
    @Override
    public boolean isBreakBuildOnScore() {
        return breakBuildOnScore;
    }

    @DataBoundSetter
    public void setBreakBuildOnScore(boolean breakBuildOnScore) {
        this.breakBuildOnScore = breakBuildOnScore;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getScoreThreshold()
     */
    @Override
    public int getScoreThreshold() {
        return scoreThreshold == 0 || scoreThreshold > 100 ? DEFAULT_SCORE_THRESHOLD : scoreThreshold;
    }

    @DataBoundSetter
    public void setScoreThreshold(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public boolean isUseBuildEndpoint() {
        return useBuildEndpoint;
    }

    @DataBoundSetter
    public void setUseBuildEndpoint(boolean useBuildEndpoint) {
        this.useBuildEndpoint = useBuildEndpoint;
    }

    @SuppressWarnings("deprecation")
    @Override
    @POST
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        String token = run.getEnvironment().get("apiKey");
        if (token == null || token.isEmpty()) {
            token = apiKey;
        }
        if (token == null || token.isEmpty()) {
            throw new AbortException(Messages.NSAutoPlugin_DescriptorImpl_errors_missingKey());
        }
        new NSAutoGateway(this, run.getArtifactsDir(), workspace, listener, token).execute();
    }

    @Symbol({ "apiKey", "apiUrl", "binaryName" })
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doValidateParams(@QueryParameter("apiKey") String apiKey,
                @QueryParameter("apiUrl") String apiUrl, @QueryParameter("binaryName") final String binaryName,
                @SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                @AncestorInPath final Job<?, ?> owner)
                throws MessagingException, IOException, JSONException, ServletException {
            if (binaryName == null || binaryName.isEmpty()) {
                return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_missingBinary());
            }
            if (apiKey != null) {
                if (apiUrl == null || apiUrl.isEmpty()) {
                    apiUrl = DEFAULT_URL;
                }
                try {
                    String url = NSAutoGateway.buildUrl("/resource/usage", new URL(apiUrl), null);
                    IOHelper.get(url, apiKey); // .getPlainText());
                    return FormValidation.ok();
                } catch (Exception e) {
                    return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_invalidKey());
                }
            } else {
                return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_missingKey());
            }
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.NSAutoPlugin_DescriptorImpl_DisplayName();
        }

    }

    @Override
    public String toString() {
        return "NSAutoPlugin [apiUrl=" + apiUrl + ", group=" + group + ", binaryName=" + binaryName
               + ", waitForResults=" + waitForResults + ", waitMinutes=" + waitMinutes + ", breakBuildOnScore="
               + breakBuildOnScore + ", scoreThreshold=" + scoreThreshold + ", apiKey="
               + (apiKey != null ? "****" : "undefined") + "]";
    }

}
