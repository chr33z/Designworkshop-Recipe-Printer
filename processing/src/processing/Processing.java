package processing;

import processing.core.PApplet;
import processing.serial.*;

public class Processing extends PApplet {

	private Serial port;

	private String buff = "";

	private int wRed, wGreen, wBlue, wClear;
	private String hexColor = "ffffff";
	
	private RGB background = new RGB(0,0,0);
	
	YUV yuvTest = new YUV(new RGB(0,0,0));
	
	YUV yuvblue = new YUV(38, 103, 101);
	YUV yuvgreen = new YUV(62, 121, 57);
	YUV yuvorange = new YUV(160, 60, 34);
	
	YUV detected = null;
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { processing.Processing.class.getName() });
	}

	/**
	 * programm setup
	 */
	public void setup() {
		size(200,200);
		port = new Serial(this, "/dev/ttyACM2", 9600); //remember to replace COM20 with the appropriate serial port on your computer
	}

	/**
	 * Main loop
	 */
	public void draw() {
		background((background.r * 255), (background.g * 255), (background.b * 255));
		while (port.available() > 0) {
			serialEvent(port.readChar());
		}
	}

	void serialEvent(char serial) {
		if(serial == '\n') {
			if(buff.length() == 6){
				background.set(buff);
				yuvTest.set(background);
				
				if(yuvTest.distanceTo(yuvblue) < 0.05 && !yuvblue.equals(detected)){
					System.out.println("blue detected");
					detected = yuvblue;
				} else if(yuvTest.distanceTo(yuvgreen) < 0.05 && !yuvgreen.equals(detected)){
					System.out.println("green detected");
					detected = yuvgreen;
				} else if(yuvTest.distanceTo(yuvorange) < 0.05 && !yuvorange.equals(detected)){
					System.out.println("orange detected");
					detected = yuvorange;
				}
			}
			buff = "";
		} else {
			buff += serial;
//			System.out.println(buff);
		}
	}
}
