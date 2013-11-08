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
package org.apache.openjpa.trader.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * The contract to handle {@link ServiceEvent service events}.
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public interface ServiceEventHandler {

    public interface AddTradableHandler extends EventHandler {
        public void onTradableAdded(ServiceEvent.TradableAdded event);
    }
    
    public static interface AddTradeHandler extends EventHandler {
        public void onTradeCommitted(ServiceEvent.TradeCommitted event);
    }
    
    public static interface RemoveTradableHandler extends EventHandler {
        public void onTradableRemoved(ServiceEvent.TradableRemoved event);
    }
    
    public static interface UpdateStockHandler extends EventHandler {
        public void onStockUpdated(ServiceEvent.StockUpdated event);
    }
}
