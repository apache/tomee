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
package org.apache.openjpa.util;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.JavaVersions;

/**
 * Utility methods for externalizing and handling exceptions.
 *
 * @author Marc Prud'hommeaux
 * @since 0.2.5
 * @nojavadoc
 */
public class Exceptions {

    public static final Throwable[] EMPTY_THROWABLES = new Throwable[0];

    static final String SEP = J2DoPrivHelper.getLineSeparator();

    private static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) {
        }
    };

    /**
     * Test to see if the specified object will be able to be serialized. This
     * will check if the object implements {@link Serializable}, and if so,
     * will try to perform an actual serialization. This is in case the object
     * has fields which, in turn, are not serializable.
     *
     * @param ob the object to test
     * @return true if the object will be able to be serialized
     */
    private static boolean isSerializable(Object ob) {
        if (!(ob instanceof Serializable))
            return false;

        // don't serialize persistent objects exceptions to prevent
        // reading in all the state
        if (!ImplHelper.isManagedType(null, ob.getClass()))
            return false;

        // now do an actual test to see if we will be
        // able to perform the serialization
        try {
            new ObjectOutputStream(DEV_NULL).writeObject(ob);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Safely stringify the given object.
     */
    public static String toString(Object ob) {
        if (ob == null)
            return "null";

        // don't take oid of new objects since it can cause a flush if auto-inc
        // and the id is meaningless anyway
        Object oid = getObjectId(ob);

        if (oid != null) {
            if (oid instanceof Id)
                return oid.toString();
            String oidString = oid.toString();
            // some oids stringify their class names. Some do not.
            if (oidString.indexOf(ob.getClass().getName()) == -1) {
                return ob.getClass().getName() + "-" + oidString;
            } else {
                return oidString;
            }
        }

        if (ImplHelper.isManagedType(null, ob.getClass())) {
            // never call toString() on a PersistenceCapable, since
            // it may access persistent fields; fall-back to using
            // the standard object stringification mechanism. New
            // instances that use proxying (property-access instances,
            // for example) that were created with the 'new' keyword
            // will not end up in this code, which is ok since they
            // don't do lazy loading anyways, so they will stringify
            // safely.
            return ob.getClass().getName() + "@"
                + Integer.toHexString(System.identityHashCode(ob));
        }

        try {
            String s = ob.toString();
            if (s.indexOf(ob.getClass().getName()) == -1)
                s += " [" + ob.getClass().getName() + "]";
            return s;
        } catch (Throwable t) {
            return ob.getClass().getName();
        }
    }

    /**
     * Safely stringify the given objects.
     */
    public static String toString(Collection failed) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (Iterator itr = failed.iterator(); itr.hasNext();) {
            buf.append(Exceptions.toString(itr.next()));
            if (itr.hasNext())
                buf.append(", ");
        }
        buf.append("]");
        return buf.toString();
    }

    /**
     * Stringify the given exception.
     */
    public static String toString(ExceptionInfo e) {
        int type = e.getType();
        StringBuilder buf = new StringBuilder();
        buf.append("<").
            append(OpenJPAVersion.VERSION_ID).
            append(' ').
            append(e.isFatal() ? "fatal " : "nonfatal ").
            append (type == ExceptionInfo.GENERAL ? "general error" :
                type == ExceptionInfo.INTERNAL ? "internal error" :
                type == ExceptionInfo.STORE ? "store error" :
                type == ExceptionInfo.UNSUPPORTED ? "unsupported error" :
                type == ExceptionInfo.USER ? "user error" :
                (type + " error")).
            append("> ");
        buf.append(e.getClass().getName()).append(": ").
            append(e.getMessage());
        Object failed = e.getFailedObject();
        if (failed != null)
            buf.append(SEP).append("FailedObject: ").
                append(toString(failed));
        return buf.toString();
    }

    /**
     * Print the stack trace of the exception's nested throwables.
     */
    public static void printNestedThrowables(ExceptionInfo e, PrintStream out) {
        // if this is Java 1.4 and there is exactly a single
        // exception, then defer to 1.4's behavior of printing
        // out the result of getCause(). This deferral happens in
        // the calling code.
        Throwable[] nested = e.getNestedThrowables();
        int i = (JavaVersions.VERSION >= 4) ? 1 : 0;
        if (i < nested.length) {
            out.println("NestedThrowables:");
            for (; i < nested.length; i++)
                // guard against a nasty null in the array
                if (nested[i] != null)
                    nested[i].printStackTrace(out);
        }
    }

    /**
     * Print the stack trace of the exception's nested throwables.
     */
    public static void printNestedThrowables(ExceptionInfo e, PrintWriter out) {
        // if this is Java 1.4 and there is exactly a single
        // exception, then defer to 1.4's behavior of printing
        // out the result of getCause(). This deferral happens in
        // the calling code.
        Throwable[] nested = e.getNestedThrowables();
        int i = (JavaVersions.VERSION >= 4) ? 1 : 0;
        if (i < nested.length) {
            out.println("NestedThrowables:");
            for (; i < nested.length; i++)
                // guard against a nasty null in the array
                if (nested[i] != null)
                    nested[i].printStackTrace(out);
        }
    }

    /**
     * Convert the specified failed object into a serializable
     * object for when we are serializing an Exception. It will
     * try the following:
     * <ul>
     * <li>if the object can be serialized, return the object itself</li>
     * <li>if the object has a serializable oid, return the oid</li>
     * <li>if the object has a non-serializable oid, return the oid's
     * toString and the object class</li>
     * <li>return the object's toString</li>
     * </ul>
     *
     * @param ob the object to convert
     * @return some serialized representation of the object
     */
    public static Object replaceFailedObject(Object ob) {
        if (ob == null)
            return null;
        if (isSerializable(ob))
            return ob;

        // don't take oid of new objects since it can cause a flush if auto-inc
        // and the id is meaningless anyway
        Object oid = getObjectId(ob);
        if (oid != null && isSerializable(oid))
            return oid;

        // last ditch: stringify the object
        return toString(ob);
    }

    /**
     * Convert the specified throwables into a serialzable array. If
     * any of the nested throwables cannot be serialized, they will
     * be converted into a Exception with the original message.
     */
    public static Throwable[] replaceNestedThrowables(Throwable[] nested) {
        if (nested == null || nested.length == 0)
            return nested;
        if (isSerializable(nested))
            return nested;

        Throwable[] newNested = new Throwable[nested.length];
        for (int i = 0; i < nested.length; i++) {
            if (isSerializable(nested[i]))
                newNested[i] = nested[i];
            else
                // guard against a nasty null in the array by using valueOf
                // instead of toString to prevent throwing yet another 
                // exception
                newNested[i] = new Exception(String.valueOf(nested[i]));
        }
        return newNested;
    }

    /**
     * Return the object id for <code>ob</code> if it has one, or
     * <code>null</code> otherwise.
     */
    private static Object getObjectId(Object ob) {
        if (!ImplHelper.isManageable(ob))
            return null;

        PersistenceCapable pc = ImplHelper.toPersistenceCapable(ob, null);
        if (pc == null || pc.pcIsNew())
            return null;
        else
            return pc.pcFetchObjectId();
	}
    
    public static String toClassName(Class<?> cls) {
        if (cls == null) return "";
        if (cls.isArray())
            return toClassName(cls.getComponentType())+"[]";
        return cls.getName();
    }
    
    public static String toClassNames(Collection<? extends Class<?>> classes) {
        if (classes == null) return "";
        StringBuilder buffer = new StringBuilder();
        for (Class<?> cls : classes) {
            buffer.append("\r\n").append(toClassName(cls));
        }
        return buffer.toString();
    }
}
