/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class References {

    public static interface Visitor<T> {
        String getName(T t);

        Set<String> getReferences(T t);
    }

    public static <T> List<T> sort(List<T> objects, Visitor<T> visitor) {

        final Map<String, Node> nodes = new LinkedHashMap<String, Node>();

        // Create nodes
        for (T obj : objects) {
            String name = visitor.getName(obj);
            Node node = new Node(name, obj);
            nodes.put(name, node);
        }

        // Link nodes
        for (Node node : nodes.values()) {
            for (String name : visitor.getReferences((T) node.object)) {
                Node ref = nodes.get(name);
                if (ref == null) throw new IllegalArgumentException("No such object in list: "+name);
                node.references.add(ref);
                node.initialReferences.add(ref);
            }

        }

        boolean circuitFounded = false;
        for (Node node : nodes.values()) {
            Set<Node> visitedNodes = new HashSet<Node>();
            if (!normalizeNodeReferences(node, node, visitedNodes)) {
                circuitFounded = true;
                break;
            }
            node.references.addAll(visitedNodes);
        }

        //detect circus
        if (circuitFounded) {
            Set<Circuit> circuits = new LinkedHashSet<Circuit>();

            for (Node node : nodes.values()) {
                findCircuits(circuits, node, new java.util.Stack<Node>());
            }

            ArrayList<Circuit> list = new ArrayList<Circuit>(circuits);
            Collections.sort(list);

            List<List> all = new ArrayList<List>();
            for (Circuit circuit : list) {
                all.add(unwrap(circuit.nodes));
            }

            throw new CircularReferencesException(all);
        }

        LinkedList<Node> sortedNodes = new LinkedList<Node>();
        sortedNodes.addAll(nodes.values());

        for (Node node : nodes.values()) {
            for (Node reference : node.references) {
                final int i = sortedNodes.indexOf(node);
                swap(node.name, reference.name, sortedNodes);
            }
        }
        return unwrap(sortedNodes);
    }

    private static boolean normalizeNodeReferences(Node rootNode, Node node, Set<Node> referenceNodes) {
        if (node.references.contains(rootNode)) {
            return false;
        }
        for (Node reference : node.references) {
            if (!referenceNodes.add(reference)) {
                //this reference node has been visited in the past
                continue;
            }
            if (!normalizeNodeReferences(rootNode, reference, referenceNodes)) {
                return false;
            }
        }
        return true;
    }

    private static void swap(String shouldAfterNodeName, String shouldBeforeNodeName, LinkedList<Node> nodes) {
        int shouldAfterIndex = -1;
        int shouldBeforeIndex = -1;
        int index = 0;
        for (Node node : nodes) {
            if (shouldAfterIndex == -1 || shouldBeforeIndex == -1) {
                if (shouldAfterNodeName.equals(node.name)) {
                    shouldAfterIndex = index;
                } else if (shouldBeforeNodeName.equals(node.name)) {
                    shouldBeforeIndex = index;
                }
                index++;
            } else {
                break;
            }
        }
        System.out.println(shouldAfterNodeName + "=" + shouldAfterIndex + "\t" + shouldBeforeNodeName + "=" + shouldBeforeIndex);
        if (shouldAfterIndex < shouldBeforeIndex) {
            Node node = nodes.remove(shouldAfterIndex);
            nodes.add(shouldBeforeIndex, node);
        }
    }

    private static <T> List<T> unwrap(List<Node> nodes) {
        ArrayList<T> referees = new ArrayList<T>(nodes.size());
        for (Node node : nodes) {
            referees.add((T) node.object);
        }
        return referees;
    }

    private static void findCircuits(Set<Circuit> circuits, Node node, java.util.Stack<Node> stack) {
        if (stack.contains(node)) {
            int fromIndex = stack.indexOf(node);
            int toIndex = stack.size();
            ArrayList<Node> circularity = new ArrayList<Node>(stack.subList(fromIndex, toIndex));

            // add ending node to list so a full circuit is shown
            circularity.add(node);

            Circuit circuit = new Circuit(circularity);

            circuits.add(circuit);

            return;
        }

        stack.push(node);

        for (Node reference : node.initialReferences) {
            findCircuits(circuits, reference, stack);
        }

        stack.pop();
    }

    private static class Node implements Comparable<Node> {
        private final String name;
        private Object object;
        private final List<Node> initialReferences = new ArrayList<Node>();
        private final Set<Node> references = new HashSet<Node>();
        //private int refernceCount;

        public Node(String name, Object object) {
            this.name = name;
            this.object = object;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Node node = (Node) o;

            return name.equals(node.name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public int compareTo(Node o) {
            return this.name.compareTo(o.name);
        }

        public String toString() {
            return name;
            //return "Node("+ name +" : "+ Join.join(",", unwrap(initialReferences))+")";
        }
    }

    private static class Circuit implements Comparable<Circuit> {
        private final List<Node> nodes;
        private final List<Node> atomic;

        public Circuit(List<Node> nodes) {
            this.nodes = nodes;
            atomic = new ArrayList<Node>(nodes);
            atomic.remove(atomic.size()-1);
            Collections.sort(atomic);
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Circuit circuit = (Circuit) o;

            if (!atomic.equals(circuit.atomic)) return false;

            return true;
        }

        public int hashCode() {
            return atomic.hashCode();
        }

        public int compareTo(Circuit o) {
            int i = atomic.size() - o.atomic.size();
            if (i != 0) return i;

            Iterator<Node> iterA = atomic.listIterator();
            Iterator<Node> iterB = o.atomic.listIterator();
            while (iterA.hasNext() && iterB.hasNext()) {
                Node a = iterA.next();
                Node b = iterB.next();
                i = a.compareTo(b);
                if (i != 0) return i;
            }

            return 0;
        }

        public String toString() {
            return "Circuit(" + Join.join(",", unwrap(nodes)) + ")";
        }
    }

}
