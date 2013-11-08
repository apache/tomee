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
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.DiscriminatorStrategy;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;

/**
 * No-op strategy for easy extension.
 *
 * @author Abe White
 */
public abstract class AbstractDiscriminatorStrategy
    extends AbstractStrategy
    implements DiscriminatorStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (AbstractDiscriminatorStrategy.class);

    /**
     * The owning discriminator.
     */
    protected Discriminator disc = null;

    /**
     * Whether the owning class is final.
     */
    protected boolean isFinal = false;

    public void setDiscriminator(Discriminator owner) {
        disc = owner;
        ClassMapping cls = disc.getClassMapping();
        isFinal = Modifier.isFinal(cls.getDescribedType().getModifiers());
    }

    public boolean select(Select sel, ClassMapping mapping) {
        return false;
    }

    /**
     * By default, logs a warning that this discriminator cannot calculate
     * its list of subclasses on its own.
     */
    public void loadSubclasses(JDBCStore store)
        throws SQLException, ClassNotFoundException {
        if (!isFinal) {
            Log log = disc.getMappingRepository().getLog();
            if (log.isWarnEnabled())
                log.warn(_loc.get("cant-init-subs", disc.getClassMapping()));
        }

        // don't need to call this method again
        disc.setSubclassesLoaded(true);
    }

    public Class getClass(JDBCStore store, ClassMapping base, Result result)
        throws SQLException, ClassNotFoundException {
        return base.getDescribedType();
    }

    public boolean hasClassConditions(ClassMapping base, boolean subs) {
        return false;
    }

    public SQLBuffer getClassConditions(Select sel, Joins joins, 
        ClassMapping base, boolean subs) {
        return null;
    }
}
