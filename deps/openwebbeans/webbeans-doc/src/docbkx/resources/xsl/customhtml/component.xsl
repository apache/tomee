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

	<xsl:template match="chapter">
		<div class="{name(.)}">
			<xsl:call-template name="language.attribute"/>
			<xsl:if test="$generate.id.attributes != 0">
				<xsl:attribute name="id">
					<xsl:call-template name="object.id"/>
				</xsl:attribute>
			</xsl:if>

			<xsl:call-template name="component.separator"/>
			<xsl:call-template name="chapter.titlepage"/>
			<xsl:apply-templates/>
			<xsl:variable name="toc.params">
				<xsl:call-template name="find.path.params">
					<xsl:with-param name="table" select="normalize-space($generate.toc)"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="contains($toc.params, 'toc')">
				<xsl:call-template name="component.toc">
					<xsl:with-param name="toc.title.p" select="contains($toc.params, 'title')"/>
				</xsl:call-template>
				<xsl:call-template name="component.toc.separator"/>
			</xsl:if>
			<xsl:call-template name="process.footnotes"/>
		</div>
	</xsl:template>

</xsl:stylesheet>
