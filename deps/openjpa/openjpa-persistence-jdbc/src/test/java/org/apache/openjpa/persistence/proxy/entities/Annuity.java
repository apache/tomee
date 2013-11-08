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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@SuppressWarnings("serial")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="DTYPE", discriminatorType=DiscriminatorType.STRING)
@DiscriminatorValue(value="ANNUITY")
@AttributeOverride(name="lastUpdateDate", column=@Column(name="LAST_UPDATE_TS"))
public class Annuity extends AnnuityPersistebleObject implements IAnnuity {
    
    private Double lastPaidAmt; 
    private String AccountNumber;       
    private Double amount;
    private String annuityHolderId;
    private List<IPayout> payouts = new ArrayList<IPayout>();
    private List<IRider> riders = new ArrayList<IRider>();
    private List<IPayor> payors = new ArrayList<IPayor>();
    private List<String> comments;
    private Date approvedAt;
    
    private Annuity previousAnnuity;
    public Annuity(){
    }

    @Column(name="LAST_PAID_AMT")
    public Double getLastPaidAmt() {
        return lastPaidAmt;
    }
    public void setLastPaidAmt(Double lastPaidAmt) {
        this.lastPaidAmt = lastPaidAmt;
        if (this.lastPaidAmt != null) {
            DecimalFormat df = new DecimalFormat("#.##");
            this.lastPaidAmt= new Double(df.format(lastPaidAmt));
        }
    }
    
    @Column(name="ACCOUNT_NUMBER")
    public String getAccountNumber() {
        return AccountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }
    
    @Column(name="AMOUNT")
    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
        if (this.amount != null) {
            DecimalFormat df = new DecimalFormat("#.##");
            this.amount = new Double(df.format(amount));
        }
    }

    @Column(name="FK_ANNUITY_HOLDER_ID")
    public String getAnnuityHolderId() {
        return this.annuityHolderId;
    }
    public void setAnnuityHolderId(String annuityHolderId) {
        this.annuityHolderId = annuityHolderId;
        
    }
    
    @ManyToMany(targetEntity=Payor.class, 
            fetch=FetchType.EAGER)          
    @JoinTable(name="ANNUITY_PAYOR", 
            joinColumns={@JoinColumn(name="FK_ANNUITY_ID")}, 
            inverseJoinColumns={@JoinColumn(name="FK_PAYOR_ID")})
    public List<IPayor> getPayors() {
        return this.payors;
    }
    public void setPayors(List<IPayor> payors) {
        this.payors = payors;
        
    }
    
    @OneToMany(targetEntity=Payout.class,       
            mappedBy="annuity", 
            fetch=FetchType.EAGER)  
    public List<IPayout> getPayouts() {
        return this.payouts;
    }
    public void setPayouts(List<IPayout> payouts) {
        this.payouts = payouts;     
    }

    @OneToMany(cascade={CascadeType.ALL}, 
            targetEntity=Rider.class, 
            fetch=FetchType.EAGER)
    @JoinTable(name="ANNUITY_RIDER", 
            joinColumns={@JoinColumn(name="FK_ANNUITY_ID")}, 
            inverseJoinColumns={@JoinColumn(name="FK_RIDER_ID")})   
    public List<IRider> getRiders() {
        return this.riders;
    }
    public void setRiders(List<IRider> riders) {
        this.riders = riders;
    }

    @ElementCollection
    public List<String> getComments() {
        return comments;
    }
    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    @Temporal(TemporalType.DATE)
    public Date getApprovedAt() {
        return approvedAt;
    }
    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }

    @OneToOne
    public Annuity getPreviousAnnuity() {
        return previousAnnuity;
    }
    public void setPreviousAnnuity(Annuity previousAnnuity) {
        this.previousAnnuity = previousAnnuity;
    }
}
