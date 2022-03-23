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

import org.apache.openejb.test.entity.cmr.onetomany.ArtistLocal;
import org.apache.openejb.test.entity.cmr.onetomany.ArtistLocalHome;
import org.apache.openejb.test.entity.cmr.onetomany.ArtistPk;
import org.apache.openejb.test.entity.cmr.onetomany.SongLocal;
import org.apache.openejb.test.entity.cmr.onetomany.SongLocalHome;
import org.apache.openejb.test.entity.cmr.onetomany.SongPk;

import jakarta.ejb.CreateException;
import jakarta.ejb.FinderException;
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
public class OneToManyComplexPkTests extends AbstractCMRTest {
    private ArtistLocalHome artistLocalHome;
    private SongLocalHome songLocalHome;

    public OneToManyComplexPkTests() {
        super("OneToManyComplex.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        artistLocalHome = (ArtistLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToMany/ComplexArtistLocal");
        songLocalHome = (SongLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToMany/ComplexSongLocal");
    }

    public void test00_AGetBExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            final Set bSet = artist.getPerformed();
            assertEquals(2, bSet.size());
            for (final Object value : bSet) {
                final SongLocal song = (SongLocal) value;
                if (song.getId().equals(11)) {
                    assertEquals("value11", song.getName());
                } else if (song.getId().equals(22)) {
                    assertEquals("value22", song.getName());
                } else {
                    fail();
                }
            }
        } finally {
            completeTransaction();
        }
    }

    public void test01_BGetAExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            SongLocal song = findSong(11);
            ArtistLocal artist = song.getPerformer();
            assertNotNull(artist);
            assertEquals(new Integer(1), artist.getId());
            assertEquals("value1", artist.getName());

            song = findSong(22);
            artist = song.getPerformer();
            assertNotNull(artist);
            assertEquals(new Integer(1), artist.getId());
            assertEquals("value1", artist.getName());
        } finally {
            completeTransaction();
        }
    }

    public void test02_ASetBDropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            artist.setPerformed(new HashSet<SongLocal>());
        } finally {
            completeTransaction();
        }
        assertUnlinked(1);
    }

    public void test03_BSetADropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            SongLocal song = findSong(11);
            song.setPerformer(null);
            song = findSong(22);
            song.setPerformer(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }


    public void test04_ASetBNewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(2);
            final SongLocal song = findSong(22);
            final Set<SongLocal> songSets = new HashSet<SongLocal>();
            songSets.add(song);
            artist.setPerformed(songSets);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void test05_BSetANewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(2);
            final SongLocal song = findSong(22);
            song.setPerformer(artist);
        } finally {
            completeTransaction();
        }
        assertLinked(2, 22);
    }

    public void test06_ASetBExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(2);
            final SongLocal song = findSong(11);
            final Set<SongLocal> songSets = artist.getPerformed();
            songSets.add(song);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void test07_BSetAExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(2);
            final SongLocal song = findSong(11);
            song.setPerformer(artist);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void test08_ASetBExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            final SongLocal song = createSong(33);
            final Set<SongLocal> songSets = artist.getPerformed();
            songSets.add(song);
        } finally {
            completeTransaction();
        }
        assertLinked(1, 11, 22, 33);
    }

    public void test09_BSetAExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            final SongLocal song = createSong(33);
            song.setPerformer(artist);
        } finally {
            completeTransaction();
        }

        assertLinked(1, 11, 22, 33);
    }

    public void test10_RemoveRelationships() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final SongLocal song = findSong(11);
            final ArtistLocal artist = song.getPerformer();
            final Set<SongLocal> songs = artist.getPerformed();
            assertTrue(songs.contains(song));
            song.remove();
            assertFalse(songs.contains(song));
        } finally {
            completeTransaction();
        }
        assertLinked(1, 22);
        assertUnlinked(2);
    }

    // uncomment when cmp to cmr is supported
    public void TODO_testCMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final SongLocal song = findSong(11);

            final Integer field3 = song.getBpm();
            assertEquals(song.getPerformer().getPrimaryKey(), field3);
        } finally {
            completeTransaction();
        }
    }

    // uncomment when cmp to cmr is supported
    public void TODO_testSetCMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final SongLocal song = findSong(11);

            song.setBpm(2);

            final ArtistLocal artist = song.getPerformer();
            assertEquals(new Integer(2), artist.getId());
            assertEquals("value2", artist.getName());
        } finally {
            completeTransaction();
        }
    }

    public void test11_Delete() throws Exception {
        resetDB();

        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            artist.setPerformed(new HashSet<SongLocal>());
            final Set<SongLocal> songs = artist.getComposed();
            final Set<SongLocal> bsCopies = new HashSet<SongLocal>(songs);
            assertFalse(songs.isEmpty());
            artist.remove();
            assertTrue(songs.isEmpty());
            for (final SongLocal songLocal : bsCopies) {
                assertNull(songLocal.getComposer());
            }
        } finally {
            completeTransaction();
        }
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();
        final ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexSong");
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void test12_CascadeDelete() throws Exception {
        resetDB();

        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(1);
            final Set<SongLocal> songs = artist.getPerformed();
            assertFalse(songs.isEmpty());
            artist.remove();
            assertTrue(songs.isEmpty());
        } finally {
            completeTransaction();
        }
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();
        final ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexSong");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        rs.close();
        s.close();
        c.close();
    }

    public void testIllegalCmrCollectionArgument() throws Exception {
        resetDB();
        beginTransaction();
        try {
            final ArtistLocal artist = findArtist(new Integer(1));
            final Set songs = artist.getComposed();

            try {
                songs.add(new Object());
                fail("expected games.add(new Object()) to throw an IllegalArgumentException");
            } catch (final IllegalArgumentException e) {
            }

            try {
                songs.addAll(Arrays.asList(new Object()));
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
        Set songs;
        SongLocal newSong;
        try {
            final ArtistLocal artist = findArtist(new Integer(1));
            newSong = createSong(new Integer(33));
            songs = artist.getComposed();
        } finally {
            completeTransaction();
        }

        // CMR collections should still be readable
        assertFalse(songs.isEmpty());
        assertEquals(2, songs.size());
        for (final Iterator iter = songs.iterator(); iter.hasNext(); ) {
            final SongLocal song = (SongLocal) iter.next();
            if (song.getId().equals(new Integer(11))) {
                assertEquals("value11", song.getName());
            } else if (song.getId().equals(new Integer(22))) {
                assertEquals("value22", song.getName());
            } else {
                fail();
            }
        }

        // But CMR collections should not be modifiable
        try {
            songs.add(newSong);
            fail("expected songs.add(newSong) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        try {
            songs.addAll(Arrays.asList(newSong));
            fail("expected songs.addAll(Arrays.asList(newSong)) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        try {
            songs.remove(newSong);
            fail("expected songs.remove(newSong) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        try {
            songs.removeAll(Arrays.asList(newSong));
            fail("expected songs.removeAll(Arrays.asList(newSong)) to throw an IllegalStateException");
        } catch (final IllegalStateException expected) {
        }
        final Iterator iterator = songs.iterator();
        try {
            iterator.remove();
            fail("expected iterator.remove() to throw an ConcurrentModificationException");
        } catch (final ConcurrentModificationException expected) {
        }
    }

    public void testModifyCmrCollectionInNewTx() throws Exception {
        resetDB();
        beginTransaction();
        Set songs;
        SongLocal newSong;
        try {
            final ArtistLocal artist = findArtist(new Integer(1));
            newSong = createSong(new Integer(33));
            songs = artist.getComposed();
        } finally {
            completeTransaction();
        }

        beginTransaction();
        try {
            // CMR collections should still be readable
            assertFalse(songs.isEmpty());
            assertEquals(2, songs.size());
            for (final Iterator iter = songs.iterator(); iter.hasNext(); ) {
                final SongLocal song = (SongLocal) iter.next();
                if (song.getId().equals(new Integer(11))) {
                    assertEquals("value11", song.getName());
                } else if (song.getId().equals(new Integer(22))) {
                    assertEquals("value22", song.getName());
                } else {
                    fail();
                }
            }

            // But CMR collections should not be modifiable
            try {
                songs.add(newSong);
                fail("expected songs.add(newSong) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            try {
                songs.addAll(Arrays.asList(newSong));
                fail("expected songs.addAll(Arrays.asList(newSong)) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            try {
                songs.remove(newSong);
                fail("expected songs.remove(newSong) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            try {
                songs.removeAll(Arrays.asList(newSong));
                fail("expected songs.removeAll(Arrays.asList(newSong)) to throw an IllegalStateException");
            } catch (final IllegalStateException expected) {
            }
            final Iterator iterator = songs.iterator();
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
        try {
            final ArtistLocal artist = findArtist(new Integer(1));
            final SongLocal song = findSong(new Integer(11));
            final Set songs = artist.getComposed();
            assertFalse(songs.isEmpty());
            assertEquals(2, songs.size());

            final Iterator iterator = songs.iterator();

            songs.remove(song);
            assertEquals(1, songs.size());

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
        try {
            final ArtistLocal artist = findArtist(new Integer(1));
            final SongLocal song = findSong(new Integer(11));
            final Set games = artist.getComposed();
            assertFalse(games.isEmpty());
            assertEquals(2, games.size());

            final Iterator iterator = games.iterator();

            assertTrue(games.contains(song));
            artist.remove();
            assertFalse(games.contains(song));
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

    private ArtistLocal createArtist(final int artistId) throws CreateException {
        final ArtistLocal artist = artistLocalHome.create(new ArtistPk(artistId, "value" + artistId));
        return artist;
    }

    private ArtistLocal findArtist(final int artistId) throws FinderException {
        return artistLocalHome.findByPrimaryKey(new ArtistPk(artistId, "value" + artistId));
    }

    private SongLocal createSong(final int songId) throws CreateException {
        final SongLocal song = songLocalHome.create(new SongPk(songId, "value" + songId));
        return song;
    }

    private SongLocal findSong(final int songId) throws FinderException {
        return songLocalHome.findByPrimaryKey(new SongPk(songId, "value" + songId));
    }

    private void assertLinked(final int artistId, final int... songIds) throws Exception {
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT name FROM ComplexArtist WHERE id = " + artistId);
        assertTrue(rs.next());
        assertEquals("value" + artistId, rs.getString("name"));
        close(rs);

        // assert that there we are looking for the same number of linked beans
        rs = s.executeQuery("SELECT COUNT(*) FROM ComplexSong WHERE performer_id = 1");
        assertTrue(rs.next());
        assertEquals(songIds.length, rs.getInt(1));
        rs.close();

        // assert each of the listed b pks is linked to a
        for (final int songId : songIds) {
            rs = s.executeQuery("SELECT name, performer_id FROM ComplexSong WHERE id = " + songId);
            assertTrue(rs.next());
            assertEquals("value" + songId, rs.getString("name"));
            assertEquals(artistId, rs.getInt("performer_id"));
            close(rs);
        }
        close(s);
        close(c);
    }

    private void assertUnlinked(final int aPk) throws Exception {
        final Connection c = ds.getConnection();
        final Statement s = c.createStatement();
        final ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM ComplexSong WHERE performer_id = " + aPk);
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }

    private void resetDB() throws Exception {
        final Connection connection = ds.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            try {
                statement.execute("DELETE FROM ComplexArtist");
            } catch (final SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM ComplexSong");
            } catch (final SQLException ignored) {
            }
        } finally {
            close(statement);
            close(connection);
        }

        final ArtistLocal artist1 = createArtist(1);
        createArtist(2);

        final SongLocal song1 = createSong(11);
        final SongLocal song2 = createSong(22);

        song1.setPerformer(artist1);
        song2.setPerformer(artist1);

        song1.setComposer(artist1);
        song2.setComposer(artist1);
    }

    protected void dump() throws SQLException {
        dumpTable(ds, "ComplexArtist");
        dumpTable(ds, "ComplexSong");
    }
}
