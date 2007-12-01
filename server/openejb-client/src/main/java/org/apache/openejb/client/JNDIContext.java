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

import org.omg.CORBA.ORB;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import javax.naming.AuthenticationException;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.ServiceUnavailableException;
import javax.naming.NameClassPair;
import javax.naming.Binding;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;


public class JNDIContext implements Serializable, InitialContextFactory, Context {

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

    protected AuthenticationResponse requestAuthorization(AuthenticationRequest req) throws RemoteException {
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

        if (serverURI == null) serverURI = "ejbd://localhost:4201";
        // if (userID == null) userID = "anonymous";
        // if (psswrd == null) psswrd = "anon";

        String uriString = (String) serverURI;
        URI location = null;
        try {
            location = new URI(uriString);
        } catch (Exception e) {
            if (uriString.indexOf("://") == -1) {
                try {
                    location = new URI("foo://" + uriString);
                } catch (URISyntaxException giveUp) {
                    // Was worth a try, let's give up and throw the original exception.
                    throw (ConfigurationException) new ConfigurationException("Context property value error for " + Context.PROVIDER_URL + " :" + e.getMessage()).initCause(e);
                }
            }
        }
        this.server = new ServerMetaData(location);
        //TODO:1: Either aggressively initiate authentication or wait for the
        //        server to send us an authentication challange.
        if (userID != null) {
            authenticate(userID, psswrd);
        } else {
            client = new ClientMetaData();
        }

        return this;
    }

    public void authenticate(String userID, String psswrd) throws AuthenticationException {

        // May be null
        String realmName = (String) env.get("openejb.authentication.realmName");

        AuthenticationRequest req = new AuthenticationRequest(realmName, userID, psswrd);
        AuthenticationResponse res = null;

        try {
            res = requestAuthorization(req);
        } catch (RemoteException e) {
            throw new AuthenticationException(e.getLocalizedMessage());
        }

        switch (res.getResponseCode()) {
            case ResponseCodes.AUTH_GRANTED:
                client = res.getIdentity();
                break;
            case ResponseCodes.AUTH_REDIRECT:
                client = res.getIdentity();
                server = res.getServer();
                break;
            case ResponseCodes.AUTH_DENIED:
                throw (AuthenticationException) new AuthenticationException("This principle is not authorized.").initCause(res.getDeniedCause());
        }
    }

    public EJBHomeProxy createEJBHomeProxy(EJBMetaDataImpl ejbData) {

        EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejbData, server, client);
        EJBHomeProxy proxy = handler.createEJBHomeProxy();
        handler.ejb.ejbHomeProxy = proxy;

        return proxy;

    }

    private Object createBusinessObject(Object result) {
        EJBMetaDataImpl ejb = (EJBMetaDataImpl) result;
        Object primaryKey = ejb.getPrimaryKey();

        EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);
        return handler.createEJBObjectProxy();
    }

    public Object lookup(String name) throws NamingException {

        if (name == null) throw new InvalidNameException("The name cannot be null");
        else if (name.equals("")) return new JNDIContext(this);
        else if (name.startsWith("java:")) name = name.replaceFirst("^java:", "");
        else if (!name.startsWith("/")) name = tail + name;

        String prop = name.replaceFirst("comp/env/", "");
        String value = System.getProperty(prop);
        if (value != null) {
            return parseEntry(prop, value);
        }

        if (name.equals("comp/ORB")) {
            return getDefaultOrb();
        }

        JNDIRequest req = new JNDIRequest();
        req.setRequestMethod(RequestMethodConstants.JNDI_LOOKUP);
        req.setRequestString(name);
        req.setModuleId(moduleId);

        JNDIResponse res = null;
        try {
            res = request(req);
        } catch (Exception e) {
            if (e instanceof RemoteException && e.getCause() instanceof ConnectException) {
                e = (Exception) e.getCause();
                throw (ServiceUnavailableException) new ServiceUnavailableException("Cannot lookup '" + name + "'.").initCause(e);
            }
            throw (NamingException) new NamingException("Cannot lookup '" + name + "'.").initCause(e);
        }

        switch (res.getResponseCode()) {
            case ResponseCodes.JNDI_EJBHOME:
                return createEJBHomeProxy((EJBMetaDataImpl) res.getResult());

            case ResponseCodes.JNDI_BUSINESS_OBJECT:
                return createBusinessObject(res.getResult());

            case ResponseCodes.JNDI_OK:
                return res.getResult();

            case ResponseCodes.JNDI_INJECTIONS:
                return res.getResult();

            case ResponseCodes.JNDI_CONTEXT:
                JNDIContext subCtx = new JNDIContext(this);
                if (!name.endsWith("/")) name += '/';
                subCtx.tail = name;
                return subCtx;

            case ResponseCodes.JNDI_DATA_SOURCE:
                return createDataSource((DataSourceMetaData) res.getResult());

            case ResponseCodes.JNDI_WEBSERVICE:
                return createWebservice((WsMetaData) res.getResult());

            case ResponseCodes.JNDI_RESOURCE:
                String type = (String) res.getResult();
                value = System.getProperty("Resource/" + type);
                if (value == null) {
                    return null;
                }
                return parseEntry(prop, value);

            case ResponseCodes.JNDI_NOT_FOUND:
                throw new NameNotFoundException(name + " does not exist in the system.  Check that the app was successfully deployed.");

            case ResponseCodes.JNDI_NAMING_EXCEPTION:
                Throwable throwable = ((ThrowableArtifact) res.getResult()).getThrowable();
                if (throwable instanceof NamingException) {
                    throw (NamingException) throwable;
                }
                throw (NamingException) new NamingException().initCause(throwable);

            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
                throw (RuntimeException) res.getResult();

            case ResponseCodes.JNDI_ERROR:
                throw (Error) res.getResult();

            default:
                throw new RuntimeException("Invalid response from server :" + res.getResponseCode());
        }
    }

    private Object parseEntry(String name, String value) throws NamingException {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme.equals("link")) {
                value = System.getProperty(uri.getSchemeSpecificPart());
                if (value == null) {
                    return null;
                }
                return parseEntry(name, value);
            } else if (scheme.equals("datasource")) {
                uri = new URI(uri.getSchemeSpecificPart());
                String driver = uri.getScheme();
                String url = uri.getSchemeSpecificPart();
                return new ClientDataSource(driver, url, null, null);
            } else if (scheme.equals("connectionfactory")) {
                uri = new URI(uri.getSchemeSpecificPart());
                String driver = uri.getScheme();
                String url = uri.getSchemeSpecificPart();
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) getClass().getClassLoader();
                if (classLoader == null) ClassLoader.getSystemClassLoader();
                try {
                    Class<?> clazz = Class.forName(driver, true, classLoader);
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    Object connectionFactory = constructor.newInstance(url);
                    return connectionFactory;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot use ConnectionFactory in client VM without the classh: "+driver, e);
                }
            } else if (scheme.equals("javamail")) {
                return javax.mail.Session.getDefaultInstance(new Properties());
            } else if (scheme.equals("orb")) {
                return getDefaultOrb();
            } else {
                throw new UnsupportedOperationException("Unsupported Naming URI scheme '" + scheme + "'");
            }
        } catch (URISyntaxException e) {
            throw (NamingException) new NamingException("Unparsable jndi entry '" + name + "=" + value + "'.  Exception: " + e.getMessage()).initCause(e);
        }
    }

    private DataSource createDataSource(DataSourceMetaData dataSourceMetaData) {
        return new ClientDataSource(dataSourceMetaData);
    }

    private Object createWebservice(WsMetaData webserviceMetaData) throws NamingException {
        try {
            return webserviceMetaData.createWebservice();
        } catch (Exception e) {
            throw (NamingException) new NamingException("Error creating webservice").initCause(e);
        }
    }

    private ORB getDefaultOrb() {
        return ORB.init();
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        if (name == null) throw new InvalidNameException("The name cannot be null");
        else if (name.startsWith("java:")) name = name.replaceFirst("^java:", "");
        else if (!name.startsWith("/")) name = tail + name;

        JNDIRequest req = new JNDIRequest(RequestMethodConstants.JNDI_LIST, name);
        req.setModuleId(moduleId);

        JNDIResponse res = null;
        try {
            res = request(req);
        } catch (Exception e) {
            if (e instanceof RemoteException && e.getCause() instanceof ConnectException) {
                e = (Exception) e.getCause();
                throw (ServiceUnavailableException) new ServiceUnavailableException("Cannot list '" + name + "'.").initCause(e);
            }
            throw (NamingException) new NamingException("Cannot list '" + name + "'.").initCause(e);
        }

        switch (res.getResponseCode()) {

            case ResponseCodes.JNDI_OK:
                return null;

            case ResponseCodes.JNDI_ENUMERATION:
                return (NamingEnumeration) res.getResult();

            case ResponseCodes.JNDI_NOT_FOUND:
                throw new NameNotFoundException(name);

            case ResponseCodes.JNDI_NAMING_EXCEPTION:
                Throwable throwable = ((ThrowableArtifact) res.getResult()).getThrowable();
                if (throwable instanceof NamingException) {
                    throw (NamingException) throwable;
                }
                throw (NamingException) new NamingException().initCause(throwable);

            case ResponseCodes.JNDI_ERROR:
                throw (Error) res.getResult();

            default:
                throw new RuntimeException("Invalid response from server :" + res.getResponseCode());
        }

    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        Object o = lookup(name);
        if (o instanceof Context) {
            Context context = (Context) o;
            NamingEnumeration<NameClassPair> enumeration = context.list("");
            List<NameClassPair> bindings = new ArrayList<NameClassPair>();

            while (enumeration.hasMoreElements()) {
                NameClassPair pair = enumeration.nextElement();
                bindings.add(new LazyBinding(pair.getName(), pair.getClassName(), context));
            }

            return new NameClassPairEnumeration(bindings);

        } else {
            return null;
        }

    }

    private static class LazyBinding extends Binding {
        private RuntimeException failed;
        private Context context;

        public LazyBinding(String name, String className, Context context) {
            super(name, className, null);
            this.context = context;
        }

        public synchronized Object getObject() {
            if (super.getObject() == null){
                if (failed != null) throw failed;
                try {
                    super.setObject(context.lookup(getName()));
                } catch (NamingException e) {
                    throw failed = new RuntimeException("Failed to lazily fetch the binding '"+getName()+"'", e);
                }
            }
            return super.getObject();
        }
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException{
        return listBindings(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException("TODO: Needs to be implemented");
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        throw new OperationNotSupportedException("TODO: Needs to be implemented");
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new OperationNotSupportedException("TODO: Needs to be implemented");
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
        throw new OperationNotSupportedException();
    }

    public void bind(Name name, Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(Name name, Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    public void rename(String oldname, String newname) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(Name oldname, Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

}

