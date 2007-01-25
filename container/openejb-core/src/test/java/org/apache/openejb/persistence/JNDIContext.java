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
package org.apache.openejb.persistence;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class JNDIContext implements Context {

    private Hashtable<String, Object> db = new Hashtable<String, Object>();

    private int level = 0;

    public JNDIContext() {
    }

    private JNDIContext(int nextLevel) {
        level = nextLevel;
    }
    
    public void clear(){
       db.clear(); 
    }
    
    public Object lookup(Name arg0) throws NamingException {
        return null;
    }

    public Object lookup(String name) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        if (name.startsWith("/"))
            name = name.substring(1);

        if (name.length() == 0)
            return this;
        
        CompositeName composite = new CompositeName(name);

        String segment = composite.get(0);
        Object value = db.get(segment);
        if (value instanceof JNDIContext) {
            if (composite.size() > 1)
                return ((JNDIContext) value).lookup(composite.getSuffix(1)
                        .toString());
        }

        if (value == null)
            throw new NameNotFoundException();

        return value;
    }

    public void bind(Name arg0, Object arg1) throws NamingException {
    }

    public void bind(String name, Object arg1) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        if (name.equals(""))
            throw new NamingException(name + "is not valid.");

        CompositeName composite = new CompositeName(name);

        String segment = composite.get(0);
        Object value = db.get(segment);
        if (value instanceof JNDIContext) {
            if (composite.size() > 1)
                ((JNDIContext) value).bind(composite.getSuffix(1).toString(),
                        arg1);
        }

        if (value != null)
            throw new NameAlreadyBoundException();

        db.put(name, arg1);
    }

    public void rebind(Name arg0, Object arg1) throws NamingException {
    }

    public void rebind(String arg0, Object arg1) throws NamingException {
        bind(arg0, arg1);
    }

    public void unbind(Name arg0) throws NamingException {
    }

    public void unbind(String name) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        if (name.startsWith("/"))
            name = name.substring(1);

        if (name.equals(""))
            throw new NamingException(name + "is not valid.");

        CompositeName composite = new CompositeName(name);
        String segment = composite.get(0);
        Object value = db.get(segment);
        if (value == null)
            throw new NameNotFoundException();

        if (value instanceof JNDIContext) {
            if (composite.size() > 1){
                ((JNDIContext) value).unbind(composite.getSuffix(1).toString());
                return;
            }else
                throw new NameNotFoundException("Cannot unbind a subcontext.");
        }

        db.remove(segment);
    }

    public void rename(Name arg0, Name arg1) throws NamingException {
    }

    public void rename(String arg0, String arg1) throws NamingException {
    }

    public NamingEnumeration<NameClassPair> list(Name arg0)
            throws NamingException {
        return null;
    }

    public NamingEnumeration<NameClassPair> list(String arg0)
            throws NamingException {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(Name arg0)
            throws NamingException {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(String arg0)
            throws NamingException {
        return null;
    }

    public void destroySubcontext(Name arg0) throws NamingException {
    }

    public void destroySubcontext(String name) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);
        Object value = db.get(name);
        if (value instanceof Context)
            db.remove(name);
        else
            throw new NameNotFoundException();
    }

    public Context createSubcontext(Name arg0) throws NamingException {
        return null;
    }

    public Context createSubcontext(String name) throws NamingException {
        if (name.startsWith("java:"))
            name = name.substring(5);

        Object value = db.get(name);
        if (value != null)
            throw new NameAlreadyBoundException();

        Context ctx = new JNDIContext(level + 1);
        db.put(name, ctx);
        return ctx;
    }

    public Object lookupLink(Name arg0) throws NamingException {
        return null;
    }

    public Object lookupLink(String arg0) throws NamingException {
        return null;
    }

    public NameParser getNameParser(Name arg0) throws NamingException {
        return null;
    }

    public NameParser getNameParser(String arg0) throws NamingException {
        return null;
    }

    public Name composeName(Name arg0, Name arg1) throws NamingException {
        return null;
    }

    public String composeName(String arg0, String arg1) throws NamingException {
        return null;
    }

    public Object addToEnvironment(String arg0, Object arg1)
            throws NamingException {
        return null;
    }

    public Object removeFromEnvironment(String arg0) throws NamingException {
        return null;
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
    }

    public void close() throws NamingException {
    }

    public String getNameInNamespace() throws NamingException {
        return null;
    }

}
