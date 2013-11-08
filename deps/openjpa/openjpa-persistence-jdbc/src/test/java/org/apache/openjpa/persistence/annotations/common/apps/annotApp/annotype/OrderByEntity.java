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

import java.util.*;

import javax.persistence.*;

import org.apache.openjpa.persistence.*;

/**
 * <p>Entity used to test parsing of @OrderBy.</p>
 *
 * @author Abe White
 */
@Entity
public class OrderByEntity {

    @Id
    private long id;
    private String string;

    @PersistentCollection
    @OrderBy
    private List<String> strings = new ArrayList();

    @ManyToMany
    @OrderBy
    @JoinTable(name = "ORDERBY_PKRELS",
        inverseJoinColumns = @JoinColumn(name = "REL_ID",
            referencedColumnName = "ID"))
    private List<OrderByEntity> pkRels = new ArrayList();

    @ManyToMany
    @OrderBy("string desc")
    @JoinTable(name = "ORDERBY_STRINGRELS",
        inverseJoinColumns = @JoinColumn(name = "REL_ID",
            referencedColumnName = "ID"))
    private List<OrderByEntity> stringRels = new ArrayList();

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getString() {
        return this.string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public List<String> getStrings() {
        return this.strings;
    }

    public List<OrderByEntity> getPKRels() {
        return this.pkRels;
    }

    public List<OrderByEntity> getStringRels() {
        return this.stringRels;
    }
}
