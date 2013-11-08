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

import java.io.File;
import java.io.Serializable;

import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.xml.Commentable;

/**
 * Metadata about a persistence-aware type.
 *
 * @author Pinaki Poddar
 */
public class NonPersistentMetaData 
	implements Comparable, SourceTracker, Commentable, MetaDataContext,
        Serializable {
    public static final int TYPE_PERSISTENCE_AWARE = 1;
    public static final int TYPE_NON_MAPPED_INTERFACE = 2;

    private final MetaDataRepository _repos;
	private final Class _class;
    private final int _type;
	
    private File _srcFile = null;
    private int _lineNum = 0;  
    private int _colNum = 0;  
    
    private int _srcType = SRC_OTHER;
    private String[] _comments = null;
    private int _listIndex = -1;
	
	protected NonPersistentMetaData(Class cls, MetaDataRepository repos, 
        int type) {
		_repos = repos;
		_class = cls;
        _type = type;
	}
	
    /**
     * Owning repository.
     */
	public MetaDataRepository getRepository() {
		return _repos;
	}
	
    /**
     * Persistence-aware type.
     */
	public Class getDescribedType() {
		return _class;
	}

    /**
     * The type of metadata.
     */
    public int getType() {
        return _type;
    }
	
    /**
     * The index in which this class was listed in the metadata. Defaults to
     * <code>-1</code> if this class was not listed in the metadata.
     */
    public int getListingIndex() {
        return _listIndex;
    }

    /**
     * The index in which this field was listed in the metadata. Defaults to
     * <code>-1</code> if this class was not listed in the metadata.
     */
    public void setListingIndex(int index) {
        _listIndex = index;
    }

    public File getSourceFile() {
        return _srcFile;
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File file, int srcType) {
        _srcFile = file;
        _srcType = srcType;
    }

    public int getLineNumber() {
        return _lineNum;
    }

    public void setLineNumber(int lineNum) {
        _lineNum = lineNum;
    }

    public int getColNumber() {
        return _colNum;
    }

    public void setColNumber(int colNum) {
        _colNum = colNum;
    }
    
    public String getResourceName() {
        return _class.getName();
    }

    public String[] getComments() {
        return (_comments == null) ? ClassMetaData.EMPTY_COMMENTS : _comments;
    }

    public void setComments(String[] comments) {
        _comments = comments;
    }
    
    public int compareTo(Object o) {
        if (o == this)
            return 0;
        if (!(o instanceof NonPersistentMetaData))
        	return 1;
        NonPersistentMetaData other = (NonPersistentMetaData) o;
        if (_type != other.getType())
            return _type - other.getType();
        return _class.getName().compareTo(other.getDescribedType().getName());
    }
}
