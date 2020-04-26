/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.cmp2;

import jakarta.ejb.EntityBean;

public abstract class MovieBean implements EntityBean {

    public MovieBean() {
    }

    public Integer ejbCreate(final String director, String title, final int year) {
        this.setDirector(director);
        this.setTitle(title);
        this.setYear(year);
        return null;
    }

    public abstract java.lang.Integer getId();

    public abstract void setId(java.lang.Integer id);

    public abstract String getDirector();

    public abstract void setDirector(String director);

    public abstract String getTitle();

    public abstract void setTitle(String title);

    public abstract int getYear();

    public abstract void setYear(int year);

}
