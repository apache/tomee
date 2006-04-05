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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * $Id$
 */
package org.openejb.proxy;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;

import org.openejb.DeploymentNotFoundException;


/**
 *
 */
public abstract class SessionEJBHome extends EJBHomeImpl{

    public SessionEJBHome(EJBMethodInterceptor handler) {
        super(handler);
    }
    
    public void remove(Handle handle) throws RemoteException, RemoveException {
        if (getEJBMetaData().isStatelessSession()){
            if (handle == null) {
                throw new RemoveException("Handle is null");
            }
            ProxyInfo proxyInfo = null;
            try {
                proxyInfo = ejbHandler.getProxyInfo();
            } catch (DeploymentNotFoundException e) {
                throw new NoSuchObjectException(e.getMessage());
            }
            Class remoteInterface = proxyInfo.getRemoteInterface();
            if (!remoteInterface.isInstance(handle.getEJBObject())) {
                throw new RemoteException("Handle does not hold a " + remoteInterface.getName());
            }
        } else {
            EJBObject ejbObject = handle.getEJBObject();
            ejbObject.remove();
        }
    }
    
    public void remove(Object primaryKey) throws RemoteException, RemoveException {
        throw new RemoveException("Session objects are private resources and do not have primary keys");
    }
}
