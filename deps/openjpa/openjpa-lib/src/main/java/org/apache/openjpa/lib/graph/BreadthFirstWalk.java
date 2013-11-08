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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Performs a breadth-first walk of a given {@link Graph},
 * notifying visitors as it sees each node.  See the BFS algorithm
 * in the book 'Introduction to Algorithms' by Cormen, Leiserson, and
 * Rivest.</p>
 * <p/>
 * <p>Each {@link GraphVisitor} will be notified when a node
 * is colored black (nodeVisited), edge seen (edgeVisited),
 * and a node is seen for the first time, i.e. colored gray (nodeSeen).</p>
 *
 * @author Steve Kim
 * @since 1.0.0
 * @nojavadoc
 */
public class BreadthFirstWalk {

    private final Graph _graph;
    private final Set<GraphVisitor> _visitors = new HashSet<GraphVisitor>();
    private final List<Object> _queue = new LinkedList<Object>();
    private final Map<Object, NodeInfo> _nodeInfo = new HashMap<Object, NodeInfo>();

    public BreadthFirstWalk(Graph graph) {
        _graph = graph;
    }

    /**
     * Begins the breadth first traversal.
     */
    public void walk() {
        _queue.clear();
        _nodeInfo.clear();

        Collection<Object> nodes = _graph.getNodes();
        for (Object node: nodes)
            _nodeInfo.put(node, new NodeInfo());

        NodeInfo info;
        for (Object node : nodes){
            info = _nodeInfo.get(node);
            if (info.color == NodeInfo.COLOR_WHITE)
                enqueue(node, info);
            processQueue();
        }
    }

    /**
     * Process the queue to see what data needs to be obtained.
     */
    private void processQueue() {
        while (_queue.size() > 0) {
        	Object node = _queue.remove(0);
            NodeInfo info = _nodeInfo.get(node);
            visit(node, info);

            Collection<Edge> edges = _graph.getEdgesFrom(node);
            for (Edge edge : edges) {
                edgeVisited(edge);
                Object other = edge.getOther(node);
                NodeInfo otherInfo = _nodeInfo.get(other);
                if (otherInfo.color == NodeInfo.COLOR_WHITE)
                    enqueue(other, otherInfo);
            }
        }
    }

    /**
     * Push the given node onto the queue to be processed.
     * Notify visitors.
     */
    protected void enqueue(Object node, NodeInfo info) {
        _queue.add(node);
        info.color = NodeInfo.COLOR_GRAY;
        for (GraphVisitor visitor : _visitors)
        	visitor.nodeSeen(node);
    }

    /**
     * Visit the node.  Mark the node black and notify visitors.
     */
    protected void visit(Object node, NodeInfo info) {
        info.color = NodeInfo.COLOR_BLACK;
        for (GraphVisitor visitor : _visitors)
        	visitor.nodeVisited(node);
    }

    /**
     * An edge is seen.  Notify visitors.
     */
    protected void edgeVisited(Edge edge) {
    	for (GraphVisitor visitor : _visitors)
    		visitor.edgeVisited(edge);
    }

    /**
     * add a {@link GraphVisitor} to be notified during breadth first search.
     */
    public void addGraphVisitor(GraphVisitor visitor) {
        _visitors.add(visitor);
    }

    /**
     * remove a given {@link GraphVisitor} from the listener set.
     */
    public void removeGraphVisitor(GraphVisitor visitor) {
        _visitors.remove(visitor);
    }
}
