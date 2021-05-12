/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Creates Account objects to be used by other classes, e.g., the Ledger
 */

package com.cscie97.ledger;

public class Account
{
    /* API Variables */
    
    private String address;
    private Integer balance;
    
    /* Constructor */
    
    public Account(String address)
    { 
        this.address = address;
        
        // Initialize balance to 0
        balance = 0;
    }
    
    /* *
     * Utility Methods
     */
    
    public void increaseBalance(Integer amount)
    {
        balance += amount;
    }
    
    public void deductBalance(Integer amount)
    {
        balance -= amount;
    }
    
    /* *
     * Getters and Setters
     */
    
    public Integer getBalance()
    {
        return balance;
    }
    
    public void setBalance(Integer balance)
    {
        this.balance = balance;
    }   
    
    public String getAddress()
    {
        return address;
    }        
}
