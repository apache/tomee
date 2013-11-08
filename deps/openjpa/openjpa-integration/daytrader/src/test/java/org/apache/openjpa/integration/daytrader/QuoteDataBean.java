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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

// import org.apache.geronimo.samples.daytrader.util.Log;
// import org.apache.geronimo.samples.daytrader.util.TradeConfig;

@Entity(name = "quoteejb")
@Table(name = "quoteejb")
@NamedQueries({
    @NamedQuery(name = "quoteejb.allQuotes",query = "SELECT q FROM quoteejb q"),
//    @NamedQuery(name = "quoteejb.quotesByChange",query = "SELECT q FROM quoteejb q WHERE q.symbol LIKE 's:1__' ORDER BY q.change1 DESC"),
    @NamedQuery(name = "quoteejb.quotesByChange",query = "SELECT q FROM quoteejb q WHERE q.symbol LIKE 's:1__' ORDER BY q.change1 "),
    @NamedQuery(name = "quoteejb.findByLow", query = "SELECT q FROM quoteejb q WHERE q.low = :low"),
    @NamedQuery(name = "quoteejb.findByOpen1", query = "SELECT q FROM quoteejb q WHERE q.open1 = :open1"),
    @NamedQuery(name = "quoteejb.findByVolume", query = "SELECT q FROM quoteejb q WHERE q.volume = :volume"),
    @NamedQuery(name = "quoteejb.findByPrice", query = "SELECT q FROM quoteejb q WHERE q.price = :price"),
    @NamedQuery(name = "quoteejb.findByHigh", query = "SELECT q FROM quoteejb q WHERE q.high = :high"),
    @NamedQuery(name = "quoteejb.findByCompanyname", query = "SELECT q FROM quoteejb q WHERE q.companyName = :companyname"),
    @NamedQuery(name = "quoteejb.findBySymbol", query = "SELECT q FROM quoteejb q WHERE q.symbol = :symbol"),
    @NamedQuery(name = "quoteejb.findByChange1", query = "SELECT q FROM quoteejb q WHERE q.change1 = :change1")
})
@NamedNativeQueries({
    // @NamedNativeQuery(name="quoteejb.quoteForUpdate", query="select * from quoteejb q where q.symbol=? for update",resultClass=org.apache.geronimo.samples.daytrader.beans.QuoteDataBean.class)
    @NamedNativeQuery(name="quoteejb.quoteForUpdate", query="select * from quoteejb q where q.symbol=? for update",resultClass=org.apache.openjpa.integration.daytrader.QuoteDataBean.class)
})
public class QuoteDataBean implements Serializable {

    private static final long serialVersionUID = 124109406376061341L;

    /* Accessor methods for persistent fields */

    @Id
    @Column(name = "SYMBOL", nullable = false)
    private String symbol;          /* symbol */
    
    @Column(name = "COMPANYNAME")
    private String companyName;     /* companyName */
    
    @Column(name = "VOLUME", nullable = false)
    private double volume;          /* volume */
    
    @Column(name = "PRICE")
    private BigDecimal price;       /* price */
    
    @Column(name = "OPEN1")
    private BigDecimal open1;       /* open1 price */
    
    @Column(name = "LOW")
    private BigDecimal low;         /* low price */
    
    @Column(name = "HIGH")
    private BigDecimal high;        /* high price */
    
    @Column(name = "CHANGE1", nullable = false)
    private double change1;         /* price change */
    
    /* @OneToMany(mappedBy = "quote")
    private Collection<OrderDataBean> orders;
    */
    
//    @Version
//    private Integer optLock;

    /* Accessor methods for relationship fields are not kept in the DataBean */
    
    public QuoteDataBean() {
    }

    public QuoteDataBean(String symbol, String companyName, double volume,
            BigDecimal price, BigDecimal open, BigDecimal low,
            BigDecimal high, double change) {
        setSymbol(symbol);
        setCompanyName(companyName);
        setVolume(volume);
        setPrice(price);
        setOpen(open);
        setLow(low);
        setHigh(high);
        setChange(change);
    }

    public static QuoteDataBean getRandomInstance() {
        return new QuoteDataBean(
                TradeConfig.rndSymbol(),                 //symbol
                TradeConfig.rndSymbol() + " Incorporated",         //Company Name
                TradeConfig.rndFloat(100000),            //volume
                TradeConfig.rndBigDecimal(1000.0f),     //price
                TradeConfig.rndBigDecimal(1000.0f),     //open1
                TradeConfig.rndBigDecimal(1000.0f),     //low
                TradeConfig.rndBigDecimal(1000.0f),     //high
                TradeConfig.rndFloat(100000)            //volume
        );
    }

    //Create a "zero" value quoteDataBean for the given symbol
    public QuoteDataBean(String symbol) {
        setSymbol(symbol);
    }

    public String toString() {
        return "\n\tQuote Data for: " + getSymbol()
                + "\n\t\t companyName: " + getCompanyName()
                + "\n\t\t      volume: " + getVolume()
                + "\n\t\t       price: " + getPrice()
                + "\n\t\t        open1: " + getOpen()
                + "\n\t\t         low: " + getLow()
                + "\n\t\t        high: " + getHigh()
                + "\n\t\t      change1: " + getChange()
                ;
    }

    public String toHTML() {
        return "<BR>Quote Data for: " + getSymbol()
                + "<LI> companyName: " + getCompanyName() + "</LI>"
                + "<LI>      volume: " + getVolume() + "</LI>"
                + "<LI>       price: " + getPrice() + "</LI>"
                + "<LI>        open1: " + getOpen() + "</LI>"
                + "<LI>         low: " + getLow() + "</LI>"
                + "<LI>        high: " + getHigh() + "</LI>"
                + "<LI>      change1: " + getChange() + "</LI>"
                ;
    }

    public void print() {
        // Log.log(this.toString());
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOpen() {
        return open1;
    }

    public void setOpen(BigDecimal open) {
        this.open1 = open;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public double getChange() {
        return change1;
    }

    public void setChange(double change) {
        this.change1 = change;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.symbol != null ? this.symbol.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof QuoteDataBean)) {
            return false;
        }
        QuoteDataBean other = (QuoteDataBean)object;
        if (this.symbol != other.symbol && (this.symbol == null || !this.symbol.equals(other.symbol))) return false;
        return true;
    }
}
