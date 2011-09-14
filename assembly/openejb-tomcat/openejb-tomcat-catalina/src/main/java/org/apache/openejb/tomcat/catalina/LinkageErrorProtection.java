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
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.core.StandardContext;

/**
* @version $Rev$ $Date$
*/
public class LinkageErrorProtection {

    public static void preload(StandardContext standardContext) {
        try {
            String[] classNames = {
                    "javax.servlet.jsp.ErrorData",
                    "javax.servlet.jsp.HttpJspPage",
                    "javax.servlet.jsp.JspApplicationContext",
                    "javax.servlet.jsp.JspContext",
                    "javax.servlet.jsp.JspEngineInfo",
                    "javax.servlet.jsp.JspException",
                    "javax.servlet.jsp.JspFactory",
                    "javax.servlet.jsp.JspPage",
                    "javax.servlet.jsp.JspTagException",
                    "javax.servlet.jsp.JspWriter",
                    "javax.servlet.jsp.PageContext",
                    "javax.servlet.jsp.SkipPageException",
                    "javax.servlet.jsp.el.ELException",
                    "javax.servlet.jsp.el.ELParseException",
                    "javax.servlet.jsp.el.Expression",
                    "javax.servlet.jsp.el.ExpressionEvaluator",
                    "javax.servlet.jsp.el.FunctionMapper",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$1",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$1",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$10",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$2",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$3",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$4",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$5",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$6",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$7",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$8",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$9",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeMap$ScopeEntry",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver$ScopeMap",
                    "javax.servlet.jsp.el.ImplicitObjectELResolver",
                    "javax.servlet.jsp.el.ScopedAttributeELResolver",
                    "javax.servlet.jsp.el.VariableResolver",
                    "javax.servlet.jsp.tagext.BodyContent",
                    "javax.servlet.jsp.tagext.BodyTag",
                    "javax.servlet.jsp.tagext.BodyTagSupport",
                    "javax.servlet.jsp.tagext.DynamicAttributes",
                    "javax.servlet.jsp.tagext.FunctionInfo",
                    "javax.servlet.jsp.tagext.IterationTag",
                    "javax.servlet.jsp.tagext.JspFragment",
                    "javax.servlet.jsp.tagext.JspIdConsumer",
                    "javax.servlet.jsp.tagext.JspTag",
                    "javax.servlet.jsp.tagext.PageData",
                    "javax.servlet.jsp.tagext.SimpleTag",
                    "javax.servlet.jsp.tagext.SimpleTagSupport",
                    "javax.servlet.jsp.tagext.Tag",
                    "javax.servlet.jsp.tagext.TagAdapter",
                    "javax.servlet.jsp.tagext.TagAttributeInfo",
                    "javax.servlet.jsp.tagext.TagData",
                    "javax.servlet.jsp.tagext.TagExtraInfo",
                    "javax.servlet.jsp.tagext.TagFileInfo",
                    "javax.servlet.jsp.tagext.TagInfo",
                    "javax.servlet.jsp.tagext.TagLibraryInfo",
                    "javax.servlet.jsp.tagext.TagLibraryValidator",
                    "javax.servlet.jsp.tagext.TagSupport",
                    "javax.servlet.jsp.tagext.TagVariableInfo",
                    "javax.servlet.jsp.tagext.TryCatchFinally",
                    "javax.servlet.jsp.tagext.ValidationMessage",
                    "javax.servlet.jsp.tagext.VariableInfo",
            };

            for (String className : classNames) {
                load(className, standardContext);
            }
        } catch (Exception e) {
            // not critical, this is only to avoid possible jsp LinkageError in Oracle JDK 1.6
        }
    }

    private static void load(String className, StandardContext standardContext) {
        final ClassLoader classLoader = standardContext.getLoader().getClassLoader();
        try {
            classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                classLoader.loadClass(className);
            } catch (ClassNotFoundException e2) {
            }
        }
    }


    /*

Here's what the LinkageError looks like

SEVERE: Servlet.service() for servlet [jsp] in context with path [/mywebapp] threw exception [java.lang.LinkageError: loader (instance of  org/apache/catalina/loader/StandardClassLoader): attempted  duplicate class definition for name: "javax/servlet/jsp/JspWriter"] with root cause
java.lang.LinkageError: loader (instance of  org/apache/catalina/loader/StandardClassLoader): attempted  duplicate class definition for name: "javax/servlet/jsp/JspWriter"
        at java.lang.ClassLoader.defineClass1(Native Method)
        at java.lang.ClassLoader.defineClassCond(ClassLoader.java:631)
        at java.lang.ClassLoader.defineClass(ClassLoader.java:615)
        at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:141)
        at java.net.URLClassLoader.defineClass(URLClassLoader.java:283)
        at java.net.URLClassLoader.access$000(URLClassLoader.java:58)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:197)
        at java.security.AccessController.doPrivileged(Native Method)
        at java.net.URLClassLoader.findClass(URLClassLoader.java:190)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:306)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:295)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:247)
        at java.lang.Class.getDeclaredMethods0(Native Method)
        at java.lang.Class.privateGetDeclaredMethods(Class.java:2427)
        at java.lang.Class.privateGetPublicMethods(Class.java:2547)
        at java.lang.Class.getMethods(Class.java:1410)
        at java.beans.Introspector.getPublicDeclaredMethods(Introspector.java:1284)
        at java.beans.Introspector.getTargetMethodInfo(Introspector.java:1158)
        at java.beans.Introspector.getBeanInfo(Introspector.java:408)
        at java.beans.Introspector.getBeanInfo(Introspector.java:167)
        at org.apache.jasper.compiler.Generator$TagHandlerInfo.<init>(Generator.java:3932)
        at org.apache.jasper.compiler.Generator$GenerateVisitor.getTagHandlerInfo(Generator.java:2207)
        at org.apache.jasper.compiler.Generator$GenerateVisitor.visit(Generator.java:1635)
        at org.apache.jasper.compiler.Node$CustomTag.accept(Node.java:1539)
        at org.apache.jasper.compiler.Node$Nodes.visit(Node.java:2376)
        at org.apache.jasper.compiler.Node$Visitor.visitBody(Node.java:2428)
        at org.apache.jasper.compiler.Node$Visitor.visit(Node.java:2434)
        at org.apache.jasper.compiler.Node$Root.accept(Node.java:475)
        at org.apache.jasper.compiler.Node$Nodes.visit(Node.java:2376)
        at org.apache.jasper.compiler.Generator.generate(Generator.java:3485)
        at org.apache.jasper.compiler.Compiler.generateJava(Compiler.java:249)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:372)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:352)
        at org.apache.jasper.compiler.Compiler.compile(Compiler.java:339)
        at org.apache.jasper.JspCompilationContext.compile(JspCompilationContext.java:601)
        at org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:344)
        at org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:389)
        at org.apache.jasper.servlet.JspServlet.service(JspServlet.java:333)
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:722)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:304)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:210)
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:240)
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:164)
        at org.apache.openejb.tomcat.catalina.OpenEJBValve.invoke(OpenEJBValve.java:44)
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:462)
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:164)
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:100)
        at org.apache.catalina.valves.AccessLogValve.invoke(AccessLogValve.java:563)
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:118)
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:403)
        at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:301)
        at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:162)
        at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:140)
        at org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:309)
        at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
        at java.lang.Thread.run(Thread.java:680)


     */
}
