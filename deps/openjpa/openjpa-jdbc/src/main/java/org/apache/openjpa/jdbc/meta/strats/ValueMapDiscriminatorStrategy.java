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

import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Maps metadata-given values to classes.
 *
 * @author Abe White
 */
public class ValueMapDiscriminatorStrategy
    extends InValueDiscriminatorStrategy {

    public static final String ALIAS = "value-map";

    private static final Localizer _loc = Localizer.forPackage
        (ValueMapDiscriminatorStrategy.class);

    private Map _vals = null;

    public String getAlias() {
        return ALIAS;
    }

    protected int getJavaType() {
        Object val = disc.getValue();
        if (val != null && val != Discriminator.NULL)
            return JavaTypes.getTypeCode(val.getClass());

        // if the user wants the type to be null, we need a jdbc-type
        // on the column or an existing column to figure out the java type
        DiscriminatorMappingInfo info = disc.getMappingInfo();
        List cols = info.getColumns();
        Column col = (cols.isEmpty()) ? null : (Column) cols.get(0);
        if (col != null) {
            if (col.getJavaType() != JavaTypes.OBJECT)
                return col.getJavaType();
            if (col.getType() != Types.OTHER)
                return JavaTypes.getTypeCode(Schemas.getJavaType
                    (col.getType(), col.getSize(), col.getDecimalDigits()));
        }
        return JavaTypes.STRING;
    }

    protected Object getDiscriminatorValue(ClassMapping cls) {
        Object val = cls.getDiscriminator().getValue();
        return (val == Discriminator.NULL) ? null : val;
    }

    protected Class getClass(Object val, JDBCStore store)
        throws ClassNotFoundException {
        if (_vals == null) {
            ClassMapping cls = disc.getClassMapping();
            ClassMapping[] subs = cls.getJoinablePCSubclassMappings();
            Map map = new HashMap((int) ((subs.length + 1) * 1.33 + 1));
            mapDiscriminatorValue(cls, map);
            for (int i = 0; i < subs.length; i++)
                mapDiscriminatorValue(subs[i], map);
            _vals = map;
        }

        String str = (val == null) ? null : val.toString();
        Class cls = (Class) _vals.get(str);
        if (cls != null)
            return cls;
        throw new ClassNotFoundException(_loc.get("unknown-discrim-value",
            new Object[]{ str, disc.getClassMapping().getDescribedType().
            getName(), new TreeSet(_vals.keySet()) }).getMessage());
    }

    /**
     * Map the stringified version of the discriminator value of the given type.
     */
    private static void mapDiscriminatorValue(ClassMapping cls, Map map) {
        // possible that some types will never be persisted and therefore
        // can have no discriminator value
        Object val = cls.getDiscriminator().getValue();
        if (val == null)
            return;

        String str = (val == Discriminator.NULL) ? null : val.toString();
        Class exist = (Class) map.get(str);
        if (exist != null)
            throw new MetaDataException(_loc.get("dup-discrim-value",
                str, exist, cls));
        map.put(str, cls.getDescribedType());
    }

    public void map(boolean adapt) {
        Object val = disc.getMappingInfo().getValue(disc, adapt);
        if (val == null && !Modifier.isAbstract(disc.getClassMapping().
            getDescribedType().getModifiers()))
            throw new MetaDataException(_loc.get("no-discrim-value",
                disc.getClassMapping()));

        // we set the value before mapping to use to calculate the template
        // column's java type
        disc.setValue(val);
        super.map(adapt);
    }
}
