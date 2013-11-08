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
package org.apache.openjpa.persistence;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.meta.AbstractMetaDataDefaults;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.AccessType.PROPERTY;
import static org.apache.openjpa.persistence.PersistenceStrategy.*;

import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.Reflection;

/**
 * JPA-based metadata defaults.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @author Pinaki Poddar
 * @nojavadoc
 */
public class PersistenceMetaDataDefaults
    extends AbstractMetaDataDefaults {

    private static final Localizer _loc = Localizer.forPackage
        (PersistenceMetaDataDefaults.class);

    private static final Map<Class<?>, PersistenceStrategy> _strats =
        new HashMap<Class<?>, PersistenceStrategy>();
    private static final Set<String> _ignoredAnnos = new HashSet<String>();

    static {
        _strats.put(Basic.class, BASIC);
        _strats.put(ManyToOne.class, MANY_ONE);
        _strats.put(OneToOne.class, ONE_ONE);
        _strats.put(Embedded.class, EMBEDDED);
        _strats.put(EmbeddedId.class, EMBEDDED);
        _strats.put(OneToMany.class, ONE_MANY);
        _strats.put(ManyToMany.class, MANY_MANY);
        _strats.put(Persistent.class, PERS);
        _strats.put(PersistentCollection.class, PERS_COLL);
        _strats.put(ElementCollection.class, ELEM_COLL);
        _strats.put(PersistentMap.class, PERS_MAP);

        _ignoredAnnos.add(DetachedState.class.getName());
        _ignoredAnnos.add(PostLoad.class.getName());
        _ignoredAnnos.add(PostPersist.class.getName());
        _ignoredAnnos.add(PostRemove.class.getName());
        _ignoredAnnos.add(PostUpdate.class.getName());
        _ignoredAnnos.add(PrePersist.class.getName());
        _ignoredAnnos.add(PreRemove.class.getName());
        _ignoredAnnos.add(PreUpdate.class.getName());
    }

	/**
     * Set of Inclusion Filters based on member type, access type or transient
     * annotations. Used to determine the persistent field/methods.
     */
    protected AccessFilter propertyAccessFilter = new AccessFilter(PROPERTY);
    protected AccessFilter fieldAccessFilter = new AccessFilter(FIELD);

    protected MemberFilter fieldFilter = new MemberFilter(Field.class);
    protected MemberFilter methodFilter = new MemberFilter(Method.class);
    protected TransientFilter nonTransientFilter = new TransientFilter(false);
    protected AnnotatedFilter annotatedFilter = new AnnotatedFilter();
    protected GetterFilter getterFilter = new GetterFilter();
    protected SetterFilter setterFilter = new SetterFilter();
    private Boolean _isAbstractMappingUniDirectional = null;
    private Boolean _isNonDefaultMappingAllowed = null;
    
    public PersistenceMetaDataDefaults() {
        setCallbackMode(CALLBACK_RETHROW | CALLBACK_ROLLBACK |
            CALLBACK_FAIL_FAST);
        setDataStoreObjectIdFieldUnwrapped(true);
    }

    /**
     * Return the code for the strategy of the given member. Return null if
     * no strategy.
     */
    public static PersistenceStrategy getPersistenceStrategy
    (FieldMetaData fmd, Member member) {
        return getPersistenceStrategy(fmd, member, false);
    }
    
    /**
     * Return the code for the strategy of the given member. Return null if
     * no strategy.
     */
    public static PersistenceStrategy getPersistenceStrategy
        (FieldMetaData fmd, Member member, boolean ignoreTransient) {
        if (member == null)
            return null;
        AnnotatedElement el = (AnnotatedElement) member;
        if (!ignoreTransient && (AccessController.doPrivileged(J2DoPrivHelper
            .isAnnotationPresentAction(el, Transient.class))).booleanValue())
            return TRANSIENT;
        if (fmd != null
            && fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            return null;

        // look for persistence strategy in annotation table
        PersistenceStrategy pstrat = null;
        for (Annotation anno : el.getDeclaredAnnotations()) {
            if (pstrat != null && _strats.containsKey(anno.annotationType()))
                throw new MetaDataException(_loc.get("already-pers", member));
            if (pstrat == null)
                pstrat = _strats.get(anno.annotationType());
        }
        if (pstrat != null)
            return pstrat;

        Class type;
        int code;
        if (fmd != null) {
            type = fmd.getType();
            code = fmd.getTypeCode();
        } else if (member instanceof Field) {
            type = ((Field) member).getType();
            code = JavaTypes.getTypeCode(type);
        } else {
            type = ((Method) member).getReturnType();
            code = JavaTypes.getTypeCode(type);
        }

        switch (code) {
            case JavaTypes.ARRAY:
                if (type == byte[].class
                    || type == char[].class
                    || type == Byte[].class
                    || type == Character[].class)
                    return BASIC;
                break;
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.STRING:
            case JavaTypes.BIGDECIMAL:
            case JavaTypes.BIGINTEGER:
            case JavaTypes.DATE:
                return BASIC;
            case JavaTypes.OBJECT:
                if (Enum.class.isAssignableFrom(type))
                    return BASIC;
                break;
        }

        //### EJB3: what if defined in XML?
        if ((AccessController.doPrivileged(J2DoPrivHelper
            .isAnnotationPresentAction(type, Embeddable.class))).booleanValue())
            return EMBEDDED;
        if (Serializable.class.isAssignableFrom(type))
            return BASIC;
        return null;
    }
    
    /**
     * Auto-configuration method for the default access type of base classes 
     * with ACCESS_UNKNOWN
     */
    public void setDefaultAccessType(String type) {
        if ("PROPERTY".equals(type.toUpperCase()))
            setDefaultAccessType(AccessCode.PROPERTY);
        else if ("FIELD".equals(type.toUpperCase()))
            setDefaultAccessType(AccessCode.FIELD);
        else
        	throw new IllegalArgumentException(_loc.get("access-invalid", 
        	    type).toString());
    }

    /**
     * Populates the given class metadata. The access style determines which
     * field and/or getter method will contribute as the persistent property
     * of the given class. If the given access is unknown, then the access
     * type is to be determined at first. 
     * 
     * @see #determineAccessType(ClassMetaData)
     */
    @Override
    public void populate(ClassMetaData meta, int access) {
        populate(meta, access, false);
    }
    
    /**
     * Populates the given class metadata. The access style determines which
     * field and/or getter method will contribute as the persistent property
     * of the given class. If the given access is unknown, then the access
     * type is to be determined at first. 
     * 
     * @see #determineAccessType(ClassMetaData)
     */
    @Override
    public void populate(ClassMetaData meta, int access, boolean ignoreTransient) {
    	if (AccessCode.isUnknown(access)) {
    		access = determineAccessType(meta);
    	}
    	if (AccessCode.isUnknown(access)) {
    		error(meta, _loc.get("access-unknown", meta));
    	}
        super.populate(meta, access, ignoreTransient);
        meta.setDetachable(true);
        // do not call get*Fields as it will lock down the fields.
    }

    @Override
    protected void populate(FieldMetaData fmd) {
        setCascadeNone(fmd);
        setCascadeNone(fmd.getKey());
        setCascadeNone(fmd.getElement());
    }

    /**
     * Turns off auto cascading of persist, refresh, attach, detach.
     */
    static void setCascadeNone(ValueMetaData vmd) {
        vmd.setCascadePersist(ValueMetaData.CASCADE_NONE);
        vmd.setCascadeRefresh(ValueMetaData.CASCADE_NONE);
        vmd.setCascadeAttach(ValueMetaData.CASCADE_NONE);
        vmd.setCascadeDetach(ValueMetaData.CASCADE_NONE);
    }
    
    ClassMetaData getCachedSuperclassMetaData(ClassMetaData meta) {
    	if (meta == null)
    		return null;
    	Class<?> cls = meta.getDescribedType();
    	Class<?> sup = cls.getSuperclass();
    	if (sup == null || "java.lang.Object".equals(
    	    sup.getName()))
    		return null;
    	MetaDataRepository repos = meta.getRepository();
    	ClassMetaData supMeta = repos.getCachedMetaData(sup);
    	if (supMeta == null)
    		supMeta = repos.getMetaData(sup, null, false);
    	return supMeta;
    }

    /**
     * Recursive helper to determine access type based on annotation placement
     * on members for the given class without an explicit access annotation.
     * 
     * @return must return a not-unknown access code 
     */
    private int determineAccessType(ClassMetaData meta) {
    	if (meta == null)
    		return AccessCode.UNKNOWN;
        if (meta.getDescribedType().isInterface()) // managed interfaces 
        	return AccessCode.PROPERTY;
    	if (!AccessCode.isUnknown(meta))
    		return meta.getAccessType();
    	int access = determineExplicitAccessType(meta.getDescribedType());
    	if (!AccessCode.isUnknown(access))
    		return access;
    	access = determineImplicitAccessType(meta.getDescribedType(),
    	            meta.getRepository().getConfiguration());
    	if (!AccessCode.isUnknown(access))
    		return access;
    	
    	ClassMetaData sup = getCachedSuperclassMetaData(meta);
    	ClassMetaData tmpSup = sup;
    	while (tmpSup != null && tmpSup.isExplicitAccess()) {
            tmpSup = getCachedSuperclassMetaData(tmpSup);
            if (tmpSup != null) {
                sup = tmpSup;
            }    	    
    	}
    	if (sup != null && !AccessCode.isUnknown(sup))
    		return sup.getAccessType();

        trace(meta, _loc.get("access-default", meta, AccessCode.toClassString(getDefaultAccessType())));
        return getDefaultAccessType();
    }
    
    /**
     * Determines the access type for the given class by placement of 
     * annotations on field or getter method. Does not consult the
     * super class.
     * 
     * Annotation can be placed on either fields or getters but not on both.
     * If no field or getter is annotated then UNKNOWN access code is returned.
     */
    private int determineImplicitAccessType(Class<?> cls, OpenJPAConfiguration
        conf) {
    	if (cls.isInterface()) // Managed interfaces
    		return AccessCode.PROPERTY;
        Field[] allFields = AccessController.doPrivileged(J2DoPrivHelper.
                getDeclaredFieldsAction(cls));
		Method[] methods = AccessController.doPrivileged(
				J2DoPrivHelper.getDeclaredMethodsAction(cls));
        List<Field> fields = filter(allFields, new TransientFilter(true));
        /*
         * OpenJPA 1.x permitted private properties to be persistent.  This is
         * contrary to the JPA 1.0 specification, which states that persistent
         * properties must be public or protected. OpenJPA 2.0+ will adhere
         * to the specification by default, but provides a compatibility
         * option to provide pre-2.0 behavior.
         */
        getterFilter.setIncludePrivate(
            conf.getCompatibilityInstance().getPrivatePersistentProperties());
        List<Method> getters = filter(methods, getterFilter);
        if (fields.isEmpty() && getters.isEmpty())
        	return AccessCode.EMPTY;
        
        fields = filter(fields, annotatedFilter);
        getters = filter(getters, annotatedFilter);
        
        List<Method> setters = filter(methods, setterFilter);
        getters =  matchGetterAndSetter(getters, setters);
        
        boolean mixed = !fields.isEmpty() && !getters.isEmpty();
        if (mixed)
        	throw new UserException(_loc.get("access-mixed", 
        		cls, toFieldNames(fields), toMethodNames(getters)));
        if (!fields.isEmpty()) {
        	return AccessCode.FIELD;
        } 
        if (!getters.isEmpty()) {
        	return AccessCode.PROPERTY;
        } 
        return AccessCode.UNKNOWN;
    }
    
    /**
     * Explicit access type, if any, is generally detected by the parser. This
     * is only used for metadata of an embeddable type which is encountered
     * as a field during some other owning entity.
     * 
     * @see ValueMetaData#addEmbeddedMetaData()
     */
    private int determineExplicitAccessType(Class<?> cls) {
        Access access = cls.getAnnotation(Access.class);
        return access == null ? AccessCode.UNKNOWN : ((access.value() == 
            AccessType.FIELD ? AccessCode.FIELD : AccessCode.PROPERTY) |
            AccessCode.EXPLICIT);
    }
    
    /**
     * Matches the given getters with the given setters. Removes the getters
     * that do not have a corresponding setter.
     */
    private List<Method> matchGetterAndSetter(List<Method> getters,  
    		List<Method> setters) {
        Collection<Method> unmatched =  new ArrayList<Method>();
       
        for (Method getter : getters) {
            String getterName = getter.getName();
            Class<?> getterReturnType = getter.getReturnType();
            String expectedSetterName = "set" + getterName.substring(
                (isBooleanGetter(getter) ? "is" : "get").length());
            boolean matched = false;
            for (Method setter : setters) {
                Class<?> setterArgType = setter.getParameterTypes()[0];
                String actualSetterName = setter.getName();
                matched = actualSetterName.equals(expectedSetterName)
                    && setterArgType == getterReturnType;
                if (matched)
                    break;
            }
            if (!matched) {
                unmatched.add(getter);
            }

        }
        getters.removeAll(unmatched);
        return getters;
    }

    /**
     * Gets the fields that are possible candidate for being persisted. The  
     * result depends on the current access style of the given class. 
     */
    List<Field> getPersistentFields(ClassMetaData meta, boolean ignoreTransient) {
    	boolean explicit = meta.isExplicitAccess();
    	boolean unknown  = AccessCode.isUnknown(meta);
    	boolean isField  = AccessCode.isField(meta);
    	
    	if (explicit || unknown || isField) {
    		Field[] fields = AccessController.doPrivileged(J2DoPrivHelper.
                getDeclaredFieldsAction(meta.getDescribedType()));
    		
        	return filter(fields, fieldFilter, 
        	    ignoreTransient ? null : nonTransientFilter, 
        		unknown || isField  ? null : annotatedFilter, 
        	    explicit ? (isField ? null : fieldAccessFilter) : null);
    	} 
    	return Collections.EMPTY_LIST;
    }
    
    /**
     * Gets the methods that are possible candidate for being persisted. The  
     * result depends on the current access style of the given class. 
     */
    List<Method> getPersistentMethods(ClassMetaData meta, boolean ignoreTransient) {
    	boolean explicit = meta.isExplicitAccess();
    	boolean unknown  = AccessCode.isUnknown(meta.getAccessType());
    	boolean isProperty  = AccessCode.isProperty(meta.getAccessType());
    	
    	if (explicit || unknown || isProperty) {
    		Method[] publicMethods = AccessController.doPrivileged(
              J2DoPrivHelper.getDeclaredMethodsAction(meta.getDescribedType()));
        
            /*
             * OpenJPA 1.x permitted private accessor properties to be persistent.  This is
             * contrary to the JPA 1.0 specification, which states that persistent
             * properties must be public or protected. OpenJPA 2.0+ will adhere
             * to the specification by default, but provides a compatibility
             * option to provide pre-2.0 behavior.
             */
            getterFilter.setIncludePrivate(
                meta.getRepository().getConfiguration().getCompatibilityInstance().getPrivatePersistentProperties());

            List<Method> getters = filter(publicMethods, methodFilter, 
                getterFilter, 
                ignoreTransient ? null : nonTransientFilter, 
        		unknown || isProperty ? null : annotatedFilter, 
                explicit ? (isProperty ? null : propertyAccessFilter) : null);
            
            List<Method> setters = filter(publicMethods, setterFilter);
            return getters = matchGetterAndSetter(getters, setters);
    	}
        
    	return Collections.EMPTY_LIST;
    }
     
    /**
     * Gets the members that are backing members for attributes being persisted.
     * Unlike {@linkplain #getPersistentFields(ClassMetaData)} and 
     * {@linkplain #getPersistentMethods(ClassMetaData)} which returns 
     * <em>possible</em> candidates, the result of this method is definite.
     * 
     * Side-effect of this method is if the given class metadata has 
     * no access type set, this method will set it.
     */
    @Override
    public List<Member> getPersistentMembers(ClassMetaData meta, boolean ignoreTransient) {
    	List<Member> members = new ArrayList<Member>();
    	List<Field> fields   = getPersistentFields(meta, ignoreTransient);
    	List<Method> getters = getPersistentMethods(meta, ignoreTransient);
    	
    	boolean isMixed = !fields.isEmpty() && !getters.isEmpty();
    	boolean isEmpty = fields.isEmpty() && getters.isEmpty();

    	boolean explicit    = meta.isExplicitAccess();
    	boolean unknown     = AccessCode.isUnknown(meta.getAccessType());
    	
    	if (isEmpty) {
    		warn(meta, _loc.get("access-empty", meta));
    		return Collections.EMPTY_LIST;
    	}
    	if (explicit) {
    		if (isMixed) {
    			assertNoDuplicate(fields, getters);
                meta.setAccessType(AccessCode.MIXED | meta.getAccessType());
    			members.addAll(fields);
    			members.addAll(getters);
    		} else {
    			members.addAll(fields.isEmpty() ? getters : fields);
    		}
    	} else {
    		if (isMixed)
                error(meta, _loc.get("access-mixed", meta, fields, getters));
    		if (fields.isEmpty()) {
    			meta.setAccessType(AccessCode.PROPERTY);
    			members.addAll(getters);
    		} else {
    			meta.setAccessType(AccessCode.FIELD);
    			members.addAll(fields);
    		}
    	}
    	return members;
    }
    
    void assertNoDuplicate(List<Field> fields, List<Method> getters) {
    	
    }
    
    void error(ClassMetaData meta, Localizer.Message message) {
    	Log log = meta.getRepository().getConfiguration()
    		.getLog(OpenJPAConfiguration.LOG_RUNTIME);
    	log.error(message.toString());
    	throw new UserException(message.toString());
    }
    
    void warn(ClassMetaData meta, Localizer.Message message) {
    	Log log = meta.getRepository().getConfiguration()
		.getLog(OpenJPAConfiguration.LOG_RUNTIME);
    	log.warn(message.toString());
    }

    void trace(ClassMetaData meta, Localizer.Message message) {
        Log log = meta.getRepository().getConfiguration()
        .getLog(OpenJPAConfiguration.LOG_RUNTIME);
        log.trace(message.toString());
    }

    @Override
    protected List<String> getFieldAccessNames(ClassMetaData meta) {
    	return toNames(getPersistentFields(meta, false));
    }

    @Override
    protected List<String> getPropertyAccessNames(ClassMetaData meta) {
    	return toNames(getPersistentMethods(meta, false));
    }

    protected boolean isDefaultPersistent(ClassMetaData meta, Member member,
        String name) {
        return isDefaultPersistent(meta, member, name, false);
    }
    
    protected boolean isDefaultPersistent(ClassMetaData meta, Member member,
        String name, boolean ignoreTransient) {
        int mods = member.getModifiers();
        if (Modifier.isTransient(mods))
            return false;
        int access = meta.getAccessType();        
        
        if (member instanceof Field) {
            // If mixed or unknown, default property access, keep explicit 
            // field members
            if (AccessCode.isProperty(access)) {
                if (!isAnnotatedAccess(member, AccessType.FIELD))
                    return false;
            }
        }        
        else if (member instanceof Method) {
            // If mixed or unknown, field default access, keep explicit property
            // members
            if (AccessCode.isField(access)) {
                if (!isAnnotatedAccess(member, AccessType.PROPERTY))
                    return false;
            }            
            try {
                // check for setters for methods
                Method setter = (Method) AccessController.doPrivileged(
                    J2DoPrivHelper.getDeclaredMethodAction(
                        meta.getDescribedType(), "set" +
                        StringUtils.capitalize(name), new Class[] { 
                            ((Method) member).getReturnType() }));
                if (setter == null && !isAnnotatedTransient(member)) {
                    logNoSetter(meta, name, null);
                    return false;
                }
            } catch (Exception e) {
                // e.g., NoSuchMethodException
                if (!isAnnotatedTransient(member))
                    logNoSetter(meta, name, e);
                return false;
            }
        }

        PersistenceStrategy strat = getPersistenceStrategy(null, member, ignoreTransient);
        if (strat == null) {
            warn(meta, _loc.get("no-pers-strat", name));
            return false;
        } else if (strat == PersistenceStrategy.TRANSIENT) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isAnnotatedTransient(Member member) {
        return member instanceof AnnotatedElement
            && (AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(((AnnotatedElement) member),
                    Transient.class))).booleanValue();
    }

    /**
     * May be used to determine if member is annotated with the specified 
     * access type.
     * @param member class member
     * @param type expected access type
     * @return true if access is specified on member and that access
     *         type matches the expected type
     */
    private boolean isAnnotatedAccess(Member member, AccessType type) {
    	if (member == null)
    		return false;
        Access anno = 
            AccessController.doPrivileged(J2DoPrivHelper
                .getAnnotationAction((AnnotatedElement)member, 
                Access.class));
        return anno != null && anno.value() == type;
    }    

    private boolean isAnnotated(Member member) {
    	return member != null && member instanceof AnnotatedElement
    	    && annotatedFilter.includes((AnnotatedElement)member);
    }

    private boolean isNotTransient(Member member) {
        return member != null && member instanceof AnnotatedElement
            && nonTransientFilter.includes((AnnotatedElement)member);
    }

    /**
     * Gets either the instance field or the getter method depending upon the 
     * access style of the given meta-data.
     */
    public Member getMemberByProperty(ClassMetaData meta, String property, 
    	int access, boolean applyDefaultRule) {
    	Class<?> cls = meta.getDescribedType();
        Field field = Reflection.findField(cls, property, false);;
        Method getter = Reflection.findGetter(cls, property, false);
        Method setter = Reflection.findSetter(cls, property, false);
        int accessCode = AccessCode.isUnknown(access) ? meta.getAccessType() :
        	access;
        if (field == null && getter == null)
        	error(meta, _loc.get("access-no-property", cls, property));
    	if ((isNotTransient(getter) && isAnnotated(getter)) && 
    	     isNotTransient(field) && isAnnotated(field))
    		throw new IllegalStateException(_loc.get("access-duplicate", 
    			field, getter).toString());
    	
        if (AccessCode.isField(accessCode)) {
           if (isAnnotatedAccess(getter, AccessType.PROPERTY)) {
        	   meta.setAccessType(AccessCode.MIXED | meta.getAccessType());
               return getter;
           }
           return field == null ? getter : field; 
        } else if (AccessCode.isProperty(accessCode)) {
            if (isAnnotatedAccess(field, AccessType.FIELD)) {
         	   meta.setAccessType(AccessCode.MIXED | meta.getAccessType());
               return field;
            }            
            return getter == null ? field : getter;
        } else if (AccessCode.isUnknown(accessCode)) {
        	if (isAnnotated(field)) {
        		meta.setAccessType(AccessCode.FIELD);
        		return field;
        	} else if (isAnnotated(getter)) {
        		meta.setAccessType(AccessCode.PROPERTY);
        		return getter;
        	} else {
        		warn(meta, _loc.get("access-none", meta, property));
        		throw new IllegalStateException(
                    _loc.get("access-none", meta, property).toString());
        	}
        } else {
        	throw new InternalException(meta + " " + 
        		AccessCode.toClassString(meta.getAccessType()));
        }
    }
    
    // ========================================================================
    //  Selection Filters select specific elements from a collection.
    //  Used to determine the persistent members of a given class.
    // ========================================================================
    
    /**
     * Inclusive element filtering predicate.
     *
     */
    private static interface InclusiveFilter<T extends AnnotatedElement> {
        /**
         * Return true to include the given element.
         */
        boolean includes(T e);
    }

    /**
     * Filter the given collection with the conjunction of filters. The given
     * collection itself is not modified.
     */
    <T extends AnnotatedElement> List<T> filter(T[] array, 
    	InclusiveFilter... filters) {
        List<T> result = new ArrayList<T>();
        for (T e : array) {
            boolean include = true;
            for (InclusiveFilter f : filters) {
                if (f != null && !f.includes(e)) {
                    include = false;
                    break;
                }
            }
            if (include)
                result.add(e);
        }
        return result;
    }
    
    <T extends AnnotatedElement> List<T> filter(List<T> list, 
        	InclusiveFilter... filters) {
        List<T> result = new ArrayList<T>();
        for (T e : list) {
            boolean include = true;
            for (InclusiveFilter f : filters) {
                if (f != null && !f.includes(e)) {
                    include = false;
                    break;
                }
            }
            if (include)
                result.add(e);
        }
        return result;
    }

    /**
     * Selects getter method. A getter method name starts with 'get', returns a
     * non-void type and has no argument. Or starts with 'is', returns a boolean
     * and has no argument.
     * 
     */
    static class GetterFilter implements InclusiveFilter<Method> {
        
        private boolean includePrivate;        
                       
        public boolean includes(Method method) {
            return isGetter(method, isIncludePrivate());
        }

        public void setIncludePrivate(boolean includePrivate) {
            this.includePrivate = includePrivate;
        }

        public boolean isIncludePrivate() {
            return includePrivate;
        }
    }

    /**
     * Selects setter method. A setter method name starts with 'set', returns a
     * void and has single argument.
     * 
     */
    static class SetterFilter implements InclusiveFilter<Method> {
        public boolean includes(Method method) {
            return isSetter(method);
        }
        /**
         * Affirms if the given method matches the following signature
         * <code> public void setXXX(T t) </code>
         */
        public static boolean isSetter(Method method) {
        	String methodName = method.getName();
        	return startsWith(methodName, "set") 
        	    && method.getParameterTypes().length == 1
        	    && method.getReturnType() == void.class;
        }
    }

    /**
     * Selects elements which is annotated with @Access annotation and that 
     * annotation has the given AccessType value.
     * 
     */
    static class AccessFilter implements InclusiveFilter<AnnotatedElement> {
        final AccessType target;

        public AccessFilter(AccessType target) {
            this.target = target;
        }

        public boolean includes(AnnotatedElement obj) {
        	Access access = obj.getAnnotation(Access.class);
        	return access != null && access.value().equals(target);
        }
    }
    
    /**
     * Selects elements which is annotated with @Access annotation and that 
     * annotation has the given AccessType value.
     * 
     */
    static class MemberFilter implements InclusiveFilter<AnnotatedElement> {
        final Class<?> target;

        public MemberFilter(Class<?> target) {
            this.target = target;
        }

        public boolean includes(AnnotatedElement obj) {
        	int mods = ((Member)obj).getModifiers();
        	
            return obj.getClass() == target && 
                 !(Modifier.isStatic(mods) || Modifier.isFinal(mods) 
                || Modifier.isTransient(mods) || Modifier.isNative(mods));
                  
        }
    }

    /**
     * Selects non-transient elements.  Selectively will examine only the 
     * transient field modifier.
     */
    static class TransientFilter implements InclusiveFilter<AnnotatedElement> {
        final boolean modifierOnly;
        
        public TransientFilter(boolean modOnly) {
            modifierOnly = modOnly;
        }
        
        public boolean includes(AnnotatedElement obj) {
            if (modifierOnly) {
                return !Modifier.isTransient(((Member)obj).getModifiers());
            }
        	return !obj.isAnnotationPresent(Transient.class) && 
        	       !Modifier.isTransient(((Member)obj).getModifiers());
        }
    }
    
    /**
     * Selects all element annotated with <code>javax.persistence.*</code> or 
     * <code>org.apache.openjpa.*</code> annotation except the annotations
     * marked to be ignored.
     */
    static class AnnotatedFilter implements InclusiveFilter<AnnotatedElement> {
        public boolean includes(AnnotatedElement obj) {
            Annotation[] annos = AccessController.doPrivileged(J2DoPrivHelper
                    .getAnnotationsAction(obj));
        	for (Annotation anno : annos) {
        		String name = anno.annotationType().getName();
                if ((name.startsWith("javax.persistence.")
                  || name.startsWith("org.apache.openjpa.persistence."))
                  && !_ignoredAnnos.contains(name))
                	return true;
        	}
        	return false;
        }
    }
    
    private void logNoSetter(ClassMetaData meta, String name, Exception e) {
        Log log = meta.getRepository().getConfiguration()
            .getLog(OpenJPAConfiguration.LOG_METADATA);
        if (log.isWarnEnabled())
            log.warn(_loc.get("no-setter-for-getter", name,
                meta.getDescribedType().getName()));
        else if (log.isTraceEnabled())
            // log the exception, if any, if we're in trace-level debugging
            log.warn(_loc.get("no-setter-for-getter", name,
                meta.getDescribedType().getName()), e);
    }
    
    private Log getLog(ClassMetaData meta) {
        return meta.getRepository().getConfiguration()
            .getLog(OpenJPAConfiguration.LOG_METADATA);
    }
    
    String toFieldNames(List<Field> fields) {
    	return fields.toString();
    }
    
    String toMethodNames(List<Method> methods) {
    	return methods.toString();
    }
    
    public boolean isAbstractMappingUniDirectional(OpenJPAConfiguration conf) {
        if (_isAbstractMappingUniDirectional == null)
            setAbstractMappingUniDirectional(conf);
        return _isAbstractMappingUniDirectional;
    }
    
    public void setAbstractMappingUniDirectional(OpenJPAConfiguration conf) {
        _isAbstractMappingUniDirectional = conf.getCompatibilityInstance().isAbstractMappingUniDirectional();
    }
    
    public boolean isNonDefaultMappingAllowed(OpenJPAConfiguration conf) {
        if (_isNonDefaultMappingAllowed == null)
            setNonDefaultMappingAllowed(conf);
        return _isNonDefaultMappingAllowed;
    }

    public void setNonDefaultMappingAllowed(OpenJPAConfiguration conf) {
        _isNonDefaultMappingAllowed = conf.getCompatibilityInstance().
            isNonDefaultMappingAllowed();
    }
}
