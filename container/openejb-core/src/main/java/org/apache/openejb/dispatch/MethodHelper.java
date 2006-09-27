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
package org.apache.openejb.dispatch;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.asm.Type;
import org.apache.openejb.EJBInterfaceType;

/**
 * Helper methods to deal with the whack handeling of indexes in cglib MethodProxy objects.
 *
 * @version $Revision$ $Date$
 */
public final class MethodHelper {
    private MethodHelper() {
    }

    public static int getSuperIndex(Class proxyImpl, MethodSignature signature) {
        try {
            //TODO look at asm Type and see if there is a more straightforward way to do this.
            // lookup the method object and get its index
            Method method = signature.getMethod(proxyImpl);
            return getSuperIndex(proxyImpl, method);
        } catch (Exception e) {
            // didn't find the method
            return -1;
        }
    }

    public static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
    }

    public static Map getMethodMap(EJBInterfaceType type, MethodSignature[] signatures, Class interfaceClass){
        if (type == EJBInterfaceType.HOME || type == EJBInterfaceType.LOCALHOME ){
            return getHomeMethodMap(signatures, interfaceClass);
        } else {
            return getObjectMethodMap(signatures, interfaceClass);
        }
    }
    
    public static Map getHomeMethodMap(MethodSignature[] signatures, Class homeClass) {
        return getMethodMap(homeClass, translateToHome(signatures));
    }

    public static Map getObjectMethodMap(MethodSignature[] signatures, Class objectClass) {
        return getMethodMap(objectClass, translateToObject(signatures));
    }

    private static Map getMethodMap(Class homeClass, MethodSignature[] signatures) {
        Method[] methods = homeClass.getMethods();
        Map methodMap = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Integer index = findMethodIndex(signatures, method);
            if (index != null) {
                methodMap.put(method, index);
            }
        }
        return methodMap;
    }

    private static Integer findMethodIndex(MethodSignature[] signatures, Method method) {
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            if (signature != null && signature.match(method)) {
                return new Integer(i);
            }
        }
        return null;
    }

    public static MethodSignature[] translateToHome(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbCreate")) {
                translated[i] = new MethodSignature("c" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbFind")) {
                translated[i] = new MethodSignature("f" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbHome")) {
                String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
                translated[i] = new MethodSignature(translatedName, signature.getParameterTypes());
            } else if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            }
        }
        return translated;
    }

    public static MethodSignature[] translateToObject(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            } else {
                translated[i] = new MethodSignature(signature.getMethodName(), signature.getParameterTypes());
            }
        }
        return translated;
    }

    public static InterfaceMethodSignature translateToInterface(MethodSignature signature) {
        String name = signature.getMethodName();
        if (name.startsWith("ejbCreate")) {
            return new InterfaceMethodSignature("c" + name.substring(4), signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbFind")) {
            return new InterfaceMethodSignature("f" + name.substring(4), signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbHome")) {
            String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
            return new InterfaceMethodSignature(translatedName, signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbRemove")) {
            boolean isHome = signature.getParameterTypes().length == 1;
            return new InterfaceMethodSignature("remove", signature.getParameterTypes(), isHome);
        } else {
            return new InterfaceMethodSignature(signature.getMethodName(), signature.getParameterTypes(),false);
        }
    }

    public static String capitalize(String string) {
        if (string == null) throw new NullPointerException("string is null");
        if (string.length() == 0) throw new IllegalArgumentException("string is empty");

        if (string.length() == 1) {
            return "" + Character.toUpperCase(string.charAt(0));
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
