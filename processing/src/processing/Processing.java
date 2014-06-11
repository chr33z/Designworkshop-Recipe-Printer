package processing;

import java.io.File;
import processing.ColorMatcher.ColorMatchListener;
import processing.core.PApplet;
import processing.serial.*;
import recipe.Recipe;

public class Processing extends PApplet implements ColorMatchListener {

	private static final boolean DRAW_COLOR_WINDOW = false;

	private Serial port;

	private String buff = "";

	private int wRed, wGreen, wBlue, wClear;
	private String hexColor = "ffffff";

	/** rgb color for debugging */
	private RGB background = new RGB(0,0,0);

	private YUV detectedColor = new YUV(new RGB(0,0,0));

	private ColorMatcher matcher;

	private String matchDistanceInput = "0.05";

	private File recipeDirectory = new File(new File(System.getProperty("user.dir")).getParentFile()+"/src/data");

	public static void main(String _args[]) {
		PApplet.main(new String[] { processing.Processing.class.getName() });
	}

	/**
	 * programm setup
	 */
	public void setup() {
		matcher = new ColorMatcher(this);
		matchDistanceInput = matcher.matchDistance + "";

		Recipe.pickRecipe(new String[]{"meat"}, 1200000L);

		//		size(200,200);
		//		port = new Serial(this, "/dev/ttyACM1", 9600); //remember to replace COM20 with the appropriate serial port on your computer
	}

	/**
	 * Main loop
	 */
	public void draw() {
		if(DRAW_COLOR_WINDOW){
			background((background.r * 255), (background.g * 255), (background.b * 255));
			text(matchDistanceInput, 4, 180);

			while (port.available() > 0) {
				serialEvent(port.readChar());
			}
		}
	}

	void serialEvent(char serial) {
		if(serial == '\n') {
			if(buff.length() == 6){
				background.set(buff);
				detectedColor.set(background);
				matcher.match(detectedColor);
			}
			buff = "";
		} else {
			buff += serial;
		}
	}

	@Override
	public void onColorMatch(YUV color) {
		System.out.println("Detected color " + ColorMatcher.colorMap.get(color));
	}

	public void keyReleased() {
		if (key != CODED) {
			switch(key) {
			case BACKSPACE:
				matchDistanceInput = matchDistanceInput.substring(0,max(0,matchDistanceInput.length()-1));
				break;
			case ENTER:
			case RETURN:
				// comment out the following two lines to disable line-breaks
				try{
					matcher.matchDistance = Float.parseFloat(matchDistanceInput);
				} catch(NumberFormatException e){

				}
				break;
			case ESC:
			case DELETE:
				break;
			default:
				matchDistanceInput += key;
			}
		}
	}
}
