package org.apache.openejb.karaf.command;

import org.apache.openejb.assembler.DeployerEjb;

import java.io.File;
import java.util.Properties;

public abstract class DeploymentCommand extends JndiOsgiCommand {
    protected Properties properties(final String path) {
        final Properties properties = new Properties();
        properties.setProperty(DeployerEjb.OPENEJB_DEPLOYER_FORCED_APP_ID_PROP, moduleId(path));
        return properties;
    }

    protected final String moduleId(String path) {
        return path.replace("/", "_").replace(File.pathSeparatorChar, '_');
    }
}
