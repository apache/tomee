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
package org.apache.tomee.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.util.doc.Revisit;

/**
* @version $Rev$ $Date$
*/
@Revisit("this is here to work around LinkageErrors that can occur - seems most often in mac")
public class LinkageErrorProtection {

    public static void preload(final StandardContext standardContext) {
        try {
            final String[] classNames = {
                    "jakarta.servlet.jsp.ErrorData",
                    "jakarta.servlet.jsp.HttpJspPage",
                    "jakarta.servlet.jsp.JspApplicationContext",
                    "jakarta.servlet.jsp.JspContext",
                    "jakarta.servlet.jsp.JspEngineInfo",
                    "jakarta.servlet.jsp.JspException",
                    "jakarta.servlet.jsp.JspFactory",
                    "jakarta.servlet.jsp.JspPage",
                    "jakarta.servlet.jsp.JspTagException",
                    "jakarta.servlet.jsp.JspWriter",
                    "jakarta.servlet.jsp.PageContext",
                    "jakarta.servlet.jsp.SkipPageException",
                    "jakarta.servlet.jsp.el.ELException",
                    "jakarta.servlet.jsp.el.ELParseException",
                    "jakarta.servlet.jsp.el.Expression",
                    "jakarta.servlet.jsp.el.ExpressionEvaluator",
                    "jakarta.servlet.jsp.el.FunctionMapper",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$1",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$1",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$10",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$2",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$3",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$4",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$5",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$6",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$7",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$8",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager$9",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeManager",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeMap$ScopeEntry",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver$ScopeMap",
                    "jakarta.servlet.jsp.el.ImplicitObjectELResolver",
                    "jakarta.servlet.jsp.el.ScopedAttributeELResolver",
                    "jakarta.servlet.jsp.el.VariableResolver",
                    "jakarta.servlet.jsp.tagext.BodyContent",
                    "jakarta.servlet.jsp.tagext.BodyTag",
                    "jakarta.servlet.jsp.tagext.BodyTagSupport",
                    "jakarta.servlet.jsp.tagext.DynamicAttributes",
                    "jakarta.servlet.jsp.tagext.FunctionInfo",
                    "jakarta.servlet.jsp.tagext.IterationTag",
                    "jakarta.servlet.jsp.tagext.JspFragment",
                    "jakarta.servlet.jsp.tagext.JspIdConsumer",
                    "jakarta.servlet.jsp.tagext.JspTag",
                    "jakarta.servlet.jsp.tagext.PageData",
                    "jakarta.servlet.jsp.tagext.SimpleTag",
                    "jakarta.servlet.jsp.tagext.SimpleTagSupport",
                    "jakarta.servlet.jsp.tagext.Tag",
                    "jakarta.servlet.jsp.tagext.TagAdapter",
                    "jakarta.servlet.jsp.tagext.TagAttributeInfo",
                    "jakarta.servlet.jsp.tagext.TagData",
                    "jakarta.servlet.jsp.tagext.TagExtraInfo",
                    "jakarta.servlet.jsp.tagext.TagFileInfo",
                    "jakarta.servlet.jsp.tagext.TagInfo",
                    "jakarta.servlet.jsp.tagext.TagLibraryInfo",
                    "jakarta.servlet.jsp.tagext.TagLibraryValidator",
                    "jakarta.servlet.jsp.tagext.TagSupport",
                    "jakarta.servlet.jsp.tagext.TagVariableInfo",
                    "jakarta.servlet.jsp.tagext.TryCatchFinally",
                    "jakarta.servlet.jsp.tagext.ValidationMessage",
                    "jakarta.servlet.jsp.tagext.VariableInfo",
            };

            for (final String className : classNames) {
                try {
                    load(className, standardContext);
                } catch (final Throwable e) {
                    // no-op
                }
            }
        } catch (final Throwable e) {
            // not critical, this is only to avoid possible jsp LinkageError in Oracle JDK 1.6
        }
    }

    private static void load(final String className, final StandardContext standardContext) {
        final ClassLoader classLoader = standardContext.getLoader().getClassLoader();
        try {
            classLoader.loadClass(className);
        } catch (final ClassNotFoundException e) {
            // no-op
        } catch (final LinkageError e) {
            try {
                classLoader.loadClass(className);
            } catch (final ClassNotFoundException e2) {
                // no-op
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
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:722)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:304)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:210)
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:240)
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:164)
        at org.apache.tomee.catalina.OpenEJBValve.invoke(OpenEJBValve.java:44)
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
