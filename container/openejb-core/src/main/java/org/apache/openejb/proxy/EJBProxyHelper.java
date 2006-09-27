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
package org.apache.openejb.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodHelper;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyHelper {
    public static int[] getOperationMap(Class proxyType, InterfaceMethodSignature[] signatures, boolean isMessageDriven) {
        boolean isHomeInterface = isMessageDriven? false: isHomeInterface(proxyType);

        // get the map from method keys to the intercepted shadow index
        Map proxyToShadowIndex = buildProxyToShadowIndex(proxyType, isHomeInterface);

        // create the method lookup table and fill it with -1
        int[] shadowIndexToProxy = new int[FastClass.create(proxyType).getMaxIndex() + 1];
        Arrays.fill(shadowIndexToProxy, -1);

        // for each interface method, fill in it's id into the shadowIndex table
        for (int i = 0; i < signatures.length; i++) {
            if (signatures[i] != null) {
                Integer shadowIndex = (Integer) proxyToShadowIndex.get(signatures[i]);
                if (shadowIndex != null) {
                    shadowIndexToProxy[shadowIndex.intValue()] = i;
                }
            }
        }
        return shadowIndexToProxy;
    }

    private static boolean isHomeInterface(Class proxyType) {
        //
        // NOTE: We must load the ejb classes from the proxy's classloader because during deployment the
        // proxy's classloader is not a child of the classloader of this class
        //
        try {
            ClassLoader cl = proxyType.getClassLoader();
            Class ejbHomeClass = cl.loadClass("javax.ejb.EJBHome");
            Class ejbLocalHomeClass = cl.loadClass("javax.ejb.EJBLocalHome");
            if(ejbHomeClass.isAssignableFrom(proxyType) || ejbLocalHomeClass.isAssignableFrom(proxyType)) {
                return true;
            }

            Class ejbObjectClass = cl.loadClass("javax.ejb.EJBObject");
            Class ejbLocalObjectClass = cl.loadClass("javax.ejb.EJBLocalObject");
            if (ejbObjectClass.isAssignableFrom(proxyType) || ejbLocalObjectClass.isAssignableFrom(proxyType)) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            // ignore... exception thrown below
        }

        throw new IllegalArgumentException("ProxyType must be an instance of EJBHome, EJBLocalHome, EJBObject, or EJBLocalObject");
    }

    /**
     * Builds a map from the MethodKeys for the real method to the index of
     * the shadow method, which is the same number returned from MethodProxy.getSuperIndex().
     * The map contains only the MethodKeys of methods that have shadow methods (i.e., only
     * the enhanced methods).
     * @param proxyType the generated proxy implementation class
     * @return a map from MethodKeys to the Integer for the shadow method
     */
    private static Map buildProxyToShadowIndex(Class proxyType, boolean isHome) {
        Map shadowMap = new HashMap();
        Method[] methods = proxyType.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            int shadowIndex = MethodHelper.getSuperIndex(proxyType, methods[i]);
            if (shadowIndex >= 0) {
                shadowMap.put(new InterfaceMethodSignature(methods[i], isHome), new Integer(shadowIndex));
            }
        }
        return shadowMap;
    }
}
