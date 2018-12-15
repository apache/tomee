/*
 * Copyright © 2017 Ivar Grimstad (ivar.grimstad@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.mvc.util;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependencies;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WebArchiveBuilder {

    private final WebArchive archive = ShrinkWrap.create(WebArchive.class);
    private List<MavenDependency> additionalDependencies = new ArrayList<>();

    public WebArchiveBuilder addPackage(String packageName) {
        archive.addPackage(packageName);
        return this;
    }

    public WebArchiveBuilder addView(Asset asset, String name) {
        archive.addAsWebInfResource(asset, "views/" + name);
        return this;
    }

    public WebArchiveBuilder addView(File file, String name) {
        return this.addView(new FileAsset(file), name);
    }

    public WebArchiveBuilder addBeansXml() {
        archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return this;
    }

    public WebArchiveBuilder addDependencies(MavenDependency... dependencies) {
        this.additionalDependencies.addAll(Arrays.asList(dependencies));
        return this;
    }

    public WebArchiveBuilder addDependency(String coordinates) {
        addDependencies(MavenDependencies.createDependency(coordinates, ScopeType.RUNTIME,false));
        return this;
    }

    public WebArchive build() {
        PomEquippedResolveStage stage = Maven.configureResolver().workOffline()
            .withClassPathResolution(true)
            .loadPomFromFile("pom.xml")
            .importCompileAndRuntimeDependencies();

        if (!this.additionalDependencies.isEmpty()) {
            stage = stage.addDependencies(this.additionalDependencies);
        }

        return archive.addAsLibraries(stage.resolve().withTransitivity().asFile());
    }

}
