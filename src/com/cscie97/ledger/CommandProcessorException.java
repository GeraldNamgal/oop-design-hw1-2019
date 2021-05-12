/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Handles exceptions thrown by the CommandProcessor
 */

package com.cscie97.ledger;

@SuppressWarnings("serial")
public class CommandProcessorException extends java.lang.Exception
{
    /* API Variables */
    
    private String action;
    private String reason;
    private Integer lineNumber;
    
    /* Constructor */
    
    // Two constructors; for exceptions that output file line numbers and those that don't
    public CommandProcessorException(String action, String reason)
    {
        this.action = action;
        this.reason = reason;        
    }
    
    public CommandProcessorException(String action, String reason, Integer lineNumber)
    {
        this.action = action;
        this.reason = reason;
        this.lineNumber = lineNumber;
    }
    
    /* Methods */
    
    public String getMessage()
    {
        return "CommandProcessorException thrown --\n Action: " + action + "\n" + " Reason: " + reason + "\n";
    }
    
    public String getMessageLine()
    {
        return "CommandProcessorException thrown --\n Action: " + action + "\n" + " Reason: " + reason +
                "\n Line number: " + lineNumber + "\n";
    }
}
