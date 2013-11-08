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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;

@Entity
@Table(name = "Site1")
public class Site1  extends Party1 implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String SiteName;
    private String SiteDescription;

    @OneToMany(mappedBy="site",  cascade=CascadeType.ALL, fetch=FetchType.LAZY, 
            targetEntity=Store1.class)
    private List<Store1> stores;


    public List<Store1> getStores() {
        return stores;
    }

    public void setStores(List<Store1> storeList){
        this.stores = storeList;
    }

    public void setSiteName(String s) {
        this.SiteName = s;
    }

    public String getSiteName(){
        return this.SiteName;
    }

    public void setSiteDescription(String s) {
        this.SiteDescription = s;
    }

    public String getSiteDescription() {
        return this.SiteDescription;
    }
}
