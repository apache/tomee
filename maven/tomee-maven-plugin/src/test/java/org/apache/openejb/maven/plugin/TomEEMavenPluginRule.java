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

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.OpenEjbVersion;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Allows to write unit tests on TomEE Maven Plugin.
 *
 * The basic usage is:
 *
 *
 public class TomEEMavenPluginTest {
@Rule
public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

@Url // get the base http url injected
private String url;

@Config
private String tomeeHost = "localhost";

@Test
public void simpleStart() throws Exception {
assertThat(IO.slurp(new URL(url + "/docs")), containsString("Apache Tomcat"));
}
}
 *
 * @Url specifies you want the http url base injected (without any webapp context)
 * @Config specifies you want to configure the backend mojo with the value specifies on the test instance
 */
public class TomEEMavenPluginRule implements MethodRule {
    private boolean run = true;

    public TomEEMavenPluginRule noRun() {
        run = false;
        return this;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod ignored, final Object target) {
        return new RunTest(target, base);
    }

    public class RunTest extends Statement {
        // The TestCase instance
        private final Object testInstance;
        private final Statement next;

        public RunTest(final Object testInstance, final Statement next) {
            this.testInstance = testInstance;
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
            final TestTomEEMojo testMojo = newMojo();

            for (final Field f : testInstance.getClass().getDeclaredFields()) {
                if (f.getAnnotation(Url.class) != null) {
                    f.setAccessible(true);
                    f.set(testInstance, "http://localhost:" + testMojo.tomeeHttpPort);
                } else if (f.getAnnotation(Mojo.class) != null) {
                    f.setAccessible(true);
                    f.set(testInstance, testMojo);
                } else if (f.getAnnotation(Config.class) != null) {
                    f.setAccessible(true);

                    Field mojoField;
                    try {
                        mojoField = AbstractTomEEMojo.class.getDeclaredField(f.getName());
                    } catch (final Exception e) {
                        mojoField = AbstractAddressMojo.class.getDeclaredField(f.getName());
                    }
                    mojoField.setAccessible(true);
                    mojoField.set(testMojo, f.get(testInstance));
                }
            }

            testMojo.runTest();
        }

        private TestTomEEMojo newMojo() {
            return defaults(new TestTomEEMojo() {
                @Override
                protected void asserts() throws Throwable {
                    next.evaluate();
                }

                @Override
                protected void run() {
                    if (run) {
                        super.run();
                    } else {
                        try {
                            next.evaluate();
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }
            });
        }
    }

    protected static abstract class TestTomEEMojo extends StartTomEEMojo {
        final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();

        protected abstract void asserts() throws Throwable;

        public void runTest() throws Throwable {
            execute();
            if (ex.get() != null) {
                throw ex.get();
            }
        }

        protected void serverCmd(final RemoteServer server, final List<String> strings) {
            super.serverCmd(server, strings);

            // the test
            try {
                asserts();
            } catch (final Throwable e) {
                ex.set(e);
            } finally {
                destroy(this);
            }
        }
    }

    protected static <T extends AbstractTomEEMojo> T defaults(final T tomEEMojo) {
        // settings
        final File settingsXml = new File(System.getProperty("user.home") + "/.m2/settings.xml");
        if (settingsXml.exists()) {
            try {
                final FileReader reader = new FileReader(settingsXml);
                try {
                    tomEEMojo.settings = new SettingsXpp3Reader().read(reader, false);
                } finally {
                    reader.close();
                }
            } catch (final Exception e) {
                // no-op
            }
        }
        tomEEMojo.project = new MavenProject() {
            @Override
            public Set getArtifacts() {
                return Collections.emptySet();
            }
        };
        if (tomEEMojo.settings == null) {
            tomEEMojo.settings = new Settings();
        }
        tomEEMojo.settings.setOffline(true);
        if (tomEEMojo.settings.getLocalRepository() == null || "".equals(tomEEMojo.settings.getLocalRepository())) {
            tomEEMojo.settings.setLocalRepository(System.getProperty("openejb.m2.home", System.getProperty("user.home") + "/.m2/repository"));
        }

        // we don't deploy anything by default
        tomEEMojo.skipCurrentProject = true;

        // our well known web profile ;)
        tomEEMojo.tomeeGroupId = "org.apache.tomee";
        tomEEMojo.tomeeArtifactId = "apache-tomee";
        tomEEMojo.tomeeVersion = OpenEjbVersion.get().getVersion();
        tomEEMojo.tomeeClassifier = "webprofile";
        tomEEMojo.tomeeType = "zip";

        // target config
        tomEEMojo.catalinaBase = new File("target/mvn-test");
        Files.mkdirs(tomEEMojo.catalinaBase);

        // some defaults
        tomEEMojo.simpleLog = true;
        tomEEMojo.quickSession = true;

        tomEEMojo.libDir = "lib";
        tomEEMojo.webappDir = "webapps";
        tomEEMojo.appDir = "apps";

        tomEEMojo.bin = new File(tomEEMojo.catalinaBase.getPath() + "-bin");
        tomEEMojo.config = new File(tomEEMojo.catalinaBase.getPath() + "-conf");
        tomEEMojo.lib = new File(tomEEMojo.catalinaBase.getPath() + "-lib");

        tomEEMojo.tomeeHttpPort = Integer.toString(NetworkUtil.getNextAvailablePort());
        tomEEMojo.tomeeAjpPort = Integer.toString(NetworkUtil.getNextAvailablePort());
        tomEEMojo.tomeeShutdownPort = Integer.toString(NetworkUtil.getNextAvailablePort());
        tomEEMojo.tomeeShutdownCommand = "SHUTDOWN";
        tomEEMojo.tomeeHost = "localhost";

        tomEEMojo.useConsole = true;
        tomEEMojo.checkStarted = true;

        tomEEMojo.overrideOnUnzip = true;
        tomEEMojo.skipRootFolderOnUnzip = true;

        // we mock all the artifact resolution in test
        tomEEMojo.remoteRepos = new LinkedList<ArtifactRepository>();
        tomEEMojo.local = new DefaultArtifactRepository("local", tomEEMojo.settings.getLocalRepository(), new DefaultRepositoryLayout());

        tomEEMojo.factory = ArtifactFactory.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ArtifactFactory.class}, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return new DefaultArtifact(
                        String.class.cast(args[0]),
                        String.class.cast(args[1]),
                        VersionRange.class.cast(args[2]),
                        String.class.cast(args[5]),
                        String.class.cast(args[3]),
                        args[4] == null ? "" : String.class.cast(args[4]),
                        null) {
                    @Override
                    public File getFile() {
                        return new File(
                                tomEEMojo.settings.getLocalRepository(),
                                getGroupId().replace('.', '/')
                                        + '/' + getArtifactId().replace('.', '/')
                                        + '/' + getVersion()
                                        + '/' + getArtifactId().replace('.', '/') + '-' + getVersion() + (args[4] == null ? "" : '-' + getClassifier()) + '.' + getType());
                    }
                };
            }
        }));

        tomEEMojo.resolver = ArtifactResolver.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { ArtifactResolver.class }, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return null;
            }
        }));

        return tomEEMojo;
    }

    protected static void destroy(final AbstractTomEEMojo run) {
        if (run.server != null) {
            run.server.destroy();
        }
    }
}
