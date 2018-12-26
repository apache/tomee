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
package org.apache.tomee.catalina;

import org.apache.naming.ContextBindings;
import org.apache.openejb.assembler.DeployerEjb;
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
     * {@inheritDoc}
     */
    @Override
    public Object lookup(final Name name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookup(final String name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final Name name, final Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final String name, final Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rebind(final Name name, final Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rebind(final String name, final Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind(final Name name) throws NamingException {
        getThreadContext().unbind(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind(final String name) throws NamingException {
        getThreadContext().unbind(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final Name oldName, final Name newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rename(final String oldName, final String newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        return getThreadContext().list(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return getThreadContext().list(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroySubcontext(final Name name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroySubcontext(final String name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context createSubcontext(final Name name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context createSubcontext(final String name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupLink(final Name name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupLink(final String name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameParser getNameParser(final Name name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameParser getNameParser(final String name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String composeName(final String name, final String prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        return getThreadContext().addToEnvironment(propName, propVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeFromEnvironment(final String propName) throws NamingException {
        return getThreadContext().removeFromEnvironment(propName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return getThreadContext().getEnvironment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws NamingException {
        getThreadContext().close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        if (skipEjbContext(threadContext)) {
            return ContextBindings.getClassLoader();
        }
        final Context context = threadContext.getBeanContext().getJndiEnc();
        return context;
    }

    private boolean skipEjbContext(final ThreadContext threadContext) {
        // we use it to deploy so if any lookup is done during the deployment
        // we don't want to get the DeployerEjb JNDI tree
        // since this method is pretty quick that's not an issue to do the test
        return threadContext == null || DeployerEjb.class.equals(threadContext.getBeanContext().getBeanClass());
    }

}

