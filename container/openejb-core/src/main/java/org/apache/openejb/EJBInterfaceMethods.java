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
