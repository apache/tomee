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

	<xsl:template match="*" mode="admon.graphic.width">
		<xsl:param name="node" select="."/>
		<xsl:text>40</xsl:text>
	</xsl:template>

	<xsl:template name="graphical.admonition">
		<xsl:variable name="admon.type">
			<xsl:choose>
				<xsl:when test="local-name(.)='note'">Note</xsl:when>
				<xsl:when test="local-name(.)='warning'">Warning</xsl:when>
				<xsl:when test="local-name(.)='caution'">Caution</xsl:when>
				<xsl:when test="local-name(.)='tip'">Tip</xsl:when>
				<xsl:when test="local-name(.)='important'">Important</xsl:when>
				<xsl:otherwise>Note</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="alt">
			<xsl:call-template name="gentext">
				<xsl:with-param name="key" select="$admon.type"/>
			</xsl:call-template>
		</xsl:variable>
		<div class="{name(.)}">
			<xsl:if test="$admon.style != ''">
				<xsl:attribute name="style">
					<xsl:value-of select="$admon.style"/>
				</xsl:attribute>
			</xsl:if>
			<table border="0" class="admon">
				<xsl:attribute name="summary">
					<xsl:value-of select="$admon.type"/>
					<xsl:if test="title">
						<xsl:text>: </xsl:text>
						<xsl:value-of select="title"/>
					</xsl:if>
				</xsl:attribute>
				<tr>
					<td rowspan="2" align="center" valign="top">
						<xsl:attribute name="width">
							<xsl:apply-templates select="." mode="admon.graphic.width"/>
						</xsl:attribute>
						<img alt="[{$alt}]">
							<xsl:attribute name="src">
								<xsl:call-template name="admon.graphic"/>
							</xsl:attribute>
						</img>
					</td>
					<th align="left">
						<xsl:call-template name="anchor"/>
						<xsl:if test="$admon.textlabel != 0 or title">
							<xsl:apply-templates select="." mode="object.title.markup"/>
						</xsl:if>
					</th>
				</tr>
				<tr>
					<td align="left" valign="top">
						<xsl:apply-templates/>
					</td>
				</tr>
			</table>
		</div>
	</xsl:template>

</xsl:stylesheet>
