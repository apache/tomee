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
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

public class OpenEJBContext implements Context {

    public Object lookup(Name name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    public Object lookup(String name) throws NamingException {
        return getThreadContext().lookup(name);
    }

    public void bind(Name name, Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    public void bind(String name, Object obj) throws NamingException {
        getThreadContext().bind(name, obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        getThreadContext().rebind(name, obj);
    }

    public void unbind(Name name) throws NamingException {
        getThreadContext().unbind(name);
    }

    public void unbind(String name) throws NamingException {
        getThreadContext().unbind(name);
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    public void rename(String oldName, String newName) throws NamingException {
        getThreadContext().rename(oldName, newName);
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return getThreadContext().list(name);
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return getThreadContext().list(name);
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return getThreadContext().listBindings(name);
    }

    public void destroySubcontext(Name name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    public void destroySubcontext(String name) throws NamingException {
        getThreadContext().destroySubcontext(name);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    public Context createSubcontext(String name) throws NamingException {
        return getThreadContext().createSubcontext(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    public Object lookupLink(String name) throws NamingException {
        return getThreadContext().lookupLink(name);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    public NameParser getNameParser(String name) throws NamingException {
        return getThreadContext().getNameParser(name);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return getThreadContext().composeName(name, prefix);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return getThreadContext().addToEnvironment(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return getThreadContext().removeFromEnvironment(propName);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return getThreadContext().getEnvironment();
    }

    public void close() throws NamingException {
        getThreadContext().close();
    }

    public String getNameInNamespace() throws NamingException {
        return "";
    }

    private Context getThreadContext() throws NamingException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        Context context = threadContext.getDeploymentInfo().getJndiEnc();
        return context;
    }

}

