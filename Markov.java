import java.util.HashMap;
import java.io.*;
import java.io.IOException;
import java.util.Scanner;


abstract class MarkovMachine {
   // These variables will help us keep track of our states.
   public static final String FIRST_STATE_KEY = "^";
   public static final String LAST_STATE_KEY = "$";
   
   // This puts an upper limit on the number of tokens we will use.
   public static final int MAX_SAMPLES = 50000;
   
   // The Markov Machine only knows it's current state and possible next states.
   // This variable tracks the currentState
   private String currentState;
   
   // This is the Markov Probability Distribution. 
   // It tracks each state and possible subsequent states
   HashMap<String,HashMap<String, Integer>> markovStates;
   
   // The text will be analyzed differently in each machine. 
   // Primarily due to the order of the machine and delimiters.
   abstract void analyzeText(String[] tokens);

   // A default constructor for child classes to use in initialization.
   public MarkovMachine()
   {
      this.currentState = FIRST_STATE_KEY;
      this.markovStates = null;
   }
   
   // Run the markov machine until it encounters the LAST_STATE_KEY
   public String generateSequence()
   {
      String sequence = "";
      while(!sequence.contains(LAST_STATE_KEY))
      {
	 sequence += getNextState();
      }
      resetState();
      return sequence.replace(LAST_STATE_KEY, "");
   }

   // Randomly determine the next state and return it.
   public String getNextState()
   {
      HashMap<String,Integer> currentMap = markovStates.get(currentState);
      // Get the total number of state occurances
      int currentSum = getSumOfValues(currentMap);
      
      // Generate a random index or 'roll' to pull the next state from.
      int roll = randomInt(0,currentSum);
      
      // Traverse through the contents of the HashMap, key by key.
      // When we pass the threshold of number of items skipped, return the random key.
      for(String key : currentMap.keySet())
      {
	 roll -= currentMap.get(key);
	 if(roll < 0)
	 {
	    currentState = key;
	    return key;
	 }
      }
      return "";
      
   }
   
   // Accessor for current State
   public String getCurrentState()
   {
      return currentState;
   }
   
   // Return a Deep Copy of markovStates
   public HashMap<String,HashMap<String, Integer>> getMarkovStates()
   {
      // Create the return HashMap
      HashMap<String,HashMap<String, Integer>> statesToPass;
      statesToPass = new HashMap<String,HashMap<String, Integer>>();
      
      // Iterate through all "current state" keys
      for(String key : markovStates.keySet())
      {
	 // For each "current state" key, create a new HashMap for that key/value pair.
	 HashMap<String, Integer> subSet = markovStates.get(key);
	 
	 HashMap<String, Integer> subSetToPass = new HashMap<String, Integer>();

	 for(String subKey : subSet.keySet())
	 {
	    // For each "next state" key, copy the key/value pair.
	    subSetToPass.put(subKey, subSet.get(subKey));
	 }
	 statesToPass.put(key, subSetToPass);
      }
      return statesToPass;
   }

   // Resets the current state of the Markov machine
   public boolean resetState()
   {
      currentState = FIRST_STATE_KEY;
      if(currentState == FIRST_STATE_KEY)
      {
	 return true;
      }
      else
      {
	 return false;
      }
   }
   
   // Function to clean repeated operations.
   void addStateEntry(String currentState, String nextState)
   {    
      // If we do not have an entry for the currentState in markovStates, create one.
      if(!this.markovStates.containsKey(currentState))
      {
	 HashMap<String,Integer> nextMap = new HashMap<String, Integer>();
	 nextMap.put(nextState, 1);
	 markovStates.put(currentState, nextMap);
      }
      else
      {
	 
	 HashMap<String, Integer> currentMap = markovStates.get(currentState);
	 
	 // Check to see if there is an entry for the next state in the current state's HashMap
	 // If it exists, increment the value. If it does not exist, create the entry.
	 if(currentMap.containsKey(nextState))
	 {
	    int nextStateCount = currentMap.get(nextState);
	    currentMap.put(nextState, nextStateCount + 1);
	 }
	 else
	 {
	    currentMap.put(nextState, 1);
	 }	 
      }

   }
   
   // Separates tokens (usually individual words) into an array of Strings. 
   String[] getTokens(Scanner fileReader) {
      // Names are in this format: tom, dick, harry, phil
      fileReader.useDelimiter(",[ \n\r]");
      
      String[] tokens = new String[MAX_SAMPLES];
      
      // Index counter to make sure we don't exceed MAX_SAMPLES
      int tokenIndex = 0;
      
      // Go through the file, token by token, add them to the return array.
      while(fileReader.hasNext())
      {
	 if(tokenIndex < MAX_SAMPLES)
	 {
	    tokens[tokenIndex++] = fileReader.next();
	 } 
      }
      return tokens;
   }
   
   // Separates tokens (usually individual words) into Markov states.
   String[] tokenToStates(String token,int order)
   {
      // the 'order' parameter refers to how large each sample is.
      // First order is letter by letter, Second order is letter pairs, etc.
      String[] states = new String[token.length()+2];
      for(int i = 0; i*order < token.length(); i++)
      {
	 if(i*order+order > token.length())
	 {
	    states[i] = token.substring(i*order,token.length());
	 }
	 else
	 {
	    states[i] = token.substring(i*order,i*order+order);
	 }
	 
      }
      return states;
   }
   
   // Add up all the occurances of a 'next state' HashMap
   private int getSumOfValues(HashMap<String,Integer> frequencyTable)
   {
      int collector = 0;
      for(String key : frequencyTable.keySet())
      {
	 collector += frequencyTable.get(key);
      }
      return collector;
   }
   
   // Generates a random int where min <= return < max
   private int randomInt(int min, int max)
   {
      return (int)(Math.random()*max) + min;
   }


}

// This Markov Machine will sequences using a first order states.
class FirstOrderMarkovMachine extends MarkovMachine {

   // Only Constructor for this Markov Machine. Takes in a Scanner to crunch.
   public FirstOrderMarkovMachine(Scanner nameScanner)
   {
      super();
      // Initialize the object that stores our Markov State Table.
      this.markovStates = new HashMap<String,HashMap<String,Integer>>();
      
      // Separate the file contents into tokens.
      String[] tokens = getTokens(nameScanner);
      
      // analyze the text and fill the Markov State Table
      analyzeText(tokens);
   }
   
   // This function takes in individual tokens (usually words) and creates a Markov State Table.
   void analyzeText(String[] tokens)
   {
      //FIRST_STATE_KEY is a special value. It indicates a state that starts a pattern (word).
      markovStates.put(FIRST_STATE_KEY, new HashMap<String, Integer>());

      // Loop through all pattern entries (words)
      for(int i = 0; i < tokens.length; i++)
      {
	 //Cut up the pattern (word) into states.
	 String[] states = tokenToStates(tokens[i],1);

	 //Iterate through the states, in order, to record the 1:1 state relationship.
	 //I.E., record when one state follows another.
	 for(int j = 0; j < states.length; j++)
	 {
	    //Out of useful states.
	    if(states[j] == null)
	    {
	       j = states.length;
	       break;
	    }
	    
	    //The first state is special, so we add it to the FIRST_STATE_KEY entry
	    if(j == 0)
	    {
	       addStateEntry(FIRST_STATE_KEY,states[j]);
	    }
	    
	    // Since Markov chains are 1:1, current:next, we track both here.
	    String currentState = states[j];
	    String nextState;
	    
	    //Initialize nextState to the state that succeeds the current one.
	    //If there is no such token (either null or j + 1 > states.length) add LAST_STATE_KEY
	    if(j + 1 < states.length)
	    {
	       if(states[j+1] != null)
	       {
		  nextState = states[j+1];
	       }
	       else
	       {
		  nextState = LAST_STATE_KEY;
	       }
	    }
	    else 
	    {
	       nextState = LAST_STATE_KEY;
	    }
	    

	    // Add the state entry.
	    addStateEntry(currentState,nextState);
	 }
	
	 // This section breaks out of our loop if we run out of tokens.
	 if(i+1< tokens.length)
	 {
	    if(tokens[i+1] == null)
	    {
	       i = tokens.length;
	    }
	 }
      }
      
   }
   

}

//This Markov Machine will sequences using a second order states.
class SecondOrderMarkovMachine extends MarkovMachine {

   // Only Constructor for this Markov Machine. Takes in a Scanner to crunch.
   public SecondOrderMarkovMachine(Scanner nameScanner)
   {
      super();
      
      // Initialize the object that stores our Markov State Table.
      this.markovStates = new HashMap<String,HashMap<String,Integer>>();
      
      // Separate the file contents into tokens.
      String[] tokens = getTokens(nameScanner);
      
      // analyze the text and fill the Markov State Table
      analyzeText(tokens);
   }
   
   
   void analyzeText(String[] tokens)
   {
      //FIRST_STATE_KEY is a special value. It indicates a state that starts a pattern (word).
      markovStates.put(FIRST_STATE_KEY, new HashMap<String, Integer>());

      // Loop through all pattern entries (words)
      for(int i = 0; i < tokens.length; i++)
      {
	 //Cut up the pattern (word) into states.
	 String[] states = tokenToStates(tokens[i],2);

	 //Iterate through the states, in order, to record the 1:1 state relationship.
	 //I.E., record when one state follows another.
	 for(int j = 0; j < states.length; j++)
	 {
	    //Out of useful states.
	    if(states[j] == null)
	    {
	       j = states.length;
	       break;
	    }
	    
	    //The first state is special, so we add it to the FIRST_STATE_KEY entry
	    if(j == 0)
	    {
	       addStateEntry(FIRST_STATE_KEY,states[j]);
	    }
	    
	    // Since Markov chains are 1:1, current:next, we track both here.
	    String currentState = states[j];
	    String nextState;
	    
	    //Initialize nextState to the state that succeeds the current one.
	    //If there is no such token (either null or j + 1 > states.length) add LAST_STATE_KEY
	    if(j + 1 < states.length)
	    {
	       if(states[j+1] != null)
	       {
		  nextState = states[j+1];
	       }
	       else
	       {
		  nextState = LAST_STATE_KEY;
	       }
	    }
	    else 
	    {
	       nextState = LAST_STATE_KEY;
	    }
	    

	    // Add the state entry.
	    addStateEntry(currentState,nextState);
	 }
	
	 // This section breaks out of our loop if we run out of tokens.
	 if(i+1< tokens.length)
	 {
	    if(tokens[i+1] == null)
	    {
	       i = tokens.length;
	    }
	 }
      }
      
   }
   
}

class SentenceMarkovMachine extends MarkovMachine {

   // Only Constructor for this Markov Machine. Takes in a Scanner to crunch.
   public SentenceMarkovMachine(Scanner nameScanner)
   {
      super();
      
      // Initialize the object that stores our Markov State Table.
      this.markovStates = new HashMap<String,HashMap<String,Integer>>();
      
      // Separate the file contents into tokens.
      String[] tokens = getTokens(nameScanner);
      
      // analyze the text and fill the Markov State Table
      analyzeText(tokens);
   }
   
   // Run the markov machine until it encounters the LAST_STATE_KEY
   public String generateSequence()
   {
      // Here we are generating a collection of words and concatenating them
      // into a sequence. Each word needs a space between it, but commas need
      // spaces removed.
      String sequence = "";
      while(!sequence.contains(LAST_STATE_KEY))
      {
	 String nextState = getNextState();
	 if(nextState.contains(","))
	 {
	    sequence = sequence.substring(0,sequence.length()-1) + nextState + " ";
	 }
	 else
	 {
	    sequence += nextState + " ";
	 }
	 
      }
      resetState();
      return sequence.replace(" " + LAST_STATE_KEY, ".").substring(1);
   }
   
   void analyzeText(String[] tokens)
   {
      //FIRST_STATE_KEY is a special value. It indicates a state that starts a pattern (word).
      markovStates.put(FIRST_STATE_KEY, new HashMap<String, Integer>());

      // Loop through all pattern entries (words)
      for(int i = 0; i < tokens.length; i++)
      {
	 //Cut up the pattern (word) into states.
	 String[] states = tokenToStates(tokens[i]);

	 //Iterate through the states, in order, to record the 1:1 state relationship.
	 //I.E., record when one state follows another.
	 for(int j = 0; j < states.length; j++)
	 {
	    //Out of useful states.
	    if(states[j] == null)
	    {
	       j = states.length;
	       break;
	    }
	    
	    
	    //The first state is special, so we add it to the FIRST_STATE_KEY entry
	    if(j == 0)
	    {
	       addStateEntry(FIRST_STATE_KEY,states[j]);
	    }
	    
	    // Since Markov chains are 1:1, current:next, we track both here.
	    String currentState = states[j];
	    String nextState;
	    
	    //Initialize nextState to the state that succeeds the current one.
	    //If there is no such token (either null or j + 1 > states.length) add LAST_STATE_KEY
	    if(j + 1 < states.length)
	    {
	       if(states[j+1] != null)
	       {
		  nextState = states[j+1];
	       }
	       else
	       {
		  nextState = LAST_STATE_KEY;
	       }
	    }
	    else 
	    {
	       nextState = LAST_STATE_KEY;
	    }
	    

	    // Add the state entry.
	    addStateEntry(currentState,nextState);
	 }
	
	 // This section breaks out of our loop if we run out of tokens.
	 if(i+1< tokens.length)
	 {
	    if(tokens[i+1] == null)
	    {
	       i = tokens.length;
	    }
	 }
      }
   }

   // Separates tokens (usually individual words) into Markov states.
   String[] tokenToStates(String token)
   {
      return token.split("[ \n]");
   }
   
   // Separates tokens (usually individual words) into an array of Strings. 
   String[] getTokens(Scanner fileReader) {
      // Each token will be an entire sentence. Delimiters are punctuation.
      fileReader.useDelimiter("[!?.]");
      String[] tokens = new String[MAX_SAMPLES];
      
      
      //counter to make sure we don't go above MAX_SAMPLES
      int tokenIndex = 0;
      while(fileReader.hasNext())
      {
	 if(tokenIndex < MAX_SAMPLES)
	 {
	    tokens[tokenIndex++] = textFormatter(fileReader.next());
	 } 
      }
      return tokens;
   } 
   
   // Helper function that clears unwanted chracters and separates commas from words.
   private String textFormatter(String text)
   {
      String[] unwantedSubstrings = {"\n","\r"};
      for(int i = 0; i < unwantedSubstrings.length; i++)
      {
	 text = text.replace(unwantedSubstrings[i], "");
      }
      return text.replace(","," ,");
   }
   
}


/*************Output************************
Here are your 5 Tolkien-inspired Dwarf names:
Thrin
Har
Nor
Gri
Thr

Here are your 5 Tolkien-inspired Orc names:
Zagduf
Gorshak
Muzthak
Muzthak
Muzgaz

Here are your 5 sentences written in the style of Charles Dickens:
France. 

In both countries it was clearer than her five-and-twentieth blessed birthday, 
Fate, we were sheltered from the year of her sister of belief, it was the age of 
France and his tongue torn out its noisiest authorities insisted on its noisiest 
authorities insisted on its noisiest authorities insisted on the guidance of the 
rain to be sawn into boards, there were all going direct the chickens of the whole 
as the human race than any communications yet received through any of England; there 
were a plain face, we had heralded the Cock-lane brood. 

It is likely enough that, the spring of Darkness, we were made for evil, which, have 
proved more important to the Farmer, have his view, had everything before us, when 
that sufferer was the age of its being received, rude carts, because he had recently 
attained her Christian pastors, snuffed about by poultry, because he had recently 
attained her sister of incredulity, it was the Cock-lane ghost had been laid only. 

Mrs Southcott had everything before us, it, we were all going direct the age of 
times, she entertained herself, already marked by poultry, besides, had nothing 
before us, less favoured on the spirits of some tillers of the woods of wisdom, with 
a plain face, as the State preserves of foolishness, with rustic mire, to come to 
matters spiritual than any communications yet received, from a king with rustic mire, 
besides, to do honour to come to a congress of monks which the heavy lands adjacent 
to the season of this. 

Under the weather that some of its being received, we had lately come down in the 
guidance of British subjects in America: which the Cock-lane brood. 

*************************************************/