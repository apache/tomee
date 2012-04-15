/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xbean.finder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.xbean.osgi.bundle.util.ClassDiscoveryFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class BundleAssignableClassFinder extends BundleClassFinder {

    private static final Logger logger = LoggerFactory.getLogger(BundleAssignableClassFinder.class);

    private Class<?>[] clses;

    private Set<String> targetClassNames = new HashSet<String>();

    private Set<String> targetInterfaceNames = new HashSet<String>();

    private Set<String> wiredImportedPackageNames = new HashSet<String>();

    /**
     * Create a new BundleClassFinder, it will search all the classes based the rule defined by the parameters via ASM tool
     * @param packageAdmin
     * @param bundle
     * @param clses
     * @param discoveryFilter
     */
    public BundleAssignableClassFinder(PackageAdmin packageAdmin, Bundle bundle, Class<?>[] clses, ClassDiscoveryFilter discoveryFilter) {
        super(packageAdmin, bundle, discoveryFilter);
        if (clses == null || clses.length == 0) {
            throw new IllegalArgumentException("At least one class or interface should be specified");
        }
        this.clses = clses;
        for (Class<?> cls : clses) {
            String asmStyleName = cls.getName().replace('.', '/');
            if (cls.isInterface()) {
                targetInterfaceNames.add(asmStyleName);
            } else {
                targetClassNames.add(asmStyleName);
            }
        }
        initialize();
    }

    public BundleAssignableClassFinder(PackageAdmin packageAdmin, Class<?>[] clses, Bundle bundle) {
        this(packageAdmin, bundle, clses, FULL_CLASS_DISCOVERY_FILTER);
    }

    @Override
    protected BundleClassFinder createSubBundleClassFinder(PackageAdmin packageAdmin, Bundle bundle, ClassDiscoveryFilter classDiscoveryFilter) {
        return new BundleAssignableClassFinder(packageAdmin, bundle, clses, classDiscoveryFilter);
    }

    @Override
    protected boolean isClassAcceptable(String name, InputStream in) throws IOException {
        ClassReader classReader = new ClassReader(in);
        String className = classReader.getClassName();
        if ((classReader.getAccess() & Opcodes.ACC_INTERFACE) == 0) {
            if (targetClassNames.contains(className)) {
                return true;
            }
        } else {
            if (targetInterfaceNames.contains(className)) {
                return true;
            }
        }
        String[] interfaceNames = classReader.getInterfaces();
        try {
            for (String interfaceName : interfaceNames) {
                if (wiredImportedPackageNames.contains(toASMStylePackageName(interfaceName))) {
                    return isClassAssignable(bundle.loadClass(toJavaStyleClassName(interfaceName)));
                } else {
                    if (isInterfaceAssignable(interfaceName)) {
                        return true;
                    }
                }
            }
            String superClassName = classReader.getSuperName();
            if (wiredImportedPackageNames.contains(toASMStylePackageName(superClassName))) {
                return isClassAssignable(bundle.loadClass(toJavaStyleClassName(superClassName)));
            }
            return isSuperClassAssignable(superClassName);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    protected boolean isClassAcceptable(URL url) {
        InputStream in = null;
        try {
            in = url.openStream();
            return isClassAcceptable("", in);
        } catch (IOException e) {
            logger.warn("Unable to check the class of url " + url, e);
            return false;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                }
        }
    }

    private void initialize() {
        BundleDescription description = new BundleDescription(bundle.getHeaders());
        List<BundleDescription.ImportPackage> imports = description.getExternalImports();
        for (BundleDescription.ImportPackage packageImport : imports) {
            String packageName = packageImport.getName();
            ExportedPackage[] exports = packageAdmin.getExportedPackages(packageName);
            Bundle wiredBundle = isWired(bundle, exports);
            if (wiredBundle != null) {
                wiredImportedPackageNames.add(packageName.replace('.', '/'));
                break;
            }
        }
    }

    private boolean isClassAssignable(Class<?> cls) {
        for (Class<?> targetClass : clses) {
            if (targetClass.isAssignableFrom(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param interfaceName The interface name should be in the format of org/test/SimpleInterface
     * @return return true if the method parameter interfaceName is assignable to any interface in the expected interfaces
     */
    private boolean isInterfaceAssignable(String interfaceName) {
        //Check each interface in interfaceNames set
        if (targetInterfaceNames.contains(interfaceName)) {
            return true;
        }
        //Check ancestor intefaces
        URL url = bundle.getResource(interfaceName + ".class");
        if (url == null) {
            //TODO what should we do if we do not find the interface ?
            return false;
        }
        InputStream in = null;
        try {
            in = url.openStream();
            ClassReader classReader = new ClassReader(in);
            String[] superInterfaceNames = classReader.getInterfaces();
            for (String superInterfaceName : superInterfaceNames) {
                if (isInterfaceAssignable(superInterfaceName)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            logger.warn("Unable to check the interface " + interfaceName, e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     *
     * @param superClassName The super class name should be in the format of org/test/SimpleClass
     * @return return true if the method parameter superClassName  is assignable to any interface in the expected interfaces or any class in the expected classes
     */
    private boolean isSuperClassAssignable(String superClassName) {
        if (targetClassNames.contains(superClassName)) {
            return true;
        } else if (superClassName.equals("java/lang/Object")) {
            return false;
        }
        
        //Check parent class
        URL url = bundle.getResource(superClassName + ".class");
        if (url == null) {
            //TODO what should we do if we do not find the super class ?
            return false;
        }
        InputStream in = null;
        try {
            in = url.openStream();
            ClassReader classReader = new ClassReader(in);
            
            //Check interfaces
            String[] superInterfaceNames = classReader.getInterfaces();            
            for (String superInterfaceName : superInterfaceNames) {
                if (isInterfaceAssignable(superInterfaceName)) {
                    return true;
                }                
            }
            
            //Check className
            return isSuperClassAssignable(classReader.getSuperName());            
        } catch (IOException e) {
            logger.warn("Unable to check the super class  " + superClassName, e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Get the ASM style package name from the parameter className.
     * If the className is ended with .class extension, e.g.  /org/apache/geronimo/TestCass.class or org.apache.geronimo.TestClass.class,
     *      then org/apache/geronimo is returned
     * If the className is not ended with .class extension, e.g.  /org/apache/geronimo/TestCass or org.apache.geronimo.TestClass,
     *      then org/apache/geronimo is returned
     * @param className
     * @return ASM style package name, should be in the format of  "org/apache/geronimo"
     */
    protected String toASMStylePackageName(String className) {
        if (className.endsWith(EXT)) {
            className = className.substring(0, className.length() - EXT.length());
        }
        className = className.replace('.', '/');
        int iLastDotIndex = className.lastIndexOf('/');
        if (iLastDotIndex != -1) {
            return className.substring(0, iLastDotIndex);
        } else {
            return "";
        }
    }
}
