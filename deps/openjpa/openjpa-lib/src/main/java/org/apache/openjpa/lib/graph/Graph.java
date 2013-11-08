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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Graph representation using the adjacency list form.  See the book
 * 'Introduction to Algorithms' by Cormen, Leiserson, and Rivest.</p>
 *
 * @author Abe White
 * @since 1.0.0
 * @nojavadoc
 */
public class Graph {

    /**
     * Map each node to list of edges from that node.
     * Using a LinkedHashMap to ensure order of iterator processing.
     */ 
    private final Map<Object, Collection<Edge>> _nodes = new LinkedHashMap<Object, Collection<Edge>>();

    /**
     * Clear the graph.
     */
    public void clear() {
        _nodes.clear();
    }

    /**
     * Return true if the graph contains the given node.
     */
    public boolean containsNode(Object node) {
        return _nodes.containsKey(node);
    }

    /**
     * Return a view of all nodes in the graph.
     */
    public Collection<Object> getNodes() {
        return _nodes.keySet();
    }

    /**
     * Add a node to the graph.  Adding a node a second time has no effect.
     */
    public void addNode(Object node) {
        if (node == null)
            throw new NullPointerException("node = null");
        if (!containsNode(node))
            _nodes.put(node, null);
    }

    /**
     * Remove a node from the graph.  All edges to and from the node
     * will be cleared.
     *
     * @return true if the node was removed, false otherwise
     */
    public boolean removeNode(Object node) {
        boolean rem = containsNode(node);
        if (rem) {
            Collection<Edge> edges = getEdgesTo(node);
            for (Edge edge : edges)
                removeEdge( edge);
            _nodes.remove(node);
        }
        return rem;
    }

    /**
     * Return all edges in the graph.
     */
    public Collection<Edge> getEdges() {
        Collection<Edge> all = new HashSet<Edge>();
        for (Collection<Edge> edges : _nodes.values()) {
        	if (edges != null)
        		all.addAll(edges);
        }
        return all;
    }

    /**
     * Return all the edges from a particular node.
     */
    public Collection<Edge> getEdgesFrom(Object node) {
        Collection<Edge> edges = _nodes.get(node);
        if(edges == null ) {
            edges = Collections.emptyList();
        }
        return edges; 
    }

    /**
     * Return all the edges to a particular node.
     */
    public Collection<Edge> getEdgesTo(Object node) {
    	Collection<Edge> edges = getEdges();
    	Collection<Edge> to = new ArrayList<Edge>();
    	for (Edge edge : edges) {
    		if (edge.isTo(node))
    			to.add(edge);
    	}
    	return to;
    }

    /**
     * Return all the edges from one node to another.
     */
    public Collection<Edge> getEdges(Object from, Object to) {
        Collection<Edge> edges = getEdgesFrom(from);
        Collection<Edge> matches = new ArrayList<Edge>(edges.size());
        for (Edge edge : edges) {
            if (edge.isTo(to))
                matches.add(edge);
        }
        return matches;
    }

    /**
     * Add an edge to the graph.
     */
    public void addEdge(Edge edge) {
        if (!containsNode(edge.getTo()))
            throw new IllegalArgumentException(edge.getTo().toString());
        if (!containsNode(edge.getFrom()))
            throw new IllegalArgumentException(edge.getFrom().toString());

        Collection<Edge> from = _nodes.get(edge.getFrom());
        if (from == null) {
            from = new ArrayList<Edge>(3);
            _nodes.put(edge.getFrom(), from);
        }
        from.add(edge);

        if (!edge.isDirected() && !edge.getFrom().equals(edge.getTo())) {
            Collection<Edge> to = _nodes.get(edge.getTo());
            if (to == null) {
                to = new ArrayList<Edge>(3);
                _nodes.put(edge.getTo(), to);
            }
            to.add(edge);
        }
    }

    /**
     * Remove an edge from the graph.
     *
     * @return true if the edge was removed, false if not in the graph
     */
    public boolean removeEdge(Edge edge) {
        Collection<Edge> edges = _nodes.get(edge.getFrom());
        if (edges == null)
            return false;
        boolean rem = edges.remove(edge);
        if (rem && !edge.isDirected()) {
            edges = _nodes.get(edge.getTo());
            if (edges != null)
                edges.remove(edge);
        }
        return rem;
    }

    /**
     *	Clear all nodes and edges of the bookkeeping information from their
     *	last traversal.
     */
    public void clearTraversal() {
    	for (Collection<Edge> edges : _nodes.values()) {
    		if (edges != null)
    			for (Edge edge : edges)
    				edge.clearTraversal();
    	}
    }
}
