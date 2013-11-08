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

import java.util.StringTokenizer;
import javax.persistence.Entity;

/**
 * Abstract subclass that defines one more primary key field than its
 * abstract superclass.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
@Entity
public abstract class AppIdSubB
    extends AppIdSubA {

    private String pkb;
    private String stringFieldB;

    public void setStringFieldB(String stringFieldB) {
        this.stringFieldB = stringFieldB;
    }

    public String getStringFieldB() {
        return this.stringFieldB;
    }

    public void setPkb(String pkb) {
        this.pkb = pkb;
    }

    public String getPkb() {
        return this.pkb;
    }

    public static abstract class ID
        extends AppIdSubA.ID {

        public String pkb;

        public ID() {
            super();
        }

        public ID(String str) {
            super();
            fromString(str);
        }

        public int hashCode() {
            return (super.hashCode() + (pkb == null ? 0 : pkb.hashCode()))
                % Integer.MAX_VALUE;
        }

        public boolean equals(Object other) {
            return super.equals(other)
                && ((ID) other).pkb == null ? pkb == null
                : ((ID) other).pkb.equals(pkb);
        }

        public String toString() {
            return super.toString() + DELIMITER + pkb;
        }

        StringTokenizer fromString(String idString) {
            StringTokenizer tok = super.fromString(idString);
            pkb = tok.nextToken();
            return tok; // return the tokenizer for subclasses to use
        }
    }
}
