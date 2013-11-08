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

import java.util.Date;
import java.util.List;

public interface IAnnuity extends IAnnuityObject {
	
	public abstract Double getLastPaidAmt();

	public abstract void setLastPaidAmt(Double lastPaidAmt);
	
	public abstract String getAccountNumber();

	public abstract void setAccountNumber(String accountNumber);

	public abstract Double getAmount();

	public abstract void setAmount(Double amount);
	
	public abstract List<IPayout> getPayouts();
	public abstract void setPayouts(List<IPayout> payout);
	
	public abstract List<IRider> getRiders();
	public abstract void setRiders(List<IRider> riders);
	
	public abstract String getAnnuityHolderId();
	public abstract void setAnnuityHolderId(String annuityHolderId);
	
	public abstract List<IPayor> getPayors();
	public abstract void setPayors(List<IPayor> payors);

    public abstract List<String> getComments();
    public abstract void setComments(List<String> comments);

    public abstract Date getApprovedAt();
    public void setApprovedAt(Date approvedAt);

    public Annuity getPreviousAnnuity();
    public void setPreviousAnnuity(Annuity previousAnnuity);
}
