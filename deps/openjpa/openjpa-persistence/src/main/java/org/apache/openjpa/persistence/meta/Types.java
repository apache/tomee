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

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Type;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Persistent Type according to JPA 2.0.
 * <br>
 * JPA 2.0 defines a type system for persistent objects to decorate a core Java type system
 * with persistence-relevant properties such as persistent identity, independently persistence
 * capable etc. 
 * <br>
 * Implemented as a thin adapter to OpenJPA metadata system. Mostly immutable.
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 * 
 */
public class Types {
    protected static Localizer _loc = Localizer.forPackage(Types.class);

    /**
     * Mirrors a Java class.
     *
     * @param <X> Java class 
     */
    static abstract class BaseType<X> implements Type<X> {
        public final Class<X> cls;

        protected BaseType(Class<X> cls) {
            this.cls = cls;
        }

        public final Class<X> getJavaType() {
            return cls;
        }

        public String toString() {
            return cls.getName();
        }
    }

    /**
     * Basic non-relational types of a persistent attribute such as <code>long</code> or 
     * <code>java.util.Date</code>.
     * 
     * @author Pinaki Poddar
     *
     * @param <X> represented Java type.
     */
    public static class Basic<X> extends BaseType<X> implements Type<X> {
        public Basic(Class<X> cls) {
            super(cls);
        }

        public PersistenceType getPersistenceType() {
            return PersistenceType.BASIC;
        }
    }

    /**
     * Represents an abstract persistent type that has a persistent identity.
     * 
     * @author Pinaki Poddar
     *
     * @param <X>
     */
    public static abstract class Identifiable<X> extends AbstractManagedType<X> 
        implements IdentifiableType<X> {

        public Identifiable(ClassMetaData meta, MetamodelImpl model) {
            super(meta, model);
        }

        /**
         *  Whether or not the identifiable type has a version attribute.
         *  @return boolean indicating whether or not the identifiable
         *          type has a version attribute
         */
        public boolean hasVersionAttribute() {
            return meta.getVersionField() != null;
        }


        /**
         *  Return the identifiable type that corresponds to the most
         *  specific mapped superclass or entity extended by the entity 
         *  or mapped superclass. 
         *  @return super type of identifiable type or null if no such super type
         */
        public IdentifiableType<? super X> getSupertype() {
            ClassMetaData superMeta = meta.getPCSuperclassMetaData();
            if (superMeta == null)
                return null;
            return (IdentifiableType<? super X>) model.managedType(superMeta.getDescribedType());
        }

        public boolean hasIdAttribute() {
            return meta.getIdentityType() == ClassMetaData.ID_APPLICATION;
        }
        
        /**
         *  Whether or not the identifiable type uses an attribute to represents its persistent identity.
         *  Returns true for a simple or embedded identifier.
         *  Returns false for an classes that use separate identifier class for its persistent identity.
         *  
         *  @return boolean indicating whether or not the identifiable
         *          type represents its persistent identity via a single identifier attribute.
         */
        public boolean hasSingleIdAttribute() {
            return meta.getPrimaryKeyFields().length == 1;
        }

        /**
         *  Return the type that represents the type of the id.
         *  @return type of identifier
         */
        public Type<?> getIdType() {
            Class<?> idType = hasSingleIdAttribute() 
                     ? meta.getPrimaryKeyFields()[0].getDeclaredType() : meta.getObjectIdType();
            return model.getType(idType);
        }
    }

    /**
     * An embedded, not independently identifiable type.
     * 
     * @author Pinaki Poddar
     *
     * @param <X> the represented Java type. 
     */
    public static class Embeddable<X> extends AbstractManagedType<X> 
        implements EmbeddableType<X> {
        public Embeddable(ClassMetaData meta, MetamodelImpl model) {
            super(meta, model);
        }
        
        public PersistenceType getPersistenceType() {
            return PersistenceType.EMBEDDABLE;
        }
    }

    /**
     * A abstract, independently identifiable persistent type.
     *  
     * @author Pinaki Poddar
     *
     * @param <X> the represented Java type. 
     */
    public static class MappedSuper<X> extends Identifiable<X> implements
        MappedSuperclassType<X> {

        public MappedSuper(ClassMetaData meta, MetamodelImpl model) {
            super(meta, model);
        }
        
        public PersistenceType getPersistenceType() {
            return PersistenceType.MAPPED_SUPERCLASS;
        }

    }
    
    /**
     * An entity type that is independently identifiable.
     * 
     * @author Pinaki Poddar
     *
     * @param <X> the represented Java type. 
     */
    public static class Entity<X> extends Identifiable<X> 
        implements EntityType<X> {

        public Entity(ClassMetaData meta, MetamodelImpl model) {
            super(meta, model);
        }
        
        public PersistenceType getPersistenceType() {
            return PersistenceType.ENTITY;
        }
        
        public String getName() {
        	return meta.getTypeAlias();
        }
        /**
         *  Return the bindable type of the represented object.
         *  @return bindable type
         */ 
        public BindableType getBindableType() {
            return BindableType.ENTITY_TYPE;
        }
        
        /**
         * Return the Java type of the represented object.
         * If the bindable type of the object is PLURAL_ATTRIBUTE,
         * the Java element type is returned. If the bindable type is
         * SINGULAR_ATTRIBUTE or ENTITY_TYPE, the Java type of the
         * represented entity or attribute is returned.
         * @return Java type
         */
        public Class<X> getBindableJavaType() {
            return getJavaType();
        }
    }   
    
    /**
     * A pseudo managed type used to represent keys of a java.util.Map as a 
     * pseudo attribute.
    **/ 
    public static class PseudoEntity<X> extends AbstractManagedType<X> {

        protected PseudoEntity(Class<X> cls, MetamodelImpl model) {
            super(cls, model);
        }

        public javax.persistence.metamodel.Type.PersistenceType getPersistenceType() {
            return PersistenceType.ENTITY;
        }       
    }
}
