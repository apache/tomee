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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
