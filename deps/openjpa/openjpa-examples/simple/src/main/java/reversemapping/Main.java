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
package reversemapping;

import java.text.*;
import java.util.*;
import javax.persistence.*;


/** 
 * A simple program that uses the reverse-mapped classes from the airlines
 * schema to print out a list of schedules flightes and the 
 * projected profits from them.
 */
public class Main {

    private static void print(String msg) {
        System.out.println(msg);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // Create a new EntityManagerFactory using the System properties.
        // The "reversemapping" name will be used to configure based on the
        // corresponding name in the META-INF/persistence.xml file
        EntityManagerFactory factory = Persistence.
            createEntityManagerFactory("reversemapping",
                System.getProperties());

        // Create a new EntityManager from the EntityManagerFactory. The
        // EntityManager is the main object in the persistence API, and is
        // used to create, delete, and query objects, as well as access
        // the current transaction
        EntityManager em = factory.createEntityManager();

        List<Availability> schedule = (List<Availability>)
            em.createQuery("select avail from Availability avail "
            + "join fetch avail.flight "
            + "order by avail.flightDate asc, avail.flight.departureTime asc").
                getResultList();
       for (Availability avail : schedule) {
           Flight flight = avail.getFlight();

           // note that Availability.getFlightDate is just a DATE with no
           // time component, and Flight.getDepartureTime() is just a TIME
           // with no date component
           print(new SimpleDateFormat("MMM dd, yyyy").
                    format(avail.getFlightDate())
               + " flight "
               + flight.getFlightId() + " departs "
               + new SimpleDateFormat("hh:mm aa").
                    format(flight.getDepartureTime())
               + " from " + flight.getOrigAirport()
               + " to " + flight.getDestAirport());

            // look up the Airline reference based on the flight ID
            Airline airline = em.getReference(Airline.class,
                flight.getFlightId().substring(0, 2));
            double ratePerMile = airline.getBasicRate();
            double rate = flight.getMiles() * ratePerMile;

            int econTaken = avail.getEconomySeatsTaken();
            int businessTaken = avail.getBusinessSeatsTaken();
            int firstclassTaken = avail.getFirstclassSeatsTaken();

            double income = (econTaken * rate)
                + (businessTaken * rate)
                + (businessTaken * rate * airline.getBusinessLevelFactor())
                + (firstclassTaken * rate)
                + (firstclassTaken * rate * airline.getFirstclassLevelFactor());

            int seatsTaken = econTaken + businessTaken + firstclassTaken;
            int totalSeats = airline.getEconomySeats()
                    + airline.getBusinessSeats()
                    + airline.getFirstclassSeats();
            double percentFull = (double) seatsTaken / (double) totalSeats;

            print("  income from flight: "
                + NumberFormat.getCurrencyInstance().format(income)
                + " with " + seatsTaken + " seats taken ("
                + NumberFormat.getPercentInstance().format(percentFull)
                + " full)");

            double gallonsPerMile = 2.0d; // approx for a small plane
            double totalGallons = gallonsPerMile * flight.getMiles();
            double costPerGallon = 0.50d; // approx 2006 prices
            double totalFuelCost = totalGallons * costPerGallon;
            print("  fuel cost of flight over "
                + NumberFormat.getNumberInstance().format(flight.getMiles())
                + " miles: "
                + NumberFormat.getCurrencyInstance().format(totalFuelCost));

            double totalCost = totalFuelCost;

            print("  total profit: " + NumberFormat.getCurrencyInstance().
                format(income - totalCost));
       } 

        // Again, it is always good to clean up after ourselves
        em.close();

        factory.close();
    }
}
