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

import org.apache.openejb.AppContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Strings;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContextualJndiReference extends IntraVmJndiReference {
    public static final ThreadLocal<Boolean> followReference = new ThreadLocal<Boolean>() {
        @Override
        public Boolean initialValue() {
            return true;
        }
    };

    private Object defaultValue;
    private final List<String> prefixes = new CopyOnWriteArrayList<>();

    public ContextualJndiReference(final String jndiName) {
        super(jndiName);
    }

    public void setDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void addPrefix(final String value) {
        if (value != null) {
            prefixes.add(value);
        }
    }

    public void removePrefix(final String value) {
        if (value != null) {
            prefixes.remove(value);
        }
    }

    public String lastPrefix() {
        if (prefixes.isEmpty()) {
            return null;
        }
        return prefixes.get(prefixes.size() - 1);
    }

    public boolean hasNoMorePrefix() {
        return prefixes.isEmpty();
    }

    public int prefixesSize() {
        return prefixes.size();
    }

    @Override
    public Object getObject() throws NamingException {
        final Object delegate = findDelegate();
        if (Reference.class.isInstance(delegate)) {
            return Reference.class.cast(delegate).getObject();
        }
        return delegate;
    }

    private Object findDelegate() throws NameNotFoundException {
        final Boolean rawValue = !followReference.get();
        followReference.remove();
        if (rawValue) {
            return this;
        }

        final String prefix = findPrefix();
        final String jndiName = getJndiName();

        if (prefix != null && !prefix.isEmpty()) {
            try {
                return lookup(prefix + '/' + jndiName);
            } catch (final NamingException e) {
                // no-op
            }
        }

        final Collection<Object> values = new ArrayList<>();
        for (final String p : prefixes) {
            if (p != null && !p.isEmpty()) {
                try {
                    values.add(lookup(p + '/' + jndiName));
                } catch (final NamingException e) {
                    // no-op
                }
            }
        }

        if (1 == values.size()) {
            return values.iterator().next();
        } else if (!values.isEmpty()) {
            throw new NameNotFoundException("Ambiguous resource '" + getJndiName()
                + "'  for classloader " + Thread.currentThread().getContextClassLoader());
        }

        return defaultValue;
    }

    private String findPrefix() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        for (final AppContext appContext : containerSystem.getAppContexts()) {
            if (appContext.getClassLoader().equals(loader)) {
                return appContext.getId();
            }
            for (final WebContext web : appContext.getWebContexts()) {
                if (web.getClassLoader().equals(loader)) {
                    return appContext.getId();
                }
            }
        }

        if (1 == containerSystem.getAppContexts().size()) {
            return containerSystem.getAppContexts().iterator().next().getId();
        }

        return null;
    }

    private Object lookup(final String s) throws NamingException {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final Context jndiContext = containerSystem.getJNDIContext();
        try {
            if (s.startsWith("java:") | s.startsWith("openejb:")) {
                return jndiContext.lookup(s);
            } else {
                return jndiContext.lookup("openejb/Resource/" + s);
            }
        } catch (final NameNotFoundException e) {
            return jndiContext.lookup("java:module/" + Strings.lastPart(getClassName(), '.'));
        } catch (final NamingException e) {
            throw (NamingException) new NamingException("could not look up " + s).initCause(e);
        }
    }

    @Override
    public String toString() {
        return "ContextualJndiReference{" +
            "jndiName='" + getJndiName() + '\'' +
            '}';
    }
}
