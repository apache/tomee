/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package jpa.tools.swing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A* Algorithm to find rectilinear path through a {@link Maze}.
 * 
 * @author Pinaki Poddar
 *
 */
public class PathFinder {
    private final Maze _maze;
    
    public PathFinder(Maze maze) {
        _maze = maze;
    }
    
    /**
     * A* algorithm to find a path through a maze.
     * The algorithm follows these steps
     *   <LI> Add the starting square (or node) to the open list.
     *   <LI> Repeat the following:
     *   <LI> Current node is the lowest cost square on the open list
     *   <LI> Move current node to the closed list.
     *   <LI> For each of adjacent neighbor n to this current square
     *   <LI> If n is not {@link Maze#isReachable(int, int) reachable} or if n is on the closed list, ignore. 
     *        Otherwise do the following.
     *   <LI> If n is not on the open list, add it to the open list. Make the current square 
     *        the parent of n. Record the cost of n. 
     *   <LI> If n is on the open list already, replace if this path to n is lower cost. 
     *   until the target node is added to the closed list, or fail to find the target square
     *   i.e. the open list is empty.   
     *
     * @param x1 the x-coordinate of the starting point
     * @param y1 the y-coordinate of the starting point
     * @param x2 the x-coordinate of the target point
     * @param y2 the y-coordinate of the target point
     * @return a array of points in the form of x1,y1, x2,y2, ....
     */
    public List<Point> findPath(int x1, int y1, int x2, int y2) {
        Node source = new Node(null, x1, y1);
        Node target = new Node(null, x2, y2);
        int maxCost = distance(source, target)*2;
        LinkedList<Node> openList = new LinkedList<Node>();
        List<Node> closedList = new ArrayList<Node>();
        openList.add(source);
        do {
            Node current = openList.remove(0);
            closedList.add(current);
            if (current.f < maxCost) {
                exploreNeighbours(current, target, openList, closedList);
            }
        } while (!openList.isEmpty() && findMatchingNode(x2, y2, closedList) == null);
        target = findMatchingNode(x2, y2, closedList);
        if (target == null) 
            return traceBackPath(closedList.get(closedList.size()-1));
        return traceBackPath(target);
    }
    
    private void exploreNeighbours(Node current, Node target, List<Node> openList, List<Node> closedList) {
        insertNeighbour(current, current.x+1, current.y, target, openList, closedList);
        insertNeighbour(current, current.x-1, current.y, target, openList, closedList);
        insertNeighbour(current, current.x,   current.y+1, target, openList, closedList);
        insertNeighbour(current, current.x,   current.y-1, target, openList, closedList);
        Collections.sort(openList);
    }
    
    private Node insertNeighbour(Node n, int x, int y, Node target, List<Node> openList, List<Node> closedList) {
        if (distance(x,y,target) != 0) {
            if (!_maze.isReachable(x, y) || findMatchingNode(x, y, closedList) != null) {
                return null;
            }
        }
        Node m = findMatchingNode(x, y, openList);
        if (m == null) {
            m = new Node(n,x,y);
            m.g = n.g + 1;
            m.h = distance(target, m);
            m.f = m.g + m.h;
            openList.add(m);
        } else if (m.g > n.g+1){
            m.parent = n;
            m.g = n.g + 1;
            m.f = m.g + m.h;
        }
        return m;
    }
    
    private Node findMatchingNode(int x, int y, List<Node> list) {
        for (Node n : list) {
            if (n.x == x && n.y == y)
                return n;
        }
        return null;
    }
    
    int distance(Node n, Node m) {
        return Math.abs(n.x - m.x) + Math.abs(n.y - m.y);
    }
    int distance(int x, int y, Node m) {
        return Math.abs(x - m.x) + Math.abs(y - m.y);
    }
    
    List<Point> traceBackPath(Node target) {
        LinkedList<Point> path = new LinkedList<Point>();
        path.add(new Point(target.x, target.y));       
        Node next = target.parent;
        while (next != null) {
            path.add(0,new Point(next.x, next.y));
            next = next.parent;
        }
        return straighten(path);
    }
    
    List<Point> straighten(List<Point> path) {
        if (path.size() < 3)
            return path;
        List<Point> mids = new ArrayList<Point>();
        Point prev = path.get(0);
        Point mid  = path.get(1);
        for (int i = 2; i < path.size(); i++) {
            Point next = path.get(i);
            if ((mid.x == prev.x && mid.x == next.x) || (mid.y == prev.y && mid.y == next.y)) {
                mids.add(mid);
            }
            prev = mid;
            mid = next;
        }
        path.removeAll(mids);
        return path;
    }
    
    private static class Node implements Comparable<Node> {
        int f,g,h;
        int x; int y;
        Node parent;
        
        public Node(Node p, int x, int y) {
            parent = p;
            this.x = x;
            this.y = y;
        }
        
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Node) {
                Node that = (Node)o;
                return x == that.x && y == that.y;
            }
            return false;
        }

        @Override
        public int compareTo(Node o) {
            if (f == o.f) return 0;
            return f > o.f ? 1 : -1;
        }
        
        public String toString() {
            return "(" + x + "," + y + ":" + g + ")";
        }
    }
}
