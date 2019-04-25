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
import org.apache.openejb.client.serializer.EJBDSerializer;

import javax.naming.AuthenticationException;
import javax.naming.Binding;
import javax.naming.CompoundName;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.ServiceUnavailableException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class JNDIContext implements InitialContextFactory, Context {
    private static final Logger LOGGER = Logger.getLogger("OpenEJB.client");

    @SuppressWarnings("UnusedDeclaration")
    public static final String DEFAULT_PROVIDER_URL = "ejbd://localhost:4201";
    public static final String SERIALIZER = "openejb.ejbd.serializer";
    public static final String AUTHENTICATE_WITH_THE_REQUEST = "openejb.ejbd.authenticate-with-request";
    public static final String POOL_QUEUE_SIZE = "openejb.client.invoker.queue";
    @SuppressWarnings("UnusedDeclaration")
    public static final String POOL_THREAD_NUMBER = "openejb.client.invoker.threads";
    public static final String AUTHENTICATION_REALM_NAME = "openejb.authentication.realmName";
    public static final String IDENTITY_TIMEOUT = "tomee.authentication.identity.timeout";
    public static final String BASIC_AUTH_LOGIN = "tomee.ejb.authentication.basic.login";
    public static final String BASIC_AUTH_PASSWORD = "tomee.ejb.authentication.basic.password";

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private String tail = "/";
    private ServerMetaData server;
    private ClientMetaData client;
    private Hashtable env;
    private String moduleId;
    private ClientInstance clientIdentity;

    private static final ThreadPoolExecutor GLOBAL_CLIENT_POOL = newExecutor(10, null);

    static {
        final ClassLoader classLoader = Client.class.getClassLoader();
        Class<?> container;
        try {
            container = Class.forName("org.apache.openejb.OpenEJB", false, classLoader);
        } catch (final Throwable e) {
            container = null;
        }
        if (classLoader == ClassLoader.getSystemClassLoader() || Boolean.getBoolean("openejb.client.flus-tasks")
                || (container != null && container.getClassLoader() == classLoader)) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    waitForShutdown(GLOBAL_CLIENT_POOL);
                }
            });
        }
    }

    private AuthenticationInfo authenticationInfo = null;

    //TODO figure out how to configure and manage the thread pool on the client side, this will do for now...
    private transient int threads;
    private transient LinkedBlockingQueue<Runnable> blockingQueue;

    protected transient ThreadPoolExecutor executorService;

    public static ThreadPoolExecutor globalExecutor() {
        return GLOBAL_CLIENT_POOL;
    }

    private ThreadPoolExecutor executor() {
        if (executorService != null) {
            return executorService;
        }
        if (threads < 0) {
            return GLOBAL_CLIENT_POOL;
        }
        synchronized (this) {
            if (executorService != null) {
                return executorService;
            }
            executorService = newExecutor(threads, blockingQueue);
        }
        return executorService;
    }

    public static ThreadPoolExecutor newExecutor(final int threads, final BlockingQueue<Runnable> blockingQueue) {
        /**
         This thread pool starts with 3 core threads and can grow to the limit defined by 'threads'.
         If a pool thread is idle for more than 1 minute it will be discarded, unless the core size is reached.
         It can accept up to the number of processes defined by 'queue'.
         If the queue is full then an attempt is made to add the process to the queue for 10 seconds.
         Failure to add to the queue in this time will either result in a logged rejection, or if 'block'
         is true then a final attempt is made to run the process in the current thread (the service thread).
         */

        final ThreadPoolExecutor executorService = new ThreadPoolExecutor(3, (threads < 3 ? 3 : threads), 1, TimeUnit.MINUTES, blockingQueue == null ? new LinkedBlockingDeque<Runnable>(Integer.parseInt(getProperty(null, POOL_QUEUE_SIZE, "2"))) : blockingQueue);
        executorService.setThreadFactory(new ThreadFactory() {

            private final AtomicInteger i = new AtomicInteger(0);

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "OpenEJB.Client." + i.incrementAndGet());
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        Logger.getLogger(EJBObjectHandler.class.getName()).log(Level.SEVERE, "Uncaught error in: " + t.getName(), e);
                    }
                });

                return t;
            }

        });

        executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor tpe) {

                if (null == r || null == tpe || tpe.isShutdown() || tpe.isTerminated() || tpe.isTerminating()) {
                    return;
                }

                final Logger log = Logger.getLogger(EJBObjectHandler.class.getName());

                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "EJBObjectHandler ExecutorService at capacity for process: " + r);
                }

                boolean offer = false;
                try {
                    offer = tpe.getQueue().offer(r, 10, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    //Ignore
                }

                if (!offer) {
                    log.log(Level.SEVERE, "EJBObjectHandler ExecutorService failed to run asynchronous process: " + r);
                }
            }
        });
        return executorService;
    }

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

        final boolean authWithRequest = "true".equalsIgnoreCase(String.class.cast(env.get(AUTHENTICATE_WITH_THE_REQUEST)));
        moduleId = (String) env.get("openejb.client.moduleId");

        final URI location;
        try {
            providerUrl = addMissingParts(providerUrl);
            location = new URI(providerUrl);
        } catch (final URISyntaxException e) {
            throw (ConfigurationException) new ConfigurationException("Property value for " +
                    Context.PROVIDER_URL +
                    " invalid: " +
                    providerUrl +
                    " - " +
                    e.getMessage()).initCause(e);
        }
        this.server = new ServerMetaData(location);

        final String basicAuthLogin = (String) env.get(BASIC_AUTH_LOGIN);
        final String basicAuthPassword = (String) env.get(BASIC_AUTH_PASSWORD);
        if (basicAuthLogin != null) {
            this.server = new ServerMetaData(server, basicAuthLogin, basicAuthPassword);
        }

        final Client.Context context = Client.getContext(this.server);
        context.getProperties().putAll(environment);

        final String strategy = context.getOptions().get("openejb.client.connection.strategy", "default");
        context.getClusterMetaData().setConnectionStrategy(strategy);

        Client.fireEvent(new RemoteInitialContextCreated(location));

        //TODO: Either aggressively initiate authentication or wait for the server to send us an authentication challenge.
        if (userID != null) {
            if (!authWithRequest) {
                authenticate(userID, psswrd, false);
            } else {
                authenticationInfo = new AuthenticationInfo(String.class.cast(env.get(AUTHENTICATION_REALM_NAME)), userID, psswrd.toCharArray(), getTimeout(env));
            }
        }
        if (client == null) {
            client = new ClientMetaData();
        }

        seedClientSerializer();

        final int queue = Integer.parseInt(getProperty(env, JNDIContext.POOL_QUEUE_SIZE, "2"));
        blockingQueue = new LinkedBlockingQueue<Runnable>((queue < 2 ? 2 : queue));
        threads = Integer.parseInt(getProperty(env, "openejb.client.invoker.threads", "-1"));

        return this;
    }

    private void seedClientSerializer() {
        final String serializer = (String) env.get(SERIALIZER);
        if (serializer != null) {
            try {
                client.setSerializer(EJBDSerializer.class.cast(Thread.currentThread().getContextClassLoader().loadClass(serializer).newInstance()));
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private long getTimeout(final Hashtable env) {
        final Object o = env.get(IDENTITY_TIMEOUT);
        if (null != o) {
            final Long l = Long.class.cast(o);
            //noinspection ConstantConditions
            if (null != l) {
                return l;
            }
        }

        return 0L;
    }

    private static String getProperty(final Hashtable env, final String key, final String defaultValue) {
        final Object value = env == null ? null : env.get(key);
        if (value != null) {
            return value.toString();
        }
        return System.getProperty(key, defaultValue);
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

    public void authenticate(final String userID, final String psswrd, final boolean logout) throws AuthenticationException {

        final AuthenticationRequest req = new AuthenticationRequest(String.class.cast(env.get(AUTHENTICATION_REALM_NAME)), userID, psswrd, getTimeout(env));

        if (logout) {
            req.setLogoutIdentity(null != client ? client.getClientIdentity() : null);
        }

        final AuthenticationResponse res;
        try {
            res = requestAuthorization(req);
        } catch (final RemoteException e) {
            throw new AuthenticationException(e.getLocalizedMessage());
        }

        switch (res.getResponseCode()) {
            case ResponseCodes.AUTH_GRANTED:
                client = logout ? new ClientMetaData() : res.getIdentity();
                break;
            case ResponseCodes.AUTH_REDIRECT:
                client = logout ? new ClientMetaData() : res.getIdentity();
                server = res.getServer();
                break;
            case ResponseCodes.AUTH_DENIED:
                throw (AuthenticationException) new AuthenticationException("This principle is not authorized.").initCause(res.getDeniedCause());
        }

        seedClientSerializer();
    }

    public EJBHomeProxy createEJBHomeProxy(final EJBMetaDataImpl ejbData) {
        final EJBHomeHandler handler = EJBHomeHandler.createEJBHomeHandler(executor(), ejbData, server, client, authenticationInfo);
        final EJBHomeProxy proxy = handler.createEJBHomeProxy();
        handler.ejb.ejbHomeProxy = proxy;

        return proxy;

    }

    private Object createBusinessObject(final Object result) {
        final EJBMetaDataImpl ejb = (EJBMetaDataImpl) result;
        final Object primaryKey = ejb.getPrimaryKey();

        final EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(executor(), ejb, server, client, primaryKey, authenticationInfo);
        return handler.createEJBObjectProxy();
    }

    @Override
    public Object lookup(String name) throws NamingException {

        checkState();

        if (name == null) {
            throw new InvalidNameException("The name cannot be null");
        } else if (name.equals("")) {
            return new JNDIContext(this);
        } else if (name.startsWith("java:")) {
            name = name.replaceFirst("^java:", "");
        } else if (!name.startsWith("/")) {
            name = tail + name;
        }

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
            if (e instanceof RemoteException) {
                if (e.getCause() instanceof ConnectException) {
                    e = (Exception) e.getCause();
                    throw (ServiceUnavailableException) new ServiceUnavailableException("Cannot lookup '" + name + "'.").initCause(e);
                } else if (AuthenticationException.class.isInstance(e.getCause())) {
                    throw AuthenticationException.class.cast(e.getCause());
                }
            }

            if (e instanceof RemoteException && e.getCause() instanceof AuthenticationException) {
                throw (AuthenticationException) new AuthenticationException(
                        "Cannot Basic Auth into server. Please use " +
                        BASIC_AUTH_LOGIN +
                        " and " +
                        BASIC_AUTH_PASSWORD +
                        " to set up credentials.").initCause(e);
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
                if (!name.endsWith("/")) {
                    name += '/';
                }
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
                } catch (final Exception e) {
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
        } catch (final URISyntaxException e) {
            throw (NamingException) new NamingException("Unparsable jndi entry '" + name + "=" + value + "'.  Exception: " + e.getMessage()).initCause(e);
        }
    }

    private Object build(final URI inputUri) throws URISyntaxException {
        final URI uri = new URI(inputUri.getSchemeSpecificPart());
        final String driver = uri.getScheme();
        final String url = uri.getSchemeSpecificPart();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            getClass().getClassLoader();
        }
        if (classLoader == null) {
            ClassLoader.getSystemClassLoader();
        }
        try {
            final Class<?> clazz = Class.forName(driver, true, classLoader);
            final Constructor<?> constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(url);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot use " + driver + " with parameter " + url, e);
        }
    }

    private DataSource createDataSource(final DataSourceMetaData dataSourceMetaData) {
        return new ClientDataSource(dataSourceMetaData);
    }

    private Object createWebservice(final WsMetaData webserviceMetaData) throws NamingException {
        try {
            return webserviceMetaData.createWebservice();
        } catch (final Exception e) {
            throw (NamingException) new NamingException("Error creating webservice").initCause(e);
        }
    }

    private Object getDefaultOrb() {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass("org.omg.CORBA.ORB").getMethod("init").invoke(null);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("No CORBA available", e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("No CORBA available", e);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("No CORBA available", e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("No CORBA available", e.getCause());
        }
    }

    @Override
    public Object lookup(final Name name) throws NamingException {
        return lookup(name.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {

        checkState();

        if (name == null) {
            throw new InvalidNameException("The name cannot be null");
        } else if (name.startsWith("java:")) {
            name = name.replaceFirst("^java:", "");
        } else if (!name.startsWith("/")) {
            name = tail + name;
        }

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
        private final Context context;

        public LazyBinding(final String name, final String className, final Context context) {
            super(name, className, null);
            this.context = context;
        }

        @Override
        public synchronized Object getObject() {
            if (super.getObject() == null) {
                if (failed != null) {
                    throw failed;
                }
                try {
                    super.setObject(context.lookup(getName()));
                } catch (final NamingException e) {
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

    private void checkState() throws NamingException {
        if (isShutdown.get()) {
            throw new NamingException("Context has been closed. Please create a new instance.");
        }
    }

    @Override
    public void close() throws NamingException {

        if (isShutdown.getAndSet(true)) {
            return;
        }

        waitForShutdown(executorService);

        final String userID = (String) env.get(Context.SECURITY_PRINCIPAL);

        if (userID != null) {
            final String psswrd = (String) env.get(Context.SECURITY_CREDENTIALS);
            final boolean logout = true;
            try {
                this.authenticate(userID, psswrd, logout);
            } catch (final Exception ignore) {
                //no-op
            }
        }
    }

    private static void waitForShutdown(final ExecutorService executor) {
        if (executor == null || executor.isShutdown()) {
            return;
        }

        final List<Runnable> runnables = executor.shutdownNow();
        for (final Runnable r : runnables) {
            try {
                r.run();
            } catch (final Throwable th) {
                LOGGER.log(Level.SEVERE, th.getMessage(), th);
            }
        }
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

    public static class AuthenticationInfo implements Serializable {

        private static final long serialVersionUID = -8898613592355280735L;
        private final String realm;
        private final String user;
        private final char[] password;
        private final long timeout;

        public AuthenticationInfo(final String realm, final String user, final char[] chars) {
            this(realm, user, chars, 0);
        }

        public AuthenticationInfo(final String realm, final String user, final char[] password, final long timeout) {
            this.realm = realm;
            this.user = user;
            this.password = password;
            this.timeout = timeout;
        }

        public String getRealm() {
            return realm;
        }

        public String getUser() {
            return user;
        }

        public char[] getPassword() {
            return password;
        }

        public long getTimeout() {
            return timeout;
        }
    }
}

