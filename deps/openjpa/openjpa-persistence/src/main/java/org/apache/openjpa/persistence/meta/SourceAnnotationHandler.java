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
package org.apache.openjpa.persistence.meta;

import static javax.persistence.AccessType.FIELD;
import static javax.persistence.AccessType.PROPERTY;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.util.UserException;

/**
 * Extracts persistent metadata information by analyzing available annotation
 * in *.java source files. Requires JDK6 Annotation Processing environment
 * available.
 *   
 * @author Pinaki Poddar
 * @since 2.0.0
 */
public class SourceAnnotationHandler 
    implements MetadataProcessor<TypeElement, Element> {
	
	private final ProcessingEnvironment processingEnv;
	private final Types typeUtility;
	private final CompileTimeLogger logger;
	/**
     * Set of Inclusion Filters based on member type, access type or transient
     * annotations. Used to determine the subset of available field/method that 
     * are persistent.   
     */
    protected AccessFilter propertyAccessFilter = new AccessFilter(PROPERTY);
    protected AccessFilter fieldAccessFilter = new AccessFilter(FIELD);

    protected KindFilter fieldFilter  = new KindFilter(ElementKind.FIELD);
    protected KindFilter methodFilter = new KindFilter(ElementKind.METHOD);
    protected TransientFilter nonTransientFilter = new TransientFilter();
    protected AnnotatedFilter annotatedFilter = new AnnotatedFilter();
    protected GetterFilter getterFilter = new GetterFilter();
    protected SetterFilter setterFilter = new SetterFilter();
    
    protected static List<Class<? extends Annotation>> mappingAnnos = new ArrayList<Class<? extends Annotation>>();
    static {
        mappingAnnos.add(OneToOne.class);
        mappingAnnos.add(OneToMany.class);
        mappingAnnos.add(ManyToOne.class);
        mappingAnnos.add(ManyToMany.class);
    }
    private static Localizer _loc = Localizer.forPackage(SourceAnnotationHandler.class);
    
	/**
	 * Construct with JDK6 annotation processing environment.
	 * 
	 */
    public SourceAnnotationHandler(ProcessingEnvironment processingEnv, 
        CompileTimeLogger logger) {
		super();
		this.processingEnv = processingEnv;
		this.typeUtility   = processingEnv.getTypeUtils();
		this.logger = logger;
	}

	public int determineTypeAccess(TypeElement type) {
        AccessType access = getExplicitAccessType(type);
        boolean isExplicit = access != null;
        return isExplicit ? access == AccessType.FIELD 
                ? AccessCode.EXPLICIT | AccessCode.FIELD
                : AccessCode.EXPLICIT | AccessCode.PROPERTY
                : getImplicitAccessType(type);
	}
	
	public int determineMemberAccess(Element m) {
		return 0;
	}

	public List<Exception> validateAccess(TypeElement t) {
		return null;
	}
	
	public boolean isMixedAccess(TypeElement t) {
		return false;
	}
    /**
     * Gets the list of persistent fields and/or methods for the given type.
     * 
     * Scans relevant @AccessType annotation and field/method as per JPA
     * specification to determine the candidate set of field/methods.
     */
	
    public Set<Element> getPersistentMembers(TypeElement type) {
        int access = determineTypeAccess(type);
        if (AccessCode.isExplicit(access)) {
            return AccessCode.isField(access) 
                ? getFieldAccessPersistentMembers(type) 
        		: getPropertyAccessPersistentMembers(type);
        }
        return getDefaultAccessPersistentMembers(type, access);
    }
    
    /**
     * Collect members for the given type which uses explicit field access.
     */
    private Set<Element> getFieldAccessPersistentMembers(TypeElement type) {   
        List<? extends Element> allMembers = type.getEnclosedElements();       
        Set<VariableElement> allFields = (Set<VariableElement>) 
           filter(allMembers, fieldFilter, nonTransientFilter);
        Set<ExecutableElement> allMethods = (Set<ExecutableElement>) 
            filter(allMembers, methodFilter, nonTransientFilter);
        Set<ExecutableElement> getters = filter(allMethods, getterFilter, 
        		propertyAccessFilter, annotatedFilter);
        Set<ExecutableElement> setters = filter(allMethods, setterFilter);
        getters = matchGetterAndSetter(getters, setters);
        
        return merge(getters, allFields);
    }
    
    /**
     * Collect members for the given type which uses explicit field access.
     */
     private Set<Element> getPropertyAccessPersistentMembers(TypeElement type)
     {
        List<? extends Element> allMembers = type.getEnclosedElements();
        Set<ExecutableElement> allMethods = (Set<ExecutableElement>) 
            filter(allMembers, methodFilter, nonTransientFilter);

        Set<ExecutableElement> getters = filter(allMethods, getterFilter);
        Set<ExecutableElement> setters = filter(allMethods, setterFilter);
        getters = matchGetterAndSetter(getters, setters);
        
        return merge(filter(allMembers, fieldFilter, nonTransientFilter, 
        	fieldAccessFilter), getters);
    }
    
    private Set<Element> getDefaultAccessPersistentMembers(TypeElement type,
        int access) {
        Set<Element> result = new HashSet<Element>();
        List<? extends Element> allMembers = type.getEnclosedElements();
        if (AccessCode.isField(access)) {
            Set<VariableElement> allFields = (Set<VariableElement>) 
                filter(allMembers, fieldFilter, nonTransientFilter);
            result.addAll(allFields);
        } else {
            Set<ExecutableElement> allMethods = (Set<ExecutableElement>) 
               filter(allMembers, methodFilter, nonTransientFilter);
            Set<ExecutableElement> getters = filter(allMethods, getterFilter); 
            Set<ExecutableElement> setters = filter(allMethods, setterFilter);
            getters = matchGetterAndSetter(getters, setters);
            result.addAll(getters);
        }
        return result;
    }
    
    private int getImplicitAccessType(TypeElement type) {
        List<? extends Element> allMembers = type.getEnclosedElements();
        Set<VariableElement> allFields = (Set<VariableElement>) filter(allMembers, fieldFilter, nonTransientFilter);
        Set<ExecutableElement> allMethods = (Set<ExecutableElement>) filter(allMembers, methodFilter, 
                nonTransientFilter);

        Set<VariableElement> annotatedFields = filter(allFields, annotatedFilter);
        Set<ExecutableElement> getters = filter(allMethods, getterFilter, annotatedFilter);
        Set<ExecutableElement> setters = filter(allMethods, setterFilter);
        getters = matchGetterAndSetter(getters, setters);
        
        boolean isFieldAccess = !annotatedFields.isEmpty();
        boolean isPropertyAccess = !getters.isEmpty();

        if (isFieldAccess && isPropertyAccess) {
            throw new UserException(_loc.get("access-mixed", type,
                    toString(annotatedFields), toString(getters)));
        }    
        if (isFieldAccess) {
            return AccessCode.FIELD;
        } else if (isPropertyAccess) {
            return AccessCode.PROPERTY;
        } else {
            TypeElement superType = getPersistentSupertype(type);
            return (superType == null)
                ? AccessCode.FIELD : determineTypeAccess(superType);
        }
    }
    
    Set<Element> merge(Set<? extends Element> a, Set<? extends Element> b) {
    	Set<Element> result = new HashSet<Element>();
    	result.addAll(a);
    	for (Element e1 : b) {
    		boolean hide = false;
    		String key = getPersistentMemberName(e1);
    		for (Element e2 : a) {
    			if (getPersistentMemberName(e2).equals(key)) {
    				hide = true;
    				break;
    			}
    		}
    		if (!hide) {
    			result.add(e1);
    		}
    	}
    	return result;
    }

    /**
     * Matches the given getters with the given setters. Removes the getters
     * that do not have a corresponding setter.
     */
    private Set<ExecutableElement> matchGetterAndSetter(
        Set<ExecutableElement> getters,  Set<ExecutableElement> setters) {
        Collection<ExecutableElement> unmatched =  new ArrayList<ExecutableElement>();
        
        for (ExecutableElement getter : getters) {
            String getterName = getter.getSimpleName().toString();
            TypeMirror getterReturnType = getter.getReturnType();
            String expectedSetterName = "set" + getterName.substring(
                (isBooleanGetter(getter) ? "is" : "get").length());
            boolean matched = false;
            for (ExecutableElement setter : setters) {
                TypeMirror setterArgType = setter.getParameters()
                                     .iterator().next().asType();
                String actualSetterName = setter.getSimpleName().toString();
                matched = actualSetterName.equals(expectedSetterName)
                       && typeUtility.isSameType(setterArgType, getterReturnType);
                if (matched)
                    break;
            }
            if (!matched) {
                logger.warn(_loc.get("getter-unmatched", getter, getter.getEnclosingElement()));
                unmatched.add(getter);
            }

        }
        getters.removeAll(unmatched);
        return getters;
    }

    // ========================================================================
    //  Selection Filters select specific elements from a collection.
    // ========================================================================
    
    /**
     * Inclusive element filtering predicate.
     *
     */
    private static interface InclusiveFilter<T extends Element> {
        /**
         * Return true to include the given element.
         */
        boolean includes(T e);
    }

    /**
     * Filter the given collection with the conjunction of filters. The given
     * collection itself is not modified.
     */
    <T extends Element> Set<T> filter(Collection<T> coll, 
        InclusiveFilter... filters) {
        Set<T> result = new HashSet<T>();
        for (T e : coll) {
            boolean include = true;
            for (InclusiveFilter f : filters) {
                if (!f.includes(e)) {
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
    static class GetterFilter implements InclusiveFilter<ExecutableElement> {
        public boolean includes(ExecutableElement method) {
            return isGetter(method);
        }
    }

    /**
     * Selects setter method. A setter method name starts with 'set', returns a
     * void and has single argument.
     * 
     */
    static class SetterFilter implements InclusiveFilter<ExecutableElement> {
        public boolean includes(ExecutableElement method) {
            return isSetter(method);
        }
    }

    /**
     * Selects elements which is annotated with @Access annotation and that 
     * annotation has the given AccessType value.
     * 
     */
    static class AccessFilter implements InclusiveFilter<Element> {
        final AccessType target;

        public AccessFilter(AccessType target) {
            this.target = target;
        }

        public boolean includes(Element obj) {
            Object value = getAnnotationValue(obj, Access.class);
            return equalsByValue(target, value);
        }
    }

    /**
     * Selects elements of given kind.
     * 
     */
    static class KindFilter implements InclusiveFilter<Element> {
        final ElementKind target;

        public KindFilter(ElementKind target) {
            this.target = target;
        }

        public boolean includes(Element obj) {
            return obj.getKind() == target;
        }
    }

    /**
     * Selects all non-transient element.
     */
    static class TransientFilter implements InclusiveFilter<Element> {
        public boolean includes(Element obj) {
            Set<Modifier> modifiers = obj.getModifiers();
            boolean isTransient = isAnnotatedWith(obj, Transient.class)
                            || modifiers.contains(Modifier.TRANSIENT);
           return !isTransient && !modifiers.contains(Modifier.STATIC);
        }
    }
    
    /**
     * Selects all annotated element.
     */
    static class AnnotatedFilter implements InclusiveFilter<Element> {
        public boolean includes(Element obj) {
            return isAnnotated(obj);
        }
    }

    /**
     * Get  access type of the given class, if specified explicitly. 
     * null otherwise.
     * 
     * @param type
     * @return FIELD or PROPERTY 
     */
    AccessType getExplicitAccessType(TypeElement type) {
        Object access = getAnnotationValue(type, Access.class);
        if (equalsByValue(AccessType.FIELD, access))
            return AccessType.FIELD;
        if (equalsByValue(AccessType.PROPERTY, access))
            return AccessType.PROPERTY;
        return null;
    }
    
    /**
     * Gets the value of the given annotation, if present, in the given
     * declaration. Otherwise, null.
     */
    public static Object getAnnotationValue(Element decl,
        Class<? extends Annotation> anno) {
        return getAnnotationValue(decl, anno, "value");
    }

    /**
     * Gets the value of the given attribute of the given annotation, if
     * present, in the given declaration. Otherwise, null.
     */
    public static Object getAnnotationValue(Element e,
        Class<? extends Annotation> anno, String attr) {
        if (e == null || e.getAnnotation(anno) == null)
            return null;
        List<? extends AnnotationMirror> annos = e.getAnnotationMirrors();
        for (AnnotationMirror mirror : annos) {
            if (mirror.getAnnotationType().toString().equals(anno.getName())) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(attr)) {
                        return entry.getValue().getValue();
                    }
                }
            }
        }
        return null;
    }

    public static String toString(Collection<? extends Element> elements) {
        StringBuilder tmp = new StringBuilder();
        int i = 0;
        for (Element e : elements) {
            tmp.append(e.getSimpleName() + (++i == elements.size() ? "" : ","));
        }
        return tmp.toString();
    }
    
    String toDetails(Element e) {
        TypeMirror mirror = e.asType();
        return new StringBuilder(e.getKind().toString()).append(" ")
                           .append(e.toString())
                           .append("Mirror ")
                           .append(mirror.getKind().toString())
                           .append(mirror.toString()).toString();
    }

    String getPersistentMemberName(Element e) {
    	return isMethod(e) ? extractFieldName((ExecutableElement)e) 
    			: e.getSimpleName().toString();
    }
    
    public String extractFieldName(ExecutableElement method) {
    	String name = method.getSimpleName().toString();
		String head = isNormalGetter(method) ? "get" : "is";
		name = name.substring(head.length());
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    

    // =========================================================================
    // Annotation processing utilities
    // =========================================================================
    
    /**
     * Affirms if the given element is annotated with <em>any</em> 
     * <code>javax.persistence.*</code> or <code>org.apache.openjpa.*</code>
     * annotation.
     */
    public static boolean isAnnotated(Element e) {
    	return isAnnotatedWith(e, (Set<String>)null);
    }
    
    /**
     * Affirms if the given declaration has the given annotation.
     */
    boolean isAnnotatedAsEntity(Element e) {
        return isAnnotatedWith(e, Entity.class)
            || isAnnotatedWith(e, Embeddable.class)
            || isAnnotatedWith(e, MappedSuperclass.class);
    }

    /**
     * Affirms if the given declaration has the given annotation.
     */
    public static boolean isAnnotatedWith(Element e,
        Class<? extends Annotation> anno) {
        return e != null && e.getAnnotation(anno) != null;
    }
    
    /**
     * Affirms if the given element is annotated with any of the given 
     * annotations.
     * 
     * @param annos null checks for any annotation that starts with 
     *            'javax.persistence.' or 'openjpa.*'.
     * 
     */
    public static boolean isAnnotatedWith(Element e, Set<String> annos) {
        if (e == null)
            return false;
        List<? extends AnnotationMirror> mirrors = e.getAnnotationMirrors();
        if (annos == null) {
            for (AnnotationMirror mirror : mirrors) {
                String name = mirror.getAnnotationType().toString();
                if (startsWith(name, "javax.persistence.")
                 || startsWith(name, "org.apache.openjpa."))
                    return true;
            }
            return false;
        } else {
            for (AnnotationMirror mirror : mirrors) {
                String name = mirror.getAnnotationType().toString();
                if (annos.contains(name))
                    return true;
            }
            return false;
        }
    }
    
    TypeMirror getTargetEntityType(Element e) {
        for (Class<? extends Annotation> anno : mappingAnnos) {
            Object target = getAnnotationValue(e, anno, "targetEntity");
            if (target != null) {
                return (TypeMirror)target;
            }
            
        };
        return null;
    }
    
    String getDeclaredTypeName(TypeMirror mirror) {
    	return getDeclaredTypeName(mirror, true);
    }
    
    String getDeclaredTypeName(TypeMirror mirror, boolean box) {
        return getDeclaredTypeName(mirror, box, false);
    }
    
     /**
     * Get the element name of the class the given mirror represents. If the
     * mirror is primitive then returns the corresponding boxed class name.
     * If the mirror is parameterized returns only the generic type i.e.
     * if the given declared type is 
     * <code>java.util.Set&lt;java.lang.String&gt;</code> this method will 
     * return <code>java.util.Set</code>.
     */
    String getDeclaredTypeName(TypeMirror mirror, boolean box, boolean persistentCollection) {
        if (mirror == null || mirror.getKind() == TypeKind.NULL || mirror.getKind() == TypeKind.WILDCARD)
            return "java.lang.Object";
    	if (mirror.getKind() == TypeKind.ARRAY) {
    	    if(persistentCollection) { 
    	        TypeMirror comp = ((ArrayType)mirror).getComponentType();
    	        return getDeclaredTypeName(comp, false);
    	    }
    	    else { 
    	        return mirror.toString();
    	    }
    	}
    	mirror = box ? box(mirror) : mirror;
    	if (isPrimitive(mirror))
    		return ((PrimitiveType)mirror).toString();
    	Element elem = typeUtility.asElement(mirror);
    	if (elem == null)
    	    throw new RuntimeException(_loc.get("mmg-no-type", mirror).getMessage());
        return elem.toString();
    }

    /**
     * Gets the declared type of the given member. For fields, returns the 
     * declared type while for method returns the return type. 
     * 
     * @param e a field or method.
     * @exception if given member is neither a field nor a method.
     */
    TypeMirror getDeclaredType(Element e) {
        TypeMirror result = null;
        switch (e.getKind()) {
        case FIELD:
            result = e.asType();
            break;
        case METHOD:
            result = ((ExecutableElement) e).getReturnType();
            break;
        default:
            throw new IllegalArgumentException(toDetails(e));
        }
        return result;
    }
    
    /**
     * Affirms if the given type mirrors a primitive.
     */
    private boolean isPrimitive(TypeMirror mirror) {
        TypeKind kind = mirror.getKind();
        return kind == TypeKind.BOOLEAN 
            || kind == TypeKind.BYTE
            || kind == TypeKind.CHAR
            || kind == TypeKind.DOUBLE
            || kind == TypeKind.FLOAT
            || kind == TypeKind.INT
            || kind == TypeKind.LONG
            || kind == TypeKind.SHORT;
    }
    
    public TypeMirror box(TypeMirror t) {
        if (isPrimitive(t))
            return processingEnv.getTypeUtils()
            .boxedClass((PrimitiveType)t).asType();
        return t;
    }

    /**
     * Gets the parameter type argument at the given index of the given type.
     * 
     * @return if the given type represents a parameterized type, then the
     *         indexed parameter type argument. Otherwise null.
     */
    TypeMirror getTypeParameter(Element e, TypeMirror mirror, int index, boolean checkTarget) {
        if (mirror.getKind() == TypeKind.ARRAY)
            return ((ArrayType)mirror).getComponentType();
    	if (mirror.getKind() != TypeKind.DECLARED)
    		return null;
    	if (checkTarget) {
    	    TypeMirror target = getTargetEntityType(e);
    	    if (target != null)
    	        return target;
    	}
        List<? extends TypeMirror> params = ((DeclaredType)mirror).getTypeArguments();
        TypeMirror param = (params == null || params.size() < index+1) 
            ? typeUtility.getNullType() : params.get(index);
        if (param.getKind() == TypeKind.NULL || param.getKind() == TypeKind.WILDCARD) {
            logger.warn(_loc.get("generic-type-param", e, getDeclaredType(e), e.getEnclosingElement()));
        }
        return param;
    }

    public TypeElement getPersistentSupertype(TypeElement cls) {
    	if (cls == null) return null;
        TypeMirror sup = cls.getSuperclass();
        if (sup == null || sup.getKind() == TypeKind.NONE ||  isRootObject(sup))
            return null;
        TypeElement supe = (TypeElement) processingEnv.getTypeUtils().asElement(sup);
        if (isAnnotatedAsEntity(supe)) 
            return supe;
        return getPersistentSupertype(supe);
    }


    // ========================================================================
    //  Utilities
    // ========================================================================

    /**
     * Affirms if the given mirror represents a primitive or non-primitive
     * boolean.
     */
    public static boolean isBoolean(TypeMirror type) {
        return (type != null && (type.getKind() == TypeKind.BOOLEAN 
            || "java.lang.Boolean".equals(type.toString())));
    }

    /**
     * Affirms if the given mirror represents a void.
     */
    public static boolean isVoid(TypeMirror type) {
        return (type != null && type.getKind() == TypeKind.VOID);
    }

    /**
     * Affirms if the given element represents a method.
     */
    public static boolean isMethod(Element e) {
        return e != null && ExecutableElement.class.isInstance(e)
            && e.getKind() == ElementKind.METHOD;
    }
    
    /**
     * Affirms if the given method matches the following signature
     * <code> public T getXXX() </code>
     * where T is any non-void type.
     */
    public static boolean isNormalGetter(ExecutableElement method) {
    	String methodName = method.getSimpleName().toString();
    	return method.getKind() == ElementKind.METHOD
    	    && startsWith(methodName, "get") 
    	    && method.getParameters().isEmpty()
    	    && !isVoid(method.getReturnType());
    }
    
    /**
     * Affirms if the given method matches the following signature
     * <code> public boolean isXyz() </code>
     * <code> public Boolean isXyz() </code>
     */
    public static boolean isBooleanGetter(ExecutableElement method) {
    	String methodName = method.getSimpleName().toString();
    	return method.getKind() == ElementKind.METHOD
    	    && startsWith(methodName, "is") 
    	    && method.getParameters().isEmpty()
    	    && isBoolean(method.getReturnType());
    }

    public static boolean isGetter(ExecutableElement method) {
    	return isNormalGetter(method) || isBooleanGetter(method);
    }
    
    /**
     * Affirms if the given method matches the following signature
     * <code> public void setXXX(T t) </code>
     */
    public static boolean isSetter(ExecutableElement method) {
    	String methodName = method.getSimpleName().toString();
    	return method.getKind() == ElementKind.METHOD
    	    && startsWith(methodName, "set") 
    	    && method.getParameters().size() == 1
    	    && isVoid(method.getReturnType());
    }
    
    /**
     * Affirms if the given mirror represents root java.lang.Object.
     */
    public static boolean isRootObject(TypeMirror type) {
        return type != null && "java.lang.Object".equals(type.toString());
    }
    
    /**
     * Affirms if the given full string starts with the given head.
     */
    public static boolean startsWith(String full, String head) {
        return full != null && head != null && full.startsWith(head) 
            && full.length() > head.length();
    }

    /**
     * Affirms if the given enum equals the given value.
     */
    public static boolean equalsByValue(Enum<?> e, Object v) {
        return e == v 
             || (v != null && e != null && e.toString().equals(v.toString()));
    }
}
