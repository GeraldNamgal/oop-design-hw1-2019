/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Creates Block objects for blockchain to be utilized by other classes, e.g., the Ledger's blockMap
 */

package com.cscie97.ledger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Block
{    
    /* My Variables */
    
    // Initialize first "block number" in tmp variable
    private static int tmpBlockNumber = 1;
    // Initialize first "previous hash" in tmp variable
    private static String tmpPreviousHash = null;
    // Initialize other tmp variables
    private static Block tmpPreviousBlock = null;
    private static LinkedHashMap<String, Account> tmpAccountBalanceMap = null;
    
    /* API Variables */
    
    private Integer blockNumber;
    private String previousHash;
    private String hash;
    private LinkedHashMap<String, Transaction> transactionList;
    private LinkedHashMap<String, Account> accountBalanceMap;
    private Block previousBlock;
    
    /* Constructor */
    
    public Block()
    {        
        blockNumber = tmpBlockNumber;
        previousHash = tmpPreviousHash;            
        previousBlock = tmpPreviousBlock;      
        
        // Create accountBalanceMap and copy previous block's (if there is one) accounts into new block                
        accountBalanceMap = new LinkedHashMap<String, Account>();
        if (tmpAccountBalanceMap != null)
        {
            for (Entry<String, Account> entry : tmpAccountBalanceMap.entrySet())
            {
                Account accountToCopy = entry.getValue();
                Account account = new Account(accountToCopy.getAddress());
                account.setBalance(accountToCopy.getBalance());
                accountBalanceMap.put(account.getAddress(), account);
            }            
        }
        
        // Create transactionList
        transactionList = new LinkedHashMap<String, Transaction>();               
    }
    
    /* *
     * Utility Methods
     */
    
    // Method: Adds accounts to accountBalanceMap   
    public void addAccount(Account account)
    {
        accountBalanceMap.put(account.getAddress(), account);
    }

    // Method: Adds transactions to block
    public void addTransaction(Transaction transaction)
    {
        transactionList.put(transaction.getTransactionId(), transaction);
    }   
    
    // Method: "Closes" a block
    public void closeBlock(String seed)
    {     
        // Calculate hash for current block
        hash = hashCalculator(seed);
                
        // Update temporary state holder variables (for information transfer to next block)
        tmpPreviousHash = hash;
        tmpPreviousBlock = this;
        tmpBlockNumber++;
        tmpAccountBalanceMap = accountBalanceMap;
    }
    
    // Method: Creates a hash from String input (referenced https://www.baeldung.com/sha-256-hashing-java)
    public String hashCalculator(String seed)
    {
        // Get transactionList keys (for hash string)
        String transactionListString = "";
        for (Entry<String, Transaction> entry : transactionList.entrySet())
        {
            transactionListString = transactionListString.concat(entry.getKey());            
        }
        
        // Get accountBalanceMap keys (for hash string)
        String accountBalanceMapString = "";
        for (Entry<String, Account> entry : accountBalanceMap.entrySet())
        {
            accountBalanceMapString = accountBalanceMapString.concat(entry.getKey());            
        }
        
        String originalString = seed + Integer.toString(blockNumber) + previousHash + transactionListString + accountBalanceMapString;
        
        try
        {
            // Create message digest
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Create hashed value
            byte[] encodedHash = digest.digest(originalString.getBytes(StandardCharsets.UTF_8));        
            
            // Convert hashed value from bytes to hexadecimal
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < encodedHash.length; i++)
            {
                String hex = Integer.toHexString(0xff & encodedHash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            
            // Return hash
            return hexString.toString();
        }
        
        catch (Exception exception)
        {
            exception.printStackTrace();              
            
            // Print what exception has been thrown 
            System.out.println(exception); 
        }
        
        return null;
    }    
    
    /* *
     * Getters and Setters
     */
    
    public Integer getBlockNumber()
    {
        return blockNumber;
    }

    public String getPreviousHash()
    {
        return previousHash;
    }   

    public String getHash()
    {
        return hash;
    }

    public LinkedHashMap<String, Transaction> getTransactionList()
    {
        return transactionList;
    }

    public LinkedHashMap<String, Account> getAccountBalanceMap()
    {
        return accountBalanceMap;
    }

    public Block getPreviousBlock()
    {
        return previousBlock;
    }    
}
