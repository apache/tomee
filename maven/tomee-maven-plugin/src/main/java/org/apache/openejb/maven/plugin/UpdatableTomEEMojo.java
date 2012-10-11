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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class UpdatableTomEEMojo extends AbstractTomEEMojo {
    public static final int INITIAL_DELAY = 5000;

    /**
     * @parameter
     */
    private Synchronization synchronization;

    /**
     * @parameter expression="${tomee-plugin.buildDir}" default-value="${project.build.directory}"
     * @required
     * @readOnly
     */
    private File buildDir;

    /**
     * @parameter expression="${tomee-plugin.baseDir}" default-value="${project.basedir}"
     * @required
     * @readOnly
     */
    private File baseDir;

    /**
     * @parameter expression="${tomee-plugin.finalName}" default-value="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * @parameter expression="${tomee-plugin.reload-on-update}" default-value="false"
     * @required
     */
    private boolean reloadOnUpdate;

    private Timer timer;

    @Override
    protected void run() {
        if (synchronization != null) {
            if (synchronization.getBinariesDir() == null) {
                synchronization.setBinariesDir(new File(buildDir, "classes"));
            }
            if (synchronization.getResourcesDir() == null) {
                synchronization.setResourcesDir(new File(baseDir, "src/main/webapp"));
            }
            if (synchronization.getTargetResourcesDir() == null) {
                synchronization.setTargetResourcesDir(new File(buildDir, "apache-tomee/webapps/" + finalName));
            }
            if (synchronization.getTargetBinariesDir() == null) {
                synchronization.setTargetBinariesDir(new File(buildDir, "apache-tomee/webapps/" + finalName + "/WEB-INF/classes"));
            }
            if (synchronization.getUpdateInterval() <= 0) {
                synchronization.setUpdateInterval(15); // sec
            }
            if (synchronization.getExtensions() == null) {
                synchronization.setExtensions(Arrays.asList(".html", ".css", ".js", ".xhtml"));
            }
            startSynchronizer();
        }
        super.run();
    }

    @Override
    protected void addShutdownHooks(final RemoteServer server) {
        if (synchronization != null) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override public void run() {
                    timer.cancel();
                }
            });
        }
        super.addShutdownHooks(server);
    }

    protected void startSynchronizer() {
        timer = new Timer("tomee-maven-plugin-synchronizer");
        long interval = TimeUnit.SECONDS.toMillis(synchronization.getUpdateInterval());
        if (interval > INITIAL_DELAY) {
            timer.scheduleAtFixedRate(new Synchronizer(), interval, interval);
        } else {
            timer.scheduleAtFixedRate(new Synchronizer(), INITIAL_DELAY, interval);
        }
    }

    private class Synchronizer extends TimerTask {
        private final FileFilter fileFilter;
        private long lastUpdate = System.currentTimeMillis();

        private Synchronizer() {
            if (synchronization.getRegex() != null) {
                fileFilter = new SuffixesAndRegexFileFilter(synchronization.getExtensions(), Pattern.compile(synchronization.getRegex()));
            } else {
                fileFilter = new SuffixesFileFilter(synchronization.getExtensions());
            }
        }

        @Override
        public void run() {
            final long ts = System.currentTimeMillis();
            updateFiles(synchronization.getResourcesDir(), synchronization.getTargetResourcesDir(), ts);
            updateFiles(synchronization.getBinariesDir(), synchronization.getTargetBinariesDir(), ts);
            lastUpdate = ts;
        }

        private void updateFiles(final File source, final File output, final long ts) {
            if (!source.exists()) {
                getLog().debug(source.getAbsolutePath() + " does'tn exist");
                return;
            }

            if (!source.isDirectory()) {
                getLog().warn(source.getAbsolutePath() + " is not a directory, skipping");
                return;
            }

            final Collection<File> files = Files.collect(source, fileFilter);
            int updated = 0;
            for (File file : files) {
                if (file.isDirectory()
                        || file.lastModified() < lastUpdate) {
                    continue;
                }

                updateFile(source, output, file, ts);
                updated++;
            }

            if (updated > 0 && reloadOnUpdate) {
                if (deployedFile != null && deployedFile.exists()) {
                    String path = deployedFile.getAbsolutePath();
                    if (path.endsWith(".war")) {
                        path = path.substring(0, path.length() - ".war".length());
                    }
                    getLog().info("Reloading " + path);
                    deployer().reload(path);
                }
            }
        }

        private void updateFile(final File source, final File target, final File file, final long ts) {
            String relativized = file.getAbsolutePath().replace(source.getAbsolutePath(), "");
            if (relativized.startsWith(File.separator)) {
                relativized = relativized.substring(1);
            }

            final File output = new File(target, relativized);
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
            if (file.isDirectory()) {
                return true;
            }

            return super.accept(file) && pattern.matcher(file.getAbsolutePath()).matches();
        }
    }
}
