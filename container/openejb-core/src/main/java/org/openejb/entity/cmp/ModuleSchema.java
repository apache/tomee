/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.entity.cmp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ModuleSchema implements Serializable {
    private static final long serialVersionUID = 6744154273253141071L;
    private final String name;
    private boolean enforceForeignKeyConstraints;
    private String ejbQlCompilerFactory;
    private String dbSyntaxFactory;
    private final Map entities = new TreeMap();
    private final Set relations = new LinkedHashSet();

    public ModuleSchema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnforceForeignKeyConstraints() {
        return enforceForeignKeyConstraints;
    }

    public void setEnforceForeignKeyConstraints(boolean enforceForeignKeyConstraints) {
        this.enforceForeignKeyConstraints = enforceForeignKeyConstraints;
    }

    public String getEjbQlCompilerFactory() {
        return ejbQlCompilerFactory;
    }

    public void setEjbQlCompilerFactory(String ejbQlCompilerFactory) {
        this.ejbQlCompilerFactory = ejbQlCompilerFactory;
    }

    public String getDbSyntaxFactory() {
        return dbSyntaxFactory;
    }

    public void setDbSyntaxFactory(String dbSyntaxFactory) {
        this.dbSyntaxFactory = dbSyntaxFactory;
    }

    public EntitySchema addEntity(String ejbName) {
        if (entities.containsKey(ejbName)) {
            throw new IllegalStateException("Entity " + ejbName + " already exists");
        }
        EntitySchema entitySchema = new EntitySchema(ejbName);
        entities.put(ejbName, entitySchema);
        return entitySchema;
    }

    public void addEntity(EntitySchema entitySchema) {
        if (entities.containsKey(entitySchema.getEjbName())) {
            throw new IllegalStateException("Entity " + entitySchema.getEjbName() + " already exists");
        }
        entities.put(entitySchema.getEjbName(), entitySchema);
    }

    public EntitySchema getEntity(String ejbName) {
        return (EntitySchema) entities.get(ejbName);
    }

    public Map getEntities() {
        return Collections.unmodifiableMap(entities);
    }

    public void setEntities(Collection entities) {
        this.entities.clear();
        for (Iterator iterator = entities.iterator(); iterator.hasNext();) {
            EntitySchema entitySchema = (EntitySchema) iterator.next();
            addEntity(entitySchema);
        }
    }


    public void setEntities(Map entities) {
        this.entities.clear();
        for (Iterator iterator = entities.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String ejbName = (String) entry.getKey();
            EntitySchema entitySchema = (EntitySchema) entry.getValue();
            this.entities.put(ejbName, entitySchema);
        }
    }

    public void addRelation(RelationSchema relationSchema) {
        // todo consider adding cmr fields to the entity schema object
        this.relations.add(relationSchema);
    }

    public Set getRelations() {
        return Collections.unmodifiableSet(relations);
    }

    public void setRelations(Set relations) {
        this.relations.clear();
        for (Iterator iterator = relations.iterator(); iterator.hasNext();) {
            RelationSchema relationSchema = (RelationSchema) iterator.next();
            addRelation(relationSchema);
        }
    }

}