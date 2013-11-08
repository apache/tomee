/**
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
package org.apache.openjpa.persistence.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import serp.bytecode.Annotations;
import serp.bytecode.BCClass;
import serp.bytecode.BCMethod;
import serp.bytecode.Project;

/**
 * List class names that match specific selection criteria based on inheritance,
 * implemented interface or annotations. The classes are scanned starting from
 * a root directory or a single file. Uses serp bytecode library for reading the
 * bytecode. The classes are not loaded in Java Virtual Machine and hence
 * dependent classes need not be in the classpath.
 * 
 * @author Pinaki Poddar
 * 
 */
public class ClassSelector {
	private List<String> _supers = new ArrayList<String>();
	private List<String> _interfaces = new ArrayList<String>();
	private List<String> _annotations = new ArrayList<String>();

	/**
	 * Prints the class names that satisfy the following criteria
	 *   extends org.apache.openjpa.persistence.test.SingleEMFTestCase or 
	 *           junit.framework.TestCase
	 *   and annotated with org.apache.openjpa.persistence.test.AllowFailure
	 *   
     * @param args the root directory of the class files to be scanned. If no
	 * argument is given then assumes the current directory.
	 * 
	 */
	public static void main(String[] args) throws Exception {
		String dir = (args.length == 0) ? System.getProperty("user.dir")
				: args[0];
        ClassSelector reader = new ClassSelector()
            .addSuper("org.apache.openjpa.persistence.test.SingleEMTestCase")
            .addSuper("org.apache.openjpa.persistence.test.SingleEMFTestCase")
            .addSuper("org.apache.openjpa.persistence.kernel.BaseKernelTest")
            .addSuper("org.apache.openjpa.persistence.query.BaseQueryTest")
            .addSuper("org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest")
            .addSuper(
                "org.apache.openjpa.persistence.common.utils.AbstractTestCase")
            .addAnnotation("org.apache.openjpa.persistence.test.AllowFailure");
		List<String> names = reader.list(new File(dir), true);
		String spec = reader.getSpecification();
		System.err.println("Found " + names.size() + " classes under " 
				+ dir + (spec.length() > 0 ? " that" : ""));
		System.err.println(spec);
		for (String name : names)
			System.err.println(name);
	}
	
	/**
	 * List all the class names that match the selection.
	 * 
	 * @param file a root file or directory
	 * @param recursive if true scans all directory recursively
	 * @return list of class names that match the selection. 
	 */
	public List<String> list(File file, boolean recursive) {
		List<String> names = new ArrayList<String>();
		list(file, recursive, names);
		return names;
	}
	
	private void list(File file, boolean recursive, List<String> names) {
		if (file.isDirectory()) {
			if (recursive) {
                String[] children = file.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
						return name.endsWith(".class");
					}
				});
				for (String name : children)
                    list(new File(file, name), recursive, names);
				String[] dirs = file.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return new File(dir, name).isDirectory();
					}
				});
				for (String name : dirs)
                    list(new File(file, name), recursive, names);
			}
		} else if (file.getName().endsWith(".class")) {
			String cls = select(file);
			if (cls != null)
				names.add(cls);
		}
	}

	/**
	 * Adds fully-qualified name of a super class for selection.
	 */
	public ClassSelector addSuper(String s) {
		_supers.add(s);
		return this;
	}

	/**
	 * Adds fully-qualified name of an interface for selection.
	 */
	public ClassSelector addInterface(String s) {
		_interfaces.add(s);
		return this;
	}

	/**
	 * Adds fully-qualified name of an annotation for selection.
	 */
	public ClassSelector addAnnotation(String s) {
		_annotations.add(s);
		return this;
	}

	private String select(File file) {
		try {
			BCClass bcls = new Project().loadClass(file);
			if (applyInheritanceFilter(bcls) 
			 && applyInterfaceFilter(bcls)
			 && applyAnnotationFilter(bcls))
				return bcls.getName();
		} catch (Exception e) {
            System.err.println("Error reading " + file.getAbsolutePath()
					+ " : " + e);
		}
		return null;
	}

	/**
	 * Affirms if super class of the given class matches any of the
	 * selection filter names. If no super class name has been set for
	 * selection then return true.
	 * 
	 * @see #addSuper(String)
	 */
	private boolean applyInheritanceFilter(BCClass bcls) {
		if (_supers.isEmpty())
			return true;
		String superc = bcls.getSuperclassName();
		return _supers.contains(superc);
	}

	/**
	 * Affirms if interfaces of the given class match any of the
	 * selection filter names. If no interface name has been set for
	 * selection then return true.
	 * 
	 * @see #addInterface(String)
	 */
	private boolean applyInterfaceFilter(BCClass bcls) {
		if (_interfaces.isEmpty())
			return true;
		String[] ifaces = bcls.getInterfaceNames();
		if (ifaces == null || ifaces.length == 0)
			return false;
		for (String iface : ifaces)
			if (_interfaces.contains(iface))
				return true;
		return false;
	}

	/**
     * Affirms if annotations of the given class or its methods match any of the
	 * selection filter names. If no annotation name has been set for
	 * selection then return true.
	 * 
	 * @see #addAnnotation(String)
	 */
	private boolean applyAnnotationFilter(BCClass bcls) {
		if (_annotations.isEmpty())
			return true;
		Annotations annos = bcls.getDeclaredRuntimeAnnotations(false);
		if (hasAnnotation(annos))
			return true;
		BCMethod[] methods = bcls.getDeclaredMethods();
		for (BCMethod m : methods) {
			annos = m.getDeclaredRuntimeAnnotations(false);
			if (hasAnnotation(annos))
				return true;
		}
		return false;
	}

	private boolean hasAnnotation(Annotations annos) {
		if (annos == null)
			return false;
		for (String anno : _annotations)
			if (annos.getAnnotation(anno) != null)
				return true;
		return false;
	}
	
	/**
	 * Gets a printable description of the currently set selection criteria.
	 */
	public String getSpecification() {
		StringBuffer tmp = new StringBuffer();
		String and = "";
		if (!_supers.isEmpty()) {
			tmp.append("\textends ");
			and = "and ";
			for (int i=0; i<_supers.size(); i++)
                tmp.append(_supers.get(i)).append(
                    (i != _supers.size()-1 ? "\r\n\t     or " : "\r\n"));
		}
		if (!_interfaces.isEmpty()) {
			tmp.append("\t" + and + "implements ");
			and = "and ";
			for (int i=0; i<_interfaces.size(); i++)
                tmp.append(_interfaces.get(i)).append(
                    (i != _interfaces.size()-1 ? "\r\n\t        or " : "\r\n"));
		}
		if (!_annotations.isEmpty()) {
			tmp.append("\t" + and + "annotatated with ");
			for (int i=0; i<_annotations.size(); i++)
				tmp.append(_annotations.get(i))
                    .append((i != _annotations.size()-1 ? " or " : "\r\n"));
		}
		return tmp.toString();
	}
}
