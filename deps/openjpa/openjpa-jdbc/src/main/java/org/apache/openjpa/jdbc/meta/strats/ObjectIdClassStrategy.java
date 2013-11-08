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

import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Class mapping for embedded object id fields.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ObjectIdClassStrategy
    extends AbstractClassStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (ObjectIdClassStrategy.class);

    public void map(boolean adapt) {
        ValueMapping vm = cls.getEmbeddingMapping();
        if (vm == null || vm.getType() != cls.getDescribedType()
            || vm.getTypeCode() != JavaTypes.OID)
            throw new MetaDataException(_loc.get("not-oid", cls));

        ClassMappingInfo info = cls.getMappingInfo();
        info.assertNoSchemaComponents(cls, true);
        cls.setTable(vm.getFieldMapping().getTable());
    }
}
