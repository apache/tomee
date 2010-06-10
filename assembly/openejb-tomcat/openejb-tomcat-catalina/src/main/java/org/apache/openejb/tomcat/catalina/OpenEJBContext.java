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
package org.apache.openejb.tomcat.catalina;

import org.apache.openejb.core.ThreadContext;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * {@link Context} implementation for using it
 * via Tomcat integration.
 *
 * @version $Rev$ $Date$
 */
public class OpenEJBContext implements Context {

    /**
     * Default constructor.
     */
    public OpenEJBContext() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public Object lookup(Name name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    /**
     * {@inheritDoc}
     */
    public Object lookup(String name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(Name name, Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(String name, Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    public void rebind(Name name, Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    public void rebind(String name, Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    public void unbind(Name name) throws NamingException {
        getThreadContext().unbind(name);
    }

    /**
     * {@inheritDoc}
     */
    public void unbind(String name) throws NamingException {
        getThreadContext().unbind(name);
    }

    /**
     * {@inheritDoc}
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public void rename(String oldName, String newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return getThreadContext().list(name);
    }

    /**
     * {@inheritDoc}
     */
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return getThreadContext().list(name);
    }

    /**
     * {@inheritDoc}
     */
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    /**
     * {@inheritDoc}
     */
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    /**
     * {@inheritDoc}
     */
    public void destroySubcontext(Name name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    public void destroySubcontext(String name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    public Context createSubcontext(Name name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    public Context createSubcontext(String name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupLink(Name name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupLink(String name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    /**
     * {@inheritDoc}
     */
    public NameParser getNameParser(Name name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    /**
     * {@inheritDoc}
     */
    public NameParser getNameParser(String name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    /**
     * {@inheritDoc}
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public String composeName(String name, String prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    /**
     * {@inheritDoc}
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return getThreadContext().addToEnvironment(propName, propVal);
    }

    /**
     * {@inheritDoc}
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        return getThreadContext().removeFromEnvironment(propName);
    }

    /**
     * {@inheritDoc}
     */
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return getThreadContext().getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws NamingException {
        getThreadContext().close();
    }

    /**
     * {@inheritDoc}
     */
    public String getNameInNamespace() throws NamingException {
        return "";
    }

    /**
     * Gets current context deployment context.
     *
     * @return context of deployment
     * @throws NamingException for exception
     */
    private Context getThreadContext() throws NamingException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        Context context = threadContext.getDeploymentInfo().getJndiEnc();
        return context;
    }

}

