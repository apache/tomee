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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;

/**
 * @version $Revision$ $Date$
 */
public class MappedClass implements MappedItem {
    private final Class clazz;
    private final SchemaType type;
    private final String elementName;
    private final SchemaParticle particle;

    public MappedClass(Class clazz, SchemaParticle particle) {
        this.clazz = clazz;
        this.particle = particle;
        this.type = particle.getType();
        this.elementName = particle.getName().getLocalPart();
    }

    public SchemaParticle getParticle() {
        return particle;
    }

    public Class getClazz() {
        return clazz;
    }

    public SchemaType getType() {
        return type;
    }

    public String getElementName() {
        return elementName;
    }

    public String getXmlName() {
        return getElementName();
    }

    public Class getClassType() {
        return getClazz();
    }

    public Class getGenericType() {
        return null;
    }

    public String getJavaName() {
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MappedClass that = (MappedClass) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    public int hashCode() {
        return (clazz != null ? clazz.hashCode() : 0);
    }
}

