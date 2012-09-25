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
package org.apache.webbeans.reservation.model;

import java.util.Date;

import javax.faces.model.SelectItem;

public class ReservationModel
{
    private SelectItem item;
    
    private Date date;

    public ReservationModel(SelectItem item, Date date)
    {
        this.item = item;
        this.date = date;
    }

    /**
     * @return the item
     */
    public SelectItem getItem()
    {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(SelectItem item)
    {
        this.item = item;
    }

    /**
     * @return the date
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date)
    {
        this.date = date;
    }
    
    
}
