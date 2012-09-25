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
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exsl="http://exslt.org/common" 
    version="1.0"
	exclude-result-prefixes="exsl">

	<xsl:template name="book.titlepage.recto">
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/productname" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/productname" />
		<xsl:choose>
			<xsl:when test="bookinfo/title">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/title" />
			</xsl:when>
			<xsl:when test="info/title">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/title" />
			</xsl:when>
			<xsl:when test="title">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="title" />
			</xsl:when>
		</xsl:choose>

		<xsl:choose>
			<xsl:when test="bookinfo/subtitle">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/subtitle" />
			</xsl:when>
			<xsl:when test="info/subtitle">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/subtitle" />
			</xsl:when>
			<xsl:when test="subtitle">
				<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="subtitle" />
			</xsl:when>
		</xsl:choose>

		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/address" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/address" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/legalnotice" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/legalnotice" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/edition" />
		<xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/edition" />
	</xsl:template>

	<xsl:template name="book.titlepage.verso">
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="bookinfo/productname" />
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="info/productname" />
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="bookinfo/abstract" />
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="info/abstract" />
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="bookinfo/revhistory" />
		<xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="info/revhistory" />
	</xsl:template>

	<xsl:template name="book.titlepage">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<xsl:variable name="recto.content">
				<xsl:call-template name="book.titlepage.before.recto" />
				<xsl:call-template name="book.titlepage.recto" />
			</xsl:variable>
			<xsl:variable name="recto.elements.count">
				<xsl:choose>
					<xsl:when test="function-available('exsl:node-set')">
						<xsl:value-of select="count(exsl:node-set($recto.content)/*)" />
					</xsl:when>
					<xsl:otherwise>1</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:if
				test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
				<fo:block>
					<xsl:copy-of select="$recto.content" />
				</fo:block>
			</xsl:if>
			<xsl:variable name="verso.content">
				<xsl:call-template name="book.titlepage.before.verso" />
				<xsl:call-template name="book.titlepage.verso" />
			</xsl:variable>
			<xsl:variable name="verso.elements.count">
				<xsl:choose>
					<xsl:when test="function-available('exsl:node-set')">
						<xsl:value-of select="count(exsl:node-set($verso.content)/*)" />
					</xsl:when>
					<xsl:otherwise>1</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:if
				test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
				<fo:block>
					<xsl:copy-of select="$verso.content" />
				</fo:block>
			</xsl:if>
			<xsl:call-template name="book.titlepage.separator" />
		</fo:block>
	</xsl:template>

	<xsl:template match="*" mode="book.titlepage.recto.mode">
		<!-- if an element isn't found in this mode, -->
		<!-- try the generic titlepage.mode -->
		<xsl:apply-templates select="." mode="titlepage.mode" />
	</xsl:template>

	<xsl:template match="*" mode="book.titlepage.verso.mode">
		<!-- if an element isn't found in this mode, -->
		<!-- try the generic titlepage.mode -->
		<xsl:apply-templates select="." mode="titlepage.mode" />
	</xsl:template>

	<xsl:template match="productname"
		mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.style" margin-top="30px"
			text-align="right" font-size="22pt" font-weight="normal"
			font-family="{$title.fontset}">
			<xsl:apply-templates select="."
				mode="book.titlepage.recto.mode" />
		</fo:block>
	</xsl:template>

	<xsl:template match="title" mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.style"
			text-align="right" font-size="24pt" space-before="40pt"
			font-weight="bold" font-family="{$title.fontset}">
			<xsl:call-template name="division.title">
				<xsl:with-param name="node"
					select="ancestor-or-self::book[1]" />
			</xsl:call-template>
		</fo:block>
	</xsl:template>

	<xsl:template match="subtitle"
		mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.style"
			text-align="right" font-size="20pt" font-weight="normal"
			font-family="{$title.fontset}">
			<xsl:apply-templates select="."
				mode="book.titlepage.recto.mode" />
		</fo:block>
	</xsl:template>

	<xsl:attribute-set name="book.titlepage.recto.address.style" />
	<xsl:template match="address"
		mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.address.style">
			<xsl:apply-templates select="."
				mode="book.titlepage.recto.mode" />
		</fo:block>
	</xsl:template>
	<xsl:template match="address" mode="book.titlepage.recto.mode">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="legalnotice"
		mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.style" text-align="left"
			font-size="8pt" space-before="18pt" font-weight="normal"
			font-family="{$body.fontset}" border-bottom-width="0.5pt"
			border-bottom-style="solid" border-bottom-color="black"
			border-top-width="0.5pt" border-top-style="solid"
			border-top-color="black">
			<xsl:apply-templates select="."
				mode="book.titlepage.recto.mode" />
		</fo:block>
	</xsl:template>

	<xsl:template match="edition"
		mode="book.titlepage.recto.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.recto.style">
			<xsl:call-template name="book.recto.edition">
			</xsl:call-template>
		</fo:block>
	</xsl:template>

	<xsl:template match="productname"
		mode="book.titlepage.verso.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.verso.style">
			<xsl:call-template name="book.verso.productname">
			</xsl:call-template>
		</fo:block>
	</xsl:template>

	<xsl:template match="abstract"
		mode="book.titlepage.verso.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.verso.style">
			<xsl:apply-templates select="."
				mode="book.titlepage.verso.mode" />
		</fo:block>
	</xsl:template>

	<xsl:template match="revhistory"
		mode="book.titlepage.verso.auto.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
			xsl:use-attribute-sets="book.titlepage.verso.style">
			<xsl:call-template name="book.verso.revhistory">
			</xsl:call-template>
		</fo:block>
	</xsl:template>
	
	<xsl:template name="book.recto.edition">
	</xsl:template>
	
	<xsl:template match="bookinfo/author" mode="docinfo">
		<fo:table-row xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:table-cell>
				<fo:block> </fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block> </fo:block>
			</fo:table-cell>
		</fo:table-row>
		<fo:table-row xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:table-cell>
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">
					Contact Person
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">
					<xsl:call-template name="anchor" />
					<xsl:call-template name="person.name" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
		<xsl:if test="email|affiliation/address/email">
			<fo:table-row
				xmlns:fo="http://www.w3.org/1999/XSL/Format">
				<fo:table-cell>
					<fo:block> </fo:block>
				</fo:table-cell>
				<fo:table-cell>
					<fo:block text-align="left" font-size="11pt"
						font-weight="normal" font-family="{$body.fontset}">
						<xsl:apply-templates
							select="(email|affiliation/address/email)[1]" />
					</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="bookinfo/pubdate|info/pubdate"
		mode="titlepage.mode" priority="2">
		<xsl:apply-templates mode="titlepage.mode" />
	</xsl:template>
	
	<xsl:template name="book.verso.productname">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:block xsl:use-attribute-sets="formal.title.properties"
				keep-with-next.within-column="always">
				Purpose and scope of this document
			</fo:block>
			<fo:block>
				This document is part of the
				<xsl:apply-templates mode="titlepage.mode" />
				<xsl:call-template name="gentext.space" />
				<xsl:if test="//productnumber">
					<xsl:apply-templates select="//productnumber" />
				</xsl:if>
				<xsl:call-template name="gentext.space" />
				release.
			</fo:block>
		</fo:block>
	</xsl:template>
	
	<xsl:template match="abstract" mode="titlepage.mode">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:block xsl:use-attribute-sets="formal.title.properties"
				keep-with-next.within-column="always">
				<xsl:apply-templates select="." mode="title.markup" />
			</fo:block>
			<xsl:apply-templates mode="titlepage.mode" />
		</fo:block>
	</xsl:template>
	
	<xsl:template name="book.verso.revhistory">
		<fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:block xsl:use-attribute-sets="formal.title.properties"
				keep-with-next.within-column="always">
				Version control
			</fo:block>
			<fo:block>
				This document is updated continuously. Major
				modifications on content or size will lead to new
				release numbers, whereas textual revisions are reflected
				as new level numbers. The following list shows the
				document's history.
			</fo:block>
		</fo:block>

		<xsl:if test="revision">
			<fo:table xmlns:fo="http://www.w3.org/1999/XSL/Format"
				table-layout="fixed" width="16cm" space-before="8.25pt"
				border-width="0.5pt" border-style="solid" border-color="black">
				<fo:table-column column-number="1" column-width="2cm" />
				<fo:table-column column-number="2" column-width="3cm" />
				<fo:table-column column-number="3" column-width="4cm" />
				<fo:table-column column-number="4" column-width="7.5cm" />

				<fo:table-header background-color="#E0E0E0">
					<fo:table-row background-color="#E0E0E0">
						<fo:table-cell padding="2pt">
							<fo:block text-align="left" font-size="11pt"
								font-weight="bold">
								Version
							</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="2pt">
							<fo:block text-align="left" font-size="11pt"
								font-weight="bold">
								Date
							</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="2pt">
							<fo:block text-align="left" font-size="11pt"
								font-weight="bold">
								Author
							</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="2pt">
							<fo:block text-align="left" font-size="11pt"
								font-weight="bold">
								Reason of modification
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-header>

				<fo:table-body start-indent="0pt" end-indent="0pt">
					<xsl:apply-templates select="."
						mode="book.verso.revhistory" />
				</fo:table-body>
			</fo:table>
		</xsl:if>
	</xsl:template>

	<xsl:template match="revision" mode="book.verso.revhistory">
		<xsl:variable name="revnumber" select="revnumber" />
		<xsl:variable name="revdate" select="date" />
		<xsl:variable name="revauthor" select="authorinitials" />
		<xsl:variable name="revremark" select="revremark|revdescription" />
			
		<fo:table-row xmlns:fo="http://www.w3.org/1999/XSL/Format"
			border-width="0.5pt" border-style="solid" border-color="black"
			>
			
			<xsl:if test="(position() mod 2) = 0">
				<xsl:attribute name="background-color">#EFEFEF</xsl:attribute>
			</xsl:if>
			
			<fo:table-cell padding="2pt">
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">
					<xsl:if test="$revnumber">
						<xsl:apply-templates select="$revnumber[1]" mode="titlepage.mode" />
					</xsl:if>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding="2pt">
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">
					<xsl:if test="$revdate">
						<xsl:apply-templates select="$revdate[1]" mode="titlepage.mode" />
					</xsl:if>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding="2pt">
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">

					<xsl:for-each select="$revauthor">
						<xsl:apply-templates select="." mode="titlepage.mode" />
						<xsl:if test="position() != last()">
							<xsl:text>, </xsl:text>
						</xsl:if>
					</xsl:for-each>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding="2pt">
				<fo:block text-align="left" font-size="11pt"
					font-weight="normal" font-family="{$body.fontset}">
					<xsl:if test="$revremark">
						<xsl:apply-templates select="$revremark[1]" mode="titlepage.mode" />
					</xsl:if>
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

</xsl:stylesheet>