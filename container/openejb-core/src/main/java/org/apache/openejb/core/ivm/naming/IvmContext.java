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

package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.core.ivm.naming.java.javaURLContextFactory;
import org.apache.openejb.core.ivm.naming.openejb.openejbURLContextFactory;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.xbean.naming.context.ContextUtil;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.ObjectFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/*
* This class wrappers a specific NameNode which is the data model for the JNDI
* name space. This class provides javax.naming.Context specific functionality
* to the NameNode so that it can be used by beans the JNDI ENC.
*/

/**
 * @org.apache.xbean.XBean element="ivmContext"
 */
public class IvmContext implements Context, Serializable {
    private static final long serialVersionUID = -626353930051783641L;
    Hashtable<String, Object> myEnv;
    boolean readOnly;
    Map<String, Object> fastCache = new ConcurrentHashMap<>();
    static final String JNDI_EXCEPTION_ON_FAILED_WRITE = "openejb.jndiExceptionOnFailedWrite";
    public NameNode mynode;

    public static IvmContext createRootContext() {
        return new IvmContext();
    }

    public IvmContext() {
        this(new NameNode(null, new ParsedName(""), null, null));
    }

    public IvmContext(final String nodeName) {
        this(new NameNode(null, new ParsedName(nodeName), null, null));
    }

    public IvmContext(final NameNode node) {
        this(node, false);
    }
    
    public IvmContext(final NameNode node, final boolean isReadOnly) {
        mynode = node;
        readOnly = isReadOnly;
//        mynode.setMyContext(this);
    }

    public IvmContext(final Hashtable<String, Object> environment) throws NamingException {
        this();
        if (environment == null) {
            throw new NamingException("Invalid Argument");
        } else {
            myEnv = (Hashtable<String, Object>) environment.clone();
        }

    }

    public Object lookup(final String compositName) throws NamingException {
        if (compositName.isEmpty()) {
            return this;
        }

        final String compoundName;
        final int index = compositName.indexOf(':');
        if (index > -1) {

            final String prefix = compositName.substring(0, index);

            String path = compositName.substring(index + 1);
            final ParsedName name = new ParsedName(path);

            if (prefix.equals("openejb")) {
                path = name.path();
                return openejbURLContextFactory.getContext().lookup(path);
            } else if (prefix.equals("java")) {
                if (name.getComponent().equals("openejb")) {
                    path = name.remaining().path();
                    return openejbURLContextFactory.getContext().lookup(path);
                } else {
                    path = name.path();
                    return javaURLContextFactory.getContext().lookup(path);
                }
            } else {
                // we don't know what the prefix means, throw an exception
                throw new NamingException("Unknown JNDI name prefix '" + prefix + ":'");
            }
        } else {
            /*
              the resolve method always starts with the comparison assuming that the first
              component of the name is a context of a peer node or the same node, so we have
              to prepend the current context name to the relative lookup path.
            */
            compoundName = mynode.getAtomicName() + '/' + compositName;
        }

        /*
           If the object has been resolved in the past from this context and the specified path (name)
           it will be in the fastCache which is significantly faster then peruse the Node graph.
           80 ms compared to 300 ms for a full node path search.
        */
        Object obj = fastCache.get(compoundName);
        if (obj == null) {
            try {
                obj = mynode.resolve(new ParsedName(compoundName), readOnly);
            } catch (final NameNotFoundException nnfe) {
                obj = federate(compositName);
            }

            // don't cache proxies
            if (!(obj instanceof IntraVmProxy) && !(obj instanceof ContextualJndiReference)) {
                fastCache.put(compoundName, obj);
            }
        }

        if (obj == null) {
            throw new NameNotFoundException("Name \"" + compositName + "\" not found.");
        }

        if (obj.getClass() == IvmContext.class) {
            ((IvmContext) obj).myEnv = myEnv;
        } else if (obj instanceof Reference) {
            /**
             * EJB references and resource references are wrapped in special
             * org.apache.openejb.core.ivm.naming.Reference types that check to
             * see if the current operation is allowed access to the entry (See EJB 1.1/2.0 Allowed Operations)
             * If the operation is not allowed, a javax.naming.NameNotFoundException is thrown.
             *
             * A Reference type can also carry out dynamic resolution of references if necessary.
             */

            // TODO: JRG - this needs a test
            while (obj instanceof Reference) {
                obj = ((Reference)obj).getObject();
            }
        } else if (obj instanceof LinkRef) {
            obj = lookup(((LinkRef) obj).getLinkName());
        }
        return obj;
    }

    protected Object federate(final String compositName) throws NamingException {
        final ObjectFactory[] factories = getFederatedFactories();
        for (final ObjectFactory factory : factories) {
            try {
                final CompositeName name = new CompositeName(compositName);
                final Object obj = factory.getObjectInstance(null, name, null, null);

                if (obj instanceof Context) {
                    return ((Context) obj).lookup(compositName);
                } else if (obj != null) {
                    return obj;
                }
            } catch (final Exception doNothing) {
                // no-op
            }
        }

        throw new NameNotFoundException("Name \"" + compositName + "\" not found.");
    }

    static ObjectFactory[] federatedFactories;

    public static ObjectFactory[] getFederatedFactories() throws NamingException {
        if (federatedFactories == null) {
            final Set<ObjectFactory> factories = new HashSet<>();
            final String urlPackagePrefixes = getUrlPackagePrefixes();
            if (urlPackagePrefixes == null) {
                return new ObjectFactory[0];
            }
            for (final StringTokenizer tokenizer = new StringTokenizer(urlPackagePrefixes, ":"); tokenizer.hasMoreTokens(); ) {
                final String urlPackagePrefix = tokenizer.nextToken();
                final String className = urlPackagePrefix + ".java.javaURLContextFactory";
                if (className.equals("org.apache.openejb.core.ivm.naming.java.javaURLContextFactory")) {
                    continue;
                }
                try {
                    final ClassLoader cl = ClassLoaderUtil.getContextClassLoader();
                    final Class factoryClass = Class.forName(className, true, cl);
                    final ObjectFactory factoryInstance = (ObjectFactory) factoryClass.newInstance();
                    factories.add(factoryInstance);
                } catch (final ClassNotFoundException cnfe) {
                    // no-op

                } catch (final Throwable e) {
                    final NamingException ne = new NamingException("Federation failed: Cannot instantiate " + className);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            final Object[] temp = factories.toArray();
            federatedFactories = new ObjectFactory[temp.length];
            System.arraycopy(temp, 0, federatedFactories, 0, federatedFactories.length);
        }
        return federatedFactories;
    }

    private static String getUrlPackagePrefixes() {
        // 1. System.getProperty
        String urlPackagePrefixes = JavaSecurityManagers.getSystemProperty(Context.URL_PKG_PREFIXES);

        // 2. Thread.currentThread().getContextClassLoader().getResources("jndi.properties")
        if (urlPackagePrefixes == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }

            try {
                final Enumeration<URL> resources = classLoader.getResources("jndi.properties");
                while (urlPackagePrefixes == null && resources.hasMoreElements()) {
                    final URL resource = resources.nextElement();
                    final InputStream in = IO.read(resource);
                    urlPackagePrefixes = getUrlPackagePrefixes(in);
                }
            } catch (final IOException ignored) {
                // no-op
            }
        }

        // 3. ${java.home}/lib/jndi.properties
        if (urlPackagePrefixes == null) {
            final String javahome = JavaSecurityManagers.getSystemProperty("java.home");
            if (javahome != null) {
                InputStream in = null;
                try {
                    final File propertiesFile = new File(new File(javahome, "lib"), "jndi.properties");
                    in = IO.read(propertiesFile);
                    urlPackagePrefixes = getUrlPackagePrefixes(in);
                } catch (final FileNotFoundException ignored) {
                    // no-op
                } finally {
                    IO.close(in);
                }
            }

        }
        return urlPackagePrefixes;
    }

    private static String getUrlPackagePrefixes(final InputStream in) {
        try {
            final Properties properties = IO.readProperties(in, new Properties());
            return properties.getProperty(Context.URL_PKG_PREFIXES);
        } catch (final IOException e) {
            return null;
        }
    }

    public Object lookup(final Name compositName) throws NamingException {
        return lookup(compositName.toString());
    }

    public void bind(String name, final Object obj) throws NamingException {
        if(checkReadOnly()) {
            return;
        }
        final int indx = name.indexOf(':');
        if (indx > -1) {
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx + 1);
        }
        if (fastCache.containsKey(name)) {
            throw new NameAlreadyBoundException();
        } else {
            final ParsedName parsedName = getParsedNameFor(name);
            mynode.bind(parsedName, obj);
        }
    }

    private ParsedName getParsedNameFor(String name){
        return new ParsedName(mynode.getAtomicName() + "/" + name);
    }

    public void bind(final Name name, final Object obj) throws NamingException {
        bind(name.toString(), obj);
    }

    public void rebind(final String name, final Object obj) throws NamingException {
        try {
            unbind(name);
        } catch (final NameNotFoundException e) {
            // no-op
        }
        bind(name, obj);
    }

    public void rebind(final Name name, final Object obj) throws NamingException {
        rebind(name.toString(), obj);
    }

    public void unbind(String name) throws NamingException {
        if(checkReadOnly()) {
            return;
        }
        final int indx = name.indexOf(':');
        if (indx > -1) {
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx + 1);
        }
        fastCache.clear();
        mynode.clearCache();

        mynode.unbind(getParsedNameFor(name));
    }

    public void unbind(final Name name) throws NamingException {
        unbind(name.toString());
    }

    public void prune(final String name) throws NamingException {
        final IvmContext ctx = (IvmContext) lookup(name);
        ctx.prune();
    }

    public void prune() throws NamingException {
        mynode.prune();
    }

    public void rename(final String oldname, final String newname) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(final Name oldname, final Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        final Object obj = lookup(name);
        if (obj.getClass() == IvmContext.class) {
            return new MyListEnumeration(((IvmContext) obj).mynode);
        } else {
            return null;
        }
    }

    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        final Object obj = lookup(name);
        if (obj.getClass() == IvmContext.class) {
            return new MyBindingEnumeration(((IvmContext) obj).mynode);
        } else {
            return null;
        }
    }

    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        return listBindings(name.toString());
    }

    public void destroySubcontext(final String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(final Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name) throws NamingException {
        if(checkReadOnly()) {
            //TODO: null is fine if there is a one time - 10 calls will log a single time - log line (warning?)
            return null;
        }
        final int indx = name.indexOf(':');
        if (indx > -1) {
            /*
          The ':' character will be in the path if its an absolute path name starting with the schema
          'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx + 1);
        }
        if (fastCache.containsKey(name)) {
            throw new NameAlreadyBoundException();
        } else {
            return mynode.createSubcontext(getParsedNameFor(name), readOnly);
        }
    }

    public Context createSubcontext(final Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    public Object lookupLink(final String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(final Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(final String name) throws NamingException {
        return ContextUtil.NAME_PARSER;
    }

    public NameParser getNameParser(final Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(final String name, final String prefix) throws NamingException {
        final Name result = composeName(new CompositeName(name),
                new CompositeName(prefix));
        return result.toString();
    }

    public Name composeName(final Name name, final Name prefix) throws NamingException {
        final Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }

    public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable<>(5);
        }
        return myEnv.put(propName, propVal);
    }

    public Object removeFromEnvironment(final String propName) throws NamingException {
        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {

            return new Hashtable(3);
        } else {
            return (Hashtable) myEnv.clone();
        }
    }

    public String getNameInNamespace() throws NamingException {
        return "";
    }

    public void close() throws NamingException {
        /*
         * 10.4.4. Container Provider Responsibility
         *
         * The container must ensure that the enterprise bean instances have only read access to their environment
         * variables. The container must throw the javax.naming.OperationNotSupportedException from all the methods of
         * the javax.naming.Context interface that modify the environment naming context and its subcontexts.
         *
         * We can question if close() is modifying the context. But from what TCK does, it looks like no.
         * So no need to check read only here. Tomcat does consider it to be a write operation so it fails
         */
    }

    /*
     * return false if current naming context is not marked as read only
     * return true if current naming context is marked as read only and system property jndiExceptionOnFailedWrite is set to false
     * 
     * throws OperationNotSupportedException if naming context:
     *   - is marked as read only and
     *   - system property jndiExceptionOnFailedWrite is set to true
     *   
     * jndiExceptionOnFailedWrite property is defined by tomcat and is used in similar context for web app components
     * https://tomcat.apache.org/tomcat-7.0-doc/config/context.html#jndiExceptionOnFailedWrite
     * 
     */
    protected boolean checkReadOnly() throws OperationNotSupportedException {
        if (readOnly) {
            //alignment with tomcat behavior
            if("true".equals(SystemInstance.get().getProperty(JNDI_EXCEPTION_ON_FAILED_WRITE,"true"))) {
                throw new OperationNotSupportedException();
            }
            return true;
        }
        return false;
    }
    
    public void setReadOnly(boolean isReadOnly) {
        this.readOnly = isReadOnly;
        if(mynode != null) {
            mynode.setReadOnly(readOnly);
        }
    }

    protected class MyBindingEnumeration extends MyNamingEnumeration {

        public MyBindingEnumeration(final NameNode parentNode) {
            super(parentNode);
        }

        protected void buildEnumeration(final Vector vect) {
            for (int i = 0; i < vect.size(); i++) {
                final NameNode node = (NameNode) vect.elementAt(i);
                final String className = node.getBinding().getClass().getName();
                vect.setElementAt(new Binding(node.getAtomicName(), className, node.getBinding()), i);
            }
            myEnum = vect.elements();
        }

    }

    protected class MyListEnumeration extends MyNamingEnumeration {

        public MyListEnumeration(final NameNode parentNode) {
            super(parentNode);
        }

        protected void buildEnumeration(final Vector vect) {
            for (int i = 0; i < vect.size(); i++) {
                final NameNode node = (NameNode) vect.elementAt(i);
                final String className = node.getBinding().getClass().getName();
                vect.setElementAt(new NameClassPair(node.getAtomicName(), className), i);
            }
            myEnum = vect.elements();
        }

    }

    protected abstract class MyNamingEnumeration implements NamingEnumeration {

        Enumeration myEnum;

        public MyNamingEnumeration(final NameNode parentNode) {
            final Vector vect = new Vector();

            NameNode node = parentNode.getSubTree();

            if (node == null) {
                node = parentNode;
            } else {
                vect.addElement(node);
            }

            gatherNodes(parentNode, node, vect);

            buildEnumeration(vect);
        }

        protected abstract void buildEnumeration(Vector<NameNode> vect);

        protected void gatherNodes(NameNode initiallyRequestedNode, final NameNode node, final Vector vect) {
            addInListIfNeeded(initiallyRequestedNode, node.getLessTree(), vect);
            addInListIfNeeded(initiallyRequestedNode, node.getGrtrTree(), vect);
            addInListIfNeeded(initiallyRequestedNode, node.getSubTree(), vect);

            if (NameNode.Federation.class.isInstance(initiallyRequestedNode.getObject())) { // tomcat mainly
                for (final Context c : NameNode.Federation.class.cast(initiallyRequestedNode.getObject())) {
                    if (c == IvmContext.this || !IvmContext.class.isInstance(c)) {
                        continue;
                    }

                    final IvmContext ctx = IvmContext.class.cast(c);
                    if (ctx.mynode == node || vect.contains(ctx.mynode)) {
                        continue;
                    }

                    addInListIfNeeded(ctx.mynode, ctx.mynode.getGrtrTree(), vect);
                    addInListIfNeeded(ctx.mynode, ctx.mynode.getLessTree(), vect);
                    addInListIfNeeded(ctx.mynode, ctx.mynode.getSubTree(), vect);
                }
            }
        }

        private void addInListIfNeeded(final NameNode parent, final NameNode node, final Vector vect) {
            if (node == null || vect.contains(node) || !isMyChild(parent, node)) {
                return;
            }
            vect.addElement(node);
            gatherNodes(parent, node, vect);
        }

        private boolean isMyChild(final NameNode parent, final NameNode node) {
            if (node.getParent() == parent) {
                return true;
            }

            /*
             * Handle the special case of the top-level contexts like global, module, app, etc
             */
            if (null == node.getParent() && null == parent.getParentTree()) {
                return true;
            }

            return false;
        }

        public void close() {
            myEnum = null;
        }

        public boolean hasMore() {
            return hasMoreElements();
        }

        public Object next() {
            return nextElement();
        }

        public boolean hasMoreElements() {
            return myEnum.hasMoreElements();
        }

        public Object nextElement() {
            return myEnum.nextElement();
        }
    }

    public void tree(final PrintStream out) {
        mynode.tree("", out);
    }

    @Override
    public String toString() {
        return "IvmContext{" +
                "mynode=" + mynode.getAtomicName() +
                '}';
    }

    protected Object writeReplace() throws ObjectStreamException {
        if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return new JndiEncArtifact(this);
        } else if (IntraVmCopyMonitor.isCrossClassLoaderOperation()) {
            return new JndiEncArtifact(this);
        }

        throw new NotSerializableException("IntraVM java.naming.Context objects can not be passed as arguments");
    }

}
