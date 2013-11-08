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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

//import org.apache.geronimo.samples.daytrader.util.Log;
//import org.apache.geronimo.samples.daytrader.util.TradeConfig;

@Entity(name = "accountejb")
@Table(name = "accountejb")
@NamedQueries( {
        @NamedQuery(name = "accountejb.findByCreationdate", query = "SELECT a FROM accountejb a WHERE a.creationDate = :creationdate"),
        @NamedQuery(name = "accountejb.findByOpenbalance", query = "SELECT a FROM accountejb a WHERE a.openBalance = :openbalance"),
        @NamedQuery(name = "accountejb.findByLogoutcount", query = "SELECT a FROM accountejb a WHERE a.logoutCount = :logoutcount"),
        @NamedQuery(name = "accountejb.findByBalance", query = "SELECT a FROM accountejb a WHERE a.balance = :balance"),
        @NamedQuery(name = "accountejb.findByAccountid", query = "SELECT a FROM accountejb a WHERE a.accountID = :accountid"),
        @NamedQuery(name = "accountejb.findByAccountid_eager", query = "SELECT a FROM accountejb a LEFT JOIN FETCH a.profile WHERE a.accountID = :accountid"),
        @NamedQuery(name = "accountejb.findByAccountid_eagerholdings", query = "SELECT a FROM accountejb a LEFT JOIN FETCH a.holdings WHERE a.accountID = :accountid"),
        @NamedQuery(name = "accountejb.findByLastlogin", query = "SELECT a FROM accountejb a WHERE a.lastLogin = :lastlogin"),
        @NamedQuery(name = "accountejb.findByLogincount", query = "SELECT a FROM accountejb a WHERE a.loginCount = :logincount")
    })
public class AccountDataBean implements Serializable {

    private static final long serialVersionUID = -7478907635822129745L;

    /* Accessor methods for persistent fields */
    @TableGenerator(
            name="accountIdGen",
            table="KEYGENEJB",
            pkColumnName="KEYNAME",
            valueColumnName="KEYVAL",
            pkColumnValue="account",
            allocationSize=1000)
    
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="accountIdGen")
    @Column(name = "ACCOUNTID", nullable = false)
    private Integer accountID;              /* accountID */
    
    @Column(name = "LOGINCOUNT", nullable = false)
    private int loginCount;                 /* loginCount */
    
    @Column(name = "LOGOUTCOUNT", nullable = false)
    private int logoutCount;                /* logoutCount */
    
    @Column(name = "LASTLOGIN")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;                 /* lastLogin Date */
    
    @Column(name = "CREATIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;              /* creationDate */
    
    @Column(name = "BALANCE", precision=12, scale=2)
    private BigDecimal balance;             /* balance */
    
     @Column(name = "OPENBALANCE", precision=12, scale=2)
    private BigDecimal openBalance;         /* open balance */
    
    @OneToMany(mappedBy = "account", fetch=FetchType.LAZY)
    private Collection<OrderDataBean> orders;
    
    @OneToMany(mappedBy = "account", fetch=FetchType.LAZY)
    private Collection<HoldingDataBean> holdings;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PROFILE_USERID")
    private AccountProfileDataBean profile;

//    @Version
//    private Integer optLock;

    /* Accessor methods for relationship fields are only included for the AccountProfile profileID */
    @Transient
    private String profileID;

    public AccountDataBean() {
    }

    public AccountDataBean(Integer accountID,
            int loginCount,
            int logoutCount,
            Date lastLogin,
            Date creationDate,
            BigDecimal balance,
            BigDecimal openBalance,
            String profileID) {
        setAccountID(accountID);
        setLoginCount(loginCount);
        setLogoutCount(logoutCount);
        setLastLogin(lastLogin);
        setCreationDate(creationDate);
        setBalance(balance);
        setOpenBalance(openBalance);
        setProfileID(profileID);
    }

    public AccountDataBean(int loginCount,
            int logoutCount,
            Date lastLogin,
            Date creationDate,
            BigDecimal balance,
            BigDecimal openBalance,
            String profileID) {
        setLoginCount(loginCount);
        setLogoutCount(logoutCount);
        setLastLogin(lastLogin);
        setCreationDate(creationDate);
        setBalance(balance);
        setOpenBalance(openBalance);
        setProfileID(profileID);
    }

    public static AccountDataBean getRandomInstance() {
        return new AccountDataBean(new Integer(TradeConfig.rndInt(100000)), //accountID
                TradeConfig.rndInt(10000), //loginCount
                TradeConfig.rndInt(10000), //logoutCount
                new java.util.Date(), //lastLogin
                new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), //creationDate
                TradeConfig.rndBigDecimal(1000000.0f), //balance
                TradeConfig.rndBigDecimal(1000000.0f), //openBalance
                TradeConfig.rndUserID() //profileID
        );
    }

    public String toString() {
        return "\n\tAccount Data for account: " + getAccountID()
                + "\n\t\t   loginCount:" + getLoginCount()
                + "\n\t\t  logoutCount:" + getLogoutCount()
                + "\n\t\t    lastLogin:" + getLastLogin()
                + "\n\t\t creationDate:" + getCreationDate()
                + "\n\t\t      balance:" + getBalance()
                + "\n\t\t  openBalance:" + getOpenBalance()
                + "\n\t\t    profileID:" + getProfileID()
                ;
    }

    public String toHTML() {
        return "<BR>Account Data for account: <B>" + getAccountID() + "</B>"
                + "<LI>   loginCount:" + getLoginCount() + "</LI>"
                + "<LI>  logoutCount:" + getLogoutCount() + "</LI>"
                + "<LI>    lastLogin:" + getLastLogin() + "</LI>"
                + "<LI> creationDate:" + getCreationDate() + "</LI>"
                + "<LI>      balance:" + getBalance() + "</LI>"
                + "<LI>  openBalance:" + getOpenBalance() + "</LI>"
                + "<LI>    profileID:" + getProfileID() + "</LI>"
                ;
    }

    public void print() {
        // Log.log(this.toString());
    }

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public int getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    /* Disabled for D185273
     public String getUserID() {
         return getProfileID();
     }
     */

    public Collection<OrderDataBean> getOrders() {
        return orders;
    }

    public void setOrders(Collection<OrderDataBean> orders) {
        this.orders = orders;
    }
    
    public Collection<HoldingDataBean> getHoldings() {
        return holdings;
    }

    public void setHoldings(Collection<HoldingDataBean> holdings) {
        this.holdings = holdings;
    }

    public AccountProfileDataBean getProfile() {
        return profile;
    }

    public void setProfile(AccountProfileDataBean profile) {
        this.profile = profile;
    }

    public void login(String password) {
        AccountProfileDataBean profile = getProfile();
        if ((profile == null) || (profile.getPassword().equals(password) == false)) {
            String error = "AccountBean:Login failure for account: " + getAccountID() +
                    ((profile == null) ? "null AccountProfile" :
                            "\n\tIncorrect password-->" + profile.getUserID() + ":" + profile.getPassword());
            throw new RuntimeException(error);
            
        }

        setLastLogin(new Timestamp(System.currentTimeMillis()));
        setLoginCount(getLoginCount() + 1);
    }

    public void logout() {
        setLogoutCount(getLogoutCount() + 1);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.accountID != null ? this.accountID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AccountDataBean)) {
            return false;
        }
        AccountDataBean other = (AccountDataBean)object;
        if (this.accountID != other.accountID && (this.accountID == null || !this.accountID.equals(other.accountID))) return false;
        return true;
    }
}
