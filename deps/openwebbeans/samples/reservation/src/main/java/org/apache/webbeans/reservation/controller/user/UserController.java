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
package org.apache.webbeans.reservation.controller.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.bindings.EntityManagerQualifier;
import org.apache.webbeans.reservation.bindings.intercep.Transactional;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.entity.Reservation;
import org.apache.webbeans.reservation.entity.User;
import org.apache.webbeans.reservation.model.ReservationModel;

@RequestScoped
public class UserController
{
    private @Inject @EntityManagerQualifier EntityManager entityManager;
    
    private @Inject @ApplicationLog Log logger;

    public UserController()
    {
        
    }
    
    @PostConstruct
    public void postConstruct()
    {
        System.out.println("Post Construct Sample .... " + UserController.class.getName() + " is instantiated");
        
    }
    
    @PreDestroy
    public void preDestroy()
    {
        System.out.println("Pre Destroy Construct Sample .... " + UserController.class.getName() + " is destroyed by the container");
    }
    
    public User getUser(int id)
    {
        User user = this.entityManager.find(User.class, id);
        
        return user;
    }
    
    @Transactional
    public void updateUserInfo(int userId, String name, String surname, int age, String userName, String password)
    {
        logger.debug("Updating user with id : " + userId);
        
        User user = this.entityManager.find(User.class, userId);
        
        user.setName(name);
        user.setSurname(surname);
        user.setAge(age);
        user.setUserName(userName);
        user.setPassword(password);
    }
    
    @Transactional
    public void addReservation(Map<String, ReservationModel> reservations, int userId)
    {
        User user = this.entityManager.find(User.class, userId);
        
        Set<String> keys = reservations.keySet();
        
        for(String item : keys)
        {
            ReservationModel rm = reservations.get(item);
            SelectItem si = rm.getItem();
            Hotel hotel = this.entityManager.find(Hotel.class, si.getValue());
            
            Reservation reservation = new Reservation();
            
            user.addHotel(reservation);
            
            reservation.setHotel(hotel);
            reservation.setReservationDate(rm.getDate());
            
            this.entityManager.persist(reservation);            
        }
                
    }
    
    @Transactional
    public void deleteReservation(int reservsitonId)
    {
        Reservation res = this.entityManager.find(Reservation.class, reservsitonId);
        User user = res.getUser();
        
        user.getReservations().remove(res);
        
        this.entityManager.remove(res);
    }
    
    public List<Reservation> getReservations(int id)
    {
        User user = this.entityManager.find(User.class, id);
        
        Set<Reservation> res = user.getReservations();
        
        List<Reservation> l = new ArrayList<Reservation>();
        for(Reservation r : res)
        {
            l.add(r);
        }
        
        return l;
    }
}
