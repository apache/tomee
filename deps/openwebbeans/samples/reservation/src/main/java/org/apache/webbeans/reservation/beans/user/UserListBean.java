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
package org.apache.webbeans.reservation.beans.user;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.component.html.HtmlDataTable;

import org.apache.webbeans.reservation.controller.user.UserController;
import org.apache.webbeans.reservation.entity.Reservation;
import org.apache.webbeans.reservation.session.SessionTracker;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named("listReservationBean")
@RequestScoped
public class UserListBean
{

    private List<Reservation> reservations = new ArrayList<Reservation>();
    
    private HtmlDataTable model = null;
    
    private @Inject @Default UserController controller;
    
    private @Inject @Default SessionTracker tracker;
    
    public UserListBean()
    {
        
    }

    public String delete()
    {
        Reservation res = (Reservation)model.getRowData();
        
        controller.deleteReservation(res.getId());
        
        JSFUtility.addInfoMessage("Reservation is succesfully delete", "");
        
        
        return null;
    }
    
    public String showReservations()
    {
        return "toReservePage";
    }
    
    /**
     * @return the reservations
     */
    public List<Reservation> getReservations()
    {
        this.reservations = this.controller.getReservations(tracker.getUser().getId());
        
        return reservations;
    }

    /**
     * @param reservations the reservations to set
     */
    public void setReservations(List<Reservation> reservations)
    {
        this.reservations = reservations;
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
    
    
}
