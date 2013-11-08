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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.Entity;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

@Entity
public class MapElementPCChild
    extends MapElementPC {

    private String elementDataChild = AbstractTestCase.randomString();

    public void setElementDataChild(String elementDataChild) {
        this.elementDataChild = elementDataChild;
    }

    public String getElementDataChild() {
        return this.elementDataChild;
    }

    public int hashCode() {
        return (super.hashCode() + elementDataChild.hashCode())
            % Integer.MAX_VALUE;
    }

    public boolean equals(Object other) {
        return super.equals(other) &&
            ((MapElementPCChild) other)
                .elementDataChild.equals(elementDataChild);
    }

    public String toString() {
        return super.toString() + "::" + elementDataChild;
    }
}

