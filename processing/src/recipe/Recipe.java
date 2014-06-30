package recipe;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import processing.serial.Serial;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;

public class Recipe {

	private static File recipeDirectory = new File(new File(System.getProperty("user.dir")).getParentFile()+"/src/data");

	private static final Long TIME_MARGIN = 0L;
	
	private static final boolean PRINT_MODE = false;

	private static final String DEL = "#";

	public static File pickRecipe(String[] tags, Long prepTime, boolean dontBePicky){
		String[] recipeFiles = recipeDirectory.list(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.endsWith(".xml");
			}
		});

		// all recipes that fit the constraints
		List<File> recipes = new ArrayList<File>();

		fileLoop:
			for (String fileName : recipeFiles) {
				File file = new File(recipeDirectory, fileName);
				RecipeData recipeDate = parseRecipeData(file);

				// check if all tags are there
				for (String tag : tags) {
					if(!Arrays.asList(recipeDate.tags).contains(tag)){
						continue fileLoop; // if tag is not in recipe than continue with next
					}
				}

				// simple throw out
				if(tags.length != recipeDate.tags.length && !dontBePicky){
					continue;
				}

				// check if preparation time is ok
//				if(prepTime < recipeDate.prepTime - TIME_MARGIN){
//					continue;
//				}
				recipes.add(file);
			}
		try {
			int randomRecipeIndex = new Random().nextInt(recipes.size());
			System.out.println("Random index: " + randomRecipeIndex);
			return recipes.get(randomRecipeIndex);
		} catch(IndexOutOfBoundsException e){
			return null;
		} catch(IllegalArgumentException e){
			return null;
		}
	}

	/**
	 * Parse a recipe xml file and store the main information in a {@link #recipe.Recipe.RecipeData} object
	 * 
	 * @param file
	 * @return a {@link #recipe.Recipe.RecipeData} object or null
	 */
	public static RecipeData parseRecipeData(File file){
		try {
			//			System.out.println("Parsing recipe file "+file+"...");

			Builder parser = new Builder();
			Document doc = parser.build(file);

			Nodes tagNodes = doc.query("//recipeinfo/tag");
			String[] tags = new String[tagNodes.size()];
			for (int i = 0; i < tagNodes.size(); i++) {
				tags[i] = tagNodes.get(i).getValue();
			}

			Nodes preTimeNodes = doc.query("//recipeinfo/preptimeutc");
			Long prepTime = 0L;
			for (int i = 0; i < preTimeNodes.size(); i++) {
				try {
					prepTime = Long.parseLong(preTimeNodes.get(i).getValue());
					break;
				} catch(NumberFormatException e){
					System.err.println("Preparation time is not a valid number. Should be a number describing the milliseconds");
				}
			}

			Nodes authorNodes = doc.query("//recipeinfo/author");
			String author = "";
			for (int i = 0; i < authorNodes.size(); i++) {
				author = authorNodes.get(i).getValue();
				break;
			}

			//			System.out.println("...done");
			return new RecipeData(tags, author, prepTime);
		}
		catch (ParsingException ex) {
			System.err.println("...the file can not be parsed. It may be malformed");
			return null;
		}
		catch (IOException ex) {
			System.err.println("...the file can not be read. Does the file exist?");
			return null;
		}
	}

	public static boolean printRecipe(Serial serial, File file){
		if(file == null){
			System.err.println("Recipe file is null.");
			return false;
		}

		try {
			System.out.println("Parsing and printing recipe file "+file+"...");

			Builder parser = new Builder();
			Document doc = parser.build(file);

			try {
				print(serial, "MODE_PRINT#", 200);
				
				print(serial, "PRINT_FEED#5#", 200);
				// wait for serial to transmit data. Otherwise command is not processed by arduino

				printTitle(serial, doc.query("//title"), 1000L);
				print(serial, "PRINT_FEED#1#", 200);

				printAuthor(serial, doc.query("//recipeinfo/author"), 1000L);
				print(serial, "PRINT_FEED#3#", 200);

				printIngredients(serial, doc.query("//ingredientlist/ingredient"), 1000L);
				print(serial, "PRINT_FEED#3#", 200);

				printPreparation(serial, doc.query("//preparation"), 1000L);

				print(serial, "PRINT_FEED#7#", 200);
				
				print(serial, "MODE_SENSOR#", 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}
		catch (ParsingException ex) {
			System.err.println("...the file can not be parsed. It may be malformed");
			return false;
		}
		catch (IOException ex) {
			System.err.println("...the file can not be read. Does the file exist?");
			return false;
		}
	}


	private static void printTitle(Serial serial, Nodes nodes, long ms) {
		if(nodes != null && nodes.size() > 0){
			System.out.println(nodes.get(0).getValue());
			print(serial, "PRINT_LINE" + DEL + nodes.get(0).getValue() + DEL, ms);
		} else {
			print(serial, "PRINT_LINE" + DEL + "Unknown Meal" + DEL, ms);
		}
	}

	private static void printAuthor(Serial serial, Nodes nodes, long ms) {
		if(nodes != null && nodes.size() > 0){
			System.out.println(nodes.get(0).getValue());
			print(serial, "PRINT_LINE" + DEL + "by "+nodes.get(0).getValue() + DEL, ms);
		} else {
			print(serial, "PRINT_LINE" + DEL + "by "+"Unknown Author" + DEL, ms);
		}
	}

	private static void printIngredients(Serial serial, Nodes nodes, long ms) throws InterruptedException {
		if(nodes != null && nodes.size() > 0){
			for (int i = 0; i < nodes.size(); i++) {
				String ingredient = nodes.get(i).getValue();
				ingredient = ingredient.replace("\t", " ");
				ingredient = ingredient.replace("\n", "");
				ingredient = ingredient.trim().replaceAll(" +", " ");

				System.out.println(ingredient);
				print(serial, "PRINT_LINE" + DEL + ingredient + DEL, ms);
			}
		} else {
			print(serial, "PRINT_LINE" + DEL + "Unknown Ingredients" + DEL, ms);
		}
	}

	private static void printPreparation(Serial serial, Nodes nodes, long ms) {
		if(nodes != null && nodes.size() > 0){
			String prepString = nodes.get(0).getValue();
			prepString = prepString.replace("\t", "");
			prepString = prepString.trim().replace("\n", "");
			prepString = prepString.trim().replaceAll(" +", " ");

			System.out.println(prepString);
			print(serial, "PRINT_LINE" + DEL + prepString + DEL, ms);
		} else {
			print(serial, "PRINT_LINE" + DEL + "Unknown Author" + DEL, ms);
		}
	}

	/**
	 * Sends data to the printer, has a conditional for PRINT_MODE and a
	 * parameter that holds the thread for some ms after printing
	 * @param serial
	 * @param string
	 */
	private static void print(Serial serial, String string, long ms){
		if(PRINT_MODE){
			serial.write(string);
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Datatype to store the main infos about a recipe that are necessary 
	 * for the printer to decide on one
	 * 
	 * @author Christopher Gebhardt
	 * @date 11.06.2014
	 *
	 */
	public static class RecipeData {

		private String[] tags;

		private String author;

		private Long prepTime;

		public RecipeData(String[] tags, String author, Long prepTime){
			this.tags = tags;
			this.author = author;
			this.prepTime = prepTime;
		}

		public String[] getTags() {
			return tags;
		}

		public void setTags(String[] tags) {
			this.tags = tags;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public Long getPrepTime() {
			return prepTime;
		}

		public void setPrepTime(Long prepTime) {
			this.prepTime = prepTime;
		}

		public String toString(){
			String tagstrings = "[";
			for (int i = 0; i < tags.length; i++) {
				tagstrings += tags[i];
				if(i < tags.length - 1){
					tagstrings += ",";
				}
			}
			tagstrings += "]";
			return tagstrings+"; "+author+"; "+prepTime;
		}
	}
}
