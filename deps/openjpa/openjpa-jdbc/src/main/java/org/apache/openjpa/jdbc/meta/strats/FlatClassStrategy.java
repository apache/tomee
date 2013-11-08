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
package org.apache.openjpa.jdbc.meta.strats;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for classes mapped to their superclass table.
 *
 * @author Abe White
 */
public class FlatClassStrategy
    extends AbstractClassStrategy {

    public static final String ALIAS = "flat";

    private static final Localizer _loc = Localizer.forPackage
        (FlatClassStrategy.class);

    public String getAlias() {
        return ALIAS;
    }

    public void map(boolean adapt) {
        ClassMapping sup = cls.getMappedPCSuperclassMapping();
        if (sup == null || cls.getEmbeddingMetaData() != null)
            throw new MetaDataException(_loc.get("not-sub", cls));

        ClassMappingInfo info = cls.getMappingInfo();
        info.assertNoSchemaComponents(cls, true);

        if (!DBIdentifier.isNull(info.getTableIdentifier())) {
            Table table = info.createTable(cls, null, info.getSchemaIdentifier(),
                info.getTableIdentifier(), false);
            if (table != sup.getTable())
                throw new MetaDataException(_loc.get("flat-table", cls,
                    table.getFullName(), sup.getTable().getFullName()));
        }

        cls.setTable(sup.getTable());
        cls.setPrimaryKeyColumns(sup.getPrimaryKeyColumns());
        cls.setColumnIO(sup.getColumnIO());
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return cls.getMappedPCSuperclassMapping().isPrimaryKeyObjectId(hasAll);
    }
}
