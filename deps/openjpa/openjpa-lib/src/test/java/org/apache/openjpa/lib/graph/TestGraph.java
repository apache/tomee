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
import java.util.Iterator;

import org.apache.openjpa.lib.test.AbstractTestCase;

/**
 * <p>Tests the {@link Graph} type, and in so doing implicitly tests the
 * {@link Edge} as well.</p>
 *
 * @author Abe White
 */
public class TestGraph
    extends AbstractTestCase {

    private Graph _graph = new Graph();
    private Object _node1 = new Object();
    private Object _node2 = new Object();
    private Object _node3 = new Object();
    private Edge _edge1 = new Edge(_node1, _node2, true);
    private Edge _edge2 = new Edge(_node2, _node3, true);
    private Edge _edge3 = new Edge(_node1, _node3, false);
    private Edge _edge4 = new Edge(_node2, _node2, false);

    public void setUp() {
        _graph.addNode(_node1);
        _graph.addNode(_node2);
        _graph.addNode(_node3);
        _graph.addEdge(_edge1);
        _graph.addEdge(_edge2);
        _graph.addEdge(_edge3);
        _graph.addEdge(_edge4);
    }

    /**
     * Tests adding and retrieving nodes and edges.
     */
    public void testAddRetrieve() {
        assertEquals(3, _graph.getNodes().size());
        assertEquals(4, _graph.getEdges().size());

        Collection edges = _graph.getEdgesFrom(_node1);
        assertEquals(2, edges.size());
        Iterator itr = edges.iterator();
        Edge edge0 = (Edge) itr.next();
        Edge edge1 = (Edge) itr.next();
        assertTrue((edge0 == _edge1 && edge1 == _edge3)
            || (edge0 == _edge3 && edge1 == _edge1));

        edges = _graph.getEdgesTo(_node1);
        assertEquals(1, edges.size());
        assertEquals(_edge3, edges.iterator().next());

        edges = _graph.getEdges(_node1, _node3);
        assertEquals(1, edges.size());
        assertEquals(_edge3, edges.iterator().next());
        edges = _graph.getEdges(_node3, _node1);
        assertEquals(1, edges.size());
        assertEquals(_edge3, edges.iterator().next());

        edges = _graph.getEdgesFrom(_node2);
        assertEquals(2, edges.size());
        itr = edges.iterator();
        edge0 = (Edge) itr.next();
        edge1 = (Edge) itr.next();
        assertTrue((edge0 == _edge2 && edge1 == _edge4)
            || (edge0 == _edge4 && edge1 == _edge2));

        edges = _graph.getEdgesTo(_node2);
        assertEquals(2, edges.size());
        itr = edges.iterator();
        edge0 = (Edge) itr.next();
        edge1 = (Edge) itr.next();
        assertTrue((edge0 == _edge1 && edge1 == _edge4)
            || (edge0 == _edge4 && edge1 == _edge1));

        edges = _graph.getEdges(_node2, _node2);
        assertEquals(1, edges.size());
        assertEquals(_edge4, edges.iterator().next());

        edges = _graph.getEdgesFrom(_node3);
        assertEquals(1, edges.size());
        assertEquals(_edge3, edges.iterator().next());
    }

    /**
     * Test removing edges.
     */
    public void testRemoveEdges() {
        assertTrue(_graph.removeEdge(_edge2));
        Collection edges = _graph.getEdgesFrom(_node2);
        assertEquals(1, edges.size());
        assertEquals(_edge4, edges.iterator().next());

        assertTrue(_graph.removeEdge(_edge3));
        edges = _graph.getEdgesFrom(_node1);
        assertEquals(1, edges.size());
        assertEquals(_edge1, edges.iterator().next());
        edges = _graph.getEdgesTo(_node1);
        assertEquals(0, edges.size());
        edges = _graph.getEdgesTo(_node3);
        assertEquals(0, edges.size());
        edges = _graph.getEdgesFrom(_node3);
        assertEquals(0, edges.size());
    }

    /**
     * Test removing nodes.
     */
    public void testRemoveNodes() {
        assertTrue(_graph.removeNode(_node3));
        Collection edges = _graph.getEdges();
        assertEquals(2, edges.size());
        Iterator itr = edges.iterator();
        Edge edge0 = (Edge) itr.next();
        Edge edge1 = (Edge) itr.next();
        assertTrue((edge0 == _edge1 && edge1 == _edge4)
            || (edge0 == _edge4 && edge1 == _edge1));
        edges = _graph.getEdgesFrom(_node1);
        assertEquals(1, edges.size());
        assertEquals(_edge1, edges.iterator().next());
        edges = _graph.getEdgesTo(_node1);
        assertEquals(0, edges.size());
    }

    public static void main(String[] args) {
        main(TestGraph.class);
	}
}
