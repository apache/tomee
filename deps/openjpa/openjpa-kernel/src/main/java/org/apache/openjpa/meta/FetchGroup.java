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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Captures fetch group meta-data.
 *  
 * Fetch Group is identified and referred by its immutable name.
 * Fetch Group can nest other groups. The nested group reference is the name of the nested group.
 * 
 * Defines two <em>standard</em> fetch group named <tt>default</tt> and <tt>all</tt>. 
 */
@SuppressWarnings("serial")
public class FetchGroup 
    implements Serializable {

    /**
     * Name of the default fetch group.
     */
    public static final String NAME_DEFAULT = "default";

    /**
     * Name of the "all" fetch group.
     */
    public static final String NAME_ALL = "all";

    /**
     * Default field recursion depth.
     */
    public static final int RECURSION_DEPTH_DEFAULT = 1;

    /**
     * Infinite depth.
     */
	public static final int DEPTH_INFINITE = -1;

    /**
     *  Standard default fetch group.
     */
    static final FetchGroup DEFAULT = new FetchGroup(NAME_DEFAULT, true);

    /**
     *  Standard "all" fetch group.
     */
    static final FetchGroup ALL = new FetchGroup(NAME_ALL, false);

    private static final FieldMetaData[] EMPTY_FIELD_ARRAY = {}; 
    private static final Localizer _loc = Localizer.forPackage(FetchGroup.class);

    private final String        _name;
    private final ClassMetaData _meta;
    private final boolean       _readOnly;
    private List<String>        _includes;
    private Set<String>         _containedBy;
    private Map<FieldMetaData,Number> _depths;
    private Boolean             _postLoad;

    /**
     * Constructor; supply immutable name.
     *
     * @param cm class meta data that owns this group. Can be null for standard groups.
     * @param name must not by null or empty.
     */
    FetchGroup(ClassMetaData cm, String name) {
        _meta = cm;
        _name = name;
        _readOnly = false;
    }

    /**
     * Internal constructor for built-in fetch groups.
     */
    private FetchGroup(String name, boolean postLoad) {
        _meta = null;
        _name = name;
        _postLoad = (postLoad) ? Boolean.TRUE : Boolean.FALSE;
        _readOnly = true;
    }

    /**
     * Copy state from the given fetch group.
     */
    void copy(FetchGroup fg) {
        if (fg._includes != null) {
            for (String included : fg._includes) {
                addDeclaredInclude(included);
            }
        }
        if (fg._containedBy != null) {
        	this._containedBy = new HashSet<String>(fg._containedBy);
        }
        if (fg._depths != null) {
            for (Map.Entry<FieldMetaData,Number> entry : fg._depths.entrySet()) { 
                setRecursionDepth(entry.getKey(), entry.getValue().intValue());
            }
        }
        if (fg._postLoad != null) {
            _postLoad = fg._postLoad;
        }
    }

    /**
     * Fetch group name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Includes given fetch group within this receiver.
     */
    public void addDeclaredInclude(String fgName) {
        if (_readOnly)
            throw new UnsupportedOperationException();
        if (StringUtils.isEmpty(fgName))
            throw new MetaDataException(_loc.get("null-include-fg", this));
        if (_includes == null)
            _includes = new ArrayList<String>();
        if (!_includes.contains(fgName))
            _includes.add(fgName);
    }

    /**
     * Affirms if given fetch group is included by this receiver.  Includes
     * superclass definition of fetch group and optionally other included 
     * groups.
     *
     * @param recurse if true then recursively checks within the included
     * fecth groups
     */
    public boolean includes(String fgName, boolean recurse) {
        // check our includes
        if (_includes != null) {
            if (_includes.contains(fgName))
                return true;
            if (recurse && _meta !=null) {
                FetchGroup fg;
                for (String included : _includes) {
                    fg = _meta.getFetchGroup(included);
                    if (fg != null && fg.includes(fgName, true)) {
                        return true;
                    }
                }
            }
        }
        if (_meta != null) {
            // check superclass includes
            ClassMetaData sup = _meta.getPCSuperclassMetaData();
            if (sup != null) {
                FetchGroup supFG = sup.getFetchGroup(_name);
                if (supFG != null)
                    return supFG.includes(fgName, recurse);
            }
        }
        return false;
    }
    
    /**
     * Adds this receiver as one of the included fetch groups of the given
     * parent. 
     * The parent fetch group will include this receiver as a side-effect of
     * this call.
     * 
     * @see #includes(String, boolean)
     * @see #addDeclaredInclude(String) 
     * 
     * @return true if given parent is a new addition. false othrwise.
     * @since 1.1.0
     */
    public boolean addContainedBy(FetchGroup parent) {
    	parent.addDeclaredInclude(this.getName());
    	if (_containedBy==null)
    		_containedBy = new HashSet<String>();
    	return _containedBy.add(parent.getName());
    }
    
    /**
     * Gets the name of the fetch groups in which this receiver has been
     * included.
     * 
     * @see #addContainedBy(FetchGroup)
     * @since 1.1.0
     */
    public Set<String> getContainedBy() {
        if (_containedBy == null)
            return Collections.emptySet();
    	return Collections.unmodifiableSet(_containedBy);
    }

    /**
     * Return the fetch group names declared included by this group.
     */
    public String[] getDeclaredIncludes() {
        // only used during serialization; no need to cache
        return (_includes == null) ? new String[0] : _includes.toArray(new String[_includes.size()]);
    }

    /**
     * Recursion depth for the given field.  This is the depth of relations of
     * the same class as this one we can fetch through the given field.
     */
    public void setRecursionDepth(FieldMetaData fm, int depth) {
        if (_readOnly)
            throw new UnsupportedOperationException();
        if (depth < -1)
            throw new MetaDataException(_loc.get("invalid-fg-depth", _name, fm, 
                depth));
        if (_depths == null)
            _depths = new HashMap<FieldMetaData, Number>();
        _depths.put(fm, depth);
    }

    /**
     * Recursion depth for the given field.  This is the depth of relations of
     * the same class as this one we can fetch through the given field.
     */
    public int getRecursionDepth(FieldMetaData fm) {
        Number depth = findRecursionDepth(fm);
        return (depth == null) ? RECURSION_DEPTH_DEFAULT : depth.intValue();
    }

    /**
     * Return the recursion depth declared for the given field, or 
     * 0 if none.
     */
    public int getDeclaredRecursionDepth(FieldMetaData fm) {
        Number depth = (_depths == null) ? null : _depths.get(fm);
        return (depth == null) ? 0 : depth.intValue();
    }

    /**
     * Helper to find recursion depth recursively in our includes.
     */
    private Number findRecursionDepth(FieldMetaData fm) { 
        Number depth = (_depths == null) ? null : _depths.get(fm);
        if (depth != null)
            return depth;

        // check for superclass declaration of depth
        Number max = null;
        if (_meta != null && fm.getDeclaringMetaData() != _meta) {
            ClassMetaData sup = _meta.getPCSuperclassMetaData();
            if (sup != null) {
                FetchGroup supFG = sup.getFetchGroup(_name);
                if (supFG != null)
                    max = supFG.findRecursionDepth(fm);
            }
        }
        if (_includes == null)
            return max;

        // find largest included depth
        FetchGroup fg;
        for (String included : _includes) {
            fg = _meta.getFetchGroup(included);
            depth = (fg == null) ? null : fg.findRecursionDepth(fm);
            if (depth != null && (max == null || depth.intValue() > max.intValue()))
                max = depth;
        }
        return max;
    }

    /**
     * Return the fields with declared recursion depths in this group.
     */
    public FieldMetaData[] getDeclaredRecursionDepthFields() {
        // used in serialization only; no need to cache
        if (_depths == null)
            return EMPTY_FIELD_ARRAY;
         return _depths.keySet().toArray(new FieldMetaData[_depths.size()]);
    } 

    /**
     * Whether loading this fetch group causes a post-load callback on the
     * loaded instance.
     */
    public void setPostLoad (boolean flag) {
        if (_readOnly && flag != isPostLoad())
            throw new UnsupportedOperationException();
    	_postLoad = (flag) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Whether loading this fetch group causes a post-load callback on the
     * loaded instance.
     */
    public boolean isPostLoad () {
    	if (_postLoad != null)
            return _postLoad.booleanValue();

        if (_meta != null) {
            ClassMetaData sup = _meta.getPCSuperclassMetaData();
            if (sup != null) {
                FetchGroup supFG = sup.getFetchGroup(_name);
                if (supFG != null && supFG.isPostLoad())
                    return true;
            }
        }

        if (_includes == null)
            return false;
        FetchGroup fg;
        for (String included : _includes) {
            fg = _meta.getFetchGroup(included);
            if (fg != null && fg.isPostLoad())
                return true;
        }
        return false;
    }

    /**
     * Whether the post-load value is declared for this group.  
     */
    public boolean isPostLoadExplicit() {
        return _postLoad != null;
    }

    /**
     * Resolve and validate fetch group meta-data.
     */
    public void resolve() {
        if (_includes == null)
            return;

        // validate includes
        FetchGroup fg;
        for (String name : _includes) {
            if (name.equals(_name))
                throw new MetaDataException(_loc.get("cyclic-fg", this, name));
            fg = _meta.getFetchGroup(name);
            if (fg == null)
                throw new MetaDataException(_loc.get("bad-fg-include", this, name));
            if (fg.includes(_name, true))
                throw new MetaDataException(_loc.get("cyclic-fg", this, name));
        }
    }
    
    /**
     * Affirms equality if the other has the same name and declaring type.
     */
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof FetchGroup))
            return false;
        FetchGroup that = (FetchGroup) other;
        return _name.equals(that._name)
            && ObjectUtils.equals(_meta, that._meta);
    }

    public int hashCode() {
        return _name.hashCode() + ((_meta == null) ? 0 : _meta.hashCode());
    }

    public String toString() {
        return ((_meta == null) ? "Builtin" : _meta.toString ()) + "." + _name;
    }
}
