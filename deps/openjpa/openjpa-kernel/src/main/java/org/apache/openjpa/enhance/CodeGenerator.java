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
package org.apache.openjpa.enhance;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.util.CodeFormat;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.ParameterTemplate;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import serp.util.Strings;

/**
 * Generates Java class code from metadata.
 *
 * @author Abe White
 * @author Stephen Kim
 * @since 0.3.0
 * @nojavadoc
 */
public class CodeGenerator {

    private File _dir = null;
    private CodeFormat _format = null;
    private ClassMetaData _meta = null;
    private Class _type = null;
    private ParameterTemplate _code = null;

    /**
     * Constructor. Supply configuration and class to generate code for.
     */
    public CodeGenerator(OpenJPAConfiguration conf, Class type) {
        this(conf.newMetaDataRepositoryInstance().
            getMetaData(type, null, true));
    }

    /**
     * Constructor. Supply configuration and metadata to generate code for.
     */
    public CodeGenerator(ClassMetaData meta) {
        _meta = meta;
        _type = meta.getDescribedType();
    }

    /**
     * The directory to write source to. Defaults to the current directory.
     * If the given directory does not match the package of the metadata, the
     * package structure will be created below the directory.
     */
    public File getCodeDirectory() {
        return _dir;
    }

    /**
     * The directory to write source to. Defaults to the current directory.
     * If the given directory does not match the package of the metadata, the
     * package structure will be created below the directory.
     */
    public void setDirectory(File dir) {
        _dir = dir;
    }

    /**
     * The code formatter for the generated Java code.
     */
    public CodeFormat getCodeFormat() {
        return _format;
    }

    /**
     * Set the code formatter for the generated Java code.
     */
    public void setCodeFormat(CodeFormat format) {
        _format = format;
    }

    /**
     * Return the type being generated.
     */
    public Class getType() {
        return _type;
    }

    /**
     * Return metadata for the type being generated.
     */
    public ClassMetaData getMetaData() {
        return _meta;
    }

    /**
     * Return the generated code, or null if {@link #generateCode} has not
     * been called.
     */
    public String getCode() {
        return (_code == null) ? null : _code.toString();
    }

    /**
     * Writes the generated code to the proper directory.
     */
    public void generateCode() {
        // setup parameters
        String className = Strings.getClassName(_type);
        String packageName = Strings.getPackageName(_type);
        String packageDec = "";
        if (packageName.length() > 0)
            packageDec = "package " + packageName + ";";

        String extendsDec = "";
        String extendsName = "";
        if (!_type.getSuperclass().getName().equals(Object.class.getName())) {
            extendsName = Strings.getClassName(_type.getSuperclass());
            extendsDec = "extends " + extendsName;
        }

        String imports = getImports();
        String[] fieldCode = getFieldCode();
        String constructor = getConstructor();

        // get code template
        _code = new ParameterTemplate();
        String codeStr = getClassCode();
        if (codeStr != null) {
            _code.append(codeStr);
            _code.setParameter("packageDec", packageDec);
            _code.setParameter("imports", imports);
            _code.setParameter("className", className);
            _code.setParameter("extendsDec", extendsDec);
            _code.setParameter("constructor", constructor);
            _code.setParameter("fieldDecs", fieldCode[0]);
            _code.setParameter("fieldCode", fieldCode[1]);
        } else
            _code.append(getClassCode(packageDec, imports, className,
                extendsName, constructor, fieldCode[0], fieldCode[1]));
    }

    /**
     * Write the generated code to the proper file.
     */
    public void writeCode()
        throws IOException {
        if (_code == null)
            return;

        File file = getFile();
        Files.backup(file, false);
        _code.write(file);
    }

    /**
     * Write the code to the specified {@link Writer}.
     */
    public void writeCode(Writer out)
        throws IOException {
        if (_code == null)
            return;

        _code.write(out);
    }

    /**
     * Return the necessary imports for the class.
     */
    private String getImports() {
        Set pkgs = getImportPackages();

        CodeFormat imports = newCodeFormat();
        String base = Strings.getPackageName(_type);
        String pkg;
        for (Iterator itr = pkgs.iterator(); itr.hasNext();) {
            pkg = (String) itr.next();
            if (pkg.length() > 0 && !"java.lang".equals(pkg)
                && !base.equals(pkg)) {
                if (imports.length() > 0)
                    imports.endl();
                imports.append("import ").append(pkg).append(".*;");
            }
        }
        return imports.toString();
    }

    /**
     * Returns the set of packages that needs to be imported for this code.
     */
    public Set getImportPackages() {
        Set pkgs = new TreeSet();
        pkgs.add(Strings.getPackageName(_type.getSuperclass()));

        FieldMetaData[] fields = _meta.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
            pkgs.add(Strings.getPackageName(fields[i].getDeclaredType()));

        fields = _meta.getPrimaryKeyFields();
        for (int i = 0; i < fields.length; i++)
            pkgs.add(Strings.getPackageName(fields[i].getDeclaredType()));

        return pkgs;
    }

    /**
     * Return code for a primary key constructor for the given class.
     */
    private String getConstructor() {
        FieldMetaData[] fields = _meta.getPrimaryKeyFields();
        if (fields.length == 0)
            return "";

        CodeFormat cons = newCodeFormat();
        CodeFormat body = newCodeFormat();

        // public <class> (
        cons.tab().append("public ").append(Strings.getClassName(_type));
        cons.openParen(true);

        // append args to constructor, and build up body at same time
        String propertyName;
        String fieldType;
        for (int i = 0; i < fields.length; i++) {
            propertyName = fields[i].getName();
            if (propertyName.startsWith("_"))
                propertyName = propertyName.substring(1);
            fieldType = Strings.getClassName(fields[i].getDeclaredType());

            if (i > 0)
                cons.append(", ");
            cons.append(fieldType).append(" ").append(propertyName);

            if (_meta.getPCSuperclass() == null) {
                if (i > 0)
                    body.endl();
                body.tab(2);
                if (propertyName.equals(fields[i].getName()))
                    body.append("this.");
                body.append(fields[i].getName());
                body.append(" = ").append(propertyName).append(";");
            } else {
                // super (...);
                if (i == 0)
                    body.tab(2).append("super").openParen(true);
                else
                    body.append(", ");
                body.append(propertyName);
                if (i == fields.length - 1)
                    body.closeParen().append(";");
            }
        }
        cons.closeParen();

        cons.openBrace(2).endl();
        cons.append(body.toString()).endl();
        cons.closeBrace(2);
        return cons.toString();
    }

    /**
     * Returns the Java declaration and access method code for all declared
     * fields.
     */
    private String[] getFieldCode() {
        CodeFormat decs = newCodeFormat();
        CodeFormat code = newCodeFormat();

        FieldMetaData[] fields = _meta.getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
            appendFieldCode(fields[i], decs, code);
        fields = _meta.getDeclaredUnmanagedFields();
        for (int i = 0; i < fields.length; i++)
            appendFieldCode(fields[i], decs, code);
        return new String[]{ decs.toString(), code.toString() };
    }

    /**
     * Append the declaration and code for the given field to the given buffers.
     */
    private void appendFieldCode(FieldMetaData fmd, CodeFormat decs,
        CodeFormat code) {
        String fieldName = fmd.getName();
        String capFieldName = StringUtils.capitalize(fieldName);
        String propertyName = fieldName;
        if (propertyName.startsWith("_"))
            propertyName = propertyName.substring(1);
        String fieldType = Strings.getClassName(fmd.getDeclaredType());

        String keyType = null;
        String elementType = null;
        String paramType = "";
        if (useGenericCollections()) {
            if (fmd.getDeclaredTypeCode() == JavaTypes.COLLECTION) {
                Class elmCls = fmd.getElement().getDeclaredType();
                elementType = Strings.getClassName(elmCls);
                paramType = decs.getParametrizedType(
                    new String[] {elementType});
            } else if (fmd.getDeclaredTypeCode() == JavaTypes.MAP) {
                Class keyCls = fmd.getKey().getDeclaredType();
                Class elmCls = fmd.getElement().getDeclaredType();
                keyType = Strings.getClassName(keyCls);
                elementType = Strings.getClassName(elmCls);
                paramType = decs.getParametrizedType(
                    new String[] {keyType, elementType});
            }
        }

        String fieldValue = getInitialValue(fmd);
        if (fieldValue == null) {
            if ("Set".equals(fieldType))
                fieldValue = "new HashSet" + paramType + decs.getParens();
            else if ("TreeSet".equals(fieldType))
                fieldValue = "new TreeSet" + paramType + decs.getParens();
            else if ("Collection".equals(fieldType))
                fieldValue = "new ArrayList" + paramType + decs.getParens();
            else if ("Map".equals(fieldType))
                fieldValue = "new HashMap" + paramType + decs.getParens();
            else if ("TreeMap".equals(fieldType))
                fieldValue = "new TreeMap" + paramType + decs.getParens();
            else if (fmd.getDeclaredTypeCode() == JavaTypes.COLLECTION ||
                fmd.getDeclaredTypeCode() == JavaTypes.MAP)
                fieldValue = "new " + fieldType + paramType + decs.getParens();
            else
                fieldValue = "";
        }
        if (fieldValue.length() > 0)
            fieldValue = " = " + fieldValue;

        boolean fieldAccess = !usePropertyBasedAccess();
        String custom = getDeclaration(fmd);
        if (decs.length() > 0)
            decs.endl();
        ParameterTemplate templ;
        if (custom != null) {
            templ = new ParameterTemplate();
            templ.append(custom);
            templ.setParameter("fieldName", fieldName);
            templ.setParameter("capFieldName", capFieldName);
            templ.setParameter("propertyName", propertyName);
            templ.setParameter("fieldType", fieldType);
            templ.setParameter("keyType", keyType);
            templ.setParameter("elementType", elementType);
            templ.setParameter("fieldValue", fieldValue);
            decs.append(templ.toString());
        } else {
            if (fieldAccess)
                writeAnnotations(decs, getFieldAnnotations(fmd), 1);
            decs.tab().append("private ").append(fieldType).
                append(paramType).append(" ").append(fieldName).
                append(fieldValue).append(";");
            if (fieldAccess)
                decs.endl();
        }

        custom = getFieldCode(fmd);
        if (code.length() > 0)
            code.afterSection();
        if (custom != null) {
            templ = new ParameterTemplate();
            templ.append(custom);
            templ.setParameter("fieldName", fieldName);
            templ.setParameter("capFieldName", capFieldName);
            templ.setParameter("propertyName", propertyName);
            templ.setParameter("fieldType", fieldType);
            templ.setParameter("keyType", keyType);
            templ.setParameter("elementType", elementType);
            templ.setParameter("fieldValue", fieldValue);
            code.append(templ.toString());
        } else {
            // getter
            if (!fieldAccess)
                writeAnnotations(code, getFieldAnnotations(fmd), 1);
            code.tab().append("public ").append(fieldType).append(paramType).
                 append(" ");
            if ("boolean".equalsIgnoreCase(fieldType))
                code.append("is");
            else
                code.append("get");
            code.append(capFieldName).parens();
            code.openBrace(2).endl();
            code.tab(2).append("return ").append(fieldName).
                append(";").endl();
            code.closeBrace(2).afterSection();

            // setter
            code.tab().append("public void set").append(capFieldName);
            code.openParen(true).append(fieldType).append(paramType).
                append(" ").append(propertyName).closeParen();
            code.openBrace(2).endl();
            code.tab(2);
            if (propertyName.equals(fieldName))
                code.append("this.");
            code.append(fieldName).append(" = ").append(propertyName).
                append(";").endl();
            code.closeBrace(2);
        }
    }

    /**
     * Return a code template for a generated Java class.
     */
    private String getClassCode(String packageDec, String imports,
        String className, String extendsName, String constructor,
        String fieldDecs, String fieldCode) {
        CodeFormat code = newCodeFormat();
        if (packageDec.length() > 0)
            code.append(packageDec).afterSection();
        if (imports.length() > 0)
            code.append(imports).afterSection();

        code.append("/**").endl().
            append(" * Auto-generated by:").endl().
            append(" * ").append(getClass().getName()).endl().
            append(" */").endl();

        writeAnnotations(code, getClassAnnotations(), 0);
        code.append("public class ").append(className);
        if (extendsName.length() > 0)
            code.extendsDec(1).append(" ").append(extendsName);
        openClassBrace(code);

        if (fieldDecs.length() > 0)
            code.append(fieldDecs).afterSection();

        // default constructor
        code.tab().append("public ").append(className).parens();
        code.openBrace(2).endl().closeBrace(2);

        if (constructor.length() > 0)
            code.afterSection().append(constructor);
        if (fieldCode.length() > 0)
            code.afterSection().append(fieldCode);
        code.endl();

        closeClassBrace(code);

        return code.toString();
    }

    /**
     * Appends the given list of annotations to code buffer.
     */
    private void writeAnnotations (CodeFormat code, List ann,
        int tabLevel) {
        if (ann == null || ann.size() == 0)
            return;
        for (Iterator i = ann.iterator(); i.hasNext();) {
            if (tabLevel > 0)
                code.tab(tabLevel);
            String s = (String) i.next();
            code.append(s).endl();
        }
    }

    /**
     * Append the opening code-level brace to the code; this can be
     * overridden to add code to the top of the class.
     */
    protected void openClassBrace(CodeFormat code) {
        code.openBrace(1).endl();
    }

    /**
     * Append the closing code-level brace to the code; this can be
     * overridden to add code to the bottom of the class.
     */
    protected void closeClassBrace(CodeFormat code) {
        code.closeBrace(1);
    }

    /**
     * Return Java file to write to.
     */
    public File getFile() {
        String packageName = Strings.getPackageName(_type);
        String fileName = Strings.getClassName(_type) + ".java";

        File dir = Files.getPackageFile(_dir, packageName, true);
        return new File(dir, fileName);
    }

    /**
     * Return a copy of the internal code format.
     */
    protected CodeFormat newCodeFormat() {
        if (_format == null)
            return new CodeFormat();
        return (CodeFormat) _format.clone();
    }

    /**
     * Return a code template for the given class, or null to use the standard
     * system-generated Java code. To facilitate template reuse, the
     * following parameters can appear in the template; the proper values
     * will be subtituted by the system:
     * <ul>
     * <li>${packageDec}: The package declaration, in the form
     * "package &lt;package name &gt;;", or empty string if no package.</li>
     * <li>${imports}: Imports for the packages used by the declared
     * field types.</li>
     * <li>${className}: The name of the class, without package.</li>
     * <li>${extendsDec}: Extends declaration, in the form
     * "extends &lt;superclass&gt;", or empty string if no superclass.</li>
     * <li>${constructor}: A constructor that takes in all primary key fields
     * of the class, or empty string if the class uses datastore identity.</li>
     * <li>${fieldDecs}: Declarations of all the declared fields.</li>
     * <li>${fieldCode}: Get/set methods for all the declared fields.</li>
     * </ul> Returns null by default.
     */
    protected String getClassCode() {
        return null;
    }

    /**
     * Return code for the initial value for the given field, or null to use
     * the default generated by the system. Returns null by default.
     */
    protected String getInitialValue(FieldMetaData field) {
        return null;
    }

    /**
     * Return a code template for the declaration of the given field, or null
     * to use the system-generated default Java code.
     * To facilitate template reuse, the following parameters can appear in
     * your template; the proper values will be subtituted by the system:
     * <ul>
     * <li>${fieldName}: The name of the field.</li>
     * <li>${capFieldName}: The capitalized field name.</li>
     * <li>${propertyName}: The field name without leading '_', if any.</li>
     * <li>${fieldType}: The field's type name.</li>
     * <li>${keyType}: Key type name for maps, null otherwise.</li>
     * <li>${elementType}: Element type name for collections, null otherwise.
     * </li>
     * <li>${fieldValue}: The field's initial value, in the form
     * " = &lt;value&gt;", or empty string if none.</li>
     * </ul> Returns null by default.
     */
    protected String getDeclaration(FieldMetaData field) {
        return null;
    }

    /**
     * Return a code template for the get/set methods of the given field, or
     * null to use the system-generated default Java code.
     * To facilitate template reuse, the following parameters can appear in
     * your template; the proper values will be subtituted by the system:
     * <ul>
     * <li>${fieldName}: The name of the field.</li>
     * <li>${capFieldName}: The capitalized field name.</li>
     * <li>${propertyName}: The field name without leading '_', if any.</li>
     * <li>${fieldType}: The field's type name.</li>
     * <li>${keyType}: Key type name for maps, null otherwise.</li>
     * <li>${elementType}: Element type name for collections, null otherwise.
     * </li>
     * <li>${fieldValue}: The field's initial value, in the form
     * "= &lt;value&gt;", or empty string if none.</li>
     * </ul>
     */
    protected String getFieldCode (FieldMetaData field)
	{
		return null;
	}

    /**
     * Whether to use property-based access on generated code.
     * Defaults to false (field-based).
     */    
    protected boolean usePropertyBasedAccess () {
        return false;
    }

    /**
     * Return class-level annotations. Returns null by default.
     */
    protected List getClassAnnotations() {
        return null;
    }

    /**
     * Return field-level annotations. Returns null by default.
     */
    protected List getFieldAnnotations(FieldMetaData field) {
        return null;
    }

    /**
     * Whether to use generic collections on one-to-many and many-to-many
     * relations instead of untyped collections.
     *
     * Override in descendants to change default behavior.
     */
    protected boolean useGenericCollections() {
        return false;
    }

}
