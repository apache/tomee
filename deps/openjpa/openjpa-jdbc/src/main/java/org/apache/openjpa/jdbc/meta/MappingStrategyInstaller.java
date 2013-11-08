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

import org.apache.openjpa.meta.MetaDataModes;

/**
 * Installer used during mapping that attempts to use the given mapping
 * information (if any), and fails if it does not work.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public class MappingStrategyInstaller
    extends StrategyInstaller {

    /**
     * Constructor; supply configuration.
     */
    public MappingStrategyInstaller(MappingRepository repos) {
        super(repos);
    }

    public boolean isAdapting() {
        return true;
    }

    public void installStrategy(ClassMapping cls) {
        ClassStrategy strat = repos.namedStrategy(cls);
        if (strat == null)
            strat = repos.defaultStrategy(cls, true);
        cls.setStrategy(strat, Boolean.TRUE);
        cls.setSourceMode(MetaDataModes.MODE_MAPPING, true);
    }

    public void installStrategy(FieldMapping field) {
        FieldStrategy strategy = repos.namedStrategy(field, true);
        if (strategy == null)
            strategy = repos.defaultStrategy(field, true, true);
        field.setStrategy(strategy, Boolean.TRUE);
    }

    public void installStrategy(Version version) {
        VersionStrategy strat = repos.namedStrategy(version);
        if (strat == null)
            strat = repos.defaultStrategy(version, true);
        version.setStrategy(strat, Boolean.TRUE);
    }

    public void installStrategy(Discriminator discrim) {
        DiscriminatorStrategy strat = repos.namedStrategy(discrim);
        if (strat == null)
            strat = repos.defaultStrategy(discrim, true);
        discrim.setStrategy(strat, Boolean.TRUE);
    }
}
