public class Colours {
	public static boolean compareColours(int[] c, int[] min, int[] max) {
		return c[0] > min[0] && c[1] > min[1] && c[2] > min[2] &&
				c[0] < max[0] && c[1] < max[1] && c[2] < max[2];
	}
	
	public static int[] RGBToYCbCr(int[] c) {
		int[] newColour = {(int)(      0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]),
                           (int)(128 - 0.169 * c[0] - 0.331 * c[1] + 0.500 * c[2]),
                           (int)(128 + 0.500 * c[0] - 0.419 * c[1] - 0.081 * c[2])};
		
		return newColour;
	}
	
	public static int[] split(int c) {
		int[] colour = {(c & 0xff0000) >> 16,
		                (c & 0x00ff00) >> 8,
						(c & 0x0000ff)};
		
		return colour;
	}
}