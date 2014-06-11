package processing;

import java.util.HashMap;
import java.util.Map.Entry;

public class ColorMatcher {
	
	ColorMatchListener listener;
	
	public interface ColorMatchListener {
		
		public void onColorMatch(YUV color);
		
	}

	public static HashMap<YUV, String> colorMap;
	
	static {
		colorMap = new HashMap<YUV, String>();
		colorMap.put(new YUV(38, 103, 101), "blue");
		colorMap.put(new YUV(66, 84, 103), "darkblue");
		colorMap.put(new YUV(62, 121, 57), "green");
		colorMap.put(new YUV(124, 88, 31), "yellow");
		colorMap.put(new YUV(163, 60, 34), "orange");
		colorMap.put(new YUV(172, 51, 46), "red");
		colorMap.put(new YUV(145, 42, 74), "pink");
		colorMap.put(new YUV(113, 67, 82), "violett");
	}
	
	public float matchDistance = 0.02f;
	
	private YUV previouslyMatched = null;
	
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
			previouslyMatched = bestMatch;
			listener.onColorMatch(bestMatch);
		}
	}
}
