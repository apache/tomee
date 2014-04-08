/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.NoEndPointException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class GenericServiceEndpoint extends org.apache.axis.client.Stub {
    public GenericServiceEndpoint(QName portQName, Service service, URL location) {
        this.service = service;
        cachedEndpoint = location;
        cachedPortName = portQName;

    }

    Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call =
                    (org.apache.axis.client.Call) service.createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            //TODO figure out if this can be done during deployment!
//            synchronized (this) {
//                if (firstCall()) {
//                    // must set encoding style before registering serializers
//                    //TODO these constants probably need to be parameters of GSE.
//                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
////                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
//                    //override unsigned long mapping
//                    _call.registerTypeMapping(BigInteger.class,
//                            Constants.XSD_UNSIGNEDLONG,
//                            new SimpleSerializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG),
//                            new SimpleDeserializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG));
//                    _call.registerTypeMapping(URI.class,
//                            Constants.XSD_ANYURI,
//                            new SimpleSerializerFactory(URI.class, Constants.XSD_ANYURI),
//                            new SimpleDeserializerFactory(URI.class, Constants.XSD_ANYURI));
//                    for (Iterator iterator = typeInfo.iterator(); iterator.hasNext();) {
//                        TypeInfo info = (TypeInfo) iterator.next();
//                        _call.registerTypeMapping(info.getClazz(), info.getqName(), info.getSerFactoryClass(), info.getDeserFactoryClass(), false);
//                    }
//                }
//            }
            return _call;
        } catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", t);
        }
    }

    void checkCachedEndpoint() throws NoEndPointException {
        if (cachedEndpoint == null) {
            throw new NoEndPointException();
        }
    }

    void setUpCall(Call call) throws AxisFault {
        setRequestHeaders(call);
        setAttachments(call);
    }
}
