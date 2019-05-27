/**
 *
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

package org.apache.tomee.installer;

import java.io.File;
import java.util.List;

public interface PathsInterface {
    File getCatalinaHomeDir();

    void setCatalinaHomeDir(String catalinaHomeDir);

    void setCatalinaHomeDir(File catalinaHomeDir);

    File getCatalinaBaseDir();

    void setCatalinaBaseDir(String catalinaBaseDir);

    void setCatalinaBaseDir(File catalinaBaseDir);

    File getServerXmlFile();

    File getHome();

    void setServerXmlFile(String serverXmlFile);

    void setServerXmlFile(File serverXmlFile);

    File getCatalinaLibDir();

    File getCatalinaConfDir();

    File getCatalinaBinDir();

    File getCatalinaShFile();

    File getCatalinaBatFile();

    File getOpenEJBLibDir();

    File getOpenEJBTomcatLoaderJar();

    File getJavaEEAPIJar();

    File getOpenEJBJavaagentJar();

    File getOpenEJBCoreJar();

    File geOpenEJBTomcatCommonJar();

    File findOpenEJBJar(String namePrefix);

    File findOpenEJBWebJar(String namePrefix);

    File findTomEELibJar(String prefix);

    boolean verify();

    void reset();

    boolean hasErrors();

    List<String> getErrors();

    File getOpenEJBWebLibDir();

    File getTomcatUsersXml();

    File getSetClasspathSh();

    File getSetClasspathBat();
}
