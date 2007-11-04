/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

// todo deal with handlers
public class JaxRpcServiceReference extends Reference {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, JaxRpcServiceReference.class);

    private String serviceClassName;
    private ClassLoader classLoader;
    private URI wsdlURI;
    private QName serviceQName;
    private String referenceClassName;

    public JaxRpcServiceReference(QName serviceQName, URI wsdlURI, String referenceClassName, String serviceClassName, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.serviceQName = serviceQName;
        this.wsdlURI = wsdlURI;
        this.referenceClassName = referenceClassName;
        this.serviceClassName = serviceClassName;
    }

    public Object getObject() throws NamingException {
        Class<? extends Service> serviceClass = loadClass(serviceClassName).asSubclass(Service.class);
        Class<?> referenceClass = getReferenceClass();

        if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
            serviceClass = referenceClass.asSubclass(Service.class);
        }

        try {
            Service instance;
            if (Service.class.equals(serviceClass)) {
                instance = ServiceFactory.newInstance().createService(getWsdlURL(), serviceQName);
            } else {
                try {
                    instance = serviceClass.getConstructor(URL.class, QName.class).newInstance(getWsdlURL(), serviceQName);
                } catch (Throwable e) {
                    throw (NamingException) new NamingException("Could not instantiate jax-ws service class " + serviceClass.getName()).initCause(e);
                }
            }

            if (referenceClass != null && !Service.class.isAssignableFrom(referenceClass)) {
                // do port lookup
                return instance.getPort(referenceClass);
            } else {
                // return service
                return instance;
            }
        } catch (ServiceException e) {
            throw (NamingException) new NamingException("Error creating service proxy").initCause(e);
        }
    }

    private Class getReferenceClass() throws NamingException {
        if (referenceClassName == null) return null;
        return loadClass(referenceClassName);
    }

    private Class<?> loadClass(String name) throws NamingException {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            NamingException exception = new NamingException("Count not load class " + name);
            exception.initCause(e);
            throw exception;
        }
    }

    private URL getWsdlURL() {
        if (wsdlURI == null) return null;

        try {
            return new URL(wsdlURI.toString());
        } catch (MalformedURLException e) {
            URL wsdlURL = classLoader.getResource(this.wsdlURI.toString());
            if (wsdlURL == null) {
                logger.warning("Error obtaining WSDL: " + this.wsdlURI, e);
            }
            return wsdlURL;
        }
    }
}
