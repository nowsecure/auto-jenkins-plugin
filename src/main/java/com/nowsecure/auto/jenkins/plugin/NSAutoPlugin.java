package com.nowsecure.auto.jenkins.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
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
import hudson.remoting.Callable;
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
public class NSAutoPlugin extends Builder implements SimpleBuildStep, NSAutoParameters, Serializable {
    private static final String NSAUTO_JENKINS = "nsauto_jenkins_";
    private static final long serialVersionUID = 1L;
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

    private static class Logger implements NSAutoLogger, Serializable {
        private static final long serialVersionUID = 1L;
        private final TaskListener listener;

        private Logger(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public void error(String msg) {
            listener.error(
                    "ERROR " + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg);
            System.err.println(
                    "ERROR " + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg);
        }

        @Override
        public void info(String msg) {
            listener.getLogger().println("INFO " + new Date() + "@" + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v"
                                         + IOHelper.getVersion() + " " + msg);
            System.out.println("INFO " + new Date() + "@" + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v"
                               + IOHelper.getVersion() + " " + msg);
        }
    }

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
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getScoreThreshold( )
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

    @SuppressWarnings("deprecation")
    @Override
    @POST
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        final File workspaceDir = new File(workspace.getRemote());
        final String token = run.getEnvironment().get("apiKey");
        final NSAutoLogger logger = new Logger(listener);
        final File localArtifactsDir = new File(run.getArtifactsDir(), NS_REPORTS_DIR);
        final File remoteArtifactsDir = new File(NSAUTO_JENKINS + run.getQueueId());
        //
        if (ParamsAdapter.hasFile(workspaceDir, localArtifactsDir, binaryName, PLUGIN_NAME)) {
            final ParamsAdapter params = new ParamsAdapter(this, token, workspaceDir, localArtifactsDir, binaryName,
                    breakBuildOnScore, waitForResults, PLUGIN_NAME);
            logger.info("****** Starting Local Execution with " + params + " ******\n");

            execute(listener, params, logger);
        } else {
            final ParamsAdapter params = new ParamsAdapter(this, token, workspaceDir, remoteArtifactsDir, binaryName,
                    breakBuildOnScore, waitForResults, PLUGIN_NAME);
            logger.info("****** Starting Remote Execution with " + params + " ******\n");
            Callable<Map<String, String>, IOException> task = new Callable<Map<String, String>, IOException>() {
                private static final long serialVersionUID = 1L;

                public Map<String, String> call() throws IOException {
                    return execute(listener, params, logger);
                }

                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {

                }
            };
            //
            Map<String, String> artifacts = launcher.getChannel().call(task);
            IOHelper helper = new IOHelper(PLUGIN_NAME, TIMEOUT);
            for (Map.Entry<String, String> e : artifacts.entrySet()) {
                File path = new File(localArtifactsDir, e.getKey());
                helper.save(path, e.getValue());
            }
        }
    }

    private static Map<String, String> execute(final TaskListener listener, ParamsAdapter params, NSAutoLogger logger)
            throws IOException {
        try {
            params.getFile();
            NSAutoGateway gw = new NSAutoGateway(params, logger, new IOHelper(PLUGIN_NAME, TIMEOUT));
            gw.execute();
            return gw.getArtifactContents(true);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new AbortException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            throw new AbortException(e.getMessage());
        }

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