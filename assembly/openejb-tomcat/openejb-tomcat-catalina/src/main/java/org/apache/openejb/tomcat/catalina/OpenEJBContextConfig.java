package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.startup.ContextConfig;

/**
 * @author rmannibucau
 */
public class OpenEJBContextConfig extends ContextConfig {
    @Override protected WebXml createWebXml() {
        String prefix = "";
        if (context instanceof StandardContext) {
            StandardContext standardContext = (StandardContext) context;
            prefix = standardContext.getEncodedPath();
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }
        }
        return new OpenEJBWebXml(prefix);
    }

    private class OpenEJBWebXml extends WebXml {
        public static final String OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY = "openejb.web.xml.major";
        private String prefix;

        public OpenEJBWebXml(String prefix) {
            this.prefix = prefix;
        }

        @Override public int getMajorVersion() {
            return Integer.getInteger(prefix + "." + OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY,
                    Integer.getInteger(OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY, super.getMajorVersion()));
        }
    }
}
