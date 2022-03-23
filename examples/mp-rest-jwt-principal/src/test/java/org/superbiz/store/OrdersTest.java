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
package org.superbiz.store;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.spi.ArquillianProxyException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.store.entity.Order;
import org.superbiz.store.entity.Product;
import org.superbiz.store.rest.OrderRest;
import org.superbiz.store.rest.RestApplication;
import org.superbiz.store.service.OrderService;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class OrdersTest {

    private final static Logger LOGGER = Logger.getLogger(OrdersTest.class.getName());

    @Inject
    @RestClient
    private OrderRestClient orderRestClient;

    @Deployment()
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(OrderRest.class, RestApplication.class)
                .addClasses(Order.class, Product.class)
                .addClass(OrderService.class)
                .addClasses(OrderRestClient.class, TokenUtils.class)
                .addPackages(true, "com.nimbusds", "net.minidev.json")
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsResource("META-INF/microprofile-config.properties")
                .addAsResource("john-doe-jwt.json")
                .addAsResource("alice-wonder-jwt.json")
                .addAsResource("privateKey.pem")
                .addAsResource("publicKey.pem");
        return webArchive;
    }

    @Test
    public void shouldBeRunning() {
        assertEquals("running", orderRestClient.status());
    }

    @Test
    public void shouldReturnUserInfo() throws Exception {
        assertEquals("User: john is in groups [merchant]", orderRestClient.getUserInfo("Bearer " + createJwtToken(true)));
    }

    @Test
    public void shouldReturnOrder() throws Exception {
        int statusCode = orderRestClient.addOrder("Bearer " + createJwtToken(true), createTestOrder()).getStatus();

        assertEquals(Response.Status.CREATED.getStatusCode(), statusCode);

        Order order = orderRestClient.getOrder("Bearer " + createJwtToken(false), 1).readEntity(Order.class);

        assertEquals("john", order.getCreatedUser());
    }

    @Test
    public void shouldReturnListOfOrders() throws Exception {
        List<Order> ordersList = orderRestClient.getOrders("Bearer " + createJwtToken(true));
        assertEquals(0, ordersList.size());
    }

    @Test
    public void shouldSaveOrder() throws Exception {
        Order newOrder = orderRestClient.addOrder("Bearer " + createJwtToken(true), createTestOrder()).readEntity(Order.class);

        assertEquals("john", newOrder.getCreatedUser());
    }

    @Test
    public void shouldUpdateOrder() throws Exception {
        Order newOrder = orderRestClient.addOrder("Bearer " + createJwtToken(true), createTestOrder()).readEntity(Order.class);

        assertEquals("john", newOrder.getCreatedUser());
        assertEquals(null, newOrder.getUpdatedUser());

        newOrder.setOrderPrice(new BigDecimal(1000));
        Order updatedOrder = orderRestClient.updateOrder("Bearer " + createJwtToken(false), newOrder).readEntity(Order.class);

        assertEquals("alice", updatedOrder.getUpdatedUser());
    }

    @Test
    public void shouldDeleteOrder() throws Exception {
        int statusCode = orderRestClient.addOrder("Bearer " + createJwtToken(true), createTestOrder()).getStatus();

        assertEquals(Response.Status.CREATED.getStatusCode(), statusCode);

        statusCode = orderRestClient.deleteOrder("Bearer " + createJwtToken(true), 1).getStatus();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), statusCode);
    }

    @Test(expected = ArquillianProxyException.class)
    public void shouldNotHaveAccess() throws Exception {
        orderRestClient.deleteOrder("Bearer " + createJwtToken(false), 1).getStatus();
    }

    public String createJwtToken(boolean john) throws Exception {
        return TokenUtils.generateJWTString((john ? "john-doe-jwt.json" : "alice-wonder-jwt.json"));
    }

    private Order createTestOrder() {
        List<Product> products = new ArrayList<>();
        Product huaweiProduct = new Product();
        huaweiProduct.setId(1);
        huaweiProduct.setName("Huawei P20 Lite");
        huaweiProduct.setPrice(new BigDecimal(203.31));
        huaweiProduct.setStock(2);
        products.add(huaweiProduct);

        Product samsungProduct = new Product();
        samsungProduct.setId(2);
        samsungProduct.setName("Samsung S9");
        samsungProduct.setPrice(new BigDecimal(821.42));
        samsungProduct.setStock(1);
        products.add(samsungProduct);

        Order order = new Order();
        order.setId(1);
        order.setProducts(products);

        return order;
    }
}
