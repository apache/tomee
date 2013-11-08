/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package openbook.tools.parser;
// $ANTLR 3.2 Sep 23, 2009 12:02:23 Java.g 2010-05-15 01:06:37

import org.antlr.runtime.*;
import java.util.HashMap;
import org.antlr.runtime.debug.*;
import java.io.IOException;

/** A Java 1.5 grammar for ANTLR v3 derived from the spec
 *
 *  This is a very close representation of the spec; the changes
 *  are comestic (remove left recursion) and also fixes (the spec
 *  isn't exactly perfect).  I have run this on the 1.4.2 source
 *  and some nasty looking enums from 1.5, but have not really
 *  tested for 1.5 compatibility.
 *
 *  I built this with: java -Xmx100M org.antlr.Tool java.g 
 *  and got two errors that are ok (for now):
 *  java.g:691:9: Decision can match input such as
 *    "'0'..'9'{'E', 'e'}{'+', '-'}'0'..'9'{'D', 'F', 'd', 'f'}"
 *    using multiple alternatives: 3, 4
 *  As a result, alternative(s) 4 were disabled for that input
 *  java.g:734:35: Decision can match input such as "{'$', 'A'..'Z',
 *    '_', 'a'..'z', '\u00C0'..'\u00D6', '\u00D8'..'\u00F6',
 *    '\u00F8'..'\u1FFF', '\u3040'..'\u318F', '\u3300'..'\u337F',
 *    '\u3400'..'\u3D2D', '\u4E00'..'\u9FFF', '\uF900'..'\uFAFF'}"
 *    using multiple alternatives: 1, 2
 *  As a result, alternative(s) 2 were disabled for that input
 *
 *  You can turn enum on/off as a keyword :)
 *
 *  Version 1.0 -- initial release July 5, 2006 (requires 3.0b2 or higher)
 *
 *  Primary author: Terence Parr, July 2006
 *
 *  Version 1.0.1 -- corrections by Koen Vanderkimpen & Marko van Dooren,
 *      October 25, 2006;
 *      fixed normalInterfaceDeclaration: now uses typeParameters instead
 *          of typeParameter (according to JLS, 3rd edition)
 *      fixed castExpression: no longer allows expression next to type
 *          (according to semantics in JLS, in contrast with syntax in JLS)
 *
 *  Version 1.0.2 -- Terence Parr, Nov 27, 2006
 *      java spec I built this from had some bizarre for-loop control.
 *          Looked weird and so I looked elsewhere...Yep, it's messed up.
 *          simplified.
 *
 *  Version 1.0.3 -- Chris Hogue, Feb 26, 2007
 *      Factored out an annotationName rule and used it in the annotation rule.
 *          Not sure why, but typeName wasn't recognizing references to inner
 *          annotations (e.g. @InterfaceName.InnerAnnotation())
 *      Factored out the elementValue section of an annotation reference.  Created 
 *          elementValuePair and elementValuePairs rules, then used them in the 
 *          annotation rule.  Allows it to recognize annotation references with 
 *          multiple, comma separated attributes.
 *      Updated elementValueArrayInitializer so that it allows multiple elements.
 *          (It was only allowing 0 or 1 element).
 *      Updated localVariableDeclaration to allow annotations.  Interestingly the JLS
 *          doesn't appear to indicate this is legal, but it does work as of at least
 *          JDK 1.5.0_06.
 *      Moved the Identifier portion of annotationTypeElementRest to annotationMethodRest.
 *          Because annotationConstantRest already references variableDeclarator which 
 *          has the Identifier portion in it, the parser would fail on constants in 
 *          annotation definitions because it expected two identifiers.  
 *      Added optional trailing ';' to the alternatives in annotationTypeElementRest.
 *          Wouldn't handle an inner interface that has a trailing ';'.
 *      Swapped the expression and type rule reference order in castExpression to 
 *          make it check for genericized casts first.  It was failing to recognize a
 *          statement like  "Class<Byte> TYPE = (Class<Byte>)...;" because it was seeing
 *          'Class<Byte' in the cast expression as a less than expression, then failing 
 *          on the '>'.
 *      Changed createdName to use typeArguments instead of nonWildcardTypeArguments.
 *          Again, JLS doesn't seem to allow this, but java.lang.Class has an example of
 *          of this construct.
 *      Changed the 'this' alternative in primary to allow 'identifierSuffix' rather than
 *          just 'arguments'.  The case it couldn't handle was a call to an explicit
 *          generic method invocation (e.g. this.<E>doSomething()).  Using identifierSuffix
 *          may be overly aggressive--perhaps should create a more constrained thisSuffix rule?
 *      
 *  Version 1.0.4 -- Hiroaki Nakamura, May 3, 2007
 *
 *  Fixed formalParameterDecls, localVariableDeclaration, forInit,
 *  and forVarControl to use variableModifier* not 'final'? (annotation)?
 *
 *  Version 1.0.5 -- Terence, June 21, 2007
 *  --a[i].foo didn't work. Fixed unaryExpression
 *
 *  Version 1.0.6 -- John Ridgway, March 17, 2008
 *      Made "assert" a switchable keyword like "enum".
 *      Fixed compilationUnit to disallow "annotation importDeclaration ...".
 *      Changed "Identifier ('.' Identifier)*" to "qualifiedName" in more 
 *          places.
 *      Changed modifier* and/or variableModifier* to classOrInterfaceModifiers,
 *          modifiers or variableModifiers, as appropriate.
 *      Renamed "bound" to "typeBound" to better match language in the JLS.
 *      Added "memberDeclaration" which rewrites to methodDeclaration or 
 *      fieldDeclaration and pulled type into memberDeclaration.  So we parse 
 *          type and then move on to decide whether we're dealing with a field
 *          or a method.
 *      Modified "constructorDeclaration" to use "constructorBody" instead of
 *          "methodBody".  constructorBody starts with explicitConstructorInvocation,
 *          then goes on to blockStatement*.  Pulling explicitConstructorInvocation
 *          out of expressions allowed me to simplify "primary".
 *      Changed variableDeclarator to simplify it.
 *      Changed type to use classOrInterfaceType, thus simplifying it; of course
 *          I then had to add classOrInterfaceType, but it is used in several 
 *          places.
 *      Fixed annotations, old version allowed "@X(y,z)", which is illegal.
 *      Added optional comma to end of "elementValueArrayInitializer"; as per JLS.
 *      Changed annotationTypeElementRest to use normalClassDeclaration and 
 *          normalInterfaceDeclaration rather than classDeclaration and 
 *          interfaceDeclaration, thus getting rid of a couple of grammar ambiguities.
 *      Split localVariableDeclaration into localVariableDeclarationStatement
 *          (includes the terminating semi-colon) and localVariableDeclaration.  
 *          This allowed me to use localVariableDeclaration in "forInit" clauses,
 *           simplifying them.
 *      Changed switchBlockStatementGroup to use multiple labels.  This adds an
 *          ambiguity, but if one uses appropriately greedy parsing it yields the
 *           parse that is closest to the meaning of the switch statement.
 *      Renamed "forVarControl" to "enhancedForControl" -- JLS language.
 *      Added semantic predicates to test for shift operations rather than other
 *          things.  Thus, for instance, the string "< <" will never be treated
 *          as a left-shift operator.
 *      In "creator" we rule out "nonWildcardTypeArguments" on arrayCreation, 
 *          which are illegal.
 *      Moved "nonWildcardTypeArguments into innerCreator.
 *      Removed 'super' superSuffix from explicitGenericInvocation, since that
 *          is only used in explicitConstructorInvocation at the beginning of a
 *           constructorBody.  (This is part of the simplification of expressions
 *           mentioned earlier.)
 *      Simplified primary (got rid of those things that are only used in
 *          explicitConstructorInvocation).
 *      Lexer -- removed "Exponent?" from FloatingPointLiteral choice 4, since it
 *          led to an ambiguity.
 *
 *      This grammar successfully parses every .java file in the JDK 1.5 source 
 *          tree (excluding those whose file names include '-', which are not
 *          valid Java compilation units).
 *
 *  Known remaining problems:
 *      "Letter" and "JavaIDDigit" are wrong.  The actual specification of
 *      "Letter" should be "a character for which the method
 *      Character.isJavaIdentifierStart(int) returns true."  A "Java 
 *      letter-or-digit is a character for which the method 
 *      Character.isJavaIdentifierPart(int) returns true."
 */
public class JavaParser extends DebugParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Identifier", "ENUM", "FloatingPointLiteral", "CharacterLiteral", "StringLiteral", "HexLiteral", "OctalLiteral", "DecimalLiteral", "ASSERT", "HexDigit", "IntegerTypeSuffix", "Exponent", "FloatTypeSuffix", "EscapeSequence", "UnicodeEscape", "OctalEscape", "Letter", "JavaIDDigit", "WS", "COMMENT", "LINE_COMMENT", "'package'", "';'", "'import'", "'static'", "'.'", "'*'", "'public'", "'protected'", "'private'", "'abstract'", "'final'", "'strictfp'", "'class'", "'extends'", "'implements'", "'<'", "','", "'>'", "'&'", "'{'", "'}'", "'interface'", "'void'", "'['", "']'", "'throws'", "'='", "'native'", "'synchronized'", "'transient'", "'volatile'", "'boolean'", "'char'", "'byte'", "'short'", "'int'", "'long'", "'float'", "'double'", "'?'", "'super'", "'('", "')'", "'...'", "'this'", "'null'", "'true'", "'false'", "'@'", "'default'", "':'", "'if'", "'else'", "'for'", "'while'", "'do'", "'try'", "'finally'", "'switch'", "'return'", "'throw'", "'break'", "'continue'", "'catch'", "'case'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'||'", "'&&'", "'|'", "'^'", "'=='", "'!='", "'instanceof'", "'+'", "'-'", "'/'", "'%'", "'++'", "'--'", "'~'", "'!'", "'new'"
    };
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int FloatTypeSuffix=16;
    public static final int T__25=25;
    public static final int OctalLiteral=10;
    public static final int EOF=-1;
    public static final int Identifier=4;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int COMMENT=23;
    public static final int T__99=99;
    public static final int T__98=98;
    public static final int T__97=97;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int LINE_COMMENT=24;
    public static final int IntegerTypeSuffix=14;
    public static final int T__85=85;
    public static final int T__84=84;
    public static final int ASSERT=12;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int T__88=88;
    public static final int WS=22;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__70=70;
    public static final int FloatingPointLiteral=6;
    public static final int JavaIDDigit=21;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int Letter=20;
    public static final int EscapeSequence=17;
    public static final int T__73=73;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int CharacterLiteral=7;
    public static final int Exponent=15;
    public static final int T__61=61;
    public static final int T__60=60;
    public static final int HexDigit=13;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__107=107;
    public static final int T__108=108;
    public static final int T__109=109;
    public static final int T__59=59;
    public static final int T__103=103;
    public static final int T__104=104;
    public static final int T__105=105;
    public static final int T__106=106;
    public static final int T__111=111;
    public static final int T__110=110;
    public static final int T__113=113;
    public static final int T__112=112;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int HexLiteral=9;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__102=102;
    public static final int T__101=101;
    public static final int T__100=100;
    public static final int DecimalLiteral=11;
    public static final int StringLiteral=8;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int ENUM=5;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int UnicodeEscape=18;
    public static final int OctalEscape=19;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "synpred179_Java", "block", "synpred250_Java", "synpred137_Java", 
        "synpred169_Java", "synpred154_Java", "catches", "relationalExpression", 
        "synpred237_Java", "synpred105_Java", "synpred88_Java", "expression", 
        "synpred242_Java", "synpred138_Java", "synpred151_Java", "typeList", 
        "formalParameterDecls", "interfaceBodyDeclaration", "synpred74_Java", 
        "typeName", "classDeclaration", "synpred252_Java", "selector", "synpred25_Java", 
        "synpred7_Java", "modifiers", "exclusiveOrExpression", "synpred11_Java", 
        "synpred164_Java", "variableModifiers", "synpred261_Java", "synpred42_Java", 
        "synpred128_Java", "synpred245_Java", "synpred264_Java", "synpred100_Java", 
        "synpred206_Java", "elementValuePair", "packageDeclaration", "variableModifier", 
        "synpred196_Java", "synpred203_Java", "arguments", "synpred149_Java", 
        "booleanLiteral", "synpred254_Java", "synpred96_Java", "synpred176_Java", 
        "synpred195_Java", "innerCreator", "compilationUnit", "synpred22_Java", 
        "integerLiteral", "annotationTypeDeclaration", "synpred165_Java", 
        "synpred55_Java", "synpred106_Java", "synpred158_Java", "annotationMethodOrConstantRest", 
        "synpred6_Java", "instanceOfExpression", "enumBody", "synpred262_Java", 
        "synpred117_Java", "synpred228_Java", "synpred269_Java", "synpred97_Java", 
        "synpred112_Java", "interfaceMethodOrFieldDecl", "switchBlockStatementGroups", 
        "synpred210_Java", "synpred44_Java", "synpred188_Java", "statementExpression", 
        "annotationTypeBody", "synpred170_Java", "synpred120_Java", "interfaceGenericMethodDecl", 
        "synpred89_Java", "synpred205_Java", "synpred230_Java", "arrayCreatorRest", 
        "synpred75_Java", "synpred130_Java", "annotationTypeElementRest", 
        "creator", "variableDeclarator", "synpred93_Java", "synpred115_Java", 
        "synpred119_Java", "synpred110_Java", "synpred186_Java", "synpred265_Java", 
        "synpred202_Java", "synpred253_Java", "andExpression", "synpred32_Java", 
        "synpred135_Java", "synpred148_Java", "synpred132_Java", "conditionalOrExpression", 
        "relationalOp", "synpred21_Java", "qualifiedNameList", "synpred58_Java", 
        "qualifiedName", "synpred51_Java", "conditionalExpression", "synpred175_Java", 
        "synpred90_Java", "synpred181_Java", "synpred31_Java", "shiftExpression", 
        "synpred197_Java", "fieldDeclaration", "synpred17_Java", "synpred231_Java", 
        "literal", "expressionList", "classBody", "synpred3_Java", "synpred160_Java", 
        "synpred190_Java", "synpred20_Java", "synpred219_Java", "synpred235_Java", 
        "synpred184_Java", "synpred222_Java", "synpred94_Java", "synpred147_Java", 
        "elementValue", "synpred64_Java", "synpred180_Java", "synpred82_Java", 
        "synpred30_Java", "synpred26_Java", "statement", "inclusiveOrExpression", 
        "multiplicativeExpression", "switchLabel", "synpred168_Java", "blockStatement", 
        "synpred111_Java", "synpred270_Java", "synpred134_Java", "synpred236_Java", 
        "enhancedForControl", "synpred213_Java", "synpred62_Java", "synpred227_Java", 
        "classCreatorRest", "synpred54_Java", "synpred104_Java", "synpred136_Java", 
        "synpred191_Java", "interfaceMemberDecl", "enumConstantName", "synpred67_Java", 
        "synpred49_Java", "typeParameters", "synpred123_Java", "synpred50_Java", 
        "synpred15_Java", "classOrInterfaceModifiers", "catchClause", "shiftOp", 
        "synpred63_Java", "synpred86_Java", "synpred212_Java", "normalClassDeclaration", 
        "methodDeclaratorRest", "conditionalAndExpression", "synpred79_Java", 
        "synpred239_Java", "synpred260_Java", "equalityExpression", "synpred161_Java", 
        "synpred221_Java", "annotationMethodRest", "synpred214_Java", "synpred113_Java", 
        "synpred92_Java", "synpred101_Java", "synpred234_Java", "synpred272_Java", 
        "synpred258_Java", "synpred95_Java", "synpred10_Java", "synpred125_Java", 
        "enumBodyDeclarations", "additiveExpression", "synpred133_Java", 
        "synpred14_Java", "interfaceMethodDeclaratorRest", "synpred248_Java", 
        "synpred208_Java", "formalParameterDeclsRest", "primary", "synpred78_Java", 
        "normalInterfaceDeclaration", "synpred145_Java", "synpred85_Java", 
        "synpred33_Java", "localVariableDeclaration", "synpred211_Java", 
        "synpred204_Java", "primitiveType", "switchBlockStatementGroup", 
        "synpred40_Java", "interfaceDeclaration", "formalParameters", "synpred238_Java", 
        "synpred99_Java", "synpred56_Java", "synpred177_Java", "synpred37_Java", 
        "synpred249_Java", "synpred46_Java", "identifierSuffix", "synpred116_Java", 
        "synpred69_Java", "synpred259_Java", "synpred65_Java", "synpred80_Java", 
        "synpred68_Java", "synpred66_Java", "elementValuePairs", "synpred71_Java", 
        "annotation", "synpred126_Java", "synpred102_Java", "synpred223_Java", 
        "synpred87_Java", "synpred27_Java", "synpred34_Java", "arrayInitializer", 
        "synpred77_Java", "typeArguments", "synpred187_Java", "synpred140_Java", 
        "variableDeclarators", "memberDecl", "voidMethodDeclaratorRest", 
        "enumConstant", "typeBound", "synpred70_Java", "synpred224_Java", 
        "constructorBody", "synpred84_Java", "interfaceBody", "classOrInterfaceDeclaration", 
        "synpred166_Java", "annotationName", "synpred251_Java", "synpred226_Java", 
        "synpred189_Java", "synpred246_Java", "forControl", "synpred108_Java", 
        "explicitConstructorInvocation", "synpred157_Java", "synpred4_Java", 
        "memberDeclaration", "synpred12_Java", "synpred243_Java", "synpred201_Java", 
        "parExpression", "synpred209_Java", "typeArgument", "synpred73_Java", 
        "synpred167_Java", "synpred53_Java", "synpred59_Java", "classBodyDeclaration", 
        "synpred233_Java", "explicitGenericInvocation", "synpred244_Java", 
        "synpred215_Java", "synpred139_Java", "synpred122_Java", "synpred266_Java", 
        "classOrInterfaceModifier", "synpred267_Java", "genericMethodOrConstructorDecl", 
        "synpred271_Java", "synpred183_Java", "synpred216_Java", "nonWildcardTypeArguments", 
        "variableDeclaratorId", "synpred131_Java", "constructorDeclaratorRest", 
        "synpred268_Java", "typeDeclaration", "annotationTypeElementDeclaration", 
        "synpred103_Java", "synpred127_Java", "synpred263_Java", "synpred23_Java", 
        "synpred150_Java", "defaultValue", "constantDeclarator", "genericMethodOrConstructorRest", 
        "synpred72_Java", "synpred217_Java", "synpred8_Java", "constantExpression", 
        "synpred52_Java", "synpred152_Java", "createdName", "synpred178_Java", 
        "synpred156_Java", "synpred162_Java", "synpred232_Java", "synpred118_Java", 
        "synpred91_Java", "synpred19_Java", "synpred29_Java", "elementValueArrayInitializer", 
        "synpred61_Java", "synpred141_Java", "synpred114_Java", "unaryExpression", 
        "synpred45_Java", "synpred16_Java", "synpred229_Java", "synpred2_Java", 
        "enumConstants", "synpred194_Java", "synpred43_Java", "importDeclaration", 
        "localVariableDeclarationStatement", "synpred121_Java", "forUpdate", 
        "synpred60_Java", "synpred143_Java", "forInit", "constantDeclaratorsRest", 
        "annotationConstantRest", "synpred36_Java", "superSuffix", "synpred39_Java", 
        "enumDeclaration", "synpred192_Java", "castExpression", "synpred1_Java", 
        "formalParameter", "synpred107_Java", "synpred155_Java", "synpred163_Java", 
        "synpred83_Java", "synpred129_Java", "synpred146_Java", "synpred207_Java", 
        "synpred13_Java", "synpred218_Java", "classOrInterfaceType", "synpred109_Java", 
        "synpred9_Java", "assignmentOperator", "synpred255_Java", "constantDeclaratorRest", 
        "variableInitializer", "synpred256_Java", "synpred28_Java", "synpred35_Java", 
        "synpred193_Java", "synpred174_Java", "synpred124_Java", "modifier", 
        "synpred81_Java", "synpred159_Java", "synpred185_Java", "synpred38_Java", 
        "synpred144_Java", "synpred199_Java", "typeParameter", "annotations", 
        "synpred173_Java", "synpred172_Java", "synpred18_Java", "packageOrTypeName", 
        "synpred241_Java", "synpred153_Java", "synpred247_Java", "synpred48_Java", 
        "synpred47_Java", "synpred98_Java", "synpred5_Java", "interfaceMethodOrFieldRest", 
        "synpred57_Java", "methodDeclaration", "synpred76_Java", "methodBody", 
        "synpred41_Java", "synpred142_Java", "synpred198_Java", "synpred225_Java", 
        "unaryExpressionNotPlusMinus", "synpred240_Java", "synpred200_Java", 
        "voidInterfaceMethodDeclaratorRest", "synpred182_Java", "synpred24_Java", 
        "synpred257_Java", "type", "synpred220_Java", "synpred171_Java"
    };
     
        public int ruleLevel = 0;
        public int getRuleLevel() { return ruleLevel; }
        public void incRuleLevel() { ruleLevel++; }
        public void decRuleLevel() { ruleLevel--; }
        public JavaParser(TokenStream input) {
            this(input, DebugEventSocketProxy.DEFAULT_DEBUGGER_PORT, new RecognizerSharedState());
        }
        public JavaParser(TokenStream input, int port, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[407+1];
             
            DebugEventSocketProxy proxy =
                new DebugEventSocketProxy(this, port, null);
            setDebugListener(proxy);
            try {
                proxy.handshake();
            }
            catch (IOException ioe) {
                reportError(ioe);
            }
        }
    public JavaParser(TokenStream input, DebugEventListener dbg) {
        super(input, dbg, new RecognizerSharedState());
        this.state.ruleMemo = new HashMap[407+1];
         
    }
    protected boolean evalPredicate(boolean result, String predicate) {
        dbg.semanticPredicate(result, predicate);
        return result;
    }


    public String[] getTokenNames() { return JavaParser.tokenNames; }
    public String getGrammarFileName() { return "Java.g"; }



    // $ANTLR start "compilationUnit"
    // Java.g:177:1: compilationUnit : ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* );
    public final void compilationUnit() throws RecognitionException {
        int compilationUnit_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "compilationUnit");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(177, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return ; }
            // Java.g:178:5: ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* )
            int alt8=2;
            try { dbg.enterDecision(8);

            try {
                isCyclicDecision = true;
                alt8 = dfa8.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(8);}

            switch (alt8) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:178:9: annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
                    {
                    dbg.location(178,9);
                    pushFollow(FOLLOW_annotations_in_compilationUnit44);
                    annotations();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(179,9);
                    // Java.g:179:9: ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
                    int alt4=2;
                    try { dbg.enterSubRule(4);
                    try { dbg.enterDecision(4);

                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==25) ) {
                        alt4=1;
                    }
                    else if ( (LA4_0==ENUM||LA4_0==28||(LA4_0>=31 && LA4_0<=37)||LA4_0==46||LA4_0==73) ) {
                        alt4=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 0, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(4);}

                    switch (alt4) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:179:13: packageDeclaration ( importDeclaration )* ( typeDeclaration )*
                            {
                            dbg.location(179,13);
                            pushFollow(FOLLOW_packageDeclaration_in_compilationUnit58);
                            packageDeclaration();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(179,32);
                            // Java.g:179:32: ( importDeclaration )*
                            try { dbg.enterSubRule(1);

                            loop1:
                            do {
                                int alt1=2;
                                try { dbg.enterDecision(1);

                                int LA1_0 = input.LA(1);

                                if ( (LA1_0==27) ) {
                                    alt1=1;
                                }


                                } finally {dbg.exitDecision(1);}

                                switch (alt1) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // Java.g:0:0: importDeclaration
                            	    {
                            	    dbg.location(179,32);
                            	    pushFollow(FOLLOW_importDeclaration_in_compilationUnit60);
                            	    importDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop1;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(1);}

                            dbg.location(179,51);
                            // Java.g:179:51: ( typeDeclaration )*
                            try { dbg.enterSubRule(2);

                            loop2:
                            do {
                                int alt2=2;
                                try { dbg.enterDecision(2);

                                int LA2_0 = input.LA(1);

                                if ( (LA2_0==ENUM||LA2_0==26||LA2_0==28||(LA2_0>=31 && LA2_0<=37)||LA2_0==46||LA2_0==73) ) {
                                    alt2=1;
                                }


                                } finally {dbg.exitDecision(2);}

                                switch (alt2) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // Java.g:0:0: typeDeclaration
                            	    {
                            	    dbg.location(179,51);
                            	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit63);
                            	    typeDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop2;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(2);}


                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:180:13: classOrInterfaceDeclaration ( typeDeclaration )*
                            {
                            dbg.location(180,13);
                            pushFollow(FOLLOW_classOrInterfaceDeclaration_in_compilationUnit78);
                            classOrInterfaceDeclaration();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(180,41);
                            // Java.g:180:41: ( typeDeclaration )*
                            try { dbg.enterSubRule(3);

                            loop3:
                            do {
                                int alt3=2;
                                try { dbg.enterDecision(3);

                                int LA3_0 = input.LA(1);

                                if ( (LA3_0==ENUM||LA3_0==26||LA3_0==28||(LA3_0>=31 && LA3_0<=37)||LA3_0==46||LA3_0==73) ) {
                                    alt3=1;
                                }


                                } finally {dbg.exitDecision(3);}

                                switch (alt3) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // Java.g:0:0: typeDeclaration
                            	    {
                            	    dbg.location(180,41);
                            	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit80);
                            	    typeDeclaration();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop3;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(3);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(4);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:182:9: ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )*
                    {
                    dbg.location(182,9);
                    // Java.g:182:9: ( packageDeclaration )?
                    int alt5=2;
                    try { dbg.enterSubRule(5);
                    try { dbg.enterDecision(5);

                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==25) ) {
                        alt5=1;
                    }
                    } finally {dbg.exitDecision(5);}

                    switch (alt5) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: packageDeclaration
                            {
                            dbg.location(182,9);
                            pushFollow(FOLLOW_packageDeclaration_in_compilationUnit101);
                            packageDeclaration();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(5);}

                    dbg.location(182,29);
                    // Java.g:182:29: ( importDeclaration )*
                    try { dbg.enterSubRule(6);

                    loop6:
                    do {
                        int alt6=2;
                        try { dbg.enterDecision(6);

                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==27) ) {
                            alt6=1;
                        }


                        } finally {dbg.exitDecision(6);}

                        switch (alt6) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:0:0: importDeclaration
                    	    {
                    	    dbg.location(182,29);
                    	    pushFollow(FOLLOW_importDeclaration_in_compilationUnit104);
                    	    importDeclaration();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(6);}

                    dbg.location(182,48);
                    // Java.g:182:48: ( typeDeclaration )*
                    try { dbg.enterSubRule(7);

                    loop7:
                    do {
                        int alt7=2;
                        try { dbg.enterDecision(7);

                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==ENUM||LA7_0==26||LA7_0==28||(LA7_0>=31 && LA7_0<=37)||LA7_0==46||LA7_0==73) ) {
                            alt7=1;
                        }


                        } finally {dbg.exitDecision(7);}

                        switch (alt7) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:0:0: typeDeclaration
                    	    {
                    	    dbg.location(182,48);
                    	    pushFollow(FOLLOW_typeDeclaration_in_compilationUnit107);
                    	    typeDeclaration();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(7);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, compilationUnit_StartIndex); }
        }
        dbg.location(183, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "compilationUnit");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "compilationUnit"


    // $ANTLR start "packageDeclaration"
    // Java.g:185:1: packageDeclaration : 'package' qualifiedName ';' ;
    public final void packageDeclaration() throws RecognitionException {
        int packageDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "packageDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(185, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return ; }
            // Java.g:186:5: ( 'package' qualifiedName ';' )
            dbg.enterAlt(1);

            // Java.g:186:9: 'package' qualifiedName ';'
            {
            dbg.location(186,9);
            match(input,25,FOLLOW_25_in_packageDeclaration127); if (state.failed) return ;
            dbg.location(186,19);
            pushFollow(FOLLOW_qualifiedName_in_packageDeclaration129);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(186,33);
            match(input,26,FOLLOW_26_in_packageDeclaration131); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, packageDeclaration_StartIndex); }
        }
        dbg.location(187, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "packageDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "packageDeclaration"


    // $ANTLR start "importDeclaration"
    // Java.g:189:1: importDeclaration : 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';' ;
    public final void importDeclaration() throws RecognitionException {
        int importDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "importDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(189, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return ; }
            // Java.g:190:5: ( 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';' )
            dbg.enterAlt(1);

            // Java.g:190:9: 'import' ( 'static' )? qualifiedName ( '.' '*' )? ';'
            {
            dbg.location(190,9);
            match(input,27,FOLLOW_27_in_importDeclaration154); if (state.failed) return ;
            dbg.location(190,18);
            // Java.g:190:18: ( 'static' )?
            int alt9=2;
            try { dbg.enterSubRule(9);
            try { dbg.enterDecision(9);

            int LA9_0 = input.LA(1);

            if ( (LA9_0==28) ) {
                alt9=1;
            }
            } finally {dbg.exitDecision(9);}

            switch (alt9) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: 'static'
                    {
                    dbg.location(190,18);
                    match(input,28,FOLLOW_28_in_importDeclaration156); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(190,28);
            pushFollow(FOLLOW_qualifiedName_in_importDeclaration159);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(190,42);
            // Java.g:190:42: ( '.' '*' )?
            int alt10=2;
            try { dbg.enterSubRule(10);
            try { dbg.enterDecision(10);

            int LA10_0 = input.LA(1);

            if ( (LA10_0==29) ) {
                alt10=1;
            }
            } finally {dbg.exitDecision(10);}

            switch (alt10) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:190:43: '.' '*'
                    {
                    dbg.location(190,43);
                    match(input,29,FOLLOW_29_in_importDeclaration162); if (state.failed) return ;
                    dbg.location(190,47);
                    match(input,30,FOLLOW_30_in_importDeclaration164); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(10);}

            dbg.location(190,53);
            match(input,26,FOLLOW_26_in_importDeclaration168); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, importDeclaration_StartIndex); }
        }
        dbg.location(191, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "importDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "importDeclaration"


    // $ANTLR start "typeDeclaration"
    // Java.g:193:1: typeDeclaration : ( classOrInterfaceDeclaration | ';' );
    public final void typeDeclaration() throws RecognitionException {
        int typeDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(193, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return ; }
            // Java.g:194:5: ( classOrInterfaceDeclaration | ';' )
            int alt11=2;
            try { dbg.enterDecision(11);

            int LA11_0 = input.LA(1);

            if ( (LA11_0==ENUM||LA11_0==28||(LA11_0>=31 && LA11_0<=37)||LA11_0==46||LA11_0==73) ) {
                alt11=1;
            }
            else if ( (LA11_0==26) ) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(11);}

            switch (alt11) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:194:9: classOrInterfaceDeclaration
                    {
                    dbg.location(194,9);
                    pushFollow(FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration191);
                    classOrInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:195:9: ';'
                    {
                    dbg.location(195,9);
                    match(input,26,FOLLOW_26_in_typeDeclaration201); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 4, typeDeclaration_StartIndex); }
        }
        dbg.location(196, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeDeclaration"


    // $ANTLR start "classOrInterfaceDeclaration"
    // Java.g:198:1: classOrInterfaceDeclaration : classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration ) ;
    public final void classOrInterfaceDeclaration() throws RecognitionException {
        int classOrInterfaceDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classOrInterfaceDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(198, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return ; }
            // Java.g:199:5: ( classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration ) )
            dbg.enterAlt(1);

            // Java.g:199:9: classOrInterfaceModifiers ( classDeclaration | interfaceDeclaration )
            {
            dbg.location(199,9);
            pushFollow(FOLLOW_classOrInterfaceModifiers_in_classOrInterfaceDeclaration224);
            classOrInterfaceModifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(199,35);
            // Java.g:199:35: ( classDeclaration | interfaceDeclaration )
            int alt12=2;
            try { dbg.enterSubRule(12);
            try { dbg.enterDecision(12);

            int LA12_0 = input.LA(1);

            if ( (LA12_0==ENUM||LA12_0==37) ) {
                alt12=1;
            }
            else if ( (LA12_0==46||LA12_0==73) ) {
                alt12=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(12);}

            switch (alt12) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:199:36: classDeclaration
                    {
                    dbg.location(199,36);
                    pushFollow(FOLLOW_classDeclaration_in_classOrInterfaceDeclaration227);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:199:55: interfaceDeclaration
                    {
                    dbg.location(199,55);
                    pushFollow(FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration231);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, classOrInterfaceDeclaration_StartIndex); }
        }
        dbg.location(200, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classOrInterfaceDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classOrInterfaceDeclaration"


    // $ANTLR start "classOrInterfaceModifiers"
    // Java.g:202:1: classOrInterfaceModifiers : ( classOrInterfaceModifier )* ;
    public final void classOrInterfaceModifiers() throws RecognitionException {
        int classOrInterfaceModifiers_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classOrInterfaceModifiers");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(202, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return ; }
            // Java.g:203:5: ( ( classOrInterfaceModifier )* )
            dbg.enterAlt(1);

            // Java.g:203:9: ( classOrInterfaceModifier )*
            {
            dbg.location(203,9);
            // Java.g:203:9: ( classOrInterfaceModifier )*
            try { dbg.enterSubRule(13);

            loop13:
            do {
                int alt13=2;
                try { dbg.enterDecision(13);

                int LA13_0 = input.LA(1);

                if ( (LA13_0==73) ) {
                    int LA13_2 = input.LA(2);

                    if ( (LA13_2==Identifier) ) {
                        alt13=1;
                    }


                }
                else if ( (LA13_0==28||(LA13_0>=31 && LA13_0<=36)) ) {
                    alt13=1;
                }


                } finally {dbg.exitDecision(13);}

                switch (alt13) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: classOrInterfaceModifier
            	    {
            	    dbg.location(203,9);
            	    pushFollow(FOLLOW_classOrInterfaceModifier_in_classOrInterfaceModifiers255);
            	    classOrInterfaceModifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 6, classOrInterfaceModifiers_StartIndex); }
        }
        dbg.location(204, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classOrInterfaceModifiers");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classOrInterfaceModifiers"


    // $ANTLR start "classOrInterfaceModifier"
    // Java.g:206:1: classOrInterfaceModifier : ( annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'final' | 'strictfp' );
    public final void classOrInterfaceModifier() throws RecognitionException {
        int classOrInterfaceModifier_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classOrInterfaceModifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(206, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return ; }
            // Java.g:207:5: ( annotation | 'public' | 'protected' | 'private' | 'abstract' | 'static' | 'final' | 'strictfp' )
            int alt14=8;
            try { dbg.enterDecision(14);

            switch ( input.LA(1) ) {
            case 73:
                {
                alt14=1;
                }
                break;
            case 31:
                {
                alt14=2;
                }
                break;
            case 32:
                {
                alt14=3;
                }
                break;
            case 33:
                {
                alt14=4;
                }
                break;
            case 34:
                {
                alt14=5;
                }
                break;
            case 28:
                {
                alt14=6;
                }
                break;
            case 35:
                {
                alt14=7;
                }
                break;
            case 36:
                {
                alt14=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(14);}

            switch (alt14) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:207:9: annotation
                    {
                    dbg.location(207,9);
                    pushFollow(FOLLOW_annotation_in_classOrInterfaceModifier275);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:208:9: 'public'
                    {
                    dbg.location(208,9);
                    match(input,31,FOLLOW_31_in_classOrInterfaceModifier288); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:209:9: 'protected'
                    {
                    dbg.location(209,9);
                    match(input,32,FOLLOW_32_in_classOrInterfaceModifier303); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:210:9: 'private'
                    {
                    dbg.location(210,9);
                    match(input,33,FOLLOW_33_in_classOrInterfaceModifier315); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:211:9: 'abstract'
                    {
                    dbg.location(211,9);
                    match(input,34,FOLLOW_34_in_classOrInterfaceModifier329); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:212:9: 'static'
                    {
                    dbg.location(212,9);
                    match(input,28,FOLLOW_28_in_classOrInterfaceModifier342); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:213:9: 'final'
                    {
                    dbg.location(213,9);
                    match(input,35,FOLLOW_35_in_classOrInterfaceModifier357); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:214:9: 'strictfp'
                    {
                    dbg.location(214,9);
                    match(input,36,FOLLOW_36_in_classOrInterfaceModifier373); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 7, classOrInterfaceModifier_StartIndex); }
        }
        dbg.location(215, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classOrInterfaceModifier");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classOrInterfaceModifier"


    // $ANTLR start "modifiers"
    // Java.g:217:1: modifiers : ( modifier )* ;
    public final void modifiers() throws RecognitionException {
        int modifiers_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "modifiers");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(217, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return ; }
            // Java.g:218:5: ( ( modifier )* )
            dbg.enterAlt(1);

            // Java.g:218:9: ( modifier )*
            {
            dbg.location(218,9);
            // Java.g:218:9: ( modifier )*
            try { dbg.enterSubRule(15);

            loop15:
            do {
                int alt15=2;
                try { dbg.enterDecision(15);

                int LA15_0 = input.LA(1);

                if ( (LA15_0==73) ) {
                    int LA15_2 = input.LA(2);

                    if ( (LA15_2==Identifier) ) {
                        alt15=1;
                    }


                }
                else if ( (LA15_0==28||(LA15_0>=31 && LA15_0<=36)||(LA15_0>=52 && LA15_0<=55)) ) {
                    alt15=1;
                }


                } finally {dbg.exitDecision(15);}

                switch (alt15) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: modifier
            	    {
            	    dbg.location(218,9);
            	    pushFollow(FOLLOW_modifier_in_modifiers395);
            	    modifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 8, modifiers_StartIndex); }
        }
        dbg.location(219, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "modifiers");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "modifiers"


    // $ANTLR start "classDeclaration"
    // Java.g:221:1: classDeclaration : ( normalClassDeclaration | enumDeclaration );
    public final void classDeclaration() throws RecognitionException {
        int classDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(221, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return ; }
            // Java.g:222:5: ( normalClassDeclaration | enumDeclaration )
            int alt16=2;
            try { dbg.enterDecision(16);

            int LA16_0 = input.LA(1);

            if ( (LA16_0==37) ) {
                alt16=1;
            }
            else if ( (LA16_0==ENUM) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(16);}

            switch (alt16) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:222:9: normalClassDeclaration
                    {
                    dbg.location(222,9);
                    pushFollow(FOLLOW_normalClassDeclaration_in_classDeclaration415);
                    normalClassDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:223:9: enumDeclaration
                    {
                    dbg.location(223,9);
                    pushFollow(FOLLOW_enumDeclaration_in_classDeclaration425);
                    enumDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 9, classDeclaration_StartIndex); }
        }
        dbg.location(224, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classDeclaration"


    // $ANTLR start "normalClassDeclaration"
    // Java.g:226:1: normalClassDeclaration : 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody ;
    public final void normalClassDeclaration() throws RecognitionException {
        int normalClassDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "normalClassDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(226, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return ; }
            // Java.g:227:5: ( 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody )
            dbg.enterAlt(1);

            // Java.g:227:9: 'class' Identifier ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody
            {
            dbg.location(227,9);
            match(input,37,FOLLOW_37_in_normalClassDeclaration448); if (state.failed) return ;
            dbg.location(227,17);
            match(input,Identifier,FOLLOW_Identifier_in_normalClassDeclaration450); if (state.failed) return ;
            dbg.location(227,28);
            // Java.g:227:28: ( typeParameters )?
            int alt17=2;
            try { dbg.enterSubRule(17);
            try { dbg.enterDecision(17);

            int LA17_0 = input.LA(1);

            if ( (LA17_0==40) ) {
                alt17=1;
            }
            } finally {dbg.exitDecision(17);}

            switch (alt17) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: typeParameters
                    {
                    dbg.location(227,28);
                    pushFollow(FOLLOW_typeParameters_in_normalClassDeclaration452);
                    typeParameters();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(228,9);
            // Java.g:228:9: ( 'extends' type )?
            int alt18=2;
            try { dbg.enterSubRule(18);
            try { dbg.enterDecision(18);

            int LA18_0 = input.LA(1);

            if ( (LA18_0==38) ) {
                alt18=1;
            }
            } finally {dbg.exitDecision(18);}

            switch (alt18) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:228:10: 'extends' type
                    {
                    dbg.location(228,10);
                    match(input,38,FOLLOW_38_in_normalClassDeclaration464); if (state.failed) return ;
                    dbg.location(228,20);
                    pushFollow(FOLLOW_type_in_normalClassDeclaration466);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(229,9);
            // Java.g:229:9: ( 'implements' typeList )?
            int alt19=2;
            try { dbg.enterSubRule(19);
            try { dbg.enterDecision(19);

            int LA19_0 = input.LA(1);

            if ( (LA19_0==39) ) {
                alt19=1;
            }
            } finally {dbg.exitDecision(19);}

            switch (alt19) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:229:10: 'implements' typeList
                    {
                    dbg.location(229,10);
                    match(input,39,FOLLOW_39_in_normalClassDeclaration479); if (state.failed) return ;
                    dbg.location(229,23);
                    pushFollow(FOLLOW_typeList_in_normalClassDeclaration481);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(230,9);
            pushFollow(FOLLOW_classBody_in_normalClassDeclaration493);
            classBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 10, normalClassDeclaration_StartIndex); }
        }
        dbg.location(231, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "normalClassDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "normalClassDeclaration"


    // $ANTLR start "typeParameters"
    // Java.g:233:1: typeParameters : '<' typeParameter ( ',' typeParameter )* '>' ;
    public final void typeParameters() throws RecognitionException {
        int typeParameters_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeParameters");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(233, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return ; }
            // Java.g:234:5: ( '<' typeParameter ( ',' typeParameter )* '>' )
            dbg.enterAlt(1);

            // Java.g:234:9: '<' typeParameter ( ',' typeParameter )* '>'
            {
            dbg.location(234,9);
            match(input,40,FOLLOW_40_in_typeParameters516); if (state.failed) return ;
            dbg.location(234,13);
            pushFollow(FOLLOW_typeParameter_in_typeParameters518);
            typeParameter();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(234,27);
            // Java.g:234:27: ( ',' typeParameter )*
            try { dbg.enterSubRule(20);

            loop20:
            do {
                int alt20=2;
                try { dbg.enterDecision(20);

                int LA20_0 = input.LA(1);

                if ( (LA20_0==41) ) {
                    alt20=1;
                }


                } finally {dbg.exitDecision(20);}

                switch (alt20) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:234:28: ',' typeParameter
            	    {
            	    dbg.location(234,28);
            	    match(input,41,FOLLOW_41_in_typeParameters521); if (state.failed) return ;
            	    dbg.location(234,32);
            	    pushFollow(FOLLOW_typeParameter_in_typeParameters523);
            	    typeParameter();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);
            } finally {dbg.exitSubRule(20);}

            dbg.location(234,48);
            match(input,42,FOLLOW_42_in_typeParameters527); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 11, typeParameters_StartIndex); }
        }
        dbg.location(235, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeParameters");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeParameters"


    // $ANTLR start "typeParameter"
    // Java.g:237:1: typeParameter : Identifier ( 'extends' typeBound )? ;
    public final void typeParameter() throws RecognitionException {
        int typeParameter_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeParameter");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(237, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return ; }
            // Java.g:238:5: ( Identifier ( 'extends' typeBound )? )
            dbg.enterAlt(1);

            // Java.g:238:9: Identifier ( 'extends' typeBound )?
            {
            dbg.location(238,9);
            match(input,Identifier,FOLLOW_Identifier_in_typeParameter546); if (state.failed) return ;
            dbg.location(238,20);
            // Java.g:238:20: ( 'extends' typeBound )?
            int alt21=2;
            try { dbg.enterSubRule(21);
            try { dbg.enterDecision(21);

            int LA21_0 = input.LA(1);

            if ( (LA21_0==38) ) {
                alt21=1;
            }
            } finally {dbg.exitDecision(21);}

            switch (alt21) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:238:21: 'extends' typeBound
                    {
                    dbg.location(238,21);
                    match(input,38,FOLLOW_38_in_typeParameter549); if (state.failed) return ;
                    dbg.location(238,31);
                    pushFollow(FOLLOW_typeBound_in_typeParameter551);
                    typeBound();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(21);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 12, typeParameter_StartIndex); }
        }
        dbg.location(239, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeParameter");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeParameter"


    // $ANTLR start "typeBound"
    // Java.g:241:1: typeBound : type ( '&' type )* ;
    public final void typeBound() throws RecognitionException {
        int typeBound_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeBound");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(241, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return ; }
            // Java.g:242:5: ( type ( '&' type )* )
            dbg.enterAlt(1);

            // Java.g:242:9: type ( '&' type )*
            {
            dbg.location(242,9);
            pushFollow(FOLLOW_type_in_typeBound580);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(242,14);
            // Java.g:242:14: ( '&' type )*
            try { dbg.enterSubRule(22);

            loop22:
            do {
                int alt22=2;
                try { dbg.enterDecision(22);

                int LA22_0 = input.LA(1);

                if ( (LA22_0==43) ) {
                    alt22=1;
                }


                } finally {dbg.exitDecision(22);}

                switch (alt22) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:242:15: '&' type
            	    {
            	    dbg.location(242,15);
            	    match(input,43,FOLLOW_43_in_typeBound583); if (state.failed) return ;
            	    dbg.location(242,19);
            	    pushFollow(FOLLOW_type_in_typeBound585);
            	    type();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);
            } finally {dbg.exitSubRule(22);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 13, typeBound_StartIndex); }
        }
        dbg.location(243, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeBound");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeBound"


    // $ANTLR start "enumDeclaration"
    // Java.g:245:1: enumDeclaration : ENUM Identifier ( 'implements' typeList )? enumBody ;
    public final void enumDeclaration() throws RecognitionException {
        int enumDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(245, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return ; }
            // Java.g:246:5: ( ENUM Identifier ( 'implements' typeList )? enumBody )
            dbg.enterAlt(1);

            // Java.g:246:9: ENUM Identifier ( 'implements' typeList )? enumBody
            {
            dbg.location(246,9);
            match(input,ENUM,FOLLOW_ENUM_in_enumDeclaration606); if (state.failed) return ;
            dbg.location(246,14);
            match(input,Identifier,FOLLOW_Identifier_in_enumDeclaration608); if (state.failed) return ;
            dbg.location(246,25);
            // Java.g:246:25: ( 'implements' typeList )?
            int alt23=2;
            try { dbg.enterSubRule(23);
            try { dbg.enterDecision(23);

            int LA23_0 = input.LA(1);

            if ( (LA23_0==39) ) {
                alt23=1;
            }
            } finally {dbg.exitDecision(23);}

            switch (alt23) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:246:26: 'implements' typeList
                    {
                    dbg.location(246,26);
                    match(input,39,FOLLOW_39_in_enumDeclaration611); if (state.failed) return ;
                    dbg.location(246,39);
                    pushFollow(FOLLOW_typeList_in_enumDeclaration613);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(23);}

            dbg.location(246,50);
            pushFollow(FOLLOW_enumBody_in_enumDeclaration617);
            enumBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 14, enumDeclaration_StartIndex); }
        }
        dbg.location(247, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumDeclaration"


    // $ANTLR start "enumBody"
    // Java.g:249:1: enumBody : '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' ;
    public final void enumBody() throws RecognitionException {
        int enumBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(249, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return ; }
            // Java.g:250:5: ( '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' )
            dbg.enterAlt(1);

            // Java.g:250:9: '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}'
            {
            dbg.location(250,9);
            match(input,44,FOLLOW_44_in_enumBody636); if (state.failed) return ;
            dbg.location(250,13);
            // Java.g:250:13: ( enumConstants )?
            int alt24=2;
            try { dbg.enterSubRule(24);
            try { dbg.enterDecision(24);

            int LA24_0 = input.LA(1);

            if ( (LA24_0==Identifier||LA24_0==73) ) {
                alt24=1;
            }
            } finally {dbg.exitDecision(24);}

            switch (alt24) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: enumConstants
                    {
                    dbg.location(250,13);
                    pushFollow(FOLLOW_enumConstants_in_enumBody638);
                    enumConstants();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(24);}

            dbg.location(250,28);
            // Java.g:250:28: ( ',' )?
            int alt25=2;
            try { dbg.enterSubRule(25);
            try { dbg.enterDecision(25);

            int LA25_0 = input.LA(1);

            if ( (LA25_0==41) ) {
                alt25=1;
            }
            } finally {dbg.exitDecision(25);}

            switch (alt25) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: ','
                    {
                    dbg.location(250,28);
                    match(input,41,FOLLOW_41_in_enumBody641); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(25);}

            dbg.location(250,33);
            // Java.g:250:33: ( enumBodyDeclarations )?
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26);

            int LA26_0 = input.LA(1);

            if ( (LA26_0==26) ) {
                alt26=1;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: enumBodyDeclarations
                    {
                    dbg.location(250,33);
                    pushFollow(FOLLOW_enumBodyDeclarations_in_enumBody644);
                    enumBodyDeclarations();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(26);}

            dbg.location(250,55);
            match(input,45,FOLLOW_45_in_enumBody647); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 15, enumBody_StartIndex); }
        }
        dbg.location(251, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumBody"


    // $ANTLR start "enumConstants"
    // Java.g:253:1: enumConstants : enumConstant ( ',' enumConstant )* ;
    public final void enumConstants() throws RecognitionException {
        int enumConstants_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumConstants");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(253, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return ; }
            // Java.g:254:5: ( enumConstant ( ',' enumConstant )* )
            dbg.enterAlt(1);

            // Java.g:254:9: enumConstant ( ',' enumConstant )*
            {
            dbg.location(254,9);
            pushFollow(FOLLOW_enumConstant_in_enumConstants666);
            enumConstant();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(254,22);
            // Java.g:254:22: ( ',' enumConstant )*
            try { dbg.enterSubRule(27);

            loop27:
            do {
                int alt27=2;
                try { dbg.enterDecision(27);

                int LA27_0 = input.LA(1);

                if ( (LA27_0==41) ) {
                    int LA27_1 = input.LA(2);

                    if ( (LA27_1==Identifier||LA27_1==73) ) {
                        alt27=1;
                    }


                }


                } finally {dbg.exitDecision(27);}

                switch (alt27) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:254:23: ',' enumConstant
            	    {
            	    dbg.location(254,23);
            	    match(input,41,FOLLOW_41_in_enumConstants669); if (state.failed) return ;
            	    dbg.location(254,27);
            	    pushFollow(FOLLOW_enumConstant_in_enumConstants671);
            	    enumConstant();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);
            } finally {dbg.exitSubRule(27);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 16, enumConstants_StartIndex); }
        }
        dbg.location(255, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumConstants");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumConstants"


    // $ANTLR start "enumConstant"
    // Java.g:257:1: enumConstant : ( annotations )? Identifier ( arguments )? ( classBody )? ;
    public final void enumConstant() throws RecognitionException {
        int enumConstant_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumConstant");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(257, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return ; }
            // Java.g:258:5: ( ( annotations )? Identifier ( arguments )? ( classBody )? )
            dbg.enterAlt(1);

            // Java.g:258:9: ( annotations )? Identifier ( arguments )? ( classBody )?
            {
            dbg.location(258,9);
            // Java.g:258:9: ( annotations )?
            int alt28=2;
            try { dbg.enterSubRule(28);
            try { dbg.enterDecision(28);

            int LA28_0 = input.LA(1);

            if ( (LA28_0==73) ) {
                alt28=1;
            }
            } finally {dbg.exitDecision(28);}

            switch (alt28) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: annotations
                    {
                    dbg.location(258,9);
                    pushFollow(FOLLOW_annotations_in_enumConstant696);
                    annotations();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(28);}

            dbg.location(258,22);
            match(input,Identifier,FOLLOW_Identifier_in_enumConstant699); if (state.failed) return ;
            dbg.location(258,33);
            // Java.g:258:33: ( arguments )?
            int alt29=2;
            try { dbg.enterSubRule(29);
            try { dbg.enterDecision(29);

            int LA29_0 = input.LA(1);

            if ( (LA29_0==66) ) {
                alt29=1;
            }
            } finally {dbg.exitDecision(29);}

            switch (alt29) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: arguments
                    {
                    dbg.location(258,33);
                    pushFollow(FOLLOW_arguments_in_enumConstant701);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(29);}

            dbg.location(258,44);
            // Java.g:258:44: ( classBody )?
            int alt30=2;
            try { dbg.enterSubRule(30);
            try { dbg.enterDecision(30);

            int LA30_0 = input.LA(1);

            if ( (LA30_0==44) ) {
                alt30=1;
            }
            } finally {dbg.exitDecision(30);}

            switch (alt30) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: classBody
                    {
                    dbg.location(258,44);
                    pushFollow(FOLLOW_classBody_in_enumConstant704);
                    classBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(30);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 17, enumConstant_StartIndex); }
        }
        dbg.location(259, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumConstant");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumConstant"


    // $ANTLR start "enumBodyDeclarations"
    // Java.g:261:1: enumBodyDeclarations : ';' ( classBodyDeclaration )* ;
    public final void enumBodyDeclarations() throws RecognitionException {
        int enumBodyDeclarations_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumBodyDeclarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(261, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return ; }
            // Java.g:262:5: ( ';' ( classBodyDeclaration )* )
            dbg.enterAlt(1);

            // Java.g:262:9: ';' ( classBodyDeclaration )*
            {
            dbg.location(262,9);
            match(input,26,FOLLOW_26_in_enumBodyDeclarations728); if (state.failed) return ;
            dbg.location(262,13);
            // Java.g:262:13: ( classBodyDeclaration )*
            try { dbg.enterSubRule(31);

            loop31:
            do {
                int alt31=2;
                try { dbg.enterDecision(31);

                int LA31_0 = input.LA(1);

                if ( ((LA31_0>=Identifier && LA31_0<=ENUM)||LA31_0==26||LA31_0==28||(LA31_0>=31 && LA31_0<=37)||LA31_0==40||LA31_0==44||(LA31_0>=46 && LA31_0<=47)||(LA31_0>=52 && LA31_0<=63)||LA31_0==73) ) {
                    alt31=1;
                }


                } finally {dbg.exitDecision(31);}

                switch (alt31) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:262:14: classBodyDeclaration
            	    {
            	    dbg.location(262,14);
            	    pushFollow(FOLLOW_classBodyDeclaration_in_enumBodyDeclarations731);
            	    classBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);
            } finally {dbg.exitSubRule(31);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 18, enumBodyDeclarations_StartIndex); }
        }
        dbg.location(263, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumBodyDeclarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumBodyDeclarations"


    // $ANTLR start "interfaceDeclaration"
    // Java.g:265:1: interfaceDeclaration : ( normalInterfaceDeclaration | annotationTypeDeclaration );
    public final void interfaceDeclaration() throws RecognitionException {
        int interfaceDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(265, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return ; }
            // Java.g:266:5: ( normalInterfaceDeclaration | annotationTypeDeclaration )
            int alt32=2;
            try { dbg.enterDecision(32);

            int LA32_0 = input.LA(1);

            if ( (LA32_0==46) ) {
                alt32=1;
            }
            else if ( (LA32_0==73) ) {
                alt32=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(32);}

            switch (alt32) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:266:9: normalInterfaceDeclaration
                    {
                    dbg.location(266,9);
                    pushFollow(FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration756);
                    normalInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:267:9: annotationTypeDeclaration
                    {
                    dbg.location(267,9);
                    pushFollow(FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration766);
                    annotationTypeDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 19, interfaceDeclaration_StartIndex); }
        }
        dbg.location(268, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceDeclaration"


    // $ANTLR start "normalInterfaceDeclaration"
    // Java.g:270:1: normalInterfaceDeclaration : 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody ;
    public final void normalInterfaceDeclaration() throws RecognitionException {
        int normalInterfaceDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "normalInterfaceDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(270, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return ; }
            // Java.g:271:5: ( 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody )
            dbg.enterAlt(1);

            // Java.g:271:9: 'interface' Identifier ( typeParameters )? ( 'extends' typeList )? interfaceBody
            {
            dbg.location(271,9);
            match(input,46,FOLLOW_46_in_normalInterfaceDeclaration789); if (state.failed) return ;
            dbg.location(271,21);
            match(input,Identifier,FOLLOW_Identifier_in_normalInterfaceDeclaration791); if (state.failed) return ;
            dbg.location(271,32);
            // Java.g:271:32: ( typeParameters )?
            int alt33=2;
            try { dbg.enterSubRule(33);
            try { dbg.enterDecision(33);

            int LA33_0 = input.LA(1);

            if ( (LA33_0==40) ) {
                alt33=1;
            }
            } finally {dbg.exitDecision(33);}

            switch (alt33) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: typeParameters
                    {
                    dbg.location(271,32);
                    pushFollow(FOLLOW_typeParameters_in_normalInterfaceDeclaration793);
                    typeParameters();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(33);}

            dbg.location(271,48);
            // Java.g:271:48: ( 'extends' typeList )?
            int alt34=2;
            try { dbg.enterSubRule(34);
            try { dbg.enterDecision(34);

            int LA34_0 = input.LA(1);

            if ( (LA34_0==38) ) {
                alt34=1;
            }
            } finally {dbg.exitDecision(34);}

            switch (alt34) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:271:49: 'extends' typeList
                    {
                    dbg.location(271,49);
                    match(input,38,FOLLOW_38_in_normalInterfaceDeclaration797); if (state.failed) return ;
                    dbg.location(271,59);
                    pushFollow(FOLLOW_typeList_in_normalInterfaceDeclaration799);
                    typeList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(34);}

            dbg.location(271,70);
            pushFollow(FOLLOW_interfaceBody_in_normalInterfaceDeclaration803);
            interfaceBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 20, normalInterfaceDeclaration_StartIndex); }
        }
        dbg.location(272, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "normalInterfaceDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "normalInterfaceDeclaration"


    // $ANTLR start "typeList"
    // Java.g:274:1: typeList : type ( ',' type )* ;
    public final void typeList() throws RecognitionException {
        int typeList_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(274, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return ; }
            // Java.g:275:5: ( type ( ',' type )* )
            dbg.enterAlt(1);

            // Java.g:275:9: type ( ',' type )*
            {
            dbg.location(275,9);
            pushFollow(FOLLOW_type_in_typeList826);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(275,14);
            // Java.g:275:14: ( ',' type )*
            try { dbg.enterSubRule(35);

            loop35:
            do {
                int alt35=2;
                try { dbg.enterDecision(35);

                int LA35_0 = input.LA(1);

                if ( (LA35_0==41) ) {
                    alt35=1;
                }


                } finally {dbg.exitDecision(35);}

                switch (alt35) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:275:15: ',' type
            	    {
            	    dbg.location(275,15);
            	    match(input,41,FOLLOW_41_in_typeList829); if (state.failed) return ;
            	    dbg.location(275,19);
            	    pushFollow(FOLLOW_type_in_typeList831);
            	    type();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);
            } finally {dbg.exitSubRule(35);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 21, typeList_StartIndex); }
        }
        dbg.location(276, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeList"


    // $ANTLR start "classBody"
    // Java.g:278:1: classBody : '{' ( classBodyDeclaration )* '}' ;
    public final void classBody() throws RecognitionException {
        int classBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(278, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return ; }
            // Java.g:279:5: ( '{' ( classBodyDeclaration )* '}' )
            dbg.enterAlt(1);

            // Java.g:279:9: '{' ( classBodyDeclaration )* '}'
            {
            dbg.location(279,9);
            match(input,44,FOLLOW_44_in_classBody856); if (state.failed) return ;
            dbg.location(279,13);
            // Java.g:279:13: ( classBodyDeclaration )*
            try { dbg.enterSubRule(36);

            loop36:
            do {
                int alt36=2;
                try { dbg.enterDecision(36);

                int LA36_0 = input.LA(1);

                if ( ((LA36_0>=Identifier && LA36_0<=ENUM)||LA36_0==26||LA36_0==28||(LA36_0>=31 && LA36_0<=37)||LA36_0==40||LA36_0==44||(LA36_0>=46 && LA36_0<=47)||(LA36_0>=52 && LA36_0<=63)||LA36_0==73) ) {
                    alt36=1;
                }


                } finally {dbg.exitDecision(36);}

                switch (alt36) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: classBodyDeclaration
            	    {
            	    dbg.location(279,13);
            	    pushFollow(FOLLOW_classBodyDeclaration_in_classBody858);
            	    classBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);
            } finally {dbg.exitSubRule(36);}

            dbg.location(279,35);
            match(input,45,FOLLOW_45_in_classBody861); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 22, classBody_StartIndex); }
        }
        dbg.location(280, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classBody"


    // $ANTLR start "interfaceBody"
    // Java.g:282:1: interfaceBody : '{' ( interfaceBodyDeclaration )* '}' ;
    public final void interfaceBody() throws RecognitionException {
        int interfaceBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(282, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return ; }
            // Java.g:283:5: ( '{' ( interfaceBodyDeclaration )* '}' )
            dbg.enterAlt(1);

            // Java.g:283:9: '{' ( interfaceBodyDeclaration )* '}'
            {
            dbg.location(283,9);
            match(input,44,FOLLOW_44_in_interfaceBody884); if (state.failed) return ;
            dbg.location(283,13);
            // Java.g:283:13: ( interfaceBodyDeclaration )*
            try { dbg.enterSubRule(37);

            loop37:
            do {
                int alt37=2;
                try { dbg.enterDecision(37);

                int LA37_0 = input.LA(1);

                if ( ((LA37_0>=Identifier && LA37_0<=ENUM)||LA37_0==26||LA37_0==28||(LA37_0>=31 && LA37_0<=37)||LA37_0==40||(LA37_0>=46 && LA37_0<=47)||(LA37_0>=52 && LA37_0<=63)||LA37_0==73) ) {
                    alt37=1;
                }


                } finally {dbg.exitDecision(37);}

                switch (alt37) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: interfaceBodyDeclaration
            	    {
            	    dbg.location(283,13);
            	    pushFollow(FOLLOW_interfaceBodyDeclaration_in_interfaceBody886);
            	    interfaceBodyDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);
            } finally {dbg.exitSubRule(37);}

            dbg.location(283,39);
            match(input,45,FOLLOW_45_in_interfaceBody889); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 23, interfaceBody_StartIndex); }
        }
        dbg.location(284, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceBody"


    // $ANTLR start "classBodyDeclaration"
    // Java.g:286:1: classBodyDeclaration : ( ';' | ( 'static' )? block | modifiers memberDecl );
    public final void classBodyDeclaration() throws RecognitionException {
        int classBodyDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classBodyDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(286, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return ; }
            // Java.g:287:5: ( ';' | ( 'static' )? block | modifiers memberDecl )
            int alt39=3;
            try { dbg.enterDecision(39);

            switch ( input.LA(1) ) {
            case 26:
                {
                alt39=1;
                }
                break;
            case 28:
                {
                int LA39_2 = input.LA(2);

                if ( ((LA39_2>=Identifier && LA39_2<=ENUM)||LA39_2==28||(LA39_2>=31 && LA39_2<=37)||LA39_2==40||(LA39_2>=46 && LA39_2<=47)||(LA39_2>=52 && LA39_2<=63)||LA39_2==73) ) {
                    alt39=3;
                }
                else if ( (LA39_2==44) ) {
                    alt39=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                }
                break;
            case 44:
                {
                alt39=2;
                }
                break;
            case Identifier:
            case ENUM:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 40:
            case 46:
            case 47:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 73:
                {
                alt39=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(39);}

            switch (alt39) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:287:9: ';'
                    {
                    dbg.location(287,9);
                    match(input,26,FOLLOW_26_in_classBodyDeclaration908); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:288:9: ( 'static' )? block
                    {
                    dbg.location(288,9);
                    // Java.g:288:9: ( 'static' )?
                    int alt38=2;
                    try { dbg.enterSubRule(38);
                    try { dbg.enterDecision(38);

                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==28) ) {
                        alt38=1;
                    }
                    } finally {dbg.exitDecision(38);}

                    switch (alt38) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: 'static'
                            {
                            dbg.location(288,9);
                            match(input,28,FOLLOW_28_in_classBodyDeclaration918); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(38);}

                    dbg.location(288,19);
                    pushFollow(FOLLOW_block_in_classBodyDeclaration921);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:289:9: modifiers memberDecl
                    {
                    dbg.location(289,9);
                    pushFollow(FOLLOW_modifiers_in_classBodyDeclaration931);
                    modifiers();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(289,19);
                    pushFollow(FOLLOW_memberDecl_in_classBodyDeclaration933);
                    memberDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 24, classBodyDeclaration_StartIndex); }
        }
        dbg.location(290, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classBodyDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classBodyDeclaration"


    // $ANTLR start "memberDecl"
    // Java.g:292:1: memberDecl : ( genericMethodOrConstructorDecl | memberDeclaration | 'void' Identifier voidMethodDeclaratorRest | Identifier constructorDeclaratorRest | interfaceDeclaration | classDeclaration );
    public final void memberDecl() throws RecognitionException {
        int memberDecl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "memberDecl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(292, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return ; }
            // Java.g:293:5: ( genericMethodOrConstructorDecl | memberDeclaration | 'void' Identifier voidMethodDeclaratorRest | Identifier constructorDeclaratorRest | interfaceDeclaration | classDeclaration )
            int alt40=6;
            try { dbg.enterDecision(40);

            switch ( input.LA(1) ) {
            case 40:
                {
                alt40=1;
                }
                break;
            case Identifier:
                {
                int LA40_2 = input.LA(2);

                if ( (LA40_2==66) ) {
                    alt40=4;
                }
                else if ( (LA40_2==Identifier||LA40_2==29||LA40_2==40||LA40_2==48) ) {
                    alt40=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                }
                break;
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt40=2;
                }
                break;
            case 47:
                {
                alt40=3;
                }
                break;
            case 46:
            case 73:
                {
                alt40=5;
                }
                break;
            case ENUM:
            case 37:
                {
                alt40=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(40);}

            switch (alt40) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:293:9: genericMethodOrConstructorDecl
                    {
                    dbg.location(293,9);
                    pushFollow(FOLLOW_genericMethodOrConstructorDecl_in_memberDecl956);
                    genericMethodOrConstructorDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:294:9: memberDeclaration
                    {
                    dbg.location(294,9);
                    pushFollow(FOLLOW_memberDeclaration_in_memberDecl966);
                    memberDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:295:9: 'void' Identifier voidMethodDeclaratorRest
                    {
                    dbg.location(295,9);
                    match(input,47,FOLLOW_47_in_memberDecl976); if (state.failed) return ;
                    dbg.location(295,16);
                    match(input,Identifier,FOLLOW_Identifier_in_memberDecl978); if (state.failed) return ;
                    dbg.location(295,27);
                    pushFollow(FOLLOW_voidMethodDeclaratorRest_in_memberDecl980);
                    voidMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:296:9: Identifier constructorDeclaratorRest
                    {
                    dbg.location(296,9);
                    match(input,Identifier,FOLLOW_Identifier_in_memberDecl990); if (state.failed) return ;
                    dbg.location(296,20);
                    pushFollow(FOLLOW_constructorDeclaratorRest_in_memberDecl992);
                    constructorDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:297:9: interfaceDeclaration
                    {
                    dbg.location(297,9);
                    pushFollow(FOLLOW_interfaceDeclaration_in_memberDecl1002);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:298:9: classDeclaration
                    {
                    dbg.location(298,9);
                    pushFollow(FOLLOW_classDeclaration_in_memberDecl1012);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 25, memberDecl_StartIndex); }
        }
        dbg.location(299, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "memberDecl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "memberDecl"


    // $ANTLR start "memberDeclaration"
    // Java.g:301:1: memberDeclaration : type ( methodDeclaration | fieldDeclaration ) ;
    public final void memberDeclaration() throws RecognitionException {
        int memberDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "memberDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(301, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return ; }
            // Java.g:302:5: ( type ( methodDeclaration | fieldDeclaration ) )
            dbg.enterAlt(1);

            // Java.g:302:9: type ( methodDeclaration | fieldDeclaration )
            {
            dbg.location(302,9);
            pushFollow(FOLLOW_type_in_memberDeclaration1035);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(302,14);
            // Java.g:302:14: ( methodDeclaration | fieldDeclaration )
            int alt41=2;
            try { dbg.enterSubRule(41);
            try { dbg.enterDecision(41);

            int LA41_0 = input.LA(1);

            if ( (LA41_0==Identifier) ) {
                int LA41_1 = input.LA(2);

                if ( (LA41_1==66) ) {
                    alt41=1;
                }
                else if ( (LA41_1==26||LA41_1==41||LA41_1==48||LA41_1==51) ) {
                    alt41=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 41, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(41);}

            switch (alt41) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:302:15: methodDeclaration
                    {
                    dbg.location(302,15);
                    pushFollow(FOLLOW_methodDeclaration_in_memberDeclaration1038);
                    methodDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:302:35: fieldDeclaration
                    {
                    dbg.location(302,35);
                    pushFollow(FOLLOW_fieldDeclaration_in_memberDeclaration1042);
                    fieldDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(41);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 26, memberDeclaration_StartIndex); }
        }
        dbg.location(303, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "memberDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "memberDeclaration"


    // $ANTLR start "genericMethodOrConstructorDecl"
    // Java.g:305:1: genericMethodOrConstructorDecl : typeParameters genericMethodOrConstructorRest ;
    public final void genericMethodOrConstructorDecl() throws RecognitionException {
        int genericMethodOrConstructorDecl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "genericMethodOrConstructorDecl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(305, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return ; }
            // Java.g:306:5: ( typeParameters genericMethodOrConstructorRest )
            dbg.enterAlt(1);

            // Java.g:306:9: typeParameters genericMethodOrConstructorRest
            {
            dbg.location(306,9);
            pushFollow(FOLLOW_typeParameters_in_genericMethodOrConstructorDecl1062);
            typeParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(306,24);
            pushFollow(FOLLOW_genericMethodOrConstructorRest_in_genericMethodOrConstructorDecl1064);
            genericMethodOrConstructorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 27, genericMethodOrConstructorDecl_StartIndex); }
        }
        dbg.location(307, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "genericMethodOrConstructorDecl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "genericMethodOrConstructorDecl"


    // $ANTLR start "genericMethodOrConstructorRest"
    // Java.g:309:1: genericMethodOrConstructorRest : ( ( type | 'void' ) Identifier methodDeclaratorRest | Identifier constructorDeclaratorRest );
    public final void genericMethodOrConstructorRest() throws RecognitionException {
        int genericMethodOrConstructorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "genericMethodOrConstructorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(309, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return ; }
            // Java.g:310:5: ( ( type | 'void' ) Identifier methodDeclaratorRest | Identifier constructorDeclaratorRest )
            int alt43=2;
            try { dbg.enterDecision(43);

            int LA43_0 = input.LA(1);

            if ( (LA43_0==Identifier) ) {
                int LA43_1 = input.LA(2);

                if ( (LA43_1==Identifier||LA43_1==29||LA43_1==40||LA43_1==48) ) {
                    alt43=1;
                }
                else if ( (LA43_1==66) ) {
                    alt43=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 43, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA43_0==47||(LA43_0>=56 && LA43_0<=63)) ) {
                alt43=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:310:9: ( type | 'void' ) Identifier methodDeclaratorRest
                    {
                    dbg.location(310,9);
                    // Java.g:310:9: ( type | 'void' )
                    int alt42=2;
                    try { dbg.enterSubRule(42);
                    try { dbg.enterDecision(42);

                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==Identifier||(LA42_0>=56 && LA42_0<=63)) ) {
                        alt42=1;
                    }
                    else if ( (LA42_0==47) ) {
                        alt42=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 42, 0, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(42);}

                    switch (alt42) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:310:10: type
                            {
                            dbg.location(310,10);
                            pushFollow(FOLLOW_type_in_genericMethodOrConstructorRest1088);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:310:17: 'void'
                            {
                            dbg.location(310,17);
                            match(input,47,FOLLOW_47_in_genericMethodOrConstructorRest1092); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(42);}

                    dbg.location(310,25);
                    match(input,Identifier,FOLLOW_Identifier_in_genericMethodOrConstructorRest1095); if (state.failed) return ;
                    dbg.location(310,36);
                    pushFollow(FOLLOW_methodDeclaratorRest_in_genericMethodOrConstructorRest1097);
                    methodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:311:9: Identifier constructorDeclaratorRest
                    {
                    dbg.location(311,9);
                    match(input,Identifier,FOLLOW_Identifier_in_genericMethodOrConstructorRest1107); if (state.failed) return ;
                    dbg.location(311,20);
                    pushFollow(FOLLOW_constructorDeclaratorRest_in_genericMethodOrConstructorRest1109);
                    constructorDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 28, genericMethodOrConstructorRest_StartIndex); }
        }
        dbg.location(312, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "genericMethodOrConstructorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "genericMethodOrConstructorRest"


    // $ANTLR start "methodDeclaration"
    // Java.g:314:1: methodDeclaration : Identifier methodDeclaratorRest ;
    public final void methodDeclaration() throws RecognitionException {
        int methodDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "methodDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(314, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return ; }
            // Java.g:315:5: ( Identifier methodDeclaratorRest )
            dbg.enterAlt(1);

            // Java.g:315:9: Identifier methodDeclaratorRest
            {
            dbg.location(315,9);
            match(input,Identifier,FOLLOW_Identifier_in_methodDeclaration1128); if (state.failed) return ;
            dbg.location(315,20);
            pushFollow(FOLLOW_methodDeclaratorRest_in_methodDeclaration1130);
            methodDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 29, methodDeclaration_StartIndex); }
        }
        dbg.location(316, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "methodDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "methodDeclaration"


    // $ANTLR start "fieldDeclaration"
    // Java.g:318:1: fieldDeclaration : variableDeclarators ';' ;
    public final void fieldDeclaration() throws RecognitionException {
        int fieldDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "fieldDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(318, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return ; }
            // Java.g:319:5: ( variableDeclarators ';' )
            dbg.enterAlt(1);

            // Java.g:319:9: variableDeclarators ';'
            {
            dbg.location(319,9);
            pushFollow(FOLLOW_variableDeclarators_in_fieldDeclaration1149);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(319,29);
            match(input,26,FOLLOW_26_in_fieldDeclaration1151); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 30, fieldDeclaration_StartIndex); }
        }
        dbg.location(320, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "fieldDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fieldDeclaration"


    // $ANTLR start "interfaceBodyDeclaration"
    // Java.g:322:1: interfaceBodyDeclaration : ( modifiers interfaceMemberDecl | ';' );
    public final void interfaceBodyDeclaration() throws RecognitionException {
        int interfaceBodyDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceBodyDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(322, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return ; }
            // Java.g:323:5: ( modifiers interfaceMemberDecl | ';' )
            int alt44=2;
            try { dbg.enterDecision(44);

            int LA44_0 = input.LA(1);

            if ( ((LA44_0>=Identifier && LA44_0<=ENUM)||LA44_0==28||(LA44_0>=31 && LA44_0<=37)||LA44_0==40||(LA44_0>=46 && LA44_0<=47)||(LA44_0>=52 && LA44_0<=63)||LA44_0==73) ) {
                alt44=1;
            }
            else if ( (LA44_0==26) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(44);}

            switch (alt44) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:323:9: modifiers interfaceMemberDecl
                    {
                    dbg.location(323,9);
                    pushFollow(FOLLOW_modifiers_in_interfaceBodyDeclaration1178);
                    modifiers();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(323,19);
                    pushFollow(FOLLOW_interfaceMemberDecl_in_interfaceBodyDeclaration1180);
                    interfaceMemberDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:324:9: ';'
                    {
                    dbg.location(324,9);
                    match(input,26,FOLLOW_26_in_interfaceBodyDeclaration1190); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 31, interfaceBodyDeclaration_StartIndex); }
        }
        dbg.location(325, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceBodyDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceBodyDeclaration"


    // $ANTLR start "interfaceMemberDecl"
    // Java.g:327:1: interfaceMemberDecl : ( interfaceMethodOrFieldDecl | interfaceGenericMethodDecl | 'void' Identifier voidInterfaceMethodDeclaratorRest | interfaceDeclaration | classDeclaration );
    public final void interfaceMemberDecl() throws RecognitionException {
        int interfaceMemberDecl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceMemberDecl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(327, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return ; }
            // Java.g:328:5: ( interfaceMethodOrFieldDecl | interfaceGenericMethodDecl | 'void' Identifier voidInterfaceMethodDeclaratorRest | interfaceDeclaration | classDeclaration )
            int alt45=5;
            try { dbg.enterDecision(45);

            switch ( input.LA(1) ) {
            case Identifier:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt45=1;
                }
                break;
            case 40:
                {
                alt45=2;
                }
                break;
            case 47:
                {
                alt45=3;
                }
                break;
            case 46:
            case 73:
                {
                alt45=4;
                }
                break;
            case ENUM:
            case 37:
                {
                alt45=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(45);}

            switch (alt45) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:328:9: interfaceMethodOrFieldDecl
                    {
                    dbg.location(328,9);
                    pushFollow(FOLLOW_interfaceMethodOrFieldDecl_in_interfaceMemberDecl1209);
                    interfaceMethodOrFieldDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:329:9: interfaceGenericMethodDecl
                    {
                    dbg.location(329,9);
                    pushFollow(FOLLOW_interfaceGenericMethodDecl_in_interfaceMemberDecl1219);
                    interfaceGenericMethodDecl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:330:9: 'void' Identifier voidInterfaceMethodDeclaratorRest
                    {
                    dbg.location(330,9);
                    match(input,47,FOLLOW_47_in_interfaceMemberDecl1229); if (state.failed) return ;
                    dbg.location(330,16);
                    match(input,Identifier,FOLLOW_Identifier_in_interfaceMemberDecl1231); if (state.failed) return ;
                    dbg.location(330,27);
                    pushFollow(FOLLOW_voidInterfaceMethodDeclaratorRest_in_interfaceMemberDecl1233);
                    voidInterfaceMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:331:9: interfaceDeclaration
                    {
                    dbg.location(331,9);
                    pushFollow(FOLLOW_interfaceDeclaration_in_interfaceMemberDecl1243);
                    interfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:332:9: classDeclaration
                    {
                    dbg.location(332,9);
                    pushFollow(FOLLOW_classDeclaration_in_interfaceMemberDecl1253);
                    classDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 32, interfaceMemberDecl_StartIndex); }
        }
        dbg.location(333, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceMemberDecl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceMemberDecl"


    // $ANTLR start "interfaceMethodOrFieldDecl"
    // Java.g:335:1: interfaceMethodOrFieldDecl : type Identifier interfaceMethodOrFieldRest ;
    public final void interfaceMethodOrFieldDecl() throws RecognitionException {
        int interfaceMethodOrFieldDecl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceMethodOrFieldDecl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(335, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return ; }
            // Java.g:336:5: ( type Identifier interfaceMethodOrFieldRest )
            dbg.enterAlt(1);

            // Java.g:336:9: type Identifier interfaceMethodOrFieldRest
            {
            dbg.location(336,9);
            pushFollow(FOLLOW_type_in_interfaceMethodOrFieldDecl1276);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(336,14);
            match(input,Identifier,FOLLOW_Identifier_in_interfaceMethodOrFieldDecl1278); if (state.failed) return ;
            dbg.location(336,25);
            pushFollow(FOLLOW_interfaceMethodOrFieldRest_in_interfaceMethodOrFieldDecl1280);
            interfaceMethodOrFieldRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 33, interfaceMethodOrFieldDecl_StartIndex); }
        }
        dbg.location(337, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceMethodOrFieldDecl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceMethodOrFieldDecl"


    // $ANTLR start "interfaceMethodOrFieldRest"
    // Java.g:339:1: interfaceMethodOrFieldRest : ( constantDeclaratorsRest ';' | interfaceMethodDeclaratorRest );
    public final void interfaceMethodOrFieldRest() throws RecognitionException {
        int interfaceMethodOrFieldRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceMethodOrFieldRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(339, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return ; }
            // Java.g:340:5: ( constantDeclaratorsRest ';' | interfaceMethodDeclaratorRest )
            int alt46=2;
            try { dbg.enterDecision(46);

            int LA46_0 = input.LA(1);

            if ( (LA46_0==48||LA46_0==51) ) {
                alt46=1;
            }
            else if ( (LA46_0==66) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:340:9: constantDeclaratorsRest ';'
                    {
                    dbg.location(340,9);
                    pushFollow(FOLLOW_constantDeclaratorsRest_in_interfaceMethodOrFieldRest1303);
                    constantDeclaratorsRest();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(340,33);
                    match(input,26,FOLLOW_26_in_interfaceMethodOrFieldRest1305); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:341:9: interfaceMethodDeclaratorRest
                    {
                    dbg.location(341,9);
                    pushFollow(FOLLOW_interfaceMethodDeclaratorRest_in_interfaceMethodOrFieldRest1315);
                    interfaceMethodDeclaratorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 34, interfaceMethodOrFieldRest_StartIndex); }
        }
        dbg.location(342, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceMethodOrFieldRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceMethodOrFieldRest"


    // $ANTLR start "methodDeclaratorRest"
    // Java.g:344:1: methodDeclaratorRest : formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' ) ;
    public final void methodDeclaratorRest() throws RecognitionException {
        int methodDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "methodDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(344, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return ; }
            // Java.g:345:5: ( formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' ) )
            dbg.enterAlt(1);

            // Java.g:345:9: formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( methodBody | ';' )
            {
            dbg.location(345,9);
            pushFollow(FOLLOW_formalParameters_in_methodDeclaratorRest1338);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(345,26);
            // Java.g:345:26: ( '[' ']' )*
            try { dbg.enterSubRule(47);

            loop47:
            do {
                int alt47=2;
                try { dbg.enterDecision(47);

                int LA47_0 = input.LA(1);

                if ( (LA47_0==48) ) {
                    alt47=1;
                }


                } finally {dbg.exitDecision(47);}

                switch (alt47) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:345:27: '[' ']'
            	    {
            	    dbg.location(345,27);
            	    match(input,48,FOLLOW_48_in_methodDeclaratorRest1341); if (state.failed) return ;
            	    dbg.location(345,31);
            	    match(input,49,FOLLOW_49_in_methodDeclaratorRest1343); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);
            } finally {dbg.exitSubRule(47);}

            dbg.location(346,9);
            // Java.g:346:9: ( 'throws' qualifiedNameList )?
            int alt48=2;
            try { dbg.enterSubRule(48);
            try { dbg.enterDecision(48);

            int LA48_0 = input.LA(1);

            if ( (LA48_0==50) ) {
                alt48=1;
            }
            } finally {dbg.exitDecision(48);}

            switch (alt48) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:346:10: 'throws' qualifiedNameList
                    {
                    dbg.location(346,10);
                    match(input,50,FOLLOW_50_in_methodDeclaratorRest1356); if (state.failed) return ;
                    dbg.location(346,19);
                    pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaratorRest1358);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}

            dbg.location(347,9);
            // Java.g:347:9: ( methodBody | ';' )
            int alt49=2;
            try { dbg.enterSubRule(49);
            try { dbg.enterDecision(49);

            int LA49_0 = input.LA(1);

            if ( (LA49_0==44) ) {
                alt49=1;
            }
            else if ( (LA49_0==26) ) {
                alt49=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(49);}

            switch (alt49) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:347:13: methodBody
                    {
                    dbg.location(347,13);
                    pushFollow(FOLLOW_methodBody_in_methodDeclaratorRest1374);
                    methodBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:348:13: ';'
                    {
                    dbg.location(348,13);
                    match(input,26,FOLLOW_26_in_methodDeclaratorRest1388); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(49);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 35, methodDeclaratorRest_StartIndex); }
        }
        dbg.location(350, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "methodDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "methodDeclaratorRest"


    // $ANTLR start "voidMethodDeclaratorRest"
    // Java.g:352:1: voidMethodDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' ) ;
    public final void voidMethodDeclaratorRest() throws RecognitionException {
        int voidMethodDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "voidMethodDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(352, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return ; }
            // Java.g:353:5: ( formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' ) )
            dbg.enterAlt(1);

            // Java.g:353:9: formalParameters ( 'throws' qualifiedNameList )? ( methodBody | ';' )
            {
            dbg.location(353,9);
            pushFollow(FOLLOW_formalParameters_in_voidMethodDeclaratorRest1421);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(353,26);
            // Java.g:353:26: ( 'throws' qualifiedNameList )?
            int alt50=2;
            try { dbg.enterSubRule(50);
            try { dbg.enterDecision(50);

            int LA50_0 = input.LA(1);

            if ( (LA50_0==50) ) {
                alt50=1;
            }
            } finally {dbg.exitDecision(50);}

            switch (alt50) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:353:27: 'throws' qualifiedNameList
                    {
                    dbg.location(353,27);
                    match(input,50,FOLLOW_50_in_voidMethodDeclaratorRest1424); if (state.failed) return ;
                    dbg.location(353,36);
                    pushFollow(FOLLOW_qualifiedNameList_in_voidMethodDeclaratorRest1426);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(50);}

            dbg.location(354,9);
            // Java.g:354:9: ( methodBody | ';' )
            int alt51=2;
            try { dbg.enterSubRule(51);
            try { dbg.enterDecision(51);

            int LA51_0 = input.LA(1);

            if ( (LA51_0==44) ) {
                alt51=1;
            }
            else if ( (LA51_0==26) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(51);}

            switch (alt51) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:354:13: methodBody
                    {
                    dbg.location(354,13);
                    pushFollow(FOLLOW_methodBody_in_voidMethodDeclaratorRest1442);
                    methodBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:355:13: ';'
                    {
                    dbg.location(355,13);
                    match(input,26,FOLLOW_26_in_voidMethodDeclaratorRest1456); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 36, voidMethodDeclaratorRest_StartIndex); }
        }
        dbg.location(357, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "voidMethodDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "voidMethodDeclaratorRest"


    // $ANTLR start "interfaceMethodDeclaratorRest"
    // Java.g:359:1: interfaceMethodDeclaratorRest : formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' ;
    public final void interfaceMethodDeclaratorRest() throws RecognitionException {
        int interfaceMethodDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceMethodDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(359, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return ; }
            // Java.g:360:5: ( formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' )
            dbg.enterAlt(1);

            // Java.g:360:9: formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';'
            {
            dbg.location(360,9);
            pushFollow(FOLLOW_formalParameters_in_interfaceMethodDeclaratorRest1489);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(360,26);
            // Java.g:360:26: ( '[' ']' )*
            try { dbg.enterSubRule(52);

            loop52:
            do {
                int alt52=2;
                try { dbg.enterDecision(52);

                int LA52_0 = input.LA(1);

                if ( (LA52_0==48) ) {
                    alt52=1;
                }


                } finally {dbg.exitDecision(52);}

                switch (alt52) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:360:27: '[' ']'
            	    {
            	    dbg.location(360,27);
            	    match(input,48,FOLLOW_48_in_interfaceMethodDeclaratorRest1492); if (state.failed) return ;
            	    dbg.location(360,31);
            	    match(input,49,FOLLOW_49_in_interfaceMethodDeclaratorRest1494); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);
            } finally {dbg.exitSubRule(52);}

            dbg.location(360,37);
            // Java.g:360:37: ( 'throws' qualifiedNameList )?
            int alt53=2;
            try { dbg.enterSubRule(53);
            try { dbg.enterDecision(53);

            int LA53_0 = input.LA(1);

            if ( (LA53_0==50) ) {
                alt53=1;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:360:38: 'throws' qualifiedNameList
                    {
                    dbg.location(360,38);
                    match(input,50,FOLLOW_50_in_interfaceMethodDeclaratorRest1499); if (state.failed) return ;
                    dbg.location(360,47);
                    pushFollow(FOLLOW_qualifiedNameList_in_interfaceMethodDeclaratorRest1501);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(360,67);
            match(input,26,FOLLOW_26_in_interfaceMethodDeclaratorRest1505); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 37, interfaceMethodDeclaratorRest_StartIndex); }
        }
        dbg.location(361, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceMethodDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceMethodDeclaratorRest"


    // $ANTLR start "interfaceGenericMethodDecl"
    // Java.g:363:1: interfaceGenericMethodDecl : typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest ;
    public final void interfaceGenericMethodDecl() throws RecognitionException {
        int interfaceGenericMethodDecl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "interfaceGenericMethodDecl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(363, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return ; }
            // Java.g:364:5: ( typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest )
            dbg.enterAlt(1);

            // Java.g:364:9: typeParameters ( type | 'void' ) Identifier interfaceMethodDeclaratorRest
            {
            dbg.location(364,9);
            pushFollow(FOLLOW_typeParameters_in_interfaceGenericMethodDecl1528);
            typeParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(364,24);
            // Java.g:364:24: ( type | 'void' )
            int alt54=2;
            try { dbg.enterSubRule(54);
            try { dbg.enterDecision(54);

            int LA54_0 = input.LA(1);

            if ( (LA54_0==Identifier||(LA54_0>=56 && LA54_0<=63)) ) {
                alt54=1;
            }
            else if ( (LA54_0==47) ) {
                alt54=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:364:25: type
                    {
                    dbg.location(364,25);
                    pushFollow(FOLLOW_type_in_interfaceGenericMethodDecl1531);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:364:32: 'void'
                    {
                    dbg.location(364,32);
                    match(input,47,FOLLOW_47_in_interfaceGenericMethodDecl1535); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(364,40);
            match(input,Identifier,FOLLOW_Identifier_in_interfaceGenericMethodDecl1538); if (state.failed) return ;
            dbg.location(365,9);
            pushFollow(FOLLOW_interfaceMethodDeclaratorRest_in_interfaceGenericMethodDecl1548);
            interfaceMethodDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 38, interfaceGenericMethodDecl_StartIndex); }
        }
        dbg.location(366, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "interfaceGenericMethodDecl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "interfaceGenericMethodDecl"


    // $ANTLR start "voidInterfaceMethodDeclaratorRest"
    // Java.g:368:1: voidInterfaceMethodDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? ';' ;
    public final void voidInterfaceMethodDeclaratorRest() throws RecognitionException {
        int voidInterfaceMethodDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "voidInterfaceMethodDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(368, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return ; }
            // Java.g:369:5: ( formalParameters ( 'throws' qualifiedNameList )? ';' )
            dbg.enterAlt(1);

            // Java.g:369:9: formalParameters ( 'throws' qualifiedNameList )? ';'
            {
            dbg.location(369,9);
            pushFollow(FOLLOW_formalParameters_in_voidInterfaceMethodDeclaratorRest1571);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(369,26);
            // Java.g:369:26: ( 'throws' qualifiedNameList )?
            int alt55=2;
            try { dbg.enterSubRule(55);
            try { dbg.enterDecision(55);

            int LA55_0 = input.LA(1);

            if ( (LA55_0==50) ) {
                alt55=1;
            }
            } finally {dbg.exitDecision(55);}

            switch (alt55) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:369:27: 'throws' qualifiedNameList
                    {
                    dbg.location(369,27);
                    match(input,50,FOLLOW_50_in_voidInterfaceMethodDeclaratorRest1574); if (state.failed) return ;
                    dbg.location(369,36);
                    pushFollow(FOLLOW_qualifiedNameList_in_voidInterfaceMethodDeclaratorRest1576);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(369,56);
            match(input,26,FOLLOW_26_in_voidInterfaceMethodDeclaratorRest1580); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 39, voidInterfaceMethodDeclaratorRest_StartIndex); }
        }
        dbg.location(370, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "voidInterfaceMethodDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "voidInterfaceMethodDeclaratorRest"


    // $ANTLR start "constructorDeclaratorRest"
    // Java.g:372:1: constructorDeclaratorRest : formalParameters ( 'throws' qualifiedNameList )? constructorBody ;
    public final void constructorDeclaratorRest() throws RecognitionException {
        int constructorDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constructorDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(372, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return ; }
            // Java.g:373:5: ( formalParameters ( 'throws' qualifiedNameList )? constructorBody )
            dbg.enterAlt(1);

            // Java.g:373:9: formalParameters ( 'throws' qualifiedNameList )? constructorBody
            {
            dbg.location(373,9);
            pushFollow(FOLLOW_formalParameters_in_constructorDeclaratorRest1603);
            formalParameters();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(373,26);
            // Java.g:373:26: ( 'throws' qualifiedNameList )?
            int alt56=2;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56);

            int LA56_0 = input.LA(1);

            if ( (LA56_0==50) ) {
                alt56=1;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:373:27: 'throws' qualifiedNameList
                    {
                    dbg.location(373,27);
                    match(input,50,FOLLOW_50_in_constructorDeclaratorRest1606); if (state.failed) return ;
                    dbg.location(373,36);
                    pushFollow(FOLLOW_qualifiedNameList_in_constructorDeclaratorRest1608);
                    qualifiedNameList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(373,56);
            pushFollow(FOLLOW_constructorBody_in_constructorDeclaratorRest1612);
            constructorBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 40, constructorDeclaratorRest_StartIndex); }
        }
        dbg.location(374, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constructorDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constructorDeclaratorRest"


    // $ANTLR start "constantDeclarator"
    // Java.g:376:1: constantDeclarator : Identifier constantDeclaratorRest ;
    public final void constantDeclarator() throws RecognitionException {
        int constantDeclarator_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constantDeclarator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(376, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return ; }
            // Java.g:377:5: ( Identifier constantDeclaratorRest )
            dbg.enterAlt(1);

            // Java.g:377:9: Identifier constantDeclaratorRest
            {
            dbg.location(377,9);
            match(input,Identifier,FOLLOW_Identifier_in_constantDeclarator1631); if (state.failed) return ;
            dbg.location(377,20);
            pushFollow(FOLLOW_constantDeclaratorRest_in_constantDeclarator1633);
            constantDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 41, constantDeclarator_StartIndex); }
        }
        dbg.location(378, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constantDeclarator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constantDeclarator"


    // $ANTLR start "variableDeclarators"
    // Java.g:380:1: variableDeclarators : variableDeclarator ( ',' variableDeclarator )* ;
    public final void variableDeclarators() throws RecognitionException {
        int variableDeclarators_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableDeclarators");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return ; }
            // Java.g:381:5: ( variableDeclarator ( ',' variableDeclarator )* )
            dbg.enterAlt(1);

            // Java.g:381:9: variableDeclarator ( ',' variableDeclarator )*
            {
            dbg.location(381,9);
            pushFollow(FOLLOW_variableDeclarator_in_variableDeclarators1656);
            variableDeclarator();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(381,28);
            // Java.g:381:28: ( ',' variableDeclarator )*
            try { dbg.enterSubRule(57);

            loop57:
            do {
                int alt57=2;
                try { dbg.enterDecision(57);

                int LA57_0 = input.LA(1);

                if ( (LA57_0==41) ) {
                    alt57=1;
                }


                } finally {dbg.exitDecision(57);}

                switch (alt57) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:381:29: ',' variableDeclarator
            	    {
            	    dbg.location(381,29);
            	    match(input,41,FOLLOW_41_in_variableDeclarators1659); if (state.failed) return ;
            	    dbg.location(381,33);
            	    pushFollow(FOLLOW_variableDeclarator_in_variableDeclarators1661);
            	    variableDeclarator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);
            } finally {dbg.exitSubRule(57);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 42, variableDeclarators_StartIndex); }
        }
        dbg.location(382, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableDeclarators");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableDeclarators"


    // $ANTLR start "variableDeclarator"
    // Java.g:384:1: variableDeclarator : variableDeclaratorId ( '=' variableInitializer )? ;
    public final void variableDeclarator() throws RecognitionException {
        int variableDeclarator_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableDeclarator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return ; }
            // Java.g:385:5: ( variableDeclaratorId ( '=' variableInitializer )? )
            dbg.enterAlt(1);

            // Java.g:385:9: variableDeclaratorId ( '=' variableInitializer )?
            {
            dbg.location(385,9);
            pushFollow(FOLLOW_variableDeclaratorId_in_variableDeclarator1682);
            variableDeclaratorId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(385,30);
            // Java.g:385:30: ( '=' variableInitializer )?
            int alt58=2;
            try { dbg.enterSubRule(58);
            try { dbg.enterDecision(58);

            int LA58_0 = input.LA(1);

            if ( (LA58_0==51) ) {
                alt58=1;
            }
            } finally {dbg.exitDecision(58);}

            switch (alt58) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:385:31: '=' variableInitializer
                    {
                    dbg.location(385,31);
                    match(input,51,FOLLOW_51_in_variableDeclarator1685); if (state.failed) return ;
                    dbg.location(385,35);
                    pushFollow(FOLLOW_variableInitializer_in_variableDeclarator1687);
                    variableInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 43, variableDeclarator_StartIndex); }
        }
        dbg.location(386, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableDeclarator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableDeclarator"


    // $ANTLR start "constantDeclaratorsRest"
    // Java.g:388:1: constantDeclaratorsRest : constantDeclaratorRest ( ',' constantDeclarator )* ;
    public final void constantDeclaratorsRest() throws RecognitionException {
        int constantDeclaratorsRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constantDeclaratorsRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return ; }
            // Java.g:389:5: ( constantDeclaratorRest ( ',' constantDeclarator )* )
            dbg.enterAlt(1);

            // Java.g:389:9: constantDeclaratorRest ( ',' constantDeclarator )*
            {
            dbg.location(389,9);
            pushFollow(FOLLOW_constantDeclaratorRest_in_constantDeclaratorsRest1712);
            constantDeclaratorRest();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,32);
            // Java.g:389:32: ( ',' constantDeclarator )*
            try { dbg.enterSubRule(59);

            loop59:
            do {
                int alt59=2;
                try { dbg.enterDecision(59);

                int LA59_0 = input.LA(1);

                if ( (LA59_0==41) ) {
                    alt59=1;
                }


                } finally {dbg.exitDecision(59);}

                switch (alt59) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:389:33: ',' constantDeclarator
            	    {
            	    dbg.location(389,33);
            	    match(input,41,FOLLOW_41_in_constantDeclaratorsRest1715); if (state.failed) return ;
            	    dbg.location(389,37);
            	    pushFollow(FOLLOW_constantDeclarator_in_constantDeclaratorsRest1717);
            	    constantDeclarator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);
            } finally {dbg.exitSubRule(59);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 44, constantDeclaratorsRest_StartIndex); }
        }
        dbg.location(390, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constantDeclaratorsRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constantDeclaratorsRest"


    // $ANTLR start "constantDeclaratorRest"
    // Java.g:392:1: constantDeclaratorRest : ( '[' ']' )* '=' variableInitializer ;
    public final void constantDeclaratorRest() throws RecognitionException {
        int constantDeclaratorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constantDeclaratorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return ; }
            // Java.g:393:5: ( ( '[' ']' )* '=' variableInitializer )
            dbg.enterAlt(1);

            // Java.g:393:9: ( '[' ']' )* '=' variableInitializer
            {
            dbg.location(393,9);
            // Java.g:393:9: ( '[' ']' )*
            try { dbg.enterSubRule(60);

            loop60:
            do {
                int alt60=2;
                try { dbg.enterDecision(60);

                int LA60_0 = input.LA(1);

                if ( (LA60_0==48) ) {
                    alt60=1;
                }


                } finally {dbg.exitDecision(60);}

                switch (alt60) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:393:10: '[' ']'
            	    {
            	    dbg.location(393,10);
            	    match(input,48,FOLLOW_48_in_constantDeclaratorRest1739); if (state.failed) return ;
            	    dbg.location(393,14);
            	    match(input,49,FOLLOW_49_in_constantDeclaratorRest1741); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);
            } finally {dbg.exitSubRule(60);}

            dbg.location(393,20);
            match(input,51,FOLLOW_51_in_constantDeclaratorRest1745); if (state.failed) return ;
            dbg.location(393,24);
            pushFollow(FOLLOW_variableInitializer_in_constantDeclaratorRest1747);
            variableInitializer();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 45, constantDeclaratorRest_StartIndex); }
        }
        dbg.location(394, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constantDeclaratorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constantDeclaratorRest"


    // $ANTLR start "variableDeclaratorId"
    // Java.g:396:1: variableDeclaratorId : Identifier ( '[' ']' )* ;
    public final void variableDeclaratorId() throws RecognitionException {
        int variableDeclaratorId_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableDeclaratorId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(396, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return ; }
            // Java.g:397:5: ( Identifier ( '[' ']' )* )
            dbg.enterAlt(1);

            // Java.g:397:9: Identifier ( '[' ']' )*
            {
            dbg.location(397,9);
            match(input,Identifier,FOLLOW_Identifier_in_variableDeclaratorId1770); if (state.failed) return ;
            dbg.location(397,20);
            // Java.g:397:20: ( '[' ']' )*
            try { dbg.enterSubRule(61);

            loop61:
            do {
                int alt61=2;
                try { dbg.enterDecision(61);

                int LA61_0 = input.LA(1);

                if ( (LA61_0==48) ) {
                    alt61=1;
                }


                } finally {dbg.exitDecision(61);}

                switch (alt61) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:397:21: '[' ']'
            	    {
            	    dbg.location(397,21);
            	    match(input,48,FOLLOW_48_in_variableDeclaratorId1773); if (state.failed) return ;
            	    dbg.location(397,25);
            	    match(input,49,FOLLOW_49_in_variableDeclaratorId1775); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);
            } finally {dbg.exitSubRule(61);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 46, variableDeclaratorId_StartIndex); }
        }
        dbg.location(398, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableDeclaratorId");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableDeclaratorId"


    // $ANTLR start "variableInitializer"
    // Java.g:400:1: variableInitializer : ( arrayInitializer | expression );
    public final void variableInitializer() throws RecognitionException {
        int variableInitializer_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableInitializer");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(400, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return ; }
            // Java.g:401:5: ( arrayInitializer | expression )
            int alt62=2;
            try { dbg.enterDecision(62);

            int LA62_0 = input.LA(1);

            if ( (LA62_0==44) ) {
                alt62=1;
            }
            else if ( (LA62_0==Identifier||(LA62_0>=FloatingPointLiteral && LA62_0<=DecimalLiteral)||LA62_0==47||(LA62_0>=56 && LA62_0<=63)||(LA62_0>=65 && LA62_0<=66)||(LA62_0>=69 && LA62_0<=72)||(LA62_0>=105 && LA62_0<=106)||(LA62_0>=109 && LA62_0<=113)) ) {
                alt62=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(62);}

            switch (alt62) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:401:9: arrayInitializer
                    {
                    dbg.location(401,9);
                    pushFollow(FOLLOW_arrayInitializer_in_variableInitializer1796);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:402:9: expression
                    {
                    dbg.location(402,9);
                    pushFollow(FOLLOW_expression_in_variableInitializer1806);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 47, variableInitializer_StartIndex); }
        }
        dbg.location(403, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableInitializer");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableInitializer"


    // $ANTLR start "arrayInitializer"
    // Java.g:405:1: arrayInitializer : '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}' ;
    public final void arrayInitializer() throws RecognitionException {
        int arrayInitializer_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "arrayInitializer");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return ; }
            // Java.g:406:5: ( '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}' )
            dbg.enterAlt(1);

            // Java.g:406:9: '{' ( variableInitializer ( ',' variableInitializer )* ( ',' )? )? '}'
            {
            dbg.location(406,9);
            match(input,44,FOLLOW_44_in_arrayInitializer1833); if (state.failed) return ;
            dbg.location(406,13);
            // Java.g:406:13: ( variableInitializer ( ',' variableInitializer )* ( ',' )? )?
            int alt65=2;
            try { dbg.enterSubRule(65);
            try { dbg.enterDecision(65);

            int LA65_0 = input.LA(1);

            if ( (LA65_0==Identifier||(LA65_0>=FloatingPointLiteral && LA65_0<=DecimalLiteral)||LA65_0==44||LA65_0==47||(LA65_0>=56 && LA65_0<=63)||(LA65_0>=65 && LA65_0<=66)||(LA65_0>=69 && LA65_0<=72)||(LA65_0>=105 && LA65_0<=106)||(LA65_0>=109 && LA65_0<=113)) ) {
                alt65=1;
            }
            } finally {dbg.exitDecision(65);}

            switch (alt65) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:406:14: variableInitializer ( ',' variableInitializer )* ( ',' )?
                    {
                    dbg.location(406,14);
                    pushFollow(FOLLOW_variableInitializer_in_arrayInitializer1836);
                    variableInitializer();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(406,34);
                    // Java.g:406:34: ( ',' variableInitializer )*
                    try { dbg.enterSubRule(63);

                    loop63:
                    do {
                        int alt63=2;
                        try { dbg.enterDecision(63);

                        int LA63_0 = input.LA(1);

                        if ( (LA63_0==41) ) {
                            int LA63_1 = input.LA(2);

                            if ( (LA63_1==Identifier||(LA63_1>=FloatingPointLiteral && LA63_1<=DecimalLiteral)||LA63_1==44||LA63_1==47||(LA63_1>=56 && LA63_1<=63)||(LA63_1>=65 && LA63_1<=66)||(LA63_1>=69 && LA63_1<=72)||(LA63_1>=105 && LA63_1<=106)||(LA63_1>=109 && LA63_1<=113)) ) {
                                alt63=1;
                            }


                        }


                        } finally {dbg.exitDecision(63);}

                        switch (alt63) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:406:35: ',' variableInitializer
                    	    {
                    	    dbg.location(406,35);
                    	    match(input,41,FOLLOW_41_in_arrayInitializer1839); if (state.failed) return ;
                    	    dbg.location(406,39);
                    	    pushFollow(FOLLOW_variableInitializer_in_arrayInitializer1841);
                    	    variableInitializer();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop63;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(63);}

                    dbg.location(406,61);
                    // Java.g:406:61: ( ',' )?
                    int alt64=2;
                    try { dbg.enterSubRule(64);
                    try { dbg.enterDecision(64);

                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==41) ) {
                        alt64=1;
                    }
                    } finally {dbg.exitDecision(64);}

                    switch (alt64) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:406:62: ','
                            {
                            dbg.location(406,62);
                            match(input,41,FOLLOW_41_in_arrayInitializer1846); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(64);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(406,71);
            match(input,45,FOLLOW_45_in_arrayInitializer1853); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 48, arrayInitializer_StartIndex); }
        }
        dbg.location(407, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "arrayInitializer");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "arrayInitializer"


    // $ANTLR start "modifier"
    // Java.g:409:1: modifier : ( annotation | 'public' | 'protected' | 'private' | 'static' | 'abstract' | 'final' | 'native' | 'synchronized' | 'transient' | 'volatile' | 'strictfp' );
    public final void modifier() throws RecognitionException {
        int modifier_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "modifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return ; }
            // Java.g:410:5: ( annotation | 'public' | 'protected' | 'private' | 'static' | 'abstract' | 'final' | 'native' | 'synchronized' | 'transient' | 'volatile' | 'strictfp' )
            int alt66=12;
            try { dbg.enterDecision(66);

            switch ( input.LA(1) ) {
            case 73:
                {
                alt66=1;
                }
                break;
            case 31:
                {
                alt66=2;
                }
                break;
            case 32:
                {
                alt66=3;
                }
                break;
            case 33:
                {
                alt66=4;
                }
                break;
            case 28:
                {
                alt66=5;
                }
                break;
            case 34:
                {
                alt66=6;
                }
                break;
            case 35:
                {
                alt66=7;
                }
                break;
            case 52:
                {
                alt66=8;
                }
                break;
            case 53:
                {
                alt66=9;
                }
                break;
            case 54:
                {
                alt66=10;
                }
                break;
            case 55:
                {
                alt66=11;
                }
                break;
            case 36:
                {
                alt66=12;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(66);}

            switch (alt66) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:410:9: annotation
                    {
                    dbg.location(410,9);
                    pushFollow(FOLLOW_annotation_in_modifier1872);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:411:9: 'public'
                    {
                    dbg.location(411,9);
                    match(input,31,FOLLOW_31_in_modifier1882); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:412:9: 'protected'
                    {
                    dbg.location(412,9);
                    match(input,32,FOLLOW_32_in_modifier1892); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:413:9: 'private'
                    {
                    dbg.location(413,9);
                    match(input,33,FOLLOW_33_in_modifier1902); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:414:9: 'static'
                    {
                    dbg.location(414,9);
                    match(input,28,FOLLOW_28_in_modifier1912); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:415:9: 'abstract'
                    {
                    dbg.location(415,9);
                    match(input,34,FOLLOW_34_in_modifier1922); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:416:9: 'final'
                    {
                    dbg.location(416,9);
                    match(input,35,FOLLOW_35_in_modifier1932); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:417:9: 'native'
                    {
                    dbg.location(417,9);
                    match(input,52,FOLLOW_52_in_modifier1942); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // Java.g:418:9: 'synchronized'
                    {
                    dbg.location(418,9);
                    match(input,53,FOLLOW_53_in_modifier1952); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    dbg.enterAlt(10);

                    // Java.g:419:9: 'transient'
                    {
                    dbg.location(419,9);
                    match(input,54,FOLLOW_54_in_modifier1962); if (state.failed) return ;

                    }
                    break;
                case 11 :
                    dbg.enterAlt(11);

                    // Java.g:420:9: 'volatile'
                    {
                    dbg.location(420,9);
                    match(input,55,FOLLOW_55_in_modifier1972); if (state.failed) return ;

                    }
                    break;
                case 12 :
                    dbg.enterAlt(12);

                    // Java.g:421:9: 'strictfp'
                    {
                    dbg.location(421,9);
                    match(input,36,FOLLOW_36_in_modifier1982); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 49, modifier_StartIndex); }
        }
        dbg.location(422, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "modifier");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "modifier"


    // $ANTLR start "packageOrTypeName"
    // Java.g:424:1: packageOrTypeName : qualifiedName ;
    public final void packageOrTypeName() throws RecognitionException {
        int packageOrTypeName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "packageOrTypeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(424, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return ; }
            // Java.g:425:5: ( qualifiedName )
            dbg.enterAlt(1);

            // Java.g:425:9: qualifiedName
            {
            dbg.location(425,9);
            pushFollow(FOLLOW_qualifiedName_in_packageOrTypeName2001);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 50, packageOrTypeName_StartIndex); }
        }
        dbg.location(426, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "packageOrTypeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "packageOrTypeName"


    // $ANTLR start "enumConstantName"
    // Java.g:428:1: enumConstantName : Identifier ;
    public final void enumConstantName() throws RecognitionException {
        int enumConstantName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enumConstantName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return ; }
            // Java.g:429:5: ( Identifier )
            dbg.enterAlt(1);

            // Java.g:429:9: Identifier
            {
            dbg.location(429,9);
            match(input,Identifier,FOLLOW_Identifier_in_enumConstantName2020); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 51, enumConstantName_StartIndex); }
        }
        dbg.location(430, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enumConstantName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enumConstantName"


    // $ANTLR start "typeName"
    // Java.g:432:1: typeName : qualifiedName ;
    public final void typeName() throws RecognitionException {
        int typeName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(432, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return ; }
            // Java.g:433:5: ( qualifiedName )
            dbg.enterAlt(1);

            // Java.g:433:9: qualifiedName
            {
            dbg.location(433,9);
            pushFollow(FOLLOW_qualifiedName_in_typeName2039);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 52, typeName_StartIndex); }
        }
        dbg.location(434, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeName"


    // $ANTLR start "type"
    // Java.g:436:1: type : ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* );
    public final void type() throws RecognitionException {
        int type_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "type");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return ; }
            // Java.g:437:2: ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* )
            int alt69=2;
            try { dbg.enterDecision(69);

            int LA69_0 = input.LA(1);

            if ( (LA69_0==Identifier) ) {
                alt69=1;
            }
            else if ( ((LA69_0>=56 && LA69_0<=63)) ) {
                alt69=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 69, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(69);}

            switch (alt69) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:437:4: classOrInterfaceType ( '[' ']' )*
                    {
                    dbg.location(437,4);
                    pushFollow(FOLLOW_classOrInterfaceType_in_type2053);
                    classOrInterfaceType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(437,25);
                    // Java.g:437:25: ( '[' ']' )*
                    try { dbg.enterSubRule(67);

                    loop67:
                    do {
                        int alt67=2;
                        try { dbg.enterDecision(67);

                        int LA67_0 = input.LA(1);

                        if ( (LA67_0==48) ) {
                            alt67=1;
                        }


                        } finally {dbg.exitDecision(67);}

                        switch (alt67) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:437:26: '[' ']'
                    	    {
                    	    dbg.location(437,26);
                    	    match(input,48,FOLLOW_48_in_type2056); if (state.failed) return ;
                    	    dbg.location(437,30);
                    	    match(input,49,FOLLOW_49_in_type2058); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop67;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(67);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:438:4: primitiveType ( '[' ']' )*
                    {
                    dbg.location(438,4);
                    pushFollow(FOLLOW_primitiveType_in_type2065);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(438,18);
                    // Java.g:438:18: ( '[' ']' )*
                    try { dbg.enterSubRule(68);

                    loop68:
                    do {
                        int alt68=2;
                        try { dbg.enterDecision(68);

                        int LA68_0 = input.LA(1);

                        if ( (LA68_0==48) ) {
                            alt68=1;
                        }


                        } finally {dbg.exitDecision(68);}

                        switch (alt68) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:438:19: '[' ']'
                    	    {
                    	    dbg.location(438,19);
                    	    match(input,48,FOLLOW_48_in_type2068); if (state.failed) return ;
                    	    dbg.location(438,23);
                    	    match(input,49,FOLLOW_49_in_type2070); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop68;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(68);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 53, type_StartIndex); }
        }
        dbg.location(439, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "type");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "type"


    // $ANTLR start "classOrInterfaceType"
    // Java.g:441:1: classOrInterfaceType : Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )* ;
    public final void classOrInterfaceType() throws RecognitionException {
        int classOrInterfaceType_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classOrInterfaceType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return ; }
            // Java.g:442:2: ( Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )* )
            dbg.enterAlt(1);

            // Java.g:442:4: Identifier ( typeArguments )? ( '.' Identifier ( typeArguments )? )*
            {
            dbg.location(442,4);
            match(input,Identifier,FOLLOW_Identifier_in_classOrInterfaceType2083); if (state.failed) return ;
            dbg.location(442,15);
            // Java.g:442:15: ( typeArguments )?
            int alt70=2;
            try { dbg.enterSubRule(70);
            try { dbg.enterDecision(70);

            int LA70_0 = input.LA(1);

            if ( (LA70_0==40) ) {
                int LA70_1 = input.LA(2);

                if ( (LA70_1==Identifier||(LA70_1>=56 && LA70_1<=64)) ) {
                    alt70=1;
                }
            }
            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: typeArguments
                    {
                    dbg.location(442,15);
                    pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2085);
                    typeArguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(442,30);
            // Java.g:442:30: ( '.' Identifier ( typeArguments )? )*
            try { dbg.enterSubRule(72);

            loop72:
            do {
                int alt72=2;
                try { dbg.enterDecision(72);

                int LA72_0 = input.LA(1);

                if ( (LA72_0==29) ) {
                    alt72=1;
                }


                } finally {dbg.exitDecision(72);}

                switch (alt72) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:442:31: '.' Identifier ( typeArguments )?
            	    {
            	    dbg.location(442,31);
            	    match(input,29,FOLLOW_29_in_classOrInterfaceType2089); if (state.failed) return ;
            	    dbg.location(442,35);
            	    match(input,Identifier,FOLLOW_Identifier_in_classOrInterfaceType2091); if (state.failed) return ;
            	    dbg.location(442,46);
            	    // Java.g:442:46: ( typeArguments )?
            	    int alt71=2;
            	    try { dbg.enterSubRule(71);
            	    try { dbg.enterDecision(71);

            	    int LA71_0 = input.LA(1);

            	    if ( (LA71_0==40) ) {
            	        int LA71_1 = input.LA(2);

            	        if ( (LA71_1==Identifier||(LA71_1>=56 && LA71_1<=64)) ) {
            	            alt71=1;
            	        }
            	    }
            	    } finally {dbg.exitDecision(71);}

            	    switch (alt71) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // Java.g:0:0: typeArguments
            	            {
            	            dbg.location(442,46);
            	            pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2093);
            	            typeArguments();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(71);}


            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);
            } finally {dbg.exitSubRule(72);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 54, classOrInterfaceType_StartIndex); }
        }
        dbg.location(443, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classOrInterfaceType");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classOrInterfaceType"


    // $ANTLR start "primitiveType"
    // Java.g:445:1: primitiveType : ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' );
    public final void primitiveType() throws RecognitionException {
        int primitiveType_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "primitiveType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return ; }
            // Java.g:446:5: ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' )
            dbg.enterAlt(1);

            // Java.g:
            {
            dbg.location(446,5);
            if ( (input.LA(1)>=56 && input.LA(1)<=63) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 55, primitiveType_StartIndex); }
        }
        dbg.location(454, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "primitiveType");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "primitiveType"


    // $ANTLR start "variableModifier"
    // Java.g:456:1: variableModifier : ( 'final' | annotation );
    public final void variableModifier() throws RecognitionException {
        int variableModifier_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableModifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(456, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return ; }
            // Java.g:457:5: ( 'final' | annotation )
            int alt73=2;
            try { dbg.enterDecision(73);

            int LA73_0 = input.LA(1);

            if ( (LA73_0==35) ) {
                alt73=1;
            }
            else if ( (LA73_0==73) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(73);}

            switch (alt73) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:457:9: 'final'
                    {
                    dbg.location(457,9);
                    match(input,35,FOLLOW_35_in_variableModifier2202); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:458:9: annotation
                    {
                    dbg.location(458,9);
                    pushFollow(FOLLOW_annotation_in_variableModifier2212);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 56, variableModifier_StartIndex); }
        }
        dbg.location(459, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableModifier");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableModifier"


    // $ANTLR start "typeArguments"
    // Java.g:461:1: typeArguments : '<' typeArgument ( ',' typeArgument )* '>' ;
    public final void typeArguments() throws RecognitionException {
        int typeArguments_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeArguments");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(461, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return ; }
            // Java.g:462:5: ( '<' typeArgument ( ',' typeArgument )* '>' )
            dbg.enterAlt(1);

            // Java.g:462:9: '<' typeArgument ( ',' typeArgument )* '>'
            {
            dbg.location(462,9);
            match(input,40,FOLLOW_40_in_typeArguments2231); if (state.failed) return ;
            dbg.location(462,13);
            pushFollow(FOLLOW_typeArgument_in_typeArguments2233);
            typeArgument();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(462,26);
            // Java.g:462:26: ( ',' typeArgument )*
            try { dbg.enterSubRule(74);

            loop74:
            do {
                int alt74=2;
                try { dbg.enterDecision(74);

                int LA74_0 = input.LA(1);

                if ( (LA74_0==41) ) {
                    alt74=1;
                }


                } finally {dbg.exitDecision(74);}

                switch (alt74) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:462:27: ',' typeArgument
            	    {
            	    dbg.location(462,27);
            	    match(input,41,FOLLOW_41_in_typeArguments2236); if (state.failed) return ;
            	    dbg.location(462,31);
            	    pushFollow(FOLLOW_typeArgument_in_typeArguments2238);
            	    typeArgument();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);
            } finally {dbg.exitSubRule(74);}

            dbg.location(462,46);
            match(input,42,FOLLOW_42_in_typeArguments2242); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 57, typeArguments_StartIndex); }
        }
        dbg.location(463, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeArguments");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeArguments"


    // $ANTLR start "typeArgument"
    // Java.g:465:1: typeArgument : ( type | '?' ( ( 'extends' | 'super' ) type )? );
    public final void typeArgument() throws RecognitionException {
        int typeArgument_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "typeArgument");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(465, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return ; }
            // Java.g:466:5: ( type | '?' ( ( 'extends' | 'super' ) type )? )
            int alt76=2;
            try { dbg.enterDecision(76);

            int LA76_0 = input.LA(1);

            if ( (LA76_0==Identifier||(LA76_0>=56 && LA76_0<=63)) ) {
                alt76=1;
            }
            else if ( (LA76_0==64) ) {
                alt76=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(76);}

            switch (alt76) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:466:9: type
                    {
                    dbg.location(466,9);
                    pushFollow(FOLLOW_type_in_typeArgument2265);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:467:9: '?' ( ( 'extends' | 'super' ) type )?
                    {
                    dbg.location(467,9);
                    match(input,64,FOLLOW_64_in_typeArgument2275); if (state.failed) return ;
                    dbg.location(467,13);
                    // Java.g:467:13: ( ( 'extends' | 'super' ) type )?
                    int alt75=2;
                    try { dbg.enterSubRule(75);
                    try { dbg.enterDecision(75);

                    int LA75_0 = input.LA(1);

                    if ( (LA75_0==38||LA75_0==65) ) {
                        alt75=1;
                    }
                    } finally {dbg.exitDecision(75);}

                    switch (alt75) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:467:14: ( 'extends' | 'super' ) type
                            {
                            dbg.location(467,14);
                            if ( input.LA(1)==38||input.LA(1)==65 ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                dbg.recognitionException(mse);
                                throw mse;
                            }

                            dbg.location(467,36);
                            pushFollow(FOLLOW_type_in_typeArgument2286);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 58, typeArgument_StartIndex); }
        }
        dbg.location(468, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeArgument");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeArgument"


    // $ANTLR start "qualifiedNameList"
    // Java.g:470:1: qualifiedNameList : qualifiedName ( ',' qualifiedName )* ;
    public final void qualifiedNameList() throws RecognitionException {
        int qualifiedNameList_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "qualifiedNameList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(470, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return ; }
            // Java.g:471:5: ( qualifiedName ( ',' qualifiedName )* )
            dbg.enterAlt(1);

            // Java.g:471:9: qualifiedName ( ',' qualifiedName )*
            {
            dbg.location(471,9);
            pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList2311);
            qualifiedName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(471,23);
            // Java.g:471:23: ( ',' qualifiedName )*
            try { dbg.enterSubRule(77);

            loop77:
            do {
                int alt77=2;
                try { dbg.enterDecision(77);

                int LA77_0 = input.LA(1);

                if ( (LA77_0==41) ) {
                    alt77=1;
                }


                } finally {dbg.exitDecision(77);}

                switch (alt77) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:471:24: ',' qualifiedName
            	    {
            	    dbg.location(471,24);
            	    match(input,41,FOLLOW_41_in_qualifiedNameList2314); if (state.failed) return ;
            	    dbg.location(471,28);
            	    pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList2316);
            	    qualifiedName();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop77;
                }
            } while (true);
            } finally {dbg.exitSubRule(77);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 59, qualifiedNameList_StartIndex); }
        }
        dbg.location(472, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "qualifiedNameList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "qualifiedNameList"


    // $ANTLR start "formalParameters"
    // Java.g:474:1: formalParameters : '(' ( formalParameterDecls )? ')' ;
    public final void formalParameters() throws RecognitionException {
        int formalParameters_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "formalParameters");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(474, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return ; }
            // Java.g:475:5: ( '(' ( formalParameterDecls )? ')' )
            dbg.enterAlt(1);

            // Java.g:475:9: '(' ( formalParameterDecls )? ')'
            {
            dbg.location(475,9);
            match(input,66,FOLLOW_66_in_formalParameters2337); if (state.failed) return ;
            dbg.location(475,13);
            // Java.g:475:13: ( formalParameterDecls )?
            int alt78=2;
            try { dbg.enterSubRule(78);
            try { dbg.enterDecision(78);

            int LA78_0 = input.LA(1);

            if ( (LA78_0==Identifier||LA78_0==35||(LA78_0>=56 && LA78_0<=63)||LA78_0==73) ) {
                alt78=1;
            }
            } finally {dbg.exitDecision(78);}

            switch (alt78) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: formalParameterDecls
                    {
                    dbg.location(475,13);
                    pushFollow(FOLLOW_formalParameterDecls_in_formalParameters2339);
                    formalParameterDecls();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(475,35);
            match(input,67,FOLLOW_67_in_formalParameters2342); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 60, formalParameters_StartIndex); }
        }
        dbg.location(476, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "formalParameters");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "formalParameters"


    // $ANTLR start "formalParameterDecls"
    // Java.g:478:1: formalParameterDecls : variableModifiers type formalParameterDeclsRest ;
    public final void formalParameterDecls() throws RecognitionException {
        int formalParameterDecls_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "formalParameterDecls");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(478, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return ; }
            // Java.g:479:5: ( variableModifiers type formalParameterDeclsRest )
            dbg.enterAlt(1);

            // Java.g:479:9: variableModifiers type formalParameterDeclsRest
            {
            dbg.location(479,9);
            pushFollow(FOLLOW_variableModifiers_in_formalParameterDecls2365);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(479,27);
            pushFollow(FOLLOW_type_in_formalParameterDecls2367);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(479,32);
            pushFollow(FOLLOW_formalParameterDeclsRest_in_formalParameterDecls2369);
            formalParameterDeclsRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 61, formalParameterDecls_StartIndex); }
        }
        dbg.location(480, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "formalParameterDecls");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "formalParameterDecls"


    // $ANTLR start "formalParameterDeclsRest"
    // Java.g:482:1: formalParameterDeclsRest : ( variableDeclaratorId ( ',' formalParameterDecls )? | '...' variableDeclaratorId );
    public final void formalParameterDeclsRest() throws RecognitionException {
        int formalParameterDeclsRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "formalParameterDeclsRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(482, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return ; }
            // Java.g:483:5: ( variableDeclaratorId ( ',' formalParameterDecls )? | '...' variableDeclaratorId )
            int alt80=2;
            try { dbg.enterDecision(80);

            int LA80_0 = input.LA(1);

            if ( (LA80_0==Identifier) ) {
                alt80=1;
            }
            else if ( (LA80_0==68) ) {
                alt80=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:483:9: variableDeclaratorId ( ',' formalParameterDecls )?
                    {
                    dbg.location(483,9);
                    pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2392);
                    variableDeclaratorId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(483,30);
                    // Java.g:483:30: ( ',' formalParameterDecls )?
                    int alt79=2;
                    try { dbg.enterSubRule(79);
                    try { dbg.enterDecision(79);

                    int LA79_0 = input.LA(1);

                    if ( (LA79_0==41) ) {
                        alt79=1;
                    }
                    } finally {dbg.exitDecision(79);}

                    switch (alt79) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:483:31: ',' formalParameterDecls
                            {
                            dbg.location(483,31);
                            match(input,41,FOLLOW_41_in_formalParameterDeclsRest2395); if (state.failed) return ;
                            dbg.location(483,35);
                            pushFollow(FOLLOW_formalParameterDecls_in_formalParameterDeclsRest2397);
                            formalParameterDecls();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(79);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:484:9: '...' variableDeclaratorId
                    {
                    dbg.location(484,9);
                    match(input,68,FOLLOW_68_in_formalParameterDeclsRest2409); if (state.failed) return ;
                    dbg.location(484,15);
                    pushFollow(FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2411);
                    variableDeclaratorId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 62, formalParameterDeclsRest_StartIndex); }
        }
        dbg.location(485, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "formalParameterDeclsRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "formalParameterDeclsRest"


    // $ANTLR start "methodBody"
    // Java.g:487:1: methodBody : block ;
    public final void methodBody() throws RecognitionException {
        int methodBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "methodBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(487, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return ; }
            // Java.g:488:5: ( block )
            dbg.enterAlt(1);

            // Java.g:488:9: block
            {
            dbg.location(488,9);
            pushFollow(FOLLOW_block_in_methodBody2434);
            block();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 63, methodBody_StartIndex); }
        }
        dbg.location(489, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "methodBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "methodBody"


    // $ANTLR start "constructorBody"
    // Java.g:491:1: constructorBody : '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' ;
    public final void constructorBody() throws RecognitionException {
        int constructorBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constructorBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(491, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return ; }
            // Java.g:492:5: ( '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' )
            dbg.enterAlt(1);

            // Java.g:492:9: '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
            {
            dbg.location(492,9);
            match(input,44,FOLLOW_44_in_constructorBody2453); if (state.failed) return ;
            dbg.location(492,13);
            // Java.g:492:13: ( explicitConstructorInvocation )?
            int alt81=2;
            try { dbg.enterSubRule(81);
            try { dbg.enterDecision(81);

            try {
                isCyclicDecision = true;
                alt81 = dfa81.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(81);}

            switch (alt81) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: explicitConstructorInvocation
                    {
                    dbg.location(492,13);
                    pushFollow(FOLLOW_explicitConstructorInvocation_in_constructorBody2455);
                    explicitConstructorInvocation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(492,44);
            // Java.g:492:44: ( blockStatement )*
            try { dbg.enterSubRule(82);

            loop82:
            do {
                int alt82=2;
                try { dbg.enterDecision(82);

                int LA82_0 = input.LA(1);

                if ( ((LA82_0>=Identifier && LA82_0<=ASSERT)||LA82_0==26||LA82_0==28||(LA82_0>=31 && LA82_0<=37)||LA82_0==44||(LA82_0>=46 && LA82_0<=47)||LA82_0==53||(LA82_0>=56 && LA82_0<=63)||(LA82_0>=65 && LA82_0<=66)||(LA82_0>=69 && LA82_0<=73)||LA82_0==76||(LA82_0>=78 && LA82_0<=81)||(LA82_0>=83 && LA82_0<=87)||(LA82_0>=105 && LA82_0<=106)||(LA82_0>=109 && LA82_0<=113)) ) {
                    alt82=1;
                }


                } finally {dbg.exitDecision(82);}

                switch (alt82) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: blockStatement
            	    {
            	    dbg.location(492,44);
            	    pushFollow(FOLLOW_blockStatement_in_constructorBody2458);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop82;
                }
            } while (true);
            } finally {dbg.exitSubRule(82);}

            dbg.location(492,60);
            match(input,45,FOLLOW_45_in_constructorBody2461); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 64, constructorBody_StartIndex); }
        }
        dbg.location(493, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constructorBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constructorBody"


    // $ANTLR start "explicitConstructorInvocation"
    // Java.g:495:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );
    public final void explicitConstructorInvocation() throws RecognitionException {
        int explicitConstructorInvocation_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "explicitConstructorInvocation");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(495, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return ; }
            // Java.g:496:5: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' )
            int alt85=2;
            try { dbg.enterDecision(85);

            try {
                isCyclicDecision = true;
                alt85 = dfa85.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(85);}

            switch (alt85) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:496:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
                    {
                    dbg.location(496,9);
                    // Java.g:496:9: ( nonWildcardTypeArguments )?
                    int alt83=2;
                    try { dbg.enterSubRule(83);
                    try { dbg.enterDecision(83);

                    int LA83_0 = input.LA(1);

                    if ( (LA83_0==40) ) {
                        alt83=1;
                    }
                    } finally {dbg.exitDecision(83);}

                    switch (alt83) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: nonWildcardTypeArguments
                            {
                            dbg.location(496,9);
                            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2480);
                            nonWildcardTypeArguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(83);}

                    dbg.location(496,35);
                    if ( input.LA(1)==65||input.LA(1)==69 ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(496,54);
                    pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation2491);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(496,64);
                    match(input,26,FOLLOW_26_in_explicitConstructorInvocation2493); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:497:9: primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';'
                    {
                    dbg.location(497,9);
                    pushFollow(FOLLOW_primary_in_explicitConstructorInvocation2503);
                    primary();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(497,17);
                    match(input,29,FOLLOW_29_in_explicitConstructorInvocation2505); if (state.failed) return ;
                    dbg.location(497,21);
                    // Java.g:497:21: ( nonWildcardTypeArguments )?
                    int alt84=2;
                    try { dbg.enterSubRule(84);
                    try { dbg.enterDecision(84);

                    int LA84_0 = input.LA(1);

                    if ( (LA84_0==40) ) {
                        alt84=1;
                    }
                    } finally {dbg.exitDecision(84);}

                    switch (alt84) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: nonWildcardTypeArguments
                            {
                            dbg.location(497,21);
                            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2507);
                            nonWildcardTypeArguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(84);}

                    dbg.location(497,47);
                    match(input,65,FOLLOW_65_in_explicitConstructorInvocation2510); if (state.failed) return ;
                    dbg.location(497,55);
                    pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation2512);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(497,65);
                    match(input,26,FOLLOW_26_in_explicitConstructorInvocation2514); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 65, explicitConstructorInvocation_StartIndex); }
        }
        dbg.location(498, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "explicitConstructorInvocation");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "explicitConstructorInvocation"


    // $ANTLR start "qualifiedName"
    // Java.g:501:1: qualifiedName : Identifier ( '.' Identifier )* ;
    public final void qualifiedName() throws RecognitionException {
        int qualifiedName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "qualifiedName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(501, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return ; }
            // Java.g:502:5: ( Identifier ( '.' Identifier )* )
            dbg.enterAlt(1);

            // Java.g:502:9: Identifier ( '.' Identifier )*
            {
            dbg.location(502,9);
            match(input,Identifier,FOLLOW_Identifier_in_qualifiedName2534); if (state.failed) return ;
            dbg.location(502,20);
            // Java.g:502:20: ( '.' Identifier )*
            try { dbg.enterSubRule(86);

            loop86:
            do {
                int alt86=2;
                try { dbg.enterDecision(86);

                int LA86_0 = input.LA(1);

                if ( (LA86_0==29) ) {
                    int LA86_2 = input.LA(2);

                    if ( (LA86_2==Identifier) ) {
                        alt86=1;
                    }


                }


                } finally {dbg.exitDecision(86);}

                switch (alt86) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:502:21: '.' Identifier
            	    {
            	    dbg.location(502,21);
            	    match(input,29,FOLLOW_29_in_qualifiedName2537); if (state.failed) return ;
            	    dbg.location(502,25);
            	    match(input,Identifier,FOLLOW_Identifier_in_qualifiedName2539); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop86;
                }
            } while (true);
            } finally {dbg.exitSubRule(86);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 66, qualifiedName_StartIndex); }
        }
        dbg.location(503, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "qualifiedName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "qualifiedName"


    // $ANTLR start "literal"
    // Java.g:505:1: literal : ( integerLiteral | FloatingPointLiteral | CharacterLiteral | StringLiteral | booleanLiteral | 'null' );
    public final void literal() throws RecognitionException {
        int literal_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "literal");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(505, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return ; }
            // Java.g:506:5: ( integerLiteral | FloatingPointLiteral | CharacterLiteral | StringLiteral | booleanLiteral | 'null' )
            int alt87=6;
            try { dbg.enterDecision(87);

            switch ( input.LA(1) ) {
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
                {
                alt87=1;
                }
                break;
            case FloatingPointLiteral:
                {
                alt87=2;
                }
                break;
            case CharacterLiteral:
                {
                alt87=3;
                }
                break;
            case StringLiteral:
                {
                alt87=4;
                }
                break;
            case 71:
            case 72:
                {
                alt87=5;
                }
                break;
            case 70:
                {
                alt87=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(87);}

            switch (alt87) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:506:9: integerLiteral
                    {
                    dbg.location(506,9);
                    pushFollow(FOLLOW_integerLiteral_in_literal2565);
                    integerLiteral();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:507:9: FloatingPointLiteral
                    {
                    dbg.location(507,9);
                    match(input,FloatingPointLiteral,FOLLOW_FloatingPointLiteral_in_literal2575); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:508:9: CharacterLiteral
                    {
                    dbg.location(508,9);
                    match(input,CharacterLiteral,FOLLOW_CharacterLiteral_in_literal2585); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:509:9: StringLiteral
                    {
                    dbg.location(509,9);
                    match(input,StringLiteral,FOLLOW_StringLiteral_in_literal2595); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:510:9: booleanLiteral
                    {
                    dbg.location(510,9);
                    pushFollow(FOLLOW_booleanLiteral_in_literal2605);
                    booleanLiteral();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:511:9: 'null'
                    {
                    dbg.location(511,9);
                    match(input,70,FOLLOW_70_in_literal2615); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 67, literal_StartIndex); }
        }
        dbg.location(512, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "literal");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "literal"


    // $ANTLR start "integerLiteral"
    // Java.g:514:1: integerLiteral : ( HexLiteral | OctalLiteral | DecimalLiteral );
    public final void integerLiteral() throws RecognitionException {
        int integerLiteral_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "integerLiteral");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(514, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return ; }
            // Java.g:515:5: ( HexLiteral | OctalLiteral | DecimalLiteral )
            dbg.enterAlt(1);

            // Java.g:
            {
            dbg.location(515,5);
            if ( (input.LA(1)>=HexLiteral && input.LA(1)<=DecimalLiteral) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 68, integerLiteral_StartIndex); }
        }
        dbg.location(518, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "integerLiteral");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "integerLiteral"


    // $ANTLR start "booleanLiteral"
    // Java.g:520:1: booleanLiteral : ( 'true' | 'false' );
    public final void booleanLiteral() throws RecognitionException {
        int booleanLiteral_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "booleanLiteral");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(520, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return ; }
            // Java.g:521:5: ( 'true' | 'false' )
            dbg.enterAlt(1);

            // Java.g:
            {
            dbg.location(521,5);
            if ( (input.LA(1)>=71 && input.LA(1)<=72) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 69, booleanLiteral_StartIndex); }
        }
        dbg.location(523, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "booleanLiteral");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "booleanLiteral"


    // $ANTLR start "annotations"
    // Java.g:527:1: annotations : ( annotation )+ ;
    public final void annotations() throws RecognitionException {
        int annotations_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(527, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return ; }
            // Java.g:528:5: ( ( annotation )+ )
            dbg.enterAlt(1);

            // Java.g:528:9: ( annotation )+
            {
            dbg.location(528,9);
            // Java.g:528:9: ( annotation )+
            int cnt88=0;
            try { dbg.enterSubRule(88);

            loop88:
            do {
                int alt88=2;
                try { dbg.enterDecision(88);

                int LA88_0 = input.LA(1);

                if ( (LA88_0==73) ) {
                    int LA88_2 = input.LA(2);

                    if ( (LA88_2==Identifier) ) {
                        int LA88_3 = input.LA(3);

                        if ( (synpred128_Java()) ) {
                            alt88=1;
                        }


                    }


                }


                } finally {dbg.exitDecision(88);}

                switch (alt88) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: annotation
            	    {
            	    dbg.location(528,9);
            	    pushFollow(FOLLOW_annotation_in_annotations2704);
            	    annotation();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt88 >= 1 ) break loop88;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(88, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt88++;
            } while (true);
            } finally {dbg.exitSubRule(88);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 70, annotations_StartIndex); }
        }
        dbg.location(529, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotations"


    // $ANTLR start "annotation"
    // Java.g:531:1: annotation : '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )? ;
    public final void annotation() throws RecognitionException {
        int annotation_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotation");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(531, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return ; }
            // Java.g:532:5: ( '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )? )
            dbg.enterAlt(1);

            // Java.g:532:9: '@' annotationName ( '(' ( elementValuePairs | elementValue )? ')' )?
            {
            dbg.location(532,9);
            match(input,73,FOLLOW_73_in_annotation2724); if (state.failed) return ;
            dbg.location(532,13);
            pushFollow(FOLLOW_annotationName_in_annotation2726);
            annotationName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(532,28);
            // Java.g:532:28: ( '(' ( elementValuePairs | elementValue )? ')' )?
            int alt90=2;
            try { dbg.enterSubRule(90);
            try { dbg.enterDecision(90);

            int LA90_0 = input.LA(1);

            if ( (LA90_0==66) ) {
                alt90=1;
            }
            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:532:30: '(' ( elementValuePairs | elementValue )? ')'
                    {
                    dbg.location(532,30);
                    match(input,66,FOLLOW_66_in_annotation2730); if (state.failed) return ;
                    dbg.location(532,34);
                    // Java.g:532:34: ( elementValuePairs | elementValue )?
                    int alt89=3;
                    try { dbg.enterSubRule(89);
                    try { dbg.enterDecision(89);

                    int LA89_0 = input.LA(1);

                    if ( (LA89_0==Identifier) ) {
                        int LA89_1 = input.LA(2);

                        if ( (LA89_1==51) ) {
                            alt89=1;
                        }
                        else if ( ((LA89_1>=29 && LA89_1<=30)||LA89_1==40||(LA89_1>=42 && LA89_1<=43)||LA89_1==48||LA89_1==64||(LA89_1>=66 && LA89_1<=67)||(LA89_1>=98 && LA89_1<=110)) ) {
                            alt89=2;
                        }
                    }
                    else if ( ((LA89_0>=FloatingPointLiteral && LA89_0<=DecimalLiteral)||LA89_0==44||LA89_0==47||(LA89_0>=56 && LA89_0<=63)||(LA89_0>=65 && LA89_0<=66)||(LA89_0>=69 && LA89_0<=73)||(LA89_0>=105 && LA89_0<=106)||(LA89_0>=109 && LA89_0<=113)) ) {
                        alt89=2;
                    }
                    } finally {dbg.exitDecision(89);}

                    switch (alt89) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:532:36: elementValuePairs
                            {
                            dbg.location(532,36);
                            pushFollow(FOLLOW_elementValuePairs_in_annotation2734);
                            elementValuePairs();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:532:56: elementValue
                            {
                            dbg.location(532,56);
                            pushFollow(FOLLOW_elementValue_in_annotation2738);
                            elementValue();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(89);}

                    dbg.location(532,72);
                    match(input,67,FOLLOW_67_in_annotation2743); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 71, annotation_StartIndex); }
        }
        dbg.location(533, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotation");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotation"


    // $ANTLR start "annotationName"
    // Java.g:535:1: annotationName : Identifier ( '.' Identifier )* ;
    public final void annotationName() throws RecognitionException {
        int annotationName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(535, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return ; }
            // Java.g:536:5: ( Identifier ( '.' Identifier )* )
            dbg.enterAlt(1);

            // Java.g:536:7: Identifier ( '.' Identifier )*
            {
            dbg.location(536,7);
            match(input,Identifier,FOLLOW_Identifier_in_annotationName2767); if (state.failed) return ;
            dbg.location(536,18);
            // Java.g:536:18: ( '.' Identifier )*
            try { dbg.enterSubRule(91);

            loop91:
            do {
                int alt91=2;
                try { dbg.enterDecision(91);

                int LA91_0 = input.LA(1);

                if ( (LA91_0==29) ) {
                    alt91=1;
                }


                } finally {dbg.exitDecision(91);}

                switch (alt91) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:536:19: '.' Identifier
            	    {
            	    dbg.location(536,19);
            	    match(input,29,FOLLOW_29_in_annotationName2770); if (state.failed) return ;
            	    dbg.location(536,23);
            	    match(input,Identifier,FOLLOW_Identifier_in_annotationName2772); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop91;
                }
            } while (true);
            } finally {dbg.exitSubRule(91);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 72, annotationName_StartIndex); }
        }
        dbg.location(537, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationName"


    // $ANTLR start "elementValuePairs"
    // Java.g:539:1: elementValuePairs : elementValuePair ( ',' elementValuePair )* ;
    public final void elementValuePairs() throws RecognitionException {
        int elementValuePairs_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "elementValuePairs");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(539, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return ; }
            // Java.g:540:5: ( elementValuePair ( ',' elementValuePair )* )
            dbg.enterAlt(1);

            // Java.g:540:9: elementValuePair ( ',' elementValuePair )*
            {
            dbg.location(540,9);
            pushFollow(FOLLOW_elementValuePair_in_elementValuePairs2793);
            elementValuePair();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(540,26);
            // Java.g:540:26: ( ',' elementValuePair )*
            try { dbg.enterSubRule(92);

            loop92:
            do {
                int alt92=2;
                try { dbg.enterDecision(92);

                int LA92_0 = input.LA(1);

                if ( (LA92_0==41) ) {
                    alt92=1;
                }


                } finally {dbg.exitDecision(92);}

                switch (alt92) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:540:27: ',' elementValuePair
            	    {
            	    dbg.location(540,27);
            	    match(input,41,FOLLOW_41_in_elementValuePairs2796); if (state.failed) return ;
            	    dbg.location(540,31);
            	    pushFollow(FOLLOW_elementValuePair_in_elementValuePairs2798);
            	    elementValuePair();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop92;
                }
            } while (true);
            } finally {dbg.exitSubRule(92);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 73, elementValuePairs_StartIndex); }
        }
        dbg.location(541, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementValuePairs");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementValuePairs"


    // $ANTLR start "elementValuePair"
    // Java.g:543:1: elementValuePair : Identifier '=' elementValue ;
    public final void elementValuePair() throws RecognitionException {
        int elementValuePair_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "elementValuePair");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(543, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return ; }
            // Java.g:544:5: ( Identifier '=' elementValue )
            dbg.enterAlt(1);

            // Java.g:544:9: Identifier '=' elementValue
            {
            dbg.location(544,9);
            match(input,Identifier,FOLLOW_Identifier_in_elementValuePair2819); if (state.failed) return ;
            dbg.location(544,20);
            match(input,51,FOLLOW_51_in_elementValuePair2821); if (state.failed) return ;
            dbg.location(544,24);
            pushFollow(FOLLOW_elementValue_in_elementValuePair2823);
            elementValue();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 74, elementValuePair_StartIndex); }
        }
        dbg.location(545, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementValuePair");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementValuePair"


    // $ANTLR start "elementValue"
    // Java.g:547:1: elementValue : ( conditionalExpression | annotation | elementValueArrayInitializer );
    public final void elementValue() throws RecognitionException {
        int elementValue_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "elementValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(547, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return ; }
            // Java.g:548:5: ( conditionalExpression | annotation | elementValueArrayInitializer )
            int alt93=3;
            try { dbg.enterDecision(93);

            switch ( input.LA(1) ) {
            case Identifier:
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 47:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 65:
            case 66:
            case 69:
            case 70:
            case 71:
            case 72:
            case 105:
            case 106:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
                {
                alt93=1;
                }
                break;
            case 73:
                {
                alt93=2;
                }
                break;
            case 44:
                {
                alt93=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:548:9: conditionalExpression
                    {
                    dbg.location(548,9);
                    pushFollow(FOLLOW_conditionalExpression_in_elementValue2846);
                    conditionalExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:549:9: annotation
                    {
                    dbg.location(549,9);
                    pushFollow(FOLLOW_annotation_in_elementValue2856);
                    annotation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:550:9: elementValueArrayInitializer
                    {
                    dbg.location(550,9);
                    pushFollow(FOLLOW_elementValueArrayInitializer_in_elementValue2866);
                    elementValueArrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 75, elementValue_StartIndex); }
        }
        dbg.location(551, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementValue"


    // $ANTLR start "elementValueArrayInitializer"
    // Java.g:553:1: elementValueArrayInitializer : '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' ;
    public final void elementValueArrayInitializer() throws RecognitionException {
        int elementValueArrayInitializer_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "elementValueArrayInitializer");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(553, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return ; }
            // Java.g:554:5: ( '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' )
            dbg.enterAlt(1);

            // Java.g:554:9: '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}'
            {
            dbg.location(554,9);
            match(input,44,FOLLOW_44_in_elementValueArrayInitializer2889); if (state.failed) return ;
            dbg.location(554,13);
            // Java.g:554:13: ( elementValue ( ',' elementValue )* )?
            int alt95=2;
            try { dbg.enterSubRule(95);
            try { dbg.enterDecision(95);

            int LA95_0 = input.LA(1);

            if ( (LA95_0==Identifier||(LA95_0>=FloatingPointLiteral && LA95_0<=DecimalLiteral)||LA95_0==44||LA95_0==47||(LA95_0>=56 && LA95_0<=63)||(LA95_0>=65 && LA95_0<=66)||(LA95_0>=69 && LA95_0<=73)||(LA95_0>=105 && LA95_0<=106)||(LA95_0>=109 && LA95_0<=113)) ) {
                alt95=1;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:554:14: elementValue ( ',' elementValue )*
                    {
                    dbg.location(554,14);
                    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer2892);
                    elementValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(554,27);
                    // Java.g:554:27: ( ',' elementValue )*
                    try { dbg.enterSubRule(94);

                    loop94:
                    do {
                        int alt94=2;
                        try { dbg.enterDecision(94);

                        int LA94_0 = input.LA(1);

                        if ( (LA94_0==41) ) {
                            int LA94_1 = input.LA(2);

                            if ( (LA94_1==Identifier||(LA94_1>=FloatingPointLiteral && LA94_1<=DecimalLiteral)||LA94_1==44||LA94_1==47||(LA94_1>=56 && LA94_1<=63)||(LA94_1>=65 && LA94_1<=66)||(LA94_1>=69 && LA94_1<=73)||(LA94_1>=105 && LA94_1<=106)||(LA94_1>=109 && LA94_1<=113)) ) {
                                alt94=1;
                            }


                        }


                        } finally {dbg.exitDecision(94);}

                        switch (alt94) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:554:28: ',' elementValue
                    	    {
                    	    dbg.location(554,28);
                    	    match(input,41,FOLLOW_41_in_elementValueArrayInitializer2895); if (state.failed) return ;
                    	    dbg.location(554,32);
                    	    pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer2897);
                    	    elementValue();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop94;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(94);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}

            dbg.location(554,49);
            // Java.g:554:49: ( ',' )?
            int alt96=2;
            try { dbg.enterSubRule(96);
            try { dbg.enterDecision(96);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==41) ) {
                alt96=1;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:554:50: ','
                    {
                    dbg.location(554,50);
                    match(input,41,FOLLOW_41_in_elementValueArrayInitializer2904); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}

            dbg.location(554,56);
            match(input,45,FOLLOW_45_in_elementValueArrayInitializer2908); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 76, elementValueArrayInitializer_StartIndex); }
        }
        dbg.location(555, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementValueArrayInitializer");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementValueArrayInitializer"


    // $ANTLR start "annotationTypeDeclaration"
    // Java.g:557:1: annotationTypeDeclaration : '@' 'interface' Identifier annotationTypeBody ;
    public final void annotationTypeDeclaration() throws RecognitionException {
        int annotationTypeDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationTypeDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(557, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return ; }
            // Java.g:558:5: ( '@' 'interface' Identifier annotationTypeBody )
            dbg.enterAlt(1);

            // Java.g:558:9: '@' 'interface' Identifier annotationTypeBody
            {
            dbg.location(558,9);
            match(input,73,FOLLOW_73_in_annotationTypeDeclaration2931); if (state.failed) return ;
            dbg.location(558,13);
            match(input,46,FOLLOW_46_in_annotationTypeDeclaration2933); if (state.failed) return ;
            dbg.location(558,25);
            match(input,Identifier,FOLLOW_Identifier_in_annotationTypeDeclaration2935); if (state.failed) return ;
            dbg.location(558,36);
            pushFollow(FOLLOW_annotationTypeBody_in_annotationTypeDeclaration2937);
            annotationTypeBody();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 77, annotationTypeDeclaration_StartIndex); }
        }
        dbg.location(559, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationTypeDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationTypeDeclaration"


    // $ANTLR start "annotationTypeBody"
    // Java.g:561:1: annotationTypeBody : '{' ( annotationTypeElementDeclaration )* '}' ;
    public final void annotationTypeBody() throws RecognitionException {
        int annotationTypeBody_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationTypeBody");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(561, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return ; }
            // Java.g:562:5: ( '{' ( annotationTypeElementDeclaration )* '}' )
            dbg.enterAlt(1);

            // Java.g:562:9: '{' ( annotationTypeElementDeclaration )* '}'
            {
            dbg.location(562,9);
            match(input,44,FOLLOW_44_in_annotationTypeBody2960); if (state.failed) return ;
            dbg.location(562,13);
            // Java.g:562:13: ( annotationTypeElementDeclaration )*
            try { dbg.enterSubRule(97);

            loop97:
            do {
                int alt97=2;
                try { dbg.enterDecision(97);

                int LA97_0 = input.LA(1);

                if ( ((LA97_0>=Identifier && LA97_0<=ENUM)||LA97_0==28||(LA97_0>=31 && LA97_0<=37)||LA97_0==40||(LA97_0>=46 && LA97_0<=47)||(LA97_0>=52 && LA97_0<=63)||LA97_0==73) ) {
                    alt97=1;
                }


                } finally {dbg.exitDecision(97);}

                switch (alt97) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:562:14: annotationTypeElementDeclaration
            	    {
            	    dbg.location(562,14);
            	    pushFollow(FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody2963);
            	    annotationTypeElementDeclaration();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop97;
                }
            } while (true);
            } finally {dbg.exitSubRule(97);}

            dbg.location(562,49);
            match(input,45,FOLLOW_45_in_annotationTypeBody2967); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 78, annotationTypeBody_StartIndex); }
        }
        dbg.location(563, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationTypeBody");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationTypeBody"


    // $ANTLR start "annotationTypeElementDeclaration"
    // Java.g:565:1: annotationTypeElementDeclaration : modifiers annotationTypeElementRest ;
    public final void annotationTypeElementDeclaration() throws RecognitionException {
        int annotationTypeElementDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationTypeElementDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(565, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return ; }
            // Java.g:566:5: ( modifiers annotationTypeElementRest )
            dbg.enterAlt(1);

            // Java.g:566:9: modifiers annotationTypeElementRest
            {
            dbg.location(566,9);
            pushFollow(FOLLOW_modifiers_in_annotationTypeElementDeclaration2990);
            modifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(566,19);
            pushFollow(FOLLOW_annotationTypeElementRest_in_annotationTypeElementDeclaration2992);
            annotationTypeElementRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 79, annotationTypeElementDeclaration_StartIndex); }
        }
        dbg.location(567, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationTypeElementDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationTypeElementDeclaration"


    // $ANTLR start "annotationTypeElementRest"
    // Java.g:569:1: annotationTypeElementRest : ( type annotationMethodOrConstantRest ';' | normalClassDeclaration ( ';' )? | normalInterfaceDeclaration ( ';' )? | enumDeclaration ( ';' )? | annotationTypeDeclaration ( ';' )? );
    public final void annotationTypeElementRest() throws RecognitionException {
        int annotationTypeElementRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationTypeElementRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(569, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return ; }
            // Java.g:570:5: ( type annotationMethodOrConstantRest ';' | normalClassDeclaration ( ';' )? | normalInterfaceDeclaration ( ';' )? | enumDeclaration ( ';' )? | annotationTypeDeclaration ( ';' )? )
            int alt102=5;
            try { dbg.enterDecision(102);

            switch ( input.LA(1) ) {
            case Identifier:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt102=1;
                }
                break;
            case 37:
                {
                alt102=2;
                }
                break;
            case 46:
                {
                alt102=3;
                }
                break;
            case ENUM:
                {
                alt102=4;
                }
                break;
            case 73:
                {
                alt102=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(102);}

            switch (alt102) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:570:9: type annotationMethodOrConstantRest ';'
                    {
                    dbg.location(570,9);
                    pushFollow(FOLLOW_type_in_annotationTypeElementRest3015);
                    type();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(570,14);
                    pushFollow(FOLLOW_annotationMethodOrConstantRest_in_annotationTypeElementRest3017);
                    annotationMethodOrConstantRest();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(570,45);
                    match(input,26,FOLLOW_26_in_annotationTypeElementRest3019); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:571:9: normalClassDeclaration ( ';' )?
                    {
                    dbg.location(571,9);
                    pushFollow(FOLLOW_normalClassDeclaration_in_annotationTypeElementRest3029);
                    normalClassDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(571,32);
                    // Java.g:571:32: ( ';' )?
                    int alt98=2;
                    try { dbg.enterSubRule(98);
                    try { dbg.enterDecision(98);

                    int LA98_0 = input.LA(1);

                    if ( (LA98_0==26) ) {
                        alt98=1;
                    }
                    } finally {dbg.exitDecision(98);}

                    switch (alt98) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: ';'
                            {
                            dbg.location(571,32);
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3031); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(98);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:572:9: normalInterfaceDeclaration ( ';' )?
                    {
                    dbg.location(572,9);
                    pushFollow(FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementRest3042);
                    normalInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(572,36);
                    // Java.g:572:36: ( ';' )?
                    int alt99=2;
                    try { dbg.enterSubRule(99);
                    try { dbg.enterDecision(99);

                    int LA99_0 = input.LA(1);

                    if ( (LA99_0==26) ) {
                        alt99=1;
                    }
                    } finally {dbg.exitDecision(99);}

                    switch (alt99) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: ';'
                            {
                            dbg.location(572,36);
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3044); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(99);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:573:9: enumDeclaration ( ';' )?
                    {
                    dbg.location(573,9);
                    pushFollow(FOLLOW_enumDeclaration_in_annotationTypeElementRest3055);
                    enumDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(573,25);
                    // Java.g:573:25: ( ';' )?
                    int alt100=2;
                    try { dbg.enterSubRule(100);
                    try { dbg.enterDecision(100);

                    int LA100_0 = input.LA(1);

                    if ( (LA100_0==26) ) {
                        alt100=1;
                    }
                    } finally {dbg.exitDecision(100);}

                    switch (alt100) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: ';'
                            {
                            dbg.location(573,25);
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3057); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(100);}


                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:574:9: annotationTypeDeclaration ( ';' )?
                    {
                    dbg.location(574,9);
                    pushFollow(FOLLOW_annotationTypeDeclaration_in_annotationTypeElementRest3068);
                    annotationTypeDeclaration();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(574,35);
                    // Java.g:574:35: ( ';' )?
                    int alt101=2;
                    try { dbg.enterSubRule(101);
                    try { dbg.enterDecision(101);

                    int LA101_0 = input.LA(1);

                    if ( (LA101_0==26) ) {
                        alt101=1;
                    }
                    } finally {dbg.exitDecision(101);}

                    switch (alt101) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: ';'
                            {
                            dbg.location(574,35);
                            match(input,26,FOLLOW_26_in_annotationTypeElementRest3070); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(101);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 80, annotationTypeElementRest_StartIndex); }
        }
        dbg.location(575, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationTypeElementRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationTypeElementRest"


    // $ANTLR start "annotationMethodOrConstantRest"
    // Java.g:577:1: annotationMethodOrConstantRest : ( annotationMethodRest | annotationConstantRest );
    public final void annotationMethodOrConstantRest() throws RecognitionException {
        int annotationMethodOrConstantRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationMethodOrConstantRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(577, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return ; }
            // Java.g:578:5: ( annotationMethodRest | annotationConstantRest )
            int alt103=2;
            try { dbg.enterDecision(103);

            int LA103_0 = input.LA(1);

            if ( (LA103_0==Identifier) ) {
                int LA103_1 = input.LA(2);

                if ( (LA103_1==66) ) {
                    alt103=1;
                }
                else if ( (LA103_1==26||LA103_1==41||LA103_1==48||LA103_1==51) ) {
                    alt103=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 103, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(103);}

            switch (alt103) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:578:9: annotationMethodRest
                    {
                    dbg.location(578,9);
                    pushFollow(FOLLOW_annotationMethodRest_in_annotationMethodOrConstantRest3094);
                    annotationMethodRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:579:9: annotationConstantRest
                    {
                    dbg.location(579,9);
                    pushFollow(FOLLOW_annotationConstantRest_in_annotationMethodOrConstantRest3104);
                    annotationConstantRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 81, annotationMethodOrConstantRest_StartIndex); }
        }
        dbg.location(580, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationMethodOrConstantRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationMethodOrConstantRest"


    // $ANTLR start "annotationMethodRest"
    // Java.g:582:1: annotationMethodRest : Identifier '(' ')' ( defaultValue )? ;
    public final void annotationMethodRest() throws RecognitionException {
        int annotationMethodRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationMethodRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(582, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return ; }
            // Java.g:583:5: ( Identifier '(' ')' ( defaultValue )? )
            dbg.enterAlt(1);

            // Java.g:583:9: Identifier '(' ')' ( defaultValue )?
            {
            dbg.location(583,9);
            match(input,Identifier,FOLLOW_Identifier_in_annotationMethodRest3127); if (state.failed) return ;
            dbg.location(583,20);
            match(input,66,FOLLOW_66_in_annotationMethodRest3129); if (state.failed) return ;
            dbg.location(583,24);
            match(input,67,FOLLOW_67_in_annotationMethodRest3131); if (state.failed) return ;
            dbg.location(583,28);
            // Java.g:583:28: ( defaultValue )?
            int alt104=2;
            try { dbg.enterSubRule(104);
            try { dbg.enterDecision(104);

            int LA104_0 = input.LA(1);

            if ( (LA104_0==74) ) {
                alt104=1;
            }
            } finally {dbg.exitDecision(104);}

            switch (alt104) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: defaultValue
                    {
                    dbg.location(583,28);
                    pushFollow(FOLLOW_defaultValue_in_annotationMethodRest3133);
                    defaultValue();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(104);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 82, annotationMethodRest_StartIndex); }
        }
        dbg.location(584, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationMethodRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationMethodRest"


    // $ANTLR start "annotationConstantRest"
    // Java.g:586:1: annotationConstantRest : variableDeclarators ;
    public final void annotationConstantRest() throws RecognitionException {
        int annotationConstantRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "annotationConstantRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(586, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return ; }
            // Java.g:587:5: ( variableDeclarators )
            dbg.enterAlt(1);

            // Java.g:587:9: variableDeclarators
            {
            dbg.location(587,9);
            pushFollow(FOLLOW_variableDeclarators_in_annotationConstantRest3157);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 83, annotationConstantRest_StartIndex); }
        }
        dbg.location(588, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "annotationConstantRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "annotationConstantRest"


    // $ANTLR start "defaultValue"
    // Java.g:590:1: defaultValue : 'default' elementValue ;
    public final void defaultValue() throws RecognitionException {
        int defaultValue_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "defaultValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(590, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return ; }
            // Java.g:591:5: ( 'default' elementValue )
            dbg.enterAlt(1);

            // Java.g:591:9: 'default' elementValue
            {
            dbg.location(591,9);
            match(input,74,FOLLOW_74_in_defaultValue3180); if (state.failed) return ;
            dbg.location(591,19);
            pushFollow(FOLLOW_elementValue_in_defaultValue3182);
            elementValue();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 84, defaultValue_StartIndex); }
        }
        dbg.location(592, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "defaultValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "defaultValue"


    // $ANTLR start "block"
    // Java.g:596:1: block : '{' ( blockStatement )* '}' ;
    public final void block() throws RecognitionException {
        int block_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "block");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(596, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return ; }
            // Java.g:597:5: ( '{' ( blockStatement )* '}' )
            dbg.enterAlt(1);

            // Java.g:597:9: '{' ( blockStatement )* '}'
            {
            dbg.location(597,9);
            match(input,44,FOLLOW_44_in_block3203); if (state.failed) return ;
            dbg.location(597,13);
            // Java.g:597:13: ( blockStatement )*
            try { dbg.enterSubRule(105);

            loop105:
            do {
                int alt105=2;
                try { dbg.enterDecision(105);

                int LA105_0 = input.LA(1);

                if ( ((LA105_0>=Identifier && LA105_0<=ASSERT)||LA105_0==26||LA105_0==28||(LA105_0>=31 && LA105_0<=37)||LA105_0==44||(LA105_0>=46 && LA105_0<=47)||LA105_0==53||(LA105_0>=56 && LA105_0<=63)||(LA105_0>=65 && LA105_0<=66)||(LA105_0>=69 && LA105_0<=73)||LA105_0==76||(LA105_0>=78 && LA105_0<=81)||(LA105_0>=83 && LA105_0<=87)||(LA105_0>=105 && LA105_0<=106)||(LA105_0>=109 && LA105_0<=113)) ) {
                    alt105=1;
                }


                } finally {dbg.exitDecision(105);}

                switch (alt105) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: blockStatement
            	    {
            	    dbg.location(597,13);
            	    pushFollow(FOLLOW_blockStatement_in_block3205);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop105;
                }
            } while (true);
            } finally {dbg.exitSubRule(105);}

            dbg.location(597,29);
            match(input,45,FOLLOW_45_in_block3208); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 85, block_StartIndex); }
        }
        dbg.location(598, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "block");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "block"


    // $ANTLR start "blockStatement"
    // Java.g:600:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );
    public final void blockStatement() throws RecognitionException {
        int blockStatement_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "blockStatement");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return ; }
            // Java.g:601:5: ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement )
            int alt106=3;
            try { dbg.enterDecision(106);

            try {
                isCyclicDecision = true;
                alt106 = dfa106.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(106);}

            switch (alt106) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:601:9: localVariableDeclarationStatement
                    {
                    dbg.location(601,9);
                    pushFollow(FOLLOW_localVariableDeclarationStatement_in_blockStatement3231);
                    localVariableDeclarationStatement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:602:9: classOrInterfaceDeclaration
                    {
                    dbg.location(602,9);
                    pushFollow(FOLLOW_classOrInterfaceDeclaration_in_blockStatement3241);
                    classOrInterfaceDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:603:9: statement
                    {
                    dbg.location(603,9);
                    pushFollow(FOLLOW_statement_in_blockStatement3251);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 86, blockStatement_StartIndex); }
        }
        dbg.location(604, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "blockStatement");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "blockStatement"


    // $ANTLR start "localVariableDeclarationStatement"
    // Java.g:606:1: localVariableDeclarationStatement : localVariableDeclaration ';' ;
    public final void localVariableDeclarationStatement() throws RecognitionException {
        int localVariableDeclarationStatement_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "localVariableDeclarationStatement");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(606, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return ; }
            // Java.g:607:5: ( localVariableDeclaration ';' )
            dbg.enterAlt(1);

            // Java.g:607:10: localVariableDeclaration ';'
            {
            dbg.location(607,10);
            pushFollow(FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement3275);
            localVariableDeclaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(607,35);
            match(input,26,FOLLOW_26_in_localVariableDeclarationStatement3277); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 87, localVariableDeclarationStatement_StartIndex); }
        }
        dbg.location(608, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "localVariableDeclarationStatement");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "localVariableDeclarationStatement"


    // $ANTLR start "localVariableDeclaration"
    // Java.g:610:1: localVariableDeclaration : variableModifiers type variableDeclarators ;
    public final void localVariableDeclaration() throws RecognitionException {
        int localVariableDeclaration_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "localVariableDeclaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(610, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return ; }
            // Java.g:611:5: ( variableModifiers type variableDeclarators )
            dbg.enterAlt(1);

            // Java.g:611:9: variableModifiers type variableDeclarators
            {
            dbg.location(611,9);
            pushFollow(FOLLOW_variableModifiers_in_localVariableDeclaration3296);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(611,27);
            pushFollow(FOLLOW_type_in_localVariableDeclaration3298);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(611,32);
            pushFollow(FOLLOW_variableDeclarators_in_localVariableDeclaration3300);
            variableDeclarators();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 88, localVariableDeclaration_StartIndex); }
        }
        dbg.location(612, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "localVariableDeclaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "localVariableDeclaration"


    // $ANTLR start "variableModifiers"
    // Java.g:614:1: variableModifiers : ( variableModifier )* ;
    public final void variableModifiers() throws RecognitionException {
        int variableModifiers_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "variableModifiers");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(614, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return ; }
            // Java.g:615:5: ( ( variableModifier )* )
            dbg.enterAlt(1);

            // Java.g:615:9: ( variableModifier )*
            {
            dbg.location(615,9);
            // Java.g:615:9: ( variableModifier )*
            try { dbg.enterSubRule(107);

            loop107:
            do {
                int alt107=2;
                try { dbg.enterDecision(107);

                int LA107_0 = input.LA(1);

                if ( (LA107_0==35||LA107_0==73) ) {
                    alt107=1;
                }


                } finally {dbg.exitDecision(107);}

                switch (alt107) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: variableModifier
            	    {
            	    dbg.location(615,9);
            	    pushFollow(FOLLOW_variableModifier_in_variableModifiers3323);
            	    variableModifier();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop107;
                }
            } while (true);
            } finally {dbg.exitSubRule(107);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 89, variableModifiers_StartIndex); }
        }
        dbg.location(616, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "variableModifiers");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "variableModifiers"


    // $ANTLR start "statement"
    // Java.g:618:1: statement : ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );
    public final void statement() throws RecognitionException {
        int statement_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "statement");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(618, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return ; }
            // Java.g:619:5: ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement )
            int alt114=16;
            try { dbg.enterDecision(114);

            try {
                isCyclicDecision = true;
                alt114 = dfa114.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(114);}

            switch (alt114) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:619:7: block
                    {
                    dbg.location(619,7);
                    pushFollow(FOLLOW_block_in_statement3341);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:620:9: ASSERT expression ( ':' expression )? ';'
                    {
                    dbg.location(620,9);
                    match(input,ASSERT,FOLLOW_ASSERT_in_statement3351); if (state.failed) return ;
                    dbg.location(620,16);
                    pushFollow(FOLLOW_expression_in_statement3353);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(620,27);
                    // Java.g:620:27: ( ':' expression )?
                    int alt108=2;
                    try { dbg.enterSubRule(108);
                    try { dbg.enterDecision(108);

                    int LA108_0 = input.LA(1);

                    if ( (LA108_0==75) ) {
                        alt108=1;
                    }
                    } finally {dbg.exitDecision(108);}

                    switch (alt108) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:620:28: ':' expression
                            {
                            dbg.location(620,28);
                            match(input,75,FOLLOW_75_in_statement3356); if (state.failed) return ;
                            dbg.location(620,32);
                            pushFollow(FOLLOW_expression_in_statement3358);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(108);}

                    dbg.location(620,45);
                    match(input,26,FOLLOW_26_in_statement3362); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:621:9: 'if' parExpression statement ( options {k=1; } : 'else' statement )?
                    {
                    dbg.location(621,9);
                    match(input,76,FOLLOW_76_in_statement3372); if (state.failed) return ;
                    dbg.location(621,14);
                    pushFollow(FOLLOW_parExpression_in_statement3374);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(621,28);
                    pushFollow(FOLLOW_statement_in_statement3376);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(621,38);
                    // Java.g:621:38: ( options {k=1; } : 'else' statement )?
                    int alt109=2;
                    try { dbg.enterSubRule(109);
                    try { dbg.enterDecision(109);

                    int LA109_0 = input.LA(1);

                    if ( (LA109_0==77) ) {
                        int LA109_2 = input.LA(2);

                        if ( (synpred157_Java()) ) {
                            alt109=1;
                        }
                    }
                    } finally {dbg.exitDecision(109);}

                    switch (alt109) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:621:54: 'else' statement
                            {
                            dbg.location(621,54);
                            match(input,77,FOLLOW_77_in_statement3386); if (state.failed) return ;
                            dbg.location(621,61);
                            pushFollow(FOLLOW_statement_in_statement3388);
                            statement();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(109);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:622:9: 'for' '(' forControl ')' statement
                    {
                    dbg.location(622,9);
                    match(input,78,FOLLOW_78_in_statement3400); if (state.failed) return ;
                    dbg.location(622,15);
                    match(input,66,FOLLOW_66_in_statement3402); if (state.failed) return ;
                    dbg.location(622,19);
                    pushFollow(FOLLOW_forControl_in_statement3404);
                    forControl();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(622,30);
                    match(input,67,FOLLOW_67_in_statement3406); if (state.failed) return ;
                    dbg.location(622,34);
                    pushFollow(FOLLOW_statement_in_statement3408);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:623:9: 'while' parExpression statement
                    {
                    dbg.location(623,9);
                    match(input,79,FOLLOW_79_in_statement3418); if (state.failed) return ;
                    dbg.location(623,17);
                    pushFollow(FOLLOW_parExpression_in_statement3420);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(623,31);
                    pushFollow(FOLLOW_statement_in_statement3422);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:624:9: 'do' statement 'while' parExpression ';'
                    {
                    dbg.location(624,9);
                    match(input,80,FOLLOW_80_in_statement3432); if (state.failed) return ;
                    dbg.location(624,14);
                    pushFollow(FOLLOW_statement_in_statement3434);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(624,24);
                    match(input,79,FOLLOW_79_in_statement3436); if (state.failed) return ;
                    dbg.location(624,32);
                    pushFollow(FOLLOW_parExpression_in_statement3438);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(624,46);
                    match(input,26,FOLLOW_26_in_statement3440); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:625:9: 'try' block ( catches 'finally' block | catches | 'finally' block )
                    {
                    dbg.location(625,9);
                    match(input,81,FOLLOW_81_in_statement3450); if (state.failed) return ;
                    dbg.location(625,15);
                    pushFollow(FOLLOW_block_in_statement3452);
                    block();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(626,9);
                    // Java.g:626:9: ( catches 'finally' block | catches | 'finally' block )
                    int alt110=3;
                    try { dbg.enterSubRule(110);
                    try { dbg.enterDecision(110);

                    int LA110_0 = input.LA(1);

                    if ( (LA110_0==88) ) {
                        int LA110_1 = input.LA(2);

                        if ( (synpred162_Java()) ) {
                            alt110=1;
                        }
                        else if ( (synpred163_Java()) ) {
                            alt110=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 110, 1, input);

                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                    }
                    else if ( (LA110_0==82) ) {
                        alt110=3;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 110, 0, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(110);}

                    switch (alt110) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:626:11: catches 'finally' block
                            {
                            dbg.location(626,11);
                            pushFollow(FOLLOW_catches_in_statement3464);
                            catches();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(626,19);
                            match(input,82,FOLLOW_82_in_statement3466); if (state.failed) return ;
                            dbg.location(626,29);
                            pushFollow(FOLLOW_block_in_statement3468);
                            block();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:627:11: catches
                            {
                            dbg.location(627,11);
                            pushFollow(FOLLOW_catches_in_statement3480);
                            catches();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            dbg.enterAlt(3);

                            // Java.g:628:13: 'finally' block
                            {
                            dbg.location(628,13);
                            match(input,82,FOLLOW_82_in_statement3494); if (state.failed) return ;
                            dbg.location(628,23);
                            pushFollow(FOLLOW_block_in_statement3496);
                            block();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(110);}


                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:630:9: 'switch' parExpression '{' switchBlockStatementGroups '}'
                    {
                    dbg.location(630,9);
                    match(input,83,FOLLOW_83_in_statement3516); if (state.failed) return ;
                    dbg.location(630,18);
                    pushFollow(FOLLOW_parExpression_in_statement3518);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(630,32);
                    match(input,44,FOLLOW_44_in_statement3520); if (state.failed) return ;
                    dbg.location(630,36);
                    pushFollow(FOLLOW_switchBlockStatementGroups_in_statement3522);
                    switchBlockStatementGroups();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(630,63);
                    match(input,45,FOLLOW_45_in_statement3524); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // Java.g:631:9: 'synchronized' parExpression block
                    {
                    dbg.location(631,9);
                    match(input,53,FOLLOW_53_in_statement3534); if (state.failed) return ;
                    dbg.location(631,24);
                    pushFollow(FOLLOW_parExpression_in_statement3536);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(631,38);
                    pushFollow(FOLLOW_block_in_statement3538);
                    block();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 10 :
                    dbg.enterAlt(10);

                    // Java.g:632:9: 'return' ( expression )? ';'
                    {
                    dbg.location(632,9);
                    match(input,84,FOLLOW_84_in_statement3548); if (state.failed) return ;
                    dbg.location(632,18);
                    // Java.g:632:18: ( expression )?
                    int alt111=2;
                    try { dbg.enterSubRule(111);
                    try { dbg.enterDecision(111);

                    int LA111_0 = input.LA(1);

                    if ( (LA111_0==Identifier||(LA111_0>=FloatingPointLiteral && LA111_0<=DecimalLiteral)||LA111_0==47||(LA111_0>=56 && LA111_0<=63)||(LA111_0>=65 && LA111_0<=66)||(LA111_0>=69 && LA111_0<=72)||(LA111_0>=105 && LA111_0<=106)||(LA111_0>=109 && LA111_0<=113)) ) {
                        alt111=1;
                    }
                    } finally {dbg.exitDecision(111);}

                    switch (alt111) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: expression
                            {
                            dbg.location(632,18);
                            pushFollow(FOLLOW_expression_in_statement3550);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(111);}

                    dbg.location(632,30);
                    match(input,26,FOLLOW_26_in_statement3553); if (state.failed) return ;

                    }
                    break;
                case 11 :
                    dbg.enterAlt(11);

                    // Java.g:633:9: 'throw' expression ';'
                    {
                    dbg.location(633,9);
                    match(input,85,FOLLOW_85_in_statement3563); if (state.failed) return ;
                    dbg.location(633,17);
                    pushFollow(FOLLOW_expression_in_statement3565);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(633,28);
                    match(input,26,FOLLOW_26_in_statement3567); if (state.failed) return ;

                    }
                    break;
                case 12 :
                    dbg.enterAlt(12);

                    // Java.g:634:9: 'break' ( Identifier )? ';'
                    {
                    dbg.location(634,9);
                    match(input,86,FOLLOW_86_in_statement3577); if (state.failed) return ;
                    dbg.location(634,17);
                    // Java.g:634:17: ( Identifier )?
                    int alt112=2;
                    try { dbg.enterSubRule(112);
                    try { dbg.enterDecision(112);

                    int LA112_0 = input.LA(1);

                    if ( (LA112_0==Identifier) ) {
                        alt112=1;
                    }
                    } finally {dbg.exitDecision(112);}

                    switch (alt112) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: Identifier
                            {
                            dbg.location(634,17);
                            match(input,Identifier,FOLLOW_Identifier_in_statement3579); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}

                    dbg.location(634,29);
                    match(input,26,FOLLOW_26_in_statement3582); if (state.failed) return ;

                    }
                    break;
                case 13 :
                    dbg.enterAlt(13);

                    // Java.g:635:9: 'continue' ( Identifier )? ';'
                    {
                    dbg.location(635,9);
                    match(input,87,FOLLOW_87_in_statement3592); if (state.failed) return ;
                    dbg.location(635,20);
                    // Java.g:635:20: ( Identifier )?
                    int alt113=2;
                    try { dbg.enterSubRule(113);
                    try { dbg.enterDecision(113);

                    int LA113_0 = input.LA(1);

                    if ( (LA113_0==Identifier) ) {
                        alt113=1;
                    }
                    } finally {dbg.exitDecision(113);}

                    switch (alt113) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: Identifier
                            {
                            dbg.location(635,20);
                            match(input,Identifier,FOLLOW_Identifier_in_statement3594); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(113);}

                    dbg.location(635,32);
                    match(input,26,FOLLOW_26_in_statement3597); if (state.failed) return ;

                    }
                    break;
                case 14 :
                    dbg.enterAlt(14);

                    // Java.g:636:9: ';'
                    {
                    dbg.location(636,9);
                    match(input,26,FOLLOW_26_in_statement3607); if (state.failed) return ;

                    }
                    break;
                case 15 :
                    dbg.enterAlt(15);

                    // Java.g:637:9: statementExpression ';'
                    {
                    dbg.location(637,9);
                    pushFollow(FOLLOW_statementExpression_in_statement3618);
                    statementExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(637,29);
                    match(input,26,FOLLOW_26_in_statement3620); if (state.failed) return ;

                    }
                    break;
                case 16 :
                    dbg.enterAlt(16);

                    // Java.g:638:9: Identifier ':' statement
                    {
                    dbg.location(638,9);
                    match(input,Identifier,FOLLOW_Identifier_in_statement3630); if (state.failed) return ;
                    dbg.location(638,20);
                    match(input,75,FOLLOW_75_in_statement3632); if (state.failed) return ;
                    dbg.location(638,24);
                    pushFollow(FOLLOW_statement_in_statement3634);
                    statement();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 90, statement_StartIndex); }
        }
        dbg.location(639, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "statement");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "statement"


    // $ANTLR start "catches"
    // Java.g:641:1: catches : catchClause ( catchClause )* ;
    public final void catches() throws RecognitionException {
        int catches_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "catches");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(641, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return ; }
            // Java.g:642:5: ( catchClause ( catchClause )* )
            dbg.enterAlt(1);

            // Java.g:642:9: catchClause ( catchClause )*
            {
            dbg.location(642,9);
            pushFollow(FOLLOW_catchClause_in_catches3657);
            catchClause();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(642,21);
            // Java.g:642:21: ( catchClause )*
            try { dbg.enterSubRule(115);

            loop115:
            do {
                int alt115=2;
                try { dbg.enterDecision(115);

                int LA115_0 = input.LA(1);

                if ( (LA115_0==88) ) {
                    alt115=1;
                }


                } finally {dbg.exitDecision(115);}

                switch (alt115) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:642:22: catchClause
            	    {
            	    dbg.location(642,22);
            	    pushFollow(FOLLOW_catchClause_in_catches3660);
            	    catchClause();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop115;
                }
            } while (true);
            } finally {dbg.exitSubRule(115);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 91, catches_StartIndex); }
        }
        dbg.location(643, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "catches");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "catches"


    // $ANTLR start "catchClause"
    // Java.g:645:1: catchClause : 'catch' '(' formalParameter ')' block ;
    public final void catchClause() throws RecognitionException {
        int catchClause_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "catchClause");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(645, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return ; }
            // Java.g:646:5: ( 'catch' '(' formalParameter ')' block )
            dbg.enterAlt(1);

            // Java.g:646:9: 'catch' '(' formalParameter ')' block
            {
            dbg.location(646,9);
            match(input,88,FOLLOW_88_in_catchClause3685); if (state.failed) return ;
            dbg.location(646,17);
            match(input,66,FOLLOW_66_in_catchClause3687); if (state.failed) return ;
            dbg.location(646,21);
            pushFollow(FOLLOW_formalParameter_in_catchClause3689);
            formalParameter();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(646,37);
            match(input,67,FOLLOW_67_in_catchClause3691); if (state.failed) return ;
            dbg.location(646,41);
            pushFollow(FOLLOW_block_in_catchClause3693);
            block();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 92, catchClause_StartIndex); }
        }
        dbg.location(647, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "catchClause");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "catchClause"


    // $ANTLR start "formalParameter"
    // Java.g:649:1: formalParameter : variableModifiers type variableDeclaratorId ;
    public final void formalParameter() throws RecognitionException {
        int formalParameter_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "formalParameter");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(649, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 93) ) { return ; }
            // Java.g:650:5: ( variableModifiers type variableDeclaratorId )
            dbg.enterAlt(1);

            // Java.g:650:9: variableModifiers type variableDeclaratorId
            {
            dbg.location(650,9);
            pushFollow(FOLLOW_variableModifiers_in_formalParameter3712);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(650,27);
            pushFollow(FOLLOW_type_in_formalParameter3714);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(650,32);
            pushFollow(FOLLOW_variableDeclaratorId_in_formalParameter3716);
            variableDeclaratorId();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 93, formalParameter_StartIndex); }
        }
        dbg.location(651, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "formalParameter");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "formalParameter"


    // $ANTLR start "switchBlockStatementGroups"
    // Java.g:653:1: switchBlockStatementGroups : ( switchBlockStatementGroup )* ;
    public final void switchBlockStatementGroups() throws RecognitionException {
        int switchBlockStatementGroups_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "switchBlockStatementGroups");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(653, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 94) ) { return ; }
            // Java.g:654:5: ( ( switchBlockStatementGroup )* )
            dbg.enterAlt(1);

            // Java.g:654:9: ( switchBlockStatementGroup )*
            {
            dbg.location(654,9);
            // Java.g:654:9: ( switchBlockStatementGroup )*
            try { dbg.enterSubRule(116);

            loop116:
            do {
                int alt116=2;
                try { dbg.enterDecision(116);

                int LA116_0 = input.LA(1);

                if ( (LA116_0==74||LA116_0==89) ) {
                    alt116=1;
                }


                } finally {dbg.exitDecision(116);}

                switch (alt116) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:654:10: switchBlockStatementGroup
            	    {
            	    dbg.location(654,10);
            	    pushFollow(FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups3744);
            	    switchBlockStatementGroup();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop116;
                }
            } while (true);
            } finally {dbg.exitSubRule(116);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 94, switchBlockStatementGroups_StartIndex); }
        }
        dbg.location(655, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "switchBlockStatementGroups");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "switchBlockStatementGroups"


    // $ANTLR start "switchBlockStatementGroup"
    // Java.g:661:1: switchBlockStatementGroup : ( switchLabel )+ ( blockStatement )* ;
    public final void switchBlockStatementGroup() throws RecognitionException {
        int switchBlockStatementGroup_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "switchBlockStatementGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(661, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 95) ) { return ; }
            // Java.g:662:5: ( ( switchLabel )+ ( blockStatement )* )
            dbg.enterAlt(1);

            // Java.g:662:9: ( switchLabel )+ ( blockStatement )*
            {
            dbg.location(662,9);
            // Java.g:662:9: ( switchLabel )+
            int cnt117=0;
            try { dbg.enterSubRule(117);

            loop117:
            do {
                int alt117=2;
                try { dbg.enterDecision(117);

                int LA117_0 = input.LA(1);

                if ( (LA117_0==89) ) {
                    int LA117_2 = input.LA(2);

                    if ( (synpred178_Java()) ) {
                        alt117=1;
                    }


                }
                else if ( (LA117_0==74) ) {
                    int LA117_3 = input.LA(2);

                    if ( (synpred178_Java()) ) {
                        alt117=1;
                    }


                }


                } finally {dbg.exitDecision(117);}

                switch (alt117) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: switchLabel
            	    {
            	    dbg.location(662,9);
            	    pushFollow(FOLLOW_switchLabel_in_switchBlockStatementGroup3771);
            	    switchLabel();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt117 >= 1 ) break loop117;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(117, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt117++;
            } while (true);
            } finally {dbg.exitSubRule(117);}

            dbg.location(662,22);
            // Java.g:662:22: ( blockStatement )*
            try { dbg.enterSubRule(118);

            loop118:
            do {
                int alt118=2;
                try { dbg.enterDecision(118);

                int LA118_0 = input.LA(1);

                if ( ((LA118_0>=Identifier && LA118_0<=ASSERT)||LA118_0==26||LA118_0==28||(LA118_0>=31 && LA118_0<=37)||LA118_0==44||(LA118_0>=46 && LA118_0<=47)||LA118_0==53||(LA118_0>=56 && LA118_0<=63)||(LA118_0>=65 && LA118_0<=66)||(LA118_0>=69 && LA118_0<=73)||LA118_0==76||(LA118_0>=78 && LA118_0<=81)||(LA118_0>=83 && LA118_0<=87)||(LA118_0>=105 && LA118_0<=106)||(LA118_0>=109 && LA118_0<=113)) ) {
                    alt118=1;
                }


                } finally {dbg.exitDecision(118);}

                switch (alt118) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:0:0: blockStatement
            	    {
            	    dbg.location(662,22);
            	    pushFollow(FOLLOW_blockStatement_in_switchBlockStatementGroup3774);
            	    blockStatement();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop118;
                }
            } while (true);
            } finally {dbg.exitSubRule(118);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 95, switchBlockStatementGroup_StartIndex); }
        }
        dbg.location(663, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "switchBlockStatementGroup");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "switchBlockStatementGroup"


    // $ANTLR start "switchLabel"
    // Java.g:665:1: switchLabel : ( 'case' constantExpression ':' | 'case' enumConstantName ':' | 'default' ':' );
    public final void switchLabel() throws RecognitionException {
        int switchLabel_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "switchLabel");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(665, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 96) ) { return ; }
            // Java.g:666:5: ( 'case' constantExpression ':' | 'case' enumConstantName ':' | 'default' ':' )
            int alt119=3;
            try { dbg.enterDecision(119);

            int LA119_0 = input.LA(1);

            if ( (LA119_0==89) ) {
                int LA119_1 = input.LA(2);

                if ( (LA119_1==Identifier) ) {
                    int LA119_3 = input.LA(3);

                    if ( (LA119_3==75) ) {
                        int LA119_5 = input.LA(4);

                        if ( (synpred180_Java()) ) {
                            alt119=1;
                        }
                        else if ( (synpred181_Java()) ) {
                            alt119=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 119, 5, input);

                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                    }
                    else if ( ((LA119_3>=29 && LA119_3<=30)||LA119_3==40||(LA119_3>=42 && LA119_3<=43)||LA119_3==48||LA119_3==51||LA119_3==64||LA119_3==66||(LA119_3>=90 && LA119_3<=110)) ) {
                        alt119=1;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 119, 3, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                }
                else if ( ((LA119_1>=FloatingPointLiteral && LA119_1<=DecimalLiteral)||LA119_1==47||(LA119_1>=56 && LA119_1<=63)||(LA119_1>=65 && LA119_1<=66)||(LA119_1>=69 && LA119_1<=72)||(LA119_1>=105 && LA119_1<=106)||(LA119_1>=109 && LA119_1<=113)) ) {
                    alt119=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 119, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA119_0==74) ) {
                alt119=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:666:9: 'case' constantExpression ':'
                    {
                    dbg.location(666,9);
                    match(input,89,FOLLOW_89_in_switchLabel3798); if (state.failed) return ;
                    dbg.location(666,16);
                    pushFollow(FOLLOW_constantExpression_in_switchLabel3800);
                    constantExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(666,35);
                    match(input,75,FOLLOW_75_in_switchLabel3802); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:667:9: 'case' enumConstantName ':'
                    {
                    dbg.location(667,9);
                    match(input,89,FOLLOW_89_in_switchLabel3812); if (state.failed) return ;
                    dbg.location(667,16);
                    pushFollow(FOLLOW_enumConstantName_in_switchLabel3814);
                    enumConstantName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(667,33);
                    match(input,75,FOLLOW_75_in_switchLabel3816); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:668:9: 'default' ':'
                    {
                    dbg.location(668,9);
                    match(input,74,FOLLOW_74_in_switchLabel3826); if (state.failed) return ;
                    dbg.location(668,19);
                    match(input,75,FOLLOW_75_in_switchLabel3828); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 96, switchLabel_StartIndex); }
        }
        dbg.location(669, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "switchLabel");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "switchLabel"


    // $ANTLR start "forControl"
    // Java.g:671:1: forControl options {k=3; } : ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? );
    public final void forControl() throws RecognitionException {
        int forControl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "forControl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(671, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 97) ) { return ; }
            // Java.g:673:5: ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? )
            int alt123=2;
            try { dbg.enterDecision(123);

            try {
                isCyclicDecision = true;
                alt123 = dfa123.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:673:9: enhancedForControl
                    {
                    dbg.location(673,9);
                    pushFollow(FOLLOW_enhancedForControl_in_forControl3859);
                    enhancedForControl();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:674:9: ( forInit )? ';' ( expression )? ';' ( forUpdate )?
                    {
                    dbg.location(674,9);
                    // Java.g:674:9: ( forInit )?
                    int alt120=2;
                    try { dbg.enterSubRule(120);
                    try { dbg.enterDecision(120);

                    int LA120_0 = input.LA(1);

                    if ( (LA120_0==Identifier||(LA120_0>=FloatingPointLiteral && LA120_0<=DecimalLiteral)||LA120_0==35||LA120_0==47||(LA120_0>=56 && LA120_0<=63)||(LA120_0>=65 && LA120_0<=66)||(LA120_0>=69 && LA120_0<=73)||(LA120_0>=105 && LA120_0<=106)||(LA120_0>=109 && LA120_0<=113)) ) {
                        alt120=1;
                    }
                    } finally {dbg.exitDecision(120);}

                    switch (alt120) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: forInit
                            {
                            dbg.location(674,9);
                            pushFollow(FOLLOW_forInit_in_forControl3869);
                            forInit();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}

                    dbg.location(674,18);
                    match(input,26,FOLLOW_26_in_forControl3872); if (state.failed) return ;
                    dbg.location(674,22);
                    // Java.g:674:22: ( expression )?
                    int alt121=2;
                    try { dbg.enterSubRule(121);
                    try { dbg.enterDecision(121);

                    int LA121_0 = input.LA(1);

                    if ( (LA121_0==Identifier||(LA121_0>=FloatingPointLiteral && LA121_0<=DecimalLiteral)||LA121_0==47||(LA121_0>=56 && LA121_0<=63)||(LA121_0>=65 && LA121_0<=66)||(LA121_0>=69 && LA121_0<=72)||(LA121_0>=105 && LA121_0<=106)||(LA121_0>=109 && LA121_0<=113)) ) {
                        alt121=1;
                    }
                    } finally {dbg.exitDecision(121);}

                    switch (alt121) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: expression
                            {
                            dbg.location(674,22);
                            pushFollow(FOLLOW_expression_in_forControl3874);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}

                    dbg.location(674,34);
                    match(input,26,FOLLOW_26_in_forControl3877); if (state.failed) return ;
                    dbg.location(674,38);
                    // Java.g:674:38: ( forUpdate )?
                    int alt122=2;
                    try { dbg.enterSubRule(122);
                    try { dbg.enterDecision(122);

                    int LA122_0 = input.LA(1);

                    if ( (LA122_0==Identifier||(LA122_0>=FloatingPointLiteral && LA122_0<=DecimalLiteral)||LA122_0==47||(LA122_0>=56 && LA122_0<=63)||(LA122_0>=65 && LA122_0<=66)||(LA122_0>=69 && LA122_0<=72)||(LA122_0>=105 && LA122_0<=106)||(LA122_0>=109 && LA122_0<=113)) ) {
                        alt122=1;
                    }
                    } finally {dbg.exitDecision(122);}

                    switch (alt122) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: forUpdate
                            {
                            dbg.location(674,38);
                            pushFollow(FOLLOW_forUpdate_in_forControl3879);
                            forUpdate();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(122);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 97, forControl_StartIndex); }
        }
        dbg.location(675, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "forControl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "forControl"


    // $ANTLR start "forInit"
    // Java.g:677:1: forInit : ( localVariableDeclaration | expressionList );
    public final void forInit() throws RecognitionException {
        int forInit_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "forInit");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(677, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 98) ) { return ; }
            // Java.g:678:5: ( localVariableDeclaration | expressionList )
            int alt124=2;
            try { dbg.enterDecision(124);

            try {
                isCyclicDecision = true;
                alt124 = dfa124.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(124);}

            switch (alt124) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:678:9: localVariableDeclaration
                    {
                    dbg.location(678,9);
                    pushFollow(FOLLOW_localVariableDeclaration_in_forInit3899);
                    localVariableDeclaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:679:9: expressionList
                    {
                    dbg.location(679,9);
                    pushFollow(FOLLOW_expressionList_in_forInit3909);
                    expressionList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 98, forInit_StartIndex); }
        }
        dbg.location(680, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "forInit");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "forInit"


    // $ANTLR start "enhancedForControl"
    // Java.g:682:1: enhancedForControl : variableModifiers type Identifier ':' expression ;
    public final void enhancedForControl() throws RecognitionException {
        int enhancedForControl_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "enhancedForControl");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(682, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 99) ) { return ; }
            // Java.g:683:5: ( variableModifiers type Identifier ':' expression )
            dbg.enterAlt(1);

            // Java.g:683:9: variableModifiers type Identifier ':' expression
            {
            dbg.location(683,9);
            pushFollow(FOLLOW_variableModifiers_in_enhancedForControl3932);
            variableModifiers();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(683,27);
            pushFollow(FOLLOW_type_in_enhancedForControl3934);
            type();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(683,32);
            match(input,Identifier,FOLLOW_Identifier_in_enhancedForControl3936); if (state.failed) return ;
            dbg.location(683,43);
            match(input,75,FOLLOW_75_in_enhancedForControl3938); if (state.failed) return ;
            dbg.location(683,47);
            pushFollow(FOLLOW_expression_in_enhancedForControl3940);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 99, enhancedForControl_StartIndex); }
        }
        dbg.location(684, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "enhancedForControl");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "enhancedForControl"


    // $ANTLR start "forUpdate"
    // Java.g:686:1: forUpdate : expressionList ;
    public final void forUpdate() throws RecognitionException {
        int forUpdate_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "forUpdate");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(686, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 100) ) { return ; }
            // Java.g:687:5: ( expressionList )
            dbg.enterAlt(1);

            // Java.g:687:9: expressionList
            {
            dbg.location(687,9);
            pushFollow(FOLLOW_expressionList_in_forUpdate3959);
            expressionList();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 100, forUpdate_StartIndex); }
        }
        dbg.location(688, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "forUpdate");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "forUpdate"


    // $ANTLR start "parExpression"
    // Java.g:692:1: parExpression : '(' expression ')' ;
    public final void parExpression() throws RecognitionException {
        int parExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "parExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(692, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 101) ) { return ; }
            // Java.g:693:5: ( '(' expression ')' )
            dbg.enterAlt(1);

            // Java.g:693:9: '(' expression ')'
            {
            dbg.location(693,9);
            match(input,66,FOLLOW_66_in_parExpression3980); if (state.failed) return ;
            dbg.location(693,13);
            pushFollow(FOLLOW_expression_in_parExpression3982);
            expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(693,24);
            match(input,67,FOLLOW_67_in_parExpression3984); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 101, parExpression_StartIndex); }
        }
        dbg.location(694, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "parExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "parExpression"


    // $ANTLR start "expressionList"
    // Java.g:696:1: expressionList : expression ( ',' expression )* ;
    public final void expressionList() throws RecognitionException {
        int expressionList_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "expressionList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(696, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 102) ) { return ; }
            // Java.g:697:5: ( expression ( ',' expression )* )
            dbg.enterAlt(1);

            // Java.g:697:9: expression ( ',' expression )*
            {
            dbg.location(697,9);
            pushFollow(FOLLOW_expression_in_expressionList4007);
            expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(697,20);
            // Java.g:697:20: ( ',' expression )*
            try { dbg.enterSubRule(125);

            loop125:
            do {
                int alt125=2;
                try { dbg.enterDecision(125);

                int LA125_0 = input.LA(1);

                if ( (LA125_0==41) ) {
                    alt125=1;
                }


                } finally {dbg.exitDecision(125);}

                switch (alt125) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:697:21: ',' expression
            	    {
            	    dbg.location(697,21);
            	    match(input,41,FOLLOW_41_in_expressionList4010); if (state.failed) return ;
            	    dbg.location(697,25);
            	    pushFollow(FOLLOW_expression_in_expressionList4012);
            	    expression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop125;
                }
            } while (true);
            } finally {dbg.exitSubRule(125);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 102, expressionList_StartIndex); }
        }
        dbg.location(698, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expressionList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expressionList"


    // $ANTLR start "statementExpression"
    // Java.g:700:1: statementExpression : expression ;
    public final void statementExpression() throws RecognitionException {
        int statementExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "statementExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(700, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 103) ) { return ; }
            // Java.g:701:5: ( expression )
            dbg.enterAlt(1);

            // Java.g:701:9: expression
            {
            dbg.location(701,9);
            pushFollow(FOLLOW_expression_in_statementExpression4033);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 103, statementExpression_StartIndex); }
        }
        dbg.location(702, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "statementExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "statementExpression"


    // $ANTLR start "constantExpression"
    // Java.g:704:1: constantExpression : expression ;
    public final void constantExpression() throws RecognitionException {
        int constantExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "constantExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(704, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 104) ) { return ; }
            // Java.g:705:5: ( expression )
            dbg.enterAlt(1);

            // Java.g:705:9: expression
            {
            dbg.location(705,9);
            pushFollow(FOLLOW_expression_in_constantExpression4056);
            expression();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 104, constantExpression_StartIndex); }
        }
        dbg.location(706, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "constantExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "constantExpression"


    // $ANTLR start "expression"
    // Java.g:708:1: expression : conditionalExpression ( assignmentOperator expression )? ;
    public final void expression() throws RecognitionException {
        int expression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(708, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 105) ) { return ; }
            // Java.g:709:5: ( conditionalExpression ( assignmentOperator expression )? )
            dbg.enterAlt(1);

            // Java.g:709:9: conditionalExpression ( assignmentOperator expression )?
            {
            dbg.location(709,9);
            pushFollow(FOLLOW_conditionalExpression_in_expression4079);
            conditionalExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(709,31);
            // Java.g:709:31: ( assignmentOperator expression )?
            int alt126=2;
            try { dbg.enterSubRule(126);
            try { dbg.enterDecision(126);

            try {
                isCyclicDecision = true;
                alt126 = dfa126.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:709:32: assignmentOperator expression
                    {
                    dbg.location(709,32);
                    pushFollow(FOLLOW_assignmentOperator_in_expression4082);
                    assignmentOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(709,51);
                    pushFollow(FOLLOW_expression_in_expression4084);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 105, expression_StartIndex); }
        }
        dbg.location(710, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "assignmentOperator"
    // Java.g:712:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?);
    public final void assignmentOperator() throws RecognitionException {
        int assignmentOperator_StartIndex = input.index();
        Token t1=null;
        Token t2=null;
        Token t3=null;
        Token t4=null;

        try { dbg.enterRule(getGrammarFileName(), "assignmentOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(712, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 106) ) { return ; }
            // Java.g:713:5: ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?)
            int alt127=12;
            try { dbg.enterDecision(127);

            try {
                isCyclicDecision = true;
                alt127 = dfa127.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(127);}

            switch (alt127) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:713:9: '='
                    {
                    dbg.location(713,9);
                    match(input,51,FOLLOW_51_in_assignmentOperator4109); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:714:9: '+='
                    {
                    dbg.location(714,9);
                    match(input,90,FOLLOW_90_in_assignmentOperator4119); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:715:9: '-='
                    {
                    dbg.location(715,9);
                    match(input,91,FOLLOW_91_in_assignmentOperator4129); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:716:9: '*='
                    {
                    dbg.location(716,9);
                    match(input,92,FOLLOW_92_in_assignmentOperator4139); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:717:9: '/='
                    {
                    dbg.location(717,9);
                    match(input,93,FOLLOW_93_in_assignmentOperator4149); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:718:9: '&='
                    {
                    dbg.location(718,9);
                    match(input,94,FOLLOW_94_in_assignmentOperator4159); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:719:9: '|='
                    {
                    dbg.location(719,9);
                    match(input,95,FOLLOW_95_in_assignmentOperator4169); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:720:9: '^='
                    {
                    dbg.location(720,9);
                    match(input,96,FOLLOW_96_in_assignmentOperator4179); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    dbg.enterAlt(9);

                    // Java.g:721:9: '%='
                    {
                    dbg.location(721,9);
                    match(input,97,FOLLOW_97_in_assignmentOperator4189); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    dbg.enterAlt(10);

                    // Java.g:722:9: ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}?
                    {
                    dbg.location(722,27);
                    t1=(Token)match(input,40,FOLLOW_40_in_assignmentOperator4210); if (state.failed) return ;
                    dbg.location(722,34);
                    t2=(Token)match(input,40,FOLLOW_40_in_assignmentOperator4214); if (state.failed) return ;
                    dbg.location(722,41);
                    t3=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4218); if (state.failed) return ;
                    dbg.location(723,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() &&
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() && 
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() &&\n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() &&\n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 11 :
                    dbg.enterAlt(11);

                    // Java.g:727:9: ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}?
                    {
                    dbg.location(727,31);
                    t1=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4252); if (state.failed) return ;
                    dbg.location(727,38);
                    t2=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4256); if (state.failed) return ;
                    dbg.location(727,45);
                    t3=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4260); if (state.failed) return ;
                    dbg.location(727,52);
                    t4=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4264); if (state.failed) return ;
                    dbg.location(728,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() &&
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() &&
                              t3.getLine() == t4.getLine() && 
                              t3.getCharPositionInLine() + 1 == t4.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() &&\n          $t3.getLine() == $t4.getLine() && \n          $t3.getCharPositionInLine() + 1 == $t4.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() &&\n          $t3.getLine() == $t4.getLine() && \n          $t3.getCharPositionInLine() + 1 == $t4.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 12 :
                    dbg.enterAlt(12);

                    // Java.g:734:9: ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?
                    {
                    dbg.location(734,27);
                    t1=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4295); if (state.failed) return ;
                    dbg.location(734,34);
                    t2=(Token)match(input,42,FOLLOW_42_in_assignmentOperator4299); if (state.failed) return ;
                    dbg.location(734,41);
                    t3=(Token)match(input,51,FOLLOW_51_in_assignmentOperator4303); if (state.failed) return ;
                    dbg.location(735,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() && 
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "assignmentOperator", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() && \n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 106, assignmentOperator_StartIndex); }
        }
        dbg.location(739, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "assignmentOperator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "assignmentOperator"


    // $ANTLR start "conditionalExpression"
    // Java.g:741:1: conditionalExpression : conditionalOrExpression ( '?' expression ':' expression )? ;
    public final void conditionalExpression() throws RecognitionException {
        int conditionalExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "conditionalExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(741, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 107) ) { return ; }
            // Java.g:742:5: ( conditionalOrExpression ( '?' expression ':' expression )? )
            dbg.enterAlt(1);

            // Java.g:742:9: conditionalOrExpression ( '?' expression ':' expression )?
            {
            dbg.location(742,9);
            pushFollow(FOLLOW_conditionalOrExpression_in_conditionalExpression4332);
            conditionalOrExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(742,33);
            // Java.g:742:33: ( '?' expression ':' expression )?
            int alt128=2;
            try { dbg.enterSubRule(128);
            try { dbg.enterDecision(128);

            int LA128_0 = input.LA(1);

            if ( (LA128_0==64) ) {
                alt128=1;
            }
            } finally {dbg.exitDecision(128);}

            switch (alt128) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:742:35: '?' expression ':' expression
                    {
                    dbg.location(742,35);
                    match(input,64,FOLLOW_64_in_conditionalExpression4336); if (state.failed) return ;
                    dbg.location(742,39);
                    pushFollow(FOLLOW_expression_in_conditionalExpression4338);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(742,50);
                    match(input,75,FOLLOW_75_in_conditionalExpression4340); if (state.failed) return ;
                    dbg.location(742,54);
                    pushFollow(FOLLOW_expression_in_conditionalExpression4342);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(128);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 107, conditionalExpression_StartIndex); }
        }
        dbg.location(743, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "conditionalExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "conditionalExpression"


    // $ANTLR start "conditionalOrExpression"
    // Java.g:745:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
    public final void conditionalOrExpression() throws RecognitionException {
        int conditionalOrExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "conditionalOrExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(745, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 108) ) { return ; }
            // Java.g:746:5: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
            dbg.enterAlt(1);

            // Java.g:746:9: conditionalAndExpression ( '||' conditionalAndExpression )*
            {
            dbg.location(746,9);
            pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression4364);
            conditionalAndExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(746,34);
            // Java.g:746:34: ( '||' conditionalAndExpression )*
            try { dbg.enterSubRule(129);

            loop129:
            do {
                int alt129=2;
                try { dbg.enterDecision(129);

                int LA129_0 = input.LA(1);

                if ( (LA129_0==98) ) {
                    alt129=1;
                }


                } finally {dbg.exitDecision(129);}

                switch (alt129) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:746:36: '||' conditionalAndExpression
            	    {
            	    dbg.location(746,36);
            	    match(input,98,FOLLOW_98_in_conditionalOrExpression4368); if (state.failed) return ;
            	    dbg.location(746,41);
            	    pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression4370);
            	    conditionalAndExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop129;
                }
            } while (true);
            } finally {dbg.exitSubRule(129);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 108, conditionalOrExpression_StartIndex); }
        }
        dbg.location(747, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "conditionalOrExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "conditionalOrExpression"


    // $ANTLR start "conditionalAndExpression"
    // Java.g:749:1: conditionalAndExpression : inclusiveOrExpression ( '&&' inclusiveOrExpression )* ;
    public final void conditionalAndExpression() throws RecognitionException {
        int conditionalAndExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "conditionalAndExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(749, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 109) ) { return ; }
            // Java.g:750:5: ( inclusiveOrExpression ( '&&' inclusiveOrExpression )* )
            dbg.enterAlt(1);

            // Java.g:750:9: inclusiveOrExpression ( '&&' inclusiveOrExpression )*
            {
            dbg.location(750,9);
            pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4392);
            inclusiveOrExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(750,31);
            // Java.g:750:31: ( '&&' inclusiveOrExpression )*
            try { dbg.enterSubRule(130);

            loop130:
            do {
                int alt130=2;
                try { dbg.enterDecision(130);

                int LA130_0 = input.LA(1);

                if ( (LA130_0==99) ) {
                    alt130=1;
                }


                } finally {dbg.exitDecision(130);}

                switch (alt130) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:750:33: '&&' inclusiveOrExpression
            	    {
            	    dbg.location(750,33);
            	    match(input,99,FOLLOW_99_in_conditionalAndExpression4396); if (state.failed) return ;
            	    dbg.location(750,38);
            	    pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4398);
            	    inclusiveOrExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop130;
                }
            } while (true);
            } finally {dbg.exitSubRule(130);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 109, conditionalAndExpression_StartIndex); }
        }
        dbg.location(751, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "conditionalAndExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "conditionalAndExpression"


    // $ANTLR start "inclusiveOrExpression"
    // Java.g:753:1: inclusiveOrExpression : exclusiveOrExpression ( '|' exclusiveOrExpression )* ;
    public final void inclusiveOrExpression() throws RecognitionException {
        int inclusiveOrExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "inclusiveOrExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(753, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 110) ) { return ; }
            // Java.g:754:5: ( exclusiveOrExpression ( '|' exclusiveOrExpression )* )
            dbg.enterAlt(1);

            // Java.g:754:9: exclusiveOrExpression ( '|' exclusiveOrExpression )*
            {
            dbg.location(754,9);
            pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4420);
            exclusiveOrExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(754,31);
            // Java.g:754:31: ( '|' exclusiveOrExpression )*
            try { dbg.enterSubRule(131);

            loop131:
            do {
                int alt131=2;
                try { dbg.enterDecision(131);

                int LA131_0 = input.LA(1);

                if ( (LA131_0==100) ) {
                    alt131=1;
                }


                } finally {dbg.exitDecision(131);}

                switch (alt131) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:754:33: '|' exclusiveOrExpression
            	    {
            	    dbg.location(754,33);
            	    match(input,100,FOLLOW_100_in_inclusiveOrExpression4424); if (state.failed) return ;
            	    dbg.location(754,37);
            	    pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4426);
            	    exclusiveOrExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop131;
                }
            } while (true);
            } finally {dbg.exitSubRule(131);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 110, inclusiveOrExpression_StartIndex); }
        }
        dbg.location(755, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "inclusiveOrExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "inclusiveOrExpression"


    // $ANTLR start "exclusiveOrExpression"
    // Java.g:757:1: exclusiveOrExpression : andExpression ( '^' andExpression )* ;
    public final void exclusiveOrExpression() throws RecognitionException {
        int exclusiveOrExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "exclusiveOrExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(757, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 111) ) { return ; }
            // Java.g:758:5: ( andExpression ( '^' andExpression )* )
            dbg.enterAlt(1);

            // Java.g:758:9: andExpression ( '^' andExpression )*
            {
            dbg.location(758,9);
            pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression4448);
            andExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(758,23);
            // Java.g:758:23: ( '^' andExpression )*
            try { dbg.enterSubRule(132);

            loop132:
            do {
                int alt132=2;
                try { dbg.enterDecision(132);

                int LA132_0 = input.LA(1);

                if ( (LA132_0==101) ) {
                    alt132=1;
                }


                } finally {dbg.exitDecision(132);}

                switch (alt132) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:758:25: '^' andExpression
            	    {
            	    dbg.location(758,25);
            	    match(input,101,FOLLOW_101_in_exclusiveOrExpression4452); if (state.failed) return ;
            	    dbg.location(758,29);
            	    pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression4454);
            	    andExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop132;
                }
            } while (true);
            } finally {dbg.exitSubRule(132);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 111, exclusiveOrExpression_StartIndex); }
        }
        dbg.location(759, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "exclusiveOrExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "exclusiveOrExpression"


    // $ANTLR start "andExpression"
    // Java.g:761:1: andExpression : equalityExpression ( '&' equalityExpression )* ;
    public final void andExpression() throws RecognitionException {
        int andExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "andExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(761, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 112) ) { return ; }
            // Java.g:762:5: ( equalityExpression ( '&' equalityExpression )* )
            dbg.enterAlt(1);

            // Java.g:762:9: equalityExpression ( '&' equalityExpression )*
            {
            dbg.location(762,9);
            pushFollow(FOLLOW_equalityExpression_in_andExpression4476);
            equalityExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(762,28);
            // Java.g:762:28: ( '&' equalityExpression )*
            try { dbg.enterSubRule(133);

            loop133:
            do {
                int alt133=2;
                try { dbg.enterDecision(133);

                int LA133_0 = input.LA(1);

                if ( (LA133_0==43) ) {
                    alt133=1;
                }


                } finally {dbg.exitDecision(133);}

                switch (alt133) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:762:30: '&' equalityExpression
            	    {
            	    dbg.location(762,30);
            	    match(input,43,FOLLOW_43_in_andExpression4480); if (state.failed) return ;
            	    dbg.location(762,34);
            	    pushFollow(FOLLOW_equalityExpression_in_andExpression4482);
            	    equalityExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop133;
                }
            } while (true);
            } finally {dbg.exitSubRule(133);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 112, andExpression_StartIndex); }
        }
        dbg.location(763, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "andExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "andExpression"


    // $ANTLR start "equalityExpression"
    // Java.g:765:1: equalityExpression : instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* ;
    public final void equalityExpression() throws RecognitionException {
        int equalityExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "equalityExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(765, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 113) ) { return ; }
            // Java.g:766:5: ( instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* )
            dbg.enterAlt(1);

            // Java.g:766:9: instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )*
            {
            dbg.location(766,9);
            pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression4504);
            instanceOfExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(766,30);
            // Java.g:766:30: ( ( '==' | '!=' ) instanceOfExpression )*
            try { dbg.enterSubRule(134);

            loop134:
            do {
                int alt134=2;
                try { dbg.enterDecision(134);

                int LA134_0 = input.LA(1);

                if ( ((LA134_0>=102 && LA134_0<=103)) ) {
                    alt134=1;
                }


                } finally {dbg.exitDecision(134);}

                switch (alt134) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:766:32: ( '==' | '!=' ) instanceOfExpression
            	    {
            	    dbg.location(766,32);
            	    if ( (input.LA(1)>=102 && input.LA(1)<=103) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(766,46);
            	    pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression4516);
            	    instanceOfExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop134;
                }
            } while (true);
            } finally {dbg.exitSubRule(134);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 113, equalityExpression_StartIndex); }
        }
        dbg.location(767, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "equalityExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "equalityExpression"


    // $ANTLR start "instanceOfExpression"
    // Java.g:769:1: instanceOfExpression : relationalExpression ( 'instanceof' type )? ;
    public final void instanceOfExpression() throws RecognitionException {
        int instanceOfExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "instanceOfExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(769, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 114) ) { return ; }
            // Java.g:770:5: ( relationalExpression ( 'instanceof' type )? )
            dbg.enterAlt(1);

            // Java.g:770:9: relationalExpression ( 'instanceof' type )?
            {
            dbg.location(770,9);
            pushFollow(FOLLOW_relationalExpression_in_instanceOfExpression4538);
            relationalExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(770,30);
            // Java.g:770:30: ( 'instanceof' type )?
            int alt135=2;
            try { dbg.enterSubRule(135);
            try { dbg.enterDecision(135);

            int LA135_0 = input.LA(1);

            if ( (LA135_0==104) ) {
                alt135=1;
            }
            } finally {dbg.exitDecision(135);}

            switch (alt135) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:770:31: 'instanceof' type
                    {
                    dbg.location(770,31);
                    match(input,104,FOLLOW_104_in_instanceOfExpression4541); if (state.failed) return ;
                    dbg.location(770,44);
                    pushFollow(FOLLOW_type_in_instanceOfExpression4543);
                    type();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(135);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 114, instanceOfExpression_StartIndex); }
        }
        dbg.location(771, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "instanceOfExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "instanceOfExpression"


    // $ANTLR start "relationalExpression"
    // Java.g:773:1: relationalExpression : shiftExpression ( relationalOp shiftExpression )* ;
    public final void relationalExpression() throws RecognitionException {
        int relationalExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "relationalExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(773, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 115) ) { return ; }
            // Java.g:774:5: ( shiftExpression ( relationalOp shiftExpression )* )
            dbg.enterAlt(1);

            // Java.g:774:9: shiftExpression ( relationalOp shiftExpression )*
            {
            dbg.location(774,9);
            pushFollow(FOLLOW_shiftExpression_in_relationalExpression4564);
            shiftExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(774,25);
            // Java.g:774:25: ( relationalOp shiftExpression )*
            try { dbg.enterSubRule(136);

            loop136:
            do {
                int alt136=2;
                try { dbg.enterDecision(136);

                int LA136_0 = input.LA(1);

                if ( (LA136_0==40) ) {
                    int LA136_2 = input.LA(2);

                    if ( (LA136_2==Identifier||(LA136_2>=FloatingPointLiteral && LA136_2<=DecimalLiteral)||LA136_2==47||LA136_2==51||(LA136_2>=56 && LA136_2<=63)||(LA136_2>=65 && LA136_2<=66)||(LA136_2>=69 && LA136_2<=72)||(LA136_2>=105 && LA136_2<=106)||(LA136_2>=109 && LA136_2<=113)) ) {
                        alt136=1;
                    }


                }
                else if ( (LA136_0==42) ) {
                    int LA136_3 = input.LA(2);

                    if ( (LA136_3==Identifier||(LA136_3>=FloatingPointLiteral && LA136_3<=DecimalLiteral)||LA136_3==47||LA136_3==51||(LA136_3>=56 && LA136_3<=63)||(LA136_3>=65 && LA136_3<=66)||(LA136_3>=69 && LA136_3<=72)||(LA136_3>=105 && LA136_3<=106)||(LA136_3>=109 && LA136_3<=113)) ) {
                        alt136=1;
                    }


                }


                } finally {dbg.exitDecision(136);}

                switch (alt136) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:774:27: relationalOp shiftExpression
            	    {
            	    dbg.location(774,27);
            	    pushFollow(FOLLOW_relationalOp_in_relationalExpression4568);
            	    relationalOp();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(774,40);
            	    pushFollow(FOLLOW_shiftExpression_in_relationalExpression4570);
            	    shiftExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop136;
                }
            } while (true);
            } finally {dbg.exitSubRule(136);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 115, relationalExpression_StartIndex); }
        }
        dbg.location(775, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "relationalExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "relationalExpression"


    // $ANTLR start "relationalOp"
    // Java.g:777:1: relationalOp : ( ( '<' '=' )=>t1= '<' t2= '=' {...}? | ( '>' '=' )=>t1= '>' t2= '=' {...}? | '<' | '>' );
    public final void relationalOp() throws RecognitionException {
        int relationalOp_StartIndex = input.index();
        Token t1=null;
        Token t2=null;

        try { dbg.enterRule(getGrammarFileName(), "relationalOp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(777, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 116) ) { return ; }
            // Java.g:778:5: ( ( '<' '=' )=>t1= '<' t2= '=' {...}? | ( '>' '=' )=>t1= '>' t2= '=' {...}? | '<' | '>' )
            int alt137=4;
            try { dbg.enterDecision(137);

            int LA137_0 = input.LA(1);

            if ( (LA137_0==40) ) {
                int LA137_1 = input.LA(2);

                if ( (LA137_1==51) && (synpred211_Java())) {
                    alt137=1;
                }
                else if ( (LA137_1==Identifier||(LA137_1>=FloatingPointLiteral && LA137_1<=DecimalLiteral)||LA137_1==47||(LA137_1>=56 && LA137_1<=63)||(LA137_1>=65 && LA137_1<=66)||(LA137_1>=69 && LA137_1<=72)||(LA137_1>=105 && LA137_1<=106)||(LA137_1>=109 && LA137_1<=113)) ) {
                    alt137=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 137, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else if ( (LA137_0==42) ) {
                int LA137_2 = input.LA(2);

                if ( (LA137_2==51) && (synpred212_Java())) {
                    alt137=2;
                }
                else if ( (LA137_2==Identifier||(LA137_2>=FloatingPointLiteral && LA137_2<=DecimalLiteral)||LA137_2==47||(LA137_2>=56 && LA137_2<=63)||(LA137_2>=65 && LA137_2<=66)||(LA137_2>=69 && LA137_2<=72)||(LA137_2>=105 && LA137_2<=106)||(LA137_2>=109 && LA137_2<=113)) ) {
                    alt137=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 137, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 137, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:778:9: ( '<' '=' )=>t1= '<' t2= '=' {...}?
                    {
                    dbg.location(778,23);
                    t1=(Token)match(input,40,FOLLOW_40_in_relationalOp4605); if (state.failed) return ;
                    dbg.location(778,30);
                    t2=(Token)match(input,51,FOLLOW_51_in_relationalOp4609); if (state.failed) return ;
                    dbg.location(779,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "relationalOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:781:9: ( '>' '=' )=>t1= '>' t2= '=' {...}?
                    {
                    dbg.location(781,23);
                    t1=(Token)match(input,42,FOLLOW_42_in_relationalOp4639); if (state.failed) return ;
                    dbg.location(781,30);
                    t2=(Token)match(input,51,FOLLOW_51_in_relationalOp4643); if (state.failed) return ;
                    dbg.location(782,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "relationalOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:784:9: '<'
                    {
                    dbg.location(784,9);
                    match(input,40,FOLLOW_40_in_relationalOp4664); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:785:9: '>'
                    {
                    dbg.location(785,9);
                    match(input,42,FOLLOW_42_in_relationalOp4675); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 116, relationalOp_StartIndex); }
        }
        dbg.location(786, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "relationalOp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "relationalOp"


    // $ANTLR start "shiftExpression"
    // Java.g:788:1: shiftExpression : additiveExpression ( shiftOp additiveExpression )* ;
    public final void shiftExpression() throws RecognitionException {
        int shiftExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "shiftExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(788, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 117) ) { return ; }
            // Java.g:789:5: ( additiveExpression ( shiftOp additiveExpression )* )
            dbg.enterAlt(1);

            // Java.g:789:9: additiveExpression ( shiftOp additiveExpression )*
            {
            dbg.location(789,9);
            pushFollow(FOLLOW_additiveExpression_in_shiftExpression4695);
            additiveExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(789,28);
            // Java.g:789:28: ( shiftOp additiveExpression )*
            try { dbg.enterSubRule(138);

            loop138:
            do {
                int alt138=2;
                try { dbg.enterDecision(138);

                int LA138_0 = input.LA(1);

                if ( (LA138_0==40) ) {
                    int LA138_1 = input.LA(2);

                    if ( (LA138_1==40) ) {
                        int LA138_4 = input.LA(3);

                        if ( (LA138_4==Identifier||(LA138_4>=FloatingPointLiteral && LA138_4<=DecimalLiteral)||LA138_4==47||(LA138_4>=56 && LA138_4<=63)||(LA138_4>=65 && LA138_4<=66)||(LA138_4>=69 && LA138_4<=72)||(LA138_4>=105 && LA138_4<=106)||(LA138_4>=109 && LA138_4<=113)) ) {
                            alt138=1;
                        }


                    }


                }
                else if ( (LA138_0==42) ) {
                    int LA138_2 = input.LA(2);

                    if ( (LA138_2==42) ) {
                        int LA138_5 = input.LA(3);

                        if ( (LA138_5==42) ) {
                            int LA138_7 = input.LA(4);

                            if ( (LA138_7==Identifier||(LA138_7>=FloatingPointLiteral && LA138_7<=DecimalLiteral)||LA138_7==47||(LA138_7>=56 && LA138_7<=63)||(LA138_7>=65 && LA138_7<=66)||(LA138_7>=69 && LA138_7<=72)||(LA138_7>=105 && LA138_7<=106)||(LA138_7>=109 && LA138_7<=113)) ) {
                                alt138=1;
                            }


                        }
                        else if ( (LA138_5==Identifier||(LA138_5>=FloatingPointLiteral && LA138_5<=DecimalLiteral)||LA138_5==47||(LA138_5>=56 && LA138_5<=63)||(LA138_5>=65 && LA138_5<=66)||(LA138_5>=69 && LA138_5<=72)||(LA138_5>=105 && LA138_5<=106)||(LA138_5>=109 && LA138_5<=113)) ) {
                            alt138=1;
                        }


                    }


                }


                } finally {dbg.exitDecision(138);}

                switch (alt138) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:789:30: shiftOp additiveExpression
            	    {
            	    dbg.location(789,30);
            	    pushFollow(FOLLOW_shiftOp_in_shiftExpression4699);
            	    shiftOp();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(789,38);
            	    pushFollow(FOLLOW_additiveExpression_in_shiftExpression4701);
            	    additiveExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop138;
                }
            } while (true);
            } finally {dbg.exitSubRule(138);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 117, shiftExpression_StartIndex); }
        }
        dbg.location(790, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "shiftExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "shiftExpression"


    // $ANTLR start "shiftOp"
    // Java.g:792:1: shiftOp : ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?);
    public final void shiftOp() throws RecognitionException {
        int shiftOp_StartIndex = input.index();
        Token t1=null;
        Token t2=null;
        Token t3=null;

        try { dbg.enterRule(getGrammarFileName(), "shiftOp");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(792, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 118) ) { return ; }
            // Java.g:793:5: ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?)
            int alt139=3;
            try { dbg.enterDecision(139);

            try {
                isCyclicDecision = true;
                alt139 = dfa139.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(139);}

            switch (alt139) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:793:9: ( '<' '<' )=>t1= '<' t2= '<' {...}?
                    {
                    dbg.location(793,23);
                    t1=(Token)match(input,40,FOLLOW_40_in_shiftOp4732); if (state.failed) return ;
                    dbg.location(793,30);
                    t2=(Token)match(input,40,FOLLOW_40_in_shiftOp4736); if (state.failed) return ;
                    dbg.location(794,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:796:9: ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}?
                    {
                    dbg.location(796,27);
                    t1=(Token)match(input,42,FOLLOW_42_in_shiftOp4768); if (state.failed) return ;
                    dbg.location(796,34);
                    t2=(Token)match(input,42,FOLLOW_42_in_shiftOp4772); if (state.failed) return ;
                    dbg.location(796,41);
                    t3=(Token)match(input,42,FOLLOW_42_in_shiftOp4776); if (state.failed) return ;
                    dbg.location(797,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() &&
                              t2.getLine() == t3.getLine() && 
                              t2.getCharPositionInLine() + 1 == t3.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() &&\n          $t2.getLine() == $t3.getLine() && \n          $t2.getCharPositionInLine() + 1 == $t3.getCharPositionInLine() ");
                    }

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:801:9: ( '>' '>' )=>t1= '>' t2= '>' {...}?
                    {
                    dbg.location(801,23);
                    t1=(Token)match(input,42,FOLLOW_42_in_shiftOp4806); if (state.failed) return ;
                    dbg.location(801,30);
                    t2=(Token)match(input,42,FOLLOW_42_in_shiftOp4810); if (state.failed) return ;
                    dbg.location(802,9);
                    if ( !(evalPredicate( t1.getLine() == t2.getLine() && 
                              t1.getCharPositionInLine() + 1 == t2.getCharPositionInLine() ," $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ")) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "shiftOp", " $t1.getLine() == $t2.getLine() && \n          $t1.getCharPositionInLine() + 1 == $t2.getCharPositionInLine() ");
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 118, shiftOp_StartIndex); }
        }
        dbg.location(804, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "shiftOp");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "shiftOp"


    // $ANTLR start "additiveExpression"
    // Java.g:807:1: additiveExpression : multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* ;
    public final void additiveExpression() throws RecognitionException {
        int additiveExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "additiveExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(807, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 119) ) { return ; }
            // Java.g:808:5: ( multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* )
            dbg.enterAlt(1);

            // Java.g:808:9: multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )*
            {
            dbg.location(808,9);
            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression4840);
            multiplicativeExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(808,34);
            // Java.g:808:34: ( ( '+' | '-' ) multiplicativeExpression )*
            try { dbg.enterSubRule(140);

            loop140:
            do {
                int alt140=2;
                try { dbg.enterDecision(140);

                int LA140_0 = input.LA(1);

                if ( ((LA140_0>=105 && LA140_0<=106)) ) {
                    alt140=1;
                }


                } finally {dbg.exitDecision(140);}

                switch (alt140) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:808:36: ( '+' | '-' ) multiplicativeExpression
            	    {
            	    dbg.location(808,36);
            	    if ( (input.LA(1)>=105 && input.LA(1)<=106) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(808,48);
            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression4852);
            	    multiplicativeExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop140;
                }
            } while (true);
            } finally {dbg.exitSubRule(140);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 119, additiveExpression_StartIndex); }
        }
        dbg.location(809, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "additiveExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "additiveExpression"


    // $ANTLR start "multiplicativeExpression"
    // Java.g:811:1: multiplicativeExpression : unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* ;
    public final void multiplicativeExpression() throws RecognitionException {
        int multiplicativeExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "multiplicativeExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(811, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 120) ) { return ; }
            // Java.g:812:5: ( unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* )
            dbg.enterAlt(1);

            // Java.g:812:9: unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )*
            {
            dbg.location(812,9);
            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression4874);
            unaryExpression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(812,25);
            // Java.g:812:25: ( ( '*' | '/' | '%' ) unaryExpression )*
            try { dbg.enterSubRule(141);

            loop141:
            do {
                int alt141=2;
                try { dbg.enterDecision(141);

                int LA141_0 = input.LA(1);

                if ( (LA141_0==30||(LA141_0>=107 && LA141_0<=108)) ) {
                    alt141=1;
                }


                } finally {dbg.exitDecision(141);}

                switch (alt141) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // Java.g:812:27: ( '*' | '/' | '%' ) unaryExpression
            	    {
            	    dbg.location(812,27);
            	    if ( input.LA(1)==30||(input.LA(1)>=107 && input.LA(1)<=108) ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        dbg.recognitionException(mse);
            	        throw mse;
            	    }

            	    dbg.location(812,47);
            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression4892);
            	    unaryExpression();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop141;
                }
            } while (true);
            } finally {dbg.exitSubRule(141);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 120, multiplicativeExpression_StartIndex); }
        }
        dbg.location(813, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "multiplicativeExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "multiplicativeExpression"


    // $ANTLR start "unaryExpression"
    // Java.g:815:1: unaryExpression : ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus );
    public final void unaryExpression() throws RecognitionException {
        int unaryExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "unaryExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(815, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 121) ) { return ; }
            // Java.g:816:5: ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus )
            int alt142=5;
            try { dbg.enterDecision(142);

            switch ( input.LA(1) ) {
            case 105:
                {
                alt142=1;
                }
                break;
            case 106:
                {
                alt142=2;
                }
                break;
            case 109:
                {
                alt142=3;
                }
                break;
            case 110:
                {
                alt142=4;
                }
                break;
            case Identifier:
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 47:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 65:
            case 66:
            case 69:
            case 70:
            case 71:
            case 72:
            case 111:
            case 112:
            case 113:
                {
                alt142=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 142, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(142);}

            switch (alt142) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:816:9: '+' unaryExpression
                    {
                    dbg.location(816,9);
                    match(input,105,FOLLOW_105_in_unaryExpression4918); if (state.failed) return ;
                    dbg.location(816,13);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4920);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:817:9: '-' unaryExpression
                    {
                    dbg.location(817,9);
                    match(input,106,FOLLOW_106_in_unaryExpression4930); if (state.failed) return ;
                    dbg.location(817,13);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4932);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:818:9: '++' unaryExpression
                    {
                    dbg.location(818,9);
                    match(input,109,FOLLOW_109_in_unaryExpression4942); if (state.failed) return ;
                    dbg.location(818,14);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4944);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:819:9: '--' unaryExpression
                    {
                    dbg.location(819,9);
                    match(input,110,FOLLOW_110_in_unaryExpression4954); if (state.failed) return ;
                    dbg.location(819,14);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression4956);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:820:9: unaryExpressionNotPlusMinus
                    {
                    dbg.location(820,9);
                    pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression4966);
                    unaryExpressionNotPlusMinus();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 121, unaryExpression_StartIndex); }
        }
        dbg.location(821, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "unaryExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "unaryExpression"


    // $ANTLR start "unaryExpressionNotPlusMinus"
    // Java.g:823:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );
    public final void unaryExpressionNotPlusMinus() throws RecognitionException {
        int unaryExpressionNotPlusMinus_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "unaryExpressionNotPlusMinus");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(823, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 122) ) { return ; }
            // Java.g:824:5: ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? )
            int alt145=4;
            try { dbg.enterDecision(145);

            try {
                isCyclicDecision = true;
                alt145 = dfa145.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(145);}

            switch (alt145) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:824:9: '~' unaryExpression
                    {
                    dbg.location(824,9);
                    match(input,111,FOLLOW_111_in_unaryExpressionNotPlusMinus4985); if (state.failed) return ;
                    dbg.location(824,13);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4987);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:825:9: '!' unaryExpression
                    {
                    dbg.location(825,9);
                    match(input,112,FOLLOW_112_in_unaryExpressionNotPlusMinus4997); if (state.failed) return ;
                    dbg.location(825,13);
                    pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4999);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:826:9: castExpression
                    {
                    dbg.location(826,9);
                    pushFollow(FOLLOW_castExpression_in_unaryExpressionNotPlusMinus5009);
                    castExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:827:9: primary ( selector )* ( '++' | '--' )?
                    {
                    dbg.location(827,9);
                    pushFollow(FOLLOW_primary_in_unaryExpressionNotPlusMinus5019);
                    primary();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(827,17);
                    // Java.g:827:17: ( selector )*
                    try { dbg.enterSubRule(143);

                    loop143:
                    do {
                        int alt143=2;
                        try { dbg.enterDecision(143);

                        int LA143_0 = input.LA(1);

                        if ( (LA143_0==29||LA143_0==48) ) {
                            alt143=1;
                        }


                        } finally {dbg.exitDecision(143);}

                        switch (alt143) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:0:0: selector
                    	    {
                    	    dbg.location(827,17);
                    	    pushFollow(FOLLOW_selector_in_unaryExpressionNotPlusMinus5021);
                    	    selector();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop143;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(143);}

                    dbg.location(827,27);
                    // Java.g:827:27: ( '++' | '--' )?
                    int alt144=2;
                    try { dbg.enterSubRule(144);
                    try { dbg.enterDecision(144);

                    int LA144_0 = input.LA(1);

                    if ( ((LA144_0>=109 && LA144_0<=110)) ) {
                        alt144=1;
                    }
                    } finally {dbg.exitDecision(144);}

                    switch (alt144) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:
                            {
                            dbg.location(827,27);
                            if ( (input.LA(1)>=109 && input.LA(1)<=110) ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                dbg.recognitionException(mse);
                                throw mse;
                            }


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(144);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 122, unaryExpressionNotPlusMinus_StartIndex); }
        }
        dbg.location(828, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "unaryExpressionNotPlusMinus");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "unaryExpressionNotPlusMinus"


    // $ANTLR start "castExpression"
    // Java.g:830:1: castExpression : ( '(' primitiveType ')' unaryExpression | '(' ( type | expression ) ')' unaryExpressionNotPlusMinus );
    public final void castExpression() throws RecognitionException {
        int castExpression_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "castExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(830, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 123) ) { return ; }
            // Java.g:831:5: ( '(' primitiveType ')' unaryExpression | '(' ( type | expression ) ')' unaryExpressionNotPlusMinus )
            int alt147=2;
            try { dbg.enterDecision(147);

            int LA147_0 = input.LA(1);

            if ( (LA147_0==66) ) {
                int LA147_1 = input.LA(2);

                if ( (synpred233_Java()) ) {
                    alt147=1;
                }
                else if ( (true) ) {
                    alt147=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 147, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 147, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(147);}

            switch (alt147) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:831:8: '(' primitiveType ')' unaryExpression
                    {
                    dbg.location(831,8);
                    match(input,66,FOLLOW_66_in_castExpression5047); if (state.failed) return ;
                    dbg.location(831,12);
                    pushFollow(FOLLOW_primitiveType_in_castExpression5049);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(831,26);
                    match(input,67,FOLLOW_67_in_castExpression5051); if (state.failed) return ;
                    dbg.location(831,30);
                    pushFollow(FOLLOW_unaryExpression_in_castExpression5053);
                    unaryExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:832:8: '(' ( type | expression ) ')' unaryExpressionNotPlusMinus
                    {
                    dbg.location(832,8);
                    match(input,66,FOLLOW_66_in_castExpression5062); if (state.failed) return ;
                    dbg.location(832,12);
                    // Java.g:832:12: ( type | expression )
                    int alt146=2;
                    try { dbg.enterSubRule(146);
                    try { dbg.enterDecision(146);

                    try {
                        isCyclicDecision = true;
                        alt146 = dfa146.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(146);}

                    switch (alt146) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:832:13: type
                            {
                            dbg.location(832,13);
                            pushFollow(FOLLOW_type_in_castExpression5065);
                            type();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:832:20: expression
                            {
                            dbg.location(832,20);
                            pushFollow(FOLLOW_expression_in_castExpression5069);
                            expression();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(146);}

                    dbg.location(832,32);
                    match(input,67,FOLLOW_67_in_castExpression5072); if (state.failed) return ;
                    dbg.location(832,36);
                    pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_castExpression5074);
                    unaryExpressionNotPlusMinus();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 123, castExpression_StartIndex); }
        }
        dbg.location(833, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "castExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "castExpression"


    // $ANTLR start "primary"
    // Java.g:835:1: primary : ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | 'super' superSuffix | literal | 'new' creator | Identifier ( '.' Identifier )* ( identifierSuffix )? | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );
    public final void primary() throws RecognitionException {
        int primary_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "primary");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(835, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 124) ) { return ; }
            // Java.g:836:5: ( parExpression | 'this' ( '.' Identifier )* ( identifierSuffix )? | 'super' superSuffix | literal | 'new' creator | Identifier ( '.' Identifier )* ( identifierSuffix )? | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' )
            int alt153=8;
            try { dbg.enterDecision(153);

            switch ( input.LA(1) ) {
            case 66:
                {
                alt153=1;
                }
                break;
            case 69:
                {
                alt153=2;
                }
                break;
            case 65:
                {
                alt153=3;
                }
                break;
            case FloatingPointLiteral:
            case CharacterLiteral:
            case StringLiteral:
            case HexLiteral:
            case OctalLiteral:
            case DecimalLiteral:
            case 70:
            case 71:
            case 72:
                {
                alt153=4;
                }
                break;
            case 113:
                {
                alt153=5;
                }
                break;
            case Identifier:
                {
                alt153=6;
                }
                break;
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
                {
                alt153=7;
                }
                break;
            case 47:
                {
                alt153=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 153, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(153);}

            switch (alt153) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:836:9: parExpression
                    {
                    dbg.location(836,9);
                    pushFollow(FOLLOW_parExpression_in_primary5093);
                    parExpression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:837:9: 'this' ( '.' Identifier )* ( identifierSuffix )?
                    {
                    dbg.location(837,9);
                    match(input,69,FOLLOW_69_in_primary5103); if (state.failed) return ;
                    dbg.location(837,16);
                    // Java.g:837:16: ( '.' Identifier )*
                    try { dbg.enterSubRule(148);

                    loop148:
                    do {
                        int alt148=2;
                        try { dbg.enterDecision(148);

                        int LA148_0 = input.LA(1);

                        if ( (LA148_0==29) ) {
                            int LA148_2 = input.LA(2);

                            if ( (LA148_2==Identifier) ) {
                                int LA148_3 = input.LA(3);

                                if ( (synpred236_Java()) ) {
                                    alt148=1;
                                }


                            }


                        }


                        } finally {dbg.exitDecision(148);}

                        switch (alt148) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:837:17: '.' Identifier
                    	    {
                    	    dbg.location(837,17);
                    	    match(input,29,FOLLOW_29_in_primary5106); if (state.failed) return ;
                    	    dbg.location(837,21);
                    	    match(input,Identifier,FOLLOW_Identifier_in_primary5108); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop148;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(148);}

                    dbg.location(837,34);
                    // Java.g:837:34: ( identifierSuffix )?
                    int alt149=2;
                    try { dbg.enterSubRule(149);
                    try { dbg.enterDecision(149);

                    try {
                        isCyclicDecision = true;
                        alt149 = dfa149.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(149);}

                    switch (alt149) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: identifierSuffix
                            {
                            dbg.location(837,34);
                            pushFollow(FOLLOW_identifierSuffix_in_primary5112);
                            identifierSuffix();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(149);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:838:9: 'super' superSuffix
                    {
                    dbg.location(838,9);
                    match(input,65,FOLLOW_65_in_primary5123); if (state.failed) return ;
                    dbg.location(838,17);
                    pushFollow(FOLLOW_superSuffix_in_primary5125);
                    superSuffix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:839:9: literal
                    {
                    dbg.location(839,9);
                    pushFollow(FOLLOW_literal_in_primary5135);
                    literal();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:840:9: 'new' creator
                    {
                    dbg.location(840,9);
                    match(input,113,FOLLOW_113_in_primary5145); if (state.failed) return ;
                    dbg.location(840,15);
                    pushFollow(FOLLOW_creator_in_primary5147);
                    creator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:841:9: Identifier ( '.' Identifier )* ( identifierSuffix )?
                    {
                    dbg.location(841,9);
                    match(input,Identifier,FOLLOW_Identifier_in_primary5157); if (state.failed) return ;
                    dbg.location(841,20);
                    // Java.g:841:20: ( '.' Identifier )*
                    try { dbg.enterSubRule(150);

                    loop150:
                    do {
                        int alt150=2;
                        try { dbg.enterDecision(150);

                        int LA150_0 = input.LA(1);

                        if ( (LA150_0==29) ) {
                            int LA150_2 = input.LA(2);

                            if ( (LA150_2==Identifier) ) {
                                int LA150_3 = input.LA(3);

                                if ( (synpred242_Java()) ) {
                                    alt150=1;
                                }


                            }


                        }


                        } finally {dbg.exitDecision(150);}

                        switch (alt150) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:841:21: '.' Identifier
                    	    {
                    	    dbg.location(841,21);
                    	    match(input,29,FOLLOW_29_in_primary5160); if (state.failed) return ;
                    	    dbg.location(841,25);
                    	    match(input,Identifier,FOLLOW_Identifier_in_primary5162); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop150;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(150);}

                    dbg.location(841,38);
                    // Java.g:841:38: ( identifierSuffix )?
                    int alt151=2;
                    try { dbg.enterSubRule(151);
                    try { dbg.enterDecision(151);

                    try {
                        isCyclicDecision = true;
                        alt151 = dfa151.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(151);}

                    switch (alt151) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: identifierSuffix
                            {
                            dbg.location(841,38);
                            pushFollow(FOLLOW_identifierSuffix_in_primary5166);
                            identifierSuffix();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(151);}


                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:842:9: primitiveType ( '[' ']' )* '.' 'class'
                    {
                    dbg.location(842,9);
                    pushFollow(FOLLOW_primitiveType_in_primary5177);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(842,23);
                    // Java.g:842:23: ( '[' ']' )*
                    try { dbg.enterSubRule(152);

                    loop152:
                    do {
                        int alt152=2;
                        try { dbg.enterDecision(152);

                        int LA152_0 = input.LA(1);

                        if ( (LA152_0==48) ) {
                            alt152=1;
                        }


                        } finally {dbg.exitDecision(152);}

                        switch (alt152) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:842:24: '[' ']'
                    	    {
                    	    dbg.location(842,24);
                    	    match(input,48,FOLLOW_48_in_primary5180); if (state.failed) return ;
                    	    dbg.location(842,28);
                    	    match(input,49,FOLLOW_49_in_primary5182); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop152;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(152);}

                    dbg.location(842,34);
                    match(input,29,FOLLOW_29_in_primary5186); if (state.failed) return ;
                    dbg.location(842,38);
                    match(input,37,FOLLOW_37_in_primary5188); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:843:9: 'void' '.' 'class'
                    {
                    dbg.location(843,9);
                    match(input,47,FOLLOW_47_in_primary5198); if (state.failed) return ;
                    dbg.location(843,16);
                    match(input,29,FOLLOW_29_in_primary5200); if (state.failed) return ;
                    dbg.location(843,20);
                    match(input,37,FOLLOW_37_in_primary5202); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 124, primary_StartIndex); }
        }
        dbg.location(844, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "primary");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "primary"


    // $ANTLR start "identifierSuffix"
    // Java.g:846:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator );
    public final void identifierSuffix() throws RecognitionException {
        int identifierSuffix_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "identifierSuffix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(846, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 125) ) { return ; }
            // Java.g:847:5: ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator )
            int alt156=8;
            try { dbg.enterDecision(156);

            try {
                isCyclicDecision = true;
                alt156 = dfa156.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(156);}

            switch (alt156) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:847:9: ( '[' ']' )+ '.' 'class'
                    {
                    dbg.location(847,9);
                    // Java.g:847:9: ( '[' ']' )+
                    int cnt154=0;
                    try { dbg.enterSubRule(154);

                    loop154:
                    do {
                        int alt154=2;
                        try { dbg.enterDecision(154);

                        int LA154_0 = input.LA(1);

                        if ( (LA154_0==48) ) {
                            alt154=1;
                        }


                        } finally {dbg.exitDecision(154);}

                        switch (alt154) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:847:10: '[' ']'
                    	    {
                    	    dbg.location(847,10);
                    	    match(input,48,FOLLOW_48_in_identifierSuffix5222); if (state.failed) return ;
                    	    dbg.location(847,14);
                    	    match(input,49,FOLLOW_49_in_identifierSuffix5224); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt154 >= 1 ) break loop154;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(154, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt154++;
                    } while (true);
                    } finally {dbg.exitSubRule(154);}

                    dbg.location(847,20);
                    match(input,29,FOLLOW_29_in_identifierSuffix5228); if (state.failed) return ;
                    dbg.location(847,24);
                    match(input,37,FOLLOW_37_in_identifierSuffix5230); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:848:9: ( '[' expression ']' )+
                    {
                    dbg.location(848,9);
                    // Java.g:848:9: ( '[' expression ']' )+
                    int cnt155=0;
                    try { dbg.enterSubRule(155);

                    loop155:
                    do {
                        int alt155=2;
                        try { dbg.enterDecision(155);

                        try {
                            isCyclicDecision = true;
                            alt155 = dfa155.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(155);}

                        switch (alt155) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:848:10: '[' expression ']'
                    	    {
                    	    dbg.location(848,10);
                    	    match(input,48,FOLLOW_48_in_identifierSuffix5241); if (state.failed) return ;
                    	    dbg.location(848,14);
                    	    pushFollow(FOLLOW_expression_in_identifierSuffix5243);
                    	    expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(848,25);
                    	    match(input,49,FOLLOW_49_in_identifierSuffix5245); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt155 >= 1 ) break loop155;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(155, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt155++;
                    } while (true);
                    } finally {dbg.exitSubRule(155);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:849:9: arguments
                    {
                    dbg.location(849,9);
                    pushFollow(FOLLOW_arguments_in_identifierSuffix5258);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:850:9: '.' 'class'
                    {
                    dbg.location(850,9);
                    match(input,29,FOLLOW_29_in_identifierSuffix5268); if (state.failed) return ;
                    dbg.location(850,13);
                    match(input,37,FOLLOW_37_in_identifierSuffix5270); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:851:9: '.' explicitGenericInvocation
                    {
                    dbg.location(851,9);
                    match(input,29,FOLLOW_29_in_identifierSuffix5280); if (state.failed) return ;
                    dbg.location(851,13);
                    pushFollow(FOLLOW_explicitGenericInvocation_in_identifierSuffix5282);
                    explicitGenericInvocation();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // Java.g:852:9: '.' 'this'
                    {
                    dbg.location(852,9);
                    match(input,29,FOLLOW_29_in_identifierSuffix5292); if (state.failed) return ;
                    dbg.location(852,13);
                    match(input,69,FOLLOW_69_in_identifierSuffix5294); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // Java.g:853:9: '.' 'super' arguments
                    {
                    dbg.location(853,9);
                    match(input,29,FOLLOW_29_in_identifierSuffix5304); if (state.failed) return ;
                    dbg.location(853,13);
                    match(input,65,FOLLOW_65_in_identifierSuffix5306); if (state.failed) return ;
                    dbg.location(853,21);
                    pushFollow(FOLLOW_arguments_in_identifierSuffix5308);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // Java.g:854:9: '.' 'new' innerCreator
                    {
                    dbg.location(854,9);
                    match(input,29,FOLLOW_29_in_identifierSuffix5318); if (state.failed) return ;
                    dbg.location(854,13);
                    match(input,113,FOLLOW_113_in_identifierSuffix5320); if (state.failed) return ;
                    dbg.location(854,19);
                    pushFollow(FOLLOW_innerCreator_in_identifierSuffix5322);
                    innerCreator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 125, identifierSuffix_StartIndex); }
        }
        dbg.location(855, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "identifierSuffix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "identifierSuffix"


    // $ANTLR start "creator"
    // Java.g:857:1: creator : ( nonWildcardTypeArguments createdName classCreatorRest | createdName ( arrayCreatorRest | classCreatorRest ) );
    public final void creator() throws RecognitionException {
        int creator_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "creator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(857, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 126) ) { return ; }
            // Java.g:858:5: ( nonWildcardTypeArguments createdName classCreatorRest | createdName ( arrayCreatorRest | classCreatorRest ) )
            int alt158=2;
            try { dbg.enterDecision(158);

            int LA158_0 = input.LA(1);

            if ( (LA158_0==40) ) {
                alt158=1;
            }
            else if ( (LA158_0==Identifier||(LA158_0>=56 && LA158_0<=63)) ) {
                alt158=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 158, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(158);}

            switch (alt158) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:858:9: nonWildcardTypeArguments createdName classCreatorRest
                    {
                    dbg.location(858,9);
                    pushFollow(FOLLOW_nonWildcardTypeArguments_in_creator5341);
                    nonWildcardTypeArguments();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(858,34);
                    pushFollow(FOLLOW_createdName_in_creator5343);
                    createdName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(858,46);
                    pushFollow(FOLLOW_classCreatorRest_in_creator5345);
                    classCreatorRest();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:859:9: createdName ( arrayCreatorRest | classCreatorRest )
                    {
                    dbg.location(859,9);
                    pushFollow(FOLLOW_createdName_in_creator5355);
                    createdName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(859,21);
                    // Java.g:859:21: ( arrayCreatorRest | classCreatorRest )
                    int alt157=2;
                    try { dbg.enterSubRule(157);
                    try { dbg.enterDecision(157);

                    int LA157_0 = input.LA(1);

                    if ( (LA157_0==48) ) {
                        alt157=1;
                    }
                    else if ( (LA157_0==66) ) {
                        alt157=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 157, 0, input);

                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(157);}

                    switch (alt157) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:859:22: arrayCreatorRest
                            {
                            dbg.location(859,22);
                            pushFollow(FOLLOW_arrayCreatorRest_in_creator5358);
                            arrayCreatorRest();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            dbg.enterAlt(2);

                            // Java.g:859:41: classCreatorRest
                            {
                            dbg.location(859,41);
                            pushFollow(FOLLOW_classCreatorRest_in_creator5362);
                            classCreatorRest();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(157);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 126, creator_StartIndex); }
        }
        dbg.location(860, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "creator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "creator"


    // $ANTLR start "createdName"
    // Java.g:862:1: createdName : ( classOrInterfaceType | primitiveType );
    public final void createdName() throws RecognitionException {
        int createdName_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "createdName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(862, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 127) ) { return ; }
            // Java.g:863:5: ( classOrInterfaceType | primitiveType )
            int alt159=2;
            try { dbg.enterDecision(159);

            int LA159_0 = input.LA(1);

            if ( (LA159_0==Identifier) ) {
                alt159=1;
            }
            else if ( ((LA159_0>=56 && LA159_0<=63)) ) {
                alt159=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 159, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(159);}

            switch (alt159) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:863:9: classOrInterfaceType
                    {
                    dbg.location(863,9);
                    pushFollow(FOLLOW_classOrInterfaceType_in_createdName5382);
                    classOrInterfaceType();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:864:9: primitiveType
                    {
                    dbg.location(864,9);
                    pushFollow(FOLLOW_primitiveType_in_createdName5392);
                    primitiveType();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 127, createdName_StartIndex); }
        }
        dbg.location(865, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "createdName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "createdName"


    // $ANTLR start "innerCreator"
    // Java.g:867:1: innerCreator : ( nonWildcardTypeArguments )? Identifier classCreatorRest ;
    public final void innerCreator() throws RecognitionException {
        int innerCreator_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "innerCreator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(867, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 128) ) { return ; }
            // Java.g:868:5: ( ( nonWildcardTypeArguments )? Identifier classCreatorRest )
            dbg.enterAlt(1);

            // Java.g:868:9: ( nonWildcardTypeArguments )? Identifier classCreatorRest
            {
            dbg.location(868,9);
            // Java.g:868:9: ( nonWildcardTypeArguments )?
            int alt160=2;
            try { dbg.enterSubRule(160);
            try { dbg.enterDecision(160);

            int LA160_0 = input.LA(1);

            if ( (LA160_0==40) ) {
                alt160=1;
            }
            } finally {dbg.exitDecision(160);}

            switch (alt160) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: nonWildcardTypeArguments
                    {
                    dbg.location(868,9);
                    pushFollow(FOLLOW_nonWildcardTypeArguments_in_innerCreator5415);
                    nonWildcardTypeArguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(160);}

            dbg.location(868,35);
            match(input,Identifier,FOLLOW_Identifier_in_innerCreator5418); if (state.failed) return ;
            dbg.location(868,46);
            pushFollow(FOLLOW_classCreatorRest_in_innerCreator5420);
            classCreatorRest();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 128, innerCreator_StartIndex); }
        }
        dbg.location(869, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "innerCreator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "innerCreator"


    // $ANTLR start "arrayCreatorRest"
    // Java.g:871:1: arrayCreatorRest : '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* ) ;
    public final void arrayCreatorRest() throws RecognitionException {
        int arrayCreatorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "arrayCreatorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(871, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 129) ) { return ; }
            // Java.g:872:5: ( '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* ) )
            dbg.enterAlt(1);

            // Java.g:872:9: '[' ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* )
            {
            dbg.location(872,9);
            match(input,48,FOLLOW_48_in_arrayCreatorRest5439); if (state.failed) return ;
            dbg.location(873,9);
            // Java.g:873:9: ( ']' ( '[' ']' )* arrayInitializer | expression ']' ( '[' expression ']' )* ( '[' ']' )* )
            int alt164=2;
            try { dbg.enterSubRule(164);
            try { dbg.enterDecision(164);

            int LA164_0 = input.LA(1);

            if ( (LA164_0==49) ) {
                alt164=1;
            }
            else if ( (LA164_0==Identifier||(LA164_0>=FloatingPointLiteral && LA164_0<=DecimalLiteral)||LA164_0==47||(LA164_0>=56 && LA164_0<=63)||(LA164_0>=65 && LA164_0<=66)||(LA164_0>=69 && LA164_0<=72)||(LA164_0>=105 && LA164_0<=106)||(LA164_0>=109 && LA164_0<=113)) ) {
                alt164=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 164, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(164);}

            switch (alt164) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:873:13: ']' ( '[' ']' )* arrayInitializer
                    {
                    dbg.location(873,13);
                    match(input,49,FOLLOW_49_in_arrayCreatorRest5453); if (state.failed) return ;
                    dbg.location(873,17);
                    // Java.g:873:17: ( '[' ']' )*
                    try { dbg.enterSubRule(161);

                    loop161:
                    do {
                        int alt161=2;
                        try { dbg.enterDecision(161);

                        int LA161_0 = input.LA(1);

                        if ( (LA161_0==48) ) {
                            alt161=1;
                        }


                        } finally {dbg.exitDecision(161);}

                        switch (alt161) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:873:18: '[' ']'
                    	    {
                    	    dbg.location(873,18);
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5456); if (state.failed) return ;
                    	    dbg.location(873,22);
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5458); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop161;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(161);}

                    dbg.location(873,28);
                    pushFollow(FOLLOW_arrayInitializer_in_arrayCreatorRest5462);
                    arrayInitializer();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:874:13: expression ']' ( '[' expression ']' )* ( '[' ']' )*
                    {
                    dbg.location(874,13);
                    pushFollow(FOLLOW_expression_in_arrayCreatorRest5476);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(874,24);
                    match(input,49,FOLLOW_49_in_arrayCreatorRest5478); if (state.failed) return ;
                    dbg.location(874,28);
                    // Java.g:874:28: ( '[' expression ']' )*
                    try { dbg.enterSubRule(162);

                    loop162:
                    do {
                        int alt162=2;
                        try { dbg.enterDecision(162);

                        try {
                            isCyclicDecision = true;
                            alt162 = dfa162.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(162);}

                        switch (alt162) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:874:29: '[' expression ']'
                    	    {
                    	    dbg.location(874,29);
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5481); if (state.failed) return ;
                    	    dbg.location(874,33);
                    	    pushFollow(FOLLOW_expression_in_arrayCreatorRest5483);
                    	    expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    dbg.location(874,44);
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5485); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop162;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(162);}

                    dbg.location(874,50);
                    // Java.g:874:50: ( '[' ']' )*
                    try { dbg.enterSubRule(163);

                    loop163:
                    do {
                        int alt163=2;
                        try { dbg.enterDecision(163);

                        int LA163_0 = input.LA(1);

                        if ( (LA163_0==48) ) {
                            int LA163_2 = input.LA(2);

                            if ( (LA163_2==49) ) {
                                alt163=1;
                            }


                        }


                        } finally {dbg.exitDecision(163);}

                        switch (alt163) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // Java.g:874:51: '[' ']'
                    	    {
                    	    dbg.location(874,51);
                    	    match(input,48,FOLLOW_48_in_arrayCreatorRest5490); if (state.failed) return ;
                    	    dbg.location(874,55);
                    	    match(input,49,FOLLOW_49_in_arrayCreatorRest5492); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop163;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(163);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(164);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 129, arrayCreatorRest_StartIndex); }
        }
        dbg.location(876, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "arrayCreatorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "arrayCreatorRest"


    // $ANTLR start "classCreatorRest"
    // Java.g:878:1: classCreatorRest : arguments ( classBody )? ;
    public final void classCreatorRest() throws RecognitionException {
        int classCreatorRest_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "classCreatorRest");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(878, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 130) ) { return ; }
            // Java.g:879:5: ( arguments ( classBody )? )
            dbg.enterAlt(1);

            // Java.g:879:9: arguments ( classBody )?
            {
            dbg.location(879,9);
            pushFollow(FOLLOW_arguments_in_classCreatorRest5523);
            arguments();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(879,19);
            // Java.g:879:19: ( classBody )?
            int alt165=2;
            try { dbg.enterSubRule(165);
            try { dbg.enterDecision(165);

            int LA165_0 = input.LA(1);

            if ( (LA165_0==44) ) {
                alt165=1;
            }
            } finally {dbg.exitDecision(165);}

            switch (alt165) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: classBody
                    {
                    dbg.location(879,19);
                    pushFollow(FOLLOW_classBody_in_classCreatorRest5525);
                    classBody();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(165);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 130, classCreatorRest_StartIndex); }
        }
        dbg.location(880, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "classCreatorRest");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "classCreatorRest"


    // $ANTLR start "explicitGenericInvocation"
    // Java.g:882:1: explicitGenericInvocation : nonWildcardTypeArguments Identifier arguments ;
    public final void explicitGenericInvocation() throws RecognitionException {
        int explicitGenericInvocation_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "explicitGenericInvocation");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(882, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 131) ) { return ; }
            // Java.g:883:5: ( nonWildcardTypeArguments Identifier arguments )
            dbg.enterAlt(1);

            // Java.g:883:9: nonWildcardTypeArguments Identifier arguments
            {
            dbg.location(883,9);
            pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitGenericInvocation5549);
            nonWildcardTypeArguments();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(883,34);
            match(input,Identifier,FOLLOW_Identifier_in_explicitGenericInvocation5551); if (state.failed) return ;
            dbg.location(883,45);
            pushFollow(FOLLOW_arguments_in_explicitGenericInvocation5553);
            arguments();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 131, explicitGenericInvocation_StartIndex); }
        }
        dbg.location(884, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "explicitGenericInvocation");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "explicitGenericInvocation"


    // $ANTLR start "nonWildcardTypeArguments"
    // Java.g:886:1: nonWildcardTypeArguments : '<' typeList '>' ;
    public final void nonWildcardTypeArguments() throws RecognitionException {
        int nonWildcardTypeArguments_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "nonWildcardTypeArguments");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(886, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 132) ) { return ; }
            // Java.g:887:5: ( '<' typeList '>' )
            dbg.enterAlt(1);

            // Java.g:887:9: '<' typeList '>'
            {
            dbg.location(887,9);
            match(input,40,FOLLOW_40_in_nonWildcardTypeArguments5576); if (state.failed) return ;
            dbg.location(887,13);
            pushFollow(FOLLOW_typeList_in_nonWildcardTypeArguments5578);
            typeList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(887,22);
            match(input,42,FOLLOW_42_in_nonWildcardTypeArguments5580); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 132, nonWildcardTypeArguments_StartIndex); }
        }
        dbg.location(888, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "nonWildcardTypeArguments");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "nonWildcardTypeArguments"


    // $ANTLR start "selector"
    // Java.g:890:1: selector : ( '.' Identifier ( arguments )? | '.' 'this' | '.' 'super' superSuffix | '.' 'new' innerCreator | '[' expression ']' );
    public final void selector() throws RecognitionException {
        int selector_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(890, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 133) ) { return ; }
            // Java.g:891:5: ( '.' Identifier ( arguments )? | '.' 'this' | '.' 'super' superSuffix | '.' 'new' innerCreator | '[' expression ']' )
            int alt167=5;
            try { dbg.enterDecision(167);

            int LA167_0 = input.LA(1);

            if ( (LA167_0==29) ) {
                switch ( input.LA(2) ) {
                case Identifier:
                    {
                    alt167=1;
                    }
                    break;
                case 69:
                    {
                    alt167=2;
                    }
                    break;
                case 65:
                    {
                    alt167=3;
                    }
                    break;
                case 113:
                    {
                    alt167=4;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 167, 1, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }

            }
            else if ( (LA167_0==48) ) {
                alt167=5;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 167, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(167);}

            switch (alt167) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:891:9: '.' Identifier ( arguments )?
                    {
                    dbg.location(891,9);
                    match(input,29,FOLLOW_29_in_selector5603); if (state.failed) return ;
                    dbg.location(891,13);
                    match(input,Identifier,FOLLOW_Identifier_in_selector5605); if (state.failed) return ;
                    dbg.location(891,24);
                    // Java.g:891:24: ( arguments )?
                    int alt166=2;
                    try { dbg.enterSubRule(166);
                    try { dbg.enterDecision(166);

                    int LA166_0 = input.LA(1);

                    if ( (LA166_0==66) ) {
                        alt166=1;
                    }
                    } finally {dbg.exitDecision(166);}

                    switch (alt166) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: arguments
                            {
                            dbg.location(891,24);
                            pushFollow(FOLLOW_arguments_in_selector5607);
                            arguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(166);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:892:9: '.' 'this'
                    {
                    dbg.location(892,9);
                    match(input,29,FOLLOW_29_in_selector5618); if (state.failed) return ;
                    dbg.location(892,13);
                    match(input,69,FOLLOW_69_in_selector5620); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // Java.g:893:9: '.' 'super' superSuffix
                    {
                    dbg.location(893,9);
                    match(input,29,FOLLOW_29_in_selector5630); if (state.failed) return ;
                    dbg.location(893,13);
                    match(input,65,FOLLOW_65_in_selector5632); if (state.failed) return ;
                    dbg.location(893,21);
                    pushFollow(FOLLOW_superSuffix_in_selector5634);
                    superSuffix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // Java.g:894:9: '.' 'new' innerCreator
                    {
                    dbg.location(894,9);
                    match(input,29,FOLLOW_29_in_selector5644); if (state.failed) return ;
                    dbg.location(894,13);
                    match(input,113,FOLLOW_113_in_selector5646); if (state.failed) return ;
                    dbg.location(894,19);
                    pushFollow(FOLLOW_innerCreator_in_selector5648);
                    innerCreator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // Java.g:895:9: '[' expression ']'
                    {
                    dbg.location(895,9);
                    match(input,48,FOLLOW_48_in_selector5658); if (state.failed) return ;
                    dbg.location(895,13);
                    pushFollow(FOLLOW_expression_in_selector5660);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(895,24);
                    match(input,49,FOLLOW_49_in_selector5662); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 133, selector_StartIndex); }
        }
        dbg.location(896, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "superSuffix"
    // Java.g:898:1: superSuffix : ( arguments | '.' Identifier ( arguments )? );
    public final void superSuffix() throws RecognitionException {
        int superSuffix_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "superSuffix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(898, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 134) ) { return ; }
            // Java.g:899:5: ( arguments | '.' Identifier ( arguments )? )
            int alt169=2;
            try { dbg.enterDecision(169);

            int LA169_0 = input.LA(1);

            if ( (LA169_0==66) ) {
                alt169=1;
            }
            else if ( (LA169_0==29) ) {
                alt169=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 169, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(169);}

            switch (alt169) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:899:9: arguments
                    {
                    dbg.location(899,9);
                    pushFollow(FOLLOW_arguments_in_superSuffix5685);
                    arguments();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // Java.g:900:9: '.' Identifier ( arguments )?
                    {
                    dbg.location(900,9);
                    match(input,29,FOLLOW_29_in_superSuffix5695); if (state.failed) return ;
                    dbg.location(900,13);
                    match(input,Identifier,FOLLOW_Identifier_in_superSuffix5697); if (state.failed) return ;
                    dbg.location(900,24);
                    // Java.g:900:24: ( arguments )?
                    int alt168=2;
                    try { dbg.enterSubRule(168);
                    try { dbg.enterDecision(168);

                    int LA168_0 = input.LA(1);

                    if ( (LA168_0==66) ) {
                        alt168=1;
                    }
                    } finally {dbg.exitDecision(168);}

                    switch (alt168) {
                        case 1 :
                            dbg.enterAlt(1);

                            // Java.g:0:0: arguments
                            {
                            dbg.location(900,24);
                            pushFollow(FOLLOW_arguments_in_superSuffix5699);
                            arguments();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(168);}


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 134, superSuffix_StartIndex); }
        }
        dbg.location(901, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "superSuffix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "superSuffix"


    // $ANTLR start "arguments"
    // Java.g:903:1: arguments : '(' ( expressionList )? ')' ;
    public final void arguments() throws RecognitionException {
        int arguments_StartIndex = input.index();
        try { dbg.enterRule(getGrammarFileName(), "arguments");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(903, 1);

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 135) ) { return ; }
            // Java.g:904:5: ( '(' ( expressionList )? ')' )
            dbg.enterAlt(1);

            // Java.g:904:9: '(' ( expressionList )? ')'
            {
            dbg.location(904,9);
            match(input,66,FOLLOW_66_in_arguments5719); if (state.failed) return ;
            dbg.location(904,13);
            // Java.g:904:13: ( expressionList )?
            int alt170=2;
            try { dbg.enterSubRule(170);
            try { dbg.enterDecision(170);

            int LA170_0 = input.LA(1);

            if ( (LA170_0==Identifier||(LA170_0>=FloatingPointLiteral && LA170_0<=DecimalLiteral)||LA170_0==47||(LA170_0>=56 && LA170_0<=63)||(LA170_0>=65 && LA170_0<=66)||(LA170_0>=69 && LA170_0<=72)||(LA170_0>=105 && LA170_0<=106)||(LA170_0>=109 && LA170_0<=113)) ) {
                alt170=1;
            }
            } finally {dbg.exitDecision(170);}

            switch (alt170) {
                case 1 :
                    dbg.enterAlt(1);

                    // Java.g:0:0: expressionList
                    {
                    dbg.location(904,13);
                    pushFollow(FOLLOW_expressionList_in_arguments5721);
                    expressionList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(170);}

            dbg.location(904,29);
            match(input,67,FOLLOW_67_in_arguments5724); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 135, arguments_StartIndex); }
        }
        dbg.location(905, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "arguments");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "arguments"

    // $ANTLR start synpred5_Java
    public final void synpred5_Java_fragment() throws RecognitionException {   
        // Java.g:178:9: ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) )
        dbg.enterAlt(1);

        // Java.g:178:9: annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
        {
        dbg.location(178,9);
        pushFollow(FOLLOW_annotations_in_synpred5_Java44);
        annotations();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(179,9);
        // Java.g:179:9: ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* )
        int alt176=2;
        try { dbg.enterSubRule(176);
        try { dbg.enterDecision(176);

        int LA176_0 = input.LA(1);

        if ( (LA176_0==25) ) {
            alt176=1;
        }
        else if ( (LA176_0==ENUM||LA176_0==28||(LA176_0>=31 && LA176_0<=37)||LA176_0==46||LA176_0==73) ) {
            alt176=2;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 176, 0, input);

            dbg.recognitionException(nvae);
            throw nvae;
        }
        } finally {dbg.exitDecision(176);}

        switch (alt176) {
            case 1 :
                dbg.enterAlt(1);

                // Java.g:179:13: packageDeclaration ( importDeclaration )* ( typeDeclaration )*
                {
                dbg.location(179,13);
                pushFollow(FOLLOW_packageDeclaration_in_synpred5_Java58);
                packageDeclaration();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(179,32);
                // Java.g:179:32: ( importDeclaration )*
                try { dbg.enterSubRule(173);

                loop173:
                do {
                    int alt173=2;
                    try { dbg.enterDecision(173);

                    int LA173_0 = input.LA(1);

                    if ( (LA173_0==27) ) {
                        alt173=1;
                    }


                    } finally {dbg.exitDecision(173);}

                    switch (alt173) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // Java.g:0:0: importDeclaration
                	    {
                	    dbg.location(179,32);
                	    pushFollow(FOLLOW_importDeclaration_in_synpred5_Java60);
                	    importDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop173;
                    }
                } while (true);
                } finally {dbg.exitSubRule(173);}

                dbg.location(179,51);
                // Java.g:179:51: ( typeDeclaration )*
                try { dbg.enterSubRule(174);

                loop174:
                do {
                    int alt174=2;
                    try { dbg.enterDecision(174);

                    int LA174_0 = input.LA(1);

                    if ( (LA174_0==ENUM||LA174_0==26||LA174_0==28||(LA174_0>=31 && LA174_0<=37)||LA174_0==46||LA174_0==73) ) {
                        alt174=1;
                    }


                    } finally {dbg.exitDecision(174);}

                    switch (alt174) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // Java.g:0:0: typeDeclaration
                	    {
                	    dbg.location(179,51);
                	    pushFollow(FOLLOW_typeDeclaration_in_synpred5_Java63);
                	    typeDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop174;
                    }
                } while (true);
                } finally {dbg.exitSubRule(174);}


                }
                break;
            case 2 :
                dbg.enterAlt(2);

                // Java.g:180:13: classOrInterfaceDeclaration ( typeDeclaration )*
                {
                dbg.location(180,13);
                pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred5_Java78);
                classOrInterfaceDeclaration();

                state._fsp--;
                if (state.failed) return ;
                dbg.location(180,41);
                // Java.g:180:41: ( typeDeclaration )*
                try { dbg.enterSubRule(175);

                loop175:
                do {
                    int alt175=2;
                    try { dbg.enterDecision(175);

                    int LA175_0 = input.LA(1);

                    if ( (LA175_0==ENUM||LA175_0==26||LA175_0==28||(LA175_0>=31 && LA175_0<=37)||LA175_0==46||LA175_0==73) ) {
                        alt175=1;
                    }


                    } finally {dbg.exitDecision(175);}

                    switch (alt175) {
                	case 1 :
                	    dbg.enterAlt(1);

                	    // Java.g:0:0: typeDeclaration
                	    {
                	    dbg.location(180,41);
                	    pushFollow(FOLLOW_typeDeclaration_in_synpred5_Java80);
                	    typeDeclaration();

                	    state._fsp--;
                	    if (state.failed) return ;

                	    }
                	    break;

                	default :
                	    break loop175;
                    }
                } while (true);
                } finally {dbg.exitSubRule(175);}


                }
                break;

        }
        } finally {dbg.exitSubRule(176);}


        }
    }
    // $ANTLR end synpred5_Java

    // $ANTLR start synpred113_Java
    public final void synpred113_Java_fragment() throws RecognitionException {   
        // Java.g:492:13: ( explicitConstructorInvocation )
        dbg.enterAlt(1);

        // Java.g:492:13: explicitConstructorInvocation
        {
        dbg.location(492,13);
        pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred113_Java2455);
        explicitConstructorInvocation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred113_Java

    // $ANTLR start synpred117_Java
    public final void synpred117_Java_fragment() throws RecognitionException {   
        // Java.g:496:9: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' )
        dbg.enterAlt(1);

        // Java.g:496:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
        {
        dbg.location(496,9);
        // Java.g:496:9: ( nonWildcardTypeArguments )?
        int alt184=2;
        try { dbg.enterSubRule(184);
        try { dbg.enterDecision(184);

        int LA184_0 = input.LA(1);

        if ( (LA184_0==40) ) {
            alt184=1;
        }
        } finally {dbg.exitDecision(184);}

        switch (alt184) {
            case 1 :
                dbg.enterAlt(1);

                // Java.g:0:0: nonWildcardTypeArguments
                {
                dbg.location(496,9);
                pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred117_Java2480);
                nonWildcardTypeArguments();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }
        } finally {dbg.exitSubRule(184);}

        dbg.location(496,35);
        if ( input.LA(1)==65||input.LA(1)==69 ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            dbg.recognitionException(mse);
            throw mse;
        }

        dbg.location(496,54);
        pushFollow(FOLLOW_arguments_in_synpred117_Java2491);
        arguments();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(496,64);
        match(input,26,FOLLOW_26_in_synpred117_Java2493); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred117_Java

    // $ANTLR start synpred128_Java
    public final void synpred128_Java_fragment() throws RecognitionException {   
        // Java.g:528:9: ( annotation )
        dbg.enterAlt(1);

        // Java.g:528:9: annotation
        {
        dbg.location(528,9);
        pushFollow(FOLLOW_annotation_in_synpred128_Java2704);
        annotation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred128_Java

    // $ANTLR start synpred151_Java
    public final void synpred151_Java_fragment() throws RecognitionException {   
        // Java.g:601:9: ( localVariableDeclarationStatement )
        dbg.enterAlt(1);

        // Java.g:601:9: localVariableDeclarationStatement
        {
        dbg.location(601,9);
        pushFollow(FOLLOW_localVariableDeclarationStatement_in_synpred151_Java3231);
        localVariableDeclarationStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred151_Java

    // $ANTLR start synpred152_Java
    public final void synpred152_Java_fragment() throws RecognitionException {   
        // Java.g:602:9: ( classOrInterfaceDeclaration )
        dbg.enterAlt(1);

        // Java.g:602:9: classOrInterfaceDeclaration
        {
        dbg.location(602,9);
        pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred152_Java3241);
        classOrInterfaceDeclaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred152_Java

    // $ANTLR start synpred157_Java
    public final void synpred157_Java_fragment() throws RecognitionException {   
        // Java.g:621:54: ( 'else' statement )
        dbg.enterAlt(1);

        // Java.g:621:54: 'else' statement
        {
        dbg.location(621,54);
        match(input,77,FOLLOW_77_in_synpred157_Java3386); if (state.failed) return ;
        dbg.location(621,61);
        pushFollow(FOLLOW_statement_in_synpred157_Java3388);
        statement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred157_Java

    // $ANTLR start synpred162_Java
    public final void synpred162_Java_fragment() throws RecognitionException {   
        // Java.g:626:11: ( catches 'finally' block )
        dbg.enterAlt(1);

        // Java.g:626:11: catches 'finally' block
        {
        dbg.location(626,11);
        pushFollow(FOLLOW_catches_in_synpred162_Java3464);
        catches();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(626,19);
        match(input,82,FOLLOW_82_in_synpred162_Java3466); if (state.failed) return ;
        dbg.location(626,29);
        pushFollow(FOLLOW_block_in_synpred162_Java3468);
        block();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred162_Java

    // $ANTLR start synpred163_Java
    public final void synpred163_Java_fragment() throws RecognitionException {   
        // Java.g:627:11: ( catches )
        dbg.enterAlt(1);

        // Java.g:627:11: catches
        {
        dbg.location(627,11);
        pushFollow(FOLLOW_catches_in_synpred163_Java3480);
        catches();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred163_Java

    // $ANTLR start synpred178_Java
    public final void synpred178_Java_fragment() throws RecognitionException {   
        // Java.g:662:9: ( switchLabel )
        dbg.enterAlt(1);

        // Java.g:662:9: switchLabel
        {
        dbg.location(662,9);
        pushFollow(FOLLOW_switchLabel_in_synpred178_Java3771);
        switchLabel();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred178_Java

    // $ANTLR start synpred180_Java
    public final void synpred180_Java_fragment() throws RecognitionException {   
        // Java.g:666:9: ( 'case' constantExpression ':' )
        dbg.enterAlt(1);

        // Java.g:666:9: 'case' constantExpression ':'
        {
        dbg.location(666,9);
        match(input,89,FOLLOW_89_in_synpred180_Java3798); if (state.failed) return ;
        dbg.location(666,16);
        pushFollow(FOLLOW_constantExpression_in_synpred180_Java3800);
        constantExpression();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(666,35);
        match(input,75,FOLLOW_75_in_synpred180_Java3802); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred180_Java

    // $ANTLR start synpred181_Java
    public final void synpred181_Java_fragment() throws RecognitionException {   
        // Java.g:667:9: ( 'case' enumConstantName ':' )
        dbg.enterAlt(1);

        // Java.g:667:9: 'case' enumConstantName ':'
        {
        dbg.location(667,9);
        match(input,89,FOLLOW_89_in_synpred181_Java3812); if (state.failed) return ;
        dbg.location(667,16);
        pushFollow(FOLLOW_enumConstantName_in_synpred181_Java3814);
        enumConstantName();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(667,33);
        match(input,75,FOLLOW_75_in_synpred181_Java3816); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred181_Java

    // $ANTLR start synpred182_Java
    public final void synpred182_Java_fragment() throws RecognitionException {   
        // Java.g:673:9: ( enhancedForControl )
        dbg.enterAlt(1);

        // Java.g:673:9: enhancedForControl
        {
        dbg.location(673,9);
        pushFollow(FOLLOW_enhancedForControl_in_synpred182_Java3859);
        enhancedForControl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred182_Java

    // $ANTLR start synpred186_Java
    public final void synpred186_Java_fragment() throws RecognitionException {   
        // Java.g:678:9: ( localVariableDeclaration )
        dbg.enterAlt(1);

        // Java.g:678:9: localVariableDeclaration
        {
        dbg.location(678,9);
        pushFollow(FOLLOW_localVariableDeclaration_in_synpred186_Java3899);
        localVariableDeclaration();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred186_Java

    // $ANTLR start synpred188_Java
    public final void synpred188_Java_fragment() throws RecognitionException {   
        // Java.g:709:32: ( assignmentOperator expression )
        dbg.enterAlt(1);

        // Java.g:709:32: assignmentOperator expression
        {
        dbg.location(709,32);
        pushFollow(FOLLOW_assignmentOperator_in_synpred188_Java4082);
        assignmentOperator();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(709,51);
        pushFollow(FOLLOW_expression_in_synpred188_Java4084);
        expression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred188_Java

    // $ANTLR start synpred198_Java
    public final void synpred198_Java_fragment() throws RecognitionException {   
        // Java.g:722:9: ( '<' '<' '=' )
        dbg.enterAlt(1);

        // Java.g:722:10: '<' '<' '='
        {
        dbg.location(722,10);
        match(input,40,FOLLOW_40_in_synpred198_Java4200); if (state.failed) return ;
        dbg.location(722,14);
        match(input,40,FOLLOW_40_in_synpred198_Java4202); if (state.failed) return ;
        dbg.location(722,18);
        match(input,51,FOLLOW_51_in_synpred198_Java4204); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred198_Java

    // $ANTLR start synpred199_Java
    public final void synpred199_Java_fragment() throws RecognitionException {   
        // Java.g:727:9: ( '>' '>' '>' '=' )
        dbg.enterAlt(1);

        // Java.g:727:10: '>' '>' '>' '='
        {
        dbg.location(727,10);
        match(input,42,FOLLOW_42_in_synpred199_Java4240); if (state.failed) return ;
        dbg.location(727,14);
        match(input,42,FOLLOW_42_in_synpred199_Java4242); if (state.failed) return ;
        dbg.location(727,18);
        match(input,42,FOLLOW_42_in_synpred199_Java4244); if (state.failed) return ;
        dbg.location(727,22);
        match(input,51,FOLLOW_51_in_synpred199_Java4246); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred199_Java

    // $ANTLR start synpred200_Java
    public final void synpred200_Java_fragment() throws RecognitionException {   
        // Java.g:734:9: ( '>' '>' '=' )
        dbg.enterAlt(1);

        // Java.g:734:10: '>' '>' '='
        {
        dbg.location(734,10);
        match(input,42,FOLLOW_42_in_synpred200_Java4285); if (state.failed) return ;
        dbg.location(734,14);
        match(input,42,FOLLOW_42_in_synpred200_Java4287); if (state.failed) return ;
        dbg.location(734,18);
        match(input,51,FOLLOW_51_in_synpred200_Java4289); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred200_Java

    // $ANTLR start synpred211_Java
    public final void synpred211_Java_fragment() throws RecognitionException {   
        // Java.g:778:9: ( '<' '=' )
        dbg.enterAlt(1);

        // Java.g:778:10: '<' '='
        {
        dbg.location(778,10);
        match(input,40,FOLLOW_40_in_synpred211_Java4597); if (state.failed) return ;
        dbg.location(778,14);
        match(input,51,FOLLOW_51_in_synpred211_Java4599); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred211_Java

    // $ANTLR start synpred212_Java
    public final void synpred212_Java_fragment() throws RecognitionException {   
        // Java.g:781:9: ( '>' '=' )
        dbg.enterAlt(1);

        // Java.g:781:10: '>' '='
        {
        dbg.location(781,10);
        match(input,42,FOLLOW_42_in_synpred212_Java4631); if (state.failed) return ;
        dbg.location(781,14);
        match(input,51,FOLLOW_51_in_synpred212_Java4633); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred212_Java

    // $ANTLR start synpred215_Java
    public final void synpred215_Java_fragment() throws RecognitionException {   
        // Java.g:793:9: ( '<' '<' )
        dbg.enterAlt(1);

        // Java.g:793:10: '<' '<'
        {
        dbg.location(793,10);
        match(input,40,FOLLOW_40_in_synpred215_Java4724); if (state.failed) return ;
        dbg.location(793,14);
        match(input,40,FOLLOW_40_in_synpred215_Java4726); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred215_Java

    // $ANTLR start synpred216_Java
    public final void synpred216_Java_fragment() throws RecognitionException {   
        // Java.g:796:9: ( '>' '>' '>' )
        dbg.enterAlt(1);

        // Java.g:796:10: '>' '>' '>'
        {
        dbg.location(796,10);
        match(input,42,FOLLOW_42_in_synpred216_Java4758); if (state.failed) return ;
        dbg.location(796,14);
        match(input,42,FOLLOW_42_in_synpred216_Java4760); if (state.failed) return ;
        dbg.location(796,18);
        match(input,42,FOLLOW_42_in_synpred216_Java4762); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred216_Java

    // $ANTLR start synpred217_Java
    public final void synpred217_Java_fragment() throws RecognitionException {   
        // Java.g:801:9: ( '>' '>' )
        dbg.enterAlt(1);

        // Java.g:801:10: '>' '>'
        {
        dbg.location(801,10);
        match(input,42,FOLLOW_42_in_synpred217_Java4798); if (state.failed) return ;
        dbg.location(801,14);
        match(input,42,FOLLOW_42_in_synpred217_Java4800); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred217_Java

    // $ANTLR start synpred229_Java
    public final void synpred229_Java_fragment() throws RecognitionException {   
        // Java.g:826:9: ( castExpression )
        dbg.enterAlt(1);

        // Java.g:826:9: castExpression
        {
        dbg.location(826,9);
        pushFollow(FOLLOW_castExpression_in_synpred229_Java5009);
        castExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred229_Java

    // $ANTLR start synpred233_Java
    public final void synpred233_Java_fragment() throws RecognitionException {   
        // Java.g:831:8: ( '(' primitiveType ')' unaryExpression )
        dbg.enterAlt(1);

        // Java.g:831:8: '(' primitiveType ')' unaryExpression
        {
        dbg.location(831,8);
        match(input,66,FOLLOW_66_in_synpred233_Java5047); if (state.failed) return ;
        dbg.location(831,12);
        pushFollow(FOLLOW_primitiveType_in_synpred233_Java5049);
        primitiveType();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(831,26);
        match(input,67,FOLLOW_67_in_synpred233_Java5051); if (state.failed) return ;
        dbg.location(831,30);
        pushFollow(FOLLOW_unaryExpression_in_synpred233_Java5053);
        unaryExpression();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred233_Java

    // $ANTLR start synpred234_Java
    public final void synpred234_Java_fragment() throws RecognitionException {   
        // Java.g:832:13: ( type )
        dbg.enterAlt(1);

        // Java.g:832:13: type
        {
        dbg.location(832,13);
        pushFollow(FOLLOW_type_in_synpred234_Java5065);
        type();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred234_Java

    // $ANTLR start synpred236_Java
    public final void synpred236_Java_fragment() throws RecognitionException {   
        // Java.g:837:17: ( '.' Identifier )
        dbg.enterAlt(1);

        // Java.g:837:17: '.' Identifier
        {
        dbg.location(837,17);
        match(input,29,FOLLOW_29_in_synpred236_Java5106); if (state.failed) return ;
        dbg.location(837,21);
        match(input,Identifier,FOLLOW_Identifier_in_synpred236_Java5108); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred236_Java

    // $ANTLR start synpred237_Java
    public final void synpred237_Java_fragment() throws RecognitionException {   
        // Java.g:837:34: ( identifierSuffix )
        dbg.enterAlt(1);

        // Java.g:837:34: identifierSuffix
        {
        dbg.location(837,34);
        pushFollow(FOLLOW_identifierSuffix_in_synpred237_Java5112);
        identifierSuffix();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred237_Java

    // $ANTLR start synpred242_Java
    public final void synpred242_Java_fragment() throws RecognitionException {   
        // Java.g:841:21: ( '.' Identifier )
        dbg.enterAlt(1);

        // Java.g:841:21: '.' Identifier
        {
        dbg.location(841,21);
        match(input,29,FOLLOW_29_in_synpred242_Java5160); if (state.failed) return ;
        dbg.location(841,25);
        match(input,Identifier,FOLLOW_Identifier_in_synpred242_Java5162); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred242_Java

    // $ANTLR start synpred243_Java
    public final void synpred243_Java_fragment() throws RecognitionException {   
        // Java.g:841:38: ( identifierSuffix )
        dbg.enterAlt(1);

        // Java.g:841:38: identifierSuffix
        {
        dbg.location(841,38);
        pushFollow(FOLLOW_identifierSuffix_in_synpred243_Java5166);
        identifierSuffix();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred243_Java

    // $ANTLR start synpred249_Java
    public final void synpred249_Java_fragment() throws RecognitionException {   
        // Java.g:848:10: ( '[' expression ']' )
        dbg.enterAlt(1);

        // Java.g:848:10: '[' expression ']'
        {
        dbg.location(848,10);
        match(input,48,FOLLOW_48_in_synpred249_Java5241); if (state.failed) return ;
        dbg.location(848,14);
        pushFollow(FOLLOW_expression_in_synpred249_Java5243);
        expression();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(848,25);
        match(input,49,FOLLOW_49_in_synpred249_Java5245); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred249_Java

    // $ANTLR start synpred262_Java
    public final void synpred262_Java_fragment() throws RecognitionException {   
        // Java.g:874:29: ( '[' expression ']' )
        dbg.enterAlt(1);

        // Java.g:874:29: '[' expression ']'
        {
        dbg.location(874,29);
        match(input,48,FOLLOW_48_in_synpred262_Java5481); if (state.failed) return ;
        dbg.location(874,33);
        pushFollow(FOLLOW_expression_in_synpred262_Java5483);
        expression();

        state._fsp--;
        if (state.failed) return ;
        dbg.location(874,44);
        match(input,49,FOLLOW_49_in_synpred262_Java5485); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred262_Java

    // Delegated rules

    public final boolean synpred157_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred157_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred211_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred211_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred249_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred249_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred243_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred243_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred5_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred229_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred229_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred178_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred178_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred215_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred215_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred113_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred113_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred151_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred151_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred117_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred117_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred162_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred162_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred217_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred217_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred186_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred186_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred188_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred188_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred212_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred212_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred163_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred163_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred152_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred152_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred242_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred242_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred199_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred199_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred216_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred216_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred236_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred236_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred262_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred262_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred198_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred198_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred233_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred233_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred180_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred180_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred128_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred128_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred200_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred200_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred234_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred234_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred182_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred182_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred181_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred181_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred237_Java() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred237_Java_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA8 dfa8 = new DFA8(this);
    protected DFA81 dfa81 = new DFA81(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA106 dfa106 = new DFA106(this);
    protected DFA114 dfa114 = new DFA114(this);
    protected DFA123 dfa123 = new DFA123(this);
    protected DFA124 dfa124 = new DFA124(this);
    protected DFA126 dfa126 = new DFA126(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA139 dfa139 = new DFA139(this);
    protected DFA145 dfa145 = new DFA145(this);
    protected DFA146 dfa146 = new DFA146(this);
    protected DFA149 dfa149 = new DFA149(this);
    protected DFA151 dfa151 = new DFA151(this);
    protected DFA156 dfa156 = new DFA156(this);
    protected DFA155 dfa155 = new DFA155(this);
    protected DFA162 dfa162 = new DFA162(this);
    static final String DFA8_eotS =
        "\21\uffff";
    static final String DFA8_eofS =
        "\1\2\20\uffff";
    static final String DFA8_minS =
        "\1\5\1\0\17\uffff";
    static final String DFA8_maxS =
        "\1\111\1\0\17\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\15\uffff\1\1";
    static final String DFA8_specialS =
        "\1\uffff\1\0\17\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\2\23\uffff\4\2\2\uffff\7\2\10\uffff\1\2\32\uffff\1\1",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "177:1: compilationUnit : ( annotations ( packageDeclaration ( importDeclaration )* ( typeDeclaration )* | classOrInterfaceDeclaration ( typeDeclaration )* ) | ( packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA8_1 = input.LA(1);

                         
                        int index8_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Java()) ) {s = 16;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 8, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA81_eotS =
        "\57\uffff";
    static final String DFA81_eofS =
        "\57\uffff";
    static final String DFA81_minS =
        "\1\4\1\uffff\15\0\40\uffff";
    static final String DFA81_maxS =
        "\1\161\1\uffff\15\0\40\uffff";
    static final String DFA81_acceptS =
        "\1\uffff\1\1\15\uffff\1\2\37\uffff";
    static final String DFA81_specialS =
        "\2\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\40\uffff}>";
    static final String[] DFA81_transitionS = {
            "\1\14\1\17\1\6\1\7\1\10\3\5\1\17\15\uffff\1\17\1\uffff\1\17"+
            "\2\uffff\7\17\2\uffff\1\1\3\uffff\3\17\1\16\5\uffff\1\17\2\uffff"+
            "\10\15\1\uffff\1\4\1\3\2\uffff\1\2\1\12\2\11\1\17\2\uffff\1"+
            "\17\1\uffff\4\17\1\uffff\5\17\21\uffff\2\17\2\uffff\4\17\1\13",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA81_eot = DFA.unpackEncodedString(DFA81_eotS);
    static final short[] DFA81_eof = DFA.unpackEncodedString(DFA81_eofS);
    static final char[] DFA81_min = DFA.unpackEncodedStringToUnsignedChars(DFA81_minS);
    static final char[] DFA81_max = DFA.unpackEncodedStringToUnsignedChars(DFA81_maxS);
    static final short[] DFA81_accept = DFA.unpackEncodedString(DFA81_acceptS);
    static final short[] DFA81_special = DFA.unpackEncodedString(DFA81_specialS);
    static final short[][] DFA81_transition;

    static {
        int numStates = DFA81_transitionS.length;
        DFA81_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA81_transition[i] = DFA.unpackEncodedString(DFA81_transitionS[i]);
        }
    }

    class DFA81 extends DFA {

        public DFA81(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 81;
            this.eot = DFA81_eot;
            this.eof = DFA81_eof;
            this.min = DFA81_min;
            this.max = DFA81_max;
            this.accept = DFA81_accept;
            this.special = DFA81_special;
            this.transition = DFA81_transition;
        }
        public String getDescription() {
            return "492:13: ( explicitConstructorInvocation )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA81_2 = input.LA(1);

                         
                        int index81_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA81_3 = input.LA(1);

                         
                        int index81_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA81_4 = input.LA(1);

                         
                        int index81_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_4);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA81_5 = input.LA(1);

                         
                        int index81_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA81_6 = input.LA(1);

                         
                        int index81_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA81_7 = input.LA(1);

                         
                        int index81_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA81_8 = input.LA(1);

                         
                        int index81_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA81_9 = input.LA(1);

                         
                        int index81_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA81_10 = input.LA(1);

                         
                        int index81_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA81_11 = input.LA(1);

                         
                        int index81_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA81_12 = input.LA(1);

                         
                        int index81_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA81_13 = input.LA(1);

                         
                        int index81_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA81_14 = input.LA(1);

                         
                        int index81_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred113_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 15;}

                         
                        input.seek(index81_14);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 81, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA85_eotS =
        "\17\uffff";
    static final String DFA85_eofS =
        "\17\uffff";
    static final String DFA85_minS =
        "\1\4\1\uffff\1\0\1\uffff\1\0\12\uffff";
    static final String DFA85_maxS =
        "\1\161\1\uffff\1\0\1\uffff\1\0\12\uffff";
    static final String DFA85_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\13\uffff";
    static final String DFA85_specialS =
        "\2\uffff\1\0\1\uffff\1\1\12\uffff}>";
    static final String[] DFA85_transitionS = {
            "\1\3\1\uffff\6\3\34\uffff\1\1\6\uffff\1\3\10\uffff\10\3\1\uffff"+
            "\1\4\1\3\2\uffff\1\2\3\3\50\uffff\1\3",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA85_eot = DFA.unpackEncodedString(DFA85_eotS);
    static final short[] DFA85_eof = DFA.unpackEncodedString(DFA85_eofS);
    static final char[] DFA85_min = DFA.unpackEncodedStringToUnsignedChars(DFA85_minS);
    static final char[] DFA85_max = DFA.unpackEncodedStringToUnsignedChars(DFA85_maxS);
    static final short[] DFA85_accept = DFA.unpackEncodedString(DFA85_acceptS);
    static final short[] DFA85_special = DFA.unpackEncodedString(DFA85_specialS);
    static final short[][] DFA85_transition;

    static {
        int numStates = DFA85_transitionS.length;
        DFA85_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA85_transition[i] = DFA.unpackEncodedString(DFA85_transitionS[i]);
        }
    }

    class DFA85 extends DFA {

        public DFA85(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 85;
            this.eot = DFA85_eot;
            this.eof = DFA85_eof;
            this.min = DFA85_min;
            this.max = DFA85_max;
            this.accept = DFA85_accept;
            this.special = DFA85_special;
            this.transition = DFA85_transition;
        }
        public String getDescription() {
            return "495:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA85_2 = input.LA(1);

                         
                        int index85_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred117_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index85_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA85_4 = input.LA(1);

                         
                        int index85_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred117_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index85_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 85, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA106_eotS =
        "\56\uffff";
    static final String DFA106_eofS =
        "\56\uffff";
    static final String DFA106_minS =
        "\1\4\4\0\51\uffff";
    static final String DFA106_maxS =
        "\1\161\4\0\51\uffff";
    static final String DFA106_acceptS =
        "\5\uffff\1\2\10\uffff\1\3\36\uffff\1\1";
    static final String DFA106_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\51\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\3\1\5\7\16\15\uffff\1\16\1\uffff\1\5\2\uffff\4\5\1\1\2\5"+
            "\6\uffff\1\16\1\uffff\1\5\1\16\5\uffff\1\16\2\uffff\10\4\1\uffff"+
            "\2\16\2\uffff\4\16\1\2\2\uffff\1\16\1\uffff\4\16\1\uffff\5\16"+
            "\21\uffff\2\16\2\uffff\5\16",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA106_eot = DFA.unpackEncodedString(DFA106_eotS);
    static final short[] DFA106_eof = DFA.unpackEncodedString(DFA106_eofS);
    static final char[] DFA106_min = DFA.unpackEncodedStringToUnsignedChars(DFA106_minS);
    static final char[] DFA106_max = DFA.unpackEncodedStringToUnsignedChars(DFA106_maxS);
    static final short[] DFA106_accept = DFA.unpackEncodedString(DFA106_acceptS);
    static final short[] DFA106_special = DFA.unpackEncodedString(DFA106_specialS);
    static final short[][] DFA106_transition;

    static {
        int numStates = DFA106_transitionS.length;
        DFA106_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA106_transition[i] = DFA.unpackEncodedString(DFA106_transitionS[i]);
        }
    }

    class DFA106 extends DFA {

        public DFA106(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 106;
            this.eot = DFA106_eot;
            this.eof = DFA106_eof;
            this.min = DFA106_min;
            this.max = DFA106_max;
            this.accept = DFA106_accept;
            this.special = DFA106_special;
            this.transition = DFA106_transition;
        }
        public String getDescription() {
            return "600:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA106_1 = input.LA(1);

                         
                        int index106_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (synpred152_Java()) ) {s = 5;}

                         
                        input.seek(index106_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA106_2 = input.LA(1);

                         
                        int index106_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (synpred152_Java()) ) {s = 5;}

                         
                        input.seek(index106_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA106_3 = input.LA(1);

                         
                        int index106_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index106_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA106_4 = input.LA(1);

                         
                        int index106_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred151_Java()) ) {s = 45;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index106_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 106, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA114_eotS =
        "\22\uffff";
    static final String DFA114_eofS =
        "\22\uffff";
    static final String DFA114_minS =
        "\1\4\17\uffff\1\32\1\uffff";
    static final String DFA114_maxS =
        "\1\161\17\uffff\1\156\1\uffff";
    static final String DFA114_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1"+
        "\15\1\16\1\17\1\uffff\1\20";
    static final String DFA114_specialS =
        "\22\uffff}>";
    static final String[] DFA114_transitionS = {
            "\1\20\1\uffff\6\17\1\2\15\uffff\1\16\21\uffff\1\1\2\uffff\1"+
            "\17\5\uffff\1\11\2\uffff\10\17\1\uffff\2\17\2\uffff\4\17\3\uffff"+
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\10\1\12\1\13\1\14\1\15"+
            "\21\uffff\2\17\2\uffff\5\17",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\17\2\uffff\2\17\11\uffff\1\17\1\uffff\2\17\4\uffff\1\17"+
            "\2\uffff\1\17\14\uffff\1\17\1\uffff\1\17\10\uffff\1\21\16\uffff"+
            "\25\17",
            ""
    };

    static final short[] DFA114_eot = DFA.unpackEncodedString(DFA114_eotS);
    static final short[] DFA114_eof = DFA.unpackEncodedString(DFA114_eofS);
    static final char[] DFA114_min = DFA.unpackEncodedStringToUnsignedChars(DFA114_minS);
    static final char[] DFA114_max = DFA.unpackEncodedStringToUnsignedChars(DFA114_maxS);
    static final short[] DFA114_accept = DFA.unpackEncodedString(DFA114_acceptS);
    static final short[] DFA114_special = DFA.unpackEncodedString(DFA114_specialS);
    static final short[][] DFA114_transition;

    static {
        int numStates = DFA114_transitionS.length;
        DFA114_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA114_transition[i] = DFA.unpackEncodedString(DFA114_transitionS[i]);
        }
    }

    class DFA114 extends DFA {

        public DFA114(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 114;
            this.eot = DFA114_eot;
            this.eof = DFA114_eof;
            this.min = DFA114_min;
            this.max = DFA114_max;
            this.accept = DFA114_accept;
            this.special = DFA114_special;
            this.transition = DFA114_transition;
        }
        public String getDescription() {
            return "618:1: statement : ( block | ASSERT expression ( ':' expression )? ';' | 'if' parExpression statement ( options {k=1; } : 'else' statement )? | 'for' '(' forControl ')' statement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | 'try' block ( catches 'finally' block | catches | 'finally' block ) | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( Identifier )? ';' | 'continue' ( Identifier )? ';' | ';' | statementExpression ';' | Identifier ':' statement );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA123_eotS =
        "\u0087\uffff";
    static final String DFA123_eofS =
        "\u0087\uffff";
    static final String DFA123_minS =
        "\5\4\22\uffff\10\4\1\32\30\uffff\1\61\1\uffff\1\32\21\0\2\uffff"+
        "\3\0\21\uffff\1\0\5\uffff\1\0\30\uffff\1\0\5\uffff";
    static final String DFA123_maxS =
        "\1\161\1\111\1\4\1\156\1\60\22\uffff\2\60\1\111\1\4\1\111\3\161"+
        "\1\113\30\uffff\1\61\1\uffff\1\113\21\0\2\uffff\3\0\21\uffff\1\0"+
        "\5\uffff\1\0\30\uffff\1\0\5\uffff";
    static final String DFA123_acceptS =
        "\5\uffff\1\2\166\uffff\1\1\12\uffff";
    static final String DFA123_specialS =
        "\73\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\2\uffff\1\21\1\22\1\23\21\uffff\1\24\5\uffff"+
        "\1\25\30\uffff\1\26\5\uffff}>";
    static final String[] DFA123_transitionS = {
            "\1\3\1\uffff\6\5\16\uffff\1\5\10\uffff\1\1\13\uffff\1\5\10"+
            "\uffff\10\4\1\uffff\2\5\2\uffff\4\5\1\2\37\uffff\2\5\2\uffff"+
            "\5\5",
            "\1\27\36\uffff\1\31\24\uffff\10\30\11\uffff\1\32",
            "\1\33",
            "\1\37\25\uffff\1\5\2\uffff\1\35\1\5\11\uffff\1\34\3\5\4\uffff"+
            "\1\36\2\uffff\1\5\14\uffff\1\5\1\uffff\1\5\27\uffff\25\5",
            "\1\72\30\uffff\1\5\22\uffff\1\70",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\76\30\uffff\1\74\12\uffff\1\73\7\uffff\1\75",
            "\1\100\53\uffff\1\77",
            "\1\101\36\uffff\1\103\24\uffff\10\102\11\uffff\1\104",
            "\1\105",
            "\1\110\30\uffff\1\106\5\uffff\1\112\24\uffff\10\111\2\uffff"+
            "\1\107\6\uffff\1\113",
            "\1\116\1\uffff\6\5\34\uffff\1\5\6\uffff\1\5\3\uffff\1\5\4"+
            "\uffff\10\117\1\120\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "\1\142\40\uffff\1\5\2\uffff\1\5\30\uffff\1\5\3\uffff\1\5\53"+
            "\uffff\1\5",
            "\1\5\1\uffff\6\5\43\uffff\1\5\1\uffff\1\150\6\uffff\10\5\1"+
            "\uffff\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "\1\5\16\uffff\1\5\6\uffff\1\5\2\uffff\1\5\27\uffff\1\174",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0081",
            "",
            "\1\5\16\uffff\1\5\6\uffff\1\5\2\uffff\1\5\27\uffff\1\174",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA123_eot = DFA.unpackEncodedString(DFA123_eotS);
    static final short[] DFA123_eof = DFA.unpackEncodedString(DFA123_eofS);
    static final char[] DFA123_min = DFA.unpackEncodedStringToUnsignedChars(DFA123_minS);
    static final char[] DFA123_max = DFA.unpackEncodedStringToUnsignedChars(DFA123_maxS);
    static final short[] DFA123_accept = DFA.unpackEncodedString(DFA123_acceptS);
    static final short[] DFA123_special = DFA.unpackEncodedString(DFA123_specialS);
    static final short[][] DFA123_transition;

    static {
        int numStates = DFA123_transitionS.length;
        DFA123_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA123_transition[i] = DFA.unpackEncodedString(DFA123_transitionS[i]);
        }
    }

    class DFA123 extends DFA {

        public DFA123(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 123;
            this.eot = DFA123_eot;
            this.eof = DFA123_eof;
            this.min = DFA123_min;
            this.max = DFA123_max;
            this.accept = DFA123_accept;
            this.special = DFA123_special;
            this.transition = DFA123_transition;
        }
        public String getDescription() {
            return "671:1: forControl options {k=3; } : ( enhancedForControl | ( forInit )? ';' ( expression )? ';' ( forUpdate )? );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA123_59 = input.LA(1);

                         
                        int index123_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_59);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA123_60 = input.LA(1);

                         
                        int index123_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_60);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA123_61 = input.LA(1);

                         
                        int index123_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_61);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA123_62 = input.LA(1);

                         
                        int index123_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_62);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA123_63 = input.LA(1);

                         
                        int index123_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_63);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA123_64 = input.LA(1);

                         
                        int index123_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_64);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA123_65 = input.LA(1);

                         
                        int index123_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_65);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA123_66 = input.LA(1);

                         
                        int index123_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_66);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA123_67 = input.LA(1);

                         
                        int index123_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_67);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA123_68 = input.LA(1);

                         
                        int index123_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_68);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA123_69 = input.LA(1);

                         
                        int index123_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_69);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA123_70 = input.LA(1);

                         
                        int index123_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_70);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA123_71 = input.LA(1);

                         
                        int index123_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_71);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA123_72 = input.LA(1);

                         
                        int index123_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_72);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA123_73 = input.LA(1);

                         
                        int index123_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_73);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA123_74 = input.LA(1);

                         
                        int index123_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_74);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA123_75 = input.LA(1);

                         
                        int index123_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_75);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA123_78 = input.LA(1);

                         
                        int index123_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_78);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA123_79 = input.LA(1);

                         
                        int index123_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_79);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA123_80 = input.LA(1);

                         
                        int index123_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_80);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA123_98 = input.LA(1);

                         
                        int index123_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_98);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA123_104 = input.LA(1);

                         
                        int index123_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_104);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA123_129 = input.LA(1);

                         
                        int index123_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred182_Java()) ) {s = 124;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index123_129);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 123, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA124_eotS =
        "\26\uffff";
    static final String DFA124_eofS =
        "\26\uffff";
    static final String DFA124_minS =
        "\1\4\2\uffff\2\0\21\uffff";
    static final String DFA124_maxS =
        "\1\161\2\uffff\2\0\21\uffff";
    static final String DFA124_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\20\uffff";
    static final String DFA124_specialS =
        "\3\uffff\1\0\1\1\21\uffff}>";
    static final String[] DFA124_transitionS = {
            "\1\3\1\uffff\6\5\27\uffff\1\1\13\uffff\1\5\10\uffff\10\4\1"+
            "\uffff\2\5\2\uffff\4\5\1\1\37\uffff\2\5\2\uffff\5\5",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA124_eot = DFA.unpackEncodedString(DFA124_eotS);
    static final short[] DFA124_eof = DFA.unpackEncodedString(DFA124_eofS);
    static final char[] DFA124_min = DFA.unpackEncodedStringToUnsignedChars(DFA124_minS);
    static final char[] DFA124_max = DFA.unpackEncodedStringToUnsignedChars(DFA124_maxS);
    static final short[] DFA124_accept = DFA.unpackEncodedString(DFA124_acceptS);
    static final short[] DFA124_special = DFA.unpackEncodedString(DFA124_specialS);
    static final short[][] DFA124_transition;

    static {
        int numStates = DFA124_transitionS.length;
        DFA124_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA124_transition[i] = DFA.unpackEncodedString(DFA124_transitionS[i]);
        }
    }

    class DFA124 extends DFA {

        public DFA124(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 124;
            this.eot = DFA124_eot;
            this.eof = DFA124_eof;
            this.min = DFA124_min;
            this.max = DFA124_max;
            this.accept = DFA124_accept;
            this.special = DFA124_special;
            this.transition = DFA124_transition;
        }
        public String getDescription() {
            return "677:1: forInit : ( localVariableDeclaration | expressionList );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA124_3 = input.LA(1);

                         
                        int index124_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred186_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index124_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA124_4 = input.LA(1);

                         
                        int index124_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred186_Java()) ) {s = 1;}

                        else if ( (true) ) {s = 5;}

                         
                        input.seek(index124_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 124, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA126_eotS =
        "\16\uffff";
    static final String DFA126_eofS =
        "\1\14\15\uffff";
    static final String DFA126_minS =
        "\1\32\13\0\2\uffff";
    static final String DFA126_maxS =
        "\1\141\13\0\2\uffff";
    static final String DFA126_acceptS =
        "\14\uffff\1\2\1\1";
    static final String DFA126_specialS =
        "\1\uffff\1\5\1\2\1\12\1\10\1\1\1\4\1\7\1\11\1\0\1\6\1\3\2\uffff}>";
    static final String[] DFA126_transitionS = {
            "\1\14\15\uffff\1\12\1\14\1\13\2\uffff\1\14\3\uffff\1\14\1\uffff"+
            "\1\1\17\uffff\1\14\7\uffff\1\14\16\uffff\1\2\1\3\1\4\1\5\1\6"+
            "\1\7\1\10\1\11",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA126_eot = DFA.unpackEncodedString(DFA126_eotS);
    static final short[] DFA126_eof = DFA.unpackEncodedString(DFA126_eofS);
    static final char[] DFA126_min = DFA.unpackEncodedStringToUnsignedChars(DFA126_minS);
    static final char[] DFA126_max = DFA.unpackEncodedStringToUnsignedChars(DFA126_maxS);
    static final short[] DFA126_accept = DFA.unpackEncodedString(DFA126_acceptS);
    static final short[] DFA126_special = DFA.unpackEncodedString(DFA126_specialS);
    static final short[][] DFA126_transition;

    static {
        int numStates = DFA126_transitionS.length;
        DFA126_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA126_transition[i] = DFA.unpackEncodedString(DFA126_transitionS[i]);
        }
    }

    class DFA126 extends DFA {

        public DFA126(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 126;
            this.eot = DFA126_eot;
            this.eof = DFA126_eof;
            this.min = DFA126_min;
            this.max = DFA126_max;
            this.accept = DFA126_accept;
            this.special = DFA126_special;
            this.transition = DFA126_transition;
        }
        public String getDescription() {
            return "709:31: ( assignmentOperator expression )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA126_9 = input.LA(1);

                         
                        int index126_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_9);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA126_5 = input.LA(1);

                         
                        int index126_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_5);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA126_2 = input.LA(1);

                         
                        int index126_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA126_11 = input.LA(1);

                         
                        int index126_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA126_6 = input.LA(1);

                         
                        int index126_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA126_1 = input.LA(1);

                         
                        int index126_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_1);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA126_10 = input.LA(1);

                         
                        int index126_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_10);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA126_7 = input.LA(1);

                         
                        int index126_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA126_4 = input.LA(1);

                         
                        int index126_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_4);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA126_8 = input.LA(1);

                         
                        int index126_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_8);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA126_3 = input.LA(1);

                         
                        int index126_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred188_Java()) ) {s = 13;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index126_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 126, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA127_eotS =
        "\17\uffff";
    static final String DFA127_eofS =
        "\17\uffff";
    static final String DFA127_minS =
        "\1\50\12\uffff\2\52\2\uffff";
    static final String DFA127_maxS =
        "\1\141\12\uffff\1\52\1\63\2\uffff";
    static final String DFA127_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\2\uffff\1\13"+
        "\1\14";
    static final String DFA127_specialS =
        "\1\1\13\uffff\1\0\2\uffff}>";
    static final String[] DFA127_transitionS = {
            "\1\12\1\uffff\1\13\10\uffff\1\1\46\uffff\1\2\1\3\1\4\1\5\1"+
            "\6\1\7\1\10\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\14",
            "\1\15\10\uffff\1\16",
            "",
            ""
    };

    static final short[] DFA127_eot = DFA.unpackEncodedString(DFA127_eotS);
    static final short[] DFA127_eof = DFA.unpackEncodedString(DFA127_eofS);
    static final char[] DFA127_min = DFA.unpackEncodedStringToUnsignedChars(DFA127_minS);
    static final char[] DFA127_max = DFA.unpackEncodedStringToUnsignedChars(DFA127_maxS);
    static final short[] DFA127_accept = DFA.unpackEncodedString(DFA127_acceptS);
    static final short[] DFA127_special = DFA.unpackEncodedString(DFA127_specialS);
    static final short[][] DFA127_transition;

    static {
        int numStates = DFA127_transitionS.length;
        DFA127_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA127_transition[i] = DFA.unpackEncodedString(DFA127_transitionS[i]);
        }
    }

    class DFA127 extends DFA {

        public DFA127(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 127;
            this.eot = DFA127_eot;
            this.eof = DFA127_eof;
            this.min = DFA127_min;
            this.max = DFA127_max;
            this.accept = DFA127_accept;
            this.special = DFA127_special;
            this.transition = DFA127_transition;
        }
        public String getDescription() {
            return "712:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | ( '<' '<' '=' )=>t1= '<' t2= '<' t3= '=' {...}? | ( '>' '>' '>' '=' )=>t1= '>' t2= '>' t3= '>' t4= '=' {...}? | ( '>' '>' '=' )=>t1= '>' t2= '>' t3= '=' {...}?);";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA127_12 = input.LA(1);

                         
                        int index127_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA127_12==42) && (synpred199_Java())) {s = 13;}

                        else if ( (LA127_12==51) && (synpred200_Java())) {s = 14;}

                         
                        input.seek(index127_12);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA127_0 = input.LA(1);

                         
                        int index127_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA127_0==51) ) {s = 1;}

                        else if ( (LA127_0==90) ) {s = 2;}

                        else if ( (LA127_0==91) ) {s = 3;}

                        else if ( (LA127_0==92) ) {s = 4;}

                        else if ( (LA127_0==93) ) {s = 5;}

                        else if ( (LA127_0==94) ) {s = 6;}

                        else if ( (LA127_0==95) ) {s = 7;}

                        else if ( (LA127_0==96) ) {s = 8;}

                        else if ( (LA127_0==97) ) {s = 9;}

                        else if ( (LA127_0==40) && (synpred198_Java())) {s = 10;}

                        else if ( (LA127_0==42) ) {s = 11;}

                         
                        input.seek(index127_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 127, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA139_eotS =
        "\30\uffff";
    static final String DFA139_eofS =
        "\30\uffff";
    static final String DFA139_minS =
        "\1\50\1\uffff\1\52\1\4\24\uffff";
    static final String DFA139_maxS =
        "\1\52\1\uffff\1\52\1\161\24\uffff";
    static final String DFA139_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\23\3";
    static final String DFA139_specialS =
        "\1\0\2\uffff\1\1\24\uffff}>";
    static final String[] DFA139_transitionS = {
            "\1\1\1\uffff\1\2",
            "",
            "\1\3",
            "\1\25\1\uffff\1\17\1\20\1\21\3\16\36\uffff\1\4\4\uffff\1\27"+
            "\10\uffff\10\26\1\uffff\1\15\1\13\2\uffff\1\14\1\23\2\22\40"+
            "\uffff\1\5\1\6\2\uffff\1\7\1\10\1\11\1\12\1\24",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA139_eot = DFA.unpackEncodedString(DFA139_eotS);
    static final short[] DFA139_eof = DFA.unpackEncodedString(DFA139_eofS);
    static final char[] DFA139_min = DFA.unpackEncodedStringToUnsignedChars(DFA139_minS);
    static final char[] DFA139_max = DFA.unpackEncodedStringToUnsignedChars(DFA139_maxS);
    static final short[] DFA139_accept = DFA.unpackEncodedString(DFA139_acceptS);
    static final short[] DFA139_special = DFA.unpackEncodedString(DFA139_specialS);
    static final short[][] DFA139_transition;

    static {
        int numStates = DFA139_transitionS.length;
        DFA139_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA139_transition[i] = DFA.unpackEncodedString(DFA139_transitionS[i]);
        }
    }

    class DFA139 extends DFA {

        public DFA139(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 139;
            this.eot = DFA139_eot;
            this.eof = DFA139_eof;
            this.min = DFA139_min;
            this.max = DFA139_max;
            this.accept = DFA139_accept;
            this.special = DFA139_special;
            this.transition = DFA139_transition;
        }
        public String getDescription() {
            return "792:1: shiftOp : ( ( '<' '<' )=>t1= '<' t2= '<' {...}? | ( '>' '>' '>' )=>t1= '>' t2= '>' t3= '>' {...}? | ( '>' '>' )=>t1= '>' t2= '>' {...}?);";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA139_0 = input.LA(1);

                         
                        int index139_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA139_0==40) && (synpred215_Java())) {s = 1;}

                        else if ( (LA139_0==42) ) {s = 2;}

                         
                        input.seek(index139_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA139_3 = input.LA(1);

                         
                        int index139_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA139_3==42) && (synpred216_Java())) {s = 4;}

                        else if ( (LA139_3==105) && (synpred217_Java())) {s = 5;}

                        else if ( (LA139_3==106) && (synpred217_Java())) {s = 6;}

                        else if ( (LA139_3==109) && (synpred217_Java())) {s = 7;}

                        else if ( (LA139_3==110) && (synpred217_Java())) {s = 8;}

                        else if ( (LA139_3==111) && (synpred217_Java())) {s = 9;}

                        else if ( (LA139_3==112) && (synpred217_Java())) {s = 10;}

                        else if ( (LA139_3==66) && (synpred217_Java())) {s = 11;}

                        else if ( (LA139_3==69) && (synpred217_Java())) {s = 12;}

                        else if ( (LA139_3==65) && (synpred217_Java())) {s = 13;}

                        else if ( ((LA139_3>=HexLiteral && LA139_3<=DecimalLiteral)) && (synpred217_Java())) {s = 14;}

                        else if ( (LA139_3==FloatingPointLiteral) && (synpred217_Java())) {s = 15;}

                        else if ( (LA139_3==CharacterLiteral) && (synpred217_Java())) {s = 16;}

                        else if ( (LA139_3==StringLiteral) && (synpred217_Java())) {s = 17;}

                        else if ( ((LA139_3>=71 && LA139_3<=72)) && (synpred217_Java())) {s = 18;}

                        else if ( (LA139_3==70) && (synpred217_Java())) {s = 19;}

                        else if ( (LA139_3==113) && (synpred217_Java())) {s = 20;}

                        else if ( (LA139_3==Identifier) && (synpred217_Java())) {s = 21;}

                        else if ( ((LA139_3>=56 && LA139_3<=63)) && (synpred217_Java())) {s = 22;}

                        else if ( (LA139_3==47) && (synpred217_Java())) {s = 23;}

                         
                        input.seek(index139_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 139, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA145_eotS =
        "\21\uffff";
    static final String DFA145_eofS =
        "\21\uffff";
    static final String DFA145_minS =
        "\1\4\2\uffff\1\0\15\uffff";
    static final String DFA145_maxS =
        "\1\161\2\uffff\1\0\15\uffff";
    static final String DFA145_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\13\uffff\1\3";
    static final String DFA145_specialS =
        "\3\uffff\1\0\15\uffff}>";
    static final String[] DFA145_transitionS = {
            "\1\4\1\uffff\6\4\43\uffff\1\4\10\uffff\10\4\1\uffff\1\4\1\3"+
            "\2\uffff\4\4\46\uffff\1\1\1\2\1\4",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA145_eot = DFA.unpackEncodedString(DFA145_eotS);
    static final short[] DFA145_eof = DFA.unpackEncodedString(DFA145_eofS);
    static final char[] DFA145_min = DFA.unpackEncodedStringToUnsignedChars(DFA145_minS);
    static final char[] DFA145_max = DFA.unpackEncodedStringToUnsignedChars(DFA145_maxS);
    static final short[] DFA145_accept = DFA.unpackEncodedString(DFA145_acceptS);
    static final short[] DFA145_special = DFA.unpackEncodedString(DFA145_specialS);
    static final short[][] DFA145_transition;

    static {
        int numStates = DFA145_transitionS.length;
        DFA145_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA145_transition[i] = DFA.unpackEncodedString(DFA145_transitionS[i]);
        }
    }

    class DFA145 extends DFA {

        public DFA145(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 145;
            this.eot = DFA145_eot;
            this.eof = DFA145_eof;
            this.min = DFA145_min;
            this.max = DFA145_max;
            this.accept = DFA145_accept;
            this.special = DFA145_special;
            this.transition = DFA145_transition;
        }
        public String getDescription() {
            return "823:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA145_3 = input.LA(1);

                         
                        int index145_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred229_Java()) ) {s = 16;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index145_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 145, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA146_eotS =
        "\7\uffff";
    static final String DFA146_eofS =
        "\7\uffff";
    static final String DFA146_minS =
        "\1\4\1\0\1\35\2\uffff\1\61\1\35";
    static final String DFA146_maxS =
        "\1\161\1\0\1\103\2\uffff\1\61\1\103";
    static final String DFA146_acceptS =
        "\3\uffff\1\2\1\1\2\uffff";
    static final String DFA146_specialS =
        "\1\uffff\1\0\5\uffff}>";
    static final String[] DFA146_transitionS = {
            "\1\1\1\uffff\6\3\43\uffff\1\3\10\uffff\10\2\1\uffff\2\3\2\uffff"+
            "\4\3\40\uffff\2\3\2\uffff\5\3",
            "\1\uffff",
            "\1\3\22\uffff\1\5\22\uffff\1\4",
            "",
            "",
            "\1\6",
            "\1\3\22\uffff\1\5\22\uffff\1\4"
    };

    static final short[] DFA146_eot = DFA.unpackEncodedString(DFA146_eotS);
    static final short[] DFA146_eof = DFA.unpackEncodedString(DFA146_eofS);
    static final char[] DFA146_min = DFA.unpackEncodedStringToUnsignedChars(DFA146_minS);
    static final char[] DFA146_max = DFA.unpackEncodedStringToUnsignedChars(DFA146_maxS);
    static final short[] DFA146_accept = DFA.unpackEncodedString(DFA146_acceptS);
    static final short[] DFA146_special = DFA.unpackEncodedString(DFA146_specialS);
    static final short[][] DFA146_transition;

    static {
        int numStates = DFA146_transitionS.length;
        DFA146_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA146_transition[i] = DFA.unpackEncodedString(DFA146_transitionS[i]);
        }
    }

    class DFA146 extends DFA {

        public DFA146(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 146;
            this.eot = DFA146_eot;
            this.eof = DFA146_eof;
            this.min = DFA146_min;
            this.max = DFA146_max;
            this.accept = DFA146_accept;
            this.special = DFA146_special;
            this.transition = DFA146_transition;
        }
        public String getDescription() {
            return "832:12: ( type | expression )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA146_1 = input.LA(1);

                         
                        int index146_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred234_Java()) ) {s = 4;}

                        else if ( (true) ) {s = 3;}

                         
                        input.seek(index146_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 146, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA149_eotS =
        "\41\uffff";
    static final String DFA149_eofS =
        "\1\4\40\uffff";
    static final String DFA149_minS =
        "\1\32\1\0\1\uffff\1\0\35\uffff";
    static final String DFA149_maxS =
        "\1\156\1\0\1\uffff\1\0\35\uffff";
    static final String DFA149_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\34\uffff";
    static final String DFA149_specialS =
        "\1\uffff\1\0\1\uffff\1\1\35\uffff}>";
    static final String[] DFA149_transitionS = {
            "\1\4\2\uffff\1\3\1\4\11\uffff\4\4\1\uffff\1\4\2\uffff\1\1\1"+
            "\4\1\uffff\1\4\14\uffff\1\4\1\uffff\1\2\1\4\7\uffff\1\4\16\uffff"+
            "\25\4",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA149_eot = DFA.unpackEncodedString(DFA149_eotS);
    static final short[] DFA149_eof = DFA.unpackEncodedString(DFA149_eofS);
    static final char[] DFA149_min = DFA.unpackEncodedStringToUnsignedChars(DFA149_minS);
    static final char[] DFA149_max = DFA.unpackEncodedStringToUnsignedChars(DFA149_maxS);
    static final short[] DFA149_accept = DFA.unpackEncodedString(DFA149_acceptS);
    static final short[] DFA149_special = DFA.unpackEncodedString(DFA149_specialS);
    static final short[][] DFA149_transition;

    static {
        int numStates = DFA149_transitionS.length;
        DFA149_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA149_transition[i] = DFA.unpackEncodedString(DFA149_transitionS[i]);
        }
    }

    class DFA149 extends DFA {

        public DFA149(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 149;
            this.eot = DFA149_eot;
            this.eof = DFA149_eof;
            this.min = DFA149_min;
            this.max = DFA149_max;
            this.accept = DFA149_accept;
            this.special = DFA149_special;
            this.transition = DFA149_transition;
        }
        public String getDescription() {
            return "837:34: ( identifierSuffix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA149_1 = input.LA(1);

                         
                        int index149_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred237_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index149_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA149_3 = input.LA(1);

                         
                        int index149_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred237_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index149_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 149, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA151_eotS =
        "\41\uffff";
    static final String DFA151_eofS =
        "\1\4\40\uffff";
    static final String DFA151_minS =
        "\1\32\1\0\1\uffff\1\0\35\uffff";
    static final String DFA151_maxS =
        "\1\156\1\0\1\uffff\1\0\35\uffff";
    static final String DFA151_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\34\uffff";
    static final String DFA151_specialS =
        "\1\uffff\1\0\1\uffff\1\1\35\uffff}>";
    static final String[] DFA151_transitionS = {
            "\1\4\2\uffff\1\3\1\4\11\uffff\4\4\1\uffff\1\4\2\uffff\1\1\1"+
            "\4\1\uffff\1\4\14\uffff\1\4\1\uffff\1\2\1\4\7\uffff\1\4\16\uffff"+
            "\25\4",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA151_eot = DFA.unpackEncodedString(DFA151_eotS);
    static final short[] DFA151_eof = DFA.unpackEncodedString(DFA151_eofS);
    static final char[] DFA151_min = DFA.unpackEncodedStringToUnsignedChars(DFA151_minS);
    static final char[] DFA151_max = DFA.unpackEncodedStringToUnsignedChars(DFA151_maxS);
    static final short[] DFA151_accept = DFA.unpackEncodedString(DFA151_acceptS);
    static final short[] DFA151_special = DFA.unpackEncodedString(DFA151_specialS);
    static final short[][] DFA151_transition;

    static {
        int numStates = DFA151_transitionS.length;
        DFA151_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA151_transition[i] = DFA.unpackEncodedString(DFA151_transitionS[i]);
        }
    }

    class DFA151 extends DFA {

        public DFA151(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 151;
            this.eot = DFA151_eot;
            this.eof = DFA151_eof;
            this.min = DFA151_min;
            this.max = DFA151_max;
            this.accept = DFA151_accept;
            this.special = DFA151_special;
            this.transition = DFA151_transition;
        }
        public String getDescription() {
            return "841:38: ( identifierSuffix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA151_1 = input.LA(1);

                         
                        int index151_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred243_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index151_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA151_3 = input.LA(1);

                         
                        int index151_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred243_Java()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index151_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 151, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA156_eotS =
        "\13\uffff";
    static final String DFA156_eofS =
        "\13\uffff";
    static final String DFA156_minS =
        "\1\35\1\4\1\uffff\1\45\7\uffff";
    static final String DFA156_maxS =
        "\1\102\1\161\1\uffff\1\161\7\uffff";
    static final String DFA156_acceptS =
        "\2\uffff\1\3\1\uffff\1\1\1\2\1\4\1\6\1\7\1\10\1\5";
    static final String DFA156_specialS =
        "\13\uffff}>";
    static final String[] DFA156_transitionS = {
            "\1\3\22\uffff\1\1\21\uffff\1\2",
            "\1\5\1\uffff\6\5\43\uffff\1\5\1\uffff\1\4\6\uffff\10\5\1\uffff"+
            "\2\5\2\uffff\4\5\40\uffff\2\5\2\uffff\5\5",
            "",
            "\1\6\2\uffff\1\12\30\uffff\1\10\3\uffff\1\7\53\uffff\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA156_eot = DFA.unpackEncodedString(DFA156_eotS);
    static final short[] DFA156_eof = DFA.unpackEncodedString(DFA156_eofS);
    static final char[] DFA156_min = DFA.unpackEncodedStringToUnsignedChars(DFA156_minS);
    static final char[] DFA156_max = DFA.unpackEncodedStringToUnsignedChars(DFA156_maxS);
    static final short[] DFA156_accept = DFA.unpackEncodedString(DFA156_acceptS);
    static final short[] DFA156_special = DFA.unpackEncodedString(DFA156_specialS);
    static final short[][] DFA156_transition;

    static {
        int numStates = DFA156_transitionS.length;
        DFA156_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA156_transition[i] = DFA.unpackEncodedString(DFA156_transitionS[i]);
        }
    }

    class DFA156 extends DFA {

        public DFA156(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 156;
            this.eot = DFA156_eot;
            this.eof = DFA156_eof;
            this.min = DFA156_min;
            this.max = DFA156_max;
            this.accept = DFA156_accept;
            this.special = DFA156_special;
            this.transition = DFA156_transition;
        }
        public String getDescription() {
            return "846:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' explicitGenericInvocation | '.' 'this' | '.' 'super' arguments | '.' 'new' innerCreator );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA155_eotS =
        "\41\uffff";
    static final String DFA155_eofS =
        "\1\1\40\uffff";
    static final String DFA155_minS =
        "\1\32\1\uffff\1\0\36\uffff";
    static final String DFA155_maxS =
        "\1\156\1\uffff\1\0\36\uffff";
    static final String DFA155_acceptS =
        "\1\uffff\1\2\36\uffff\1\1";
    static final String DFA155_specialS =
        "\2\uffff\1\0\36\uffff}>";
    static final String[] DFA155_transitionS = {
            "\1\1\2\uffff\2\1\11\uffff\4\1\1\uffff\1\1\2\uffff\1\2\1\1\1"+
            "\uffff\1\1\14\uffff\1\1\2\uffff\1\1\7\uffff\1\1\16\uffff\25"+
            "\1",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA155_eot = DFA.unpackEncodedString(DFA155_eotS);
    static final short[] DFA155_eof = DFA.unpackEncodedString(DFA155_eofS);
    static final char[] DFA155_min = DFA.unpackEncodedStringToUnsignedChars(DFA155_minS);
    static final char[] DFA155_max = DFA.unpackEncodedStringToUnsignedChars(DFA155_maxS);
    static final short[] DFA155_accept = DFA.unpackEncodedString(DFA155_acceptS);
    static final short[] DFA155_special = DFA.unpackEncodedString(DFA155_specialS);
    static final short[][] DFA155_transition;

    static {
        int numStates = DFA155_transitionS.length;
        DFA155_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA155_transition[i] = DFA.unpackEncodedString(DFA155_transitionS[i]);
        }
    }

    class DFA155 extends DFA {

        public DFA155(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 155;
            this.eot = DFA155_eot;
            this.eof = DFA155_eof;
            this.min = DFA155_min;
            this.max = DFA155_max;
            this.accept = DFA155_accept;
            this.special = DFA155_special;
            this.transition = DFA155_transition;
        }
        public String getDescription() {
            return "()+ loopback of 848:9: ( '[' expression ']' )+";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA155_2 = input.LA(1);

                         
                        int index155_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred249_Java()) ) {s = 32;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index155_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 155, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA162_eotS =
        "\41\uffff";
    static final String DFA162_eofS =
        "\1\2\40\uffff";
    static final String DFA162_minS =
        "\1\32\1\0\37\uffff";
    static final String DFA162_maxS =
        "\1\156\1\0\37\uffff";
    static final String DFA162_acceptS =
        "\2\uffff\1\2\35\uffff\1\1";
    static final String DFA162_specialS =
        "\1\uffff\1\0\37\uffff}>";
    static final String[] DFA162_transitionS = {
            "\1\2\2\uffff\2\2\11\uffff\4\2\1\uffff\1\2\2\uffff\1\1\1\2\1"+
            "\uffff\1\2\14\uffff\1\2\2\uffff\1\2\7\uffff\1\2\16\uffff\25"+
            "\2",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA162_eot = DFA.unpackEncodedString(DFA162_eotS);
    static final short[] DFA162_eof = DFA.unpackEncodedString(DFA162_eofS);
    static final char[] DFA162_min = DFA.unpackEncodedStringToUnsignedChars(DFA162_minS);
    static final char[] DFA162_max = DFA.unpackEncodedStringToUnsignedChars(DFA162_maxS);
    static final short[] DFA162_accept = DFA.unpackEncodedString(DFA162_acceptS);
    static final short[] DFA162_special = DFA.unpackEncodedString(DFA162_specialS);
    static final short[][] DFA162_transition;

    static {
        int numStates = DFA162_transitionS.length;
        DFA162_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA162_transition[i] = DFA.unpackEncodedString(DFA162_transitionS[i]);
        }
    }

    class DFA162 extends DFA {

        public DFA162(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 162;
            this.eot = DFA162_eot;
            this.eof = DFA162_eof;
            this.min = DFA162_min;
            this.max = DFA162_max;
            this.accept = DFA162_accept;
            this.special = DFA162_special;
            this.transition = DFA162_transition;
        }
        public String getDescription() {
            return "()* loopback of 874:28: ( '[' expression ']' )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA162_1 = input.LA(1);

                         
                        int index162_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred262_Java()) ) {s = 32;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index162_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 162, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_annotations_in_compilationUnit44 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_compilationUnit58 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_compilationUnit60 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit63 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_compilationUnit78 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit80 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_compilationUnit101 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_compilationUnit104 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit107 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_25_in_packageDeclaration127 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_packageDeclaration129 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_packageDeclaration131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_importDeclaration154 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_28_in_importDeclaration156 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_importDeclaration159 = new BitSet(new long[]{0x0000000024000000L});
    public static final BitSet FOLLOW_29_in_importDeclaration162 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_importDeclaration164 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_importDeclaration168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_typeDeclaration201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceModifiers_in_classOrInterfaceDeclaration224 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_classDeclaration_in_classOrInterfaceDeclaration227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceModifier_in_classOrInterfaceModifiers255 = new BitSet(new long[]{0x0000001F90000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotation_in_classOrInterfaceModifier275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_classOrInterfaceModifier288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_classOrInterfaceModifier303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_classOrInterfaceModifier315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_classOrInterfaceModifier329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_classOrInterfaceModifier342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_classOrInterfaceModifier357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_classOrInterfaceModifier373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifier_in_modifiers395 = new BitSet(new long[]{0x00F0001F90000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_normalClassDeclaration_in_classDeclaration415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumDeclaration_in_classDeclaration425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_normalClassDeclaration448 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_normalClassDeclaration450 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_typeParameters_in_normalClassDeclaration452 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_38_in_normalClassDeclaration464 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_normalClassDeclaration466 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_39_in_normalClassDeclaration479 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_normalClassDeclaration481 = new BitSet(new long[]{0x000011C000000000L});
    public static final BitSet FOLLOW_classBody_in_normalClassDeclaration493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_typeParameters516 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_typeParameter_in_typeParameters518 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_41_in_typeParameters521 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_typeParameter_in_typeParameters523 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_42_in_typeParameters527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_typeParameter546 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_38_in_typeParameter549 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeBound_in_typeParameter551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeBound580 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_43_in_typeBound583 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeBound585 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_ENUM_in_enumDeclaration606 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enumDeclaration608 = new BitSet(new long[]{0x0000108000000000L});
    public static final BitSet FOLLOW_39_in_enumDeclaration611 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_enumDeclaration613 = new BitSet(new long[]{0x0000108000000000L});
    public static final BitSet FOLLOW_enumBody_in_enumDeclaration617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_enumBody636 = new BitSet(new long[]{0x0000220004000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_enumConstants_in_enumBody638 = new BitSet(new long[]{0x0000220004000000L});
    public static final BitSet FOLLOW_41_in_enumBody641 = new BitSet(new long[]{0x0000200004000000L});
    public static final BitSet FOLLOW_enumBodyDeclarations_in_enumBody644 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_enumBody647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumConstant_in_enumConstants666 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_enumConstants669 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_enumConstant_in_enumConstants671 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_annotations_in_enumConstant696 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enumConstant699 = new BitSet(new long[]{0x000011C000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_enumConstant701 = new BitSet(new long[]{0x000011C000000002L});
    public static final BitSet FOLLOW_classBody_in_enumConstant704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_enumBodyDeclarations728 = new BitSet(new long[]{0x00F0101F94000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_classBodyDeclaration_in_enumBodyDeclarations731 = new BitSet(new long[]{0x00F0101F94000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_normalInterfaceDeclaration789 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_normalInterfaceDeclaration791 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_typeParameters_in_normalInterfaceDeclaration793 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_38_in_normalInterfaceDeclaration797 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_normalInterfaceDeclaration799 = new BitSet(new long[]{0x0000114000000000L});
    public static final BitSet FOLLOW_interfaceBody_in_normalInterfaceDeclaration803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeList826 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_typeList829 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeList831 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_44_in_classBody856 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_classBodyDeclaration_in_classBody858 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_classBody861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_interfaceBody884 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_interfaceBodyDeclaration_in_interfaceBody886 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_interfaceBody889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_classBodyDeclaration908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_classBodyDeclaration918 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_classBodyDeclaration921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_classBodyDeclaration931 = new BitSet(new long[]{0xFF00C13F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_memberDecl_in_classBodyDeclaration933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_genericMethodOrConstructorDecl_in_memberDecl956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_memberDeclaration_in_memberDecl966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_memberDecl976 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_memberDecl978 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_voidMethodDeclaratorRest_in_memberDecl980 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_memberDecl990 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_constructorDeclaratorRest_in_memberDecl992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_memberDecl1002 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classDeclaration_in_memberDecl1012 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_memberDeclaration1035 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_methodDeclaration_in_memberDeclaration1038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldDeclaration_in_memberDeclaration1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeParameters_in_genericMethodOrConstructorDecl1062 = new BitSet(new long[]{0xFF00800000000010L});
    public static final BitSet FOLLOW_genericMethodOrConstructorRest_in_genericMethodOrConstructorDecl1064 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_genericMethodOrConstructorRest1088 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_47_in_genericMethodOrConstructorRest1092 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_genericMethodOrConstructorRest1095 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_methodDeclaratorRest_in_genericMethodOrConstructorRest1097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_genericMethodOrConstructorRest1107 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_constructorDeclaratorRest_in_genericMethodOrConstructorRest1109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_methodDeclaration1128 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_methodDeclaratorRest_in_methodDeclaration1130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarators_in_fieldDeclaration1149 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_fieldDeclaration1151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_interfaceBodyDeclaration1178 = new BitSet(new long[]{0xFF00C13F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_interfaceMemberDecl_in_interfaceBodyDeclaration1180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_interfaceBodyDeclaration1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceMethodOrFieldDecl_in_interfaceMemberDecl1209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceGenericMethodDecl_in_interfaceMemberDecl1219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_interfaceMemberDecl1229 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceMemberDecl1231 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_voidInterfaceMethodDeclaratorRest_in_interfaceMemberDecl1233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceDeclaration_in_interfaceMemberDecl1243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classDeclaration_in_interfaceMemberDecl1253 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_interfaceMethodOrFieldDecl1276 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceMethodOrFieldDecl1278 = new BitSet(new long[]{0x0009000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_interfaceMethodOrFieldRest_in_interfaceMethodOrFieldDecl1280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constantDeclaratorsRest_in_interfaceMethodOrFieldRest1303 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_interfaceMethodOrFieldRest1305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_interfaceMethodDeclaratorRest_in_interfaceMethodOrFieldRest1315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_methodDeclaratorRest1338 = new BitSet(new long[]{0x0005100014000000L});
    public static final BitSet FOLLOW_48_in_methodDeclaratorRest1341 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_methodDeclaratorRest1343 = new BitSet(new long[]{0x0005100014000000L});
    public static final BitSet FOLLOW_50_in_methodDeclaratorRest1356 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaratorRest1358 = new BitSet(new long[]{0x0000100014000000L});
    public static final BitSet FOLLOW_methodBody_in_methodDeclaratorRest1374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_methodDeclaratorRest1388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_voidMethodDeclaratorRest1421 = new BitSet(new long[]{0x0004100014000000L});
    public static final BitSet FOLLOW_50_in_voidMethodDeclaratorRest1424 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_voidMethodDeclaratorRest1426 = new BitSet(new long[]{0x0000100014000000L});
    public static final BitSet FOLLOW_methodBody_in_voidMethodDeclaratorRest1442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_voidMethodDeclaratorRest1456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_interfaceMethodDeclaratorRest1489 = new BitSet(new long[]{0x0005000004000000L});
    public static final BitSet FOLLOW_48_in_interfaceMethodDeclaratorRest1492 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_interfaceMethodDeclaratorRest1494 = new BitSet(new long[]{0x0005000004000000L});
    public static final BitSet FOLLOW_50_in_interfaceMethodDeclaratorRest1499 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_interfaceMethodDeclaratorRest1501 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_interfaceMethodDeclaratorRest1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_typeParameters_in_interfaceGenericMethodDecl1528 = new BitSet(new long[]{0xFF00800000000010L});
    public static final BitSet FOLLOW_type_in_interfaceGenericMethodDecl1531 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_47_in_interfaceGenericMethodDecl1535 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_interfaceGenericMethodDecl1538 = new BitSet(new long[]{0x0009000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_interfaceMethodDeclaratorRest_in_interfaceGenericMethodDecl1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_voidInterfaceMethodDeclaratorRest1571 = new BitSet(new long[]{0x0004000004000000L});
    public static final BitSet FOLLOW_50_in_voidInterfaceMethodDeclaratorRest1574 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_voidInterfaceMethodDeclaratorRest1576 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_voidInterfaceMethodDeclaratorRest1580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formalParameters_in_constructorDeclaratorRest1603 = new BitSet(new long[]{0x0004100000000000L});
    public static final BitSet FOLLOW_50_in_constructorDeclaratorRest1606 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedNameList_in_constructorDeclaratorRest1608 = new BitSet(new long[]{0x0004100000000000L});
    public static final BitSet FOLLOW_constructorBody_in_constructorDeclaratorRest1612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_constantDeclarator1631 = new BitSet(new long[]{0x0009000000000000L});
    public static final BitSet FOLLOW_constantDeclaratorRest_in_constantDeclarator1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarator_in_variableDeclarators1656 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_variableDeclarators1659 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclarator_in_variableDeclarators1661 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_variableDeclarator1682 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_51_in_variableDeclarator1685 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_variableDeclarator1687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constantDeclaratorRest_in_constantDeclaratorsRest1712 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_constantDeclaratorsRest1715 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_constantDeclarator_in_constantDeclaratorsRest1717 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_48_in_constantDeclaratorRest1739 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_constantDeclaratorRest1741 = new BitSet(new long[]{0x0009000000000000L});
    public static final BitSet FOLLOW_51_in_constantDeclaratorRest1745 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_constantDeclaratorRest1747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_variableDeclaratorId1770 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_variableDeclaratorId1773 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_variableDeclaratorId1775 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arrayInitializer_in_variableInitializer1796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_variableInitializer1806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_arrayInitializer1833 = new BitSet(new long[]{0xFF00B00000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer1836 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_arrayInitializer1839 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer1841 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_arrayInitializer1846 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_arrayInitializer1853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_modifier1872 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_modifier1882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_modifier1892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_modifier1902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_modifier1912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_modifier1922 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_modifier1932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_modifier1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_modifier1952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_modifier1962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_modifier1972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_modifier1982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_packageOrTypeName2001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_enumConstantName2020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_typeName2039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceType_in_type2053 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_type2056 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_type2058 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_type2065 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_type2068 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_type2070 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_Identifier_in_classOrInterfaceType2083 = new BitSet(new long[]{0x0000010020000002L});
    public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2085 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_classOrInterfaceType2089 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_classOrInterfaceType2091 = new BitSet(new long[]{0x0000010020000002L});
    public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2093 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_set_in_primitiveType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_variableModifier2202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_variableModifier2212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_typeArguments2231 = new BitSet(new long[]{0xFF00000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_typeArgument_in_typeArguments2233 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_41_in_typeArguments2236 = new BitSet(new long[]{0xFF00000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_typeArgument_in_typeArguments2238 = new BitSet(new long[]{0x0000060000000000L});
    public static final BitSet FOLLOW_42_in_typeArguments2242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_typeArgument2265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_64_in_typeArgument2275 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_typeArgument2278 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_typeArgument2286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList2311 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_qualifiedNameList2314 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList2316 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_66_in_formalParameters2337 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000208L});
    public static final BitSet FOLLOW_formalParameterDecls_in_formalParameters2339 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_formalParameters2342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_formalParameterDecls2365 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_formalParameterDecls2367 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000010L});
    public static final BitSet FOLLOW_formalParameterDeclsRest_in_formalParameterDecls2369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2392 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_formalParameterDeclsRest2395 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_formalParameterDecls_in_formalParameterDeclsRest2397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_68_in_formalParameterDeclsRest2409 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameterDeclsRest2411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_methodBody2434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_constructorBody2453 = new BitSet(new long[]{0xFF20F13F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_explicitConstructorInvocation_in_constructorBody2455 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_blockStatement_in_constructorBody2458 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_45_in_constructorBody2461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2480 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_explicitConstructorInvocation2483 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation2491 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_explicitConstructorInvocation2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_explicitConstructorInvocation2503 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_explicitConstructorInvocation2505 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation2507 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_explicitConstructorInvocation2510 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation2512 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_explicitConstructorInvocation2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName2534 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_qualifiedName2537 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName2539 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_integerLiteral_in_literal2565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FloatingPointLiteral_in_literal2575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CharacterLiteral_in_literal2585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_literal2595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal2605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_70_in_literal2615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_integerLiteral0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_booleanLiteral0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_annotations2704 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_73_in_annotation2724 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_annotationName_in_annotation2726 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_annotation2730 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003EEL});
    public static final BitSet FOLLOW_elementValuePairs_in_annotation2734 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_elementValue_in_annotation2738 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_annotation2743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_annotationName2767 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_29_in_annotationName2770 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_annotationName2772 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs2793 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_elementValuePairs2796 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs2798 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_Identifier_in_elementValuePair2819 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_elementValuePair2821 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValuePair2823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_elementValue2846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_elementValue2856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elementValueArrayInitializer_in_elementValue2866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_elementValueArrayInitializer2889 = new BitSet(new long[]{0xFF00B20000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer2892 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_elementValueArrayInitializer2895 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer2897 = new BitSet(new long[]{0x0000220000000000L});
    public static final BitSet FOLLOW_41_in_elementValueArrayInitializer2904 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_elementValueArrayInitializer2908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_73_in_annotationTypeDeclaration2931 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_46_in_annotationTypeDeclaration2933 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_annotationTypeDeclaration2935 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_annotationTypeBody_in_annotationTypeDeclaration2937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_annotationTypeBody2960 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody2963 = new BitSet(new long[]{0x00F0301F94000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_45_in_annotationTypeBody2967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_modifiers_in_annotationTypeElementDeclaration2990 = new BitSet(new long[]{0xFF00403F92000030L,0x0000000000000200L});
    public static final BitSet FOLLOW_annotationTypeElementRest_in_annotationTypeElementDeclaration2992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_annotationTypeElementRest3015 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_annotationMethodOrConstantRest_in_annotationTypeElementRest3017 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalClassDeclaration_in_annotationTypeElementRest3029 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementRest3042 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumDeclaration_in_annotationTypeElementRest3055 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationTypeDeclaration_in_annotationTypeElementRest3068 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_annotationTypeElementRest3070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationMethodRest_in_annotationMethodOrConstantRest3094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotationConstantRest_in_annotationMethodOrConstantRest3104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_annotationMethodRest3127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_annotationMethodRest3129 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_annotationMethodRest3131 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_defaultValue_in_annotationMethodRest3133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableDeclarators_in_annotationConstantRest3157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_defaultValue3180 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_elementValue_in_defaultValue3182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_block3203 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_blockStatement_in_block3205 = new BitSet(new long[]{0xFF20F03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_45_in_block3208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclarationStatement_in_blockStatement3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_blockStatement3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_blockStatement3251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement3275 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_localVariableDeclarationStatement3277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_localVariableDeclaration3296 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_localVariableDeclaration3298 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclarators_in_localVariableDeclaration3300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifier_in_variableModifiers3323 = new BitSet(new long[]{0x0000000800000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_block_in_statement3341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSERT_in_statement3351 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3353 = new BitSet(new long[]{0x0000000004000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_statement3356 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3358 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_statement3372 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3374 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3376 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_statement3386 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_statement3400 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_statement3402 = new BitSet(new long[]{0xFF00900804000FD0L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_forControl_in_statement3404 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_statement3406 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_79_in_statement3418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3420 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_statement3432 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3434 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_79_in_statement3436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3438 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_statement3450 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3452 = new BitSet(new long[]{0x0000000000000000L,0x0000000001040000L});
    public static final BitSet FOLLOW_catches_in_statement3464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_statement3466 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_statement3480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_82_in_statement3494 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_83_in_statement3516 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3518 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_44_in_statement3520 = new BitSet(new long[]{0x0000200000000000L,0x0000000002000400L});
    public static final BitSet FOLLOW_switchBlockStatementGroups_in_statement3522 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_45_in_statement3524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_statement3534 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_parExpression_in_statement3536 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_statement3538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_84_in_statement3548 = new BitSet(new long[]{0xFF00900004000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3550 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_85_in_statement3563 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_statement3565 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_statement3577 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_Identifier_in_statement3579 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_87_in_statement3592 = new BitSet(new long[]{0x0000000004000010L});
    public static final BitSet FOLLOW_Identifier_in_statement3594 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_statement3607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statementExpression_in_statement3618 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_statement3620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_statement3630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_statement3632 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_statement3634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catchClause_in_catches3657 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_catchClause_in_catches3660 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_catchClause3685 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_catchClause3687 = new BitSet(new long[]{0xFF00000800000010L,0x0000000000000200L});
    public static final BitSet FOLLOW_formalParameter_in_catchClause3689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_catchClause3691 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_catchClause3693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_formalParameter3712 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_formalParameter3714 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_variableDeclaratorId_in_formalParameter3716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups3744 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000400L});
    public static final BitSet FOLLOW_switchLabel_in_switchBlockStatementGroup3771 = new BitSet(new long[]{0xFF20D03F96001FF2L,0x0003E60002FBD7E6L});
    public static final BitSet FOLLOW_blockStatement_in_switchBlockStatementGroup3774 = new BitSet(new long[]{0xFF20D03F96001FF2L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_89_in_switchLabel3798 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_constantExpression_in_switchLabel3800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_switchLabel3812 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_enumConstantName_in_switchLabel3814 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_74_in_switchLabel3826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_switchLabel3828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enhancedForControl_in_forControl3859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forInit_in_forControl3869 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_forControl3872 = new BitSet(new long[]{0xFF00900004000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_forControl3874 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_forControl3877 = new BitSet(new long[]{0xFF00900800000FD2L,0x0003E600000003E6L});
    public static final BitSet FOLLOW_forUpdate_in_forControl3879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_forInit3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionList_in_forInit3909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableModifiers_in_enhancedForControl3932 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_enhancedForControl3934 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_enhancedForControl3936 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_enhancedForControl3938 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_enhancedForControl3940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionList_in_forUpdate3959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_parExpression3980 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_parExpression3982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_parExpression3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionList4007 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_41_in_expressionList4010 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_expressionList4012 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_expression_in_statementExpression4033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_constantExpression4056 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_expression4079 = new BitSet(new long[]{0x0008050000000002L,0x00000003FC000000L});
    public static final BitSet FOLLOW_assignmentOperator_in_expression4082 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_expression4084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_90_in_assignmentOperator4119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_91_in_assignmentOperator4129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_92_in_assignmentOperator4139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_assignmentOperator4149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_94_in_assignmentOperator4159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_95_in_assignmentOperator4169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_96_in_assignmentOperator4179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_97_in_assignmentOperator4189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_assignmentOperator4210 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_assignmentOperator4214 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4252 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4256 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4260 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4295 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_assignmentOperator4299 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_assignmentOperator4303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalOrExpression_in_conditionalExpression4332 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_conditionalExpression4336 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_conditionalExpression4338 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_conditionalExpression4340 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_conditionalExpression4342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression4364 = new BitSet(new long[]{0x0000000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_98_in_conditionalOrExpression4368 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression4370 = new BitSet(new long[]{0x0000000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4392 = new BitSet(new long[]{0x0000000000000002L,0x0000000800000000L});
    public static final BitSet FOLLOW_99_in_conditionalAndExpression4396 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression4398 = new BitSet(new long[]{0x0000000000000002L,0x0000000800000000L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4420 = new BitSet(new long[]{0x0000000000000002L,0x0000001000000000L});
    public static final BitSet FOLLOW_100_in_inclusiveOrExpression4424 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression4426 = new BitSet(new long[]{0x0000000000000002L,0x0000001000000000L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression4448 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
    public static final BitSet FOLLOW_101_in_exclusiveOrExpression4452 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression4454 = new BitSet(new long[]{0x0000000000000002L,0x0000002000000000L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression4476 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_43_in_andExpression4480 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_equalityExpression_in_andExpression4482 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression4504 = new BitSet(new long[]{0x0000000000000002L,0x000000C000000000L});
    public static final BitSet FOLLOW_set_in_equalityExpression4508 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression4516 = new BitSet(new long[]{0x0000000000000002L,0x000000C000000000L});
    public static final BitSet FOLLOW_relationalExpression_in_instanceOfExpression4538 = new BitSet(new long[]{0x0000000000000002L,0x0000010000000000L});
    public static final BitSet FOLLOW_104_in_instanceOfExpression4541 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_type_in_instanceOfExpression4543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression4564 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_relationalOp_in_relationalExpression4568 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression4570 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_40_in_relationalOp4605 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_relationalOp4609 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_relationalOp4639 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_relationalOp4643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_relationalOp4664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_relationalOp4675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression4695 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_shiftOp_in_shiftExpression4699 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression4701 = new BitSet(new long[]{0x0000050000000002L});
    public static final BitSet FOLLOW_40_in_shiftOp4732 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_shiftOp4736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_shiftOp4768 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4772 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_shiftOp4806 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_shiftOp4810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression4840 = new BitSet(new long[]{0x0000000000000002L,0x0000060000000000L});
    public static final BitSet FOLLOW_set_in_additiveExpression4844 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression4852 = new BitSet(new long[]{0x0000000000000002L,0x0000060000000000L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression4874 = new BitSet(new long[]{0x0000000040000002L,0x0000180000000000L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpression4878 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression4892 = new BitSet(new long[]{0x0000000040000002L,0x0000180000000000L});
    public static final BitSet FOLLOW_105_in_unaryExpression4918 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_106_in_unaryExpression4930 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_109_in_unaryExpression4942 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_110_in_unaryExpression4954 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression4956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression4966 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_111_in_unaryExpressionNotPlusMinus4985 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_112_in_unaryExpressionNotPlusMinus4997 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus4999 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_unaryExpressionNotPlusMinus5009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_unaryExpressionNotPlusMinus5019 = new BitSet(new long[]{0x0001000020000002L,0x0000600000000000L});
    public static final BitSet FOLLOW_selector_in_unaryExpressionNotPlusMinus5021 = new BitSet(new long[]{0x0001000020000002L,0x0000600000000000L});
    public static final BitSet FOLLOW_set_in_unaryExpressionNotPlusMinus5024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_castExpression5047 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_primitiveType_in_castExpression5049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_castExpression5051 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_castExpression5053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_castExpression5062 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_type_in_castExpression5065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_expression_in_castExpression5069 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_castExpression5072 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_castExpression5074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_parExpression_in_primary5093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_69_in_primary5103 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_29_in_primary5106 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary5108 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary5112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_primary5123 = new BitSet(new long[]{0x0000000020000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_superSuffix_in_primary5125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_primary5135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_113_in_primary5145 = new BitSet(new long[]{0xFF00010000000010L});
    public static final BitSet FOLLOW_creator_in_primary5147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_primary5157 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_29_in_primary5160 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_primary5162 = new BitSet(new long[]{0x0001000020000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_identifierSuffix_in_primary5166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_primary5177 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_48_in_primary5180 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_primary5182 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_29_in_primary5186 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_primary5188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_primary5198 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_primary5200 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_primary5202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_identifierSuffix5222 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_identifierSuffix5224 = new BitSet(new long[]{0x0001000020000000L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5228 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_identifierSuffix5230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_identifierSuffix5241 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_identifierSuffix5243 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_identifierSuffix5245 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arguments_in_identifierSuffix5258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5268 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_identifierSuffix5270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5280 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_explicitGenericInvocation_in_identifierSuffix5282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5292 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_identifierSuffix5294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_identifierSuffix5306 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_identifierSuffix5308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_identifierSuffix5318 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
    public static final BitSet FOLLOW_113_in_identifierSuffix5320 = new BitSet(new long[]{0x0000010000000010L});
    public static final BitSet FOLLOW_innerCreator_in_identifierSuffix5322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_creator5341 = new BitSet(new long[]{0xFF00010000000010L});
    public static final BitSet FOLLOW_createdName_in_creator5343 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_classCreatorRest_in_creator5345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createdName_in_creator5355 = new BitSet(new long[]{0x0001000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arrayCreatorRest_in_creator5358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classCreatorRest_in_creator5362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceType_in_createdName5382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primitiveType_in_createdName5392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_innerCreator5415 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_innerCreator5418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_classCreatorRest_in_innerCreator5420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5439 = new BitSet(new long[]{0xFF02900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5453 = new BitSet(new long[]{0x0001100000000000L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5456 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5458 = new BitSet(new long[]{0x0001100000000000L});
    public static final BitSet FOLLOW_arrayInitializer_in_arrayCreatorRest5462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_arrayCreatorRest5476 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5478 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5481 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_arrayCreatorRest5483 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5485 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_arrayCreatorRest5490 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_arrayCreatorRest5492 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_arguments_in_classCreatorRest5523 = new BitSet(new long[]{0x000011C000000002L});
    public static final BitSet FOLLOW_classBody_in_classCreatorRest5525 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitGenericInvocation5549 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_explicitGenericInvocation5551 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_explicitGenericInvocation5553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_nonWildcardTypeArguments5576 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_typeList_in_nonWildcardTypeArguments5578 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_nonWildcardTypeArguments5580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5603 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_selector5605 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_selector5607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5618 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_selector5620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_selector5632 = new BitSet(new long[]{0x0000000020000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_superSuffix_in_selector5634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_selector5644 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
    public static final BitSet FOLLOW_113_in_selector5646 = new BitSet(new long[]{0x0000010000000010L});
    public static final BitSet FOLLOW_innerCreator_in_selector5648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_selector5658 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_selector5660 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_selector5662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arguments_in_superSuffix5685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_superSuffix5695 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_superSuffix5697 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_superSuffix5699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_arguments5719 = new BitSet(new long[]{0xFF00900800000FD0L,0x0003E600000003EEL});
    public static final BitSet FOLLOW_expressionList_in_arguments5721 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_arguments5724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotations_in_synpred5_Java44 = new BitSet(new long[]{0x0000403F92000020L,0x0000000000000200L});
    public static final BitSet FOLLOW_packageDeclaration_in_synpred5_Java58 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_importDeclaration_in_synpred5_Java60 = new BitSet(new long[]{0x0000403F9E000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_synpred5_Java63 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred5_Java78 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_typeDeclaration_in_synpred5_Java80 = new BitSet(new long[]{0x0000403F96000022L,0x0000000000000200L});
    public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred113_Java2455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred117_Java2480 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000022L});
    public static final BitSet FOLLOW_set_in_synpred117_Java2483 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_arguments_in_synpred117_Java2491 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_synpred117_Java2493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_annotation_in_synpred128_Java2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclarationStatement_in_synpred151_Java3231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred152_Java3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_77_in_synpred157_Java3386 = new BitSet(new long[]{0xFF20D03F96001FF0L,0x0003E60000FBD3E6L});
    public static final BitSet FOLLOW_statement_in_synpred157_Java3388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_synpred162_Java3464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_synpred162_Java3466 = new BitSet(new long[]{0x0000100010000000L});
    public static final BitSet FOLLOW_block_in_synpred162_Java3468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_catches_in_synpred163_Java3480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchLabel_in_synpred178_Java3771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_synpred180_Java3798 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_constantExpression_in_synpred180_Java3800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_synpred180_Java3802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_synpred181_Java3812 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_enumConstantName_in_synpred181_Java3814 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_synpred181_Java3816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enhancedForControl_in_synpred182_Java3859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_localVariableDeclaration_in_synpred186_Java3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignmentOperator_in_synpred188_Java4082 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_synpred188_Java4084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred198_Java4200 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_synpred198_Java4202 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred198_Java4204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4240 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4242 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred199_Java4244 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred199_Java4246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred200_Java4285 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred200_Java4287 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred200_Java4289 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred211_Java4597 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred211_Java4599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred212_Java4631 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_51_in_synpred212_Java4633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_synpred215_Java4724 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_40_in_synpred215_Java4726 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4758 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4760 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred216_Java4762 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_synpred217_Java4798 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_synpred217_Java4800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpression_in_synpred229_Java5009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_66_in_synpred233_Java5047 = new BitSet(new long[]{0xFF00000000000010L});
    public static final BitSet FOLLOW_primitiveType_in_synpred233_Java5049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_synpred233_Java5051 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_unaryExpression_in_synpred233_Java5053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_synpred234_Java5065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_synpred236_Java5106 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_synpred236_Java5108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifierSuffix_in_synpred237_Java5112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_synpred242_Java5160 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_synpred242_Java5162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_identifierSuffix_in_synpred243_Java5166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_synpred249_Java5241 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_synpred249_Java5243 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_synpred249_Java5245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_synpred262_Java5481 = new BitSet(new long[]{0xFF00900000000FD0L,0x0003E600000001E6L});
    public static final BitSet FOLLOW_expression_in_synpred262_Java5483 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_synpred262_Java5485 = new BitSet(new long[]{0x0000000000000002L});

}