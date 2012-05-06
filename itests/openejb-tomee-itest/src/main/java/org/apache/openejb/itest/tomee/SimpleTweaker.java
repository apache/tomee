package org.apache.openejb.itest.tomee;

import java.io.File;
import java.io.IOException;
import org.apache.openejb.loader.IO;

public class SimpleTweaker implements ServerTweaker {
    @Override
    public void tweak(final File home) {
        // no-op
    }

    protected void addLib(final File home, final String groupId, final String artifactId, final String version) {
        final File jar = findJar(groupId, artifactId, version);
        addLib(home, jar);
    }

    protected void addLib(final File home, final File jar) {
        copy(jar, new File(home, "lib/" + jar.getName()));
    }

    protected void addWebapp(final File home, final String groupId, final String artifactId, final String version) {
        final File war = findWebapp(groupId, artifactId, version);
        addWebapp(home, war);
    }

    protected void addWebapp(final File home, final File war) {
        copy(war, webApp(home, war.getName()));
    }

    protected void addArchive(final File home, final File war) {
        copy(war, archive(home, war.getName()));
    }

    protected File webApp(final File home, final String name) {
        return new File(home, "webapps/" + name);
    }

    protected File archive(final File home, final String name) {
        return new File(home, "apps/" + name);
    }

    protected File findJar(final String groupId, final String artifactId, final String version) {
        return Repository.getArtifact(groupId, artifactId, version, "jar", null);
    }

    protected File findWebapp(final String groupId, final String artifactId, final String version) {
        return Repository.getArtifact(groupId, artifactId, version, "war", null);
    }

    protected void copy(final File from, final File to) {
        try {
            IO.copy(from, to);
        } catch (IOException e) {
            throw new ITRuntimeException(e);
        }
    }
}
