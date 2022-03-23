/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.deltaspike.domain;

import org.superbiz.deltaspike.domain.validation.DifferentName;
import org.superbiz.deltaspike.domain.validation.Name;
import org.superbiz.deltaspike.domain.validation.Partial;
import org.superbiz.deltaspike.domain.validation.UniqueUserName;
import org.superbiz.deltaspike.domain.validation.UserName;

import jakarta.enterprise.inject.Typed;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Table(name = "T_User")
@Entity
@NamedQuery(name = "findUserByName", query = "select u from User u where u.userName = :currentUser")
@DifferentName(groups = Partial.class)
@Typed()
public class User extends AbstractDomainObject {
    private static final long serialVersionUID = 3810638653455000233L;

    @UserName(groups = UniqueUserName.class)
    @Column(nullable = false, length = 9, unique = true)
    private String userName;

    @Size(min = 2, max = 20, message = "invalid first name")
    @NotNull
    @Column
    private String firstName;

    @Column
    @Name(message = "invalid last name")
    private String lastName;

    @Column
    private String password;

    /*
     * generated
     */

    public User() {
    }

    public User(String userName, String firstName, String lastName) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
