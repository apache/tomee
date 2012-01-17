package org.apache.openejb.resolver.maven;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

public class ManualWagonProvider implements WagonProvider {
    public Wagon lookup(String roleHint)
            throws Exception {
        if ("file".equals(roleHint)) {
            return new FileWagon();
        } else if ("http".equals(roleHint)) {
            return new LightweightHttpWagon();
        } else if ("https".equals(roleHint)) {
            return new LightweightHttpsWagon();
        }
        return null;
    }

    public void release(Wagon wagon) {
        // no-op
    }
}
