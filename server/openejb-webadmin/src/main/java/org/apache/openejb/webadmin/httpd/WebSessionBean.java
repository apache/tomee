/**
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
package org.apache.openejb.webadmin.httpd;

import java.util.HashMap;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

import org.apache.openejb.core.stateful.StatefulEjbObjectHandler;
import org.apache.openejb.util.proxy.ProxyManager;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "httpd/session")
@RemoteHome(WebSessionHome.class)
public class WebSessionBean implements SessionBean {

    private SessionContext ctx;
    private HashMap attributes;

    public void ejbCreate() throws CreateException {
        attributes = new HashMap();
    }

    public String getId() {
        Object obj = ProxyManager.getInvocationHandler(ctx.getEJBObject());
        StatefulEjbObjectHandler handler = (StatefulEjbObjectHandler) obj;
        return handler.getRegistryId() + "";
    }

//    private void uberHack() throws Exception {
//        org.apache.catalina.core.StandardWrapperFacade standardWrapperFacade = (org.apache.catalina.core.StandardWrapperFacade)config;
//        org.apache.catalina.core.ContainerBase containerBase = (org.apache.catalina.core.ContainerBase)standardWrapperFacade.config;
//        org.apache.catalina.core.ContainerBase containerBase2 = (org.apache.catalina.core.ContainerBase)containerBase.parent;
//        java.util.HashMap children = (java.util.HashMap)containerBase2.children;
//        org.apache.catalina.core.StandardWrapper standardWrapper = (org.apache.catalina.core.StandardWrapper) children.get("jsp");
//        org.apache.jasper.servlet.JspServlet jspServlet = (org.apache.jasper.servlet.JspServlet)standardWrapper.instance;
//        jspServlet.rctxt;
//    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void ejbActivate() {

    }

    public void ejbPassivate() {

    }

    public void ejbRemove() {

    }

    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
    }
}
