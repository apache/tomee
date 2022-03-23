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
import org.apache.openejb.test.entity.cmr.manytomany.GamePk;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformLocal;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformLocalHome;
import org.apache.openejb.test.entity.cmr.manytomany.PlatformPk;

import jakarta.ejb.CreateException;
import jakarta.ejb.FinderException;
import jakarta.ejb.TransactionRolledbackLocalException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Revision$ $Date$
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            final Set<GameLocal> gameSets = platform.getGames();
            assertEquals(2, gameSets.size());
            for (final Iterator iter = gameSets.iterator(); iter.hasNext(); ) {
                final GameLocal game = (GameLocal) iter.next();
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            try {
                platform.setGames(null);
                fail("expected platform.setGames(null) to throw an IllegalArgumentException");
            } catch (final TransactionRolledbackLocalException e) {
                final Throwable cause = e.getCause();
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
            final GameLocal game = findGame(new Integer(22));
            final Set aSet = game.getPlatforms();
            assertEquals(3, aSet.size());
            for (final Iterator iter = aSet.iterator(); iter.hasNext(); ) {
                final PlatformLocal platform = (PlatformLocal) iter.next();
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
            final PlatformLocal platform = createPlatform(new Integer(4));
            final GameLocal game = createGame(new Integer(33));
            final Set<GameLocal> gameSets = platform.getGames();
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
            final PlatformLocal platform = createPlatform(new Integer(4));
            final GameLocal game = createGame(new Integer(33));
            final Set<PlatformLocal> platformSets = game.getPlatforms();
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
            final PlatformLocal platform = createPlatform(new Integer(4));
            final GameLocal game = findGame(new Integer(11));
            final Set<GameLocal> gameSets = platform.getGames();
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
            final PlatformLocal platform = createPlatform(new Integer(4));
            final GameLocal game = findGame(new Integer(11));
            final Set<PlatformLocal> platformSets = game.getPlatforms();
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            final GameLocal game = createGame(new Integer(33));
            final Set<GameLocal> gameSets = platform.getGames();
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            final GameLocal game = createGame(new Integer(33));
            final Set<PlatformLocal> platformSets = game.getPlatforms();
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
            final PlatformLocal platform = findPlatform(new Integer(1));
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            final Set games = platform.getGames();

            try {
                games.add(new Object());
                fail("expected games.add(new Object()) to throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
            }

            try {
                games.addAll(Arrays.asList(new Object()));
                fail("expected games.addAll(Arrays.asList(new Object())) to throw an IllegalArgumentException");
            } catch (final IllegalArgumentException expected) {
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
            final PlatformLocal platform = findPlatform(new Integer(1));
            newGame = createGame(new Integer(33));
            games = platform.getGames();
        } finally {
            completeTransaction();
        }

        // CMR collections should still be readable
        assertFalse(games.isEmpty());
        assertEquals(2, games.size());
        for (final Iterator iter = games.iterator(); iter.hasNext(); ) {
            final GameLocal game = (GameLocal) iter.next();
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
        } catch (final IllegalStateException expected) {
        }
        try {
            games.addAll(Arrays.asList(newGame));
            fail("expected games.addAll(Arrays.asList(game)) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        try {
            games.remove(newGame);
            fail("expected games.remove(game) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        try {
            games.removeAll(Arrays.asList(newGame));
            fail("expected games.removeAll(game) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        final Iterator iterator = games.iterator();
        try {
            iterator.remove();
            fail("expected iterator.remove() to throw an ConcurrentModificationException");
        } catch (final ConcurrentModificationException expected) {
        }
    }

    public void testModifyCmrCollectionInNewTx() throws Exception {
        resetDB();
        beginTransaction();
        Set games;
        GameLocal newGame;
        try {
            final PlatformLocal platform = findPlatform(new Integer(1));
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
            for (final Iterator iter = games.iterator(); iter.hasNext(); ) {
                final GameLocal game = (GameLocal) iter.next();
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
            } catch (final IllegalStateException expected) {
            }
            try {
                games.addAll(Arrays.asList(newGame));
                fail("expected games.addAll(Arrays.asList(game)) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            try {
                games.remove(newGame);
                fail("expected games.remove(game) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            try {
                games.removeAll(Arrays.asList(newGame));
                fail("expected games.removeAll(game) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            final Iterator iterator = games.iterator();
            try {
                iterator.remove();
                fail("expected iterator.remove() to throw an ConcurrentModificationException");
            } catch (final ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    public void testIteratorConcurrentModification() throws Exception {
        resetDB();
        beginTransaction();
        final Set games;
        try {
            final PlatformLocal platform = findPlatform(new Integer(1));
            final GameLocal game = findGame(new Integer(11));
            games = platform.getGames();
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());

            final Iterator iterator = games.iterator();

            games.remove(game);
            assertEquals(1, games.size());

            try {
                iterator.next();
                fail("expected iterator.next() to throw an ConcurrentModificationException");
            } catch (final ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    public void testIteratorAndRemove() throws Exception {
        resetDB();
        beginTransaction();
        final Set games;
        try {
            final PlatformLocal platform = findPlatform(new Integer(1));
            final GameLocal game = findGame(new Integer(11));
            games = platform.getGames();
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());

            final Iterator iterator = games.iterator();

            assertTrue(games.contains(game));
            platform.remove();
            assertFalse(games.contains(game));
            assertEquals(0, games.size());

            try {
                iterator.next();
                fail("expected iterator.next() to throw an ConcurrentModificationException");
            } catch (final ConcurrentModificationException expected) {
            }
        } finally {
            completeTransaction();
        }
    }

    private void assertPlatformDeleted(final int platformId) throws Exception {
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();

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
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();

        final ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexGame_ComplexPlatform");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();

        s.close();
        c.close();
    }

    private void assertLinked(final int platformId, final int gameId) throws Exception {
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();

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

    private GameLocal createGame(final int gameId) throws CreateException {
        final GameLocal menu = gameLocalHome.create(new GamePk(gameId, "value" + gameId));
        return menu;
    }

    private GameLocal findGame(final int gameId) throws FinderException {
        return gameLocalHome.findByPrimaryKey(new GamePk(gameId, "value" + gameId));
    }

    private PlatformLocal createPlatform(final int platformId) throws CreateException {
        final PlatformLocal platform = platformLocalHome.create(new PlatformPk(platformId, "value" + platformId));
        return platform;
    }

    private PlatformLocal findPlatform(final int platformId) throws FinderException {
        return platformLocalHome.findByPrimaryKey(new PlatformPk(platformId, "value" + platformId));
    }

    private void resetDB() throws Exception {
        final Connection connection = ds.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            try {
                statement.execute("DELETE FROM ComplexGame_ComplexPlatform");
            } catch (final SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM ComplexGame");
            } catch (final SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM ComplexPlatform");
            } catch (final SQLException ignored) {
            }
        } finally {
            close(statement);
            close(connection);
        }

        beginTransaction();
        try {
            final PlatformLocal platform1 = createPlatform(1);
            assertNotNull("platform1.getGames() is null", platform1.getGames());
            final PlatformLocal platform2 = createPlatform(2);
            assertNotNull("platform2.getGames() is null", platform2.getGames());
            final PlatformLocal platform3 = createPlatform(3);
            assertNotNull("platform3.getGames() is null", platform3.getGames());

            final GameLocal game1 = createGame(11);
            assertNotNull("game1.getPlatforms() is null", game1.getPlatforms());
            final GameLocal game2 = createGame(22);
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
