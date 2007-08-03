/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: WebSessionBean.java 445605 2005-08-04 01:20:07Z dblevins $
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
