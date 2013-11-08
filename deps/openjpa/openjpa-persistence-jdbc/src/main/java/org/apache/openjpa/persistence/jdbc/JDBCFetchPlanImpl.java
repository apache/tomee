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
package org.apache.openjpa.persistence.jdbc;

import java.sql.ResultSet;
import java.util.Collection;

import javax.persistence.LockModeType;

import org.apache.openjpa.jdbc.kernel.DelegatingJDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.kernel.DelegatingFetchConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.persistence.FetchPlanImpl;
import org.apache.openjpa.persistence.HintValueConverter;
import org.apache.openjpa.persistence.PersistenceExceptions;

/**
 * JDBC extensions to the fetch plan.
 *
 * @since 0.4.0
 * @author Abe White
 * @nojavadoc
 */
public class JDBCFetchPlanImpl
    extends FetchPlanImpl
    implements JDBCFetchPlan {

    private DelegatingJDBCFetchConfiguration _fetch;
    static {
        registerHint(new String[]{"openjpa.FetchPlan.EagerFetchMode", "openjpa.jdbc.EagerFetchMode"},
            new HintValueConverter.StringToInteger(new String[]{"none", "0", "join", "1", "parallel", "2"}, 
                new int[]{EagerFetchModes.EAGER_NONE, EagerFetchModes.EAGER_NONE, 
                          EagerFetchModes.EAGER_JOIN, EagerFetchModes.EAGER_JOIN,
                          EagerFetchModes.EAGER_PARALLEL,EagerFetchModes.EAGER_PARALLEL}),
            new HintValueConverter.EnumToInteger(FetchMode.class, 
                new int[]{EagerFetchModes.EAGER_NONE, EagerFetchModes.EAGER_JOIN, EagerFetchModes.EAGER_PARALLEL}));
        registerHint(new String[]{"openjpa.JoinSyntax", "openjpa.jdbc.JoinSyntax","openjpa.FetchPlan.JoinSyntax"}, 
            new HintValueConverter.EnumToInteger(JoinSyntax.class,
                new int[]{JoinSyntaxes.SYNTAX_SQL92, JoinSyntaxes.SYNTAX_TRADITIONAL, JoinSyntaxes.SYNTAX_DATABASE}),
            new HintValueConverter.StringToInteger(new String[]{"sql92", "0", "traditional", "1", "database", "2"}, 
                new int[]{JoinSyntaxes.SYNTAX_SQL92, JoinSyntaxes.SYNTAX_SQL92, 
                    JoinSyntaxes.SYNTAX_TRADITIONAL, JoinSyntaxes.SYNTAX_TRADITIONAL,
                    JoinSyntaxes.SYNTAX_DATABASE, JoinSyntaxes.SYNTAX_DATABASE}));
        registerHint(new String[]{"openjpa.FetchDirection", "openjpa.jdbc.FetchDirection",
                "openjpa.FetchPlan.FetchDirection"}, 
                new HintValueConverter.EnumToInteger(FetchDirection.class,
                    new int[]{ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE, ResultSet.FETCH_UNKNOWN}),    
                new HintValueConverter.StringToInteger(new String[]{"forward", String.valueOf(ResultSet.FETCH_FORWARD), 
                                                       "reverse", String.valueOf(ResultSet.FETCH_REVERSE), 
                                                       "unknown", String.valueOf(ResultSet.FETCH_UNKNOWN)}, 
                    new int[]{ResultSet.FETCH_FORWARD, ResultSet.FETCH_FORWARD, 
                        ResultSet.FETCH_REVERSE, ResultSet.FETCH_REVERSE,
                        ResultSet.FETCH_UNKNOWN, ResultSet.FETCH_UNKNOWN}));
        registerHint(new String[]{"openjpa.FetchPlan.Isolation", "openjpa.jdbc.TransactionIsolation"}, 
                new HintValueConverter.OpenJPAEnumToInteger(IsolationLevel.DEFAULT));    
        registerHint(new String[]{"openjpa.FetchPlan.LRSSizeAlgorithm", "openjpa.FetchPlan.LRSSize",
        "openjpa.jdbc.LRSSize"}, 
        new HintValueConverter.OpenJPAEnumToInteger(LRSSizeAlgorithm.QUERY));
        registerHint(new String[]{"openjpa.FetchPlan.ResultSetType", "openjpa.jdbc.ResultSetType"}, 
                new HintValueConverter.OpenJPAEnumToInteger(ResultSetType.FORWARD_ONLY));
        registerHint(new String[]{"openjpa.FetchPlan.SubclassFetchMode", "openjpa.jdbc.SubclassFetchMode"}, 
                new HintValueConverter.OpenJPAEnumToInteger(FetchMode.NONE));
        
//        "openjpa.FetchPlan.FetchDirection"
//        _hints.add("openjpa.FetchPlan.LockScope");
//        _hints.add("openjpa.FetchPlan.LockTimeout");
//        _hints.add("openjpa.FetchPlan.MaxFetchDepth");
//        _hints.add("openjpa.FetchPlan.QueryTimeout");
//        _hints.add("openjpa.FetchPlan.ReadLockMode");
//        _hints.add("openjpa.FetchPlan.WriteLockMode");
    }

    /**
     * Constructor; supply delegate.
     */
    public JDBCFetchPlanImpl(FetchConfiguration fetch) {
        super(fetch);
    }

    @Override
    protected DelegatingFetchConfiguration newDelegatingFetchConfiguration(
        FetchConfiguration fetch) {
        _fetch = new DelegatingJDBCFetchConfiguration((JDBCFetchConfiguration)
            fetch, PersistenceExceptions.TRANSLATOR);
        return _fetch;
    }

    public FetchMode getEagerFetchMode() {
        return FetchMode.fromKernelConstant(_fetch.getEagerFetchMode());
    }

    public JDBCFetchPlanImpl setEagerFetchMode(FetchMode mode) {
        _fetch.setEagerFetchMode(mode.toKernelConstant());
        return this;
    }

    public JDBCFetchPlan setEagerFetchMode(int mode) {
        _fetch.setEagerFetchMode(mode);
        return this;
    }

    public FetchMode getSubclassFetchMode() {
        return FetchMode.fromKernelConstant(_fetch.getSubclassFetchMode());
    }

    public JDBCFetchPlanImpl setSubclassFetchMode(FetchMode mode) {
        _fetch.setSubclassFetchMode(mode.toKernelConstant());
        return this;
    }

    public JDBCFetchPlan setSubclassFetchMode(int mode) {
        _fetch.setSubclassFetchMode(mode);
        return this;
    }

    public ResultSetType getResultSetType() {
        return ResultSetType.fromKernelConstant(_fetch.getResultSetType());
    }

    public JDBCFetchPlanImpl setResultSetType(ResultSetType type) {
        _fetch.setResultSetType(type.toKernelConstant());
        return this;
    }

    public JDBCFetchPlan setResultSetType(int mode) {
        _fetch.setResultSetType(mode);
        return this;
    }

    public FetchDirection getFetchDirection() {
        return FetchDirection.fromKernelConstant(_fetch.getFetchDirection());
    }

    public JDBCFetchPlanImpl setFetchDirection(FetchDirection direction) {
        _fetch.setFetchDirection(direction.toKernelConstant());
        return this;
    }

    public JDBCFetchPlan setFetchDirection(int direction) {
        _fetch.setFetchDirection(direction);
        return this;
    }

    public LRSSizeAlgorithm getLRSSizeAlgorithm() {
        return LRSSizeAlgorithm.fromKernelConstant(_fetch.getLRSSize());
    }

    public JDBCFetchPlanImpl setLRSSizeAlgorithm(
            LRSSizeAlgorithm lrsSizeAlgorithm) {
        _fetch.setLRSSize(lrsSizeAlgorithm.toKernelConstant());
        return this;
    }

    public int getLRSSize() {
        return _fetch.getLRSSize();
    }

    public JDBCFetchPlan setLRSSize(int lrsSizeMode) {
        _fetch.setLRSSize(lrsSizeMode);
        return this;
    }

    public JoinSyntax getJoinSyntax() {
        return JoinSyntax.fromKernelConstant(_fetch.getJoinSyntax());
    }

    public JDBCFetchPlanImpl setJoinSyntax(JoinSyntax syntax) {
        _fetch.setJoinSyntax(syntax.toKernelConstant());
        return this;
    }

    public JDBCFetchPlan setJoinSyntax(int syntax) {
        _fetch.setJoinSyntax(syntax);
        return this;
    }

    public IsolationLevel getIsolation() {
        return IsolationLevel.fromConnectionConstant(_fetch.getIsolation());
    }

    public JDBCFetchPlan setIsolation(IsolationLevel level) {
        _fetch.setIsolation(level.getConnectionConstant());
        return this;
    }

    @Override
    public JDBCFetchPlan addFetchGroup(String group) {
        return (JDBCFetchPlan) super.addFetchGroup(group);
    }

    @Override
    public JDBCFetchPlan addFetchGroups(Collection groups) {
        return (JDBCFetchPlan) super.addFetchGroups(groups);
    }

    @Override
    public JDBCFetchPlan addFetchGroups(String... groups) {
        return (JDBCFetchPlan) super.addFetchGroups(groups);
    }

    @Override
    public JDBCFetchPlan addField(Class cls, String field) {
        return (JDBCFetchPlan) super.addField(cls, field);
    }

    @Override
    public JDBCFetchPlan addField(String field) {
        return (JDBCFetchPlan) super.addField(field);
    }

    @Override
    public JDBCFetchPlan addFields(Class cls, Collection fields) {
        return (JDBCFetchPlan) super.addFields(cls, fields);
    }

    @Override
    public JDBCFetchPlan addFields(Class cls, String... fields) {
        return (JDBCFetchPlan) super.addFields(cls, fields);
    }

    @Override
    public JDBCFetchPlan addFields(Collection fields) {
        return (JDBCFetchPlan) super.addFields(fields);
    }

    @Override
    public JDBCFetchPlan addFields(String... fields) {
        return (JDBCFetchPlan) super.addFields(fields);
    }

    @Override
    public JDBCFetchPlan clearFetchGroups() {
        return (JDBCFetchPlan) super.clearFetchGroups();
    }

    @Override
    public JDBCFetchPlan clearFields() {
        return (JDBCFetchPlan) super.clearFields();
    }

    @Override
    public JDBCFetchPlan removeFetchGroup(String group) {
        return (JDBCFetchPlan) super.removeFetchGroup(group);
    }

    @Override
    public JDBCFetchPlan removeFetchGroups(Collection groups) {
        return (JDBCFetchPlan) super.removeFetchGroups(groups);
    }

    @Override
    public JDBCFetchPlan removeFetchGroups(String... groups) {
        return (JDBCFetchPlan) super.removeFetchGroups(groups);
    }

    @Override
    public JDBCFetchPlan removeField(Class cls, String field) {
        return (JDBCFetchPlan) super.removeField(cls, field);
    }

    @Override
    public JDBCFetchPlan removeField(String field) {
        return (JDBCFetchPlan) super.removeField(field);
    }

    @Override
    public JDBCFetchPlan removeFields(Class cls, Collection fields) {
        return (JDBCFetchPlan) super.removeFields(cls, fields);
    }

    @Override
    public JDBCFetchPlan removeFields(Class cls, String... fields) {
        return (JDBCFetchPlan) super.removeFields(cls, fields);
    }

    @Override
    public JDBCFetchPlan removeFields(Collection fields) {
        return (JDBCFetchPlan) super.removeFields(fields);
    }

    @Override
    public JDBCFetchPlan removeFields(String... fields) {
        return (JDBCFetchPlan) super.removeFields(fields);
    }

    @Override
    public JDBCFetchPlan resetFetchGroups() {
        return (JDBCFetchPlan) super.resetFetchGroups();
    }

    @Override
    public JDBCFetchPlan setQueryResultCacheEnabled(boolean cache) {
        return (JDBCFetchPlan) super.setQueryResultCacheEnabled(cache);
    }

    @Override
    public JDBCFetchPlan setFetchBatchSize(int fetchBatchSize) {
        return (JDBCFetchPlan) super.setFetchBatchSize(fetchBatchSize);
    }

    @Override
    public JDBCFetchPlan setLockTimeout(int timeout) {
        return (JDBCFetchPlan) super.setLockTimeout(timeout);
    }

    @Override
    public JDBCFetchPlan setMaxFetchDepth(int depth) {
        return (JDBCFetchPlan) super.setMaxFetchDepth(depth);
    }

    @Override
    public JDBCFetchPlan setReadLockMode(LockModeType mode) {
        return (JDBCFetchPlan) super.setReadLockMode(mode);
    }

    @Override
    public JDBCFetchPlan setWriteLockMode(LockModeType mode) {
        return (JDBCFetchPlan) super.setWriteLockMode(mode);
    }

    @Override
    public JDBCFetchPlan setQueryTimeout(int timeout) {
        return (JDBCFetchPlan) super.setQueryTimeout(timeout);
    }

    @Override
    public boolean getIgnoreDfgForFkSelect() {
        return _fetch.getIgnoreDfgForFkSelect();
    }

    @Override
    public void setIgnoreDfgForFkSelect(boolean b) {
        _fetch.setIgnoreDfgForFkSelect(b);
    }
}
