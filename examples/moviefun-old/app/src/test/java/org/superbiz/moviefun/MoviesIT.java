/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class MoviesIT {

    @Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
        WebClient webClient = new WebClient();
        HtmlPage page = webClient.getPage("http://localhost:9999/moviefun/setup.jsp");

        assertMoviesPresent(page);

        page = webClient.getPage("http://localhost:9999/moviefun/faces/movie/List.xhtml");

        assertMoviesPresent(page);
        webClient.closeAllWindows();
    }

    private void assertMoviesPresent(HtmlPage page) {
        String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Wedding Crashers"));
        assertTrue(pageAsText.contains("Starsky & Hutch"));
        assertTrue(pageAsText.contains("Shanghai Knights"));
        assertTrue(pageAsText.contains("I-Spy"));
        assertTrue(pageAsText.contains("The Royal Tenenbaums"));
        assertTrue(pageAsText.contains("Zoolander"));
        assertTrue(pageAsText.contains("Shanghai Noon"));
    }

    private void clickOnLink(HtmlPage page, String lookFor) throws Exception {
        DomNodeList<HtmlElement> links = page.getElementsByTagName("a");
        Iterator<HtmlElement> iterator = links.iterator();
        while (iterator.hasNext()) {
            HtmlAnchor anchor = (HtmlAnchor) iterator.next();

            if (lookFor.equals(anchor.getTextContent())) {
                anchor.click();
                break;
            }
        }
    }

}
