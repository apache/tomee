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
package org.apache.openejb.core.ivm.naming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.ObjectFactory;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;

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
    boolean readOnly = false;
    Map<String, Object> fastCache = new HashMap<String, Object>();
    public NameNode mynode;

    public static IvmContext createRootContext() {
        return new IvmContext(new NameNode(null, new ParsedName("/"), null));
    }

    public IvmContext() {
        this(new NameNode(null, new ParsedName("root"), null));
    }

    public IvmContext(String nodeName) {
        this(new NameNode(null, new ParsedName(nodeName), null));
    }

    public IvmContext(NameNode node) {
        mynode = node;
    }

    public IvmContext(Hashtable<String, Object> environment) throws NamingException {
        this();
        if (environment == null)
            throw new NamingException("Invalid Argument");
        else
            myEnv = (Hashtable<String, Object>) environment.clone();

    }

    public Object lookup(String compositName) throws NamingException {
        if (compositName.equals("")) {
            return this;
        }

        String compoundName;
        int indx = compositName.indexOf(":");
        if (indx > -1) {
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            compoundName = compositName.substring(indx + 1);
        } else {
            /*
              the resolve method always starts with the comparison assuming that the first
              component of the name is a context of a peer node or the same node, so we have
              to prepend the current context name to the relative lookup path.
            */
            compoundName = mynode.atomicName + '/' + compositName;
        }

        /*
           If the object has been resolved in the past from this context and the specified path (name)
           it will be in the fastCache which is significantly faster then peruse the Node graph.
           80 ms compared to 300 ms for a full node path search.
        */
        Object obj = fastCache.get(compoundName);
        if (obj == null) {

            try {
                obj = mynode.resolve(new ParsedName(compoundName));
            } catch (NameNotFoundException nnfe) {
                obj = federate(compositName);
            }

            fastCache.put(compoundName, obj);
        }
        if (obj.getClass() == IvmContext.class)
            ((IvmContext) obj).myEnv = myEnv;
        else if (obj instanceof Reference) {
            /**
             * EJB references and resource references are wrapped in special
             * org.apache.openejb.core.ivm.naming.Reference types that check to
             * see if the current operation is allowed access to the entry (See EJB 1.1/2.0 Allowed Operations)
             * If the operation is not allowed, a javax.naming.NameNotFoundException is thrown.
             *
             * A Reference type can also carry out dynamic resolution of references if necessary.
             */
            obj = ((Reference) obj).getObject();
        } else if (obj instanceof LinkRef) {
            obj = lookup(((LinkRef)obj).getLinkName());
        }
        return obj;
    }

    protected Object federate(String compositName) throws NamingException {
        ObjectFactory factories [] = getFederatedFactories();
        for (ObjectFactory factory : factories) {
            try {
                CompositeName name = new CompositeName(compositName);
                Object obj = factory.getObjectInstance(null, name, null, null);

                if (obj instanceof Context)
                    return ((Context) obj).lookup(compositName);
                else if (obj != null)
                    return obj;
            } catch (Exception doNothing) {
            }
        }

        throw new javax.naming.NameNotFoundException("Name \"" + compositName + "\" not found.");
    }

    static ObjectFactory [] federatedFactories = null;

    public static ObjectFactory [] getFederatedFactories() throws NamingException {
        if (federatedFactories == null) {
            Set<ObjectFactory> factories = new HashSet<ObjectFactory>();
            String urlPackagePrefixes = getUrlPackagePrefixes();
            if (urlPackagePrefixes == null) {
                return new ObjectFactory[0];
            }
            for (StringTokenizer tokenizer = new StringTokenizer(urlPackagePrefixes, ":"); tokenizer.hasMoreTokens();) {
                String urlPackagePrefix = tokenizer.nextToken();
                String className = urlPackagePrefix + ".java.javaURLContextFactory";
                if (className.equals("org.apache.openejb.core.ivm.naming.java.javaURLContextFactory"))
                    continue;
                try {
                    ClassLoader cl = ClassLoaderUtil.getContextClassLoader();
                    Class factoryClass = Class.forName(className, true, cl);
                    ObjectFactory factoryInstance = (ObjectFactory) factoryClass.newInstance();
                    factories.add(factoryInstance);
                } catch (ClassNotFoundException cnfe) {

                } catch (Throwable e) {
                    NamingException ne = new NamingException("Federation failed: Cannot instantiate " + className);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
            Object [] temp = factories.toArray();
            federatedFactories = new ObjectFactory[temp.length];
            System.arraycopy(temp, 0, federatedFactories, 0, federatedFactories.length);
        }
        return federatedFactories;
    }

    private static String getUrlPackagePrefixes() {
        // 1. System.getProperty
        String urlPackagePrefixes = System.getProperty(Context.URL_PKG_PREFIXES);

        // 2. Thread.currentThread().getContextClassLoader().getResources("jndi.properties")
        if (urlPackagePrefixes == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();

            try {
                Enumeration<URL> resources = classLoader.getResources("jndi.properties");
                while (urlPackagePrefixes == null && resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    InputStream in = resource.openStream();
                    urlPackagePrefixes = getUrlPackagePrefixes(in);
                }
            } catch (IOException ignored) {
            }
        }

        // 3. ${java.home}/lib/jndi.properties
        if (urlPackagePrefixes == null) {
            String javahome = System.getProperty("java.home");
            if (javahome != null) {
                try {
                    File propertiesFile = new File(new File(javahome, "lib"), "jndi.properties");
                    InputStream in = new FileInputStream(propertiesFile);
                    urlPackagePrefixes = getUrlPackagePrefixes(in);
                } catch (FileNotFoundException ignored) {
                }
            }

        }
        return urlPackagePrefixes;
    }

    private static String getUrlPackagePrefixes(InputStream in) {
        try {
            Properties properties = new Properties();
            properties.load(in);
            String urlPackagePrefixes = properties.getProperty(Context.URL_PKG_PREFIXES);
            return urlPackagePrefixes;
        } catch (IOException e) {
            return null;
        }
    }

    public Object lookup(Name compositName) throws NamingException {
        return lookup(compositName.toString());
    }

    public void bind(String name, Object obj) throws NamingException {
        checkReadOnly();
        int indx = name.indexOf(":");
        if (indx > -1) {
            /*
             The ':' character will be in the path if its an absolute path name starting with the schema
             'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx + 1);
        }
        if (fastCache.containsKey(name))
            throw new javax.naming.NameAlreadyBoundException();
        else
            mynode.bind(new ParsedName(name), obj);
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

    public void rename(String oldname, String newname) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public void rename(Name oldname, Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        Object obj = lookup(name);
        if (obj.getClass() == IvmContext.class)
            return new MyListEnumeration(((IvmContext) obj).mynode);
        else {
            return null;
        }
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        Object obj = lookup(name);
        if (obj.getClass() == IvmContext.class)
            return new MyBindingEnumeration(((IvmContext) obj).mynode);
        else {
            return null;
        }
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name) throws NamingException {
        checkReadOnly();
        int indx = name.indexOf(":");
        if (indx > -1) {
            /*
	      The ':' character will be in the path if its an absolute path name starting with the schema
	      'java:'.  We strip the schema off the path before passing it to the node.resolve method.
            */
            name = name.substring(indx + 1);
        }
        if (fastCache.containsKey(name))
            throw new javax.naming.NameAlreadyBoundException();
        else
            return mynode.createSubcontext(new ParsedName(name));
    }

    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new javax.naming.OperationNotSupportedException();
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix) throws NamingException {
        Name result = composeName(new CompositeName(name),
                                  new CompositeName(prefix));
        return result.toString();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable<String, Object>(5);
        }
        return myEnv.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        if (myEnv == null)
            return null;
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
    }

    protected void checkReadOnly() throws OperationNotSupportedException {
        if (readOnly) throw new OperationNotSupportedException();
    }

    protected class MyBindingEnumeration extends MyNamingEnumeration {

        public MyBindingEnumeration(NameNode parentNode) {
            super(parentNode);
        }

        protected void buildEnumeration(Vector vect) {
            for (int i = 0; i < vect.size(); i++) {
                NameNode node = (NameNode) vect.elementAt(i);
                String className = node.getBinding().getClass().getName();
                vect.setElementAt(new Binding(node.atomicName, className, node.getBinding()), i);
            }
            myEnum = vect.elements();
        }

    }

    protected class MyListEnumeration extends MyNamingEnumeration {

        public MyListEnumeration(NameNode parentNode) {
            super(parentNode);
        }

        protected void buildEnumeration(Vector vect) {
            for (int i = 0; i < vect.size(); i++) {
                NameNode node = (NameNode) vect.elementAt(i);
                String className = node.getBinding().getClass().getName();
                vect.setElementAt(new NameClassPair(node.atomicName, className), i);
            }
            myEnum = vect.elements();
        }

    }

    protected abstract class MyNamingEnumeration implements NamingEnumeration {

        Enumeration myEnum;

        public MyNamingEnumeration(NameNode parentNode) {
            Vector vect = new Vector();

            NameNode node = parentNode.subTree;

            if (node == null) {
                node = parentNode;
            } else {
                vect.addElement(node);
            }

            gatherNodes(node, vect);

            buildEnumeration(vect);
        }

        abstract protected void buildEnumeration(Vector<NameNode> vect);

        protected void gatherNodes(NameNode node, Vector vect) {
            if (node.lessTree != null) {
                vect.addElement(node.lessTree);
                gatherNodes(node.lessTree, vect);
            }
            if (node.grtrTree != null) {
                vect.addElement(node.grtrTree);
                gatherNodes(node.grtrTree, vect);
            }
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

    protected Object writeReplace() throws ObjectStreamException {
        if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {

            return new JndiEncArtifact(this);
        }

        throw new NotSerializableException("IntraVM java.naming.Context objects can not be passed as arguments");
    }

    /* for testing only*/
    public static void main(String str []) throws Exception {
        String str1 = "root/comp/env/rate/work/doc/lot/pop";
        String str2 = "root/comp/env/rate/work/doc/lot/price";
        String str3 = "root/comp/env/rate/work/doc/lot";
        String str4 = "root/comp/env/rate/work/doc/lot/break/story";

        IvmContext context = new IvmContext();
        context.bind(str1, new Integer(1));
        context.bind(str2, new Integer(2));
        context.bind(str4, new Integer(3));
/*
        Object obj = context.lookup(str1);
        obj = context.lookup(str2);
        obj = context.lookup(str1);
        obj = context.lookup(str3);
        obj = obj;

        NamingEnumeration ne = context.list(str3);
        while(ne.hasMore()){
            NameClassPair ncp = (NameClassPair)ne.nextElement();
            System.out.println(ncp.getName()+" "+ncp.getClassName());
        }
        */

        Context subcntx = (Context) context.lookup(str3);
        org.apache.openejb.core.ivm.IntraVmCopyMonitor x = null;
        java.io.FileOutputStream fos = new java.io.FileOutputStream("x.ser");
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
        org.apache.openejb.core.ivm.IntraVmCopyMonitor.prePassivationOperation();
        oos.writeObject(subcntx);
        org.apache.openejb.core.ivm.IntraVmCopyMonitor.postPassivationOperation();
        oos.flush();
        oos.close();
        java.io.FileInputStream fis = new java.io.FileInputStream("x.ser");
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
        Object newObj = ois.readObject();
        ois.close();
    }

}
