/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.inheritance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.inheritance.Tower.Fit;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InheritanceTest {

    //START SNIPPET: setup
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "webservice-inheritance.war")
                .addClasses(Item.class, Tower.class, Wakeboard.class, WakeboardBinding.class, Wearable.class,
                        WakeRiderImpl.class, WakeRiderWs.class)
                .addAsResource("META-INF/persistence.xml");
    }
    //END SNIPPET: setup

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    @Test
    public void testInheritanceViaWsInterface() throws Exception {
        Service service = Service.create(
                new URL(base.toExternalForm() + "webservices/WakeRiderImpl?wsdl"),
                new QName("http://superbiz.org/wsdl", "InheritanceWsService"));
        assertNotNull(service);

        WakeRiderWs ws = service.getPort(WakeRiderWs.class);

        Tower tower = createTower();
        Item item = createItem();
        Wakeboard wakeBoard = createWakeBoard();
        WakeboardBinding wakeBoardbinding = createWakeboardBinding();

        ws.addItem(tower);
        ws.addItem(item);
        ws.addItem(wakeBoard);
        ws.addItem(wakeBoardbinding);

        List<Item> returnedItems = ws.getItems();

        assertEquals("testInheritanceViaWsInterface, nb Items", 4, returnedItems.size());

        //check tower
        assertEquals("testInheritanceViaWsInterface, first Item", returnedItems.get(0).getClass(), Tower.class);
        tower = (Tower) returnedItems.get(0);
        assertEquals("testInheritanceViaWsInterface, first Item", tower.getBrand(), "Tower brand");
        assertEquals("testInheritanceViaWsInterface, first Item", tower.getFit().ordinal(), Fit.Custom.ordinal());
        assertEquals("testInheritanceViaWsInterface, first Item", tower.getItemName(), "Tower item name");
        assertEquals("testInheritanceViaWsInterface, first Item", tower.getPrice(), 1.0d, 0.0d);
        assertEquals("testInheritanceViaWsInterface, first Item", tower.getTubing(), "Tower tubing");

        //check item
        assertEquals("testInheritanceViaWsInterface, second Item", returnedItems.get(1).getClass(), Item.class);
        item = (Item) returnedItems.get(1);
        assertEquals("testInheritanceViaWsInterface, second Item", item.getBrand(), "Item brand");
        assertEquals("testInheritanceViaWsInterface, second Item", item.getItemName(), "Item name");
        assertEquals("testInheritanceViaWsInterface, second Item", item.getPrice(), 2.0d, 0.0d);

        //check wakeboard
        assertEquals("testInheritanceViaWsInterface, third Item", returnedItems.get(2).getClass(), Wakeboard.class);
        wakeBoard = (Wakeboard) returnedItems.get(2);
        assertEquals("testInheritanceViaWsInterface, third Item", wakeBoard.getBrand(), "Wakeboard brand");
        assertEquals("testInheritanceViaWsInterface, third Item", wakeBoard.getItemName(), "Wakeboard item name");
        assertEquals("testInheritanceViaWsInterface, third Item", wakeBoard.getPrice(), 3.0d, 0.0d);
        assertEquals("testInheritanceViaWsInterface, third Item", wakeBoard.getSize(), "WakeBoard size");

        //check wakeboardbinding
        assertEquals("testInheritanceViaWsInterface, fourth Item", returnedItems.get(3).getClass(), WakeboardBinding.class);
        wakeBoardbinding = (WakeboardBinding) returnedItems.get(3);
        assertEquals("testInheritanceViaWsInterface, fourth Item", wakeBoardbinding.getBrand(), "Wakeboardbinding brand");
        assertEquals("testInheritanceViaWsInterface, fourth Item", wakeBoardbinding.getItemName(), "Wakeboardbinding item name");
        assertEquals("testInheritanceViaWsInterface, fourth Item", wakeBoardbinding.getPrice(), 4.0d, 0.0d);
        assertEquals("testInheritanceViaWsInterface, fourth Item", wakeBoardbinding.getSize(), "WakeBoardbinding size");
    }
    //END SNIPPET: webservice

    private Tower createTower() {
        Tower tower = new Tower();
        tower.setBrand("Tower brand");
        tower.setFit(Fit.Custom);
        tower.setItemName("Tower item name");
        tower.setPrice(1.0f);
        tower.setTubing("Tower tubing");
        return tower;
    }

    private Item createItem() {
        Item item = new Item();
        item.setBrand("Item brand");
        item.setItemName("Item name");
        item.setPrice(2.0f);
        return item;
    }

    private Wakeboard createWakeBoard() {
        Wakeboard wakeBoard = new Wakeboard();
        wakeBoard.setBrand("Wakeboard brand");
        wakeBoard.setItemName("Wakeboard item name");
        wakeBoard.setPrice(3.0f);
        wakeBoard.setSize("WakeBoard size");
        return wakeBoard;
    }

    private WakeboardBinding createWakeboardBinding() {
        WakeboardBinding wakeBoardBinding = new WakeboardBinding();
        wakeBoardBinding.setBrand("Wakeboardbinding brand");
        wakeBoardBinding.setItemName("Wakeboardbinding item name");
        wakeBoardBinding.setPrice(4.0f);
        wakeBoardBinding.setSize("WakeBoardbinding size");
        return wakeBoardBinding;
    }
}
