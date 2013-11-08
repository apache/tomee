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
package org.apache.openjpa.persistence.models.library;

import javax.persistence.*;

@Entity
@Table(name="LIBVOLUNTEER")
public class Volunteer {
    @Id
    @GeneratedValue
    private int oid;

    @Basic
    private int hours_per_week;

    @OneToOne(optional = false)
    private Borrower borrower;

    protected Volunteer() {
        // used only by OpenJPA
    }

    public Volunteer(Borrower borrower) {
        if (borrower == null)
            throw new IllegalArgumentException("borrower cannot be null");

        if (borrower.getVolunteer() != null)
            throw new IllegalArgumentException(
                    "borrower is already a volunteer");

        this.borrower = borrower;
        borrower.setVolunteer(this);
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public int getHoursPerWeek() {
        return hours_per_week;
    }

    public void setHoursPerWeek(int hours) {
        if (hours >= 0)
            hours_per_week = hours;
        else
            throw new IllegalArgumentException("hours must be >= 0");
    }

    public String toString() {
        return "volunteer [" + oid + "] \"" + borrower.getName() + "\"";
    }

    public int hashCode() {
        return oid;
    }

    /**
     * Uses the object's persistent identity value to determine equivalence.
     */
    public boolean equals(Object other) {
        // standard fare
        if (other == this)
            return true;

        // if the oid is 0, then this object is not persistent.
        // in that case, it cannot be considered equivalent to
        // other managed or unmanaged objects
        if (oid == 0)
            return false;

        if (other instanceof Volunteer) {
            Volunteer ov = (Volunteer) other;
            return oid == ov.oid;
        }

        return false;
    }
}
