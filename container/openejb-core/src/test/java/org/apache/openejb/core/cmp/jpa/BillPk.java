/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp.jpa;

public class BillPk {
    private long billNumber;
    private long billVersion;
    private long billRevision;

    public BillPk() {

    }

    public BillPk(long number, long version, long revision) {
        this.billNumber = number;
        this.billVersion = version;
        this.billRevision = revision;
    }

    public long getBillNumber() {
        return this.billNumber;
    }

    public void setBillNumber(long number) {
        this.billNumber = number;
    }

    public long getBillVersion() {
        return this.billVersion;
    }

    public void setBillVersion(long version) {
        this.billVersion = version;
    }

    public long getBillRevision() {
        return this.billRevision;
    }

    public void setBillRevision(long revision) {
        this.billRevision = revision;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof BillPk))
            return false;

        BillPk pk = (BillPk)obj;

        if (billNumber != pk.billNumber)
            return false;

        if (billVersion != pk.billVersion)
            return false;

        if (billRevision != pk.billRevision)
            return false;

        return true;
    }

    public int hashCode() {
        return (billNumber + "." + billVersion + "." + billRevision).hashCode();
    }
}
