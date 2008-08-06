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

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * @version $Rev$ $Date$
 */
public class References {

    public static interface Visitor<T> {
        String getName(T t);

        Set<String> getReferences(T t);
    }

    /**
     * Items will be sorted from least amount of references to most amount of references.
     * Original order will be maintained only within those bounds.
     *
     * If one or more circular references are detected a CircularReferenceException will
     * be thrown containing lists of all circuits sorted from lowest to highest.
     *
     * @param objects
     * @param visitor
     * @return
     */
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
                ref.refernceCount++;
            }

        }

        // find all initial leaf nodes (and islands)
        List<Node> sortedNodes = new ArrayList<Node>(nodes.size());
        LinkedList<Node> leafNodes = new LinkedList<Node>();
        for (Node n : nodes.values()) {
            if (n.refernceCount == 0) {
                // if the node is totally isolated (no in or out refs),
                // move it directly to the finished list, so they are first

                if (n.references.size() == 0) {

                    sortedNodes.add(n);


                } else {


                    leafNodes.add(n);

                }
            }
        }

        // pluck the leaves until there are no leaves remaining
        while (!leafNodes.isEmpty()) {
            Node node = leafNodes.removeFirst();
            sortedNodes.add(node);
            for (Node ref : node.references) {
                ref.refernceCount--;
                if (ref.refernceCount == 0) {
                    leafNodes.add(ref);
                }
            }
        }

        // There are no more leaves so if there are there still
        // unprocessed nodes in the graph, we have one or more curcuits
        if (sortedNodes.size() != nodes.size()) {

            Set<Circuit> circuits = new LinkedHashSet<Circuit>();

            for (Node node : nodes.values()) {
                findCircuits(circuits, node, new java.util.Stack<Node>());
            }

            ArrayList<Circuit> list = new ArrayList<Circuit>(circuits);
            Collections.sort(list);

            List<List> all = new ArrayList<List>();
            for (Circuit circuit : list) {
                all.add((List) unwrap(circuit.nodes));
            }

            throw new CircularReferencesException(all);
        }

        List<T> sortedObjects = unwrap(sortedNodes);
        Collections.reverse(sortedObjects);
        return sortedObjects;
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

        for (Node reference : node.references) {
            findCircuits(circuits, reference, stack);
        }

        stack.pop();
    }

    private static class Node implements Comparable<Node> {
        private final String name;
        private Object object;
        private final List<Node> references = new ArrayList<Node>();
        private int refernceCount;

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
            return "Node("+ name +" : "+ Join.join(",", unwrap(references))+")";
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
