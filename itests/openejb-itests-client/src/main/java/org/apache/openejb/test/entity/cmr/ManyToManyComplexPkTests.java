/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmr;

import org.apache.openejb.test.entity.cmr.manytomany.GameLocal;
import org.apache.openejb.test.entity.cmr.manytomany.GameLocalHome;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformLocal;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformLocalHome;
import org.apache.openejb.test.entity.cmr.manytomany.GamePk;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformPk;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.TransactionRolledbackLocalException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class ManyToManyComplexPkTests extends AbstractCMRTest {
    private PlatformLocalHome platformLocalHome;
    private GameLocalHome gameLocalHome;

    public ManyToManyComplexPkTests() {
        super("ManyToManyComplex.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        platformLocalHome = (PlatformLocalHome) initialContext.lookup("client/tests/entity/cmr/manyToMany/ComplexPlatformLocal");
        gameLocalHome = (GameLocalHome) initialContext.lookup("client/tests/entity/cmr/manyToMany/ComplexGameLocal");
    }

    public void testAGetBExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            Set<GameLocal> gameSets = platform.getGames();
            assertEquals(2, gameSets.size());
            for (Iterator iter = gameSets.iterator(); iter.hasNext();) {
                GameLocal game = (GameLocal) iter.next();
                if (game.getId().equals(new Integer(11))) {
                    assertEquals("value11", game.getName());
                } else if (game.getId().equals(new Integer(22))) {
                    assertEquals("value22", game.getName());
                } else {
                    fail();
                }
            }
        } finally {
            completeTransaction();
        }
    }

    public void testSetCmrNull() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            try {
                platform.setGames(null);
                fail("expected platform.setGames(null) to throw an IllegalArgumentException");
            } catch (TransactionRolledbackLocalException e) {
                Throwable cause = e.getCause();
                assertNotNull("cause is null", cause);
                assertTrue("cause is not a instance of IllegalArgumentException", cause instanceof IllegalArgumentException);
            }
        } finally {
            completeTransaction();
        }
    }

    public void testBGetAExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            GameLocal game = findGame(new Integer(22));
            Set aSet = game.getPlatforms();
            assertEquals(3, aSet.size());
            for (Iterator iter = aSet.iterator(); iter.hasNext();) {
                PlatformLocal platform = (PlatformLocal) iter.next();
                if (platform.getId().equals(new Integer(1))) {
                    assertEquals("value1", platform.getName());
                } else if (platform.getId().equals(new Integer(2))) {
                    assertEquals("value2", platform.getName());
                } else if (platform.getId().equals(new Integer(3))) {
                    assertEquals("value3", platform.getName());
                } else {
                    fail();
                }
            }
        } finally {
            completeTransaction();
        }
    }

    public void testASetBDropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            platform.setGames(new HashSet<GameLocal>());
            platform = findPlatform(new Integer(2));
            platform.setGames(new HashSet<GameLocal>());
            platform = findPlatform(new Integer(3));
            platform.setGames(new HashSet<GameLocal>());
        } finally {
            completeTransaction();
        }

        assertAllUnlinked();
    }

    public void testBSetADropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            GameLocal game = findGame(new Integer(11));
            game.setPlatforms(new HashSet<PlatformLocal>());
            game = findGame(new Integer(22));
            game.setPlatforms(new HashSet<PlatformLocal>());
        } finally {
            completeTransaction();
        }

        assertAllUnlinked();
    }

    public void testASetBNewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = createPlatform(new Integer(4));
            GameLocal game = createGame(new Integer(33));
            Set<GameLocal> gameSets = platform.getGames();
            gameSets.add(game);
        } finally {
            completeTransaction();
        }

        assertLinked(4, 33);
    }

    public void testBSetANewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = createPlatform(new Integer(4));
            GameLocal game = createGame(new Integer(33));
            Set<PlatformLocal> platformSets = game.getPlatforms();
            platformSets.add(platform);
        } finally {
            completeTransaction();
        }

        assertLinked(4, 33);
    }

    public void testASetBExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = createPlatform(new Integer(4));
            GameLocal game = findGame(new Integer(11));
            Set<GameLocal> gameSets = platform.getGames();
            gameSets.add(game);
        } finally {
            completeTransaction();
        }

        assertLinked(4, 11);
    }

    public void testBSetAExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = createPlatform(new Integer(4));
            GameLocal game = findGame(new Integer(11));
            Set<PlatformLocal> platformSets = game.getPlatforms();
            platformSets.add(platform);
        } finally {
            completeTransaction();
        }

        assertLinked(4, 11);
    }

    public void testASetBExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            GameLocal game = createGame(new Integer(33));
            Set<GameLocal> gameSets = platform.getGames();
            gameSets.add(game);
        } finally {
            completeTransaction();
        }

        assertLinked(1, 33);
    }

    public void testBSetAExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            GameLocal game = createGame(new Integer(33));
            Set<PlatformLocal> platformSets = game.getPlatforms();
            platformSets.add(platform);
        } finally {
            completeTransaction();
        }

        assertLinked(1, 33);
    }

    public void testRemoveRelationships() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            platform.remove();
        } finally {
            completeTransaction();
        }

        assertPlatformDeleted(1);
    }

       public void testIllegalCmrCollectionArgument() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            Set games = platform.getGames();

            try {
                games.add(new Object());
                fail("expected games.add(new Object()) to throw an IllegalArgumentException");
            } catch (IllegalArgumentException e) {
            }

            try {
                games.addAll(Arrays.asList(new Object()));
                fail("expected games.addAll(Arrays.asList(new Object())) to throw an IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    public void testModifyCmrCollectionOusideTx() throws Exception {
        resetDB();
        beginTransaction();
        Set games;
        GameLocal newGame;
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            newGame = createGame(new Integer(33));
            games = platform.getGames();
        } finally {
            completeTransaction();
        }

        // CMR collections should still be readable
        assertFalse(games.isEmpty());
        assertEquals(2, games.size());
        for (Iterator iter = games.iterator(); iter.hasNext();) {
            GameLocal game = (GameLocal) iter.next();
            if (game.getId().equals(new Integer(11))) {
                assertEquals("value11", game.getName());
            } else if (game.getId().equals(new Integer(22))) {
                assertEquals("value22", game.getName());
            } else {
                fail();
            }
        }

        // But CMR collections should not be modifiable
        try {
            games.add(newGame);
            fail("expected games.add(game) to throw an IllegalStateException");
        } catch (IllegalStateException expected) {
        }
        try {
            games.addAll(Arrays.asList(newGame));
            fail("expected games.addAll(Arrays.asList(game)) to throw an IllegalStateException");
        } catch (IllegalStateException expected) {
        }
        try {
            games.remove(newGame);
            fail("expected games.remove(game) to throw an IllegalStateException");
        } catch (IllegalStateException expected) {
        }
        try {
            games.removeAll(Arrays.asList(newGame));
            fail("expected games.removeAll(game) to throw an IllegalStateException");
        } catch (IllegalStateException expected) {
        }
        Iterator iterator = games.iterator();
        try {
            iterator.remove();
            fail("expected iterator.remove() to throw an ConcurrentModificationException");
        } catch (ConcurrentModificationException expected) {
        }
    }

    public void testModifyCmrCollectionInNewTx() throws Exception {
        resetDB();
        beginTransaction();
        Set games;
        GameLocal newGame;
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            newGame = createGame(new Integer(33));
            games = platform.getGames();
        } finally {
            completeTransaction();
        }

        beginTransaction();
        try {
            // CMR collections should still be readable
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());
            for (Iterator iter = games.iterator(); iter.hasNext();) {
                GameLocal game = (GameLocal) iter.next();
                if (game.getId().equals(new Integer(11))) {
                    assertEquals("value11", game.getName());
                } else if (game.getId().equals(new Integer(22))) {
                    assertEquals("value22", game.getName());
                } else {
                    fail();
                }
            }

            // But CMR collections should not be modifiable
            try {
                games.add(newGame);
                fail("expected games.add(game) to throw an IllegalStateException");
            } catch (IllegalStateException expected) {
            }
            try {
                games.addAll(Arrays.asList(newGame));
                fail("expected games.addAll(Arrays.asList(game)) to throw an IllegalStateException");
            } catch (IllegalStateException expected) {
            }
            try {
                games.remove(newGame);
                fail("expected games.remove(game) to throw an IllegalStateException");
            } catch (IllegalStateException expected) {
            }
            try {
                games.removeAll(Arrays.asList(newGame));
                fail("expected games.removeAll(game) to throw an IllegalStateException");
            } catch (IllegalStateException expected) {
            }
            Iterator iterator = games.iterator();
            try {
                iterator.remove();
                fail("expected iterator.remove() to throw an ConcurrentModificationException");
            } catch (ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    public void testIteratorConcurrentModification() throws Exception {
        resetDB();
        beginTransaction();
        Set games;
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            GameLocal game = findGame(new Integer(11));
            games = platform.getGames();
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());

            Iterator iterator = games.iterator();

            games.remove(game);
            assertEquals(1, games.size());

            try {
                iterator.next();
                fail("expected iterator.next() to throw an ConcurrentModificationException");
            } catch (ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    public void testIteratorAndRemove() throws Exception {
        resetDB();
        beginTransaction();
        Set games;
        try {
            PlatformLocal platform = findPlatform(new Integer(1));
            GameLocal game = findGame(new Integer(11));
            games = platform.getGames();
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());

            Iterator iterator = games.iterator();

            assertTrue(games.contains(game));
            platform.remove();
            assertFalse(games.contains(game));
            assertEquals(0, games.size());

            try {
                iterator.next();
                fail("expected iterator.next() to throw an ConcurrentModificationException");
            } catch (ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    private void assertPlatformDeleted(int platformId) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();

        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexGame_ComplexPlatform WHERE Platforms_id=" + platformId);
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT COUNT(*) FROM ComplexPlatform WHERE id=" + platformId);
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();

        s.close();
        c.close();
    }

    private void assertAllUnlinked() throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();

        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexGame_ComplexPlatform");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();

        s.close();
        c.close();
    }

    private void assertLinked(int platformId, int gameId) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();

        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexGame_ComplexPlatform WHERE Platforms_id = " + platformId + " AND Games_id = " + gameId);
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        rs.close();

        rs = s.executeQuery("SELECT name FROM ComplexPlatform WHERE id = " + platformId);
        assertTrue(rs.next());
        assertEquals("value" + platformId, rs.getString(1));

        rs = s.executeQuery("SELECT name FROM ComplexGame WHERE id = " + gameId);
        assertTrue(rs.next());
        assertEquals("value" + gameId, rs.getString(1));
        rs.close();

        s.close();
        c.close();
    }

    private GameLocal createGame(int gameId) throws CreateException {
        GameLocal menu = gameLocalHome.create(new GamePk(gameId, "value" + gameId));
        return menu;
    }

    private GameLocal findGame(int gameId) throws FinderException {
        return gameLocalHome.findByPrimaryKey(new GamePk(gameId, "value" + gameId));
    }

    private PlatformLocal createPlatform(int platformId) throws CreateException {
        PlatformLocal platform = platformLocalHome.create(new PlatformPk(platformId, "value" + platformId));
        return platform;
    }

    private PlatformLocal findPlatform(int platformId) throws FinderException {
        return platformLocalHome.findByPrimaryKey(new PlatformPk(platformId, "value" + platformId));
    }

    private void resetDB() throws Exception {
        Connection connection = ds.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            try {
                statement.execute("DELETE FROM ComplexGame_ComplexPlatform");
            } catch (SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM ComplexGame");
            } catch (SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM ComplexPlatform");
            } catch (SQLException ignored) {
            }
        } finally {
            close(statement);
            close(connection);
        }

        beginTransaction();
        try {
            PlatformLocal platform1 = createPlatform(1);
            assertNotNull("platform1.getGames() is null", platform1.getGames());
            PlatformLocal platform2 = createPlatform(2);
            assertNotNull("platform2.getGames() is null", platform2.getGames());
            PlatformLocal platform3 = createPlatform(3);
            assertNotNull("platform3.getGames() is null", platform3.getGames());

            GameLocal game1 = createGame(11);
            assertNotNull("game1.getPlatforms() is null", game1.getPlatforms());
            GameLocal game2 = createGame(22);
            assertNotNull("game2.getPlatforms() is null", game2.getPlatforms());

            platform1.getGames().add(game1);

            platform1.getGames().add(game2);
            platform2.getGames().add(game2);
            platform3.getGames().add(game2);
        } finally {
            completeTransaction();
        }
    }

    protected void dump() throws SQLException {
        dumpTable(ds, "ComplexGame");
        dumpTable(ds, "ComplexPlatform");
        dumpTable(ds, "ComplexGame_ComplexPlatform");
    }
}
