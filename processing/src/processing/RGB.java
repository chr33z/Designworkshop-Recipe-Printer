package processing;


/**
 * Color stored in RGB space. Colors are stored as values from [0 - 1]
 * 
 * @author Christopher Gebhardt
 * @date 04.06.2014
 *
 */
public class RGB {
	
	public float r;
	public float g;
	public float b;
	
	/**
	 * conversion matrix for yuv to rgb.
	 * [conversionMatrix] * (yuv)
	 */
	float[] convMatrix = new float[]{
			1f, 0f, 1.13983f,
			1f, -0.39465f, -0.58060f,
			1f, 2.03211f, -0f
		};
	
	public RGB(float red, float green, float blue){
		r = red;
		g = green;
		b = blue;
	}

	
	public RGB(int red, int green, int blue){
		r = red / 255f;
		g = green / 255f;
		b = blue / 255f;
	}
	
	public RGB(String hexString){
		if(hexString.length() == 6){
			r = Integer.parseInt(hexString.substring(0,2), 16) / 255f;
			g = Integer.parseInt(hexString.substring(2,4), 16) / 255f;
			b = Integer.parseInt(hexString.substring(4), 16) / 255f;
		} else {
			System.out.println("RGB: not a valid hex value "+hexString);
		}
	}
	
	public RGB(YUV yuv){
		r = convMatrix[0] * yuv.y + convMatrix[1] * yuv.u + convMatrix[2] * yuv.v;
		g = convMatrix[3] * yuv.y + convMatrix[4] * yuv.u + convMatrix[5] * yuv.v;
		b = convMatrix[6] * yuv.y + convMatrix[7] * yuv.u + convMatrix[8] * yuv.v;
	}
	
	public void set(String hexString){
		if(hexString.length() == 6){
			r = Integer.parseInt(hexString.substring(0,2), 16) / 255f;
			g = Integer.parseInt(hexString.substring(2,4), 16) / 255f;
			b = Integer.parseInt(hexString.substring(4), 16) / 255f;
		} else {
			System.out.println("RGB: not a valid hex value");
		}
	}
	
	public String toString(){
		return "["+(int)(r * 255)+","+(int)(g * 255)+","+(int)(b * 255)+"]";
	}
}