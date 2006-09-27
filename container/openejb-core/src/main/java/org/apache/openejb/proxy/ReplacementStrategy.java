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
package org.apache.openejb.proxy;

import java.io.ObjectStreamException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;


public interface ReplacementStrategy {
    Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException ;
    
    static final ReplacementStrategy COPY = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            return new ImmutableArtifact(object);
        }
    };
    
    static final ReplacementStrategy PASSIVATE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            return object;
        }
    };
    
    static final ReplacementStrategy REPLACE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            //TODO: I have plans to clean/speed this up.  This really only happens on serialization to an external VM, so it isn't much of a performance issue anyway.
            
            if (object instanceof EJBObject){
                return org.apache.openejb.OpenEJB.getApplicationServer().getEJBObject(proxyInfo);
            } else if (object instanceof EJBHome){
                return org.apache.openejb.OpenEJB.getApplicationServer().getEJBHome(proxyInfo);
            } else if (object instanceof EJBMetaData){
                return org.apache.openejb.OpenEJB.getApplicationServer().getEJBMetaData(proxyInfo);
            } else if (object instanceof HandleImpl){
                HandleImpl handle = (HandleImpl)object;
                
                if (handle.type == HandleImpl.HANDLE){
                    return org.apache.openejb.OpenEJB.getApplicationServer().getHandle(proxyInfo);
                } else {
                    return org.apache.openejb.OpenEJB.getApplicationServer().getHomeHandle(proxyInfo);
                }
            } else /*should never happen */ {
                return object;
            }
        }
    };


    static final ReplacementStrategy IN_VM_REPLACE = new ReplacementStrategy(){
        public Object writeReplace(Object object, ProxyInfo proxyInfo) {
            if (object instanceof EJBObject){
                return ProxyMemento.createEjbObject(proxyInfo);
            } else if (object instanceof EJBHome){
                return ProxyMemento.createEjbHome(proxyInfo);
            } else if (object instanceof EJBMetaData){
                return ProxyMemento.createEjbMetaData(proxyInfo);
            } else if (object instanceof HandleImpl){
                HandleImpl handle = (HandleImpl)object;

                if (handle.type == HandleImpl.HANDLE){
                    return ProxyMemento.createHandle(proxyInfo);
                } else {
                    return ProxyMemento.createHomeHanldle(proxyInfo);
                }
            } else /*should never happen */ {
                return object;
            }
        }
    };

}
