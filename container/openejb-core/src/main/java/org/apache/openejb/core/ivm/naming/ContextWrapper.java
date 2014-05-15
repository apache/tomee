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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @version $Rev$ $Date$
 */
public class ContextWrapper implements Context {

    protected final Context context;

    public ContextWrapper(final Context context) {
        this.context = context;
    }

    @Override
    public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        return context.addToEnvironment(propName, propVal);
    }

    @Override
    public void bind(final Name name, final Object obj) throws NamingException {
        context.bind(name, obj);
    }

    @Override
    public void bind(final String name, final Object obj) throws NamingException {
        context.bind(name, obj);
    }

    @Override
    public void close() throws NamingException {
        context.close();
    }

    @Override
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    @Override
    public String composeName(final String name, final String prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    @Override
    public Context createSubcontext(final Name name) throws NamingException {
        return context.createSubcontext(name);
    }

    @Override
    public Context createSubcontext(final String name) throws NamingException {
        return context.createSubcontext(name);
    }

    @Override
    public void destroySubcontext(final Name name) throws NamingException {
        context.destroySubcontext(name);
    }

    @Override
    public void destroySubcontext(final String name) throws NamingException {
        context.destroySubcontext(name);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return context.getEnvironment();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return context.getNameInNamespace();
    }

    @Override
    public NameParser getNameParser(final Name name) throws NamingException {
        return context.getNameParser(name);
    }

    @Override
    public NameParser getNameParser(final String name) throws NamingException {
        return context.getNameParser(name);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        return context.list(name);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return context.list(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        return context.listBindings(name);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return context.listBindings(name);
    }

    @Override
    public Object lookup(final Name name) throws NamingException {
        return context.lookup(name);
    }

    @Override
    public Object lookup(final String name) throws NamingException {
        return context.lookup(name);
    }

    @Override
    public Object lookupLink(final Name name) throws NamingException {
        return context.lookupLink(name);
    }

    @Override
    public Object lookupLink(final String name) throws NamingException {
        return context.lookupLink(name);
    }

    @Override
    public void rebind(final Name name, final Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    @Override
    public void rebind(final String name, final Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    @Override
    public Object removeFromEnvironment(final String propName) throws NamingException {
        return context.removeFromEnvironment(propName);
    }

    @Override
    public void rename(final Name oldName, final Name newName) throws NamingException {
        context.rename(oldName, newName);
    }

    @Override
    public void rename(final String oldName, final String newName) throws NamingException {
        context.rename(oldName, newName);
    }

    @Override
    public void unbind(final Name name) throws NamingException {
        context.unbind(name);
    }

    @Override
    public void unbind(final String name) throws NamingException {
        context.unbind(name);
    }
}
