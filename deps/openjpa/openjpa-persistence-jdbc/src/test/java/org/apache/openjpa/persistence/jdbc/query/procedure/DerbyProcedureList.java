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
package org.apache.openjpa.persistence.jdbc.query.procedure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/*
 * holds the stored procedures that will be used by test cases
 */
public class DerbyProcedureList extends AbstractProcedureList {

    public List<String> getCreateProcedureList () {
        ArrayList<String> retList = new ArrayList<String>();

        retList.add("create procedure ADD_X_TO_CHARLIE () "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA MODIFIES SQL DATA "
            + "EXTERNAL NAME '" + DerbyProcedureList.class.getName()
            + ".addXToCharlie'");
        retList.add("create procedure ADD_SUFFIX_TO_NAME (NAME VARCHAR(128), "
            + "SUFFIX VARCHAR(128)) "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA MODIFIES SQL DATA "
            + "EXTERNAL NAME '" + DerbyProcedureList.class.getName()
            + ".addSuffixToName'");
        retList.add("create procedure GET_ALL_APPLICANTS () "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA READS SQL DATA DYNAMIC "
            + "RESULT SETS 1 " + "EXTERNAL NAME '"
            + DerbyProcedureList.class.getName() + ".getAllApplicants'");
        retList.add("create procedure GET_TWO_APPLICANTS (NAME VARCHAR(128), "
            + "SUFFIX VARCHAR(128)) "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA READS SQL DATA DYNAMIC "
            + "RESULT SETS 1 " + "EXTERNAL NAME '"
            + DerbyProcedureList.class.getName() + ".getTwoApplicants'");
        retList.add("create procedure GET_ALL_APPLICANTS_AND_GAMES () "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA READS SQL DATA DYNAMIC "
            + "RESULT SETS 2 " + "EXTERNAL NAME '"
            + DerbyProcedureList.class.getName()
            + ".getAllApplicantsAndGames'");
        retList.add("create procedure GET_TWO_APPLICANTS_AND_GAMES "
            + "(NAME VARCHAR(128), SUFFIX VARCHAR(128)) "
            + "PARAMETER STYLE JAVA LANGUAGE JAVA READS SQL DATA DYNAMIC "
            + "RESULT SETS 2 " + "EXTERNAL NAME '"
            + DerbyProcedureList.class.getName()
            + ".getTwoApplicantsAndGames'");

        return retList;
    }

    public List<String> getDropProcedureList () {
        ArrayList<String> retList = new ArrayList<String>();

        retList.add ("drop procedure ADD_X_TO_CHARLIE");
        retList.add ("drop procedure ADD_SUFFIX_TO_NAME");
        retList.add ("drop procedure GET_ALL_APPLICANTS");
        retList.add ("drop procedure GET_TWO_APPLICANTS");
        retList.add ("drop procedure GET_ALL_APPLICANTS_AND_GAMES");
        retList.add ("drop procedure GET_TWO_APPLICANTS_AND_GAMES");

        return retList;
    }

    public String callAddXToCharlie () {
        return "{ call ADD_X_TO_CHARLIE () }";
    }

    public static void addXToCharlie() throws Exception {
        Connection conn =
            DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement ps1 =
            conn
                .prepareStatement("update APPLICANT set name = 'Charliex' " 
                    + "where name = 'Charlie'");
        ps1.executeUpdate();

        conn.close();
    }

    public String callAddSuffixToName () {
        return "{ call ADD_SUFFIX_TO_NAME (?, ?) }";
    }

    public static void addSuffixToName(String name, String suffix)
        throws Exception {
        Connection conn =
            DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement ps1 =
            conn.prepareStatement("update APPLICANT set name = ? "
                + "where name = ?");
        ps1.setString(1, name + suffix);
        ps1.setString(2, name);
        ps1.executeUpdate();

        conn.close();
    }

    public String callGetAllApplicants () {
        return "{ call GET_ALL_APPLICANTS () }";
    }

    public static void getAllApplicants(ResultSet[] rs1) throws Exception {
        Connection conn =
            DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement ps1 =
            conn.prepareStatement("select * from APPLICANT");
        rs1[0] = ps1.executeQuery();

        conn.close();
    }

    public String callGetTwoApplicants () {
        return "{ call GET_TWO_APPLICANTS (?, ?) }";
    }

    public static void getTwoApplicants(String name1, String name2,
        ResultSet[] rs1) throws Exception {
        Connection conn =
            DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement ps1 =
            conn.prepareStatement("select * from APPLICANT where name = ? "
                + "or name = ?");
        ps1.setString(1, name1);
        ps1.setString(2, name2);
        rs1[0] = ps1.executeQuery();

        conn.close();
    }

    public String callGetAllApplicantsAndGames () {
        return "{ call GET_ALL_APPLICANTS_AND_GAMES () }";
    }

    public static void getAllApplicantsAndGames(ResultSet[] rs1, 
        ResultSet[] rs2)
        throws Exception {
        Connection conn =
            DriverManager.getConnection("jdbc:default:connection");
        PreparedStatement ps1 =
            conn.prepareStatement("select * from APPLICANT");
        rs1[0] = ps1.executeQuery();

        PreparedStatement ps2 = conn.prepareStatement("select * from GAME");
        rs2[0] = ps2.executeQuery();

        conn.close();
    }

//    public String callGetTwoApplicantsAndGames () {
//        return "{ call GET_TWO_APPLICANTS_AND_GAMES (?, ?) }";
//    }
//
//    public static void getTwoApplicantsAndGames(String name1, String name2,
//        ResultSet[] rs1, ResultSet[] rs2) throws Exception {
//        Connection conn =
//            DriverManager.getConnection("jdbc:default:connection");
//        PreparedStatement ps1 =
//            conn.prepareStatement("select * from APPLICANT where name = ?");
//        ps1.setString(1, name1);
//        rs1[0] = ps1.executeQuery();
//
//        PreparedStatement ps2 =
//            conn.prepareStatement("select * from GAME where name = ?");
//        ps2.setString(2, name2);
//        rs2[0] = ps2.executeQuery();
//
//        conn.close();
//    }
}
