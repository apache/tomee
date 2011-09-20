package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.startup.ContextConfig;

/**
 * @author rmannibucau
 */
public class OpenEJBContextConfig extends ContextConfig {
    @Override protected WebXml createWebXml() {
        return new OpenEJBWebXml();
    }

    private class OpenEJBWebXml extends WebXml {
        @Override public int getMajorVersion() {
            return Integer.getInteger("openejb.web.xml.major", super.getMajorVersion());
        }
    }
}
