package org.apache.openejb.maven.plugin.embedded;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.openejb.maven.util.MavenLogStreamFactory;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @goal run
 * @phase compile
 */
public class OpenEJBEmbeddedMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String id;

    /**
     * @parameter expression="${embedded.provider}" default-value="org.apache.openejb.OpenEjbContainer"
     * @required
     */
    private String provider;

    /**
     * @parameter expression="${embedded.modules}" default-value="${project.build.outputDirectory}"
     * @required
     */
    private String modules;

    /**
     * @parameter expression="${embedded.await}" default-value="true"
     * @required
     */
    private boolean await;

    /**
     * @parameter
     */
    private Map<String, String> properties;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MavenLogStreamFactory.setLogger(getLog());
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(createClassLoader(oldCl));

        EJBContainer container = null;
        try {
            container = EJBContainer.createEJBContainer(map());
            if (await) {
                final CountDownLatch latch = new CountDownLatch(1);
                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                    }
                }));
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // ignored
                }
            }
        } finally {
            if (container != null) {
                container.close();
            }
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private ClassLoader createClassLoader(final ClassLoader parent) {
        final List<URL> urls = new ArrayList<URL>();
        for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        for (String str : modules.split(",")) {
            final File file = new File(str);
            if (file.exists()) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    getLog().warn("can't use path " + str);
                }
            } else {
                getLog().warn("can't find " + str);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    private Map<?, ?> map() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(EJBContainer.APP_NAME, id);
        map.put(EJBContainer.PROVIDER, provider);
        map.put(EJBContainer.MODULES, modules.split(","));
        map.put("openejb.log.factory", "org.apache.openejb.maven.util.MavenLogStreamFactory");
        if (properties != null) {
            map.putAll(properties);
        }
        return map;
    }
}
