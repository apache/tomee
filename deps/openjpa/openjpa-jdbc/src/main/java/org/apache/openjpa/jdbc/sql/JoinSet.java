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
package org.apache.openjpa.jdbc.sql;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.ObjectUtils;

/**
 * Set type that recognizes that inner joins should take precedence
 * over otherwise equal outer joins.
 *
 * @author Abe White
 */
class JoinSet {

    // the joins are stored as an adjacency list graph.  it's a reasonably
    // efficient representation with O(1) lookup, add, remove operations for
    // typical sets of joins, and it means we'd have to create a graph anyway
    // when joinIterator() is called
    private final List _graph = new ArrayList();
    private int _size = 0;
    private List _sorted = null;

    public JoinSet() {
    }

    public JoinSet(JoinSet copy) {
        for (int i = 0; i < copy._graph.size(); i++) {
            if (copy._graph.get(i) == null)
                _graph.add(null);
            else
                _graph.add(((Node) copy._graph.get(i)).clone());
        }
        _size = copy._size;
        _sorted = copy._sorted;
    }

    /**
     * Return the recorded join matching the given join's aliases.
     */
    public Join getRecordedJoin(Join join) {
        if (join == null)
            return null;
        Node node = getNode(join, join.getIndex1());
        return (node == null) ? null : node.join;
    }

    /**
     * Return the node for the specified join and index.
     */
    private Node getNode(Join join, int idx) {
        if (_graph.size() <= idx)
            return null;
        Node node = (Node) _graph.get(idx);
        for (; node != null; node = node.next)
            if (node.join.equals(join))
                return node;
        return null;
    }

    /**
     * Return the logical last join.
     */
    public Join last() {
        if (_size == 0)
            return null;
        Node node = (Node) _graph.get(_graph.size() - 1);
        while (node.next != null)
            node = node.next;
        return node.join;
    }

    /**
     * Iterator over joins that prepares them for SQL translation.
     */
    public Iterator joinIterator() {
        if (_size < 2)
            return iterator();
        if (_sorted != null)
            return _sorted.iterator();

        List sorted = new ArrayList(_size);
        LinkedList queue = new LinkedList();
        BitSet seen = new BitSet(_graph.size() * _graph.size()
            + _graph.size());

        // traverse graph
        Node n;
        int idx, sidx;
        for (int i = 0; i < _graph.size(); i++) {
            // seed queue with next set of disconnected joins
            for (n = (Node) _graph.get(i); n != null; n = n.next) {
                sidx = getSeenIndex(n.join);
                if (!seen.get(sidx)) {
                    seen.set(sidx);
                    queue.add(n);
                }
            }
            if (queue.isEmpty())
                continue;

            // traverse from those joins to reachables
            while (!queue.isEmpty()) {
                n = (Node) queue.removeFirst();

                // don't repeat a join to a table we've already joined, but
                // do traverse through it in the graph (the first indexes of
                // the seeen bitset are reserved for joined-to tables)
                idx = (n.forward) ? n.join.getIndex2() : n.join.getIndex1();
                if (!seen.get(idx)) {
                    sorted.add((n.forward) ? n.join : n.join.reverse());
                    seen.set(idx);
                }

                for (n = (Node) _graph.get(idx); n != null; n = n.next) {
                    sidx = getSeenIndex(n.join);
                    if (!seen.get(sidx)) {
                        seen.set(sidx);
                        queue.add(n);
                    }
                }
            }
        }
        _sorted = sorted;
        return _sorted.iterator();
    }

    /**
     * We create a unique index for recording seen nodes by
     * treating the joined indexes as a base (max-index) number.
     */
    private int getSeenIndex(Join join) {
        // we reserve indexes 0..._graph.size() for joined tables
        return join.getIndex1() * _graph.size() + join.getIndex2()
            + _graph.size();
    }

    public boolean add(Join join) {
        if (join.getType() == Join.TYPE_OUTER) {
            // outer shouldn't override any other join type
            if (!contains(join)) {
                addNode(join);
                return true;
            }
            return false;
        }

        // replace any existing join with this one
        Node node = getNode(join, join.getIndex1());
        if (node != null) {
            node.join = join;
            getNode(join, join.getIndex2()).join = join;
            _sorted = null;
        } else
            addNode(join);
        return true;
    }

    public boolean addAll(JoinSet js) {
        if (js.isEmpty())
            return false;

        boolean added = false;
        for (Iterator itr = js.iterator(); itr.hasNext();)
            added = add((Join) itr.next()) || added;
        return added;
    }

    /**
     * Add the give join to our graph.
     */
    private void addNode(Join join) {
        _sorted = null;

        int size = Math.max(join.getIndex1(), join.getIndex2()) + 1;
        while (_graph.size() < size)
            _graph.add(null);

        Node node = (Node) _graph.get(join.getIndex1());
        if (node == null)
            _graph.set(join.getIndex1(), new Node(join, true));
        else {
            while (node.next != null)
                node = node.next;
            node.next = new Node(join, true);
        }

        node = (Node) _graph.get(join.getIndex2());
        if (node == null)
            _graph.set(join.getIndex2(), new Node(join, false));
        else {
            while (node.next != null)
                node = node.next;
            node.next = new Node(join, false);
        }
        _size++;
    }

    public Iterator iterator() {
        return new Iterator() {
            private Node _next = null;
            private int _idx = -1;

            public boolean hasNext() {
                if (_next != null)
                    return true;

                while (++_idx < _graph.size()) {
                    _next = (Node) _graph.get(_idx);
                    while (_next != null && !_next.forward)
                        _next = _next.next;
                    if (_next != null)
                        return true;
                }
                return false;
            }

            public Object next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                Join j = _next.join;
                do {
                    _next = _next.next;
                } while (_next != null && !_next.forward);
                return j;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean remove(Join join) {
        if (join == null || _graph.size() <= join.getIndex1())
            return false;
        if (remove(join, join.getIndex1())) {
            _size--;
            return remove(join, join.getIndex2());
        }
        return false;
    }

    /**
     * Each join is recorded one at its first index and once at its second;
     * remove the join at one of its indexes.
     */
    private boolean remove(Join join, int idx) {
        Node node = (Node) _graph.get(idx);
        for (Node prev = null; node != null; prev = node, node = node.next) {
            if (!node.join.equals(join))
                continue;

            if (prev != null)
                prev.next = node.next;
            else {
                _graph.set(idx, node.next);
                // trim to size
                while (!_graph.isEmpty() && _graph.get(idx) == null
                    && idx == _graph.size() - 1)
                    _graph.remove(idx--);
            }
            return true;
        }
        return false;
    }

    public boolean removeAll(JoinSet js) {
        boolean remd = false;
        for (Iterator itr = js.iterator(); itr.hasNext();)
            remd = remove((Join) itr.next()) || remd;
        return remd;
    }

    public boolean retainAll(JoinSet js) {
        boolean remd = false;
        Join join;
        for (Iterator itr = iterator(); itr.hasNext();) {
            join = (Join) itr.next();
            if (!js.contains(join))
                remd = remove(join);
        }
        return remd;
    }

    public void clear() {
        _graph.clear();
        _sorted = null;
        _size = 0;
    }

    public boolean contains(Join join) {
        return getRecordedJoin(join) != null;
    }

    public boolean containsAll(JoinSet js) {
        if (js._size > _size || js._graph.size() > _graph.size())
            return false;
        for (Iterator itr = js.iterator(); itr.hasNext();)
            if (!contains((Join) itr.next()))
                return false;
        return true;
    }

    public boolean isEmpty() {
        return _size == 0;
    }

    public int size() {
        return _size;
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof JoinSet))
            return false;
        return _graph.equals(((JoinSet) other)._graph);
    }

    public int hashCode() {
        return _graph.hashCode();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (Iterator itr = iterator(); itr.hasNext();) {
            buf.append("<").append(itr.next()).append(">");
            if (itr.hasNext())
                buf.append(", ");
        }
        return buf.append("]").toString();
    }

    /**
     * A graph node.
     */
    private static class Node
        implements Cloneable {

        public Join join;
        public Node next;
        public boolean forward;

        public Node(Join join, boolean forward) {
            this.join = join;
            this.forward = forward;
        }

        public int hashCode() {
            int rs = 17;
            rs = 37 * rs + join.hashCode();
            if (next != null)
                rs = 37 * rs + next.hashCode();
            return rs;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Node))
                return false;
            Node node = (Node) other;
            return ObjectUtils.equals(join, node.join)
                && ObjectUtils.equals(next, node.next);
        }

        public Object clone() {
            try {
                Node node = (Node) super.clone();
                if (node.next != null)
                    node.next = (Node) node.next.clone();
                return node;
            } catch (CloneNotSupportedException cnse) {
                // can't happen
                return null;
            }
        }

        public String toString() {
            return join + "(" + ((forward) ? "forward" : "backward") + ")"
                + "; next: " + next;
        }
    }
}	
