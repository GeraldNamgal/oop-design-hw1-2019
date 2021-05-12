/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Handles exceptions thrown by the Ledger
 */

package com.cscie97.ledger;

@SuppressWarnings("serial")
public class LedgerException extends java.lang.Exception
{
    /* API Variables */
    
    private String action;
    private String reason;
    
    /* Constructor */
    
    public LedgerException(String action, String reason)
    {
        this.action = action;
        this.reason = reason;
    }
    
    /* Methods */
    
    public String getMessage()
    {
        return "LedgerException thrown --\n - Action: " + action + "\n" + " - Reason: " + reason + "\n";
    }    
}
