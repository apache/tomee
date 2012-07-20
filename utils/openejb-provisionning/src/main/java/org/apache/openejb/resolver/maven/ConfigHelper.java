package org.apache.openejb.resolver.maven;

import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.util.property.PropertiesPropertyResolver;

public final class ConfigHelper {
    private ConfigHelper() {
        // no-op
    }

    public static MavenConfigurationImpl createConfig() {
        final MavenConfigurationImpl config = new MavenConfigurationImpl(
                new PropertiesPropertyResolver( System.getProperties() ), "org.ops4j.pax.url.mvn");

        config.setSettings( new MavenSettingsImpl( config.getSettingsFileUrl(), config.useFallbackRepositories() ) );
        return config;
    }
}
