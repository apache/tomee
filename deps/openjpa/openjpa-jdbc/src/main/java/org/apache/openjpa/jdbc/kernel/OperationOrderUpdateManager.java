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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.sql.PrimaryRow;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.RowManagerImpl;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Update manager that writes SQL in object-level operation order.
 *
 * @author Abe White
 */
public class OperationOrderUpdateManager
    extends AbstractUpdateManager {

    public boolean orderDirty() {
        return true;
    }

    protected RowManager newRowManager() {
        return new RowManagerImpl(true);
    }

    protected PreparedStatementManager newPreparedStatementManager
        (JDBCStore store, Connection conn) {
        return new PreparedStatementManagerImpl(store, conn);
    }

    protected Collection flush(RowManager rowMgr,
        PreparedStatementManager psMgr, Collection exceps) {
        RowManagerImpl rmimpl = (RowManagerImpl) rowMgr;

        // first take care of all secondary table deletes and 'all row' deletes
        // (which are probably secondary table deletes), since no foreign
        // keys ever rely on secondary table pks
        flush(rmimpl.getAllRowDeletes(), psMgr);
        flush(rmimpl.getSecondaryDeletes(), psMgr);

        // now do any 'all row' updates, which typically null keys
        flush(rmimpl.getAllRowUpdates(), psMgr);

        // gather any updates we need to avoid fk constraints on deletes
        Collection constraintUpdates = null;
        for (Iterator itr = rmimpl.getDeletes().iterator(); itr.hasNext();) {
            try {
                constraintUpdates = analyzeDeleteConstraints(rmimpl,
                    (PrimaryRow) itr.next(), constraintUpdates);
            } catch (SQLException se) {
                exceps = addException(exceps, SQLExceptions.getStore
                    (se, dict));
            }
        }
        if (constraintUpdates != null) {
            flush(constraintUpdates, psMgr);
            constraintUpdates.clear();
        }

        // flush primary rows in order
        for (Iterator itr = rmimpl.getOrdered().iterator(); itr.hasNext();) {
            try {
                constraintUpdates = flushPrimaryRow(rmimpl, (PrimaryRow)
                    itr.next(), psMgr, constraintUpdates);
            } catch (SQLException se) {
                exceps = addException(exceps, SQLExceptions.getStore
                    (se, dict));
            }
        }
        if (constraintUpdates != null)
            flush(constraintUpdates, psMgr);

        // take care of all secondary table inserts and updates last, since
        // they may rely on previous inserts or updates, but nothing relies
        // on them
        flush(rmimpl.getSecondaryUpdates(), psMgr);

        // flush any left over prepared statements
        psMgr.flush();
        return exceps;
    }

    /**
     * Analyze the delete constraints on the given row, gathering necessary
     * updates to null fks before deleting.
     */
    private Collection analyzeDeleteConstraints(RowManagerImpl rowMgr,
        PrimaryRow row, Collection updates)
        throws SQLException {
        if (!row.isValid())
            return updates;

        ForeignKey[] fks = row.getTable().getForeignKeys();
        OpenJPAStateManager sm;
        PrimaryRow rel;
        RowImpl update;
        for (int i = 0; i < fks.length; i++) {
            // when deleting ref fks we set the where value instead
            sm = row.getForeignKeySet(fks[i]);
            if (sm == null)
                sm = row.getForeignKeyWhere(fks[i]);
            if (sm == null)
                continue;

            // only need an update if we have an fk to a row that's being
            // deleted before we are
            rel = (PrimaryRow) rowMgr.getRow(fks[i].getPrimaryKeyTable(),
                Row.ACTION_DELETE, sm, false);
            if (rel == null || !rel.isValid()
                || rel.getIndex() >= row.getIndex())
                continue;

            // create an update to null the offending fk before deleting.  use
            // a primary row to be sure to copy delayed-flush pks/fks
            update = new PrimaryRow(row.getTable(), Row.ACTION_UPDATE, null);
            row.copyInto(update, true);
            update.setForeignKey(fks[i], row.getForeignKeyIO(fks[i]), null);
            if (updates == null)
                updates = new ArrayList();
            updates.add(update);
        }
        return updates;
    }

    /**
     * Flush the given row, creating deferred updates for dependencies.
     */
    private Collection flushPrimaryRow(RowManagerImpl rowMgr, PrimaryRow row,
        PreparedStatementManager psMgr, Collection updates)
        throws SQLException {
        if (!row.isValid())
            return updates;

        // already analyzed deletes
        if (row.getAction() == Row.ACTION_DELETE) {
            psMgr.flush(row);
            return updates;
        }

        ForeignKey[] fks = row.getTable().getForeignKeys();
        OpenJPAStateManager sm;
        PrimaryRow rel;
        PrimaryRow update;
        for (int i = 0; i < fks.length; i++) {
            sm = row.getForeignKeySet(fks[i]);
            if (sm == null)
                continue;

            // only need an update if we have an fk to a row that's being
            // inserted after we are; if row is dependent on itself and no
            // fk, must be an auto-inc because otherwise we wouldn't have
            // recorded it
            rel = (PrimaryRow) rowMgr.getRow(fks[i].getPrimaryKeyTable(),
                Row.ACTION_INSERT, sm, false);
            if (rel == null || !rel.isValid()
                || rel.getIndex() < row.getIndex()
                || (rel == row && !fks[i].isDeferred() && !fks[i].isLogical()))
                continue;

            // don't insert or update with the given fk; create a deferred
            // update for after the rel row has been inserted; use a primary row
            // to prevent setting values until after flush to get auto-inc
            update = new PrimaryRow(row.getTable(), Row.ACTION_UPDATE, null);
            if (row.getAction() == Row.ACTION_INSERT)
                update.wherePrimaryKey(row.getPrimaryKey());
            else
                row.copyInto(update, true);
            update.setForeignKey(fks[i], row.getForeignKeyIO(fks[i]), sm);
            row.clearForeignKey(fks[i]);

            if (updates == null)
                updates = new ArrayList();
            updates.add(update);
        }

        if (row.isValid()) // if update, maybe no longer needed
            psMgr.flush(row);
        return updates;
    }

    /**
     * Flush the given collection of secondary rows.
     */
    protected void flush(Collection rows, PreparedStatementManager psMgr) {
        if (rows.isEmpty())
            return;

        RowImpl row;
        for (Iterator itr = rows.iterator(); itr.hasNext();) {
            row = (RowImpl) itr.next();
            if (row.isValid())
                psMgr.flush(row);
        }
    }
}
