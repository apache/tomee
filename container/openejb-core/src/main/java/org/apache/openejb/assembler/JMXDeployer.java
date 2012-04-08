package org.apache.openejb.assembler;

import java.util.Collection;
import java.util.Properties;
import javax.management.Description;
import javax.management.MBean;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.core.LocalInitialContextFactory;

@MBean
@Description("OpenEJB Deployer")
public class JMXDeployer {
    @ManagedOperation
    @Description("Deploy the specified application")
    public String deploy(final String location) {
        try {
            deployer().deploy(location);
            return "OK";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    @ManagedOperation
    @Description("Undeploy the specified application")
    public String undeploy(final String moduleId) {
        try {
            deployer().undeploy(moduleId);
            return "OK";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    @ManagedAttribute
    @Description("List available applications")
    public String[] getDeployedApplications() {
        try {
            final Collection<AppInfo> apps = deployer().getDeployedApps();
            final String[] appsNames = new String[apps.size()];
            int i = 0;
            for (AppInfo info : apps) {
                appsNames[i++] = info.path;
            }
            return appsNames;
        } catch (Exception e) {
            return new String[] { "ERR:" + e.getMessage() };
        }
    }

    private static Deployer deployer() throws NamingException {
        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DeployerEjb.class.getClassLoader());
        try {
            return (Deployer) new InitialContext(p).lookup("openejb/DeployerBusinessRemote");
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
