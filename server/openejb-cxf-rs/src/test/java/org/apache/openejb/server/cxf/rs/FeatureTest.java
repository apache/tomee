package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
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
            setProperty(CxfRsHttpListener.OPENEJB_JAXRS_READ_PROPERTIES, "true");
            setProperty(MySecondRestClass.class.getName() + "." + CxfRsHttpListener.OPENEJB_JAXRS_CXF_FEATURES, MyFeature.class.getName());
        }};
    }

    @Module
    public StatelessBean app() {
        final StatelessBean bean = (StatelessBean) new StatelessBean(MySecondRestClass.class).localBean();
        bean.setRestService(true);
        return bean;
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
