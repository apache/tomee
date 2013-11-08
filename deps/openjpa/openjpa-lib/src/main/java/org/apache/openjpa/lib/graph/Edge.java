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
package org.apache.openjpa.lib.graph;

import java.util.List;

/**
 * <p>A graph edge.  Includes the from and to nodes, an arbitrary user object,
 * and a weight.  Edges can be either directed or undirected.</p>
 *
 * @author Abe White
 * @since 1.0.0
 * @nojavadoc
 */
public class Edge {

    /**
     * An edge (u, v) is a tree edge if node v was first discovered by
     * traversing the edge.
     */
    public static final int TYPE_TREE = 1;

    /**
     * An edge (u, v) is a back edge if it creates a cycle back to an
     * ancestor in the graph.
     */
    public static final int TYPE_BACK = 2;

    /**
     * An edge (u, v) is a forward edge if it is not a tree or back edge.
     */
    public static final int TYPE_FORWARD = 3;

    private final Object _from;
    private final Object _to;
    private final boolean _directed;

    private int _type = 0;
    private double _weight = 0;
    private Object _userObj = null;
    private List<Edge> _cycle = null;
    private boolean _removedFromGraph = false;

    /**
     * Constructor.
     *
     * @param    from        the node the edge comes from
     * @param    to            the node the edge goes to
     * @param    directed    whether the edge is directed
     */
    public Edge(Object from, Object to, boolean directed) {
        if (from == null)
            throw new NullPointerException("from == null");
        if (to == null)
            throw new NullPointerException("to == null");
        _from = from;
        _to = to;
        _directed = directed;
    }

    /**
     * Constructor.
     *
     * @param    from        the node the edge comes from
     * @param    to            the node the edge goes to
     * @param    directed    whether the edge is directed
     * @param    userObject    an associated object
     */
    public Edge(Object from, Object to, boolean directed, Object userObject) {
        this(from, to, directed);
        _userObj = userObject;
    }

    /**
     * Return the node the edge links from.
     */
    public Object getFrom() {
        return _from;
    }

    /**
     * Return the node the edge links to.
     */
    public Object getTo() {
        return _to;
    }

    /**
     * Return the node on the opposite end of the given one, or null if the
     * given node is not part of this edge.
     */
    public Object getOther(Object node) {
        if (_to.equals(node))
            return _from;
        if (_from.equals(node))
            return _to;
        return null;
    }

    /**
     * Return true if this edge links to the given node.  For undirected edges,
     * this method returns true if either side is equal to the given node.
     */
    public boolean isTo(Object node) {
        return _to.equals(node) || (!_directed && _from.equals(node));
    }

    /**
     * Return true if this edge links from the given node.  For undirected
     * edges, this method returns true if either side is equal to the given
     * node.
     */
    public boolean isFrom(Object node) {
        return _from.equals(node) || (!_directed && _to.equals(node));
    }

    /**
     * Return whether the edge is directed.
     */
    public boolean isDirected() {
        return _directed;
    }

    /**
     * Return the weight of the edge.
     */
    public double getWeight() {
        return _weight;
    }

    /**
     * Set the weight of the edge.
     */
    public void setWeight(double weight) {
        _weight = weight;
    }

    /**
     * Arbitrary user object associated with the edge.
     */
    public Object getUserObject() {
        return _userObj;
    }

    /**
     * Arbitrary user object associated with the edge.
     */
    public void setUserObject(Object obj) {
        _userObj = obj;
    }

    /**
     * Traversal bookkeeping info.
     */
    public int getType() {
        return _type;
    }

    /**
     * Traversal bookkeeping info.
     */
    public void setType(int type) {
        _type = type;
    }

    /**
     * List of edges forming a cycle. Only set for TYPE_BACK and TYPE_FORWARD
     * edges.
     */
    public List<Edge> getCycle() {
        return _cycle;
    }
    
    /**
     * List of edges forming a cycle. Only set for TYPE_BACK and TYPE_FORWARD
     * edges.
     */
    public void setCycle(List<Edge> cycle) {
        _cycle = cycle;
    }

    /**
     * Returns if this edge is (still) part of the graph.
     */
    public boolean isRemovedFromGraph() {
        return _removedFromGraph;
    }

    /**
     * Mark this edge as removed from the graph.
     */
    public void setRemovedFromGraph() {
        this._removedFromGraph = true;
    }

    /**
     * Clear traversal info.
     */
    public void clearTraversal() {
        _type = 0;
        _cycle = null;
    }

    public String toString() {
        return super.toString() + "[from=" + getFrom() + ";to=" + getTo()
            + ";directed=" + isDirected () + ";weight=" + getWeight () + "]";
	}
}
