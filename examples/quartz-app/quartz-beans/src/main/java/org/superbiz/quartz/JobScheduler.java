/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.superbiz.quartz;

import java.util.Date;
import javax.ejb.Local;

@Local
public interface JobScheduler {

    Date createJob() throws Exception;
}
