/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.InfoObject;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.JavaSecurityManagers;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.naming.InitialContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.openejb.loader.IO.close;

public class VmDeploymentManager implements DeploymentManager {
    private static final Target DEFAULT_TARGET = new TargetImpl("DefaultTarget", "OpenEJB Default Target");
    private static final DConfigBeanVersionType DCONFIG_BEAN_VERSION = DConfigBeanVersionType.V1_4;
    private static final Locale LOCALE = Locale.getDefault();

    private boolean connected = true;
    private Deployer deployer;
    private final String openejbUri;
    private boolean deployerLocal;

    public VmDeploymentManager() {
        final String openejbHome = JavaSecurityManagers.getSystemProperty("openejb.home");
        final File openejbHomeDir = new File(openejbHome);
        if (!openejbHomeDir.exists()) {
            throw new IllegalArgumentException("OpenEJB home dir does not exist: " + openejbHomeDir);
        }
        if (!openejbHomeDir.isDirectory()) {
            throw new IllegalArgumentException("OpenEJB home dir is not a directory: " + openejbHomeDir);
        }

        String openejbUri = JavaSecurityManagers.getSystemProperty("openejb.server.uri");
        if (openejbUri == null) {
            try {
                openejbUri = new URI("ejbd", null, "localhost", 4201, null, null, null).toString();
            } catch (final URISyntaxException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
        this.openejbUri = openejbUri;

    }

    private Deployer getDeployer() {
        final String deployerJndi = JavaSecurityManagers.getSystemProperty("openejb.deployer.jndiname", "openejb/DeployerBusinessRemote");

        if (deployer == null) {
            try {
                final Properties p = new Properties();
                p.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
                p.put("java.naming.provider.url", openejbUri);

                final InitialContext ctx = new InitialContext(p);
                deployer = (Deployer) ctx.lookup(deployerJndi);
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
        return deployer;
    }

    private boolean isDeployerLocal() {
        if (deployer == null) {
            deployerLocal = new File(getDeployer().getUniqueFile()).exists();
        }
        return deployerLocal;
    }

    @Override
    public void release() {
        connected = false;
    }

    @Override
    public Target[] getTargets() {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        return new Target[]{DEFAULT_TARGET};
    }


    @Override
    public TargetModuleID[] getAvailableModules(final ModuleType moduleType, final Target[] targetList) throws TargetException {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        final Set<TargetModuleID> targetModuleIds = toTargetModuleIds(getDeployer().getDeployedApps(), moduleType);
        return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
    }

    private static Set<TargetModuleID> toTargetModuleIds(final Collection<AppInfo> deployedApps, final ModuleType allowedModuleType) {
        final Set<TargetModuleID> targetModuleIds = new HashSet<>(deployedApps.size());
        for (final AppInfo deployedApp : deployedApps) {
            final TargetModuleID moduleId = toTargetModuleId(deployedApp, allowedModuleType);
            // moduleID will be null if the module was filtered
            if (moduleId != null) {
                targetModuleIds.add(moduleId);
            }
        }
        return targetModuleIds;
    }

    private static TargetModuleID toTargetModuleId(final AppInfo appInfo, final ModuleType allowedModuleType) {
        final List<InfoObject> infos = new ArrayList<>();
        infos.addAll(appInfo.clients);
        infos.addAll(appInfo.ejbJars);
        infos.addAll(appInfo.webApps);
        infos.addAll(appInfo.connectors);

        // if the module id is the same as the appInfo, then it is a standalone module
        if (infos.size() == 1) {
            final InfoObject infoObject = infos.get(0);
            if (infoObject instanceof ClientInfo) {
                final ClientInfo clientInfo = (ClientInfo) infoObject;
                if (null != appInfo.path && appInfo.path.equals(clientInfo.path)) {
                    // are client modules allowed
                    if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.CAR)) {
                        return null;
                    }
                    if (null != clientInfo.moduleId && clientInfo.moduleId.equals(appInfo.path)) {
                        return new TargetModuleIDImpl(DEFAULT_TARGET, clientInfo.moduleId);
                    }
                }
            }
            if (infoObject instanceof EjbJarInfo) {
                final EjbJarInfo ejbJarInfo = (EjbJarInfo) infoObject;
                if (null != appInfo.path && appInfo.path.equals(ejbJarInfo.path)) {
                    // are ejb modules allowed
                    if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.EJB)) {
                        return null;
                    }
                    if (null != ejbJarInfo.moduleName && ejbJarInfo.moduleName.equals(appInfo.appId)) {
                        return new TargetModuleIDImpl(DEFAULT_TARGET, ejbJarInfo.moduleName);
                    }
                }
            }
            if (infoObject instanceof ConnectorInfo) {
                final ConnectorInfo connectorInfo = (ConnectorInfo) infoObject;
                if (null != appInfo.path && appInfo.path.equals(connectorInfo.path)) {
                    // are connector modules allowed
                    if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.RAR)) {
                        return null;
                    }
                    if (null != connectorInfo.moduleId && connectorInfo.moduleId.equals(appInfo.path)) {
                        return new TargetModuleIDImpl(DEFAULT_TARGET, connectorInfo.moduleId);
                    }
                }
            }
            if (infoObject instanceof WebAppInfo) {
                final WebAppInfo webAppInfo = (WebAppInfo) infoObject;
                if (null != appInfo.path && appInfo.path.equals(webAppInfo.path)) {
                    // are web app modules allowed
                    if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.WAR)) {
                        return null;
                    }
                    if (null != webAppInfo.moduleId && webAppInfo.moduleId.equals(appInfo.path)) {
                        return new TargetModuleIDImpl(DEFAULT_TARGET, webAppInfo.moduleId); //todo web module
                    }
                }
            }
        }

        // regular ear

        // are ear modules allowed
        if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.EAR)) {
            return null;
        }

        final TargetModuleIDImpl earModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, appInfo.path);
        for (final ClientInfo clientInfo : appInfo.clients) {
            final TargetModuleIDImpl clientModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, clientInfo.moduleId);
            clientModuleId.setParentTargetModuleID(earModuleId);
        }
        for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            final TargetModuleIDImpl ejbJarModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, ejbJarInfo.moduleName);
            ejbJarModuleId.setParentTargetModuleID(earModuleId);
        }
        for (final ConnectorInfo connectorInfo : appInfo.connectors) {
            final TargetModuleIDImpl clientModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, connectorInfo.moduleId);
            clientModuleId.setParentTargetModuleID(earModuleId);
        }
        for (final WebAppInfo webAppInfo : appInfo.webApps) {
            final TargetModuleIDImpl clientModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, webAppInfo.moduleId, webAppInfo.contextRoot);
            clientModuleId.setParentTargetModuleID(earModuleId);
        }

        return earModuleId;
    }

    @Override
    public TargetModuleID[] getNonRunningModules(final ModuleType moduleType, final Target[] targetList) throws TargetException {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        return new TargetModuleIDImpl[0];
    }

    @Override
    public TargetModuleID[] getRunningModules(final ModuleType moduleType, final Target[] targetList) throws TargetException {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        final Set<TargetModuleID> targetModuleIds = toTargetModuleIds(getDeployer().getDeployedApps(), moduleType);
        return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
    }

    @Override
    public ProgressObject distribute(final Target[] targetList, final File moduleFile, final File planFile) {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        if (!isDeployerLocal()) {
            // todo when we input stream is a valid remote type we can implement this
            throw new UnsupportedOperationException("Deployment from a remote computer is not currently supproted");
        }

        // load properties
        final Properties properties = new Properties();
        if (planFile != null) {
            try {
                IO.readProperties(planFile, properties);
            } catch (final IOException ignored) {
                // no-op
            }
        }

        return deploy(targetList, properties);
    }

    @Override
    public ProgressObject distribute(final Target[] targetList, final InputStream moduleStream, final InputStream planStream) {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        // consume module stream
        if (isDeployerLocal()) {
            close(moduleStream);
        } else {
            // todo when we input stream is a valid remote type we can implement this
            throw new UnsupportedOperationException("Deployment from a remote computer is not currently supproted");
        }

        // load properties
        final Properties properties = new Properties();
        if (planStream != null) {
            try {
                properties.load(planStream);
            } catch (final IOException ignored) {
                // no-op
            } finally {
                close(planStream);
            }

        }
        return deploy(targetList, properties);
    }

    private ProgressObject deploy(final Target[] targetList, final Properties properties) {
        if (targetList == null) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, new NullPointerException("targetList is null"));
        }

        if (!containsDefaultTarget(targetList)) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, Collections.<TargetModuleID>emptySet());
        }

        try {
            final AppInfo appInfo = getDeployer().deploy(properties);
            final TargetModuleID targetModuleId = toTargetModuleId(appInfo, null);

            return new ProgressObjectImpl(CommandType.DISTRIBUTE, Collections.singleton(targetModuleId));
        } catch (ValidationFailedException e) {
            final String s = JavaSecurityManagers.getSystemProperty(ReportValidationResults.VALIDATION_LEVEL, "3");
            final int level = Integer.parseInt(s);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream(baos);
            out.println(e.getMessage());
            print(e.getErrors(), out, level);
            print(e.getFailures(), out, level);
            print(e.getWarnings(), out, level);
            out.close();
            e = new ValidationFailedException(new String(baos.toByteArray()), e);
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        } catch (final OpenEJBException e) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        }
    }

    protected void print(final ValidationException[] exceptions, final PrintStream out, final int level) {

        for (final ValidationException exception : exceptions) {
            out.print(" ");
            out.print(exception.getPrefix());
            out.print(" ... ");
            if (!(exception instanceof ValidationError)) {
                out.print(exception.getComponentName());
                out.print(": ");
            }
            out.println(exception.getMessage(level));
        }
    }


    private boolean containsDefaultTarget(final Target[] targetList) {
        for (final Target target : targetList) {
            if (DEFAULT_TARGET.equals(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ProgressObject start(final TargetModuleID[] moduleIdList) {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        final Set<TargetModuleID> deployedModules = toTargetModuleIds(getDeployer().getDeployedApps(), null);
        final Set<TargetModuleID> targetModuleIds = new HashSet<>(Arrays.asList(moduleIdList));
        targetModuleIds.retainAll(deployedModules);

        return new ProgressObjectImpl(CommandType.START, targetModuleIds);
    }

    @Override
    public ProgressObject stop(final TargetModuleID[] moduleIdList) {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        return new ProgressObjectImpl(CommandType.START, Collections.<TargetModuleID>emptySet());
    }

    @Override
    public ProgressObject undeploy(final TargetModuleID[] moduleIdList) {
        if (!connected) {
            throw new IllegalStateException("Deployment manager is disconnected");
        }

        UndeployException undeployException = null;
        final Set<TargetModuleID> results = new TreeSet<>();
        for (final TargetModuleID targetModuleId : moduleIdList) {
            try {
                getDeployer().undeploy(targetModuleId.getModuleID());
                results.add(targetModuleId);
            } catch (final UndeployException e) {
                if (undeployException == null) {
                    undeployException = e;
                }
            } catch (final NoSuchApplicationException e) {
                // app was not deployed... this should be ignored by jsr88
            }
        }

        if (undeployException == null) {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, results);
        } else {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, undeployException);
        }
    }

    @Override
    public boolean isRedeploySupported() {
        return false;
    }

    @Override
    public ProgressObject redeploy(final TargetModuleID[] moduleIDList, final File moduleArchive, final File deploymentPlan) {
        throw new UnsupportedOperationException("redeploy is not supported");
    }

    @Override
    public ProgressObject redeploy(final TargetModuleID[] moduleIDList, final InputStream moduleArchive, final InputStream deploymentPlan) {
        throw new UnsupportedOperationException("redeploy is not supported");
    }

    @Override
    public Locale[] getSupportedLocales() {
        return new Locale[]{getDefaultLocale()};
    }

    @Override
    public Locale getCurrentLocale() {
        return getDefaultLocale();
    }

    @Override
    public Locale getDefaultLocale() {
        return LOCALE;
    }

    @Override
    public boolean isLocaleSupported(final Locale locale) {
        return getDefaultLocale().equals(locale);
    }

    @Override
    public void setLocale(final Locale locale) {
        if (!isLocaleSupported(locale)) {
            throw new UnsupportedOperationException("Unsupported locale");
        }
    }

    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DCONFIG_BEAN_VERSION;
    }

    @Override
    public boolean isDConfigBeanVersionSupported(final DConfigBeanVersionType version) {
        return DCONFIG_BEAN_VERSION.equals(version);
    }

    @Override
    public void setDConfigBeanVersion(final DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    @Override
    public DeploymentConfiguration createConfiguration(final DeployableObject deployableObject) throws InvalidModuleException {
        throw new InvalidModuleException("Not supported: " + deployableObject.getType());
    }

    public String toString() {
        if (connected) {
            return "OpenEJBDeploymentManager - connected";
        } else {
            return "OpenEJBDeploymentManager - disconnected";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TargetImpl implements Target, Comparable, Serializable {
        private static final long serialVersionUID = -7257857314911948377L;
        private final String name;
        private final String description;

        public TargetImpl(final String name) {
            this(name, null);
        }

        public TargetImpl(final String name, final String description) {
            if (name == null) {
                throw new NullPointerException("name is null");
            }
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public String toString() {
            return name;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TargetImpl)) {
                return false;
            }

            final TargetImpl target = (TargetImpl) o;
            return name.equals(target.name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public int compareTo(final Object o) {
            final TargetImpl target = (TargetImpl) o;
            return name.compareTo(target.name);
        }
    }

    public static class TargetModuleIDImpl implements TargetModuleID, Comparable, Serializable {
        private static final long serialVersionUID = 2471961579457311472L;

        private final Target target;
        private final String moduleId;
        private final String webUrl;
        private TargetModuleID parentTargetModuleId;
        private final Set<TargetModuleID> children = new TreeSet<>();

        public TargetModuleIDImpl(final Target target, final String moduleId) {
            this(target, moduleId, null);
        }

        public TargetModuleIDImpl(final Target target, final String moduleId, String webUrl) {
            if (target == null) {
                throw new NullPointerException("target is null");
            }
            if (moduleId == null) {
                throw new NullPointerException("moduleId is null");
            }
            this.target = target;
            this.moduleId = moduleId;
            if (webUrl != null && !webUrl.startsWith("http:")) {
                webUrl = "http://localhost:8080/" + webUrl;
            }
            this.webUrl = webUrl;
        }

        @Override
        public Target getTarget() {
            return target;
        }

        @Override
        public String getModuleID() {
            return moduleId;
        }

        @Override
        public TargetModuleID getParentTargetModuleID() {
            return parentTargetModuleId;
        }

        public void setParentTargetModuleID(final TargetModuleIDImpl parentTargetModuleId) {
            this.parentTargetModuleId = parentTargetModuleId;
            parentTargetModuleId.children.add(this);
        }

        @Override
        public TargetModuleID[] getChildTargetModuleID() {
            return children.toArray(new TargetModuleID[children.size()]);
        }

        @Override
        public String getWebURL() {
            return webUrl;
        }

        public String toString() {
            return target + "/" + moduleId + (webUrl == null ? " " : webUrl);
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TargetModuleIDImpl)) {
                return false;
            }

            final TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl) o;
            return target.equals(targetModuleID.target) &&
                moduleId.equals(targetModuleID.moduleId);
        }

        public int hashCode() {
            int result;
            result = target.hashCode();
            result = 29 * result + moduleId.hashCode();
            return result;
        }

        @Override
        public int compareTo(final Object o) {
            final TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl) o;

            // compare target name
            final int val = target.getName().compareTo(targetModuleID.target.getName());
            if (val != 0) {
                return val;
            }

            // compare moduleId
            return moduleId.compareTo(targetModuleID.moduleId);
        }
    }

    public class ProgressObjectImpl implements ProgressObject {
        private final Set<TargetModuleID> targetModuleIds;
        private final ProgressEvent event;
        private final DeploymentStatus deploymentStatus;

        public ProgressObjectImpl(final CommandType command, final Set<TargetModuleID> targetModuleIds) {
            this.targetModuleIds = targetModuleIds;
            deploymentStatus = new DeploymentStatusImpl(command);
            event = new ProgressEvent(this, null, deploymentStatus);
        }

        public ProgressObjectImpl(final CommandType command, final Exception exception) {
            this.targetModuleIds = null;
            deploymentStatus = new DeploymentStatusImpl(command, exception);
            event = new ProgressEvent(this, null, deploymentStatus);
        }

        @Override
        public synchronized TargetModuleID[] getResultTargetModuleIDs() {
            if (targetModuleIds == null) {
                return new TargetModuleID[0];
            }
            return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
        }

        @Override
        public synchronized DeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }

        @Override
        public ClientConfiguration getClientConfiguration(final TargetModuleID id) {
            return null;
        }

        @Override
        public boolean isCancelSupported() {
            return false;
        }

        @Override
        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("cancel is not supported");
        }

        @Override
        public boolean isStopSupported() {
            return false;
        }

        @Override
        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("stop is not supported");
        }

        @Override
        public void addProgressListener(final ProgressListener pol) {
            pol.handleProgressEvent(event);
        }

        @Override
        public void removeProgressListener(final ProgressListener pol) {
        }

    }

    public static class DeploymentStatusImpl implements DeploymentStatus {
        private final CommandType command;
        private final StateType state;
        private final String message;

        public DeploymentStatusImpl(final CommandType command) {
            this.command = command;
            this.state = StateType.COMPLETED;
            this.message = null;
        }

        public DeploymentStatusImpl(final CommandType command, final Exception exception) {
            this.command = command;
            this.state = StateType.FAILED;

            final StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer, true));
            this.message = writer.toString();
        }

        @Override
        public CommandType getCommand() {
            return command;
        }

        @Override
        public ActionType getAction() {
            return ActionType.EXECUTE;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public StateType getState() {
            return state;
        }

        @Override
        public boolean isRunning() {
            return StateType.RUNNING.equals(state);
        }

        @Override
        public boolean isCompleted() {
            return StateType.COMPLETED.equals(state);
        }

        @Override
        public boolean isFailed() {
            return StateType.FAILED.equals(state);
        }

        public String toString() {
            final StringBuilder buf = new StringBuilder();
            buf.append("DeploymentStatus[").append(command).append(',');
            buf.append(state);
            if (message != null) {
                buf.append(',').append(message);
            }
            buf.append(']');
            return buf.toString();
        }
    }
}
