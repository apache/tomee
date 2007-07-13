/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
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

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.InfoObject;
import org.apache.openejb.assembler.Deployer;

public class VmDeploymentManager implements DeploymentManager {
    private static final Target DEFAULT_TARGET = new TargetImpl("DefaultTarget", "OpenEJB Default Target");
    private static final DConfigBeanVersionType DCONFIG_BEAN_VERSION = DConfigBeanVersionType.V1_4;
    private static final Locale LOCALE = Locale.getDefault();

    private boolean connected = true;
    private Deployer deployer;
    private final String openejbUri;
    private boolean deployerLocal;

    public VmDeploymentManager() {
        String openejbHome = System.getProperty("openejb.home", "target/openejb-3.0.0-SNAPSHOT");
        File openejbHomeDir = new File(openejbHome);
        if (!openejbHomeDir.exists()) {
            throw new IllegalArgumentException("OpenEJB home dir does not exist: " + openejbHomeDir);
        }
        if (!openejbHomeDir.isDirectory()) {
            throw new IllegalArgumentException("OpenEJB home dir is not a directory: " + openejbHomeDir);
        }

        String openejbUri = System.getProperty("openejb.server.uri");
        if (openejbUri == null) {
            try {
                openejbUri = new URI("ejb", null, "localhost", 4201, null, null, null).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        this.openejbUri = openejbUri;

    }

    private Deployer getDeployer() {
        if (deployer == null) {
            try {
                Properties p = new Properties();
                p.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
                p.put("java.naming.provider.url", openejbUri);

                InitialContext ctx = new InitialContext(p);
                deployer = (Deployer) ctx.lookup("openejb/DeployerBusinessRemote");
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    public void release() {
        connected = false;
    }

    public Target[] getTargets() {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        return new Target[]{DEFAULT_TARGET};
    }


    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        Set<TargetModuleID> targetModuleIds = toTargetModuleIds(getDeployer().getDeployedApps(), moduleType);
        return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
    }

    private static Set<TargetModuleID> toTargetModuleIds(Collection<AppInfo> deployedApps, ModuleType allowedModuleType) {
        Set<TargetModuleID> targetModuleIds = new HashSet<TargetModuleID>(deployedApps.size());
        for (AppInfo deployedApp : deployedApps) {
            TargetModuleID moduleId = toTargetModuleId(deployedApp, allowedModuleType);
            // moduleID will be null if the module was filtered
            if (moduleId != null) {
                targetModuleIds.add(moduleId);
            }
        }
        return targetModuleIds;
    }

    private static TargetModuleID toTargetModuleId(AppInfo appInfo, ModuleType allowedModuleType) {
        List<InfoObject> infos = new ArrayList<InfoObject>();
        infos.addAll(appInfo.clients);
        infos.addAll(appInfo.ejbJars);

        // if the module id is the same as the appInfo, then it is a standalone module
        if (infos.size() == 1) {
            InfoObject infoObject = infos.get(0);
            if (infoObject instanceof ClientInfo) {
                // are client modules allowed
                if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.CAR)) {
                    return null;
                }
                ClientInfo clientInfo = (ClientInfo) infoObject;
                if (clientInfo.moduleId == appInfo.jarPath) {
                    return new TargetModuleIDImpl(DEFAULT_TARGET, clientInfo.moduleId);
                }
            }
            if (infoObject instanceof EjbJarInfo) {
                // are ejb modules allowed
                if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.EJB)) {
                    return null;
                }
                EjbJarInfo ejbJarInfo = (EjbJarInfo) infoObject;
                if (ejbJarInfo.moduleId == appInfo.jarPath) {
                    return new TargetModuleIDImpl(DEFAULT_TARGET, ejbJarInfo.moduleId);
                }
            }
        }

        // regular ear

        // are ear modules allowed
        if (allowedModuleType != null && !allowedModuleType.equals(ModuleType.EAR)) {
            return null;
        }

        TargetModuleIDImpl earModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, appInfo.jarPath);
        for (ClientInfo clientInfo : appInfo.clients) {
            TargetModuleIDImpl clientModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, clientInfo.moduleId);
            clientModuleId.setParentTargetModuleID(earModuleId);
        }
        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
            TargetModuleIDImpl ejbJarModuleId = new TargetModuleIDImpl(DEFAULT_TARGET, ejbJarInfo.moduleId);
            ejbJarModuleId.setParentTargetModuleID(earModuleId);
        }

        return earModuleId;
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        return new TargetModuleIDImpl[0];
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        Set<TargetModuleID> targetModuleIds = toTargetModuleIds(getDeployer().getDeployedApps(), moduleType);
        return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
    }

    public ProgressObject distribute(Target[] targetList, File moduleFile, File planFile) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!isDeployerLocal()) {
            // todo when we input stream is a valid remote type we can implement this
            throw new UnsupportedOperationException("Deployment from a remote computer is not currently supproted");
        }

        // load properties
        Properties properties = new Properties();
        if (planFile != null) {
            InputStream in = null;
            try {
                in = new FileInputStream(planFile);
                properties.load(in);
            } catch (IOException ignored) {
            } finally {
                close(in);
            }

        }

        ProgressObject progressObject = deploy(targetList, properties);
        return progressObject;
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleStream, InputStream planStream) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        // consume module stream
        if (isDeployerLocal()) {
            close(moduleStream);
        } else {
            // todo when we input stream is a valid remote type we can implement this
            throw new UnsupportedOperationException("Deployment from a remote computer is not currently supproted");
        }

        // load properties
        Properties properties = new Properties();
        if (planStream != null) {
            try {
                properties.load(planStream);
            } catch (IOException ignored) {
            } finally {
                close(planStream);
            }

        }
        ProgressObject progressObject = deploy(targetList, properties);
        return progressObject;
    }

    private ProgressObject deploy(Target[] targetList, Properties properties) {
        if (targetList == null) return new ProgressObjectImpl(CommandType.DISTRIBUTE, new NullPointerException("targetList is null"));

        if (!containsDefaultTarget(targetList)) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, Collections.<TargetModuleID>emptySet());
        }

        try {
            AppInfo appInfo = getDeployer().deploy(properties);
            TargetModuleID targetModuleId = toTargetModuleId(appInfo, null);

            return new ProgressObjectImpl(CommandType.DISTRIBUTE, Collections.singleton(targetModuleId));
        } catch (OpenEJBException e) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        }
    }

    private boolean containsDefaultTarget(Target[] targetList) {
        for (Target target : targetList) {
            if (DEFAULT_TARGET.equals(target)) return true;
        }
        return false;
    }

    public ProgressObject start(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        Set<TargetModuleID> deployedModules = toTargetModuleIds(getDeployer().getDeployedApps(), null);
        Set<TargetModuleID> targetModuleIds = new HashSet<TargetModuleID>(Arrays.asList(moduleIdList));
        targetModuleIds.retainAll(deployedModules);

        return new ProgressObjectImpl(CommandType.START, targetModuleIds);
    }

    public ProgressObject stop(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        return new ProgressObjectImpl(CommandType.START, Collections.<TargetModuleID>emptySet());
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        UndeployException undeployException = null;
        Set<TargetModuleID> results = new TreeSet<TargetModuleID>();
        for (TargetModuleID targetModuleId : moduleIdList) {
            try {
                getDeployer().undeploy(targetModuleId.getModuleID());
                results.add(targetModuleId);
            } catch (UndeployException e) {
                if (undeployException == null) {
                    undeployException = e;
                }
            } catch (NoSuchApplicationException e) {
                // app was not deployed... this should be ignored by jsr88
            }
        }

        if (undeployException == null) {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, results);
        } else {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, undeployException);
        }
    }

    public boolean isRedeploySupported() {
        return false;
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, File moduleArchive, File deploymentPlan) {
        throw new UnsupportedOperationException("redeploy is not supported");
    }

    public ProgressObject redeploy(TargetModuleID[] moduleIDList, InputStream moduleArchive, InputStream deploymentPlan) {
        throw new UnsupportedOperationException("redeploy is not supported");
    }

    public Locale[] getSupportedLocales() {
        return new Locale[]{getDefaultLocale()};
    }

    public Locale getCurrentLocale() {
        return getDefaultLocale();
    }

    public Locale getDefaultLocale() {
        return LOCALE;
    }

    public boolean isLocaleSupported(Locale locale) {
        return getDefaultLocale().equals(locale);
    }

    public void setLocale(Locale locale) {
        if (!isLocaleSupported(locale)) {
            throw new UnsupportedOperationException("Unsupported locale");
        }
    }

    public DConfigBeanVersionType getDConfigBeanVersion() {
        return DCONFIG_BEAN_VERSION;
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return DCONFIG_BEAN_VERSION.equals(version);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        throw new InvalidModuleException("Not supported: " + deployableObject.getType());
    }

    public String toString() {
        if (connected) {
            return "OpenEJBDeploymentManager - connected";
        } else {
            return "OpenEJBDeploymentManager - disconnected";
        }
    }

    public static class TargetImpl implements Target, Comparable, Serializable {
        private static final long serialVersionUID = -7257857314911948377L;
        private final String name;
        private final String description;

        public TargetImpl(String name) {
            this(name, null);
        }

        public TargetImpl(String name, String description) {
            if (name == null) throw new NullPointerException("name is null");
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String toString() {
            return name;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TargetImpl)) return false;

            TargetImpl target = (TargetImpl) o;
            return name.equals(target.name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public int compareTo(Object o) {
            TargetImpl target = (TargetImpl) o;
            return name.compareTo(target.name);
        }
    }

    public static class TargetModuleIDImpl implements TargetModuleID, Comparable, Serializable {
        private static final long serialVersionUID = 2471961579457311472L;

        private final Target target;
        private final String moduleId;
        private final String webUrl;
        private TargetModuleID parentTargetModuleId;
        private Set<TargetModuleID> children = new TreeSet<TargetModuleID>();

        public TargetModuleIDImpl(Target target, String moduleId) {
            this(target, moduleId, null);
        }

        public TargetModuleIDImpl(Target target, String moduleId, String webUrl) {
            if (target == null) throw new NullPointerException("target is null");
            if (moduleId == null) throw new NullPointerException("moduleId is null");
            this.target = target;
            this.moduleId = moduleId;
            this.webUrl = webUrl;
        }

        public Target getTarget() {
            return target;
        }

        public String getModuleID() {
            return moduleId;
        }

        public TargetModuleID getParentTargetModuleID() {
            return parentTargetModuleId;
        }

        public void setParentTargetModuleID(TargetModuleIDImpl parentTargetModuleId) {
            this.parentTargetModuleId = parentTargetModuleId;
            parentTargetModuleId.children.add(this);
        }

        public TargetModuleID[] getChildTargetModuleID() {
            return children.toArray(new TargetModuleID[children.size()]);
        }

        public String getWebURL() {
            return webUrl;
        }

        public String toString() {
            return target + "/" + moduleId + (webUrl == null ? " " : webUrl);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TargetModuleIDImpl)) return false;

            TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl) o;
            return target.equals(targetModuleID.target) &&
                    moduleId.equals(targetModuleID.moduleId);
        }

        public int hashCode() {
            int result;
            result = target.hashCode();
            result = 29 * result + moduleId.hashCode();
            return result;
        }

        public int compareTo(Object o) {
            TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl) o;

            // compare target name
            int val = target.getName().compareTo(targetModuleID.target.getName());
            if (val != 0) return val;

            // compare moduleId
            return moduleId.compareTo(targetModuleID.moduleId);
        }
    }

    public class ProgressObjectImpl implements ProgressObject {
        private final Set<TargetModuleID> targetModuleIds;
        private final ProgressEvent event;
        private final DeploymentStatus deploymentStatus;

        public ProgressObjectImpl(CommandType command, Set<TargetModuleID> targetModuleIds) {
            this.targetModuleIds = targetModuleIds;
            deploymentStatus = new DeploymentStatusImpl(command);
            event = new ProgressEvent(this, null, deploymentStatus);
        }

        public ProgressObjectImpl(CommandType command, Exception exception) {
            this.targetModuleIds = null;
            deploymentStatus = new DeploymentStatusImpl(command, exception);
            event = new ProgressEvent(this, null, deploymentStatus);
        }

        public synchronized TargetModuleID[] getResultTargetModuleIDs() {
            if (targetModuleIds == null) return new TargetModuleID[0];
            return targetModuleIds.toArray(new TargetModuleID[targetModuleIds.size()]);
        }

        public synchronized DeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID id) {
            return null;
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("cancel is not supported");
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("stop is not supported");
        }

        public void addProgressListener(ProgressListener pol) {
            pol.handleProgressEvent(event);
        }

        public void removeProgressListener(ProgressListener pol) {
        }

    }

    public static class DeploymentStatusImpl implements DeploymentStatus {
        private final CommandType command;
        private final StateType state;
        private final String message;

        public DeploymentStatusImpl(CommandType command) {
            this.command = command;
            this.state = StateType.COMPLETED;
            this.message = null;
        }

        public DeploymentStatusImpl(CommandType command, Exception exception) {
            this.command = command;
            this.state = StateType.FAILED;

            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer, true));
            this.message = writer.toString();
        }

        public CommandType getCommand() {
            return command;
        }

        public ActionType getAction() {
            return ActionType.EXECUTE;
        }

        public String getMessage() {
            return message;
        }

        public StateType getState() {
            return state;
        }

        public boolean isRunning() {
            return StateType.RUNNING.equals(state);
        }

        public boolean isCompleted() {
            return StateType.COMPLETED.equals(state);
        }

        public boolean isFailed() {
            return StateType.FAILED.equals(state);
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("DeploymentStatus[").append(command).append(',');
            buf.append(state);
            if (message != null) {
                buf.append(',').append(message);
            }
            buf.append(']');
            return buf.toString();
        }
    }

    private static void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }
}
