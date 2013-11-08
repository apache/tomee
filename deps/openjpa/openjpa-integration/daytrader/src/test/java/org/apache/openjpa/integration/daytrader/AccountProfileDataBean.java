/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.integration.daytrader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

// import org.apache.geronimo.samples.daytrader.util.Log;
// import org.apache.geronimo.samples.daytrader.util.TradeConfig;

@Entity(name = "accountprofileejb")
@Table(name = "accountprofileejb")
@NamedQueries( {
        @NamedQuery(name = "accountprofileejb.findByAddress", query = "SELECT a FROM accountprofileejb a WHERE a.address = :address"),
        @NamedQuery(name = "accountprofileejb.findByPasswd", query = "SELECT a FROM accountprofileejb a WHERE a.passwd = :passwd"),
        @NamedQuery(name = "accountprofileejb.findByUserid", query = "SELECT a FROM accountprofileejb a WHERE a.userID = :userid"),
        @NamedQuery(name = "accountprofileejb.findByEmail", query = "SELECT a FROM accountprofileejb a WHERE a.email = :email"),
        @NamedQuery(name = "accountprofileejb.findByCreditcard", query = "SELECT a FROM accountprofileejb a WHERE a.creditCard = :creditcard"),
        @NamedQuery(name = "accountprofileejb.findByFullname", query = "SELECT a FROM accountprofileejb a WHERE a.fullName = :fullname")
    })
public class AccountProfileDataBean implements java.io.Serializable {

    private static final long serialVersionUID = 4243248264186612106L;

    /* Accessor methods for persistent fields */

    @Id
    @Column(name = "USERID", nullable = false)
    private String userID;              /* userID */
    
    @Column(name = "PASSWD")
    private String passwd;              /* password */
    
    @Column(name = "FULLNAME")
    private String fullName;            /* fullName */
    
    @Column(name = "ADDRESS")
    private String address;             /* address */
    
    @Column(name = "EMAIL")
    private String email;               /* email */
    
    @Column(name = "CREDITCARD")
    private String creditCard;          /* creditCard */
    
    @OneToOne(mappedBy="profile", fetch=FetchType.LAZY)
    private AccountDataBean account;

//    @Version
//    private Integer optLock;

    public AccountProfileDataBean() {
    }

    public AccountProfileDataBean(String userID,
            String password,
            String fullName,
            String address,
            String email,
            String creditCard) {
        setUserID(userID);
        setPassword(password);
        setFullName(fullName);
        setAddress(address);
        setEmail(email);
        setCreditCard(creditCard);
    }

    public static AccountProfileDataBean getRandomInstance() {
        return new AccountProfileDataBean(
                TradeConfig.rndUserID(),                        // userID
                TradeConfig.rndUserID(),                        // passwd
                TradeConfig.rndFullName(),                      // fullname
                TradeConfig.rndAddress(),                       // address
                TradeConfig.rndEmail(TradeConfig.rndUserID()),  //email
                TradeConfig.rndCreditCard()                     // creditCard
        );
    }

    public String toString() {
        return "\n\tAccount Profile Data for userID:" + getUserID()
                + "\n\t\t   passwd:" + getPassword()
                + "\n\t\t   fullName:" + getFullName()
                + "\n\t\t    address:" + getAddress()
                + "\n\t\t      email:" + getEmail()
                + "\n\t\t creditCard:" + getCreditCard()
                ;
    }

    public String toHTML() {
        return "<BR>Account Profile Data for userID: <B>" + getUserID() + "</B>"
                + "<LI>   passwd:" + getPassword() + "</LI>"
                + "<LI>   fullName:" + getFullName() + "</LI>"
                + "<LI>    address:" + getAddress() + "</LI>"
                + "<LI>      email:" + getEmail() + "</LI>"
                + "<LI> creditCard:" + getCreditCard() + "</LI>"
                ;
    }

    public void print() {
        // Log.log(this.toString());
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return passwd;
    }

    public void setPassword(String password) {
        this.passwd = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public AccountDataBean getAccount() {
        return account;
    }

    public void setAccount(AccountDataBean account) {
        this.account = account;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.userID != null ? this.userID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccountProfileDataBean)) {
            return false;
        }
        AccountProfileDataBean other = (AccountProfileDataBean)object;
        if (this.userID != other.userID && (this.userID == null || !this.userID.equals(other.userID))) return false;
        return true;
    }
}
