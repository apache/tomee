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
package org.apache.openjpa.jdbc.identifier;

import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;

/**
 * Default rule for column definition.  This rule disables delimiting of
 * column definitions.  Column definitions can be extremely tricky to 
 * delimit correctly.  Blindly delimiting them causes failures on most
 * databases.  Where user defined types are concerned, generally they don't
 * need to be delimited and if so, they are more appropriately delimited
 * when they are specified.
 */
public class ColumnDefIdentifierRule extends DBIdentifierRule {

    public ColumnDefIdentifierRule() {
        super();
        setName(DBIdentifierType.COLUMN_DEFINITION.toString());
        // Disable auto delimiting of column definition.
        setCanDelimit(false);
    }
}
