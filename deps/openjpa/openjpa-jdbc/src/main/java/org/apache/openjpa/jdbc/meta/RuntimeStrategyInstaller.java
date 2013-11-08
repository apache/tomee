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

import org.apache.openjpa.jdbc.meta.strats.NoneFieldStrategy;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.MetaDataException;

/**
 * Installer that attempts to use the given mapping information, and
 * fails if it does not work.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class RuntimeStrategyInstaller
    extends StrategyInstaller {

    private static final Localizer _loc = Localizer.forPackage
        (RuntimeStrategyInstaller.class);

    /**
     * Constructor; supply configuration.
     */
    public RuntimeStrategyInstaller(MappingRepository repos) {
        super(repos);
    }

    public void installStrategy(ClassMapping cls) {
        if ((cls.getSourceMode() & cls.MODE_MAPPING) == 0)
            throw new MetaDataException(_loc.get("no-mapping", cls));

        ClassStrategy strat = repos.namedStrategy(cls);
        if (strat == null)
            strat = repos.defaultStrategy(cls, false);
        cls.setStrategy(strat, Boolean.FALSE);
    }

    public void installStrategy(FieldMapping field) {
        FieldStrategy strategy = null;
        ClassMapping owner = getOutermostDefiningMapping(field); 
        if (owner != null && !owner.isEmbeddable() && !owner.isAbstract())
            strategy = repos.namedStrategy(field, true);
        if (strategy == null) {
            try {
                strategy = repos.defaultStrategy(field, true, false);
            } catch (MetaDataException mde) {
                // if the parent class is abstract and field is unmapped,
                // allow it to pass (assume subclasses map the field)
                Class cls = field.getDefiningMetaData().getDescribedType();
                if (!Modifier.isAbstract(cls.getModifiers())
                    || field.getMappedBy() != null
                    || field.getMappingInfo().hasSchemaComponents()
                    || field.getValueInfo().hasSchemaComponents()
                    || field.getElementMapping().getValueInfo().hasSchemaComponents()
                    || field.getKeyMapping().getValueInfo().hasSchemaComponents())
                    throw mde;

                strategy = NoneFieldStrategy.getInstance();
            }
        }
        field.setStrategy(strategy, Boolean.FALSE);
    }
    
    private ClassMapping getOutermostDefiningMapping(ValueMetaData vm) {
        if (vm instanceof FieldMapping) {
            ClassMapping owner = ((FieldMapping)vm).getDefiningMapping();
            ValueMetaData val = owner.getEmbeddingMetaData();
            if (val == null)
                return owner; 
            return getOutermostDefiningMapping(val);
        } else if (vm instanceof ValueMappingImpl) {
            FieldMapping owner = ((ValueMappingImpl)vm).getFieldMapping();
            return getOutermostDefiningMapping(owner);
        }
        return null;
    }

    public void installStrategy(Version version) {
        VersionStrategy strat = repos.namedStrategy(version);
        if (strat == null)
            strat = repos.defaultStrategy(version, false);
        version.setStrategy(strat, Boolean.FALSE);
    }

    public void installStrategy(Discriminator discrim) {
        DiscriminatorStrategy strat = repos.namedStrategy(discrim);
        if (strat == null)
            strat = repos.defaultStrategy(discrim, false);
        discrim.setStrategy(strat, Boolean.FALSE);
    }
}
