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

import org.apache.openejb.client.event.RemoteInitialContextCreated;
import org.omg.CORBA.ORB;

import javax.naming.*;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
public class JNDIContext implements InitialContextFactory, Context {

    public static final String DEFAULT_PROVIDER_URL = "ejbd://localhost:4201";

    private String tail = "/";
    private ServerMetaData server;
    private ClientMetaData client;
    private Hashtable env;
    private String moduleId;
    private ClientInstance clientIdentity;

    public JNDIContext() {
    }

    /*
     * A neater version of clone
     */
    public JNDIContext(final JNDIContext that) {
        this.tail = that.tail;
        this.server = that.server;
        this.client = that.client;
        this.moduleId = that.moduleId;
        this.env = (Hashtable) that.env.clone();
        this.clientIdentity = that.clientIdentity;
    }

    private JNDIResponse request(final JNDIRequest req) throws Exception {
        req.setServerHash(server.buildHash());

        final JNDIResponse response = new JNDIResponse();
        Client.request(req, response, server);
        if (null != response.getServer()) {
            server.merge(response.getServer());
        }
        return response;
    }

    protected AuthenticationResponse requestAuthorization(final AuthenticationRequest req) throws RemoteException {
        return (AuthenticationResponse) Client.request(req, new AuthenticationResponse(), server);
    }

    @Override
    public Context getInitialContext(final Hashtable environment) throws NamingException {
        if (environment == null) {
            throw new NamingException("Invalid argument, hashtable cannot be null.");
        } else {
            env = (Hashtable) environment.clone();
        }

        final String userID = (String) env.get(Context.SECURITY_PRINCIPAL);
        final String psswrd = (String) env.get(Context.SECURITY_CREDENTIALS);
        String providerUrl = (String) env.get(Context.PROVIDER_URL);
        moduleId = (String) env.get("openejb.client.moduleId");

        final URI location;
        try {
            providerUrl = addMissingParts(providerUrl);
            location = new URI(providerUrl);
        } catch (URISyntaxException e) {
            throw (ConfigurationException) new ConfigurationException("Property value for " + Context.PROVIDER_URL + " invalid: " + providerUrl + " - " + e.getMessage()).initCause(e);
        }
        this.server = new ServerMetaData(location);

        final Client.Context context = Client.getContext(this.server);
        context.getProperties().putAll(environment);

        final String strategy = context.getOptions().get("openejb.client.connection.strategy", "default");
        context.getClusterMetaData().setConnectionStrategy(strategy);

        Client.fireEvent(new RemoteInitialContextCreated(location));

        //TODO:1: Either aggressively initiate authentication or wait for the
        //        server to send us an authentication challange.
        if (userID != null) {
            authenticate(userID, psswrd);
        } else {
            client = new ClientMetaData();
        }

        return this;
    }

    /**
     * Add missing parts - expected only part of the required providerUrl
     * <p/>
     * TODO: Move the check to a place where it really belongs - ConnectionManager, ConnectionFactory or such
     * This method (class in general) doesn't really know what is required as far as connection details go
     * Assuming that java.net.URI or java.net.URL are going to be used is overly stated
     */
    String addMissingParts(String providerUrl) throws URISyntaxException {

        final int port = Integer.parseInt(System.getProperty("ejbd.port", "4201"));

        if (providerUrl == null || providerUrl.length() == 0) {
            providerUrl = "ejbd://localhost:" + port;
        } else {

            final int colonIndex = providerUrl.indexOf(":");
            final int slashesIndex = providerUrl.indexOf("//");

            if (colonIndex == -1 && slashesIndex == -1) {   // hostname or ip address only
                providerUrl = "ejbd://" + providerUrl + ":" + port;
            } else if (colonIndex == -1) {
                final URI providerUri = new URI(providerUrl);
                final String scheme = providerUri.getScheme();
                if (!(scheme.equals("http") || scheme.equals("https"))) {
                    providerUrl = providerUrl + ":" + port;
                }
            } else if (slashesIndex == -1) {
                providerUrl = "ejbd://" + providerUrl;
            }
        }
        return providerUrl;
    }

    public void authenticate(final String userID, final String psswrd) throws AuthenticationException {

        // May be null
        final String realmName = (String) env.get("openejb.authentication.realmName");

        final AuthenticationRequest req = new AuthenticationRequest(realmName, userID, psswrd);
        final AuthenticationResponse res;

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

    public EJBHomeProxy createEJBHomeProxy(final EJBMetaDataImpl ejbData) {
        final EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(ejbData, server, client);
        final EJBHomeProxy proxy = handler.createEJBHomeProxy();
        handler.ejb.ejbHomeProxy = proxy;

        return proxy;

    }

    private Object createBusinessObject(final Object result) {
        final EJBMetaDataImpl ejb = (EJBMetaDataImpl) result;
        final Object primaryKey = ejb.getPrimaryKey();

        final EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);
        return handler.createEJBObjectProxy();
    }

    @Override
    public Object lookup(String name) throws NamingException {

        if (name == null)
            throw new InvalidNameException("The name cannot be null");
        else if (name.equals(""))
            return new JNDIContext(this);
        else if (name.startsWith("java:"))
            name = name.replaceFirst("^java:", "");
        else if (!name.startsWith("/"))
            name = tail + name;

        final String prop = name.replaceFirst("comp/env/", "");
        String value = System.getProperty(prop);
        if (value != null) {
            return parseEntry(prop, value);
        }

        if (name.equals("comp/ORB")) {
            return getDefaultOrb();
        }

        final JNDIRequest req = new JNDIRequest();
        req.setRequestMethod(RequestMethodCode.JNDI_LOOKUP);
        req.setRequestString(name);
        req.setModuleId(moduleId);

        final JNDIResponse res;
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
                final JNDIContext subCtx = new JNDIContext(this);
                if (!name.endsWith("/"))
                    name += '/';
                subCtx.tail = name;
                return subCtx;

            case ResponseCodes.JNDI_DATA_SOURCE:
                return createDataSource((DataSourceMetaData) res.getResult());

            case ResponseCodes.JNDI_WEBSERVICE:
                return createWebservice((WsMetaData) res.getResult());

            case ResponseCodes.JNDI_RESOURCE:
                final String type = (String) res.getResult();
                value = System.getProperty("Resource/" + type);
                if (value == null) {
                    return null;
                }
                return parseEntry(prop, value);

            case ResponseCodes.JNDI_REFERENCE:
                final Reference ref = (Reference) res.getResult();
                try {
                    return NamingManager.getObjectInstance(ref, getNameParser(name).parse(name), this, env);
                } catch (Exception e) {
                    throw (NamingException) new NamingException("Could not dereference " + ref).initCause(e);
                }

            case ResponseCodes.JNDI_NOT_FOUND:
                throw new NameNotFoundException(name + " does not exist in the system.  Check that the app was successfully deployed.");

            case ResponseCodes.JNDI_NAMING_EXCEPTION:
                final Throwable throwable = ((ThrowableArtifact) res.getResult()).getThrowable();
                if (throwable instanceof NamingException) {
                    throw (NamingException) throwable;
                }
                throw (NamingException) new NamingException().initCause(throwable);

            case ResponseCodes.JNDI_RUNTIME_EXCEPTION:
                throw (RuntimeException) res.getResult();

            case ResponseCodes.JNDI_ERROR:
                throw (Error) res.getResult();

            default:
                throw new ClientRuntimeException("Invalid response from server: " + res.getResponseCode());
        }
    }

    private Object parseEntry(final String name, String value) throws NamingException {
        try {
            URI uri = new URI(value);
            final String scheme = uri.getScheme();
            if (scheme.equals("link")) {
                value = System.getProperty(uri.getSchemeSpecificPart());
                if (value == null) {
                    return null;
                }
                return parseEntry(name, value);
            } else if (scheme.equals("datasource")) {
                uri = new URI(uri.getSchemeSpecificPart());
                final String driver = uri.getScheme();
                final String url = uri.getSchemeSpecificPart();
                return new ClientDataSource(driver, url, null, null);
            } else if (scheme.equals("connectionfactory")) {
                return build(uri);
            } else if (scheme.equals("javamail")) {
                return javax.mail.Session.getDefaultInstance(new Properties());
            } else if (scheme.equals("orb")) {
                return getDefaultOrb();
            } else if (scheme.equals("queue")) {
                return build(uri);
            } else if (scheme.equals("topic")) {
                return build(uri);
            } else {
                throw new UnsupportedOperationException("Unsupported Naming URI scheme '" + scheme + "'");
            }
        } catch (URISyntaxException e) {
            throw (NamingException) new NamingException("Unparsable jndi entry '" + name + "=" + value + "'.  Exception: " + e.getMessage()).initCause(e);
        }
    }

    private Object build(final URI inputUri) throws URISyntaxException {
        final URI uri = new URI(inputUri.getSchemeSpecificPart());
        final String driver = uri.getScheme();
        final String url = uri.getSchemeSpecificPart();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null)
            getClass().getClassLoader();
        if (classLoader == null)
            ClassLoader.getSystemClassLoader();
        try {
            final Class<?> clazz = Class.forName(driver, true, classLoader);
            final Constructor<?> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(url);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot use " + driver + " with parameter " + url, e);
        }
    }

    private DataSource createDataSource(final DataSourceMetaData dataSourceMetaData) {
        return new ClientDataSource(dataSourceMetaData);
    }

    private Object createWebservice(final WsMetaData webserviceMetaData) throws NamingException {
        try {
            return webserviceMetaData.createWebservice();
        } catch (Exception e) {
            throw (NamingException) new NamingException("Error creating webservice").initCause(e);
        }
    }

    private ORB getDefaultOrb() {
        return ORB.init();
    }

    @Override
    public Object lookup(final Name name) throws NamingException {
        return lookup(name.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        if (name == null)
            throw new InvalidNameException("The name cannot be null");
        else if (name.startsWith("java:"))
            name = name.replaceFirst("^java:", "");
        else if (!name.startsWith("/"))
            name = tail + name;

        final JNDIRequest req = new JNDIRequest(RequestMethodCode.JNDI_LIST, name);
        req.setModuleId(moduleId);

        final JNDIResponse res;
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
                final Throwable throwable = ((ThrowableArtifact) res.getResult()).getThrowable();
                if (throwable instanceof NamingException) {
                    throw (NamingException) throwable;
                }
                throw (NamingException) new NamingException().initCause(throwable);

            case ResponseCodes.JNDI_ERROR:
                throw (Error) res.getResult();

            default:
                throw new ClientRuntimeException("Invalid response from server :" + res.getResponseCode());
        }

    }

    @Override
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        return list(name.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        final Object o = lookup(name);
        if (o instanceof Context) {
            final Context context = (Context) o;
            final NamingEnumeration<NameClassPair> enumeration = context.list("");
            final List<NameClassPair> bindings = new ArrayList<NameClassPair>();

            while (enumeration.hasMoreElements()) {
                final NameClassPair pair = enumeration.nextElement();
                bindings.add(new LazyBinding(pair.getName(), pair.getClassName(), context));
            }

            return new NameClassPairEnumeration(bindings);

        } else {
            return null;
        }

    }

    private static class LazyBinding extends Binding {

        private static final long serialVersionUID = 1L;
        private RuntimeException failed;
        private Context context;

        public LazyBinding(final String name, final String className, final Context context) {
            super(name, className, null);
            this.context = context;
        }

        @Override
        public synchronized Object getObject() {
            if (super.getObject() == null) {
                if (failed != null)
                    throw failed;
                try {
                    super.setObject(context.lookup(getName()));
                } catch (NamingException e) {
                    throw failed = new ClientRuntimeException("Failed to lazily fetch the binding '" + getName() + "'", e);
                }
            }
            return super.getObject();
        }
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        return listBindings(name.toString());
    }

    @Override
    public Object lookupLink(final String name) throws NamingException {
        return lookup(name);
    }

    @Override
    public Object lookupLink(final Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    @Override
    public NameParser getNameParser(final String name) throws NamingException {
        return new SimpleNameParser();
    }

    @Override
    public NameParser getNameParser(final Name name) throws NamingException {
        return new SimpleNameParser();
    }

    @Override
    public String composeName(final String name, final String prefix) throws NamingException {
        throw new OperationNotSupportedException("TODO: Needs to be implemented");
    }

    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        throw new OperationNotSupportedException("TODO: Needs to be implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object addToEnvironment(final String key, final Object value) throws NamingException {
        return env.put(key, value);
    }

    @Override
    public Object removeFromEnvironment(final String key) throws NamingException {
        return env.remove(key);
    }

    @Override
    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) env.clone();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return "";
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public void bind(final String name, final Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(final Name name, final Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    @Override
    public void rebind(final String name, final Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(final Name name, final Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    @Override
    public void unbind(final String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void unbind(final Name name) throws NamingException {
        unbind(name.toString());
    }

    @Override
    public void rename(final String oldname, final String newname) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(final Name oldname, final Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    @Override
    public void destroySubcontext(final String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(final Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    @Override
    public Context createSubcontext(final String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Context createSubcontext(final Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    private static final class SimpleNameParser implements NameParser {

        private static final Properties PARSER_PROPERTIES = new Properties();

        static {
            PARSER_PROPERTIES.put("jndi.syntax.direction", "left_to_right");
            PARSER_PROPERTIES.put("jndi.syntax.separator", "/");
        }

        private SimpleNameParser() {
        }

        @Override
        public Name parse(final String name) throws NamingException {
            return new CompoundName(name, PARSER_PROPERTIES);
        }
    }

}

