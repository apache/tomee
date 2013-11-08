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
package
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype;

import javax.persistence.*;

@Embeddable
public class EmbeddedIdClass {

    @Column(name = "EPK1")
    private long pk1;

    @Column(name = "EPK2")
    private long pk2;

    @Column(name = "EPK3")
    @GeneratedValue
    private long pk3;

    public long getPk1() {
        return pk1;
    }

    public void setPk1(long pk1) {
        this.pk1 = pk1;
    }

    public long getPk2() {
        return pk2;
    }

    public void setPk2(long pk2) {
        this.pk2 = pk2;
    }

    public long getPk3() {
        return pk3;
    }

    public void setPk3(long pk3) {
        this.pk3 = pk3;
    }

    public String toString() {
        return pk1 + ":" + pk2 + ":" + pk3;
    }

    public int hashCode() {
        return (int) ((pk1 ^ pk2 ^ pk3) % Integer.MAX_VALUE);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof EmbeddedIdClass))
            return false;

        EmbeddedIdClass id = (EmbeddedIdClass) other;
        return id.pk1 == pk1
            && id.pk2 == pk2
            && id.pk3 == pk3;
    }
}
