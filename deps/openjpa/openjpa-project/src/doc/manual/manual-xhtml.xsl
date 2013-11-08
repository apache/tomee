<?xml version="1.0" encoding="UTF-8"?>
<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.   
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>

    <!-- docbook stylesheet customizations for openjpa manual -->
	<!-- <xsl:import href="http://docbook.sourceforge.net/release/xsl/docbook-xsl-1.67.2/html/docbook.xsl"/> -->
    <!-- locally downloaded cache of stylesheets -->
	<!-- <xsl:import href="../../../target/stylesheets/docbook-xsl-1.67.2/html/docbook.xsl"/> -->

    <!-- used by docbkx-maven-plugin to reference the core styles -->
    <xsl:import href="urn:docbkx:stylesheet"/>

	<xsl:param name="html.stylesheet">css/docbook.css</xsl:param>

	<xsl:param name="html.cleanup" select="1"/>
	<xsl:param name="label.from.part" select="1"/>
	<xsl:param name="annotate.toc" select="1"/>
	<xsl:param name="toc.section.depth">5</xsl:param>
	<xsl:param name="generate.section.toc.level" select="8"/>
	<xsl:param name="generate.id.attributes" select="1"/>
	<xsl:param name="generate.index" select="1"/>
	<xsl:param name="chapter.autolabel" select="1"/>
	<xsl:param name="appendix.autolabel" select="1"/>
	<xsl:param name="part.autolabel" select="1"/>
	<xsl:param name="preface.autolabel" select="0"/>
	<xsl:param name="qandadiv.autolabel" select="1"/>
	<xsl:param name="section.autolabel" select="1"/>
	<xsl:template name="process.image.attributes"/>
</xsl:stylesheet>

