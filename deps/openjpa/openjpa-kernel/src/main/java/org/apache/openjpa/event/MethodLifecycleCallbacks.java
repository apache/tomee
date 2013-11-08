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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Arrays;

import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Callback adapter that invokes a callback method via reflection.
 *
 * @author Steve Kim
 */
public class MethodLifecycleCallbacks
    implements LifecycleCallbacks, Externalizable {

    private static final Localizer _loc = Localizer.forPackage
        (MethodLifecycleCallbacks.class);

    private transient Method _callback;
    private boolean _arg;

    /**
     * Constructor. Supply callback class and its callback method name.
     *
     * @arg Whether we expect a further argument such as in AfterDetach
     */
    public MethodLifecycleCallbacks(Class cls, String method, boolean arg) {
        Class[] args = arg ? new Class[]{ Object.class } : null;
        _callback = getMethod(cls, method, args);
        _arg = arg;
    }

    /**
     * Constructor. Supply callback method.
     */
    public MethodLifecycleCallbacks(Method method, boolean arg) {
        _callback = method;
        _arg = arg;
    }

    /**
     * The callback method.
     */
    public Method getCallbackMethod() {
        return _callback;
    }

    /**
     * Returns if this callback expects another argument
     */
    public boolean requiresArgument() {
        return _arg;
    }

    public boolean hasCallback(Object obj, int eventType) {
        return true;
    }

    public void makeCallback(Object obj, Object arg, int eventType)
        throws Exception {
        if (!_callback.isAccessible())
            AccessController.doPrivileged(J2DoPrivHelper.setAccessibleAction(
                _callback, true));

        if (_arg)
            _callback.invoke(obj, new Object[]{ arg });
        else
            _callback.invoke(obj, (Object[]) null);
    }

    public String toString() {
        return getClass().getName() + ":" + _callback;
    }

    /**
     * Helper method to return the named method of the given class, throwing
     * the proper exception on error.
     */
    protected static Method getMethod(Class cls, String method, Class[] args) {
        Class currentClass = cls;
        do {
            Method[] methods = (Method[]) AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredMethodsAction(currentClass)); 
            for (int i = 0; i < methods.length; i++) {
                if (!method.equals(methods[i].getName()))
                    continue;

                if (isAssignable(methods[i].getParameterTypes(), args))
                    return methods[i];
            }
        } while ((currentClass = currentClass.getSuperclass()) != null);

        // if we get here, no suitable method was found
        throw new UserException(_loc.get("method-notfound", cls.getName(),
                method, args == null ? null : Arrays.asList(args)));
	}

    /** 
     * Returns true if all parameters in the from array are assignable
     * from the corresponding parameters of the to array. 
     */
    private static boolean isAssignable(Class[] from, Class[] to) {
        if (from == null)
            return to == null || to.length == 0;
        if (to == null)
            return from == null || from.length == 0;

        if (from.length != to.length)
            return false;

        for (int i = 0; i < from.length; i++) {
            if (from[i] != null && !from[i].isAssignableFrom(to[i]))
                return false;
        }

        return true;
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        Class cls = (Class) in.readObject();
        String methName = (String) in.readObject();
        _arg = in.readBoolean();

        Class[] args = _arg ? new Class[]{ Object.class } : null;
        _callback = getMethod(cls, methName, args);
    }

    public void writeExternal(ObjectOutput out)
        throws IOException {
        out.writeObject(_callback.getClass());
        out.writeObject(_callback.getName());
        out.writeBoolean(_arg);
    } 
}
