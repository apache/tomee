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
package org.openejb.jee.ejbjar;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Entity extends EnterpriseBean {
    private PersistenceType persistenceType;
    private String primaryKeyClass;
    private boolean reenterant;
    private String cmpVersion;
    private String abstractSchemaName;
    private List<CmpField> cmpFields = new ArrayList<CmpField>();
    private String primkeyField;
    private Query query;

    public Entity() {
    }

    public Entity(String ejbName, String ejbClass) {
        super(ejbName, ejbClass);
    }

    public Entity(String ejbName, String ejbClass, PersistenceType persistenceType, String cmpVersion) {
        super(ejbName, ejbClass);
        this.persistenceType = persistenceType;
        this.cmpVersion = cmpVersion;
    }

    public PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }

    public String getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    public void setPrimaryKeyClass(String primaryKeyClass) {
        this.primaryKeyClass = primaryKeyClass;
    }

    public boolean isReenterant() {
        return reenterant;
    }

    public void setReenterant(boolean reenterant) {
        this.reenterant = reenterant;
    }

    public String getCmpVersion() {
        return cmpVersion;
    }

    public void setCmpVersion(String cmpVersion) {
        this.cmpVersion = cmpVersion;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public void setAbstractSchemaName(String abstractSchemaName) {
        this.abstractSchemaName = abstractSchemaName;
    }

    public List<CmpField> getCmpFields() {
        return cmpFields;
    }

    public void setCmpFields(List<CmpField> cmpFields) {
        this.cmpFields = cmpFields;
    }

    public String getPrimkeyField() {
        return primkeyField;
    }

    public void setPrimkeyField(String primkeyField) {
        this.primkeyField = primkeyField;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
