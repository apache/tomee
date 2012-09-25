<?xml version="1.0" encoding="UTF-8"?>
    <!--
        Licensed to the Apache Software Foundation (ASF) under one or more
        contributor license agreements. See the NOTICE file distributed with
        this work for additional information regarding copyright ownership. The
        ASF licenses this file to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance with the
        License. You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
        law or agreed to in writing, software distributed under the License is
        distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied. See the License for the specific
        language governing permissions and limitations under the License.
    -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="section">
		<xsl:variable name="depth" select="count(ancestor::section)+1"/>
		<div class="{name(.)}">
			<xsl:call-template name="language.attribute"/>
			<xsl:call-template name="section.titlepage"/>
			<xsl:variable name="toc.params">
				<xsl:call-template name="find.path.params">
					<xsl:with-param name="table" select="normalize-space($generate.toc)"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:apply-templates/>
			<!-- ToC -->
			<xsl:if
				test="contains($toc.params, 'toc') and $depth &lt;= $generate.section.toc.level">
				<xsl:call-template name="section.toc">
					<xsl:with-param name="toc.title.p" select="contains($toc.params, 'title')"/>
				</xsl:call-template>
				<xsl:call-template name="section.toc.separator"/>
			</xsl:if>
			<xsl:call-template name="process.chunk.footnotes"/>
		</div>
	</xsl:template>

	<xsl:template match="sect1">
		<div class="{name(.)}">
			<xsl:call-template name="language.attribute"/>
			<xsl:choose>
				<xsl:when test="@renderas = 'sect2'">
					<xsl:call-template name="sect2.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect3'">
					<xsl:call-template name="sect3.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect4'">
					<xsl:call-template name="sect4.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect5'">
					<xsl:call-template name="sect5.titlepage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="sect1.titlepage"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates/>
			<!-- ToC -->
			<xsl:variable name="toc.params">
				<xsl:call-template name="find.path.params">
					<xsl:with-param name="table" select="normalize-space($generate.toc)"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:if
				test="contains($toc.params, 'toc') and $generate.section.toc.level &gt;= 1">
				<xsl:call-template name="section.toc">
					<xsl:with-param name="toc.title.p" select="contains($toc.params, 'title')"/>
				</xsl:call-template>
				<xsl:call-template name="section.toc.separator"/>
			</xsl:if>
			<xsl:call-template name="process.chunk.footnotes"/>
		</div>
	</xsl:template>
	
	
	<xsl:template match="sect2">
		<div class="{name(.)}">
			<xsl:call-template name="language.attribute"/>
			<xsl:choose>
				<xsl:when test="@renderas = 'sect1'">
					<xsl:call-template name="sect1.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect3'">
					<xsl:call-template name="sect3.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect4'">
					<xsl:call-template name="sect4.titlepage"/>
				</xsl:when>
				<xsl:when test="@renderas = 'sect5'">
					<xsl:call-template name="sect5.titlepage"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="sect2.titlepage"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:variable name="toc.params">
				<xsl:call-template name="find.path.params">
					<xsl:with-param name="table" select="normalize-space($generate.toc)"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="contains($toc.params, 'toc') and $generate.section.toc.level &gt;= 2">
				<xsl:call-template name="section.toc">
					<xsl:with-param name="toc.title.p" select="contains($toc.params, 'title')"/>
				</xsl:call-template>
				<xsl:call-template name="section.toc.separator"/>
			</xsl:if>
			<xsl:apply-templates/>
			<xsl:call-template name="process.chunk.footnotes"/>
		</div>
	</xsl:template>

</xsl:stylesheet>
