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
package org.apache.openjpa.persistence.proxy.entities;

import java.io.Serializable;

public interface IAddress extends Serializable {

	public String getCity();

	public void setCity(String city);

	public String getCountry();

	public void setCountry(String country);

	public String getLine1();

	public void setLine1(String line1);

	public String getLine2();

	public void setLine2(String line2);

	public String getState();

	public void setState(String state);

	public String getZipCode();

	public void setZipCode(String zipCode);

}
