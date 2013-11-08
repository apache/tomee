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
package org.apache.openjpa.persistence.jdbc.order;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestOrderColumn extends SingleEMFTestCase {   
    
    private Student[] students = new Student[12];
    
    public void setUp() {
        super.setUp(
                CLEAR_TABLES,
                Person.class, Player.class, BattingOrder.class,
                Trainer.class, Game.class, Inning.class,
                Course.class, Student.class,
                Owner.class, Bicycle.class, Car.class, Home.class,
                Widget.class,BiOrderMappedByEntity.class, BiOrderEntity.class );
        try {
            createQueryData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Verifies that a collection remains contiguous and element
     * indexes are reordered if an element is removed for a
     * OneToMany relationship 
     */
    public void testOneToManyElementRemoval() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "batters", 
            "batters_ORDER");// "batters_ORDER");

        // Create some data
        Player[] players = new Player[10];
        ArrayList<Player> playersArr = new ArrayList<Player>();
        em.getTransaction().begin();
        for (int i = 0; i < 10 ; i++) {
            players[i] = new Player("Player" + i, i+100);
            em.persist(players[i]);
            playersArr.add(players[i]);
        }
        em.getTransaction().commitAndResume();
                      
        // Persist the related entities
        BattingOrder order = new BattingOrder();
        order.setBatters(playersArr);
        em.persist(order);
        em.getTransaction().commit();
        em.refresh(order);
        em.clear();
        
        // Verify order is correct.
        BattingOrder newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        for (int i = 0; i < 10 ; i++) {
            assertEquals(newOrder.getBatters().get(i), (players[i]));
        }
        
        // Remove some items
        em.getTransaction().begin();
        newOrder.getBatters().remove(1);
        playersArr.remove(1);
        newOrder.getBatters().remove(5);
        playersArr.remove(5);        
        em.getTransaction().commit();
        em.clear();

        // Simple assertion via find
        newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        assertNotNull(newOrder.getBatters());
        assertEquals(playersArr.size(), newOrder.getBatters().size());
        for (int i = 0; i < playersArr.size() ; i++) {
              assertEquals(newOrder.getBatters().get(i), (playersArr.get(i)));
        }

        // Stronger assertion via INDEX value
        validateIndexAndValues(em, "BattingOrder", "batters", 0, 
            playersArr.toArray(), "id", 
            order.id);
        
        em.close();        
    }

    /*
     * Verifies that a collection remains contiguous and element
     * indexes are reordered if an element is removed for a
     * OneToMany relationship 
     */
    public void testOneToManyBiDirElementRemoval() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Verify field name is the default via fm
        validateOrderColumnName(BiOrderMappedByEntity.class, "bo2mEntities", 
            "bo2mEntities_ORDER");

        // Create some data
        BiOrderMappedByEntity bome = new BiOrderMappedByEntity();
        bome.setId(1);
        List<BiOrderEntity> boea = new ArrayList<BiOrderEntity>();
        for (int i = 0; i < 5; i++) {
            BiOrderEntity boe = new BiOrderEntity();
            boe.setId(i+1);
            boe.setName("Entity" + i);
            boe.setEntity(bome);
            boea.add(boe);
            bome.addBo2mEntity(boe);
        }

        // Persist
        em.getTransaction().begin();
        em.persist(bome);
        for (BiOrderEntity boe : boea) {
            em.persist(boe);
        }
        em.getTransaction().commit();        
        em.refresh(bome);
        em.clear();
        
        // Verify order is correct.
        BiOrderMappedByEntity newBome = em.find(BiOrderMappedByEntity.class, 
            bome.getId());
        assertNotNull(newBome);
        for (int i = 0; i < 5 ; i++) {
            assertEquals(newBome.getBo2mEntities().get(i), boea.get(i));
        }
        
        // Remove an item
        em.getTransaction().begin();
        newBome.getBo2mEntities().get(2).setEntity(null);
        newBome.removeBo2mEntity(2);
        boea.remove(2);
        em.getTransaction().commit();
        em.clear();

        // Simple assertion via find
        newBome = em.find(BiOrderMappedByEntity.class, bome.getId());
        assertNotNull(newBome);
        assertNotNull(newBome.getBo2mEntities());
        assertEquals(boea.size(), newBome.getBo2mEntities().size());
        for (int i = 0; i < boea.size() ; i++) {
              assertEquals(newBome.getBo2mEntities().get(i), (boea.get(i)));
        }

        // Stronger assertion via INDEX value
        validateIndexAndValues(em, "BiOrderMappedByEntity", "bo2mEntities", 0, 
            boea.toArray(), "id", 
            bome.getId());
        
        em.close();
    }

    /*
     * Verifies that a collection remains contiguous and element
     * indexes are reordered if an element is removed for an
     * ElementCollection 
     */
    public void testElementCollectionElementRemoval() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        Game game = new Game();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Game.class, "rainDates", 
            "dateOrder"); 
        
        // Create a list of basic types
        java.sql.Date dates[] = new java.sql.Date[10];
        ArrayList<java.sql.Date> rainDates = new ArrayList<java.sql.Date>(10);
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 10; i++) {
            today.set(2009, 1, i+1);
            dates[i] = new java.sql.Date(today.getTimeInMillis());
        }
        // Add in reverse order
        for (int i = 9; i >= 0; i--) {
            rainDates.add(dates[i]);
        }
        game.setRainDates(rainDates);
        
        em.getTransaction().begin();
        em.persist(game);
        em.getTransaction().commit();
               
        em.clear();
        
        Game newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        // Verify the order
        for (int i = 0; i < 10; i++) {
            assertEquals(game.getRainDates().get(i),
                rainDates.get(i));
        }
        
        // Remove some dates
        em.getTransaction().begin();
        newGame.getRainDates().remove(4);
        rainDates.remove(4);
        newGame.getRainDates().remove(2);
        rainDates.remove(2);
        em.getTransaction().commit();
        em.clear();

        newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        assertNotNull(newGame.getRainDates());
        assertEquals(8, newGame.getRainDates().size());
        // Verify the order
        for (int i = 0; i < newGame.getRainDates().size(); i++) {
            assertEquals(newGame.getRainDates().get(i).toString(),
                rainDates.get(i).toString());
        }
        
        // Stronger assertion via INDEX value
        validateCollIndexAndValues(em, "Game", "rainDates", 0, 
            newGame.getRainDates().toArray(), "id", 
            newGame.getId());
        
        em.close();
    }
    /*
     * Verifies that a collection remains contiguous and element
     * indexes are reordered if an element is inserted into the collection. 
     */
    public void testOneToManyElementInsert() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "batters", 
            "batters_ORDER");// "batters_ORDER");

        // Create some data
        Player[] players = new Player[10];
        ArrayList<Player> playersArr = new ArrayList<Player>();
        em.getTransaction().begin();
        for (int i = 0; i < 10 ; i++) {
            players[i] = new Player("Player" + i, i+100);
            em.persist(players[i]);
            playersArr.add(players[i]);
        }
        em.getTransaction().commitAndResume();
                      
        // Persist the related entities
        BattingOrder order = new BattingOrder();
        order.setBatters(playersArr);
        em.persist(order);
        em.getTransaction().commitAndResume();
        em.refresh(order);
        
        em.getTransaction().commit();
        em.clear();
        
        // Verify order is correct.
        BattingOrder newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        for (int i = 0; i < 10 ; i++) {
            assertEquals(newOrder.getBatters().get(i), (players[i]));
        }
                
        Player p = new Player("PlayerNew", 150);
        playersArr.add(2, p);

        Player p2 = new Player("PlayerNew2", 151);
        playersArr.add(p2);
        // Add an item at index 2 and at the end of the list 
        em.getTransaction().begin();
        newOrder.getBatters().add(2, p);
        newOrder.getBatters().add(p2);
        em.getTransaction().commit();
        em.clear();

        // Simple assertion via find
        newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        assertNotNull(newOrder.getBatters());
        assertEquals(playersArr.size(), newOrder.getBatters().size());
        for (int i = 0; i < playersArr.size() ; i++) {
              assertEquals(newOrder.getBatters().get(i), (playersArr.get(i)));
        }

        // Stronger assertion via INDEX value
        validateIndexAndValues(em, "BattingOrder", "batters", 0, 
            playersArr.toArray(), "id", 
            order.id);
        
        em.close();        
    }
    /*
     * Verifies that a collection remains contiguous and element
     * indexes are reordered if an element is inserted into an
     * ElementCollection 
     */
    public void testElementCollectionElementInsert() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        Game game = new Game();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Game.class, "rainDates", 
            "dateOrder"); 
        
        // Create a list of basic types
        java.sql.Date dates[] = new java.sql.Date[10];
        ArrayList<java.sql.Date> rainDates = new ArrayList<java.sql.Date>(10);
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 10; i++) {
            today.set(2009, 1, i+1);
            dates[i] = new java.sql.Date(today.getTimeInMillis());
        }
        // Add in reverse order
        for (int i = 9; i >= 0; i--) {
            rainDates.add(dates[i]);
        }
        game.setRainDates(rainDates);
        
        em.getTransaction().begin();
        em.persist(game);
        em.getTransaction().commit();
               
        em.clear();
        
        Game newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        // Verify the order
        for (int i = 0; i < 10; i++) {
            assertEquals(game.getRainDates().get(i),
                rainDates.get(i));
        }
        
        // Add some dates
        today.set(2009, 1, 15);
        rainDates.add(1, new java.sql.Date(today.getTimeInMillis()));
        today.set(2009, 1, 20);
        rainDates.add(6, new java.sql.Date(today.getTimeInMillis()));
        
        em.getTransaction().begin();
        game.getRainDates().add(1, rainDates.get(1));
        game.getRainDates().add(6, rainDates.get(6));
        em.getTransaction().commit();
        em.clear();

        newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        assertNotNull(game.getRainDates());
        assertEquals(12, game.getRainDates().size());
        // Verify the order
        for (int i = 0; i < game.getRainDates().size(); i++) {
            assertEquals(game.getRainDates().get(i),
                rainDates.get(i));
        }
        
        em.close();
    }

    
    /*
     * Validates use of OrderColumn with OneToMany using the default
     * order column name
     */
    public void testOneToManyDefault() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "batters", 
            "batters_ORDER");// "batters_ORDER");

        // Create some data
        Player[] players = new Player[10];
        em.getTransaction().begin();
        for (int i = 0; i < 10 ; i++) {
            players[i] = new Player("Player" + i, i);
            em.persist(players[i]);
        }
        em.getTransaction().commitAndResume();
        
        
        // Add it to the persistent list in reverse order
        ArrayList<Player> playersArr = new ArrayList<Player>();
        for (int i = 0; i < 10 ; i++) {
            playersArr.add(players[9 - i]);
        }
        
        // Persist the related entities
        BattingOrder order = new BattingOrder();
        order.setBatters(playersArr);
        em.persist(order);
        em.getTransaction().commit();
        em.refresh(order);
        em.clear();
        
        // Verify order is correct.
        BattingOrder newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        for (int i = 0; i < 10 ; i++) {
            assertEquals(newOrder.getBatters().get(i), (players[9 - i]));
        }
        
        // Add another entity and check order
        Player newPlayer = new Player("New Player", 99);
        
        em.getTransaction().begin();
        newOrder.getBatters().add(9, newPlayer);
        em.getTransaction().commit();
        em.clear();

        newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        for (int i = 0; i <= 10 ; i++) {
            if (i < 9)
              assertEquals(newOrder.getBatters().get(i), (players[9 - i]));
            else if (i == 9)
              assertEquals(newOrder.getBatters().get(i), newPlayer);
            else if (i == 10)
              assertEquals(newOrder.getBatters().get(i), players[0]);
        }
        
        em.close();
    }

    /*
     * Validates use of OrderColumn with OneToMany using a specified
     * order column name
     */
    public void testOneToManyNamed() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "pinch_hitters", 
            "pinch_order");

        // Create some data
        Player[] players = new Player[4];
        em.getTransaction().begin();
        for (int i = 0; i < 4 ; i++) {
            players[i] = new Player("PinchHitter" + i, i);
            em.persist(players[i]);
        }
        em.getTransaction().commitAndResume();
                
        // Add it to the persistent list in reverse order
        ArrayList<Player> pinchArr = new ArrayList<Player>();
        for (int i = 0; i < players.length ; i++) {
            pinchArr.add(players[players.length - 1 - i]);
        }
        
        // Persist the related entities
        BattingOrder order = new BattingOrder();
        order.setPinchHitters(pinchArr);
        em.persist(order);
        em.getTransaction().commit();        
        em.clear();
        
        // Verify order is correct.
        BattingOrder newOrder = em.find(BattingOrder.class, order.id);
        assertNotNull(newOrder);
        for (int i = 0; i < players.length ; i++) {
            assertEquals(newOrder.getPinchHitters().get(i), 
                    (players[players.length - 1 - i]));
        }
        em.close();
    }

    /*
     * Validates use of OrderColumn with ManyToMany relationship
     */
    public void testManyToMany() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Trainer.class, "playersTrained", 
            "trainingOrder"); 
        
        // Create some data
        Player[] players = new Player[25];
        em.getTransaction().begin();
        for (int i = 0; i < players.length ; i++) {
            players[i] = new Player("TrainedPlayer" + i, i);
            em.persist(players[i]);
        }
        em.getTransaction().commitAndResume();

        // Create M2M, add players in reverse insert order to
        // validate column order
        Trainer[] trainers = new Trainer[5];
        for (int i = 0; i < trainers.length; i++) {
            trainers[i] = new Trainer("Trainer" + i);
            ArrayList<Player> trained = new ArrayList<Player>();
            for (int j = ((i * 5) + 4); j >= (i * 5); j--) {
                trained.add(players[j]);
                if (players[j].getTrainers() == null)
                    players[j].setTrainers(new ArrayList<Trainer>());
                players[j].getTrainers().add(trainers[i]);
            }
            trainers[i].setPlayersTrained(trained);
            em.persist(trainers[i]);
        }
        em.getTransaction().commit();
        
        em.clear();
        // Verify order is correct.
        for (int i = 0; i < trainers.length; i++) {
            Trainer trainer = em.find(Trainer.class, trainers[i].getId());
            assertNotNull(trainer);
            List<Player> trainedPlayers = trainer.getPlayersTrained();
            assertNotNull(trainedPlayers);
            assertEquals(trainedPlayers.size(), 5);
            for (int j = trainedPlayers.size() - 1; j >=0  ; j--) {
                assertEquals(trainedPlayers.get(j), 
                    (players[(i * 5) + (4 - j)]));
            }
        }
        em.close();
    }

    /*
     * Validates use of OrderColumn with ManyToMany bi-directional with
     * both sides of the relationship maintaining order.  This test is not
     * currently run since work is underway to determine the feasiblity of
     * bi-directional ordering.  
     */
    public void validateBiOrderedManyToMany() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Game.class, "playedIn", 
            "playerOrder"); 

        validateOrderColumnName(Player.class, "gamesPlayedIn", 
            "playedInOrder"); 

        // Create some data
        Player[] players = new Player[25];
        em.getTransaction().begin();
        for (int i = 0; i < players.length ; i++) {
            players[i] = new Player("GamePlayer" + i, i);
            em.persist(players[i]);
        }
        em.getTransaction().commitAndResume();

        // Create M2M, add players in reverse insert order to
        // validate column order
        Game[] games = new Game[5];
        for (int i = 0; i < games.length; i++) {
            games[i] = new Game();
            ArrayList<Player> playedIn = new ArrayList<Player>();
            for (int j = ((i * 5) + 4); j >= (i * 5); j--) {
                playedIn.add(players[j]);
                if (players[j].getGamesPlayedIn() == null)
                    players[j].setGamesPlayedIn(new ArrayList<Game>());
                players[j].getGamesPlayedIn().add(games[i]);
            }
            games[i].setPlayedIn(playedIn);
            em.persist(games[i]);
        }
        em.getTransaction().commit();
        
        em.clear();
        // Verify order is correct.
        for (int i = 0; i < games.length; i++) {
            Game game = em.find(Game.class, games[i].getId());
            assertNotNull(game);
            List<Player> playedIn = game.getPlayedIn();
            assertNotNull(playedIn);
            assertEquals(playedIn.size(), 5);
            for (int j = playedIn.size() - 1; j >=0  ; j--) {
                Player p = playedIn.get(j);
                assertEquals(p, 
                    (players[(i * 5) + (4 - j)]));
                for (int k = 0; k < p.getGamesPlayedIn().size(); k++) {
                    assertNotNull(p.getGamesPlayedIn());
                    assertEquals(p.getGamesPlayedIn().get(k),
                            games[k]);
                }
            }            
        }
        em.close();        
    }

    /*
     * Validates use of OrderColumn with ElementCollection of basic
     * elements
     */
    public void testElementCollectionBasic() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        Game game = new Game();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Game.class, "rainDates", 
            "dateOrder"); 
        
        // Create a list of basic types
        java.sql.Date dates[] = new java.sql.Date[10];
        ArrayList<java.sql.Date> rainDates = new ArrayList<java.sql.Date>(10);
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 10; i++) {
            today.set(2009, 1, i+1);
            dates[i] = new java.sql.Date(today.getTimeInMillis());
        }
        // Add in reverse order
        for (int i = 9; i >= 0; i--) {
            rainDates.add(dates[i]);
        }
        game.setRainDates(rainDates);
        
        em.getTransaction().begin();
        em.persist(game);
        em.getTransaction().commit();

        em.clear();
        
        Game newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        // Verify the order
        for (int i = 0; i < 10; i++) {
            assertEquals(game.getRainDates().get(i),
                dates[9-i]);
        }
        em.close();
    }

    /*
     * Validates use of OrderColumn with ElementCollection of Embeddables
     */
    public void testElementCollectionEmbeddables() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        Game game = new Game();
        
        // Verify field name is the default via fm
        validateOrderColumnName(Game.class, "innings", 
            "inningOrder"); 
        
        // Create a list of basic types
        Inning innings[] = new Inning[9];
        Collection<Inning> inningCol = new ArrayList<Inning>();
        Random rnd = new Random();
        for (int i = 8; i >= 0; i--) {
            innings[i] = new Inning(i, Math.abs(rnd.nextInt()) % 10, 
                Math.abs(rnd.nextInt()) % 10);
        }
        // Add in reverse (correct) order
        for (int i = 8; i >= 0; i--) {
            inningCol.add(innings[i]);
        }
        game.setInnings(inningCol);
        
        em.getTransaction().begin();
        em.persist(game);
        em.getTransaction().commit();

        em.clear();
        
        Game newGame = em.find(Game.class, game.getId());
        assertNotNull(newGame);
        // Verify the order
        Inning[] inningArr = (Inning[])game.getInnings().
            toArray(new Inning[9]);
        for (int i = 0; i < 9; i++) {
            assertEquals(inningArr[i],
                innings[8-i]);
        }
        em.close();        
    }


    /*
     * Validates the use of the updatable on OrderColumn.  insertable=false 
     * simply means the order column is omitted from the sql. Having the 
     * appropriate field mapping will enforce that. 
     */
    public void testOrderColumnInsertable() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        // Create a collection using secondary entities

        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "titles", 
            "titles_ORDER");

        validateOrderColumnInsertable(emf, BattingOrder.class, "fixedBatters", 
                false);
        
        em.close();
    }
    
    /*
     * Validates the use of the updatable on OrderColumn.  updatable=false 
     * simply means the order column is omitted from the sql. Having the 
     * appropriate field mapping will enforce that. 
     */
    public void testOrderColumnUpdateable() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Verify field name is the default via fm
        validateOrderColumnName(BattingOrder.class, "titles", 
            "titles_ORDER");

        validateOrderColumnUpdatable(emf, BattingOrder.class, "titles", 
            false);
        
        em.close();
    }

    /*
     * Validates the use of the OrderColumn with o2o, o2m, m2m relationships
     * and collection table - with and without join tables.
     */
    public void testOrderColumnTable() {   
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        validateOrderColumnTable(emf, Owner.class, "cars", 
            "OC_CAR", "car_o2m_order"); 
                
        validateOrderColumnTable(emf, Owner.class, "homes", 
             "home_o2m_table", "homes_ORDER"); 

        validateOrderColumnTable(emf, Owner.class, "bikeColl", 
             "bike_table", "bike_coll_order"); 

        validateOrderColumnTable(emf, Owner.class, "widgets", 
                "widget_m2m_table", "widgets_ORDER"); 

        Owner owner = new Owner();
        Collection<Car> cars = new ArrayList<Car>();
        Collection<Home> homes = new ArrayList<Home>();
        Collection<Bicycle> bicycles = new ArrayList<Bicycle>();
        Collection<Widget> widgets = new ArrayList<Widget>();
        Collection<Owner> owners = new ArrayList<Owner>(); 
        owner.setCars(cars);
        owner.setHomes(homes);
        owner.setBikeColl(bicycles);
        owner.setWidgets(widgets);
        
        for (int i = 0;  i < 5; i++){
            Car car = new Car(2000 + 1, "Make"+i, "Model"+i);
            car.setOwner(owner);
            cars.add(car);
            
            Home home = new Home(2000 + i);
            homes.add(home);
            
            Bicycle bike = new Bicycle("Brand"+i, "Model"+i);
            bicycles.add(bike);   
            
            Widget widget = new Widget("Name"+i);
            widgets.add(widget);
            widget.setOwners(owners);
        }
        
        Object[] carArr = cars.toArray();
        Object[] homeArr = homes.toArray();
        Object[] bikeArr = bicycles.toArray();
        Object[] widgetArr = widgets.toArray();
        
        em.getTransaction().begin();
        em.persist(owner);
        em.getTransaction().commit();
        String oid = owner.getId();
        
        em.clear();
        
        // Run queries to ensure the query component uses the correct tables
        validateIndexAndValues(em, "Owner", "cars", 0, 
                carArr, "id", 
                oid);

        validateIndexAndValues(em, "Owner", "homes", 0, 
                homeArr, "id", 
                oid);

        validateIndexAndValues(em, "Owner", "widgets", 0, 
                widgetArr, "id", 
                oid);

        validateIndexAndValues(em, "Owner", "bikeColl", 0, 
                bikeArr, "id", 
                oid);
        
        em.close();
    }    

    /*
     * Validates the use of order column (via INDEX) in the predicate of a
     * JPQL query.
     */
    public void testOrderColumnPredicateQuery() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Query and verify the result
        String queryString = "SELECT w FROM Course c JOIN c.waitList w " +
            "WHERE c.name = :cname AND INDEX(w) = :widx";
        Query qry = em.createQuery(queryString);
        qry.setParameter("widx", 0);
        qry.setParameter("cname", "Course B");
        Student idx0 = (Student)qry.getSingleResult();
        assertNotNull(idx0);
        assertEquals(idx0, students[10]);

        qry.setParameter("widx", 1);
        idx0 = (Student)qry.getSingleResult();
        assertNotNull(idx0);
        assertEquals(idx0, students[11]);

        qry.setParameter("cname", "Course A");
        qry.setParameter("widx", 0);
        idx0 = (Student)qry.getSingleResult();
        assertNotNull(idx0);
        assertEquals(idx0, students[11]);

        qry.setParameter("widx", 1);
        idx0 = (Student)qry.getSingleResult();
        assertNotNull(idx0);
        assertEquals(idx0, students[10]);  
        
        em.close();
    }

    /*
     * Validates the use of order column (via INDEX) in the projection of
     * a JPQL query.
     */
    public void testOrderColumnProjectionQuery() {
        
        OpenJPAEntityManagerSPI em = emf.createEntityManager();

        // Query and verify the result
        String queryString = "SELECT INDEX(w) FROM Course c JOIN c.waitList w" +
            " WHERE c.name = :cname ORDER BY w";
        Query qry = em.createQuery(queryString);
        qry.setParameter("cname", "Course A");
        List rlist = qry.getResultList();       
        assertNotNull(rlist);
        assertEquals(2, rlist.size());
        assertEquals(0l, rlist.get(0));
        assertEquals(1l, rlist.get(1));

        queryString = "SELECT INDEX(w) FROM Course c JOIN c.waitList w" +
            " WHERE c.name = :cname AND w.name = 'Student11'";
        qry = em.createQuery(queryString);
        qry.setParameter("cname", "Course B");
        Long idx = (Long)qry.getSingleResult();       
        assertNotNull(idx);
        assertEquals((Long)idx, (Long)1L);
        
        em.close();
    }
    
    /*
     * Create the data used by the query tests
     */
    private void createQueryData() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        // Add some data
        for (int i = 0; i < 12; i++) {
            students[i] = new Student("Student" + i);
        }
        Course courseA = new Course("Course A");
        Course courseB = new Course("Course B");
        
        HashSet<Course> courses = new HashSet<Course>();
        courses.add(courseA);
        courses.add(courseB);
        HashSet<Student> cAstudents = new HashSet<Student>(); 
        for (int i = 0; i < 5; i++) {
            cAstudents.add(students[i]);
            students[i].setCourses(courses);
        }
        courseA.setStudents(cAstudents);
        ArrayList<Student> cAwaitlist = new ArrayList<Student>();
        cAwaitlist.add(students[11]);
        cAwaitlist.add(students[10]);
        courseA.setWaitList(cAwaitlist);
        
        HashSet<Student> cBstudents = new HashSet<Student>(); 
        for (int i = 5; i < 10; i++) {
            cBstudents.add(students[i]);
        }
        courseB.setStudents(cBstudents);
        ArrayList<Student> cBwaitlist = new ArrayList<Student>();
        cBwaitlist.add(students[10]);
        cBwaitlist.add(students[11]);
        courseB.setWaitList(cBwaitlist);
        
        em.getTransaction().begin();
        em.persist(courseA);
        em.persist(courseB);
        em.getTransaction().commit();
        em.close();
    }
    
    private void validateIndexAndValues(OpenJPAEntityManagerSPI em, 
            String entity, String indexedCol, int base, Object[] objs, String
            idField, Object idValue) {
        String queryString =
            "SELECT INDEX(b), b FROM " + entity + " a JOIN a." + indexedCol
                + " b WHERE a." + idField + " = :idVal";
        em.clear();
        Query qry = em.createQuery(queryString);
        qry.setParameter("idVal", idValue);
        List rlist = qry.getResultList();  
        
        assertNotNull(rlist);
        assertEquals(objs.length, rlist.size()); 
        TreeMap<Long, Object> objMap = new TreeMap<Long, Object>();
        for (int i = 0; i < objs.length; i++)
        {
            Object[] rvals = (Object[])rlist.get(i);
            Long idx = (Long)rvals[0];
            Object objVal = rvals[1];
            objMap.put(idx, objVal);
        }
        for (int i = 0; i < objs.length; i++) {
            Object val = objMap.get((new Long(base + i)));
            assertEquals(val, objs[i]);
        }
    }

    private void validateCollIndexAndValues(OpenJPAEntityManagerSPI em, 
        String entity, String indexedCol, int base, Object[] objs, String
        idField, Object idValue) {
    String queryString =
        "SELECT INDEX(b), b FROM " + entity + " a, IN(a." + indexedCol
            + ") b WHERE a." + idField + " = :idVal";
    em.clear();
    Query qry = em.createQuery(queryString);
    qry.setParameter("idVal", idValue);
    List rlist = qry.getResultList();  
    
    assertNotNull(rlist);
    assertEquals(objs.length, rlist.size()); 
    TreeMap<Long, Object> objMap = new TreeMap<Long, Object>();
    for (int i = 0; i < objs.length; i++)
    {
        Object[] rvals = (Object[])rlist.get(i);
        Long idx = (Long)rvals[0];
        Object objVal = rvals[1];
        objMap.put(idx, objVal);
    }
    for (int i = 0; i < objs.length; i++) {
        Object val = objMap.get((new Long(base + i)));
        assertEquals(val, objs[i]);
    }
}

    private void validateOrderColumnName(Class clazz, String fieldName, 
            String columnName) {
        validateOrderColumnName(emf, clazz, fieldName, columnName);
    }
    
    private Column getOrderColumn(OpenJPAEntityManagerFactorySPI emf1, 
        Class clazz, String fieldName) {
        JDBCConfiguration conf = (JDBCConfiguration) emf1.getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(clazz, null, true);
        FieldMapping fm = cls.getFieldMapping(fieldName);
        Column oc = fm.getOrderColumn();
        assertNotNull(oc);
        return oc;
    }

    private void validateOrderColumnName(OpenJPAEntityManagerFactorySPI emf1, 
        Class clazz, String fieldName, String columnName) {        
        Column oc = getOrderColumn(emf1, clazz, fieldName);
        assertTrue(oc.getName().equalsIgnoreCase(columnName));
    }

    private void validateOrderColumnTable(
            OpenJPAEntityManagerFactorySPI emf1, 
            Class clazz, String fieldName, String tableName, 
            String columnName) {        
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            // Verify the oc has the correct table name
            assertTrue(oc.getTableName().equalsIgnoreCase(tableName));
            // Verify the table exists in the db
            assertTrue(tableAndColumnExists(emf1, null, tableName, null, 
                columnName));
    }

    private void validateOrderColumnUpdatable(
            OpenJPAEntityManagerFactorySPI emf1, Class clazz, String fieldName, 
            boolean updatable) {
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            assertEquals(updatable, !oc.getFlag(Column.FLAG_DIRECT_UPDATE));
    }

    private void validateOrderColumnInsertable(
            OpenJPAEntityManagerFactorySPI emf1, Class clazz, String fieldName, 
            boolean insertable) {
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            assertEquals(insertable, !oc.getFlag(Column.FLAG_DIRECT_INSERT));
    }

    /**
     * Method to verify a table was created for the given name and schema
     */
    private boolean tableAndColumnExists(OpenJPAEntityManagerFactorySPI emf1, 
            OpenJPAEntityManagerSPI em, String tableName, String schemaName,
            String columnName) {
        JDBCConfiguration conf = (JDBCConfiguration) emf1.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        OpenJPAEntityManagerSPI em1 = em;
                
        // If no em supplied, create one
        if (em1 == null) {
            em1 = emf1.createEntityManager();
        }
        Connection conn = (Connection)em1.getConnection();
        try {
            DatabaseMetaData dbmd = conn.getMetaData();
            // (meta, catalog, schemaName, tableName, conn)
            Column[] cols = dict.getColumns(dbmd, null, null, 
                    tableName, columnName, conn);
            if (cols != null && cols.length == 1) {
                Column col = cols[0];
                String colName = col.getName();
                if (col.getTableName().equalsIgnoreCase(tableName) &&
                    (schemaName == null || 
                    col.getSchemaName().equalsIgnoreCase(schemaName)) &&
                    colName.equalsIgnoreCase(columnName))
                    return true;
            }
        } catch (Throwable e) {
            fail("Unable to get column information.");
        } finally {
            if (em == null) {
                em1.close();
            }
        }
        return false;
    }
 }
