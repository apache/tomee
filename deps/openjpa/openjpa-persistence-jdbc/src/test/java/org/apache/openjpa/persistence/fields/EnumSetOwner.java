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
package org.apache.openjpa.persistence.fields;

import java.util.EnumSet;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.Externalizer;

@Entity
public class EnumSetOwner {

    @Id
    private int id;

    @Version
    private int version;

    @Persistent
    @Externalizer("externalizer")
    private EnumSetHolder enumSetHolder;

    public Set<SampleEnum> getEnumSet() {
        return Collections.unmodifiableSet(enumSetHolder.enumSet);
    }

    public void setEnumSet(EnumSet<SampleEnum> enumSet) {
        enumSetHolder = new EnumSetHolder(enumSet);
    }

    public static class EnumSetHolder {

        private final EnumSet<SampleEnum> enumSet;

        public EnumSetHolder(String externalizedValue) {
            // this implementation can only handle the special form created
            // by the externalizer above
            if (!"all".equals(externalizedValue))
                throw new IllegalStateException(externalizedValue);

            enumSet = EnumSet.allOf(SampleEnum.class);
        }

        private EnumSetHolder(EnumSet<SampleEnum> enumSet) {
            this.enumSet = enumSet;
        }

        public String externalizer() {
            // this implementation always externalizes to a special
            // representation of all the values.
            return "all";
        }
    }
}
