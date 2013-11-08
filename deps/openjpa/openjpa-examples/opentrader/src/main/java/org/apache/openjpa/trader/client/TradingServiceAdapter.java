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
package org.apache.openjpa.trader.client;

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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * <br>
 * This is a delegating interface to the original {@link TradingService} interface. This delegation
 * pattern serves several purposes
 * <LI> a) the delegator adds RuntimeException to each of the original service methods. 
 * The original {@link TradingService} has not defined the exception in its method signature, 
 * However, the actual implementation of {@link TradingService} is based on
 * JPA and hence will throw <code>javax.persistence.PersistenceException</code>. To propagate
 * these exceptions to the client, these exceptions need to be translated for serialization by 
 * GWT compiler and will bring in heavier dependency. 
 * <br>
 * On the other hand, GWT will not propagate a non-translatable exception to the client,
 * thereby making develop-debug cycles harder.
 * <LI> b) GWT requires the service interface to extend GWT-defined 
 * <code>com.google.gwt.user.client.rpc.RemoteService</code>. But the discipline used for this
 * application dictates that the <em>service interface</em> be independent of either
 * how it is implemented or how it will be accessed. That is why original {@link TradingService}
 * definition neither depends on <code>javax.persistence.*</code> nor on <code>com.google.gwt.*</code>.
 * <p>
 * Because the interface is delegated, it is not necessary to have the same method name or 
 * even signature. It may not have to declare all the original service interface methods either.
 * <p>
 * 
 * Any type appearing in this interface must be serializable per GWT requirement.
 */

/**
 * This <code>@RemoteServiceRelativePath</code> annotation defines the relative URL of the
 * deployed servlet. It appears in <code>web.xml</code> as: 
 * <pre>
 *  &lt;servlet-mapping>
 *       &lt;servlet-name>opentrader&lt;/servlet-name>
 *       &lt;url-pattern>/opentrader/<b>trade</b>&lt;/url-pattern>
 *   &lt;/servlet-mapping>
 *  </pre>
 *  <p>
 *  The servlet name matches the name used in module descriptor <code>OpenTrader.gwt.xml</code> as
 *  <pre>
 *  &lt;module rename-to='opentrader'>
 *  </pre>
 */
@RemoteServiceRelativePath("trade")
public interface TradingServiceAdapter extends RemoteService {
    Trader login(String name) 
           throws RuntimeException;
    List<Stock> getStocks()   
           throws RuntimeException;
    Ask ask(Trader trader, Stock stock, int volume, double price) 
           throws RuntimeException;
    Bid bid(Trader trader, Stock stock, int volume, double price) 
           throws RuntimeException;
    Tradable withdraw(Tradable t) 
           throws RuntimeException;
    Tradable refresh(Tradable t)  
           throws RuntimeException;
    List<Match> matchBid(Bid bid) 
           throws RuntimeException;
    List<Match> matchAsk(Ask ask) 
           throws RuntimeException;
    Trade trade(Match match)      
           throws RuntimeException;
    List<Trade> getTrades(Timestamp from, Timestamp to) 
           throws RuntimeException;
    List<Trade> getTrades(Trader trader, Boolean boughtOrsold, Timestamp from, Timestamp to) 
           throws RuntimeException;
    Stock getStock(String symbol) 
           throws RuntimeException;
    List<LogStatement> getLog() 
           throws RuntimeException;
    String getServiceURI()
    		throws RuntimeException;
}
