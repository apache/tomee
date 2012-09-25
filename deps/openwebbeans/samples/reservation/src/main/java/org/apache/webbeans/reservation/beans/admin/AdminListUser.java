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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.component.html.HtmlDataTable;

import org.apache.webbeans.reservation.controller.admin.AdminController;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.entity.User;

@Named
@RequestScoped
public class AdminListUser
{
    private List<User> users = new ArrayList<User>();
    
    private @Inject @Default AdminController controller;
    
    private HtmlDataTable model;
    
    private List<Hotel> hotels = new ArrayList<Hotel>();
    
    public AdminListUser()
    {
        
    }

    public String getReservations()
    {
        User user = (User) model.getRowData();
        
        Set<Hotel> set  = this.controller.getReservationsWithUser(user.getId());
        for(Hotel hotel : set)
        {
            hotels.add(hotel);
        }
        
        return null;
    }
    
    /**
     * @return the users
     */
    public List<User> getUsers()
    {   
        this.users = this.controller.getUsers(); 
        
        return this.users; 
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<User> users)
    {
        this.users = users;
    }

    /**
     * @return the model
     */
    public HtmlDataTable getModel()
    {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(HtmlDataTable model)
    {
        this.model = model;
    }

    /**
     * @return the hotels
     */
    public List<Hotel> getHotels()
    {
        
       return hotels;

    }

    /**
     * @param hotels the hotels to set
     */
    public void setHotels(List<Hotel> hotels)
    {
        this.hotels = hotels;
    }

    
    
}
