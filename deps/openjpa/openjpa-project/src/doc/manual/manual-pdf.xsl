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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	version='1.0'>
    <!-- used by docbkx-maven-plugin to reference the core styles -->
    <xsl:import href="urn:docbkx:stylesheet"/>

	<!-- no pictures in note/warn/caution pargs -->
	<xsl:param name="admon.graphics" select="0" />
	<xsl:param name="autotoc.label.separator" select="'. '"/>
	<xsl:param name="toc.indent.width" select="24"/>

	<!-- required for PDF bookmarks and some other stuff -->
    <xsl:param name="fop.extensions" select="0"/>
    <xsl:param name="fop1.extensions" select="1"/>
	<!-- <xsl:param name="use.extensions" select="1"/> -->

	<xsl:param name="ulink.hyphenate" select="''"/>

	<!-- don't show link source -->
	<xsl:param name="ulink.show" select="0"/>


	<!-- <xsl:param name="ulink.footnotes" select="1"/> -->
	<!-- <xsl:param name="ulink.hyphenate" select="1"/> -->

	<!-- don't ever hyphenate words -->
	<!-- <xsl:param name="hyphenate">false</xsl:param> -->

	<!-- left justify -->
	<xsl:param name="alignment">left</xsl:param>

	<!-- print page numbers in references -->
	<xsl:param name="insert.xref.page.number" select="1"/>

	<!-- make all cross-refernce links appear in bold and dark blue -->
	<xsl:attribute-set name="xref.properties">
		<!-- <xsl:attribute name="color">#AFAFAF</xsl:attribute> -->
		<xsl:attribute name="color">#17184A</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
	</xsl:attribute-set>


	<!-- page break before level1 sections -->
	<!--xsl:attribute-set name="section.level1.properties">
		<xsl:attribute name="break-before">page</xsl:attribute>
	</xsl:attribute-set-->

	<!-- Chapters: white font on light blue backgrouns -->
	<xsl:attribute-set name="title.properties">
		<xsl:attribute name="color">#000000</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="background-color">#3366CC</xsl:attribute>
	</xsl:attribute-set>


	<!-- Section title: underline -->
	<xsl:attribute-set name="section.title.properties">
		<!-- <xsl:attribute name="color">#005BA6</xsl:attribute> -->

		<!-- simulate underline with border of 1px bottom -->
		<xsl:attribute name="border-bottom-width">1px</xsl:attribute>
		<xsl:attribute name="border-top-width">0px</xsl:attribute>
		<xsl:attribute name="border-left-width">0px</xsl:attribute>
		<xsl:attribute name="border-right-width">0px</xsl:attribute>
		<xsl:attribute name="border-style">solid</xsl:attribute>
		<xsl:attribute name="border-width">1px</xsl:attribute>
		<xsl:attribute name="border-color">#17184A</xsl:attribute>
	</xsl:attribute-set>


	<!-- Examples and other fomal sections: italic -->
	<xsl:attribute-set name="formal.title.properties">
		<xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.2"/>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>

		<xsl:attribute name="font-style">italic</xsl:attribute>
	</xsl:attribute-set>


	<xsl:param name="segmentedlist.as.table" select="1"/>
	<xsl:param name="variablelist.as.blocks" select="1"/>

	<xsl:param name="html.stylesheet">documentation.css</xsl:param>
	<xsl:param name="annotate.toc" select="1"/>
	<xsl:param name="toc.section.depth" select="8"/>
	<xsl:param name="generate.section.toc.level" select="8"/>
	<xsl:param name="generate.index" select="1"/>
	<xsl:param name="chapter.autolabel" select="1"/>
	<xsl:param name="appendix.autolabel" select="1"/>
	<xsl:param name="part.autolabel" select="1"/>
	<xsl:param name="preface.autolabel" select="1"/>
	<xsl:param name="qandadiv.autolabel" select="1"/>
	<xsl:param name="section.autolabel" select="1"/>
	<xsl:param name="section.label.includes.component.label" select="1"/>
	<xsl:param name="label.from.part" select="1"/>

	<xsl:param name="generate.toc">
		/appendix  toc    
		article   toc    
		book      toc,figure,table,example,equation
		/chapter   toc    
		part      toc    
		/preface   toc    
		qandadiv  toc    
		qandaset  toc    
		reference toc    
		/section   toc    
		set       toc    
	</xsl:param>     


	<!-- small margins for the PDF -->
	<xsl:param name="page.margin.inner">0.3in</xsl:param>
	<xsl:param name="page.margin.outer">0.3in</xsl:param>


	<!-- without this, some parts of the body seem to overrin the -->
	<!-- page number part of the footer -->
	<xsl:param name="body.margin.bottom">0.88in</xsl:param>
	<xsl:param name="page.margin.bottom">0.01in</xsl:param>

	<!-- make source code listings be boxed and have a grey background -->
	<xsl:attribute-set name="monospace.verbatim.properties"
		use-attribute-sets="verbatim.properties">
		<xsl:attribute name="font-family">
			<xsl:value-of select="$monospace.font.family"/>
		</xsl:attribute>
		<xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 0.7"/>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>

		<xsl:attribute name="background-color">#F0F0F0</xsl:attribute>
		<xsl:attribute name="border-color">#000000</xsl:attribute>
		<xsl:attribute name="border-style">solid</xsl:attribute>
		<xsl:attribute name="border-width">1px</xsl:attribute>
		<xsl:attribute name="padding-top">0.5cm</xsl:attribute>
		<xsl:attribute name="padding-bottom">0.5cm</xsl:attribute>
		<xsl:attribute name="padding-left">0.5cm</xsl:attribute>
		<xsl:attribute name="padding-right">0.5cm</xsl:attribute>
	</xsl:attribute-set>


	<!-- admonition (note/warn/caution) title properties: italics,		-->
	<!-- with a blue background and white text							-->
	<xsl:attribute-set name="admonition.title.properties">
		<xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.5"/>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>

		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="text-align">center</xsl:attribute>

		<xsl:attribute name="color">#FFFFFF</xsl:attribute>
		<xsl:attribute name="background-color">#17184A</xsl:attribute>

		<xsl:attribute name="border-color">#000000</xsl:attribute>
		<xsl:attribute name="border-style">solid</xsl:attribute>
		<xsl:attribute name="border-width">0px</xsl:attribute>
		<xsl:attribute name="padding-left">0.2cm</xsl:attribute>
		<xsl:attribute name="padding-right">0.2cm</xsl:attribute>
	</xsl:attribute-set>

	<!-- admonition (note/warn/caution) properties: gray background -->
	<xsl:attribute-set name="admonition.properties">
		<xsl:attribute name="background-color">#B0B3B2</xsl:attribute>
		<xsl:attribute name="border-color">#000000</xsl:attribute>
		<xsl:attribute name="border-style">solid</xsl:attribute>
		<xsl:attribute name="border-width">0px</xsl:attribute>
		<xsl:attribute name="padding-top">0.2cm</xsl:attribute>
		<xsl:attribute name="padding-bottom">0.2cm</xsl:attribute>
		<xsl:attribute name="padding-left">0.2cm</xsl:attribute>
		<xsl:attribute name="padding-right">0.2cm</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="book.titlepage.recto.style">
		<xsl:attribute name="font-size">18px</xsl:attribute>
		<xsl:attribute name="text-align">center</xsl:attribute>
		<xsl:attribute name="padding-top">3cm</xsl:attribute>
	</xsl:attribute-set>



	<!-- patches and fixes for stylesheet bugs -->


	<!--
		fix for duplicate ids generated by <qandaset> attributes
		overrides docbook-xsl/fo/qandaset.xsl

    	See: http://lists.oasis-open.org/archives/docbook/200309/msg00070.html
	-->
<xsl:template match="question">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>

  <xsl:variable name="entry.id">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="parent::*"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="deflabel">
    <xsl:choose>
      <xsl:when test="ancestor-or-self::*[@defaultlabel]">
        <xsl:value-of select="(ancestor-or-self::*[@defaultlabel])[last()]
                              /@defaultlabel"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$qanda.defaultlabel"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <fo:list-item id="{$entry.id}" xsl:use-attribute-sets="list.item.spacing">
    <!--
    This adds duplicate id attributes for some reason
    See: http://lists.oasis-open.org/archives/docbook/200309/msg00070.html
    <fo:list-item-label id="{$id}" end-indent="label-end()">
    -->
    <fo:list-item-label end-indent="label-end()">
      <xsl:choose>
        <xsl:when test="$deflabel = 'none'">
          <fo:block/>
        </xsl:when>
        <xsl:otherwise>
          <fo:block>
            <xsl:apply-templates select="." mode="label.markup"/>
	    <xsl:if test="$deflabel = 'number' and not(label)">
              <xsl:apply-templates select="." mode="intralabel.punctuation"/>
	    </xsl:if>
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()">
      <xsl:choose>
        <xsl:when test="$deflabel = 'none'">
          <fo:block font-weight="bold">
            <xsl:apply-templates select="*[local-name(.)!='label']"/>
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="*[local-name(.)!='label']"/>
        </xsl:otherwise>
      </xsl:choose>
    </fo:list-item-body>
  </fo:list-item>
</xsl:template>


<xsl:template match="answer">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>
  <xsl:variable name="entry.id">
    <xsl:call-template name="object.id">
      <xsl:with-param name="object" select="parent::*"/>
    </xsl:call-template>
  </xsl:variable>
      
  <xsl:variable name="deflabel">
    <xsl:choose>
      <xsl:when test="ancestor-or-self::*[@defaultlabel]">
        <xsl:value-of select="(ancestor-or-self::*[@defaultlabel])[last()]
                              /@defaultlabel"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$qanda.defaultlabel"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <fo:list-item xsl:use-attribute-sets="list.item.spacing">
    <!--
    This adds duplicate id attributes for some reason
    See: http://lists.oasis-open.org/archives/docbook/200309/msg00070.html
    <fo:list-item-label id="{$id}" end-indent="label-end()">
    -->
    <fo:list-item-label end-indent="label-end()">
      <xsl:choose>
        <xsl:when test="$deflabel = 'none'">
          <fo:block/>
        </xsl:when>
        <xsl:otherwise>
          <fo:block>
            <xsl:variable name="answer.label">
              <xsl:apply-templates select="." mode="label.markup"/>
            </xsl:variable>
            <xsl:copy-of select="$answer.label"/>
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()">
      <xsl:apply-templates select="*[local-name(.)!='label']"/>
    </fo:list-item-body>
  </fo:list-item>
</xsl:template>


<!-- the default stylesheets move the alphabet letter out of the fo
	block. this fixes that. -->
<xsl:template name="indexdiv.title">
  <xsl:param name="title"/>
  <xsl:param name="titlecontent"/>

  <fo:block 
	    font-size="14.4pt"
            font-family="{$title.fontset}"
            font-weight="bold"
            keep-with-next.within-column="always"
            space-before.optimum="{$body.font.master}pt"
            space-before.minimum="{$body.font.master * 0.8}pt"
            space-before.maximum="{$body.font.master * 1.2}pt">
    <xsl:choose>
      <xsl:when test="$title">
        <xsl:apply-templates select="." mode="object.title.markup">
          <xsl:with-param name="allow-anchors" select="1"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$titlecontent"/>
      </xsl:otherwise>
    </xsl:choose>
  </fo:block>
</xsl:template>

</xsl:stylesheet>

