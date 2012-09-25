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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.SelectItem;

import org.apache.webbeans.reservation.controller.admin.AdminController;
import org.apache.webbeans.reservation.controller.user.UserController;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.model.ReservationModel;
import org.apache.webbeans.reservation.session.SessionTracker;
import org.apache.webbeans.reservation.util.JSFUtility;

@Named
@ConversationScoped
public class UserReservationBean implements Serializable
{
    private static final long serialVersionUID = -5860989760497059459L;

    private List<SelectItem> reservations = new ArrayList<SelectItem>();
    
    private @Inject @Default UserController controller;
    
    private List<Hotel> hotels = new ArrayList<Hotel>();
    
    private @Inject @Default AdminController adminController;
    
    private String reservationDate;
    
    private HtmlDataTable model;
    
    private @Inject @Default Conversation conversation;
    
    private @Inject @Default SessionTracker tracker;
        
    private Integer[] itemSelected = new Integer[0];
    
    private Map<String, ReservationModel> models = new HashMap<String, ReservationModel>();
    
    public UserReservationBean()
    {
    }

    
    public String addReservation()
    {
        if(getReservationDate() == null)
        {
            JSFUtility.addErrorMessage("Reservation date can not be empty!", "");
            return null;
        }
             
        Date date = null;
        try
        {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(getReservationDate());
            
        }
        catch(Exception e)
        {
            
            JSFUtility.addErrorMessage("Please give a date with dd/MM/yyyy", "");
            return null;
        }
        
        if(conversation.isTransient())
        {
            conversation.begin();
            
            JSFUtility.addInfoMessage("Reservation conversation with started with id : " + conversation.getId(), "");            
        }
        
        Hotel hotel = (Hotel)model.getRowData();
        
        SelectItem item = new SelectItem();
        item.setValue(hotel.getId());
        item.setLabel(hotel.getName());
        
        if(contains(item.getValue()) != null)
        {
            JSFUtility.addErrorMessage("Given hotel is already added", "");
            
            return null;
        }
                
        reservations.add(item);
        
        
        ReservationModel model = new ReservationModel(item,date);
        models.put(item.getValue().toString(), model);
        
        return null;
    }
    
    private SelectItem contains(Object id)
    {
        for(SelectItem i : reservations)
        {
            if(i.getValue().toString().equals(id.toString()))
            {
                return i;
            }
        }
        
        return null;
        
    }
    
    public String delete()
    {
        if(this.itemSelected.length == 0)
        {
            JSFUtility.addErrorMessage("Please select reservation to remove", "");
            
            return null;
        }
        
        for(Integer i : this.itemSelected)
        {
            SelectItem item = contains(i);
            if(item != null)
            {
                this.reservations.remove(item);   
                
                this.models.remove(item);
            }    
        }
        
        
        return null;
    }
    
    public String checkout()
    {
        if(conversation.isTransient())
        {
            JSFUtility.addErrorMessage("Conversation is not running! Please add hotel for reservation", "");
            this.reservations.clear();
            this.reservationDate = null;
        }
        else
        {
            this.controller.addReservation(models,tracker.getUser().getId());
            
            conversation.end();
            
            JSFUtility.addInfoMessage("Reservation are completed succesfully. Conversation with id "+conversation.getId() + " is ended ", "");
            
            this.reservations.clear();
            
            this.reservationDate = null;            
        }        
        
        return null;
    }


    /**
     * @return the reservations
     */
    public List<SelectItem> getReservations()
    {
        return reservations;
    }

    
    public String clear()
    {
        this.reservations.clear();
                
        this.reservationDate = null;
        
        if(!conversation.isTransient())
        {
            this.conversation.end();
            JSFUtility.addInfoMessage("Reservation are deleted succesfully. Conversation with id "+conversation.getId() + "is ended ", "");
        }
        
        return null;
    }

    /**
     * @param reservations the reservations to set
     */
    public void setReservations(List<SelectItem> reservations)
    {
        this.reservations = reservations;
    }


    /**
     * @return the hotels
     */
    public List<Hotel> getHotels()
    {
        this.hotels = this.adminController.getHotels();
        
        return hotels;
    }


    /**
     * @param hotels the hotels to set
     */
    public void setHotels(List<Hotel> hotels)
    {
        this.hotels = hotels;
    }


    /**
     * @return the reservationDate
     */
    public String getReservationDate()
    {
        return reservationDate;
    }


    /**
     * @param reservationDate the reservationDate to set
     */
    public void setReservationDate(String reservationDate)
    {
        this.reservationDate = reservationDate;
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
     * @return the itemSelected
     */
    public Integer[] getItemSelected()
    {
        
        return itemSelected;
    }


    /**
     * @param itemSelected the itemSelected to set
     */
    public void setItemSelected(Integer[] itemSelected)
    {
        this.itemSelected = itemSelected;
    }
    
    
}
