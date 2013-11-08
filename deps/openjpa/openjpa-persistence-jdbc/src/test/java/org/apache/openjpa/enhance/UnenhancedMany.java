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
package org.apache.openjpa.enhance;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class UnenhancedMany implements Serializable, Cloneable {
    private static final long serialVersionUID = 4041356744771116705L;

    @Id
    private int id;

    @ManyToOne
    private UnenhancedOne one;

    public UnenhancedMany() {
    }

    public UnenhancedMany(int id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public UnenhancedOne getOne() {
        return one;
    }

    public void setOne(UnenhancedOne one) {
        this.one = one;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!getClass().isAssignableFrom(o.getClass())) return false;

        return id == ((UnenhancedMany) o).id;
    }

    public int hashCode() {
        return id;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
