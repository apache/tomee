package org.apache.openejb.assembler.classic;

import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.loader.Files;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DeployTimeEnhancer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DeployTimeEnhancer.class);
    public static final String CLASS_EXT = ".class";
    public static final String PROPERTIES_FILE_PROP = "propertiesFile";

    private final Method enhancerMethod;
    private final Constructor<?> optionsConstructor;

    public DeployTimeEnhancer() {
        Method mtd;
        Constructor<?> cstr;
        final ClassLoader cl = DeployTimeEnhancer.class.getClassLoader();
        try {
            final Class<?> enhancerClass = cl.loadClass("org.apache.openjpa.enhance.PCEnhancer");
            final Class<?> arg2 = cl.loadClass("org.apache.openjpa.lib.util.Options");
            cstr = arg2.getConstructor(Properties.class);
            mtd = enhancerClass.getMethod("run", String[].class, arg2);
        } catch (Exception e) {
            LOGGER.warning("openjpa enhancer can't be found in the contanier, will be skipped");
            mtd = null;
            cstr = null;
        }
        optionsConstructor = cstr;
        enhancerMethod = mtd;
    }

    // TODO: manage jars: unpack();enhance();repack();
    public void enhance(@Observes BeforeDeploymentEvent event) {
        if (enhancerMethod == null) {
            LOGGER.debug("OpenJPA is not available so no deploy-time enhancement will be done");
            return;
        }

        /*
        algorithm could be something like:
        1) browsing all urls (event.getUrls()) to get unpacked persistence.xml and packed persistence.xml (in jar)
        2) for each url parse it with a small sax parser to get jar-file for each one --> list of jar needed by pu
        3) for each persistence.xml aggregate file needed (all of jars, include/exclude could be nice)
        4) for each persistence.xml run enhancer
        5) for all jar unpacked repack it and replace original one
        6) clean up unpacked jar (don't delete already unpacked folder like WEB-INF/classes ;))
         */

        final Properties opts = options(event.getUrls());
        final Object optsArg;
        try {
            optsArg = optionsConstructor.newInstance(opts);
        } catch (Exception e) {
            LOGGER.debug("can't create options for enhancing");
            return;
        }

        if (opts.containsKey(PROPERTIES_FILE_PROP)) {
            LOGGER.info("enhancing url(s): " + Arrays.asList(event.getUrls()));
            // TODO: manage lib folder
            try {
                enhancerMethod.invoke(null, toFilePaths(event.getUrls()), optsArg);
            } catch (Exception e) {
                LOGGER.warning("can't enhanced at deploy-time entities", e);
            }
        } else {
            LOGGER.debug("no persistence.xml so no enhancing");
        }
    }

    private Properties options(final URL[] urls) {
        final Properties props = new Properties();
        final String pXmls = getWarPersistenceXml(urls);
        if (!pXmls.isEmpty()) {
            props.setProperty(PROPERTIES_FILE_PROP, pXmls);
        }
        return props;
    }

    private String getWarPersistenceXml(final URL[] urls) { // META-INF/persistence.xml will be managed by jar, not here
        final StringBuilder files = new StringBuilder();
        for (URL url : urls) {
            final File dir = URLs.toFile(url);
            if (!dir.isDirectory()) {
                continue;
            }

            if (dir.getAbsolutePath().endsWith("/WEB-INF/classes")) {
                final File pXml = new File(dir, "META-INF/persistence.xml");
                if (pXml.exists()) {
                    files.append(pXml.getAbsolutePath());
                }

                final File pXml2 = new File(dir.getParentFile(), "persistence.xml");
                if (pXml2.exists()) {
                    if (!files.toString().isEmpty()) {
                        files.append(",");
                    }
                    files.append(pXml2.getAbsolutePath());
                }
            }
        }
        return files.toString();
    }

    private String[] toFilePaths(final URL[] urls) {
        final List<String> files = new ArrayList<String>();
        for (URL url : urls) {
            final File dir = URLs.toFile(url);
            if (!dir.isDirectory()) {
                continue;
            }

            for (File f : Files.collect(dir, new ClassFilter())) {
                files.add(f.getAbsolutePath());
            }
        }
        return files.toArray(new String[files.size()]);
    }

    private static class ClassFilter implements FileFilter {
        @Override
        public boolean accept(final File file) {
            return file.getName().endsWith(CLASS_EXT);
        }
    }
}
