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

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TablePerClass1 {

    @Id
    @GeneratedValue
    protected int pk;

    @Column(name = "TPC_BASIC")
    protected int basic;

    @Embedded
    @AttributeOverrides({
    @AttributeOverride(name = "basic",
        column = @Column(name = "TPC_EMB_BASIC")),
    @AttributeOverride(name = "clob", column = @Column(name = "TPC_EMB_CLOB")),
    @AttributeOverride(name = "blob", column = @Column(name = "TPC_EMB_BLOB"))
        })
    protected EmbedValue embed;

    public int getPk() {
        return pk;
    }

    public void setBasic(int i) {
        basic = i;
    }

    public int getBasic() {
        return basic;
    }

    public EmbedValue getEmbed() {
        return embed;
    }

    public void setEmbed(EmbedValue ev) {
        embed = ev;
    }
}

