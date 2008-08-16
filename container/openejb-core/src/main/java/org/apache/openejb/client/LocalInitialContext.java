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
package org.apache.openejb.client;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;

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
public class LocalInitialContext implements Context {

    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, LocalInitialContext.class);
    private final Context context;
    private final LocalInitialContextFactory factory;

    public LocalInitialContext(Context context, LocalInitialContextFactory factory) {
        this.context = context;
        this.factory = factory;
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return context.addToEnvironment(propName, propVal);
    }

    public void bind(Name name, Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void bind(String name, Object obj) throws NamingException {
        context.bind(name, obj);
    }

    public void close() throws NamingException {
        if (factory.bootedOpenEJB()){
            logger.info("Destroying container system");
            factory.close();
            context.close();
            OpenEJB.destroy();
        }
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return context.composeName(name, prefix);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return context.createSubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return context.createSubcontext(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        context.destroySubcontext(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        context.destroySubcontext(name);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return context.getEnvironment();
    }

    public String getNameInNamespace() throws NamingException {
        return context.getNameInNamespace();
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return context.getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return context.getNameParser(name);
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return context.list(name);
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return context.listBindings(name);
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return context.listBindings(name);
    }

    public Object lookup(Name name) throws NamingException {
        return context.lookup(name);
    }

    public Object lookup(String name) throws NamingException {
        return context.lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return context.lookupLink(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return context.lookupLink(name);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        context.rebind(name, obj);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return context.removeFromEnvironment(propName);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public void rename(String oldName, String newName) throws NamingException {
        context.rename(oldName, newName);
    }

    public void unbind(Name name) throws NamingException {
        context.unbind(name);
    }

    public void unbind(String name) throws NamingException {
        context.unbind(name);
    }
}
