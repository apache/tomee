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

/**
 * <p>A helper interface that allows third parties to be notified of
 * graph events during graph traversals</p>
 *
 * @author Steve Kim
 * @since 1.0.0
 * @nojavadoc
 */
public interface GraphVisitor {

    /**
     * May not be called.  The meaning of this method is dependent
     * on the traversal being used.  See each appropriate graph
     * walker for details.
     */
    public void nodeSeen(Object node);

    /**
     * will only be called once per node
     */
    public void nodeVisited(Object node);

    /**
     * may visit the node twice (both sides)
     */
    public void edgeVisited(Edge edge);
}
