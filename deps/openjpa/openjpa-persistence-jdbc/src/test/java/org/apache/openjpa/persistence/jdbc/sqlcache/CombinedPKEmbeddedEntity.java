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

package org.apache.openjpa.persistence.jdbc.sqlcache;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CombinedPKEmbeddedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "keyA", nullable = false)
    private int keyA;
    @Column(name = "keyB", nullable = false)
    private int keyB;
    @Column(name = "keyC", nullable = false)
    private int keyC;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + keyA;
        result = prime * result + keyB;
        result = prime * result + keyC;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CombinedPKEmbeddedEntity other = (CombinedPKEmbeddedEntity) obj;
        if (keyA != other.keyA)
            return false;
        if (keyB != other.keyB)
            return false;
        if (keyC != other.keyC)
            return false;
        return true;
    }

    public int getKeyA() {
        return keyA;
    }

    public void setKeyA(int keyA) {
        this.keyA = keyA;
    }

    public int getKeyB() {
        return keyB;
    }

    public void setKeyB(int keyB) {
        this.keyB = keyB;
    }

    public int getKeyC() {
        return keyC;
    }

    public void setKeyC(int keyC) {
        this.keyC = keyC;
    }

}
