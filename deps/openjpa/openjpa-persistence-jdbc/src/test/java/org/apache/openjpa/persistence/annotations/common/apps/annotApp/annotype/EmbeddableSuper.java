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

@MappedSuperclass
public class EmbeddableSuper {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long pk;

    @Version
    @Column(name = "SUPVERS")
    private int version;

    @Transient
    private int trans;

    @Lob
    @Column(name = "CLOBVAL")
    protected String clob;

    public EmbeddableSuper() {
    }

    public long getPK() {
        return this.pk;
    }

    public void setPK(long pk) {
        this.pk = pk;
    }

    public int getTrans() {
        return this.trans;
    }

    public void setTrans(int trans) {
        this.trans = trans;
    }

    public String getClob() {
        return this.clob;
    }

    public void setClob(String clob) {
        this.clob = clob;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
