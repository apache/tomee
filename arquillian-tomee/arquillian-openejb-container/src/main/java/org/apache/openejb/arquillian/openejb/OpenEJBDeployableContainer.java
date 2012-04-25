package org.apache.openejb.arquillian.openejb;

import java.io.File;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class OpenEJBDeployableContainer implements DeployableContainer<OpenEJBConfiguration> {
    private static final String DEPLOYMENT_SUB_DIR = "/arquillian-openejb-working-dir";
    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        PROPERTIES.setProperty(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        PROPERTIES.setProperty(DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY, "false");
    }

    private InitialContext initialContext;
    private DeployerEjb deployer;
    private File baseDeploymentDir;
    private File archiveFile;
    private ContainerSystem containerSystem;


    @Inject
    @DeploymentScoped
    private InstanceProducer<AppContext> appContextProducer;

    @Inject
    @ContainerScoped
    private InstanceProducer<ContainerSystem> containerSystemProducer;

    @Override
    public Class<OpenEJBConfiguration> getConfigurationClass() {
        return OpenEJBConfiguration.class;
    }

    @Override
    public void setup(final OpenEJBConfiguration openEJBConfiguration) {
        // no-op
    }

    @Override
    public void start() throws LifecycleException {
        try {
            initialContext = new InitialContext(PROPERTIES);
        } catch (NamingException e) {
            throw new LifecycleException("can't start the OpenEJB container", e);
        }

        deployer = new DeployerEjb();
        baseDeploymentDir = new File(System.getProperty("java.io.tmpdir") + DEPLOYMENT_SUB_DIR);
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        containerSystemProducer.set(containerSystem);
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        int i = 0;
        do {
            archiveFile = new File(baseDeploymentDir, i++ + "/" + archive.getName());
        } while (archiveFile.exists());
        Files.mkdirs(archiveFile.getParentFile());
        archive.as(ZipExporter.class).exportTo(archiveFile, true);

        try {
            final AppInfo info = deployer.deploy(archiveFile.getCanonicalPath());
            final AppContext appCtx = containerSystem.getAppContext(info.appId);
            appContextProducer.set(appCtx);
        } catch (Exception e) {
            throw new DeploymentException("can't deploy " + archive.getName(), e);
        }
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        try {
            deployer.undeploy(archiveFile.getCanonicalPath());
            Files.delete(archiveFile);
        } catch (Exception e) {
            throw new DeploymentException("can't undeploy " + archive.getName(), e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            if (initialContext != null) {
                initialContext.close();
            }
        } catch (NamingException e) {
            throw new LifecycleException("can't close the OpenEJB container", e);
        } finally {
            OpenEJB.destroy();
            Files.delete(baseDeploymentDir);
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }
}
