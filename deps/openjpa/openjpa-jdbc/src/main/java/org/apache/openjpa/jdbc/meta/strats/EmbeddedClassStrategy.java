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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Class mapping for embedded objects.
 *
 * @author Abe White
 * @nojavadoc
 */
public class EmbeddedClassStrategy
    extends AbstractClassStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (EmbeddedClassStrategy.class);

    public void map(boolean adapt) {
        ValueMapping vm = cls.getEmbeddingMapping();
        if (vm == null || vm.getType() != cls.getDescribedType())
            throw new MetaDataException(_loc.get("not-embed", cls));

        ClassMappingInfo info = cls.getMappingInfo();
        info.assertNoSchemaComponents(cls, true);

        ClassMapping owner = null;
        if (vm.getValueMappedByMapping() != null)
            owner = vm.getValueMappedByMapping().getDefiningMapping();
        else
            owner = vm.getFieldMapping().getDefiningMapping();
        cls.setIdentityType(owner.getIdentityType());
        cls.setObjectIdType(owner.getObjectIdType(),
            owner.isObjectIdTypeShared());
        cls.setTable(vm.getFieldMapping().getTable());
        cls.setPrimaryKeyColumns(owner.getPrimaryKeyColumns());
        cls.setColumnIO(owner.getColumnIO());
    }

    /**
     * Return the proper synthetic null indicator value for the given instance.
     */
    public Object getNullIndicatorValue(OpenJPAStateManager sm) {
        Column[] cols = cls.getEmbeddingMapping().getColumns();
        if (cols.length != 1)
            return null;
        if (sm == null && !cols[0].isNotNull())
            return null;
        if (sm == null)
            return JavaSQLTypes.getEmptyValue(cols[0].getJavaType());
        return JavaSQLTypes.getNonEmptyValue(cols[0].getJavaType());
    }

    /**
     * Return whether the given null indicator value means the object is null.
     */
    public boolean indicatesNull(Object val) {
        Column[] cols = cls.getEmbeddingMapping().getColumns();
        if (cols.length != 1)
            return false;
        if (val == null)
            return true;
        if (cols[0].isNotNull()
            && val.equals(JavaSQLTypes.getEmptyValue(cols[0].getJavaType())))
            return true;
        if (cols[0].getDefaultString() != null
            && val.toString().equals(cols[0].getDefaultString()))
            return true;
        return false;
    }

    public boolean isPrimaryKeyObjectId(boolean hasAll) {
        return cls.getEmbeddingMapping().getFieldMapping().
            getDefiningMapping().isPrimaryKeyObjectId(hasAll);
    }
}
