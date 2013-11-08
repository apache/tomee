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
package org.apache.openjpa.slice.jdbc;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.schema.DataSourceFactory;
import org.apache.openjpa.lib.conf.BooleanValue;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.conf.StringListValue;
import org.apache.openjpa.lib.conf.StringValue;
import org.apache.openjpa.lib.jdbc.DecoratingDataSource;
import org.apache.openjpa.lib.jdbc.DelegatingDataSource;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactory;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.slice.DistributedBrokerImpl;
import org.apache.openjpa.slice.DistributionPolicy;
import org.apache.openjpa.slice.FinderTargetPolicy;
import org.apache.openjpa.slice.ProductDerivation;
import org.apache.openjpa.slice.QueryTargetPolicy;
import org.apache.openjpa.slice.ReplicationPolicy;
import org.apache.openjpa.slice.Slice;
import org.apache.openjpa.util.UserException;

/**
 * A specialized configuration embodies a set of Slice configurations.
 * The original configuration properties are analyzed to create a set of
 * Slice specific properties with defaulting rules. 
 * 
 * @author Pinaki Poddar
 * 
 */
public class DistributedJDBCConfigurationImpl extends JDBCConfigurationImpl
        implements DistributedJDBCConfiguration {

    private final List<Slice> _slices = new ArrayList<Slice>();
    private Slice _master;
    
    private DistributedDataSource virtualDataSource;
    
    protected BooleanValue lenientPlugin;
    protected StringValue masterPlugin;
    protected StringListValue namesPlugin;
    public PluginValue distributionPolicyPlugin;
    public PluginValue replicationPolicyPlugin;
    public PluginValue queryTargetPolicyPlugin;
    public PluginValue finderTargetPolicyPlugin;
    public StringListValue replicatedTypesPlugin;
    
    private ReplicatedTypeRepository _replicationRepos;
    
    public static final String DOT = ".";
    public static final String REGEX_DOT = "\\.";
    public static final String PREFIX_SLICE = ProductDerivation.PREFIX_SLICE + DOT;
    public static final String PREFIX_OPENJPA = "openjpa.";
    private static Localizer _loc = Localizer.forPackage(DistributedJDBCConfigurationImpl.class);
    
    /**
     * Create a configuration and declare the plug-ins.
     */
    public DistributedJDBCConfigurationImpl() {
        super(true,   // load derivations
              false); // load globals
        brokerPlugin.setString(DistributedBrokerImpl.class.getName());
        
        distributionPolicyPlugin = addPlugin(PREFIX_SLICE + "DistributionPolicy", true);
        distributionPolicyPlugin.setAlias("random", DistributionPolicy.Default.class.getName());
        distributionPolicyPlugin.setDefault("random");
        distributionPolicyPlugin.setString("random");
        distributionPolicyPlugin.setDynamic(true);
        
        replicationPolicyPlugin = addPlugin(PREFIX_SLICE + "ReplicationPolicy", true);
        replicationPolicyPlugin.setAlias("all", ReplicationPolicy.Default.class.getName());
        replicationPolicyPlugin.setDefault("all");
        replicationPolicyPlugin.setString("all");
        replicationPolicyPlugin.setDynamic(true);
        
        queryTargetPolicyPlugin = addPlugin(PREFIX_SLICE + "QueryTargetPolicy", true);
        queryTargetPolicyPlugin.setDynamic(true);
        
        finderTargetPolicyPlugin = addPlugin(PREFIX_SLICE + "FinderTargetPolicy", true);
        finderTargetPolicyPlugin.setDynamic(true);
        
        replicatedTypesPlugin = new StringListValue(PREFIX_SLICE + "ReplicatedTypes");
        addValue(replicatedTypesPlugin);
        
        lenientPlugin = addBoolean(PREFIX_SLICE + "Lenient");
        lenientPlugin.setDefault("true");
        
        masterPlugin  = addString(PREFIX_SLICE + "Master");
        namesPlugin   = addStringList(PREFIX_SLICE + "Names");
    }
    
    /**
     * Configure itself as well as underlying slices.
     * 
     */
    public DistributedJDBCConfigurationImpl(ConfigurationProvider cp) {
        this();
        cp.setInto(this);
        setDiagnosticContext(this);
    }
    
    private void setDiagnosticContext(OpenJPAConfiguration conf) {
        LogFactory logFactory = conf.getLogFactory();
        try {
            Method setter = AccessController.doPrivileged(J2DoPrivHelper.
                    getDeclaredMethodAction(logFactory.getClass(), 
                    "setDiagnosticContext", new Class[]{String.class}));
            setter.invoke(logFactory, conf.getId());
        } catch (Throwable t) {
            // no contextual logging
        }
    }

    /**
     * Gets the name of the active slices.
     */
    public List<String> getActiveSliceNames() {
        List<String> result = new ArrayList<String>();
        for (Slice slice : _slices) {
           if (slice.isActive() && !result.contains(slice.getName()))
              result.add(slice.getName());
        }
        return result;
    }
    
    /**
     * Gets the name of the available slices.
     */
    public List<String> getAvailableSliceNames() {
        List<String> result = new ArrayList<String>();
        for (Slice slice : _slices)
            result.add(slice.getName());
        return result;
    }
    
    /**
     * Gets the slices of given status. Null returns all irrespective of status.
     */
    public List<Slice> getSlices(Slice.Status...statuses) {
        if (statuses == null)
            return _slices == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(_slices);
        List<Slice> result = new ArrayList<Slice>();
        for (Slice slice:_slices) {
            for (Slice.Status status:statuses)
                if (slice.getStatus().equals(status))
                    result.add(slice);
        }
        return result;
    }
    

    public Slice getSlice(String name) {
        return getSlice(name, false);
    }
    
    /**
     * Get the Slice of the given slice.
     * 
     * @param mustExist if true an exception if raised if the given slice name
     * is not a valid slice.
     */
    public Slice getSlice(String name, boolean mustExist) {
        for (Slice slice : _slices)
            if (slice.getName().equals(name))
                return slice;
        if (mustExist) {
            throw new UserException(_loc.get("slice-not-found", name,
                    getActiveSliceNames()));
        }
        return null;
    }

    public DistributionPolicy getDistributionPolicyInstance() {
        if (distributionPolicyPlugin.get() == null) {
            distributionPolicyPlugin.instantiate(DistributionPolicy.class,
                    this, true);
        }
        return (DistributionPolicy) distributionPolicyPlugin.get();
    }
    
    public String getDistributionPolicy() {
        if (distributionPolicyPlugin.get() == null) {
            distributionPolicyPlugin.instantiate(DistributionPolicy.class,
                    this, true);
        }
        return distributionPolicyPlugin.getString();
    }

    public void setDistributionPolicyInstance(DistributionPolicy policy) {
        distributionPolicyPlugin.set(policy);
    }
    
    public void setDistributionPolicy(String policy) {
        distributionPolicyPlugin.setString(policy);
    }

    public ReplicationPolicy getReplicationPolicyInstance() {
        if (replicationPolicyPlugin.get() == null) {
            replicationPolicyPlugin.instantiate(ReplicationPolicy.class,
                    this, true);
        }
        return (ReplicationPolicy) replicationPolicyPlugin.get();
    }
    
    public String getReplicationPolicy() {
        if (replicationPolicyPlugin.get() == null) {
            replicationPolicyPlugin.instantiate(ReplicationPolicy.class,
                    this, true);
        }
        return replicationPolicyPlugin.getString();
    }

    public void setReplicationPolicyInstance(ReplicationPolicy policy) {
        replicationPolicyPlugin.set(policy);
    }
    
    public void setReplicationPolicy(String policy) {
        replicationPolicyPlugin.setString(policy);
    }

    public QueryTargetPolicy getQueryTargetPolicyInstance() {
        if (queryTargetPolicyPlugin.get() == null) {
            queryTargetPolicyPlugin.instantiate(QueryTargetPolicy.class,
                    this, true);
        }
        return (QueryTargetPolicy) queryTargetPolicyPlugin.get();
    }
    
    public String getQueryTargetPolicy() {
        if (queryTargetPolicyPlugin.get() == null) {
            queryTargetPolicyPlugin.instantiate(QueryTargetPolicy.class,
                    this, true);
        }
        return queryTargetPolicyPlugin.getString();
    }

    public void setQueryTargetPolicyInstance(QueryTargetPolicy policy) {
        queryTargetPolicyPlugin.set(policy);
    }
    
    public void setQueryTargetPolicy(String policy) {
        queryTargetPolicyPlugin.setString(policy);
    }
    
    public FinderTargetPolicy getFinderTargetPolicyInstance() {
        if (finderTargetPolicyPlugin.get() == null) {
            finderTargetPolicyPlugin.instantiate(FinderTargetPolicy.class,
                    this, true);
        }
        return (FinderTargetPolicy) finderTargetPolicyPlugin.get();
    }
    
    public String getFinderTargetPolicy() {
        if (finderTargetPolicyPlugin.get() == null) {
            finderTargetPolicyPlugin.instantiate(FinderTargetPolicy.class,
                    this, true);
        }
        return finderTargetPolicyPlugin.getString();
    }

    public void setFinderTargetPolicyInstance(FinderTargetPolicy policy) {
        finderTargetPolicyPlugin.set(policy);
    }
    
    public void setFinderTargetPolicy(String policy) {
        finderTargetPolicyPlugin.setString(policy);
    }

    public DistributedDataSource getConnectionFactory() {
        if (virtualDataSource == null) {
            virtualDataSource = createDistributedDataStore();
            DataSourceFactory.installDBDictionary(
                getDBDictionaryInstance(), virtualDataSource, this, false);
        }
        return virtualDataSource;
    }
    
    public boolean isLenient() {
        return lenientPlugin.get();
    }
    
    public void setLenient(boolean lenient) {
        lenientPlugin.set(lenient);
    }

    public void setMaster(String master) {
        masterPlugin.set(master);
    }
    
    /**
     * Gets the master slice.
     */
    public Slice getMasterSlice() {
        if (_master == null) {
            String value = masterPlugin.get();
            if (value == null) {
                _master = _slices.get(0);
            } else {
                _master = getSlice(value, true);
            }
        }
        return _master;
    }

    /**
     * Create a virtual DistributedDataSource as a composite of individual
     * slices as per configuration, optionally ignoring slices that can not be
     * connected.
     */
    private DistributedDataSource createDistributedDataStore() {
        List<DataSource> dataSources = new ArrayList<DataSource>();
        boolean isXA = true;
        for (Slice slice : _slices) {
            try {
                DataSource ds = createDataSource(slice);
                dataSources.add(ds);
                isXA &= isXACompliant(ds);
            } catch (Throwable ex) {
                handleBadConnection(isLenient(), slice, ex);
            }
        }
        if (dataSources.isEmpty())
            throw new UserException(_loc.get("no-slice"));
        DistributedDataSource result = new DistributedDataSource(dataSources);
        return result;
    }
    
    DataSource createDataSource(Slice slice) throws Exception {
        JDBCConfiguration conf = (JDBCConfiguration)slice.getConfiguration();
        DataSource ds = (DataSource)conf.getConnectionFactory();
        if (ds == null) {
            Log log = conf.getConfigurationLog();
            String url = getConnectionInfo(conf);
            if (log.isInfoEnabled())
                log.info(_loc.get("slice-connect", slice, url));
            ds = DataSourceFactory.newDataSource(conf, false);
            DecoratingDataSource dds = new DecoratingDataSource(ds);
            ds = DataSourceFactory.installDBDictionary(
                    conf.getDBDictionaryInstance(), dds, conf, false);
        }
        verifyDataSource(slice, ds, conf);
        
        return ds;
    }

    String getConnectionInfo(OpenJPAConfiguration conf) {
        String result = conf.getConnectionURL();
        if (result == null) {
            result = conf.getConnectionDriverName();
            String props = conf.getConnectionProperties();
            if (props != null)
                result += "(" + props + ")";
        }
        return result;
    }

    boolean isXACompliant(DataSource ds) {
        if (ds instanceof DelegatingDataSource)
            return ((DelegatingDataSource) ds).getInnermostDelegate() 
               instanceof XADataSource;
        return ds instanceof XADataSource;
    }

    /**
     * Verify that a connection can be established to the given slice. If
     * connection can not be established then slice is set to INACTIVE state.
     */
    private boolean verifyDataSource(Slice slice, DataSource ds, 
    		JDBCConfiguration conf) {
        Connection con = null;
        try {
            con = ds.getConnection(conf.getConnectionUserName(), 
            		conf.getConnectionPassword());
            slice.setStatus(Slice.Status.ACTIVE);
            if (con == null) {
                slice.setStatus(Slice.Status.INACTIVE);
                return false;
            }
            return true;
        } catch (SQLException ex) {
            slice.setStatus(Slice.Status.INACTIVE);
            return false;
        } finally {
            if (con != null)
                try {
                    con.close();
                } catch (SQLException ex) {
                    // ignore
                }
        }
    }

    /**
     * Either throw a user exception or add the configuration to the given list,
     * based on <code>isLenient</code>.
     */
    private void handleBadConnection(boolean isLenient, Slice slice, Throwable ex) {
        OpenJPAConfiguration conf = slice.getConfiguration();
        String url = conf.getConnectionURL();
        Log log = conf.getConfigurationLog();
        if (isLenient) {
            if (ex != null) {
                log.warn(_loc.get("slice-connect-known-warn", slice, url, ex.getCause()));
            } else {
                log.warn(_loc.get("slice-connect-warn", slice, url));
            }
        } else if (ex != null) {
            throw new UserException(_loc.get("slice-connect-known-error", slice, url, ex), ex.getCause());
        } else {
            throw new UserException(_loc.get("slice-connect-error", slice, url));
        }
    }

    /**
     * Create a new Slice of given name and given properties.
     * 
     * @param key name of the slice to be created
     * @param original a set of properties.
     * @return a newly configured slice
     */
    private Slice newSlice(String key, Map original) {
        JDBCConfiguration child = new JDBCConfigurationImpl();
        child.fromProperties(createSliceProperties(original, key));
        child.setId(getId()+DOT+key);
        setDiagnosticContext(child);
        child.setMappingDefaults(this.getMappingDefaultsInstance());
        child.setDataCacheManager(this.getDataCacheManagerInstance());
        child.setMetaDataRepository(this.getMetaDataRepositoryInstance());
        Slice slice = new Slice(key, child);
        Log log = getConfigurationLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("slice-configuration", key, child
                    .toProperties(false)));
        return slice;
    }

    /**
     * Finds the slices. If <code>openjpa.slice.Names</code> property is 
     * specified then the slices are ordered in the way they are listed. 
     * Otherwise scans all available slices by looking for property of the form
     * <code>openjpa.slice.XYZ.abc</code> where <code>XYZ</code> is the slice
     * identifier and <code>abc</code> is any openjpa property name. The slices
     * are then ordered alphabetically by their identifier.
     */
    private List<String> findSlices(Map p) {
        List<String> sliceNames = new ArrayList<String>();
        
        Log log = getConfigurationLog();
        String key = namesPlugin.getProperty();
        boolean explicit = p.containsKey(key);
        if (explicit) {
            String[] values = p.get(key).toString().split("\\,");
            for (String name:values)
                if (!sliceNames.contains(name.trim()))
                    sliceNames.add(name.trim());
        } else {
            if (log.isWarnEnabled())
                log.warn(_loc.get("no-slice-names", key));
            sliceNames = scanForSliceNames(p);
            Collections.sort(sliceNames);
        }
        if (log.isInfoEnabled()) {
            log.info(_loc.get("slice-available", sliceNames));
        }
        return sliceNames;
    }
    
    /**
     * Scan the given map for slice-specific property of the form 
     * <code>openjpa.slice.XYZ.abc</code> (while ignoring 
     * <code>openjpa.slice.XYZ</code> as they refer to slice-wide property)
     * to determine the names of all available slices.
     */
    private List<String> scanForSliceNames(Map p) {
        List<String> sliceNames = new ArrayList<String>();
        for (Object o : p.keySet()) {
            String key = o.toString();
            if (key.startsWith(PREFIX_SLICE) && getPartCount(key) > 3) {
                String sliceName =
                    chopTail(chopHead(o.toString(), PREFIX_SLICE), DOT);
                if (!sliceNames.contains(sliceName))
                    sliceNames.add(sliceName);
            }
        }
        return sliceNames;
    }

    private static int getPartCount(String s) {
        return (s == null) ? 0 : s.split(REGEX_DOT).length;
    }
    
    private static String chopHead(String s, String head) {
        if (s.startsWith(head))
            return s.substring(head.length());
        return s;
    }

    private static String chopTail(String s, String tail) {
        int i = s.lastIndexOf(tail);
        if (i == -1)
            return s;
        return s.substring(0, i);
    }
    
    /**
     * Creates given <code>slice</code> specific configuration properties from
     * given <code>original</code> key-value map. The rules are
     * <LI> if key begins with <code>"openjpa.slice.XXX."</code> where
     * <code>XXX</code> is the given slice name, then replace
     * <code>"openjpa.slice.XXX.</code> with <code>openjpa.</code>.
     * <LI>if key begins with <code>"openjpa.slice."</code> but not with
     * <code>"openjpa.slice.XXX."</code>, then ignore i.e. any property of other
     * slices or global slice property e.g.
     * <code>openjpa.slice.DistributionPolicy</code>
     * <li>if key starts with <code>"openjpa."</code> and a corresponding
     * <code>"openjpa.slice.XXX."</code> property does not exist, then use this as
     * default property
     * <li>property with any other prefix is simply copied
     *
     */
    Map createSliceProperties(Map original, String slice) {
        Map result = new Properties();
        String prefix = PREFIX_SLICE + slice + DOT;
        for (Object o : original.keySet()) {
            String key = o.toString();
            Object value = original.get(key);
            if (value == null)
                continue;
            if (key.startsWith(prefix)) {
                String newKey = PREFIX_OPENJPA + key.substring(prefix.length());
                result.put(newKey, value);
            } else if (key.startsWith(PREFIX_SLICE)) {
                // ignore keys that are in 'slice.' namespace but not this slice
            } else if (key.startsWith(PREFIX_OPENJPA)) {
                String newKey = prefix + key.substring(PREFIX_OPENJPA.length());
                if (!original.containsKey(newKey))
                    result.put(key, value);
            } else { // keys that are neither "openjpa" nor "slice" namespace
                result.put(key, value);
            }
        }
        return result;
    }
    
    Slice addSlice(String name, Map newProps) {
        String prefix = PREFIX_SLICE + DOT + name + DOT;
        for (Object key : newProps.keySet()) {
            if (!String.class.isInstance(key) 
             && key.toString().startsWith(prefix))
                throw new UserException(_loc.get("slice-add-wrong-key", key));
        }
        Slice slice = getSlice(name);
        if (slice != null)
            throw new UserException(_loc.get("slice-exists", name));
        Map<String,String> original = super.toProperties(true);
        original.putAll(newProps);
         slice = newSlice(name, original);
        _slices.add(slice);
        try {
            getConnectionFactory().addDataSource(createDataSource(slice));
        } catch (Exception ex) {
            handleBadConnection(false, slice, ex);
            return null;
        }
        return slice;
    }
    
    /**
     * Given the properties, creates a set of individual configurations.
     */
    @Override
    public void fromProperties(Map original) {
        super.fromProperties(original);
        setDiagnosticContext(this);
        List<String> sliceNames = findSlices(original);
        for (String name : sliceNames) {
            Slice slice = newSlice(name, original);
            _slices.add(slice);
        }
    }
       
    @Override
    public DecoratingDataSource createConnectionFactory() {
        if (virtualDataSource == null) {
            virtualDataSource = createDistributedDataStore();
        }
        return virtualDataSource;
    }
    
    public boolean isReplicated(Class<?> cls) {
        if (_replicationRepos == null) {
            _replicationRepos = new ReplicatedTypeRepository(getMetaDataRepositoryInstance(),
                    Arrays.asList(replicatedTypesPlugin.get()));
        }
        return _replicationRepos.contains(cls);
    }
    
    
    /**
     * A private repository of replicated types.
     * 
     * @author Pinaki Poddar
     *
     */
    private static class ReplicatedTypeRepository {
        private Set<Class<?>> _replicatedTypes = new HashSet<Class<?>>();
        private Set<Class<?>> _nonreplicatedTypes = new HashSet<Class<?>>();


        List<String> names;
        MetaDataRepository repos;
        
        ReplicatedTypeRepository(MetaDataRepository repos, List<String> given) {
            names = given;
            this.repos = repos;
        }
        
        boolean contains(Class<?> cls) {
            if (_replicatedTypes.contains(cls))
                return true;
            if (_nonreplicatedTypes.contains(cls)) 
                return false;
            ClassMetaData meta = repos.getMetaData(cls, null, false);
            if (meta == null) {
                _nonreplicatedTypes.add(cls);
                return false;
            }
            boolean replicated = names.contains(meta.getDescribedType().getName());
            if (replicated) {
                _replicatedTypes.add(cls);
            } else {
                _nonreplicatedTypes.add(cls);
            }
            return replicated;
        }
    }
}
