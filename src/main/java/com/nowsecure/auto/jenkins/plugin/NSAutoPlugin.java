package com.nowsecure.auto.jenkins.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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

import com.nowsecure.auto.domain.Color;
import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ProxySettings;
import com.nowsecure.auto.gateway.NSAutoGateway;
import com.nowsecure.auto.utils.IOHelper;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
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
    private String username;
    private String password;
    private boolean showStatusMessages;
    private String stopTestsForStatusMessage;
    private String artifactsDir;
    private ProxySettings proxySettings = new ProxySettings();
    private boolean debug;
    private boolean proxyEnabled;
    private Boolean validateDnsUrlConnection = true;

    private static class Logger implements NSAutoLogger, Serializable {
        private static final long serialVersionUID = 1L;
        private final TaskListener listener;
        private final boolean debug;

        private Logger(TaskListener listener, boolean debug) {
            this.listener = listener;
            this.debug = debug;
        }

        @Override
        public void error(String msg) {
            listener.error(Color.Red.format(
                    "ERROR " + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg));
            System.err.println(Color.Red.format(
                    "ERROR " + IOHelper.getLocalHost() + ":" + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg));
        }

        @Override
        public void info(String msg, Color color) {
            if (color == null) {
                color = Color.Cyan;
            }
            listener.getLogger().println(color.format("INFO " + new Date() + "@" + IOHelper.getLocalHost() + ":"
                                                      + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg));
            System.out.println(color.format("INFO " + new Date() + "@" + IOHelper.getLocalHost() + ":" + PLUGIN_NAME
                                            + "-v" + IOHelper.getVersion() + " " + msg));
        }

        @Override
        public void info(String msg) {
            info(msg, null);
        }

        @Override
        public void debug(String msg) {
            if (debug) {
                listener.getLogger()
                        .println(Color.Black.format("DEBUG " + new Date() + "@" + IOHelper.getLocalHost() + ":"
                                                    + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg));
                System.out.println(Color.Black.format("DEBUG " + new Date() + "@" + IOHelper.getLocalHost() + ":"
                                                      + PLUGIN_NAME + "-v" + IOHelper.getVersion() + " " + msg));
            }
        }

    }

    @DataBoundConstructor
    public NSAutoPlugin(String apiUrl, String group, String binaryName, String description, boolean waitForResults,
            int waitMinutes, boolean breakBuildOnScore, int scoreThreshold, String apiKey, boolean useBuildEndpoint,
            boolean validateDnsUrlConnection) {
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
        this.validateDnsUrlConnection = validateDnsUrlConnection;
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
        if (hasArtifactsDir()) {
            return new File(artifactsDir.trim());
        }
        return null;
    }

    public boolean hasArtifactsDir() {
        return artifactsDir != null && artifactsDir.trim().length() > 0;
    }

    public String getArtifactsDirPrefix() {
        if (hasArtifactsDir()) {
            return artifactsDir.trim() + System.getProperty("file.separator");
        }
        return "";
    }

    @DataBoundSetter
    public void setArtifactsDir(String dir) {
        this.artifactsDir = dir;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException("getFile not supported");
    }

    @Override
    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isShowStatusMessages() {
        return showStatusMessages;
    }

    @DataBoundSetter
    public void setShowStatusMessages(boolean showStatusMessages) {
        this.showStatusMessages = showStatusMessages;
    }

    @Override
    public String getStopTestsForStatusMessage() {
        return stopTestsForStatusMessage;
    }

    @DataBoundSetter
    public void setStopTestsForStatusMessage(String stopTestsForStatusMessage) {
        this.stopTestsForStatusMessage = stopTestsForStatusMessage;
    }

    @Override
    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @DataBoundSetter
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    @DataBoundSetter
    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    @Override
    public boolean isValidateDnsUrlConnectionEnabled() {
        if (validateDnsUrlConnection == null) {
            validateDnsUrlConnection = true;
        }
        return validateDnsUrlConnection;
    }

    @DataBoundSetter
    public void setValidateDnsUrlConnectionEnabled(boolean validateDnsUrlConnection) {
        this.validateDnsUrlConnection = validateDnsUrlConnection;
    }

    @SuppressWarnings("deprecation")
    static String normalize(final Run<?, ?> run, String value) {
        if (value == null) {
            return value;
        }
        if (value.startsWith("${") && value.endsWith("}")) {
            String name = value.substring(2, value.length() - 1);
            String newValue = System.getenv(name);
            if (newValue == null) {
                try {
                    return run.getEnvironment().get(name);
                } catch (Exception e) {
                    return value;
                }
            } else {
                return newValue;
            }
        } else {
            return value;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @POST
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        String classFilter = System.getProperty("hudson.remoting.ClassFilter", "");
        try {
            System.setProperty("hudson.remoting.ClassFilter", ProxySettings.class.getName());
            final File workspaceDir = new File(workspace.getRemote());
            final String token = run.getEnvironment().get("apiKey");
            final NSAutoLogger logger = new Logger(listener, debug);
            final File localArtifactsDir = new File(run.getArtifactsDir(), NS_REPORTS_DIR + run.getQueueId());
            final File remoteArtifactsDir = new File(getArtifactsDirPrefix() + NSAUTO_JENKINS + run.getQueueId());

            binaryName = normalize(run, binaryName);
            username = normalize(run, username);
            password = normalize(run, password);

            if (debug) {
                if (token != null) {
                    logger.info("Using API token from runtime environment: " + token, Color.Purple);
                } else {
                    logger.info("Using API token from bind variable: " + apiKey, Color.Purple);
                }
            }
            //
            if (proxyEnabled && Jenkins.getInstance() != null && Jenkins.getInstance().proxy != null) {
                final ProxyConfiguration proxy = Jenkins.getInstance().proxy;
                proxySettings.setProxyServer(proxy.name);
                proxySettings.setProxyPort(proxy.port);
                proxySettings.setUserName(proxy.getUserName());
                proxySettings.setProxyPass(proxy.getPassword());
                proxySettings.setNoProxyHost(proxy.noProxyHost);
                logger.info("Extracting proxy settings from Jenkins " + proxySettings, Color.Purple);
            } else {
                logger.info("Ignored proxy settings from Jenkins, proxyEnabled " + proxyEnabled + ", settings "
                            + (Jenkins.getInstance() != null && Jenkins.getInstance().proxy != null
                                    ? Jenkins.getInstance().proxy.name : "N/A"),
                        Color.Purple);
            }
            //
            if (ParamsAdapter.hasFile(workspaceDir, localArtifactsDir, binaryName, PLUGIN_NAME)) {
                final ParamsAdapter params = new ParamsAdapter(this, token, workspaceDir, localArtifactsDir, binaryName,
                        breakBuildOnScore, waitForResults, PLUGIN_NAME, username, password, showStatusMessages,
                        stopTestsForStatusMessage, proxySettings, debug);
                logger.info("****** Starting Local Execution with " + params + " ******\n", Color.Purple);

                execute(listener, params, logger, true);
            } else {
                final ParamsAdapter params = new ParamsAdapter(this, token, workspaceDir, remoteArtifactsDir,
                        binaryName, breakBuildOnScore, waitForResults, PLUGIN_NAME, username, password,
                        showStatusMessages, stopTestsForStatusMessage, proxySettings, debug);
                logger.info("****** Starting Remote Execution with " + params + " ******\n", Color.Purple);
                Callable<Map<String, String>, IOException> task = new Callable<Map<String, String>, IOException>() {
                    private static final long serialVersionUID = 1L;

                    public Map<String, String> call() throws IOException {
                        return execute(listener, params, logger, false);
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
        } finally {
            System.setProperty("hudson.remoting.ClassFilter", classFilter);
        }
    }

    private static Map<String, String> execute(final TaskListener listener, ParamsAdapter params, NSAutoLogger logger,
            boolean master) throws IOException {
        try {
            params.getFile();
            NSAutoGateway gw = new NSAutoGateway(params, logger, new IOHelper(PLUGIN_NAME, TIMEOUT));
            gw.execute(master);
            return gw.getArtifactContents(false); // !master);
        } catch (IOException e) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bos, true, "UTF-8"));
            logger.error(e.toString() + "\n" + bos.toString("UTF-8"));
            throw new AbortException(e.toString());
        } catch (RuntimeException e) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bos, true, "UTF-8"));
            logger.error(e.toString() + "\n" + bos.toString("UTF-8"));
            throw new AbortException(e.toString());
        }

    }

    @Symbol({ "apiKey", "apiUrl", "binaryName", "group", "artifactsDir" })
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doValidateParams(@QueryParameter("apiKey") String apiKey,
                @QueryParameter("apiUrl") String apiUrl, @QueryParameter("binaryName") final String binaryName,
                @QueryParameter("group") String group, @QueryParameter("artifactsDir") String artifactsDir,
                @SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                @AncestorInPath final Job<?, ?> owner)
                throws MessagingException, IOException, JSONException, ServletException {
            if (binaryName == null || binaryName.isEmpty()) {
                return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_missingBinary());
            }
            if (group == null || group.isEmpty()) {
                return FormValidation.errorWithMarkup(Messages.NSAutoPlugin_DescriptorImpl_errors_missingGroup());
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