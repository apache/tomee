/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.event;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Performs a callback method on a cached bean instance.
 *
 * @author Steve Kim
 */
public class BeanLifecycleCallbacks
    extends MethodLifecycleCallbacks {

    private static final Localizer _loc = Localizer.forPackage
        (BeanLifecycleCallbacks.class);

    private transient Object _listener;

    /**
     * Constructor. Make the callback on an instance of the given type.
     *
     * @arg whether another argument is expected such as AfterDetach
     */
    public BeanLifecycleCallbacks(Class<?> cls, String method, boolean arg,
        Class<?> type) {
        this(cls, getMethod(cls, method, arg ? new Class[]{ Object.class,
            type } : new Class[]{ type }), arg);
    }

    /**
     * Constructor. Make the callback on an instance of the given type.
     */
    public BeanLifecycleCallbacks(Class<?> cls, Method method, boolean arg) {
        super(method, arg);
        _listener = newListener(cls);
    }
    
    private Object newListener(Class<?> cls) {
        try {
            return AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(cls));
        } catch (Throwable t) {
            if (t instanceof PrivilegedActionException)
                t = ((PrivilegedActionException) t).getException();            
            throw new UserException(_loc.get("bean-constructor",
                cls.getName()), t);
        }
    }

    public void makeCallback(Object obj, Object rel, int eventType)
        throws Exception {
        Method callback = getCallbackMethod();
        if (!callback.isAccessible())
            AccessController.doPrivileged(J2DoPrivHelper.setAccessibleAction(
                callback, true));
        if (requiresArgument())
            callback.invoke(_listener, new Object[]{ obj, rel });
        else
            callback.invoke(_listener, new Object[]{ obj });
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Class<?> cls = (Class<?>) in.readObject();
        _listener = newListener(cls);
    }

    public void writeExternal(ObjectOutput out)
        throws IOException {
        super.writeExternal(out);
        out.writeObject(_listener.getClass());
    }
}
