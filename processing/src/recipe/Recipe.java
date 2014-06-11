package recipe;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;

public class Recipe {
	
	private static File recipeDirectory = new File(new File(System.getProperty("user.dir")).getParentFile()+"/src/data");
	
	private static final Long TIME_MARGIN = 0L;
	
	public static File pickRecipe(String[] tags, Long prepTime){
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
			
			// check if preparation time is ok
			if(prepTime < recipeDate.prepTime - TIME_MARGIN){
				continue;
			}
			
			recipes.add(file);
		}
		
		for (File file : recipes) {
			System.out.println("selected recipes: "+file);
		}
		
		return null;
	}

	/**
	 * Parse a recipe xml file and store the main information in a {@link #recipe.Recipe.RecipeData} object
	 * 
	 * @param file
	 * @return a {@link #recipe.Recipe.RecipeData} object or null
	 */
	public static RecipeData parseRecipeData(File file){
		try {
			System.out.println("Parsing recipe file "+file+"...");

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
			
			System.out.println("...done");
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
