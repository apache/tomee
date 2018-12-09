/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @version $Rev$ $Date$
 */
public class References {

    public interface Visitor<T> {
        String getName(T t);

        Set<String> getReferences(T t);
    }

    public static <T> List<T> sort(final List<T> objects, final Visitor<T> visitor) {

        if (objects.size() <= 1) {
            return objects;
        }

        final Map<String, Node> nodes = new LinkedHashMap<>();

        // Create nodes
        for (final T obj : objects) {
            final String name = visitor.getName(obj);
            final Node node = new Node(name, obj);
            nodes.put(name, node);
        }

        // Link nodes
        for (final Node node : nodes.values()) {
            for (final String name : visitor.getReferences((T) node.object)) {
                final Node ref = nodes.get(name);
                if (ref == null) {
                    throw new IllegalArgumentException("No such object in list: " + name);
                }
                node.references.add(ref);
                node.initialReferences.add(ref);
            }
        }
        boolean circuitFounded = false;
        for (final Node node : nodes.values()) {
            final Set<Node> visitedNodes = new HashSet<>();
            if (!normalizeNodeReferences(node, node, visitedNodes)) {
                circuitFounded = true;
                break;
            }
            node.references.addAll(visitedNodes);
        }

        //detect circus
        if (circuitFounded) {
            final Set<Circuit> circuits = new LinkedHashSet<>();

            for (final Node node : nodes.values()) {
                findCircuits(circuits, node, new Stack<>());
            }

            final ArrayList<Circuit> list = new ArrayList<>(circuits);
            Collections.sort(list);

            final List<List> all = new ArrayList<>();
            for (final Circuit circuit : list) {
                all.add(unwrap(circuit.nodes));
            }

            throw new CircularReferencesException(all);
        }

        //Build Double Link Node List
        final Node rootNode = new Node(null, null);
        rootNode.previous = rootNode;
        rootNode.next = nodes.values().iterator().next();

        for (final Node node : nodes.values()) {
            node.previous = rootNode.previous;
            rootNode.previous.next = node;
            node.next = rootNode;
            rootNode.previous = node;
        }

        for (final Node node : nodes.values()) {
            for (final Node reference : node.references) {
                swap(node, reference, rootNode);
            }
        }

        final List sortedList = new ArrayList(nodes.size());
        Node currentNode = rootNode.next;
        while (currentNode != rootNode) {
            sortedList.add(currentNode.object);
            currentNode = currentNode.next;
        }
        return sortedList;
    }

    private static boolean normalizeNodeReferences(final Node rootNode, final Node node, final Set<Node> referenceNodes) {
        if (node.references.contains(rootNode)) {
            return false;
        }
        for (final Node reference : node.references) {
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

    private static void swap(final Node shouldAfterNode, final Node shouldBeforeNode, final Node rootNode) {
        Node currentNode = shouldBeforeNode;
        while (currentNode.next != rootNode) {
            if (currentNode.next == shouldAfterNode) {
                return;
            }
            currentNode = currentNode.next;
        }
        //Remove the shouldAfterNode from list
        shouldAfterNode.previous.next = shouldAfterNode.next;
        shouldAfterNode.next.previous = shouldAfterNode.previous;
        //Insert the node immediately after the shouldBeforeNode
        shouldAfterNode.previous = shouldBeforeNode;
        shouldAfterNode.next = shouldBeforeNode.next;
        shouldBeforeNode.next = shouldAfterNode;
        shouldAfterNode.next.previous = shouldAfterNode;
    }

    private static <T> List<T> unwrap(final List<Node> nodes) {
        final ArrayList<T> referees = new ArrayList<>(nodes.size());
        for (final Node node : nodes) {
            referees.add((T) node.object);
        }
        return referees;
    }

    private static void findCircuits(final Set<Circuit> circuits, final Node node, final Stack<Node> stack) {
        if (stack.contains(node)) {
            final int fromIndex = stack.indexOf(node);
            final int toIndex = stack.size();
            final ArrayList<Node> circularity = new ArrayList<>(stack.subList(fromIndex, toIndex));

            // add ending node to list so a full circuit is shown
            circularity.add(node);

            final Circuit circuit = new Circuit(circularity);

            circuits.add(circuit);

            return;
        }

        stack.push(node);

        for (final Node reference : node.initialReferences) {
            findCircuits(circuits, reference, stack);
        }

        stack.pop();
    }

    private static class Node implements Comparable<Node> {
        private final String name;
        private final Object object;
        private final List<Node> initialReferences = new ArrayList<>();
        private final Set<Node> references = new HashSet<>();
        private Node next;
        private Node previous;

        public Node(final String name, final Object object) {
            this.name = name;
            this.object = object;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Node node = (Node) o;

            return name.equals(node.name);
        }

        public int hashCode() {
            return name.hashCode();
        }

        public int compareTo(final Node o) {
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

        public Circuit(final List<Node> nodes) {
            this.nodes = nodes;
            atomic = new ArrayList<>(nodes);
            atomic.remove(atomic.size() - 1);
            Collections.sort(atomic);
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Circuit circuit = (Circuit) o;

            if (!atomic.equals(circuit.atomic)) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            return atomic.hashCode();
        }

        public int compareTo(final Circuit o) {
            int i = atomic.size() - o.atomic.size();
            if (i != 0) {
                return i;
            }

            final Iterator<Node> iterA = atomic.listIterator();
            final Iterator<Node> iterB = o.atomic.listIterator();
            while (iterA.hasNext() && iterB.hasNext()) {
                final Node a = iterA.next();
                final Node b = iterB.next();
                i = a.compareTo(b);
                if (i != 0) {
                    return i;
                }
            }

            return 0;
        }

        public String toString() {
            return "Circuit(" + Join.join(",", unwrap(nodes)) + ")";
        }
    }

}
