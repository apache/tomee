/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.config;

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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class VmDeploymentManager implements DeploymentManager {
    public static final String MODULE_TYPE = "moduleType";
    public static final String MODULE_ID = "moduleId";
    public static final String FILENAME = "filename";

    private static final Target DEFAULT_TARGET = new TargetImpl("DefaultTarget", null);
    private static final DConfigBeanVersionType DCONFIG_BEAN_VERSION = DConfigBeanVersionType.V1_4;
    private static final Locale LOCALE = Locale.getDefault();

    private boolean connected = true;
    private final SortedMap<TargetModuleID, Properties> deployed = new TreeMap<TargetModuleID, Properties>();
    private final SortedSet<TargetModuleID> running = new TreeSet<TargetModuleID>();
    private final File beansDir;
    private final File appsDir;
    private RemoteServer remoteServer;

    public VmDeploymentManager() {
        String openejbHome = System.getProperty("openejb.home", "target/openejb-3.0-incubating-SNAPSHOT");
        File openejbHomeDir = new File(openejbHome);
        assertIsDirectory(openejbHomeDir, "beans");
        beansDir = new File(openejbHomeDir, "beans");
        assertIsDirectory(beansDir, "beans");
        appsDir = new File(openejbHomeDir, "apps");

        remoteServer = new RemoteServer();
    }

    private static void assertIsDirectory(File beansDir, String variableName) {
        if (!beansDir.exists()) {
            throw new IllegalArgumentException(variableName + " dir does not exist: " + beansDir);
        }
        if (!beansDir.isDirectory()) {
            throw new IllegalArgumentException(variableName + " dir is not a directory: " + beansDir);
        }
    }

    public void release() {
        connected = false;
        remoteServer.destroy();
        remoteServer = null;
    }

    public Target[] getTargets() {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        return new Target[] { DEFAULT_TARGET };
    }


    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        Set<TargetModuleID> modules = new HashSet<TargetModuleID>(deployed.keySet());
        filterModulesByType(modules, moduleType);
        return modules.toArray(new TargetModuleID[modules.size()]);
    }

    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        Set<TargetModuleID> modules = new HashSet<TargetModuleID>(deployed.keySet());
        modules.removeAll(running);
        filterModulesByType(modules, moduleType);
        return modules.toArray(new TargetModuleID[modules.size()]);
    }

    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] targetList) throws TargetException {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        if (!containsDefaultTarget(targetList)) {
            return null;
        }

        Set<TargetModuleID> modules = new HashSet<TargetModuleID>(running);
        filterModulesByType(modules, moduleType);
        return modules.toArray(new TargetModuleID[modules.size()]);
    }

    private Set<TargetModuleID> filterModulesByType(Set<TargetModuleID> modules, ModuleType moduleType) {
        for (Iterator<TargetModuleID> iterator = modules.iterator(); iterator.hasNext();) {
            TargetModuleID moduleID = iterator.next();
            if (!isModuleType(moduleID, moduleType)) {
                iterator.remove();
            }
        }
        return modules;
    }

    private boolean isModuleType(TargetModuleID moduleID, ModuleType moduleType) {
        if (moduleType == null) return true;

        Properties properties = deployed.get(moduleID);
        String typeString = properties.getProperty(MODULE_TYPE);
        return moduleType.toString().equalsIgnoreCase(typeString);
    }

    public ProgressObject distribute(Target[] targetList, File moduleFile, File planFile) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

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
        try {
            byte[] buf = new byte[4096];
            while (moduleStream.read(buf) >= 0);
        } catch (IOException ignored) {
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

        File source = new File(properties.getProperty(FILENAME));
        String moduleId = source.getName().substring(source.getName().lastIndexOf('.'));
        
        TargetModuleID moduleID = new TargetModuleIDImpl(DEFAULT_TARGET, moduleId);
        deployed.put(moduleID, properties);

        File destination = new File(beansDir, source.getName());
        try {
            copyFile(source, destination);
        } catch (IOException e) {
            return new ProgressObjectImpl(CommandType.DISTRIBUTE, e);
        }

        return new ProgressObjectImpl(CommandType.DISTRIBUTE, Collections.singleton(moduleID));
    }

    private boolean containsDefaultTarget(Target[] targetList) {
        for (Target target : targetList) {
            if  (DEFAULT_TARGET.equals(target)) return true;
        }
        return false;
    }

    public ProgressObject start(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        Map<TargetModuleID, Properties> toStart = new LinkedHashMap<TargetModuleID, Properties>();
        for (TargetModuleID moduleID : moduleIdList) {
            if (!running.contains(moduleID)) {
                Properties properties = deployed.get(moduleID);
                if (properties != null) {
                    toStart.put(moduleID, properties);
                }
            }
        }

        try {
            start(toStart);
            Set<TargetModuleID> results = new TreeSet<TargetModuleID>(toStart.keySet());
            running.addAll(results);
            return new ProgressObjectImpl(CommandType.START, results);
        } catch (Exception e) {
            return new ProgressObjectImpl(CommandType.START, e);
        }
    }

    private void start(Map<TargetModuleID, Properties> moduleIdList) {
        remoteServer.start();
//        for (Iterator<Map.Entry<TargetModuleID, Properties>> iterator = moduleIdList.entrySet().iterator(); iterator.hasNext();) {
//            Map.Entry<TargetModuleID, Properties> entry = iterator.next();
//            TargetModuleID module = entry.getKey();
//            Properties properties =  entry.getValue();
//            File source = new File(properties.getProperty(FILENAME));
//            File destination = new File(beansDir, source.getName());
//            copyFile(source, destination);
//        }
    }

    public ProgressObject stop(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        Map<TargetModuleID, Properties> toStop = new LinkedHashMap<TargetModuleID, Properties>();
        for (TargetModuleID moduleID : moduleIdList) {
            if (running.contains(moduleID)) {
                Properties properties = deployed.get(moduleID);
                if (properties != null) {
                    toStop.put(moduleID, properties);
                }
            }
        }

        try {
            stop(toStop);
            Set<TargetModuleID> results = new TreeSet<TargetModuleID>(toStop.keySet());
            running.removeAll(results);
            return new ProgressObjectImpl(CommandType.STOP, results);
        } catch (Exception e) {
            return new ProgressObjectImpl(CommandType.STOP, e);
        }
    }

    private void stop(Map<TargetModuleID, Properties> moduleIdList) {
        remoteServer.stop();
        for (Iterator<Map.Entry<TargetModuleID, Properties>> iterator = moduleIdList.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<TargetModuleID, Properties> entry = iterator.next();
            TargetModuleID module = entry.getKey();
            Properties properties =  entry.getValue();
            String filename = properties.getProperty(FILENAME);
            System.out.println("STOPPED " + module.getModuleID());
            System.out.println("        " + filename);
        }
    }

    public ProgressObject undeploy(TargetModuleID[] moduleIdList) {
        if (!connected) throw new IllegalStateException("Deployment manager is disconnected");

        Map<TargetModuleID, Properties> toUndeploy = new LinkedHashMap<TargetModuleID, Properties>();
        for (TargetModuleID moduleID : moduleIdList) {
            Properties properties = deployed.get(moduleID);
            if (properties != null) {
                toUndeploy.put(moduleID, properties);
            }
        }


        try {
            undeploy(toUndeploy);
            Set<TargetModuleID> results = new TreeSet<TargetModuleID>(toUndeploy.keySet());
            running.removeAll(results);
            return new ProgressObjectImpl(CommandType.UNDEPLOY, results);
        } catch (Exception e) {
            return new ProgressObjectImpl(CommandType.UNDEPLOY, e);
        }
    }

    private void undeploy(Map<TargetModuleID, Properties> moduleIdList) {
        for (Iterator<Map.Entry<TargetModuleID, Properties>> iterator = moduleIdList.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<TargetModuleID, Properties> entry = iterator.next();
            TargetModuleID module = entry.getKey();
            Properties properties =  entry.getValue();
            String fileName = new File(properties.getProperty(FILENAME)).getName();
            File destination = new File(beansDir, fileName);
            destination.delete();
            File unpackedDir = new File(appsDir, fileName.substring(0, fileName.lastIndexOf('.')));
            recursiveDelete(unpackedDir);
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

    private static void copyFile(File source, File destination) throws IOException {
        File destinationDir = destination.getParentFile();
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new java.io.IOException("Cannot create directory : " + destinationDir);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            writeAll(in, out);
        } finally {
            close(in);
            close(out);
        }
    }

    private static void writeAll(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    private static void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static boolean recursiveDelete(File root) {
        if (root == null) {
            return true;
        }

        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return root.delete();
    }
}
