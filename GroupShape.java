import java.awt.image.BufferedImage;
import java.lang.Math;

public class GroupShape {
	public static double[][] getShape(int x, int y, int points, BufferedImage image, int[] minColor, int[] maxColor, int maxError) {
		int[] p0 = Colours.RGBToYCbCr(Colours.split(image.getRGB(x, y)));
		
		double[][] shape = new double[2][points];
		
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2.0 * (double)i / (double)points;
			
			int len = 1;
			double[] vec = {Math.cos(angle), Math.sin(angle)};
			int[] point = {(int)(x + len * vec[0]), (int)(y + len * vec[1])};
			int[] p1 = Colours.RGBToYCbCr(Colours.split(image.getRGB(point[0], point[1])));
			int error = 0;
			
			while (point[0] >= 0 && point[0] < image.getWidth() &&
					point[1] >= 0 && point[1] < image.getHeight() &&
					error < maxError) {
				if (!Colours.compareColours(p1, minColor, maxColor)) error++;
				else error = 0;

				p1 = Colours.RGBToYCbCr(Colours.split(image.getRGB(point[0], point[1])));

				len++;

				point[0] = (int)(x + len * vec[0]);
				point[1] = (int)(y + len * vec[1]);
			}

			shape[0][i] = point[0];
			shape[1][i] = point[1];
		}
		
		return shape;
	}
}
