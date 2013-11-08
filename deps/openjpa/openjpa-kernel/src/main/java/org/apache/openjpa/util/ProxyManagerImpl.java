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

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.enhance.AsmAdaptor;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.util.concurrent.NullSafeConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

import serp.bytecode.BCClass;
import serp.bytecode.BCField;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.JumpInstruction;
import serp.bytecode.Project;
import serp.util.Strings;

/**
 * Default implementation of the {@link ProxyManager} interface.
 *
 * @author Abe White
 */
public class ProxyManagerImpl
    implements ProxyManager {

    private static final String PROXY_SUFFIX = "$proxy";

    private static final Localizer _loc = Localizer.forPackage
        (ProxyManagerImpl.class);

    private static long _proxyId = 0L;
    private static final Map _stdCollections = new HashMap();
    private static final Map _stdMaps = new HashMap();
    static {
        _stdCollections.put(Collection.class, ArrayList.class);
        _stdCollections.put(Set.class, HashSet.class);
        _stdCollections.put(SortedSet.class, TreeSet.class);
        _stdCollections.put(List.class, ArrayList.class);
        _stdCollections.put(Queue.class, LinkedList.class);
        _stdMaps.put(Map.class, HashMap.class);
        _stdMaps.put(SortedMap.class, TreeMap.class);
    }

    private final Set _unproxyable = new HashSet();
    private final Map _proxies = new NullSafeConcurrentHashMap();
    private boolean _trackChanges = true;
    private boolean _assertType = false;
    private boolean _delayedCollectionLoading = false;

    public ProxyManagerImpl() {
        _unproxyable.add(TimeZone.class.getName());
    }

    /**
     * Whether proxies produced by this factory will use {@link ChangeTracker}s
     * to try to cut down on data store operations at the cost of some extra
     * bookkeeping overhead. Defaults to true.
     */
    public boolean getTrackChanges() {
        return _trackChanges;
    }

    /**
     * Whether proxies produced by this factory will use {@link ChangeTracker}s
     * to try to cut down on data store operations at the cost of some extra
     * bookkeeping overhead. Defaults to true.
     */
    public void setTrackChanges(boolean track) {
        _trackChanges = track;
    }

    /**
     * Whether to perform runtime checks to ensure that all elements
     * added to collection and map proxies are the proper element/key/value
     * type as defined by the metadata. Defaults to false.
     */
    public boolean getAssertAllowedType() {
        return _assertType;
    }

    /**
     * Whether to perform runtime checks to ensure that all elements
     * added to collection and map proxies are the proper element/key/value
     * type as defined by the metadata. Defaults to false.
     */
    public void setAssertAllowedType(boolean assertType) {
        _assertType = assertType;
    }
    
    /**
     * Whether loading of collections should be delayed until an operation
     * is performed that requires them to be loaded.  This property only
     * applies to proxies that implement java.util.Collection (ie. not arrays
     * or maps).  Defaults to false.
     * @return 
     */
    public boolean getDelayCollectionLoading() {
        return _delayedCollectionLoading;
    }
    
    /**
     * Whether loading of collections should be delayed until an operation
     * is performed that requires them to be loaded.  Defaults to false.
     */
    public void setDelayCollectionLoading(boolean delay) {
        _delayedCollectionLoading = delay;
    }
       
    /**
     * Return a mutable view of class names we know cannot be proxied  
     * correctly by this manager.
     */
    public Collection getUnproxyable() {
        return _unproxyable;
    }

    /**
     * Provided for auto-configuration.  Add the given semicolon-separated
     * class names to the set of class names we know cannot be proxied correctly
     * by this manager.
     */
    public void setUnproxyable(String clsNames) {
        if (clsNames != null)
            _unproxyable.addAll(Arrays.asList(Strings.split(clsNames, ";", 0)));
    }

    public Object copyArray(Object orig) {
        if (orig == null)
            return null;

        try {
            int length = Array.getLength(orig);
            Object array = Array.newInstance(orig.getClass().
                getComponentType(), length);

            System.arraycopy(orig, 0, array, 0, length);
            return array;
        } catch (Exception e) {
            throw new UnsupportedException(_loc.get("bad-array",
                e.getMessage()), e);
        }
    }

    public Collection copyCollection(Collection orig) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return (Collection) ((Proxy) orig).copy(orig);

        ProxyCollection proxy = getFactoryProxyCollection(orig.getClass());
        return (Collection) proxy.copy(orig);
    }

    public Proxy newCollectionProxy(Class type, Class elementType,
        Comparator compare, boolean autoOff) {
        type = toProxyableCollectionType(type);
        ProxyCollection proxy = getFactoryProxyCollection(type);
        return proxy.newInstance((_assertType) ? elementType : null, compare,
            _trackChanges, autoOff);
    }

    public Map copyMap(Map orig) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return (Map) ((Proxy) orig).copy(orig);

        ProxyMap proxy = getFactoryProxyMap(orig.getClass());
        return (Map) proxy.copy(orig);
    }

    public Proxy newMapProxy(Class type, Class keyType, 
        Class elementType, Comparator compare,boolean autoOff) {
        type = toProxyableMapType(type);
        ProxyMap proxy = getFactoryProxyMap(type);
        return proxy.newInstance((_assertType) ? keyType : null, 
            (_assertType) ? elementType : null, compare, _trackChanges, autoOff);
    }

    public Date copyDate(Date orig) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return (Date) ((Proxy) orig).copy(orig);

        ProxyDate proxy = getFactoryProxyDate(orig.getClass());
        return (Date) proxy.copy(orig);
    }

    public Proxy newDateProxy(Class type) {
        ProxyDate proxy = getFactoryProxyDate(type);
        return proxy.newInstance();
    }

    public Calendar copyCalendar(Calendar orig) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return (Calendar) ((Proxy) orig).copy(orig);

        ProxyCalendar proxy = getFactoryProxyCalendar(orig.getClass());
        return (Calendar) proxy.copy(orig);
    }

    public Proxy newCalendarProxy(Class type, TimeZone zone) {
        if (type == Calendar.class)
            type = GregorianCalendar.class;
        ProxyCalendar proxy = getFactoryProxyCalendar(type);
        ProxyCalendar cal = proxy.newInstance();
        if (zone != null)
            ((Calendar) cal).setTimeZone(zone);
        return cal;
    }

    public Object copyCustom(Object orig) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return ((Proxy) orig).copy(orig);
        if (ImplHelper.isManageable(orig))
            return null;
        if (orig instanceof Collection)
            return copyCollection((Collection) orig);
        if (orig instanceof Map)
            return copyMap((Map) orig);
        if (orig instanceof Date)
            return copyDate((Date) orig);
        if (orig instanceof Calendar)
            return copyCalendar((Calendar) orig);
        ProxyBean proxy = getFactoryProxyBean(orig);
        return (proxy == null) ? null : proxy.copy(orig); 
    }

    public Proxy newCustomProxy(Object orig, boolean autoOff) {
        if (orig == null)
            return null;
        if (orig instanceof Proxy)
            return (Proxy) orig;
        if (ImplHelper.isManageable(orig))
            return null;
        if (!isProxyable(orig.getClass()))
            return null;
        
        if (orig instanceof Collection) {
            Comparator comp = (orig instanceof SortedSet) 
                ? ((SortedSet) orig).comparator() : null;
            Collection c = (Collection) newCollectionProxy(orig.getClass(), 
                null, comp, autoOff); 
            c.addAll((Collection) orig);
            return (Proxy) c;
        }
        if (orig instanceof Map) {
            Comparator comp = (orig instanceof SortedMap) 
                ? ((SortedMap) orig).comparator() : null;
            Map m = (Map) newMapProxy(orig.getClass(), null, null, comp, autoOff);
            m.putAll((Map) orig);
            return (Proxy) m;
        }
        if (orig instanceof Date) {
            Date d = (Date) newDateProxy(orig.getClass());
            d.setTime(((Date) orig).getTime());
            if (orig instanceof Timestamp)
                ((Timestamp) d).setNanos(((Timestamp) orig).getNanos());
            return (Proxy) d;
        }
        if (orig instanceof Calendar) {
            Calendar c = (Calendar) newCalendarProxy(orig.getClass(),
                ((Calendar) orig).getTimeZone());
            c.setTimeInMillis(((Calendar) orig).getTimeInMillis());
            return (Proxy) c;
        }

        ProxyBean proxy = getFactoryProxyBean(orig);
        return (proxy == null) ? null : proxy.newInstance(orig);
    }

    /**
     * Return the concrete type for proxying.
     */
    protected Class toProxyableCollectionType(Class type) {
        if (type.getName().endsWith(PROXY_SUFFIX))
            type = type.getSuperclass();
        else if (type.isInterface()) {
            type = toConcreteType(type, _stdCollections);
            if (type == null)
                throw new UnsupportedException(_loc.get("no-proxy-intf", type));
        } else if (Modifier.isAbstract(type.getModifiers()))
            throw new UnsupportedException(_loc.get("no-proxy-abstract", type));
        return type;
    }

    /**
     * Return the concrete type for proxying.
     */
    protected Class toProxyableMapType(Class type) {
        if (type.getName().endsWith(PROXY_SUFFIX))
            type = type.getSuperclass();
        else if (type.isInterface()) {
            type = toConcreteType(type, _stdMaps);
            if (type == null)
                throw new UnsupportedException(_loc.get("no-proxy-intf", type));
        } else if (Modifier.isAbstract(type.getModifiers()))
            throw new UnsupportedException(_loc.get("no-proxy-abstract", type));
        return type;
    }

    /**
     * Locate a concrete type to proxy for the given collection interface.
     */
    private static Class toConcreteType(Class intf, Map concretes) {
        Class concrete = (Class) concretes.get(intf);
        if (concrete != null)
            return concrete;
        Class[] intfs = intf.getInterfaces();         
        for (int i = 0; i < intfs.length; i++) {
            concrete = toConcreteType(intfs[i], concretes);
            if (concrete != null)
                return concrete;
        }
        return null; 
    }

    /**
     * Return the cached factory proxy for the given collection type.
     */
    private ProxyCollection getFactoryProxyCollection(Class type) {
        // we don't lock here; ok if two proxies get generated for same type
        ProxyCollection proxy = (ProxyCollection) _proxies.get(type);
        if (proxy == null) {
            ClassLoader l = GeneratedClasses.getMostDerivedLoader(type,
                ProxyCollection.class);
            Class pcls = loadBuildTimeProxy(type, l);
            if (pcls == null)
                pcls = GeneratedClasses.loadBCClass(
                    generateProxyCollectionBytecode(type, true), l);
            proxy = (ProxyCollection) instantiateProxy(pcls, null, null);
            _proxies.put(type, proxy);
        }
        return proxy;
    }

    /**
     * Return the cached factory proxy for the given map type.
     */
    private ProxyMap getFactoryProxyMap(Class type) {
        // we don't lock here; ok if two proxies get generated for same type
        ProxyMap proxy = (ProxyMap) _proxies.get(type);
        if (proxy == null) {
            ClassLoader l = GeneratedClasses.getMostDerivedLoader(type,
                ProxyMap.class);
            Class pcls = loadBuildTimeProxy(type, l);
            if (pcls == null)
                pcls = GeneratedClasses.loadBCClass(
                    generateProxyMapBytecode(type, true), l);
            proxy = (ProxyMap) instantiateProxy(pcls, null, null);
            _proxies.put(type, proxy);
        }
        return proxy;
    }

    /**
     * Return the cached factory proxy for the given date type.
     */
    private ProxyDate getFactoryProxyDate(Class type) {
        // we don't lock here; ok if two proxies get generated for same type
        ProxyDate proxy = (ProxyDate) _proxies.get(type);
        if (proxy == null) {
            ClassLoader l = GeneratedClasses.getMostDerivedLoader(type,
                ProxyDate.class);
            Class pcls = loadBuildTimeProxy(type, l);
            if (pcls == null)
                pcls = GeneratedClasses.loadBCClass(
                    generateProxyDateBytecode(type, true), l);
            proxy = (ProxyDate) instantiateProxy(pcls, null, null);
            _proxies.put(type, proxy);
        }
        return proxy;
    }

    /**
     * Return the cached factory proxy for the given calendar type.
     */
    private ProxyCalendar getFactoryProxyCalendar(Class type) {
        // we don't lock here; ok if two proxies get generated for same type
        ProxyCalendar proxy = (ProxyCalendar) _proxies.get(type);
        if (proxy == null) {
            ClassLoader l = GeneratedClasses.getMostDerivedLoader(type,
                ProxyCalendar.class);
            Class pcls = loadBuildTimeProxy(type, l);
            if (pcls == null)
                pcls = GeneratedClasses.loadBCClass(
                    generateProxyCalendarBytecode(type, true), l);
            proxy = (ProxyCalendar) instantiateProxy(pcls, null, null);
            _proxies.put(type, proxy);
        }
        return proxy;
    }

    /**
     * Return the cached factory proxy for the given bean type.
     */
    private ProxyBean getFactoryProxyBean(Object orig) {
        final Class type = orig.getClass();
        if (isUnproxyable(type))
            return null;

        // we don't lock here; ok if two proxies get generated for same type
        ProxyBean proxy = (ProxyBean) _proxies.get(type);
        if (proxy == null && !_proxies.containsKey(type)) {
            ClassLoader l = GeneratedClasses.getMostDerivedLoader(type,
                ProxyBean.class);
            Class pcls = loadBuildTimeProxy(type, l);
            if (pcls == null) {
                // TODO Move this to J2DOPrivHelper? 
                BCClass bc = AccessController
                    .doPrivileged(new PrivilegedAction<BCClass>() {
                        public BCClass run() {
                            return generateProxyBeanBytecode(type, true);
                        }
                    });
                if (bc != null)
                    pcls = GeneratedClasses.loadBCClass(bc, l);
            }
            if (pcls != null)
                proxy = (ProxyBean) instantiateProxy(pcls,
                    findCopyConstructor(type), new Object[] {orig});
            _proxies.put(type, proxy);
        }
        return proxy;
    }

    /**
     * Return whether the given type is known to be unproxyable.
     */
    protected boolean isUnproxyable(Class type) {
        for (; type != null && type != Object.class; 
            type = type.getSuperclass()) {
            if (_unproxyable.contains(type.getName()))
                return true;
        }
        return false;
    }

    /**
     * Load the proxy class generated at build time for the given type,
     * returning null if none exists.
     */
    protected Class loadBuildTimeProxy(Class type, ClassLoader loader) {
        try {
            Class<?> proxyClass = null;
            if (_delayedCollectionLoading) {
                proxyClass = loadDelayedProxy(type);
                if (proxyClass != null) {
                    return proxyClass;
                }
            }
            return Class.forName(getProxyClassName(type, false), true, loader);
        } catch (Throwable t) {
            return null;
        }
    }
    
    protected Class<?> loadDelayedProxy(Class<?> type) {
        if (type.equals(java.util.ArrayList.class)) {
            return org.apache.openjpa.util.DelayedArrayListProxy.class;
        }
        if (type.equals(java.util.HashSet.class)) {
            return org.apache.openjpa.util.DelayedHashSetProxy.class;
        }
        if (type.equals(java.util.LinkedList.class)) {
            return org.apache.openjpa.util.DelayedLinkedListProxy.class;
        }
        if (type.equals(java.util.Vector.class)) {
            return org.apache.openjpa.util.DelayedVectorProxy.class;
        }
        if (type.equals(java.util.LinkedHashSet.class)) {
            return org.apache.openjpa.util.DelayedLinkedHashSetProxy.class;
        }
        if (type.equals(java.util.SortedSet.class) || type.equals(java.util.TreeSet.class)) {
            return org.apache.openjpa.util.DelayedTreeSetProxy.class;
        }
        if (type.equals(java.util.PriorityQueue.class)) {
            return org.apache.openjpa.util.DelayedPriorityQueueProxy.class;
        }
        return null;
    }

    /**
     * Instantiate the given proxy class.
     */
    private Proxy instantiateProxy(Class cls, Constructor cons, Object[] args) {
        try {
            if (cons != null)
                return (Proxy) cls.getConstructor(cons.getParameterTypes()).
                    newInstance(args);
            return (Proxy) AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(cls));
        } catch (InstantiationException ie) {
            throw new UnsupportedException(_loc.get("cant-newinstance",
                cls.getSuperclass().getName()));
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof InstantiationException)
                throw new UnsupportedException(_loc.get("cant-newinstance",
                    cls.getSuperclass().getName()));
            else
                throw new GeneralException(cls.getName()).setCause(e);
        } catch (Throwable t) {
            throw new GeneralException(cls.getName()).setCause(t);
        }
    }

    /**
     * Generate the bytecode for a collection proxy for the given type.
     */
    protected BCClass generateProxyCollectionBytecode(Class type, 
        boolean runtime) {
        assertNotFinal(type);
        Project project = new Project(); 
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper
            .loadProjectClassAction(project, getProxyClassName(type, runtime)));
        bc.setSuperclass(type);
        bc.declareInterface(ProxyCollection.class);
 
        delegateConstructors(bc, type);
        addProxyMethods(bc, false);
        addProxyCollectionMethods(bc, type);
        proxyRecognizedMethods(bc, type, ProxyCollections.class, 
            ProxyCollection.class);
        proxySetters(bc, type);
        addWriteReplaceMethod(bc, runtime);
        return bc;
    }

    /**
     * Return the name of the proxy class to generate for the given type.   
     */
    private static String getProxyClassName(Class type, boolean runtime) {
        String id = (runtime) ? "$" + nextProxyId() : "";
        return Strings.getPackageName(ProxyManagerImpl.class) + "." 
            + type.getName().replace('.', '$') + id + PROXY_SUFFIX;
    }

    /**
     * Throw appropriate exception if the given type is final.
     */
    private static void assertNotFinal(Class type) {
        if (Modifier.isFinal(type.getModifiers()))
            throw new UnsupportedException(_loc.get("no-proxy-final", type));
    }
    
    private static boolean isProxyable(Class<?> cls){
        int mod = cls.getModifiers();
        if(Modifier.isFinal(mod))
            return false;
        if(Modifier.isProtected(mod) || Modifier.isPublic(mod))
            return true;
        // Default scoped class, we can only extend if it is in the same package as the generated proxy. Ideally
        // we'd fix the code gen portion and place proxies in the same pacakge as the types being proxied.
        if(cls.getPackage().getName().equals("org.apache.openjpa.util"))
            return true;
        
        return false;
                    
    }

    /**
     * Generate the bytecode for a map proxy for the given type.
     */
    protected BCClass generateProxyMapBytecode(Class type, boolean runtime) {
        assertNotFinal(type);
        Project project = new Project(); 
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper
            .loadProjectClassAction(project, getProxyClassName(type, runtime)));
        bc.setSuperclass(type);
        bc.declareInterface(ProxyMap.class);
 
        delegateConstructors(bc, type);
        addProxyMethods(bc, false);
        addProxyMapMethods(bc, type);
        Class<? extends ProxyMaps> mapProxyClassType =
            ConcurrentMap.class.isAssignableFrom(type) ? ProxyConcurrentMaps.class : ProxyMaps.class;
        proxyRecognizedMethods(bc, type, mapProxyClassType, ProxyMap.class);
        proxySetters(bc, type);
        addWriteReplaceMethod(bc, runtime);
        return bc;
    }

    /**
     * Generate the bytecode for a date proxy for the given type.
     */
    protected BCClass generateProxyDateBytecode(Class type, boolean runtime) {
        assertNotFinal(type);
        Project project = new Project(); 
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper
            .loadProjectClassAction(project, getProxyClassName(type, runtime)));
        bc.setSuperclass(type);
        bc.declareInterface(ProxyDate.class);
 
        delegateConstructors(bc, type);
        addProxyMethods(bc, true);
        addProxyDateMethods(bc, type);
        proxySetters(bc, type);
        addWriteReplaceMethod(bc, runtime);
        return bc;
    }

    /**
     * Generate the bytecode for a calendar proxy for the given type.
     */
    protected BCClass generateProxyCalendarBytecode(Class type, 
        boolean runtime) {
        assertNotFinal(type);
        Project project = new Project(); 
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper
            .loadProjectClassAction(project, getProxyClassName(type, runtime)));
        bc.setSuperclass(type);
        bc.declareInterface(ProxyCalendar.class);
 
        delegateConstructors(bc, type);
        addProxyMethods(bc, true);
        addProxyCalendarMethods(bc, type);
        proxySetters(bc, type);
        addWriteReplaceMethod(bc, runtime);
        return bc;
    }

    /**
     * Generate the bytecode for a bean proxy for the given type.
     */
    protected BCClass generateProxyBeanBytecode(Class type, boolean runtime) {
        if (Modifier.isFinal(type.getModifiers()))
            return null;
        if (ImplHelper.isManagedType(null, type))
            return null;

        // we can only generate a valid proxy if there is a copy constructor
        // or a default constructor
        Constructor cons = findCopyConstructor(type);
        if (cons == null) {
            Constructor[] cs = type.getConstructors();
            for (int i = 0; cons == null && i < cs.length; i++)
               if (cs[i].getParameterTypes().length == 0)
                    cons = cs[i]; 
            if (cons == null)
                return null;
        }

        Project project = new Project(); 
        BCClass bc = AccessController.doPrivileged(J2DoPrivHelper
            .loadProjectClassAction(project, getProxyClassName(type, runtime)));
        bc.setSuperclass(type);
        bc.declareInterface(ProxyBean.class);
 
        delegateConstructors(bc, type);
        addProxyMethods(bc, true);
        addProxyBeanMethods(bc, type, cons);
        if (!proxySetters(bc, type))
            return null;
        addWriteReplaceMethod(bc, runtime);
        return bc;
    }

    /**
     * Create pass-through constructors to base type.
     */
    private void delegateConstructors(BCClass bc, Class type) {
        Constructor[] cons = type.getConstructors();
        Class[] params;
        BCMethod m;
        Code code;
        for (int i = 0; i < cons.length; i++) {
            params = cons[i].getParameterTypes();
            m = bc.declareMethod("<init>", void.class, params); 
            m.makePublic();

            code = m.getCode(true);
            code.aload().setThis();
            for (int j = 0; j < params.length; j++)
                code.xload().setParam(j).setType(params[j]);
            code.invokespecial().setMethod(cons[i]);
            code.vreturn();
            code.calculateMaxStack();
            code.calculateMaxLocals();
        }
    }

    /**
     * Implement the methods in the {@link Proxy} interface, with the exception
     * of {@link Proxy#copy}.
     *
     * @param changeTracker whether to implement a null change tracker; if false
     * the change tracker method is left unimplemented
     */
    private void addProxyMethods(BCClass bc, boolean changeTracker) {
        BCField sm = bc.declareField("sm", OpenJPAStateManager.class);
        sm.setTransient(true);
        BCField field = bc.declareField("field", int.class);
        field.setTransient(true);

        BCMethod m = bc.declareMethod("setOwner", void.class, new Class[] {
            OpenJPAStateManager.class, int.class });
        m.makePublic();
        Code code = m.getCode(true);
        code.aload().setThis();
        code.aload().setParam(0);
        code.putfield().setField(sm);
        code.aload().setThis();
        code.iload().setParam(1);
        code.putfield().setField(field);
        code.vreturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        m = bc.declareMethod("getOwner", OpenJPAStateManager.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(sm);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        m = bc.declareMethod("getOwnerField", int.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(field);
        code.ireturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        /* 
         * clone (return detached proxy object)
         * Note:  This method is only being provided to satisfy a quirk with
         * the IBM JDK -- while comparing Calendar objects, the clone() method
         * was invoked.  So, we are now overriding the clone() method so as to
         * provide a detached proxy object (null out the StateManager).
         */
        m = bc.declareMethod("clone", Object.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.invokespecial().setMethod(bc.getSuperclassType(), "clone",
                Object.class, null);  
        code.checkcast().setType(Proxy.class);  
        int other = code.getNextLocalsIndex();
        code.astore().setLocal(other);
        code.aload().setLocal(other);
        code.constant().setNull();
        code.constant().setValue(0);
        code.invokeinterface().setMethod(Proxy.class, "setOwner", void.class,
                new Class[] { OpenJPAStateManager.class, int.class });
        code.aload().setLocal(other);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
        
        if (changeTracker) {
            m = bc.declareMethod("getChangeTracker", ChangeTracker.class, null);
            m.makePublic();
            code = m.getCode(true);
            code.constant().setNull();
            code.areturn();
            code.calculateMaxStack();
            code.calculateMaxLocals();
        }
    }

    /**
     * Implement the methods in the {@link ProxyCollection} interface.
     */
    private void addProxyCollectionMethods(BCClass bc, Class type) {
        // change tracker
        BCField changeTracker = bc.declareField("changeTracker", 
            CollectionChangeTracker.class);
        changeTracker.setTransient(true);
        BCMethod m = bc.declareMethod("getChangeTracker", ChangeTracker.class, 
            null);
        m.makePublic();
        Code code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(changeTracker);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // collection copy
        Constructor cons = findCopyConstructor(type);
        if (cons == null && SortedSet.class.isAssignableFrom(type))
            cons = findComparatorConstructor(type);
        Class[] params = (cons == null) ? new Class[0] 
            : cons.getParameterTypes();

        m = bc.declareMethod("copy", Object.class, new Class[] {Object.class});
        m.makePublic();
        code = m.getCode(true);

        code.anew().setType(type);
        code.dup();
        if (params.length == 1) {
            code.aload().setParam(0);
            if (params[0] == Comparator.class) {
                code.checkcast().setType(SortedSet.class);
                code.invokeinterface().setMethod(SortedSet.class, "comparator", 
                    Comparator.class, null);
            } else
                code.checkcast().setType(params[0]);
        }
        code.invokespecial().setMethod(type, "<init>", void.class, params);
        if (params.length == 0 || params[0] == Comparator.class) {
            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Collection.class);
            code.invokevirtual().setMethod(type, "addAll", boolean.class, 
                new Class[] { Collection.class });
            code.pop();
        }
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // element type
        BCField elementType = bc.declareField("elementType", Class.class);
        elementType.setTransient(true);
        m = bc.declareMethod("getElementType", Class.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(elementType);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // new instance factory
        m = bc.declareMethod("newInstance", ProxyCollection.class, 
            new Class[] { Class.class, Comparator.class, boolean.class, boolean.class });
        m.makePublic();
        code = m.getCode(true);

        code.anew().setType(bc); 
        code.dup();
        cons = findComparatorConstructor(type);
        params = (cons == null) ? new Class[0] : cons.getParameterTypes();
        if (params.length == 1)
            code.aload().setParam(1);
        code.invokespecial().setMethod("<init>", void.class, params);
        int ret = code.getNextLocalsIndex();
        code.astore().setLocal(ret);

        // set element type
        code.aload().setLocal(ret);
        code.aload().setParam(0);
        code.putfield().setField(elementType);

        // create change tracker and set it
        code.iload().setParam(2);
        JumpInstruction ifins = code.ifeq();
        code.aload().setLocal(ret);
        code.anew().setType(CollectionChangeTrackerImpl.class);
        code.dup();
        code.aload().setLocal(ret);
        code.constant().setValue(allowsDuplicates(type));
        code.constant().setValue(isOrdered(type));
        code.aload().setParam(3);
        code.invokespecial().setMethod(CollectionChangeTrackerImpl.class, 
            "<init>", void.class, new Class[] { Collection.class, 
            boolean.class, boolean.class, boolean.class });
        code.putfield().setField(changeTracker);

        ifins.setTarget(code.aload().setLocal(ret));
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Return whether the given collection type allows duplicates.
     */
    protected boolean allowsDuplicates(Class type) {
        return !Set.class.isAssignableFrom(type);
    }

    /**
     * Return whether the given collection type maintains an artificial 
     * ordering.
     */
    protected boolean isOrdered(Class type) {
        return List.class.isAssignableFrom(type)
            || "java.util.LinkedHashSet".equals(type.getName());
    }

    /**
     * Implement the methods in the {@link ProxyMap} interface.
     */
    private void addProxyMapMethods(BCClass bc, Class type) {
        // change tracker
        BCField changeTracker = bc.declareField("changeTracker", 
            MapChangeTracker.class);
        changeTracker.setTransient(true);
        BCMethod m = bc.declareMethod("getChangeTracker", ChangeTracker.class, 
            null);
        m.makePublic();
        Code code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(changeTracker);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // map copy
        Constructor cons = findCopyConstructor(type);
        if (cons == null && SortedMap.class.isAssignableFrom(type))
            cons = findComparatorConstructor(type);
        Class[] params = (cons == null) ? new Class[0] 
            : cons.getParameterTypes();

        m = bc.declareMethod("copy", Object.class, new Class[] {Object.class});
        m.makePublic();
        code = m.getCode(true);

        code.anew().setType(type);
        code.dup();
        if (params.length == 1) {
            code.aload().setParam(0);
            if (params[0] == Comparator.class) {
                code.checkcast().setType(SortedMap.class);
                code.invokeinterface().setMethod(SortedMap.class, "comparator", 
                    Comparator.class, null);
            } else 
                code.checkcast().setType(params[0]);
        }
        code.invokespecial().setMethod(type, "<init>", void.class, params);
        if (params.length == 0 || params[0] == Comparator.class) {
            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Map.class);
            code.invokevirtual().setMethod(type, "putAll", void.class, 
                new Class[] { Map.class });
        }
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // key type
        BCField keyType = bc.declareField("keyType", Class.class);
        keyType.setTransient(true);
        m = bc.declareMethod("getKeyType", Class.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(keyType);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // value type
        BCField valueType = bc.declareField("valueType", Class.class);
        valueType.setTransient(true);
        m = bc.declareMethod("getValueType", Class.class, null);
        m.makePublic();
        code = m.getCode(true);
        code.aload().setThis();
        code.getfield().setField(valueType);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // new instance factory
        m = bc.declareMethod("newInstance", ProxyMap.class, 
            new Class[] { Class.class, Class.class, Comparator.class, 
            boolean.class,boolean.class });
        m.makePublic();
        code = m.getCode(true);

        code.anew().setType(bc); 
        code.dup();
        cons = findComparatorConstructor(type);
        params = (cons == null) ? new Class[0] : cons.getParameterTypes();
        if (params.length == 1)
            code.aload().setParam(2);
        code.invokespecial().setMethod("<init>", void.class, params);
        int ret = code.getNextLocalsIndex();
        code.astore().setLocal(ret);

        // set key and value types
        code.aload().setLocal(ret);
        code.aload().setParam(0);
        code.putfield().setField(keyType);
        code.aload().setLocal(ret);
        code.aload().setParam(1);
        code.putfield().setField(valueType);

        // create change tracker and set it
        code.iload().setParam(3);
        JumpInstruction ifins = code.ifeq();
        code.aload().setLocal(ret);
        code.anew().setType(MapChangeTrackerImpl.class);
        code.dup();
        code.aload().setLocal(ret);
        code.aload().setParam(4);
        code.invokespecial().setMethod(MapChangeTrackerImpl.class, 
            "<init>", void.class, new Class[] { Map.class, boolean.class });
        code.putfield().setField(changeTracker);

        ifins.setTarget(code.aload().setLocal(ret));
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Implement the methods in the {@link ProxyDate} interface.
     */
    private void addProxyDateMethods(BCClass bc, Class type) {
        boolean hasDefaultCons = bc.getDeclaredMethod("<init>", 
            (Class[]) null) != null;
        boolean hasMillisCons = bc.getDeclaredMethod("<init>", 
            new Class[] { long.class }) != null;
        if (!hasDefaultCons && !hasMillisCons)
            throw new UnsupportedException(_loc.get("no-date-cons", type));

        // add a default constructor that delegates to the millis constructor
        BCMethod m;
        Code code;
        if (!hasDefaultCons) {
            m = bc.declareMethod("<init>", void.class, null);
            m.makePublic();
            code = m.getCode(true);
            code.aload().setThis();
            code.invokestatic().setMethod(System.class, "currentTimeMillis",
                long.class, null);
            code.invokespecial().setMethod(type, "<init>", void.class,
                new Class[] { long.class });
            code.vreturn();
            code.calculateMaxStack();           
            code.calculateMaxLocals();
        }

        // date copy
        Constructor cons = findCopyConstructor(type);
        Class[] params;
        if (cons != null)
            params = cons.getParameterTypes();
        else if (hasMillisCons)
            params = new Class[] { long.class };
        else
            params = new Class[0];

        m = bc.declareMethod("copy", Object.class, new Class[] {Object.class});
        m.makePublic();
        code = m.getCode(true);

        code.anew().setType(type);
        code.dup();
        if (params.length == 1) {
            if (params[0] == long.class) {
                code.aload().setParam(0);
                code.checkcast().setType(Date.class);
                code.invokevirtual().setMethod(Date.class, "getTime", 
                    long.class, null);
            } else {
                code.aload().setParam(0);
                code.checkcast().setType(params[0]);
            }
        }
        code.invokespecial().setMethod(type, "<init>", void.class, params);
        if (params.length == 0) {
            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Date.class);
            code.invokevirtual().setMethod(Date.class, "getTime", long.class, 
                null);
            code.invokevirtual().setMethod(type, "setTime", void.class,
                new Class[] { long.class });
        }
        if ((params.length == 0 || params[0] == long.class) 
            && Timestamp.class.isAssignableFrom(type)) {
            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Timestamp.class);
            code.invokevirtual().setMethod(Timestamp.class, "getNanos", 
                int.class, null);
            code.invokevirtual().setMethod(type, "setNanos", void.class,
                new Class[] { int.class });
        }
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // new instance factory
        m = bc.declareMethod("newInstance", ProxyDate.class, null); 
        m.makePublic();
        code = m.getCode(true);
        code.anew().setType(bc); 
        code.dup();
        code.invokespecial().setMethod("<init>", void.class, null);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Implement the methods in the {@link ProxyCalendar} interface.
     */
    private void addProxyCalendarMethods(BCClass bc, Class type) {
        // calendar copy
        Constructor cons = findCopyConstructor(type);
        Class[] params = (cons == null) ? new Class[0] 
            : cons.getParameterTypes();

        BCMethod m = bc.declareMethod("copy", Object.class, 
            new Class[] {Object.class});
        m.makePublic();
        Code code = m.getCode(true);

        code.anew().setType(type);
        code.dup();
        if (params.length == 1) {
            code.aload().setParam(0);
            code.checkcast().setType(params[0]);
        }
        code.invokespecial().setMethod(type, "<init>", void.class, params);
        if (params.length == 0) {
            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Calendar.class);
            code.invokevirtual().setMethod(Calendar.class, "getTimeInMillis", 
                long.class, null);
            code.invokevirtual().setMethod(type, "setTimeInMillis", void.class,
                new Class[] { long.class });

            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Calendar.class);
            code.invokevirtual().setMethod(Calendar.class, "isLenient", 
                boolean.class, null);
            code.invokevirtual().setMethod(type, "setLenient", void.class,
                new Class[] { boolean.class });

            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Calendar.class);
            code.invokevirtual().setMethod(Calendar.class, "getFirstDayOfWeek", 
                int.class, null);
            code.invokevirtual().setMethod(type, "setFirstDayOfWeek", 
                void.class, new Class[] { int.class });

            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Calendar.class);
            code.invokevirtual().setMethod(Calendar.class, 
                "getMinimalDaysInFirstWeek", int.class, null);
            code.invokevirtual().setMethod(type, "setMinimalDaysInFirstWeek", 
                void.class, new Class[] { int.class });

            code.dup();
            code.aload().setParam(0);
            code.checkcast().setType(Calendar.class);
            code.invokevirtual().setMethod(Calendar.class, "getTimeZone", 
                TimeZone.class, null);
            code.invokevirtual().setMethod(type, "setTimeZone", void.class,
                new Class[] { TimeZone.class });
        }
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // new instance factory
        m = bc.declareMethod("newInstance", ProxyCalendar.class, null); 
        m.makePublic();
        code = m.getCode(true);
        code.anew().setType(bc); 
        code.dup();
        code.invokespecial().setMethod("<init>", void.class, null);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // proxy the protected computeFields method b/c it is called on
        // mutate, and some setters are final and therefore not proxyable
        m = bc.declareMethod("computeFields", void.class, null);
        m.makeProtected();
        code = m.getCode(true);
        code.aload().setThis();
        code.constant().setValue(true);
        code.invokestatic().setMethod(Proxies.class, "dirty", void.class,
            new Class[] { Proxy.class, boolean.class });
        code.aload().setThis();
        code.invokespecial().setMethod(type, "computeFields", void.class, null);
        code.vreturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Implement the methods in the {@link ProxyBean} interface.
     */
    private void addProxyBeanMethods(BCClass bc, Class type, Constructor cons) {
        // bean copy
        BCMethod m = bc.declareMethod("copy", Object.class, 
            new Class[] { Object.class });
        m.makePublic();
        Code code = m.getCode(true);

        code.anew().setType(type);
        code.dup();
        Class[] params = cons.getParameterTypes();
        if (params.length == 1) {
            code.aload().setParam(0);
            code.checkcast().setType(params[0]);
        }
        code.invokespecial().setMethod(cons);
        if (params.length == 0)
            copyProperties(type, code);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();

        // new instance factory
        m = bc.declareMethod("newInstance", ProxyBean.class, 
            new Class[] { Object.class }); 
        m.makePublic();
        code = m.getCode(true);
        code.anew().setType(bc); 
        code.dup();
        if (params.length == 1) {
            code.aload().setParam(0);
            code.checkcast().setType(params[0]);
        }
        code.invokespecial().setMethod("<init>", void.class, params);
        if (params.length == 0)
            copyProperties(type, code);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Copy bean properties.  Called with the copy object on the stack.  Must
     * return with the copy object on the stack.
     */
    private void copyProperties(Class type, Code code) {
        int copy = code.getNextLocalsIndex();
        code.astore().setLocal(copy);
        
        Method[] meths = type.getMethods();
        Method getter;
        int mods;
        for (int i = 0; i < meths.length; i++) {
            mods = meths[i].getModifiers(); 
            if (!Modifier.isPublic(mods) || Modifier.isStatic(mods))
                continue;
            if (!startsWith(meths[i].getName(), "set")
                || meths[i].getParameterTypes().length != 1)
                continue;
            getter = findGetter(type, meths[i]);
            if (getter == null)
                continue;

            // copy.setXXX(orig.getXXX());
            code.aload().setLocal(copy);
            code.aload().setParam(0);
            code.checkcast().setType(type);
            code.invokevirtual().setMethod(getter);
            code.invokevirtual().setMethod(meths[i]);
        }
        code.aload().setLocal(copy);
    }

    /**
     * Proxy recognized methods to invoke helpers in given helper class.
     */
    private void proxyRecognizedMethods(BCClass bc, Class type, Class helper,
        Class proxyType) {
        Method[] meths = type.getMethods();
        Class[] params;
        Class[] afterParams;
        Method match;
        Method after;
        for (int i = 0; i < meths.length; i++) {
            params = toHelperParameters(meths[i].getParameterTypes(), 
                proxyType);

            // first check for overriding method 
            try {
                match = helper.getMethod(meths[i].getName(), params); 
                proxyOverrideMethod(bc, meths[i], match, params);
                continue;
            } catch (NoSuchMethodException nsme) {
            } catch (Exception e) {
                throw new GeneralException(e);
            }

            // check for before and after methods, either of which may not
            // exist
            match = null;
            try {
                match = helper.getMethod("before" 
                    + StringUtils.capitalize(meths[i].getName()), params);
            } catch (NoSuchMethodException nsme) {
            } catch (Exception e) {
                throw new GeneralException(e);
            }
            after = null;
            afterParams = null;
            try {
                afterParams = toHelperAfterParameters(params, 
                    meths[i].getReturnType(), (match == null) 
                    ? void.class : match.getReturnType());
                after = helper.getMethod("after" 
                    + StringUtils.capitalize(meths[i].getName()), afterParams);
            } catch (NoSuchMethodException nsme) {
            } catch (Exception e) {
                throw new GeneralException(e);
            }
            if (match != null || after != null)
                proxyBeforeAfterMethod(bc, type, meths[i], match, params, after,
                    afterParams);
        }
    }

    /**
     * Return the parameter types to the corresponding helper class method.
     */ 
    private static Class[] toHelperParameters(Class[] cls, Class helper) {
        Class[] params = new Class[cls.length + 1];
        params[0] = helper;
        System.arraycopy(cls, 0, params, 1, cls.length); 
        return params;
    }

    /**
     * Return the parameter types to the corresponding helper class "after"     
     * method.
     */
    private static Class[] toHelperAfterParameters(Class[] cls, Class ret,
        Class beforeRet) {
        if (ret == void.class && beforeRet == void.class)
            return cls;
        int len = cls.length;
        if (ret != void.class)
            len++;
        if (beforeRet != void.class)
            len++;
        Class[] params = new Class[len];
        System.arraycopy(cls, 0, params, 0, cls.length);
        int pos = cls.length;
        if (ret != void.class)
            params[pos++] = ret;
        if (beforeRet != void.class)
            params[pos++] = beforeRet;
        return params;
    }

    /**
     * Proxy setter methods of the given type.
     * 
     * @return true if we find any setters, false otherwise
     */
    private boolean proxySetters(BCClass bc, Class type) {
        Method[] meths = type.getMethods();
        int setters = 0;
        for (int i = 0; i < meths.length; i++) {
            if (isSetter(meths[i]) && !Modifier.isFinal(meths[i].getModifiers())
                && bc.getDeclaredMethod(meths[i].getName(),
                meths[i].getParameterTypes()) == null) {
                setters++;
                proxySetter(bc, type, meths[i]);
            }
        } 
        return setters > 0;
    }

    /**
     * Proxy the given method with one that overrides it by calling into the
     * given helper.
     */
    private void proxyOverrideMethod(BCClass bc, Method meth, 
        Method helper, Class[] params) {
        BCMethod m = bc.declareMethod(meth.getName(), meth.getReturnType(),
            meth.getParameterTypes());
        m.makePublic();
        Code code = m.getCode(true);

        code.aload().setThis();
        for (int i = 1; i < params.length; i++)
            code.xload().setParam(i - 1).setType(params[i]);
        code.invokestatic().setMethod(helper);
        code.xreturn().setType(meth.getReturnType());

        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Proxy the given method with one that overrides it by calling into the
     * given helper.
     */
    private void proxyBeforeAfterMethod(BCClass bc, Class type, Method meth, 
        Method before, Class[] params, Method after, Class[] afterParams) {
        BCMethod m = bc.declareMethod(meth.getName(), meth.getReturnType(),
            meth.getParameterTypes());
        m.makePublic();
        Code code = m.getCode(true);

        // invoke before
        int beforeRet = -1;
        if (before != null) {
            code.aload().setThis();
            for (int i = 1; i < params.length; i++)
                code.xload().setParam(i - 1).setType(params[i]);
            code.invokestatic().setMethod(before);
            if (after != null && before.getReturnType() != void.class) {
                beforeRet = code.getNextLocalsIndex();
                code.xstore().setLocal(beforeRet).
                    setType(before.getReturnType());
            }
        }

        // invoke super
        code.aload().setThis();
        for (int i = 1; i < params.length; i++)
            code.xload().setParam(i - 1).setType(params[i]);
        code.invokespecial().setMethod(type, meth.getName(), 
            meth.getReturnType(), meth.getParameterTypes());

        // invoke after 
        if (after != null) {
            int ret = -1;
            if (meth.getReturnType() != void.class) {
                ret = code.getNextLocalsIndex();
                code.xstore().setLocal(ret).setType(meth.getReturnType());
            }
            code.aload().setThis();
            for (int i = 1; i < params.length; i++)
                code.xload().setParam(i - 1).setType(params[i]);
            if (ret != -1)
                code.xload().setLocal(ret).setType(meth.getReturnType());
            if (beforeRet != -1)
                code.xload().setLocal(beforeRet).
                    setType(before.getReturnType());
            code.invokestatic().setMethod(after);
        }
        code.xreturn().setType(meth.getReturnType());

        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Return whether the given method is a setter.
     */
    protected boolean isSetter(Method meth) {
        return startsWith(meth.getName(), "set")
            || startsWith(meth.getName(), "add")
            || startsWith(meth.getName(), "remove")
            || startsWith(meth.getName(), "insert")
            || startsWith(meth.getName(), "clear")
            || startsWith(meth.getName(), "roll"); // used by Calendar
    }

    /**
     * Return the getter corresponding to the given setter, or null.
     */
    protected Method findGetter(Class type, Method setter) {
        String name = setter.getName().substring(3);
        Class param = setter.getParameterTypes()[0];
        Method getter;
        try {
            getter = type.getMethod("get" + name, (Class[]) null);   
            if (getter.getReturnType().isAssignableFrom(param)
                || param.isAssignableFrom(getter.getReturnType()))
                return getter;
        } catch (NoSuchMethodException nsme) {
        } catch (Exception e) {
            throw new GeneralException(e);
        }

        if (param == boolean.class || param == Boolean.class) {
            try {
                getter = type.getMethod("is" + name, (Class[]) null);   
                if (getter.getReturnType().isAssignableFrom(param)
                    || param.isAssignableFrom(getter.getReturnType()))
                    return getter;
            } catch (NoSuchMethodException nsme) {
            } catch (Exception e) {
                throw new GeneralException(e);
            }
        }
        return null;
    }

    /**
     * Return whether the target string stars with the given token.
     */
    private static boolean startsWith(String str, String token) {
        return str.startsWith(token)
            && (str.length() == token.length() 
            || Character.isUpperCase(str.charAt(token.length())));
    }

    /**
     * Proxy the given setter method to dirty the proxy owner.
     */
    private void proxySetter(BCClass bc, Class type, Method meth) {
        Class[] params = meth.getParameterTypes();
        Class ret = meth.getReturnType();
        BCMethod m = bc.declareMethod(meth.getName(), ret, params);
        m.makePublic();
        Code code = m.getCode(true);
        code.aload().setThis();
        code.constant().setValue(true);
        code.invokestatic().setMethod(Proxies.class, "dirty", void.class,
            new Class[] { Proxy.class, boolean.class });
        code.aload().setThis();
        for (int i = 0; i < params.length; i++)
            code.xload().setParam(i).setType(params[i]);
        code.invokespecial().setMethod(type, meth.getName(), ret, params);
        code.xreturn().setType(ret);
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    /**
     * Add a writeReplace implementation that serializes to a non-proxy type
     * unless detached and this is a build-time generated class.
     */
    private void addWriteReplaceMethod(BCClass bc, boolean runtime) {
        BCMethod m = bc.declareMethod("writeReplace", Object.class, null);
        m.makeProtected();
        m.getExceptions(true).addException(ObjectStreamException.class);
        Code code = m.getCode(true);
        code.aload().setThis();
        code.constant().setValue(!runtime);
        code.invokestatic().setMethod(Proxies.class, "writeReplace", 
            Object.class, new Class[] { Proxy.class, boolean.class });
        code.areturn();
        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    /**
     * Create a unique id to avoid proxy class name conflicts.
     */
    private static synchronized long nextProxyId() {
        return _proxyId++;
    }

    /**
     * Find an appropriate copy constructor for the given type, or return null
     * if none.
     */
    protected Constructor findCopyConstructor(Class cls) {
        Constructor[] cons = cls.getConstructors();
        Constructor match = null;
        Class matchParam = null;
        Class[] params;
        for (int i = 0; i < cons.length; i++) {
            params = cons[i].getParameterTypes();
            if (params.length != 1)
                continue;

            // quit immediately on exact match
            if (params[0] == cls)
                return cons[i];

            if (params[0].isAssignableFrom(cls) && (matchParam == null
                || matchParam.isAssignableFrom(params[0]))) {
                 // track most derived collection constructor
                match = cons[i];
                matchParam = params[0];
            }
        }
        return match; 
    }

    /**
     * Return the constructor that takes a comparator for the given type, or
     * null if none.
     */
    private static Constructor findComparatorConstructor(Class cls) {
        try {
            return cls.getConstructor(new Class[] { Comparator.class });
        } catch (NoSuchMethodException nsme) {
            return null;
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

    /**
     * Usage: java org.apache.openjpa.util.proxy.ProxyManagerImpl [option]*
     * &lt;class name&gt;+<br />
     * Where the following options are recognized:
     * <ul> 
     * <li><i>-utils/-u &lt;number&gt;</i>: Generate proxies for the standard
     * java.util collection, map, date, and calendar classes of the given Java
     * version.  Use 4 for Java 1.4, 5 for Java 5, etc.</li>
     * </ul>
     *
     * The main method generates .class files for the proxies to the classes    
     * given on the command line.  It writes the generated classes to beside the
     * ProxyManagerImpl.class file if possible; otherwise it writes to the 
     * current directory.  The proxy manager looks for these classes 
     * before generating its own proxies at runtime.
     */
    public static void main(String[] args) 
        throws ClassNotFoundException, IOException {
        File dir = Files.getClassFile(ProxyManagerImpl.class);
        dir = (dir == null) ? new File(AccessController.doPrivileged(
            J2DoPrivHelper.getPropertyAction("user.dir")))
            : dir.getParentFile();

        Options opts = new Options();
        args = opts.setFromCmdLine(args);

        List types = new ArrayList();
        types.addAll(Arrays.asList(args));
        int utils = opts.removeIntProperty("utils", "u", 0);
        if (utils >= 4) {
            types.addAll(Arrays.asList(new String[] {
                java.sql.Date.class.getName(),
                java.sql.Time.class.getName(),
                java.sql.Timestamp.class.getName(),
                java.util.ArrayList.class.getName(),
                java.util.Date.class.getName(),
                java.util.GregorianCalendar.class.getName(),
                java.util.HashMap.class.getName(),
                java.util.HashSet.class.getName(),
                java.util.Hashtable.class.getName(),
                java.util.LinkedList.class.getName(),
                java.util.Properties.class.getName(),
                java.util.TreeMap.class.getName(),
                java.util.TreeSet.class.getName(),
                java.util.Vector.class.getName(),
            })); 
        }
        if (utils >= 5) {
            types.addAll(Arrays.asList(new String[] {
                "java.util.EnumMap",
                "java.util.IdentityHashMap",
                "java.util.LinkedHashMap",
                "java.util.LinkedHashSet",
                "java.util.PriorityQueue",
            })); 
        }

        final ProxyManagerImpl mgr = new ProxyManagerImpl();
        Class cls;
        BCClass bc;
        for (int i = 0; i < types.size(); i++) {
            cls = Class.forName((String) types.get(i));
            try {
                if (Class.forName(getProxyClassName(cls, false), true,
                    GeneratedClasses.getMostDerivedLoader(cls, Proxy.class))
                    != null)
                    continue;
            } catch (Throwable t) {
                // expected if the class hasn't been generated
            }

            if (Collection.class.isAssignableFrom(cls))
                bc = mgr.generateProxyCollectionBytecode(cls, false);         
            else if (Map.class.isAssignableFrom(cls))
                bc = mgr.generateProxyMapBytecode(cls, false);         
            else if (Date.class.isAssignableFrom(cls))
                bc = mgr.generateProxyDateBytecode(cls, false);
            else if (Calendar.class.isAssignableFrom(cls))
                bc = mgr.generateProxyCalendarBytecode(cls, false);
            else {
                final Class fCls = cls;
                // TODO Move this to J2DOPrivHelper
                bc = AccessController
                    .doPrivileged(new PrivilegedAction<BCClass>() {
                        public BCClass run() {
                            return mgr.generateProxyBeanBytecode(fCls, false);
                        }
                    });
            }
            // START - ALLOW PRINT STATEMENTS
            System.out.println(bc.getName());
            // STOP - ALLOW PRINT STATEMENTS
            AsmAdaptor.write(bc, new File(dir, bc.getClassName() + ".class"));
        }
    }
}
