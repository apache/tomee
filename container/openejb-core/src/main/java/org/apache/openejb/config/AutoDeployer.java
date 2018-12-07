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

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.event.ContainerSystemPostCreate;
import org.apache.openejb.assembler.classic.event.ContainerSystemPreDestroy;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class AutoDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, AutoDeployer.class);
    private static final Semaphore SEMAPHORE = new Semaphore(1, true);

    private final ConfigurationFactory factory;
    private final long pollIntervalMillis;
    private final Map<String, FileInfo> files = new HashMap<>();
    private final Timer timer;
    private final List<Deployments> deployments = new ArrayList<>();

    public AutoDeployer(final ConfigurationFactory factory, final List<Deployments> deployments) {
        final Options options = SystemInstance.get().getOptions();
        final Duration interval = options.get("openejb.autodeploy.interval", new Duration(2, TimeUnit.SECONDS));

        if (interval.getUnit() == null) {
            interval.setUnit(TimeUnit.SECONDS);
        }

        this.factory = factory;
        this.deployments.addAll(deployments);
        this.pollIntervalMillis = interval.getUnit().toMillis(interval.getTime());
        this.timer = new Timer(this.getClass().getSimpleName(), true);
    }

    private boolean fileAdded(final File file) {
        final String appPath = file.getAbsolutePath();
        logger.info("Starting Auto-Deployment of: " + appPath);

        try {

            final AppInfo appInfo = factory.configureApplication(file);
            appInfo.paths.add(appPath);
            appInfo.paths.add(appInfo.path);

            if (logger.isDebugEnabled()) {
                for (final String path : appInfo.paths) {
                    logger.debug("Auto-Deployment path: " + path);
                }
            }

            final Assembler assembler = getAssembler();

            if (null == assembler) {
                throw new Exception("Assembler is not available for Auto-Deployment of: " + appPath);
            }

            assembler.createApplication(appInfo);

            // war can be unpacked so it changes the last modified time
            files.get(appPath).setModified(getLastModifiedInDir(new File(appPath)));

        } catch (final Exception e) {
            logger.error("Failed Auto-Deployment of: " + appPath, e);
        }

        return true;
    }

    private static Assembler getAssembler() {
        return SystemInstance.get().getComponent(Assembler.class);
    }

    private static boolean fileRemoved(final File file) {

        if (null == file) {
            return true;
        }

        final String path = file.getAbsolutePath();
        final Assembler assembler = getAssembler();

        if (null != assembler) {

            final Collection<AppInfo> apps = assembler.getDeployedApplications();

            for (final AppInfo app : apps) {

                if (app.paths.contains(path)) {

                    logger.info("Starting Auto-Undeployment of: " + app.appId + " - " + file.getAbsolutePath());

                    try {
                        assembler.destroyApplication(app);

                        for (final String location : app.paths) {
                            if (new File(location).equals(file)) {
                                continue;
                            }

                            final File delete = new File(location.replace("%20", " ").replace("%23", "#"));

                            for (int i = 0; i < 3; i++) {
                                try {
                                    Files.remove(delete);
                                    break;
                                } catch (final Exception e) {
                                    if (i < 2) {
                                        //Try again as file IO is not a science
                                        Thread.sleep(100);
                                    } else {
                                        logger.warning("Failed to delete: " + delete);
                                    }
                                }
                            }

                            logger.debug("Auto-Undeploy: Delete " + location);
                        }

                        logger.info("Completed Auto-Undeployment of: " + app.appId);

                    } catch (final Throwable e) {
                        logger.error("Auto-Undeploy Failed: " + file.getAbsolutePath(), e);
                    }
                    break;
                }
            }
        }
        return true;
    }

    public void fileUpdated(final File file) {
        fileRemoved(file);
        fileAdded(file);
    }

    @SuppressWarnings("UnusedParameters")
    public void observe(@Observes final ContainerSystemPostCreate postCreate) {
        start();
    }

    @SuppressWarnings("UnusedParameters")
    public void observe(@Observes final ContainerSystemPreDestroy preDestroy) {
        stop();
    }

    /**
     * Will stop the AutoDeployer from scanning, and waits for a running scan to complete.
     */
    public void stop() {

        timer.cancel();

        try {
            //Will block if scanning
            SEMAPHORE.acquire();
        } catch (final InterruptedException e) {
            //Ignore
        } finally {
            SEMAPHORE.release();
        }

    }

    public void start() {

        try {
            SEMAPHORE.acquire();
        } catch (final InterruptedException e) {
            logger.warning("AutoDeployer.start failed to obtain lock");
            return;
        }

        try {
            initialize();

            logger.info("Starting Auto-Deployer with a polling interval of " + pollIntervalMillis + "ms");

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        scan();
                    } catch (final Exception e) {
                        logger.error("Scan failed.", e);
                    }
                }
            }, pollIntervalMillis, pollIntervalMillis);
        } finally {
            SEMAPHORE.release();
        }

    }

    private void initialize() {

        for (final File file : list()) {

            if (!file.canRead()) {
                continue;
            }

            final FileInfo now = newInfo(file);
            now.setChanging(false);
            now.setNewFile(false);

            logger.debug("Auto-Deployer initialization found: " + file.getAbsolutePath());
        }
    }

    private FileInfo newInfo(final File child) {
        final FileInfo fileInfo = child.isDirectory() ? new DirectoryInfo(child) : new FileInfo(child);
        files.put(fileInfo.getPath(), fileInfo);
        return fileInfo;
    }

    /**
     * Looks for changes to the immediate contents of the directory we're watching.
     */
    public synchronized void scan() {

        try {
            SEMAPHORE.acquire();
        } catch (final InterruptedException e) {
            logger.warning("AutoDeployer.scan failed to obtain lock");
            return;
        }

        try {
            final List<File> files = list();

            final HashSet<String> missingFilesList = new HashSet<>(this.files.keySet());

            for (final File file : files) {

                missingFilesList.remove(file.getAbsolutePath());

                if (!file.canRead()) {
                    logger.debug("not readable " + file.getName());
                    continue;
                }

                final FileInfo oldStatus = oldInfo(file);
                final FileInfo newStatus = newInfo(file);

                newStatus.diff(oldStatus);

                if (oldStatus == null) {
                    // Brand new, but assume it's changing and
                    // wait a bit to make sure it's not still changing
                    logger.debug("File Discovered: " + newStatus);
                } else if (newStatus.isChanging()) {
                    // The two records are different -- record the latest as a file that's changing
                    // and later when it stops changing we'll do the add or update as appropriate.
                    logger.debug("File Changing: " + newStatus);
                } else if (oldStatus.isNewFile()) {
                    // Used to be changing, now in (hopefully) its final state
                    logger.info("New File: " + newStatus);
                    newStatus.setNewFile(!fileAdded(file));
                    newStatus.setChanging(false);
                } else if (oldStatus.isChanging()) {
                    logger.info("Updated Auto-Deployer File: " + newStatus);
                    fileUpdated(file);

                    missingFilesList.remove(oldStatus.getPath());
                }
                // else it's just totally unchanged and we ignore it this pass
            }

            // Look for any files we used to know about but didn't find in this pass
            for (final String path : missingFilesList) {
                logger.info("File removed: " + path);

                if (fileRemoved(new File(path))) {
                    this.files.remove(path);
                }
            }
        } finally {
            SEMAPHORE.release();
        }
    }

    private List<File> list() {
        final List<File> files = new ArrayList<>();

        { // list all the files associated with hot deploy locations

            final FileUtils base = SystemInstance.get().getBase();
            for (final Deployments deployment : deployments) {
                DeploymentsResolver.loadFrom(deployment, base, files);
            }
        }
        return files;
    }

    private FileInfo oldInfo(final File file) {
        return files.get(file.getAbsolutePath());
    }

    /**
     * Provides details about a directory.
     */
    private static class DirectoryInfo extends FileInfo {

        public DirectoryInfo(final File dir) {
            //
            // We don't pay attention to the size of the directory or files in the
            // directory, only the highest last modified time of anything in the
            // directory.  Hopefully this is good enough.
            //
            super(dir.getAbsolutePath(), 0, getLastModifiedInDir(dir));
        }
    }

    public static long getLastModifiedInDir(final File dir) {
        assert dir != null;

        if (dir.isFile()) {
            return dir.lastModified();
        }

        long value = dir.lastModified();
        final File[] children = dir.listFiles();
        long test;

        if (children != null) {
            for (final File child : children) {
                if (!child.canRead()) {
                    continue;
                }

                if (child.isDirectory()) {
                    if (new File(child.getParentFile(), child.getName() + ".war").exists()) { // unpacked
                        continue;
                    }
                    test = getLastModifiedInDir(child);
                } else {
                    test = child.lastModified();
                }

                if (test > value) {
                    value = test;
                }
            }
        }

        return value;
    }

    /**
     * Provides details about a file.
     */
    private static class FileInfo implements Serializable {

        private final String path;

        private long size;

        private long modified;

        private boolean newFile;

        private boolean changing;

        public FileInfo(final File file) {
            this(file.getAbsolutePath(), file.length(), file.lastModified());
        }

        public FileInfo(final String path, final long size, final long modified) {
            assert path != null;

            this.path = path;
            this.size = size;
            this.modified = modified;
            this.newFile = true;
            this.changing = true;
        }

        public String getPath() {
            return path;
        }

        public long getSize() {
            return size;
        }

        public void setSize(final long size) {
            this.size = size;
        }

        public long getModified() {
            return modified;
        }

        public void setModified(final long modified) {
            this.modified = modified;
        }

        public boolean isNewFile() {
            return newFile;
        }

        public void setNewFile(final boolean newFile) {
            this.newFile = newFile;
        }

        public boolean isChanging() {
            return changing;
        }

        public void setChanging(final boolean changing) {
            this.changing = changing;
        }

        public boolean isSame(final FileInfo info) {
            assert info != null;

            if (!path.equals(info.path)) {
                throw new IllegalArgumentException("Should only be used to compare two files representing the same path!");
            }

            return size == info.size && modified == info.modified;
        }

        public String toString() {
            return path;
        }

        public void diff(final FileInfo old) {
            if (old != null) {
                this.changing = !isSame(old);
                this.newFile = old.newFile;
            }
        }
    }
}
