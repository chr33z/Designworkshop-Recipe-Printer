package server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.serial.*;
import recipe.Recipe;
import server.ColorMatcher.ColorMatchListener;

public class Processing extends PApplet implements ColorMatchListener {

	private static final long serialVersionUID = -5092376021673312150L;

	public static enum Status {
		NOT_CONNECTED, IDLE, READING
	}

	int screenDimension = 200;

	private Serial port;
	private String[] portList;
	private boolean serialConnected = false;

	/** rgb color for debugging */
	private RGB background = new RGB(0,0,0);

	private YUV detectedColor = new YUV(new RGB(0,0,0));

	private ColorMatcher matcher;

	private String matchDistanceInput = "0.1";
	
	/** time when the last parsable input was received from the serial */
	private long timeLastReceived = 0;
	
	private boolean connectionLost = false;

	/** static path to data directory where all the recipes are stored */
	private static File recipeDirectory = new File(new File(System.getProperty("user.dir")).getParentFile()+"/src/data");
	
	public static void main(String args[]) {
		   PApplet.main(new String[] { "--present", "Processing" });
		} 

	/**
	 * programm setup
	 */
	public void setup() {
		port = findSerialPort();

		matcher = new ColorMatcher(this);
		matchDistanceInput = matcher.matchDistance + "";

		size(200, 200);
	}

	/**
	 * Main loop
	 */
	public void draw() {
		if(serialConnected){
			background((background.r * 255), (background.g * 255), (background.b * 255));
			text(matchDistanceInput, 4, 180);

			while (port != null && port.available() > 0) {
				serialEvent(port.readStringUntil('\n'));
			}
			
			if(timeLastReceived != 0 && System.currentTimeMillis() - timeLastReceived > 10000L){
				if(!connectionLost){
					System.out.println("Connection lost.");
					connectionLost = true;
				}
//				resetConnection();
			}
			
			// periodically check if colors are matched
			matcher.checkMatchedColors();
		}
	}

	void serialEvent(String input) {
		if(input != null && !input.equals("")) {
			input = input.replace("\n", "");
			
			if(input.length() == 6){
				background.set(input);
				detectedColor.set(background);
				matcher.match(detectedColor);
				
				// store last received time to determine timeout
				timeLastReceived = System.currentTimeMillis();
			}
		}
	}

	@Override
	public void onColorMatch(List<YUV> colors) {
		String[] col = new String[colors.size()];
		for (int i = 0; i < col.length; i++) {
			col[i] = ColorMatcher.colorMap.get(colors.get(i));
		}

		for (YUV c : colors) {
			System.out.println(ColorMatcher.colorMap.get(c).toUpperCase());
		}

		File file = Recipe.pickRecipe(col, 1200000L, false);
		if(file == null){
			file = Recipe.pickRecipe(col, 1200000L, true);
		}
		
		System.out.println(file);
		
		Recipe.printRecipe(port, file);
	}

	/*
	 * Some keyboard bindings for testing
	 * 
	 * (non-Javadoc)
	 * @see processing.core.PApplet#keyReleased()
	 */
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
			case TAB:
				//				port.write("PRINT_LINE\n");
				//				port.write("Das ist ein Teststring\n");
//				/port.write("PRINT_LINE#TEST");
				System.out.println(background.toString());
				break;
			case ESC:
			case DELETE:

				break;
			default:
				matchDistanceInput += key;
			}
		}
	}

	private Serial findSerialPort(){
		// save serial ports
		portList = Serial.list();
		
		String input = "";
		Serial currentPort = null;
		if(Serial.list().length > 0 && !serialConnected){
			while(!serialConnected){
				for (int i = 0; i < portList.length; i++) {
					try {
						currentPort = new Serial(this, portList[i], 9600);
					} catch(RuntimeException e){
						// could not connect to serial
						continue;
					}
					
					if(currentPort.available() > 0){
						System.out.println("Checking serial input...");
						
						input = currentPort.readStringUntil('\n');
						if(input != null && !input.equals("")){
							serialConnected = true;
							System.out.println("Connected to serial: " + portList[i]);
							return currentPort;
						}
					}
				}
				
				try {
					portList = Serial.list();
				} catch(NullPointerException e){
					// just skip this part for now
				}
				
				try {
					Thread.sleep(200L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.err.println("Error: Did not find any serial ports.");
		return null;
	}
	
	private void resetConnection(){
		port = null;
		serialConnected = false;
		timeLastReceived = 0;
		
		Thread connectThread = new Thread(new Runnable() {
			@Override
			public void run() {
				port = findSerialPort();
			}
		});
		connectThread.start();
		
//		try {
//			restartApplication();
//		} catch (URISyntaxException | IOException e) {
//			e.printStackTrace();
//		}
		
//		port = findSerialPort();
		/*
		 * FIXME find the serial port again. Blocks thread, so no trivial task
		 */
		
//		System.err.println("Connect arduino and restart the app.");
	}
	
	public void restartApplication() throws URISyntaxException, IOException
	{
	  final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	  final File currentJar = new File(Processing.class.getProtectionDomain().getCodeSource().getLocation().toURI());

	  /* is it a jar file? */
	  if(!currentJar.getName().endsWith(".jar"))
	    return;

	  /* Build command: java -jar application.jar */
	  final ArrayList<String> command = new ArrayList<String>();
	  command.add(javaBin);
	  command.add("-jar");
	  command.add(currentJar.getPath());

	  final ProcessBuilder builder = new ProcessBuilder(command);
	  builder.start();
	  System.exit(0);
	}
}
