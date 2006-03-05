/**
 * 
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openejb.persistence;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class JNDIContext implements Context {
    
    private static Hashtable<String, Object> db = new Hashtable<String,Object>();

    public Object lookup(Name arg0) throws NamingException {
        return null;
    }

    public Object lookup(String arg0) throws NamingException {
        Object value = db.get(arg0);
        if (value == null)
            throw new NamingException(arg0 + " not found.");
        
        return value;
    }

    public void bind(Name arg0, Object arg1) throws NamingException {
    }

    public void bind(String arg0, Object arg1) throws NamingException {
        db.put(arg0, arg1);
    }

    public void rebind(Name arg0, Object arg1) throws NamingException {
    }

    public void rebind(String arg0, Object arg1) throws NamingException {
        bind(arg0, arg1);
    }

    public void unbind(Name arg0) throws NamingException {
    }

    public void unbind(String arg0) throws NamingException {
        db.remove(arg0);
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

    public void destroySubcontext(String arg0) throws NamingException {
    }

    public Context createSubcontext(Name arg0) throws NamingException {
        return null;
    }

    public Context createSubcontext(String arg0) throws NamingException {
        return null;
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
