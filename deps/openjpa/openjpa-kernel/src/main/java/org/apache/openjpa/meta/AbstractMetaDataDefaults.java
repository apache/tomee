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
package org.apache.openjpa.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * Abstract implementation provides a set of generic utilities for detecting
 * persistence meta-data of Field/Member. Also provides bean-style properties 
 * such as access style or identity type to be used by default when such 
 * information is not derivable from available meta-data.
 *
 * @author Abe White
 * @author Pinaki Poddar
 */
public abstract class AbstractMetaDataDefaults
    implements MetaDataDefaults {

    private static final Localizer _loc = Localizer.forPackage
        (AbstractMetaDataDefaults.class);

    private int _access = AccessCode.FIELD;
    private int _identity = ClassMetaData.ID_UNKNOWN;
    private boolean _ignore = true;
    private boolean _interface = true;
    private boolean _pcRegistry = true;
    private int _callback = CALLBACK_RETHROW;
    private boolean _unwrapped = false;

    /**
     * Whether to attempt to use the information from registered classes
     * to populate metadata defaults. Defaults to true.
     */
    public boolean getUsePCRegistry() {
        return _pcRegistry;
    }

    /**
     * Whether to attempt to use the information from registered classes
     * to populate metadata defaults. Defaults to true.
     */
    public void setUsePCRegistry(boolean pcRegistry) {
        _pcRegistry = pcRegistry;
    }

    /**
     * The default access type for base classes with ACCESS_UNKNOWN.
     * ACCESS_FIELD by default.
     */
    public int getDefaultAccessType() {
        return _access;
    }

    /**
     * The default access type for base classes with ACCESS_UNKNOWN.
     * ACCESS_FIELD by default.
     */
    public void setDefaultAccessType(int access) {
        _access = access;
    }

    /**
     * The default identity type for unmapped classes without primary 
     * key fields. ID_UNKNOWN by default.
     */
    public int getDefaultIdentityType() {
        return _identity;
    }

    /**
     * The default identity type for unmapped classes without primary 
     * key fields. ID_UNKNOWN by default.
     */
    public void setDefaultIdentityType(int identity) {
        _identity = identity;
    }

    public int getCallbackMode() {
        return _callback;
    }

    public void setCallbackMode(int mode) {
        _callback = mode;
    }

    public void setCallbackMode(int mode, boolean on) {
        if (on)
            _callback |= mode;
        else
            _callback &= ~mode;
    }

    public boolean getCallbacksBeforeListeners(int type) {
        return false;
    }

    public boolean isDeclaredInterfacePersistent() {
        return _interface;
    }

    public void setDeclaredInterfacePersistent(boolean pers) {
        _interface = pers;
    }

    public boolean isDataStoreObjectIdFieldUnwrapped() {
        return _unwrapped;
    }

    public void setDataStoreObjectIdFieldUnwrapped(boolean unwrapped) {
        _unwrapped = unwrapped;
    }

    public boolean getIgnoreNonPersistent() {
        return _ignore;
    }

    public void setIgnoreNonPersistent(boolean ignore) {
        _ignore = ignore;
    }

    public void populate(ClassMetaData meta, int access) {
        populate(meta, access, false);
    }
    
    public void populate(ClassMetaData meta, int access, boolean ignoreTransient) {
        if (meta.getDescribedType() == Object.class)
            return;
        meta.setAccessType(access);

        Log log = meta.getRepository().getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("gen-meta", meta));
        if (!_pcRegistry || !populateFromPCRegistry(meta)) {
            if (log.isTraceEnabled())
                log.trace(_loc.get("meta-reflect"));
            populateFromReflection(meta, ignoreTransient);
        }
    }

    /**
     * Populate the given metadata using the {@link PCRegistry}.
     */
    private boolean populateFromPCRegistry(ClassMetaData meta) {
        Class<?> cls = meta.getDescribedType();
        if (!PCRegistry.isRegistered(cls))
            return false;
        try {
            String[] fieldNames = PCRegistry.getFieldNames(cls);
            Class<?>[] fieldTypes = PCRegistry.getFieldTypes(cls);
            Member member;
            FieldMetaData fmd;
            for (int i = 0; i < fieldNames.length; i ++) {
            	String property = fieldNames[i];
                member = getMemberByProperty(meta, property,
                	AccessCode.UNKNOWN, true);
                if (member == null) // transient or indeterminable access
                	continue;
                fmd = meta.addDeclaredField(property, fieldTypes[i]);
                fmd.backingMember(member);
                populate(fmd);
            }
            return true;
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            if (e instanceof PrivilegedActionException)
                e = ((PrivilegedActionException) e).getException();
            throw new UserException(e);
        }
    }

    protected abstract List<Member> getPersistentMembers(ClassMetaData meta, boolean ignoreTransient);
    /**
     * Generate the given meta-data using reflection.
     * Adds FieldMetaData for each persistent state.
     * Delegate to concrete implementation to determine the persistent
     * members.
     */
    private void populateFromReflection(ClassMetaData meta, boolean ignoreTransient) {
        List<Member> members = getPersistentMembers(meta, ignoreTransient);
        boolean iface = meta.getDescribedType().isInterface();
        // If access is mixed or if the default is currently unknown, 
        // process all fields, otherwise only process members of the class  
        // level default access type. 
        
        String name;
        boolean def;
        FieldMetaData fmd;
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            name = getFieldName(member);
            if (name == null || isReservedFieldName(name))
                continue;

            def = isDefaultPersistent(meta, member, name, ignoreTransient);
            if (!def && _ignore)
                continue;

            // passed the tests; persistent type -- we construct with
            // Object.class because setting backing member will set proper
            // type anyway
            fmd = meta.addDeclaredField(name, Object.class);
            fmd.backingMember(member);
            if (!def) {
                fmd.setExplicit(true);
                fmd.setManagement(FieldMetaData.MANAGE_NONE);
            }
            populate(fmd);
        }
    }

    protected void populate(FieldMetaData fmd) {
    	
    }
    
    /**
     * Return the list of fields in <code>meta</code> that use field access,
     * or <code>null</code> if a list of fields is unobtainable. An empty list
     * should be returned if the list of fields is obtainable, but there
     * happens to be no field access in <code>meta</code>.
     *
     * This is used for error reporting purposes only, so need not be efficient.
     *
     * This implementation returns <code>null</code>.
     */
    protected List<String> getFieldAccessNames(ClassMetaData meta) {
        return null;
    }

    /**
     * Return the list of methods in <code>meta</code> that use property access,
     * or <code>null</code> if a list of methods is unobtainable. An empty list
     * should be returned if the list of methods is obtainable, but there
     * happens to be no property access in <code>meta</code>.
     *
     * This is used for error reporting purposes only, so need not be efficient.
     *
     * This implementation returns <code>null</code>.
     */
    protected List<String> getPropertyAccessNames(ClassMetaData meta) {
        return null;
    }

    /**
     * Return the field name for the given member. This will only be invoked
     * on members of the right type (field vs. method). Return null if the
     * member cannot be managed. Default behavior: For fields, returns the
     * field name. For getter methods, returns the minus "get" or "is" with
     * the next letter lower-cased. For other methods, returns null.
     */
    public static String getFieldName(Member member) {
        if (member instanceof Field)
            return member.getName();
        if (member instanceof Method == false)
        	return null;
        Method method = (Method) member;
        String name = method.getName();
        if (isNormalGetter(method))
        	name = name.substring("get".length());
        else if (isBooleanGetter(method))
        	name = name.substring("is".length());
        else
            return null;

        if (name.length() == 1)
            return name.toLowerCase();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Returns true if the given field name is reserved for unmanaged fields.
     */
    protected boolean isReservedFieldName(String name) {
        // names used by enhancers
        return name.startsWith("openjpa") || name.startsWith("jdo");
    }

    /**
     * Return true if the given member is persistent by default. This will
     * only be invoked on members of the right type (field vs. method).
     * Returns false if member is static or final by default.
     *
     * @param name the field name from {@link #getFieldName}
     */
    protected abstract boolean isDefaultPersistent(ClassMetaData meta,
        Member member, String name, boolean ignoreTransient);

    /**
     * Gets the backing member of the given field. If the field has not been
     * assigned a backing member then get either the instance field or the
     * getter method depending upon the access style of the defining class.
     * <br>
     * Defining class is used instead of declaring class because this method
     * may be invoked during parsing phase when declaring metadata may not be
     * available.  
     */
    public Member getBackingMember(FieldMetaData fmd) {
        if (fmd == null)
            return null;
        if (fmd.getBackingMember() != null)
        	return fmd.getBackingMember();
        return getMemberByProperty(fmd.getDeclaringMetaData(), fmd.getName(),
            fmd.getAccessType(), true);
    }
    
    public Class<?> getUnimplementedExceptionType() {
        return UnsupportedOperationException.class;
    }

    /**
     * Helper method; returns true if the given class appears to be
     * user-defined.
     */
    protected static boolean isUserDefined(Class<?> cls) {
        return cls != null && !cls.getName().startsWith("java.")
            && !cls.getName().startsWith ("javax.");
	}
    
    /**
     * Affirms if the given method matches the following signature
     * <code> public T getXXX() </code>
     * where T is any non-void type.
     */
    public static boolean isNormalGetter(Method method) {
    	String methodName = method.getName();
    	return startsWith(methodName, "get") 
    	    && method.getParameterTypes().length == 0
    	    && method.getReturnType() != void.class;
    }
    
    /**
     * Affirms if the given method matches the following signature
     * <code> public boolean isXXX() </code>
     * <code> public Boolean isXXX() </code>
     */
    public static boolean isBooleanGetter(Method method) {
    	String methodName = method.getName();
    	return startsWith(methodName, "is") 
    	    && method.getParameterTypes().length == 0
    	    && isBoolean(method.getReturnType());
    }

    /**
     * Affirms if the given method signature matches bean-style getter method
     * signature.<br>
     * <code> public T getXXX()</code> where T is any non-void type.<br>
     * or<br>
     * <code> public T isXXX()</code> where T is boolean or Boolean.<br>
     */
    public static boolean isGetter(Method method, boolean includePrivate) {
    	if (method == null)
    		return false;
    	int mods = method.getModifiers();
    	if (!(Modifier.isPublic(mods) 
    	      || Modifier.isProtected(mods)
    	      || (Modifier.isPrivate(mods) && includePrivate))
    	 || Modifier.isNative(mods) 
    	 || Modifier.isStatic(mods))
    		return false;
    	return isNormalGetter(method) || isBooleanGetter(method);
    }
    
    /**
     * Affirms if the given full string starts with the given head.
     */
    public static boolean startsWith(String full, String head) {
        return full != null && head != null && full.startsWith(head) 
            && full.length() > head.length();
    }
    
    public static boolean isBoolean(Class<?> cls) {
    	return cls == boolean.class || cls == Boolean.class;
    }
    
    public static List<String> toNames(List<? extends Member> members) {
    	List<String> result = new ArrayList<String>();
    	for (Member m : members)
    		result.add(m.getName());
    	return result;
    }

}
