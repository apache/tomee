/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.WebXml;
import org.apache.catalina.startup.ContextConfig;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.reflection.ReflectionUtil;

import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.LinkedHashSet;

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

    public class OpenEJBWebXml extends WebXml {
        public static final String OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY = "openejb.web.xml.major";

        private String prefix;

        public OpenEJBWebXml(String prefix) {
            this.prefix = prefix;

            // some hack since tomcat doesn't preserve order of jsppropertygroup because of the hashset
            // to remove if tomcat fixes it.
            try {
                ReflectionUtil.set(this, "jspPropertyGroups", new LinkedHashSet<JspPropertyGroupDescriptor>());
            } catch (OpenEJBException e) {
                // ignored, applications often work even with this error...which shouldn't happen often
            }
        }

        @Override public int getMajorVersion() {
            return Integer.parseInt(SystemInstance.get().getProperty(prefix + "." + OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY),
                    Integer.parseInt(SystemInstance.get().getProperty(OPENEJB_WEB_XML_MAJOR_VERSION_PROPERTY, Integer.toString(super.getMajorVersion()))));
        }
    }
}
