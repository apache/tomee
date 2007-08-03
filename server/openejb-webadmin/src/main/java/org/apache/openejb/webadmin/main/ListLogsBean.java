/** 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 * 
 * $Id: ListLogsBean.java 445536 2005-07-09 08:51:00Z dblevins $
 */
package org.apache.openejb.webadmin.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.WebAdminBean;
import org.apache.openejb.webadmin.HttpHome;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Stateless;
import javax.ejb.RemoteHome;

/** This bean lists the openejb.log and transaction.log files
 *
 * @author  <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
@Stateless(name = "Webadmin/ListLogs")
@RemoteHome(HttpHome.class)
public class ListLogsBean extends WebAdminBean {
    /** the form field used for a regular expression search */
    private static final String REGULAR_EXPRESSION_SEARCH = "regularExpression";
    private static final String DISPLAY_TYPE_FILTER = "filter";
    private static final String DISPLAY_TYPE_HIGHLIGHT = "highlight";
    private static final String DISPLAY_TYPE = "displayType";

    /** the type of log we're using */
    private String logType;
    /** called with the bean is created */
    public void ejbCreate() {
        this.section = "ListLogs";
    }

    /** after the processing is completed
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** before the processing is done
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {
        //get the log type
        this.logType = request.getQueryParameter("log");
    }

    /** Write the main content
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writeBody(PrintWriter body) throws IOException {
        //string for re search
        String regularExpressionSearch = request.getFormParameter(REGULAR_EXPRESSION_SEARCH);
        String displayType = request.getFormParameter(DISPLAY_TYPE);
        if (regularExpressionSearch == null) {
            regularExpressionSearch = "";
        }

        // Get the logs directory
        File logsDir = SystemInstance.get().getBase().getDirectory("logs");
        String path;

        File[] openejbLogs = logsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.indexOf(".log") != -1);
            }
        });

        Arrays.sort(openejbLogs);
        int printIndex = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");

        for (int i = 0; i < openejbLogs.length; i++) {
            if ((this.logType == null && i == 0)
                || (this.logType != null && this.logType.equals(openejbLogs[i].getName()))) {
                body.print(openejbLogs[i].getName());
                printIndex = i;
            } else {
                body.print("<a href=\"ListLogs?log=" + openejbLogs[i].getName() + "\">" + openejbLogs[i].getName() + "</a>");
            }

            if (i < openejbLogs.length - 1) {
                body.print("&nbsp;|&nbsp;");
            }
        }
        body.println("<br><br>");

        //calculate the size of the file in kb or bytes
        String fileLength = "0 bytes";
        long longFileLength = 0;
        if (openejbLogs[printIndex].length() > 0) {
            if (openejbLogs[printIndex].length() > 1000) {
                longFileLength = openejbLogs[printIndex].length() / 1000;
                fileLength = String.valueOf(longFileLength) + " kb";
            } else {
                fileLength = String.valueOf(openejbLogs[printIndex].length()) + " bytes";
            }
        }

        //set the path for the form action
        if (request.getURI().getQuery() == null || "".equals(request.getURI().getQuery())) {
            path = request.getURI().getPath();
        } else {
            path = request.getURI().getPath() + "?" + request.getURI().getQuery();
        }

        body.print("<form action=\"");
        body.print(path.substring(1));
        body.println("\" method=\"post\">");
        body.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"525\">");
        body.println("<tr>\n<td valign=\"top\">");
        body.println("----------------------------------------------------------<br>");
        body.println("Last Modified: " + dateFormat.format(new Date(openejbLogs[printIndex].lastModified())) + "<br>");
        body.println("Size: " + fileLength + "<br>");
        body.println("----------------------------------------------------------");
        body.println("</td>\n<td>");
        //the form for regular expresions goes here
        body.println(
            "You may do a text search by using regular<br>expressions. Enter "
                + "your regular expression<br>below. If you are not familiar with regular<br>"
                + "expresions see <a href=\"http://jakarta.apache.org/regexp/apidocs/org/apache/"
                + "regexp/RE.html\" target=\"_blank\">Apache Regexp</a> for more<br>information.<br>");
        body.print("Regular Expression:&nbsp&nbsp");
        body.print("<input type=\"text\" name=\"");
        body.print(REGULAR_EXPRESSION_SEARCH);
        body.print("\" value=\"");
        body.print(regularExpressionSearch);
        body.println("\"><br>\nHightlight <input type=\"radio\" name=\"");
        body.print(DISPLAY_TYPE);
        body.print("\" value=\"");
        body.print(DISPLAY_TYPE_HIGHLIGHT);
        body.print("\" checked>\nFilter <input type=\"radio\" name=\"");
        body.print(DISPLAY_TYPE);
        body.print("\" value=\"");
        body.print(DISPLAY_TYPE_FILTER);
        body.println("\"><br><br>\n<input type=\"submit\" value=\"Search\" name=\"regExpSubmit\">");
        body.println("</td>\n</tr>\n</table>\n</form>\n<br>");

        if (!"".equals(regularExpressionSearch)) {
            body.println("The results of your search are highlighted below.<br><br>");
        }

        this.printLogFile(body, openejbLogs[printIndex], regularExpressionSearch, displayType);
    }

    /** gets the openejb.log file
     * @param body the output to send the data to
     * @param logFile the logfile that we're printing
     * @throws IOException if an exception is thrown
     */
    private void printLogFile(PrintWriter body, File logFile, String reSearch, String displayType) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(logFile));
        StringBuffer lineOfText;
        RE regularExpressionSearch = null;
        boolean filterSearch = false;
        boolean highlightSearch = false;
        boolean matchFound;

        //create a search reg expression
        if (!"".equals(reSearch.trim())) {
            try {
                regularExpressionSearch = new RE(reSearch);
            } catch (RESyntaxException e) {
                throw new IOException(e.getMessage());
            }

            //set these only if there is a search
            if (DISPLAY_TYPE_FILTER.equals(displayType)) {
                filterSearch = true;
            } else if (DISPLAY_TYPE_HIGHLIGHT.equals(displayType)) {
                highlightSearch = true;
            }
        }

        //create a list of special characters
        String[][] specialChars = new String[3][2];
        specialChars[0][0] = "&";
        specialChars[0][1] = "&amp;";
        specialChars[1][0] = "<";
        specialChars[1][1] = "&lt;";
        specialChars[2][0] = ">";
        specialChars[2][1] = "&gt;";
        int lineCounter = 0;
        String background;

        try {
            //create an array of regular expressions
            RE[] expArray = new RE[5];
            expArray[0] = new RE("^INFO :");
            expArray[1] = new RE("^DEBUG:");
            expArray[2] = new RE("^WARN :");
            expArray[3] = new RE("^ERROR:");
            expArray[4] = new RE("^FATAL:");

            //create an array of colors
            String[] colorArray = new String[5];
            colorArray[0] = "log4j-info";
            colorArray[1] = "log4j-debug";
            colorArray[2] = "log4j-warn";
            colorArray[3] = "log4j-error";
            colorArray[4] = "log4j-fatal";

            //create a table to write the file to
            body.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");

            //read the file line by line
            String expMatch = colorArray[0];
            String temp;
            while (true) {
                //check for null and break
                temp = fileReader.readLine();
                if (temp == null) {
                    break;
                }

                lineCounter++;
                lineOfText = new StringBuffer(temp);

                //check for and replace special characters
                String charToCheck;
                for (int i = 0; i < lineOfText.length(); i++) {
                    //pick out the current character
                    charToCheck = String.valueOf(lineOfText.charAt(i));
                    for (int j = 0; j < specialChars.length; j++) {
                        //do the check for equals
                        if (charToCheck.equals(specialChars[j][0])) {
                            lineOfText.replace(i, i + 1, specialChars[j][1]);
                            break;
                        }
                    }
                }

                temp = lineOfText.toString();
                //loop through the array of expressions to find a match
                for (int i = 0; i < expArray.length; i++) {
                    if (expArray[i].match(temp)) {
                        expMatch = colorArray[i];
                        break;
                    }
                }

                //check for an re search
                matchFound = false;
                background = "";
                if (regularExpressionSearch != null && regularExpressionSearch.match(temp)) {
                    matchFound = true;
                    if (highlightSearch) {
                        background = " bgcolor=\"#00ffff\"";
                    }
                }

                //print line of text to the page
                if ((filterSearch || highlightSearch) && (filterSearch && !matchFound)) {
                    continue;
                }

                body.println(
                    new StringBuffer(100)
                        .append("<tr")
                        .append(background)
                        .append("><td align=\"right\" valign=\"top\" class=\"")
                        .append(colorArray[0])
                        .append("\">")
                        .append(lineCounter)
                        .append("</td><td>&nbsp;</td><td class=\"")
                        .append(expMatch)
                        .append("\">")
                        .append(temp)
                        .append("</td></tr>")
                        .toString());
            }

            body.println("</table>");
        } catch (RESyntaxException se) {
            throw new IOException(se.getMessage());
        }

        //close the file
        fileReader.close();
    }

    /** Write the TITLE of the HTML document.  This is the part
      * that goes into the <code>&lt;head&gt;&lt;title&gt;
      * &lt;/title&gt;&lt;/head&gt;</code> tags
      *
      * @param body the output to write to
      * @exception IOException of an exception is thrown
      *
      */
    public void writeHtmlTitle(PrintWriter body) throws IOException {
        body.println(HTML_TITLE);
    }

    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writePageTitle(PrintWriter body) throws IOException {
        body.println("System Log Files");
    }

    /** Write the sub items for this bean in the left navigation bar of
     * the page.  This should look somthing like the one below:
     *
     *      <code>
     *      &lt;tr&gt;
     *       &lt;td valign="top" align="left"&gt;
     *        &lt;a href="system?show=deployments"&gt;&lt;span class="subMenuOff"&gt;
     *        &nbsp;&nbsp;&nbsp;Deployments
     *        &lt;/span&gt;
     *        &lt;/a&gt;&lt;/td&gt;
     *      &lt;/tr&gt;
     *      </code>
     *
     * Alternately, the bean can use the method formatSubMenuItem(..) which
     * will create HTML like the one above
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeSubMenuItems(PrintWriter body) throws IOException {}

}
