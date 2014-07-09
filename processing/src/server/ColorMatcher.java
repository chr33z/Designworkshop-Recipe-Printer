package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class ColorMatcher {

	private static final int QUEUE_LENGTH = 10;

	private static final long MAX_TIME = 1000L;

	private static final boolean SCANNING_MODE = false;

	private long timeLastMatch = 0;

	boolean started = false;

	/** when adding colors to queue, interpolate color if distance over this value */
	private float maxColorDistance = 0.1f;
	public float matchDistance = 0.06f;

	/** color buffer that hold the last detected colors */
	private LinkedList<YUV> colors = new LinkedList<YUV>();

	ColorMatchListener listener;
	
	// fields for scanning the neutral color
	private long scanStart = 0;
	private YUV neutral = null;
	private YUV averagedColor = null;
	private List<YUV> scannedColors = new ArrayList<YUV>();
	private float scanDistance = 0.1f;

	public interface ColorMatchListener {

		public void onColorMatch(List<YUV> colors);

	}

	public static HashMap<YUV, String> colorMap;

	static {
		colorMap = new HashMap<YUV, String>();
		colorMap.put(new YUV(180, 58, 50), "meat");
		colorMap.put(new YUV(75, 113, 62), "vegetables");
		colorMap.put(new YUV(125, 87, 35), "carbs");
		colorMap.put(new YUV(63, 127, 52), "light-green");
	}


	private YUV previouslyMatched = null;

	/** contains all matched colors including neutral values */
	private List<YUV> matched = new ArrayList<YUV>();
	
	/** includes all matched colors without neutral values */
	private List<YUV> matchedStripped = new ArrayList<YUV>();

	public ColorMatcher(ColorMatchListener listener){
		this.listener = listener;
	}

	/**
	 * Match a color with a color from the color table {@link #colorMap}.
	 * If a match is detected (for the first time), the interface function {@link #ColorMatchListener)}
	 * is called
	 * 
	 * @param color
	 */
	public void match(YUV color){
		if(SCANNING_MODE){
			scanColor(color);
		} else {
			// detect neutral color first
			if(neutral == null){
				findNeutralColor(color);
				return;
			}
			
			// if neutral is found then proceed with matching colors
			addColorToQueue(color);

			YUV bestMatch = null;

			for (Entry<YUV, String> entry : colorMap.entrySet()) {
				if(color.distanceTo(entry.getKey()) < matchDistance){
					if(bestMatch == null){
						bestMatch = entry.getKey();
					} else {
						if(color.distanceTo(entry.getKey()) < 
								color.distanceTo(bestMatch)){
							bestMatch = entry.getKey();
						}
					}		
				}
			}

			if(bestMatch != null && bestMatch != previouslyMatched){
				if(timeLastMatch == 0 || System.currentTimeMillis() - timeLastMatch < MAX_TIME){
					previouslyMatched = bestMatch;
					timeLastMatch = System.currentTimeMillis();
					matched.add(bestMatch);
				}
			}
		}
	}

	private void scanColor(YUV color) {
		if(scanStart == 0){
			scanStart = System.currentTimeMillis();
		}

		// find neutral color
		if(System.currentTimeMillis() - scanStart < 3000){
			if(neutral == null){
				neutral = color.copy();
			} else {
				neutral = color.average(neutral);
			}
		} else {
			if(color.distanceTo(neutral) > scanDistance){
				scannedColors.add(color.copy());

				float y = 0;
				float u = 0;
				float v = 0;
				int n = 0;;
				for (YUV c : scannedColors) {
					y += c.y;
					u += c.u;
					v += c.v;
					n++;
				}

				averagedColor = new YUV(y / (float)n, u / (float)n, v / (float)n, true);

				System.out.println("Scanned: " +color.getRGB().toString());
				System.out.println("Averaged: " +averagedColor.getRGB().toString());
			}
		}
	}

	private void findNeutralColor(YUV color){
		if(scanStart == 0){
			scanStart = System.currentTimeMillis();
		}

		// find neutral color
		if(System.currentTimeMillis() - scanStart < 3000){
			if(neutral == null){
				neutral = color.copy();
			} else {
				neutral = color.average(neutral);
			}
		} else {
			colorMap.put(neutral, "neutral");
		}
	}

	public void checkMatchedColors(){
		if(matched.size() > 0 && System.currentTimeMillis() - timeLastMatch > MAX_TIME){
			matchedStripped.clear();
			
			// remove all neutral colors from matched list
			for (YUV matchedColor : matched) {
				if(!matchedColor.equals(neutral)){
					matchedStripped.add(matchedColor);
				}
			}
			
			listener.onColorMatch(matchedStripped);
			matched.clear();
			timeLastMatch = 0;
			previouslyMatched = null;
		}
	}

	public void addColorToQueue(YUV color){
		YUV averagedColor = null;

		// interpolate color if distance is too
		if(colors.size() > 0){
			if(color.distanceTo(colors.peek()) > maxColorDistance){
				averagedColor = color.average(colors.peek());
				System.out.println("averaged color");
			}
		}

		if(colors.size() < QUEUE_LENGTH){
			if(averagedColor != null){
				colors.push(averagedColor);
			} else {
				colors.push(color);
			}
		} else {
			colors.remove(colors.getLast());
			if(averagedColor != null){
				colors.push(averagedColor);
			} else {
				colors.push(color);
			}
		}
	}
}
