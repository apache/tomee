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
package org.apache.openejb.jpa.integration.eclipselink;

import org.apache.openejb.jpa.integration.JPAThreadContext;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.ManyToManyMapping;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.sessions.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PrefixSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(final Session session) throws Exception {
        if (JPAThreadContext.infos.containsKey("properties")) {
            final String prefix = ((Properties) JPAThreadContext.infos.get("properties")).getProperty("openejb.jpa.table_prefix");
            final List<DatabaseTable> tables = new ArrayList<DatabaseTable>();
            for (final ClassDescriptor cd : session.getDescriptors().values()) {
                for (final DatabaseTable table : cd.getTables()) {
                    update(prefix, tables, table);
                }
                for (final DatabaseMapping mapping : cd.getMappings()) {
                    if (mapping instanceof ManyToManyMapping) {
                        update(prefix, tables, ((ManyToManyMapping) mapping).getRelationTable());
                    } else if (mapping instanceof DirectCollectionMapping) {
                        update(prefix, tables, ((DirectCollectionMapping) mapping).getReferenceTable());
                    } // TODO: else check we need to update something
                }
            }

            final Sequence sequence = session.getDatasourcePlatform().getDefaultSequence();
            if (sequence instanceof TableSequence) {
                final TableSequence ts = ((TableSequence) sequence);
                ts.setName(prefix + ts.getName());
            }
        }
    }

    private void update(final String prefix, final List<DatabaseTable> tables, final DatabaseTable table) {
        if (!tables.contains(table)) {
            table.setName(prefix + table.getName());
            tables.add(table);
        }
    }
}
