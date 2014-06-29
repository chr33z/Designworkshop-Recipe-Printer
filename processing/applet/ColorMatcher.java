package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class ColorMatcher {

	private static final int QUEUE_LENGTH = 10;

	private static final long MAX_TIME = 1000L;

	private long timeLastMatch = 0;

	/** when adding colors to queue, interpolate color if distance over this value */
	private float maxColorDistance = 0.7f;

	/** color buffer that hold the last detected colors */
	private LinkedList<YUV> colors = new LinkedList<YUV>();

	ColorMatchListener listener;

	public interface ColorMatchListener {

		public void onColorMatch(List<YUV> colors);

	}

	public static HashMap<YUV, String> colorMap;

	static {
		colorMap = new HashMap<YUV, String>();
		colorMap.put(new YUV(154, 57, 43), "meat");
		colorMap.put(new YUV(50, 131, 50), "vegetables");
		colorMap.put(new YUV(53, 89, 97), "carbs");
		//		colorMap.put(new YUV(81, 115, 53), "green");
	}

	public float matchDistance = 0.1f;

	private YUV previouslyMatched = null;

	private List<YUV> matched = new ArrayList<YUV>();

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

		//		if(bestMatch != null){
		//			previouslyMatched = bestMatch;
		//			listener.onColorMatch(bestMatch);
		//		}
		
		if(bestMatch != null && bestMatch != previouslyMatched){
			if(timeLastMatch == 0 || System.currentTimeMillis() - timeLastMatch < MAX_TIME){
				previouslyMatched = bestMatch;
				timeLastMatch = System.currentTimeMillis();
				matched.add(bestMatch);
			}
		}
	}
	
	public void checkMatchedColors(){
		if(matched.size() > 0 && System.currentTimeMillis() - timeLastMatch > MAX_TIME){
			listener.onColorMatch(matched);
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
