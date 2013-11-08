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
package org.apache.openjpa.persistence.proxy;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.apache.openjpa.persistence.DetachedState;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;
import org.apache.openjpa.persistence.jdbc.OrderColumn;

/**
 * Persistent entity for testing adding/removing elements of collection valued
 * field while in detached state.
 * 
 * Node refers to a list of Nodes as children.
 * 
 * Contains recursive methods to create or modify uniform subtree. Uniform
 * subtree implies that each child at a level L has equal number of
 * grand children at level L+1.
 * 
 * @author Pinaki Poddar
 * 
 */
@Entity
@DetachedState
public class TreeNode implements Serializable {
	@Id
	@GeneratedValue
	private long id;

	private String name;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@ElementJoinColumn(name = "ParentID")
	@OrderColumn(name = "Sequence")
	@ElementDependent
	private List<TreeNode> childern = new ArrayList<TreeNode>();

	@Version
	private int version;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Add a child node at the end of the current list of children.
	 */
	public void addNode(TreeNode node) {
		addNode(node, childern.size());
	}

	/**
     * Insert a child node at the specified position in the list of children.
	 */
	public void addNode(TreeNode node, int position) {
		checkSequenceRange(position);
		childern.add(position, node);
	}

	public boolean removeNode(TreeNode node) {
		return childern.remove(node);
	}

	public TreeNode removeNode(int sequence) {
		checkSequenceRange(sequence);
		return childern.remove(sequence);
	}

	public TreeNode getNode(int sequence) {
		checkSequenceRange(sequence);
		return childern.get(sequence);
	}

	public List<TreeNode> getNodes() {
		return childern;
	}

	public void clearNodes() {
		childern.clear();
	}

	public boolean isLeaf() {
		return childern.isEmpty();
	}

	protected void checkSequenceRange(int sequence)
			throws IllegalArgumentException {
		int size = childern.size();
		if (sequence < 0 || sequence > size)
            throw new IllegalArgumentException("Sequence number is beyond "
					+ "range of 0 to " + size + ".");
	}

	public int getVersion() {
		return version;
	}

	/**
     * Create a uniform subtree below the receiver. Uniform subtree implies that
     * each child at a level L has equal number of grand children at level L+1.
	 * 
	 * @param fanOuts
	 *            array of fan outs for children at every level.
	 */
	public void createTree(int[] fanOuts) {
		if (fanOuts.length == 0)
			return;
		int[] nextFanOuts = new int[fanOuts.length];
        System.arraycopy(fanOuts, 1, nextFanOuts, 0, fanOuts.length - 1);
		for (int j = 0; j < fanOuts[0]; j++) {
			TreeNode child = new TreeNode();
			child.setName(getName() + "." + j);
			addNode(child);
			child.createTree(nextFanOuts);
		}
	}

	/**
	 * Add or remove subtree of the receiver to match the given fanOut.
	 */
	public void modify(int[] fanOuts) {
		if (fanOuts == null || fanOuts.length == 0)
			return;
		int n = fanOuts[0];
		int[] nextFanOuts = new int[fanOuts.length];
        System.arraycopy(fanOuts, 1, nextFanOuts, 0, fanOuts.length - 1);
		List<TreeNode> children = getNodes();
		int diff = children.size() - n;
		if (diff < 0) {
			for (int i = 0; i < -diff; i++) {
				TreeNode newChild = new TreeNode();
				int position = getNodes().size();
				newChild.setName(getName() + "." + position);
				addNode(newChild);
			}
		} else if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				int position = getNodes().size() - 1;
				removeNode(position);
			}
		}
		children = getNodes();
		for (TreeNode child : children) {
			child.modify(nextFanOuts);
		}
	}

	/**
	 * Get the fan outs of the given receiver. Assumes that the subtree is
	 * uniform. Otherwise throws exception.
	 */
	public int[] getFanOuts() {
		return getFanOuts(new int[] {});
	}

	private int[] getFanOuts(int[] list) {
		List<TreeNode> children = getNodes();
		if (children.isEmpty())
			return list;
		int[] fanOuts = new int[children.size()];
		int i = 0;
		for (TreeNode child : children) {
			fanOuts[i++] = child.getNodes().size();
		}
		for (int j = 0; j < fanOuts.length - 1; j++)
			if (fanOuts[j] != fanOuts[j + 1])
                throw new RuntimeException("non-uniform fanouts for children "
						+ " of " + getName());

		int[] newList = new int[list.length + 1];
		System.arraycopy(list, 0, newList, 0, list.length);
		newList[list.length] = children.size();
		return children.get(0).getFanOuts(newList);
	}

	/**
	 * Prints this receiver and its subtree.
	 */
	public void print(PrintStream out) {
		print(2, out);
	}

	private void print(int tab, PrintStream out) {
		for (int i = 0; i < tab; i++)
			out.print(" ");
		out.println(getName());
		for (TreeNode child : getNodes()) {
			child.print(tab + 2, out);
		}
	}

}
