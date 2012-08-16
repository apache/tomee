package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
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
        final StatelessBean bean = (StatelessBean) new StatelessBean(MySecondRestClass.class).localBean();
        bean.setRestService(true);

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        final EjbModule module = new EjbModule(ejbJar);
        final Resources resources = new Resources();

        final Service beanService = new Service(null);
        beanService.setClassName(MySecondRestClass.class.getName());
        beanService.getProperties().setProperty(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.FEATURES, "my-feature");
        resources.getService().add(beanService);

        final Service feature = new Service("my-feature", null);
        feature.setClassName(MyFeature.class.getName());
        resources.getService().add(feature);

        module.initResources(resources);

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
