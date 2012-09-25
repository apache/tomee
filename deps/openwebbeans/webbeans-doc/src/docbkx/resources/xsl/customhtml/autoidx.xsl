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
<!DOCTYPE xsl:stylesheet [

<!ENTITY lowercase "'abcdefghijklmnopqrstuvwxyz'">
<!ENTITY uppercase "'ABCDEFGHIJKLMNOPQRSTUVWXYZ'">

<!ENTITY primary   'normalize-space(concat(primary/@sortas, primary[not(@sortas)]))'>
<!ENTITY secondary 'normalize-space(concat(secondary/@sortas, secondary[not(@sortas)]))'>
<!ENTITY tertiary  'normalize-space(concat(tertiary/@sortas, tertiary[not(@sortas)]))'>

<!ENTITY section   '(ancestor-or-self::set
                     |ancestor-or-self::book
                     |ancestor-or-self::part
                     |ancestor-or-self::reference
                     |ancestor-or-self::partintro
                     |ancestor-or-self::chapter
                     |ancestor-or-self::appendix
                     |ancestor-or-self::preface
                     |ancestor-or-self::article
                     |ancestor-or-self::section
                     |ancestor-or-self::sect1
                     |ancestor-or-self::sect2
                     |ancestor-or-self::sect3
                     |ancestor-or-self::sect4
                     |ancestor-or-self::sect5
                     |ancestor-or-self::refentry
                     |ancestor-or-self::refsect1
                     |ancestor-or-self::refsect2
                     |ancestor-or-self::refsect3
                     |ancestor-or-self::simplesect
                     |ancestor-or-self::bibliography
                     |ancestor-or-self::glossary
                     |ancestor-or-self::index
                     |ancestor-or-self::webpage)[last()]'>

<!ENTITY section.id 'generate-id(&section;)'>
<!ENTITY sep '" "'>
<!ENTITY scope 'count(ancestor::node()|$scope) = count(ancestor::node())
                and ($role = @role or $type = @type or
                (string-length($role) = 0 and string-length($type) = 0))'>
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:key name="letter" match="indexterm"
		use="translate(substring(&primary;, 1, 1),&lowercase;,&uppercase;)"/>

	<xsl:key name="primary" match="indexterm" use="&primary;"/>

	<xsl:key name="secondary" match="indexterm"
		use="concat(&primary;, &sep;, &secondary;)"/>

	<xsl:key name="tertiary" match="indexterm"
		use="concat(&primary;, &sep;, &secondary;, &sep;, &tertiary;)"/>

	<xsl:key name="endofrange" match="indexterm[@class='endofrange']" use="@startref"/>

	<xsl:key name="primary-section" match="indexterm[not(secondary) and not(see)]"
		use="concat(&primary;, &sep;, &section.id;)"/>

	<xsl:key name="secondary-section" match="indexterm[not(tertiary) and not(see)]"
		use="concat(&primary;, &sep;, &secondary;, &sep;, &section.id;)"/>

	<xsl:key name="tertiary-section" match="indexterm[not(see)]"
		use="concat(&primary;, &sep;, &secondary;, &sep;, &tertiary;, &sep;, &section.id;)"/>

	<xsl:key name="see-also" match="indexterm[seealso]"
		use="concat(&primary;, &sep;, &secondary;, &sep;, &tertiary;, &sep;, seealso)"/>

	<xsl:key name="see" match="indexterm[see]"
		use="concat(&primary;, &sep;, &secondary;, &sep;, &tertiary;, &sep;, see)"/>

	<xsl:key name="sections" match="*[@id]" use="@id"/>

	<xsl:template match="indexterm" mode="reference">
		<xsl:param name="scope" select="."/>
		<xsl:param name="role" select="''"/>
		<xsl:param name="type" select="''"/>
		<xsl:param name="separator" select="', '"/>

		<xsl:value-of select="$separator"/>
		<xsl:choose>
			<xsl:when test="@zone and string(@zone)">
				<xsl:call-template name="reference">
					<xsl:with-param name="zones" select="normalize-space(@zone)"/>
					<xsl:with-param name="scope" select="$scope"/>
					<xsl:with-param name="role" select="$role"/>
					<xsl:with-param name="type" select="$type"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<a>
					<xsl:variable name="title">
						<xsl:choose>
							<xsl:when test="&section;/titleabbrev and $index.prefer.titleabbrev != 0">
								<xsl:apply-templates select="&section;" mode="titleabbrev.markup"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="&section;" mode="title.markup"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>

					<xsl:attribute name="href">
						<xsl:call-template name="href.target">
							<xsl:with-param name="object" select="&section;"/>
							<xsl:with-param name="context" select="//index[&scope;][1]"/>
						</xsl:call-template>
					</xsl:attribute>

					<xsl:value-of select="$title"/>
					<!-- text only -->
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
