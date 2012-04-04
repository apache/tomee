package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

public class EnvEntriesMerger extends Merger<Properties> {
    public EnvEntriesMerger(final Log logger) {
        super(logger);
    }

    @Override
    public Properties merge(final Properties reference, final Properties toMerge) {
        for (Object key : toMerge.keySet()) {
            if (reference.containsKey(key)) {
                log.warn("property " + key + " found in multiple env-entries.properties, will be overriden");
            }
        }

        reference.putAll(toMerge);
        return reference;
    }

    @Override
    public Properties createEmpty() {
        return new Properties();
    }

    @Override
    public Properties read(URL url) {
        final Properties read = new Properties();
        try {
            read.load(new BufferedInputStream(url.openStream()));
        } catch (IOException e) {
            // ignored
        }
        return read;
    }

    @Override
    public String descriptorName() {
        return "env-entries.properties";
    }

    @Override
    public void dump(final File dump, final Properties object) throws Exception {
        final Writer writer = new FileWriter(dump);
        try {
            object.store(writer, "merged env-entries.properties");
        } finally {
            writer.close();
        }
    }
}
