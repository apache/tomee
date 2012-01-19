package org.apache.openejb.config;

import java.io.File;
import java.net.URI;
import java.util.Set;

// a fake module type to wrap a xml resource file in a module
// to be able to deploy only resources
public class ResourcesModule extends Module implements DeploymentModule {
    @Override
    public String getModuleId() {
        return null;
    }

    @Override
    public URI getModuleUri() {
        return null;
    }

    @Override
    public String getJarLocation() {
        return null;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public ValidationContext getValidation() {
        return null;
    }

    @Override
    public Set<String> getWatchedResources() {
        return null;
    }
}
