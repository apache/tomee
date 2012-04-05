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

import org.apache.openejb.loader.IO;
import org.hsqldb.lib.Collection;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class Debug {

    public static String printStackTrace(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(baos));
        return new String(baos.toByteArray());
    }

    public static Map<String,Object> contextToMap(Context context) throws NamingException {
        Map<String, Object> map = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        contextToMap(context, "", map);
        return map;
    }

    public static void contextToMap(Context context, String baseName, Map<String,Object> results) throws NamingException {
        NamingEnumeration<Binding> namingEnumeration = context.listBindings("");
        while (namingEnumeration.hasMoreElements()) {
            Binding binding = namingEnumeration.nextElement();
            String name = binding.getName();
            String fullName = baseName + name;
            Object object = binding.getObject();
            results.put(fullName, object);
            if (object instanceof Context) {
                contextToMap((Context) object, fullName + "/", results);
            }
        }
    }

    public static Map<String,Object> printContext(Context context) throws NamingException {
        return printContext(context, System.out);
    }

    public static Map<String,Object> printContext(Context context, PrintStream out) throws NamingException {
        Map<String, Object> map = contextToMap(context);
        for (Entry<String, Object> entry : map.entrySet()) {
            out.println(entry.getKey() + "=" + entry.getValue().getClass().getName());
        }
        return map;
    }

    public static Map<String,Object> printContextValues(Context context) throws NamingException {
        return printContextValues(context, System.out);
    }

    public static Map<String,Object> printContextValues(Context context, PrintStream out) throws NamingException {
        Map<String, Object> map = contextToMap(context);
        for (Entry<String, Object> entry : map.entrySet()) {
            out.println(entry.getKey() + "=" + entry.getValue());
        }
        return map;
    }

    public static List<Field> getFields(Class clazz){
        if (clazz == null) return Collections.EMPTY_LIST;

        List<Field> fields = new ArrayList<Field>();

        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        fields.addAll(getFields(clazz.getSuperclass()));

        return fields;
    }


    public static class Trace {

        private static final Trace trace = new Trace();

        private final Map<String, Node> elements = new LinkedHashMap<String, Node>();

        private final List<Event> events = new ArrayList<Event>();

        private static class Event {
            private final long time = System.currentTimeMillis();
            private final List<StackTraceElement> elements;

            private Event(List<StackTraceElement> elements) {
                this.elements = Collections.unmodifiableList(elements);
            }

            @Override
            public String toString() {
                return "<li>" +
                        "time=" + time +
                        ", elements=" + elements +
                        "</li>";
            }
        }

        public static void record() {
            try {
                final File file = new File("/tmp/trace.html");
                final OutputStream write = IO.write(file);

                report(write);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public static void report(OutputStream write) {
            final PrintStream stream = new PrintStream(write);
            report(stream);
            stream.close();
        }

        public static void report(PrintStream stream) {
            trace.print(stream);

            stream.print("<br/><ul>");
            for (Trace.Event event : trace.events) {
                stream.println(event);
            }
            stream.print("</ul>");
        }

        public static void mark() {
            Throwable throwable = new Exception().fillInStackTrace();
            List<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>(Arrays.asList(throwable.getStackTrace()));
            Collections.reverse(stackTraceElements);

            Iterator<StackTraceElement> iterator = stackTraceElements.iterator();
            while (iterator.hasNext()) {
                StackTraceElement element = iterator.next();
                if (!element.getClassName().startsWith("org.apache")) iterator.remove();
                if (element.getClassName().endsWith("Debug") && element.getMethodName().equals("mark")) iterator.remove();
            }

            trace.link(stackTraceElements);
        }

        public void print(PrintStream out) {
            Set<Node> seen = new HashSet<Node>();

            for (Node node : elements.values()) {
                if (node.parent == null) {
                    out.println("<ul>");
                    print(seen, out, node, "- ");
                    out.println("</ul>");
                }
            }
        }

        private void print(Set<Node> seen, PrintStream out, Node node, String s) {
            if (!seen.add(node)) return;

            out.print("<li>\n");

            StackTraceElement e = node.getElement();
            out.printf("<b>%s</b> <i>%s <font color='gray'>(%s)</font></i>\n", escape(e.getMethodName()), reverse(e.getClassName()), e.getLineNumber());

            if (node.children.size()> 0) {
                out.println("<ul>");
                for (Node child : node.children) {
                    print(seen, out, child, s);
                }
                out.println("</ul>");
            }

            out.print("</li>\n");
        }

        private String escape(String methodName) {
            return methodName.replace("<","&lt;").replace(">","&gt;");
        }

        private void printTxt(Set<Node> seen, PrintStream out, Node node, String s) {
            if (!seen.add(node)) return;

            out.print(s);
            StackTraceElement e = node.getElement();
            out.printf("**%s** *%s* (%s)\n", e.getMethodName(), reverse(e.getClassName()), e.getLineNumber());
            s = "  " + s;
            for (Node child : node.children) {
                print(seen, out, child, s);
            }
        }

        private String reverse2(String className) {
            List<String> list = Arrays.asList(className.split("\\."));
            Collections.reverse(list);

            String string = Join.join(".", list);
            string = string.replaceAll("(.*?)(\\..*)","$1<font color=\"gray\">$2</font>");
            return string;
        }

        private String reverse(String string) {
            string = string.replaceAll("(.*)\\.([^.]+)","$2 <font color=\"gray\">$1</font>");
            return string;
        }

        public static class Node {
            private Node parent;
            private final String trace;
            private final StackTraceElement element;

            private final List<Node> children = new ArrayList<Node>();


            public Node(StackTraceElement element) {
                this.element = element;
                this.trace = element.toString();
            }

            public String getTrace() {
                return trace;
            }

            public StackTraceElement getElement() {
                return element;
            }

            public Node addChild(Node node) {
                node.parent = this;
                children.add(node);
                return node;
            }
        }

        public static void reset() {
            trace.events.clear();
            trace.elements.clear();
        }

        public void link(List<StackTraceElement> elements) {
            events.add(new Event(elements));
            Iterator<StackTraceElement> iterator = elements.iterator();
            if (!iterator.hasNext()) return;

            Node parent = get(iterator.next());

            while (iterator.hasNext()) {

                parent = parent.addChild(get(iterator.next()));
            }
        }

        private Node get(StackTraceElement element) {
            String key = element.toString();
            Node node = elements.get(key);
            if (node == null) {
                node = new Node(element);
                elements.put(key, node);
            }
            return node;
        }
    }
}
