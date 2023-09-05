/*
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */


package ee.jakarta.tck.ws.rs.lib.implementation.sun.jersey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ee.jakarta.tck.ws.rs.lib.deliverable.PropertyManagerInterface;

import ee.jakarta.tck.ws.rs.lib.util.TestUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This is a utility class that borrowed from Kyle Grucci's original work in
 * TSRuntimeConfiguration.java. It is called to replace any configurable
 * properties in the web xml files with the implementation specific values
 * specified in ts.jte. A copy is made of each web xml file (with the
 * substituted values) in the same location with file extension .new.
 *
 * @author Dianne Jiao
 */
public class TSWebConfiguration {

  // whether running with s1as or j2sdkee RI
  private static Boolean runningS1AS;

  public final static String WEB_XML = "web.xml.template";

  private File tempFile;

  private PrintWriter log;

  private static Hashtable htReplacementProps = new Hashtable();

  private Hashtable htReplacerTable = new Hashtable();

  private static String sTempDir = "";

  private StringReplacer replacer = new StringReplacer();

  private static File jteFile = new File(System.getProperty("TS_HOME")
      + File.separator + "bin" + File.separator + "ts.jte");

  private static String servlet_adaptor = "servlet_adaptor";

  private static String implementation_name = "jaxrs_impl_name";

  private PropertyManagerInterface propMgr;

  static Properties props;

  static List<String> props_name = Arrays.asList("servlet_adaptor");

  public TSWebConfiguration() {
    System.out.println("Dummy TSWebConfiguration Conctructor");
  }

  public static void main(String[] args) {

    TSWebConfiguration webconfig = new TSWebConfiguration();

    implementation_name = webconfig.findProps(implementation_name);
    System.out.println("++++++++++++=" + implementation_name);

    servlet_adaptor = webconfig.findProps(servlet_adaptor);
    servlet_adaptor = servlet_adaptor.replace(".class", "").replace("/", ".");
    System.out
        .println("Replacement value for servlet_adaptor =" + servlet_adaptor);

    PrintWriter logOut = new PrintWriter(System.out, true);

    File fileList = new File(System.getProperty("TS_HOME") + File.separator
        + "bin" + File.separator + "jaxrs_filelist");

    try {
      BufferedReader input = new BufferedReader(new FileReader(fileList));

      try {
        String line = null; // not declared within while loop
        while ((line = input.readLine()) != null) {
          System.out.println("Processing file " + line);
          File file = new File(
              System.getProperty("TS_HOME") + File.separator + line);
          try {
            htReplacementProps.put("servlet_adaptor", servlet_adaptor);
            webconfig.sweepwebFile(file);
            System.out.println("Done with file " + file.toString());
          } catch (Exception e) {
            e.printStackTrace();
            System.out
                .println("Failed to modify xml files with correct settings.  "
                    + "Please check the values of");
          }
        }
      } finally {
        input.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

  String findProps(String prop) {
    System.out.println("Processing jte file");
    String replace = null;

    try {
      BufferedReader input = new BufferedReader(new FileReader(jteFile));

      try {
        String line = null;
        while ((line = input.readLine()) != null) {
          if (line.startsWith(prop)) {
            replace = line.split("=")[1];
            return replace;
          }
        }
      } finally {
        input.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /// I am here now
  public String sweepwebFile(File file)
      throws FileNotFoundException, IOException {
    if (htReplacerTable != null && !(htReplacerTable.isEmpty())) {
      htReplacerTable = replaceOnWebInfoStrings(htReplacerTable);
    }
    String dir = file.getParent();
    File xml = new File(dir + File.separator + file.getName());

    return replacer.replace(file, htReplacementProps, htReplacerTable, dir);
  }

  final class StringReplacer {

    public String sFindString; // string we are searching for

    public String sReplaceString; // replace sFindString with this

    public String sDirString;

    public String sFileNameStringToReplace;

    public String sNewFileNameString;

    public int iHowMany = -1;

    public String[] sFileList;

    public boolean bNewFile = false;

    private Hashtable htFindAndReplace = null;

    private Hashtable htCustomTable = null;

    private Vector vInfoObjects = new Vector();

    private Vector vMatchingInfoObjects = new Vector();

    private Vector vNonMatchingInfoObjects = new Vector();

    private boolean bSomethingWasReplaced = false;

    public String replace(File file, Hashtable htStrings,
        Hashtable htReplacerTable, String sTempDir) {
      String[] sFileList = null;
      htFindAndReplace = htStrings;
      htCustomTable = htReplacerTable;
      String sTemp = "";
      ReplacementInfo ri = null;
      String sNewFileName = "";
      StringBuffer sFoundBuffer = new StringBuffer();
      char c;
      bSomethingWasReplaced = false;
      BufferedReader fReader = null;
      BufferedWriter fNewWriter = null;
      try {
        vInfoObjects = new Vector();
        String sKey = "";
        // create ReplacementInfo objects
        for (Enumeration e = htFindAndReplace.keys(); e.hasMoreElements();) {
          sKey = (String) e.nextElement();
          vInfoObjects.addElement(
              new ReplacementInfo(sKey, (String) htFindAndReplace.get(sKey)));
        }
        // add in the table the custom table of replacement strings
        // if any
        if (htCustomTable != null) {
          for (Enumeration e = htCustomTable.keys(); e.hasMoreElements();) {
            sKey = (String) e.nextElement();
            vInfoObjects.addElement(
                new ReplacementInfo(sKey, (String) htCustomTable.get(sKey)));

          }
        }
        // reader of the file that we're searching
        fReader = new BufferedReader(new FileReader(file));
        System.out.println("File to read: " + file.getAbsolutePath());
        StringWriter sWriter = new StringWriter();
        // stores new file contents

        // The following code is here to account for the fact that we
        // have some properties to replace which are substrings of other
        // properties. We need to be sure that we will be able to
        // replace either. Thus, we check the char after certain props
        // to see if they continue to match another property. If so,
        // then we will replace with the longer property value
        boolean bCheckForDot = false;
        String sHold = "";
        int iCharRead; // holds each char that we read
        vMatchingInfoObjects.addAll(vInfoObjects);
        while ((iCharRead = fReader.read()) != -1) {
          c = (char) iCharRead;

          if (bCheckForDot) {
            if (c != '.') {
              // hold the dot and let the old processing happen
              sHold = new String((new Character(c)).toString());
              System.out.println("sHold = " + sHold);
            } else {
              sFoundBuffer.append(c);
              bCheckForDot = false;
              continue;
              // since there is no need to just check .
            }
          } else {
            if (sHold != null) {
              // just write the char that we were holding. This
              // assumes that this char is not the beginning of a
              // string we want to replace
              sWriter.write(sHold);
              sHold = null;
            }

            sFoundBuffer.append(c);
          }

          sTemp = new String(sFoundBuffer);

          // get all ris that still match
          for (Enumeration e = vMatchingInfoObjects.elements(); e
              .hasMoreElements();) {

            ri = (ReplacementInfo) e.nextElement();
            if (ri.sFind.startsWith(sTemp)) {
              if (ri.sFind.equals(sTemp)) {

                // what if we have a substring of another string that
                // we are searching for?

                System.out.println(
                    "REPLACER:MATCH found:  " + ri.sFind + " matches " + sTemp);
                ri.foundOccurance();
                bSomethingWasReplaced = true;
                sWriter.write(ri.sReplace);
                // reset sFoundBuffer
                sFoundBuffer = new StringBuffer();
                // reset matchers to all again
                vMatchingInfoObjects.removeAllElements();
                vMatchingInfoObjects.addAll(vInfoObjects);
                break;
              }
            } else {
              vNonMatchingInfoObjects.addElement(ri);
              // the char that we just read causes our string not
              // to match the string we're looking for. Write the
              // old string to the new file and reset sFoundBuffer.
              // this will never happen!!!
            }
          }
          // remove all non-matching ri's
          vMatchingInfoObjects.removeAll(vNonMatchingInfoObjects);
          vNonMatchingInfoObjects.removeAllElements();
          // reset everything if there are none left matching
          if (vMatchingInfoObjects.isEmpty()) {
            sWriter.write(sTemp);
            sFoundBuffer = new StringBuffer();
            vMatchingInfoObjects.removeAllElements();
            vMatchingInfoObjects.addAll(vInfoObjects);
          }
        }
        fReader.close();
        if (bSomethingWasReplaced) {
          sNewFileName = sTempDir + File.separator + "web.xml."
              + implementation_name;
          System.out.println("New filename:" + sNewFileName);
          fNewWriter = new BufferedWriter(
              new FileWriter(new File(sNewFileName)));
          fNewWriter.write(sWriter.toString());
          fNewWriter.flush();
        } else {
          sNewFileName = null;
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (fNewWriter != null) {
          try {
            fNewWriter.close();
          } catch (IOException exp) {
          }
        }
      }
      return sNewFileName;
    }

    class ReplacementInfo {

      private int iFoundOccurances = 0;

      private String sFind = null;

      private String sReplace = null;

      ReplacementInfo(String sFindString, String sReplaceString) {
        sFind = sFindString;
        sReplace = sReplaceString;
        iFoundOccurances = 0;
      }

      public void foundOccurance() {
        iFoundOccurances++;
        if (TestUtil.harnessDebug) {
          System.out.println("we found an occurance #" + iFoundOccurances
              + " of '" + sFind + "'");
        }
      }
    }
  }

  /**
   * Parse the HashTable which contains strings retrieved via calls into the
   * porting classes for deployment. We must substitute on these strings prior
   * to substituting them into the web.xml.
   */
  private Hashtable replaceOnWebInfoStrings(Hashtable extras) {

    boolean changeIt = false;

    String searchFor[] = { "servlet_adaptor" };

    for (Enumeration e = extras.keys(); e.hasMoreElements();) {
      String sKey = (String) e.nextElement();
      String val = (String) extras.get(sKey);
      String oldJndi = val;
      changeIt = false;
      String buff = null;
      int startPos = 0;
      for (int i = 0; i < searchFor.length; i++) {
        System.out.println("\n###Searching for=" + searchFor[i]);
        if ((startPos = val.lastIndexOf(searchFor[i])) != -1) {
          changeIt = true;
          String startBuff = val.substring(0, startPos);
          buff = (String) htReplacementProps.get(searchFor[i]);
          val = startBuff + buff
              + val.substring(startPos + searchFor[i].length());
        }
      }
      if (changeIt) {
        extras.put((String) sKey, val);
        System.out.println(
            "\n###\nold webInfo Val=" + oldJndi + "\nNew webInfo Val = " + val);
      }
    }
    return extras;
  }
}
