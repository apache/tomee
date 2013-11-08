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
package org.apache.openjpa.datacache;

import java.lang.reflect.Array;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.conf.PluginListValue;
import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * A partitioned data cache maintains a set of partitions that are DataCache themselves.
 * Each of the partitioned DataCaches can be individually configured. 
 * However, all partitions must be of the same type. By default, this cache uses 
 * {@linkplain ConcurrentDataCache} as its partitions.
 * <br>
 * This cache can be configured as a plug-in as follows:
 * <br>
 * <code>&lt;property name='openjpa.DataCache" 
 *         value="partitioned(name=X, PartitionType=concurrent,Partitions='(name=a,cacheSize=100),
 *         (name=b,cacheSize=200)')</code>
 * <br>
 * Notice that the individual partition properties are enclosed parentheses, separated by comma
 * and finally the whole property string is enclosed in single quote.
 * Each partition must have a non-empty name that are unique among the partitions. 
 * The {@linkplain CacheDistributionPolicy policy} can return
 * the name of a partition to distribute the managed instances to be cached in respective partition.
 *  
 * The above configuration will configure a partitioned cache named <code>X</code> with two partitions named
 * <code>a</code> and <code>b</code> with cache size <code>100</code> and <code>200</code> respectively.
 * Besides the two partitions, this cache instance itself can store data and referred by its own name
 * (<code>X</code> in the above example).
 * <br>
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 */
@SuppressWarnings("serial")
public class PartitionedDataCache extends ConcurrentDataCache {
    private static final Localizer _loc = Localizer.forPackage(PartitionedDataCache.class);
    private Class<? extends DataCache> _type = ConcurrentDataCache.class;
    private final List<String> _partProperties = new ArrayList<String>();
    private final Map<String, DataCache> _partitions = new HashMap<String, DataCache>();
    
    @Override
    public void initialize(DataCacheManager mgr) {
        super.initialize(mgr);
        for(DataCache part : _partitions.values()){
            part.initialize(mgr);
        }
    }
    /**
     * Sets the type of the partitions. 
     * Each partition is a DataCache itself.
     * 
     * @param type the name of the type that implements {@linkplain DataCache} interface.
     * Aliases such as <code>"concurrent"</code> is also recognized.
     * 
     * @throws Exception if the given type is not resolvable to a loadable type.
     */
    public void setPartitionType(String type) throws Exception {
        Value value = conf.getValue("DataCache");
        ClassLoader ctxLoader = AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
        ClassLoader loader = conf.getClassResolverInstance().getClassLoader(null, ctxLoader);
        _type = (Class<? extends DataCache>) AccessController.doPrivileged(
                J2DoPrivHelper.getForNameAction(value.unalias(type), true, loader));
    }
    
    /**
     * Set partitions from a String configuration.
     * 
     * @param parts a String of the form <code>(p1, p2, p3)</code> where p1, p2 etc. itself are plug-in strings
     * for individual Data Cache configuration.
     */
    public void setPartitions(String parts) {
        _partProperties.clear();
        parsePartitionProperties(parts);
        PluginListValue partitions = new PluginListValue("partitions");
        String[] types = (String[])Array.newInstance(String.class, _partProperties.size());
        Arrays.fill(types, _type.getName());
        partitions.setClassNames(types);
        partitions.setProperties(_partProperties.toArray(new String[_partProperties.size()]));
        DataCache[] array = (DataCache[])partitions.instantiate(_type, conf);
        for (DataCache part : array) {
            if (part.getName() == null)
                throw new UserException(_loc.get("partition-cache-null-partition", parts));
            if (_partitions.containsKey(part.getName()))
                throw new UserException(_loc.get("partition-cache-duplicate-partition", part.getName(), parts));
            if (part.getName().equals(DataCache.NAME_DEFAULT))
                throw new UserException(_loc.get("partition-cache-default-partition", part.getName(), parts));
            _partitions.put(part.getName(), part);
        }
    }
    
    /**
     * Returns the individual partition configuration properties.
     */
    public List<String> getPartitions() {
        return _partProperties;
    }
    
    public DataCache getPartition(String name, boolean create) {
        return _partitions.get(name);
    }
    
    /**
     * Gets the name of the configured partitions.
     */
    public Set<String> getPartitionNames() {
        return _partitions.keySet();
    }
    
    /**
     * Always returns true.
     */
    public final boolean isPartitioned() {
        return !_partitions.isEmpty();
    }
    
    public void endConfiguration() {
        if (!isPartitioned())
            conf.getConfigurationLog().warn(_loc.get("partition-cache-no-config"));
    }
    
    /**
     * Parses property string of the form <code>(p1),(p2),(p3)</code> to produce a list of 
     * <code>p1</code>, <code>p2</code> and <code>p3</code>. The component strings 
     * <code>p1</code> etc. must be enclosed in parentheses and separated by comma.
     * plug-in string to produce a list of 
     * 
     * @param properties property string of the form <code>(p1),(p2),(p3)</code>
     */
    private void parsePartitionProperties(String full) {
        String properties = new String(full);
        while (true) {
            if (properties == null)
                break;
            properties = properties.trim();
            if (properties.length() == 0)
                break;
            if (properties.startsWith(",")) {
                properties = properties.substring(1);
            } else if (!_partProperties.isEmpty()) {
                throw new UserException(_loc.get("partition-cache-parse-error-comma", full, properties));
            }
            if (properties.startsWith("(") && properties.endsWith(")")) {
                int i = properties.indexOf(")");
                String p = properties.substring(1,i); // exclude the end parentheses
                _partProperties.add(p);
                 if (i < properties.length()-1) {
                    properties = properties.substring(i+1);
                 } else {
                     break;
                 }
            } else {
                throw new UserException(_loc.get("partition-cache-parse-error-paren", full, properties));
            }
        }
    }
}
