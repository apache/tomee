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
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    version="1.0">
    
    <xsl:import href="urn:docbkx:stylesheet" />

     <!-- ================ Header and Footer data ============== -->
    <xsl:param name="apache.logo.src" select="concat($img.src.path, 'asf-logo.gif')" />
    <xsl:param name="apache.logo.alt" select="'Apache Software Foundation'" />
    <xsl:param name="document.logo.src" select="concat($img.src.path, 'openwebbeans.png')" />
    <xsl:param name="document.logo.alt" select="'Apache OpenWebBeans'" />
    <xsl:param name="document.copyright" select="'&#169; 2009 Apache OpenWebBeans'" />

    <!-- ================ Customization Layer =========== -->
    <xsl:include href="custompdf/pagesetup.xsl" />
    <xsl:include href="custompdf/admon.xsl" />
    <xsl:include href="custompdf/block.xsl" />
    <xsl:include href="custompdf/inline.xsl" />
    <xsl:include href="custompdf/titlepage.xsl" />
    <xsl:include href="custompdf/highlight.xsl" />
 
    <!-- =============== Customized I18n ============== -->
    <xsl:param name="local.l10n.xml" select="document('customcommon/en.xml')" />

    <!-- =============== Renderer options ============== -->
    <xsl:param name="xep.extensions" select="0" />

    <xsl:attribute-set name="xep.index.item.properties">
        <xsl:attribute name="merge-subsequent-page-numbers">true</xsl:attribute>
        <xsl:attribute name="link-back">true</xsl:attribute>
    </xsl:attribute-set>

    <!-- FOP provide only PDF Bookmarks at the moment -->
    <xsl:param name="fop.extensions">0</xsl:param> <!-- version 0.20.5 or earlier -->
    <xsl:param name="fop1.extensions">1</xsl:param> <!-- version 0.93 or later -->

    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="use.extensions">1</xsl:param>
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="callout.extensions">1</xsl:param>

    <!-- === Pagelayout === -->
    <xsl:param name="paper.type">A4</xsl:param>
    <xsl:param name="page.margin.inner">2.5cm</xsl:param>
    <xsl:param name="page.margin.outer">2.5cm</xsl:param>
    <xsl:param name="page.margin.top">0.7cm</xsl:param>
    <xsl:param name="page.margin.bottom">0.5cm</xsl:param>
    <xsl:param name="body.margin.top">3cm</xsl:param>
    <xsl:param name="body.margin.bottom">1.5cm</xsl:param>
    <xsl:param name="region.before.extent">2.3cm</xsl:param>
    <xsl:param name="region.after.extent">1.3cm</xsl:param>
    <xsl:param name="alignment">left</xsl:param>
    <xsl:param name="hyphenate">true</xsl:param>
    <xsl:attribute-set name="normal.para.spacing">
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.minimum">0.8em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">1.2em</xsl:attribute>
    </xsl:attribute-set>
    <xsl:param name="header.column.widths" select="'2 0 1'"></xsl:param>
    
    <!-- === Indentation === -->
    <xsl:param name="title.margin.left">
        <xsl:choose>
            <xsl:when test="$fop.extensions != 0">-2pt</xsl:when>
            <xsl:when test="$passivetex.extensions != 0">0pt</xsl:when>
            <xsl:otherwise>0pt</xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="body.start.indent">
        <xsl:choose>
            <xsl:when test="$fop.extensions != 0">0pt</xsl:when>
            <xsl:when test="$passivetex.extensions != 0">0pt</xsl:when>
            <xsl:otherwise>2pt</xsl:otherwise>
        </xsl:choose>
    </xsl:param>

    <!-- === Images === -->
    <xsl:param name="graphic.default.extension">.png</xsl:param>
    <xsl:param name="default.image.width">15.5cm</xsl:param>
    <xsl:param name="keep.relative.image.uris">1</xsl:param>

    <!-- === Call Outs === -->
    <xsl:param name="callout.graphics" select="'1'"></xsl:param>
    <xsl:param name="callout.graphics.extension">.png</xsl:param>

    <!-- === Admons === -->
    <xsl:param name="admon.graphics.extension">.png</xsl:param>

    <!-- === Index === -->
    <xsl:param name="generate.index" select="1"></xsl:param>
    <xsl:param name="make.index.markup" select="0"></xsl:param>
    <xsl:attribute-set name="index.div.title.properties">
        <xsl:attribute name="margin-left">0pt</xsl:attribute>
        <xsl:attribute name="font-size">16pt</xsl:attribute>
        <xsl:attribute name="font-family"><xsl:value-of select="$title.fontset"></xsl:value-of></xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="space-before.minimum"><xsl:value-of select="concat($body.font.master,'pt * 0.8')"></xsl:value-of></xsl:attribute>
        <xsl:attribute name="space-before.optimum"><xsl:value-of select="concat($body.font.master,'pt * 1.2')"></xsl:value-of></xsl:attribute>
        <xsl:attribute name="space-after.minimum"><xsl:value-of select="concat($body.font.master,'pt * 0.6')"></xsl:value-of></xsl:attribute>
        <xsl:attribute name="space-after.optimum"><xsl:value-of select="concat($body.font.master,'pt * 0.8')"></xsl:value-of></xsl:attribute>
        <xsl:attribute name="start-indent">0pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="index.entry.properties">
        <xsl:attribute name="start-indent">0pt</xsl:attribute>
        <xsl:attribute name="font-size">10pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- === Enumeration and autolabel === -->
    <xsl:param name="section.autolabel" select="1" />
    <!--
        1 Arabic numeration (1, 2, 3 ...). 
        A or upperalpha Uppercase letter numeration (A, B, C ...). 
        a or loweralpha Lowercase letter numeration (a, b, c ...). 
        I or upperroman Uppercase roman numeration (I, II, III ...). 
        i or lowerroman Lowercase roman letter numeration (i, ii, iii ...).
    -->
    <xsl:param name="chapter.autolabel" select="1" />
    <xsl:param name="section.label.includes.component.label" select="1" />
    <xsl:param name="label.from.part" select="0" />
    <xsl:param name="component.label.includes.part.label" select="0" />
    <!-- === Line deviding header and ruler from textbody === -->
    <xsl:param name="header.rule" select="1" />
    <xsl:param name="footer.rule" select="1" />

    <!-- === Header === -->
    <xsl:attribute-set name="header.content.properties">
        <xsl:attribute name="font-family">
			<xsl:value-of select="$body.fontset"></xsl:value-of>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-variant">small-caps</xsl:attribute>
        <xsl:attribute name="font-size">11pt</xsl:attribute>
        <xsl:attribute name="margin-left">
			<xsl:value-of select="$title.margin.left"></xsl:value-of>
		</xsl:attribute>
    </xsl:attribute-set>

    <!-- === Footer === -->
    <xsl:attribute-set name="footer.content.properties">
        <xsl:attribute name="font-family">
			<xsl:value-of select="$body.fontset"></xsl:value-of>
		</xsl:attribute>
        <xsl:attribute name="font-size">10pt</xsl:attribute>
        <xsl:attribute name="margin-left">
			<xsl:value-of select="$title.margin.left"></xsl:value-of>
		</xsl:attribute>
    </xsl:attribute-set>

    <!-- === Glossary === -->
    <xsl:param name="glossentry.show.acronym" select="'primary'"></xsl:param>

    <!-- === Refentry === -->
    <xsl:param name="refentry.generate.name" select="0"></xsl:param>
    <xsl:param name="refentry.generate.title" select="1"></xsl:param>
    <xsl:param name="refentry.pagebreak" select="1"></xsl:param>

    <!-- === Varlist === -->
    <xsl:param name="variablelist.max.termlength">20</xsl:param>
    <xsl:param name="variablelist.as.blocks" select="1"></xsl:param>

    <!-- === Tables === -->
    <xsl:attribute-set name="table.cell.padding">
        <xsl:attribute name="padding-left">2pt</xsl:attribute>
        <xsl:attribute name="padding-right">2pt</xsl:attribute>
        <xsl:attribute name="padding-top">2pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">2pt</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="informal.object.properties">
        <xsl:attribute name="space-before.minimum">0.5em</xsl:attribute>
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">2em</xsl:attribute>
        <xsl:attribute name="space-after.minimum">0.5em</xsl:attribute>
        <xsl:attribute name="space-after.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-after.maximum">2em</xsl:attribute>
        <xsl:attribute name="keep-together.within-line">always</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="formal.object.properties">
        <xsl:attribute name="space-before.minimum">0.5em</xsl:attribute>
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">2em</xsl:attribute>
        <xsl:attribute name="space-after.minimum">0.5em</xsl:attribute>
        <xsl:attribute name="space-after.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-after.maximum">2em</xsl:attribute>
        <xsl:attribute name="keep-together.within-line">always</xsl:attribute>
    </xsl:attribute-set>

    <!-- === ULink === -->
    <xsl:param name="ulink.footnotes" select="0"></xsl:param>
    <xsl:param name="ulink.show" select="1"></xsl:param>

    <!-- === XREF === -->
    <xsl:param name="insert.xref.page.number">
        yes
    </xsl:param>
    <!-- Make xrefs and links blue -->
    <xsl:attribute-set name="xref.properties">
        <xsl:attribute name="color">
	  	<xsl:if test="local-name() = 'link'
	  					or local-name() = 'olink'
	  					or local-name() = 'ulink'
	  					or local-name() = 'xref'">
	  		blue
	  	</xsl:if>
	</xsl:attribute>
    </xsl:attribute-set>

    <!-- === Fonts === -->
    <xsl:param name="body.font.family" select="'sans-serif'" />
    <xsl:param name="title.font.family" select="'sans-serif'" />
    <xsl:param name="monospace.font.family" select="'monospace'" />
    <xsl:param name="body.font.master">11</xsl:param>
    <xsl:param name="body.font.size">
        <xsl:value-of select="$body.font.master" />
        <xsl:text>pt</xsl:text>
    </xsl:param>
    <xsl:param name="footnote.font.size">9pt</xsl:param>

    <!-- === Font-Styles === -->
    <xsl:attribute-set name="color.properties">
<!--        <xsl:attribute name="color">#990000</xsl:attribute>-->
    </xsl:attribute-set>
    <xsl:attribute-set name="admonition.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="index.div.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="refentry.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="formal.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="sidebar.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="margin.note.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="component.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="article.appendix.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="abstract.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="revhistory.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="section.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="header.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <xsl:attribute-set name="footer.title.properties" use-attribute-sets="color.properties"></xsl:attribute-set>
    <!-- Component titles -->
    <xsl:attribute-set name="component.title.properties" use-attribute-sets="color.properties">
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 2"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="space-before.optimum">14pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">12pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">14pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">
			<xsl:choose>
				<xsl:when test="parent::book or parent::part or parent::article or parent::chapter">140pt</xsl:when>
				<xsl:otherwise>24pt</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
        <xsl:attribute name="space-after.minimum">
			<xsl:choose>
				<xsl:when test="parent::book or parent::part or parent::article">80pt</xsl:when>
				<xsl:otherwise>16pt</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
        <xsl:attribute name="space-after.maximum">
			<xsl:choose>
				<xsl:when test="parent::book or parent::part or parent::article">160pt</xsl:when>
				<xsl:otherwise>30pt</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
        <xsl:attribute name="hyphenate">false</xsl:attribute>
        <xsl:attribute name="text-align">left</xsl:attribute>
        <xsl:attribute name="start-indent"><xsl:value-of select="$title.margin.left" /></xsl:attribute>
        <xsl:attribute name="color">#990000</xsl:attribute>
    </xsl:attribute-set>
    <!-- section level 1 -->
    <xsl:attribute-set name="section.title.level1.properties">
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.454545"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="space-before.optimum">16pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">12pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">20pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">12pt</xsl:attribute>
        <xsl:attribute name="space-after.minimum">8pt</xsl:attribute>
        <xsl:attribute name="space-after.maximum">16pt</xsl:attribute>
    </xsl:attribute-set>
    <!-- section level 2 -->
    <xsl:attribute-set name="section.title.level2.properties">
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.272727"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="space-before.optimum">14pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">8pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">20pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">6pt</xsl:attribute>
        <xsl:attribute name="space-after.minimum">4pt</xsl:attribute>
        <xsl:attribute name="space-after.maximum">6pt</xsl:attribute>
    </xsl:attribute-set>
    <!-- section level 3 -->
    <xsl:attribute-set name="section.title.level3.properties">
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.090909"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="space-before.optimum">12pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">8pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">16pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">6pt</xsl:attribute>
        <xsl:attribute name="space-after.minimum">4pt</xsl:attribute>
        <xsl:attribute name="space-after.maximum">6pt</xsl:attribute>
    </xsl:attribute-set>
    <!-- section level 4 -->
    <xsl:attribute-set name="section.title.level4.properties">
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.02"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="space-before.optimum">12pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">8pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">16pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">6pt</xsl:attribute>
        <xsl:attribute name="space-after.minimum">4pt</xsl:attribute>
        <xsl:attribute name="space-after.maximum">6pt</xsl:attribute>
    </xsl:attribute-set>
    <!-- refentry -->
    <xsl:attribute-set name="refentry.title.properties">
        <xsl:attribute name="font-size">
			<xsl:value-of select="$body.font.master * 1.8"></xsl:value-of>
			<xsl:text>pt</xsl:text>
		</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="font-style">italic</xsl:attribute>
        <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
        <xsl:attribute name="hyphenate">false</xsl:attribute>
        <xsl:attribute name="space-before.optimum">16pt</xsl:attribute>
        <xsl:attribute name="space-before.minimum">12pt</xsl:attribute>
        <xsl:attribute name="space-before.maximum">20pt</xsl:attribute>
        <xsl:attribute name="space-after.optimum">30pt</xsl:attribute>
        <xsl:attribute name="space-after.minimum">20pt</xsl:attribute>
        <xsl:attribute name="space-after.maximum">40pt</xsl:attribute>
        <xsl:attribute name="start-indent"><xsl:value-of select="$title.margin.left" /></xsl:attribute>
    </xsl:attribute-set>
    <!-- Verbatim text formatting (programlistings) -->
    <xsl:attribute-set name="monospace.verbatim.properties">
        <xsl:attribute name="font-size"><xsl:value-of
            select="$body.font.master * 0.8333" /><xsl:text>pt</xsl:text></xsl:attribute>
        <xsl:attribute name="space-before.minimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">1em</xsl:attribute>
        <xsl:attribute name="border-color">#444444</xsl:attribute>
        <xsl:attribute name="border-style">solid</xsl:attribute>
        <xsl:attribute name="border-width">0.1pt</xsl:attribute>
        <xsl:attribute name="padding-top">0.5em</xsl:attribute>
        <xsl:attribute name="padding-left">0.5em</xsl:attribute>
        <xsl:attribute name="padding-right">0.5em</xsl:attribute>
        <xsl:attribute name="padding-bottom">0.5em</xsl:attribute>
        <xsl:attribute name="margin-left">0.5em</xsl:attribute>
        <xsl:attribute name="margin-right">0.5em</xsl:attribute>
    </xsl:attribute-set>

    <!-- Shade (background) programlistings -->
    <xsl:param name="shade.verbatim">1</xsl:param>
    <xsl:attribute-set name="shade.verbatim.style">
        <xsl:attribute name="background-color">#F0F0F0</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="book.titlepage.recto.address.style">
        <xsl:attribute name="font-size"><xsl:value-of select="$body.font.master" /><xsl:text>pt</xsl:text></xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="font-family"><xsl:value-of select="$body.fontset" /></xsl:attribute>
        <xsl:attribute name="text-align">right</xsl:attribute>
        <xsl:attribute name="space-before.minimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-before.maximum">1em</xsl:attribute>
        <xsl:attribute name="space-after.minimum">1em</xsl:attribute>
        <xsl:attribute name="space-after.optimum">1em</xsl:attribute>
        <xsl:attribute name="space-after.maximum">1em</xsl:attribute>
        <xsl:attribute name="hyphenate">false</xsl:attribute>
        <xsl:attribute name="linefeed-treatment">preserve</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="normal.para.spacing">
        <xsl:attribute name="text-align">justify</xsl:attribute>
    </xsl:attribute-set>

    <!-- Code highlighting -->
    <xsl:param name="highlight.source" select="1" />

</xsl:stylesheet>
