/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.superbiz.webapp1.ejb;

import javax.ejb.Stateless;
import javax.validation.constraints.Pattern;

@Stateless
public class BusinessBean {

    public void doStuff(@Pattern(regexp = "valid") final String txt) {
        System.out.println("Received: " + txt);
    }

}
