import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
/*
 * @author Nadira Azim Dewji
 * This class represents a l
 */
public class Linker {
	public static void main(String[] args){
		try{
			File file = new File(args[0]);
			ArrayList<String> finalArray = new ArrayList<String>();
			Scanner sc = new Scanner(file);
			int relocationConstant = 0;
			ArrayList<String> errorsNotDefined = new ArrayList<String>();
			StringBuilder warnings = new StringBuilder();
			StringBuilder outside = new StringBuilder();
			StringBuilder multiple = new StringBuilder();
			HashMap<String, Integer> myHashMapDefinitions = new HashMap<String, Integer>();
			HashMap<String, Integer> myHashMapModule = new HashMap<String, Integer>();
			HashMap<String, Integer> myHashMapUses = new HashMap<String, Integer>();
			HashMap<String, Integer> myHashMapProgramTexts = new HashMap<String, Integer>();
			ArrayList <String> programText = new ArrayList<String>();
			System.out.print("Symbol Table\n");
			int numModules = sc.nextInt();
			for(int j=0; j<numModules; j++) {
				int numDefinitions = sc.nextInt();
				String [] listDef = new String[numDefinitions*2]; 
				for(int i=0; i<numDefinitions; i++) {
					String symbol = sc.next();
					Integer location = sc.nextInt();
//					listDef[i] = symbol;
//					listDef[i+1]=location.toString();
//					System.out.print(listDef[i] +" ");
//					System.out.println(listDef[i+1]);
					location+= relocationConstant;
					//Check to make sure a previous key is not in the hashmap.
					if(myHashMapDefinitions.containsKey(symbol)) {
						multiple.append(" Error: This variable is multiply defined; first value used.\n");
//						System.out.println("Error: This variable is multiply defined; first value used.\n");
					}
					else {
						myHashMapDefinitions.put(symbol, location);
						myHashMapModule.put(symbol, j);	
//						System.out.print(symbol+"=");
//						System.out.println((location+" "));
					}
					
				}
				Integer numberUses = sc.nextInt();
				for(int k=0; k<numberUses; k++) {
					String symbol = sc.next();
					Integer location = sc.nextInt();
					myHashMapUses.put(symbol, location);
							
				}
				Integer numberProgramText = sc.nextInt();
				
				for(String key: myHashMapDefinitions.keySet()) {
					if(myHashMapDefinitions.get(key)>(numberProgramText-1)+relocationConstant) {
						myHashMapDefinitions.put(key, relocationConstant);
						outside.append(" Error: The definition of " + key + " is outside module 1; zero (relative) used");

					}
				}

				relocationConstant+= numberProgramText;
				for(int l=0; l<numberProgramText; l++) {
					Integer text = sc.nextInt();
					programText.add(text.toString());				
				}
			}
			for(String key: myHashMapDefinitions.keySet()) {
				if(myHashMapUses.get(key)==null) {
					warnings.append("Warning: "+ key + " was defined in module "+ myHashMapModule.get(key) + " but never used.\n");
				}
				System.out.println(key+"="+myHashMapDefinitions.get(key).toString().concat(multiple.toString().concat(outside.toString())));
				outside.delete(0,outside.length());
				
			}
			for(String key: myHashMapUses.keySet()) {
				if(myHashMapDefinitions.get(key)==null) {
					myHashMapDefinitions.put(key, 0);
					errorsNotDefined.add(key);
//					System.out.println("Error: "+ key + " is not defined; zero used.");
					
				}
				
			}
			//Beginning the second pass. Iterate over the modules (by module) and then store the relative addresses and the absolute addresses.
			File fileTwo = new File(args[0]);
			Scanner scTwo = new Scanner(fileTwo);
			int rel = 0;
		    relocationConstant = 0;
			Integer locations =0;
			HashMap<Integer, Integer> usesAbs = new HashMap<Integer, Integer>();
			int numModulesTwo = scTwo.nextInt();
			for(int j=0; j<numModulesTwo; j++) {
				HashMap<String, Integer> usesTwo = new HashMap<String, Integer>();
				int numDefinitionsTwo = scTwo.nextInt();
				for(int i=0; i<numDefinitionsTwo; i++) {
					String symbol = scTwo.next();
					locations = scTwo.nextInt()+relocationConstant;
				}
				Integer numberUsesTwo = scTwo.nextInt();
				for(int k=0; k<numberUsesTwo; k++) {
					String symbol = scTwo.next();
					Integer location = scTwo.nextInt();
					usesTwo.put(symbol, location);
					usesAbs.put(location, myHashMapDefinitions.get(symbol));
				}
				Integer numberProgramTextTwo = scTwo.nextInt();
				relocationConstant+=numberProgramTextTwo;
				String[] myArrayTexts = new String[numberProgramTextTwo];
				ArrayList<String> used = new ArrayList<String>();
				ArrayList<String> total = new ArrayList<String>();
				for(int l=0; l<numberProgramTextTwo; l++) {
					String text = scTwo.next();
					myArrayTexts[l] = text;
				}
				//Now iterate through the hashmap of uses and change the relative address to the absoulte address.
				//For every use iterate through the same list and then change the addresses. 
				for(String key: usesTwo.keySet()) {
					StringBuilder immediate = new StringBuilder();
					Integer current = usesTwo.get(key);
					Integer absolute = usesAbs.get(usesTwo.get(key));
					while(current!=777) {
						String s = myArrayTexts[current];
						myArrayTexts[current] = s.substring(0, 1)+String.format("%03d", absolute)+s.substring(4);
						used.add(myArrayTexts[current]);
						if(Integer.parseInt(s.substring(4,5))==1) {
							immediate.append((myArrayTexts[current]));
							immediate.append(" Error: Immediate address on use list; treated as External.");
							myArrayTexts[current] = immediate.toString();
							immediate.delete(0,myArrayTexts[current].length());
						}
//						used.add(myArrayTexts[current]);
						//check if the key has an error
						if(errorsNotDefined.contains(key)) {
//							System.out.println(myArrayTexts[current]);
							StringBuilder myStringBuilder = new StringBuilder();
							myStringBuilder.append((myArrayTexts[current]));
							myStringBuilder.append(" Error: "+ key + " is not defined; zero used.");
//							System.out.println(myStringBuilder.toString());
							myArrayTexts[current] = myStringBuilder.toString();
//							System.out.println(myArrayTexts[current]);

						}
						Integer next = Integer.parseInt(s.substring(1, 4));
						used.add(s);
						current = next;
					}
				}
			

				for(String myString: myArrayTexts) {
					if(Integer.parseInt(myString.substring(4,5))==3) {
						Integer abs = Integer.parseInt(myString.substring(0,4)) +rel;
						finalArray.add(abs.toString().concat(myString.substring(5)));
					}else {
//						System.out.println(myString.substring(0, 4));
						if(Integer.parseInt(myString.substring(4,5))==4) {
							if(!used.contains(myString.substring(0,5))) {
								myString = myString.concat(" Error: E type address not on use chain; treated as I type.");
							}
						}
						finalArray.add(myString.toString().substring(0,4).concat(myString.substring(5)));

					}
				}
				rel+=numberProgramTextTwo;
			}
			System.out.println();
			System.out.println("Memory Map");
			for(int i=0; i<finalArray.size(); i++) {
				System.out.printf("%3s:    ", i);
				System.out.println(finalArray.get(i));
			}
			System.out.println();
			System.out.println(warnings.toString());
			sc.close();
			scTwo.close();

		}
		catch(Exception E) {
			System.err.println(E.toString());
			E.printStackTrace();
		}

		
		
	}

}