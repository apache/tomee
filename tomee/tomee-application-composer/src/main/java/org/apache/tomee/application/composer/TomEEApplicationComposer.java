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
package org.apache.tomee.application.composer;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomee.application.composer.component.Web;
import org.apache.tomee.application.composer.internal.Bundler;
import org.apache.tomee.application.composer.internal.ClasspathBuilder;
import org.apache.tomee.application.composer.internal.LazyDeployer;
import org.apache.tomee.application.composer.internal.StandardContextCustomizer;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.xbean.finder.MetaAnnotatedClass;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class TomEEApplicationComposer implements AutoCloseable {
    private static final String[] EMPTY_ARGS = new String[0];

    private final Container closable;

    public TomEEApplicationComposer(final Class<?> application, final String[] args) {
        final MetaAnnotatedClass<?> meta = new MetaAnnotatedClass<>(application);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final List<URL> jars = ClasspathBuilder.buildClasspath(meta, loader);

        final File root = fakeRootDir();

        // ATM we only support web module so we use it directly but if we support ejb module or even ear we'll have to do it here
        final Web web = meta.getAnnotation(Web.class);
        final WebModule webModule = Bundler.createWebModule(web, loader, jars, root.getAbsolutePath());
        final AppModule app = new AppModule(webModule);
        app.setStandloneWebModule();

        try {
            final Configuration configuration = new Configuration();
            configuration.property("openejb.system.apps", "false");
            configuration.setHttpPort(4589); // just to avoid 8080 while config part is not done
            // TODO: take config from args

            final Container container = new Container(configuration);
            final SystemInstance systemInstance = SystemInstance.get();
            systemInstance.addObserver(new StandardContextCustomizer(webModule));
            systemInstance.addObserver(new LazyDeployer(webModule, meta));

            try {
                final AppInfo appInfo = new ConfigurationFactory().configureApplication(app);
                systemInstance.getComponent(Assembler.class).createApplication(appInfo);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }

            closable = container;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public TomEEApplicationComposer(final Class<?> application) {
        this(application, EMPTY_ARGS);
    }

    public int getPort() {
        return closable.getTomcat().getConnector().getPort();
    }

    @Override
    public void close() throws Exception {
        closable.close();
    }

    public static AutoCloseable run(final Class<?> application, final String[] args) {
        return new TomEEApplicationComposer(application, args);
    }

    // TODO: config it from args
    private static File fakeRootDir() {
        final File root = new File(System.getProperty("java.io.tmpdir"), "tomee-application-composer-" + TomEEApplicationComposer.class.hashCode() + "_" + new Date());
        Files.mkdirs(root);
        Files.deleteOnExit(root);
        return root;
    }

    public static void main(final String[] args) {
        if (args == null || args.length < 1) {
            System.err.println("First parameter should be the application class");
            return;
        }

        try {
            final Collection<String> newArgs = new ArrayList<>(asList(args));
            final Iterator<String> iterator = newArgs.iterator();
            iterator.next();
            iterator.remove();

            run(Thread.currentThread().getContextClassLoader().loadClass(args[0]), newArgs.toArray(new String[newArgs.size()]));
        } catch (final ClassNotFoundException e) {
            System.err.println("Can't find application class, it should be the first parameter: " + e.getMessage());
        }
    }

    public static AutoCloseable run(final Class<?> application) {
        return run(application, EMPTY_ARGS);
    }
}
