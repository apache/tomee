/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.store.service;

import org.superbiz.store.entity.Order;
import org.superbiz.store.entity.Product;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderService {

    private Map<Integer, Order> ordersInStore;

    @PostConstruct
    public void ProductService() {
        ordersInStore = new HashMap();
    }

    public List<Order> getOrders() {
        return new ArrayList<>(ordersInStore.values());
    }

    public Order getOrder(int id) {
        return ordersInStore.get(id);
    }

    public Order addOrder(Order order, String user) {
        order.setOrderPrice(calculateOrderPrice(order));
        order.setCreatedUser(user);

        ordersInStore.put(order.getId(), order);

        return order;
    }

    public void deleteOrder(int id) {
        ordersInStore.remove(id);
    }

    public Order updateOrder(Order order, String user) {
        order.setOrderPrice(calculateOrderPrice(order));
        order.setUpdatedUser(user);

        ordersInStore.put(order.getId(), order);

        return order;
    }

    private BigDecimal calculateOrderPrice(Order order) {
        return order.getProducts().stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
