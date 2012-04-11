package org.apache.openejb.itest.tomee;

import java.io.File;
import org.apache.openejb.resolver.Resolver;

import static org.apache.openejb.loader.ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER;

public class Repository {
    private static final Resolver RESOLVER = new Resolver();

    public static File getArtifact(final String groupId, final String artifactId, final String version, final String type, final String classifier) {
        final String oldCache = System.getProperty(OPENEJB_DEPLOYER_CACHE_FOLDER);
        System.setProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, System.getProperty("tomee.test.it.cache", "target/cache"));

        final StringBuilder builder = new StringBuilder("mvn:")
                .append(groupId).append(":").append(artifactId).append(":")
                .append(version).append(":")
                .append(type);

        if (classifier != null) {
            builder.append(":").append(classifier);
        }

        try {
            return new File(RESOLVER.resolve(builder.toString()));
        } catch (Exception e) {
            throw new ITRuntimeException(e);
        } finally {
            if (oldCache == null) {
                System.clearProperty(OPENEJB_DEPLOYER_CACHE_FOLDER);
            } else {
                System.setProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, oldCache);
            }
        }
    }
}
