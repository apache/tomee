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
package org.apache.openjpa.persistence.common.apps;

import javax.persistence.*;

@Entity
public class Address
{
	@Basic
	@Column(length=50)
	private String streetAd;

	@Basic
	@Column(length=50)
	private String city;

	@Basic
	@Column(length=50)
	private String country;

	@Basic
	@Column(length=25)
	private String zipcode;

	@OneToOne(mappedBy="address")
	private CompUser user;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	public Address(){}

    public Address(String streetAd, String city, String country, String zipcode)
	{
		this.streetAd = streetAd;
		this.city = city;
		this.country = country;
		this.zipcode = zipcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getId() {
		return id;
	}

	public String getStreetAd() {
		return streetAd;
	}

	public void setStreetAd(String streetAd) {
		this.streetAd = streetAd;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
}
