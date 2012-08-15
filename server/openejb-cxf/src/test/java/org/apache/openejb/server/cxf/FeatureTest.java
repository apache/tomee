package org.apache.openejb.server.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.server.cxf.fault.AuthenticatorServiceBean;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class FeatureTest {
    @Configuration
    public Properties config() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        }};
    }

    @Module
    public EjbModule app() {
        final EjbJar jar = new EjbJar();
        jar.addEnterpriseBean(new SingletonBean(AuthenticatorServiceBean.class).localBean());

        final OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment(jar.getEnterpriseBeans()[0]));
        openejbJar.getEjbDeployment().iterator().next().getProperties()
                .setProperty(CxfService.OPENEJB_JAXWS_CXF_FEATURES, MyFeature.class.getName());

        final EjbModule module = new EjbModule(jar);
        module.setOpenejbJar(openejbJar);

        return module;
    }

    @Test
    public void run() {
        assertTrue(MyFeature.ok);
    }

    public static class MyFeature extends AbstractFeature {
        public static boolean ok = false;

        @Override
        public void initialize(Server server, Bus bus) {
            ok = true;
        }
    }
}
