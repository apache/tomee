package org.apache.openejb.resolver;

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resolver.maven.Handler;
import org.apache.openejb.resolver.maven.Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class Resolver {
    public static final String MVN_PREFIX = "mvn:";
    public static final String APP_CACHE = System.getProperty("openejb.deployer.cache.folder", "temp");

    private Resolver() {
        // no-op
    }

    public static String resolve(final String rawLocation) throws Exception {
        if (rawLocation.startsWith(MVN_PREFIX) && rawLocation.length() > MVN_PREFIX.length()) {
            final String info = rawLocation.substring(MVN_PREFIX.length());
            final Parser parser = new Parser(info);
            final File file = new File(SystemInstance.get().getBase().getDirectory(),
                    APP_CACHE + File.separator + parser.getArtifactPath());
            if (!file.exists()) {
                try {
                    final URL url = new URL(MVN_PREFIX.substring(MVN_PREFIX.length() - 1), "localhost", -1, info, new Handler());
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileUtils.copy(new FileOutputStream(file), url.openStream());
                } catch (Exception e) {
                    if (file.exists()) {
                        file.delete();
                    }
                    throw e;
                }
            }
            return file.getPath();
        }
        return rawLocation;
    }
}
