-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.    

DELETE FROM TORDERXML t0
SELECT t0.countryCode, t0.id, t0.version, t0.city, t0.state, t0.street, t0.zip, t0.name FROM TCUSTOMER t0 
DELETE FROM TCUSTOMER WHERE countryCode = ? AND id = ? AND version = ?
DELETE FROM TCUSTOMER WHERE countryCode = ? AND id = ? AND version = ?
INSERT INTO TORDERXML (oid, amount, delivered, shipAddress, version, customer_countryCode, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?)
INSERT INTO TCUSTOMER (countryCode, id, creditRating, name, version, city, state, street, zip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
INSERT INTO TCUSTOMER (countryCode, id, creditRating, name, version, city, state, street, zip) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
INSERT INTO TORDERXML (oid, amount, delivered, shipAddress, version, customer_countryCode, customer_id) VALUES (?, ?, ?, ?, ?, ?, ?)
SELECT t0.shipAddress FROM TORDERXML t0 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 
SELECT t0.version, t0.countryCode, t0.id, t0.city, t0.state, t0.street, t0.zip, t0.name FROM TCUSTOMER t0 WHERE t0.countryCode = ? AND t0.id = ?  optimize for 1 row
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 JOIN TORDERXML t1 ON (1 = 1) WHERE (XMLEXISTS('$t0.shipAddress/*[City = $t1.shipAddress/*/City]' PASSING t0.shipAddress AS "t0.shipAddress", t1.shipAddress AS "t1.shipAddress")) 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 JOIN TCUSTOMER t1 ON (1 = 1) WHERE (XMLEXISTS('$t0.shipAddress/*[City = $t1.city]' PASSING t0.shipAddress AS "t0.shipAddress", t1.city AS "t1.city")) 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 WHERE (XMLEXISTS('$t0.shipAddress/*[City = $Parm]' PASSING t0.shipAddress AS "t0.shipAddress", CAST(? AS VARCHAR(254)) AS "Parm")) 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 WHERE (XMLEXISTS('$t0.shipAddress/*[City = $Parm]' PASSING t0.shipAddress AS "t0.shipAddress", CAST(? AS VARCHAR(254)) AS "Parm")) 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 WHERE (XMLEXISTS('$t0.shipAddress/*[City = $Parm]' PASSING t0.shipAddress AS "t0.shipAddress", CAST(? AS VARCHAR(254)) AS "Parm")) 
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 WHERE (XMLEXISTS('$t0.shipAddress/*[City = $Parm]' PASSING t0.shipAddress AS "t0.shipAddress", CAST(? AS VARCHAR(254)) AS "Parm")) 
SELECT t0.version, t0.countryCode, t0.id, t0.city, t0.state, t0.street, t0.zip, t0.name FROM TCUSTOMER t0 WHERE t0.countryCode = ? AND t0.id = ?  optimize for 1 row
UPDATE TORDERXML SET shipAddress = ?, version = ? WHERE oid = ? AND version = ?
SELECT t0.oid, t0.version, t0.amount, t0.customer_countryCode, t0.customer_id, t0.delivered, t0.shipAddress FROM TORDERXML t0 
SELECT t0.version, t0.countryCode, t0.id, t0.city, t0.state, t0.street, t0.zip, t0.name FROM TCUSTOMER t0 WHERE t0.countryCode = ? AND t0.id = ?  optimize for 1 row
