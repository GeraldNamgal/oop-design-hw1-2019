/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Exercises the Ledger
 */

package com.cscie97.ledger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class CommandProcessor
{
    /* My Variables */

    private Ledger ledger;
    private int lineNum = 0;

    /* *
     * API Methods
     */

    public void processCommand(String command)
    {       
        parseAndProcess(command);
    }

    // Referenced https://www.journaldev.com/709/java-read-file-line-by-line
    public void processCommandFile(String commandFile)
    {       
        // Check if the file is empty
        try
        {
            File newFile = new File(commandFile);
            if (newFile != null)
            {
                if (newFile.length() == 0)
                    throw new CommandProcessorException("in processCommandFile method", "file is empty");
            }
        }

        catch (CommandProcessorException exception)
        {
            System.out.println(exception.getMessage());
            return;
        }

        // Read file
        try
        {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(commandFile));
            String line = reader.readLine();

            while (line != null)
            {
                // Counter up lineNum
                lineNum++;

                // Call parseAndProcess method if line isn't empty
                if (line.length() > 0)
                    parseAndProcess(line);

                // Read next line
                line = reader.readLine();
            }

            reader.close();
        }

        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /* *
     * Ledger-ish API Utility Methods
     */

    public void createLedger(String name, String description, String seed)
    {
        Ledger ledger = new Ledger(name, description, seed);
        this.ledger = ledger;
    }

    public void createAccount(String address)
    {
        Account account = ledger.createAccount(address);
    }

    public void processTransaction(String transactionId, Integer amount, Integer fee, String payload, String payer, String receiver)
    {
        // Initialize Account objects for payer and receiver address inputs
        Account payerAccount = null;
        Account receiverAccount = null;

        // Account retrieval should come back null if account doesn't exist
        payerAccount = ledger.getCurrentBlock().getAccountBalanceMap().get(payer);
        receiverAccount = ledger.getCurrentBlock().getAccountBalanceMap().get(receiver);

        // Create transaction object
        Transaction transaction = new Transaction(transactionId, amount, fee, payload, payerAccount, receiverAccount);

        // Submit transaction for processing
        String processedTransactionId = ledger.processTransaction(transaction);

        // Output transaction id
        if (processedTransactionId != null)
        {
            System.out.println();
            System.out.println("Transaction \'" + processedTransactionId + "\' processed.");
        }
    }

    public void getAccountBalance(String address)
    {
        Integer acctBalance = ledger.getAccountBalance(address);

        // Print account's balance
        if (acctBalance != null)
            System.out.println("\nAccount \"" + address + "\" has balance of " + acctBalance + ".");
    }

    public void getAccountBalances()
    {
        LinkedHashMap<String, Account> acctBalances = ledger.getAccountBalances();

        // Print accounts and balances
        if (acctBalances != null)
        {
            int counter = 1;
            System.out.println();
            System.out.println("Account Balances (in units) --");
            for (Entry<String, Account> accountEntry : acctBalances.entrySet())
            {
                System.out.print(" " + counter + ".) \"" + accountEntry.getKey() + "\": "
                        + accountEntry.getValue().getBalance() + "\n");
                counter++;
            }
        }
    }

    // Method: Gets block information (not Block object)
    public void getBlock(Integer blockNumber)
    {
        Block block = ledger.getBlock(blockNumber);

        // Print block's information including transactions and accounts with balances
        if (block != null)
        {
            String string;

            if (block == ledger.getGenesisBlock())
            {
                string = "\nBlock \'" + block.getBlockNumber() + "\' information --\n" + " - previousHash = "
                        + block.getPreviousHash() + "\n - hash = " + block.getHash() + "\n - previousBlock = null\n";
            }

            else
            {
                string = "\nBlock \'" + block.getBlockNumber() + "\' information --\n" + " - previousHash = "
                        + block.getPreviousHash() + "\n - hash = " + block.getHash() + "\n - previousBlock = "
                        + block.getPreviousBlock().getBlockNumber() + "\n";
            }

            string += blockCurrentTxns(block) + blockCurrentAccts(block);

            System.out.print(string);
        }
    }

    public void getTransaction(String transactionId)
    {
        Transaction transaction = ledger.getTransaction(transactionId);

        // Print transaction's information
        if (transaction != null)
        {
            System.out.println();
            System.out.println("Transaction " + transaction.getTransactionId() + "'s information --\n"
                    + " amount = " + Integer.toString(transaction.getAmount()) + "\n fee = "
                    + Integer.toString(transaction.getFee()) + "\n payload = " + transaction.getPayload()
                    + "\n payer = " + transaction.getPayer().getAddress() + "\n receiver = "
                    + transaction.getReceiver().getAddress());
        }
    }

    public void validate()
    {
        ledger.validate();
    }

    /* *
     * Other Utility Methods
     */

    public void parseAndProcess(String line)
    {
        // Trim leading and trailing whitespace
        String trimmedLine = line.trim();

        // Check if line is a comment
        if (trimmedLine.charAt(0) == '#')
        {
            System.out.println(line);
            return;
        }

        // Delimit input string on whitespace and add each value to array
        String[] splitStringArr = trimmedLine.split("\\s+");

        // Check if array has quote characters and do extra parsing if so (for process-transaction and create-ledger)
        ArrayList<Integer> indicesOfOpeningQuotes = new ArrayList<Integer>();
        ArrayList<Integer> indicesOfClosingQuotes = new ArrayList<Integer>();
        for (int i = 0; i < splitStringArr.length; i++)
        {
            // Check if stand-alone quote
            if ((splitStringArr[i].length() == 1) && (splitStringArr[i].charAt(0) == '"'))
            {
                if (indicesOfOpeningQuotes.size() == indicesOfClosingQuotes.size())
                    indicesOfOpeningQuotes.add(i);

                else
                    indicesOfClosingQuotes.add(i);
            }

            else
            {
                // Checks string if has a quote as first character
                if (splitStringArr[i].charAt(0) == '"')
                {
                    indicesOfOpeningQuotes.add(i);
                }

                // Checks string if has a quote as last character
                if (splitStringArr[i].charAt(splitStringArr[i].length() - 1) == '"')
                {
                    indicesOfClosingQuotes.add(i);
                }
            }
        }

        // If Ledger type DSL syntax found (input syntax has quotes)
        if ((splitStringArr.length >= 6) && (indicesOfOpeningQuotes.size() == 2)
                && (indicesOfOpeningQuotes.size() == indicesOfClosingQuotes.size())
                && (indicesOfOpeningQuotes.get(0) <= indicesOfClosingQuotes.get(0))
                && (indicesOfOpeningQuotes.get(1) > indicesOfClosingQuotes.get(0))
                && (indicesOfOpeningQuotes.get(1) <= indicesOfClosingQuotes.get(1))
                && (indicesOfOpeningQuotes.get(0) == 3) && (indicesOfClosingQuotes.get(1) == (splitStringArr.length - 1))
                && ((indicesOfOpeningQuotes.get(1) - 2) == (indicesOfClosingQuotes.get(0))))
        {
            // TODO: Check if ledger exists already
            if (this.ledger != null)
            {
                try
                {
                    if (lineNum == 0)
                        throw new CommandProcessorException("in processCommand method", "ledger already exists; input rejected");

                    else
                        throw new CommandProcessorException("in processCommandFile method", "ledger already exists; input rejected", lineNum);            
                }
                
                catch (CommandProcessorException exception)
                {
                    if (lineNum == 0)
                    {
                        System.out.println("-: " + trimmedLine);
                        System.out.println();
                        System.out.println(exception.getMessage());
                        
                        return;
                    }
        
                    else
                    {
                        System.out.println("-: " + trimmedLine);
                        System.out.println();
                        System.out.println(exception.getMessageLine());
                        
                        return;
                    }
                }
            }
            
            // String quoted inputs back together
            String quote1 = "";
            for (int i = indicesOfOpeningQuotes.get(0); i < (indicesOfClosingQuotes.get(0)); i++)
            {
                quote1 += splitStringArr[i] + " ";
            }
            quote1 += splitStringArr[indicesOfClosingQuotes.get(0)];

            String quote2 = "";
            for (int i = indicesOfOpeningQuotes.get(1); i < (indicesOfClosingQuotes.get(1)); i++)
            {
                quote2 += splitStringArr[i] + " ";
            }
            quote2 += splitStringArr[indicesOfClosingQuotes.get(1)];

            // Create new splitStringArr with quoted input back together
            ArrayList<String> quotesSplitStringArr = new ArrayList<String>();
            for (int i = 0; i < indicesOfOpeningQuotes.get(0); i++)
                quotesSplitStringArr.add(splitStringArr[i]);
            quotesSplitStringArr.add(quote1);
            for (int i = (indicesOfClosingQuotes.get(0) + 1); i < indicesOfOpeningQuotes.get(1); i++)
                quotesSplitStringArr.add(splitStringArr[i]);
            quotesSplitStringArr.add(quote2);

            // Check that other DSL syntax is valid and call createLedger if so
            if (quotesSplitStringArr.get(0).equalsIgnoreCase("Create-ledger") && quotesSplitStringArr.get(2).equalsIgnoreCase("description")
                    && quotesSplitStringArr.get(4).equalsIgnoreCase("seed"))
            {
                System.out.println("-: " + trimmedLine);
                createLedger(quotesSplitStringArr.get(1), quotesSplitStringArr.get(3), quotesSplitStringArr.get(5));

                System.out.println();
            }
        }
        
        // If ledger does not exist, don't accept commands and return
        else if (ledger == null)
        {
            try
            {
                if (lineNum == 0)
                    throw new CommandProcessorException("in processCommand method", "no ledger exists; input rejected");

                else
                    throw new CommandProcessorException("in processCommandFile method", "no ledger exists; input rejected", lineNum);            
            }
            
            catch (CommandProcessorException exception)
            {
                if (lineNum == 0)
                {
                    System.out.println("-: " + trimmedLine);
                    System.out.println();
                    System.out.println(exception.getMessage());
                    
                    return;
                }
    
                else
                {
                    System.out.println("-: " + trimmedLine);
                    System.out.println();
                    System.out.println(exception.getMessageLine());
                    
                    return;
                }
            }
        }
        
        // If process-transaction type DSL syntax found (input syntax has quotes)
        else if ((splitStringArr.length >= 12) && (indicesOfOpeningQuotes.size() == 1)
                && (indicesOfOpeningQuotes.size() == indicesOfClosingQuotes.size())
                && (indicesOfOpeningQuotes.get(0) <= indicesOfClosingQuotes.get(0)) && (indicesOfOpeningQuotes.get(0) == 7)
                && (indicesOfClosingQuotes.get(0) == splitStringArr.length - 5))
        {
            // String quoted input back together
            String quote = "";
            for (int i = indicesOfOpeningQuotes.get(0); i < (indicesOfClosingQuotes.get(0)); i++)
            {
                quote += splitStringArr[i] + " ";
            }
            quote += splitStringArr[indicesOfClosingQuotes.get(0)];

            // Remove quotes from quote (referenced https://www.geeksforgeeks.org/stringbuffer-deletecharat-method-in-java/)
            StringBuffer sbf = new StringBuffer(quote);
            quote = sbf.deleteCharAt(0).toString();
            sbf = new StringBuffer(quote);
            quote = sbf.deleteCharAt(quote.length() - 1).toString();

            // Create new splitStringArr with quoted input back together
            ArrayList<String> quotesSplitStringArr = new ArrayList<String>();
            for (int i = 0; i < indicesOfOpeningQuotes.get(0); i++)
                quotesSplitStringArr.add(splitStringArr[i]);
            quotesSplitStringArr.add(quote);
            for (int i = (indicesOfClosingQuotes.get(0) + 1); i < splitStringArr.length; i++)
                quotesSplitStringArr.add(splitStringArr[i]);

            // Check if integer inputs are valid
            Boolean validInts = true;
            try
            {
                Integer.parseInt(quotesSplitStringArr.get(3));
                Integer.parseInt(quotesSplitStringArr.get(5));
            }

            catch (NumberFormatException exception)
            {
                validInts = false;
            }

            // Check that other DSL syntax is valid and call processTransaction if so
            if (quotesSplitStringArr.get(0).equalsIgnoreCase("process-transaction") && quotesSplitStringArr.get(2).equalsIgnoreCase("amount")
                    && quotesSplitStringArr.get(4).equalsIgnoreCase("fee") && quotesSplitStringArr.get(6).equalsIgnoreCase("payload")
                    && quotesSplitStringArr.get(8).equalsIgnoreCase("payer") && quotesSplitStringArr.get(10).equalsIgnoreCase("receiver")
                    && (validInts == true))
            {
                System.out.println("-: " + trimmedLine);
                processTransaction(quotesSplitStringArr.get(1), Integer.parseInt(quotesSplitStringArr.get(3)),
                        Integer.parseInt(quotesSplitStringArr.get(5)), quotesSplitStringArr.get(7),
                        quotesSplitStringArr.get(9), quotesSplitStringArr.get(11));

                System.out.println();
            }
        }       

        // Validate syntax of other types of input and call their methods accordingly
        else if (splitStringArr.length > 0)
        {
            if (splitStringArr[0].equalsIgnoreCase("create-account"))
            {
                if (splitStringArr.length == 2)
                {
                    System.out.println("-: " + trimmedLine);
                    createAccount(splitStringArr[1]);
                    System.out.println();
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("get-account-balance"))
            {
                if (splitStringArr.length == 2)
                {
                    System.out.println("-: " + trimmedLine);
                    getAccountBalance(splitStringArr[1]);
                    System.out.println();
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("get-account-balances"))
            {
                if (splitStringArr.length == 1)
                {
                    System.out.println("-: " + trimmedLine);
                    getAccountBalances();
                    System.out.println();
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("get-block"))
            {
                if (splitStringArr.length == 2)
                {
                    // Check if integer input is valid
                    Boolean validInt = true;
                    try
                    {
                        Integer.parseInt(splitStringArr[1]);
                    }

                    catch (NumberFormatException exception)
                    {
                        validInt = false;
                    }

                    if (validInt == true)
                    {
                        System.out.println("-: " + trimmedLine);
                        getBlock(Integer.parseInt(splitStringArr[1]));
                        System.out.println();
                    }
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("get-transaction"))
            {
                if (splitStringArr.length == 2)
                {
                    System.out.println("-: " + trimmedLine);
                    getTransaction(splitStringArr[1]);
                    System.out.println();
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("validate"))
            {
                if (splitStringArr.length == 1)
                {
                    System.out.println("-: " + trimmedLine);
                    validate();
                    System.out.println();
                }
            }

            else if (splitStringArr[0].equalsIgnoreCase("get-total-blocks"))
            {
                if (splitStringArr.length == 1)
                {
                    System.out.println("-: " + trimmedLine);
                    totalNumBlocks();
                    System.out.println();
                }
            }

            // Throw CommandProcessorException if invalid command
            else
            {
                try
                {
                    if (lineNum == 0)
                        throw new CommandProcessorException("in processCommand method", "invalid DSL command input");

                    else
                        throw new CommandProcessorException("in processCommandFile method", "invalid DSL command input", lineNum);
                }

                catch (CommandProcessorException exception)
                {
                    if (lineNum == 0)
                    {
                        System.out.println("-: " + trimmedLine);
                        System.out.println();
                        System.out.println(exception.getMessage());
                    }

                    else
                    {
                        System.out.println("-: " + trimmedLine);
                        System.out.println();
                        System.out.println(exception.getMessageLine());
                    }
                }
            }
        }
    }

    // Method: Returns just a block (object), e.g., without added information printed to stdout
    public Block getBlockNoOutput(Integer blockNumber)
    {
        return ledger.getBlock(blockNumber);
    }

    // Method: Returns current block (object)
    public Block getCurrentBlock()
    {
        return ledger.getCurrentBlock();
    }

    // Method: Prints the total number of blocks in the blockchain
    public void totalNumBlocks()
    {
        System.out.println();
        System.out.println("The total number of blocks in the blockchain is " + ledger.totalNumBlocks() + ".");
    }

    // Method: Returns a block's transaction information
    public String blockCurrentTxns(Block block)
    {
        if (block != null)
        {
            // Get block's transaction list
            LinkedHashMap<String, Transaction> blockCurrentTxns = block.getTransactionList();

            // Start string of information
            String string = " - Transactions:\n";
            if (blockCurrentTxns.size() != 0)
            {
                Transaction txn;
                int counter = 1;
                for (Entry<String, Transaction> transactionEntry : blockCurrentTxns.entrySet())
                {
                    txn = transactionEntry.getValue();

                    string += "    " + counter + ".) " + "transaction " + txn.getTransactionId() + " amount "
                            + txn.getAmount() + " fee " + txn.getFee() + " payload \"" + txn.getPayload() + "\" payer "
                            + txn.getPayer().getAddress() + " receiver " + txn.getReceiver().getAddress() + "\n";

                    counter++;
                }
            }

            else
                string += "    None\n";

            return string;
        }

        return null;
    }

    // Method: Returns a block's account balance information
    public String blockCurrentAccts(Block block)
    {
        if (block != null)
        {
            // Get block's account balance map
            LinkedHashMap<String, Account> blockCurrentAccts = block.getAccountBalanceMap();

            // Start string of information
            String string = " - Account balances (in units):\n";
            if (blockCurrentAccts.size() != 0)
            {
                Account acct;
                int counter = 1;
                for (Entry<String, Account> accountEntry : blockCurrentAccts.entrySet())
                {
                    acct = accountEntry.getValue();
                    string += "    " + counter + ".) " + "\"" + acct.getAddress() + "\": " + acct.getBalance() + "\n";
                    counter++;
                }
            }

            else
                string += "    None\n";

            return string;
        }

        return null;
    }
}
