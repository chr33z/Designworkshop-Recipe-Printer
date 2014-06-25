package processing;

/**
 * Color stored in YUV space.
 * 
 * Colors are stored as values from:
 * y -> [0 - 1]
 * u -> [-0.5 - 0.5]
 * v -> [-0.5 - 0.5]
 * 
 * @author Christopher Gebhardt
 * @date 04.06.2014
 *
 */
public class YUV {

	public float y = 0;
	public float u = 0;
	public float v = 0;

	/**
	 * conversion matrix for rgb to yuv.
	 * [conversionMatrix] * (rgb)
	 */
	float[] convMatrix = new float[]{
			0.299f, 0.587f, 0.114f,
			-0.14713f, -0.28886f, 0.436f,
			0.615f, -0.51499f, -0.10001f
	};

	public YUV(RGB rgb){
		y = convMatrix[0] * rgb.r + convMatrix[1] * rgb.g + convMatrix[2] * rgb.b;
		u = convMatrix[3] * rgb.r + convMatrix[4] * rgb.g + convMatrix[5] * rgb.b;
		v = convMatrix[6] * rgb.r + convMatrix[7] * rgb.g + convMatrix[8] * rgb.b;
	}
	
	public YUV(float y, float u, float v, boolean isYuv){
		this.y = y;
		this.u = u;
		this.v = v;
	}

	public YUV(String hexString){
		if(hexString.length() == 6){
			float r = Integer.parseInt(hexString.substring(0,2), 16) / 255f;
			float g = Integer.parseInt(hexString.substring(2,4), 16) / 255f;
			float b = Integer.parseInt(hexString.substring(4), 16) / 255f;

			y = convMatrix[0] * r + convMatrix[1] * g + convMatrix[2] * b;
			u = convMatrix[3] * r + convMatrix[4] * g + convMatrix[5] * b;
			v = convMatrix[6] * r + convMatrix[7] * g + convMatrix[8] * b;
		} else {
			System.out.println("RGB: not a valid hex value");
		}
	}

	public YUV(int red, int green, int blue){
		float r = (float)red / 255f;
		float g = (float)green / 255f;
		float b = (float)blue / 255f;

		y = convMatrix[0] * r + convMatrix[1] * g + convMatrix[2] * b;
		u = convMatrix[3] * r + convMatrix[4] * g + convMatrix[5] * b;
		v = convMatrix[6] * r + convMatrix[7] * g + convMatrix[8] * b;
	}

	public YUV(float red, float green, float blue){
		y = convMatrix[0] * red + convMatrix[1] * green + convMatrix[2] * blue;
		u = convMatrix[3] * red + convMatrix[4] * green + convMatrix[5] * blue;
		v = convMatrix[6] * red + convMatrix[7] * green + convMatrix[8] * blue;
	}

	/**
	 * calculate distance between two colors
	 * @param yuv
	 * @return
	 */
	public float distanceTo(YUV yuv){
//		return (float)Math.sqrt((u-yuv.u)*(u-yuv.u) + (v-yuv.v)*(v-yuv.v));
		return (float)Math.sqrt((u-yuv.u)*(u-yuv.u) + (v-yuv.v)*(v-yuv.v) + (y-yuv.y)*(y-yuv.y));
	}
	
	/**
	 * Interpolate this color with another color
	 * @param yuv
	 * @return
	 */
	public YUV average(YUV yuv){
		float ny = (y + yuv.y) / 2f;
		float nu = (u + yuv.u) / 2f;
		float nv = (v + yuv.v) / 2f;
		
		return new YUV(ny, nu, nv, true);
	}

	public void set(RGB rgb){
		y = convMatrix[0] * rgb.r + convMatrix[1] * rgb.g + convMatrix[2] * rgb.b;
		u = convMatrix[3] * rgb.r + convMatrix[4] * rgb.g + convMatrix[5] * rgb.b;
		v = convMatrix[6] * rgb.r + convMatrix[7] * rgb.g + convMatrix[8] * rgb.b;
	}
	
	public void set(float y, float u, float v, boolean isYuv){
		this.y = y;
		this.u = u;
		this.v = v;
	}
	
	public RGB getRGB(){
		return new RGB(this);
	}

	public boolean equals(YUV yuv){
		if(yuv == null){
			return false;
		}

		if(y == yuv.y && u == yuv.u && v == yuv.v){
			return true;
		} else {
			return false;
		}
	}

	public String toString(){
		return "["+y+","+u+","+v+"]";
	}
}