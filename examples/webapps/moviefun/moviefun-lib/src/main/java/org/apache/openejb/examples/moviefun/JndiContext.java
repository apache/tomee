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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.examples.moviefun;

import javax.ejb.SessionBean;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NameParser;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author David Blevins <dblevins@visi.com>
 * 
 * @version $Rev$ $Date$
 */
public interface JndiContext extends Context {
    public static final String LOCAL_NAME = "ContextEJBLocal";


    /**
     * Nested EJBLocalHome interface
     */
    public interface LocalHome extends EJBLocalHome {
        JndiContext.Local create() throws CreateException;
    }

    /**
     * Nested EJBLocalObject Interface
     */
    public interface Local extends EJBLocalObject, JndiContext {
    }

    /**
     * Static LocalObject Facade
     */
    public static final Local LOCAL = new Local(){
        private Local object;
        private Local object(){
            if (object == null){
                object = getObject();
            }
            return object;
        }
        private Local getObject() {
            try {
                Properties p = new Properties(System.getProperties());
                p.put("java.naming.factory.initial", "org.openejb.client.LocalInitialContextFactory");
                InitialContext initialContext = new InitialContext(p);
                LocalHome home = (LocalHome) initialContext.lookup(LOCAL_NAME);
                return home.create();
            } catch (NamingException e) {
                throw (IllegalStateException) new IllegalStateException(LOCAL_NAME + " cannot be retrieved from JNDI.").initCause(e);
            } catch (CreateException e) {
                throw (IllegalStateException) new IllegalStateException("Cannot create session bean "+LOCAL_NAME).initCause(e);
            }
        }

        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return getObject().getEJBLocalHome();
        }

        public java.lang.Object getPrimaryKey() throws EJBException {
            return getObject().getPrimaryKey();
        }

        public boolean isIdentical(EJBLocalObject ejbLocalObject) throws EJBException {
            return getObject().isIdentical(ejbLocalObject);
        }

        public void remove() throws RemoveException, EJBException {
            getObject().remove();
        }

        public java.lang.Object lookup(Name name) throws NamingException {
            return getObject().lookup(name);
        }

        public java.lang.Object lookup(String name) throws NamingException {
            return getObject().lookup(name);
        }

        public void bind(Name name, java.lang.Object obj) throws NamingException {
            getObject().bind(name, obj);
        }

        public void bind(String name, java.lang.Object obj) throws NamingException {
            getObject().bind(name, obj);
        }

        public void rebind(Name name, java.lang.Object obj) throws NamingException {
            getObject().rebind(name, obj);
        }

        public void rebind(String name, java.lang.Object obj) throws NamingException {
            getObject().rebind(name, obj);
        }

        public void unbind(Name name) throws NamingException {
            getObject().unbind(name);
        }

        public void unbind(String name) throws NamingException {
            getObject().unbind(name);
        }

        public void rename(Name oldName, Name newName) throws NamingException {
            getObject().rename(oldName, newName);
        }

        public void rename(String oldName, String newName) throws NamingException {
            getObject().rename(oldName, newName);
        }

        public NamingEnumeration list(Name name) throws NamingException {
            return getObject().list(name);
        }

        public NamingEnumeration list(String name) throws NamingException {
            return getObject().list(name);
        }

        public NamingEnumeration listBindings(Name name) throws NamingException {
            return getObject().listBindings(name);
        }

        public NamingEnumeration listBindings(String name) throws NamingException {
            return getObject().listBindings(name);
        }

        public void destroySubcontext(Name name) throws NamingException {
            getObject().destroySubcontext(name);
        }

        public void destroySubcontext(String name) throws NamingException {
            getObject().destroySubcontext(name);
        }

        public Context createSubcontext(Name name) throws NamingException {
            return getObject().createSubcontext(name);
        }

        public Context createSubcontext(String name) throws NamingException {
            return getObject().createSubcontext(name);
        }

        public java.lang.Object lookupLink(Name name) throws NamingException {
            return getObject().lookupLink(name);
        }

        public java.lang.Object lookupLink(String name) throws NamingException {
            return getObject().lookupLink(name);
        }

        public NameParser getNameParser(Name name) throws NamingException {
            return getObject().getNameParser(name);
        }

        public NameParser getNameParser(String name) throws NamingException {
            return getObject().getNameParser(name);
        }

        public Name composeName(Name name, Name prefix) throws NamingException {
            return getObject().composeName(name, prefix);
        }

        public String composeName(String name, String prefix)
            throws NamingException {
            return getObject().composeName(name, prefix);
        }

        public java.lang.Object addToEnvironment(String propName, java.lang.Object propVal)
        throws NamingException {
            return getObject().addToEnvironment(propName, propVal);
        }

        public java.lang.Object removeFromEnvironment(String propName)
        throws NamingException {
            return getObject().removeFromEnvironment(propName);
        }

        public Hashtable getEnvironment() throws NamingException {
            return getObject().getEnvironment();
        }

        public void close() throws NamingException {
            getObject().close();
        }

        public String getNameInNamespace() throws NamingException {
            return getObject().getNameInNamespace();
        }
    };

}
