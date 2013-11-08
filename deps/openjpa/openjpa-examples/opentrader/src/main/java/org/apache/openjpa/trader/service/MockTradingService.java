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
import java.util.List;
import java.util.Random;

import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Sector;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;


public class MockTradingService implements TradingService {
    List<Ask> _asks = new ArrayList<Ask>();
    List<Bid> _bids = new ArrayList<Bid>();
    List<Trade> _trades = new ArrayList<Trade>();
    List<Stock> _stocks = new ArrayList<Stock>();
    List<Trader> _traders = new ArrayList<Trader>();
    List<LogStatement> _logs = new ArrayList<LogStatement>();
    int counter = 0;
    private static Random rng = new Random(System.currentTimeMillis());
    
    public MockTradingService() {
        Sector[] sectors = Sector.values();
        for (int i = 0; i < 10; i++) {
            Stock stock = new Stock("Stock-"+i, "Company-"+i, sectors[rng.nextInt(sectors.length)],
                    10*rng.nextDouble());
            _stocks.add(stock);
        }
    }
    
    @Override
    public Ask ask(Trader trader, Stock stock, int volume, double price) {
        Ask ask = new Ask(trader, stock, price, volume);
        _asks.add(ask);
        log("Added " + ask + " " + counter++);
        return ask;
    }

    @Override
    public Bid bid(Trader trader, Stock stock, int volume, double price) {
        Bid bid = new Bid(trader, stock, price, volume);
        _bids.add(bid);
        log("Added new " + bid + " " + counter++);
        return bid;
    }

    @Override
    public List<LogStatement> getLog() {
        int from = Math.max(_logs.size()-5, 0);
        List<LogStatement> result = new ArrayList<LogStatement>();
        for (int i = from; i < _logs.size(); i++)
            result.add(_logs.get(i));
        return result;
    }

    @Override
    public Stock getStock(String symbol) {
        for (Stock s : _stocks) {
            if (s.getSymbol().equals(symbol))
                return s;
        }
        log("No Stock " + symbol);
        return null;
    }

    @Override
    public List<Stock> getStocks() {
        for (Stock s : _stocks) {
            double delta = 10*(2*rng.nextDouble()-1)/100;
            s.setMarketPrice(s.getMarketPrice() + s.getMarketPrice()*delta);
        }
        return _stocks;
    }

    @Override
    public List<Trade> getTrades(Timestamp from, Timestamp to) {
        return null;
    }

    @Override
    public List<Trade> getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to) {
        return null;
    }

    @Override
    public Trader login(String trader) {
        for (Trader t : _traders) {
            if (t.getName().equals(trader))
                return t;
        } 
        Trader t = new Trader(trader);
        _traders.add(t);
        return t;
    }

    @Override
    public List<Match> matchAsk(Ask ask) {
        List<Match> result = new ArrayList<Match>();
        for (Bid bid : _bids) {
            if (matches(ask, bid)) {
                result.add(new Match(ask, bid));
            }
        }
        return result;
    }

    @Override
    public List<Match> matchBid(Bid bid) {
        List<Match> result = new ArrayList<Match>();
        for (Ask ask : _asks) {
            if (matches(ask, bid)) {
                result.add(new Match(ask, bid));
            }
        }
        return result;
    }
    
    public Tradable refresh(Tradable t) {
        return t;
    }

    @Override
    public Trade trade(Match match) {
        Trade trade = new Trade(match.getAsk(), match.getBid());
        return trade;
    }
    
    private boolean matches(Ask ask, Bid bid) {
        return ((bid.getStock().getSymbol().equals(ask.getStock().getSymbol()))
                &&  (!bid.getBuyer().equals(ask.getSeller()))
                &&  (bid.getPrice() >= ask.getPrice())
                &&  (bid.getVolume()) <= ask.getVolume());
    }
    
    private void log(String s) {
        _logs.add(new LogStatement("INFO", "Context", "Thread", "Channel", s));
        System.err.println("server log:" + s );
    }

    @Override
    public Tradable withdraw(Tradable t) {
//        t.expire();
        return t;
    }

    @Override
    public void close() {
    }
    
    public String getServiceURI() {
    	return "Mock";
    }

}
