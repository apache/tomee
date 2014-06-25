/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.superbiz.webapp2.messages;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@XmlSeeAlso(ErrorResponse.class)
public class ErrorList<T> extends ArrayList<T> {

    private static final long serialVersionUID = -8861634470374757349L;

    public ErrorList() {
    }

    public ErrorList(final Collection<? extends T> clctn) {
        addAll(clctn);
    }

}
