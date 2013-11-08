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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Store")
public class Store extends Party implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String StoreName;
    private String StoreDescription;

    @ManyToOne( fetch = FetchType.LAZY,  cascade = CascadeType.ALL, 
            targetEntity=Site.class)
    @JoinColumn(name = "Store.SiteId",
            referencedColumnName="site.PartyId", nullable = false, 
            insertable = true, updatable = true)
    private Site site;

    private Long SiteId;

    public Site getSite() {
        return site;
    }

    public void setSite(Site s) {
        this.site = s;

    }

    public void setStoreName(String s) {
        this.StoreName = s;
    }

    public String getStoreName() {
        return this.StoreName;
    }

    public void setStoreDescription(String s){
        this.StoreDescription = s;
    }

    public String getStoreDescription(){
        return this.StoreDescription;
    } 


    public void setSiteId(Long pid) {
        this.SiteId = pid;
    }

    public Long getSiteId() {
        return this.SiteId;
    }

}
