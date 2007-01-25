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
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class OpenEJBDeploymentManager implements DeploymentManager {
    public static final DConfigBeanVersionType DEFAULT_DCONFIG_BEAN_VERSION = DConfigBeanVersionType.V1_4;
    public static final String DEFAULT_TARGET_NAME = "DefaultTarget";

    private Deployment deployment;
    private final Locale locale;
    private final DConfigBeanVersionType dconfigBeanVersion;
    private final Target defaultTarget;
    private final SortedMap<String, Target> targets;
    private final List<String> targetPaths;

    public OpenEJBDeploymentManager() {
        locale = Locale.getDefault();
        dconfigBeanVersion = OpenEJBDeploymentManager.DEFAULT_DCONFIG_BEAN_VERSION;
        defaultTarget = null;
        targets = null;
        targetPaths = null;
    }

    public OpenEJBDeploymentManager(Deployment deployment) throws DeploymentManagerCreationException {
        this.deployment = deployment;
        Properties properties = deployment.getProperties();

        // locale - local used by the server
        String localeString = properties.getProperty("locale");
        if (localeString != null) {
            locale = new Locale(localeString);
        } else {
            locale = Locale.getDefault();
        }

        // dconfig.bean.version - dconfig bean version supported by the server
        String dconfigBeanVersionString = properties.getProperty("locale");
        dconfigBeanVersion = OpenEJBDeploymentManager.parseDConfigBeanVersionType(dconfigBeanVersionString);

        // target.* - known targets available on the server
        // target.*.description - known targets available on the server
        SortedMap<String, Target> targets = new TreeMap<String, Target>();
        for (Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = iterator.next();
            String key = (String) entry.getKey();
            String targetName = (String) entry.getValue();
            if (key.startsWith("target.") && !key.endsWith(".description")) {
                String targetDescription = properties.getProperty(key + ".description");
                TargetImpl target = new TargetImpl(targetName, targetDescription);
                targets.put(targetName, target);
            }
        }

        // target.default - default target
        String defaultTargetName = properties.getProperty("target.default");
        if (defaultTargetName == null) defaultTargetName = OpenEJBDeploymentManager.DEFAULT_TARGET_NAME;
        if (!targets.containsKey(defaultTargetName)) {
            targets.put(defaultTargetName, new TargetImpl(defaultTargetName, null));
        }
        defaultTarget = targets.get(defaultTargetName);

        this.targets = Collections.unmodifiableSortedMap(targets);

        targetPaths = new ArrayList<String>();
        for (String targetName : targets.keySet()) {
            targetPaths.add(targetName + "/");
        }
        Collections.reverse(targetPaths);
    }

    public void release() {
        if (deployment != null) {
            deployment.release();
            deployment = null;
        }
    }

    public Target[] getTargets() {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        return targets.values().toArray(new Target[targets.size()]);
    }


    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            String type = null;
            if (type != null) {
                moduleType.toString();
            }
            Set<String> targetModulesStrings = deployment.list(type, null, toTargetSet(targetList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModulesStrings);
            return targetModules.toArray(new TargetModuleID[targetModules.size()]);
        } catch (DeploymentException e) {
            throw new IllegalStateException("DeployerException while listing deployments", e);
        }
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            String type = null;
            if (type != null) {
                moduleType.toString();
            }
            Set<String> targetModulesStrings = deployment.list(type, "stopped", toTargetSet(targetList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModulesStrings);
            return targetModules.toArray(new TargetModuleID[targetModules.size()]);
        } catch (DeploymentException e) {
            throw new IllegalStateException("DeployerException while listing deployments", e);
        }
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            String type = null;
            if (type != null) {
                moduleType.toString();
            }
            Set<String> targetModulesStrings = deployment.list(type, "running", toTargetSet(targetList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModulesStrings);
            return targetModules.toArray(new TargetModuleID[targetModules.size()]);
        } catch (DeploymentException e) {
            throw new IllegalStateException("DeployerException while listing deployments", e);
        }
    }

    public ProgressObject distribute(Target[] targetList, File moduleArchive, File deploymentPlan) {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        // todo merge files
        try {
            Set<String> targetModuleStrings = deployment.deploy(toTargetSet(targetList), moduleArchive);
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModuleStrings);
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, targetModules);
        } catch (DeploymentException e) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        }
    }

    public ProgressObject distribute(Target[] targetList, InputStream moduleArchive, InputStream deploymentPlan) {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        // todo merge files
        try {
            Set<String> targetModuleStrings = deployment.deploy(toTargetSet(targetList), null);
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModuleStrings);
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, targetModules);
        } catch (DeploymentException e) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        }
    }

    public ProgressObject start(TargetModuleID[] moduleIdList) {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            Set<String> targetModuleStrings = deployment.start(toModuleSet(moduleIdList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModuleStrings);
            return new ProgressObjectImpl(CommandType.START, targetModules);
        } catch (DeploymentException e) {
            return new ProgressObjectImpl(CommandType.START, e);
        }
    }

    public ProgressObject stop(TargetModuleID[] moduleIdList) {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            Set<String> targetModuleStrings = deployment.stop(toModuleSet(moduleIdList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModuleStrings);
            return new ProgressObjectImpl(CommandType.STOP, targetModules);
        } catch (DeploymentException e) {
            return new ProgressObjectImpl(CommandType.STOP, e);
        }
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIdList) {
        if (deployment == null) throw new IllegalStateException("Deployment manager is disconnected");

        try {
            Set<String> targetModuleStrings = deployment.undeploy(toModuleSet(moduleIdList));
            Set<TargetModuleID> targetModules = toTargetModuleIds(targetModuleStrings);
            return new ProgressObjectImpl(CommandType.UNDEPLOY, targetModules);
        } catch (DeploymentException e) {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, e);
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
        return locale;
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
        return dconfigBeanVersion;
    }

    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return dconfigBeanVersion.equals(version);
    }

    public void setDConfigBeanVersion(DConfigBeanVersionType version) throws DConfigBeanVersionUnsupportedException {
        if (!isDConfigBeanVersionSupported(version)) {
            throw new DConfigBeanVersionUnsupportedException("Version not supported " + version);
        }
    }

    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        throw new InvalidModuleException("Not supported: " + deployableObject.getType());
    }

    private Target getTargetFor(String moduleId) {
        for (String targetName : targetPaths) {
            if (moduleId.startsWith(moduleId)) {
                return targets.get(targetName);
            }
        }
        return null;
    }

    private Set<TargetModuleID> toTargetModuleIds(Set<String> modules) {
        Set<TargetModuleID> targetModuleIds = new HashSet<TargetModuleID>();
        for (String module : modules) {
            String moduleId;
            String webUrl;

            int spaceIndex = module.indexOf(' ');
            if (spaceIndex > 1) {
                moduleId = module.substring(0, spaceIndex);
                webUrl = module.substring(spaceIndex + 1);
            } else {
                moduleId = module;
                webUrl = null;
            }

            Target target = getTargetFor(moduleId);
            if (target != null) {
                if (moduleId.startsWith(target.getName())) {
                    moduleId = moduleId.substring(target.getName().length());
                }
            } else {
                target = defaultTarget;
            }

            TargetModuleIDImpl targetModuleID = new TargetModuleIDImpl(target, moduleId, webUrl);
            targetModuleIds.add(targetModuleID);
        }

        // todo link children

        return targetModuleIds;
    }

    private Set<String> toTargetSet(Target[] targets) {
        if (targets == null) return Collections.emptySet();

        TreeSet<String> targetSet = new TreeSet<String>();
        for (Target target : targets) {
            targetSet.add(target.getName());
        }
        return targetSet;
    }

    private Set<String> toModuleSet(TargetModuleID[] moduleIDList) {
        if (moduleIDList == null) return Collections.emptySet();

        TreeSet<String> moduleSet = new TreeSet<String>();
        for (TargetModuleID module : moduleIDList) {
            moduleSet.add(module.getTarget().getName() + "/" + module.getModuleID());
        }
        return moduleSet;
    }

    public static DConfigBeanVersionType parseDConfigBeanVersionType(String string) throws DeploymentManagerCreationException {
        if (string == null) {
            return OpenEJBDeploymentManager.DEFAULT_DCONFIG_BEAN_VERSION;
        }
        try {
            Field field = DConfigBeanVersionType.class.getField(string);
            if (field.getType().equals(DConfigBeanVersionType.class) && Modifier.isStatic(field.getModifiers())) {
                return (DConfigBeanVersionType) field.get(null);
            }
        } catch (Exception e) {
        }
        throw new DeploymentManagerCreationException("Unknown DConfig bean version: " + string);
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
}
