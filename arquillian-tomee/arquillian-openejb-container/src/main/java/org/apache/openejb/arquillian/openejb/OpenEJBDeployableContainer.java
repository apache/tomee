package org.apache.openejb.arquillian.openejb;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.core.LocalInitialContext;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class OpenEJBDeployableContainer implements DeployableContainer<OpenEJBConfiguration> {
    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        PROPERTIES.setProperty(LocalInitialContext.ON_CLOSE, LocalInitialContext.Close.DESTROY.name());
        PROPERTIES.setProperty(DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY, "false");
        try {
            OpenEJBDeployableContainer.class.getClassLoader().loadClass("org.apache.openejb.server.ServiceManager");
            PROPERTIES.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        } catch (Exception e) {
            // ignored
        }
    }

    // system
    private Assembler assembler;
    private InitialContext initialContext;
    private ContainerSystem containerSystem;
    private ConfigurationFactory configurationFactory;

    // suite
    private AppInfo appInfo;

    @Inject
    @DeploymentScoped
    private InstanceProducer<AppContext> appContextProducer;

    @Inject
    @ContainerScoped
    private InstanceProducer<ContainerSystem> containerSystemProducer;

    @Inject
    @SuiteScoped
    private Instance<AppModule> module;

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
        // todo: manage properties (aquillian.xml)
        try {
            initialContext = new InitialContext(PROPERTIES);
        } catch (NamingException e) {
            throw new LifecycleException("can't start the OpenEJB container", e);
        }

        assembler = SystemInstance.get().getComponent(Assembler.class);
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        containerSystemProducer.set(containerSystem);
        configurationFactory = new ConfigurationFactory();
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        try {
            appInfo = configurationFactory.configureApplication(module.get());
            final AppContext appCtx = assembler.createApplication(appInfo);
            appContextProducer.set(appCtx);
        } catch (Exception e) {
            throw new DeploymentException("can't deploy " + archive.getName(), e);
        }
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {
        try {
            assembler.destroyApplication(appInfo.path);
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
