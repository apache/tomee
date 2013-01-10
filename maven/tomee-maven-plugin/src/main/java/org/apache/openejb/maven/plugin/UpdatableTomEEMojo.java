/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.codehaus.plexus.util.FileUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class UpdatableTomEEMojo extends AbstractTomEEMojo {
    public static final int INITIAL_DELAY = 5000;

    @Parameter
    private Synchronization synchronization;

    @Parameter
    private List<Synchronization> synchronizations;

    @Parameter(property = "tomee-plugin.buildDir", defaultValue = "${project.build.directory}", readonly = true)
    private File buildDir;

    @Parameter(property = "tomee-plugin.baseDir", defaultValue = "${project.basedir}", readonly = true)
    private File baseDir;

    @Parameter(property = "tomee-plugin.finalName", defaultValue = "${project.build.finalName}")
    private String finalName;

    @Parameter(property = "tomee-plugin.reload-on-update", defaultValue = "false")
    private boolean reloadOnUpdate;

    private Timer timer;
    private SynchronizerRedeployer task;

    @Override
    protected void run() {
        if (synchronization != null) {
            initSynchronization(synchronization);
        }
        if (synchronizations != null) {
            for (Synchronization s :synchronizations) {
                initSynchronization(s);
            }
        }

        if (startSynchronizers()) {
            forceReloadable = true;
        }

        super.run();
    }

    private void initSynchronization(final Synchronization synchronization) {
        if (synchronization.getBinariesDir() == null) {
            synchronization.setBinariesDir(new File(buildDir, "classes"));
        }
        if (synchronization.getResourcesDir() == null) {
            synchronization.setResourcesDir(new File(baseDir, "src/main/webapp"));
        }
        if (synchronization.getTargetResourcesDir() == null) {
            synchronization.setTargetResourcesDir(new File(catalinaBase, webappDir + "/" + finalName));
        }
        if (synchronization.getTargetBinariesDir() == null) {
            synchronization.setTargetBinariesDir(new File(catalinaBase, webappDir + "/" + finalName + "/WEB-INF/classes"));
        }
        if (synchronization.getUpdateInterval() <= 0) {
            synchronization.setUpdateInterval(5); // sec
        }
        if (synchronization.getExtensions() == null) {
            synchronization.setExtensions(new ArrayList<String>(Arrays.asList(".html", ".css", ".js", ".xhtml")));
        }
        if (synchronization.getUpdateOnlyExtenions() == null) {
            synchronization.setUpdateOnlyExtensions(Collections.<String>emptyList());
        }

        // merge update only and normal extensions to browse more easily extensions in the timer task
        synchronization.getExtensions().addAll(synchronization.getUpdateOnlyExtenions());

        if (reloadOnUpdate) {
            deployOpenEjbApplication = true;
        }
    }

    @Override
    protected void addShutdownHooks(final RemoteServer server) {
        if (synchronization != null || synchronizations != null) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    task.cancel();
                    timer.cancel();
                }
            });
        }
        super.addShutdownHooks(server);
    }

    protected boolean startSynchronizers() {
        timer = new Timer("tomee-maven-plugin-synchronizer");

        final Collection<Synchronizer> synchronizers = new ArrayList<Synchronizer>();

        long interval = 5000; // max of all sync interval

        if (synchronization != null) {
            synchronizers.add(new Synchronizer(synchronization));
            if (interval < 0) {
                interval = TimeUnit.SECONDS.toMillis(synchronization.getUpdateInterval());
            }
        }
        if (synchronizations != null) {
            for (Synchronization s : synchronizations) {
                synchronizers.add(new Synchronizer(s));
                if (interval < s.getUpdateInterval()) {
                    interval = TimeUnit.SECONDS.toMillis(s.getUpdateInterval());
                }
            }
        }

        // serialazing synchronizers to avoid multiple updates at the same time and reload a single time the app
        if (!synchronizers.isEmpty()) {
            task = new SynchronizerRedeployer(synchronizers);
            getLog().info("Starting synchronizer with an update interval of " + interval);
            if (interval > INITIAL_DELAY) {
                timer.scheduleAtFixedRate(task, interval, interval);
            } else {
                timer.scheduleAtFixedRate(task, INITIAL_DELAY, interval);
            }
            return true;
        }
        return false;
    }

    private class SynchronizerRedeployer extends TimerTask {
        private final Collection<Synchronizer> delegates;

        public SynchronizerRedeployer(final Collection<Synchronizer> synchronizers) {
            delegates = synchronizers;
        }

        @Override
        public void run() {
            int updated = 0;
            for (Synchronizer s : delegates) {
                try {
                    updated += s.call();
                } catch (Exception e) {
                    getLog().error(e.getMessage(), e);
                }
            }

            if (updated > 0 && reloadOnUpdate) {
                if (deployedFile != null && deployedFile.exists()) {
                    String path = deployedFile.getAbsolutePath();
                    if (path.endsWith(".war") || path.endsWith(".ear")) {
                        path = path.substring(0, path.length() - ".war".length());
                    }
                    getLog().info("Reloading " + path);
                    deployer().reload(path);
                }
            }
        }
    }

    private class Synchronizer implements Callable<Integer> {
        private final FileFilter fileFilter;
        private final FileFilter updateOnlyFilter;
        private final Synchronization synchronization;
        private long lastUpdate = System.currentTimeMillis();

        public Synchronizer(final Synchronization synch) {
            synchronization = synch;
            updateOnlyFilter = new SuffixesFileFilter(synchronization.getUpdateOnlyExtenions());
            if (synchronization.getRegex() != null) {
                fileFilter = new SuffixesAndRegexFileFilter(synchronization.getExtensions(), Pattern.compile(synchronization.getRegex()));
            } else {
                fileFilter = new SuffixesFileFilter(synchronization.getExtensions());
            }
        }

        @Override
        public Integer call() throws Exception {
            final long ts = System.currentTimeMillis();
            int updated = updateFiles(synchronization.getResourcesDir(), synchronization.getTargetResourcesDir(), ts)
                + updateFiles(synchronization.getBinariesDir(), synchronization.getTargetBinariesDir(), ts);
            lastUpdate = ts;
            return updated;
        }

        private int updateFiles(final File source, final File output, final long ts) {
            if (!source.exists()) {
                getLog().debug(source.getAbsolutePath() + " doesn't exist");
                return 0;
            }

            if (source.isFile()) {
                if (source.lastModified() < lastUpdate) {
                    return 0;
                }

                return updateFile(source, output, source, ts);
            }

            if (!source.isDirectory()) {
                getLog().warn(source.getAbsolutePath() + " is not a directory, skipping");
                return 0;
            }

            final Collection<File> files = Files.collect(source, fileFilter);
            int updated = 0;
            for (File file : files) {
                if (file.isDirectory()
                        || file.lastModified() < lastUpdate) {
                    continue;
                }

                updated += updateFile(source, output, file, ts);
            }

            return updated;
        }

        private int updateFile(final File source, final File target, final File file, final long ts) {
            final File output;
            if (target.isFile() && target.exists()) {
                output = target;
            } else {
                String relativized = file.getAbsolutePath().replace(source.getAbsolutePath(), "");
                if (relativized.startsWith(File.separator)) {
                    relativized = relativized.substring(1);
                }
                output = new File(target, relativized);
            }

            if (file.exists()) {
                getLog().info("[Updating] " + file.getAbsolutePath() + " to " + output.getAbsolutePath());
            } else {
                getLog().info("[Creating] " + file.getAbsolutePath() + " to " + output.getAbsolutePath());
            }
            try {
                if (!output.getParentFile().exists()) {
                    FileUtils.forceMkdir(output.getParentFile());
                }
                FileUtils.copyFile(file, output);
                output.setLastModified(ts);
            } catch (IOException e) {
                getLog().error(e);
            }

            if (updateOnlyFilter.accept(source)) {
                return 0;
            }
            return 1;
        }
    }

    private Deployer deployer() {
        if (removeTomeeWebapp) {
            throw new OpenEJBRuntimeException("Can't use reload feature without TomEE Webapp, please set removeTomeeWebapp to false");
        }

        final Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        properties.setProperty(Context.PROVIDER_URL, "http://" + tomeeHost + ":" + tomeeHttpPort + "/tomee/ejb");
        try {
            final Context context = new InitialContext(properties);
            return (Deployer) context.lookup("openejb/DeployerBusinessRemote");
        } catch (NamingException e) {
            throw new OpenEJBRuntimeException("Can't lookup Deployer", e);
        }
    }

    private static class SuffixesFileFilter implements FileFilter {
        private final String[] suffixes;

        public SuffixesFileFilter(final List<String> extensions) {
            if (extensions == null) {
                suffixes = new String[0];
            } else {
                suffixes = extensions.toArray(new String[extensions.size()]);
            }
        }

        @Override
        public boolean accept(final File file) {
            if (file.isDirectory()) {
                return true;
            }

            for (String suffix : suffixes) {
                if (file.getName().endsWith(suffix)) {
                    return true;
                }
            }

            return false;
        }
    }

    private class SuffixesAndRegexFileFilter extends SuffixesFileFilter {
        private final Pattern pattern;

        public SuffixesAndRegexFileFilter(final List<String> extensions, final Pattern pattern) {
            super(extensions);
            this.pattern = pattern;
        }

        @Override
        public boolean accept(final File file) {
            return file.isDirectory() || (super.accept(file) && pattern.matcher(file.getAbsolutePath()).matches());

        }
    }
}
