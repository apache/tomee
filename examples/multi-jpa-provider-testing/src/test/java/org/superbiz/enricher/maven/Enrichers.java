package org.superbiz.enricher.maven;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.jboss.shrinkwrap.resolver.api.maven.filter.ScopeFilter;

import javax.enterprise.inject.ResolutionException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Enrichers {
    private static final Map<String, File[]> CACHE = new HashMap<String, File[]>();

    private Enrichers() {
     // no-op
    }

    public static File[] resolve(final String pom) {
        if (!CACHE.containsKey(pom)) {
            try { // try offline first since it is generally faster
                CACHE.put(pom, DependencyResolvers.use(MavenDependencyResolver.class)
                        .goOffline()
                        .loadEffectivePom(pom)
                        .importAnyDependencies(new ScopeFilter("compile"))
                        .resolveAsFiles());
            } catch (ResolutionException re) { // try on central
                CACHE.put(pom, DependencyResolvers.use(MavenDependencyResolver.class)
                        .loadEffectivePom(pom)
                        .importAnyDependencies(new ScopeFilter("compile"))
                        .resolveAsFiles());
            }
        }
        return CACHE.get(pom);
    }

    public static WebArchive wrap(final Archive<?> archive) {
        if (!(archive instanceof WebArchive)) {
            throw new IllegalArgumentException("not supported kind of archive: " + archive.getClass().getName());
        }
        return (WebArchive) archive;
    }
}
