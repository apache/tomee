/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.maven.plugin.dd;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.config.NewLoaderLogic;
import org.apache.openejb.maven.util.MavenLogStreamFactory;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @goal merge
 * @phase compile
 * @requiresDependencyResolution runtime
 */
public class MergeDDForWebappMojo extends AbstractMojo {
    private static final String[] MANAGED_DD = {
            "ejb-jar.xml", "openejb-jar.xml",
            "env-entries.properties",
            "validation.xml"
        };

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.sourceDirectory}/main/webapp/WEB-INF"
     * @required
     * @readonly
     */
    private File webInfSrc;

    /**
     * @parameter expression="${project.build.directory}/${project.build.finalName}/WEB-INF"
     * @required
     * @readonly
     */
    private File webInf;

    /**
     * @parameter
     */
    private List<String> includes;

    /**
     * @parameter
     */
    private List<String> excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty("openejb.log.factory", "org.apache.openejb.maven.util.MavenLogStreamFactory");
        MavenLogStreamFactory.setLogger(getLog());

        initIncludeExclude();
        final Map<String, Merger<?>> mergers;
        try {
            mergers = initMerger();
        } catch (Exception e) {
            getLog().error("can't find mergers", e);
            return;
        }

        getLog().info("looking for descriptors...");
        final List<Artifact> artifacts = getDependencies();

        final ResourceFinder webInfFinder;
        try {
            webInfFinder = new ResourceFinder(webInfSrc.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new MojoFailureException("can't create a finder for webinf", e);
        }

        final Map<Artifact, ResourceFinder> finders = finders(artifacts);
        for (String dd : MANAGED_DD) {
            if (!mergers.containsKey(dd)) {
                getLog().warn("ignoring " + dd + " because no merger found");
            }

            int ddCount = 0;
            Object reference;
            final Merger<Object> merger = (Merger<Object>) mergers.get(dd);
            try {
                final URL ddUrl = webInfFinder.find(dd);
                if (ddUrl != null) {
                    reference = merger.read(ddUrl);
                    ddCount++;
                } else {
                    reference = merger.createEmpty();
                }
            } catch (IOException e) {
                reference = merger.createEmpty();
            }

            for (Artifact artifact : artifacts) {
                try {
                    final URL otherDD = finders.get(artifact).find("META-INF/" + dd);
                    if (otherDD != null) {
                        merger.merge(reference, merger.read(otherDD));
                        ddCount++;
                    }
                } catch (IOException e) {
                    // ignore since it means the resource was not found
                }
            }

            if (ddCount > 0) {
                if (!webInf.exists() && !webInf.mkdirs()) {
                    getLog().error("can't create " + webInf.getPath());
                }
                final File dump = new File(webInf, dd);
                try {
                    merger.dump(dump, reference);
                    getLog().info(dd + " merged on " + dump.getPath());
                } catch (Exception e) {
                    getLog().error("can't save " + dd + " in " + dump.getPath());
                }
            } else {
                getLog().debug("no " + dd + " found, this descriptor will be ignored");
            }
        }
    }

    private Map<String, Merger<?>> initMerger() throws Exception {
        final Map<String, Merger<?>> mergers = new HashMap<String, Merger<?>>();
        final ClassLoader cl = new URLClassLoader(new URL[] { getClass().getProtectionDomain().getCodeSource().getLocation() }, ClassLoader.getSystemClassLoader());
        final AbstractFinder finder = new ClassFinder(cl, true).link();
        final List<Class> foundMergers = finder.findSubclasses((Class) cl.loadClass(Merger.class.getName()));
        
        for (Class<? extends Merger> m : foundMergers) {
            try {
                // reload the class with the current classloader to avoid to miss some dependencies
                // excluded to scan faster
                final Merger<?> instance = (Merger<?>) Thread.currentThread().getContextClassLoader().loadClass(m.getName())
                                                .getConstructor(Log.class).newInstance(getLog());
                mergers.put(instance.descriptorName(), instance);
            } catch (Exception e) {
                getLog().warn("can't instantiate " + m.getName() + ", does it provide a constructor with a maven logger?");
            }
        }
        return mergers;
    }

    private Map<Artifact, ResourceFinder> finders(List<Artifact> artifacts) {
        final Map<Artifact, ResourceFinder> map = new HashMap<Artifact, ResourceFinder>(artifacts.size());
        for (Artifact artifact : artifacts) {
            try {
                map.put(artifact, new ResourceFinder(artifact.getFile().toURI().toURL()));
            } catch (MalformedURLException e) {
                getLog().warn("can't manage " + artifact);
            }
        }
        return map;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<Artifact> getDependencies() {
        final List<Artifact> dependencies = new ArrayList<Artifact>(project.getArtifacts());
        final Iterator<Artifact> it = dependencies.iterator();
        while (it.hasNext()) {
            final Artifact artifact = it.next();
            if (!keep(artifact.getArtifactId())) {
                it.remove();
            }
        }
        return dependencies;
    }

    private boolean keep(final String str) {
        return matches(includes, str) && !matches(excludes, str);
    }

    private void initIncludeExclude() {
        if (includes == null) {
            includes = new ArrayList<String>();
            includes.add("");
        }
        if (excludes == null) {
            excludes = Arrays.asList(NewLoaderLogic.getExclusions());
        }
    }

    private static boolean matches(final List<String> includes, final String artifact) {
        for (String pattern : includes) {
            if (artifact.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
