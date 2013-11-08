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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.io.*;
import java.util.*;

import javax.persistence.Entity;

import org.apache.commons.collections.*;

/**
 * Normal class that has relations to and from various horizontal
 * relations.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
@Entity
public class HorizRelation
    implements Serializable {
    ///////////////////////
    // One to one mappings
    ///////////////////////

    private HorizA obHorizA;
    private HorizB obHorizB;
    private HorizC obHorizC;
    private HorizD obHorizD;
    private HorizE obHorizE;
    private HorizF obHorizF;
    private HorizG obHorizG;
    private HorizH obHorizH;
    private HorizI obHorizI;
    private HorizJ obHorizJ;
    private HorizK obHorizK;
    private HorizL obHorizL;
    private HorizM obHorizM;
    private HorizN obHorizN;
    private HorizO obHorizO;
    private HorizAppSingleA obHorizAppSingleA;
    private HorizAppSingleB obHorizAppSingleB;
    private HorizAppSingleC obHorizAppSingleC;
    private HorizAppSingleD obHorizAppSingleD;
    private HorizAppMultiA obHorizAppMultiA;
    private HorizAppMultiB obHorizAppMultiB;
    private HorizAppMultiC obHorizAppMultiC;
    private HorizAppMultiD obHorizAppMultiD;
    private HorizInterFlatA obHorizInterFlatA;
    private HorizInterFlatB obHorizInterFlatB;
    private HorizInterFlatC obHorizInterFlatC;
    private HorizInterFlatD obHorizInterFlatD;
    private HorizInterVerticalA obHorizInterVerticalA;
    private HorizInterVerticalB obHorizInterVerticalB;
    private HorizInterVerticalC obHorizInterVerticalC;
    private HorizInterVerticalD obHorizInterVerticalD;

    ////////////////////////
    // One to many mappings
    ////////////////////////

    private HashSet cHorizA = new HashSet();
    private LinkedList cHorizB = new LinkedList();
    private ArrayList cHorizC = new ArrayList();
    private Vector cHorizD = new Vector();
    private HashSet cHorizE = new HashSet();
    private LinkedList cHorizF = new LinkedList();
    private ArrayList cHorizG = new ArrayList();
    private Vector cHorizH = new Vector();
    private HashSet cHorizI = new HashSet();
    private LinkedList cHorizJ = new LinkedList();
    private ArrayList cHorizK = new ArrayList();
    private Vector cHorizL = new Vector();
    private HashSet cHorizM = new HashSet();
    private LinkedList cHorizN = new LinkedList();
    private ArrayList cHorizO = new ArrayList();
    private Vector cHorizAppSingleA = new Vector();
    private HashSet cHorizAppSingleB = new HashSet();
    private LinkedList cHorizAppSingleC = new LinkedList();
    private ArrayList cHorizAppSingleD = new ArrayList();
    private Vector cHorizAppMultiA = new Vector();
    private HashSet cHorizAppMultiB = new HashSet();
    private LinkedList cHorizAppMultiC = new LinkedList();
    private ArrayList cHorizAppMultiD = new ArrayList();
    private Vector cHorizInterFlatA = new Vector();
    private HashSet cHorizInterFlatB = new HashSet();
    private LinkedList cHorizInterFlatC = new LinkedList();
    private ArrayList cHorizInterFlatD = new ArrayList();
    private Vector cHorizInterVerticalA = new Vector();
    private HashSet cHorizInterVerticalB = new HashSet();
    private LinkedList cHorizInterVerticalC = new LinkedList();
    private ArrayList cHorizInterVerticalD = new ArrayList();

    public Map getCollections() {
        Map map = new SequencedHashMap();
        map.put("HorizA", cHorizA);
        map.put("HorizB", cHorizB);
        map.put("HorizC", cHorizC);
        map.put("HorizD", cHorizD);
        map.put("HorizE", cHorizE);
        map.put("HorizF", cHorizF);
        map.put("HorizG", cHorizG);
        map.put("HorizH", cHorizH);
        map.put("HorizI", cHorizI);
        map.put("HorizJ", cHorizJ);
        map.put("HorizK", cHorizK);
        map.put("HorizL", cHorizL);
        map.put("HorizM", cHorizM);
        map.put("HorizN", cHorizN);
        map.put("HorizO", cHorizO);
        map.put("HorizAppSingleA", cHorizAppSingleA);
        map.put("HorizAppSingleB", cHorizAppSingleB);
        map.put("HorizAppSingleC", cHorizAppSingleC);
        map.put("HorizAppSingleD", cHorizAppSingleD);
        map.put("HorizAppMultiA", cHorizAppMultiA);
        map.put("HorizAppMultiB", cHorizAppMultiB);
        map.put("HorizAppMultiC", cHorizAppMultiC);
        map.put("HorizAppMultiD", cHorizAppMultiD);
        map.put("HorizInterFlatA", cHorizInterFlatA);
        map.put("HorizInterFlatB", cHorizInterFlatB);
        map.put("HorizInterFlatC", cHorizInterFlatC);
        map.put("HorizInterFlatD", cHorizInterFlatD);
        map.put("HorizInterVerticalA", cHorizInterVerticalA);
        map.put("HorizInterVerticalB", cHorizInterVerticalB);
        map.put("HorizInterVerticalC", cHorizInterVerticalC);
        map.put("HorizInterVerticalD", cHorizInterVerticalD);

        return map;
    }

    public void setObHorizA(HorizA obHorizA) {
        this.obHorizA = obHorizA;
    }

    public HorizA getObHorizA() {
        return this.obHorizA;
    }

    public void setObHorizB(HorizB obHorizB) {
        this.obHorizB = obHorizB;
    }

    public HorizB getObHorizB() {
        return this.obHorizB;
    }

    public void setObHorizC(HorizC obHorizC) {
        this.obHorizC = obHorizC;
    }

    public HorizC getObHorizC() {
        return this.obHorizC;
    }

    public void setObHorizD(HorizD obHorizD) {
        this.obHorizD = obHorizD;
    }

    public HorizD getObHorizD() {
        return this.obHorizD;
    }

    public void setObHorizE(HorizE obHorizE) {
        this.obHorizE = obHorizE;
    }

    public HorizE getObHorizE() {
        return this.obHorizE;
    }

    public void setObHorizF(HorizF obHorizF) {
        this.obHorizF = obHorizF;
    }

    public HorizF getObHorizF() {
        return this.obHorizF;
    }

    public void setObHorizG(HorizG obHorizG) {
        this.obHorizG = obHorizG;
    }

    public HorizG getObHorizG() {
        return this.obHorizG;
    }

    public void setObHorizH(HorizH obHorizH) {
        this.obHorizH = obHorizH;
    }

    public HorizH getObHorizH() {
        return this.obHorizH;
    }

    public void setObHorizI(HorizI obHorizI) {
        this.obHorizI = obHorizI;
    }

    public HorizI getObHorizI() {
        return this.obHorizI;
    }

    public void setObHorizJ(HorizJ obHorizJ) {
        this.obHorizJ = obHorizJ;
    }

    public HorizJ getObHorizJ() {
        return this.obHorizJ;
    }

    public void setObHorizK(HorizK obHorizK) {
        this.obHorizK = obHorizK;
    }

    public HorizK getObHorizK() {
        return this.obHorizK;
    }

    public void setObHorizL(HorizL obHorizL) {
        this.obHorizL = obHorizL;
    }

    public HorizL getObHorizL() {
        return this.obHorizL;
    }

    public void setObHorizM(HorizM obHorizM) {
        this.obHorizM = obHorizM;
    }

    public HorizM getObHorizM() {
        return this.obHorizM;
    }

    public void setObHorizN(HorizN obHorizN) {
        this.obHorizN = obHorizN;
    }

    public HorizN getObHorizN() {
        return this.obHorizN;
    }

    public void setObHorizO(HorizO obHorizO) {
        this.obHorizO = obHorizO;
    }

    public HorizO getObHorizO() {
        return this.obHorizO;
    }

    public void setObHorizAppSingleA(HorizAppSingleA obHorizAppSingleA) {
        this.obHorizAppSingleA = obHorizAppSingleA;
    }

    public HorizAppSingleA getObHorizAppSingleA() {
        return this.obHorizAppSingleA;
    }

    public void setObHorizAppSingleB(HorizAppSingleB obHorizAppSingleB) {
        this.obHorizAppSingleB = obHorizAppSingleB;
    }

    public HorizAppSingleB getObHorizAppSingleB() {
        return this.obHorizAppSingleB;
    }

    public void setObHorizAppSingleC(HorizAppSingleC obHorizAppSingleC) {
        this.obHorizAppSingleC = obHorizAppSingleC;
    }

    public HorizAppSingleC getObHorizAppSingleC() {
        return this.obHorizAppSingleC;
    }

    public void setObHorizAppSingleD(HorizAppSingleD obHorizAppSingleD) {
        this.obHorizAppSingleD = obHorizAppSingleD;
    }

    public HorizAppSingleD getObHorizAppSingleD() {
        return this.obHorizAppSingleD;
    }

    public void setObHorizAppMultiA(HorizAppMultiA obHorizAppMultiA) {
        this.obHorizAppMultiA = obHorizAppMultiA;
    }

    public HorizAppMultiA getObHorizAppMultiA() {
        return this.obHorizAppMultiA;
    }

    public void setObHorizAppMultiB(HorizAppMultiB obHorizAppMultiB) {
        this.obHorizAppMultiB = obHorizAppMultiB;
    }

    public HorizAppMultiB getObHorizAppMultiB() {
        return this.obHorizAppMultiB;
    }

    public void setObHorizAppMultiC(HorizAppMultiC obHorizAppMultiC) {
        this.obHorizAppMultiC = obHorizAppMultiC;
    }

    public HorizAppMultiC getObHorizAppMultiC() {
        return this.obHorizAppMultiC;
    }

    public void setObHorizAppMultiD(HorizAppMultiD obHorizAppMultiD) {
        this.obHorizAppMultiD = obHorizAppMultiD;
    }

    public HorizAppMultiD getObHorizAppMultiD() {
        return this.obHorizAppMultiD;
    }

    public void setObHorizInterFlatA(HorizInterFlatA obHorizInterFlatA) {
        this.obHorizInterFlatA = obHorizInterFlatA;
    }

    public HorizInterFlatA getObHorizInterFlatA() {
        return this.obHorizInterFlatA;
    }

    public void setObHorizInterFlatB(HorizInterFlatB obHorizInterFlatB) {
        this.obHorizInterFlatB = obHorizInterFlatB;
    }

    public HorizInterFlatB getObHorizInterFlatB() {
        return this.obHorizInterFlatB;
    }

    public void setObHorizInterFlatC(HorizInterFlatC obHorizInterFlatC) {
        this.obHorizInterFlatC = obHorizInterFlatC;
    }

    public HorizInterFlatC getObHorizInterFlatC() {
        return this.obHorizInterFlatC;
    }

    public void setObHorizInterFlatD(HorizInterFlatD obHorizInterFlatD) {
        this.obHorizInterFlatD = obHorizInterFlatD;
    }

    public HorizInterFlatD getObHorizInterFlatD() {
        return this.obHorizInterFlatD;
    }

    public void setObHorizInterVerticalA
        (HorizInterVerticalA obHorizInterVerticalA) {
        this.obHorizInterVerticalA = obHorizInterVerticalA;
    }

    public HorizInterVerticalA getObHorizInterVerticalA() {
        return this.obHorizInterVerticalA;
    }

    public void setObHorizInterVerticalB
        (HorizInterVerticalB obHorizInterVerticalB) {
        this.obHorizInterVerticalB = obHorizInterVerticalB;
    }

    public HorizInterVerticalB getObHorizInterVerticalB() {
        return this.obHorizInterVerticalB;
    }

    public void setObHorizInterVerticalC
        (HorizInterVerticalC obHorizInterVerticalC) {
        this.obHorizInterVerticalC = obHorizInterVerticalC;
    }

    public HorizInterVerticalC getObHorizInterVerticalC() {
        return this.obHorizInterVerticalC;
    }

    public void setObHorizInterVerticalD
        (HorizInterVerticalD obHorizInterVerticalD) {
        this.obHorizInterVerticalD = obHorizInterVerticalD;
    }

    public HorizInterVerticalD getObHorizInterVerticalD() {
        return this.obHorizInterVerticalD;
    }

    public HashSet getCHorizInterVerticalB() {
        return this.cHorizInterVerticalB;
    }

    public void setCHorizInterVerticalB(HashSet cHorizInterVerticalB) {
        this.cHorizInterVerticalB = cHorizInterVerticalB;
    }
}

