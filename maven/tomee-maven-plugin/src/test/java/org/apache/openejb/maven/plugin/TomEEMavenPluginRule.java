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
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.NetworkUtil;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
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
    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
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
                if (f.getAnnotation(Mojo.class) != null) {
                    f.setAccessible(true);
                    f.set(testInstance, testMojo);
                } else if (f.getAnnotation(Url.class) != null) {
                    f.setAccessible(true);
                    f.set(testInstance, "http://localhost:" + testMojo.tomeeHttpPort);
                } else if (f.getAnnotation(Config.class) != null) {
                    f.setAccessible(true);

                    final Field mojoField = AbstractTomEEMojo.class.getDeclaredField(f.getName());
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
            });
        }
    }

    protected static abstract class TestTomEEMojo extends StartTomEEMojo {
        final AtomicReference<Throwable> ex = new AtomicReference<>();

        protected abstract void asserts() throws Throwable;

        public void runTest() throws Throwable {
            execute();
            final Throwable throwable = ex.get();
            if (throwable != null) {
                throw throwable;
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
                try (final FileReader reader = new FileReader(settingsXml)) {
                    tomEEMojo.settings = new SettingsXpp3Reader().read(reader, false);
                }
            } catch (final Exception e) {
                // no-op
            }
        }
        if (tomEEMojo.settings == null) {
            tomEEMojo.settings = new Settings();
        }
        tomEEMojo.settings.setOffline(true);
        if (tomEEMojo.settings.getLocalRepository() == null) {
            tomEEMojo.settings.setLocalRepository(System.getProperty("user.home") + "/.m2/repository");
        }

        // we don't deploy anything by default
        tomEEMojo.skipCurrentProject = true;

        // our well known web profile ;)
        tomEEMojo.tomeeGroupId = "org.apache.openejb";
        tomEEMojo.tomeeArtifactId = "apache-tomee";
        tomEEMojo.tomeeVersion = "-1";
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

        tomEEMojo.tomeeHttpsPort = NetworkUtil.getNextAvailablePort();
        tomEEMojo.tomeeHttpPort = NetworkUtil.getNextAvailablePort();
        tomEEMojo.tomeeAjpPort = NetworkUtil.getNextAvailablePort();
        tomEEMojo.tomeeShutdownPort = NetworkUtil.getNextAvailablePort();
        tomEEMojo.tomeeShutdownCommand = "SHUTDOWN";
        tomEEMojo.tomeeHost = "localhost";

        tomEEMojo.useConsole = true;
        tomEEMojo.checkStarted = true;

        // we mock all the artifact resolution in test
        tomEEMojo.remoteRepos = new LinkedList<>();
        tomEEMojo.local = new DefaultArtifactRepository("local", tomEEMojo.settings.getLocalRepository(), new DefaultRepositoryLayout());

        tomEEMojo.factory = ArtifactFactory.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ArtifactFactory.class}, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return new DefaultArtifact(
                    tomEEMojo.tomeeGroupId,
                    tomEEMojo.tomeeArtifactId,
                    VersionRange.createFromVersion(tomEEMojo.tomeeVersion),
                    "provided",
                    tomEEMojo.tomeeType,
                    tomEEMojo.tomeeClassifier,
                    null) {
                    @Override
                    public File getFile() {
                        return new File(
                            tomEEMojo.settings.getLocalRepository(),
                            getGroupId().replace('.', '/')
                                + '/' + getArtifactId().replace('.', '/')
                                + '/' + getVersion()
                                + '/' + getArtifactId().replace('.', '/') + '-' + getVersion() + '-' + getClassifier() + '.' + getType());
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
