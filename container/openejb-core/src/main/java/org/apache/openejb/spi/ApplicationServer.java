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

package org.apache.openejb.spi;

import org.apache.openejb.ProxyInfo;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;

/**
 * <h2><b>LOCAL to REMOTE SERIALIZATION</b></h2> <p>
 *
 * <i>Definition:</i><p>
 * This is a serialization that initiates in the local vm, but
 * is outside the scope of a marked IntraVM local serialization.
 *
 * <i>Circumstances:</i><p>
 * When an IntraVM implementation of a jakarta.ejb.* interface is
 * serialized outside the scope of the IntraVM Server
 *
 * These serializations happen when objects are sent from a
 * local bean to a remote client as part of a return value, or
 * when a stateful session bean is passified.
 *
 * <i>Action:</i><p>
 * Don't serialize the IntraVM jakarta.ejb.* interface
 * implementation, instead ask the ApplicationServer to nominate
 * its own implementation as a replacement.  This is done via
 * the org.apache.openejb.spi.ApplicationServer interface.
 *
 * <i>Example Scenario:</i><p>
 * SERIALIZATION<br>
 * <br>1.  ObjectOutputStream encounters an IntraVmMetaData instance
 * in the object graph and calls its writeReplace method.
 * <br>2.  The IntraVmMetaData instance determines it is being
 * serialized outside the scope of an IntraVM serialization
 * by calling IntraVmCopyMonitor.isIntraVmCopyOperation().
 * <br>3.  The IntraVmMetaData instance calls the getEJBMetaData
 * method on the ApplicationServer.
 * <br>4.  The IntraVmMetaData instance returns the
 * ApplicationServer's EJBMetaData instance from the
 * writeReplace method.
 * <br>5.  The ObjectOutputStream serializes the ApplicationServer's
 * EJBMetaData instance in place of the IntraVmMetaData
 * instance.
 *
 * Note:  The ApplicationServer's EJBMetaData instance can
 * be any object that implements the jakarta.ejb.EJBMetaData
 * interface and can also implement any serialization
 * methods, such as the writeReplace method, to nominate a
 * replacement or implement protocol specific logic or
 * otherwise gain control over the serialization of
 * EJBMetaData instances destined for its remote clients.
 *
 * DESERIALIZATION<p>
 * The deserialization of the Application Server's
 * jakarta.ejb.* implementations is implementation specific.
 *
 *
 * @version $Revision$ $Date$
 */
public interface ApplicationServer {

    EJBMetaData getEJBMetaData(ProxyInfo proxyInfo);

    Handle getHandle(ProxyInfo proxyInfo);

    HomeHandle getHomeHandle(ProxyInfo proxyInfo);

    EJBObject getEJBObject(ProxyInfo proxyInfo);

    Object getBusinessObject(ProxyInfo proxyInfo);

    EJBHome getEJBHome(ProxyInfo proxyInfo);

}