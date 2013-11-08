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
package org.apache.openjpa.kernel;

import org.apache.openjpa.enhance.UnenhancedFieldAccess;
import org.apache.openjpa.enhance.UnenhancedFieldAccessSubclass;
import org.apache.openjpa.persistence.test.AllowFailure;

@AllowFailure(message="excluded")
public class TestUnenhancedFieldAccessWithRelationInstanceBrokerSerialization
    extends AbstractUnenhancedRelationBrokerSerializationTest
        <UnenhancedFieldAccessSubclass> {

    protected Class getSecondaryType() {
        return UnenhancedFieldAccess.class;
    }

    protected Class<UnenhancedFieldAccessSubclass> getManagedType() {
        return UnenhancedFieldAccessSubclass.class;
    }

    protected UnenhancedFieldAccessSubclass newManagedInstance() {
        UnenhancedFieldAccessSubclass e = new UnenhancedFieldAccessSubclass();
        e.setStringField("foo");
        UnenhancedFieldAccess related = new UnenhancedFieldAccess();
        related.setStringField("bar");
        e.setRelated(related);
        return e;
    }

    protected void modifyInstance(UnenhancedFieldAccessSubclass e) {
        e.getRelated().setStringField("modified");
    }

    protected Object getModifiedValue(UnenhancedFieldAccessSubclass e) {
        return e.getRelated().getStringField();
    }

    @Override
    protected int graphSize() {
        return 2;
    }
}
