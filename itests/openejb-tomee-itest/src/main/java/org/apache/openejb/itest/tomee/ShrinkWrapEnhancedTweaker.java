package org.apache.openejb.itest.tomee;

import org.apache.openejb.arquillian.common.Files;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;

public class ShrinkWrapEnhancedTweaker extends SimpleTweaker {
    private final ServerTweaker decorated;
    private final Archive<?> archive;

    public ShrinkWrapEnhancedTweaker(final ServerTweaker tweaker, final Archive<?> archive) {
        this.decorated = tweaker;
        this.archive = archive;
    }

    @Override
    public void tweak(final File home) {
        decorated.tweak(home);

        final File dump;
        if (archive instanceof WebArchive) {
            dump = webApp(home, archive.getName());
        } else {
            dump = archive(home, archive.getName());
        }
        Files.mkdir(dump.getParentFile());
        archive.as(ZipExporter.class).exportTo(dump);
    }
}
