package org.superbiz.mbean;

import javax.management.Description;
import javax.management.MBean;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;

/**
 * @author Romain Manni-Bucau
 */
@MBean
@Description("play with me to guess a number")
public class GuessHowManyMBean {
    private int value = 0;

    @ManagedAttribute
    @Description("you are cheating!")
    public int getValue() {
        return value;
    }

    @ManagedAttribute
    public void setValue(int value) {
        this.value = value;
    }

    @ManagedOperation
    public String tryValue(int userValue) {
        if (userValue == value) {
            return "winner";
        }
        return "not the correct value, please have another try";
    }
}
