package org.superbiz.deltaspike.config;

import java.util.Arrays;
import java.util.List;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

public class MyConfigSourceProvider implements ConfigSourceProvider {
    @Override
    public List<ConfigSource> getConfigSources() {
        return Arrays.asList((ConfigSource) new MyConfigSource());
    }
}
