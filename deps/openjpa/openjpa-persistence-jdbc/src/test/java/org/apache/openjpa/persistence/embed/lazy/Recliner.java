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
package org.apache.openjpa.persistence.embed.lazy;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Table;

import org.apache.openjpa.persistence.Persistent;

@Entity
@Table(name="REC_TABLE")
public class Recliner {
    
    @EmbeddedId
    private ReclinerId id;

    @Enumerated(EnumType.STRING)
    @Column(name="REC_STYLE")
    private Style style;
    
    @Embedded  // Lazy fetch set via xml mapping
    private Guy guy;
    
    @Persistent(fetch=FetchType.LAZY, embedded=true)
    private BeverageHolder holder;

    public void setId(ReclinerId id) {
        this.id = id;
    }

    public ReclinerId getId() {
        return id;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Style getStyle() {
        return style;
    }

    public void setGuy(Guy guy) {
        this.guy = guy;
    }

    public Guy getGuy() {
        return guy;
    }

    public void setHolder(BeverageHolder holder) {
        this.holder = holder;
    }

    public BeverageHolder getHolder() {
        return holder;
    }
}
