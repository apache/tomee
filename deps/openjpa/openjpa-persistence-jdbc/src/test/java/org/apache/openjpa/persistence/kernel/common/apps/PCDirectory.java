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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Column;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;

/**
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 */
@Entity
@FetchGroups({
@FetchGroup(name = "name+parent+grandparent", attributes = {
@FetchAttribute(name = "_name"),
@FetchAttribute(name = "_parent", recursionDepth = 2)
    }),
@FetchGroup(name = "name+parent+grandparent+greatgrandparent", attributes = {
@FetchAttribute(name = "_name"),
@FetchAttribute(name = "_parent", recursionDepth = 3)
    }),
@FetchGroup(name = "name+parent", attributes = {
@FetchAttribute(name = "_name"),
@FetchAttribute(name = "_parent")
    }),
@FetchGroup(name = "allparents", attributes = {
@FetchAttribute(name = "_name"),
@FetchAttribute(name = "_parent", recursionDepth = -1)
    }),
@FetchGroup(name = "name", attributes = @FetchAttribute(name = "_name"))
    })
public class PCDirectory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name="name_col")
    private String _name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private PCDirectory _parent;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<PCDirectory> _children;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<PCFile> _files;

    /**
     *
     */
    public PCDirectory() {
        super();
    }

    public PCDirectory(String name) {
        super();
        _name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return _name;
    }

    public PCDirectory getParent() {
        return _parent;
    }

    public Set getChildren() {
        return _children;
    }

    public Set getFiles() {
        return _files;
    }

    public void add(PCDirectory dir) {
        if (dir == null)
            throw new NullPointerException("null directory");
        if (dir.getParent() != null && dir.getParent() != this)
            throw new IllegalArgumentException(dir + " has a different parent");
        if (_children == null)
            _children = new HashSet();
        _children.add(dir);
        dir._parent = this;
    }

    public PCFile add(String name) {
        if (name == null)
            throw new NullPointerException("null file");
        PCFile file = new PCFile(this, name);
        if (_files == null)
            _files = new HashSet();
        if (_files.contains(file))
            throw new IllegalArgumentException("duplicate file" + file);
        _files.add(file);
        return file;
    }

    public boolean isChild(PCDirectory dir, boolean recurse) {
        if (_children == null)
            return false;

        if (_children.contains(dir))
            return true;
        if (recurse) {
            Iterator i = _children.iterator();
            while (i.hasNext()) {
                PCDirectory child = (PCDirectory) i.next();
                if (child.isChild(dir, recurse))
                    return true;
            }
        }
        return false;
    }

    public boolean isChild(PCFile file, boolean recurse) {
        if (_files != null && _files.contains(file))
            return true;

        if (_children == null)
            return false;

        if (recurse) {
            Iterator i = _children.iterator();
            while (i.hasNext()) {
                PCDirectory child = (PCDirectory) i.next();
                if (child.isChild(file, recurse))
                    return true;
            }
        }
        return false;
    }

    public static Object reflect(PCDirectory instance, String name) {
        if (instance == null)
            return null;
        try {
            return PCDirectory.class.getDeclaredField(name).get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
