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

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Attempts to install using the given mapping information. If that
 * fails, clears the mapping information and constructs new mappings.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class RefreshStrategyInstaller
    extends StrategyInstaller {

    private static final Localizer _loc = Localizer.forPackage
        (RefreshStrategyInstaller.class);

    /**
     * Constructor; supply configuration.
     */
    public RefreshStrategyInstaller(MappingRepository repos) {
        super(repos);
    }

    public boolean isAdapting() {
        return true;
    }

    public void installStrategy(ClassMapping cls) {
        ClassStrategy strat = repos.namedStrategy(cls);
        if (strat == null)
            strat = repos.defaultStrategy(cls, true);
        try {
            cls.setStrategy(strat, Boolean.TRUE);
        } catch (MetaDataException mde) {
            // if this is a custom strategy, don't attempt to override
            if (isCustomStrategy(strat))
                throw mde;

            repos.getLog().warn(_loc.get("fatal-change", cls,
                mde.getMessage()));
            cls.clearMapping();
            cls.setStrategy(repos.defaultStrategy(cls, true), Boolean.TRUE);
        }
        cls.setSourceMode(cls.MODE_MAPPING, true);
    }

    public void installStrategy(FieldMapping field) {
        FieldStrategy strategy = repos.namedStrategy(field, true);
        if (strategy == null)
            strategy = repos.defaultStrategy(field, true, true);
        try {
            field.setStrategy(strategy, Boolean.TRUE);
        } catch (MetaDataException mde) {
            // if this is a custom strategy, don't override
            if (isCustomStrategy(strategy))
                throw mde;

            repos.getLog().warn(_loc.get("fatal-change", field,
                mde.getMessage()));
            field.clearMapping();
            field.setHandler(null);
            field.getKeyMapping().setHandler(null);
            field.getElementMapping().setHandler(null);
            field.setStrategy(repos.defaultStrategy(field, true, true),
                Boolean.TRUE);
        }
    }

    public void installStrategy(Version version) {
        VersionStrategy strat = repos.namedStrategy(version);
        if (strat == null)
            strat = repos.defaultStrategy(version, true);
        try {
            version.setStrategy(strat, Boolean.TRUE);
        } catch (MetaDataException mde) {
            // if this is a custom strategy, don't attempt to override
            if (isCustomStrategy(strat))
                throw mde;

            repos.getLog().warn(_loc.get("fatal-change", version,
                mde.getMessage()));
            version.clearMapping();
            version.setStrategy(repos.defaultStrategy(version, true),
                Boolean.TRUE);
        }
    }

    public void installStrategy(Discriminator discrim) {
        DiscriminatorStrategy strat = repos.namedStrategy(discrim);
        if (strat == null)
            strat = repos.defaultStrategy(discrim, true);
        try {
            discrim.setStrategy(strat, Boolean.TRUE);
        } catch (MetaDataException mde) {
            // if this is a custom strategy, don't attempt to override
            if (isCustomStrategy(strat))
                throw mde;

            repos.getLog().warn(_loc.get("fatal-change", discrim,
                mde.getMessage()));

            // retain old discriminator version, if any
            String val = discrim.getMappingInfo().getValue();
            discrim.clearMapping();
            discrim.getMappingInfo().setValue(val);
            discrim.setStrategy(repos.defaultStrategy(discrim, true),
                Boolean.TRUE);
        }
    }

    /**
     * Return true if the given strategy is not a built-in type.
     */
    private static boolean isCustomStrategy(Strategy strat) {
        return !strat.getClass().getName().startsWith("org.apache.openjpa.");
    }
}
