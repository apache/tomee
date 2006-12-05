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
package org.apache.openejb.client;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

public class JNDIContext implements Serializable, InitialContextFactory, Context, RequestMethods, ResponseCodes {

    private transient String tail = "/";
    private transient ServerMetaData server;
    private transient ClientMetaData client;
    private transient Hashtable env;
    private String moduleId;

    JNDIContext(Hashtable environment) throws NamingException {
        init(environment);
    }

    public JNDIContext() {
    }

    /*
     * A neater version of clone
     */
    public JNDIContext(JNDIContext that) {
        this.tail = that.tail;
        this.server = that.server;
        this.client = that.client;
        this.moduleId = that.moduleId;
        this.env = (Hashtable) that.env.clone();
    }

    public void init(Hashtable environment) throws NamingException {
    }

    private JNDIResponse request(JNDIRequest req) throws Exception {
        return (JNDIResponse) Client.request(req, new JNDIResponse(), server);
    }

    public static void print(String s) {

    }

    public static void println(String s) {

    }

    protected AuthenticationResponse requestAuthorization(AuthenticationRequest req) throws java.rmi.RemoteException {
        return (AuthenticationResponse) Client.request(req, new AuthenticationResponse(), server);
    }

    public Context getInitialContext(Hashtable environment) throws NamingException {
        if (environment == null)
            throw new NamingException("Invalid Argument, hashtable cannot be null.");
        else
            env = (Hashtable) environment.clone();

        String userID = (String) env.get(Context.SECURITY_PRINCIPAL);
        String psswrd = (String) env.get(Context.SECURITY_CREDENTIALS);
        Object serverURI = env.get(Context.PROVIDER_URL);
        moduleId = (String) env.get("openejb.client.moduleId");

        if (serverURI == null) serverURI = "foo://localhost:4201";
        if (userID == null) userID = "anonymous";
        if (psswrd == null) psswrd = "anon";

        String uriString = (String) serverURI;
        URI location = null;
        try {
            location = new URI(uriString);
        } catch (Exception e) {
            if (uriString.indexOf("://") == -1) {
                try {
                    location = new URI("foo://"+uriString);
                } catch (URISyntaxException giveUp) {
                    // Was worth a try, let's give up and throw the original exception.
                    throw (ConfigurationException)new ConfigurationException("Context property value error for "+Context.PROVIDER_URL + " :"+e.getMessage()).initCause(e);
                }
            }
        }
        this.server = new ServerMetaData(location);
        //TODO:1: Either aggressively initiate authentication or wait for the 
        //        server to send us an authentication challange.
        authenticate(userID, psswrd);

        return this;
    }

    public void authenticate(String userID, String psswrd) throws javax.naming.AuthenticationException {

        AuthenticationRequest req = new AuthenticationRequest(userID, psswrd);
        AuthenticationResponse res = null;

        try {
            res = requestAuthorization(req);
        } catch (java.rmi.RemoteException e) {
            throw new AuthenticationException(e.getLocalizedMessage());
        }

        switch (res.getResponseCode()) {
            case AUTH_GRANTED:
                client = res.getIdentity();
                break;
            case AUTH_REDIRECT:
                client = res.getIdentity();
                server = res.getServer();
                break;
            case AUTH_DENIED:
                throw new javax.naming.AuthenticationException("This principle is not authorized.");
        }
    }

    public EJBHomeProxy createEJBHomeProxy(EJBMetaDataImpl ejbData) {

        EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejbData, server, client);
        EJBHomeProxy proxy = handler.createEJBHomeProxy();
        handler.ejb.ejbHomeProxy = proxy;

        return proxy;

    }

    private Object createBusinessObject(Object result) {
        Object[] data = (Object[]) result;
        EJBMetaDataImpl ejb = (EJBMetaDataImpl) data[0];
        Object primaryKey = data[1];

        EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);
        return handler.createEJBObjectProxy();
    }

    public Object lookup(String name) throws NamingException {

        if (name == null) throw new InvalidNameException("The name cannot be null");
        else if (name.equals("")) return new JNDIContext(this);
        else if (name.startsWith("java:")) name = name.replaceFirst("^java:","");
        else if (!name.startsWith("/")) name = tail + name;

        String prop = name.replaceFirst("comp/env/","");
        String value = System.getProperty(prop);
        if (value != null){
            return parseEntry(prop, value);
        }

        JNDIRequest req = new JNDIRequest();
        req.setRequestMethod(JNDIRequest.JNDI_LOOKUP);
        req.setRequestString(name);
        req.setModuleId(moduleId);

        JNDIResponse res = null;
        try {
            res = request(req);
        } catch (Exception e) {
            throw (NamingException) new NamingException("Cannot lookup " + name + ": Received error: " + e.getMessage()).initCause(e);
        }

        switch (res.getResponseCode()) {
            case JNDI_EJBHOME:

                return createEJBHomeProxy((EJBMetaDataImpl) res.getResult());

            case JNDI_BUSINESS_OBJECT:
                return createBusinessObject(res.getResult());

            case JNDI_OK:
                return res.getResult();

            case JNDI_INJECTIONS:
                return res.getResult();

            case JNDI_CONTEXT:
                JNDIContext subCtx = new JNDIContext(this);
                if (!name.endsWith("/")) name += '/';
                subCtx.tail = name;
                return subCtx;
            case JNDI_DATA_SOURCE:
                return createDataSource((DataSourceMetaData)res.getResult());
            case JNDI_NOT_FOUND:
                throw new NameNotFoundException(name + " not found");

            case JNDI_NAMING_EXCEPTION:
                throw (NamingException) res.getResult();

            case JNDI_RUNTIME_EXCEPTION:
                throw (RuntimeException) res.getResult();

            case JNDI_ERROR:
                throw (Error) res.getResult();
            default:
                throw new RuntimeException("Invalid response from server :" + res.getResponseCode());
        }
    }

    private Object parseEntry(String name, String value) throws NamingException {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme.equals("datasource")){
                uri = new URI(uri.getSchemeSpecificPart());
                String driver = uri.getScheme();
                String url = uri.getSchemeSpecificPart();
                return new ClientDataSource(driver, url, null, null);
            } else {
                throw new UnsupportedOperationException("Unsupported Naming URI scheme '"+scheme+"'");
            }
        } catch (URISyntaxException e) {
            throw (NamingException) new NamingException("Unparsable jndi entry '"+name+"="+value+"'.  Exception: "+e.getMessage()).initCause(e);
        }
    }

    private DataSource createDataSource(DataSourceMetaData dataSourceMetaData) {
        return new ClientDataSource(dataSourceMetaData);
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public NamingEnumeration list(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new javax.naming.NamingException("TODO: Needs to be implemented");
    }

    public Object addToEnvironment(String key, Object value) throws NamingException {
        return env.put(key, value);
    }

    public Object removeFromEnvironment(String key) throws NamingException {
        return env.remove(key);
    }

    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) env.clone();
    }

    public String getNameInNamespace() throws NamingException {
        return "";
    }

    public void close() throws NamingException {
    }

    public void bind(String name, Object obj) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();

    }

    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    public void unbind(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();

    }

    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    public void rename(String oldname, String newname)
            throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public void rename(Name oldname, Name newname)
            throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name)
            throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

}

