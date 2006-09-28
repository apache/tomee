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
