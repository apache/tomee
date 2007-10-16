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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "jndiName",
    "defaultResourcePrincipal",
    "property",
    "createTablesAtDeploy",
    "dropTablesAtUndeploy",
    "databaseVendorName",
    "schemaGeneratorProperties"
})
public class CmpResource {
    @XmlElement(name = "jndi-name", required = true)
    protected String jndiName;
    @XmlElement(name = "default-resource-principal")
    protected DefaultResourcePrincipal defaultResourcePrincipal;
    protected List<Property> property;
    @XmlElement(name = "create-tables-at-deploy")
    protected String createTablesAtDeploy;
    @XmlElement(name = "drop-tables-at-undeploy")
    protected String dropTablesAtUndeploy;
    @XmlElement(name = "database-vendor-name")
    protected String databaseVendorName;
    @XmlElement(name = "schema-generator-properties")
    protected SchemaGeneratorProperties schemaGeneratorProperties;

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String value) {
        this.jndiName = value;
    }

    public DefaultResourcePrincipal getDefaultResourcePrincipal() {
        return defaultResourcePrincipal;
    }

    public void setDefaultResourcePrincipal(DefaultResourcePrincipal value) {
        this.defaultResourcePrincipal = value;
    }

    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

    public String getCreateTablesAtDeploy() {
        return createTablesAtDeploy;
    }

    public void setCreateTablesAtDeploy(String value) {
        this.createTablesAtDeploy = value;
    }

    public String getDropTablesAtUndeploy() {
        return dropTablesAtUndeploy;
    }

    public void setDropTablesAtUndeploy(String value) {
        this.dropTablesAtUndeploy = value;
    }

    public String getDatabaseVendorName() {
        return databaseVendorName;
    }

    public void setDatabaseVendorName(String value) {
        this.databaseVendorName = value;
    }

    public SchemaGeneratorProperties getSchemaGeneratorProperties() {
        return schemaGeneratorProperties;
    }

    public void setSchemaGeneratorProperties(SchemaGeneratorProperties value) {
        this.schemaGeneratorProperties = value;
    }
}
