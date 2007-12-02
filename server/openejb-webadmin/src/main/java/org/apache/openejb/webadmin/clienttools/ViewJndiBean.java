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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.webadmin.clienttools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.Stateless;
import javax.ejb.RemoteHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.HttpSession;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
@Stateless(name = "ClientTools/InvokeObject")
@RemoteHome(HttpHome.class)
public class ViewJndiBean extends WebAdminBean  implements Constants {


    private HttpSession session;
    private String selected;
    private String ctxID;
    private Context ctx;

    public void preProcess(HttpRequest request, HttpResponse response)
        throws IOException {
        session = request.getSession(true);
        selected = request.getQueryParameter("selected");
        if (selected == null) {
            selected = "";
        }
        ctxID = request.getQueryParameter("ctx");
        ctx = null;
    }

    public void postProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void writeHtmlTitle(PrintWriter out) throws IOException {
        out.write("Client Tools -- JNDI Viewer");
    }

    public void writePageTitle(PrintWriter out) throws IOException {
        if (ctxID == null){
            out.print("JNDI Environment Naming Context (ENC)");
        } else if (ctxID.startsWith("enc")) {
                out.print("OpenEJB Global JNDI Namespace");
        }
    }

    public void writeBody(PrintWriter out) throws IOException {

        if (ctxID == null) {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
            p.put("openejb.loader", "embed");
            try {
                ctx = new InitialContext( p );
            } catch (NamingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ctxID = null;
            out.print("<b>OpenEJB Global JNDI Namespace</b><br><br>");
        } else {
            ctx = (Context)session.getAttribute(ctxID);
            if (ctxID.startsWith("enc")) {
                out.print("This is the private namespace of an Enterprise JavaBean.");
                out.print("<BR><BR>");
            }
        }

        Node root = new RootNode();
        try {
            buildNode(root,ctx);

            printNodes(root, out, "", selected);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(out);
        }
    }

    class Node {
        static final int CONTEXT = 1;
        static final int BEAN = 2;
        static final int OTHER = 3;
        Node parent;
        Node[] children = new Node[0];
        String name;
        int type = 0;

        public String getID(){
            if (parent instanceof RootNode) {
                return name;
            } else {
                return parent.getID()+"/"+name;
            }
        }
        public String getName(){
            return name;
        }
        public int getType(){
            return type;
        }
        public void addChild(Node child){
            int len = children.length;
            Node[] newChildren = new Node[len+1];
            System.arraycopy(children,0,newChildren,0,len);
            newChildren[len] = child;
            children = newChildren;
            child.parent = this;
        }
    }

    class RootNode extends Node{
        public String getID() {
            return "";
        }
        public String getName() {
            return "";
        }
        public int getType() {
            return Node.CONTEXT;
        }
    }

    public void buildNode(Node parent, Context ctx) throws Exception{
        if (false) throw new NullPointerException();
        NamingEnumeration enumeration = ctx.list( "" );
        while (enumeration.hasMoreElements()){
            NameClassPair pair = (NameClassPair)enumeration.next();
            Node node = new Node();
            parent.addChild(node);
            node.name = pair.getName();

            Object obj = ctx.lookup(node.getName());
            if ( obj instanceof Context ){
                node.type = Node.CONTEXT;
                buildNode(node,(Context)obj);
            } else if (obj instanceof EJBHome || obj instanceof EJBLocalHome) {
                node.type = Node.BEAN;
            } else {
                node.type = Node.OTHER;
            }
        }
    }



    public void printNodes(Node node, PrintWriter out, String tabs, String selected) throws Exception {
        switch (node.getType()) {
            case Node.CONTEXT: printContextNode(node,out,tabs,selected); break;
           case Node.BEAN: printBeanNode(node,out,tabs,selected); break;
          default: printOtherNode(node,out,tabs,selected); break;
        }

    }

    public void printContextNode(Node node, PrintWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        if ( selected.startsWith(id) ) {
            if (ctxID != null) {
                out.print(tabs+"<a href='"+VIEW_JNDI+"?ctx="+ctxID+"&selected="+id+"'>"+openImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
            } else {
                out.print(tabs+"<a href='"+VIEW_JNDI+"?selected="+id+"'>"+openImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
            }
            for (int i=0; i < node.children.length; i++){
                Node child = node.children[i];
                printNodes(child,out,tabs+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",selected);
            }
        } else {
            if (ctxID != null) {
                out.print(tabs+"<a href='"+VIEW_JNDI+"?ctx="+ctxID+"&selected="+id+"'>"+closedImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
            } else {
                out.print(tabs+"<a href='"+VIEW_JNDI+"?selected="+id+"'>"+closedImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
            }
        }
    }

    public void printBeanNode(Node node, PrintWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
//        if (ctxID != null && ctxID.startsWith("enc")) {
            // HACK!
            try{
                Object ejb = ctx.lookup(id);
                Object handler = org.apache.openejb.util.proxy.ProxyManager.getInvocationHandler(ejb);
                Object deploymentID = ((org.apache.openejb.core.ivm.BaseEjbProxyHandler)handler).deploymentID;
                out.print(tabs+"<a href='"+VIEW_EJB+"?ejb="+deploymentID+"'>"+ejbImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
            } catch (Exception e){
                out.print(tabs+ejbImg+"&nbsp;&nbsp;"+node.getName()+"<br>");
            }
//        } else {
//            out.print(tabs+"<a href='"+VIEW_EJB+"?ejb="+id+"'>"+ejbImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
//        }
    }

    public void printOtherNode(Node node, PrintWriter out, String tabs, String selected) throws Exception {
        String id = node.getID();
        Object obj = ctx.lookup(id);
        String clazz = obj.getClass().getName();
        out.print(tabs+"<a href='"+VIEW_CLASS+"?class="+clazz+"'>"+javaImg+"&nbsp;&nbsp;"+node.getName()+"</a><br>");
    }
}
