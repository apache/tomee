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
package org.apache.openjpa.persistence.dynamicschema;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * Entity using reserved words for column names 
 * 
 * @see sql-keywords.rscs
 * @author Tim McConnell
 * @since 2.0.0
 */
@Entity
public class EntityReservedWords implements Serializable {

    @Id
    private int id;
    private Integer add;
    private int application;
    private BigDecimal begin;
    private BigInteger bigint;
    private Calendar calendar;
    private String character;
    private Integer conditional;
    private Date date;
    private BigDecimal decimal;
    private Time distinct;
    private String exception;
    private int each;
    private String from;
    private Integer file;
    private String grant;
    private BigDecimal global;
    private String hour;
    private String holdlock;
    private BigInteger integer;
    private int index;
    private BigInteger join;
    private String jar;
    private Calendar key;
    private Timestamp kill;
    private Integer like;
    private BigDecimal loop;
    private int minute;
    private Date merge;
    private String number;
    private Integer not;
    private Date outer;
    private String on;
    private BigInteger primary;
    private int purge;
    private Integer quiesce;
    private String quit;
    private BigDecimal restrict;
    private Time rename;
    private String select;
    private Integer savepoint;
    private Time time;
    private Timestamp timestamp;
    private Calendar trigger;
    private int update;
    private String until;
    private String varchar;
    private Integer variable;
    private Timestamp wait;
    private BigDecimal where;
    private BigInteger xml;
    private int year;
    private Integer years;
    private Date zerofill;
    private String zone; 
    private Integer type;
    private String alias;
    private int Boolean;

    public EntityReservedWords() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getAdd() {
        return add;
    }
    public void setAdd(int add) {
        this.add = add;
    }

    public int getApplication() {
        return application;
    }
    public void setApplication(int application) {
        this.application = application;
    }

    public BigDecimal getBegin() {
        return begin;
    }
    public void setBegin(BigDecimal begin) {
        this.begin = begin;
    }

    public BigInteger getBigint() {
        return bigint;
    }
    public void setBigint(BigInteger bigint) {
        this.bigint = bigint;
    }

    public Calendar getCalendar() {
        return calendar;
    }
    public void setCalendar(int Calendar) {
        this.calendar = calendar;
    }

    public String getCharacter() {
        return character;
    }
    public void setCharacter(String character) {
        this.character = character;
    }

    public int getConditional() {
        return conditional;
    }
    public void setConditional(int conditional) {
        this.conditional = conditional;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getDecimal() {
        return decimal;
    }
    public void setDecimal(BigDecimal decimal) {
        this.decimal = decimal;
    }

    public Time getDistinct() {
        return distinct;
    }
    public void setDistinct(Time distinct) {
        this.distinct = distinct;
    }

    public String getException() {
        return exception;
    }
    public void setException(String exception) {
        this.exception = exception;
    }

    public int getEach() {
        return each;
    }
    public void setEach(int each) {
        this.each = each;
    }

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public int getFile() {
        return file;
    }
    public void setFile(int file) {
        this.file = file;
    }

    public String getGrant() {
        return grant;
    }
    public void setGrant(String grant) {
        this.grant = grant;
    }

    public BigDecimal getGlobal() {
        return global;
    }
    public void setGlobal(BigDecimal global) {
        this.global = global;
    }

    public String getHour() {
        return hour;
    }
    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getHoldlock() {
        return holdlock;
    }
    public void setHoldlock(String holdlock) {
        this.holdlock = holdlock;
    }

    public BigInteger getInteger() {
        return integer;
    }
    public void setInteger(BigInteger integer) {
        this.integer = integer;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public BigInteger getJoin() {
        return join;
    }
    public void setJoin(BigInteger join) {
        this.join = join;
    }

    public String getJar() {
        return jar;
    }
    public void setJar(String jar) {
        this.jar = jar;
    }

    public Calendar getKey() {
        return key;
    }
    public void setKey(Calendar key) {
        this.key = key;
    }

    public Timestamp getKill() {
        return kill;
    }
    public void setKill(Timestamp kill) {
        this.kill = kill;
    }

    public Integer getLike() {
        return like;
    }
    public void setLike(Integer like) {
        this.like = like;
    }

    public BigDecimal getLoop() {
        return loop;
    }
    public void setLoop(BigDecimal loop) {
        this.loop = loop;
    }

    public int getMinute() {
        return minute;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Date getMerge() {
        return merge;
    }
    public void setMerge(Date merge) {
        this.merge = merge;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public int getNot() {
        return not;
    }
    public void setNot(int not) {
        this.not = not;
    }

    public Date getOuter() {
        return outer;
    }
    public void setOuter(Date outer) {
        this.outer = outer;
    }

    public String getOn() {
        return on;
    }
    public void setOn(String on) {
        this.on = on;
    }

    public BigInteger getPrimary() {
        return primary;
    }
    public void setPrimary(BigInteger primary) {
        this.primary = primary;
    }

    public int getPurge() {
        return purge;
    }
    public void setPurge(int purge) {
        this.purge = purge;
    }

    public int getQuiesce() {
        return quiesce;
    }
    public void setQuiesce(int quiesce) {
        this.quiesce = quiesce;
    }

    public String getQuit() {
        return quit;
    }
    public void setQuit(String quit) {
        this.quit = quit;
    }

    public BigDecimal getRestrict() {
        return restrict;
    }
    public void setRestrict(BigDecimal restrict) {
        this.restrict = restrict;
    }

    public Time getRename() {
        return rename;
    }
    public void setRename(Time rename) {
        this.rename = rename;
    }

    public String getSelect() {
        return select;
    }
    public void setSelect(String select) {
        this.select = select;
    }

    public int getSavepoint() {
        return savepoint;
    }
    public void setSavepoint(int savepoint) {
        this.savepoint = savepoint;
    }

    public Time getTime() {
        return time;
    }
    public void setTime(Time time) {
        this.time = time;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Calendar getTrigger() {
        return trigger;
    }
    public void settrigger(Calendar trigger) {
        this.trigger = trigger;
    }

    public int getUpdate() {
        return update;
    }
    public void setUpdate(int update) {
        this.update = update;
    }

    public String getUntil() {
        return until;
    }
    public void setUntil(String until) {
        this.until = until;
    }

    public String getVarchar() {
        return varchar;
    }
    public void setVarchar(String varchar) {
        this.varchar = varchar;
    }

    public int getVariable() {
        return variable;
    }
    public void setVariable(int variable) {
        this.variable = variable;
    }

    public Timestamp getWait() {
        return wait;
    }
    public void setWait(Timestamp wait) {
        this.wait = wait;
    }

    public BigDecimal getWhere() {
        return where;
    }
    public void setwHere(BigDecimal where) {
        this.where = where;
    }

    public BigInteger getXml() {
        return xml;
    }
    public void setxml(BigInteger xml) {
        this.xml = xml;
    }

    public int getYear() {
        return year;
    }
    public void setyear(int year) {
        this.year = year;
    }

    public int getYears() {
        return years;
    }
    public void setyears(int years) {
        this.years = years;
    }

    public Date getZerofill() {
        return zerofill;
    }
    public void setZerofill(Date zerofill) {
        this.zerofill = zerofill;
    }

    public String getZone() {
        return zone;
    }
    public void setZone(String zone) {
        this.zone = zone;
    } 

    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getBoolean() {
        return Boolean;
    }
    public void setBoolean(int Boolean) {
        this.Boolean = Boolean;
    }
}
