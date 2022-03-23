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
package org.apache.tomee.jasper;

import org.apache.jasper.servlet.TldScanner;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.util.descriptor.tld.TagXml;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.descriptor.tld.ValidatorXml;
import org.apache.tomee.installer.Paths;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.tagext.FunctionInfo;
import jakarta.servlet.jsp.tagext.TagAttributeInfo;

import static org.apache.openejb.loader.JarLocation.jarLocation;

@SuppressWarnings("PMD") // this is generated so we don't really care
public class TomEETldScanner extends TldScanner {
    private static final Paths PATHS = new Paths(null);
    private static final URL MYFACES_URL = findJar("myfaces-impl", "org.apache.myfaces.webapp.AbstractFacesInitializer");
    private static final URL JSTL_URL = findJar("taglibs-standard-impl", "jakarta.servlet.jsp.jstl.core.ConditionalTagSupport");
    private static final Map<String, TldResourcePath> URI_TLD_RESOURCE = new HashMap<>();
    private static final Map<TldResourcePath, TaglibXml> TLD_RESOURCE_TAG_LIB = new HashMap<>();

    static {
        populateMyfacesAndJstlData();
    }

    private final Map<String, TldResourcePath> uriTldResourcePathMapParent;
    private final Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMapParent;

    public TomEETldScanner(final ServletContext context, final boolean namespaceAware, final boolean validate, final boolean blockExternal) {
        super(context, namespaceAware, validate, blockExternal);
        uriTldResourcePathMapParent = (Map<String, TldResourcePath>) Reflections.get(this, "uriTldResourcePathMap");
        tldResourcePathTaglibXmlMapParent = (Map<TldResourcePath, TaglibXml>) Reflections.get(this, "tldResourcePathTaglibXmlMap");
        // we don't care about listeners since we add it ourself
    }

    @Override
    protected void scanPlatform() {
        super.scanPlatform();
        if (URLClassLoaderFirst.shouldSkipJsf(Thread.currentThread().getContextClassLoader(), "jakarta.faces.FactoryFinder")) {
            uriTldResourcePathMapParent.putAll(URI_TLD_RESOURCE);
            tldResourcePathTaglibXmlMapParent.putAll(TLD_RESOURCE_TAG_LIB);
        } else { // exclude myfaces
            for (final Map.Entry<String, TldResourcePath> entry : URI_TLD_RESOURCE.entrySet()) {
                final TldResourcePath path = entry.getValue();
                if (path.getUrl() != MYFACES_URL) { // ref works
                    uriTldResourcePathMapParent.put(entry.getKey(), path);
                    final TaglibXml tl = TLD_RESOURCE_TAG_LIB.get(path);
                    if (tl != null) {
                        tldResourcePathTaglibXmlMapParent.put(path, tl);
                    }
                }
            }
        }
    }

    //CHECKSTYLE:OFF
    private static void populateMyfacesAndJstlData() {
        // pre-populate with shared libraries (myfaces, jstl)
        if (MYFACES_URL != null) {
            {
                final TldResourcePath path = new TldResourcePath(MYFACES_URL, null, "META-INF/myfaces_html.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsf/html", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.2");
                taglibXml.setJspVersion("2.1");
                taglibXml.setShortName("h");
                taglibXml.setUri("http://java.sun.com/jsf/html");
                taglibXml.setInfo("This tag library implements the standard JSF HTML tags.");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("inputHidden");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlInputHiddenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders as an HTML input tag with its type set to \"hidden\". Unless otherwise specified, all attributes accept static values or EL expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("column");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlColumnTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Creates a UIComponent that represents a single column of data within a parent UIData component. <p> This tag is commonly used as a child of the h:dataTable tag, to represent a column of data within an html table. It can be decorated with nested \"header\" and \"footer\" facets which cause the output of header and footer rows. </p> <p> The non-facet child components of this column are re-rendered on each table row to generate the content of the cell. Those child components can reference the \"var\" attribute of the containing h:dataTable to generate appropriate output for each row. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("headerClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for the header.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("footerClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for the footer.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rowHeader", false, "jakarta.el.ValueExpression", false, false, "If true the column is rendered with \"th\" and scope=\"row\" attribute, instead \"td\"", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "java.lang.String", false, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("commandButton");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlCommandButtonTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This tag renders as an HTML input element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("image", false, "jakarta.el.ValueExpression", false, false, "HTML: The URL of an image that renders in place of the button.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "HTML: A hint to the user agent about the content type of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("alt", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies alternative text that can be used by a browser that can't show this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The text to display to the user for this command component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("action", false, "jakarta.el.MethodExpression", false, false, "The action to take when this command is invoked. <p> If the value is an expression, it is expected to be a method binding EL expression that identifies an action method. An action method accepts no parameters and has a String return value, called the action outcome, that identifies the next view displayed. The phase that this event is fired in can be controlled via the immediate attribute. </p> <p>  If the value is a string literal, it is treated as a navigation outcome for the current view. This is functionally equivalent to a reference to an action method that returns the string literal. </p>", false, true, "null", "java.lang.Object myMethod(  )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("actionListener", false, "jakarta.el.MethodExpression", false, false, "", false, true, "null", "void myMethod( jakarta.faces.event.ActionEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("commandLink");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlCommandLinkTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This tag renders as an HTML a element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("charset", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the character encoding of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("coords", false, "jakarta.el.ValueExpression", false, false, "HTML: The coordinates of regions within a client side image map.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hreflang", false, "jakarta.el.ValueExpression", false, false, "HTML: The language of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rel", false, "jakarta.el.ValueExpression", false, false, "HTML: The relationship between the current document and the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rev", false, "jakarta.el.ValueExpression", false, false, "HTML: The type(s) describing the reverse link for the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("shape", false, "jakarta.el.ValueExpression", false, false, "HTML: The shape of a region in a client side image map.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, "jakarta.el.ValueExpression", false, false, "HTML: Names the frame that should display content generated by invoking this action.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "HTML: A hint to the user agent about the content type of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The text to display to the user for this command component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("action", false, "jakarta.el.MethodExpression", false, false, "The action to take when this command is invoked. <p> If the value is an expression, it is expected to be a method binding EL expression that identifies an action method. An action method accepts no parameters and has a String return value, called the action outcome, that identifies the next view displayed. The phase that this event is fired in can be controlled via the immediate attribute. </p> <p>  If the value is a string literal, it is treated as a navigation outcome for the current view. This is functionally equivalent to a reference to an action method that returns the string literal. </p>", false, true, "null", "java.lang.Object myMethod(  )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("actionListener", false, "jakarta.el.MethodExpression", false, false, "", false, true, "null", "void myMethod( jakarta.faces.event.ActionEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("dataTable");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlDataTableTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This component renders an HTML table element. <p> This component may have nested facets with names \"header\" and \"footer\" to specify header and footer rows. </p> <p> The non-facet children of this component are expected to be h:column components which describe the columns of the table. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bgcolor", false, "jakarta.el.ValueExpression", false, false, "HTML: The background color of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bodyrows", false, "jakarta.el.ValueExpression", false, false, "CSV of several row index to start (and end a previous) tbody element", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("border", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("cellpadding", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the amount of empty space between the cell border and its contents.  It can be either a pixel length or a percentage.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("cellspacing", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the amount of space between the cells of the table. It can be either a pixel length or a percentage of available  space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("columnClasses", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to td elements in each column.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("footerClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class to be applied to footer cells.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("frame", false, "jakarta.el.ValueExpression", false, false, "HTML: Controls what part of the frame that surrounds a table is  visible.  Values include:  void, above, below, hsides, lhs,  rhs, vsides, box, and border.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("headerClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class to be applied to header cells.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rowClasses", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to td elements in each row.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rules", false, "jakarta.el.ValueExpression", false, false, "HTML: Controls how rules are rendered between cells.  Values include: none, groups, rows, cols, and all.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("summary", false, "jakarta.el.ValueExpression", false, false, "HTML: Provides a summary of the contents of the table, for accessibility purposes.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("width", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the desired width of the table, as a pixel length or a percentage of available space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("captionClass", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to all captions. If there are less classes than the number of rows, apply the same sequence of classes to the remaining captions, so the pattern is repeated. More than one class can be applied to a row by separating the classes with a space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("captionStyle", false, "jakarta.el.ValueExpression", false, false, "Gets The CSS class to be applied to the Caption.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "An EL expression that specifies the data model that backs this table. <p> The value referenced by the EL expression can be of any type. </p> <ul> <li>A value of type DataModel is used directly.</li> <li>Array-like parameters of type array-of-Object, java.util.List, java.sql.ResultSet or jakarta.servlet.jsp.jstl.sql.Result are wrapped in a corresponding DataModel that knows how to iterate over the elements.</li> <li>Other values are wrapped in a DataModel as a single row.</li> </ul> <p> Note in particular that unordered collections, eg Set are not supported. Therefore if the value expression references such an object then the table will be considered to contain just one element - the collection itself. </p>", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("first", false, "jakarta.el.ValueExpression", false, false, "Defines the index of the first row to be displayed, starting from 0.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rows", false, "jakarta.el.ValueExpression", false, false, "Defines the maximum number of rows of data to be displayed. <p> Specify zero to display all rows from the \"first\" row to the end of available data. </p>", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Defines the name of the request-scope variable that will hold the current row during iteration. <p> During rendering of child components of this UIData, the variable with this name can be read to learn what the \"rowData\" object for the row currently being rendered is. </p> <p> This value must be a static value, ie an EL expression is not permitted. </p>", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("form");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlFormTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders an HTML form element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accept", false, "jakarta.el.ValueExpression", false, false, "HTML: Provides a comma-separated list of content types that the  server processing this form can handle.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("acceptcharset", false, "jakarta.el.ValueExpression", false, false, "HTML: The list of character encodings accepted by the server for this form.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enctype", false, "jakarta.el.ValueExpression", false, false, "HTML: The content type used to submit this form to the server.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onreset", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when this form is reset.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onsubmit", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when this form is submitted.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, "jakarta.el.ValueExpression", false, false, "HTML: Names the frame that should display content generated by invoking this action.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("prependId", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("graphicImage");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlGraphicImageTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders an HTML img element. <p> The value attribute specifies the url of the image to be displayed; see the documentation for attribute \"url\" for more details. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("height", false, "jakarta.el.ValueExpression", false, false, "HTML: Overrides the natural height of this image, by specifying height in pixels.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ismap", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies server-side image map handling for this image.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("longdesc", false, "jakarta.el.ValueExpression", false, false, "HTML: A link to a long description of the image.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("usemap", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies an image map to use with this image.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("width", false, "jakarta.el.ValueExpression", false, false, "HTML: Overrides the natural width of this image, by specifying width in pixels.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("library", false, "jakarta.el.ValueExpression", false, false, "", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", false, "jakarta.el.ValueExpression", false, false, "", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("alt", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies alternative text that can be used by a browser that can't show this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, "jakarta.el.ValueExpression", false, false, "An alias for the \"value\" attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The URL of the image. <p> If the URL starts with a '/', it is relative to the context path of the web application. </p>", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("inputFile");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlInputFileTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders a HTML input element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxlength", false, "jakarta.el.ValueExpression", false, false, "HTML: The maximum number of characters allowed to be entered.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("size", false, "jakarta.el.ValueExpression", false, false, "HTML: The initial width of this control, in characters.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("autocomplete", false, "jakarta.el.ValueExpression", false, false, "If the value of this attribute is \"off\", render \"off\" as the value of the attribute. This indicates that the browser should disable its autocomplete feature for this component. This is useful for components that perform autocompletion and do not want the browser interfering. If this attribute is not set or the value is \"on\", render nothing.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("alt", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies alternative text that can be used by a browser that can't show this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("inputSecret");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlInputSecretTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders as an HTML input tag with its type set to \"password\".");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxlength", false, "jakarta.el.ValueExpression", false, false, "HTML: The maximum number of characters allowed to be entered.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("redisplay", false, "jakarta.el.ValueExpression", false, false, "If true, the value will be re-sent (in plaintext) when the form is rerendered (see JSF.7.4.4). Default is false.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("size", false, "jakarta.el.ValueExpression", false, false, "HTML: The initial width of this control, in characters.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("autocomplete", false, "jakarta.el.ValueExpression", false, false, "If the value of this attribute is \"off\", render \"off\" as the value of the attribute. This indicates that the browser should disable its autocomplete feature for this component. This is useful for components that perform autocompletion and do not want the browser interfering. If this attribute is not set or the value is \"on\", render nothing.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("alt", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies alternative text that can be used by a browser that can't show this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("inputText");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlInputTextTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders a HTML input element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxlength", false, "jakarta.el.ValueExpression", false, false, "HTML: The maximum number of characters allowed to be entered.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("size", false, "jakarta.el.ValueExpression", false, false, "HTML: The initial width of this control, in characters.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("autocomplete", false, "jakarta.el.ValueExpression", false, false, "If the value of this attribute is \"off\", render \"off\" as the value of the attribute. This indicates that the browser should disable its autocomplete feature for this component. This is useful for components that perform autocompletion and do not want the browser interfering. If this attribute is not set or the value is \"on\", render nothing.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("alt", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies alternative text that can be used by a browser that can't show this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("inputTextarea");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlInputTextareaTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders a HTML textarea element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("cols", false, "jakarta.el.ValueExpression", false, false, "HTML: The width of this element, in characters.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rows", false, "jakarta.el.ValueExpression", false, false, "HTML: The height of this element, in characters.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("message");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlMessageTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders text displaying information about the first FacesMessage           that is assigned to the component referenced by the \"for\" attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("errorClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"ERROR\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("errorStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"ERROR\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("fatalClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"FATAL\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("fatalStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"FATAL\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("infoClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"INFO\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("infoStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"INFO\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tooltip", false, "jakarta.el.ValueExpression", false, false, "If true, the message summary will be rendered as a tooltip (i.e. HTML title attribute).", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("warnClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"WARN\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("warnStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"WARN\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("for", true, "jakarta.el.ValueExpression", false, false, "The ID of the component whose attached FacesMessage object (if present)  should be diplayed by this component. <p> This is a required property on the component. </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("showDetail", false, "jakarta.el.ValueExpression", false, false, "Specifies whether the detailed information from the message should be shown.  Default to true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("showSummary", false, "jakarta.el.ValueExpression", false, false, "Specifies whether the summary information from the message should be shown. Defaults to false.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("messages");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlMessagesTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders all or some FacesMessages depending on the \"for\" and \"globalOnly\" attributes.  <ul> <li>If globalOnly = true, only global messages, that have no associated clientId, will be displayed.</li> <li>else if there is a \"for\" attribute, only messages that are assigned to the component referenced by the \"for\" attribute are displayed.</li> <li>else all messages are displayed.</li> </ul>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("layout", false, "jakarta.el.ValueExpression", false, false, "The layout: \"table\" or \"list\". Default: list", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("errorClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"ERROR\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("errorStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"ERROR\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("fatalClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"FATAL\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("fatalStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"FATAL\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("infoClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"INFO\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("infoStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"INFO\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tooltip", false, "jakarta.el.ValueExpression", false, false, "If true, the message summary will be rendered as a tooltip (i.e. HTML title attribute).", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("warnClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be used for messages with severity \"WARN\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("warnStyle", false, "jakarta.el.ValueExpression", false, false, "CSS style to be used for messages with severity \"WARN\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("globalOnly", false, "jakarta.el.ValueExpression", false, false, "Specifies whether only messages (FacesMessage objects) not associated with a specific component should be displayed, ie whether messages should be ignored if they are attached to a particular component. Defaults to false.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("showDetail", false, "jakarta.el.ValueExpression", false, false, "Specifies whether the detailed information from the message should be shown.  Default to false.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("showSummary", false, "jakarta.el.ValueExpression", false, false, "Specifies whether the summary information from the message should be shown. Defaults to true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("for", false, "jakarta.el.ValueExpression", false, false, "The ID of the component whose attached FacesMessage object (if present)  should be diplayed by this component. It takes precedence over globalOnly.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("outputFormat");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlOutputFormatTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders as text, applying the child f:param values to the value attribute as a MessageFormat string.  If this element has an ID or CSS style properties, the text is wrapped in a span element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escape", false, "jakarta.el.ValueExpression", false, false, "Indicates whether rendered markup should be escaped. Default: true", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("outputLabel");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlOutputLabelTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders a HTML label element.  In addition to the JSF specification, MyFaces allows it to directly give an output text via the \"value\" attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("for", false, "jakarta.el.ValueExpression", false, false, "The client ID of the target input element of this label.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escape", false, "jakarta.el.ValueExpression", false, false, "Indicates whether rendered markup should be escaped. Default: true", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("outputLink");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlOutputLinkTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders a HTML a element.  Child f:param elements are added to the href attribute as query parameters.  Other children are rendered as the link text or image.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("charset", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the character encoding of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("coords", false, "jakarta.el.ValueExpression", false, false, "HTML: The coordinates of regions within a client side image map.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hreflang", false, "jakarta.el.ValueExpression", false, false, "HTML: The language of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rel", false, "jakarta.el.ValueExpression", false, false, "HTML: The relationship between the current document and the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rev", false, "jakarta.el.ValueExpression", false, false, "HTML: The type(s) describing the reverse link for the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("shape", false, "jakarta.el.ValueExpression", false, false, "HTML: The shape of a region in a client side image map.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, "jakarta.el.ValueExpression", false, false, "HTML: Names the frame that should display content generated by invoking this action.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "HTML: A hint to the user agent about the content type of the linked resource.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("outputText");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlOutputTextTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Renders the value of the associated UIOutput component.  If this element has an ID or CSS style properties, the text is wrapped in a span element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escape", false, "jakarta.el.ValueExpression", false, false, "Indicates whether rendered markup should be escaped. Default: true", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("panelGrid");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlPanelGridTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This element renders as an HTML table with specified number of columns. <p> Children of this element are rendered as cells in the table, filling rows from left to right.  Facets named \"header\" and \"footer\" are optional and specify the content of the thead and tfoot rows, respectively. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bgcolor", false, "jakarta.el.ValueExpression", false, false, "HTML: The background color of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bodyrows", false, "jakarta.el.ValueExpression", false, false, "CSV of several row index to start (and end a previous) tbody element", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("border", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("cellpadding", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the amount of empty space between the cell border and its contents.  It can be either a pixel length or a percentage.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("cellspacing", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the amount of space between the cells of the table. It can be either a pixel length or a percentage of available  space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("columnClasses", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to td elements in each column.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("columns", false, "jakarta.el.ValueExpression", false, false, "Specifies the number of columns in the grid.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("footerClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class to be applied to footer cells.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("frame", false, "jakarta.el.ValueExpression", false, false, "HTML: Controls what part of the frame that surrounds a table is  visible.  Values include:  void, above, below, hsides, lhs,  rhs, vsides, box, and border.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("headerClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class to be applied to header cells.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rowClasses", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to td elements in each row.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rules", false, "jakarta.el.ValueExpression", false, false, "HTML: Controls how rules are rendered between cells.  Values include: none, groups, rows, cols, and all.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("summary", false, "jakarta.el.ValueExpression", false, false, "HTML: Provides a summary of the contents of the table, for accessibility purposes.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("width", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the desired width of the table, as a pixel length or a percentage of available space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("captionClass", false, "jakarta.el.ValueExpression", false, false, "A comma separated list of CSS class names to apply to all captions. If there are less classes than the number of rows, apply the same sequence of classes to the remaining captions, so the pattern is repeated. More than one class can be applied to a row by separating the classes with a space.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("captionStyle", false, "jakarta.el.ValueExpression", false, false, "Gets The CSS class to be applied to the Caption.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("panelGroup");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlPanelGroupTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This element is used to group other components where the specification requires one child element.  If any of the HTML or CSS attributes are set, its content is rendered within a span element.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("layout", false, "jakarta.el.ValueExpression", false, false, "The type of layout markup to use when rendering this group. If the value is \"block\" the renderer must produce an HTML \"div\" element. Otherwise HTML \"span\" element must be produced.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectBooleanCheckbox");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectBooleanCheckboxTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to choose a \"true\" or \"false\" value, presented as a checkbox. <p> Renders as an HTML input tag with its type set to \"checkbox\", and its name attribute set to the id. The \"checked\" attribute is rendered if the value of this component is true. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectManyCheckbox");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectManyCheckboxTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to select zero or more items from a set of available options. <p>  This is presented as a table with one cell per available option; each cell contains a checkbox and the option's label. The \"layout\" attribute determines whether the checkboxes are laid out horizontally or vertically. </p> <p> The set of available options is defined by adding child f:selectItem or f:selectItems components to this component. </p> <p> The value attribute must be a value-binding expression to a property of type List, Object array or primitive array. That \"collection\" is expected to contain objects of the same type as SelectItem.getValue() returns for the child SelectItem objects. On rendering, any child whose value is in the list will be selected initially. During the update phase, the property setter is called to replace the original collection with a completely new collection object of the appropriate type. The new collection object contains the value of each child SelectItem object that is currently selected. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("border", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("layout", false, "jakarta.el.ValueExpression", false, false, "Controls the layout direction of the child elements.  Values include:   lineDirection (vertical) and pageDirection (horzontal).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("selectedClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be applied to selected items", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("unselectedClass", false, "jakarta.el.ValueExpression", false, false, "CSS class to be applied to unselected items", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("collectionType", false, "jakarta.el.ValueExpression", false, false, "", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectManyListbox");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectManyListboxTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to select zero or more items from a set of available options. This is presented as a listbox which allows multiple rows in the list to be selected simultaneously. <p> The set of available options is defined by adding child f:selectItem or f:selectItems components to this component. </p> <p> The list is rendered as an HTML select element. The \"multiple\" attribute is set on the element and the size attribute is set to the provided value, defaulting to the number of items in the list if no value is provided. If the size is set to 1, then a \"drop-down\" list (aka \"combo-box\") is presented, though if this is the intention then a selectManyMenu should be used instead. </p> <p> The value attribute must be a value-binding expression to a property of type List, Object array or primitive array. That \"collection\" is expected to contain objects of the same type as SelectItem.getValue() returns for the child SelectItem objects. On rendering, any child whose value is in the list will be selected initially. During the update phase, the property is set to contain a \"collection\" of values for those child SelectItem objects that are currently selected. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("size", false, "jakarta.el.ValueExpression", false, false, "see JSF Spec.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("collectionType", false, "jakarta.el.ValueExpression", false, false, "", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectManyMenu");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectManyMenuTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to select zero or more items from a set of available options. This is presented as a drop-down \"combo-box\" which allows multiple rows in the list to be selected simultaneously. <p> The set of available options is defined by adding child f:selectItem or f:selectItems components to this component. </p> <p> Renders as an HTML select element, with the choices made up of child f:selectItem or f:selectItems elements. The multiple attribute is set and the size attribute is set to 1. </p> <p> The value attribute must be a value-binding expression to a property of type List, Object array or primitive array. That \"collection\" is expected to contain objects of the same type as SelectItem.getValue() returns for the child SelectItem objects. On rendering, any child whose value is in the list will be selected initially. During the update phase, the property is set to contain a \"collection\" of values for those child SelectItem objects that are currently selected. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("collectionType", false, "jakarta.el.ValueExpression", false, false, "", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectOneListbox");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectOneListboxTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to choose one option from a set of options. <p> Rendered as a listbox with the MULTIPLE attribute set to false. </p> <p> The available choices are defined via child f:selectItem or f:selectItems elements. The size of the listbox defaults to the number of available choices; if size is explicitly set to a smaller value, then scrollbars will be rendered. If size is set to 1 then a \"drop-down menu\" (aka \"combo-box\") is rendered, though if this is the intent then selectOneMenu should be used instead. </p> <p> The value attribute of this component is read to determine which of the available options is initially selected; its value should match the \"value\" property of one of the child SelectItem objects. </p> <p> On submit of the enclosing form, the value attribute's bound property is updated to contain the \"value\" property from the chosen SelectItem. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("size", false, "jakarta.el.ValueExpression", false, false, "see JSF Spec.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectOneMenu");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectOneMenuTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to choose one option from a set of options. <p> Renders a drop-down menu (aka \"combo-box\") containing a set of choices, of which only one can be chosen at a time. The available choices are defined via child f:selectItem or f:selectItems elements. </p> <p> The value attribute of this component is read to determine which of the available options is initially selected; its value should match the \"value\" property of one of the child SelectItem objects. </p> <p> On submit of the enclosing form, the value attribute's bound property is updated to contain the \"value\" property from the chosen SelectItem. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectOneRadio");
                    tag.setTagClass("org.apache.myfaces.taglib.html.HtmlSelectOneRadioTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Allow the user to choose one option from a set of options. <p> Renders as an HTML table element, containing an input element for each child f:selectItem or f:selectItems elements.  The input elements are rendered as type radio. </p> <p> The value attribute of this component is read to determine which of the available options is initially selected; its value should match the \"value\" property of one of the child SelectItem objects. </p> <p> On submit of the enclosing form, the value attribute's bound property is updated to contain the \"value\" property from the chosen SelectItem. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("border", false, "jakarta.el.ValueExpression", false, false, "Width in pixels of the border to be drawn around the table containing the options list.", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("layout", false, "jakarta.el.ValueExpression", false, false, "Orientation of the options list. Valid values are  \"pageDirection\" for a vertical layout, or \"lineDirection\" for horizontal. The default value is \"lineDirection\".", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("label", false, "jakarta.el.ValueExpression", false, false, "A display name for this component.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("style", false, "jakarta.el.ValueExpression", false, false, "HTML: CSS styling instructions.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("styleClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class for this element.  Corresponds to the HTML 'class' attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("tabindex", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies the position of this element within the tab order of the document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onblur", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element loses focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onfocus", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element receives focus.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("enabledClass", false, "jakarta.el.ValueExpression", false, false, "The CSS class assigned to the label element for enabled choices.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("accesskey", false, "jakarta.el.ValueExpression", false, false, "HTML: Sets the access key for this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("role", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("ondblclick", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the element is double-clicked.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeydown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed down over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeypress", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onkeyup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when a key is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousedown", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is pressed over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmousemove", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved while it is in this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseout", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moves out of this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseover", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is moved into this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onmouseup", false, "jakarta.el.ValueExpression", false, false, "HTML: Script to be invoked when the pointing device is released over this element.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onchange", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is modified.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("onselect", false, "jakarta.el.ValueExpression", false, false, "HTML: Specifies a script to be invoked when the element is selected.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dir", false, "jakarta.el.ValueExpression", false, false, "HTML: The direction of text display, either 'ltr' (left-to-right) or 'rtl' (right-to-left).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("lang", false, "jakarta.el.ValueExpression", false, false, "HTML: The base language of this document.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("title", false, "jakarta.el.ValueExpression", false, false, "HTML: An advisory title for this element.  Often used by the user agent as a tooltip.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("disabled", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, this element cannot receive focus.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("readonly", false, "jakarta.el.ValueExpression", false, false, "HTML: When true, indicates that this component cannot be modified by the user. The element may receive focus unless it has also been disabled.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("hideNoSelectionOption", false, "jakarta.el.ValueExpression", false, false, "", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("immediate", false, "jakarta.el.ValueExpression", false, false, "A boolean value that identifies the phase during which action events should fire. <p> During normal event processing, action methods and action listener methods are fired during the \"invoke application\" phase of request processing. If this attribute is set to \"true\", these methods are fired instead at the end of the \"apply request values\" phase. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(MYFACES_URL, null, "META-INF/myfaces_core.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsf/core", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.2");
                taglibXml.setJspVersion("2.1");
                taglibXml.setShortName("f");
                taglibXml.setUri("http://java.sun.com/jsf/core");
                taglibXml.setInfo("This tag library implements the standard JSF core tags.");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("subview");
                    tag.setTagClass("org.apache.myfaces.taglib.core.SubviewTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Base class for components that provide a new \"namespace\" for the ids of their child components. <p> See the javadocs for interface NamingContainer for further details. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether this component should be rendered. Default value: true.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", true, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("viewParam");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ViewParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("TODO: documentation on jsp and pld are not the same. It appear two params: maxlength and for, but no property getter and setter founded here.  If maxlength is used, we can put something like this:  JSFJspProperty(name = \"maxlength\", returnType = \"java.lang.String\")");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxlength", false, "jakarta.el.ValueExpression", false, false, "The max number or characters allowed for this param", true, false, "int", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("required", false, "jakarta.el.ValueExpression", false, false, "A boolean value that indicates whether an input value is required. <p> If this value is true and no input value is provided by a postback operation, then the \"requiredMessage\" text is registered as a FacesMessage for the request, and validation fails. </p> <p> Default value: false. </p>", true, false, "boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when conversion of a submitted value to the target type fails. <p> </p>", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("requiredMessage", false, "jakarta.el.ValueExpression", false, false, "Text to be displayed to the user as an error message when this component is marked as \"required\" but no input data is present during a postback (ie the user left the required field blank).", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validator", false, "jakarta.el.MethodExpression", false, false, "A method-binding EL expression which is invoked during the validation phase for this component. <p> The invoked method is expected to check the submitted value for this component, and if not acceptable then report a validation error for the component. </p> <p> The method is expected to have the prototype </p> <code>public void aMethod(FacesContext, UIComponent,Object)</code>", false, true, "null", "void myMethod( jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.Object )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorMessage", false, "jakarta.el.ValueExpression", false, false, "Text which will be shown if validation fails.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("valueChangeListener", false, "jakarta.el.MethodExpression", false, false, "A method which is invoked during postback processing for the current view if the submitted value for this component is not equal to the value which the \"value\" expression for this component returns. <p> The phase in which this method is invoked can be controlled via the immediate attribute. </p>", false, true, "null", "void myMethod( jakarta.faces.event.ValueChangeEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "Gets The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converter", false, "jakarta.el.ValueExpression", false, false, "An expression that specifies the Converter for this component. <p> The value can either be a static value (ID) or an EL expression. When a static id is specified, an instance of the converter type registered with that id is used. When this is an EL expression, the result of evaluating the expression must be an object that implements the Converter interface. </p>", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("view");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ViewTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Creates a JSF View, which is a container that holds all of the components that are part of the view. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p> <p> See the javadoc for this class in the <a href=\"http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a> for further details. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("afterPhase", false, "jakarta.el.MethodExpression", false, false, "MethodBinding pointing to a method that takes a jakarta.faces.event.PhaseEvent and returns void, called after every phase except for restore view.", false, true, "null", "void myMethod( jakarta.faces.event.PhaseEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("beforePhase", false, "jakarta.el.MethodExpression", false, false, "MethodBinding pointing to a method that takes a jakarta.faces.event.PhaseEvent and returns void, called before every phase except for restore view.", false, true, "null", "void myMethod( jakarta.faces.event.PhaseEvent )");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("locale", false, "jakarta.el.ValueExpression", false, false, "The locale for this view. <p> Defaults to the default locale specified in the faces configuration file. </p>", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("renderKitId", false, "jakarta.el.ValueExpression", false, false, "Defines what renderkit should be used to render this view.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This tag associates a parameter name-value pair with the nearest parent UIComponent. A UIComponent is created to represent this name-value pair, and stored as a child of the parent component; what effect this has depends upon the renderer of that parent component. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", false, "jakarta.el.ValueExpression", false, false, "The name under which the value is stored.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectItem");
                    tag.setTagClass("org.apache.myfaces.taglib.core.SelectItemTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag associates a single SelectItem with the nearest parent UIComponent. The item represents a single option for a component such as an h:selectBooleanCheckbox or h:selectOneMenu. See also component selectItems. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p> <p> UISelectItem should be nestetd inside a UISelectMany or UISelectOne component, and results in the addition of a SelectItem instance to the list of available options for the parent component </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The initial value of this component.", true, false, "jakarta.faces.model.SelectItem", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemDisabled", false, "jakarta.el.ValueExpression", false, false, "Determine whether this item can be chosen by the user. When true, this item cannot be chosen by the user. If this method is ever called, then any EL-binding for the disabled property will be ignored.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escape", false, "jakarta.el.ValueExpression", false, false, "The escape setting for the label of this selection item.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemDescription", false, "jakarta.el.ValueExpression", false, false, "For use in development tools.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemLabel", false, "jakarta.el.ValueExpression", false, false, "The string which will be presented to the user for this option.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemValue", false, "jakarta.el.ValueExpression", false, false, "The value for this Item.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("noSelectionOption", false, "jakarta.el.ValueExpression", false, false, "Indicate this component represent no selection option.  Default value is false.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("selectItems");
                    tag.setTagClass("org.apache.myfaces.taglib.core.SelectItemsTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag associates a set of selection list items with the nearest parent UIComponent. The set of SelectItem objects is retrieved via a value-binding. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p> <p> UISelectItems should be nested inside a UISelectMany or UISelectOne component, and results in  the addition of one ore more SelectItem instance to the list of available options for the parent component </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The initial value of this component.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of a request-scope attribute under which the current item of the collection, array, etc. of the value attribute will be  exposed so that it can be referred to in EL for other attributes  of this component.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemValue", false, "jakarta.el.ValueExpression", false, false, "The value for the current item.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemLabel", false, "jakarta.el.ValueExpression", false, false, "The label of the current item.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemDescription", false, "jakarta.el.ValueExpression", false, false, "The description of the current item.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemDisabled", false, "jakarta.el.ValueExpression", false, false, "Determines if the current item is selectable or not.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("itemLabelEscaped", false, "jakarta.el.ValueExpression", false, false, "Determines if the rendered markup for the current item receives normal JSF HTML escaping or not.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("id", false, "null", true, false, "Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer component. The id is not necessarily unique across all components in the current view.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Identifies a backing bean property (of type UIComponent or appropriate subclass) to bind \" + \"to this component instance. This value must be an EL expression.", true, false, "jakarta.faces.component.UIComponent", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("convertDateTime");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ConvertDateTimeTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag associates a date time converter with the nearest parent UIComponent.  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, "jakarta.el.ValueExpression", false, false, "The style of the date.  Values include: default, short, medium,  long, and full.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("locale", false, "jakarta.el.ValueExpression", false, false, "The name of the locale to be used, instead of the default.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "jakarta.el.ValueExpression", false, false, "A custom Date formatting pattern, in the format used by java.text.SimpleDateFormat.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, "jakarta.el.ValueExpression", false, false, "The style of the time.  Values include:  default, short, medium, long,  and full.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, "jakarta.el.ValueExpression", false, false, "The time zone to use instead of GMT (the default timezone). When this value is a value-binding to a TimeZone instance, that timezone is used. Otherwise this value is treated as a String containing a timezone id, ie as the ID parameter of method java.util.TimeZone.getTimeZone(String).", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "Specifies whether the date, time, or both should be  parsed/formatted.  Values include:  date, time, and both. Default based on setting of timeStyle and dateStyle.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a DateTimeConverter.", true, false, "jakarta.faces.convert.DateTimeConverter", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("convertNumber");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ConvertNumberTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag creates a number formatting converter and associates it with the nearest parent UIComponent.  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencyCode", false, "jakarta.el.ValueExpression", false, false, "ISO 4217 currency code", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencySymbol", false, "jakarta.el.ValueExpression", false, false, "The currency symbol used to format a currency value.  Defaults to the currency symbol for locale.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("groupingUsed", false, "jakarta.el.ValueExpression", false, false, "Specifies whether output will contain grouping separators.  Default: true.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("integerOnly", false, "jakarta.el.ValueExpression", false, false, "Specifies whether only the integer part of the input will be parsed.  Default: false.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("locale", false, "jakarta.el.ValueExpression", false, false, "The name of the locale to be used, instead of the default as specified in the faces configuration file.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxFractionDigits", false, "jakarta.el.ValueExpression", false, false, "The maximum number of digits in the fractional portion of the number.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxIntegerDigits", false, "jakarta.el.ValueExpression", false, false, "The maximum number of digits in the integer portion of the number.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minFractionDigits", false, "jakarta.el.ValueExpression", false, false, "The minimum number of digits in the fractional portion of the number.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minIntegerDigits", false, "jakarta.el.ValueExpression", false, false, "The minimum number of digits in the integer portion of the number.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "jakarta.el.ValueExpression", false, false, "A custom Date formatting pattern, in the format used by java.text.SimpleDateFormat.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "The type of formatting/parsing to be performed.  Values include: number, currency, and percent.  Default: number.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a NumberConverter.", true, false, "jakarta.faces.convert.NumberConverter", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("validateDoubleRange");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValidateDoubleRangeTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a validator and associateds it with the nearest parent UIComponent.  When invoked, the validator ensures that values are valid doubles that lie within the minimum and maximum values specified.  Commonly associated with a h:inputText entity.  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maximum", false, "jakarta.el.ValueExpression", false, false, "The largest value that should be considered valid.", true, false, "java.lang.Double", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minimum", false, "jakarta.el.ValueExpression", false, false, "The smallest value that should be considered valid.", true, false, "java.lang.Double", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a DoubleRangeValidator.", true, false, "jakarta.faces.validator.DoubleRangeValidator", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("validateLength");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValidateLengthTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a validator and associateds it with the nearest parent UIComponent.  When invoked, the validator ensures that values are valid strings with a length that lies within the minimum and maximum values specified.  Commonly associated with a h:inputText entity.  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maximum", false, "jakarta.el.ValueExpression", false, false, "The largest value that should be considered valid.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minimum", false, "jakarta.el.ValueExpression", false, false, "The smallest value that should be considered valid.", true, false, "java.lang.Integer", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a LengthValidator.", true, false, "jakarta.faces.validator.LengthValidator", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("validateLongRange");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValidateLongRangeTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a validator and associateds it with the nearest parent UIComponent.  When invoked, the validator ensures that values are valid longs that lie within the minimum and maximum values specified.  Commonly associated with a h:inputText entity.  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maximum", false, "jakarta.el.ValueExpression", false, false, "The largest value that should be considered valid.", true, false, "java.lang.Long", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minimum", false, "jakarta.el.ValueExpression", false, false, "The smallest value that should be considered valid.", true, false, "java.lang.Long", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a LongRangeValidator.", true, false, "jakarta.faces.validator.LongRangeValidator", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("validateRegex");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValidateRegexTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("<p>   <strong>RegexValidator</strong> is a {@link jakarta.faces.validator.Validator}   that checks the value of the corresponding component against specified   pattern using Java regular expression syntax.    The regular expression syntax accepted by the RegexValidator class is   same as mentioned in class {@link java.util.regex.Pattern} in package   <code>java.util.regex</code>. </p>  <p>   The following algorithm is implemented: </p>  <ul>   <li>If the passed value is <code>null</code>, exit immediately.</li>   <li>     If the passed value is not a String, exit with a {@link #NOT_MATCHED_MESSAGE_ID}     error message.   </li>   <li>     If no pattern has been set, or pattern resolves to <code>null</code> or an     empty String, throw a {@link jakarta.faces.validator.ValidatorException}     with a {@link #PATTERN_NOT_SET_MESSAGE_ID} message.   </li>   <li>     If pattern is not a valid regular expression, according to the rules as defined     in class {@link java.util.regex.Pattern}, throw a {@link ValidatorException}     with a (@link #MATCH_EXCEPTION_MESSAGE_ID} message.   </li>   <li>     If a <code>pattern</code> property has been configured on this     {@link jakarta.faces.validator.Validator}, check the passed value against this pattern.     If value does not match pattern throw a {@link ValidatorException}     containing a {@link #NOT_MATCHED_MESSAGE_ID} message.   </li> </ul>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", true, "jakarta.el.ValueExpression", false, false, "Return the ValueExpression that yields the regular expression pattern when evaluated.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a RegexValidator.", true, false, "jakarta.faces.validator.RegexValidator", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("facet");
                    tag.setTagClass("jakarta.faces.webapp.FacetTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("This tag adds its child as a facet of the nearest parent UIComponent. A child consisting of multiple elements should be nested within a container component (i.e., within an h:panelGroup for HTML library components).  Unless otherwise specified, all attributes accept static values or EL expressions.  see Javadoc of <a href=\"http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html\">JSF Specification</a>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, "java.lang.String", false, false, "The name of the facet to be created. This must be a static value.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("actionListener");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ActionListenerTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag creates an instance of the specified ActionListener, and associates it with the nearest parent UIComponent. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "The fully qualified class name of the ActionListener class.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Value binding expression that evaluates to an object that implements jakarta.faces.event.ActionListener.", true, false, "jakarta.faces.event.ActionListener", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("attribute");
                    tag.setTagClass("org.apache.myfaces.taglib.core.AttributeTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag associates an attribute with the nearest parent UIComponent. <p> When the value is not an EL expression, this tag has the same effect as calling component.getAttributes.put(name, value). When the attribute name specified matches a standard property of the component, that property is set. However it is also valid to assign attributes to components using any arbitrary name; the component itself won't make any use of these but other objects such as custom renderers, validators or action listeners can later retrieve the attribute from the component by name. </p> <p> When the value is an EL expression, this tag has the same effect as calling component.setValueBinding. A call to method component.getAttributes().get(name) will then cause that expression to be evaluated and the result of the expression is returned, not the original EL expression string. </p> <p> See the javadoc for UIComponent.getAttributes for more details. </p> <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", false, "jakarta.el.ValueExpression", false, false, "The name of the attribute.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", false, false, "The attribute's value.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("converter");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ConverterImplTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("This tag creates an instance of the specified Converter, and associates it with the nearest parent UIComponent.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("converterId", false, "jakarta.el.ValueExpression", false, false, "The converter's registered ID.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to a Converter.", true, false, "jakarta.faces.convert.Converter", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("loadBundle");
                    tag.setTagClass("org.apache.myfaces.taglib.core.LoadBundleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Loads a resource bundle and saves it as a variable in the request scope. <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p> <p> TODO: We should find a way to save loaded bundles in the state, because otherwise on the next request the bundle map will not be present before the render phase and value bindings that reference to the bundle will always log annoying \"Variable 'xxx' could not be resolved\" error messages. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", false, "jakarta.el.ValueExpression", false, false, "The base name of the resource bundle.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, "The name of the variable in request scope that the resources are saved to. This must be a static value.", false, false, "null", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("phaseListener");
                    tag.setTagClass("org.apache.myfaces.taglib.core.PhaseListenerTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Register a PhaseListener instance");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "Class name of the PhaseListener to be created and registered.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Value binding expression that evaluates to a PhaseListener.", true, false, "jakarta.faces.event.PhaseListener", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setPropertyActionListener");
                    tag.setTagClass("org.apache.myfaces.taglib.core.SetPropertyActionListenerTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", true, "jakarta.el.ValueExpression", false, false, "ValueExpression for the destination of the value attribute.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "jakarta.el.ValueExpression", false, false, "ValueExpression for the value of the target attribute.", true, false, "java.lang.Object", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("validator");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValidatorImplTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a validator and associates it with the nearest parent UIComponent. <p> During the validation phase (or the apply-request-values phase for immediate components), if the associated component has any submitted value and the conversion of that value to the required type has succeeded then the specified validator type is invoked to test the validity of the converted value. </p> <p> Commonly associated with an h:inputText entity, but may be applied to any input component. </p> <p> Some validators may allow the component to use attributes to define component-specific validation constraints; see the f:attribute tag. See also the \"validator\" attribute of all input components, which allows a component to specify an arbitrary validation &lt;i&gt;method&lt;/i&gt; (rather than a registered validation type, as this tag does). </p> <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p&gt;");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("validatorId", false, "jakarta.el.ValueExpression", false, false, "The registered ID of the desired Validator.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "A ValueExpression that evaluates to an implementation of the jakarta.faces.validator.Validator interface.", true, false, "jakarta.faces.validator.Validator", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("valueChangeListener");
                    tag.setTagClass("org.apache.myfaces.taglib.core.ValueChangeListenerTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Adds the specified ValueChangeListener to the nearest parent UIComponent (which is expected to be a UIInput component). <p> Whenever the form containing the parent UIComponent is submitted, an instance of the specified type is created. If the submitted value from the component is different from the component's current value then a ValueChangeEvent is queued. When the ValueChangeEvent is processed (at end of the validate phase for non-immediate components, or at end of the apply-request-values phase for immediate components) the object's processValueChange method is invoked. </p> <p> Unless otherwise specified, all attributes accept static values or EL expressions. </p>");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "jakarta.el.ValueExpression", false, false, "The name of a Java class that implements ValueChangeListener.", true, false, "java.lang.String", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("binding", false, "jakarta.el.ValueExpression", false, false, "Value binding expression that evaluates to an implementation of the jakarta.faces.event.ValueChangeListener interface.", true, false, "jakarta.faces.event.ValueChangeListener", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("verbatim");
                    tag.setTagClass("org.apache.myfaces.taglib.core.VerbatimTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escape", false, "jakarta.el.ValueExpression", false, false, "If true, generated markup is escaped. Default: false.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("rendered", false, "jakarta.el.ValueExpression", false, false, "Flag indicating whether or not this component should be rendered (during Render Response Phase), or processed on any subsequent form submit. The default value for this property is true.", true, false, "java.lang.Boolean", "null");
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                taglibXml.getListeners().add("org.apache.myfaces.webapp.StartupServletContextListener");
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
        }
        if (JSTL_URL != null) {
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/fmt.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsp/jstl/fmt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("fmt");
                taglibXml.setUri("http://java.sun.com/jsp/jstl/fmt");
                taglibXml.setInfo("JSTL 1.1 i18n-capable formatting library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlFmtTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("requestEncoding");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.RequestEncodingTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets the request character encoding");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Name of character encoding to be applied when decoding request parameters.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setLocale");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetLocaleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given locale in the locale configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "A String value is interpreted as the printable representation of a locale, which must contain a two-letter (lower-case) language code (as defined by ISO-639), and may contain a two-letter (upper-case) country code (as defined by ISO-3166). Language and country codes must be separated by hyphen (-) or underscore (_).", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("variant", false, null, true, false, "Vendor- or browser-specific variant. See the java.util.Locale javadocs for more information on variants.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of the locale configuration variable.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("timeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.TimeZoneTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Specifies the time zone for any time formatting or parsing actions         nested in its body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "The time zone. A String value is interpreted as a time zone ID. This may be one of the time zone IDs supported by the Java platform (such as \"America/Los_Angeles\") or a custom time zone ID (such as \"GMT-8\"). See java.util.TimeZone for more information on supported time zone formats.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setTimeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetTimeZoneTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given time zone in the time zone configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "The time zone. A String value is interpreted as a time zone ID. This may be one of the time zone IDs supported by the Java platform (such as \"America/Los_Angeles\") or a custom time zone ID (such as \"GMT-8\"). See java.util.TimeZone for more information on supported time zone formats.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the time zone of type java.util.TimeZone.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var or the time zone configuration variable.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("bundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.BundleTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Loads a resource bundle to be used by its tag body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, null, true, false, "Resource bundle base name. This is the bundle's fully-qualified resource name, which has the same form as a fully-qualified class name, that is, it uses \".\" as the package component separator and does not have any file type (such as \".class\" or \".properties\") suffix.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("prefix", false, null, true, false, "Prefix to be prepended to the value of the message key of any nested <fmt:message> action.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setBundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetBundleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Loads a resource bundle and stores it in the named scoped variable or         the bundle configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, null, true, false, "Resource bundle base name. This is the bundle's fully-qualified resource name, which has the same form as a fully-qualified class name, that is, it uses \".\" as the package component separator and does not have any file type (such as \".class\" or \".properties\") suffix.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the i18n localization context of type jakarta.servlet.jsp.jstl.fmt.LocalizationC ontext.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var or the localization context configuration variable.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("message");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.MessageTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Maps key to localized message and performs parametric replacement");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("key", false, null, true, false, "Message key to be looked up.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bundle", false, null, true, false, "Localization context in whose resource bundle the message key is looked up.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the localized message.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Supplies an argument for parametric replacement to a containing         <message> tag");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Argument used for parametric replacement.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.FormatNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Formats a numeric value as a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Numeric value to be formatted.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, "Specifies whether the value is to be formatted as number, currency, or percentage.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, "Custom formatting pattern.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencyCode", false, null, true, false, "ISO 4217 currency code. Applied only when formatting currencies (i.e. if type is equal to \"currency\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencySymbol", false, null, true, false, "Currency symbol. Applied only when formatting currencies (i.e. if type is equal to \"currency\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("groupingUsed", false, null, true, false, "Specifies whether the formatted output will contain any grouping separators.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxIntegerDigits", false, null, true, false, "Maximum number of digits in the integer portion of the formatted output.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minIntegerDigits", false, null, true, false, "Minimum number of digits in the integer portion of the formatted output.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxFractionDigits", false, null, true, false, "Maximum number of digits in the fractional portion of the formatted output.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minFractionDigits", false, null, true, false, "Minimum number of digits in the fractional portion of the formatted output.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the formatted result as a String.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParseNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "String to be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, "Specifies whether the string in the value attribute should be parsed as a number, currency, or percentage.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, "Custom formatting pattern that determines how the string in the value attribute is to be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, null, true, false, "Locale whose default formatting pattern (for numbers, currencies, or percentages, respectively) is to be used during the parse operation, or to which the pattern specified via the pattern attribute (if present) is applied.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("integerOnly", false, null, true, false, "Specifies whether just the integer portion of the given value should be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the parsed result (of type java.lang.Number).", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.FormatDateTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Formats a date and/or time using the supplied styles and pattern");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "Date and/or time to be formatted.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, "Specifies whether the time, the date, or both the time and date components of the given date are to be formatted.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, null, true, false, "Predefined formatting style for dates. Follows the semantics defined in class java.text.DateFormat. Applied only when formatting a date or both a date and time (i.e. if type is missing or is equal to \"date\" or \"both\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, null, true, false, "Predefined formatting style for times. Follows the semantics defined in class java.text.DateFormat. Applied only when formatting a time or both a date and time (i.e. if type is equal to \"time\" or \"both\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, "Custom formatting style for dates and times.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, null, true, false, "Time zone in which to represent the formatted time.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable which stores the formatted result as a String.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParseDateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a date and/or time");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Date string to be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, "Specifies whether the date string in the value attribute is supposed to contain a time, a date, or both.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, null, true, false, "Predefined formatting style for days which determines how the date component of the date string is to be parsed. Applied only when formatting a date or both a date and time (i.e. if type is missing or is equal to \"date\" or \"both\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, null, true, false, "Predefined formatting styles for times which determines how the time component in the date string is to be parsed. Applied only when formatting a time or both a date and time (i.e. if type is equal to \"time\" or \"both\"); ignored otherwise.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, "Custom formatting pattern which determines how the date string is to be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, null, true, false, "Time zone in which to interpret any time information in the date string.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, null, true, false, "Locale whose predefined formatting styles for dates and times are to be used during the parse operation, or to which the pattern specified via the pattern attribute (if present) is applied.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable in which the parsing result (of type java.util.Date) is stored.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/fmt-1_0.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/fmt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("fmt");
                taglibXml.setUri("http://java.sun.com/jstl/fmt");
                taglibXml.setInfo("JSTL 1.0 i18n-capable formatting library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlFmtTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("expressionAttributes", "requestEncoding:value\nsetLocale:value\nsetLocale:variant\ntimeZone:value\nsetTimeZone:value\nbundle:basename\nbundle:prefix\n" +
                    "setBundle:basename\nmessage:key\nmessage:bundle\nparam:value\nformatNumber:value\nformatNumber:pattern\nformatNumber:currencyCode" +
                    "\nformatNumber:currencySymbol\nformatNumber:groupingUsed\nformatNumber:maxIntegerDigits\nformatNumber:minIntegerDigits\n" +
                    "formatNumber:maxFractionDigits\nformatNumber:minFractionDigits\nparseNumber:value\nparseNumber:pattern\nparseNumber:parseLocale" +
                    "\nparseNumber:integerOnly\nformatDate:value\nformatDate:pattern\nformatDate:timeZone\nparseDate:value\nparseDate:pattern\n" +
                    "parseDate:timeZone\nparseDate:parseLocale");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("requestEncoding");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.RequestEncodingTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets the request character encoding");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setLocale");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.SetLocaleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given locale in the locale configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("variant", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("timeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.TimeZoneTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Specifies the time zone for any time formatting or parsing actions         nested in its body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setTimeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.SetTimeZoneTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given time zone in the time zone configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("bundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.BundleTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Loads a resource bundle to be used by its tag body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("prefix", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setBundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.SetBundleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Loads a resource bundle and stores it in the named scoped variable or         the bundle configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("message");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.MessageTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Maps key to localized message and performs parametric replacement");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("key", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bundle", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Supplies an argument for parametric replacement to a containing         <message> tag");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.FormatNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Formats a numeric value as a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencyCode", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencySymbol", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("groupingUsed", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxIntegerDigits", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minIntegerDigits", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxFractionDigits", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minFractionDigits", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.ParseNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("integerOnly", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.FormatDateTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Formats a date and/or time using the supplied styles and pattern");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.fmt.ParseDateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a date and/or time");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/sql.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsp/jstl/sql", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("sql");
                taglibXml.setUri("http://java.sun.com/jsp/jstl/sql");
                taglibXml.setInfo("JSTL 1.1 sql library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlSqlTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transaction");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.TransactionTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Provides nested database action elements with a shared Connection,         set up to execute all statements as one transaction.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, "DataSource associated with the database to access. A String value represents a relative path to a JNDI resource or the parameters for the JDBC DriverManager facility.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("isolation", false, null, true, false, "Transaction isolation level. If not specified, it is the isolation level the DataSource has been configured with.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("query");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.QueryTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL query defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, "Name of the exported scoped variable for the query result. The type of the scoped variable is jakarta.servlet.jsp.jstl.sql. Result (see Chapter 16 \"Java APIs\").", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, null, true, false, "SQL query statement.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, "Data source associated with the database to query. A String value represents a relative path to a JNDI resource or the parameters for the DriverManager class.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("startRow", false, null, true, false, "The returned Result object includes the rows starting at the specified index. The first row of the original query result set is at index 0. If not specified, rows are included starting from the first row at index 0.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxRows", false, null, true, false, "The maximum number of rows to be included in the query result. If not specified, or set to -1, no limit on the maximum number of rows is enforced.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("update");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.UpdateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL update defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the result of the database update. The type of the scoped variable is java.lang.Integer.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope of var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, null, true, false, "SQL update statement.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, "Data source associated with the database to update. A String value represents a relative path to a JNDI resource or the parameters for the JDBC DriverManager class.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified value.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Parameter value.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("dateParam");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.DateParamTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified java.util.Date value.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "Parameter value for DATE, TIME, or TIMESTAMP column in a database table.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, "One of \"date\", \"time\" or \"timestamp\".", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setDataSource");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.SetDataSourceTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a simple DataSource suitable only for prototyping.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the data source specified. Type can be String or DataSource.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "If var is specified, scope of the exported variable. Otherwise, scope of the data source configuration variable.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, "Data source. If specified as a string, it can either be a relative path to a JNDI resource, or a JDBC parameters string as defined in Section 10.1.1.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("driver", false, null, true, false, "JDBC parameter: driver class name.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, null, true, false, "JDBC parameter: URL associated with the database.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("user", false, null, true, false, "JDBC parameter: database user on whose behalf the connection to the database is being made.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("password", false, null, true, false, "JDBC parameter: user password", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/x-1_0.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/xml", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("x");
                taglibXml.setUri("http://java.sun.com/jstl/xml");
                taglibXml.setInfo("JSTL 1.0 XML library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlXmlTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("expressionAttributes", "out:escapeXml\nparse:xml\nparse:systemId\nparse:filter\n" +
                    "transform:xml\ntransform:xmlSystemId\ntransform:xslt\ntransform:xsltSystemId\ntransform:result");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for         mutually exclusive conditional operations, marked by         <when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.xml.ExprTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Like <%= ... >, but for XPath expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML conditional tag, which evalutes its body if the       supplied XPath expression evalutes to 'true' as a boolean");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.ForEachTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML iteration tag.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags \tand runs only if all of the prior conditions evaluated to \t'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.xml.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'transform' tag's Transformer");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parse");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.xml.ParseTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlParseTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses XML content from 'source' attribute or 'body'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varDom", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scopeDom", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("systemId", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("filter", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.SetTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Saves the result of an XPath expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transform");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.xml.TransformTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlTransformTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Conducts a transformation given a source XML document \tand an XSLT stylesheet");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("result", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xmlSystemId", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xslt", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xsltSystemId", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its         expression evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/sql-1_0-rt.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/sql_rt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("sql_rt");
                taglibXml.setUri("http://java.sun.com/jstl/sql_rt");
                taglibXml.setInfo("JSTL 1.0 sql library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlSqlTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transaction");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.TransactionTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Provides nested database action elements with a shared Connection,         set up to execute all statements as one transaction.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("isolation", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("query");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.QueryTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL query defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("startRow", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxRows", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("update");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.UpdateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL update defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified value.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("dateParam");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.DateParamTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified java.util.Date value.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setDataSource");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.sql.SetDataSourceTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a simple DataSource suitable only for prototyping.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("driver", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("user", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("password", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/sql-1_0.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/sql", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("sql");
                taglibXml.setUri("http://java.sun.com/jstl/sql");
                taglibXml.setInfo("JSTL 1.0 sql library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlSqlTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("expressionAttributes", "transaction:dataSource         transaction:isolation         query:sql         query:dataSource         query:startRow         query:maxRows         update:sql         update:dataSource         param:value         dateParam:value         dateParam:type         setDataSource:dataSource         setDataSource:driver         setDataSource:url         setDataSource:user         setDataSource:password");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transaction");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.TransactionTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Provides nested database action elements with a shared Connection,         set up to execute all statements as one transaction.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("isolation", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("query");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.QueryTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL query defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("startRow", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxRows", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("update");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.UpdateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Executes the SQL update defined in its body or through the         sql attribute.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("sql", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified value.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("dateParam");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.DateParamTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets a parameter in an SQL statement to the specified java.util.Date val ue.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setDataSource");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.sql.SetDataSourceTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Creates a simple DataSource suitable only for prototyping.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dataSource", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("driver", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("user", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("password", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/c-1_0.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/core", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("c");
                taglibXml.setUri("http://java.sun.com/jstl/core");
                taglibXml.setInfo("JSTL 1.0 core library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlCoreTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("expressionAttributes", "out:value\nout:default\nout:escapeXml\nif:test\n" +
                    "import:url\nimport:context\nimport:charEncoding\nforEach:items\nforEach:begin\nforEach:end\nforEach:step\n" +
                    "forTokens:items\nforTokens:begin\nforTokens:end\nforTokens:step\nparam:encode\nparam:name\nparam:value\nredirect:context" +
                    "\nredirect:url\nset:property\nset:target\nset:value\nurl:context\nurl:value\nwhen:test");
                {
                    final TagXml tag = new TagXml();
                    tag.setName("catch");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.CatchTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Catches any Throwable that occurs in its body and optionally         exposes it.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for         mutually exclusive conditional operations, marked by         <when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.OutTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Like <%= ... >, but for expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("default", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag, which evalutes its body if the         supplied condition is true and optionally exposes a Boolean         scripting variable representing the evaluation of this condition");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("import");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.ImportTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ImportTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Retrieves an absolute or relative URL and exposes its contents \tto either the page, a String in 'var', or a Reader in 'varReader'.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varReader", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("charEncoding", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.ForEachTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ForEachTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("The basic iteration tag, accepting many different         collection types and supporting subsetting and other         functionality");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forTokens");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.ForTokensTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Iterates over tokens, separated by the supplied delimeters");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("delims", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags \tand runs only if all of the prior conditions evaluated to \t'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'import' tag's URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("redirect");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.RedirectTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Redirects to a new URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("remove");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.RemoveTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Removes a scoped variable (from a particular scope, if specified).");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.SetTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets the result of an expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("property", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("url");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.UrlTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Prints or exposes a URL with optional query parameters         (via the c:param tag).");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.el.core.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its         condition evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/scriptfree.tld");
                URI_TLD_RESOURCE.put("http://jakarta.apache.org/taglibs/standard/scriptfree", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("scriptfree");
                taglibXml.setUri("http://jakarta.apache.org/taglibs/standard/scriptfree");
                taglibXml.setInfo("Validates JSP pages to prohibit use of scripting elements.");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("jakarta.servlet.jsp.jstl.tlv.ScriptFreeTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("allowExpressions", "false");
                taglibXml.getValidator().addInitParam("allowDeclarations", "false");
                taglibXml.getValidator().addInitParam("allowScriptlets", "false");
                taglibXml.getValidator().addInitParam("allowRTExpressions", "false");
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/fmt-1_0-rt.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/fmt_rt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("fmt_rt");
                taglibXml.setUri("http://java.sun.com/jstl/fmt_rt");
                taglibXml.setInfo("JSTL 1.0 i18n-capable formatting library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlFmtTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("requestEncoding");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.RequestEncodingTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Sets the request character encoding");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setLocale");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetLocaleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given locale in the locale configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("variant", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("timeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.TimeZoneTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Specifies the time zone for any time formatting or parsing actions         nested in its body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setTimeZone");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetTimeZoneTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Stores the given time zone in the time zone configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("bundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.BundleTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Loads a resource bundle to be used by its tag body");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("prefix", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("setBundle");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.SetBundleTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Loads a resource bundle and stores it in the named scoped variable or         the bundle configuration variable");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("basename", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("message");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.MessageTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Maps key to localized message and performs parametric replacement");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("key", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("bundle", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Supplies an argument for parametric replacement to a containing         <message> tag");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.FormatNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Formats a numeric value as a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencyCode", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("currencySymbol", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("groupingUsed", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxIntegerDigits", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minIntegerDigits", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("maxFractionDigits", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("minFractionDigits", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseNumber");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParseNumberTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a number, currency, or percentage");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("integerOnly", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("formatDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.FormatDateTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Formats a date and/or time using the supplied styles and pattern");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parseDate");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.fmt.ParseDateTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses the string representation of a date and/or time");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("type", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("dateStyle", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeStyle", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("pattern", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("timeZone", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("parseLocale", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/x.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsp/jstl/xml", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("x");
                taglibXml.setUri("http://java.sun.com/jsp/jstl/xml");
                taglibXml.setInfo("JSTL 1.1 XML library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlXmlTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for         mutually exclusive conditional operations, marked by         <when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ExprTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Like <%= ... >, but for XPath expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, "XPath expression to be evaluated.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, null, true, false, "Determines whether characters <,>,&,',\" in the resulting string should be converted to their corresponding character entity codes. Default value is true.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML conditional tag, which evalutes its body if the         supplied XPath expression evalutes to 'true' as a boolean");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, "The test condition that tells whether or not the body content should be processed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the resulting value of the test condition. The type of the scoped variable is Boolean.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.ForEachTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML iteration tag.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the current item of the iteration. This scoped variable has nested visibility. Its type depends on the result of the XPath expression in the select attribute.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, "XPath expression to be evaluated.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "int", true, false, "Iteration begins at the item located at the specified index. First item of the collection has index 0.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "int", true, false, "Iteration ends at the item located at the specified index (inclusive).", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "int", true, false, "Iteration will only process every step items of the collection, starting with the first one.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, "Name of the exported scoped variable for the status of the iteration. Object exported is of type jakarta.servlet.jsp.jstl.core.LoopTagStatus. This scoped variable has nested visibility.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags \tand runs only if all of the prior conditions evaluated to \t'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'transform' tag's Transformer");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, null, true, false, "Name of the transformation parameter.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Value of the parameter.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parse");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ParseTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlParseTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses XML content from 'source' attribute or 'body'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the parsed XML document. The type of the scoped variable is implementation dependent.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varDom", false, "java.lang.String", false, false, "Name of the exported scoped variable for the parsed XML document. The type of the scoped variable is org.w3c.dom.Document.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scopeDom", false, "java.lang.String", false, false, "Scope for varDom.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, null, true, false, "Deprecated. Use attribute 'doc' instead.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("doc", false, null, true, false, "Source XML document to be parsed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("systemId", false, null, true, false, "The system identifier (URI) for parsing the XML document.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("filter", false, null, true, false, "Filter to be applied to the source document.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.SetTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Saves the result of an XPath expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, "Name of the exported scoped variable to hold the value specified in the action. The type of the scoped variable is whatever type the select expression evaluates to.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", false, "java.lang.String", false, false, "XPath expression to be evaluated.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transform");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.TransformTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlTransformTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Conducts a transformation given a source XML document \tand an XSLT stylesheet");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the transformed XML document. The type of the scoped variable is org.w3c.dom.Document.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("result", false, null, true, false, "Result Object that captures or processes the transformation result.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, null, true, false, "Deprecated. Use attribute 'doc' instead.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("doc", false, null, true, false, "Source XML document to be transformed. (If exported by <x:set>, it must correspond to a well-formed XML document, not a partial document.)", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xmlSystemId", false, null, true, false, "Deprecated. Use attribute 'docSystemId' instead.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("docSystemId", false, null, true, false, "The system identifier (URI) for parsing the XML document.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xslt", false, null, true, false, "javax.xml.transform.Source Transformation stylesheet as a String, Reader, or Source object.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xsltSystemId", false, null, true, false, "The system identifier (URI) for parsing the XSLT stylesheet.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its         expression evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, "The test condition that tells whether or not the body content should be processed", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/c.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsp/jstl/core", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.1");
                taglibXml.setShortName("c");
                taglibXml.setUri("http://java.sun.com/jsp/jstl/core");
                taglibXml.setInfo("JSTL 1.1 core library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlCoreTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("catch");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.CatchTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Catches any Throwable that occurs in its body and optionally         exposes it.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the exception thrown from a nested action. The type of the scoped variable is the type of the exception thrown.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for \tmutually exclusive conditional operations, marked by \t<when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag, which evalutes its body if the \tsupplied condition is true and optionally exposes a Boolean \tscripting variable representing the evaluation of this condition");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "boolean", true, false, "The test condition that determines whether or not the body content should be processed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the resulting value of the test condition. The type of the scoped variable is Boolean.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("import");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ImportTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ImportTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Retrieves an absolute or relative URL and exposes its contents         to either the page, a String in 'var', or a Reader in 'varReader'.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", true, null, true, false, "The URL of the resource to import.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the resource's content. The type of the scoped variable is String.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varReader", false, "java.lang.String", false, false, "Name of the exported scoped variable for the resource's content. The type of the scoped variable is Reader.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, "Name of the context when accessing a relative URL resource that belongs to a foreign context.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("charEncoding", false, null, true, false, "Character encoding of the content at the input resource.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ForEachTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ForEachTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("The basic iteration tag, accepting many different         collection types and supporting subsetting and other         functionality");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", false, "jakarta.el.ValueExpression", true, false, "Collection of items to iterate over.", true, false, "java.lang.Object", null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "int", true, false, "If items specified: Iteration begins at the item located at the specified index. First item of the collection has index 0. If items not specified: Iteration begins with index set at the value specified.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "int", true, false, "If items specified: Iteration ends at the item located at the specified index (inclusive). If items not specified: Iteration ends when index reaches the value specified.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "int", true, false, "Iteration will only process every step items of the collection, starting with the first one.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the current item of the iteration. This scoped variable has nested visibility. Its type depends on the object of the underlying collection.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, "Name of the exported scoped variable for the status of the iteration. Object exported is of type jakarta.servlet.jsp.jstl.core.LoopTagStatus. This scoped variable has nested visibility.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forTokens");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ForTokensTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Iterates over tokens, separated by the supplied delimeters");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", true, "jakarta.el.ValueExpression", true, false, "String of tokens to iterate over.", true, false, "java.lang.String", null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("delims", true, "java.lang.String", true, false, "The set of delimiters (the characters that separate the tokens in the string).", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "int", true, false, "Iteration begins at the token located at the specified index. First token has index 0.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "int", true, false, "Iteration ends at the token located at the specified index (inclusive).", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "int", true, false, "Iteration will only process every step tokens of the string, starting with the first one.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the current item of the iteration. This scoped variable has nested visibility.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, "Name of the exported scoped variable for the status of the iteration. Object exported is of type jakarta.servlet.jsp.jstl.core.LoopTag Status. This scoped variable has nested visibility.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.OutTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Like <%= ... >, but for expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, "Expression to be evaluated.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("default", false, null, true, false, "Default value if the resulting value is null.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, null, true, false, "Determines whether characters <,>,&,',\" in the resulting string should be converted to their corresponding character entity codes. Default value is true.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags         and runs only if all of the prior conditions evaluated to         'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'import' tag's URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, null, true, false, "Name of the query string parameter.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "Value of the parameter.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("redirect");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.RedirectTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Redirects to a new URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, null, true, false, "The URL of the resource to redirect to.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, "Name of the context when redirecting to a relative URL resource that belongs to a foreign context.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("remove");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.RemoveTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Removes a scoped variable (from a particular scope, if specified).");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, "Name of the scoped variable to be removed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.SetTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets the result of an expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable to hold the value specified in the action. The type of the scoped variable is whatever type the value expression evaluates to.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, "jakarta.el.ValueExpression", true, false, "Expression to be evaluated.", true, false, "java.lang.Object", null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, null, true, false, "Target object whose property will be set. Must evaluate to a JavaBeans object with setter property property, or to a java.util.Map object.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("property", false, null, true, false, "Name of the property to be set in the target object.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("url");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.UrlTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Creates a URL with optional query parameters.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, "Name of the exported scoped variable for the processed url. The type of the scoped variable is String.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, "Scope for var.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, "URL to be processed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, "Name of the context when specifying a relative URL resource that belongs to a foreign context.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its \tcondition evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "boolean", true, false, "The test condition that determines whether or not the body content should be processed.", false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/c-1_0-rt.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/core_rt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("c_rt");
                taglibXml.setUri("http://java.sun.com/jstl/core_rt");
                taglibXml.setInfo("JSTL 1.0 core library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlCoreTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("catch");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.CatchTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Catches any Throwable that occurs in its body and optionally         exposes it.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for \tmutually exclusive conditional operations, marked by \t<when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag, which evalutes its body if the \tsupplied condition is true and optionally exposes a Boolean \tscripting variable representing the evaluation of this condition");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "boolean", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("import");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ImportTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ImportTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Retrieves an absolute or relative URL and exposes its contents         to either the page, a String in 'var', or a Reader in 'varReader'.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varReader", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("charEncoding", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ForEachTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.ForEachTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("The basic iteration tag, accepting many different         collection types and supporting subsetting and other         functionality");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", false, "java.lang.Object", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forTokens");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ForTokensTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Iterates over tokens, separated by the supplied delimeters");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("items", true, "java.lang.String", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("delims", true, "java.lang.String", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("begin", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("end", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("step", false, "int", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varStatus", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.OutTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Like <%= ... >, but for expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("default", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags         and runs only if all of the prior conditions evaluated to         'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'import' tag's URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("redirect");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.RedirectTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Redirects to a new URL.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("url", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("remove");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.RemoveTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Removes a scoped variable (from a particular scope, if specified).");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.SetTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Sets the result of an expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("target", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("property", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("url");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.UrlTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Creates a URL with optional query parameters.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("context", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.core.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its \tcondition evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("test", true, "boolean", true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/x-1_0-rt.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jstl/xml_rt", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.0");
                taglibXml.setJspVersion("1.2");
                taglibXml.setShortName("x_rt");
                taglibXml.setUri("http://java.sun.com/jstl/xml_rt");
                taglibXml.setInfo("JSTL 1.0 XML library");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("org.apache.taglibs.standard.tlv.JstlXmlTLV");
                taglibXml.setValidator(validator);
                {
                    final TagXml tag = new TagXml();
                    tag.setName("choose");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.ChooseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Simple conditional tag that establishes a context for         mutually exclusive conditional operations, marked by         <when> and <otherwise>");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("out");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ExprTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Like <%= ... >, but for XPath expressions.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("escapeXml", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("if");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.IfTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML conditional tag, which evalutes its body if the         supplied XPath expression evalutes to 'true' as a boolean");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("forEach");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.ForEachTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("XML iteration tag.");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("otherwise");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.core.OtherwiseTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that follows <when> tags \tand runs only if all of the prior conditions evaluated to \t'false'");
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("param");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ParamTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Adds a parameter to a containing 'transform' tag's Transformer");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("name", true, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("value", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("parse");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.ParseTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlParseTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Parses XML content from 'source' attribute or 'body'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("varDom", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scopeDom", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("systemId", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("filter", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("set");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.SetTag");
                    tag.setBodyContent("empty");
                    tag.setInfo("Saves the result of an XPath expression evaluation in a 'scope'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("transform");
                    tag.setTagClass("org.apache.taglibs.standard.tag.rt.xml.TransformTag");
                    tag.setTeiClass("org.apache.taglibs.standard.tei.XmlTransformTEI");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Conducts a transformation given a source XML document \tand an XSLT stylesheet");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("var", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("scope", false, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("result", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xml", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xmlSystemId", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xslt", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("xsltSystemId", false, null, true, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                {
                    final TagXml tag = new TagXml();
                    tag.setName("when");
                    tag.setTagClass("org.apache.taglibs.standard.tag.common.xml.WhenTag");
                    tag.setBodyContent("JSP");
                    tag.setInfo("Subtag of <choose> that includes its body if its         expression evalutes to 'true'");
                    {
                        final TagAttributeInfo attr = new TagAttributeInfo("select", true, "java.lang.String", false, false, null, false, false, null, null);
                        tag.getAttributes().add(attr);
                    }
                    taglibXml.addTag(tag);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/fn.tld");
                URI_TLD_RESOURCE.put("http://java.sun.com/jsp/jstl/functions", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("fn");
                taglibXml.setUri("http://java.sun.com/jsp/jstl/functions");
                taglibXml.setInfo("JSTL 1.1 functions library");
                {
                    final FunctionInfo function = new FunctionInfo("contains", "org.apache.taglibs.standard.functions.Functions", "boolean contains(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("containsIgnoreCase", "org.apache.taglibs.standard.functions.Functions", "boolean containsIgnoreCase(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("endsWith", "org.apache.taglibs.standard.functions.Functions", "boolean endsWith(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("escapeXml", "org.apache.taglibs.standard.functions.Functions", "java.lang.String escapeXml(java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("indexOf", "org.apache.taglibs.standard.functions.Functions", "int indexOf(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("join", "org.apache.taglibs.standard.functions.Functions", "java.lang.String join(java.lang.String[], java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("length", "org.apache.taglibs.standard.functions.Functions", "int length(java.lang.Object)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("replace", "org.apache.taglibs.standard.functions.Functions", "java.lang.String replace(java.lang.String, java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("split", "org.apache.taglibs.standard.functions.Functions", "java.lang.String[] split(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("startsWith", "org.apache.taglibs.standard.functions.Functions", "boolean startsWith(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("substring", "org.apache.taglibs.standard.functions.Functions", "java.lang.String substring(java.lang.String, int, int)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("substringAfter", "org.apache.taglibs.standard.functions.Functions", "java.lang.String substringAfter(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("substringBefore", "org.apache.taglibs.standard.functions.Functions", "java.lang.String substringBefore(java.lang.String, java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("toLowerCase", "org.apache.taglibs.standard.functions.Functions", "java.lang.String toLowerCase(java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("toUpperCase", "org.apache.taglibs.standard.functions.Functions", "java.lang.String toUpperCase(java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                {
                    final FunctionInfo function = new FunctionInfo("trim", "org.apache.taglibs.standard.functions.Functions", "java.lang.String trim(java.lang.String)");
                    taglibXml.getFunctions().add(function);
                }
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
            {
                final TldResourcePath path = new TldResourcePath(JSTL_URL, null, "META-INF/permittedTaglibs.tld");
                URI_TLD_RESOURCE.put("http://jakarta.apache.org/taglibs/standard/permittedTaglibs", path);
                final TaglibXml taglibXml = new TaglibXml();
                taglibXml.setTlibVersion("1.1");
                taglibXml.setJspVersion("2.0");
                taglibXml.setShortName("permittedTaglibs");
                taglibXml.setUri("http://jakarta.apache.org/taglibs/standard/permittedTaglibs");
                taglibXml.setInfo("Restricts JSP pages to the JSTL tag libraries");
                final ValidatorXml validator = new ValidatorXml();
                validator.setValidatorClass("jakarta.servlet.jsp.jstl.tlv.PermittedTaglibsTLV");
                taglibXml.setValidator(validator);
                taglibXml.getValidator().addInitParam("permittedTaglibs", "http://java.sun.com/jsp/jstl/core\nhttp://java.sun.com/jsp/jstl/fmt\nhttp://java.sun.com/jsp/jstl/sql\nhttp://java.sun.com/jsp/jstl/xml");
                TLD_RESOURCE_TAG_LIB.put(path, taglibXml);
            }
        }
    }
    //CHECKSTYLE:ON

    private static URL findJar(final String s, final String api) {
        try {
            final File tomEELibJar = PATHS.findTomEELibJar(s);
            return (tomEELibJar == null ? jarLocation(TomEETldScanner.class.getClassLoader().loadClass(api)) : tomEELibJar).toURI().toURL();
        } catch (final Throwable e) {
            // no-op
        }
        return null;
    }
    // used to dump container ones when nothing is in apps
    /*
    private void sysout() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        for (final Map.Entry<String, TldResourcePath> entry : uriTldResourcePathMapParent.entrySet()) {
            System.out.println("{");
            System.out.println("final TldResourcePath path = new TldResourcePath("
                + (entry.getValue().getUrl().toExternalForm().contains("myfaces")? "MYFACES_URL" :
                (entry.getValue().getUrl().toExternalForm().contains("taglibs-standard-jstlel")? "JSTL_URL" : "???"))
                + ", null, \"" + entry.getValue().getEntryName() + "\");");
            System.out.println("URI_TLD_RESOURCE.put(\"" + entry.getKey() + "\", path);");

            final TaglibXml tagLibs = tldResourcePathTaglibXmlMapParent.get(entry.getValue());
            if (tagLibs != null) {
                System.out.println("final TaglibXml taglibXml = new TaglibXml();");
                if (tagLibs.getTlibVersion() != null) System.out.println("taglibXml.setTlibVersion(\"" + tagLibs.getTlibVersion() + "\");");
                if (tagLibs.getJspVersion() != null) System.out.println("taglibXml.setJspVersion(\"" + tagLibs.getJspVersion() + "\");");
                if (tagLibs.getShortName() != null) System.out.println("taglibXml.setShortName(\"" + tagLibs.getShortName() + "\");");
                if (tagLibs.getUri() != null) System.out.println("taglibXml.setUri(\"" + tagLibs.getUri() + "\");");
                if (tagLibs.getInfo() != null) System.out.println("taglibXml.setInfo(\"" + t(tagLibs.getInfo()) + "\");");
                if (tagLibs.getValidator() != null) {
                    System.out.println("final ValidatorXml validator = new ValidatorXml();");
                    System.out.println("validator.setValidatorClass(\"" + tagLibs.getValidator().getValidatorClass() + "\");");
                    System.out.println("taglibXml.setValidator(validator);");
                    for (final Map.Entry<String, String> param : tagLibs.getValidator().getInitParams().entrySet()) {
                        System.out.println("taglibXml.getValidator().addInitParam(\"" + param.getKey() + "\", \"" + t(param.getValue()) + "\");");
                    }
                }
                for (final TagXml xml : tagLibs.getTags()) {
                    System.out.println("{");
                    System.out.println("final TagXml tag = new TagXml();");
                    if (xml.getName() != null) System.out.println("tag.setName(\"" + xml.getName() + "\");");
                    if (xml.getTagClass() != null) System.out.println("tag.setTagClass(\"" + xml.getTagClass() + "\");");
                    if (xml.getTeiClass() != null) System.out.println("tag.setTeiClass(\"" + xml.getTeiClass() + "\");");
                    if (xml.getBodyContent() != null) System.out.println("tag.setBodyContent(\"" + xml.getBodyContent() + "\");");
                    if (xml.getDisplayName() != null) System.out.println("tag.setDisplayName(\"" + xml.getDisplayName() + "\");");
                    if (xml.getSmallIcon() != null) System.out.println("tag.setSmallIcon(\"" + xml.getSmallIcon() + "\");");
                    if (xml.getLargeIcon() != null) System.out.println("tag.setLargeIcon(\"" + xml.getLargeIcon() + "\");");
                    if (xml.getInfo() != null) System.out.println("tag.setInfo(\"" + t(xml.getInfo()) + "\");");
                    if (xml.hasDynamicAttributes()) System.out.println("tag.setDynamicAttributes(" + xml.hasDynamicAttributes() + ");");
                    for (final TagAttributeInfo attr : xml.getAttributes()) {
                        System.out.println("{");
                        System.out.println("final TagAttributeInfo attr = new TagAttributeInfo(" +
                            "\"" + attr.getName() + "\", " + attr.isRequired() + ", \"" + attr.getTypeName() + "\", "
                            + attr.canBeRequestTime() + ", " + attr.isFragment() + ", \"" + t(attr.getDescription()) + "\", "
                            + attr.isDeferredValue() + ", " + attr.isDeferredMethod() + ", \"" + attr.getExpectedTypeName() + "\", \""
                            + attr.getMethodSignature() + "\");");
                        System.out.println("tag.getAttributes().add(attr);");
                        System.out.println("}");
                    }
                    for (final TagVariableInfo var : xml.getVariables()) {
                        System.out.println("{");
                        System.out.println("final TagVariableInfo var = new TagVariableInfo(\"" + var.getNameGiven() + "\", \""
                            + var.getNameFromAttribute() + "\", \"" + var.getClassName() + "\", " + var.getDeclare() + ", " + var.getScope() + ");");
                        System.out.println("tag.getVariables().add(var);");
                        System.out.println("}");
                    }
                    System.out.println("taglibXml.addTag(tag);");
                    System.out.println("}");
                }
                for (final TagFileXml file : tagLibs.getTagFiles()) {
                    System.out.println("{");
                    System.out.println("final TagFileXml file = new TagFileXml();");
                    if (file.getName() != null) System.out.println("file.setName(\"" + file.getName() + "\");");
                    if (file.getPath() != null) System.out.println("file.setPath(\"" + file.getPath() + "\");");
                    if (file.getDisplayName() != null) System.out.println("file.setDisplayName(\"" + file.getDisplayName() + "\");");
                    if (file.getSmallIcon() != null) System.out.println("file.setSmallIcon(\"" + file.getSmallIcon() + "\");");
                    if (file.getLargeIcon() != null) System.out.println("file.setLargeIcon(\"" + file.getLargeIcon() + "\");");
                    if (file.getInfo() != null) System.out.println("file.setInfo(\"" + t(file.getInfo()) + "\");");
                    System.out.println("taglibXml.getTagFiles().add(file);");
                    System.out.println("}");
                }
                for (final String listener : tagLibs.getListeners()) {
                    System.out.println("taglibXml.getListeners().add(\"" + listener + "\");");
                }
                for (final FunctionInfo function : tagLibs.getFunctions()) {
                    System.out.println("{");
                    System.out.println("final FunctionInfo function = new FunctionInfo(\"" + function.getName() + "\", \""
                        + function.getFunctionClass() + "\", \"" + function.getFunctionSignature() + "\");");
                    System.out.println("taglibXml.getFunctions().add(function);");
                    System.out.println("}");
                }
            }

            System.out.println("TLD_RESOURCE_TAG_LIB.put(path, taglibXml);");
            System.out.println("}");
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    private String t(String info) {
        return info == null ? info : info.replace("\n", " ").replace("\"", "\\\"");
    }

    @Override
    public void scan() throws IOException, SAXException {
        super.scan();
        sysout();
    }
    */
}
