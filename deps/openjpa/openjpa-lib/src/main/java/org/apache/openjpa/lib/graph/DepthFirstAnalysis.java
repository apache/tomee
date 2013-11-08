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

import org.apache.openjpa.lib.util.Localizer;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>Performs a depth-first analysis of a given {@link Graph}, caching
 * information about the graph's nodes and edges.  See the DFS algorithm
 * in the book 'Introduction to Algorithms' by Cormen, Leiserson, and
 * Rivest.  The algorithm has been modified to group sibling nodes without
 * connections together during the topological sort.</p>
 *
 * @author Abe White
 * @since 1.0.0
 * @nojavadoc
 */
public class DepthFirstAnalysis {

    private static final Localizer _loc = Localizer.forPackage
        (DepthFirstAnalysis.class);

    private final Graph _graph;
    private final Map<Object, NodeInfo> _nodeInfo = new HashMap<Object, NodeInfo>();
    private Comparator<Object> _comp;

    /**
     * Constructor.  Performs the analysis on the given graph and caches
     * the resulting information.
     */
    public DepthFirstAnalysis(Graph graph) {
        _graph = graph;

        // initialize node infos
        Collection<Object> nodes = graph.getNodes();
        for (Object node : nodes)
            _nodeInfo.put(node, new NodeInfo());

        // visit all nodes -- see intro to algo's book
        NodeInfo info;
        for (Object node : nodes) {
            info = _nodeInfo.get(node);
            if (info.color == NodeInfo.COLOR_WHITE)
                visit(graph, node, info, 0, new LinkedList<Edge>());
        }
    }

    /**
     * Visit a node.  See Introduction to Algorithms book for details.
     */
    private int visit(Graph graph, Object node, NodeInfo info, int time, 
        List<Edge> path) {
        // discover node
        info.color = NodeInfo.COLOR_GRAY;

        // explore all vertices from that node depth first
        Collection<Edge> edges = graph.getEdgesFrom(node);
        int maxChildTime = time - 1;
        int childTime;
        for (Edge edge : edges) {
        	Object other = edge.getOther(node);
        	NodeInfo otherInfo = _nodeInfo.get(other);
            if (otherInfo.color == NodeInfo.COLOR_WHITE) {
                // undiscovered node; recurse into it
                path.add(edge);
                childTime = visit(graph, other, otherInfo, time, path);
                path.remove(edge);
                edge.setType(Edge.TYPE_TREE);
            } else if (otherInfo.color == NodeInfo.COLOR_GRAY) {
                childTime = -1;
                edge.setType(Edge.TYPE_BACK);
                // calculate the cycle including this edge
                edge.setCycle(cycleForBackEdge(edge, path));
            } else {
                childTime = otherInfo.finished;
                edge.setType(Edge.TYPE_FORWARD);
                // find the cycle including this edge
                List<Edge> cycle = new LinkedList<Edge>();
                cycle.add(edge);
                if (cycleForForwardEdge(graph, other, node, cycle)) {
                    edge.setCycle(cycle);
                }
            }
            maxChildTime = Math.max(maxChildTime, childTime);
        }

        // finished with node
        info.color = NodeInfo.COLOR_BLACK;
        info.finished = maxChildTime + 1;
        return info.finished;
    }

    /**
     * Set the comparator that should be used for ordering groups of nodes
     * with the same dependencies.
     */
    public void setNodeComparator(Comparator<Object> comp) {
        _comp = comp;
    }

    /**
     * Return the nodes in topologically-sorted order.  This is often used
     * to order dependencies.  If each graph edge (u, v) represents a
     * dependency of v on u, then this method will return the nodes in the
     * order that they should be evaluated to satisfy all dependencies.  Of
     * course, if the graph is cyclic (has back edges), then no such ordering
     * is possible, though this method will still return the correct order
     * as if edges creating the cycles did not exist.
     */
    public List<Object> getSortedNodes() {
        Map.Entry<Object,NodeInfo>[] entries = 
        	_nodeInfo.entrySet().toArray(new Map.Entry[_nodeInfo.size()]);
        Arrays.sort(entries, new NodeInfoComparator(_comp));
        return new NodeList(entries);
    }

    /**
     * Return all edges of the given type.  This method can be used to
     * discover all edges that cause cycles in the graph by passing it
     * the {@link Edge#TYPE_BACK} or {@link Edge#TYPE_FORWARD} edge type.
     */
    public Collection<Edge> getEdges(int type) {
        Collection<Edge> typed = null;
        for (Object node : _graph.getNodes()) {
            for (Edge edge : _graph.getEdgesFrom(node)) {
                if (edge.getType() == type) {
                    if (typed == null)
                        typed = new ArrayList<Edge>();
                    typed.add(edge);
                }
            }
        }
        if(typed == null ) { 
            typed = Collections.emptyList();
        }
        return typed; 
    }

    /**
     * Return the logical time that the given node was finished in
     * the graph walk, or -1 if the node is not part of the graph.
     */
    public int getFinishedTime(Object node) {
        NodeInfo info = _nodeInfo.get(node);
        if (info == null)
            return -1;
        return info.finished;
    }

    /**
     * Returns a list of graph edges forming a cycle. The cycle begins 
     * with a type {@link Edge#TYPE_BACK} edge.
     * @param backEdge "Starting" edge of the cycle
     * @param path Continuous list of graph edges, may be null
     * @param pos Index of the first edge in path continuing the cycle
     * @return Cycle starting with a type {@link Edge#TYPE_BACK} edge
     */
    private List<Edge> buildCycle(Edge backEdge, List<Edge> path, int pos) {
        int length = path != null ? path.size() - pos : 0;
        List<Edge> cycle = new ArrayList<Edge>(length + 1);
        cycle.add(0, backEdge);
        for (int i = 0; i < length; i++) {
            cycle.add(i + 1, path.get(pos + i));
        }
        return cycle;
    }

    /**
     * Computes the list of edges forming a cycle. The cycle always exists for
     * a type {@link Edge#TYPE_BACK} edge. This method should only be called 
     * for type {@link Edge#TYPE_BACK} edges. 
     * @param edge Edge where the cycle was detected
     * @param path Path consisting of edges to the edge's starting node
     * @return Cycle starting with a type {@link Edge#TYPE_BACK} edge
     */
    private List<Edge> cycleForBackEdge(Edge edge, List<Edge> path) {
        if (edge.getType() != Edge.TYPE_BACK) {
            return null;
        }
        
        int pos = 0;
        if (path != null && !edge.getFrom().equals(edge.getTo())) {
            // Not a single edge loop
            pos = findNodeInPath(edge.getTo(), path);
            assert (pos >= 0): _loc.get("node-not-on-path", edge, edge.getTo());
        } else {
            assert (edge.getFrom().equals(edge.getTo())): 
                _loc.get("edge-no-loop", edge).getMessage();
            path = null;
        }
        List<Edge> cycle = buildCycle(edge, path, pos); 
        assert (cycle != null): _loc.get("cycle-null", edge).getMessage();
        return cycle;
    }

    /**
     * Computes the cycle of edges including node cycleTo. The cycle must not 
     * necessarily exist. This method should only be called for type 
     * {@link Edge#TYPE_FORWARD} edges.
     * @param graph Graph
     * @param node Current node
     * @param cycleTo End node for loop
     * @param path Path from loop end node to current node
     * @return True if a cycle has been found. The cycle will be contained in
     * the <code>path</code> parameter.
     */
    private boolean cycleForForwardEdge(Graph graph, Object node,
        Object cycleTo, List<Edge> path) {                   
        boolean found = false;
        Collection<Edge> edges = graph.getEdgesFrom(node);
        for (Edge edge : edges) {
            Object other = edge.getOther(node);
            // Single edge loops are ignored
            if (!node.equals(other)) {
                if (other.equals(cycleTo)) {
                    // Cycle complete
                    path.add(edge);
                    found = true;
                } else if (!path.contains(edge)){
                    // Walk this edge
                    path.add(edge);
                    found = cycleForForwardEdge(graph, other, cycleTo, path);
                    if (!found) {
                        // Remove edge again
                        path.remove(edge);                    
                    }
                }
            }
        }
        return found;
    }
    
    /**
     * Finds the position of the edge starting from a particular node in the 
     * continuous list of edges.
     * @param node Node on the cycle.
     * @param path Continuous list of graph edges.
     * @return Edge index if found, -1 otherwise.
     */
    private int findNodeInPath(Object node, List<Edge> path) {
        int pos = -1;
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                if ( path.get(i).getFrom().equals(node)) {
                    pos = i;
                }
            }
        }
        return pos;
    }

    /**
     * Test, if the analysis didn't find cycles.
     */
    public boolean hasNoCycles() {
        // a) there must not be any back edges
        if (!getEdges(Edge.TYPE_BACK).isEmpty()) {
            return false;
        }
        // b) there might be forward edges
        // make sure these don't indicate cycles
        Collection<Edge> edges = getEdges(Edge.TYPE_FORWARD);
        if (!edges.isEmpty()) {
            for (Edge edge : edges) {
                if (edge.getCycle() != null)  {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Comparator for topologically sorting entries in the node info map.
     */
    private static class NodeInfoComparator
        implements Comparator<Map.Entry<Object,NodeInfo>> {

        private final Comparator<Object> _subComp;

        public NodeInfoComparator(Comparator<Object> subComp) {
            _subComp = subComp;
        }

        public int compare(Map.Entry<Object,NodeInfo> e1, Map.Entry<Object,NodeInfo> e2) {
            NodeInfo n1 = e1.getValue();
            NodeInfo n2 = e2.getValue();

            // sort by finished order
            int ret = n1.finished - n2.finished;
            if (ret == 0 && _subComp != null)
                ret = _subComp.compare(e1.getKey(), e2.getKey());
            return ret;
        }
    }

    /**
     *	List of node-to-nodeinfo entries that exposes just the nodes.
     */
    private static class NodeList
        extends AbstractList<Object> {

        private final Map.Entry<Object, NodeInfo>[] _entries;

        public NodeList(Map.Entry<Object, NodeInfo>[] entries) {
            _entries = entries;
        }

        public Object get(int idx) {
            return _entries[idx].getKey();
        }

        public int size() {
            return _entries.length;
		}
	}
}
