/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.cmp.sample;

import javax.ejb.EntityBean;

public abstract class ActorBean implements EntityBean {

    public ActorBean() {
    }

    public Integer ejbCreate(final String firstName, final String lastName) {
        this.setFirstname(firstName);
        this.setLastname(lastName);
        return null;
    }

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract String getFirstname();

    public abstract void setFirstname(String firstname);

    public abstract String getLastname();

    public abstract void setLastname(String lastname);


}
