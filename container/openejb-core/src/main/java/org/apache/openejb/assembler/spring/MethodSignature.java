/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb.assembler.spring;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.geronimo.kernel.ClassLoading;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class MethodSignature implements Serializable, Comparable {
    private static final long serialVersionUID = -3801134672931277429L;

    private static final Map<String, Class> primitives;
    static {
        primitives = new LinkedHashMap<String, Class>();
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
    }

    private static final String[] NOARGS = {};
    private final String methodName;
    private final String[] parameterTypes;

    public MethodSignature(Method method) {
        methodName = method.getName();
        Class[] params = method.getParameterTypes();
        parameterTypes = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            parameterTypes[i] = params[i].getName();
        }
    }

    public MethodSignature(String text) {
        if (text.indexOf('(') < 0) {
            methodName = text;
            parameterTypes = NOARGS;
        } else {
            Pattern p = Pattern.compile("(\\S+)\\((\\S*)\\)");
            Matcher m = p.matcher(text);
            if (!m.matches()) {
                throw new IllegalArgumentException("Method signature must be in the form : methodName(type0, type1...) : " + text);
            }
            methodName = m.group(1);
            String parameters = m.group(2);
            if (parameters.length() > 0) {
                parameterTypes = parameters.split(" *, *");
            } else {
                parameterTypes = NOARGS;
            }
        }
    }

    public MethodSignature(String methodName, String[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes != null ? parameterTypes : NOARGS;
    }

    public MethodSignature(String methodName, Class[] params) {
        this.methodName = methodName;
        parameterTypes = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            parameterTypes[i] = params[i].getName();
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName).append('(');
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterType = parameterTypes[i];
                if (i > 0) {
                    buffer.append(',');
                }
                buffer.append(parameterType);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean match(Method method) {
        if(!methodName.equals(method.getName())) {
            return false;
        }
        Class[] types = method.getParameterTypes();
        if (types.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if(!types[i].getName().equals(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public Method getMethod(Class clazz) {
        try {
            ClassLoader classLoader = clazz.getClassLoader();
            Class[] args = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = primitives.get(parameterTypes[i]);
                if (args[i] == null) {
                    args[i] = ClassLoading.loadClass(parameterTypes[i], classLoader);
                }
            }
            return clazz.getMethod(methodName, args);
        } catch (Exception e) {
            return null;
        }
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodSignature)) {
            return false;
        }
        MethodSignature other = (MethodSignature) obj;
        return methodName.equals(other.methodName) && Arrays.equals(parameterTypes, other.parameterTypes);
    }

    public int compareTo(Object object) {
        MethodSignature methodSignature = (MethodSignature) object;

        // alphabetic compare of method names
        int value = methodName.compareTo(methodSignature.methodName);
        if (value != 0) {
            return value;
        }

        // shorter parameter list comes before longer parameter lists
        if (parameterTypes.length < methodSignature.parameterTypes.length) {
            return -1;
        }
        if (parameterTypes.length > methodSignature.parameterTypes.length) {
            return 1;
        }

        // alphabetic compare of each parameter type
        for (int i = 0; i < parameterTypes.length; i++) {
            value = parameterTypes[i].compareTo(methodSignature.parameterTypes[i]);
            if (value != 0) {
                return value;
            }
        }

        // they are the same
        return 0;
    }
}
