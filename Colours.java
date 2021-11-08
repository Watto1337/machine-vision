import java.lang.Math;

public class Colours {
	public static boolean compareColours(int[] c1, int[] c2, int threshold) {
		return Math.abs(c1[1] - c2[1]) < threshold && Math.abs(c1[2] - c2[2]) < threshold;
	}
	
	public static int[] RGBToYCbCr(int[] c) {
		int[] newColour = {(int)(      0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]),
                           (int)(128 - 0.169 * c[0] - 0.331 * c[1] + 0.500 * c[2]),
                           (int)(128 + 0.500 * c[0] - 0.419 * c[1] - 0.081 * c[2])};
		
		return newColour;
	}
	
	public static int[] split(int c) {
		int[] colour = {(c & 0x0000ff),
		                (c & 0x00ff00) >> 8,
						(c & 0xff0000) >> 16};
		
		return colour;
	}
}