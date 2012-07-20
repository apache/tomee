package org.apache.openejb.resolver.maven;

import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.version.Version;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

public final class VersionResolver {
    private VersionResolver() {
        // no-op
    }

    public static VersionRangeResult versions(final String info, final String defaultVersion) {
        final MavenConfigurationImpl config = ConfigHelper.createConfig();
        try {
            final Parser parser = new Parser(info);
            final AetherBasedResolver resolver = new AetherBasedResolver(config, parser.getRepositoryURL());
            return resolver.resolveVersions(parser.getGroup(), parser.getArtifact(), parser.getClassifier(), parser.getType(), parser.getVersion());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String higestVersion(final String info, final String prefix, final String defaultVersion) {
        final VersionRangeResult result = VersionResolver.versions(info, defaultVersion);
        if (result == null) {
            return defaultVersion;
        }
        final List<Version> versions = result.getVersions();
        Collections.sort(versions); // Version impl comparable so we just need to call it :)
        Version usedVersion = null;
        for (Version current : versions) {
            if (current.toString().startsWith(prefix)) {
                usedVersion = current;
            }
        }
        if (usedVersion != null) {
            return usedVersion.toString();
        }
        return defaultVersion;
    }
}
