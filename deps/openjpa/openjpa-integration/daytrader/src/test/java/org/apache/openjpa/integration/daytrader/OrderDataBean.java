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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

// import org.apache.geronimo.samples.daytrader.util.Log;
// import org.apache.geronimo.samples.daytrader.util.TradeConfig;

@Entity(name="orderejb")
@Table(name = "orderejb")
@NamedQueries( {
    @NamedQuery(name = "orderejb.findByOrderfee", query = "SELECT o FROM orderejb o WHERE o.orderFee = :orderfee"),
    @NamedQuery(name = "orderejb.findByCompletiondate", query = "SELECT o FROM orderejb o WHERE o.completionDate = :completiondate"),
    @NamedQuery(name = "orderejb.findByOrdertype", query = "SELECT o FROM orderejb o WHERE o.orderType = :ordertype"),
    @NamedQuery(name = "orderejb.findByOrderstatus", query = "SELECT o FROM orderejb o WHERE o.orderStatus = :orderstatus"),
    @NamedQuery(name = "orderejb.findByPrice", query = "SELECT o FROM orderejb o WHERE o.price = :price"),
    @NamedQuery(name = "orderejb.findByQuantity", query = "SELECT o FROM orderejb o WHERE o.quantity = :quantity"),
    @NamedQuery(name = "orderejb.findByOpendate", query = "SELECT o FROM orderejb o WHERE o.openDate = :opendate"),
    @NamedQuery(name = "orderejb.findByOrderid", query = "SELECT o FROM orderejb o WHERE o.orderID = :orderid"),
    @NamedQuery(name = "orderejb.findByAccountAccountid", query = "SELECT o FROM orderejb o WHERE o.account.accountID = :accountAccountid"),
    @NamedQuery(name = "orderejb.findByQuoteSymbol", query = "SELECT o FROM orderejb o WHERE o.quote.symbol = :quoteSymbol"),
    // Never used query related to FK constraint on holdingejb. the FK constraint will cause EJB3 runtime mode failure. So comment it.
    //@NamedQuery(name = "orderejb.findByHoldingHoldingid", query = "SELECT o FROM orderejb o WHERE o.holding.holdingID = :holdingHoldingid"),
    @NamedQuery(name = "orderejb.closedOrders", query = "SELECT o FROM orderejb o WHERE o.orderStatus = 'closed' AND o.account.profile.userID  = :userID"),
    @NamedQuery(name = "orderejb.completeClosedOrders", query = "UPDATE orderejb o SET o.orderStatus = 'completed' WHERE o.orderStatus = 'closed' AND o.account.profile.userID  = :userID")
})
public class OrderDataBean implements Serializable
{

    private static final long serialVersionUID = 7374883697399608766L;

    @TableGenerator(
            name="orderIdGen",
            table="KEYGENEJB",
            pkColumnName="KEYNAME",
            valueColumnName="KEYVAL",
            pkColumnValue="order",
            allocationSize=1000)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="orderIdGen")
    @Column(name = "ORDERID", nullable = false)        
    private Integer orderID;            /* orderID */
    
    @Column(name = "ORDERTYPE")
    private String orderType;           /* orderType (buy, sell, etc.) */
    
    @Column(name = "ORDERSTATUS")
    private String orderStatus;         /* orderStatus (open, processing, completed, closed, cancelled) */
    
    @Column(name = "OPENDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date openDate;              /* openDate (when the order was entered) */
    
    @Column(name = "COMPLETIONDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completionDate;		/* completionDate */
    
    @Column(name = "QUANTITY", nullable = false)
    private double quantity;			/* quantity */
    
    @Column(name = "PRICE")
    private BigDecimal price;				/* price */
    
    @Column(name = "ORDERFEE")
    private BigDecimal orderFee;			/* price */
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ACCOUNT_ACCOUNTID")
    private AccountDataBean account;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="QUOTE_SYMBOL")
    private QuoteDataBean quote;
    
    // Cause sell operation failed, see JIRA DAYTRADER-63 for details.
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "HOLDING_HOLDINGID")
    // Cause sell operation failed, see JIRA DAYTRADER-63 for details.
    //@Transient    
    private HoldingDataBean holding;

//    @Version
//    private Integer optLock;

    /* Fields for relationship fields are not kept in the Data Bean */
    @Transient
    private String symbol;

    public OrderDataBean() {        
    }

    public OrderDataBean(Integer orderID,
                            String orderType,
                            String orderStatus,
                            Date openDate,
                            Date completionDate,
                            double quantity,
                            BigDecimal price,
                            BigDecimal orderFee,
                            String symbol
                            ) {
        setOrderID(orderID);
        setOrderType(orderType);
        setOrderStatus(orderStatus);
        setOpenDate(openDate);
        setCompletionDate(completionDate);
        setQuantity(quantity);
        setPrice(price);
        setOrderFee(orderFee);
        setSymbol(symbol);
    }
    
    public OrderDataBean(String orderType,
            String orderStatus,
            Date openDate,
            Date completionDate,
            double quantity,
            BigDecimal price,
            BigDecimal orderFee,
            AccountDataBean account,
            QuoteDataBean quote, HoldingDataBean holding) {
        setOrderType(orderType);
        setOrderStatus(orderStatus);
        setOpenDate(openDate);
        setCompletionDate(completionDate);
        setQuantity(quantity);
        setPrice(price);
        setOrderFee(orderFee);
        setAccount(account);
        setQuote(quote);
        setHolding(holding);
    }

    public static OrderDataBean getRandomInstance() {
        return new OrderDataBean(
            new Integer(TradeConfig.rndInt(100000)),
            TradeConfig.rndBoolean() ? "buy" : "sell",
            "open",
            new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)),
            new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)),
            TradeConfig.rndQuantity(),
            TradeConfig.rndBigDecimal(1000.0f),
            TradeConfig.rndBigDecimal(1000.0f),
            TradeConfig.rndSymbol()
        );
    }

    public String toString()
    {
        return "Order " + getOrderID()
                + "\n\t      orderType: " + getOrderType()
                + "\n\t    orderStatus: " +	getOrderStatus()
                + "\n\t       openDate: " +	getOpenDate()
                + "\n\t completionDate: " +	getCompletionDate()
                + "\n\t       quantity: " +	getQuantity()
                + "\n\t          price: " +	getPrice()
                + "\n\t       orderFee: " +	getOrderFee()
                + "\n\t         symbol: " +	getSymbol()
                ;
    }
    public String toHTML()
    {
        return "<BR>Order <B>" + getOrderID() + "</B>"
                + "<LI>      orderType: " + getOrderType() + "</LI>"
                + "<LI>    orderStatus: " +	getOrderStatus() + "</LI>"
                + "<LI>       openDate: " +	getOpenDate() + "</LI>"
                + "<LI> completionDate: " +	getCompletionDate() + "</LI>"
                + "<LI>       quantity: " +	getQuantity() + "</LI>"
                + "<LI>          price: " +	getPrice() + "</LI>"
                + "<LI>       orderFee: " +	getOrderFee() + "</LI>"
                + "<LI>         symbol: " +	getSymbol() + "</LI>"
                ;
    }

    public void print()
    {
        // Log.log( this.toString() );
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOrderFee() {
        return orderFee;
    }

    public void setOrderFee(BigDecimal orderFee) {
        this.orderFee = orderFee;
    }

    public String getSymbol() {
        if (quote != null) {
            return quote.getSymbol();
        }
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AccountDataBean getAccount() {
        return account;
    }

    public void setAccount(AccountDataBean account) {
        this.account = account;
    }

    public QuoteDataBean getQuote() {
        return quote;
    }

    public void setQuote(QuoteDataBean quote) {
        this.quote = quote;
    }

    public HoldingDataBean getHolding() {
        return holding;
    }

    public void setHolding(HoldingDataBean holding) {
        this.holding = holding;
    }

    public boolean isBuy()
    {
    	String orderType = getOrderType();
    	if ( orderType.compareToIgnoreCase("buy") == 0 )
    		return true;
    	return false;
    }

    public boolean isSell()
    {
    	String orderType = getOrderType();
    	if ( orderType.compareToIgnoreCase("sell") == 0 )
    		return true;
    	return false;
    }

    public boolean isOpen()
    {
    	String orderStatus = getOrderStatus();
    	if ( (orderStatus.compareToIgnoreCase("open") == 0) ||
	         (orderStatus.compareToIgnoreCase("processing") == 0) )
	    		return true;
    	return false;
    }

    public boolean isCompleted()
    {
    	String orderStatus = getOrderStatus();
    	if ( (orderStatus.compareToIgnoreCase("completed") == 0) ||
	         (orderStatus.compareToIgnoreCase("alertcompleted") == 0)    ||
	         (orderStatus.compareToIgnoreCase("cancelled") == 0) )
	    		return true;
    	return false;
    }

    public boolean isCancelled()
    {
    	String orderStatus = getOrderStatus();
    	if (orderStatus.compareToIgnoreCase("cancelled") == 0)
	    		return true;
    	return false;
    }


	public void cancel()
	{
		setOrderStatus("cancelled");
	}

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.orderID != null ? this.orderID.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrderDataBean)) {
            return false;
        }
        OrderDataBean other = (OrderDataBean)object;
        if (this.orderID != other.orderID && (this.orderID == null || !this.orderID.equals(other.orderID))) return false;
        return true;
    }
}

