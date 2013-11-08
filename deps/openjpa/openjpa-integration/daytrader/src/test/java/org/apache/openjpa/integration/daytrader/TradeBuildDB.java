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

import java.math.BigDecimal;

import org.apache.openjpa.lib.log.Log;

// import org.apache.geronimo.samples.daytrader.core.*;
// import org.apache.geronimo.samples.daytrader.core.direct.*;
// import org.apache.geronimo.samples.daytrader.beans.*;
// import org.apache.geronimo.samples.daytrader.util.*;

/**
 * TradeBuildDB uses operations provided by the TradeApplication to 
 *   (a) create the Database tables 
 *   (b) populate a DayTrader database without creating the tables. 
 * Specifically, a new DayTrader User population is created using
 * UserIDs of the form "uid:xxx" where xxx is a sequential number 
 * (e.g. uid:0, uid:1, etc.). New stocks are also created of the form "s:xxx",
 * again where xxx represents sequential numbers (e.g. s:1, s:2, etc.)
 */
public class TradeBuildDB {

    private TradeAction trade = null;
    
    /**
     * Re-create the DayTrader db tables and populate them OR just populate a 
     * DayTrader DB, logging to the provided output stream
     */
    public TradeBuildDB(Log log, TradeAction trade) throws Exception {
        this.trade = trade;
        // update config
        
        // always use TradeJPADirect mode

        // removed - createDBTables

        // removed - Attempt to delete all of the Trade users and Trade Quotes first
        
    }

    public void setup(int quotes, int users) {
        createQuotes(quotes);
        createAccounts(users);        
    }
    private void createQuotes(int quotes) {
        int errorCount = 0;
        String symbol, companyName;
        TradeConfig.log.info("TradeBuildDB.createQuotes(" + quotes + ")");
        for (int i = 0; i < quotes; i++) {
            symbol = "s:" + i;
            companyName = "S" + i + " Incorporated";
            try {
                QuoteDataBean quoteData = trade.createQuote(symbol, companyName,
                    new java.math.BigDecimal(TradeConfig.rndPrice()));
            } catch (Exception e) {
                if (errorCount++ >= 10) {
                    TradeConfig.log.error("createQuotes - aborting after 10 create quote errors", e);
                    throw new RuntimeException(e);
                }
            }
        }

    }
    
    private void createAccounts(int users) {
        TradeConfig.log.info("TradeBuildDB.createAccounts(" + users + ")");
        for (int i = 0; i < users; i++) {
            String userID = "uid:" + i;
            String fullname = TradeConfig.rndFullName();
            String email = TradeConfig.rndEmail(userID);
            String address = TradeConfig.rndAddress();
            String creditcard = TradeConfig.rndCreditCard();
            double initialBalance = (double) (TradeConfig.rndInt(100000)) + 200000;
            if (i == 0) {
                initialBalance = 1000000; // uid:0 starts with a cool million.
            }
            
            AccountDataBean accountData = trade.register(userID, "xxx", fullname, address,
                email, creditcard, new BigDecimal(initialBalance));

            String symbol;
            if (accountData != null) {
                // 0-MAX_HOLDING (inclusive), avg holdings per user = (MAX-0)/2
                // int holdings = TradeConfig.rndInt(TradeConfig.getMAX_HOLDINGS() + 1);
                int holdings = TradeConfig.getMAX_HOLDINGS();
                double quantity = 0;
                OrderDataBean orderData = null;
                for (int j = 0; j < holdings; j++) {
                    symbol = TradeConfig.rndSymbol();
                    quantity = TradeConfig.rndQuantity();
                    orderData = trade.buy(userID, symbol, quantity, TradeConfig.orderProcessingMode);
                }
                if (TradeConfig.log.isTraceEnabled()) {
                    TradeConfig.log.trace("createAccounts - created " + holdings + " for userID=" + userID + " order=" + orderData);
                }
            } else {
                TradeConfig.log.error("createAccounts - userID=" + userID + " already registered.");
                throw new RuntimeException("createAccounts - userID=" + userID + " already registered.");
            }
        }
    }

}
