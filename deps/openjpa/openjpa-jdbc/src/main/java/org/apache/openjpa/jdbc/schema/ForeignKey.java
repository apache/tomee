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
package org.apache.openjpa.jdbc.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Represents a database foreign key; may be a logical key with no
 * database representation. This class can also represent a partial key,
 * aligning with {@link DatabaseMetaData}.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class ForeignKey
    extends Constraint {

    /**
     * Logical foreign key; links columns, but does not perform any action
     * when the joined primary key columns are modified.
     */
    public static final int ACTION_NONE = 1;

    /**
     * Throw an exception if joined primary key columns are modified.
     */
    public static final int ACTION_RESTRICT = 2;

    /**
     * Cascade any modification of the joined primary key columns to
     * this table. If the joined primary key row is deleted, the row in this
     * table will also be deleted.
     */
    public static final int ACTION_CASCADE = 3;

    /**
     * Null the local columns if the joined primary key columns are modified.
     */
    public static final int ACTION_NULL = 4;

    /**
     * Set the local columns to their default values if the primary key
     * columns are modified.
     */
    public static final int ACTION_DEFAULT = 5;

    private static final Localizer _loc = 
        Localizer.forPackage(ForeignKey.class);

    private DBIdentifier _pkTableName = DBIdentifier.NULL;
    private DBIdentifier _pkSchemaName = DBIdentifier.NULL;
    private DBIdentifier _pkColumnName = DBIdentifier.NULL;
    private int _seq = 0;

    private LinkedHashMap _joins = null;
    private LinkedHashMap _joinsPK = null;
    private LinkedHashMap _consts = null;
    private LinkedHashMap _constsPK = null;
    private int _delAction = ACTION_NONE;
    private int _upAction = ACTION_NONE;
    private int _index = 0;

    // cached items
    private Column[] _locals = null;
    private Column[] _pks = null;
    private Object[] _constVals = null;
    private Column[] _constCols = null;
    private Object[] _constValsPK = null;
    private Column[] _constColsPK = null;
    private Table _pkTable = null;
    private Boolean _autoAssign = null;

    /**
     * Return the foreign key action constant for the given action name.
     */
    public static int getAction(String name) {
        if (name == null || "none".equalsIgnoreCase(name))
            return ACTION_NONE;
        if ("cascade".equalsIgnoreCase(name))
            return ACTION_CASCADE;
        if ("default".equalsIgnoreCase(name))
            return ACTION_DEFAULT;
        if ("restrict".equalsIgnoreCase(name)
            || "exception".equalsIgnoreCase(name))
            return ACTION_RESTRICT;
        if ("null".equalsIgnoreCase(name))
            return ACTION_NULL;

        // not a recognized action; check for typo
        List recognized = Arrays.asList(new String[]{ "none", "exception",
            "restrict", "cascade", "null", "default", });
        String closest = StringDistance.getClosestLevenshteinDistance(name,
            recognized, .5F);

        String msg;
        if (closest != null)
            msg = _loc.get("bad-fk-action-hint", name, closest, recognized)
                .getMessage();
        else
            msg = _loc.get("bad-fk-action", name, recognized).getMessage();
        throw new IllegalArgumentException(msg);
    }

    /**
     * Return the foreign key action name for the given action constant.
     */
    public static String getActionName(int action) {
        switch (action) {
            case ACTION_NONE:
                return "none";
            case ACTION_RESTRICT:
                return "restrict";
            case ACTION_CASCADE:
                return "cascade";
            case ACTION_DEFAULT:
                return "default";
            case ACTION_NULL:
                return "null";
            default:
                throw new IllegalArgumentException(String.valueOf(action));
        }
    }

    /**
     * Default constructor.
     */
    public ForeignKey() {
    }

    /**
     * Constructor.
     *
     * @param name the foreign key name, if any
     * @param table the local table of the foreign key
     * @deprecated
     */
    public ForeignKey(String name, Table table) {
        super(name, table);
    }

    public ForeignKey(DBIdentifier name, Table table) {
        super(name, table);
    }

    public boolean isLogical() {
        return _delAction == ACTION_NONE;
    }

    /**
     * Whether the primary key columns of this key are auto-incrementing, or
     * whether they themselves are members of a foreign key who's primary key
     * is auto-incrementing (recursing to arbitrary depth).
     */
    public boolean isPrimaryKeyAutoAssigned() {
        if (_autoAssign != null)
            return _autoAssign.booleanValue();
        return isPrimaryKeyAutoAssigned(new ArrayList(3));
    }

    /**
     * Helper to calculate whether this foreign key depends on auto-assigned 
     * columns.  Recurses appropriately if the primary key columns this key
     * joins to are themselves members of a foreign key that is dependent on
     * auto-assigned columns.  Caches calculated auto-assign value as a side 
     * effect.
     *
     * @param seen track seen foreign keys to prevent infinite recursion in
     * the case of foreign key cycles
     */
    private boolean isPrimaryKeyAutoAssigned(List seen) {
        if (_autoAssign != null) 
            return _autoAssign.booleanValue();

        Column[] cols = getPrimaryKeyColumns();
        if (cols.length == 0) {
            _autoAssign = Boolean.FALSE;
            return false;
        }

        for (int i = 0; i < cols.length; i++) {
            if (cols[i].isAutoAssigned()) {
                _autoAssign = Boolean.TRUE;
                return true;
            }
        }

        ForeignKey[] fks = _pkTable.getForeignKeys();
        seen.add(this);
        for (int i = 0; i < cols.length; i++) {
            for (int j = 0; j < fks.length; j++) {
                if (!fks[j].containsColumn(cols[i]))
                    continue;
                if (!seen.contains(fks[j])
                    && fks[j].isPrimaryKeyAutoAssigned(seen)) {
                    _autoAssign = Boolean.TRUE;
                    return true;
                }
            }
        }

        _autoAssign = Boolean.FALSE;
        return false;
    }

    /**
     * The name of the primary key table.
     * @deprecated
     */
    public String getPrimaryKeyTableName() {
        return getPrimaryKeyTableIdentifier().getName();
    }

    public DBIdentifier getPrimaryKeyTableIdentifier() {
        Table table = getPrimaryKeyTable();
        if (table != null)
            return table.getIdentifier();
        return _pkTableName == null ? DBIdentifier.NULL : _pkTableName;
    }

    /**
     * The name of the primary key table. You can only set the primary
     * key table name on foreign keys that have not already been joined.
     * @deprecated
     */
    public void setPrimaryKeyTableName(String pkTableName) {
        setPrimaryKeyTableIdentifier(DBIdentifier.newTable(pkTableName));
    }

    public void setPrimaryKeyTableIdentifier(DBIdentifier pkTableName) {
        if (getPrimaryKeyTable() != null)
            throw new IllegalStateException();
        _pkTableName = pkTableName;
    }

    /**
     * The name of the primary key table's schema.
     * @deprecated
     */
    public String getPrimaryKeySchemaName() {
        return getPrimaryKeySchemaIdentifier().getName();
    }

    public DBIdentifier getPrimaryKeySchemaIdentifier() {
        Table table = getPrimaryKeyTable();
        if (table != null)
            return table.getSchemaIdentifier();
        return _pkSchemaName;
    }

    /**
     * The name of the primary key table's schema. You can only set the
     * primary key schema name on foreign keys that have not already been
     * joined.
     * @deprecated
     */
    public void setPrimaryKeySchemaName(String pkSchemaName) {
        setPrimaryKeySchemaIdentifier(DBIdentifier.newSchema(pkSchemaName));
    }

    public void setPrimaryKeySchemaIdentifier(DBIdentifier pkSchemaName) {
        if (getPrimaryKeyTable() != null)
            throw new IllegalStateException();
        _pkSchemaName = pkSchemaName;
    }

    /**
     * The name of the primary key column.
     * @deprecated
     */
    public String getPrimaryKeyColumnName() {
        return getPrimaryKeyColumnIdentifier().getName();
    }

    public DBIdentifier getPrimaryKeyColumnIdentifier() {
        return _pkColumnName == null ? DBIdentifier.NULL : _pkColumnName;
    }

    /**
     * The name of the primary key column. You can only set the
     * primary key column name on foreign keys that have not already been
     * joined.
     * @deprecated
     */
    public void setPrimaryKeyColumnName(String pkColumnName) {
        setPrimaryKeyColumnIdentifier(DBIdentifier.newColumn(pkColumnName));
    }

    public void setPrimaryKeyColumnIdentifier(DBIdentifier pkColumnName) {
        if (getPrimaryKeyTable() != null)
            throw new IllegalStateException();
        _pkColumnName = pkColumnName;
    }

    /**
     * The sequence of this join in the foreign key.
     */
    public int getKeySequence() {
        return _seq;
    }

    /**
     * The sequence of this join in the foreign key.
     */
    public void setKeySequence(int seq) {
        _seq = seq;
    }

    /**
     * Return the delete action for the key. Will be one of:
     * {@link #ACTION_NONE}, {@link #ACTION_RESTRICT},
     * {@link #ACTION_CASCADE}, {@link #ACTION_NULL}, {@link #ACTION_DEFAULT}.
     */
    public int getDeleteAction() {
        return _delAction;
    }

    /**
     * Set the delete action for the key. Must be one of:
     * {@link #ACTION_NONE}, {@link #ACTION_RESTRICT},
     * {@link #ACTION_CASCADE}, {@link #ACTION_NULL}, {@link #ACTION_DEFAULT}.
     */
    public void setDeleteAction(int action) {
        _delAction = action;
        if (action == ACTION_NONE)
            _upAction = ACTION_NONE;
        else if (_upAction == ACTION_NONE)
            _upAction = ACTION_RESTRICT;
    }

    /**
     * Return the update action for the key. Will be one of:
     * {@link #ACTION_NONE}, {@link #ACTION_RESTRICT},
     * {@link #ACTION_CASCADE}, {@link #ACTION_NULL}, {@link #ACTION_DEFAULT}.
     */
    public int getUpdateAction() {
        return _upAction;
    }

    /**
     * Set the update action for the key. Must be one of:
     * {@link #ACTION_NONE}, {@link #ACTION_RESTRICT},
     * {@link #ACTION_CASCADE}, {@link #ACTION_NULL}, {@link #ACTION_DEFAULT}.
     */
    public void setUpdateAction(int action) {
        _upAction = action;
        if (action == ACTION_NONE)
            _delAction = ACTION_NONE;
        else if (_delAction == ACTION_NONE)
            _delAction = ACTION_RESTRICT;
    }

    /**
     * Return the foreign key's 0-based index in the owning table.
     */
    public int getIndex() {
        Table table = getTable();
        if (table != null)
            table.indexForeignKeys();
        return _index;
    }

    /**
     * Set the foreign key's 0-based index in the owning table.
     */
    void setIndex(int index) {
        _index = index;
    }

    /**
     * Return the primary key column joined to the given local column.
     */
    public Column getPrimaryKeyColumn(Column local) {
        return (_joins == null) ? null : (Column) _joins.get(local);
    }

    /**
     * Return the local column joined to the given primary key column.
     */
    public Column getColumn(Column pk) {
        return (_joinsPK == null) ? null : (Column) _joinsPK.get(pk);
    }

    /**
     * Return the constant value assigned to the given local column.
     */
    public Object getConstant(Column local) {
        return (_consts == null) ? null : _consts.get(local);
    }

    /**
     * Return the constant value assigned to the given primary key column.
     */
    public Object getPrimaryKeyConstant(Column pk) {
        return (_constsPK == null) ? null : _constsPK.get(pk);
    }

    /**
     * Return the local columns in the foreign key local table order.
     */
    public Column[] getColumns() {
        if (_locals == null)
            _locals = (_joins == null) ? Schemas.EMPTY_COLUMNS : (Column[])
                _joins.keySet().toArray(new Column[_joins.size()]);
        return _locals;
    }

    /**
     * Return the constant values assigned to the local columns
     * returned by {@link #getConstantColumns}.
     */
    public Object[] getConstants() {
        if (_constVals == null)
            _constVals = (_consts == null) ? Schemas.EMPTY_VALUES
                : _consts.values().toArray();
        return _constVals;
    }

    /**
     * Return the constant values assigned to the primary key columns
     * returned by {@link #getConstantPrimaryKeyColumns}.
     */
    public Object[] getPrimaryKeyConstants() {
        if (_constValsPK == null)
            _constValsPK = (_constsPK == null) ? Schemas.EMPTY_VALUES
                : _constsPK.values().toArray();
        return _constValsPK;
    }

    /**
     * Return true if the fk includes the given local column.
     */
    public boolean containsColumn(Column col) {
        return _joins != null && _joins.containsKey(col);
    }

    /**
     * Return true if the fk includes the given primary key column.
     */
    public boolean containsPrimaryKeyColumn(Column col) {
        return _joinsPK != null && _joinsPK.containsKey(col);
    }

    /**
     * Return true if the fk includes the given local column.
     */
    public boolean containsConstantColumn(Column col) {
        return _consts != null && _consts.containsKey(col);
    }

    /**
     * Return true if the fk includes the given primary key column.
     */
    public boolean containsConstantPrimaryKeyColumn(Column col) {
        return _constsPK != null && _constsPK.containsKey(col);
    }

    /**
     * Return the foreign columns in the foreign key, in join-order with
     * the result of {@link #getColumns}.
     */
    public Column[] getPrimaryKeyColumns() {
        if (_pks == null)
            _pks = (_joins == null) ? Schemas.EMPTY_COLUMNS : (Column[])
                _joins.values().toArray(new Column[_joins.size()]);
        return _pks;
    }

    /**
     * Return the local columns that we link to using constant values.
     */
    public Column[] getConstantColumns() {
        if (_constCols == null)
            _constCols = (_consts == null) ? Schemas.EMPTY_COLUMNS : (Column[])
                _consts.keySet().toArray(new Column[_consts.size()]);
        return _constCols;
    }

    /**
     * Return the primary key columns that we link to using constant values.
     */
    public Column[] getConstantPrimaryKeyColumns() {
        if (_constColsPK == null)
            _constColsPK = (_constsPK == null) ? Schemas.EMPTY_COLUMNS :
                (Column[]) _constsPK.keySet().toArray
                    (new Column[_constsPK.size()]);
        return _constColsPK;
    }

    /**
     * Set the foreign key's joins.
     */
    public void setJoins(Column[] cols, Column[] pkCols) {
        Column[] cur = getColumns();
        for (int i = 0; i < cur.length; i++)
            removeJoin(cur[i]);

        if (cols != null)
            for (int i = 0; i < cols.length; i++)
                join(cols[i], pkCols[i]);
    }

    /**
     * Set the foreign key's constant joins.
     */
    public void setConstantJoins(Object[] consts, Column[] pkCols) {
        Column[] cur = getConstantPrimaryKeyColumns();
        for (int i = 0; i < cur.length; i++)
            removeJoin(cur[i]);

        if (consts != null)
            for (int i = 0; i < consts.length; i++)
                joinConstant(consts[i], pkCols[i]);
    }

    /**
     * Set the foreign key's constant joins.
     */
    public void setConstantJoins(Column[] cols, Object[] consts) {
        Column[] cur = getConstantColumns();
        for (int i = 0; i < cur.length; i++)
            removeJoin(cur[i]);

        if (consts != null)
            for (int i = 0; i < consts.length; i++)
                joinConstant(cols[i], consts[i]);
    }

    /**
     * Join a local column to a primary key column of another table.
     */
    public void join(Column local, Column toPK) {
        if (!ObjectUtils.equals(local.getTable(), getTable()))
            throw new InvalidStateException(_loc.get("table-mismatch",
                local.getTable(), getTable()));

        Table pkTable = toPK.getTable();
        if (_pkTable != null && !_pkTable.equals(pkTable))
            throw new InvalidStateException(_loc.get("fk-mismatch",
                pkTable, _pkTable));

        _pkTable = pkTable;
        if (_joins == null)
            _joins = new LinkedHashMap();
        _joins.put(local, toPK);
        local.addConstraint(this);
        if (_joinsPK == null)
            _joinsPK = new LinkedHashMap();
        _joinsPK.put(toPK, local);

        // force re-cache
        _locals = null;
        _pks = null;
        if (Boolean.FALSE.equals(_autoAssign))
            _autoAssign = null;
    }

    /**
     * Join a constant value to a primary key column of another table. The
     * constant must be either a string or a number.
     */
    public void joinConstant(Object val, Column toPK) {
        Table pkTable = toPK.getTable();
        if (_pkTable != null && !_pkTable.equals(pkTable))
            throw new InvalidStateException(_loc.get("fk-mismatch",
                pkTable, _pkTable));

        _pkTable = pkTable;
        if (_constsPK == null)
            _constsPK = new LinkedHashMap();
        _constsPK.put(toPK, val);

        // force re-cache
        _constValsPK = null;
        _constColsPK = null;
    }

    /**
     * Join a constant value to a local column of this table. The
     * constant must be either a string or a number.
     */
    public void joinConstant(Column col, Object val) {
        if (_consts == null)
            _consts = new LinkedHashMap();
        _consts.put(col, val);

        // force re-cache
        _constVals = null;
        _constCols = null;
    }

    /**
     * Remove any joins inolving the given column.
     *
     * @return true if the join was removed, false if not part of the key
     */
    public boolean removeJoin(Column col) {
        boolean remd = false;
        Object rem;

        if (_joins != null) {
            rem = _joins.remove(col);
            col.removeConstraint(this);
            if (rem != null) {
                _locals = null;
                _pks = null;
                _joinsPK.remove(rem);
                remd = true;
            }
        }

        if (_joinsPK != null) {
            rem = _joinsPK.remove(col);
            if (rem != null) {
                _locals = null;
                _pks = null;
                _joins.remove(rem);
                remd = true;
            }
        }

        if (_consts != null) {
            if (_consts.remove(col) != null) {
                _constVals = null;
                _constCols = null;
                remd = true;
            }
        }

        if (_constsPK != null) {
            if (_constsPK.containsKey(col)) {
                _constsPK.remove(col);
                _constValsPK = null;
                _constColsPK = null;
                remd = true;
            }
        }

        if ((_joins == null || _joins.isEmpty())
            && (_constsPK == null || _constsPK.isEmpty()))
            _pkTable = null;
        if (remd && Boolean.TRUE.equals(_autoAssign))
            _autoAssign = null;
        return remd;
    }

    /**
     * Returns the table this foreign key is linking to, if it is known yet.
     */
    public Table getPrimaryKeyTable() {
        return _pkTable;
    }

    /**
     * Ref all columns in this key.
     */
    public void refColumns() {
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].ref();
        cols = getConstantColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].ref();
    }

    /**
     * Deref all columns in this key.
     */
    public void derefColumns() {
        Column[] cols = getColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].deref();
        cols = getConstantColumns();
        for (int i = 0; i < cols.length; i++)
            cols[i].deref();
    }

    /**
     * Foreign keys are equal if the satisfy the equality constraints of
     * {@link Constraint} and they have the same local and primary key
     * columns and action.
     */
    public boolean equalsForeignKey(ForeignKey fk) {
        if (fk == this)
            return true;
        if (fk == null)
            return false;

        if (getDeleteAction() != fk.getDeleteAction())
            return false;
        if (isDeferred() != fk.isDeferred())
            return false;

        if (!columnsMatch(fk.getColumns(), fk.getPrimaryKeyColumns()))
            return false;
        if (!match(getConstantColumns(), fk.getConstantColumns()))
            return false;
        if (!match(getConstants(), fk.getConstants()))
            return false;
        if (!match(getConstantPrimaryKeyColumns(),
            fk.getConstantPrimaryKeyColumns()))
            return false;
        if (!match(getPrimaryKeyConstants(), fk.getPrimaryKeyConstants()))
            return false;
        return true;
    }

    /**
     * Return true if the given local and foreign columns match those
     * on this key. This can be used to find foreign keys given only
     * column linking information.
     */
    public boolean columnsMatch(Column[] fkCols, Column[] fkPKCols) {
        return match(getColumns(), fkCols)
            && match(getPrimaryKeyColumns(), fkPKCols);
    }

    /**
     * Checks for non-nullable local columns.
     */
    public boolean hasNotNullColumns() {
      Column[] columns = getColumns();
      for (int j = 0; j < columns.length; j++) {
          if (columns[j].isNotNull()) {
              return true;
          }
      }
      return false;
    }
    
    private static boolean match(Column[] cols, Column[] fkCols) {
        if (cols.length != fkCols.length)
            return false;
        for (int i = 0; i < fkCols.length; i++)
            if (!hasColumn(cols, fkCols[i]))
                return false;
        return true;
    }

    private static boolean hasColumn(Column[] cols, Column col) {
        for (int i = 0; i < cols.length; i++)
            if (cols[i].getQualifiedPath().equals(col.getQualifiedPath()))
                return true;
        return false;
    }

    private static boolean match(Object[] vals, Object[] fkVals) {
        if (vals.length != fkVals.length)
            return false;
        for (int i = 0; i < vals.length; i++)
            if (!ObjectUtils.equals(vals[i], fkVals[i]))
                return false;
        return true;
    }
    
    /**
     * Return the name of the foreignkey constraint as defined in the database.
     * @deprecated
     */
    public String loadNameFromDB(DBDictionary dbdict, Connection conn) {
        return loadIdentifierFromDB(dbdict, conn).getName();
    }

    public DBIdentifier loadIdentifierFromDB(DBDictionary dbdict, Connection conn) {
        if( isLogical() || getTable() == null)
            return DBIdentifier.NULL;
        DBIdentifier retVal = DBIdentifier.NULL;
        try{
            Schema schema = getTable().getSchema();
            ForeignKey[] fks = dbdict.getImportedKeys(conn.getMetaData(), 
                DBIdentifier.newCatalog(conn.getCatalog()), schema.getIdentifier(), 
                getTable().getIdentifier(), conn, false);
            for ( int i=0; i< fks.length; i++) {
                Table localtable = schema.getTable(fks[i].getTableIdentifier());
                Table pkTable = schema.getTable(
                    fks[i].getPrimaryKeyTableIdentifier());
                boolean addFK = false;
                ForeignKey fkTemp = localtable.getForeignKey(
                    fks[i].getIdentifier());
                if( fkTemp == null) {
                    addFK=true;
                    fkTemp = localtable.addForeignKey(
                        fks[i].getIdentifier());
                    fkTemp.setDeferred(fks[i].isDeferred());
                    fkTemp.setDeleteAction(fks[i].getDeleteAction());
                }
                if (fks[i].getColumns() == null || fks[i].getColumns().length == 0) {
                    // Singular column foreign key 
                    if( ! fkTemp.containsColumn(
                        localtable.getColumn(fks[i].getColumnIdentifier())))
                    fkTemp.join(localtable.getColumn(fks[i].getColumnIdentifier()), 
                        pkTable.getColumn(fks[i].getPrimaryKeyColumnIdentifier()));
                } else {
                    // Add the multi-column foreign key, joining local and pk columns in
                    // the temporary key
                    Column[] locCols = fks[i].getColumns();
                    Column[] pkCols = fks[i].getPrimaryKeyColumns();
                    // Column counts must match
                    if (locCols != null && pkCols != null &&
                        locCols.length != pkCols.length) {
                        Log log = dbdict.getLog();
                        if (log.isTraceEnabled()) {
                            log.trace(_loc.get("fk-column-mismatch"));
                        }
                    }
                    for (int j = 0; j < locCols.length; j++) {
                        if( ! fkTemp.containsColumn(
                            localtable.getColumn(locCols[j].getIdentifier()))) {
                            fkTemp.join(localtable.getColumn(locCols[j].getIdentifier()), 
                                pkTable.getColumn(pkCols[j].getIdentifier()));
                        }
                    }
                }
                if( equalsForeignKey(fkTemp))
                {
                    if(addFK)
                        localtable.removeForeignKey(fkTemp);
                    retVal = fks[i].getIdentifier();
                    break;
                }
                if(addFK)
                    localtable.removeForeignKey(fkTemp);
            }
        } catch(Exception ex){
            Log log = dbdict.getLog();
            if (log.isTraceEnabled()) {
                log.trace(_loc.get("except-read-fk-name"), ex);
            }
        }
        return retVal;
    }

    /**
     * Joins the column of a single column FK to this FK.
     * @param fk
     */
    public void addColumn(ForeignKey fk) {
        // Convert simple name based fk to a multi-column FK if necessary.
        if (getColumns() == null || getColumns().length == 0) {
            // If this FK is single column key, covert to a multi-column key
            Column[] keyCols = createKeyColumns(this);
            if (keyCols[0] != null && keyCols[1] != null) {
                setPrimaryKeyColumnIdentifier(DBIdentifier.NULL);
                setColumnIdentifier(DBIdentifier.NULL);
                join(keyCols[0], keyCols[1]);
            }
        }
        // Create the local and primary key columns from the fk and add them
        // to this fk.
        Column[] keyCols = createKeyColumns(fk);
        if (keyCols[0] != null && keyCols[1] != null) {
            join(keyCols[0], keyCols[1]);
        }
    }
    
    /*
     * Creates the local and primary key columns for a name-based fk. 
     * @return Column[] element 0 is local column
     *                  element 1 is the primary key in another table.
     */
    private static Column[] createKeyColumns(ForeignKey fk) {
        Column fkCol = null;
        if (!DBIdentifier.isEmpty(fk.getColumnIdentifier())) {
            fkCol = new Column();
            fkCol.setIdentifier(fk.getColumnIdentifier());
            fkCol.setTableIdentifier(fk.getTableIdentifier());
            fkCol.setSchemaIdentifier(fk.getSchemaIdentifier());
        }
        
        Column pkCol = null;
        if (!DBIdentifier.isEmpty(fk.getPrimaryKeyColumnIdentifier())) {
            pkCol = new Column();
            pkCol.setIdentifier(fk.getPrimaryKeyColumnIdentifier());
            pkCol.setTableIdentifier(fk.getPrimaryKeyTableIdentifier());
            pkCol.setSchemaIdentifier(fk.getPrimaryKeySchemaIdentifier());
        }
        return new Column[] { fkCol, pkCol };
    }
    
    /*
     * ForeignKey utility class which determines equality based upon the 
     * non-column state of the keys.  
     */
    public static class FKMapKey {
        
        private ForeignKey _fk;

        public FKMapKey(ForeignKey fk) {
            _fk = fk;
        }
        public ForeignKey getFk() {
            return _fk;
        }

        public int hashCode() {
            return getFk().getIdentifier() != null ? getFk().getIdentifier().hashCode() : getFk().hashCode();
        }
        
        public boolean equals(Object fkObj) {
            if (fkObj == this) {
                return true;
            }
            if (fkObj == null || !(fkObj instanceof FKMapKey)) {
                return false;
            }
            ForeignKey fk = ((FKMapKey)fkObj).getFk();
            if (getFk().getDeleteAction() != fk.getDeleteAction())
                return false;
            if (getFk().isDeferred() != fk.isDeferred())
                return false;
            if (!getFk().getIdentifier().equals(fk.getIdentifier())) {
                return false;
            }
            // Assert PK table name and schema
            if (!getFk().getPrimaryKeySchemaIdentifier().equals(fk.getPrimaryKeySchemaIdentifier()) ||
                !getFk().getPrimaryKeyTableIdentifier().equals(fk.getPrimaryKeyTableIdentifier()) ||
                !getFk().getSchemaIdentifier().equals(fk.getSchemaIdentifier()) ||
                !getFk().getTableIdentifier().equals(fk.getTableIdentifier())) {
                return false;
            }
            return true;
        }
    }
}
