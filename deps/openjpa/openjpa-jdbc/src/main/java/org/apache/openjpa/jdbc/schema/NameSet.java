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
package org.apache.openjpa.jdbc.schema;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Name sets track what names have been taken, ignoring case.
 * {@link SchemaGroup}s implement this interface for tables, indexes, and
 * constraints; {@link Table}s implement it for their columns.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class NameSet
    implements Serializable {

    private static final Localizer _loc = Localizer.forPackage(NameSet.class);

    private Set<DBIdentifier> _names = null;

    /**
     * Return true if the given name is in use already.
     * @deprecated
     */
    public boolean isNameTaken(String name) {
        return isNameTaken(DBIdentifier.toUpper(DBIdentifier.newDefault(name)));
    }
    
    public boolean isNameTaken(DBIdentifier name) {
        if (DBIdentifier.isEmpty(name)) {
            return true;
        }
        if (_names == null) {
            return false;
        }
        DBIdentifier sName = DBIdentifier.toUpper(name);
        return _names.contains(sName);
    }

    /**
     * @deprecated
     */
    protected void addName(String name, boolean validate) {
        addName(DBIdentifier.newIdentifier(name, DBIdentifierType.DEFAULT, true), validate);
    }
    /**
     * Attempt to add the given name to the set.
     *
     * @param name the name to add
     * @param validate if true, null or empty names will not be accepted
     */
    protected void addName(DBIdentifier name, boolean validate) {
        if (DBIdentifier.isNull(name) || StringUtils.isEmpty(name.getName())) {
            if (validate)
                throw new IllegalArgumentException(_loc.get("bad-name", name)
                    .getMessage());
            return;
        }

        // unfortunately, we can't check for duplicate names, because different
        // DBs use different namespaces for components, and it would be
        // difficult to find a scheme that fits all and is still useful
        if (_names == null)
            _names = new HashSet<DBIdentifier>();
        DBIdentifier sName = DBIdentifier.toUpper(name);
        _names.add(sName);
    }

    /**
     * @deprecated
     */
    protected void removeName(String name) {
        if (name != null && _names != null) {
            removeName(DBIdentifier.newIdentifier(name, DBIdentifierType.DEFAULT, true));
        }
    }
    /**
     * Remove the given name from the table.
     */
    protected void removeName(DBIdentifier name) {
        if (!DBIdentifier.isNull(name) && _names != null) {
            DBIdentifier sName = DBIdentifier.toUpper(name);
            _names.remove(sName);
        }
    }
    
}
