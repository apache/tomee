/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.reservation.beans.admin;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.reservation.controller.admin.AdminController;
import org.apache.webbeans.reservation.util.JSFUtility;

/**
 * Admin JSF Bean.
 */
@RequestScoped
@Named
public class AdminBean
{
    private String name;
    
    private String city;
    
    private String country;
    
    private Number star;
    
    private @Inject @Default AdminController adminController;

    /**
     * Add new hotel
     * 
     * @return navigation case
     */
    public String addNewHotel()
    {
        adminController.createNewHotel(name, star.intValue(), city, country);
        
        JSFUtility.addInfoMessage("Hotel  '" + name +  "' is successfully created", "");
        
        setCity(null);
        setCountry(null);
        setName(null);
        setStar(null);
        
        
        return null;
        
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the city
     */
    public String getCity()
    {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city)
    {
        this.city = city;
    }

    /**
     * @return the country
     */
    public String getCountry()
    {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * @return the star
     */
    public Number getStar()
    {
        return star;
    }

    /**
     * @param star the star to set
     */
    public void setStar(Number star)
    {
        this.star = star;
    }
    
    
}
