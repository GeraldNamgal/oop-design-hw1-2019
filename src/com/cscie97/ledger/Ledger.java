/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Contains methods and attributes for creating and operating a blockchain service
 */

package com.cscie97.ledger;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.HashSet;

public class Ledger
{
    /* My Variables */
    
    // Blocks must have exactly 10 transactions
    private final int REQD_TRANSACTIONS = 10;
    private final String MASTER_ACCOUNT_ID = "master";
    private final int REQD_FEE = 10;
    
    // Initialize "suggested ID" maker for transaction ID duplicates
    private int suggestedId = 0;
    private HashSet<String> transactionIdsUsed;
    private Block currentBlock;
    
    /* API Variables */
    
    private String name;
    private String description;
    private String seed;
    private Block genesisBlock;
    private LinkedHashMap<Integer, Block> blockMap;    
    
    /* Constructor */
   
    public Ledger(String name, String description, String seed)
    {
        this.name = name;
        this.description = description;
        this.seed = seed;
        
        // Create genesisblock and make it currentBlock       
        genesisBlock = currentBlock = new Block();        
        
        // Create blockMap add genesisBlock to it      
        blockMap = new LinkedHashMap<Integer, Block>();        
        blockMap.put(genesisBlock.getBlockNumber(), genesisBlock);
        
        // Create master account and intialize its balance with max value
        createAccount(MASTER_ACCOUNT_ID).setBalance(Integer.MAX_VALUE);
        
        // Create HashSet to store and track used/processed transaction IDs
        transactionIdsUsed = new HashSet<String>();
    }
    
    /* *
     * API Methods
     */    
    
    public Account createAccount(String address)
    {         
        // Check account map for duplicate ID/address
        try
        {
            if (currentBlock.getAccountBalanceMap().containsKey(address))
                throw new LedgerException("create account", "account address already exists; account not created");            
        }
        
        catch (LedgerException duplicateAddressException)
        {
            System.out.println();
            System.out.print(duplicateAddressException.getMessage());      
            return null;
        }        
        
        Account account = new Account(address);
        
        // Add account to currentBlock
        if (account != null)
        {            
            currentBlock.addAccount(account);
        }
        
        return account;        
    }
    
    public String processTransaction(Transaction transaction)
    {       
        // Reject the transaction if it's invalid; raise exception
        try
        {            
            // Check if payer account is null (the account wasn't found [code logic] or some other reason)
            if (transaction.getPayer() == null)
                throw new LedgerException("submit transaction", "payer account not found; transaction cancelled");
            
            // Check if receiver account is null (the account wasn't found [code logic] or some other reason)
            if (transaction.getReceiver() == null)
                throw new LedgerException("submit transaction", "receiver account not found; transaction cancelled");
                
            // Check if 10 unit fee requirement is met
            if (transaction.getFee() < REQD_FEE)
                throw new LedgerException("submit transaction", "10 unit minimum fee required; transaction cancelled");
            
            // Check if payer has insufficient funds
            if (transaction.getPayer().getBalance() < (transaction.getFee() + transaction.getAmount()))
            {
                throw new LedgerException("submit transaction", "Payer has insufficient funds for amount and fee; "
                        + "transaction cancelled");
            }
        }        
        
        catch (LedgerException exception)
        {
            System.out.println();
            System.out.print(exception.getMessage());            
            return null;
        }
        
        // If transaction ID is a duplicate, raise and handle an exception (change txn ID)
        try
        {            
            if (transactionIdsUsed.contains(transaction.getTransactionId()))
            {
                // Get suggested id
                while (transactionIdsUsed.contains(Integer.toString(suggestedId)))
                    suggestedId++;
                
                throw new LedgerException("submit transaction", "duplicate ID submitted; ID changed to \"" + suggestedId + "\"");
            }
        }
        
        catch (LedgerException duplicateIdException)
        {
            // Print exception's action and reason
            System.out.println();
            System.out.print(duplicateIdException.getMessage());
            
            // Assign suggested id to the transaction                        
            transaction.setTransactionId(Integer.toString(suggestedId));
            
            // Increment suggestedId for next potential duplicate id
            suggestedId++;
        }
        
        // Transfer amount from payer to receiver
        transaction.getPayer().deductBalance(transaction.getAmount());
        transaction.getReceiver().increaseBalance(transaction.getAmount());
        
        // Transfer fee from payer to master account
        transaction.getPayer().deductBalance(transaction.getFee());
        currentBlock.getAccountBalanceMap().get("master").increaseBalance(transaction.getFee());
        
        // Add transaction to currentBlock's transactions list
        currentBlock.addTransaction(transaction);
        
        // Add transaction's ID to used IDs list in ledger since it's now processed
        transactionIdsUsed.add(transaction.getTransactionId());
        
        // If currentBlock has now reached REQD_TRANSACTIONS, "close" currentBlock and move to new block
        if (transactionIdsUsed.size() % REQD_TRANSACTIONS == 0)
        {
            // Call Block's closeBlock method on currentBlock with seed argument for hash
            currentBlock.closeBlock(seed);
            
            // Make currentBlock a new block and add the new block to blockMap
            currentBlock = new Block();
            blockMap.put(currentBlock.getBlockNumber(), currentBlock);
        }
        
        return transaction.getTransactionId();        
    }
    
    public Integer getAccountBalance(String address)
    {        
        // The most recent complete block is always the one before currentBlock (logic of code)        
        try
        {
            if (currentBlock != genesisBlock)
            {
                Account account = currentBlock.getPreviousBlock().getAccountBalanceMap().get(address);
                
                if (account != null)
                    return account.getBalance();                
                else
                {
                    if (currentBlock.getAccountBalanceMap().containsKey(address))
                        throw new LedgerException("get account balance", "account information unavailable");
                    else
                        throw new LedgerException("get account balance", "account not found");
                }
            }
            
            else
                throw new LedgerException("get account balance", "account balance information unavailable");
        }
        
        catch (Exception exception)
        {
            System.out.println();
            System.out.print(exception.getMessage());
        }
        
        return null;
    }
    
    public LinkedHashMap<String, Account> getAccountBalances()
    {
        // The most recent complete block is always the one before currentBlock (logic of code)
        try
        {
            if (currentBlock != genesisBlock)            
                return currentBlock.getPreviousBlock().getAccountBalanceMap();            
            else
                throw new LedgerException("get account balances", "no account balance map has been created yet");
        }
        
        catch (Exception exception)
        {
            System.out.println();
            System.out.print(exception.getMessage());
        }
        
        return null;        
    }
    
    public Block getBlock(Integer blockNumber)
    {
        Block block = blockMap.get(blockNumber);
        
        // Throw exception if block not found
        if (block == null)
        {
            try
            {
                throw new LedgerException("get block", "block not found");
            }
            
            catch (LedgerException exception)
            {
                System.out.println();
                System.out.print(exception.getMessage());
            }
        }
        
        return block;
    }
    
    public Transaction getTransaction(String transactionId)
    {
        try
        {
            for (Entry<Integer, Block> blockEntry : blockMap.entrySet())
            {
                Block block = blockEntry.getValue();
                for (Entry<String, Transaction> transactionEntry : block.getTransactionList().entrySet())
                {                
                    if (transactionEntry.getKey().equals(transactionId))
                        return transactionEntry.getValue();
                }
            }
            
            // Throw exception if transaction not found
            throw new LedgerException("get transaction", "transaction not found");
        }        
        
        catch (LedgerException exception)
        {
            System.out.println();
            System.out.print(exception.getMessage());
        }
        
        return null;
    }
    
    public void validate()
    {
        try
        {
            // Iterate over blockMap (uses a boolean flag)
            boolean found = false;
            for (Entry<Integer, Block> entry : blockMap.entrySet())
            {
                // Iterates just past genesisBlock so can then begin checking each block against the previous block
                if (!found && genesisBlock.getBlockNumber().equals(entry.getKey())) 
                    continue;
                
                // Flag that the spot to start iterating at (block '2' in this case) is found
                found = true;               
                
                // Get current and previous blocks
                Block block = entry.getValue();
                Block previousBlock = block.getPreviousBlock();                
                
                // Compare/validate hashes
                if (!block.getPreviousHash().equals(previousBlock.hashCalculator(seed)))                
                    throw new LedgerException("validate", "block hashes do not match"); 
                
                // Debugging
                /*if (block.getPreviousHash().equals(previousBlock.hashCalculator(seed)))
                {
                    System.out.println(block.getPreviousHash());
                    System.out.println(previousBlock.getHash());
                }*/
                
                // Verify that each completed block has exactly 10 transactions                
                if (previousBlock.getTransactionList().size() != 10)
                    throw new LedgerException("validate", "block failed required 10 transaction amount");
                
                // Debugging
                /*if (previousBlock.getTransactionList().size() == 10)
                    System.out.println(previousBlock.getTransactionList().size());*/
                
                // Make sure account balances total to the max value (currentBlock is last block [code logic])            
                Integer total = 0;
            	// Check previousBlock's balances
                for (Entry<String, Account> acctEntry : previousBlock.getAccountBalanceMap().entrySet())
                {
                    total += acctEntry.getValue().getBalance();
                }
                
                if (total != Integer.MAX_VALUE)
                    throw new LedgerException("validate", "block balances did not total max value");
                
                // Debugging
                /*System.out.println(total);*/               
            }
            
            if (found == false)
                throw new LedgerException("validate", "no completed blocks yet to validate.");             
        }
        
        catch (LedgerException exception)
        {
            System.out.println();
            System.out.print(exception.getMessage());
            return;
        }
        
        // If passes all checks, blockchain is valid
        System.out.println();
        System.out.println("Blockchain is valid.");
    }
    
    /* *
     * Utility Methods
     */    
       
    // Method: Returns the total number of blocks in the blockchain
    public Integer totalNumBlocks()
    {
        return blockMap.size();
    }

    /* *
     * Getters and Setters
     */
    
    public String getName() 
    {
        return name;
    }    

    public String getDescription() 
    {
        return description;
    }    

    public String getSeed() 
    {
        return seed;
    }

    public LinkedHashMap<Integer, Block> getBlockMap()
    {
        return blockMap;
    }

    public Block getCurrentBlock()
    {
        return currentBlock;
    }

    public Block getGenesisBlock()
    {
        return genesisBlock;
    }  
}
