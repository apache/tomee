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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.util.CodeFormat;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.DelegatingMetaDataFactory;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.UserException;
import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;
import serp.bytecode.Project;
import serp.util.Strings;

/**
 * Generates a class appropriate for use as an application identity class.
 *
 * @author Patrick Linskey
 * @author Abe White
 */
public class ApplicationIdTool {

    public static final String TOKEN_DEFAULT = "::";

    private static final String TOKENIZER_CUSTOM = "Tokenizer";
    private static final String TOKENIZER_STD = "StringTokenizer";

    private static final Localizer _loc = Localizer.forPackage
        (ApplicationIdTool.class);

    private final Log _log;
    private final Class _type;
    private final ClassMetaData _meta;
    private boolean _abstract = false;
    private FieldMetaData[] _fields = null;
    private boolean _ignore = true;
    private File _dir = null;
    private Writer _writer = null;
    private String _code = null;
    private String _token = TOKEN_DEFAULT;
    private CodeFormat _format = null;

    /**
     * Constructs a new ApplicationIdTool capable of generating an
     * object id class for <code>type</code>.
     */
    public ApplicationIdTool(OpenJPAConfiguration conf, Class type) {
        _log = conf.getLog(OpenJPAConfiguration.LOG_ENHANCE);
        _type = type;

        MetaDataRepository repos = conf.newMetaDataRepositoryInstance();
        repos.setValidate(repos.VALIDATE_NONE);
        repos.setSourceMode(repos.MODE_MAPPING, false);
        loadObjectIds(repos, true);
        _meta = repos.getMetaData(type, null, false);
        if (_meta != null) {
            _abstract = Modifier.isAbstract(_meta.getDescribedType().
                getModifiers());
            _fields = getDeclaredPrimaryKeyFields(_meta);
        }
    }

    /**
     * Constructs a new tool instance capable of generating an
     * object id class for <code>meta</code>.
     */
    public ApplicationIdTool(OpenJPAConfiguration conf, Class type,
        ClassMetaData meta) {
        _log = conf.getLog(OpenJPAConfiguration.LOG_ENHANCE);

        _type = type;
        _meta = meta;
        if (_meta != null) {
            _abstract = Modifier.isAbstract(_meta.getDescribedType().
                getModifiers());
            _fields = getDeclaredPrimaryKeyFields(_meta);
        }
    }

    /**
     * Return metadata for primary key fields declared in the given class.
     */
    private static FieldMetaData[] getDeclaredPrimaryKeyFields
        (ClassMetaData meta) {
        if (meta.getPCSuperclass() == null)
            return meta.getPrimaryKeyFields();

        // remove the primary key fields that are not declared
        // in the current class
        FieldMetaData[] fields = meta.getPrimaryKeyFields();
        List decs = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++)
            if (fields[i].getDeclaringType() == meta.getDescribedType())
                decs.add(fields[i]);
        return (FieldMetaData[]) decs.toArray(new FieldMetaData[decs.size()]);
    }

    /**
     * Return false if this tool is configured to throw an exception on
     * an attempt to generate an id class for a type that does not use
     * application identity.
     */
    public boolean getIgnoreErrors() {
        return _ignore;
    }

    /**
     * Set to false if this tool should throw an exception on
     * an attempt to generate an id class for a type that does not use
     * application identity.
     */
    public void setIgnoreErrors(boolean ignore) {
        _ignore = ignore;
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
     * The directory to write source to. Defaults to the directory
     * of the Java file for the set type. If the given directory does not
     * match the package of the object id, the package structure will be
     * created below the directory.
     */
    public File getDirectory() {
        return _dir;
    }

    /**
     * The directory to write source to. Defaults to the directory
     * of the Java file for the set type. If the given directory does not
     * match the package of the object id, the package structure will be
     * created below the directory.
     */
    public void setDirectory(File dir) {
        _dir = dir;
    }

    /**
     * The token to use to separate stringified primary key field values.
     */
    public String getToken() {
        return _token;
    }

    /**
     * The token to use to separate stringified primary key field values.
     */
    public void setToken(String token) {
        _token = token;
    }

    /**
     * The writer to write source to, or null to write to default file.
     */
    public Writer getWriter() {
        return _writer;
    }

    /**
     * The writer to write source to, or null to write to default file.
     */
    public void setWriter(Writer writer) {
        _writer = writer;
    }

    /**
     * Return the type we are generating an application id for.
     */
    public Class getType() {
        return _type;
    }

    /**
     * Return metadata for the type we are generating an application id for.
     */
    public ClassMetaData getMetaData() {
        return _meta;
    }

    /**
     * Return the code generated for the application id, or null
     * if invalid class or the {@link #run} method has not been called.
     */
    public String getCode() {
        return _code;
    }

    /**
     * Returns true if the application identity class should be an inner class.
     */
    public boolean isInnerClass() {
        Class oidClass = _meta.getObjectIdType();
        return oidClass.getName().indexOf('$') != -1;
    }

    /**
     * Returns the short class name for the object id class.
     */
    private String getClassName() {
        if (_meta.isOpenJPAIdentity())
            return null;

        // convert from SomeClass$ID to ID
        String className = Strings.getClassName(_meta.getObjectIdType());
        if (isInnerClass())
            className = className.substring(className.lastIndexOf('$') + 1);
        return className;
    }

    /**
     * Generates the sourcecode for the application id class; returns
     * false if the class is invalid.
     */
    public boolean run() {
        if (_log.isInfoEnabled())
            _log.info(_loc.get("appid-start", _type));

        // ensure that this type is a candidate for application identity
        if (_meta == null
            || _meta.getIdentityType() != ClassMetaData.ID_APPLICATION
            || _meta.isOpenJPAIdentity()) {
            if (!_ignore)
                throw new UserException(_loc.get("appid-invalid", _type));

            // else just warn
            if (_log.isWarnEnabled())
                _log.warn(_loc.get("appid-warn", _type));
            return false;
        }

        Class oidClass = _meta.getObjectIdType();
        Class superOidClass = null;

        // allow diff oid class in subclass (horizontal)
        if (_meta.getPCSuperclass() != null) {
            superOidClass = _meta.getPCSuperclassMetaData().getObjectIdType();
            if (oidClass == null || oidClass.equals(superOidClass)) {
                // just warn
                if (_log.isWarnEnabled())
                    _log.warn(_loc.get("appid-warn", _type));
                return false;
            }
        }

        // ensure that an id class is declared
        if (oidClass == null)
            throw new UserException(_loc.get("no-id-class", _type)).
                setFatal(true);

        // ensure there is at least one pk field if we are
        // non-absract, and see if we have any byte[]
        boolean bytes = false;
        for (int i = 0; !bytes && i < _fields.length; i++)
            bytes = _fields[i].getDeclaredType() == byte[].class;

        // collect info on id type
        String className = getClassName();
        String packageName = Strings.getPackageName(oidClass);
        String packageDec = "";
        if (packageName.length() > 0)
            packageDec = "package " + packageName + ";";

        String imports = getImports();
        String fieldDecs = getFieldDeclarations();
        String constructor = getConstructor(superOidClass != null);
        String properties = getProperties();
        String fromStringCode = getFromStringCode(superOidClass != null);
        String toStringCode = getToStringCode(superOidClass != null);
        String equalsCode = getEqualsCode(superOidClass != null);
        String hashCodeCode = getHashCodeCode(superOidClass != null);

        // build the java code
        CodeFormat code = newCodeFormat();
        if (!isInnerClass() && packageDec.length() > 0)
            code.append(packageDec).afterSection();

        if (!isInnerClass() && imports.length() > 0)
            code.append(imports).afterSection();

        code.append("/**").endl().
            append(" * ").
            append(_loc.get("appid-comment-for", _type.getName())).
            endl().
            append(" *").endl().
            append(" * ").append(_loc.get("appid-comment-gen")).endl().
            append(" * ").append(getClass().getName()).endl().
            append(" */").endl();
        code.append("public ");
        if (isInnerClass())
            code.append("static ");
        code.append("class ").append(className);
        if (code.getBraceOnSameLine())
            code.append(" ");
        else
            code.endl().tab();

        if (superOidClass != null) {
            code.append("extends " + Strings.getClassName(superOidClass));
            if (code.getBraceOnSameLine())
                code.append(" ");
            else
                code.endl().tab();
        }
        code.append("implements Serializable").openBrace(1).endl();

        // if we use a byte array we need a static array for encoding to string
        if (bytes) {
            code.tab().append("private static final char[] HEX = ").
                append("new char[] {").endl();
            code.tab(2).append("'0', '1', '2', '3', '4', '5', '6', '7',").
                endl();
            code.tab(2).append("'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'").
                endl();
            code.tab().append("};").endl(2);
        }

        // static block to register class
        code.tab().append("static").openBrace(2).endl();
        code.tab(2).append("// register persistent class in JVM").endl();
        code.tab(2).append("try { Class.forName").openParen(true).
                append("\"").append(_type.getName()).append("\"").
                closeParen().append(";").append(" }").endl();
        code.tab(2).append("catch").openParen(true).
                append("Exception e").closeParen().append(" {}").endl();
        
        code.closeBrace(2);

        // field declarations
        if (fieldDecs.length() > 0)
            code.endl(2).append(fieldDecs);

        // default constructor
        code.afterSection().tab().append("public ").append(className).
            parens().openBrace(2).endl();
        code.closeBrace(2);

        // string constructor
        code.afterSection().append(constructor);

        // properties
        if (properties.length() > 0)
            code.afterSection().append(properties);

        // toString, equals, hashCode methods
        if (toStringCode.length() > 0)
            code.afterSection().append(toStringCode);
        if (hashCodeCode.length() > 0)
            code.afterSection().append(hashCodeCode);
        if (equalsCode.length() > 0)
            code.afterSection().append(equalsCode);
        if (fromStringCode.length() > 0)
            code.afterSection().append(fromStringCode);

        // if we have any byte array fields, we have to add the extra
        // methods for handling byte arrays
        if (bytes) {
            code.afterSection().append(getToBytesByteArrayCode());
            code.afterSection().append(getToStringByteArrayCode());
            code.afterSection().append(getEqualsByteArrayCode());
            code.afterSection().append(getHashCodeByteArrayCode());
        }

        // base classes might need to define a custom tokenizer
        if (superOidClass == null && getTokenizer(false) == TOKENIZER_CUSTOM)
            code.afterSection().append(getCustomTokenizerClass());

        code.endl();
        code.closeBrace(1);

        _code = code.toString();

        // if this is an inner class, then indent the entire
        // code unit one tab level
        if (isInnerClass()) {
            // indent the entire code block one level to make it
            // a propertly indented innder class
            _code = code.getTab() + Strings.replace(_code,
                J2DoPrivHelper.getLineSeparator(),
                J2DoPrivHelper.getLineSeparator() + code.getTab());
        }

        return true;
    }

    /**
     * Writes the generated code to the proper file.
     */
    public void record()
        throws IOException {
        if (_code == null)
            return;

        Writer writer = _writer;
        if (writer == null) {
            File file = getFile();
            Files.backup(file, false);
            writer = new FileWriter(file);
        }

        PrintWriter printer = new PrintWriter(writer);
        printer.print(_code);
        printer.flush();

        if (_writer == null)
            writer.close();
    }

    /**
     * Return the necessary imports for the class.
     */
    private String getImports() {
        Set pkgs = getImportPackages();

        CodeFormat imports = newCodeFormat();
        String base = Strings.getPackageName(_meta.getObjectIdType());
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
     * Returns the collection of packages that need to be imported.
     */
    public Set getImportPackages() {
        Set pkgs = new TreeSet();
        pkgs.add(Strings.getPackageName(_type));

        Class superOidClass = null;
        if (_meta != null && _meta.getPCSuperclassMetaData() != null)
            superOidClass = _meta.getPCSuperclassMetaData().getObjectIdType();
        if (superOidClass != null)
            pkgs.add(Strings.getPackageName(superOidClass));

        pkgs.add("java.io");
        pkgs.add("java.util");
        Class type;
        for (int i = 0; i < _fields.length; i++) {
            type = _fields[i].getObjectIdFieldType();
            if (type != byte[].class && type != char[].class
                && !type.getName().startsWith("java.sql.")) {
                pkgs.add(Strings.getPackageName(type));
            }
        }
        return pkgs;
    }

    /**
     * Return the code to declare all primary key fields.
     */
    private String getFieldDeclarations() {
        CodeFormat code = newCodeFormat();
        for (int i = 0; i < _fields.length; i++) {
            if (i > 0)
                code.endl();
            code.tab().append("public ").append(getTypeName(_fields[i])).
                append(" ").append(_fields[i].getName()).append(";");
        }
        return code.toString();
    }

    /**
     * Return the type name to declare the given field as.
     */
    private String getTypeName(FieldMetaData fmd) {
        Class type = fmd.getObjectIdFieldType();
        if (type == byte[].class)
            return "byte[]";
        if (type == char[].class)
            return "char[]";
        if (type.getName().startsWith("java.sql."))
            return type.getName();
        return Strings.getClassName(type);
    }

    /**
     * Return the getters and setters for all primary key fields.
     */
    private String getProperties() {
        if (AccessCode.isExplicit(_meta.getAccessType()) 
         && AccessCode.isField(_meta.getAccessType()))
            return "";

        CodeFormat code = newCodeFormat();
        String propName;
        String typeName;
        for (int i = 0; i < _fields.length; i++) {
            if (i > 0)
                code.afterSection();
            typeName = getTypeName(_fields[i]);
            propName = StringUtils.capitalize(_fields[i].getName());

            code.tab().append("public ").append(typeName).append(" ");
            if (_fields[i].getDeclaredTypeCode() == JavaTypes.BOOLEAN
                || _fields[i].getDeclaredTypeCode() == JavaTypes.BOOLEAN_OBJ)
                code.append("is");
            else
                code.append("get");
            code.append(propName).parens().openBrace(2).endl();
            code.tab(2).append("return ").append(_fields[i].getName()).
                append(";").endl();
            code.closeBrace(2);
            code.afterSection();

            code.tab().append("public void set").append(propName);
            code.openParen(true).append(typeName).append(" ").
                append(_fields[i].getName()).closeParen();
            code.openBrace(2).endl();
            code.tab(2).append("this.").append(_fields[i].getName()).
                append(" = ").append(_fields[i].getName()).append(";").
                endl();
            code.closeBrace(2);
        }
        return code.toString();
    }

    /**
     * Return the string constructor code.
     */
    private String getConstructor(boolean hasSuperclass) {
        CodeFormat code = newCodeFormat();
        code.tab().append("public ");
        code.append(getClassName());
        code.openParen(true).append("String str").closeParen();
        code.openBrace(2).endl();

        if (_fields.length != 0 || (hasSuperclass
            && _meta.getPrimaryKeyFields().length > 0)) {
            code.tab(2).append("fromString").openParen(true).
                append("str").closeParen().append(";").endl();
        }

        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Create the fromString method that parses the result of our toString
     * method. If we have superclasses with id fields, this will call
     * super.fromString() so that the parent class can parse its own fields.
     */
    private String getFromStringCode(boolean hasSuperclass) {
        // if we are below a concrete class then we cannot declare any
        // more primary key fields; thus, just use the parent invocation
        if (hasConcreteSuperclass())
            return "";
        if (_fields.length == 0)
            return "";
        hasSuperclass = hasSuperclass && getDeclaredPrimaryKeyFields
            (_meta.getPCSuperclassMetaData()).length > 0;

        String toke = getTokenizer(hasSuperclass);
        CodeFormat code = newCodeFormat();
        if (_abstract || hasSuperclass)
            code.tab().append("protected ").append(toke).
                append(" fromString");
        else
            code.tab().append("private void fromString");
        code.openParen(true).append("String str").closeParen();
        code.openBrace(2).endl();

        // if we have any Object-type fields, die immediately 
        for (int i = 0; i < _fields.length; i++) {
            if (_fields[i].getObjectIdFieldType() != Object.class)
                continue;
            code.tab(2).append("throw new UnsupportedOperationException").
                parens().append(";").endl();
            code.closeBrace(2); 
            return code.toString();
        } 

        if (toke != null) {
            code.tab(2).append(toke).append(" toke = ");
            if (hasSuperclass) {
                // call super.fromString(str) to get the tokenizer that was
                // used to parse the superclass
                code.append("super.fromString").openParen(true).
                    append("str").closeParen();
            } else {
                // otherwise construct a new tokenizer with the string
                code.append("new ").append(toke).openParen(true).
                    append("str");
                if (toke == TOKENIZER_STD)
                    code.append(", \"").append(_token).append("\"");
                code.closeParen();
            }
            code.append(";").endl();
        }

        for (int i = 0; i < _fields.length; i++) {
            if (toke != null) {
                code.tab(2).append("str = toke.nextToken").parens().
                    append(";").endl();
            }
            code.tab(2).append(getConversionCode(_fields[i], "str")).endl();
        }
        if (_abstract || hasSuperclass)
            code.tab(2).append("return toke;").endl();
        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Returns the type of tokenizer to use, or null if none.
     */
    private String getTokenizer(boolean hasSuperclass) {
        if (!_abstract && !hasSuperclass && _fields.length == 1)
            return null;
        if (_token.length() == 1)
            return TOKENIZER_STD;
        return TOKENIZER_CUSTOM;
    }

    /**
     * Get parsing code for the given field.
     */
    private String getConversionCode(FieldMetaData field, String var) {
        CodeFormat parse = newCodeFormat();
        if (field.getName().equals(var))
            parse.append("this.");
        parse.append(field.getName()).append(" = ");

        Class type = field.getObjectIdFieldType();
        if (type == Date.class) {
            parse.append("new Date").openParen(true).
                append("Long.parseLong").openParen(true).
                append(var).closeParen().closeParen();
        } else if (type == java.sql.Date.class
            || type == java.sql.Timestamp.class
            || type == java.sql.Time.class) {
            parse.append(type.getName()).append(".valueOf").openParen(true).
                append(var).closeParen();
        } else if (type == String.class)
            parse.append(var);
        else if (type == Character.class) {
            parse.append("new Character").openParen(true).append(var).
                append(".charAt").openParen(true).append(0).
                closeParen().closeParen();
        } else if (type == byte[].class)
            parse.append("toBytes").openParen(true).append(var).closeParen();
        else if (type == char[].class)
            parse.append(var).append(".toCharArray").parens();
        else if (!type.isPrimitive()) {
            parse.append("new ").append(Strings.getClassName(type)).
                openParen(true).append(var).closeParen();
        } else // primitive
        {
            switch (type.getName().charAt(0)) {
                case 'b':
                    if (type == boolean.class)
                        parse.append("\"true\".equals").openParen(true).
                            append(var).closeParen();
                    else
                        parse.append("Byte.parseByte").openParen(true).
                            append(var).closeParen();
                    break;
                case 'c':
                    parse.append(var).append(".charAt").openParen(true).
                        append(0).closeParen();
                    break;
                case 'd':
                    parse.append("Double.parseDouble").openParen(true).
                        append(var).closeParen();
                    break;
                case 'f':
                    parse.append("Float.parseFloat").openParen(true).
                        append(var).closeParen();
                    break;
                case 'i':
                    parse.append("Integer.parseInt").openParen(true).
                        append(var).closeParen();
                    break;
                case 'l':
                    parse.append("Long.parseLong").openParen(true).
                        append(var).closeParen();
                    break;
                case 's':
                    parse.append("Short.parseShort").openParen(true).
                        append(var).closeParen();
                    break;
            }
        }

        if (!type.isPrimitive() && type != byte[].class) {
            CodeFormat isNull = newCodeFormat();
            isNull.append("if").openParen(true).append("\"null\".equals").
                openParen(true).append(var).closeParen().closeParen().
                endl().tab(3);
            if (field.getName().equals(var))
                isNull.append("this.");
            isNull.append(field.getName()).append(" = null;").endl();
            isNull.tab(2).append("else").endl();
            isNull.tab(3).append(parse);
            parse = isNull;
        }

        return parse.append(";").toString();
    }

    /**
     * Return an equality method that compares all pk variables.
     * Must deal correctly with both primitives and objects.
     */
    private String getEqualsCode(boolean hasSuperclass) {
        // if we are below a concrete class then we cannot declare any
        // more primary key fields; thus, just use the parent invocation
        if (hasConcreteSuperclass() || (hasSuperclass && _fields.length == 0))
            return "";

        CodeFormat code = newCodeFormat();
        code.tab().append("public boolean equals").openParen(true).
            append("Object obj").closeParen().openBrace(2).endl();

        code.tab(2).append("if").openParen(true).
            append("this == obj").closeParen().endl();
        code.tab(3).append("return true;").endl();

        // call super.equals() if we have a superclass
        String className = getClassName();
        if (hasSuperclass) {
            code.tab(2).append("if").openParen(true).
                append("!super.equals").openParen(true).
                append("obj").closeParen().closeParen().endl();
            code.tab(3).append("return false;").endl();
        } else {
            code.tab(2).append("if").openParen(true).
                append("obj == null || obj.getClass").parens().
                append(" != ").append("getClass").parens().
                closeParen().endl();
            code.tab(3).append("return false;").endl();
        }

        String name;
        Class type;
        for (int i = 0; i < _fields.length; i++) {
            if (i == 0) {
                code.endl().tab(2).append(className).append(" other = ").
                    openParen(false).append(className).closeParen().
                    append(" obj;").endl();
            }

            // if this is not the first field, add an &&
            if (i == 0)
                code.tab(2).append("return ");
            else
                code.endl().tab(3).append("&& ");

            name = _fields[i].getName();
            type = _fields[i].getObjectIdFieldType();
            if (type.isPrimitive()) {
                code.openParen(false).append(name).append(" == ").
                    append("other.").append(name).closeParen();
            } else if (type == byte[].class) {
                code.openParen(false).append("equals").openParen(true).
                    append(name).append(", ").append("other.").
                    append(name).closeParen().closeParen();
            } else if (type == char[].class) {
                // ((name == null && other.name == null)
                //	|| (name != null && String.valueOf (name).
                //	equals (String.valueOf (other.name))))
                code.append("(").openParen(false).append(name).
                    append(" == null && other.").append(name).
                    append(" == null").closeParen().endl();
                code.tab(3).append("|| ");
                code.openParen(false).append(name).append(" != null ").
                    append("&& String.valueOf").openParen(true).append(name).
                    closeParen().append(".").endl();
                code.tab(3).append("equals").openParen(true).
                    append("String.valueOf").openParen(true).
                    append("other.").append(name).closeParen().closeParen().
                    closeParen().append(")");
            } else {
                // ((name == null && other.name == null)
                //	|| (name != null && name.equals (other.name)))
                code.append("(").openParen(false).append(name).
                    append(" == null && other.").append(name).
                    append(" == null").closeParen().endl();
                code.tab(3).append("|| ");
                code.openParen(false).append(name).append(" != null ").
                    append("&& ").append(name).append(".equals").
                    openParen(true).append("other.").append(name).
                    closeParen().closeParen().append(")");
            }
        }

        // no _fields: just return true after checking instanceof
        if (_fields.length == 0)
            code.tab(2).append("return true;").endl();
        else
            code.append(";").endl();

        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Return a hashCode method that takes into account all
     * primary key values. Must deal correctly with both primitives and objects.
     */
    private String getHashCodeCode(boolean hasSuperclass) {
        // if we are below a concrete class then we cannot declare any
        // more primary key fields; thus, just use the parent invocation
        if (hasConcreteSuperclass() || (hasSuperclass && _fields.length == 0))
            return "";

        CodeFormat code = newCodeFormat();
        code.tab().append("public int hashCode").parens().
            openBrace(2).endl();

        if (_fields.length == 0)
            code.tab(2).append("return 17;").endl();
        else if (_fields.length == 1 && !hasSuperclass) {
            code.tab(2).append("return ");
            appendHashCodeCode(_fields[0], code);
            code.append(";").endl();
        } else {
            code.tab(2).append("int rs = ");
            if (hasSuperclass) {
                // call super.hashCode() if we have a superclass
                code.append("super.hashCode").openParen(true).
                    closeParen().append(";");
            } else
                code.append("17;");
            code.endl();

            for (int i = 0; i < _fields.length; i++) {
                code.tab(2).append("rs = rs * 37 + ");
                appendHashCodeCode(_fields[i], code);
                code.append(";").endl();
            }
            code.tab(2).append("return rs;").endl();
        }
        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Return true if this class has a concrete superclass.
     */
    private boolean hasConcreteSuperclass() {
        for (ClassMetaData sup = _meta.getPCSuperclassMetaData();
            sup != null; sup = sup.getPCSuperclassMetaData()) {
            if (!Modifier.isAbstract(sup.getDescribedType().getModifiers()))
                return true;
        }
        return false;
    }

    /**
     * Append code calculating the hashcode for the given field.
     */
    private void appendHashCodeCode(FieldMetaData field, CodeFormat code) {
        String name = field.getName();
        if ("rs".equals(name))
            name = "this." + name;
        Class type = field.getObjectIdFieldType();
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                // ((name) ? 1 : 0)
                code.append("(").openParen(false).append(name).closeParen().
                    append(" ? 1 : 0").append(")");
            } else if (type == long.class) {
                // (int) (name ^ (name >>> 32))
                code.openParen(false).append("int").closeParen().
                    append(" ").openParen(false).append(name).
                    append(" ^ ").openParen(false).append(name).
                    append(" >>> 32").closeParen().closeParen();
            } else if (type == double.class) {
                // (int) (Double.doubleToLongBits (name)
                //     ^ (Double.doubleToLongBits (name) >>> 32))
                code.openParen(false).append("int").closeParen().
                    append(" ").openParen(false).
                    append("Double.doubleToLongBits").openParen(true).
                    append(name).closeParen().endl();
                code.tab(3).append("^ ").openParen(false).
                    append("Double.doubleToLongBits").openParen(true).
                    append(name).closeParen().append(" >>> 32").
                    closeParen().closeParen();
            } else if (type == float.class) {
                // Float.floatToIntBits (name)
                code.append("Float.floatToIntBits").openParen(true).
                    append(name).closeParen();
            } else if (type == int.class)
                code.append(name);
            else {
                // (int) name
                code.openParen(false).append("int").closeParen().
                    append(" ").append(name);
            }
        } else if (type == byte[].class) {
            // hashCode (name);
            code.append("hashCode").openParen(true).append(name).
                closeParen();
        } else if (type == char[].class) {
            // ((name == null) ? 0 : String.valueOf (name).hashCode ())
            code.append("(").openParen(false).append(name).
                append(" == null").closeParen().append(" ? 0 : ").
                append("String.valueOf").openParen(true).append(name).
                closeParen().append(".hashCode").parens().append(")");
        } else {
            // ((name == null) ? 0 : name.hashCode ())
            code.append("(").openParen(false).append(name).
                append(" == null").closeParen().append(" ? 0 : ").
                append(name).append(".hashCode").parens().append(")");
        }
    }

    /**
     * Return a method to create a string containing the primary key
     * values that define the application id object.
     */
    private String getToStringCode(boolean hasSuperclass) {
        // if we are below a concrete class then we cannot declare any
        // more primary key fields; thus, just use the parent invocation
        if (hasConcreteSuperclass() || (hasSuperclass && _fields.length == 0))
            return "";

        CodeFormat code = newCodeFormat();
        code.tab().append("public String toString").parens().
            openBrace(2).endl();

        String name;
        Class type;
        String appendDelimiter = "+ \"" + _token + "\" + ";
        for (int i = 0; i < _fields.length; i++) {
            // if this is not the first field, add a +
            if (i == 0) {
                code.tab(2).append("return ");

                // add in the super.toString() if we have a parent
                if (hasSuperclass && getDeclaredPrimaryKeyFields
                    (_meta.getPCSuperclassMetaData()).length > 0) {
                    code.append("super.toString").parens();
                    code.endl().tab(3).append(appendDelimiter);
                }
            } else
                code.endl().tab(3).append(appendDelimiter);

            name = _fields[i].getName();
            type = _fields[i].getObjectIdFieldType();
            if (type == String.class)
                code.append(name);
            else if (type == byte[].class)
                code.append("toString").openParen(true).
                    append(name).closeParen();
            else if (type == char[].class)
                code.openParen(true).openParen(true).append(name).
                    append(" == null").closeParen().append(" ? \"null\"").
                    append(": String.valueOf").openParen(true).
                    append(name).closeParen().closeParen();
            else if (type == Date.class)
                code.openParen(true).openParen(true).append(name).
                    append(" == null").closeParen().append(" ? \"null\"").
                    endl().tab(4).append(": String.valueOf").
                    openParen(true).append(name).append(".getTime").
                    parens().closeParen().closeParen();
            else
                code.append("String.valueOf").openParen(true).
                    append(name).closeParen();
        }

        // no fields; just use ""
        if (_fields.length == 0)
            code.tab(2).append("return \"\"");
        code.append(";").endl();
        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Code to convert a string to a byte array.
     *
     * @see org.apache.openjpa.lib.util.Base16Encoder#decode
     */
    private String getToBytesByteArrayCode() {
        CodeFormat code = newCodeFormat();
        code.tab().append("private static byte[] toBytes").openParen(true).
            append("String s").closeParen().openBrace(2).endl();

        code.tab(2).append("if").openParen(true).append("\"null\".equals").
            openParen(true).append("s").closeParen().closeParen().endl();
        code.tab(3).append("return null;").endl(2);

        code.tab(2).append("int len = s.length").parens().
            append(";").endl();
        code.tab(2).append("byte[] r = new byte[len / 2];").endl();
        code.tab(2).append("for").openParen(true).
            append("int i = 0; i < r.length; i++").closeParen().
            openBrace(3).endl();
        code.tab(3).append("int digit1 = s.charAt").openParen(true).
            append("i * 2").closeParen().append(", ").
            append("digit2 = s.charAt").openParen(true).
            append("i * 2 + 1").closeParen().append(";").endl();
        code.tab(3).append("if").openParen(true).
            append("digit1 >= '0' && digit1 <= '9'").closeParen().endl();
        code.tab(4).append("digit1 -= '0';").endl();
        code.tab(3).append("else if").openParen(true).
            append("digit1 >= 'A' && digit1 <= 'F'").closeParen().endl();
        code.tab(4).append("digit1 -= 'A' - 10;").endl();
        code.tab(3).append("if").openParen(true).
            append("digit2 >= '0' && digit2 <= '9'").closeParen().endl();
        code.tab(4).append("digit2 -= '0';").endl();
        code.tab(3).append("else if").openParen(true).
            append("digit2 >= 'A' && digit2 <= 'F'").closeParen().endl();
        code.tab(4).append("digit2 -= 'A' - 10;").endl(2);
        code.tab(3).append("r[i] = (byte) ").openParen(false).
            openParen(false).append("digit1 << 4").closeParen().
            append(" + digit2").closeParen().append(";").endl();
        code.closeBrace(3).endl();
        code.tab(2).append("return r;").endl();

        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Code to convert a byte array to a string.
     *
     * @see org.apache.openjpa.lib.util.Base16Encoder#encode
     */
    private String getToStringByteArrayCode() {
        CodeFormat code = newCodeFormat();
        code.tab().append("private static String toString").openParen(true).
            append("byte[] b").closeParen().openBrace(2).endl();

        code.tab(2).append("if").openParen(true).
            append("b == null").closeParen().endl();
        code.tab(3).append("return \"null\";").endl(2);

        code.tab(2).append("StringBuilder r = new StringBuilder").
            openParen(true).append("b.length * 2").closeParen().
            append(";").endl();
        code.tab(2).append("for").openParen(true).
            append("int i = 0; i < b.length; i++").closeParen().endl();
        code.tab(3).append("for").openParen(true).
            append("int j = 1; j >= 0; j--").closeParen().endl();
        code.tab(4).append("r.append").openParen(true).
            append("HEX[").openParen(false).append("b[i] >> ").
            openParen(false).append("j * 4").closeParen().closeParen().
            append(" & 0xF]").closeParen().append(";").endl();
        code.tab(2).append("return r.toString").parens().
            append(";").endl();

        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Code to test if two byte arrays are equal.
     */
    private String getEqualsByteArrayCode() {
        CodeFormat code = newCodeFormat();
        code.tab().append("private static boolean equals").openParen(true).
            append("byte[] b1, byte[] b2").closeParen().openBrace(2).endl();

        code.tab(2).append("if").openParen(true).
            append("b1 == null && b2 == null").closeParen().endl();
        code.tab(3).append("return true;").endl();
        code.tab(2).append("if").openParen(true).
            append("b1 == null || b2 == null").closeParen().endl();
        code.tab(3).append("return false;").endl();
        code.tab(2).append("if").openParen(true).
            append("b1.length != b2.length").closeParen().endl();
        code.tab(3).append("return false;").endl();
        code.tab(2).append("for").openParen(true).
            append("int i = 0; i < b1.length; i++").closeParen().endl();
        code.tab(3).append("if").openParen(true).
            append("b1[i] != b2[i]").closeParen().endl();
        code.tab(4).append("return false;").endl();
        code.tab(2).append("return true;").endl();

        code.closeBrace(2);
        return code.toString();
    }

    private String getHashCodeByteArrayCode() {
        CodeFormat code = newCodeFormat();
        code.tab().append("private static int hashCode").openParen(true).
            append("byte[] b").closeParen().openBrace(2).endl();

        code.tab(2).append("if").openParen(true).append("b == null").
            closeParen().endl();
        code.tab(3).append("return 0;").endl();
        code.tab(2).append("int sum = 0;").endl();
        code.tab(2).append("for").openParen(true).
            append("int i = 0; i < b.length; i++").closeParen().endl();
        code.tab(3).append("sum += b[i];").endl();
        code.tab(2).append("return sum;").endl();

        code.closeBrace(2);
        return code.toString();
    }

    /**
     * Code defining a tokenizer class.
     */
    private String getCustomTokenizerClass() {
        CodeFormat code = newCodeFormat();
        code.tab().append("protected static class ").
            append(TOKENIZER_CUSTOM).openBrace(2).endl();

        code.tab(2).append("private final String str;").endl();
        code.tab(2).append("private int last;").afterSection();

        code.tab(2).append("public Tokenizer (String str)").
            openBrace(3).endl();
        code.tab(3).append("this.str = str;").endl();
        code.closeBrace(3).afterSection();

        code.tab(2).append("public String nextToken ()").
            openBrace(3).endl();
        code.tab(3).append("int next = str.indexOf").openParen(true).
            append("\"").append(_token).append("\", last").closeParen().
            append(";").endl();
        code.tab(3).append("String part;").endl();
        code.tab(3).append("if").openParen(true).append("next == -1").
            closeParen().openBrace(4).endl();
        code.tab(4).append("part = str.substring").openParen(true).
            append("last").closeParen().append(";").endl();
        code.tab(4).append("last = str.length").parens().append(";").
            endl().closeBrace(4);
        if (!code.getBraceOnSameLine())
            code.endl().tab(3);
        else
            code.append(" ");
        code.append("else").openBrace(4).endl();
        code.tab(4).append("part = str.substring").openParen(true).
            append("last, next").closeParen().append(";").endl();
        code.tab(4).append("last = next + ").append(_token.length()).
            append(";").endl().closeBrace(4).endl();

        code.tab(3).append("return part;").endl();
        code.closeBrace(3);
        code.endl().closeBrace(2);
        return code.toString();
    }

    /**
     * Return the file that this tool should output to.
     */
    private File getFile() {
        if (_meta == null)
            return null;

        String packageName = Strings.getPackageName(_meta.getObjectIdType());
        String fileName = Strings.getClassName(_meta.getObjectIdType())
            + ".java";

        // if pc class in same package as oid class, try to find pc .java file
        File dir = null;
        if (_dir == null && Strings.getPackageName(_type).equals(packageName)) {
            dir = Files.getSourceFile(_type);
            if (dir != null)
                dir = dir.getParentFile();
        }
        if (dir == null)
            dir = Files.getPackageFile(_dir, packageName, true);
        return new File(dir, fileName);
    }

    /**
     * Return a copy of the correct code format.
     */
    private CodeFormat newCodeFormat() {
        return (_format == null) ? new CodeFormat()
            : (CodeFormat) _format.clone();
    }

    /**
     * Usage: java org.apache.openjpa.enhance.ApplicationIdTool [option]*
     * &lt;class name | .java file | .class file | .jdo file&gt;+
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file&gt;</i>: The path to a OpenJPA
     * properties file containing information as outlined in
     * {@link Configuration}; optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the standard OpenJPA {@link OpenJPAConfiguration} can be
     * set by using their names and supplying a value.</li>
     * <li><i>-directory/-d &lt;output directory&gt;</i>: Path to the base
     * source directory. The package structure will be created beneath
     * this directory if necessary. If not specified, the tool will try
     * to locate the .java file in the CLASSPATH and output to the same
     * directory; failing that, it will use the current directory as
     * the base directory.
     * <li><i>-ignoreErrors/-i &lt;true/t | false/f&gt;</i>: If false, an
     * exception will be thrown if the tool encounters any class that
     * does not use application identity or uses the identity class of
     * its superclass; defaults to true.</li>
     * <li><i>-token/-t &lt;token&gt;</i>: The token to use to separate
     * stingified primary key field values in the stringified oid.</li>
     * <li><i>-name/-n &lt;id class name&gt;</i>: The name of the identity
     * class to generate. If this option is specified, you must only
     * give a single class argument. If the class metadata names an object
     * id class, this argument is ignored.</li>
     * <li><i>-suffix/-s &lt;id class suffix&gt;</i>: A string to suffix each
     * persistent class with to create the identity class name. This is
     * overridden by <code>-name</code> or by any identity class name
     * specified in metadata.</li>
     * <li><i>-codeFormat/-cf.&lt;property name&gt; &lt; property value&gt;</i>
     * : Arguments like this will be used to configure the bean
     * properties of the internal {@link CodeFormat}.</li>
     * </ul>
     *  Each additional argument can be either the full class name of the
     * type to create an id class for, the path to the .java file for the type,
     * the path to the .class file for the type, or the path to a .jdo file
     * listing one or more types. If a .java file already exists for an
     * application id, it will be backed up to a file named
     * &lt;orig file name&gt;~.
     */
    public static void main(String[] args)
        throws IOException, ClassNotFoundException {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts)
                throws ClassNotFoundException, IOException {
                OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
                try {
                    return ApplicationIdTool.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        // START - ALLOW PRINT STATEMENTS
        if (!ret)
            System.err.println(_loc.get("appid-usage"));
        // STOP - ALLOW PRINT STATEMENTS
    }

    /**
     * Run the application id tool with the given command-line and
     * given configuration. Returns false if invalid options were given.
     */
    public static boolean run(OpenJPAConfiguration conf, String[] args,
        Options opts)
        throws IOException, ClassNotFoundException {
        Flags flags = new Flags();
        flags.ignoreErrors = opts.removeBooleanProperty
            ("ignoreErrors", "i", flags.ignoreErrors);
        flags.directory = Files.getFile(opts.removeProperty("directory", "d",
            null), null);
        flags.token = opts.removeProperty("token", "t", flags.token);
        flags.name = opts.removeProperty("name", "n", flags.name);
        flags.suffix = opts.removeProperty("suffix", "s", flags.suffix);

        // separate the properties for the customizer and code format
        Options formatOpts = new Options();
        Map.Entry entry;
        String key;
        for (Iterator itr = opts.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            key = (String) entry.getKey();
            if (key.startsWith("codeFormat.")) {
                formatOpts.put(key.substring(11), entry.getValue());
                itr.remove();
            } else if (key.startsWith("cf.")) {
                formatOpts.put(key.substring(3), entry.getValue());
                itr.remove();
            }
        }
        if (!formatOpts.isEmpty()) {
            flags.format = new CodeFormat();
            formatOpts.setInto(flags.format);
        }

        Configurations.populateConfiguration(conf, opts);
        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(ApplicationIdTool.class, null);
        return run(conf, args, flags, loader);
    }

    /**
     * Run the tool. Returns false if invalid options were given.
     */
    public static boolean run(OpenJPAConfiguration conf, String[] args,
        Flags flags, ClassLoader loader)
        throws IOException, ClassNotFoundException {
        MetaDataRepository repos = conf.newMetaDataRepositoryInstance();
        repos.setValidate(repos.VALIDATE_NONE, true);
        loadObjectIds(repos, flags.name == null && flags.suffix == null);

        Log log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
        Collection classes;
        if (args.length == 0) {
            log.info(_loc.get("running-all-classes"));
            classes = repos.loadPersistentTypes(true, loader);
        } else {
            ClassArgParser cap = conf.getMetaDataRepositoryInstance().
                getMetaDataFactory().newClassArgParser();
            cap.setClassLoader(loader);
            classes = new HashSet();
            for (int i = 0; i < args.length; i++)
                classes.addAll(Arrays.asList(cap.parseTypes(args[i])));
        }
        if (flags.name != null && classes.size() > 1)
            throw new UserException(_loc.get("name-mult-args", classes));

        ApplicationIdTool tool;
        Class cls;
        ClassMetaData meta;
        BCClassLoader bc = AccessController
            .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(new Project()));
        for (Iterator itr = classes.iterator(); itr.hasNext();) {
            cls = (Class) itr.next();
            log.info(_loc.get("appid-running", cls));

            meta = repos.getMetaData(cls, null, false);
            setObjectIdType(meta, flags, bc);

            tool = new ApplicationIdTool(conf, cls, meta);
            tool.setDirectory(flags.directory);
            tool.setIgnoreErrors(flags.ignoreErrors);
            tool.setToken(flags.token);
            tool.setCodeFormat(flags.format);
            if (tool.run()) {
                log.info(_loc.get("appid-output", tool.getFile()));
                tool.record();
            } else
                log.info(_loc.get("appid-norun"));
        }
        bc.getProject().clear();
        return true;
    }

    /**
     * Set the object id type of the given metadata.
     */
    private static void setObjectIdType(ClassMetaData meta, Flags flags,
        BCClassLoader bc)
        throws ClassNotFoundException {
        if (meta == null || (meta.getObjectIdType() != null
            && (!meta.isOpenJPAIdentity() || flags.name == null))
            || getDeclaredPrimaryKeyFields(meta).length == 0)
            return;

        Class desc = meta.getDescribedType();
        Class cls = null;
        if (flags.name != null)
            cls = loadClass(desc, flags.name, bc);
        else if (flags.suffix != null)
            cls = loadClass(desc, desc.getName() + flags.suffix, bc);
        meta.setObjectIdType(cls, false);
    }

    /**
     * Load the given class name even if it does not exist.
     */
    private static Class loadClass(Class context, String name,
        BCClassLoader bc)
        throws ClassNotFoundException {
        if (name.indexOf('.') == -1 && context.getName().indexOf('.') != -1)
            name = Strings.getPackageName(context) + "." + name;

        // first try with regular class loader
        ClassLoader loader = AccessController.doPrivileged(
            J2DoPrivHelper.getClassLoaderAction(context)); 
        if (loader == null)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction()); 
        try {
            return Class.forName(name, false, loader);
        } catch (Throwable t) {
        }

        // create class
        BCClass oid = bc.getProject().loadClass(name, null);
        oid.addDefaultConstructor();
        return Class.forName(name, false, bc);
    }

    /**
     * Tell the metadata factory to load object id classes even if they don't
     * exist.
     */
    private static void loadObjectIds(MetaDataRepository repos, boolean fatal) {
        MetaDataFactory mdf = repos.getMetaDataFactory();
        if (mdf instanceof DelegatingMetaDataFactory)
            mdf = ((DelegatingMetaDataFactory) mdf).getInnermostDelegate();
        if (mdf instanceof ObjectIdLoader)
            ((ObjectIdLoader) mdf).setLoadObjectIds();
        else if (fatal)
            throw new InvalidStateException(_loc.get("factory-not-oidloader")).
                setFatal(true);
    }

    /**
     * Run flags.
     */
    public static class Flags {

        public File directory = null;
        public boolean ignoreErrors = true;
        public String token = TOKEN_DEFAULT;
        public CodeFormat format = null;
        public String name = null;
        public String suffix = null;
    }

    /**
     * Interface implemented by metadata factories that can load non-existant
     * object id classes.
     */
    public static interface ObjectIdLoader
	{
		/**
         * Turn on the loading of all identity classes, even if they don't
         * exist.
	 	 */
		public void setLoadObjectIds ();
	}
}
