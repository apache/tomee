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

package org.apache.openejb.openjpa;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.jdbc.PersistenceMappingDefaults;

public class PrefixMappingRepository extends MappingRepository {
    private String prefix;

    public PrefixMappingRepository() {
        setMappingDefaults(new PrefixMappingDefaults());
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void endConfiguration() {
        super.endConfiguration();
        Configurations.configureInstance(getMappingDefaults(), getConfiguration(), "jdbc.MappingDefaults");
        ((PrefixMappingDefaults) getMappingDefaults()).endConfiguration();
    }

    private class PrefixMappingDefaults extends PersistenceMappingDefaults {
        @Override
        public String getTableName(final ClassMapping cls, final Schema schema) {
            return prefix + super.getTableName(cls, schema);
        }

        @Override
        public String getTableName(final FieldMapping fm, final Schema schema) {
            return prefix + super.getTableName(fm, schema);
        }
    }
}
