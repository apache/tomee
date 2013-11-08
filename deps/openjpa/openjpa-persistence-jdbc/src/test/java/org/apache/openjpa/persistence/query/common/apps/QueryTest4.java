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
package org.apache.openjpa.persistence.query.common.apps;

import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

/**
 * <p>Used in testing; should be enhanced.</p>
 *
 * @author Abe White
 */
@Entity
public class QueryTest4 {

    public int num3 = 0;
    @OneToOne(cascade = { CascadeType.ALL })
    public QueryTest2 oneToOne2 = null;

    @OneToOne(cascade = { CascadeType.ALL })
    public QueryTest2 oneToOne3 = null;

    @ManyToMany(cascade = { CascadeType.ALL })
    public Collection<QueryTest2> manyToMany3 = null;

    public int getNum3() {
        return num3;
    }

    public void setNum3(int val) {
        num3 = val;
    }

    public void setOneToOne2(QueryTest2 qt2) {
        oneToOne2 = qt2;
    }

    public void setOneToOne3(QueryTest2 qt2) {
        oneToOne3 = qt2;
    }

    public void setManyToMany3(Collection<QueryTest2> val) {
        manyToMany3 = val;
    }
}
