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
package org.apache.openejb.tomcat;

import org.apache.openejb.core.ThreadContext;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;

public class OpenEJBContext implements Context {
    private static final String URL_PREFIX = "comp";

    public Object lookup(Name name) throws NamingException {
        return getThreadContext().lookup(addUrlPrefix(name));
    }

    public Object lookup(String name) throws NamingException {
        return getThreadContext().lookup(addUrlPrefix(name));
    }

    public void bind(Name name, Object obj) throws NamingException {
        getThreadContext().bind(addUrlPrefix(name), obj);
    }

    public void bind(String name, Object obj) throws NamingException {
        getThreadContext().bind(addUrlPrefix(name), obj);
    }

    public void rebind(Name name, Object obj) throws NamingException {
        getThreadContext().rebind(addUrlPrefix(name), obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        getThreadContext().rebind(addUrlPrefix(name), obj);
    }

    public void unbind(Name name) throws NamingException {
        getThreadContext().unbind(addUrlPrefix(name));
    }

    public void unbind(String name) throws NamingException {
        getThreadContext().unbind(addUrlPrefix(name));
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        getThreadContext().rename(addUrlPrefix(oldName), addUrlPrefix(newName));
    }

    public void rename(String oldName, String newName) throws NamingException {
        getThreadContext().rename(addUrlPrefix(oldName), addUrlPrefix(newName));
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return getThreadContext().list(addUrlPrefix(name));
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return getThreadContext().list(addUrlPrefix(name));
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return getThreadContext().listBindings(addUrlPrefix(name));
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return getThreadContext().listBindings(addUrlPrefix(name));
    }

    public void destroySubcontext(Name name) throws NamingException {
        getThreadContext().destroySubcontext(addUrlPrefix(name));
    }

    public void destroySubcontext(String name) throws NamingException {
        getThreadContext().destroySubcontext(addUrlPrefix(name));
    }

    public Context createSubcontext(Name name) throws NamingException {
        return getThreadContext().createSubcontext(addUrlPrefix(name));
    }

    public Context createSubcontext(String name) throws NamingException {
        return getThreadContext().createSubcontext(addUrlPrefix(name));
    }

    public Object lookupLink(Name name) throws NamingException {
        return getThreadContext().lookupLink(addUrlPrefix(name));
    }

    public Object lookupLink(String name) throws NamingException {
        return getThreadContext().lookupLink(addUrlPrefix(name));
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getThreadContext().getNameParser(addUrlPrefix(name));
    }

    public NameParser getNameParser(String name) throws NamingException {
        return getThreadContext().getNameParser(addUrlPrefix(name));
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        return getThreadContext().composeName(addUrlPrefix(name), prefix);
    }

    public String composeName(String name, String prefix) throws NamingException {
        return getThreadContext().composeName(addUrlPrefix(name), prefix);
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
        return URL_PREFIX;
    }

    private Context getThreadContext() throws NamingException {
        ThreadContext threadContext = ThreadContext.getThreadContext();
        Context context = threadContext.getDeploymentInfo().getJndiEnc();
        return context;
    }

    private String addUrlPrefix(String name) throws NamingException {
        if (name.startsWith(URL_PREFIX)) {
            return name.substring(URL_PREFIX.length());
        }
        return name;
    }

    private Name addUrlPrefix(Name name) throws NamingException {
        if (!name.isEmpty() || !name.get(0).equals(URL_PREFIX)) {
            return name.getSuffix(1);
        }
        return name;
    }
}

