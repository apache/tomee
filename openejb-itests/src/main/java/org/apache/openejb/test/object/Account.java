package org.apache.openejb.test.object;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class Account implements java.io.Serializable{
    
    private String ssn;
    private String firstName;
    private String lastName;
    private int balance;

    public Account(String ssn, String firstName, String lastName, int balance){
        this.ssn = ssn;      
        this.firstName = firstName.trim();
        this.lastName = lastName.trim(); 
        this.balance = balance;     
    }
    
    public Account(){
    }

    public boolean equals(Object object){
        if ( !(object instanceof Account ) ) return false;

        Account that = (Account)object;

        return (this.ssn.equals(that.ssn) &&
                this.firstName.equals(that.firstName) &&
                this.lastName.equals(that.lastName) &&
                this.balance == that.balance);
    }


    public String getSsn(){
        return ssn;
    }
    
    public void setSsn(String ssn){
        this.ssn = ssn;
    }

    public String getFirstName(){
        return firstName;
    }
    
    public void setFirstName(String firstName){
        this.firstName = (firstName != null)? firstName.trim():null;
    }

    public String getLastName(){
        return lastName;
    }
    public void setLastName(String lastName){
        this.lastName = (lastName != null)? lastName.trim():null;
    }

    public int getBalance(){
        return balance;
    }
    public void setBalance(int balance){
        this.balance = balance;
    }


    public String toString(){
        return "["+ssn+"]["+firstName+"]["+lastName+"]["+balance+"]";
    }
}
