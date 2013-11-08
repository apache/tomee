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
package org.apache.openjpa.trader.service;

import java.sql.Timestamp;
import java.util.List;

import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

/**
 * A service to place offer to {@link Bid buy} and {@link Ask sell} stocks, 
 * matches asks to bids and registers trades.
 * 
 * @author Pinaki Poddar
 *
 */
public interface TradingService {
    /**
     * A query to find symbols of all stocks.
     */
    public static final String GET_ALL_STOCKS = "select s from Stock s";
    
    /**
     * A query to match asks to a given bid.
     */
    public static final String MATCH_BID  = "select new Match(a,b) from Ask a, Bid b " 
                      + "where b = :bid and a.stock.symbol = b.stock.symbol " 
                      + "and a.price <= b.price and a.volume >= b.volume " 
                      + "and NOT(a.seller = b.buyer) and a.trade is NULL and b.trade is NULL";
//                      + "order by a.price ASC";
    
    /**
     * A query to match bids of a given ask.
     */
    public static final String MATCH_ASK  = "select new Match(a,b) from Ask a, Bid b " 
                      + "where a = :ask  and a.stock.symbol = b.stock.symbol " 
                      + "and a.price <= b.price and a.volume >= b.volume " 
                      + "and NOT(a.seller = b.buyer) and a.trade is NULL and b.trade is NULL"; 
//                      + "order by b.price DESC";
    
    /**
     * A query to find a trader by his/her name.
     */
    public static final String QUERY_TRADER_BY_NAME = "select t from Trader t where t.name=:name";
    
    
    /**
     * A query to find all trades in a given period.
     */
    public static final String QUERY_TRADE_BY_PERIOD = "select t from Trade t where t.id between (:from, :to)";
    
    
    public static final String DEFAULT_UNIT_NAME = "exchange-local";
    
    /**
     * Gets the list of stocks registered with this service.
     */
    List<Stock> getStocks();
    
    /**
     * Logs in a trader of given name.
     * 
     * @param trader
     * @return
     */
    Trader login(String trader);
    
    /**
     * The given Trader asks (offers to sell) the given stock at given price. 
     * @param trader
     * @param stock
     * @param volume
     * @param price
     * @return
     */
    Ask ask(Trader trader, Stock stock, int volume, double price);
    
    
    /**
     * The given Trader bids (offers to buy) the given stock at given price. 
     * @param trader
     * @param stock
     * @param volume
     * @param price
     * @return
     */
    Bid bid(Trader trader, Stock stock, int volume, double price);
    
    /**
     * Matches existing asks to the given bid.
     * @param bid the bid to be matched
     * @return possible (uncommitted) trades matching the bids
     */
    List<Match> matchBid(Bid bid);
    
    /**
     * Matches existing bids to the given ask.
     * @param ask the ask to be matched with the bids
     * @return possible (uncommitted) trades matching the given ask
     */
    List<Match> matchAsk(Ask ask);
    
    Tradable withdraw(Tradable t);
    Tradable refresh(Tradable t);
    
    void close();
    /**
     * Registers the given trade.
     * @param trade
     */
    Trade trade(Match match);
    
    /**
     * Gets the trades executed between the given time periods.
     * @param from
     * @param to
     * @return
     */
    List<Trade> getTrades(Timestamp from, Timestamp to);
    
    /**
     * Gets the trades executed by the given trader between the given time periods.
     * @param from
     * @param to
     * @return
     */
    List<Trade> getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to);
    
    /**
     * Gets the current value of the stock given its symbol.
     * @param symbol
     * @return
     */
    Stock getStock(String symbol);
    
    /**
     * Gets the statements logged since the last call.
     * @return
     */
    List<LogStatement> getLog();
    
    /**
     * Get a descriptive URI-like string for this service.
     * 
     * @return
     */
    String getServiceURI();
}
