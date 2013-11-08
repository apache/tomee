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
package org.apache.openjpa.trader.server;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.openjpa.trader.client.TradingServiceAdapter;
import org.apache.openjpa.trader.domain.Ask;
import org.apache.openjpa.trader.domain.Bid;
import org.apache.openjpa.trader.domain.LogStatement;
import org.apache.openjpa.trader.domain.Match;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;
import org.apache.openjpa.trader.domain.Trader;
import org.apache.openjpa.trader.service.Exchange;
import org.apache.openjpa.trader.service.MockTradingService;
import org.apache.openjpa.trader.service.TradingService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the GWT RPC service.
 * <p>
 * This implementation delegates to original implementation, thereby blocking the GWT Servlet
 * dependency to the original implementation of the service. This implementation being a 
 * servlet allows us to switch the delegate during {@#init(ServletConfig) initialization}  
 * to either a {@link Exchange real JPA-based } implementation or a {@link MockTradingService simple in-memory}
 * implementation of the {@link TradingService service interface}.
 * <p>
 * The other important advantage of such delegation is to translate exception. The underlying service
 * exceptions are translated by an {@link ExceptionAdapter exception translator} that ensures that
 * the translated exceptions are serializable and hence accessible to the browser-based client. 
 *   
 * @author Pinaki Poddar
 */
@SuppressWarnings("serial")
public class TradingServiceAdapterImpl extends RemoteServiceServlet implements TradingServiceAdapter {
    
    private TradingService _del;
    private ExceptionAdapter _translator = new ExceptionAdapter();
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String unit = config.getInitParameter("persistence.unit");
        String mock = config.getInitParameter("mock");
        String serverTrace = config.getInitParameter("server-side-stacktrace");
        _del = ("true".equalsIgnoreCase(mock)) ? new MockTradingService() : new Exchange(unit);
        _translator.setPrintServerSideStackTrace("true".equalsIgnoreCase(serverTrace));
    }
    
    public void destroy() {
        _del.close();
        super.destroy();
    }
    
    public Ask ask(Trader trader, Stock stock, int volume, double price) {
        try {
            return _del.ask(trader, stock, volume, price);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Bid bid(Trader trader, Stock stock, int volume, double price) {
        try {
            return _del.bid(trader, stock, volume, price);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Tradable withdraw(Tradable t) {
        try {
            return _del.withdraw(t);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Tradable refresh(Tradable t) {
        try {
            return _del.refresh(t);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Stock getStock(String symbol) {
        try {
            return _del.getStock(symbol);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public List<Stock> getStocks() {
        try {
            return new ArrayList<Stock>(_del.getStocks());
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public List<Trade> getTrades(Timestamp from, Timestamp to) {
        try {
            return _del.getTrades(from, to);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public List<Trade> getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to) {
        try {
            return _del.getTrades(trader, boughtOrsold, from, to);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Trader login(String trader) throws RuntimeException {
        try {
            return _del.login(trader);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public List<Match> matchAsk(Ask ask) {
        try {
            return new ArrayList<Match>(_del.matchAsk(ask));
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public List<Match> matchBid(Bid bid) {
        try {
            return new ArrayList<Match>(_del.matchBid(bid));
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public Trade trade(Match match) {
        try {
            return _del.trade(match);
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    @Override
    public List<LogStatement> getLog() {
        try {
            return _del.getLog();
        } catch (Throwable e) {
            throw translate(e);
        }
    }
    
    public String getServiceURI() {
        try {
            return _del.getServiceURI();
        } catch (Throwable e) {
            throw translate(e);
        }
    }


    RuntimeException translate(Throwable t) {
        return _translator.translate(t);
    }
}
