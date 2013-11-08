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
package org.apache.openjpa.meta;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;
import serp.util.Strings;

/**
 * Vendor extensions. This class is thread safe for reads, but not for
 * mutations.
 *
 * @author Abe White
 */
public abstract class Extensions
    implements Serializable {

    public static final String OPENJPA = "openjpa";

    private static final Localizer _loc = Localizer.forPackage
        (Extensions.class);

    private Map _exts = null;
    private Map _embed = null;

    /**
     * Return true if there are no keys for any vendor.
     */
    public boolean isEmpty() {
        return (_exts == null || _exts.isEmpty())
            && (_embed == null || _embed.isEmpty());
    }

    /**
     * Return all vendors who have extension keys at this level.
     */
    public String[] getExtensionVendors() {
        if (_exts == null || _exts.isEmpty())
            return new String[0];

        Set vendors = new TreeSet();
        for (Iterator itr = _exts.keySet().iterator(); itr.hasNext();)
            vendors.add(getVendor(itr.next()));
        return (String[]) vendors.toArray(new String[vendors.size()]);
    }

    /**
     * Return all extension keys.
     */
    public String[] getExtensionKeys() {
        return getExtensionKeys(OPENJPA);
    }

    /**
     * Return all extension keys for the given vendor.
     */
    public String[] getExtensionKeys(String vendor) {
        if (_exts == null || _exts.isEmpty())
            return new String[0];

        Collection keys = new TreeSet();
        Object key;
        for (Iterator itr = _exts.keySet().iterator(); itr.hasNext();) {
            key = itr.next();
            if (vendor.equals(getVendor(key)))
                keys.add(getKey(key));
        }
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    /**
     * Return true if the extension with the given key exists.
     */
    public boolean hasExtension(String key) {
        return hasExtension(OPENJPA, key);
    }

    /**
     * Return true if the extension with the given key exists.
     */
    public boolean hasExtension(String vendor, String key) {
        return _exts != null && _exts.containsKey(getHashKey(vendor, key));
    }

    /**
     * Add a vendor extension to this entity.
     */
    public void addExtension(String key, Object value) {
        addExtension(OPENJPA, key, value);
    }

    /**
     * Add a vendor extension to this entity.
     */
    public void addExtension(String vendor, String key, Object value) {
        if (_exts == null)
            _exts = new HashMap();
        _exts.put(getHashKey(vendor, key), value);
    }

    /**
     * Remove a vendor extension.
     */
    public boolean removeExtension(String key) {
        return removeExtension(OPENJPA, key);
    }

    /**
     * Remove a vendor extension.
     */
    public boolean removeExtension(String vendor, String key) {
        if (_exts != null && _exts.remove(getHashKey(vendor, key)) != null) {
            removeEmbeddedExtensions(key);
            return true;
        }
        return false;
    }

    /**
     * Get the value of an extension.
     */
    public Object getObjectExtension(String key) {
        return getObjectExtension(OPENJPA, key);
    }

    /**
     * Get the value of an extension.
     */
    public Object getObjectExtension(String vendor, String key) {
        if (_exts == null)
            return null;
        return _exts.get(getHashKey(vendor, key));
    }

    /**
     * Get the value as a string.
     */
    public String getStringExtension(String key) {
        return getStringExtension(OPENJPA, key);
    }

    /**
     * Get the value as a string.
     */
    public String getStringExtension(String vendor, String key) {
        Object val = getObjectExtension(vendor, key);
        return (val == null) ? null : val.toString();
    }

    /**
     * Get the value as an int.
     */
    public int getIntExtension(String key) {
        return getIntExtension(OPENJPA, key);
    }

    /**
     * Get the value as an int.
     */
    public int getIntExtension(String vendor, String key) {
        String str = getStringExtension(vendor, key);
        return (str == null) ? 0 : Integer.parseInt(str);
    }

    /**
     * Get the value as a double.
     */
    public double getDoubleExtension(String key) {
        return getDoubleExtension(OPENJPA, key);
    }

    /**
     * Get the value as a double.
     */
    public double getDoubleExtension(String vendor, String key) {
        String str = getStringExtension(vendor, key);
        return (str == null) ? 0D : Double.parseDouble(str);
    }

    /**
     * Get the value as a boolean.
     */
    public boolean getBooleanExtension(String key) {
        return getBooleanExtension(OPENJPA, key);
    }

    /**
     * Get the value as a boolean.
     */
    public boolean getBooleanExtension(String vendor, String key) {
        String str = getStringExtension(vendor, key);
        return (str == null) ? false : Boolean.valueOf(str).booleanValue();
    }

    /**
     * Return the embedded extensions under the given key.
     */
    public Extensions getEmbeddedExtensions(String key, boolean create) {
        return getEmbeddedExtensions(OPENJPA, key, create);
    }

    /**
     * Return the embedded extensions under the given key.
     */
    public Extensions getEmbeddedExtensions(String vendor, String key,
        boolean create) {
        if (_embed == null && !create)
            return null;
        if (_embed == null)
            _embed = new HashMap();

        Object hk = getHashKey(vendor, key);
        Extensions exts = (Extensions) _embed.get(hk);
        if (exts == null && !create)
            return null;
        if (exts == null) {
            exts = new EmbeddedExtensions(this);
            _embed.put(hk, exts);

            // required to recognize embedded extensions without values
            if (_exts == null)
                _exts = new HashMap();
            if (!_exts.containsKey(hk))
                _exts.put(hk, null);
        }
        return exts;
    }

    public boolean removeEmbeddedExtensions(String key) {
        return removeEmbeddedExtensions(OPENJPA, key);
    }

    public boolean removeEmbeddedExtensions(String vendor, String key) {
        return _embed != null
            && _embed.remove(getHashKey(vendor, key)) != null;
    }

    /**
     * Copy the extensions not present in this instance but present in the
     * given instance.
     */
    protected void copy(Extensions exts) {
        if (exts.isEmpty())
            return;

        if (exts._exts != null && !exts._exts.isEmpty()) {
            if (_exts == null)
                _exts = new HashMap();

            Map.Entry entry;
            for (Iterator itr = exts._exts.entrySet().iterator();
                itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                if (!_exts.containsKey(entry.getKey()))
                    _exts.put(entry.getKey(), entry.getValue());
            }
        }

        if (exts._embed != null && !exts._embed.isEmpty()) {
            if (_embed == null)
                _embed = new HashMap();

            Map.Entry entry;
            Extensions embedded;
            for (Iterator itr = exts._embed.entrySet().iterator();
                itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                embedded = (Extensions) _embed.get(entry.getKey());
                if (embedded == null) {
                    embedded = new EmbeddedExtensions(this);
                    _embed.put(entry.getKey(), embedded);
                }
                embedded.copy((Extensions) entry.getValue());
            }
        }
    }

    /**
     * Helper method to issue warnings for any extensions that we
     * recognize but do not use.
     *
     * @since 0.3.1.3
     */
    public void validateExtensionKeys() {
        if (_exts == null || _exts.isEmpty())
            return;

        OpenJPAConfiguration conf = getRepository().getConfiguration();
        Log log = conf.getLog(conf.LOG_METADATA);
        if (!log.isWarnEnabled())
            return;

        Collection validNames = new TreeSet();
        addExtensionKeys(validNames);

        // this is where we store things like "jdbc-" for a
        // prefix for an extension name that we won't validate; that
        // way a new vendor could theoretically add in their
        // own prefix into the localizer.properties file and
        // not have to issue warnings for their extensions
        String prefixes = _loc.get("extension-datastore-prefix").getMessage();
        String[] allowedPrefixes = null;
        if (prefixes != null)
            allowedPrefixes = Strings.split(prefixes, ",", 0);

        Object next;
        String key;
        outer:
        for (Iterator itr = _exts.keySet().iterator(); itr.hasNext();) {
            next = itr.next();
            if (!OPENJPA.equals(getVendor(next)))
                continue;
            key = getKey(next);
            if (validNames.contains(key))
                continue;

            if (allowedPrefixes != null) {
                for (int j = 0; j < allowedPrefixes.length; j++) {
                    if (key.startsWith(allowedPrefixes[j])
                        && !validateDataStoreExtensionPrefix
                        (allowedPrefixes[j]))
                        continue outer;
                }
            }

            // try to determine if there are any other names that are
            // similiar to this one, so we can add in a hint
            String closestName = StringDistance.getClosestLevenshteinDistance
                (key, validNames, 0.5f);

            if (closestName == null)
                log.warn(_loc.get("unrecognized-extension", this,
                    key, validNames));
            else
                log.warn(_loc.get("unrecognized-extension-hint",
                    new Object[]{ this, key, validNames, closestName }));
        }
    }

    /**
     * Add all the known extension keys to the specified collection; any
     * implementation that utilized new extensions should override this
     * method to include both the known extensions of its superclass as well
     * as its own extension keys.
     *
     * @since 0.3.1.3
     */
    protected void addExtensionKeys(Collection exts) {
        // no extensions by default
    }

    /**
     * Return true if extensions starting with the given official datastore
     * prefix should be validated for this runtime.
     */
    protected boolean validateDataStoreExtensionPrefix(String prefix) {
        return false;
    }

    /**
     * Return the metadata repository.
     */
    public abstract MetaDataRepository getRepository();

    /**
     * Create a hash key for the given vendor/key combo.
     */
    private Object getHashKey(String vendor, String key) {
        if (OPENJPA.equals(vendor))
            return key;
        return new HashKey(vendor, key);
    }

    /**
     * Extract the vendor from the given hash key.
     */
    private String getVendor(Object hashKey) {
        return (hashKey instanceof String) ? OPENJPA :
            ((HashKey) hashKey).vendor;
    }

    /**
     * Extract the key from the given hash key.
     */
    private String getKey(Object hashKey) {
        return (hashKey instanceof String) ? (String) hashKey
            : ((HashKey) hashKey).key;
    }

    /**
     * Key class.
     */
    private static class HashKey 
        implements Serializable {

        public final String vendor;
        public final String key;

        public HashKey(String vendor, String key) {
            this.vendor = vendor;
            this.key = key;
        }

        public int hashCode() {
            int i = 0;
            if (vendor != null)
                i = vendor.hashCode();
            if (key != null)
                i += 17 * key.hashCode();
            return i;
        }

        public boolean equals(Object other) {
            if (other == this)
                return true;
            HashKey hk = (HashKey) other;
            return StringUtils.equals(vendor, hk.vendor)
                && StringUtils.equals(key, hk.key);
        }
    }

    /**
     * Embedded extensions implementation.
     */
    private static class EmbeddedExtensions
        extends Extensions {

        private final Extensions _parent;

        public EmbeddedExtensions(Extensions parent) {
            _parent = parent;
        }

        public MetaDataRepository getRepository ()
		{
			return _parent.getRepository ();
		}
	}
}
