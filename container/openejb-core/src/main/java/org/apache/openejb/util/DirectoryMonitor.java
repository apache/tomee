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
package org.apache.openejb.util;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryMonitor {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DirectoryMonitor.class.getPackage().getName());

    private final int pollIntervalMillis;

    private final File target;

    private final Listener listener;

    private final Map files = new HashMap();

    private final Timer timer;

    public DirectoryMonitor(final File target, final Listener listener, final int pollIntervalMillis) {
        assert listener == null : "No listener specified";
        assert target.isDirectory() : "File specified is not a directory. " + target.getAbsolutePath();
        assert target.canRead() : "Directory specified cannot be read. " + target.getAbsolutePath();
        assert pollIntervalMillis > 0 : "Poll Interval must be above zero.";


        this.target = target;
        this.listener = listener;
        this.pollIntervalMillis = pollIntervalMillis;

        this.timer = new Timer(this.getClass().getSimpleName());
    }

    private Logger getLogger() {
        return logger;
    }

    public int getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public File getTarget() {
        return target;
    }

    public Listener getListener() {
        return listener;
    }

    public synchronized void stop() {
        timer.cancel();
    }

    public void start() {
        initialize();

        getLogger().debug("Scanner running.  Polling every " + pollIntervalMillis + " milliseconds.");

        timer.scheduleAtFixedRate(new TimerTask(){
            public void run() {
                try {
                    scan();
                }
                catch (Exception e) {
                    getLogger().error("Scan failed.", e);
                }
            }
        }, pollIntervalMillis, pollIntervalMillis);

    }

    private void initialize() {
        getLogger().debug("Doing initial scan of " + target.getAbsolutePath());

        File[] files = (target.isDirectory()) ? target.listFiles(): new File[]{target};

        for (File file : files) {

            if (!file.canRead()) {
                continue;
            }

            FileInfo now = newInfo(file);
            now.setChanging(false);
        }
    }

    private FileInfo newInfo(File child) {
        FileInfo fileInfo = child.isDirectory() ? new DirectoryInfo(child) : new FileInfo(child);
        files.put(fileInfo.getPath(), fileInfo);
        return fileInfo;
    }

    /**
     * Looks for changes to the immediate contents of the directory we're watching.
     */
    public void scan() {

        File[] files = (target.isDirectory()) ? target.listFiles(): new File[]{target};

        HashSet<String> missingFilesList = new HashSet(this.files.keySet());

        for (File file : files) {

            missingFilesList.remove(file.getAbsolutePath());

            if (!file.canRead()) {
                getLogger().debug("not readable " + file.getName());
                continue;
            }

            FileInfo oldStatus = oldInfo(file);
            FileInfo newStatus = newInfo(file);

            newStatus.diff(oldStatus);

            if (oldStatus == null) {
                // Brand new, but assume it's changing and
                // wait a bit to make sure it's not still changing
                getLogger().debug("File Discovered: " + newStatus);
            } else if (newStatus.isChanging()) {
                // The two records are different -- record the latest as a file that's changing
                // and later when it stops changing we'll do the add or update as appropriate.
                getLogger().debug("File Changing: " + newStatus);
            } else if (oldStatus.isNewFile()) {
                // Used to be changing, now in (hopefully) its final state
                getLogger().info("New File: " + newStatus);
                newStatus.setNewFile(!listener.fileAdded(file));
            } else if (oldStatus.isChanging()) {
                getLogger().info("Updated File: " + newStatus);
                listener.fileUpdated(file);

                missingFilesList.remove(oldStatus.getPath());
            }
            // else it's just totally unchanged and we ignore it this pass
        }

        // Look for any files we used to know about but didn't find in this pass
        for (String path : missingFilesList) {
            getLogger().info("File removed: " + path);

            if (listener.fileRemoved(new File(path))) {
                this.files.remove(path);
            }
        }
    }

    private FileInfo oldInfo(File file) {
        return (FileInfo) files.get(file.getAbsolutePath());
    }

    /**
     * Allows custom behavior to be hooked up to process file state changes.
     */
    public interface Listener {
        boolean fileAdded(File file);

        boolean fileRemoved(File file);

        void fileUpdated(File file);
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

        private static long getLastModifiedInDir(final File dir) {
            assert dir != null;

            long value = dir.lastModified();
            File[] children = dir.listFiles();
            long test;

            for (int i = 0; i < children.length; i++) {
                File child = children[i];

                if (!child.canRead()) {
                    continue;
                }

                if (child.isDirectory()) {
                    test = getLastModifiedInDir(child);
                } else {
                    test = child.lastModified();
                }

                if (test > value) {
                    value = test;
                }
            }

            return value;
        }
    }

    /**
     * Provides details about a file.
     */
    private static class FileInfo implements Serializable {
        private String path;

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
