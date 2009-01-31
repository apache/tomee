package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;

import java.util.List;

public class MappedNameBuilderTest extends TestCase {
    public void testShouldCreateJndiEntryForBeanWithMappedName() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();
        OpenejbJar openejbJar = new OpenejbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        EjbDeployment ejbDeployment = new EjbDeployment("containerId","deploymentId", "SessionBean");
        openejbJar.addEjbDeployment(ejbDeployment);
        appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));

        appModule = new MappedNameBuilder().deploy(appModule);

        EjbDeployment retrievedDeployment = appModule.getEjbModules().get(0).getOpenejbJar().getDeploymentsByEjbName().get("SessionBean");
        List<Jndi> jndiList = retrievedDeployment.getJndi();

        assertNotNull(jndiList);
        assertEquals(1, jndiList.size());
        assertEquals("MappedName", jndiList.get(0).getName());
        assertEquals("Remote", jndiList.get(0).getInterface());
    }

    public void testIgnoreMappedNameIfOpenejbJarModuleDoesntExist() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        appModule.getEjbModules().add(new EjbModule(ejbJar, null));
        appModule = new MappedNameBuilder().deploy(appModule);

        OpenejbJar openejbJar = appModule.getEjbModules().get(0).getOpenejbJar();
        assertNull(openejbJar);
    }

    public void testShouldIgnoreMappedNameIfDeploymentDoesntExist() throws Exception {
        AppModule appModule = new AppModule(new FakeClassLoader(), "");
        EjbJar ejbJar = new EjbJar();
        OpenejbJar openejbJar = new OpenejbJar();

        SessionBean sessionBean = new SessionBean("SessionBean", "org.superbiz.SessionBean", SessionType.STATELESS);
        sessionBean.setMappedName("MappedName");
        ejbJar.addEnterpriseBean(sessionBean);

        appModule.getEjbModules().add(new EjbModule(ejbJar, openejbJar));
        appModule = new MappedNameBuilder().deploy(appModule);

        EjbDeployment deployment = appModule.getEjbModules().get(0).getOpenejbJar().getDeploymentsByEjbName().get("SessionBean");
        assertNull(deployment);
    }

    private class FakeClassLoader extends ClassLoader {
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return Object.class;
        }
    }
}
