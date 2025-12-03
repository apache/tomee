/*
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
package org.apache.openejb.jpa.integration.hibernate;

import org.apache.openejb.jpa.integration.JPAThreadContext;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Properties;

public class PrefixNamingStrategy implements PhysicalNamingStrategy {
    private String prefix;

    public String getPrefix() {
        if (prefix == null) {
            if (JPAThreadContext.infos.containsKey("properties")) {
                prefix = ((Properties) JPAThreadContext.infos.get("properties")).getProperty("openejb.jpa.table_prefix");
            }
            if (prefix == null) {
                prefix = "";
            }
        }
        return prefix;
    }

    private Identifier addPrefix(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        return Identifier.toIdentifier(getPrefix() + name.getText());
    }

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return addPrefix(name, context);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return identifier;
    }

}
