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
package org.apache.openejb;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * Common Methods on EJB Interfaces
 *
 * @version $Revision$ $Date$
 */
public final class EJBInterfaceMethods {
    public static final Method HOME_GET_EJBMETADATA;
    public static final Method HOME_GET_HOMEHANDLE;
    public static final Method HOME_REMOVE_OBJECT;
    public static final Method HOME_REMOVE_HANDLE;
    public static final Set HOME_METHODS = new HashSet(4);

    public static final Method LOCALHOME_REMOVE_OBJECT;
    public static final Set LOCALHOME_METHODS;

    public static final Method OBJECT_GET_HOME;
    public static final Method OBJECT_GET_HANDLE;
    public static final Method OBJECT_GET_PRIMARYKEY;
    public static final Method OBJECT_ISIDENTICAL;
    public static final Method OBJECT_REMOVE;
    public static final Set OBJECT_METHODS = new HashSet(5);

    public static final Method LOCALOBJECT_GET_LOCALHOME;
    public static final Method LOCALOBJECT_GET_PRIMARYKEY;
    public static final Method LOCALOBJECT_ISIDENTICAL;
    public static final Method LOCALOBJECT_REMOVE;
    public static final Set LOCALOBJECT_METHODS = new HashSet(4);

    static {
        try {
            HOME_GET_EJBMETADATA = EJBHome.class.getMethod("getEJBMetaData", null);
            HOME_GET_HOMEHANDLE = EJBHome.class.getMethod("getHomeHandle", null);
            HOME_REMOVE_OBJECT = EJBHome.class.getMethod("remove", new Class[]{Object.class});
            HOME_REMOVE_HANDLE = EJBHome.class.getMethod("remove", new Class[]{Handle.class});
            HOME_METHODS.add(HOME_GET_EJBMETADATA);
            HOME_METHODS.add(HOME_GET_HOMEHANDLE);
            HOME_METHODS.add(HOME_REMOVE_HANDLE);
            HOME_METHODS.add(HOME_REMOVE_OBJECT);

            LOCALHOME_REMOVE_OBJECT = EJBLocalHome.class.getMethod("remove", new Class[]{Object.class});
            LOCALHOME_METHODS = Collections.singleton(LOCALHOME_REMOVE_OBJECT);

            OBJECT_GET_HOME = EJBObject.class.getMethod("getEJBHome", null);
            OBJECT_GET_HANDLE = EJBObject.class.getMethod("getHandle", null);
            OBJECT_GET_PRIMARYKEY = EJBObject.class.getMethod("getPrimaryKey", null);
            OBJECT_ISIDENTICAL = EJBObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
            OBJECT_REMOVE = EJBObject.class.getMethod("remove", null);
            OBJECT_METHODS.add(OBJECT_GET_HOME);
            OBJECT_METHODS.add(OBJECT_GET_HANDLE);
            OBJECT_METHODS.add(OBJECT_GET_PRIMARYKEY);
            OBJECT_METHODS.add(OBJECT_ISIDENTICAL);
            OBJECT_METHODS.add(OBJECT_REMOVE);

            LOCALOBJECT_GET_LOCALHOME = EJBLocalObject.class.getMethod("getEJBLocalHome", null);
            LOCALOBJECT_GET_PRIMARYKEY = EJBLocalObject.class.getMethod("getPrimaryKey", null);
            LOCALOBJECT_ISIDENTICAL = EJBLocalObject.class.getMethod("isIdentical", new Class[]{EJBLocalObject.class});
            LOCALOBJECT_REMOVE = EJBLocalObject.class.getMethod("remove", null);
            LOCALOBJECT_METHODS.add(LOCALOBJECT_GET_LOCALHOME);
            LOCALOBJECT_METHODS.add(LOCALOBJECT_GET_PRIMARYKEY);
            LOCALOBJECT_METHODS.add(LOCALOBJECT_ISIDENTICAL);
            LOCALOBJECT_METHODS.add(LOCALOBJECT_REMOVE);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
