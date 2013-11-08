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
package org.apache.openjpa.jdbc.meta;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.SuperclassDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.ValueMapDiscriminatorStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Information about the mapping from a discriminator to the schema, in
 * raw form. The columns and tables used in mapping info will not be part of
 * the {@link SchemaGroup} used at runtime. Rather, they will be structs
 * with the relevant pieces of information filled in.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class DiscriminatorMappingInfo
    extends MappingInfo {

    private String _value = null;
    
    /**
     * Raw discriminator value string.
     */
    public String getValue() {
        return _value;
    }

    /**
     * Raw discriminator value string.
     */
    public void setValue(String value) {
        _value = value;
    }

    /**
     * Return the discriminator value as an object of the right type.
     */
    public Object getValue(Discriminator discrim, boolean adapt) {
        if (discrim.getValue() != null)
            return discrim.getValue();
        if (StringUtils.isEmpty(_value)) {
            return discrim.getMappingRepository().getMappingDefaults().
                getDiscriminatorValue(discrim, adapt);
        }
        
        switch(discrim.getJavaType()) { 
            case JavaTypes.INT:
                return Integer.valueOf(_value);
            case JavaTypes.CHAR:
               return Character.valueOf(_value.charAt(_value.indexOf('\'')+1));
            case JavaTypes.STRING:
            default: 
                return _value;
        }
    }

    /**
     * Return the columns set for this discriminator, based on the given
     * templates.
     */
    public Column[] getColumns(Discriminator discrim, Column[] tmplates,
        boolean adapt) {
        Table table = discrim.getClassMapping().getTable();
        discrim.getMappingRepository().getMappingDefaults().populateColumns
            (discrim, table, tmplates);
        return createColumns(discrim, null, tmplates, table, adapt);
    }

    /**
     * Return the index to set on the discriminator columns, or null if none.
     */
    public Index getIndex(Discriminator discrim, Column[] cols, boolean adapt) {
        Index idx = null;
        if (cols.length > 0)
            idx = discrim.getMappingRepository().getMappingDefaults().
                getIndex(discrim, cols[0].getTable(), cols);
        return createIndex(discrim, null, idx, cols, adapt);
    }

    /**
     * Synchronize internal information with the mapping data for the given
     * discriminator.
     */
    public void syncWith(Discriminator disc) {
        clear(false);

        // set io before syncing cols
        setColumnIO(disc.getColumnIO());
        syncColumns(disc, disc.getColumns(), disc.getValue() != null
            && !(disc.getValue() instanceof String));
        syncIndex(disc, disc.getIndex());
        if (disc.getValue() == Discriminator.NULL)
            _value = "null";
        else if (disc.getValue() != null)
            _value = disc.getValue().toString();

        if (disc.getStrategy() == null
            || disc.getStrategy() instanceof SuperclassDiscriminatorStrategy)
            return;

        // explicit discriminator strategy if:
        // - unmapped class and discriminator is mapped
        // - final base class and discriminator is mapped
        // - table-per-class subclass and discriminator is mapped
        // - mapped subclass and doesn't rely on superclass discriminator
        // - mapped base class and doesn't use value-map strategy with value
        //   and isn't a final class that uses the final strategy
        ClassMapping cls = disc.getClassMapping();
        String strat = disc.getStrategy().getAlias();
        boolean sync = false;

        if (!cls.isMapped()
            || (cls.getJoinablePCSuperclassMapping() != null
            && Modifier.isFinal(cls.getDescribedType().getModifiers()))
            || (cls.getJoinablePCSuperclassMapping() == null
            && cls.getMappedPCSuperclassMapping() != null))
            sync = !NoneDiscriminatorStrategy.ALIAS.equals(strat);
        else
            sync = cls.getJoinablePCSuperclassMapping() != null
                || _value == null
                || !ValueMapDiscriminatorStrategy.ALIAS.equals(strat);

        if (sync)
            setStrategy(strat);
    }

    protected void clear(boolean canFlags) {
        super.clear(canFlags);
        _value = null;
    }

    public void copy(MappingInfo info) {
        super.copy(info);
        if (!(info instanceof DiscriminatorMappingInfo))
            return;

        if (_value == null)
            _value = ((DiscriminatorMappingInfo) info).getValue();
    }
}
