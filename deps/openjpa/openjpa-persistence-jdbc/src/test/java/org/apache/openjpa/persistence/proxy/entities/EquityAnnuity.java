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

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "EQUITY")
public class EquityAnnuity extends Annuity implements IEquityAnnuity {

	private static final long serialVersionUID = -7227462924769151013L;

	private String fundNames;

	private Double indexRate;

	@Column(name="FUND_NAMES")
	public String getFundNames() {
		return fundNames;
	}

	public void setFundNames(String fundNames) {
		this.fundNames = fundNames;
	}

	@Column(name="INDEX_RATE")
	public Double getIndexRate() {
		return indexRate;
	}

    public void setIndexRate(Double indexRate) {
        if (indexRate != null) {
            DecimalFormat df = new DecimalFormat("#.##");
            try
            {
                // parse back via the DateFormat because countries might use ',' as comma separator
                this.indexRate= df.parse(df.format(indexRate)).doubleValue();
            }
            catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        }
        else {
            this.indexRate = null;
        }
    }

}
