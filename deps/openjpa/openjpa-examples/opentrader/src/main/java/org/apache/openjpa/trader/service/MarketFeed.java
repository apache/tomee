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

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.EntityManager;

import org.apache.openjpa.trader.domain.Stock;

public class MarketFeed extends TimerTask {
    /**
     * A query to find symbols of all stocks.
     */
    String GET_ALL_STOCKS = "select s from Stock s";
    private static final Random rng = new Random(System.currentTimeMillis());
    private static final int MAX_CHANGE = 10;
    private final PersistenceService _service;

    MarketFeed(PersistenceService service) {
        super();
        _service = service;
    }

    void start(long period) {
        new Timer(true).schedule(this, 0, period);
    }

    @Override
    public void run() {
        EntityManager em = _service.newEntityManager();
        em.getTransaction().begin();
        List<Stock> stocks = em.createQuery(GET_ALL_STOCKS, Stock.class).getResultList();
        int n = stocks.size();
        for (int i = 0; i < n; i++) {
            if (rng.nextDouble() < 0.25) {
                Stock stock = stocks.get(i);
                double oldPrice = stock.getMarketPrice();
                double delta = (rng.nextDouble() - 0.5) * MAX_CHANGE;
                double newPrice = Math.max(oldPrice + delta, 0.01);
                stock.setMarketPrice(newPrice);
            }
        }
        em.getTransaction().commit();
    }
}
