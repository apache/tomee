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
package org.apache.webbeans.reservation.controller.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.validator.GenericValidator;
import org.apache.webbeans.reservation.bindings.ApplicationLog;
import org.apache.webbeans.reservation.bindings.EntityManagerQualifier;
import org.apache.webbeans.reservation.bindings.intercep.Transactional;
import org.apache.webbeans.reservation.entity.Hotel;
import org.apache.webbeans.reservation.entity.Reservation;
import org.apache.webbeans.reservation.entity.User;

/**
 * Contains admin related activities.
 */
@Named
@RequestScoped
public class AdminController
{
    private @Inject @ApplicationLog Log logger;
    
    private @Inject @EntityManagerQualifier EntityManager entityManager;
    
    @Transactional
    public void createNewHotel(String name, int star, String city, String country)
    {        
        
        if(GenericValidator.isBlankOrNull(name) ||
                GenericValidator.isBlankOrNull(city) ||
                GenericValidator.isBlankOrNull(country))
        {
            logger.debug("Some of the parameters are missing to define hotel.");
            
            return;
        }
        
        Hotel hotel = new Hotel();
        hotel.setCity(city);
        hotel.setCountry(country);
        hotel.setName(name);
        hotel.setStar(star);
        
        entityManager.persist(hotel);
        
    }
    
    @SuppressWarnings("unchecked")
    public List<Hotel> getHotels()
    {
        Query query = this.entityManager.createQuery("select h from Hotel h");
        
        return  (List<Hotel>)query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<User> getUsers()
    {
        Query query = this.entityManager.createQuery("select u from User u");
        
        return  (List<User>)query.getResultList();
        
    }

    /**
     * Returns hotel with given id.
     * 
     * @param id hotel id
     * @return hotel
     */
    public Hotel getHotelWithId(int id)
    {
        Hotel hotel = this.entityManager.find(Hotel.class, id);
        
        return hotel;
    }
    
    @SuppressWarnings("unchecked")
    public List<User> getReservationsWithHotel(int hotelId)
    {
        Query query = this.entityManager.createQuery("select u from User u join fetch u.reservations r where r.id=:id");
        query.setParameter("id", hotelId);
        
        List<User> users = query.getResultList();
        
        return users;
    }
    
    public Set<Hotel> getReservationsWithUser(int userId)
    {
        Query query = this.entityManager.createQuery("select u from User u where u.id=:id");
        query.setParameter("id", userId);
        
        User user = (User)query.getSingleResult();
        
        Set<Hotel> hotels = new HashSet<Hotel>();
        
        Set<Reservation> reservations = user.getReservations();
        
        for(Reservation reserve : reservations)
        {
            hotels.add(reserve.getHotel());
        }
        
        return hotels;
    }
    
    
    @Transactional
    public void updateHotel(int id, String name, int star, String city, String country)
    {
        Hotel hotel = this.entityManager.find(Hotel.class, id);
        
        hotel.setName(name);
        hotel.setStar(star);
        hotel.setCountry(country);
        hotel.setCity(city);
    }
    
    @Transactional
    @SuppressWarnings("unchecked")
    public void deleteHotel(int id)
    {
        Hotel hotel = this.entityManager.find(Hotel.class, id);
        
        this.entityManager.remove(hotel);
        
        Query query = this.entityManager.createQuery("select r from Reservation r where r.hotel.id=:id");
        query.setParameter("id",hotel.getId());
        
        List<Reservation> res = query.getResultList();
        
        for(Reservation r : res)
        {
            this.entityManager.remove(r);
        }
        
    }
}
