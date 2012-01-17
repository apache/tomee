package org.apache.openejb.resolver.maven;

import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.util.property.PropertiesPropertyResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler  {
    @Override
    protected URLConnection openConnection( final URL url ) throws IOException {
        final MavenConfigurationImpl config = new MavenConfigurationImpl(
                new PropertiesPropertyResolver( System.getProperties() ), "org.ops4j.pax.url.mvn");

        config.setSettings( new MavenSettingsImpl( config.getSettingsFileUrl(), config.useFallbackRepositories() ) );
        return new Connection( url, config );
    }
}
