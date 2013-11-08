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
package org.apache.openjpa.persistence.jdbc.annotations;


import javax.persistence.*;

@Entity
@DiscriminatorColumn(name = "DISC")
@AttributeOverrides({
    @AttributeOverride(name = "clob", column = @Column(name = "CC")),
    @AttributeOverride(name = "version", column = @Column(name = "VERSVAL"))
})
public class EmbeddableSuperSub
    extends EmbeddableSuper {

    @ManyToOne
    private EmbeddableSuperSub sub;

    @ManyToOne
    // #####
    private EmbeddableSuper sup;

    public EmbeddableSuperSub() {
    }

    public EmbeddableSuperSub getSub() {
        return this.sub;
    }

    public void setSub(EmbeddableSuperSub sub) {
        this.sub = sub;
    }

    public EmbeddableSuper getSup() {
        return this.sup;
    }

    public void setSup(EmbeddableSuper sup) {
        this.sup = sup;
    }
}
