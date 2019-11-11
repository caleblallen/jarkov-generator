import java.util.HashMap;
import java.io.*;
import java.io.IOException;
import java.util.Scanner;


class Main {
   
   private static FirstOrderMarkovMachine dwarfNames;
   private static SecondOrderMarkovMachine orcNames;
   private static SentenceMarkovMachine twoCities;

   public static void main(String[] args)
   {
      // Load the file of comma delimited dwarf names into a Scanner.
      File dwarfFile = new File("src/dwarf_names.txt");
      Scanner dwarfReader= null;
      try 
      {
	 dwarfReader = new Scanner(dwarfFile); 
      }
      catch (FileNotFoundException e) 
      {
	 System.err.println(e.getMessage());
      }
      
      // Pass the scanner to our MarkovMachine for analysis.
      dwarfNames = new FirstOrderMarkovMachine(dwarfReader);
      
      // Load the file of comma delimited orc names into a Scanner.
      File orcFile = new File("src/orc_names.txt");
      Scanner orcReader = null;
      try 
      {
	      orcReader = new Scanner(orcFile); 
      }
      catch (FileNotFoundException e) 
      {
	      System.err.println(e.getMessage());
      }
      // Pass the scanner to our MarkovMachine for analysis.
      orcNames = new SecondOrderMarkovMachine(orcReader);
      
      // Load the file containing the first chapter of "A Tale of Two Cities" by Charles Dickens
      File dickensFile = new File("src/a_tale_of_two_cities_by_Charles_Dickens.txt");
      Scanner dickensReader = null;
      try 
      {
	 dickensReader = new Scanner(dickensFile); 
      }
      catch (FileNotFoundException e) 
      {
	 System.err.println(e.getMessage());
      }
      // Pass the scanner to our MarkovMachine for analysis.
      twoCities = new SentenceMarkovMachine(dickensReader);
      
      // Run the generator 5 times and print it all to the console.
      System.out.println("Here are your 5 Tolkien-inspired Dwarf names:");
      for(int i = 0; i < 5; i++)
      {
	 System.out.println(dwarfNames.generateSequence());
      }
      System.out.println();
      
      System.out.println("Here are your 5 Tolkien-inspired Orc names:");
      for(int i = 0; i < 5; i++)
      {
	 System.out.println(orcNames.generateSequence());
      }
      System.out.println();
      
      System.out.println("Here are your 5 sentences written in the style of Charles Dickens:");
      for(int i = 0; i < 5; i++)
      {
	 System.out.println(twoCities.generateSequence() + '\n');
      }
      System.out.println();

   }

}