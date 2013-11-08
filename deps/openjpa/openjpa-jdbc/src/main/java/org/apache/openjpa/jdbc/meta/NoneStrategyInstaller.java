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

import org.apache.openjpa.jdbc.meta.strats.NoneClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneVersionStrategy;

/**
 * Clears all mapping information from classes and installs none strategies.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class NoneStrategyInstaller
    extends StrategyInstaller {

    /**
     * Constructor; supply configuration.
     */
    public NoneStrategyInstaller(MappingRepository repos) {
        super(repos);
    }

    public void installStrategy(ClassMapping cls) {
        cls.clearMapping();
        cls.setStrategy(NoneClassStrategy.getInstance(), null);
        cls.setSourceMode(cls.MODE_MAPPING, true);
    }

    public void installStrategy(FieldMapping field) {
        field.clearMapping();
        field.setStrategy(NoneFieldStrategy.getInstance(), null);
    }

    public void installStrategy(Version version) {
        version.clearMapping();
        version.setStrategy(NoneVersionStrategy.getInstance(), null);
    }

    public void installStrategy(Discriminator discrim) {
        discrim.clearMapping();
        discrim.setStrategy(NoneDiscriminatorStrategy.getInstance(), null);
    }
}
