package org.apache.openejb.maven.plugin.spi;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.FileArchive;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @goal generate
 * @requiresDependencyResolution compile+runtime
 * @phase compile
 */
public class SpiMojo extends AbstractMojo {
    private static final String PROFILE_PATH = "org/apache/xbean/profile.properties";

    /**
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File module;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private List<String> annotations;

    /**
     * @parameter
     */
    private List<String> subclasses;

    /**
     * @parameter
     */
    private List<String> implementations;

    /**
     * @parameter
     */
    private List<String> profiles;

    /**
     * @parameter expression="${spi.output}" default-value="${project.build.outputDirectory}/META-INF/scan.xml"
     *
     * for webapp: ${project.build.directory}/${project.build.finalName}/WEB-INF/classes/META-INF/scan.xml
     */
    private String outputFilename;

    /**
     * @parameter expression="${spi.meta}" default-value="false"
     */
    private boolean useMeta;

    /**
     * @parameter expression="${spi.aggregated-archive}" default-value="true"
     */
    private boolean useAggregatedArchiveIfWar;

    /**
     * @parameter default-value="${project.packaging}"
     * @readonly
     */
    private String packaging;

    /**
     * @parameter expression="${project.pluginArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remotePluginRepositories;

    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository local;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Map<String, Profile> loadedProfiles = loadProfiles();

        //
        // create profiles to use
        //
        final List<Profile> profileToUse = new ArrayList<Profile>();
        if (profiles != null) {
            for (String profile : profiles) {
                if (loadedProfiles.containsKey(profile)) {
                    profileToUse.add(loadedProfiles.get(profile));
                } else {
                    getLog().info("can't find profile " + profile + ", available ones are " + loadedProfiles.keySet());
                }
            }
        }
        if (annotations != null || subclasses != null || implementations != null) {
            profileToUse.add(new Profile(annotations, subclasses, implementations));
        }

        if (profileToUse.isEmpty() && loadedProfiles.size() > 0) {
            final Map.Entry<String, Profile> profile = loadedProfiles.entrySet().iterator().next();
            getLog().info("using profile " + profile.getKey());
            profileToUse.add(profile.getValue());
        }

        if (profileToUse.isEmpty()) {
            getLog().warn("no profile or configuration, nothing will be done");
            return;
        }

        //
        // creating the archive and its classloader
        //

        // war is different since it will contain a single descriptor for its lib too (not an ear)
        boolean war = "war".equals(packaging);
        final Archive archive;
        final URLClassLoader loader = createClassLoader(providedDependenciesClassLoader());
        if (war && useAggregatedArchiveIfWar) {
            archive = new ClasspathArchive(loader, loader.getURLs());
            getLog().info("using an aggregated archive");
        } else {
            archive = new FileArchive(loader, module);
            getLog().info("using a file archive");
        }

        // the result
        final Set<String> classes = new TreeSet<String>();
        try {
            final AnnotationFinder finder = new AnnotationFinder(archive);
            finder.link();

            //
            // find classes
            //

            for (Profile profile : profileToUse) {
                if (profile.getAnnotations() != null) {
                    for (String annotation : profile.getAnnotations()) {
                        final Class<? extends Annotation> annClazz;
                        try {
                            annClazz = (Class<? extends Annotation>) load(loader, annotation);
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + annotation);
                            continue;
                        }

                        if (!useMeta) {
                            for (Class<?> clazz : finder.findAnnotatedClasses(annClazz)) {
                                classes.add(clazz.getName());
                            }
                        } else {
                            for (Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(annClazz)) {
                                classes.add(clazz.get().getName());
                            }
                        }

                        if (!useMeta) {
                            for (Field clazz : finder.findAnnotatedFields(annClazz)) {
                                classes.add(clazz.getDeclaringClass().getName());
                            }
                        } else {
                            for (Annotated<Field> clazz : finder.findMetaAnnotatedFields(annClazz)) {
                                classes.add(clazz.get().getDeclaringClass().getName());
                            }
                        }

                        if (!useMeta) {
                            for (Method clazz : finder.findAnnotatedMethods(annClazz)) {
                                classes.add(clazz.getDeclaringClass().getName());
                            }
                        } else {
                            for (Annotated<Method> clazz : finder.findMetaAnnotatedMethods(annClazz)) {
                                classes.add(clazz.get().getDeclaringClass().getName());
                            }
                        }
                    }
                }

                if (profile.getSubclasses() != null) {
                    for (String subclass : profile.getSubclasses()) {
                        try {
                            for (Class<?> clazz : finder.findSubclasses(load(loader, subclass))) {
                                classes.add(clazz.getName());
                            }
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + subclass);
                        }
                    }
                }

                if (profile.getImplementations() != null) {
                    for (String implementation : profile.getImplementations()) {
                        try {
                            for (Class<?> clazz : finder.findImplementations(load(loader, implementation))) {
                                classes.add(clazz.getName());
                            }
                        } catch (MojoFailureException mfe) {
                            getLog().warn("can't find " + implementation);
                        }
                    }
                }
            }

            //
            // dump found classes
            //

            final File output = new File(outputFilename);
            if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
                getLog().error("can't create " + output.getParent());
                return;
            }

            final XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(outputFilename));
            try {
                out.writeStartDocument();
                out.writeStartElement("scan");
                out.writeCharacters("\n");

                    out.writeCharacters("  ");
                    out.writeStartElement("classes");
                    out.writeCharacters("\n");

                    for (String clazz : classes) {
                        out.writeCharacters("    ");
                        out.writeStartElement("class");
                            out.writeCharacters(clazz);
                        out.writeEndElement();
                        out.writeCharacters("\n");
                    }

                    out.writeCharacters("  ");
                    out.writeEndElement();
                    out.writeCharacters("\n");

                    out.writeCharacters("  ");
                    out.writeStartElement("packages");
                    out.writeCharacters("\n  ");
                    out.writeEndElement();
                    out.writeCharacters("\n");

                out.writeEndElement();
                out.writeEndDocument();
            } finally {
                out.flush();
                out.close();
            }

            getLog().info("generated " + output.getPath());
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    private Map<String, Profile> loadProfiles() {
        final Map<String, Profile> profiles = new HashMap<String, Profile>();
        try {
            final Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(PROFILE_PATH);
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                final Properties properties = new Properties();
                properties.load(new BufferedInputStream(url.openStream()));

                final String name = properties.getProperty("name");
                if (name == null) {
                    getLog().warn("ignoring " + url.toExternalForm() + " since it doesn't contain a name");
                }
                

                final List<String> profileAnnotations = list(properties.getProperty("annotations"));
                final List<String> profileSubclasses = list(properties.getProperty("subclasses"));
                final List<String> profileImplementations = list(properties.getProperty("implementations"));

                profiles.put(name, new Profile(profileAnnotations, profileSubclasses, profileImplementations));
            }
        } catch (Exception e) {
            getLog().warn("can't look for profiles");
        }
        return profiles;
    }

    private static List<String> list(final String commaSeparatedList) {
        final List<String> list = new ArrayList<String>();
        if (commaSeparatedList != null) {
            for (String value : commaSeparatedList.split(",")) {
                final String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        }
        return list;
    }

    private ClassLoader providedDependenciesClassLoader() {
        final Set<URL> urls = new HashSet<URL>();

        // provided artifacts
        for (Artifact artifact : (Set<Artifact>) project.getDependencyArtifacts()) {
            if (!"provided".equals(artifact.getScope())) {
                continue;
            }

            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }

        // plugin dependencies
        final Plugin thisPlugin = (Plugin) project.getBuild().getPluginsAsMap().get("org.apache.openejb:spi-helper-maven-plugin");
        if (thisPlugin != null && thisPlugin.getDependencies() != null) {
            for (Dependency artifact : thisPlugin.getDependencies()) {
                final Artifact resolved = new DefaultArtifact(
                        artifact.getGroupId(), artifact.getArtifactId(), VersionRange.createFromVersion(artifact.getVersion()),
                        artifact.getScope(), artifact.getType(), artifact.getClassifier(), new DefaultArtifactHandler());
                try {
                    resolver.resolve(resolved, remotePluginRepositories, local);
                    urls.add(resolved.getFile().toURI().toURL());
                } catch (ArtifactResolutionException e) {
                    getLog().warn("can't resolve " + artifact.getArtifactId());
                } catch (ArtifactNotFoundException e) {
                    getLog().warn("can't find " + artifact.getArtifactId());
                } catch (MalformedURLException e) {
                    getLog().warn("can't get url of " + resolved.getFile() + " for artifact " + resolved.getArtifactId());
                }
            }
        }

        return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
    }

    private Class<?> load(final ClassLoader loader, final String name) throws MojoFailureException {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new MojoFailureException("can't load " + name, e);
        }
    }

    private URLClassLoader createClassLoader(final ClassLoader parent) {
        final List<URL> urls = new ArrayList<URL>();
        for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use artifact " + artifact.toString());
            }
        }
        if (module.exists()) {
            try {
                urls.add(module.toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("can't use path " + module.getPath());
            }
        } else {
            getLog().warn("can't find " + module.getPath());
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    public static final class Profile {
        private List<String> annotations;
        private List<String> subclasses;
        private List<String> implementations;

        public Profile(final List<String> annotations, final List<String> subclasses, final List<String> implementations) {
            this.annotations = annotations;
            this.subclasses = subclasses;
            this.implementations = implementations;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public List<String> getSubclasses() {
            return subclasses;
        }

        public List<String> getImplementations() {
            return implementations;
        }
    }
}
