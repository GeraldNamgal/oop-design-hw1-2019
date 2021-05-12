/* *
 * Gerald Arocena
 * CSCI E-97
 * Professor: Eric Gieseke
 * Assignment 1
 *
 * Contains main method that calls the CommandProcessor class to exercise the Ledger
 */

package com.cscie97.ledger.test;

import java.util.Scanner;

import com.cscie97.ledger.*;

public class TestDriver
{    
    // Method: Without a script file argument "main" will run manual commands (otherwise will run script file)
    public static void main(String[] args)
    {
        // Create a command processor
        CommandProcessor cp = new CommandProcessor();       
        
        // If no file argument included on command line call processCommand method
        if (args.length == 0)
        {
            String str;    
            
            Scanner input = new Scanner(System.in);           
            
            System.out.print(getMenu());
            
            while (!(str = input.nextLine()).equals("q"))
            {   
                // Check if user inputted anything and call command processor if so
                if (str.length() > 0)
                    cp.processCommand(str); 
                
                System.out.println(); 
                
                //System.out.print(getMenu());
                System.out.print("Please enter a command ('q' to quit): ");
            }
            
            // Close scanner
            input.close();            
        }
        
        // If script file argument included on command line call processCommandFile
        else if (args.length == 1)
        {
            // Call command processor with file name
            cp.processCommandFile(args[0]);       
        }    
    }
    
    public static String getMenu()
    {
        // Initialize menu string
        String string = "";
        
        // Output list of commands
        string += "Commands and syntax:\n";
        string += " 1.) Create-ledger <name> description <\"description\"> seed <\"seed\">\n"; // 6
        string += " 2.) create-account <account-id>\n"; // 2
        string += " 3.) process-transaction <transaction-id> amount <amount> fee <fee> payload " 
                + "<\"payload\"> payer <account-address> receiver <account-address>\n"; // 12
        string += " 4.) get-account-balance <account-id>\n"; // 2
        string += " 5.) get-account-balances\n"; // 1
        string += " 6.) get-block <block-number>\n"; // 2
        string += " 7.) get-transaction <transaction-id>\n"; // 2
        string += " 8.) validate\n\n"; // 1
        string += " Example command: Create-ledger test description \"test ledger\" seed \"cambridge\"\n\n";                
        string += "Please enter a command ('q' to quit): ";      
        
        return string;
    }
}
