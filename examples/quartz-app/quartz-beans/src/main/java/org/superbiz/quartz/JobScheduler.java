/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.superbiz.quartz;

import javax.ejb.Local;
import java.util.Date;

@Local
public interface JobScheduler {

    Date createJob() throws Exception;
}
