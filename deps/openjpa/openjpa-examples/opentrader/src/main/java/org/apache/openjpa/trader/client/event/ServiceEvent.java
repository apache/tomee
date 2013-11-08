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

import org.apache.openjpa.trader.client.OpenTrader;
import org.apache.openjpa.trader.client.event.ServiceEventHandler.UpdateStockHandler;
import org.apache.openjpa.trader.domain.Stock;
import org.apache.openjpa.trader.domain.Tradable;
import org.apache.openjpa.trader.domain.Trade;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 * Specialization of GWTEvent (that represent mouse/keybord events and DOM events) 
 * for service related events such as a new trade has been committed or a tradable
 * is consumed. Each service event carries a payload and one or more handlers can
 * register interest in that event via the 
 * {@link OpenTrader#registerHandler(com.google.gwt.event.shared.GwtEvent.Type, EventHandler) 
 * application controller}.
 *   
 * @author Pinaki Poddar
 *
 * @param <T>
 * @param <H>
 */
public abstract class ServiceEvent<T, H extends EventHandler> extends GwtEvent<H> {
    private final T payload;

    protected ServiceEvent(T data) {
        payload = data;
    }

    public final T getPayload() {
        return payload;
    }

    public static class TradableAdded extends ServiceEvent<Tradable, ServiceEventHandler.AddTradableHandler> {
        public static Type<ServiceEventHandler.AddTradableHandler> TYPE = 
            new Type<ServiceEventHandler.AddTradableHandler>();

        public TradableAdded(Tradable tradable) {
            super(tradable);
        }

        @Override
        protected void dispatch(ServiceEventHandler.AddTradableHandler handler) {

            handler.onTradableAdded(this);
        }

        @Override
        public Type<ServiceEventHandler.AddTradableHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public static class TradableRemoved extends ServiceEvent<Tradable, ServiceEventHandler.RemoveTradableHandler> {
        public static Type<ServiceEventHandler.RemoveTradableHandler> TYPE = 
            new Type<ServiceEventHandler.RemoveTradableHandler>();

        public TradableRemoved(Tradable tradable) {
            super(tradable);
        }

        @Override
        protected void dispatch(ServiceEventHandler.RemoveTradableHandler handler) {
            handler.onTradableRemoved(this);
        }

        @Override
        public Type<ServiceEventHandler.RemoveTradableHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public static class StockUpdated extends ServiceEvent<Stock,UpdateStockHandler> {
        public static Type<UpdateStockHandler> TYPE = new Type<UpdateStockHandler>();

        public StockUpdated(Stock stock) {
            super(stock);

        }

        @Override
        protected void dispatch(UpdateStockHandler handler) {
            handler.onStockUpdated(this);
        }

        @Override
        public Type<UpdateStockHandler> getAssociatedType() {
            return TYPE;
        }
    }
    
    public static class TradeCommitted extends ServiceEvent<Trade, ServiceEventHandler.AddTradeHandler> {
        public static Type<ServiceEventHandler.AddTradeHandler> TYPE = 
            new Type<ServiceEventHandler.AddTradeHandler>();

        public TradeCommitted(Trade trade) {
            super(trade);
        }

        @Override
        protected void dispatch(ServiceEventHandler.AddTradeHandler handler) {

            handler.onTradeCommitted(this);
        }

        @Override
        public Type<ServiceEventHandler.AddTradeHandler> getAssociatedType() {
            return TYPE;
        }
    }


}
