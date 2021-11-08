import java.awt.image.BufferedImage;
import java.lang.Math;

public class GroupShape {
	public static int[][] getShape(int x, int y, int points, BufferedImage image, int threshold) {
		int[] p0 = Colours.RGBToYCbCr(Colours.split(image.getRGB(x, y)));
		
		int[][] shape = new int[points][2];
		
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2 * i / (double)points;
			
			int len = 1;
			double[] vec = {Math.cos(angle), Math.sin(angle)};
			int[] point = new int[2];
			int[] p1;
			
			do {
				point[0] = (int)(x + len * vec[0]);
				point[1] = (int)(y + len * vec[1]);
				
				p1 = Colours.RGBToYCbCr(Colours.split(image.getRGB(point[0], point[1])));
				
				len++;
			} while (point[0] >= 0 && point[0] < image.getWidth() && point[1] >= 0 && point[1] < image.getHeight() && Colours.compareColours(p0, p1, threshold));
		}
		
		return shape;
	}
}