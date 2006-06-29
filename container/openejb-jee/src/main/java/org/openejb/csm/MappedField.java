/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.csm;

import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * @version $Revision$ $Date$
 */
public class MappedField implements MappedItem {

    private SchemaParticle particle;
    private final Field field;

    public MappedField(SchemaParticle particle, Field field) {
        this.particle = particle;
        this.field = field;
    }


    public SchemaType getSchemaType() {
        return getType();
    }

    public String getXmlName() {
        return particle.getName().getLocalPart();
    }

    public Class getClassType() {
        return field.getType();
    }

    public String getJavaName() {
        return field.getName();
    }

    public SchemaParticle getParticle() {
        return particle;
    }

    public Field getField() {
        return field;
    }

    public SchemaType getType() {
        return particle.getType();
    }

    public boolean isCollection() {
        return isJavaCollection() && isXmlCollection();
    }

    public boolean isXmlCollection() {
        int min = particle.getIntMinOccurs();
        int max = particle.getIntMaxOccurs();

        if ((min == 0 || min == 1) && max == 1){
            SchemaParticle model = particle.getType().getContentModel();
            if (model != null && model.getParticleType() == SchemaParticle.CHOICE && model.getIntMaxOccurs() > 1){
                return true;
            } else if (model != null && model.getParticleType() == SchemaParticle.ELEMENT && model.getIntMaxOccurs() > 1){
                return true;
            }
        } else if (max > 1) {
            return true;
        }

        return false;
    }

    public boolean isJavaCollection() {
        return Collection.class.isAssignableFrom(field.getType());
    }

    public boolean isImplicitCollection() {
        return isJavaCollection() && particle.getIntMaxOccurs() > 1;
    }

    public boolean isExplicitCollection() {
        int min = particle.getIntMinOccurs();
        int max = particle.getIntMaxOccurs();

        if (!isJavaCollection()){
            return false;
        }
        if ((min == 0 || min == 1) && max == 1){
            SchemaParticle model = particle.getType().getContentModel();
            if (model != null && model.getParticleType() == SchemaParticle.CHOICE && model.getIntMaxOccurs() > 1){
                return true;
            } else if (model != null && model.getParticleType() == SchemaParticle.ELEMENT && model.getIntMaxOccurs() > 1){
                return true;
            }
        }

        return false;
    }

    public SchemaType getCollectionItemSchemaType() {
        return null;
    }

    public Class getCollectionItemJavaType() {
        return null;
    }

    
    public Class getGenericType() {
        Class type = field.getType();
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type firstParamType = parameterizedType.getActualTypeArguments()[0];
            return (Class) firstParamType;
        } else if (genericType instanceof Class) {
            return (Class) genericType;
        } else {
            return type;
        }
    }

    public String getCollectionItemXmlName() {
        if (isExplicitCollection()){
            SchemaParticle model = particle.getType().getContentModel();
            if (model.getParticleType() == SchemaParticle.CHOICE){
                return null;
            } else {
                return model.getName().getLocalPart();
            }
        }
        return particle.getName().getLocalPart();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MappedField(");
        sb.append(field.getType().getSimpleName());
        sb.append(" ");
        sb.append(field.getDeclaringClass().getSimpleName());
        sb.append(".");
        sb.append(field.getName());
        sb.append(", ");
        sb.append(particle.getName().getLocalPart());
        sb.append("[");
        sb.append(particle.getIntMinOccurs());
        sb.append(":");
        int max = particle.getIntMaxOccurs();
        if (max > 2100000000) {
            sb.append("*");
        } else {
            sb.append(max);
        }
        sb.append("])");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MappedField that = (MappedField) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;

        return true;
    }

    public int hashCode() {
        return (field != null ? field.hashCode() : 0);
    }
}
