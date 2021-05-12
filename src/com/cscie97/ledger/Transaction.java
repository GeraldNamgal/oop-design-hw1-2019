/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Creates Transaction objects to be used by other classes, e.g., the Ledger
 */

package com.cscie97.ledger;

public class Transaction
{
    /* API Variables */
    
    private String transactionId;
    private Integer amount;
    private Integer fee;
    private String payload;
    private Account payer;
    private Account receiver;
    
    /* Constructor */
    
    public Transaction(String transactionId, Integer amount, Integer fee, String payload, Account payer, Account receiver)
    {
        this.transactionId = transactionId;
        this.amount = amount;
        this.fee = fee;
        this.payload = payload;
        this.payer = payer;
        this.receiver = receiver;
    }
    
    /* *
     * Getters and Setters
     */
    
    public String getTransactionId()
    {
        return transactionId;
    }
    
    public void setTransactionId(String id)
    {
        transactionId = id;
    }

    public Integer getAmount()
    {
        return amount;
    }    

    public Integer getFee()
    {
        return fee;
    }    

    public String getPayload()
    {
        return payload;
    }    

    public Account getPayer()
    {
        return payer;
    }    

    public Account getReceiver()
    {
        return receiver;
    }    
}
