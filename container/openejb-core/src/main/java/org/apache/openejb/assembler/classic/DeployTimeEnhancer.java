package org.apache.openejb.assembler.classic;

import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URLs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

public class DeployTimeEnhancer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_DEPLOY, DeployTimeEnhancer.class);

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

    public void enhance(@Observes BeforeDeploymentEvent event) {
        if (enhancerMethod == null) {
            LOGGER.debug("OpenJPA is not available so no deploy-time enhancement will be done");
            return;
        }

        LOGGER.info(">>> enhancing urls: " + Arrays.asList(event.getUrls()));
        try { // TODO: manage jars: unpack();enhance();repack();
            enhancerMethod.invoke(null, toFilePaths(event.getUrls()), options());
        } catch (Exception e) {
            LOGGER.warning("can't enhanced at deploy-time entities", e);
        }
    }

    private Object options() {
        final Properties props = new Properties();
        try {
            return optionsConstructor.newInstance(props);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] toFilePaths(final URL[] urls) {
        final String[] str = new String[urls.length];
        int i = 0;
        for (URL url : urls) {
            str[i] = URLs.toFilePath(url);
            if (!str[i].endsWith("/")) {
                str[i] += "/";
            }
            str[i] += "**/*.class";
            i++;
        }
        return str;
    }
}
