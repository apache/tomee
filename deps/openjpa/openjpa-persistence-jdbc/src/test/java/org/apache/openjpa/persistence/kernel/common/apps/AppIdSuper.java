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

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Abstract superclass with no primary key fields defined.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
public abstract class AppIdSuper {

    private String superField;

    public void setSuperField(String superField) {
        this.superField = superField;
    }

    public String getSuperField() {
        return this.superField;
    }

    public static abstract class ID
        implements Serializable {

        static String DELIMITER = ":";

        public ID() {
        }

        public ID(String str) {
            fromString(str);
        }

        public int hashCode() {
            // no key codes; all classes are equal
            return 1;
        }

        public boolean equals(Object other) {
            return other != null && other.getClass() == getClass();
        }

        public String toString() {
            return "";
        }

        StringTokenizer fromString(String idString) {
            return new StringTokenizer(idString, DELIMITER);
        }
    }
}

