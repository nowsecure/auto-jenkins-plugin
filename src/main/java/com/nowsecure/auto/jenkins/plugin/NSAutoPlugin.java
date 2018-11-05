package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

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

import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.gateway.NSAutoGateway;
import com.nowsecure.auto.utils.IOHelper;

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
    private static final String NS_REPORTS_DIR = "nowsecure-auto-security-test";
    private static final int TIMEOUT = 60000;
    private static final String DEFAULT_URL = "https://lab-api.nowsecure.com";
    private static final String PLUGIN_NAME = "jenkins-nowsecure-auto-security-test";
    @CheckForNull
    private String apiUrl = DEFAULT_URL;
    private String group;
    private String binaryName;
    private String description;
    private boolean waitForResults;
    private int waitMinutes;
    private boolean breakBuildOnScore;
    private int scoreThreshold;
    private String apiKey;
    private boolean useBuildEndpoint;

    @DataBoundConstructor
    public NSAutoPlugin(String apiUrl, String group, String binaryName, String description, boolean waitForResults,
            int waitMinutes, boolean breakBuildOnScore, int scoreThreshold, String apiKey, boolean useBuildEndpoint) {
        this.apiUrl = apiUrl;
        this.group = group;
        this.binaryName = binaryName;
        this.description = description;
        this.waitForResults = waitForResults;
        this.waitMinutes = waitMinutes;
        this.breakBuildOnScore = breakBuildOnScore;
        this.scoreThreshold = scoreThreshold;
        this.apiKey = apiKey;
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
        return apiUrl != null && apiUrl.trim().length() > 0 ? apiUrl : DEFAULT_URL;
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
        return waitMinutes;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getScoreThreshold()
     */
    @Override
    public int getScoreThreshold() {
        return scoreThreshold;
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

    @Override
    public File getArtifactsDir() {
        throw new UnsupportedOperationException("getArtifactsDir not supported");
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException("getFile not supported");
    }

    private File getBinaryFile(File workspace, File artifactsDir, IOHelper helper, NSAutoLogger logger)
            throws IOException {
        if (!artifactsDir.mkdirs()) {
            System.err.println("Couldn't create " + artifactsDir);
        }
        String binary = getBinaryName();
        if (binary == null) {
            logger.error("binaryName parameter not defined");
            throw new AbortException("binaryName parameter not defined");
        }
        binary = binary.trim();
        File file;
        if (binary.startsWith("/") || binary.startsWith("\\")) {
            file = new File(binary);
            if (!file.exists()) {
                logger.error("Failed to find binary file '" + binary + "' ('" + file.getAbsolutePath() + "'\n");
                throw new AbortException(
                        "Failed to find binary file '" + binary + "' ('" + file.getAbsolutePath() + "'");
            }
        } else {
            file = helper.find(artifactsDir, new File(binary));
            if (file == null) {
                file = helper.find(workspace, new File(binary));
            }
            if (file == null || !file.exists()) {
                logger.error(
                        "Failed to find '" + binary + "' under '" + artifactsDir + "' or under '" + workspace + "'\n");
                throw new AbortException(
                        "Failed to find '" + binary + "' under '" + artifactsDir + "' or under '" + workspace + "'\n");
            }
        }
        return file;
    }

    @SuppressWarnings("deprecation")
    @Override
    @POST
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        final IOHelper helper = new IOHelper(PLUGIN_NAME, TIMEOUT);
        String token = run.getEnvironment().get("apiKey");
        //
        NSAutoLogger logger = new NSAutoLogger() {
            @Override
            public void error(String msg) {
                listener.error(IOHelper.LOCAL_HOST + ":" + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg);

            }

            @Override
            public void info(String msg) {
                listener.getLogger().println(new Date() + "@" + IOHelper.LOCAL_HOST + ":" + PLUGIN_NAME + "-v"
                                             + IOHelper.getVersion() + " " + msg);
            }
        };
        logger.info("****** Starting apiUrl " + apiUrl + ", binaryName " + binaryName + " ******\n");

        File file = getBinaryFile(new File(workspace.getRemote()), run.getArtifactsDir(), helper, logger);
        ParamsAdapter params = new ParamsAdapter(this, token, new File(run.getArtifactsDir(), NS_REPORTS_DIR), file,
                breakBuildOnScore, waitForResults);
        new NSAutoGateway(params, logger, helper).execute();
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
                    new IOHelper(PLUGIN_NAME, TIMEOUT).get(url, apiKey); // .getPlainText());
                    return FormValidation.ok();
                } catch (IOException e) {
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

}
