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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;

import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Sector;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;

@SuppressWarnings("serial")
public class Exchange extends PersistenceService implements TradingService {
    private BufferedLog log;
    
    public Exchange(String unit) {
        this(unit, null);
    }
    
    public Exchange(String unit, Map<String,Object> config) {
        super(unit, false, PersistenceContextType.TRANSACTION, addLog(config));
        LogFactory serverLog = getUnit().getConfiguration().getLogFactory();
        try {
            log = (BufferedLog)serverLog;
        } catch (ClassCastException e) {
            System.err.println("Local Log was loaded by " + BufferedLog.class.getClassLoader());
            System.err.println("Server Log was loaded by " + serverLog.getClass().getClassLoader());
            e.printStackTrace();
        }
        populate();
        new MarketFeed(this).start(60*1000);
    }
    
    
    public Ask ask(Trader trader, Stock stock, int volume, double price) {
        EntityManager em = getEntityManager();
        begin();
        Ask ask = new Ask(trader, stock, price, volume);
        em.persist(ask);
        commit();
        return ask;
    }

    
    public Bid bid(Trader trader, Stock stock, int volume, double price) {
        EntityManager em = getEntityManager();
        begin();
        Bid bid = new Bid(trader, stock, price, volume);
        em.persist(bid);
        commit();
        return bid;
    }

    
    public List<Match> matchBid(Bid bid) {
        EntityManager em = getEntityManager();
        begin();
        TypedQuery<Match> q = em.createQuery(MATCH_BID, Match.class)
                                .setParameter("bid", bid);
        List<Match> matches = q.getResultList();
        commit();
        return matches;
    }
    
    public List<Match> matchAsk(Ask ask) {
        EntityManager em = getEntityManager();
        begin();
        TypedQuery<Match> q = em.createQuery(MATCH_ASK, Match.class)
                                .setParameter("ask", ask);
        List<Match> matches = q.getResultList();
        commit();
        return matches;
    }
    
    @Override
    public Tradable withdraw(Tradable t) {
        if (t.isTraded()) {
            throw new IllegalStateException("Can not widthdraw " + t + ". It has already been traded");
        }
        EntityManager em = getEntityManager();
        begin();
        em.createQuery("delete from " + (t instanceof Ask ? "Ask" : "Bid") + " t where t.id=:id")
          .setParameter("id", t.getId())
          .executeUpdate();
        commit();
        return t;
    }
    
    /**
     * Refresh may fail for various reasons.
     * The tradable might have been traded or withdrawn.
     */
    @Override
    public Tradable refresh(Tradable t) {
        EntityManager em = getEntityManager();
        begin();
        t = em.find(t.getClass(), t.getId());
        if (t != null) {
        	em.refresh(t);
        }
        commit();
        return t;
    }
    
    public Trade trade(Match match) {
        EntityManager em = getEntityManager();
        begin();
        Ask ask = em.merge(match.getAsk());
        Bid bid = em.merge(match.getBid());
        Trade trade = new Trade(ask, bid);
        em.persist(trade);
        commit();
        return trade;
    }

    public Trader login(String traderName) {
        EntityManager em = getEntityManager();
        begin();
        Trader trader = em.find(Trader.class, traderName);
        if (trader == null) {
            trader = new Trader(traderName);
            em.persist(trader);
        }
        commit();
        return trader;
    }

    public Stock getStock(String symbol) {
        EntityManager em = getEntityManager();
        begin();
        Stock stock = em.find(Stock.class, symbol);
        em.refresh(stock);
        commit();
        return stock;
    }

    public List<Trade> getTrades(Timestamp from, Timestamp to) {
        EntityManager em = getEntityManager();
        begin();
        List<Trade> result = em.createQuery(QUERY_TRADE_BY_PERIOD, Trade.class)
          .setParameter("from", from.getNanos())
          .setParameter("to", to.getNanos())
          .getResultList();
        commit();
        return result;
    }

    public List<Trade> getTrades(Trader trader, Boolean bought, Timestamp from, Timestamp to) {
        EntityManager em = getEntityManager();
        begin();
        StringBuilder jpql = new StringBuilder(QUERY_TRADE_BY_PERIOD);
        if (Boolean.TRUE.equals(bought)) {
            jpql.append(" AND t.buyer = : buyer");
        } else if (Boolean.FALSE.equals(bought)) {
            jpql.append(" AND t.seller = : seller");
        }
        TypedQuery<Trade> q = em.createQuery(jpql.toString(), Trade.class);
        if (Boolean.TRUE.equals(bought)) {
            q.setParameter("buyer", trader);
        } else if (Boolean.FALSE.equals(bought)) {
            q.setParameter("seller", trader);
        }
        q.setParameter("from", from.getNanos())
         .setParameter("to", to.getNanos());
        
        List<Trade> result = q.getResultList();
        commit();
        return result;
    }

    
    public void populate() {
        Object[][] data = {
                new Object[]{"IBM",  Sector.INFRASTRUCTURE, 140.03},
                new Object[]{"ORCL", Sector.INFRASTRUCTURE, 20.04},
                new Object[]{"MSFT", Sector.INFRASTRUCTURE, 32.0},
                new Object[]{"Bayer", Sector.HEALTHCARE, 120.45},
                new Object[]{"SMNS", Sector.HEALTHCARE, 34.98},
                new Object[]{"CSCO", Sector.INFRASTRUCTURE, 23.45},
                new Object[]{"GS",   Sector.FINACE, 120.09},
                new Object[]{"IFN", Sector.FINACE, 265.87},
               
        };
        EntityManager em = getEntityManager();
        
        begin();
        List<Stock> stocks = em.createQuery(GET_ALL_STOCKS, Stock.class).getResultList();
        if (stocks.isEmpty()) {
            for (int i = 0; i < data.length; i++) {
                Object[] d = data[i];
                Stock stock = new Stock((String)d[0], (String)d[0], (Sector)d[1], (Double)d[2]);
                em.persist(stock);
            }
        
            for (int i = 0; i < 4; i++) {
                Trader trader = new Trader("Trader-"+i);
                em.persist(trader);
            }
            stocks = em.createQuery(GET_ALL_STOCKS, Stock.class).getResultList();
        }
        commit();
    }


    
    public List<Stock> getStocks() {
        EntityManager em = getEntityManager();
        begin();
        List<Stock> stocks = em.createQuery(GET_ALL_STOCKS, Stock.class).getResultList();
        commit();
        return stocks;
    }
    

    public List<LogStatement> getLog() {
        if (log == null) {
            return new ArrayList<LogStatement>();
        }
        return log.get();
    }

    @Override
    public void close() {
        super.close();
    }
    
    static Map<String,Object> addLog(Map<String,Object> config) {
        if (config == null) {
            config = new HashMap<String, Object>();
        }
        config.put("openjpa.Log", BufferedLog.class.getName());
        return config;
    }
    
    public String getServiceURI() {
    	Map<String,Object> props = getUnit().getProperties();
    	Object url = props.get("openjpa.ConnectionURL");
    	try {
    	if (url == null) {
    		url = Arrays.toString((String[])props.get("openjpa.slice.Names"));
    	}
    	} catch (Exception ex) {
    		url = "?";
    	}
    	return "jpa:" + props.get("openjpa.Id") + "@" + url;
    }
}
