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
package org.apache.openjpa.persistence.meta;

import static javax.lang.model.SourceVersion.RELEASE_6;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.persistence.metamodel.StaticMetamodel;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.persistence.PersistenceMetaDataFactory;
import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.util.SourceCode;


/**
 * Annotation processing tool generates source code for a meta-model class given 
 * the annotated source code of persistent entity.
 * <p>
 * This tool is invoked during compilation for JDK6 compiler if 
 * <UL>
 * <LI>OpenJPA and JPA libraries are available in the compiler classpath
 * and <LI>Annotation Processor option <code>-Aopenjpa.metamodel=true</code> is specified.
 * </UL>
 * <br>
 * <B>Usage</B><br>
 * <code>$ javac -classpath path/to/openjpa-all.jar -Aopenjpa.metamodel=true mypackage/MyEntity.java</code><br>
 * will generate source code for canonical meta-model class <code>mypackage.MyEntity_.java</code>.
 * The source code is generated relative to the directory specified in <code>-s</code> option
 * of <code>javac</code> compiler and defaulted to the current directory.
 * <p>
 * The Annotation Processor also recognizes the following options (none of them are mandatory):<br>
 * <TABLE border="1">
 * <TR><TD>-Aopenjpa.log={log level}<TD>The logging level. Default is <code>WARN</code>. Permissible values are 
 *     <code>TRACE</code>, <code>INFO</code>, <code>WARN</code> or <code> ERROR</code>.
 * <TR><TD>-Aopenjpa.source={n}          <TD>Java source version of the generated code. Default is <code>6</code>.
 * <TR><TD>-Aopenjpa.naming={class name} <TD>fully-qualified name of a class implementing 
 * <code>org.apache.openjpa.meta.MetaDataFactory</code> that determines
 * the name of a meta-class given the name of the original persistent Java entity class. Defaults to
 * <code>org.apache.openjpa.persistence.PersistenceMetaDataFactory</code> which appends a underscore character
 * (<code>_</code>) to the original Java class name. 
 * <TR><TD>-Aopenjpa.header={url}        <TD>
 * A url whose content will appear as comment header to the generated file(s). Recognizes special value
 * <code>ASL</code> for Apache Source License header as comment. By default adds a OpenJPA proprietary   
 * text.
 * </TABLE>
 * <br>
 *
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 * 
 */
@SupportedAnnotationTypes({ 
    "javax.persistence.Entity",
    "javax.persistence.Embeddable", 
    "javax.persistence.MappedSuperclass" })
@SupportedOptions({ "openjpa.log", 
                    "openjpa.source",
                    "openjpa.naming",
                    "openjpa.header",
                    "openjpa.metamodel"
                  })
@SupportedSourceVersion(RELEASE_6)

public class AnnotationProcessor6 extends AbstractProcessor {
    private SourceAnnotationHandler handler;
    private MetaDataFactory factory;
    private int generatedSourceVersion = 6;
    private CompileTimeLogger logger;
    private List<String> header = new ArrayList<String>();
    private boolean active;
    private static Localizer _loc =  Localizer.forPackage(AnnotationProcessor6.class);

    /**
     * Category of members as per JPA 2.0 type system.
     * 
     */
    private static enum TypeCategory {
        ATTRIBUTE("javax.persistence.metamodel.SingularAttribute"), 
        COLLECTION("javax.persistence.metamodel.CollectionAttribute"), 
        SET("javax.persistence.metamodel.SetAttribute"), 
        LIST("javax.persistence.metamodel.ListAttribute"), 
        MAP("javax.persistence.metamodel.MapAttribute");

        private String type;

        private TypeCategory(String type) {
            this.type = type;
        }

        public String getMetaModelType() {
            return type;
        }
    }
    
    /**
     * Enumerates available java.util.* collection classes to categorize them
     * into corresponding JPA meta-model member type.
     */
    private static List<String> CLASSNAMES_LIST = Arrays.asList(
        new String[]{
        "java.util.List", "java.util.AbstractList", 
        "java.util.AbstractSequentialList", "java.util.ArrayList", 
        "java.util.Stack", "java.util.Vector"});
    private static List<String> CLASSNAMES_SET = Arrays.asList(
        new String[]{
        "java.util.Set", "java.util.AbstractSet", "java.util.EnumSet", 
        "java.util.HashSet", "java.util.LinkedList", "java.util.LinkedHashSet", 
        "java.util.SortedSet", "java.util.TreeSet"});
    private static List<String> CLASSNAMES_MAP = Arrays.asList(
        new String[]{
        "java.util.Map", "java.util.AbstractMap", "java.util.EnumMap", 
        "java.util.HashMap",  "java.util.Hashtable", 
        "java.util.IdentityHashMap",  "java.util.LinkedHashMap", 
        "java.util.Properties", "java.util.SortedMap", 
        "java.util.TreeMap"});
    private static List<String> CLASSNAMES_COLLECTION = Arrays.asList(
        new String[]{
        "java.util.Collection", "java.util.AbstractCollection", 
        "java.util.AbstractQueue", "java.util.Queue", 
        "java.util.PriorityQueue"});
    
    /**
     * Gets the fully-qualified name of member class in JPA 2.0 type system,
     * given the fully-qualified name of a Java class.
     *  
     */
    private TypeCategory toMetaModelTypeCategory(TypeMirror mirror, 
        String name, boolean persistentCollection) {   
        if (mirror.getKind() == TypeKind.ARRAY && persistentCollection ) {
            return TypeCategory.LIST;
        }
        if (CLASSNAMES_COLLECTION.contains(name))
            return TypeCategory.COLLECTION;
        if (CLASSNAMES_LIST.contains(name))
            return TypeCategory.LIST;
        if (CLASSNAMES_SET.contains(name))
            return TypeCategory.SET;
        if (CLASSNAMES_MAP.contains(name))
            return TypeCategory.MAP;
        return TypeCategory.ATTRIBUTE;
    }
    
    /**
     * Initialization.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        active = "true".equalsIgnoreCase(getOptionValue("openjpa.metamodel"));
        if (!active)
            return;
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, _loc.get("mmg-tool-banner").toString());
        logger = new CompileTimeLogger(processingEnv, getOptionValue("openjpa.log"));
        setSourceVersion();
        setNamingPolicy();
        setHeader();
        handler = new SourceAnnotationHandler(processingEnv, logger);
    }
    
    /**
     * The entry point for java compiler.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annos, RoundEnvironment roundEnv) {
        if (active && !roundEnv.processingOver()) {
            Set<? extends Element> elements = roundEnv.getRootElements();
            for (Element e : elements) {
                if (e instanceof TypeElement) {
                    process((TypeElement) e);
                }
            }
        }
        return true;
    }

    /**
     * Generate meta-model source code for the given type.
     * 
     * @return true if code is generated for the given element. false otherwise.
     */
    private boolean process(TypeElement e) {
    	if (!handler.isAnnotatedAsEntity(e)) {
            return false;
        }

        Elements eUtils = processingEnv.getElementUtils();
        String originalClass = eUtils.getBinaryName((TypeElement) e).toString();
        String originalSimpleClass = e.getSimpleName().toString();
        String metaClass = factory.getMetaModelClassName(originalClass);

        SourceCode source = new SourceCode(metaClass);
        comment(source);
        annotate(source, originalClass);
        TypeElement supCls = handler.getPersistentSupertype(e);
        if (supCls != null) {
            String superName = factory.getMetaModelClassName(supCls.toString());
            source.getTopLevelClass().setSuper(superName);
        }
        try {
            PrintWriter writer = createSourceFile(originalClass, metaClass, e);
            SourceCode.Class modelClass = source.getTopLevelClass();
            Set<? extends Element> members = handler.getPersistentMembers(e);
            
            for (Element m : members) {
                boolean isPersistentCollection = m.getAnnotation(PersistentCollection.class) != null; 
                
                TypeMirror decl  = handler.getDeclaredType(m);
                String fieldName = handler.getPersistentMemberName(m);
                String fieldType = handler.getDeclaredTypeName(decl, true, isPersistentCollection);  
                TypeCategory typeCategory =
                    toMetaModelTypeCategory(decl, fieldType, isPersistentCollection);
                String metaModelType = typeCategory.getMetaModelType();
                SourceCode.Field modelField = null;
                switch (typeCategory) {
                case ATTRIBUTE:
                    modelField = modelClass.addField(fieldName, metaModelType);
                    modelField.addParameter(originalSimpleClass)
                              .addParameter(fieldType);
                    break;
                case COLLECTION:
                case LIST:
                case SET:
                    TypeMirror param   = handler.getTypeParameter(m, decl, 0, true);
                    String elementType = handler.getDeclaredTypeName(param);
                    modelField = modelClass.addField(fieldName, metaModelType);
                    modelField.addParameter(originalSimpleClass)
                              .addParameter(elementType);
                    break;
                case MAP:
                    TypeMirror key   = handler.getTypeParameter(m, decl, 0, false);
                    TypeMirror value = handler.getTypeParameter(m, decl, 1, true);
                    String keyType = handler.getDeclaredTypeName(key);
                    String valueType = handler.getDeclaredTypeName(value);
                    modelField = modelClass.addField(fieldName, metaModelType);
                    modelField.addParameter(originalSimpleClass)
                              .addParameter(keyType)
                              .addParameter(valueType);
                    break;
                }
                modelField.makePublic().makeStatic().makeVolatile();
            }
            source.write(writer);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e1) {
            logger.error(_loc.get("mmg-process-error", e.getQualifiedName()), e1);
            return false;
        } 
    }
    
    private void annotate(SourceCode source, String originalClass) {
        SourceCode.Class cls = source.getTopLevelClass();
        cls.addAnnotation(StaticMetamodel.class.getName())
            .addArgument("value", originalClass + ".class", false);
        if (generatedSourceVersion >= 6) {
            cls.addAnnotation(Generated.class.getName())
            .addArgument("value", this.getClass().getName())
            .addArgument("date", new Date().toString());
        }
    }
    
    private void comment(SourceCode source) {
        if (header.size() != 0)
            source.addComment(false, header.toArray(new String[header.size()]));
        String defaultHeader = _loc.get("mmg-tool-sign").getMessage();
        source.addComment(false, defaultHeader);
    }
    
    /**
     * Parse annotation processor option <code>-Aopenjpa.source=n</code> to detect
     * the source version for the generated classes. 
     * n must be a integer. Default or wrong specification returns 6.
     */
    private void setSourceVersion() {
        String version = getOptionValue("openjpa.source");
        if (version != null) {
            try {
                generatedSourceVersion = Integer.parseInt(version);
            } catch (NumberFormatException e) {
                logger.warn(_loc.get("mmg-bad-source", version, 6));
                generatedSourceVersion = 6;
            }
        } else {
            generatedSourceVersion = 6;
        }
    }
    
    private void setNamingPolicy() {
        String policy = getOptionValue("openjpa.naming");
        if (policy != null) {
            try {
                factory = (MetaDataFactory)Class.forName(policy).newInstance();
            } catch (Throwable e) {
                logger.warn(_loc.get("mmg-bad-naming", policy, e));
                factory = new PersistenceMetaDataFactory();
            }
        } else {
            factory = new PersistenceMetaDataFactory();
        }
    }
    
    private void setHeader() {
        String headerOption = getOptionValue("openjpa.header");
        if (headerOption == null) {
            return;
        }
        if ("ASL".equalsIgnoreCase(headerOption)) {
            header.add(_loc.get("mmg-asl-header").getMessage());
        } else {
            try {
                URL url = new URL(headerOption);
                InputStream is = url.openStream();
                Scanner s = new Scanner(is);
                while (s.hasNextLine()) {
                    header.add(s.nextLine());
                }
            } catch (Throwable t) {
                
            }
        }
    }
    
    /**
     * Creates a file where source code of the given metaClass will be written.
     * 
     */
    private PrintWriter createSourceFile(String originalClass, String metaClass, TypeElement e) 
        throws IOException {
        JavaFileObject javaFile = processingEnv.getFiler().createSourceFile(metaClass, e);
        logger.info(_loc.get("mmg-process", javaFile.toUri().normalize()));
        return new PrintWriter(javaFile.openWriter());
    }
    
    /**
     * Get the value for the given keys, whoever matches first, in the current available options.
     */
    private String getOptionValue(String... keys) {
        Map<String,String> options = processingEnv.getOptions();
        for (String key : keys) {
            if (options.containsKey(key))
                return options.get(key);
        }
        return null;
    }
}
